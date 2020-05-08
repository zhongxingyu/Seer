 // Copyright (C) 2003 Philip Aston
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
 
 import java.io.PrintWriter;
 
 import net.grinder.common.GrinderException;
 import net.grinder.script.InvalidContextException;
 import net.grinder.script.Statistics;
 import net.grinder.script.StatisticsAlreadyReportedException;
 import net.grinder.statistics.CommonStatisticsViews;
 import net.grinder.statistics.ExpressionView;
 import net.grinder.statistics.StatisticExpression;
 import net.grinder.statistics.StatisticsIndexMap;
 import net.grinder.statistics.TestStatistics;
 import net.grinder.statistics.TestStatisticsFactory;
 
 
 
 /**
  * Implement the script statistics interface.
  *
  * <p>Package scope.
  *
  * @author Philip Aston
  * @version $Revision$
  **/
 final class ScriptStatisticsImplementation implements Statistics {
 
   private static final StatisticsIndexMap.LongIndex s_errorsIndex;
   private static final StatisticsIndexMap.LongIndex s_timedTransactionsIndex;
   private static final StatisticsIndexMap.LongIndex s_untimedTransactionsIndex;
   private static final StatisticsIndexMap.LongIndex s_transactionTimeIndex;
 
   static {
     final StatisticsIndexMap indexMap = StatisticsIndexMap.getInstance();
 
     try {
       s_errorsIndex = indexMap.getIndexForLong("errors");
       s_timedTransactionsIndex =
 	indexMap.getIndexForLong("timedTransactions");
       s_untimedTransactionsIndex =
 	indexMap.getIndexForLong("untimedTransactions");
       s_transactionTimeIndex =
 	indexMap.getIndexForLong("timedTransactionTime");
     }
     catch (GrinderException e) {
       throw new ExceptionInInitializerError(
 	"Assertion failure, " +
 	"ScriptStatisticsImplementation could not initialise: " +
 	e.getMessage());
     }
   }
 
   private final ThreadContext m_threadContext;
   private final StringBuffer m_buffer = new StringBuffer();
   private final int m_bufferAfterThreadIDIndex;
   private final PrintWriter m_dataWriter;
   private final boolean m_recordTime;
   private final ExpressionView[] m_detailExpressionViews =
     CommonStatisticsViews.getDetailStatisticsView().getExpressionViews();
 
   private final TestStatistics m_testStatistics = 
     TestStatisticsFactory.getInstance().create();
 
   private TestData m_currentTestData = null;
   private boolean m_noTests = true;
   private boolean m_delayReports = false;
   private int m_lastRunNumber = -1;
   private int m_bufferAfterRunNumberIndex = -1;
 
   public ScriptStatisticsImplementation(ThreadContext threadContext,
 					PrintWriter dataWriter,
 					boolean recordTime) {
     m_threadContext = threadContext;
     m_dataWriter = dataWriter;
     m_recordTime = recordTime;
 
     m_buffer.append(m_threadContext.getThreadID());
     m_buffer.append(", ");
     m_bufferAfterThreadIDIndex = m_buffer.length();
   }
 
   public final void setDelayReports(boolean b) {
     if (!b) {
       reportInternal();
     }
 
     m_delayReports = b;
   }
   
   public final void report() throws InvalidContextException {
     checkCallContext();
     reportInternal();
   }
 
   private final void checkCallContext() throws InvalidContextException {
 
     final ThreadContext threadContext = ThreadContext.getThreadInstance();
 
     if (threadContext == null) {
       throw new InvalidContextException(
 	"Statistics interface is only supported for worker threads.");
     }
 
     if (threadContext != m_threadContext) {
       throw new InvalidContextException(
	"Statistics objects must be used from the worker thread from" +
 	"which they are acquired.");
     }

    if (m_noTests) {
      throw new InvalidContextException(
	"This worker thread has not yet perfomed any tests.");
    }
   }
 
   private final void checkNotAlreadyReported()
     throws StatisticsAlreadyReportedException {
 
     if (m_currentTestData == null) {
       throw new StatisticsAlreadyReportedException(
 	"The statistics for the last test performed by this thread have " +
 	"already been reported. Perhaps you should have called " +
 	"setDelayReports(true)?");
     }
   }
   
   public final void setValue(StatisticsIndexMap.LongIndex index, long value)
     throws InvalidContextException, StatisticsAlreadyReportedException {
 
     checkCallContext();
     checkNotAlreadyReported();
     m_testStatistics.setValue(index, value);
   }
 
   public final void setValue(StatisticsIndexMap.DoubleIndex index,
 			     double value)
     throws InvalidContextException, StatisticsAlreadyReportedException {
 
     checkCallContext();
     checkNotAlreadyReported();
     m_testStatistics.setValue(index, value);
   }
 
   public final long getValue(StatisticsIndexMap.LongIndex index) {
 
     return m_testStatistics.getValue(index);
   }
 
   public final double getValue(StatisticsIndexMap.DoubleIndex index) {
 
     return m_testStatistics.getValue(index);
   }
 
   public final void setSuccess(boolean success)
     throws InvalidContextException, StatisticsAlreadyReportedException {
 
     checkCallContext();
     checkNotAlreadyReported();
 
     if (success) {
       setSuccessNoChecks();
     }
     else {
       setErrorNoChecks();
     }
   }
 
   public final boolean getSuccess() {
     return m_testStatistics.getErrors() == 0;
   }
 
   public final long getTime() {
     return getValue(s_transactionTimeIndex);
   }
 
   final void setSuccessNoChecks() {
 
     if (m_recordTime) {
       m_testStatistics.setValue(s_timedTransactionsIndex, 1);
       m_testStatistics.setValue(s_untimedTransactionsIndex, 0);
     }
     else {
       m_testStatistics.setValue(s_timedTransactionsIndex, 0);
       m_testStatistics.setValue(s_untimedTransactionsIndex, 1);
     }
 
     m_testStatistics.setValue(s_errorsIndex, 0);
   }
 
   final void setErrorNoChecks() {
 
     m_testStatistics.setValue(s_untimedTransactionsIndex, 0);
     m_testStatistics.setValue(s_timedTransactionsIndex, 0);
     m_testStatistics.setValue(s_transactionTimeIndex, 0);
     m_testStatistics.setValue(s_errorsIndex, 1);
   }
 
   final void setTimeNoChecks(long time) {
 
     if (m_recordTime) {
       m_testStatistics.setValue(s_transactionTimeIndex, time);
     }
   }
 
   final void beginTest(TestData testData) {
 
     // Flush any pending report.
     reportInternal();
 
     m_currentTestData = testData;
     m_testStatistics.reset();
     m_noTests = false;
   }
 
   final void endTest() {
     if (!m_delayReports) {
       reportInternal();
     }
   }
 
   final void endRun() {
     reportInternal();
   }
 
   private final void reportInternal() {
 
     if (m_currentTestData != null) {
       if (m_dataWriter != null) {
 	final int runNumber = m_threadContext.getRunNumber();
 	
 	if (runNumber == m_lastRunNumber) {
 	  m_buffer.setLength(m_bufferAfterRunNumberIndex);
 	}
 	else {
 	  m_lastRunNumber = runNumber;
 
 	  m_buffer.setLength(m_bufferAfterThreadIDIndex);
 	  m_buffer.append(runNumber);
 	  m_buffer.append(", " );
 	  m_bufferAfterRunNumberIndex = m_buffer.length();
 	}
 
 	m_buffer.append(m_currentTestData.getTest().getNumber());
 
 	for (int i=0; i<m_detailExpressionViews.length; ++i) {
 	  m_buffer.append(", ");
 
 	  final StatisticExpression expression =
 	    m_detailExpressionViews[i].getExpression();
 
 	  if (expression.isDouble()) {
 	    m_buffer.append(expression.getDoubleValue(m_testStatistics));
 	  }
 	  else {
 	    m_buffer.append(expression.getLongValue(m_testStatistics));
 	  }
 	}
 		
 	m_dataWriter.println(m_buffer);
       }
 
       m_currentTestData.getStatistics().add(m_testStatistics);
       m_currentTestData = null;
     }
   }
 }
 
