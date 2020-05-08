 /*
  *  Copyright 2011-2012 Mathieu ANCELIN
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 
 package iteratee;
 
 import akka.actor.*;
 import akka.util.Duration;
 import iteratee.F.Function;
 import iteratee.F.Option;
 import iteratee.F.Promise;
 import iteratee.F.Unit;
 import java.io.*;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.*;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 public class Iteratees {
 
     private static interface Akka {
         ActorSystem system();
     }
 
     private static enum AkkaInstance implements Akka {
         AKKA {
             private final ActorSystem context = ActorSystem.create("IterateesSystem");
             public ActorSystem system() {
                 return context;
             }
         }
     }
 
     static {
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 AkkaInstance.AKKA.system().shutdown();
             }
         });
     }
 
     public static ActorSystem system() {
         return AkkaInstance.AKKA.system();
     }
 
     public static final class Elem<I> {
         private final I e;
         public Elem(I e) { this.e = e; }
         public Option<I> get() { return Option.apply(e); }
     }
     public static enum EOF { INSTANCE }
     public static enum Empty { INSTANCE }
     private static enum Run { INSTANCE }
     public static enum Done { INSTANCE }
     public static enum Cont { INSTANCE }
     public static final class Error<E> {
         public final E error;
         public Error(E error) {
             this.error = error;
         }
     }
     private static class ForwarderActor extends UntypedActor {
         private final Forward forward;
         public ForwarderActor(Forward forward) {
             this.forward = forward;
         }
         @Override
         public void onReceive(Object o) throws Exception {
             forward.onReceive(o, sender(), self());
         }
 
     }
     public static interface Forward {
         public void onReceive(Object msg, ActorRef sender, ActorRef self) throws Exception;
     }
     public static abstract class Iteratee<I, O> implements Forward {
         protected Promise<O> promise = new Promise<O>();
         public void done(O result, ActorRef sender, ActorRef self) {
             promise.apply((O) result);
             sender.tell(Done.INSTANCE, self);
             self.tell(PoisonPill.getInstance());
         }
         public Promise<O> getAsyncResult() {
             return promise;
         }
         public static <T> Iteratee<T, Unit> foreach(Function<T, Unit> func) {
             return new ForeachIteratee<T>(func);
         }
         public static <T> Iteratee<Byte[], Unit> toStream(OutputStream os) {
             return new OutputStreamIteratee(os);
         }
         public static <T> Iteratee<T, Unit> ignore() {
             return new IgnoreIteratee<T>();
         }
         public static <T> Iteratee<T, Option<T>> head() {
             return new HeadIteratee<T>();
         }
     }
     public static class OutputStreamIteratee extends Iteratee<Byte[], Unit> {
         public final OutputStream stream;
 
         public OutputStreamIteratee(OutputStream stream) {
             this.stream = stream;
         }
 
         public void onReceive(Object msg, ActorRef sender, ActorRef self) throws Exception {
             for (Elem e : F.caseClassOf(Elem.class, msg)) {
                 Elem<Byte[]> el = (Elem<Byte[]>) e;
                 for (Byte[] s : el.get()) {
                     if (s != null) {
                         try {
                             byte[] b = new byte[s.length];
                             for (int i = 0; i < s.length; i++) {
                                 if (b != null && s[i] != null)
                                     b[i] = s[i];
                             }
                             stream.write(b);
                         } catch (Exception ex) { ex.printStackTrace(); }
                     }
                 }
                 sender.tell(Cont.INSTANCE, self);
             }
             for (EOF e : F.caseClassOf(EOF.class, msg)) {
                 stream.flush();
                 stream.close();
                 done(Unit.unit(), sender, self);
             }
         }
     }
     public static class HeadIteratee<T> extends Iteratee<T, Option<T>> {
         public void onReceive(Object msg, ActorRef sender, ActorRef self) throws Exception {
             for (Elem e : F.caseClassOf(Elem.class, msg)) {
                 for(Object o : e.get()) {
                     Option<T> opt = Option.apply((T) o);
                     done(opt, sender, self);
                 }
             }
             for (EOF e : F.caseClassOf(EOF.class, msg)) {
                 Option<T> opt = Option.none();
                 done(opt, sender, self);
             }
         }
     }
     public static class IgnoreIteratee<T> extends Iteratee<T, Unit> {
         public void onReceive(Object msg, ActorRef sender, ActorRef self) throws Exception {
             for (EOF e : F.caseClassOf(EOF.class, msg)) {
                 done(Unit.unit(), sender, self);
             }
         }
     }
     public static abstract class Enumerator<I> implements Forward {
 
         public void onReceive(Object msg, ActorRef sender, ActorRef self) throws Exception {
             for (Run run : F.caseClassOf(Run.class, msg)) {
                 sendNext(msg, sender, self);
             }
             for (Cont cont : F.caseClassOf(Cont.class, msg)) {
                 sendNext(msg, sender, self);
             }
             for (Done done : F.caseClassOf(Done.class, msg)) {
                 sender.tell(PoisonPill.getInstance(), self);
                 self.tell(PoisonPill.getInstance());
             }
             for (Error err : F.caseClassOf(Error.class, msg)) {
                 sender.tell(PoisonPill.getInstance(), self);
                 System.err.println(err.error);
                 self.tell(PoisonPill.getInstance());
             }
         }
         void sendNext(Object msg, ActorRef sender, ActorRef self) {
             if (!hasNext()) {
                 sender.tell(EOF.INSTANCE, self);
             } else {
                 Option<I> optElemnt = next();
                 for (I element : optElemnt) {
                     sender.tell(new Elem<I>(element), self);
                 }
                 if (optElemnt.isEmpty()) {
                     sender.tell(Empty.INSTANCE, self);
                 }
             }
         }
         public abstract boolean hasNext();
         public abstract Option<I> next();
         void onApply() {
             //System.out.println("on apply from Enumerator");
         }
         ActorRef enumerator;
         ActorRef iteratee;
         public <O> Promise<O> applyOn(Iteratee<I, O> it) {
             Promise<O> res = it.getAsyncResult();
             iteratee = system().actorOf(forwarderActorProps(it), UUID.randomUUID().toString());
             enumerator = system().actorOf(forwarderActorProps(this), UUID.randomUUID().toString());
             enumerator.tell(Run.INSTANCE, iteratee);
             return res;
         }
         public Enumerator<I> andThen(final Enumerator<I> then) {
             final Enumerator<I> and = this;
             return new Enumerator<I>() {
                 @Override
                 public boolean hasNext() {
                     if (!and.hasNext()) {
                         return then.hasNext();
                     } else {
                         return and.hasNext();
                     }
                 }
                 @Override
                 public Option<I> next() {
                     if (!and.hasNext()) {
                         return then.next();
                     } else {
                         return and.next();
                     }
                 }
             };
         }
         public <O> Enumerator<O> through(Enumeratee<I, O>... enumeratees) {
             return new DecoratedEnumerator<O>(this, enumeratees);
         }
         public static <T> Enumerator<T> interleave(Enumerator<T>... enumerators) {
             return new InterleavedEnumerators<T>(enumerators);
         }
         public static <T> Enumerator<T> of(T... args) {
             return new IterableEnumerator(Arrays.asList(args));
         }
         public static <T> Enumerator<T> of(Iterable<T> iterable) {
             return new IterableEnumerator(iterable);
         }
         public static <T> Enumerator<Byte[]> fromStream(InputStream is, int chunkSize) {
             return new FromInputStreamEnumerator(is, chunkSize);
         }
         public static <T> Enumerator<Byte[]> fromFile(File f, int chunkSize) {
             try {
                 return new FromInputStreamEnumerator(new FileInputStream(f), chunkSize);
             } catch (FileNotFoundException ex) {
                 throw new RuntimeException(ex);
             }
         }
         public static <T> Enumerator<String> fromFileLines(File f) {
             return new FromFileLinesEnumerator(f);
         }
         public static <T> PushEnumerator<T> unicast(Class<T> clazz) {
             return new PushEnumerator<T>();
         }
         public static <T> PushEnumerator<T> generate(long every, TimeUnit unit, final Function<Unit, Option<T>> callback) {
             return new CallbackPushEnumerator<T>(every, unit, callback);
         }
         public static <T> HubEnumerator<T> broadcast(Enumerator<T> enumerator) {
             return new HubEnumerator(enumerator);
         }
     }
     public static abstract class Enumeratee<I, O> implements Forward {
         private ActorRef fromEnumerator;
         private ActorRef toIteratee;
         private final Function<I, O> tranform;
         public Enumeratee(Function<I, O> tranform) {
             this.tranform = tranform;
         }
         public void setToIteratee(ActorRef toIteratee) {
             this.toIteratee = toIteratee;
         }
 
         public void setFromEnumerator(ActorRef fromEnumerator) {
             this.fromEnumerator = fromEnumerator;
         }
 
         @Override
         public void onReceive(Object msg, ActorRef sender, ActorRef self) throws Exception {
             for (Elem e : F.caseClassOf(Elem.class, msg)) {
                 Elem<I> el = (Elem<I>) e;
                 for (I elem : el.get()) {
                     O out = tranform.apply(elem);
                     if (out != null)
                         toIteratee.tell(new Elem<O>(out), self);
                 }
             }
             for (EOF eof : F.caseClassOf(EOF.class, msg)) {
                 toIteratee.tell(eof, self);
             }
             for (Empty empty : F.caseClassOf(Empty.class, msg)) {
                 toIteratee.tell(empty, self);
             }
             for (Cont cont : F.caseClassOf(Cont.class, msg)) {
                 fromEnumerator.tell(cont, self);
             }
             for (Done done : F.caseClassOf(Done.class, msg)) {
                 fromEnumerator.tell(done, self);
                 self.tell(PoisonPill.getInstance());
             }
             for (Error error : F.caseClassOf(Error.class, msg)) {
                 fromEnumerator.tell(error, self);
                 self.tell(PoisonPill.getInstance());
             }
         }
         public static <I,O> Enumeratee<I,O> map(Function<I,O> transform) {
             return new MapEnumeratee<I, O>(transform);
         }
     }
 
     /**************************************************************************/
     /**************************************************************************/
     /**************************************************************************/
 
     private static class DecoratedEnumerator<I> extends Enumerator<I> {
         private final Enumerator<?> fromEnumerator;
         private final List<Function> functions = new CopyOnWriteArrayList<Function>();
         private Iteratee<I, ?> toIteratee;
         private final Enumeratee throughEnumeratee = Enumeratee.map(new Function<Object, Object>() {
             @Override
             public Object apply(Object t) {
                 return applyTransforms(t);
             }
         });
         DecoratedEnumerator(Enumerator<?> fromEnumerator,
                             Enumeratee<?, I>... throughEnumeratees) {
             this.fromEnumerator = fromEnumerator;
             if (throughEnumeratees != null && throughEnumeratees.length > 0) {
                 for (Enumeratee enumeratee : throughEnumeratees) {
                     functions.add(enumeratee.tranform);
                 }
             } else {
                 throw new RuntimeException("You have to provide at least one enumeratee");
             }
         }
 
         @Override
         public <O> Promise<O> applyOn(Iteratee<I, O> it) {
             toIteratee = it;
             Promise<O> res = it.getAsyncResult();
             iteratee = system().actorOf(forwarderActorProps(toIteratee), UUID.randomUUID().toString());
             ActorRef enumeratee = system().actorOf(forwarderActorProps(throughEnumeratee), UUID.randomUUID().toString());
             enumerator = system().actorOf(forwarderActorProps(fromEnumerator), UUID.randomUUID().toString());
             throughEnumeratee.setFromEnumerator(enumerator);
             throughEnumeratee.setToIteratee(iteratee);
             fromEnumerator.onApply();
             enumerator.tell(Run.INSTANCE, enumeratee);
             return res;
         }
         private Object applyTransforms(Object in) {
             Object res = in;
             for (Function func : functions) {
                 res = func.apply(res);
             }
             return res;
         }
         @Override
         public boolean hasNext() {
             return fromEnumerator.hasNext();
             //throw new RuntimeException("Should never happen");
         }
         @Override
         public Option<I> next() {
             for (Object o : fromEnumerator.next()) {
                 I i = (I) applyTransforms(o);
                 return Option.some(i);
             }
             return Option.none();
             //throw new RuntimeException("Should never happen");
         }
         @Override
         void onApply() {
             fromEnumerator.onApply();
         }
         @Override
         public <O> Enumerator<O> through(Enumeratee<I, O>... enumeratees) {
             return new DecoratedEnumerator<O>(this, enumeratees);
             //throw new RuntimeException("Not allowed. Try to chained Enumeratee instead");
         }
     }
     public static class IterableEnumerator<T> extends Enumerator<T> {
         private final Iterator<T> it;
         public IterableEnumerator(Iterable<T> iterable) {
             it = iterable.iterator();
         }
         @Override
         public Option<T> next() {
             T obj = null;
             try {
                 obj = it.next();
             } catch (Exception e) { e.printStackTrace(); }
             return Option.apply(obj);
         }
         @Override
         public boolean hasNext() {
             return it.hasNext();
         }
     }
     public static class LongEnumerator extends Enumerator<Long> {
         private Long current = 0L;
         @Override
         public Option<Long> next() {
             current = current + 1L;
             return Option.some(current);
         }
         @Override
         public boolean hasNext() {
             return (current < Long.MAX_VALUE);
         }
     }
     public static class IntEnumerator extends Enumerator<Integer> {
         private Integer current = 0;
         @Override
         public Option<Integer> next() {
             current = current + 1;
             return Option.some(current);
         }
         @Override
         public boolean hasNext() {
             return (current < Integer.MAX_VALUE);
         }
     }
     public static class ShortEnumerator extends Enumerator<Short> {
         private Short current = 0;
         @Override
         public Option<Short> next() {
             current = (short)(current + 1);
             return Option.some(current);
         }
         @Override
         public boolean hasNext() {
             return (current < Short.MAX_VALUE);
         }
     }
     public static class FloatEnumerator extends Enumerator<Float> {
         private Float current = 0F;
         @Override
         public Option<Float> next() {
             current = current + 1F;
             return Option.some(current);
         }
         @Override
         public boolean hasNext() {
             return (current < Float.MAX_VALUE);
         }
     }
     public static class DoubleEnumerator extends Enumerator<Double> {
         private Double current = 0.0;
         @Override
         public Option<Double> next() {
             current = current + 1.0;
             return Option.some(current);
         }
         @Override
         public boolean hasNext() {
             return (current < Double.MAX_VALUE);
         }
     }
     public static class ByteEnumerator extends Enumerator<Byte> {
         private Byte current = 0;
         @Override
         public Option<Byte> next() {
             current = (byte)(current + 1);
             return Option.some(current);
         }
         @Override
         public boolean hasNext() {
             return (current < Byte.MAX_VALUE);
         }
     }
     public static class BigIntegerEnumerator extends Enumerator<BigInteger> {
         private BigInteger current = BigInteger.ZERO;
         @Override
         public Option<BigInteger> next() {
             current = current.add(BigInteger.ONE);
             return Option.some(current);
         }
         @Override
         public boolean hasNext() {
             return (current.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == -1);
         }
     }
     public static class BigDecimalEnumerator extends Enumerator<BigDecimal> {
         private BigDecimal current = BigDecimal.ZERO;
         @Override
         public Option<BigDecimal> next() {
             current = current.add(BigDecimal.ONE);
             return Option.some(current);
         }
         @Override
         public boolean hasNext() {
             return (current.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) == -1);
         }
     }
     public static class CharacterEnumerator extends Enumerator<Character> {
         private int current = 0;
         @Override
         public Option<Character> next() {
             current = current + 1;
             return Option.some(new Character(((char)current)));
         }
         @Override
         public boolean hasNext() {
             return (current < Character.MAX_VALUE);
         }
     }
     private static class FromInputStreamEnumerator extends Enumerator<Byte[]> {
         private final InputStream is;
         private final int chunkSize;
         private boolean hasnext = true;
         public FromInputStreamEnumerator(InputStream is, int chunkSize) {
             this.is = is;
             this.chunkSize = chunkSize;
         }
         @Override
         public Option<Byte[]> next() {
             byte[] bytes = new byte[chunkSize];
             Byte[] copy = new Byte[chunkSize];
             try {
                 int numRead = is.read(bytes);
                 if (numRead == -1) {
                     close();
                 } else {
                     int i = 0;
                     for (byte b : bytes) {
                         copy[i] = b;
                         i++;
                     }
                 }
             } catch (Exception e) {
                 e.printStackTrace();
                 close();
             }
             return Option.some(copy);
         }
         private void close() {
             hasnext = false;
             try {
                 is.close();
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
         }
         @Override
         public boolean hasNext() {
             return hasnext;
         }
     }
     private static class FromFileLinesEnumerator extends Enumerator<String> {
         private final FileInputStream fstream;
         private final DataInputStream in;
         private final BufferedReader br;
         private boolean hasnext = true;
         public FromFileLinesEnumerator(File f) {
             try {
                 fstream = new FileInputStream(f);
                 in = new DataInputStream(fstream);
                 br = new BufferedReader(new InputStreamReader(in));
             } catch (Exception e) { throw new RuntimeException(e); }
         }
         @Override
         public Option<String> next() {
             String strLine = null;
             try {
                 strLine = br.readLine();
             } catch(Exception e) { e.printStackTrace(); }
             if (strLine == null) {
                 close();
             }
             return Option.some(strLine);
         }
         private void close() {
             hasnext = false;
             try {
                 br.close();
                 in.close();
                 fstream.close();
             } catch(Exception e) { e.printStackTrace(); }
         }
         @Override
         public boolean hasNext() {
             return hasnext;
         }
     }
     public static class PushEnumerator<T> extends Enumerator<T> {
         private boolean hasnext = true;
         private final ConcurrentLinkedQueue<T> pushQueue = new ConcurrentLinkedQueue<T>();
         @Override
         public Option<T> next() {
            System.out.println("consume");
             return Option.apply(pushQueue.poll());
         }
         @Override
         public boolean hasNext() {
             if (!pushQueue.isEmpty()) {
                 return true;
             }
             return hasnext;
         }
         public void push(T elem) {
             pushQueue.offer(elem);
             try {
                 if (enumerator != null) {
                     enumerator.tell(Cont.INSTANCE, iteratee);
                 } else {
                     //throw new RuntimeException("Enumerator should not be null");
                 }
             } catch (Exception e) { e.printStackTrace(); }
         }
         public void stop() {
             hasnext = false;
             iteratee.tell(EOF.INSTANCE, enumerator);
             enumerator.tell(Done.INSTANCE, iteratee);
         }
     }
     private static class CallbackPushEnumerator<T> extends PushEnumerator<T> {
         private final long every;
         private final TimeUnit unit;
         private final Function<Unit, Option<T>> callback;
         public CallbackPushEnumerator(long every, TimeUnit unit, Function<Unit, Option<T>> callback) {
             this.every = every;
             this.unit = unit;
             this.callback = callback;
         }
         @Override
         public <O> Promise<O> applyOn(Iteratee<T, O> it) {
             Promise<O> promise = super.applyOn(it);
             schedule();
             return promise;
         }
         private void schedule() {
             cancel = system().scheduler().schedule(Duration.apply(0, TimeUnit.MILLISECONDS), Duration.apply(every, unit), new Runnable() {
                 @Override
                 public void run() {
                     Option<T> opt = callback.apply(Unit.unit());
                     for (T elem : opt) {
                         push(elem);
                     }
                 }
             });
         }
         @Override
         void onApply() {
             schedule();
         }
         private Cancellable cancel;
         @Override
         public void stop() {
             super.stop();
             if (cancel != null) {
                 cancel.cancel();
             }
         }
     }
     private static class InterleavedEnumerators<T> extends Enumerator<T> {
         private final List<Enumerator<T>> enumerators;
         private final CountDownLatch latch;
         private final ActorRef globalIteratee;
         private ActorRef finalIteratee;
         public InterleavedEnumerators(Enumerator<T>... enumerators) {
             this.latch = new CountDownLatch(enumerators.length);
             Props iterateeProp = new Props().withCreator(new UntypedActorFactory() {
                 public Actor create() {
                     return new UntypedActor() {
                         @Override
                         public void onReceive(Object msg) throws Exception {
                             if (latch.getCount() == 0) {
                                 self().tell(PoisonPill.getInstance());
                             }
                             for (Elem e : F.caseClassOf(Elem.class, msg)) {
                                 finalIteratee.tell(e, self());
                                 sender().tell(Cont.INSTANCE, sender());
                             }
                             for (EOF eof : F.caseClassOf(EOF.class, msg)) {
                                 latch.countDown();
                             }
                         }
                     };
                 }
             });
             globalIteratee = system().actorOf(iterateeProp, UUID.randomUUID().toString());
             this.enumerators = new CopyOnWriteArrayList<Enumerator<T>>(Arrays.asList(enumerators));
         }
         @Override
         public Option<T> next() {
             for (Enumerator en : enumerators) {
                 if (en.hasNext()) {
                     return en.next();
                 }
             }
             return Option.none();
             //throw new RuntimeException("next should never be called");
         }
         @Override
         public boolean hasNext() {
             for (Enumerator en : enumerators) {
                 if (en.hasNext()) {
                     return true;
                 }
             }
             return true;
             //throw new RuntimeException("next should never be called");
         }
         @Override
         public <O> Promise<O> applyOn(final Iteratee<T, O> it) {
             Promise<O> res = it.getAsyncResult();
             finalIteratee = system().actorOf(forwarderActorProps(it), UUID.randomUUID().toString());
             for (Enumerator e : enumerators) {
                 e.iteratee = globalIteratee;
                 e.enumerator = system().actorOf(forwarderActorProps(e), UUID.randomUUID().toString());
                 e.enumerator.tell(Run.INSTANCE, globalIteratee);
                 e.onApply();
             }
             return res;
         }
 
         @Override
         void onApply() {
             for (Enumerator e : enumerators) {
                 e.onApply();
             }
         }
     }
     private static class ForeachIteratee<T> extends Iteratee<T, Unit> {
         private final Function<T, Unit> func;
         public ForeachIteratee(Function<T, Unit> func) {
             this.func = func;
         }
         @Override
         public void onReceive(Object msg, ActorRef sender, ActorRef self) throws Exception {
             for (Elem e : F.caseClassOf(Elem.class, msg)) {
                 Elem<T> el = (Elem<T>) e;
                 for (T elem : el.get()) {
                     func.apply(elem);
                 }
                 sender.tell(Cont.INSTANCE, self);
             }
             for (EOF e : F.caseClassOf(EOF.class, msg)) {
                 done(Unit.unit(), sender, self);
             }
         }
     }
     private static class MapEnumeratee<I, O> extends Enumeratee<I, O> {
         public MapEnumeratee(Function<I, O> transform) {
             super(transform);
         }
     }
     public static class HubEnumerator<T> {
         private final List<ActorRef> iteratees = new CopyOnWriteArrayList<ActorRef>();
         private final Enumerator<T> fromEnumerator;
         private ActorRef enumerator;
         private final ActorRef internalIteratee;
         public HubEnumerator(Enumerator<T> fromEnumerator) {
             this.fromEnumerator = fromEnumerator;
             internalIteratee = system().actorOf(new Props().withCreator(new UntypedActorFactory() {
                 public Actor create() {
                     return new UntypedActor() {
                         @Override
                         public void onReceive(Object msg) throws Exception {
                             for (Cont cont : F.caseClassOf(Cont.class, msg)) {
                                 enumerator.tell(cont, self());
                             }
                             for (Done done : F.caseClassOf(Done.class, msg)) {
                                 if (!iteratees.isEmpty()) {
                                     iteratees.remove(sender());
                                 } else {
                                     enumerator.tell(done, self());
                                 }
                             }
                             for (Error error : F.caseClassOf(Error.class, msg)) {
                                 if (!iteratees.isEmpty()) {
                                     iteratees.remove(sender());
                                 } else {
                                     enumerator.tell(error, self());
                                 }
                             }
                             for (Elem e : F.caseClassOf(Elem.class, msg)) {
                                 for (ActorRef actor : iteratees) {
                                     actor.tell(e, self());
                                 }
                             }
                             for (EOF eof : F.caseClassOf(EOF.class, msg)) {
                                 for (ActorRef actor : iteratees) {
                                     actor.tell(eof, self());
                                 }
                             }
                             for (Empty empty : F.caseClassOf(Empty.class, msg)) {
                                 for (ActorRef actor : iteratees) {
                                     actor.tell(empty, self());
                                 }
                             }
                         }
                     };
                 }
             }), UUID.randomUUID().toString());
         }
         public HubEnumerator<T> add(final Iteratee<T, ?> iteratee) {
             iteratees.add(system().actorOf(forwarderActorProps(iteratee), UUID.randomUUID().toString()));
             return this;
         }
         public void broadcast() {
             enumerator = system().actorOf(forwarderActorProps(fromEnumerator), UUID.randomUUID().toString());
             fromEnumerator.enumerator = enumerator;
             fromEnumerator.iteratee = internalIteratee;
             enumerator.tell(Run.INSTANCE, internalIteratee);
             if (fromEnumerator instanceof CallbackPushEnumerator) {
                 final CallbackPushEnumerator<T> p = (CallbackPushEnumerator<T>) fromEnumerator;
                 p.schedule();
             }
         }
         public void stop() {
             for (ActorRef actor : iteratees) {
                 actor.tell(PoisonPill.getInstance());
             }
             enumerator.tell(PoisonPill.getInstance());
             internalIteratee.tell(PoisonPill.getInstance());
         }
     }
 
     private static Props forwarderActorProps(final Forward f) {
         return new Props().withCreator(new UntypedActorFactory() {
             public Actor create() {
                 return new ForwarderActor(f);
             }
         });
     }
 }
