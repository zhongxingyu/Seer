 package tools.benchmark.metrics;
 
 import java.util.concurrent.atomic.AtomicLong;
 
 public class ActualTimeMetric extends AbstractMetric {
     AtomicLong startNanos = new AtomicLong(0);
     AtomicLong stopNanos = new AtomicLong(0);
     String format = "Actual run time metric[avg: %,.4fms, total: %,.4fms, tps: %,.4f.]";
     final double MILLIS_BY_NANOS = 1_000_000.0;
     final double SECONDS_BY_NANOS = 1_000_000_000.0;
 
     @Override
     public void start() {
         startNanos.compareAndSet(0, System.nanoTime());
     }
 
     @Override
     public void stop() {
         long stopTime = System.nanoTime();
        long current = 0;
         while (!stopNanos.compareAndSet(current, stopTime)) {
             current = stopNanos.get();
             stopTime = Math.max(stopTime, current);
         }
     }
 
     @Override
     public String metricResult(int measureTimes) {
         long total = stopNanos.get() - startNanos.get();
         return String.format(format, total / MILLIS_BY_NANOS / measureTimes,
                 total / MILLIS_BY_NANOS, measureTimes
                         / (total / SECONDS_BY_NANOS));
     }
 
 }
