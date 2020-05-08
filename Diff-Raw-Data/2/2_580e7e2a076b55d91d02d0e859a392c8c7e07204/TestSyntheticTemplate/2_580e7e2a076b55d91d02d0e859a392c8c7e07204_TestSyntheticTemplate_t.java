 package org.webmacro.template;
 
 import java.io.*;
 import junit.framework.*;
 import org.webmacro.*;
 
 /** a simple test case to load and execute a file
  * via the path relative to the classpath.
 */
 public class TestSyntheticTemplate extends TemplateTestCase {
 
   protected int iterationCount = 12;
   protected int threadCount = 2;
 
   private static final String fileName = "org/webmacro/template/synthetictest.wm";
   private static final String reportName = "org/webmacro/template/syntheticreport.wm";
   private static final String[] LOAD = { 
     "1.wm", "2.wm", "3.wm", "4.wm", "5.wm", 
     "6.wm", "7.wm", "8.wm", "9.wm", "10.wm", 
   };
 
   private Context context = null;
   
   public TestSyntheticTemplate (String name) {
     super (name);
   }
 
   public static Test suite() {
     TestSuite suite= new TestSuite();
 
     suite.addTest(new TestSyntheticTemplate("load") {
         protected WebMacro createWebMacro() throws Exception {
           return new WM("org/webmacro/template/TST.properties");
         }
         
         protected void runTest() throws Exception {
           this.threadCount = _wm.getBroker().getIntegerSetting("TestSyntheticTemplate.ThreadCount", this.threadCount);
           this.iterationCount = _wm.getBroker().getIntegerSetting("TestSyntheticTemplate.IterationCount", this.threadCount);
           this.testLoadAndToss();
           this.testLoad();
         }
       }
     );
     return suite;
   }
 
 
   protected void stuffContext (Context context) throws Exception {
     // keep the context throughout the test pattern
     this.context = context;
   }
 
 //    public void testEvaluate() throws Exception {
 //      context.put("runLoad", Boolean.FALSE);
 //      executeFileTemplate(fileName);
 //    }
 
 //    public void testShow() throws Exception {
 //      context.put("runLoad", Boolean.FALSE);
 //      String value = templateFileToString(fileName);
 //      String output = executeStringTemplate(value);
 //    }
 
   /** Throw the first test result out due to parsing. */
   public void testLoadAndToss() throws Exception {
     context.put("runLoad", Boolean.TRUE);
     context.put("load", LOAD);
     long tet = System.currentTimeMillis();
     String value = executeFileTemplate(fileName);
     tet = System.currentTimeMillis() - tet;
     if (value.length() < 10000) 
       fail("Total character count must exceed 10,000 characters");
     Thread.sleep(2500); // let the immutable cache save to its immutable map
   }
 
   /**
    * This is the loading test and it is self verifying as to correctness.
    * by counting the character stream.
    * <p>
    * Note: this test is not run within a thread decorator.
    * That is the work of clarkware.junitperf.LoadTest
    */
   public void testLoad() throws Exception {
     context.put("runLoad", Boolean.TRUE);
     context.put("load", LOAD);
     long min = 0;
     long max = System.currentTimeMillis();
     String throwAway = executeFileTemplate(fileName);
     max = System.currentTimeMillis() - max;
     min = max;
     int contentSize = throwAway.length();
 
     long tet, singleTet; 
 
     // begin load
     tet = System.currentTimeMillis();
     for (int index = 0; index < iterationCount; index++) {
       singleTet = System.currentTimeMillis();
       String value = executeFileTemplate(fileName);
       singleTet = System.currentTimeMillis() - singleTet;
       if (value.length() != contentSize) fail("Load failure: content size mismatch");
       if (singleTet < min) min = singleTet;
       if (singleTet > max) max = singleTet;
     }
     tet = System.currentTimeMillis()-tet;
 
     // test load using threads:
     ThreadLoad threadLoad = new ThreadLoad();
     threadLoad.execute();
     
     report(iterationCount, 
            tet - max - min,
            contentSize,
            threadLoad.tet,
            threadLoad.worstCase,
            threadLoad.bestCase,
            threadCount,
            threadLoad.duration);
   }
 
   /** A webmacro report sent to LoadReport.html. */
   protected void report(int iterationCount, long totalTime,
                         int characterCount,
                         long threadTet,
                         long worstCase,
                         long bestCase,
                         int threadCount,
                         long threadDuration) throws Exception {
     context.put("IterationCount", iterationCount);
     context.put("TotalElapsedTime", totalTime);
     context.put("AverageTime", totalTime/iterationCount);
     context.put("CharacterCount", characterCount);
     context.put("SystemProperty", System.getProperties());
     context.put("TotalMemory", Runtime.getRuntime().totalMemory()/1024);
     context.put("ScriptValue", templateFileToString(fileName));
     context.put("Today", new java.util.Date());
     context.put("ThreadDuration", threadDuration);
     context.put("ThreadTotalWaitTime", threadTet-totalTime);
     context.put("ThreadAverageTime", threadTet/iterationCount );
 
     context.put("ThreadWorstCase", worstCase);
     context.put("ThreadBestCase", bestCase);
     context.put("ThreadCount", threadCount);
     context.put("ThreadIterations", iterationCount);
     String report = executeFileTemplate(reportName);
    	PrintWriter p = new PrintWriter( new FileOutputStream("LoadReport.html") );
    	p.write(report);
    	p.close();
   }
 
   /**
    * This class performs a load test but divides the load into a
    * proportional number of of threads.
    * <p>
    * execute() to execute the load and then extract the metrics.
    * <p>
    * The worst and best case are not thrown out.
    */
   class ThreadLoad {
     long tet = 0;
     long worstCase = 0;
     long bestCase = 9999999999l;
     long duration;
 
     void execute() throws Exception {
       ThreadTest[] threadTest = new ThreadTest[threadCount];
       for (int index = 0; index < threadCount; index++)
         threadTest[index] = new ThreadTest();
       duration = System.currentTimeMillis();
       for (int index = 0; index < threadCount; index++)
         threadTest[index].start();
       // Thread.sleep(1000); // allows the threads to start
       for (int index = 0; index < threadCount; index++) {
         threadTest[index].join();
         // Thread.yield();
       }
       duration = System.currentTimeMillis() - duration;
       for (int index = 0; index < threadCount; index++)
         tet += threadTest[index].tet;
     }
 
     class ThreadTest extends Thread {
       long tet = 0;
       public void run() {
         this.tet = System.currentTimeMillis();
         for (int index = 0; index < iterationCount; index++) {
           long it = System.currentTimeMillis();
           try {
             String value = executeFileTemplate(fileName);
             // Thread.yield(); // allow other threads time to run as well;
           }
          catch (Exception e) {fail("Evaluation failed in thread: "+e);}
           it = System.currentTimeMillis() - it;
           if (it < bestCase) bestCase = it;
           if (it > worstCase) worstCase = it;
         }
         this.tet = System.currentTimeMillis() - tet;
       }
     }
   }
   
 }
