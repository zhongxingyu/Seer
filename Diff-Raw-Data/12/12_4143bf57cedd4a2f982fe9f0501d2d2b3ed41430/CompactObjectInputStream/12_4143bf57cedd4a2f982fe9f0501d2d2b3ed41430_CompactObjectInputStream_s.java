 package org.kari.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectStreamClass;
 import java.util.concurrent.ConcurrentHashMap;
 
 public final class CompactObjectInputStream
     extends ObjectInputStream
 {
     static final class TypeKey {
         private int mCode;
         private byte[] mEncodedSuffix;
         private short mSuffixLen;
         private int mHashCode;
         
         /**
          * @param pClone if true copy of pEncodedSuffix is done
          */
         public TypeKey(
                 int pCode, 
                 byte[] pEncodedSuffix,
                 short pSuffixLen,
                 boolean pClone)
         {
             set(pCode, pEncodedSuffix, pSuffixLen, pClone);
         }
         
         void set(
                 int pCode, 
                 byte[] pEncodedSuffix,
                 short pSuffixLen,
                 boolean pClone) 
         {
             mCode = pCode;
             if (pClone) {
                 mEncodedSuffix = new byte[pSuffixLen];
                 System.arraycopy(pEncodedSuffix, 0, mEncodedSuffix, 0, pSuffixLen);
             } else {
                 mEncodedSuffix = pEncodedSuffix;
             }
             mSuffixLen = pSuffixLen;
             
             int hash = pCode;
             for (int i = 0; i < pSuffixLen; i++) {
                 hash = 31 * hash + pEncodedSuffix[i];
             }
             mHashCode = hash;
         }
 
         @Override
         public int hashCode() {
             return mHashCode;
         }
 
         @Override
         public boolean equals(Object pObj) {
             boolean result = pObj != null
                 && pObj.getClass() == getClass()
                 && mCode == ((TypeKey)pObj).mCode;
             if (result) {
                 byte[] encodedB = ((TypeKey)pObj).mEncodedSuffix;
                 for (int i = mSuffixLen - 1; result && i >= 0; i--) {
                     result = mEncodedSuffix[i] == encodedB[i];
                 }
             }
             
             return result;
         }
     }
     
     private static final ConcurrentHashMap<TypeKey, Class> CACHED_TYPES = new ConcurrentHashMap<TypeKey, Class>();
     private static final byte[] EMPTY_SUFFIX = CompactObjectOutputStream.EMPTY_SUFFIX;
 
     
     private final TypeKey mKey = new TypeKey(0, EMPTY_SUFFIX, (short)0, false);
     private byte[] mSuffixBuffer = EMPTY_SUFFIX;
 
 
     public CompactObjectInputStream()
         throws IOException,
             SecurityException
     {
         super();
     }
 
     public CompactObjectInputStream(InputStream pIn)
         throws IOException
     {
         super(pIn);
     }
 
     /**
      * No magic header
      */
     @Override
     protected void readStreamHeader() throws IOException {
         // nothing
     }
 
     @Override
     protected ObjectStreamClass readClassDescriptor()
         throws IOException,
             ClassNotFoundException
     {
         final byte id = readByte();
 
         Class cls = CompactObjectOutputStream.CODE_TO_TYPE.get(id);
         if (cls != null) {
             // ok fixed type
         } else {
             short suffixLen = readShort();
             byte[] encodedSuffix = mSuffixBuffer;
             if (encodedSuffix.length < suffixLen){
                 encodedSuffix = new byte[20 + suffixLen];
                 mSuffixBuffer = encodedSuffix;
             }
             read(encodedSuffix, 0, suffixLen);
 
             mKey.set(id, encodedSuffix, suffixLen, false);
 
             cls = CACHED_TYPES.get(mKey);
 
             if (cls == null) {
                 String prefix = CompactObjectOutputStream.PREFIX_VALUES.get(id);
     
                 String suffix = new String(encodedSuffix, 0, suffixLen);
                 cls = Class.forName(prefix != null ? prefix + suffix : suffix);
                 CACHED_TYPES.putIfAbsent(
                         new TypeKey(id, encodedSuffix, suffixLen, true), 
                         cls);
             }
         }
         
        return ObjectStreamClass.lookup(cls);
     }
     
     /**
      * <p>NOTE KI there is never remote class loading
      */
     @Override
     protected Class<?> resolveClass(ObjectStreamClass pDesc)
         throws IOException, ClassNotFoundException
     {
         return pDesc.forClass();
     }
 
 }
