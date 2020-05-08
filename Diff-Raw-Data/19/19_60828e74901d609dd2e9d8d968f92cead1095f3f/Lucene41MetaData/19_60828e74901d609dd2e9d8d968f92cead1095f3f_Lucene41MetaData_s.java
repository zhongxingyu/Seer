 package org.apache.lucene.codecs.lucene41;
 
 import static org.apache.lucene.codecs.lucene41.Lucene41PostingsFormat.BLOCK_SIZE;
 import static org.apache.lucene.codecs.lucene41.ForUtil.MAX_DATA_SIZE;
 import static org.apache.lucene.codecs.lucene41.ForUtil.MAX_ENCODED_SIZE;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import org.apache.lucene.codecs.BlockTermState;
 import org.apache.lucene.codecs.TermMetaData;
 import org.apache.lucene.codecs.TermProtoData;
 import org.apache.lucene.index.TermState;
 import org.apache.lucene.index.FieldInfo;
 import org.apache.lucene.index.FieldInfo.IndexOptions;
 import org.apache.lucene.store.ByteArrayDataInput;
 import org.apache.lucene.util.IOUtils;
 
 final class Lucene41MetaData extends TermMetaData {
   // NOTE: Only used for reader side
   // NOTE: Only used by the "primary" MetaData -- clones don't
   // copy this (basically they are "transient"):
   ByteArrayDataInput bytesReader;  // TODO: should this NOT be in the TermState...?
   byte[] bytes;
 
   public Lucene41MetaData(long docStartFP, long posStartFP, long payStartFP, long skipOffset, long lastPosBlockOffset, int singletonDocID) {
    super(posStartFP != -1 ? 3 : 1, posStartFP != -1 ? 20 : 4);
     setDocFP(docStartFP);
     setSingletonDocID(singletonDocID);
    if (posStartFP != -1) {
       setPosFP(posStartFP);
       setPayFP(payStartFP);
       setSkipOffset(skipOffset);
       setLastPosBlockOffset(lastPosBlockOffset);
    }
   }
   public Lucene41MetaData(FieldInfo info) {
     this(info.getIndexOptions().compareTo(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS) >= 0);
   }
   public Lucene41MetaData(boolean hasPositions) {
    super(hasPositions ? 3 : 1, hasPositions ? 20 : 4);
   }
 
   public void setSingletonDocID(int singletonDocID) {
     buffer.putInt(0, singletonDocID);
   }
   public void setSkipOffset(long skipOffset) {
     buffer.putLong(4, skipOffset);
   }
   public void setLastPosBlockOffset(long lastPosBlockOffset) {
     buffer.putLong(12, lastPosBlockOffset);
   }
   public void setDocFP(long docFP) {
     base.longs[0] = docFP;
   }
   public void setPosFP(long posFP) {
     base.longs[1] = posFP;
   }
   public void setPayFP(long payFP) {
     base.longs[2] = payFP;
   }
 
   public int singletonDocID() {
     return buffer.getInt(0);
   }
   public long skipOffset() {
     return buffer.getLong(4);
   }
   public long lastPosBlockOffset() {
     return buffer.getLong(12);
   }
   public long docFP() {
     return base.longs[0];
   }
   public long posFP() {
     return base.longs[1];
   }
   public long payFP() {
     return base.longs[2];
   }
 
 
   /*
   public Lucene41MetaData clone() {
     Lucene41MetaData other = new Lucene41MetaData();
     other.copyFrom(this);
     return other;
   }
 
   public void copyFrom(TermMetaData _other) {
     Lucene41MetaData other = (Lucene41MetaData) _other;
     this.base = LongsRef.deepCopyOf(other.base);
     this.extend = BytesRef.deepCopyOf(other.extend);
 
     // Do not copy buffered postings bytes & bytesReader 
     // (else TermMetaData is very heavy, ie drags around the 
     // entire block's byte[]).  On seek back, if next() is in fact 
     // used (rare!), they will be re-read from disk.
   }*/
 
   @Override
   public String toString() {
     return super.toString() + " docStartFP=" + docFP() + " posStartFP=" + posFP() + " payStartFP=" + payFP() + " lastPosBlockOffset=" + lastPosBlockOffset() + " singletonDocID=" + singletonDocID();
   }
 }
