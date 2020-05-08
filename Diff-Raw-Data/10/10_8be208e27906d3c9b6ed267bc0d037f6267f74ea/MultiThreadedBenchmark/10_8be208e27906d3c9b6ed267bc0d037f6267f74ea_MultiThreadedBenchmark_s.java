 package stormpot.benchmark;
 
 import java.io.File;
 import java.nio.file.Files;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.benchkit.Benchmark;
 import org.benchkit.BenchmarkRunner;
 import org.benchkit.PrintingReporter;
 import org.benchkit.Recorder;
 import org.benchkit.Param;
 import org.benchkit.WarmupPrintingReporter;
 import org.benchkit.htmlchartsreporter.DataInterpretor;
 import org.benchkit.htmlchartsreporter.HtmlChartsReporter;
 import org.benchkit.htmlchartsreporter.LatencyHistogramChart;
 import org.benchkit.htmlchartsreporter.ThroughputChart;
 
 public class MultiThreadedBenchmark implements Benchmark {
   private static final int ITERATIONS = 2 * 1000 * 1000;
 
   private final PoolFactory factory;
   private final int threads;
   private ExecutorService executor;
   private PoolFacade pool;
   
   public MultiThreadedBenchmark(
      @Param(value = "pools", defaults = "stack,generic,queue,blaze,furious") PoolFactory factory,
      @Param(value = "threads", defaults = "1,2,3,4") int threads) {
     this.factory = factory;
     this.threads = threads;
   }
 
   @Override
   public void setUp() {
     executor = Executors.newFixedThreadPool(threads);
     pool = factory.create(10, 1000);
   }
 
   @Override
   public void runSession(Recorder recorder) throws Exception {
     CountDownLatch startLatch = new CountDownLatch(1);
     CountDownLatch endLatch = new CountDownLatch(threads);
     List<Recorder> subRecorders = new ArrayList<Recorder>();
     
     for (int i = 0; i < threads; i++) {
       Recorder subRecorder = recorder.createBlankCopy();
       subRecorders.add(subRecorder);
       executor.execute(new Worker(pool, startLatch, endLatch, subRecorder));
     }
     
     startLatch.countDown();
     endLatch.await();
     
     for (Recorder subRecorder : subRecorders) {
       recorder.add(subRecorder);
     }
   }
 
   @Override
   public void tearDown() throws Exception {
     factory.shutdown(pool);
     executor.shutdown();
   }
   
   public static class Worker implements Runnable {
     private final PoolFacade pool;
     private final CountDownLatch startLatch;
     private final CountDownLatch endLatch;
     private final Recorder recorder;
 
     public Worker(PoolFacade pool, CountDownLatch startLatch,
         CountDownLatch endLatch, Recorder recorder) {
       this.pool = pool;
       this.startLatch = startLatch;
       this.endLatch = endLatch;
       this.recorder = recorder;
     }
 
     @Override
     public void run() {
       try {
         startLatch.await();
         long start = recorder.begin();
         for (int i = 0; i < ITERATIONS; i++) {
           pool.release(pool.claim());
           start = recorder.record(start);
         }
       } catch (Exception e) {
         e.printStackTrace();
       }
       endLatch.countDown();
     }
   }
   
   public static void main(String[] args) throws Exception {
     HtmlChartsReporter chartReporter = new HtmlChartsReporter(
         new Interpretor(), "Multi-Threaded Benchmark");
     PrintingReporter printingReporter = new PrintingReporter();
     WarmupPrintingReporter warmupReporter = new WarmupPrintingReporter();
     Class<? extends Benchmark> benchmarkType = MultiThreadedBenchmark.class;
     String htmlReportFilename = "multi-threaded-results.html";
     
     // Pre-heat the lot:
     System.out.println("## Warmup");
     BenchmarkRunner.run(
         benchmarkType,
         warmupReporter, 4, 0);
     BenchmarkRunner.run(
         benchmarkType,
         warmupReporter, 1, 0);
     
     // The real run:
     System.out.println("## Benchmark");
     BenchmarkRunner.run(
         benchmarkType,
         printingReporter,
         chartReporter,
         5, 0);
     
     chartReporter.addChartRender(new ThroughputChart("Multi-Threaded Benchmark", "Threads", "Ops/Sec"));
     chartReporter.addChartRender(new LatencyHistogramChart("Latency Histograms", "Threads"));
     String report = chartReporter.generateReport();
     File file = new File(htmlReportFilename);
     if (!file.exists()) file.createNewFile();
     Files.write(file.toPath(), report.getBytes("UTF-8"));
   }
   
   private static final class Interpretor implements DataInterpretor {
     public String getBenchmarkName(Object[] args) {
       return "Multi-Threaded Benchmark";
     }
 
     @SuppressWarnings("unchecked")
     public Comparable<Object> getXvalue(Object[] args) {
      Object value = String.valueOf(args[1]);
      return (Comparable<Object>) value;
     }
 
     @Override
     public String getSeriesName(Object[] args) {
       return String.valueOf(args[0]);
     }
   }
 }
