 /**
  *    This module represents an engine IMPL for the load testing framework
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
 package com.smartitengineering.loadtest.engine.impl;
 
 import java.util.Map;
 import java.util.Properties;
 
 /**
  *
  * @author imyousuf
  */
 public class DummyTestCase
     extends AbstractTestCase {
 
     private int sleepTime = 10;
     
     public static final String SLEEP_TIME_PROP = "com.smartitengineering.loadtest.dummyTestCase.sleep";
 
     public DummyTestCase() {
     }
 
     public DummyTestCase(int sleepTime) {
         this.sleepTime = sleepTime;
     }
     
     public DummyTestCase(Properties properties) {
        if(properties != null && properties.contains(SLEEP_TIME_PROP)) {
             String time = properties.getProperty(SLEEP_TIME_PROP);
             try {
                 sleepTime = Integer.parseInt(time);
             }
             catch(Exception ex) {
                 sleepTime = 10;
             }
         }
     }
 
     @Override
     protected void extendRun()
         throws Exception {
         Thread.sleep(sleepTime);
     }
 
     public <InitParam> void initTestCase(InitParam... params) {
         setState(State.INITIALIZED);
     }
 
     @Override
     protected void doInitializingTasksBeforeCreatedState() {
     }
 
     @Override
     public Map<String, String> getExtraInfo() {
         return null;
     }
 }
