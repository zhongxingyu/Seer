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
 
 package cx.ath.mancel01.utils;
 
 import cx.ath.mancel01.utils.Concurrent.Promise;
 import cx.ath.mancel01.utils.F.Function;
 import cx.ath.mancel01.utils.F.Option;
 import cx.ath.mancel01.utils.F.Unit;
 import cx.ath.mancel01.utils.actors.Actors;
 import cx.ath.mancel01.utils.actors.Actors.Actor;
 import cx.ath.mancel01.utils.actors.Actors.ActorContext;
 import cx.ath.mancel01.utils.actors.Actors.Behavior;
 import cx.ath.mancel01.utils.actors.Actors.Context;
 import cx.ath.mancel01.utils.actors.Actors.Effect;
 import cx.ath.mancel01.utils.actors.Actors.Poison;
 import java.io.*;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.*;
 import java.util.concurrent.*;
 
 public class Iteratees {
     
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
     public static abstract class Iteratee<I, O> implements Behavior {
         protected Promise<O> promise = new Promise<O>();
         public Effect done(O result, Context ctx) {
             promise.apply(result);
             ctx.from.tell(Done.INSTANCE, ctx.me);
             return Actors.DIE;
         }
         public Promise<O> getAsyncResult() {
             return promise;
         }
         public static <T> Iteratee<T, Unit> foreach(Function<T, Unit> func) {
             return new ForeachIteratee<T>(func);
         }
     }
     public static abstract class Enumerator<I> implements Behavior {
         @Override
         public Effect apply(Object msg, Context ctx) {
             for (Run run : M.caseClassOf(Run.class, msg)) {
                 sendNext(msg, ctx);
             }
             for (Cont cont : M.caseClassOf(Cont.class, msg)) {
                 sendNext(msg, ctx);
             }
             for (Done done : M.caseClassOf(Done.class, msg)) {
                 ctx.from.tell(Poison.PILL, ctx.me);
                 return Actors.DIE;
             }
             for (Error err : M.caseClassOf(Error.class, msg)) {
                 ctx.from.tell(Poison.PILL, ctx.me);
                 System.err.println(err.error);
                 return Actors.DIE;            
             }
             return Actors.CONTINUE;
         }
         void sendNext(Object msg, Context ctx) {
             if (!hasNext()) {
                 ctx.from.tell(EOF.INSTANCE, ctx.me);
             } else {
                 Option<I> optElemnt = next();
                 for (I element : optElemnt) {
                     ctx.from.tell(new Elem<I>(element), ctx.me);
                 }
                 if (optElemnt.isEmpty()) {
                     ctx.from.tell(Empty.INSTANCE, ctx.me);
                 }
             }
         }
         public abstract boolean hasNext();
         public abstract Option<I> next();
         Actor enumerator;
         Actor iteratee;
         ActorContext context;
         public <O> Promise<O> applyOn(Iteratee<I, O> it) {
             Promise<O> res = it.getAsyncResult();
             context = Actors.newContext();
             iteratee = context.create(it, UUID.randomUUID().toString());
             enumerator = context.create(this, UUID.randomUUID().toString());
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
         public static <T> PushEnumerator<T> imperative(Class<T> clazz) {
             return new PushEnumerator<T>();
         }
         public static <T> PushEnumerator<T> fromCallback(long every, TimeUnit unit, final Function<Unit, Option<T>> callback) {
             return new CallbackPushEnumerator<T>(every, unit, callback);
         }
         public static <T> HubEnumerator<T> hub(Enumerator<T> enumerator) {
             return new HubEnumerator(enumerator);
         }
     }   
     public static abstract class Enumeratee<I, O> implements Behavior {
         private Actor fromEnumerator;
         private Actor toIteratee;
         private final Function<I, O> tranform;
         public Enumeratee(Function<I, O> tranform) {
             this.tranform = tranform;
         }
         public void setToIteratee(Actor toIteratee) {
             this.toIteratee = toIteratee;
         }
 
         public void setFromEnumerator(Actor fromEnumerator) {
             this.fromEnumerator = fromEnumerator;
         }
         @Override
         public Effect apply(Object msg, Context ctx) {
             for (Elem e : M.caseClassOf(Elem.class, msg)) {
                 Elem<I> el = (Elem<I>) e;
                 for (I elem : el.get()) { 
                    toIteratee.tell(new Elem<O>(tranform.apply(elem)), ctx.me);
                 }
             }
             for (EOF eof : M.caseClassOf(EOF.class, msg)) {
                 toIteratee.tell(eof, ctx.me);
             }
             for (Empty empty : M.caseClassOf(Empty.class, msg)) {
                 toIteratee.tell(empty, ctx.me);
             }
             for (Cont cont : M.caseClassOf(Cont.class, msg)) {
                 fromEnumerator.tell(cont, ctx.me);
             }
             for (Done done : M.caseClassOf(Done.class, msg)) {
                 fromEnumerator.tell(done, ctx.me);
                 return Actors.DIE;
             }
             for (Error error : M.caseClassOf(Error.class, msg)) {
                 fromEnumerator.tell(error, ctx.me);
                 return Actors.DIE;
             }
             return Actors.CONTINUE;
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
         private final List<Function> functions = new ArrayList<Function>();
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
                 context = Actors.newContext();
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
             iteratee = context.create(toIteratee, UUID.randomUUID().toString());
             Actor enumeratee = context.create(throughEnumeratee, UUID.randomUUID().toString());
             enumerator = context.create(fromEnumerator, UUID.randomUUID().toString());
             throughEnumeratee.setFromEnumerator(enumerator);
             throughEnumeratee.setToIteratee(iteratee);
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
             throw new RuntimeException("Should never happen");
         }
         @Override
         public Option<I> next() {
             throw new RuntimeException("Should never happen");
         }
         @Override
         public <O> Enumerator<O> through(Enumeratee<I, O>... enumeratees) {
             throw new RuntimeException("Not allowed. Try to chained Enumeratee instead");
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
                     System.err.println("Push of '" + elem.toString() + "' failed because enumerator == " + enumerator);
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
             context.schedule(every, unit, new Runnable() {
                 @Override
                 public void run() {
                     Option<T> opt = callback.apply(Unit.unit());
                     for (T elem : opt) {
                         push(elem);
                     }
                 }
             });
         }
     }
     private static class InterleavedEnumerators<T> extends Enumerator<T> {
         private final List<Enumerator<T>> enumerators;
         private final CountDownLatch latch;
         private final Actor globalIteratee;
         private Actor finalIteratee;
         private final ActorContext actCtx;
         private final Behavior client = new Behavior() {
             @Override
             public Effect apply(Object msg, Context ctx) {
                 if (latch.getCount() == 0) {
                     return Actors.DIE;
                 }
                 for (Elem e : M.caseClassOf(Elem.class, msg)) {
                     finalIteratee.tell(e, ctx.me);
                     ctx.from.tell(Cont.INSTANCE, ctx.from);
                 }
                 for (EOF eof : M.caseClassOf(EOF.class, msg)) {
                     latch.countDown();
                 }
                 return Actors.CONTINUE;
             }
         };
         public InterleavedEnumerators(Enumerator<T>... enumerators) {
             this.latch = new CountDownLatch(enumerators.length);
             actCtx = Actors.newContext();
             globalIteratee = actCtx.create(client, UUID.randomUUID().toString());
             this.enumerators = Arrays.asList(enumerators);
         }
         @Override
         public Option<T> next() {
             throw new RuntimeException("next should never be called");
         }
         @Override
         public boolean hasNext() {
             throw new RuntimeException("next should never be called");
         }
         @Override
         public <O> Promise<O> applyOn(Iteratee<T, O> it) {
             Promise<O> res = it.getAsyncResult();
             finalIteratee = actCtx.create(it, UUID.randomUUID().toString());
             for (Enumerator e : enumerators) {
                 e.iteratee = globalIteratee;
                 e.enumerator = actCtx.create(e, UUID.randomUUID().toString());
                 e.enumerator.tell(Run.INSTANCE, globalIteratee);
             }
             return res;
         }
     }
     private static class ForeachIteratee<T> extends Iteratee<T, Unit> {
         private final Function<T, Unit> func;
         public ForeachIteratee(Function<T, Unit> func) {
             this.func = func;
         }
         @Override
         public Effect apply(Object msg, Context ctx) {
             for (Elem e : M.caseClassOf(Elem.class, msg)) {
                 Elem<T> el = (Elem<T>) e;
                 for (T elem : el.get()) { 
                    func.apply(elem);
                 }
                 ctx.from.tell(Cont.INSTANCE, ctx.me);
             }
             for (EOF e : M.caseClassOf(EOF.class, msg)) {
                 return done(Unit.unit(), ctx);
             }
             return Actors.CONTINUE;
         }
     }
     private static class MapEnumeratee<I, O> extends Enumeratee<I, O> {
         public MapEnumeratee(Function<I, O> transform) {
             super(transform);
         }
     }
     public static class HubEnumerator<T> {
         private final List<Actor> iteratees = new ArrayList<Actor>();
         private final Enumerator<T> fromEnumerator;
         private Actor enumerator;
         private final Actor internalIteratee;
         private final ActorContext context = Actors.newContext();
         public HubEnumerator(Enumerator<T> fromEnumerator) {
             this.fromEnumerator = fromEnumerator;
             internalIteratee = context.create(new Behavior() {
                 @Override
                 public Effect apply(Object msg, Context ctx) {
                     for (Cont cont : M.caseClassOf(Cont.class, msg)) {
                         enumerator.tell(cont, ctx.me);
                     }
                     for (Done done : M.caseClassOf(Done.class, msg)) {
                         if (!iteratees.isEmpty()) {
                             iteratees.remove(ctx.from);
                         } else {
                             enumerator.tell(done, ctx.me);
                         }
                     }
                     for (Error error : M.caseClassOf(Error.class, msg)) {
                         if (!iteratees.isEmpty()) {
                             iteratees.remove(ctx.from);
                         } else {
                             enumerator.tell(error, ctx.me);
                         }
                     }
                     for (Elem e : M.caseClassOf(Elem.class, msg)) {
                         for (Actor actor : iteratees) {
                             actor.tell(e, ctx.me);
                         }
                     }
                     for (EOF eof : M.caseClassOf(EOF.class, msg)) {
                         for (Actor actor : iteratees) {
                             actor.tell(eof, ctx.me);
                         }
                     }
                     for (Empty empty : M.caseClassOf(Empty.class, msg)) {
                         for (Actor actor : iteratees) {
                             actor.tell(empty, ctx.me);
                         }
                     }
                     return Actors.CONTINUE;
                 }
             }, UUID.randomUUID().toString());
         }
         public HubEnumerator<T> add(Iteratee<T, ?> iteratee) {
             iteratees.add(context.create(iteratee, UUID.randomUUID().toString()));
             return this;
         }
         public void broadcast() {
             enumerator = context.create(fromEnumerator, UUID.randomUUID().toString());
             fromEnumerator.context = context;
             fromEnumerator.enumerator = enumerator;
             fromEnumerator.iteratee = internalIteratee;
             enumerator.tell(Run.INSTANCE, internalIteratee);
             if (fromEnumerator instanceof CallbackPushEnumerator) {
                 final CallbackPushEnumerator<T> p = (CallbackPushEnumerator<T>) fromEnumerator;
                p.schedule();
             }
         }
         public void stop() {
             for (Actor actor : iteratees) {
                 actor.tell(Poison.PILL);
             }
             enumerator.tell(Poison.PILL);
             internalIteratee.tell(Poison.PILL);
         }
     }
 }
