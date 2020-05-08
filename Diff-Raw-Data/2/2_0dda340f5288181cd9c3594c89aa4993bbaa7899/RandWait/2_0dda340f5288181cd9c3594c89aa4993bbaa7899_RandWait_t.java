 package kembe.sim;
 
 import fj.F;
 import kembe.Time;
 import kembe.sim.rand.DoubleFromZeroIncToOne;
 import kembe.sim.rand.Rand;
 import org.joda.time.*;
 
 import java.util.Random;
 
 public abstract class RandWait {
 
 
     public static RandWait waitUntil(final DateTime instant) {
         return new RandWait() {
             @Override public Rand<DateTime> after(final DateTime dt) {
                 return new Rand<DateTime>() {
                     @Override public DateTime next(Random t) {
                         return instant;
                     }
                 };
             }
         };
     }
 
     public static RandWait waitUntil(final LocalTime localTime) {
         return new RandWait() {
             @Override public Rand<DateTime> after(final DateTime dt) {
                 return new Rand<DateTime>() {
                     @Override public DateTime next(Random t) {
                         return Time.next( localTime, dt );
                     }
                 };
             }
         };
     }
 
     public static RandWait waitUntilBetween(final LocalTime from, final LocalTime to) {
         return new RandWait() {
             @Override public Rand<DateTime> after(final DateTime dt) {
 
                 DateTime fromInstant = Time.next( from, dt );
                DateTime toInstant = Time.next( to, fromInstant );
 
                 return within( Time.from( fromInstant ).until( toInstant ) );
             }
         };
     }
 
     public static RandWait waitFor(final Duration dur) {
         return new RandWait() {
             @Override public Rand<DateTime> after(final DateTime dt) {
                 return new Rand<DateTime>() {
                     @Override public DateTime next(Random t) {
                         return dt.plus( dur );
                     }
                 };
             }
         };
     }
 
     public static RandWait waitFor(final ReadablePeriod p) {
         return waitFor( p.toPeriod().toStandardDuration() );
     }
 
     public static RandWait waitForBetween(final Duration min, final Duration max) {
         return new RandWait() {
             @Override public Rand<DateTime> after(final DateTime dt) {
                 return within( Time.from( dt.plus( min ) ).until( dt.plus( max ) ) );
             }
         };
     }
 
     public static RandWait waitAtMost(final Duration dur) {
         return new RandWait() {
             @Override public Rand<DateTime> after(final DateTime dt) {
                 return within( Time.from( dt ).lasting( dur ) );
             }
         };
     }
 
     public static RandWait waitAtMost(final ReadablePeriod p) {
         return waitAtMost( p.toPeriod().toStandardDuration() );
     }
 
     private static Rand<DateTime> within(final Interval interval) {
         Rand<DateTime> rg = new Rand<DateTime>() {
 
             @Override public DateTime next(Random random) {
                 return Rand.randomDouble().map( new F<DoubleFromZeroIncToOne, DateTime>() {
                     @Override
                     public DateTime f(DoubleFromZeroIncToOne d) {
                         long fraction = (long)(interval.toDurationMillis() * d.value);
                         Duration duration = new Duration( fraction );
                         return interval.getStart().plus( duration );
                     }
                 } ).next( random );
             }
         };
 
         return rg;
     }
 
     public abstract Rand<DateTime> after(DateTime dateTime);
 }
