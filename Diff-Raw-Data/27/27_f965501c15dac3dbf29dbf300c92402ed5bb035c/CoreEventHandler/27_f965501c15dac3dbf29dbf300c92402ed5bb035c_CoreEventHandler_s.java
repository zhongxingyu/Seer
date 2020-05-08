 /*
  * Copyright (c) 2008-2010, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  * Licensed under the terms of the New BSD License.
  */
 package chord.runtime;
 
 import java.io.IOException;
 
 import chord.instr.InstrScheme;
 import chord.util.WeakIdentityHashMap;
 import chord.util.ByteBufferedFile;
 
 /**
  * Core handler of events generated during an instrumented program's
  * execution.
  * 
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 public class CoreEventHandler {
 	public final static String EVENT_HANDLER_CLASS_KEY = "event_handler_class_name";
 	/**
 	 * Flag determining when it is safe to start handling events at runtime.
 	 * It is false when the JVM starts.  It is set to true in the
	 * {@link #open(String)} method which is called by the handler for the
 	 * JVMTI event "VMInit" (see file main/src/agent/chord_instr_agent.cpp
 	 * for the definition of this handler).
 	 */
     protected static boolean trace = false;
 
 	/**
 	 * A buffer used to buffer events sent from event-generating JVM to
 	 * event-handling JVM.
 	 * It is irrelevant if events are generated/handled by the same JVM.
 	 */ 
     protected static ByteBufferedFile buffer;
 
 	/**
 	 * Unique ID given to each object created at runtime.
 	 * ID 0 is reserved for null and ID 1 is reserved for the hypothetical
 	 * lone object of a hypothetical class all of whose instance fields are
 	 * static fields in other real classes.
 	 */
     protected static int currentId = 2;
 
     protected static WeakIdentityHashMap objmap;
 
 	// Note: CALLER MUST SYNCHRONIZE!
     public static int getObjectId(Object o) {
     	if (o == null)
     		return 0;
         Object val = objmap.get(o);
         if (val == null) {
             val = currentId++;
             objmap.put(o, val);
         }
         return (Integer) val;
     }
 
     public static long getPrimitiveId(int oId, int fId) {
         // We must add 1 below so that we never assign to a field an
         // identifier smaller than (1 << 32).
     	long l = oId + 1;
     	l = l << 32;
     	return l + fId;
     }
 
 	/**
 	 * This method is called during handing of JVMTI event "VMInit".
 	 * arguments: trace_file_name, trace_block_size
 	 * if trace_file_name is absent then buffer is not created (i.e. it is
 	 * assumed that dynamic analysis is intra-JVM).
 	 */
 	public synchronized static void init(String args) {
 		String[] a = args.split("=");
         int traceBlockSize = 4096;
         String traceFileName = null;
         for (int i = 0; i < a.length; i += 2) {
             String k = a[i];
             if (k.equals("trace_block_size"))
                 traceBlockSize = Integer.parseInt(a[i+1]);
             else if (k.equals("trace_file_name"))
                 traceFileName = a[i+1];
         }
 		if (traceFileName != null) {
 			try {
 				buffer = new ByteBufferedFile(traceBlockSize, traceFileName, false);
 			} catch (IOException ex) { throw new RuntimeException(ex); }
 		}
 		objmap = new WeakIdentityHashMap();
         trace = true;
 	}
 
 	// called during VMDeath JVMTI event
     // DO NOT REMOVE THIS SYNCHRONIZATION
     public synchronized static void done() {
         trace = false;
 		if (buffer != null) {
 			try {
 				buffer.flush();
 			} catch (IOException ex) { throw new RuntimeException(ex); }
 		}
     }
 }
 
