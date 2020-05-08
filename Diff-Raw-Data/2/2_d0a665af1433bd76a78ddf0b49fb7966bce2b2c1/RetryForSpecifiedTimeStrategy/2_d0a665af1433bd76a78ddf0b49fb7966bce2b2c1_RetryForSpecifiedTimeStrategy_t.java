 /*****************************************************************************************
  * *** BEGIN LICENSE BLOCK *****
  *
  * Version: MPL 2.0
  *
  * echocat Jomon, Copyright (c) 2012 echocat
  *
  * This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
  *
  * *** END LICENSE BLOCK *****
  ****************************************************************************************/
 
 package org.echocat.jomon.runtime.concurrent;
 
 import org.echocat.jomon.runtime.util.Duration;
 import org.echocat.jomon.runtime.util.IncreasingDurationRequirement;
 
 import javax.annotation.Nonnegative;
 import javax.annotation.Nonnull;
 import javax.annotation.concurrent.ThreadSafe;
 
 import static java.lang.Thread.currentThread;
 import static org.echocat.jomon.runtime.util.DurationGenerator.generateDuration;
 
 @ThreadSafe
 public class RetryForSpecifiedTimeStrategy<T> extends BaseRetryingStrategy<T, RetryForSpecifiedTimeStrategy<T>> {
 
     @Nonnull
     public static <T> RetryForSpecifiedTimeStrategy<T> retryForSpecifiedTimeOf(@Nonnull String maximumWaitTime) {
         return new RetryForSpecifiedTimeStrategy<>(maximumWaitTime);
     }
 
     @Nonnull
     public static <T> RetryForSpecifiedTimeStrategy<T> retryForSpecifiedTimeOf(@Nonnegative long maximumWaitTime) {
         return new RetryForSpecifiedTimeStrategy<>(maximumWaitTime);
     }
 
     @Nonnull
     public static <T> RetryForSpecifiedTimeStrategy<T> retryForSpecifiedTimeOf(@Nonnull Duration maximumWaitTime) {
         return new RetryForSpecifiedTimeStrategy<>(maximumWaitTime);
     }
 
     private final Duration _maximumWaitTime;
 
     public RetryForSpecifiedTimeStrategy(@Nonnull String maximumWaitTime) {
         this(new Duration(maximumWaitTime));
     }
 
     public RetryForSpecifiedTimeStrategy(@Nonnegative long maximumWaitTime) {
         this(new Duration(maximumWaitTime));
     }
 
     public RetryForSpecifiedTimeStrategy(@Nonnull Duration maximumWaitTime) {
         super(new IncreasingDurationRequirement("10ms"));
         _maximumWaitTime = maximumWaitTime;
     }
 
     @Override
     public boolean isRetryRequiredForException(@Nonnull Throwable e, @Nonnull RetryingStatus status) {
        return isExceptionThatForceRetry(e) && _maximumWaitTime.isGreaterThan(status.getDurationSinceStart());
     }
 
     @Override
     public void beforeTry(@Nonnull RetryingStatus status) {
         if (status.getCurrentTry() > 1) {
             final Duration durationSinceStart = status.getDurationSinceStart();
             if (_maximumWaitTime.isGreaterThan(durationSinceStart)) {
                 try {
                     final Duration targetDuration = generateDuration(getWaitBetweenEachTry());
                     final Duration leftDuration = _maximumWaitTime.minus(durationSinceStart);
                     if (targetDuration.isLessThanOrEqualTo(leftDuration)) {
                         targetDuration.sleep();
                     } else {
                         leftDuration.sleep();
                     }
                 } catch (InterruptedException ignored) {
                     currentThread().interrupt();
                 }
             }
         }
     }
 
     @Nonnull
     public Duration getMaximumWaitTime() {
         return _maximumWaitTime;
     }
 }
