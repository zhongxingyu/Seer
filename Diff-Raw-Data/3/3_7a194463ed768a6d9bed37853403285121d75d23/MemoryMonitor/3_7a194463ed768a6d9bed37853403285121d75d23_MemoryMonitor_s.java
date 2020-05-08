 package org.jvnet.hudson;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Encapsulates how to compute {@link MemoryUsage}. 
  *
  * @author Kohsuke Kawaguchi
  */
 public abstract class MemoryMonitor {
     /**
      * Obtains the memory usage statistics.
      *
      * @return
      *      always non-null object.
      * @throws IOException
      *      If the computation fails for some reason.
      */
     public abstract MemoryUsage monitor() throws IOException;
 
     /**
      * Obtains the {@link MemoryMonitor} implementation suitable
      * for the current platform.
      *
      * @throws IOException
      *      if no applicable implementation is found.
      */
     public static MemoryMonitor get() throws IOException {
         if(INSTANCE==null)
             INSTANCE = obtain();
         return INSTANCE;
     }
 
     private static MemoryMonitor obtain() throws IOException {
         if(File.pathSeparatorChar==';')
             return new Windows();
 
         if(new File("/proc/meminfo").exists())
             return new ProcMemInfo();   // Linux has this. Exactly since when, I don't know.
 
         // is 'top' available? if so, use it
         try {
             Top top = new Top();
             top.monitor();
             return top;
         } catch (Throwable _) {
             // fall through next
         }
 
         // Solaris?
         try {
             Solaris solaris = new Solaris();
             solaris.monitor();
             return solaris;
         } catch(Throwable _) {
             // next
         }
 
        throw new IOException("No suitable implementation found");
     }
 
     /**
      * Main for test
      */
     public static void main(String[] args) throws Exception {
         System.out.println(new Solaris().monitor());
     }
 
     private static volatile MemoryMonitor INSTANCE = null;
 }
