 package org.kari.io;
 
 import gnu.trove.map.TIntObjectMap;
 import gnu.trove.map.TObjectIntMap;
 import gnu.trove.map.hash.TIntObjectHashMap;
 import gnu.trove.map.hash.TObjectIntHashMap;
 import gnu.trove.procedure.TIntObjectProcedure;
 import gnu.trove.set.hash.THashSet;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.ObjectStreamClass;
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 import java.rmi.dgc.VMID;
 import java.rmi.server.UID;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Outputstream with more compact format
  * 
  * @author kari
  */
 public final class CompactObjectOutputStream
     extends ObjectOutputStream
 {
     public static final byte TYPE_PLAIN = 0;
     public static final byte TYPE_CLASS_ID = 1;
 
     public static final Charset SUFFIX_ENCODING = Charset.forName("UTF8");
     
     public static final Class ARRAY_CLASS = Object[].class;
     public static final TIntObjectMap<Class> CODE_TO_TYPE = new TIntObjectHashMap<Class>();
     public static final TObjectIntMap<Class> TYPE_TO_CODE = new TObjectIntHashMap<Class>();
 
     public static final TIntObjectMap<String> PREFIX_VALUES = new TIntObjectHashMap<String>();
     public static final int[] PREFIX_IDS;
     static {
         {
             char BASE = 'A';
             PREFIX_VALUES.put(BASE++, "org.kari.");
             PREFIX_VALUES.put(BASE++, "java.rmi.");
             PREFIX_VALUES.put(BASE++, "java.lang.");
             PREFIX_VALUES.put(BASE++, "java.util.");
             PREFIX_VALUES.put(BASE++, "java.io.");
             PREFIX_VALUES.put(BASE++, "java.");
             PREFIX_VALUES.put(BASE++, "org.trove.");
             PREFIX_VALUES.put(BASE++, "org.google.");
     
             PREFIX_IDS = PREFIX_VALUES.keys();
             Arrays.sort(PREFIX_IDS);
         }
         
         {
             char BASE = 'Z';
             CODE_TO_TYPE.put(BASE++, Throwable.class);
             CODE_TO_TYPE.put(BASE++, Error.class);
             CODE_TO_TYPE.put(BASE++, RuntimeException.class);
             CODE_TO_TYPE.put(BASE++, Exception.class);
             CODE_TO_TYPE.put(BASE++, UID.class);
             CODE_TO_TYPE.put(BASE++, VMID.class);
             CODE_TO_TYPE.put(BASE++, Object.class);
             CODE_TO_TYPE.put(BASE++, Integer.class);
             CODE_TO_TYPE.put(BASE++, Number.class);
             CODE_TO_TYPE.put(BASE++, Long.class);
             CODE_TO_TYPE.put(BASE++, byte[].class);
             CODE_TO_TYPE.put(BASE++, int[].class);
             CODE_TO_TYPE.put(BASE++, long[].class);
             CODE_TO_TYPE.put(BASE++, Object[].class);
             CODE_TO_TYPE.put(BASE++, String[].class);
             CODE_TO_TYPE.put(BASE++, StackTraceElement.class);
             CODE_TO_TYPE.put(BASE++, StackTraceElement[].class);
             CODE_TO_TYPE.put(BASE++, List.class);
             CODE_TO_TYPE.put(BASE++, Set.class);
             CODE_TO_TYPE.put(BASE++, Map.class);
             CODE_TO_TYPE.put(BASE++, ArrayList.class);
             CODE_TO_TYPE.put(BASE++, HashSet.class);
             CODE_TO_TYPE.put(BASE++, THashSet.class);
         }
         
         CODE_TO_TYPE.forEachEntry(new TIntObjectProcedure<Class>() {
             @Override
             public boolean execute(int pCode, Class pType) {
                 TYPE_TO_CODE.put(pType, pCode);
                 return true;
             }
         });
     }
 
     public CompactObjectOutputStream()
         throws IOException,
             SecurityException
     {
         super();
     }
 
     public CompactObjectOutputStream(OutputStream pOut)
         throws IOException
     {
         super(pOut);
     }
 
     /**
      * No magic header
      */
     @Override
     protected void writeStreamHeader() throws IOException {
         // nothing
     }
 
     @Override
     protected void writeClassDescriptor(ObjectStreamClass desc)
         throws IOException
     {
         final Class<?> cls = desc.forClass();
 
         byte[] encodedSuffix = null;
         int classId = 0;
         int code = TYPE_TO_CODE.get(cls);
 
         if (code != 0) {
             // ok fixed type
         } else {
             String name = cls.getName();
             code = TYPE_PLAIN;
             
             for (int prefixId : PREFIX_IDS) {
                 String prefix = PREFIX_VALUES.get(prefixId);
                 if (name.startsWith(prefix)) {
                     code = (byte)prefixId;
                    name = name.substring(prefix.length());
                     break;
                 }
             }
            encodedSuffix = name.getBytes(SUFFIX_ENCODING);
         }
         
         writeByte( (byte)code );
         if (classId != 0) {
             writeInt(classId);
         }
         
         if (encodedSuffix != null) {
            writeShort(encodedSuffix.length);
            write(encodedSuffix);
         }
     }
 }
