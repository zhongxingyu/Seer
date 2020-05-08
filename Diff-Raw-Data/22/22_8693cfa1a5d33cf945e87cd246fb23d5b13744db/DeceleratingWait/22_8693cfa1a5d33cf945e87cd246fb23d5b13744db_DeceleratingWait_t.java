 /*
  * Copyright 2012 CoreMedia AG
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.joala.condition.timing;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import org.hamcrest.Matcher;
 
 import javax.annotation.Nonnegative;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.concurrent.TimeUnit;
 
 /**
  * <p>
  * A waiter that uses the exponential backoff approach when waiting for a condition.
  * Starting with a minimum wait period, the algorithm waits successively longer on
  * subsequent attempts, increasing the wait time by a small percentage. This allows
  * short waits to be completed quickly while reducing the overhead for repeated
  * checks during long waits.
  * </p><p>
  * The expected number of checks grows logarithmically with
  * the wait duration, while the wait duration remains within a constant factor of the
  * actual time until the condition holds true.
  * </p>
  */
 // This class was in part derived from org.openqa.selenium.support.ui.FluentWait,
 // which is available under an Apache 2.0 license.
 public class DeceleratingWait implements Wait {
 
   @VisibleForTesting
  static final long DEFAULT_TIMEOUT_MILLIS = 500L;
   @VisibleForTesting
   static final long INITIAL_DELAY = 10L;
   @VisibleForTesting
   static final double DECELERATION_FACTOR = 1.1;
   @Nonnull
   private final Timeout timeout;
   @Nonnegative
   private final double timeoutFactor;
   @Nonnull
   private final WaitFailStrategy failStrategy;
 
   public DeceleratingWait() {
     this(new TimeoutImpl(DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
   }
 
   public DeceleratingWait(@Nonnull final Timeout timeout) {
     this(timeout, new WaitTimeoutFailStrategy());
   }
 
   public DeceleratingWait(@Nonnull final Timeout timeout, @Nonnegative final double timeoutFactor) {
     this(timeout, timeoutFactor, new WaitTimeoutFailStrategy());
   }
 
   public DeceleratingWait(@Nonnull final Timeout timeout, @Nonnull final WaitFailStrategy failStrategy) {
     this(timeout, 1d, failStrategy);
   }
 
   public DeceleratingWait(@Nonnull final Timeout timeout, @Nonnegative final double timeoutFactor, @Nonnull final WaitFailStrategy failStrategy) {
     this.timeout = timeout;
     this.timeoutFactor = timeoutFactor;
     this.failStrategy = failStrategy;
   }
 
   /**
    * Return the current time in milliseconds. Overwrite for tests.
    *
    * @return the current time
    */
   @VisibleForTesting
   protected long nowMillis() {
     return System.currentTimeMillis();
   }
 
   /**
    * Sleep the given number of milliseconds.
    *
    * @param millis how long to sleep
    * @throws InterruptedException if the current thread has been interrupted
    */
   @VisibleForTesting
   protected void sleep(final long millis) throws InterruptedException {
     Thread.sleep(millis);
   }
 
   @Override
   public final <F, T> T until(@Nonnull final F input, @Nonnull final Function<? super F, T> stateQuery) {
     return until(null, input, stateQuery, null);
   }
 
   @Override
   public <F, T> T until(@Nullable final String message,
                         @Nonnull final F input,
                         @Nonnull final Function<? super F, T> stateQuery,
                         @Nullable final Matcher<? super T> matcher) {
     // Compute the deadlineTimeMillis until which we want to wait.
     final long startTimeMillis = nowMillis();
     final long timeoutMillis = timeout.in(TimeUnit.MILLISECONDS, timeoutFactor);
     final long deadlineTimeMillis = startTimeMillis + timeoutMillis;
     // At first, wait 10ms between checks.
     long delay = INITIAL_DELAY;
     // We keep track of the last exception to be able to rethrow it.
     IgnorableStateQueryException lastException = null;
     T lastState = null;
     while (true) {
       // Measure the time that the evaluation takes.
       final long beforeEvaluationTimeMillis = nowMillis();
       try {
         // Evaluate and report the result unless it is null, false, or an exception.
         final T result = stateQuery.apply(input);
         if (matcher == null || matcher.matches(result)) {
           return result;
         }
         lastState = result;
       } catch (IgnorableStateQueryException e) {
         // Remember the exception for rethrowing.
         lastException = e;
       }
       final long afterEvaluationTimeMillis = nowMillis();
       // Are we past the deadlineTimeMillis?
       if (afterEvaluationTimeMillis > deadlineTimeMillis) {
         failAtDeadline(message, stateQuery, input, lastException, lastState, matcher, startTimeMillis);
       }
       delay = sleepAndRecalculateDelay(delay, deadlineTimeMillis, beforeEvaluationTimeMillis, afterEvaluationTimeMillis);
     }
   }
 
   private long sleepAndRecalculateDelay(long previousDelay, final long deadlineTimeMillis, final long beforeEvaluationTimeMillis, final long afterEvaluationTimeMillis) {
     // Leave at least as much time between two checks as the check itself took.
     final long lastDuration = afterEvaluationTimeMillis - beforeEvaluationTimeMillis;
     if (lastDuration > previousDelay) {
       previousDelay = lastDuration;
     }
 
     // Wait, but not much longer than until the deadlineTimeMillis and at least a millisecond.
     try {
       sleep(Math.max(1, Math.min(previousDelay, deadlineTimeMillis + 100 - afterEvaluationTimeMillis)));
     } catch (InterruptedException e) {
       throw new IllegalStateException("unexpected interruption", e);
     }
 
     // Make checks less and less frequently.
     // Increase the wait period using the deceleration factor, but
     // wait at least one millisecond longer next time.
     previousDelay = Math.max(previousDelay + 1, (long) (previousDelay * DECELERATION_FACTOR));
     return previousDelay;
   }
 
   private <F, T> void failAtDeadline(@Nullable final String message,
                                      @Nonnull final Function<? super F, T> stateQuery,
                                      @Nonnull final F input,
                                      @Nullable final IgnorableStateQueryException lastException,
                                      @Nullable final T lastState,
                                      @Nonnull final Matcher<? super T> matcher,
                                      @Nonnegative final long startTimeMillis) {
     final long consumedMillis = nowMillis() - startTimeMillis;
     if (lastException == null) {
       failStrategy.fail(message, stateQuery, input, lastState, matcher, consumedMillis);
     } else {
       failStrategy.fail(message, stateQuery, input, lastException, consumedMillis);
     }
   }
 
   @Override
   public String toString() {
     return Objects.toStringHelper(this)
             .add("timeout", timeout)
             .add("timeoutFactor", timeoutFactor)
             .add("failStrategy", failStrategy)
             .toString();
   }
 }
