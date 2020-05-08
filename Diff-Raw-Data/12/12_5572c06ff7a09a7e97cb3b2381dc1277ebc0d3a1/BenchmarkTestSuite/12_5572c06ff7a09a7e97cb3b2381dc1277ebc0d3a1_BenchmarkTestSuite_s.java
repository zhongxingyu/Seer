 package org.nx.pkg4;
 
 import java.nio.file.Paths;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.nx.pkg4.node.NxNode;
 
 import com.google.common.base.Stopwatch;
 
 public class BenchmarkTestSuite implements Runnable {
   private static final Logger LOGGER = Logger.getLogger(BenchmarkTestSuite.class.getName());
   
   private NxParser parser;
   
   public static void main(String[] args) {
     BenchmarkTestSuite test = new BenchmarkTestSuite();
     test.run();
   }
   
   private class LdTest implements Runnable {
     public void run() {
       try {
         parser = parser().parse();
       } catch(Throwable t) {
         LOGGER.log(Level.WARNING, "LD test failed", t);
       }
     }
   }
   
   private class SSTest implements Runnable {
     public void run() {
       NxNode node = parser.getRootNode().getChild("Map").getChild("Map").getChild("Map1").getChild("105060000.img").getChild("1").getChild("tile");
       for(NxNode subnode : node.getChildren()) {
         if(node.getChild(subnode.getName()) != subnode)
           throw new IllegalStateException("Indexed subnode does not equal iterated subnode");
       }
     }
   }
   
   private class PRTest implements Runnable {
     public void run() {
       try {
         NxContainer container = parser().parse();
         recurse(container.getRootNode());
       } catch(Throwable t) {
         LOGGER.log(Level.WARNING, "PR test failed", t);
       }
     }
   }
   
   private class ReTest implements Runnable {
     public void run() {
       recurse(parser.getRootNode());
     }
   }
   
   public void run() {
     LOGGER.info("Running NX test suite");
     try {
       test("Ld", 1000, new LdTest());
       test("SS", 1000, new SSTest());
       test("PR", 2, new PRTest());
       test("Re", 2, new ReTest());
     } catch(Throwable t) {
      LOGGER.log(Level.SEVERE, "Uncaught exeption", t);
     }
   }
   
   private void test(String description, int iterations, Runnable task) {
     LOGGER.info("Running test: " + description);
     
     long best = Long.MAX_VALUE;
     Stopwatch stopwatch = new Stopwatch();
     
     for(int i = 0; i < iterations; i++) {
       stopwatch.start();
       task.run();
       stopwatch.stop();
       long elapsed = stopwatch.elapsed(TimeUnit.MICROSECONDS);
       best = Math.min(best, elapsed);
       stopwatch.reset();
     }
     
     LOGGER.info("Finished test " + description + " in " + best + "µs");
   }
   
   private NxParser parser() {
     return new NxParser(Paths.get("Data.nx"));
   }
   
   private void recurse(NxNode node) {
     for(NxNode subnode : node.getChildren()) {
       recurse(subnode);
     }
   }
 }
