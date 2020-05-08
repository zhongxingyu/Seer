 // Copyright (C) 2006 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.engine.process;
 
 import java.util.Arrays;
 
 import net.grinder.communication.QueuedSender;
 import net.grinder.console.messages.RegisterStatisticsViewMessage;
 import net.grinder.script.InvalidContextException;
 import net.grinder.script.NoSuchStatisticException;
 import net.grinder.script.Statistics;
 import net.grinder.statistics.ExpressionView;
 import net.grinder.statistics.StatisticsIndexMap;
 import net.grinder.statistics.StatisticsServices;
 import net.grinder.statistics.StatisticsServicesImplementation;
 import net.grinder.statistics.StatisticsServicesTestFactory;
 import net.grinder.statistics.StatisticsSet;
 import net.grinder.statistics.StatisticsView;
 import net.grinder.testutility.AssertUtilities;
 import net.grinder.testutility.CallData;
 import net.grinder.testutility.RandomStubFactory;
 
 import junit.framework.TestCase;
 
 
 /**
  * Unit test case for {@link ScriptStatisticsImplementation}.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestScriptStatisticsImplementation extends TestCase {
 
   private static final StatisticsIndexMap.LongIndex s_errorsIndex;
   private static final StatisticsIndexMap.DoubleIndex s_userDouble0Index;
 
   static {
     final StatisticsIndexMap indexMap =
       StatisticsServicesImplementation.getInstance().getStatisticsIndexMap();
 
     s_errorsIndex = indexMap.getLongIndex("errors");
     s_userDouble0Index = indexMap.getDoubleIndex("userDouble0");
   }
 
   private final RandomStubFactory m_testStatisticsHelperStubFactory =
     new RandomStubFactory(TestStatisticsHelper.class);
   private final TestStatisticsHelper m_testStatisticsHelper =
     (TestStatisticsHelper)m_testStatisticsHelperStubFactory.getStub();
 
   private final RandomStubFactory m_threadContextStubFactory =
     new RandomStubFactory(ThreadContext.class);
   private final ThreadContext m_threadContext =
     (ThreadContext)m_threadContextStubFactory.getStub();
 
   private final RandomStubFactory m_dispatchContextStubFactory =
     new RandomStubFactory(DispatchContext.class);
   private final DispatchContext m_dispatchContext =
     (DispatchContext)m_dispatchContextStubFactory.getStub();
 
   final RandomStubFactory m_queuedSenderStubFactory =
     new RandomStubFactory(QueuedSender.class);
   final QueuedSender m_queuedSender =
     (QueuedSender)m_queuedSenderStubFactory.getStub();
 
   private final StubThreadContextLocator m_threadContextLocator =
     new StubThreadContextLocator();
 
   private final StatisticsServices m_statisticsServices =
     StatisticsServicesTestFactory.createTestInstance();
   private StatisticsSet m_statisticsSet =
     m_statisticsServices.getStatisticsSetFactory().create();
 
   public void testContextChecks() throws Exception {
 
     final ScriptStatisticsImplementation scriptStatistics =
       new ScriptStatisticsImplementation(
         m_threadContextLocator,
         m_testStatisticsHelper,
         m_statisticsServices,
         m_queuedSender);
 
     // 1. Null thread context.
     assertFalse(scriptStatistics.availableForUpdate());
 
     try {
       scriptStatistics.report();
       fail("Expected InvalidContextException");
     }
     catch (InvalidContextException e) {
       AssertUtilities.assertContains(e.getMessage(), "worker threads");
     }
 
     // 2. Null dispatch context.
     m_threadContextLocator.set(m_threadContext);
     m_threadContextStubFactory.setResult("getDispatchContext", null);
     assertFalse(scriptStatistics.availableForUpdate());
 
     try {
      scriptStatistics.getSuccess();
       fail("Expected InvalidContextException");
     }
     catch (InvalidContextException e) {
       AssertUtilities.assertContains(
         e.getMessage(), "should have called setDelayReports");
     }
 
     m_threadContextStubFactory.setResult("getDispatchContext", m_dispatchContext);
     m_dispatchContextStubFactory.setResult("getStatistics", m_statisticsSet);
 
     final boolean result = scriptStatistics.getSuccess();
 
     assertEquals(
       new Boolean(result),
       m_testStatisticsHelperStubFactory
      .assertSuccess("getSuccess", m_statisticsSet).getResult());
     m_testStatisticsHelperStubFactory.assertNoMoreCalls();
 
     m_queuedSenderStubFactory.assertNoMoreCalls();
   }
 
   public void testThreadContextPassThrough() throws Exception {
     final ScriptStatisticsImplementation scriptStatistics =
       new ScriptStatisticsImplementation(
           m_threadContextLocator,
           m_testStatisticsHelper,
           m_statisticsServices,
           m_queuedSender);
 
     m_threadContextLocator.set(m_threadContext);
 
     scriptStatistics.setDelayReports(true);
     m_threadContextStubFactory.assertSuccess("setDelayReports", Boolean.TRUE);
 
     scriptStatistics.setDelayReports(false);
     m_threadContextStubFactory.assertSuccess("setDelayReports", Boolean.FALSE);
 
     scriptStatistics.report();
     m_threadContextStubFactory.assertSuccess("flushPendingDispatchContext");
 
     m_threadContextStubFactory.assertNoMoreCalls();
   }
 
   public void testStatisticsPassThrough() throws Exception {
     final ScriptStatisticsImplementation scriptStatistics =
       new ScriptStatisticsImplementation(
           m_threadContextLocator,
           m_testStatisticsHelper,
           m_statisticsServices,
           m_queuedSender);
 
     m_threadContextLocator.set(m_threadContext);
 
     m_threadContextStubFactory.setResult("getDispatchContext", m_dispatchContext);
     m_dispatchContextStubFactory.setResult("getStatistics", m_statisticsSet);
 
     scriptStatistics.setLong("errors", 1);
     assertEquals(1, scriptStatistics.getLong("errors"));
     assertEquals(1, m_statisticsSet.getValue(s_errorsIndex));
 
     scriptStatistics.setDouble("userDouble0", 5.3);
     assertEquals(5.3d, scriptStatistics.getDouble("userDouble0"), 0.01);
     assertEquals(5.3d, m_statisticsSet.getValue(s_userDouble0Index), 0.01);
 
     scriptStatistics.addDouble("userDouble0", 1.0);
     assertEquals(6.3d, scriptStatistics.getDouble("userDouble0"), 0.01);
     assertEquals(6.3d, m_statisticsSet.getValue(s_userDouble0Index), 0.01);
 
     scriptStatistics.addLong("errors", 2);
     assertEquals(3, scriptStatistics.getLong("errors"));
     assertEquals(3, m_statisticsSet.getValue(s_errorsIndex));
 
     scriptStatistics.setLong("errors", 0);
     assertEquals(0, scriptStatistics.getLong("errors"));
     assertEquals(0, m_statisticsSet.getValue(s_errorsIndex));
 
     scriptStatistics.setSuccess(false);
     m_testStatisticsHelperStubFactory.assertSuccess(
       "setSuccess", m_statisticsSet, Boolean.FALSE);
     scriptStatistics.setSuccess(true);
     m_testStatisticsHelperStubFactory.assertSuccess(
       "setSuccess", m_statisticsSet, Boolean.TRUE);
 
     try {
       scriptStatistics.getLong("not there");
       fail("Expecting NoSuchStatisticException");
     }
     catch (NoSuchStatisticException e) {
     }
 
     try {
       scriptStatistics.getDouble("not there");
       fail("Expecting NoSuchStatisticException");
     }
     catch (NoSuchStatisticException e) {
     }
   }
 
   public void testDispatchContextPassThrough() throws Exception {
     final ScriptStatisticsImplementation scriptStatistics =
       new ScriptStatisticsImplementation(
           m_threadContextLocator,
           m_testStatisticsHelper,
           m_statisticsServices,
           m_queuedSender);
 
     m_threadContextLocator.set(m_threadContext);
 
     m_threadContextStubFactory.setResult("getDispatchContext", m_dispatchContext);
 
     final long time = 213214L;
     m_dispatchContextStubFactory.setResult("getElapsedTime", new Long(time));
     assertEquals(time, scriptStatistics.getTime());
     m_dispatchContextStubFactory.assertSuccess("getElapsedTime");
     m_dispatchContextStubFactory.assertNoMoreCalls();
   }
 
   public void testRegisterStatisticsViews() throws Exception {
 
     final RandomStubFactory queuedSenderStubFactory =
       new RandomStubFactory(QueuedSender.class);
     final QueuedSender queuedSender =
       (QueuedSender)queuedSenderStubFactory.getStub();
 
     final ThreadContextLocator threadContextLocator =
       new StubThreadContextLocator();
     threadContextLocator.set(m_threadContext);
 
     final Statistics scriptStatistics =
       new ScriptStatisticsImplementation(
         threadContextLocator,
         m_testStatisticsHelper,
         m_statisticsServices,
         queuedSender);
 
     final ExpressionView expressionView =
       new ExpressionView("display", "resource key", "errors");
     final StatisticsView statisticsView = new StatisticsView();
     statisticsView.add(expressionView);
     scriptStatistics.registerSummaryStatisticsView(statisticsView);
 
     final CallData callData =
       queuedSenderStubFactory.assertSuccess(
         "queue", RegisterStatisticsViewMessage.class);
     final RegisterStatisticsViewMessage message =
       (RegisterStatisticsViewMessage)callData.getParameters()[0];
     assertEquals(statisticsView, message.getStatisticsView());
     queuedSenderStubFactory.assertNoMoreCalls();
 
     final StatisticsView summaryStatisticsView =
       m_statisticsServices.getSummaryStatisticsView();
 
     final ExpressionView[] summaryExpressionViews =
       summaryStatisticsView.getExpressionViews();
     assertTrue(Arrays.asList(summaryExpressionViews).contains(expressionView));
 
     try {
       scriptStatistics.registerDetailStatisticsView(statisticsView);
       fail("Expected InvalidContextException");
     }
     catch (InvalidContextException e) {
     }
 
     threadContextLocator.set(null);
 
     scriptStatistics.registerDetailStatisticsView(statisticsView);
 
     final StatisticsView detailStatisticsView =
       m_statisticsServices.getDetailStatisticsView();
 
     final ExpressionView[] detailExpressionViews =
       detailStatisticsView.getExpressionViews();
     assertTrue(Arrays.asList(detailExpressionViews).contains(expressionView));
   }
 }
