 /**
  * 
  *    This module represents an engine for the load testing framework
  *    Copyright (C) 2008  Imran M Yousuf (imran@smartitengineering.com)
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.smartitengineering.loadtest.engine.management;
 
import com.smartitengineering.loadtest.engine.TestCase;
 
 /**
  * This API Interface represents an thread kill policy maker. It basically is a
  * helper for LoadTestEngine to decide whether to stop a thread based on a
  * particular rational provided by the client.<p/>
  * This API should be used in sequential and iterative manner. That is thread
  * monitor API should first call getNextCheckDuration to know when to check, if
  * it returns -1 then do not proceed else wait for the milliseconds specified
  * and then invoke shouldTestCaseBeStopped. This iteration should continue till
  * next interval is -1.
  *
  * @author imyousuf
  */
 public interface TestCaseThreadPolicy {
 
     /**
      * Check the thread and the test case for deciding whether the thread should
      * be stopped or not. Policy may take into account whether the test case is
      * stoppable or not and return flag based on that.
      * 
      * @param testCaseThread Thread executing the test case
      * @param testCase The test case being executed by the thread
      * @return true if it should be stopped by the client or else false
      */
     public boolean shouldTestCaseBeStopped(Thread testCaseThread,
                                            TestCase testCase);
 
     /**
      * If the client wants to know after how long more to check the thread then
      * this method can be used to find out a sleep duration for the thread-stop
      * monitor. It is to be noted that it should always return the same duration
      * between each shoutTestCaseBeStopped checks, e.g. if this operation is
      * called 100 times between the 1st and second invokation of the check op
      * then it should return same integer number.
      * @param testCase The test case being executed by the thread
      * @param testCaseThread Thread executing the test case
      * @return Duration in milisecond, -1 if no further check is required
      */
     public int getNextCheckDuration(Thread testCaseThread,
                                     TestCase testCase);
 }
