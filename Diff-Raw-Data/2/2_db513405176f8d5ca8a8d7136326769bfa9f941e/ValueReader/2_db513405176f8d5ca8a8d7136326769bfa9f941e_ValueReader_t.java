 /*
  *  Licensed to the Apache Software Foundation (ASF) under one or more
  *  contributor license agreements.  See the NOTICE file distributed with
  *  this work for additional information regarding copyright ownership.
  *  The ASF licenses this file to You under the Apache License, Version 2.0
  *  (the "License"); you may not use this file except in compliance with
  *  the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package org.apache.yoko.orb.OB;
 
 import java.io.Serializable;
 import java.lang.reflect.Array;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.rmi.CORBA.ValueHandler;
 
 import org.omg.CORBA.TCKind;
 import org.omg.CORBA.WStringValueHelper;
 import org.omg.CORBA.portable.IndirectionException;
 import org.omg.CORBA.portable.StreamableValue;
 import org.omg.SendingContext.CodeBase;
 
 final public class ValueReader {
     static final Logger logger = Logger.getLogger(ValueReader.class.getName());
     //
     // Chunk data
     //
     static private class ChunkState {
         boolean chunked;
 
         int nestingLevel;
 
         int chunkStart;
 
         int chunkSize;
 
         ChunkState() {
         }
 
         ChunkState(ChunkState s) {
             copyFrom(s);
         }
 
         void copyFrom(ChunkState s) {
             chunked = s.chunked;
             nestingLevel = s.nestingLevel;
             chunkStart = s.chunkStart;
             chunkSize = s.chunkSize;
         }
     }
 
     //
     // Valuetype header data
     //
     static private class Header {
         int tag;
 
         int headerPos;
 
         int dataPos;
 
         String[] ids;
 
         ChunkState state;
 
         Header next; // Java only
         String codebase; // Java only
 
         Header() {
             ids = new String[0];
             state = new ChunkState();
         }
         
         boolean isRMIValue () {
             return ids != null
                 && ids.length > 0
                 && ids[0].startsWith ("RMI:");
         }
 
     }
 
     private org.apache.yoko.orb.OB.ORBInstance orbInstance_;
 
     private org.apache.yoko.orb.CORBA.InputStream in_;
 
     private org.apache.yoko.orb.OCI.Buffer buf_;
 
     private java.util.Hashtable instanceTable_;
 
     private java.util.Hashtable headerTable_;
 
     private java.util.Hashtable positionTable_ = null;
 
     private ChunkState chunkState_ = new ChunkState();
 
     private Header currentHeader_ = null; // Java only
 	private ValueHandler valueHandler;
 	private org.omg.SendingContext.CodeBase remoteCodeBase;
 
     // ------------------------------------------------------------------
     // Valuetype creation strategies
     // ------------------------------------------------------------------
 
     private static abstract class CreationStrategy {
         protected ValueReader reader_;
 
         protected org.apache.yoko.orb.CORBA.InputStream is_;
 
         CreationStrategy(ValueReader reader,
                 org.apache.yoko.orb.CORBA.InputStream is) {
             reader_ = reader;
             is_ = is;
         }
 
         abstract java.io.Serializable create(Header h);
     }
 
     //
     // Create a valuebox using a BoxedValueHelper
     //
     private static class BoxCreationStrategy extends CreationStrategy {
         private org.omg.CORBA.portable.BoxedValueHelper helper_;
 
         BoxCreationStrategy(ValueReader reader,
                 org.apache.yoko.orb.CORBA.InputStream is,
                 org.omg.CORBA.portable.BoxedValueHelper helper) {
             super(reader, is);
             helper_ = helper;
         }
 
         java.io.Serializable create(Header h) {
             Assert._OB_assert(h.tag >= 0x7fffff00 && h.tag != -1);
 
             java.io.Serializable result = helper_.read_value(is_);
 
             if (result != null) {
                 reader_.addInstance(h.headerPos, result);
                 return result;
             }
 
             throw new org.omg.CORBA.MARSHAL(org.apache.yoko.orb.OB.MinorCodes
                     .describeMarshal(org.apache.yoko.orb.OB.MinorCodes.MinorNoValueFactory)
                     + ": " + helper_.get_id(), org.apache.yoko.orb.OB.MinorCodes.MinorNoValueFactory,
                     org.omg.CORBA.CompletionStatus.COMPLETED_NO);
         }
     }
 
     //
     // Create a value using a class
     //
     private static class ClassCreationStrategy extends CreationStrategy {
         private Class clz_;
 
         ClassCreationStrategy(ValueReader reader,
                 org.apache.yoko.orb.CORBA.InputStream is, Class clz) {
             super(reader, is);
             clz_ = clz;
         }
 
         java.io.Serializable create(Header h) {
             logger.fine("Creating a value object with tag value " + Integer.toHexString(h.tag)); 
             Assert._OB_assert(h.tag >= 0x7fffff00 && h.tag != -1);
 
             if (h.isRMIValue()) {
             	return reader_.readRMIValue(h, clz_, h.ids[0]);
             }
 
             java.io.Serializable result = null;
 
             try {
                 result = (java.io.Serializable) clz_.newInstance();
                 reader_.addInstance(h.headerPos, result);
                 try {
                     reader_.unmarshalValueState(result);
                 } catch (org.omg.CORBA.SystemException ex) {
                     reader_.removeInstance(h.headerPos);
                     throw ex;
                 }
                 return result;
             } catch (ClassCastException ex) {
             } catch (IllegalAccessException ex) {
             } catch (InstantiationException ex) {
             }
 
             throw new org.omg.CORBA.MARSHAL(org.apache.yoko.orb.OB.MinorCodes
                     .describeMarshal(org.apache.yoko.orb.OB.MinorCodes.MinorNoValueFactory)
                     + ": " + clz_.getName(), org.apache.yoko.orb.OB.MinorCodes.MinorNoValueFactory,
                     org.omg.CORBA.CompletionStatus.COMPLETED_NO);
         }
     }
 
     //
     // Create a value using a factory
     //
     private class FactoryCreationStrategy extends CreationStrategy {
         private String id_;
 
         private ORBInstance orbInstance_;
 
         FactoryCreationStrategy(ValueReader reader,
                 org.apache.yoko.orb.CORBA.InputStream is, String id) {
             super(reader, is);
             id_ = id;
             orbInstance_ = is._OB_ORBInstance();
         }
 
         private org.omg.CORBA.portable.ValueFactory findFactory(Header h,
                 org.omg.CORBA.StringHolder id) {
             org.omg.CORBA.portable.ValueFactory f = null;
 
             if (orbInstance_ != null) {
                 ValueFactoryManager manager = orbInstance_.getValueFactoryManager();
 
                 if (h.ids.length > 0) {
                     for (int i = 0; i < h.ids.length; i++) {
                         f = manager.lookupValueFactoryWithClass(h.ids[i]);
                         if (f != null) {
                             id.value = h.ids[i];
                             break;
                         }
 
                         //
                         // If we have a formal ID, and we haven't found
                         // a factory yet, then give up
                         //
                         if (id_ != null && h.ids[i].equals(id_))
                             break;
                     }
                 } else if (id_ != null) {
                     f = manager.lookupValueFactoryWithClass(id_);
                     id.value = id_;
                 }
             }
 
             return f;
         }
 
         private java.io.Serializable createWithFactory(Header h,
                 org.omg.CORBA.portable.ValueFactory factory) {
             //
             // The factory's read_value method is expected to create
             // an instance of the valuetype and then call the method
             // InputStream.read_value(java.io.Serializable) to read
             // the valuetype state. We use a stack to remember the
             // Header information for use by initializeValue(), which
             // is called by read_value().
             //
             try {
                 reader_.pushHeader(h);
                 return factory.read_value(is_);
             } finally {
                 reader_.popHeader();
             }
         }
 
         private org.omg.CORBA.portable.BoxedValueHelper getBoxedHelper(String id) {
             org.omg.CORBA.portable.BoxedValueHelper result = null;
 
             Class helperClass = Util.idToClass(id, "Helper");
 
             if (helperClass != null) {
                 try {
                     return (org.omg.CORBA.portable.BoxedValueHelper) helperClass
                             .newInstance();
                 } catch (ClassCastException ex) {
                 } catch (IllegalAccessException ex) {
                 } catch (InstantiationException ex) {
                 }
 
                 throw new org.omg.CORBA.MARSHAL(org.apache.yoko.orb.OB.MinorCodes
                         .describeMarshal(org.apache.yoko.orb.OB.MinorCodes.MinorNoValueFactory)
                         + ": invalid BoxedValueHelper for " + id,
                         org.apache.yoko.orb.OB.MinorCodes.MinorNoValueFactory,
                         org.omg.CORBA.CompletionStatus.COMPLETED_NO);
             }
 
             return null;
         }
 
         java.io.Serializable create(Header h) {
             org.omg.CORBA.StringHolder idH = new org.omg.CORBA.StringHolder();
             return create(h, idH);
         }
 
         java.io.Serializable create(Header h, org.omg.CORBA.StringHolder id) {
             Assert._OB_assert(h.tag >= 0x7fffff00 && h.tag != -1);
 
             if (h.isRMIValue ()) {
                 Serializable result = readRMIValue (h, null, h.ids[0]);
                 addInstance (h.headerPos, result);
                 return result;
             }
             //
             // See if a factory exists that can create the value
             //
             org.omg.CORBA.portable.ValueFactory factory = findFactory(h, id);
             if (factory != null) {
                 java.io.Serializable result = createWithFactory(h, factory);
                 reader_.addInstance(h.headerPos, result);
                 return result;
             }
 
             //
             // Another possibility is that we're unmarshalling a valuebox,
             // so we'll try to load the Helper class dynamically
             //
             org.omg.CORBA.portable.BoxedValueHelper helper = null;
             if (h.ids.length > 0) {
                 //
                 // If it's a valuebox, at most one id will be marshalled
                 //
                 helper = getBoxedHelper(h.ids[0]);
                 if (helper != null)
                     id.value = h.ids[0];
             }
 
             if (helper == null && id_ != null) {
                 helper = getBoxedHelper(id_);
                 if (helper != null)
                     id.value = id_;
             }
 
             if (helper != null) {
                 java.io.Serializable result = helper.read_value(is_);
                 reader_.addInstance(h.headerPos, result);
                 return result;
             }
 
             String type = "<unknown>";
             if (h.ids.length > 0)
                 type = h.ids[0];
             else if (id_ != null)
                 type = id_;
             throw new org.omg.CORBA.MARSHAL(org.apache.yoko.orb.OB.MinorCodes
                     .describeMarshal(org.apache.yoko.orb.OB.MinorCodes.MinorNoValueFactory)
                     + ": " + type, org.apache.yoko.orb.OB.MinorCodes.MinorNoValueFactory,
                     org.omg.CORBA.CompletionStatus.COMPLETED_NO);
         }
     }
 
     // ------------------------------------------------------------------
     // Private and protected members
     // ------------------------------------------------------------------
 
     // Java only
     private void addInstance(int pos, java.io.Serializable instance) {
         // only add this if we have a real value 
         if (instance != null) {
             instanceTable_.put(new Integer(pos), instance);
         }
     }
 
     // Java only
     private void removeInstance(int pos) {
         instanceTable_.remove(new Integer(pos));
     }
 
     private void readHeader(Header h) {
         logger.fine("Reading header with tag value " + Integer.toHexString(h.tag)); 
         
         //
         // Special cases are handled elsewhere
         //
         Assert._OB_assert(h.tag != 0 && h.tag != -1);
 
         //
         // Check if the value is chunked
         //
         if ((h.tag & 0x00000008) == 8) {
             h.state.chunked = true;
        } else {
        	h.state.chunked = false;
         }
 
         //
         // Check for presence of codebase URL
         //
         if ((h.tag & 0x00000001) == 1) {
             //
             // Check for indirection tag
             //
             int save = buf_.pos_;
             int indTag = in_.read_long();
             if (indTag == -1) {
                 int offs = in_.read_long();
                 if (offs >= -4) {
                     throw new org.omg.CORBA.MARSHAL(org.apache.yoko.orb.OB.MinorCodes
                         .describeMarshal(org.apache.yoko.orb.OB.MinorCodes.MinorReadInvalidIndirection), 
                         org.apache.yoko.orb.OB.MinorCodes.MinorReadInvalidIndirection,
                         org.omg.CORBA.CompletionStatus.COMPLETED_NO);
                 }
                 int tmp = buf_.pos_;
                 buf_.pos_ = buf_.pos_ - 4 + offs;
                 if (buf_.pos_ < 0) {
                     throw new org.omg.CORBA.MARSHAL(org.apache.yoko.orb.OB.MinorCodes
                         .describeMarshal(org.apache.yoko.orb.OB.MinorCodes.MinorReadInvalidIndirection), 
                         org.apache.yoko.orb.OB.MinorCodes.MinorReadInvalidIndirection,
                         org.omg.CORBA.CompletionStatus.COMPLETED_NO);
                 }
                 h.codebase = in_.read_string();
                 buf_.pos_ = tmp;
             } else {
                 buf_.pos_ = save;
                 h.codebase = in_.read_string();
             }
             logger.finer("Value header codebase value is " + h.codebase); 
         }
 
         //
         // Extract repository ID information
         //
         if ((h.tag & 0x00000006) == 0) {
             logger.finer("No type information was included"); 
             //
             // No type information was marshalled
             //
         } else if ((h.tag & 0x00000006) == 6) {
             logger.finer("Multiple types included in header"); 
             //
             // Extract a list of repository IDs, representing the
             // truncatable types for this value
             //
 
             //
             // Check for indirection tag (indirected list)
             //
             int saveList = buf_.pos_;
             int indTag = in_.read_long();
             boolean indList = (indTag == -1);
 
             if (indList) {
                 int offs = in_.read_long();
                 if (offs > -4) {
                     throw new org.omg.CORBA.MARSHAL(org.apache.yoko.orb.OB.MinorCodes
                         .describeMarshal(org.apache.yoko.orb.OB.MinorCodes.MinorReadInvalidIndirection), 
                         org.apache.yoko.orb.OB.MinorCodes.MinorReadInvalidIndirection,
                         org.omg.CORBA.CompletionStatus.COMPLETED_NO);
                 }
                 saveList = buf_.pos_;
                 buf_.pos_ = buf_.pos_ - 4 + offs;
                 if (buf_.pos_ < 0) {
                     throw new org.omg.CORBA.MARSHAL(org.apache.yoko.orb.OB.MinorCodes
                         .describeMarshal(org.apache.yoko.orb.OB.MinorCodes.MinorReadInvalidIndirection), 
                         org.apache.yoko.orb.OB.MinorCodes.MinorReadInvalidIndirection,
                         org.omg.CORBA.CompletionStatus.COMPLETED_NO);
                 }
             } else
                 buf_.pos_ = saveList;
 
             int count = in_.read_long();
             h.ids = new String[count];
 
             for (int i = 0; i < count; i++) {
                 //
                 // Check for indirection tag (indirected list entry)
                 //
                 int saveRep = buf_.pos_;
                 indTag = in_.read_long();
                 if (indTag == -1) {
                     int offs = in_.read_long();
                     if (offs > -4) {
                         throw new org.omg.CORBA.MARSHAL(org.apache.yoko.orb.OB.MinorCodes
                             .describeMarshal(org.apache.yoko.orb.OB.MinorCodes.MinorReadInvalidIndirection), 
                             org.apache.yoko.orb.OB.MinorCodes.MinorReadInvalidIndirection,
                             org.omg.CORBA.CompletionStatus.COMPLETED_NO);
                     }
                     saveRep = buf_.pos_;
                     buf_.pos_ = buf_.pos_ - 4 + offs;
                     if (buf_.pos_ < 0) {
                         throw new org.omg.CORBA.MARSHAL(org.apache.yoko.orb.OB.MinorCodes
                             .describeMarshal(org.apache.yoko.orb.OB.MinorCodes.MinorReadInvalidIndirection), 
                             org.apache.yoko.orb.OB.MinorCodes.MinorReadInvalidIndirection,
                             org.omg.CORBA.CompletionStatus.COMPLETED_NO);
                     }
                     h.ids[i] = in_.read_string();
                     buf_.pos_ = saveRep;
                 } else {
                     buf_.pos_ = saveRep;
                     h.ids[i] = in_.read_string();
                 }
                 logger.finer("Value header respoitory id added " + h.ids[i]); 
             }
 
             //
             // Restore buffer position (case of indirected list)
             //
             if (indList)
                 buf_.pos_ = saveList;
         } else if ((h.tag & 0x00000006) == 2) {
             //
             // Extract a single repository ID
             //
             String id;
 
             //
             // Check for indirection tag
             //
             int save = buf_.pos_;
             int indTag = in_.read_long();
             if (indTag == -1) {
                 int offs = in_.read_long();
                 if (offs > -4) {
                     throw new org.omg.CORBA.MARSHAL(org.apache.yoko.orb.OB.MinorCodes
                         .describeMarshal(org.apache.yoko.orb.OB.MinorCodes.MinorReadInvalidIndirection), 
                         org.apache.yoko.orb.OB.MinorCodes.MinorReadInvalidIndirection,
                         org.omg.CORBA.CompletionStatus.COMPLETED_NO);
                 }
                 save = buf_.pos_;
                 buf_.pos_ = buf_.pos_ - 4 + offs;
                 if (buf_.pos_ < 0) {
                     throw new org.omg.CORBA.MARSHAL(org.apache.yoko.orb.OB.MinorCodes
                         .describeMarshal(org.apache.yoko.orb.OB.MinorCodes.MinorReadInvalidIndirection), 
                         org.apache.yoko.orb.OB.MinorCodes.MinorReadInvalidIndirection,
                         org.omg.CORBA.CompletionStatus.COMPLETED_NO);
                 }
                 id = in_.read_string();
                 buf_.pos_ = save;
             } else {
                 buf_.pos_ = save;
                 id = in_.read_string();
             }
 
             h.ids = new String[1];
             h.ids[0] = id;
             logger.finer("Single header repository id read " + id); 
         }
 
         //
         // Record beginning of value data
         //
         h.dataPos = buf_.pos_;
 
         //
         // Add entry to header table
         //
         headerTable_.put(new Integer(h.headerPos), h);
     }
 
     private void readChunk(ChunkState state) {
         //
         // Check for a chunk size
         //
         int size = in_._OB_readLongUnchecked();
         logger.finest("Reading new chunk.  Size value is " + Integer.toHexString(size) + " current nest is " + state.nestingLevel + " current position=" + buf_.pos_); 
         if (size >= 0 && size < 0x7fffff00) // chunk size
         {
             state.chunkStart = buf_.pos_;
             state.chunkSize = size;
         } else if (size < 0) // end tag
         {
             buf_.pos_ -= 4; // rewind
             state.chunkStart = buf_.pos_;
             state.chunkSize = 0;
         } else {
             buf_.pos_ -= 4; // rewind
             state.chunkStart = 0;
             state.chunkSize = 0;
         }
         logger.finest("Chunk read.  start=" + state.chunkStart + ", size=" + state.chunkSize + " buffer position=" + buf_.pos_); 
     }
 
     private void initHeader(Header h) {
         //
         // Null values and indirections must be handled by caller
         //
         Assert._OB_assert(h.tag != 0 && h.tag != -1);
 
         h.headerPos = buf_.pos_ - 4; // adjust for alignment
         h.state.copyFrom(chunkState_);
 
         //
         // Read value header info
         //
         readHeader(h);
         chunkState_.copyFrom(h.state);
 
         //
         // Increment our nesting level if we are chunked
         //
         if (chunkState_.chunked) {
 //          logger.finest("Reading chunk for chunked value.  Header tag=" + Integer.toHexString(h.tag) + " current position=" + buf_.pos_); 
             readChunk(chunkState_);
             chunkState_.nestingLevel++;
 //          logger.fine("Chunk nesting level is " + chunkState_.nestingLevel + " current position=" + buf_.pos_ + " chunk size=" + chunkState_.chunkSize); 
         }
     }
 
     private void skipChunk() {
         if (chunkState_.chunked) {
             logger.fine("Skipping a chunked value.  nesting level=" + chunkState_.nestingLevel + " current position is " + buf_.pos_ + " chunk end is " + (chunkState_.chunkStart + chunkState_.chunkSize)); 
             //
             // At this point, the unmarshalling code has finished. However,
             // we may have a truncated value, or we may have unmarshalled a
             // custom value. In either case, we can't be sure that the
             // unmarshalling code has positioned the stream at the end of
             // this value.
             //
             // Therefore we will advance, chunk by chunk, until we reach
             // the end of the value.
             //
 
             //
             // Skip to the end of the current chunk (if necessary)
             //
             if (chunkState_.chunkStart > 0) {
                 buf_.pos_ = chunkState_.chunkStart;
                 in_._OB_skip(chunkState_.chunkSize);
                 logger.finest("Skipping to end of current chunk.  New position is " + buf_.pos_); 
             }
 
             chunkState_.chunkStart = 0;
             chunkState_.chunkSize = 0;
 
             //
             // Loop until we have reached the end of this value. We may
             // have to process nested values, chunks, etc.
             //
             int level = chunkState_.nestingLevel;
             int tag = in_._OB_readLongUnchecked();
             logger.finest("Skipping chunk:  read tag value =" + tag); 
             while (tag >= 0 || (tag < 0 && tag < -chunkState_.nestingLevel)) {
                 if (tag >= 0x7fffff00) {
                     logger.finest("Skipping chunk:  reading a nested chunk value"); 
                     //
                     // This indicates a nested value. We read the header
                     // information and store it away, in case a subsequent
                     // indirection refers to this value.
                     //
                     level++;
                     Header nest = new Header();
                     nest.tag = tag;
                     nest.headerPos = buf_.pos_ - 4; // adjust for alignment
                     nest.state.nestingLevel = level;
                     readHeader(nest);
                 } else if (tag >= 0) {
                     logger.finest("Skipping chunk:  skipping over a chunk for length " +tag); 
                     //
                     // Chunk size - advance the stream past the chunk
                     //
                     in_._OB_skip(tag);
                 } else {
                     logger.finest("Skipping chunk:  chunk end tag=" + Integer.toHexString(tag) + " current level=" + level); 
                     //
                     // tag is less than 0, so this is an end tag for a nested
                     // value
                     //
                     // this can terminate more than a single level. 
                     level = (-tag) - 1; 
                 }
 
                 //
                 // Read the next tag
                 //
                 tag = in_._OB_readLongUnchecked();
                 logger.finest("Skipping chunk:  read tag value =" + tag); 
             }
 
             //
             // If the tag is greater than our nesting level, then this
             // value coterminates with an outer value. We rewind the
             // stream so that the outer value can read this tag.
             //
             if (tag > -chunkState_.nestingLevel) {
                 buf_.pos_ -= 4;
             }
 
             chunkState_.nestingLevel--;
 
             logger.finest("New chunk nesting level is " + chunkState_.nestingLevel); 
             if (chunkState_.nestingLevel == 0) {
                 chunkState_.chunked = false;
             }
             else {
                 //
                 // We're chunked and still processing nested values, so
                 // another chunk may follow
                 //
                 logger.finest("Reading chunk for skipping to end of a chunk"); 
                 readChunk(chunkState_);
             }
             
             logger.finest("Final chunk state is nesting level=" + chunkState_.nestingLevel + " current position is " + buf_.pos_ + " chunk end is " + (chunkState_.chunkStart + chunkState_.chunkSize)); 
         }
     }
 
     //
     // Invoke the valuetype to unmarshal its state
     //
     // Java only
     //
     private void unmarshalValueState(java.io.Serializable v) {
         if (v instanceof org.omg.CORBA.portable.StreamableValue) {
             ((org.omg.CORBA.portable.StreamableValue) v)._read(in_);
         }
         else if (v instanceof org.omg.CORBA.CustomMarshal) {
             org.omg.CORBA.DataInputStream dis = new org.apache.yoko.orb.CORBA.DataInputStream(
                     in_);
             ((org.omg.CORBA.CustomMarshal) v).unmarshal(dis);
         } else {
             throw new org.omg.CORBA.MARSHAL("Valuetype does not implement "
                     + "StreamableValue or " + "CustomMarshal");
         }
     }
 
     private java.io.Serializable readIndirection(CreationStrategy strategy) {
         int offs = in_.read_long();
         int pos = buf_.pos_ - 4 + offs;
         pos += 3; // adjust for alignment
         pos -= (pos & 0x3);
         Integer posObj = new Integer(pos);
 
         //
         // Check the history for a value that was seen at the specified
         // position. Generally, we should expect the value to be present.
         // However, in the case of chunking, it's possible for an
         // indirection to refer to a nested value in the truncated state
         // of an enclosing value, or to a value that we could not
         // instantiate.
         //
         java.io.Serializable v = (java.io.Serializable) instanceTable_
                 .get(posObj);
 
         if (v != null) {
             return v;
         } else {
             int save = buf_.pos_;
 
             //
             // Check for indirection to null value
             //
             buf_.pos_ = pos; // rewind to offset position
             if (in_._OB_readLongUnchecked() == 0) {
                 buf_.pos_ = save;
                 // Can't put a null in a Hashtable
                 // instanceTable_.put(posObj, null);
                 return null;
             }
 
             //
             // If it's not null and it's not in our history, then
             // there's no hope
             //
             Header nest = (Header) headerTable_.get(posObj);
 
             if (nest == null)
                 throw new org.omg.CORBA.MARSHAL(org.apache.yoko.orb.OB.MinorCodes
                         .describeMarshal(org.apache.yoko.orb.OB.MinorCodes.MinorNoValueFactory)
                         + ": cannot instantiate value for indirection",
                         org.apache.yoko.orb.OB.MinorCodes.MinorNoValueFactory,
                         org.omg.CORBA.CompletionStatus.COMPLETED_NO);
 
             /* Maybe we have an indirection to an object
              * that is being deserialized. We throw an
              * IndirectionException which signals the RMI
              * implementation to handle the indirection.
              */
             if(nest.isRMIValue()) {
             	buf_.pos_ = save;
             	throw new IndirectionException(pos);
             }
             //
             // Create the value
             //
             buf_.pos_ = nest.dataPos;
             ChunkState saveState = new ChunkState(chunkState_);
             chunkState_.copyFrom(nest.state);
             if (chunkState_.chunked)
 //              logger.finest("Reading chunk in readIndirection()"); 
                 readChunk(chunkState_);
 
             try {
                 v = strategy.create(nest);
             } finally {
                 //
                 // Restore state
                 //
                 buf_.pos_ = save;
                 chunkState_.copyFrom(saveState);
             }
 
             return v;
         }
     }
 
     private java.io.Serializable read(CreationStrategy strategy) {
         Header h = new Header();
         h.tag = in_.read_long();
 
 //      logger.fine("Read tag value " + Integer.toHexString(h.tag)); 
         if (h.tag == 0) {
             return null;
         } else if (h.tag == -1) {
             return readIndirection(strategy);
         } else if (h.tag < 0x7fffff00) {
             throw new org.omg.CORBA.MARSHAL("Illegal valuetype tag 0x"
                     + Integer.toHexString(h.tag));
         } else {
             initHeader(h);
             java.io.Serializable vb = strategy.create(h);
             skipChunk();
             return vb;
         }
     }
 
     //
     // Remarshal each valuetype member
     //
     private void copyValueState(org.omg.CORBA.TypeCode tc, org.apache.yoko.orb.CORBA.OutputStream out) {
         try {
             if (tc.kind() == org.omg.CORBA.TCKind.tk_value) {
                 //
                 // First copy the state of the concrete base type, if any
                 //
                 org.omg.CORBA.TypeCode base = tc.concrete_base_type();
                 if (base != null) {
                     copyValueState(base, out);
                 }
 
                 for (int i = 0; i < tc.member_count(); i++) {
 //                  logger.fine("writing member of typecode " + tc.member_type(i).kind().value()); 
                     out.write_InputStream(in_, tc.member_type(i));
                 }
             } else if (tc.kind() == org.omg.CORBA.TCKind.tk_value_box) {
                 out.write_InputStream(in_, tc.content_type());
             } else
                 Assert._OB_assert(false);
         } catch (org.omg.CORBA.TypeCodePackage.BadKind ex) {
             logger.log(Level.FINER, "Invalid type kind", ex); 
             Assert._OB_assert(ex);
         } catch (org.omg.CORBA.TypeCodePackage.Bounds ex) {
             logger.log(Level.FINER, "Invalid type kind", ex); 
             Assert._OB_assert(ex);
         }
     }
 
     // Java only
     private void pushHeader(Header h) {
         h.next = currentHeader_;
         currentHeader_ = h;
     }
 
     // Java only
     private void popHeader() {
         Assert._OB_assert(currentHeader_ != null);
 
         currentHeader_ = currentHeader_.next;
     }
 
     //
     // Search up the valuetype's inheritance hierarchy for a TypeCode
     // with the given repository ID
     //
     private org.omg.CORBA.TypeCode findTypeCode(String id,
             org.omg.CORBA.TypeCode tc) {
         org.omg.CORBA.TypeCode result = null;
         org.omg.CORBA.TypeCode t = tc;
 //      logger.finer("Locating type code for id " + id); 
         while (result == null) {
             try {
                 org.omg.CORBA.TypeCode t2 = org.apache.yoko.orb.CORBA.TypeCode
                         ._OB_getOrigType(t);
 //              logger.finer("Checking typecode " + id + " against " + t2.id()); 
                 if (id.equals(t2.id())) {
                     result = t;
                 } else if (t2.kind() == org.omg.CORBA.TCKind.tk_value
                         && t2.type_modifier() == org.omg.CORBA.VM_TRUNCATABLE.value) {
                     t = t2.concrete_base_type();
 //                  logger.finer("Iterating with concrete type " + t.id()); 
                 } else {
                     break;
                 }
             } catch (org.omg.CORBA.TypeCodePackage.BadKind ex) {
                 Assert._OB_assert(ex);
             }
         }
 
         return result;
     }
 
     // ------------------------------------------------------------------
     // Public methods
     // ------------------------------------------------------------------
 
     public ValueReader(org.apache.yoko.orb.CORBA.InputStream in) {
         in_ = in;
         buf_ = in._OB_buffer();
         orbInstance_ = in._OB_ORBInstance();
         instanceTable_ = new java.util.Hashtable(131);
         headerTable_ = new java.util.Hashtable(131);
     }
 
     java.io.Serializable readRMIValue(Header h, Class clz, String repid)
     {
         logger.fine("Reading RMI value of type " + repid); 
         if (valueHandler == null) {
             valueHandler = javax.rmi.CORBA.Util.createValueHandler ();
         }
 
         if (repid == null) {
             repid = h.ids[0];
 
             if (repid == null) {
                 throw new org.omg.CORBA.MARSHAL("missing repository id");
             }
         }
 
         String className = Util.idToClassName (repid, "");
         String codebase  = h.codebase;
         Class repoClass = null;
 
         if (codebase == null) {
             codebase = in_.__getCodeBase ();
         }
 
         repoClass = resolveRepoClass(className, codebase);
 
         // if we have a non-null codebase and can't resolve this, throw an 
         // exception now.  Otherwise, we'll try again after grabbing the remote 
         // codebase. 
         if (repoClass == null && codebase != null && codebase.length() > 0) {
             throw new org.omg.CORBA.MARSHAL("class "+className
                                             +" not found (cannot load from "+codebase+")");
         }
 
         if (remoteCodeBase == null) {
             remoteCodeBase = in_.__getSendingContextRuntime ();
         }
         if (repoClass == null) {
             if (codebase == null && remoteCodeBase != null) {
                 try {
                     codebase = remoteCodeBase.implementation (repid);
                 } catch (org.omg.CORBA.SystemException ex) {
                     // ignore
                 }
 
             }
 
             if (codebase == null) {
                 // TODO: add minor code
                 throw new org.omg.CORBA.MARSHAL
                 ("class "+className+" not found (no codebase provided)");
             } else {
                 repoClass = resolveRepoClass(className, codebase); 
                 if (repoClass == null) {
                     throw new org.omg.CORBA.MARSHAL("class "+className+" not found (cannot load from " + codebase+ ")");
                 }
             }
         }
 
         /* Suns crappy ValueHandler implementation narrows the remote CodeBase 
          * to a com.sun.org.omg.SendingContext.CodeBase.
          * Narrowing CodeBaseProxy is not possible, we
          * need a stub.
          */
         if (remoteCodeBase instanceof CodeBaseProxy) {
             remoteCodeBase = ((CodeBaseProxy) remoteCodeBase).getCodeBase();
         }
 
         return valueHandler.readValue(in_, h.headerPos, repoClass, repid, remoteCodeBase);
     }
     
     private Class resolveRepoClass(String name, String codebase) 
     {
         logger.fine("Attempting to resolve class " + name + " from codebase " + codebase);
         if (name.startsWith("[")) {
             int levels = 0; 
             for (int i = 0; name.charAt(i) == '['; i++) {
                 levels++; 
             }
             Class elementClass = null; 
             
             // now resolve the element descriptor to a class 
             switch (name.charAt(levels)) {
                 case 'Z':
                     elementClass = Boolean.TYPE;
                     break; 
                 case 'B':
                     elementClass = Byte.TYPE;
                     break; 
                 case 'S':
                     elementClass = Short.TYPE;
                     break; 
                 case 'C':
                     elementClass = Character.TYPE;
                     break; 
                 case 'I':
                     elementClass = Integer.TYPE;
                     break; 
                 case 'J':
                     elementClass = Long.TYPE;
                     break; 
                 case 'F':
                     elementClass = Float.TYPE;
                     break; 
                 case 'D':
                     elementClass = Double.TYPE;
                     break; 
                 case 'L':
                     // extract the class from the name and resolve that.
                     elementClass = resolveRepoClass(name.substring(levels + 1, name.indexOf(';')), codebase);
                     if (elementClass == null) {
                         return null; 
                     }
                     break; 
             }
             
             // ok, we need to recurse and resolve the base array element class
             Object arrayInstance = null;
             // this is easier with a single level     
             if (levels == 1) {
                 arrayInstance = Array.newInstance(elementClass, 0); 
             }
             else {
                 // all elements will be zero 
                 int[] dimensions = new int[levels]; 
                 arrayInstance = Array.newInstance(elementClass, dimensions); 
             }
             // return the class associated with this array 
             return arrayInstance.getClass();
         }
         else {
             try
             {
                 return javax.rmi.CORBA.Util.loadClass(name, codebase,
                      Util.getContextClassLoader());
             }
             catch (ClassNotFoundException ex)
             {
                 // this will be sorted out later 
                 return null; 
             }
         }
     }
 
     public java.io.Serializable readValue() {
         FactoryCreationStrategy strategy = new FactoryCreationStrategy(this,
                 in_, null);
         return read(strategy);
     }
 
     public java.io.Serializable readValue(String id) {
         logger.fine("Reading value of type " + id); 
         FactoryCreationStrategy strategy = new FactoryCreationStrategy(this,
                 in_, id);
         return read(strategy);
     }
 
     public java.io.Serializable readValue(Class clz) {
         logger.fine("Reading value of type " + clz.getName()); 
     	if(clz.equals(String.class)) {
     		return WStringValueHelper.read(in_);
     	}
         ClassCreationStrategy strategy = new ClassCreationStrategy(this, in_,
                 clz);
         return read(strategy);
     }
 
     public java.io.Serializable readValueBox(
             org.omg.CORBA.portable.BoxedValueHelper helper) {
         BoxCreationStrategy strategy = new BoxCreationStrategy(this, in_,
                 helper);
         return read(strategy);
     }
 
     public void initializeValue(java.io.Serializable value) {
         //
         // We should have previously pushed a Header on the stack
         //
         Assert._OB_assert(currentHeader_ != null);
 
         //
         // Now that we have an instance, we can put it in our history
         //
         addInstance(currentHeader_.headerPos, value);
 
         //
         // Unmarshal the value's state
         //
         try {
             unmarshalValueState(value);
         } catch (org.omg.CORBA.SystemException ex) {
             removeInstance(currentHeader_.headerPos);
             throw ex;
         }
     }
 
     public java.lang.Object readAbstractInterface() {
         //
         // Abstract interfaces are marshalled like a union with a
         // boolean discriminator - if true, an objref follows,
         // otherwise a valuetype follows
         //
         if (in_.read_boolean())
             return in_.read_Object();
         else
             return readValue();
     }
 
     public java.lang.Object readAbstractInterface(Class clz) {
         //
         // Abstract interfaces are marshalled like a union with a
         // boolean discriminator - if true, an objref follows,
         // otherwise a valuetype follows
         //
         if (in_.read_boolean())
             return in_.read_Object(clz);
         else
             return readValue(clz);
     }
 
     //
     // Copy a value from out InputStream to the given OutputStream.
     // The return value is the most-derived TypeCode of the value
     // written to the stream. This may not be the same as the TypeCode
     // argument if the value is truncated. Furthermore, the TypeCode
     // may not represent the actual most-derived type, since a more-derived
     // value may have been read.
     //
     public org.omg.CORBA.TypeCode remarshalValue(org.omg.CORBA.TypeCode tc,
             org.apache.yoko.orb.CORBA.OutputStream out) {
         //
         // TODO: We've removed the reset of the position table at each top
         // level call. We need to perform more testing and analysis to verify
         // that this wasn't broken. remarshalDepth was also removed since it
         // wasn't being used anywhere except to determine when the table should
         // be reset.
         //
         //
         // Create a new Hashtable for each top-level call to remarshalValue
         //
         if (positionTable_ == null) {
             positionTable_ = new java.util.Hashtable(131);
         }
 
         org.omg.CORBA.TypeCode origTC = org.apache.yoko.orb.CORBA.TypeCode
                 ._OB_getOrigType(tc);
 
         org.omg.CORBA.TypeCode result = null;
 
         Header h = new Header();
         h.tag = in_.read_long();
         
         logger.fine("Read tag value " + Integer.toHexString(h.tag)); 
         h.headerPos = buf_.pos_ - 4; // adjust for alignment
         h.state.copyFrom(chunkState_);
 
         //
         // Remember starting position of this valuetype
         //
         int pos = h.headerPos;
 
         //
         // Check for special cases (null values and indirections)
         //
         if (h.tag == 0) {
             out.write_long(0);
             result = tc;
         } else if (h.tag == -1) {
             //
             // Since offsets can change as we remarshal valuetypes to the
             // output stream, we use a table to map old positions to new ones
             //
             int offs = in_.read_long();
             int oldPos = buf_.pos_ - 4 + offs;
             oldPos += 3; // adjust alignment to start of value
             oldPos -= (oldPos & 0x3);
 
             //
             // If we find the position in the table, write the translated
             // offset to the output stream. Otherwise, the indirection refers
             // to a valuetype that we were unable to create and we therefore
             // raise MARSHAL.
             //
             Integer newPos = (Integer) positionTable_.get(new Integer(oldPos));
             if (newPos != null) {
                 out.write_long(h.tag);
                 offs = newPos.intValue() - out._OB_pos();
                 out.write_long(offs);
                 //
                 // TODO: The TypeCode may not necessarily reflect the
                 // TypeCode of the indirected value.
                 //
                 result = tc;
             } else
                 throw new org.omg.CORBA.MARSHAL(
                         "Cannot find value for indirection");
         } else {
             if (h.tag < 0x7fffff00) {
                 throw new org.omg.CORBA.MARSHAL("Illegal valuetype tag 0x" + Integer.toHexString(h.tag));
             }
             
             logger.fine("Remarshalling header with tag value " + h.tag); 
             
             //
             // Add valuetype to position map
             //
             int outPos = out._OB_pos();
             outPos += 3; // adjust alignment to start of value
             outPos -= (outPos & 0x3);
             positionTable_.put(new Integer(pos), new Integer(outPos));
 
             //
             // Read value header info
             //
             readHeader(h);
             chunkState_.copyFrom(h.state);
 
             if (chunkState_.chunked) {
                 logger.finest("Reading chunk in remarshal value()"); 
                 readChunk(chunkState_);
                 chunkState_.nestingLevel++;
             }
 
             String tcId = null;
             short mod = org.omg.CORBA.VM_NONE.value;
             int idPos;
             try {
                 tcId = origTC.id();
                 if (origTC.kind() == org.omg.CORBA.TCKind.tk_value)
                     mod = origTC.type_modifier();
             } catch (org.omg.CORBA.TypeCodePackage.BadKind ex) {
                 Assert._OB_assert(ex);
             }
 
             //
             // We have two methods of extracting the state of a valuetype:
             //
             // 1) Use the TypeCode
             // 2) Use a valuetype factory
             //
             // Which method we use is determined by the repository IDs
             // in the valuetype header.
             //
             // Our goal is to preserve as much information as possible.
             // If the TypeCode describes a more-derived type than any
             // available factory, we will use the TypeCode. Otherwise,
             // we use a factory to create a temporary instance of the
             // valuetype, and subsequently marshal that instance to the
             // OutputStream.
             //
 
             logger.fine("Attempting to resolve typeId " + tcId); 
             //
             // See if the TypeCode ID matches any of the valuetype's IDs -
             // stop at the first match
             //
             String id = null;
             for (idPos = 0; idPos < h.ids.length; idPos++) {
                 logger.finer("Comparing type id " + tcId + " against " + h.ids[idPos]); 
                 if (tcId.equals(h.ids[idPos])) {
                     id = h.ids[idPos];
                     break;
                 }
             }
             
             // if this is null, then try again to see if we can find a class in the ids list
             // that is compatible with the base type.  This will require resolving the classes.
             if (id == null) {
                 // see if we can resolve the type for the stored type code 
                 Class baseType = Util.idToClass(tcId, ""); 
                 if (baseType != null) {
                     for (idPos = 0; idPos < h.ids.length; idPos++) {
                         logger.finer("Comparing type id " + tcId + " against " + h.ids[idPos]); 
                         Class idType = Util.idToClass(h.ids[idPos], "");
                         if (idType != null) {
                             // if these classes are assignment compatible, go with that as the type. 
                             logger.finer("Comparing type id " + baseType.getName() + " against " + idType.getName()); 
                             if (baseType.isAssignableFrom(idType)) {
                                 id = h.ids[idPos];
                                 break;
                             }
                         }
                     }
                 }
             }
 
             //
             // See if a factory exists for any of the valuetype's IDs -
             // stop at the first match
             //
             String factoryId = null;
             int factoryPos = 0;
             org.omg.CORBA.portable.ValueFactory factory = null;
             if (orbInstance_ != null) {
                 ValueFactoryManager manager = orbInstance_
                         .getValueFactoryManager();
 
                 for (factoryPos = 0; factoryPos < h.ids.length; factoryPos++) {
                     factory = manager
                             .lookupValueFactoryWithClass(h.ids[factoryPos]);
                     if (factory != null) {
                         factoryId = h.ids[factoryPos];
                         break;
                     }
                 }
             }
 
             //
             // If no ID matched the TypeCode, and no factory was found,
             // then we have no way to remarshal the data
             //
             if (h.ids.length > 0 && id == null && factoryId == null) {
                 logger.fine("Unable to resolve a factory for type " + tcId); 
                 throw new org.omg.CORBA.MARSHAL(org.apache.yoko.orb.OB.MinorCodes
                         .describeMarshal(org.apache.yoko.orb.OB.MinorCodes.MinorNoValueFactory)
                         + ": insufficient information to copy valuetype",
                         org.apache.yoko.orb.OB.MinorCodes.MinorNoValueFactory,
                         org.omg.CORBA.CompletionStatus.COMPLETED_NO);
             }
 
             //
             // If value is custom and there is no factory, then we have
             // no way to remarshal the data
             //
             if (mod == org.omg.CORBA.VM_CUSTOM.value && factoryId == null) {
                 throw new org.omg.CORBA.MARSHAL(org.apache.yoko.orb.OB.MinorCodes
                         .describeMarshal(org.apache.yoko.orb.OB.MinorCodes.MinorNoValueFactory)
                         + ": unable to copy custom valuetype",
                         org.apache.yoko.orb.OB.MinorCodes.MinorNoValueFactory,
                         org.omg.CORBA.CompletionStatus.COMPLETED_NO);
             }
 
             //
             // If the TypeCode is more descriptive than any available
             // factory, or if no identifiers were provided, then use the
             // TypeCode, otherwise use the factory
             //
             // NOTE: (Java only) We also use the TypeCode for boxed values,
             // because we don't have the BoxedHelper and may not be
             // able to locate one via the class loader.
             //
             if (idPos < factoryPos || h.ids.length == 0
                     || origTC.kind() == org.omg.CORBA.TCKind.tk_value_box) {
                 int i;
 
                 //
                 // We may need to truncate the state of this value, which
                 // means we need to revise the list of repository IDs
                 //
                 int numIds = h.ids.length - idPos;
                 String[] ids = new String[numIds];
                 for (i = idPos; i < h.ids.length; i++)
                     ids[i - idPos] = h.ids[i];
 
                 logger.fine("Copying value state of object using truncated type"); 
                 out._OB_beginValue(h.tag, ids, h.state.chunked);
                 copyValueState(origTC, out);
                 out._OB_endValue();
 
                 result = tc;
             } else {
                 //
                 // Create a temporary instance to use for marshalling
                 //
                 try {
                     pushHeader(h);
                     java.io.Serializable vb = factory.read_value(in_);
                     logger.fine("Creating a temporary copy of the object for marshalling"); 
                     try {
                         out.write_value(vb);
                     } finally {
                         removeInstance(h.headerPos);
                     }
                 } finally {
                     popHeader();
                 }
 
                 //
                 // Determine the TypeCode that is equivalent to the
                 // factory in use
                 //
                 result = findTypeCode(h.ids[factoryPos], tc);
 
                 if (result == null)
                     result = tc;
             }
 
             skipChunk();
         }
 
         Assert._OB_assert(result != null);
         return result;
     }
 
     public void readValueAny(org.omg.CORBA.Any any, org.omg.CORBA.TypeCode tc) {
         //
         // In constrast to other complex types, valuetypes and value boxes
         // in Anys cannot simply be "remarshalled". The reason is that
         // indirection may occur across multiple Any values in the same
         // stream. Therefore, if possible, we should attempt to construct
         // the values using a factory so that any shared values will be
         // handled correctly.
         //
         // If a factory cannot be found, we should still remarshal.
         // However, if an indirection is encountered which refers to
         // a value we were unable to construct, an exception will be
         // raised.
         //
 
         org.apache.yoko.orb.CORBA.Any obAny = null;
         try {
             obAny = (org.apache.yoko.orb.CORBA.Any) any;
         } catch (ClassCastException ex) {
             //
             // Any may have been created by a foreign ORB
             //
         }
 
         org.omg.CORBA.TypeCode origTC = org.apache.yoko.orb.CORBA.TypeCode
                 ._OB_getOrigType(tc);
 
         logger.fine("Reading an Any value of kind=" + origTC.kind().value() + " from position " + buf_.pos_); 
 
         //
         // Check if the Any contains an abstract interface
         //
         if (origTC.kind() == org.omg.CORBA.TCKind.tk_abstract_interface) {
             boolean b = in_.read_boolean();
             if (b) {
                 logger.fine("Reading an object reference for an abstract interface"); 
                 //
                 // The abstract interface represents an object reference
                 //
                 any.insert_Object(in_.read_Object(), tc);
                 return;
             } else {
                 logger.fine("Reading an object value for an abstract interface"); 
                 //
                 // The abstract interface represents a valuetype. The
                 // readValue() method will raise an exception if an
                 // instance of the valuetype could not be created.
                 // We cannot remarshal in this case because we don't
                 // have a TypeCode for the valuetype.
                 //
                 any.insert_Value(readValue(), tc);
                 return;
             }
         }
 
         //
         // If the TypeCode's repository ID is that of CORBA::ValueBase,
         // then we try to create an instance. The Any could contain a
         // valuetype *or* a boxed valuetype.
         //
         // If creation fails, we cannot remarshal the value, since
         // CORBA::ValueBase is not a truncatable base type, and we
         // have no other TypeCode information. Truncating to
         // CORBA::ValueBase doesn't seem very useful anyway.
         //
         try {
             String id = origTC.id();
             logger.fine("Reading an Any value of id=" + id); 
             if (id.equals("IDL:omg.org/CORBA/ValueBase:1.0")) {
                 any.insert_Value(readValue(), tc);
                 return;
             }
         } catch (org.omg.CORBA.TypeCodePackage.BadKind ex) {
             Assert._OB_assert(ex);
         }
 
         //
         // At this point, the Any contains a valuetype or a boxed valuetype,
         // and we have a TypeCode that can be used for remarshalling.
         //
 
         //
         // Save some state so that we can restore things prior to
         // remarshalling
         //
         int startPos = buf_.pos_;
         ChunkState startState = new ChunkState(chunkState_);
 
         //
         // No need to worry about truncation for boxed valuetypes
         //
         if (origTC.kind() == org.omg.CORBA.TCKind.tk_value_box) {
             try {
                 any.insert_Value(readValue(tc.id()), tc);
                 return;
             } catch (org.omg.CORBA.MARSHAL ex) {
                 //
                 // Creation failed - restore our state and try remarshalling
                 //
                 buf_.pos_ = startPos;
                 chunkState_.copyFrom(startState);
 
                 org.apache.yoko.orb.OCI.Buffer buf = new org.apache.yoko.orb.OCI.Buffer();
                 org.apache.yoko.orb.CORBA.OutputStream out = new org.apache.yoko.orb.CORBA.OutputStream(
                         buf);
                 out._OB_ORBInstance(orbInstance_);
                 remarshalValue(origTC, out);
                 org.apache.yoko.orb.CORBA.InputStream in = (org.apache.yoko.orb.CORBA.InputStream) out
                         .create_input_stream();
                 Assert._OB_assert(obAny != null);
                 obAny.replace(tc, in);
                 return;  
             } catch (org.omg.CORBA.TypeCodePackage.BadKind ex) {
                 Assert._OB_assert(ex);
             }
         } else {
             //
             // Read valuetype header tag
             //
             Header h = new Header();
             h.tag = in_.read_long();
             logger.fine("Read tag value " + Integer.toHexString(h.tag)); 
 
             //
             // Check tag for special cases
             //
             if (h.tag == 0) {
                 any.insert_Value(null, tc);
                 return;
             } else if (h.tag != -1 && h.tag < 0x7fffff00) {
                 throw new org.omg.CORBA.MARSHAL("Illegal valuetype tag 0x"
                         + Integer.toHexString(h.tag));
             }
 
             //
             // Try to create an instance of the valuetype using a factory
             //
             FactoryCreationStrategy strategy = new FactoryCreationStrategy(
                     this, in_, null);
             try {
                 if (h.tag == -1) {
                     //
                     // TODO: The TypeCode may not necessarily reflect
                     // the one that was used for the indirected value
                     // (i.e., the value may have been truncated).
                     // Fixing this probably requires maintaining a
                     // map of stream position to TypeCode.
                     //
                     logger.fine("Handling a value type indirection value"); 
                     any.insert_Value(readIndirection(strategy), tc);
                     return;
                 } else {
                     initHeader(h);
                     org.omg.CORBA.StringHolder idH = new org.omg.CORBA.StringHolder();
                     java.io.Serializable vb = strategy.create(h, idH);
                     logger.fine("Obtained a value of type " + vb.getClass().getName()); 
                     skipChunk();
 
                     //
                     // Find the TypeCode for the repository ID that was
                     // used to instantiate the valuetype. Three things
                     // can happen:
                     //
                     // 1) The TypeCode is equal to tc.
                     //
                     // 2) The TypeCode is null. In this case, the instance
                     // is of a more-derived type than tc, so the best
                     // we can do is to use tc.
                     //
                     // 3) The TypeCode is a base type of tc. In this case,
                     // the valuetype was truncated.
                     //
                     org.omg.CORBA.TypeCode t = null;
                     if(idH.value != null) {
                     	t = findTypeCode(idH.value, tc);
                     }
                     if (t != null) {
                         any.insert_Value(vb, t);
                     }
                     else {
                         any.insert_Value(vb, tc);
                     }
                     return;
                 }
             } catch (org.omg.CORBA.MARSHAL ex) {
                 logger.log(Level.FINE, "Marshaling exception occurred, attempting to remarshal", ex); 
                 //
                 // Creation failed - restore our state and try remarshalling
                 //
                 buf_.pos_ = startPos;
                 chunkState_.copyFrom(startState);
 
                 org.apache.yoko.orb.OCI.Buffer buf = new org.apache.yoko.orb.OCI.Buffer();
                 org.apache.yoko.orb.CORBA.OutputStream out = new org.apache.yoko.orb.CORBA.OutputStream(
                         buf);
                 out._OB_ORBInstance(orbInstance_);
                 org.omg.CORBA.TypeCode t = remarshalValue(origTC, out);
                 org.apache.yoko.orb.CORBA.InputStream in = (org.apache.yoko.orb.CORBA.InputStream) out
                         .create_input_stream();
                 Assert._OB_assert(obAny != null);
                 obAny.replace(t, in);
                 return;
             }
         }
 
         Assert._OB_assert(false); // should never reach this point
     }
 
     public void beginValue() {
         Header h = new Header();
         h.tag = in_.read_long();
         logger.fine("Read tag value " + Integer.toHexString(h.tag)); 
         Assert._OB_assert(h.tag != 0 && h.tag != -1);
 
         initHeader(h);
     }
 
     public void endValue() {
         skipChunk();
     }
 
     public void checkChunk() {
         if (!chunkState_.chunked)
             return;
         
 //      logger.finest("Checking chunk position.  end=" + (chunkState_.chunkStart + chunkState_.chunkSize) + " buffer position=" + buf_.pos_); 
         //
         // If we've reached the end of the current chunk, then check
         // for the start of a new chunk
         //
         if (chunkState_.chunkStart > 0 && chunkState_.chunkStart + chunkState_.chunkSize == buf_.pos_)
         {
 //          logger.finest("Reading chunk from check chunk"); 
             readChunk(chunkState_);
         }
     }
 }
