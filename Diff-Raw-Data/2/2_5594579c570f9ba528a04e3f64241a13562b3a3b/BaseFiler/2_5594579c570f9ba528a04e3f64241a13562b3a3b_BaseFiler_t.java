 package com.mapr.load;
 
 import java.io.IOException;
 
 public class BaseFiler implements Filer {
   // these are reset per trace
   private TopTailAnalyzer[] longAnalyzer = new TopTailAnalyzer[2];
 
   // these are reset per segment
   private TopTailAnalyzer[] shortAnalyzer = new TopTailAnalyzer[2];
 
   double t0 = System.nanoTime() * 1e-9;
 
   public BaseFiler() {
     for (int i = 0; i < longAnalyzer.length; i++) {
       longAnalyzer[i] = new TopTailAnalyzer();
       shortAnalyzer[i] = new TopTailAnalyzer();
     }
   }
 
   public Filer with(Filer baseFiler) {
     // ignore
     return this;
   }
 
   public void read(double t, int blockSize) throws IOException {
     throw new UnsupportedOperationException("default no can do");  //To change body of created methods use File | Settings | File Templates.
   }
 
   public void write(double t, int blockSize) throws IOException {
     throw new UnsupportedOperationException("default no can do");  //To change body of created methods use File | Settings | File Templates.
   }
 
   public void segmentStart(double t) {
     for (TopTailAnalyzer analyzer : shortAnalyzer) {
       analyzer.reset(t);
     }
   }
 
   public void segmentEnd(double t) {
     System.out.printf("%10.3f %5d %5d ", System.nanoTime() * 1e-9 - t0, latencySamples(Op.WRITE), latencySamples(Op.READ));
 
     if (latencySamples(Op.WRITE) > 100) {
       System.out.printf("%.3f %.3f %.3f %.3f %.3f %.3f ",
         meanBlocksPerSecond(Op.WRITE, t), quantiles(Op.WRITE, 2), quantiles(Op.WRITE, 3), quantiles(Op.WRITE, 4), quantiles(Op.WRITE, 5), quantiles(Op.WRITE, 20));
       if (latencySamples(Op.READ) > 100) {
         System.out.printf("%.3f %.3f %.3f %.3f %.3f %.3f\n",
          meanBlocksPerSecond(Op.READ, t), quantiles(Op.READ, 2), quantiles(Op.READ, 3), quantiles(Op.READ, 4), quantiles(Op.READ, 5), quantiles(Op.READ, 20));
       } else {
         System.out.printf("\n");
       }
     } else {
       System.out.printf("\n");
     }
 
   }
 
   private double meanBlocksPerSecond(Op op, double t) {
     return shortAnalyzer[op.ordinal()].meanBlocksPerSecond(t);
   }
 
   public double currentTime() {
     throw new UnsupportedOperationException("default no can do");  //To change body of created methods use File | Settings | File Templates.
   }
 
   public void sleep(double delay) throws InterruptedException {
     throw new UnsupportedOperationException("default no can do");  //To change body of created methods use File | Settings | File Templates.
   }
 
   public void reset(double t) {
     for (TopTailAnalyzer analyzer : longAnalyzer) {
       analyzer.reset(t);
     }
   }
 
   public final void recordLatency(Op kind, double latency, double bytes) {
     longAnalyzer[kind.ordinal()].add(latency, bytes);
     shortAnalyzer[kind.ordinal()].add(latency, bytes);
   }
 
   public final double quantiles(Op kind, int nines) {
     return longAnalyzer[kind.ordinal()].quantile(nines);
   }
 
   public final double meanBytesPerSecond(Op kind, double t) {
     return shortAnalyzer[kind.ordinal()].meanBytesPerSecond(t);
   }
 
 
 
   public final double meanLatency(Op kind) {
     return longAnalyzer[kind.ordinal()].meanLatency();
   }
 
   public final long latencySamples(Op kind) {
     return longAnalyzer[kind.ordinal()].size();
   }
 }
