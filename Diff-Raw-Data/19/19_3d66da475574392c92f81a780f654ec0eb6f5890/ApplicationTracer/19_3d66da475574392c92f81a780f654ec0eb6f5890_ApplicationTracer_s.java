 // Copyright 2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.trace.application;
 
 import java.io.Serializable;
 import java.lang.reflect.Member;
 import java.lang.reflect.Proxy;
 
 import org.joe_e.Struct;
 import org.joe_e.array.IntArray;
 import org.joe_e.array.PowerlessArray;
 import org.ref_send.log.CallSite;
 import org.ref_send.log.Trace;
 import org.waterken.trace.Tracer;
 
 /**
  * Produces a stack trace composed of only calls initiated by application code.
  */
 public final class
 ApplicationTracer {
    private ApplicationTracer() {}
 
     /**
      * Constructs an instance.
      * @param code  application class loader
      */
     static public Tracer
     make(final ClassLoader code) {
         class TracerX extends Struct implements Tracer, Serializable {
             static private final long serialVersionUID = 1L;
             
             public long
             timestamp() { return System.currentTimeMillis(); }
             
             public String
             readException(final Throwable e) { return e.getMessage(); }
 
             public Trace
             traceException(final Throwable e) {return trace(e.getStackTrace());}
             
             public Trace
             traceMember(final Member lambda) {
                 return trace(new StackTraceElement(
                     lambda.getDeclaringClass().getName(), lambda.getName(),
                     null, -1));
                 // TODO: want the line number of the method declaration
             }
 
             public Trace
             traceHere() {return trace(Thread.currentThread().getStackTrace());}
             
             private Trace
             trace(final StackTraceElement... frames) {
                 final PowerlessArray.Builder<CallSite> sites =
                     PowerlessArray.builder(frames.length);
                 for (final StackTraceElement frame : frames) {
                     final int line = frame.getLineNumber();
                     if (1 == line) { continue; }    // skip synthetic method
                     
                     // Is this frame from application code or system code?
                     boolean included = false;
                     try {
                         final Class<?> c = code.loadClass(frame.getClassName());
                         if (!Proxy.isProxyClass(c)) {
                             final ClassLoader lib = c.getClassLoader();
                             final ClassLoader sys =
                                 ApplicationTracer.class.getClassLoader();
                             for (ClassLoader i=code; i!=sys; i=i.getParent()) {
                                 if (lib == i) {
                                     included = true;
                                     break;
                                 }
                             }
                         }
                    } catch (final ClassNotFoundException e) {}
                     if (!included) { continue; }
                     
                     // Describe the application stack frame.
                     final String name; {
                         final String fqn = frame.getClassName();
                         final String cn = fqn.substring(fqn.lastIndexOf('.')+1);
                         final String sn = cn.replace("$1",".").replace("$",".");
                         name = sn + "." + frame.getMethodName();
                     }
                     final String source; {
                         final String fqn = frame.getClassName();
                         final int end = fqn.indexOf('$');
                         final String top = -1==end ? fqn : fqn.substring(0,end);
                         source = top.replace('.', '/') + ".java";
                     }
                     sites.append(new CallSite(name, source, line > 1
                         ? PowerlessArray.array(IntArray.array(line)) : null));
                 }
                 return sites.length() != 0 ? new Trace(sites.snapshot()) : null;
             }
         }
         return new TracerX();
     }
 }
