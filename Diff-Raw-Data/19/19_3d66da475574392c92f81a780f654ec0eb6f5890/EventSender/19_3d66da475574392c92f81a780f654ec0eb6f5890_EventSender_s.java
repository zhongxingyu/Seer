 // Copyright 2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.trace;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 
 import org.joe_e.reflect.Reflection;
 import org.ref_send.log.Comment;
 import org.ref_send.log.Event;
 import org.ref_send.log.Fulfilled;
 import org.ref_send.log.Got;
 import org.ref_send.log.Problem;
 import org.ref_send.log.Progressed;
 import org.ref_send.log.Rejected;
 import org.ref_send.log.Resolved;
 import org.ref_send.log.Returned;
 import org.ref_send.log.Sent;
 import org.ref_send.log.SentIf;
 import org.ref_send.promise.Log;
 import org.ref_send.promise.Receiver;
 
 /**
  * Event logging infrastructure.
  */
 public final class
 EventSender {
    private EventSender() {}
     
     /**
      * Constructs a log event generator.
      * @param stderr    log event output factory
      * @param mark      event counter
      * @param tracer    stack tracer
      */
     static public Log
     make(final Receiver<Event> stderr, final Marker mark, final Tracer tracer) {
         class LogX extends Log {
             static private final long serialVersionUID = 1L;
 
             public @Override void
             comment(final String text) {
                 stderr.apply(new Comment(mark.apply(), tracer.timestamp(),
                                          tracer.traceHere(), text));
             }
             
             public @Override void
             problem(final Exception reason) {
                 stderr.apply(new Problem(mark.apply(), tracer.timestamp(),
                                          tracer.traceException(reason),
                                          tracer.readException(reason), reason));
             }
 
             public @Override void
             got(final String message, final Class<?> concrete, Method method) {
                 if (null != concrete && null != method &&
                         !Modifier.isStatic(method.getModifiers())) {
                     try {
                         method = Reflection.method(concrete, method.getName(),
                                                    method.getParameterTypes());
                    } catch (final NoSuchMethodException e) {}
                 }
                 stderr.apply(new Got(mark.apply(), tracer.timestamp(),
                     null!=method ? tracer.traceMember(method) : null, message));
             }
 
             public @Override void
             sent(final String message) {
                 stderr.apply(new Sent(mark.apply(), tracer.timestamp(),
                                       tracer.traceHere(), message));
             }
 
             public @Override void
             returned(final String message) {
                 stderr.apply(new Returned(mark.apply(), tracer.timestamp(),
                                           null, message));
             }
 
             protected @Override void
             sentIf(final String message, final String condition) {
                 stderr.apply(new SentIf(mark.apply(), tracer.timestamp(),
                                         tracer.traceHere(),message, condition));
             }
 
             protected @Override void
             resolved(final String condition) {
                 stderr.apply(new Resolved(mark.apply(), tracer.timestamp(),
                                           tracer.traceHere(), condition));
             }
 
             protected @Override void
             fulfilled(final String condition) {
                 stderr.apply(new Fulfilled(mark.apply(), tracer.timestamp(),
                                            tracer.traceHere(), condition));
             }
             
             protected @Override void
             rejected(final String condition, final Exception reason) {
                 stderr.apply(new Rejected(mark.apply(), tracer.timestamp(),
                                           tracer.traceHere(),condition,reason));
             }
             
             protected @Override void
             progressed(final String condition) {
                 stderr.apply(new Progressed(mark.apply(), tracer.timestamp(),
                                             tracer.traceHere(), condition));
             }
         }
         return new LogX();
     }
 }
