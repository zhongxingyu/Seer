 package com.foursquare.heapaudit;
 
 import java.util.HashMap;
 import java.util.concurrent.ConcurrentHashMap;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodAdapter;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 
 public abstract class HeapUtil {
 
     public static void log(String text) {
 
         HeapSettings.output.println(text);
 
     }
 
     public static void instrumentation(boolean debug,
                                        String text) {
 
         if (debug) {
 
             log("\t" + text);
 
         }
 
     }
 
     public static void execution(boolean trace,
                                  MethodAdapter mv,
                                  String text) {
 
         if (trace) {
 
             // STACK [...]
             mv.visitLdcInsn(text);
             // STACK [...|text]
             mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                                "com/foursquare/heapaudit/HeapUtil",
                                "log",
                                "(Ljava/lang/String;)V");
             // STACK [...]
 
         }
 
     }
 
     protected static void visitCheck(MethodVisitor mv,
                                      Label cleanup) {
 
         // STACK: [...]
         mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                            "com/foursquare/heapaudit/HeapRecorder",
                            "hasRecorders",
                            "()Z");
         // STACK: [...|status]
         mv.visitJumpInsn(Opcodes.IFEQ,
                          cleanup);
         // STACK: [...]
 
     }
 
     protected static void visitCleanup(MethodVisitor mv,
                                        Label cleanup,
                                        Label finish) {
 
         // STACK: [...]
         mv.visitJumpInsn(Opcodes.GOTO,
                          finish);
         // STACK: [...]
         mv.visitLabel(cleanup);
         // STACK: [...]
 
     }
 
     protected static void visitFinish(MethodVisitor mv,
                                       Label finish) {
 
         // STACK: [...]
         mv.visitLabel(finish);
         // STACK: [...]
 
     }
 
     // The following holds a cache of type to size mappings.
 
     private static ConcurrentHashMap<String, Long> sizes = new ConcurrentHashMap<String, Long>();
 
     private static long sizeOf(Object obj,
                                String type) {
 
         Long size = sizes.get(type);
 
         if (size == null) {
 
             size = HeapRecorder.instrumentation.getObjectSize(obj);
 
             sizes.put(type,
                       size);
 
         }
 
         return size;
 
     }
 
     public static void record(Object obj,
                               int count,
                               String type,
                               long size) {
 
        if (size < 0) {
 
            if (HeapRecorder.suppress() != null) {
 
                size = sizeOf(obj,
                              "" + count + type);
 
            }
 
             HeapRecorder.unwind();
 
         }
 
        record(count,
               type,
               size);

     }
 
     public static void record(Object obj,
                               String type) {
 
         if (type.charAt(0) != '[') {
 
             record(obj,
                    -1,
                    type,
                    -1);
 
         }
         else if (HeapRecorder.suppress() != null) {
 
             long overhead = 0;
 
             Object[] o = (Object[])obj;
 
             int length = o.length;
 
             int count = length;
 
             for (int i = 1; i < type.length(); ++i) {
 
                 if (type.charAt(i) == '[') {
 
                     // The following assumes the size of array of array,
                     // including the overhead of the array bookkeeping itself
                     // is only affected by the number of elements, not the
                     // actual element type.
 
                     overhead += sizeOf(o,
                                        "" + length + "[[L");
 
                     switch (type.charAt(i + 1)) {
 
                     case 'Z':
 
                         length = ((boolean[])o[0]).length;
 
                         break;
 
                     case 'B':
 
                         length = ((byte[])o[0]).length;
 
                         break;
 
                     case 'C':
 
                         length = ((char[])o[0]).length;
 
                         break;
 
                     case 'S':
 
                         length = ((short[])o[0]).length;
 
                         break;
 
                     case 'I':
 
                         length = ((int[])o[0]).length;
 
                         break;
 
                     case 'J':
 
                         length = ((long[])o[0]).length;
 
                         break;
 
                     case 'F':
 
                         length = ((float[])o[0]).length;
 
                         break;
 
                     case 'D':
 
                         length = ((double[])o[0]).length;
 
                         break;
 
                     case 'L':
 
                         length = ((Object[])o[0]).length;
 
                         break;
 
                     default:
 
                         o = (Object[])(o[0]);
 
                         length = o.length;
 
                         count *= length;
 
                     }
 
                 }
                 else {
 
                     String name = type.substring(i);
 
                     long size = overhead + count * sizeOf(o[0],
                                                           "" + length + type.substring(i - 1));
 
                     HeapRecorder.unwind();
 
                     record(obj,
                            count * length,
                            name,
                            size);
 
                     break;
                 }
 
             }
 
         }
         else {
 
             HeapRecorder.unwind();
 
         }
 
     }
 
     public static void record(Object obj,
                               int count,
                               String type) {
 
         if (HeapRecorder.suppress() != null) {
 
             long size = sizeOf(obj,
                                "" + count + "[" + type);
 
             HeapRecorder.unwind();
 
             record(obj,
                    count,
                    type,
                    size);
 
         }
         else {
 
             HeapRecorder.unwind();
 
         }
 
     }
 
     public static void record(Object obj,
                               int[] dimensions,
                               String type) {
 
 	if (HeapRecorder.suppress() != null) {
 
             long overhead = 0;
 
             Object o[] = (Object[])obj;
 
             int count = 1;
 
             for (int i = 0; i < dimensions.length - 1 && count > 0; ++i) {
 
                 int length = dimensions[i];
 
                 if (length >= 0) {
 
                     // The following assumes the size of array of array, including
                     // the overhead of the array bookkeeping itself is only affected
                     // by the number of elements, not the actual element type.
 
                     overhead += sizeOf(o,
                                        "" + length + "[[L");
 
                     o = (Object[])(o[0]);
 
                 }
 
                 count *= length;
 
             }
 
             if (count > 0) {
 
                 int length = dimensions[dimensions.length - 1];
 
                 long size = overhead + count * sizeOf(o,
                                                       "" + length + "[" + type);
 
                 HeapRecorder.unwind();
 
                 record(obj,
                        count * length,
                        type,
                        size);
 
             }
             else {
 
                 HeapRecorder.unwind();
 
             }
 
         }
         else {
 
             HeapRecorder.unwind();
 
         }
 
     }
 
     public static void record(int count,
                               String type,
                               long size) {
 
         Object context = HeapRecorder.suppress();
 
         if (context != null) {
 
             try {
 
                 for (HeapRecorder recorder: HeapRecorder.getRecorders(context)) {
 
                     recorder.record(type,
                                     count,
                                     size);
 
                 }
 
             } catch (Exception e) {
 
                 System.err.println(e);
 
             }
 
         }
 
         HeapRecorder.unwind();
 
     }
 
     private final static HashMap<String, HeapQuantile> recorders = new HashMap<String, HeapQuantile>();
 
     public static boolean inject(String id) {
 
         HeapRecorder.suppress();
 
         if (recorders.containsKey(id)) {
 
             log("Recorder already exists for " + id);
 
             return false;
 
         }
 
         log(id);
 
         recorders.put(id,
                       new HeapQuantile());
 
         HeapRecorder.unwind();
 
         return true;
 
     }
 
     public static boolean remove(String id) {
 
         HeapRecorder.suppress();
 
         HeapQuantile recorder = recorders.remove(id);
 
         if (recorder == null) {
 
             log("Recorder does not exist for " + id);
 
             return false;
 
         }
 
         HeapSettings.output.println(recorder.summarize(true,
                                                        id));
 
         HeapRecorder.unwind();
 
         return true;
 
     }
 
     public static void register(String id) {
 
         HeapRecorder.suppress();
 
         HeapQuantile recorder = recorders.get(id);
 
         if (recorder != null) {
 
             HeapRecorder.register(recorder);
 
         }
 
         HeapRecorder.unwind();
 
     }
 
     public static void unregister(String id) {
 
         HeapRecorder.suppress();
 
         HeapQuantile recorder = recorders.get(id);
 
         if (recorder != null) {
 
             HeapRecorder.unregister(recorder);
 
         }
 
         HeapRecorder.unwind();
 
     }
 
 }
