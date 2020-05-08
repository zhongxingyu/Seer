 package kembe;
 
 import fj.F;
 import fj.Ord;
 import kembe.sim.rand.Rand;
 import org.joda.time.*;
 
 import java.util.Random;
 
 public class Time {
 
     public static final Ord<Instant> instantOrd = Ord.longOrd.comap( new F<Instant, Long>() {
         @Override public Long f(Instant instant) {
             return instant.getMillis();
         }
     } );
 
     public static F<Interval, Duration> toDuration = new F<Interval, Duration>() {
         @Override public Duration f(Interval interval) {
             return interval.toDuration();
         }
     };
 
     public static F<Duration, Long> toDurationMillis = new F<Duration, Long>() {
         @Override public Long f(Duration duration) {
             return duration.getMillis();
         }
     };
 
     public static Instant quantumIncrement(Instant instant) {
         return instant.plus( 1 );
     }
 
     public static Instant now() {
         return new Instant( System.currentTimeMillis() );
     }
 
     public static Instant plus(ReadableInstant instant, ReadablePeriod period) {
         return instant.toInstant().plus( period.toPeriod().toStandardDuration() );
     }
 
     public static IntervalBuilder from(ReadableInstant instant) {
         return new IntervalBuilder( instant );
     }
 
     public static DurationBuilder durationOf(ReadableInstant start){
         return new DurationBuilder(start);
     }
 
     public static Rand<Duration> randomDuration(final Duration min, final Duration max) {
         final long diff = Math.abs( max.getMillis() - min.getMillis() );
 
         if (diff > Integer.MAX_VALUE)
             return new Rand<Duration>() {
                 @Override public Duration next(Random t) {
                     int r = t.nextInt( (int) diff / 60 / 1000 );
                     return min.plus( r * 60 * 1000 );
                 }
             };
         else
             return new Rand<Duration>() {
                 @Override public Duration next(Random t) {
                     int r = t.nextInt( (int) diff );
                     return min.plus( r );
                 }
             };
     }
 
     public static DateTime midnightBefore(ReadableInstant instant) {
         return instant.toInstant().toDateTime().toDateMidnight().toDateTime();
     }
 
     public static DateTime nextMidnightAfter(ReadableInstant instant) {
         return instant.toInstant().toDateTime().toDateMidnight().toDateTime().plusDays( 1 );
     }
 
    public static DateTime next(LocalTime time, ReadableInstant instant) {
        DateTime dt = instant.toInstant().toDateTime().withFields( time );
 
         return dt.isAfter( instant )
                ? dt
                : dt.plusDays( 1 );
     }
 
     public static LocalTime timeOfDay(int hours, int seconds) {
         return new LocalTime( hours, seconds ).withMillisOfSecond( 0 );
     }
 
     public static class IntervalBuilder {
 
         final ReadableInstant start;
 
         public IntervalBuilder(ReadableInstant start) {
             this.start = start;
         }
 
         public Interval until(ReadableInstant end) {
             return new Interval( start, end );
         }
 
         public Interval lasting(ReadableDuration duration) {
             return duration.toDuration().toIntervalFrom( start );
         }
 
         public Interval lasting(ReadablePeriod period) {
             return new Interval( start, start.toInstant().toDateTime().plus( period ) );
         }
     }
 
     public static class DurationBuilder{
         final ReadableInstant start;
 
 
         public DurationBuilder(ReadableInstant start) {
             this.start = start;
         }
 
         public Duration until(ReadableInstant end){
             long s = Math.min( start.getMillis(),end.getMillis() );
             long e = Math.max( start.getMillis(),end.getMillis() );
 
             if(s == e)
                 return Duration.ZERO;
             else
                 return Duration.millis( e-s);
         }
     }
 
 }
