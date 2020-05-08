 package org.jdogma.concurrency;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 public class ThreadAccessController implements Serializable {
     final Set<Thread> seenThreads = Collections.synchronizedSet(new HashSet<Thread>());
     final StackTraceElement stackTraceAtConstruction;
    final StackTraceElement stackTraceAtConstruction2;
     Thread lastThread;
 
     public ThreadAccessController() {
         try
         {
             throw new Exception();
         }
         catch ( Exception e )
         {
            final StackTraceElement[] stackTraceElements = e.getStackTrace();
            stackTraceAtConstruction = stackTraceElements[2];
            stackTraceAtConstruction2 = stackTraceElements[3];
         }
     }
 
     public void checkThreadAccess() {
         final Thread thread = Thread.currentThread();
         if (seenThreads.contains(thread)) {
             if (!thread.equals(lastThread)) {
                 throw new UnsafeCollectionAccessException("The thread is accessing this collection intertwined with others, constructed at:" + getStackTrace());
             }
         } else {
             seenThreads.add(thread);
         }
         lastThread = thread;
     }
     public void checkWriteAccess() {
         checkThreadAccess();
     }
 
     private String getStackTrace(){
         StringBuilder result = new StringBuilder( );
         result.append(stackTraceAtConstruction.toString());
         result.append( "\n");
        result.append(stackTraceAtConstruction2.toString());
        result.append( "\n");
         return result.toString();
     }
 }
