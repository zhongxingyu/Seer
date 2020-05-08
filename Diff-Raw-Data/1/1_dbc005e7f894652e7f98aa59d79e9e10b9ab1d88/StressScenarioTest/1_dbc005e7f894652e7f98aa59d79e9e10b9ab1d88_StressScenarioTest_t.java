 package org.nohope.test.stress;
 
 import org.junit.Ignore;
 import org.junit.Test;
 import org.nohope.test.stress.action.Get;
 import org.nohope.test.stress.action.Invoke;
 
 import java.util.Map;
 
 import static org.junit.Assert.*;
 
 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
  * @since 2013-12-28 00:10
  */
 public class StressScenarioTest {
 
     @Test
     @Ignore("for manual tests only")
     public void roughTest() throws InterruptedException {
         final StressResult m1 =
                 StressScenario.of(TimerResolution.MILLISECONDS)
                               .measure(50, 1000, new NamedAction("test1") {
                                   @Override
                                   protected void doAction(final MeasureData p)
                                           throws Exception {
                                       Thread.sleep(10);
                                   }
                               });
         final StressResult m2 =
                 StressScenario.of(TimerResolution.NANOSECONDS)
                               .measure(50, 1000, new NamedAction("test2") {
                                   @Override
                                   protected void doAction(final MeasureData p)
                                           throws Exception {
                                       Thread.sleep(10);
                                   }
                               });
 
         System.err.println(m1);
         System.err.println();
         System.err.println(m2);
         System.err.println("------------------------------------------------");
 
         final StressResult m3 =
                 StressScenario.of(TimerResolution.MILLISECONDS)
                               .measure(50, 1000, new Action() {
                                   @Override
                                   protected void doAction(final MeasureProvider p)
                                           throws Exception {
                                       p.invoke("test1", new Invoke() {
                                           @Override
                                           public void invoke() throws Exception {
                                               Thread.sleep(10);
                                           }
                                       });
                                   }
                               });
         final StressResult m4 =
                 StressScenario.of(TimerResolution.NANOSECONDS)
                               .measure(50, 1000, new Action() {
                                   @Override
                                   protected void doAction(final MeasureProvider p)
                                           throws Exception {
                                       p.invoke("test1", new Invoke() {
                                           @Override
                                           public void invoke() throws Exception {
                                               Thread.sleep(10);
                                           }
                                       });
                                   }
                               });
 
         System.err.println(m3);
         System.err.println();
         System.err.println(m4);
     }
 
     @Test
     public void counts() throws InterruptedException {
         final StressResult m =
                 StressScenario.of(TimerResolution.NANOSECONDS)
                               .measure(2, 100, new NamedAction("test") {
                                   @Override
                                   protected void doAction(final MeasureData p) throws Exception {
                                       if (p.getOperationNumber() >= 100) {
                                           throw new IllegalStateException();
                                       }
                                       Thread.sleep(1);
                                   }
                               });
 
         final Map<String, IStressStat> results = m.getResults();
         assertNotNull(m.toString());
         assertEquals(1, results.size());
         final Result testResult = results.get("test").getResult();
         assertNotNull(testResult);
         assertTrue(testResult.getThroughput() <= 2000);
         assertTrue(testResult.getWorkerThroughput() <= 1000);
         assertEquals(100, results.get("test").getInvocationTimes().size());
         assertEquals(100, m.getFails());
         assertEquals(100,
                 m.getResults()
                  .get("test")
                  .getErrorStats()
                  .get(IllegalStateException.class)
                  .size()
         );
 
         final StressResult m2 =
                 StressScenario.of(TimerResolution.MILLISECONDS)
                               .measure(2, 100, new NamedAction("test") {
                                   @Override
                                   protected void doAction(final MeasureData p) throws Exception {
                                       Thread.sleep(10);
                                   }
                               });
         assertNotNull(m2.toString());
         assertTrue(m2.getRuntime() >= 1);
         assertTrue(m2.getApproxThroughput() <= 200);
 
         final StressResult m3 =
                 StressScenario.of(TimerResolution.MILLISECONDS)
                               .measure(2, 100, new Action() {
                                   @Override
                                   protected void doAction(final MeasureProvider p) throws Exception {
                                       p.invoke("test", new Invoke() {
                                           @Override
                                           public void invoke() throws Exception {
                                               Thread.sleep(10);
                                           }
                                       });
                                   }
                               });
         assertNotNull(m3.toString());
         assertTrue(m3.getRuntime() >= 1);
         assertTrue(m3.getApproxThroughput() <= 200);
 
         final StressResult m4 =
                 StressScenario.of(TimerResolution.MILLISECONDS)
                               .measure(2, 100, new Action() {
                                   @Override
                                   protected void doAction(final MeasureProvider p) throws Exception {
                                       p.invoke("test", new Get<Object>() {
                                           @Override
                                           public Object get() throws Exception {
                                               Thread.sleep(10);
                                               return null;
                                           }
                                       });
                                   }
                               });
         assertNotNull(m4.toString());
         assertTrue(m4.getRuntime() >= 1);
         assertTrue(m4.getApproxThroughput() <= 200);
     }
 }
