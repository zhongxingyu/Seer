 package com.github.zhongl.nij.btrace;
 
 import static com.sun.btrace.BTraceUtils.*;
 
 import java.text.MessageFormat;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 
 import com.sun.btrace.annotations.*;
 
 /**
  * This sample collects histogram of javax.swing.JComponets created by traced app. The histogram is printed once every 4
  * seconds.
  */
 @BTrace
 public class Profile {
   private static Map<String, AtomicInteger> histo = Collections.newHashMap();
   private static AtomicLong lastTime = newAtomicLong(0L);
   private static AtomicLong durations = newAtomicLong(0L);
   private static AtomicLong count = newAtomicLong(0L);
   private static AtomicInteger registered = newAtomicInteger(0);
 
   @OnMethod(clazz = "/.*/", method = "accept")
   public static void accept(@Self Object obj) {
     long current = System.nanoTime();
     long last = lastTime.getAndSet(current);
     if (last == 0) return;
     durations.addAndGet(current - last);
     count.getAndIncrement();
   }
 
   @OnMethod(clazz = "/.*/", method = "registeredKeys", location = @Location(Kind.RETURN))
  public static void registered(@Self Object obj, int result) {
     registered.set(result);
   }
 
   @OnTimer(1000)
   public static void print() {
     if (count.get() == 0) return;
     String status = MessageFormat
         .format("count: {0}, accept avg: {1} ns, registered cur: {2}", count.get(), (durations.get() / count
             .get()), registered.get());
     System.out.println(status);
   }
 }
