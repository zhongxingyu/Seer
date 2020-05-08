 package org.nohope.test.stress;
 
 import org.apache.commons.lang3.StringUtils;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static java.util.concurrent.TimeUnit.SECONDS;
 import static org.nohope.test.stress.TimeUtils.throughputTo;
 import static org.nohope.test.stress.TimeUtils.timeTo;
 
 /**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-12-27 16:19
 */
 public class StressResult {
     private final Map<String, Result> results = new HashMap<>();
     private final double runtime;
     private final int fails;
     private final int threadsCount;
     private final int cycleCount;
 
     public StressResult(final Map<String, Result> stats,
                         final int threadsCount,
                         final int cycleCount,
                         final int fails,
                         final double runtime) {
         this.runtime = runtime;
         this.fails = fails;
         this.threadsCount = threadsCount;
         this.cycleCount = cycleCount;
         this.results.putAll(stats);
     }
 
     /**
      * @return per test results
      */
     public Map<String, Result> getResults() {
         return results;
     }
 
     /**
      * @return approximate overall throughput in op/sec
      */
     public double getApproxThroughput() {
         return (threadsCount * cycleCount * 1.0 - fails) / runtime;
     }
 
     /**
      * @return overall running time in milliseconds
      */
     public double getRuntime() {
         return runtime;
     }
 
     /**
      * @return overall exceptions count
      */
     public int getFails() {
         return fails;
     }
 
     @Override
     public String toString() {
         final StringBuilder builder = new StringBuilder();
         final String separator = StringUtils.rightPad("", 50, '=');
         builder.append(StringUtils.rightPad("====== Stress test result ", 50, '='))
                .append("\n")
                .append(pad("Threads: "))
                .append(threadsCount)
                .append("\n")
                .append(pad("Cycles: "))
                .append(cycleCount)
                .append("\n")
                .append(separator)
                .append("\n");
         for (final Result stats : results.values()) {
             builder.append(stats.toString());
         }
         return builder.append(separator)
                       .append("\n")
                       .append(pad("Total error count:"))
                       .append(fails)
                       .append("\n")
                       .append(pad("Total running time:"))
                       .append(String.format("%.3f", timeTo(runtime, SECONDS)))
                       .append(" sec\n")
                       .append(pad("Approximate throughput:"))
                       .append(String.format("%.3e", throughputTo(getApproxThroughput(), SECONDS)))
                       .append(" op/sec")
                       .toString();
     }
 
     private static String pad(final String str) {
         final int padSize = 30;
         return StringUtils.rightPad(str, padSize, '.');
     }
 }
