 /*
  * JAHM - Java Advanced Hierarchical Model 
  * 
  * TabularCompressor.java
  * 
  * Copyright 2009 Robert Arvin Dunnagan
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.xmodel.net.bind;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBufferInputStream;
 import org.jboss.netty.channel.Channel;
 import org.xmodel.BreadthFirstIterator;
 import org.xmodel.IModelObject;
 import org.xmodel.compress.CompressorException;
 import org.xmodel.compress.TabularCompressor;
 import org.xmodel.external.ICachingPolicy;
 import org.xmodel.external.IExternalReference;
 import org.xmodel.log.Log;
 import org.xmodel.net.NetworkCachingPolicy;
 
 /**
  * A TabularCompressor that does tracks the network identifiers of remote-bound elements.
  */
 public class BindCompressor extends TabularCompressor
 {
   public final static class BindResult
   {
     public IModelObject element;
     public int netID;
   }
   
   private BindCompressor( BindProtocol protocol, boolean progressive)
   {
     super( progressive);
     
     this.protocol = protocol;
     this.localMap = new ConcurrentHashMap<Integer, IModelObject>();
     this.remoteMap = new ConcurrentHashMap<Integer, IModelObject>();
     this.remoteKeys = new ConcurrentHashMap<IModelObject, Integer>();
   }
   
   /**
    * Reset this instance by releasing internal resources.  This method should be called after 
    * the channel is closed to prevent conflict between protocol traffic and the freeing of resources.
    */
   public void reset()
   {
     log.debugf( "%X.reset()", hashCode());
     localMap.clear();
     remoteMap.clear();
     remoteKeys.clear();
   }
   
   /**
    * Create a new request compressor.
    * @param protocol The protocol bundle.
    * @param progressive See TabularCompressor for more information.
    * @return Returns the new compressor.
    */
   public static BindCompressor newRequestCompressor( BindProtocol protocol, boolean progressive)
   {
     return new BindCompressor( protocol, progressive);
   }
   
   /**
    * Create a new response compressor.
    * @param progressive See TabularCompressor for more information.
    * @return Returns the new compressor.
    */
   public static BindCompressor newResponseCompressor( boolean progressive)
   {
     return new BindCompressor( null, progressive);
   }
   
   /**
    * Set the channel for the next call to the <code>decompress</code> method.
    * @param channel The channel.
    */
   public void setChannel( Channel channel)
   {
     this.channel = channel;
   }
   
   /**
    * Set the timeout for the next call to the <code>decompress</code> method.
    * @param timeout The timeout.
    */
   public void setTimeout( int timeout)
   {
     this.timeout = timeout;
   }
 
   /**
    * Find an element by its local network identifier.
    * @param netID The network identifier.
    * @return Returns null or the element.
    */
   public IModelObject findLocal( int netID)
   {
     return localMap.get( netID);
   }
   
   /**
    * Find an element by its remote network identifier.
    * @param netID The network identifier.
    * @return Returns null or the element.
    */
   public IModelObject findRemote( int netID)
   {
     return remoteMap.get( netID);
   }
 
   /**
    * Returns the local network identifier for the specified element.
    * @param element An element that was previously sent downstream.
    * @return Returns the local network identifier.
    */
   public int getLocalID( IModelObject element)
   {
     return System.identityHashCode( element);
   }
   
   /**
    * Returns the remote network identifier for the specified element.
    * @param element An element that was previously received from upstream.
    * @return Returns the remote network identifier.
    */
   public int getRemoteID( IModelObject element)
   {
     if ( !(element instanceof IExternalReference)) return 0;
     
     NetKeyCachingPolicy cachingPolicy = (NetKeyCachingPolicy)((IExternalReference)element).getCachingPolicy();
     return cachingPolicy.getNetID();
   }
   
   /**
    * Free resources associated with the specified element.
    * @param element The element.
    */
   public void freeLocal( IModelObject element)
   {
     for( IModelObject descendant: new BreadthFirstIterator( element))
     {
       Integer key = getLocalID( descendant);
       if ( key != null) localMap.remove( key);
     }
   }
   
   /**
    * Free resources associated with the specified element.
    * @param element The element.
    */
   public void freeRemote( IModelObject element)
   {
     for( IModelObject descendant: new BreadthFirstIterator( element))
     {
       Integer key = remoteKeys.remove( descendant);
       if ( key == null || remoteMap.remove( key) == null)
        log.warnf( "%X - remote not found (memory leak): %s", hashCode(), element.getType());
     }
     
     //log.infof( "%X.freeRemote( %s) - sizes: %d/%d", hashCode(), element.getType(), remoteMap.size(), remoteKeys.size());
   }
   
   /**
    * Decompress the next element and update the specified reference.
    * @param input The input buffer.
    * @param reference The reference being remotely bound.
    * @return Returns the element that was decompressed.
    */
   public IModelObject decompress( ChannelBuffer input, IExternalReference reference) throws IOException
   {
     DataInputStream stream = new DataInputStream( new ChannelBufferInputStream( input));
     
     // header flags
     int flags = input.readUnsignedByte();
     boolean predefined = (flags & 0x20) != 0;
     
     // table
     if ( !predefined) readTable( stream);
 
     // log
     log.debugf( "%x.decompress(): predefined=%s", hashCode(), predefined);
     
     // content
     return readElement( stream, reference);
   }
 
   /**
    * Read an element from the input stream.  Note that this method does NOT update the reference.
    * @param stream The input stream.
    * @param binding The reference being remotely bound.
    * @return Returns the new element.
    */
   protected IModelObject readElement( DataInputStream stream, IExternalReference binding) throws IOException, CompressorException
   {
     Integer netID = stream.readInt();
     String type = readHash( stream);
     byte flags = stream.readByte();
     
     // create element
     IModelObject element;
     if ( (flags & 0x01) != 0)
     {
       // read static attributes
       int count = readValue( stream);
       String[] attrNames = new String[ count];
       for( int i=0; i<count; i++)
       {
         String attrName = readHash( stream);
         attrNames[ i] = attrName;
       }
       
       // create element
       IExternalReference reference = factory.createExternalObject( null, type);
       NetKeyCachingPolicy cachingPolicy = new NetKeyCachingPolicy( protocol, channel, netID, timeout, attrNames);
       reference.setCachingPolicy( cachingPolicy);
       reference.setDirty( (flags & 0x02) != 0);
       
       element = reference;
     }
     else
     {
       element = factory.createObject( null, type);
     }
     
     if ( binding != null)
     {
       // map element to network identifier
       remoteMap.put( netID, binding);
       remoteKeys.put( binding, netID);
       ((NetworkCachingPolicy)binding.getCachingPolicy()).setRemoteNetID( netID);
     }
     else
     {
       // map element to network identifier
       remoteMap.put( netID, element);
       remoteKeys.put( element, netID);
     }
 
     readAttributes( stream, element);
     readChildren( stream, element);
 
     return element;
   }
   
   /**
    * Read an element from the input stream.
    * @param stream The input stream.
    * @return Returns the new element.
    */
   @Override
   protected IModelObject readElement( DataInputStream stream) throws IOException, CompressorException
   {
     return readElement( stream, null);
   }
   
   /**
    * Write an element to the output stream.
    * @param stream The output stream.
    * @param element The element.
    */
   @Override
   protected void writeElement( DataOutputStream stream, IModelObject element) throws IOException, CompressorException
   {
     IExternalReference reference = (element instanceof IExternalReference)? (IExternalReference)element: null;
     
     // write network id
     int netID = System.identityHashCode( element);
     localMap.put( netID, element);
     stream.writeInt( netID);
 
     // write tag name
     writeHash( stream, element.getType());
     
     // write flags
     byte flags = 0;
     if ( reference != null) flags |= 0x01;
     if ( element.isDirty()) flags |= 0x02;
     stream.writeByte( flags);
     
     // write static attributes
     if ( reference != null)
     {
       ICachingPolicy cachingPolicy = reference.getCachingPolicy();
       if ( cachingPolicy != null)
       {
         String[] attrNames = cachingPolicy.getStaticAttributes();
         writeValue( stream, attrNames.length);
         for( String attrName: attrNames) writeHash( stream, attrName);
       }
     }
    
     // write attributes and children
     if ( element.isDirty())
     {
       ICachingPolicy cachingPolicy = reference.getCachingPolicy();
       String[] staticNames = (cachingPolicy != null)? cachingPolicy.getStaticAttributes(): new String[ 0];
       writeAttributes( stream, element, Arrays.asList( staticNames));
     }
     else
     {
       writeAttributes( stream, element, element.getAttributeNames());
       writeChildren( stream, element);
     }
   }
 
   private final static Log log = Log.getLog( BindCompressor.class);
   
   private BindProtocol protocol;
   private Map<Integer, IModelObject> localMap;
   private Map<Integer, IModelObject> remoteMap;
   private Map<IModelObject, Integer> remoteKeys;
   private Channel channel;
   private int timeout;
 }
