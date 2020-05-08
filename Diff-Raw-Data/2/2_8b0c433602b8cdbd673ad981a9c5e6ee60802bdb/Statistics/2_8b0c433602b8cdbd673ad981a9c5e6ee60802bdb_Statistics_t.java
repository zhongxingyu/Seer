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
 
 package net.grinder.script;
 
 import net.grinder.statistics.StatisticsIndexMap;
 
 
 /**
  * Script statisistics reporting API.
  *
  * <p>An instance of this interface can be obtained using
  * {@link ScriptContext#getStatistics}.
  *  This can be used in a script to query * the result of the last
  *  test. For example:
  *
  * <blockquote>
  * <pre>
  *   result1 = test1.doSomething()
  *   timeTaken1 = grinder.statistics.time
  *
  *   if grinder.statistics.success:
  *     # ...
  * </pre>
  * </blockquote>
  *
  *  <p>By default, test statistics reports are automatically sent to
  *  the console and data log when the test implementation returns to
  *  the script, and so the script cannot modify the test statistics.
  *  By using {@link #setDelayReports}, scripts can turn off this
  *  automatic reporting for the current worker thread. Having done
  *  this, the script can modify or set the statistics before they are
  *  sent to the log and the console. The statistics reports are sent
  *  at a later time as described in {@link #setDelayReports} below.
  *  For example:
  *
  *
  * <blockquote>
  * <pre>
  *   grinder.statistics.delayReports = 1
  *
  *   result1 = test1.doSomething()
  *
  *   if isFailed(result1): 
  *
  *      # Mark test as failure. The appropriate failure detection
  *      # depends on the type of test.
  *     grinder.statistics.success = 0
  * </pre>
  * </blockquote>
  *
  * <p>It is possible to set the statistics from within test
  * implementation itself. This is more useful for user statistics than
  * for the standard statistics (<em>[un]timedTransactions</em>,
  * <em>errors</em>, <em>transactionTime</em>) as the standard
  * statistics may be overridden by The Grinder engine when the test
  * finishes.
  *
  * @author Philip Aston
  * @version $Revision$
  * @see net.grinder.statistics.StatisticsView
  */ 
 public interface Statistics  {
 
   /**
    * Use to delay reporting of the last test statistics to the log and
    * the console so that the script can modify them. Normally test
    * statistics are reported automatically when the test
    * implementation returns.
    *
    * <p>With this set to <code>true</code> the test statistics will be
    * reported at the following times:
    * <ol>
    * <li>When the next test is invoked.</li>
    * <li>When the current run completes.</li>
    * <li>When the script calls {@link #report}.</li>
    * <li>When the script calls <code>setDelayReports(false)</code>.</li>
    * </ol>
    * </p>
    *
    * @param b <code>false</code> => enable automatic reporting when
    * tests retrun (the default behaviour); <code>true</code> => delay
    * reporting.
    * @see #report
    */
   void setDelayReports(boolean b);
 
   /**
    * Send the last test statistics to the data log and the console. If
    * called from within the test implementation, this will cause the
    * statistics to be sent when the test returns.
    *
    * <p>Calling this does nothing if the statistics have already been
    * reported.</p>
    *
    * @exception InvalidContextException If called from a different
    * thread to the thread in which the <code>Statistics</code> was was
    * acquired, or before the first test.
    *  @see #availableForUpdate
    */
   void report() throws InvalidContextException;
 
   /**
    * Return whether the statistics for the current test are available
    * for update. If this returns <code>true</code>, then other methods
    * will not throw {@link InvalidContextException} or
    * {@link StatisticsAlreadyReportedException}.
   * @return Whether the statistics for the current test are available
   * for update.
    */
   boolean availableForUpdate();
 
   /**
    * Sets the long statistic for the last test with index
    * <code>index</code> to the specified <code>value</code>.
    * 
    * @param index The statistic index.
    * @param value The value.
    * @exception InvalidContextException If called from a different
    * thread to the thread in which the <code>Statistics</code> was was
    * acquired, or before the first test.
    * @exception StatisticsAlreadyReportedException If the statistics
    * have already been sent for the last test performed by this thread
    * - see {@link #setDelayReports}.
    *  @see #availableForUpdate
    **/
   void setValue(StatisticsIndexMap.LongIndex index, long value)
     throws InvalidContextException, StatisticsAlreadyReportedException;
 
   /**
    * Sets the double statistic for the last test with index
    * <code>index</code> to the specified <code>value</code>.
    * 
    * @param index The statistic index.
    * @param value The value.
    * @exception InvalidContextException If called from a different
    * thread to the thread in which the <code>Statistics</code> was was
    * acquired, or before the first test.
    * @exception StatisticsAlreadyReportedException If the statistics
    * have already been sent for the last test performed by this thread
    * - see {@link #setDelayReports}.
    *  @see #availableForUpdate
    **/
   void setValue(StatisticsIndexMap.DoubleIndex index, double value)
     throws InvalidContextException, StatisticsAlreadyReportedException;
 
   /**
    * Return the long value for the last test specified by
    * <code>index</code>.
    *
    * @param index The statistic index.
    * @return The value.
    */
   long getValue(StatisticsIndexMap.LongIndex index);
 
   /**
    * Return the double value for the last test specified by
    * <code>index</code>.
    *
    * @param index The statistic index.
    * @return The value.
    */
   double getValue(StatisticsIndexMap.DoubleIndex index);
 
   /**
    * Convenience method that sets whether the last test should be
    * considered a success or not.
    *
    * @param success If <code>true</code>, <em>timedTransactions</em>
    * (or <em>untimedTransactions</em> if this process isn't recording
    * time) is set to one and <em>errors</em> is set to zero. Otherwise
    * <em>untimedTransactions</em> and <em>timedTransactions</em> are
    * set to zero and <em>errors</em> is set to one.
    * @exception InvalidContextException If called from a different
    * thread to the thread in which the <code>Statistics</code> was was
    * acquired, or before the first test.
    * @exception StatisticsAlreadyReportedException If the statistics
    * have already been sent for the last test performed by this thread
    * - see {@link #setDelayReports}.
    *  @see #availableForUpdate
    */
   void setSuccess(boolean success)
     throws InvalidContextException, StatisticsAlreadyReportedException;
 
   /**
    * Convenience method that returns whether the last test was a
    * success (<em>errors</em> is zero) or not.
    *
    * @return Whether the last test was a success.
    */
   boolean getSuccess();
 
   /**
    * Convenience method that returns the time taken by the last test.
    *
    * @return The transaction time for the last test.
    */
   long getTime();
 }
