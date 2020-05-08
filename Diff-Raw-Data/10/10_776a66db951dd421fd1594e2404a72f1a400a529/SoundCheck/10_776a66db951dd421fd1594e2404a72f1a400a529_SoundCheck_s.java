 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.eq;
 
import static org.ref_send.promise.Eventual.near;
 import static org.ref_send.promise.Eventual.ref;
 import static org.ref_send.test.Logic.join;
 
 import java.io.Serializable;
 
 import org.ref_send.list.List;
 import org.ref_send.promise.Channel;
 import org.ref_send.promise.Do;
 import org.ref_send.promise.Eventual;
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.Receiver;
 
 /**
  * Checks invariants of the ref_send API.
  */
 public final class
 SoundCheck {
     private SoundCheck() {}
     
     /**
      * Runs a unit test.
      * @param _ eventual operator
      */
     static public Promise<?>
     make(final Eventual _) throws Exception {
         final Channel<?> x = _.defer();
         return join(_, testNormal(_, x.resolver),
                       testNull(_, null),
                       testDouble(_),
                       testFloat(_));
     }
 
     /**
      * Tests promises for a normal reference.
      */
     static private <T extends Receiver<?>> Promise<?>
     testNormal(final Eventual _, final T x) throws Exception {
         final Promise<T> p = ref(x);
         check(p.equals(p));
         check(ref(x).equals(p));
         check(x.equals(p.call()));
         class EQ extends Do<T,Boolean> implements Serializable {
             static private final long serialVersionUID = 1L;
 
             public Boolean
             fulfill(final T arg) throws Exception {
                 check(x.equals(arg));
                 return true;
             }
         }
         final Promise<?> a = _.when(p, new EQ());
         final Promise<?> b = _.when(x, new EQ());
         final Promise<?> c = _.when(new Sneaky<T>(x), new EQ());
         
         final T x_ = _._(x);
         check(x_.equals(x_));
         check(_._(x_).equals(x_));
         check(_._(x).equals(x_));
         check(ref(x_).equals(p));
        check(x == near(x_));
         final Promise<?> d = _.when(x_, new EQ());
         
         return join(_, a, b, c, d);
     }
 
     /**
      * Tests promises for a <code>null</code>.
      */
     static private <T extends Receiver<?>> Promise<?>
     testNull(final Eventual _, final T x) throws Exception {
         final Promise<T> p = ref(x);
         check(p.equals(p));
         check(!ref(x).equals(p));
         try {
             p.call();
             check(false);
         } catch (final NullPointerException e) {}
         class NE extends Do<T,Boolean> implements Serializable {
             static private final long serialVersionUID = 1L;
 
             public Boolean
             fulfill(final T arg) throws Exception { throw new Exception(); }
             
             public Boolean
             reject(final Exception reason) throws Exception {
                 if (reason instanceof NullPointerException) { return true; }
                 throw reason;
             }
         }
         final Promise<?> a = _.when(p, new NE());
         final Promise<?> b = _.when(x, new NE());
         final Promise<?> c = _.when(new Sneaky<T>(x), new NE());
         
         final T x_ = Eventual.cast(Receiver.class, p);
         check(x_.equals(x_));
         check(_._(x_).equals(x_));
         check(Eventual.cast(Receiver.class, p).equals(x_));
         check(ref(x_).equals(p));
         final Promise<?> d = _.when(x_, new NE());
         
         return join(_, a, b, c, d);
     }
     
     static private <T> Promise<?>
     testNaN(final Eventual _, final T x) throws Exception {
         final Promise<T> p = ref(x);
         check(p.equals(p));
         check(!p.equals(ref(x)));
         try {
             p.call();
             check(false);
         } catch (final ArithmeticException e) {}
         class ENaN extends Do<T,Boolean> implements Serializable {
             static private final long serialVersionUID = 1L;
 
             public Boolean
             fulfill(final T arg) throws Exception { throw new Exception(); }
             
             public Boolean
             reject(final Exception reason) throws Exception {
                 if (reason instanceof ArithmeticException) { return true; }
                 throw reason;
             }
         }
         final Promise<?> a = _.when(p, new ENaN());
         final Promise<?> b = _.when(x, new ENaN());
         final Promise<?> c = _.when(new Sneaky<T>(x), new ENaN());
 
         return join(_, a, b, c);
     }
 
     /**
      * Tests promises for {@link Double}.
      */
     static private Promise<?>
     testDouble(final Eventual _) throws Exception {
         // check normal handling
         final Promise<Double> pMin = ref(Double.MIN_VALUE);
         check(pMin.equals(pMin));
         check(ref(Double.MIN_VALUE).equals(pMin));
         check(Double.MIN_VALUE == pMin.call());
         class EQ extends Do<Double,Boolean> implements Serializable {
             static private final long serialVersionUID = 1L;
 
             public Boolean
             fulfill(final Double arg) throws Exception {
                 check(Double.MIN_VALUE == arg);
                 return true;
             }
         }
         final Promise<?> a = _.when(pMin, new EQ());
         final Promise<?> b = _.when(Double.MIN_VALUE, new EQ());
         
         final Promise<?> c = testNaN(_, Double.NaN);
         final Promise<?> d = testNaN(_, Double.NEGATIVE_INFINITY);
         final Promise<?> e = testNaN(_, Double.POSITIVE_INFINITY);
 
         return join(_, a, b, c, d, e);
     }
 
     /**
      * Tests promises for {@link Float}.
      */
     static private Promise<?>
     testFloat(final Eventual _) throws Exception {
         // check normal handling
         final Promise<Float> pMin = ref(Float.MIN_VALUE);
         check(pMin.equals(pMin));
         check(ref(Float.MIN_VALUE).equals(pMin));
         check(Float.MIN_VALUE == pMin.call());
         class EQ extends Do<Float,Boolean> implements Serializable {
             static private final long serialVersionUID = 1L;
 
             public Boolean
             fulfill(final Float arg) throws Exception {
                 check(Float.MIN_VALUE == arg);
                 return true;
             }
         }
         final Promise<?> a = _.when(pMin, new EQ());
         final Promise<?> b = _.when(Float.MIN_VALUE, new EQ());
         
         final Promise<?> c = testNaN(_, Float.NaN);
         final Promise<?> d = testNaN(_, Float.NEGATIVE_INFINITY);
         final Promise<?> e = testNaN(_, Float.POSITIVE_INFINITY);
 
         return join(_, a, b, c, d, e);
     }
     
     static private void
     check(final boolean valid) throws Exception {
         if (!valid) { throw new Exception(); }
     }
     
     // Command line interface
 
     /**
      * Executes the test.
      * @param args  ignored
      * @throws Exception    test failed
      */
     static public void
     main(final String[] args) throws Exception {
         final List<Promise<?>> work = List.list();
         final Promise<?> result = make(new Eventual(work.appender()));
         while (!work.isEmpty()) { work.pop().call(); }
         result.call();
     }
 }
