 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.support;
 
 import java.util.concurrent.TimeUnit;
 
 import com.google.common.util.concurrent.Uninterruptibles;
 
 public class Poller {
 
  private long[] waitTimes = new long[] { 0, 50, 100, 100, 100, 500, 1000, 1000 };
 
   public void check(Probe probe) {
     int tries = 0;
     while (!probe.isSatisfied() && tries < waitTimes.length) {
       probe.sample();
       Uninterruptibles.sleepUninterruptibly(waitTimes[tries], TimeUnit.MILLISECONDS);
       tries++;
     }
 
     if (!probe.isSatisfied()) {
       throw new AssertionError(probe.message());
     }
   }
 
   public static void assertEventually(Probe probe) throws InterruptedException {
     new Poller().check(probe);
   }
 }
