 /*
  * Copyright 2012 Mitchell Just <mitch.just@gmail.com>.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.unicornlabs.kabouter.historian;
 
 import com.unicornlabs.kabouter.historian.data_objects.Device;
 import com.unicornlabs.kabouter.historian.data_objects.Powerlog;
 import com.unicornlabs.kabouter.historian.data_objects.PowerlogId;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Random;
 import junit.framework.TestCase;
 
 /**
  *
  * @author Mitchell Just <mitch.just@gmail.com>
  */
 public class HistorianTest extends TestCase {
     
     public HistorianTest(String testName) {
         super(testName);
     }
     
     @Override
     protected void setUp() throws Exception {
         super.setUp();
     }
     
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     /**
      * Check all device related methods
      */
     public void testDeviceMethods() {
         Historian myHistorian = new Historian();
         
         String testId = "JUnit_Test_Dev";
         String testDisplayName = "JUnit Test Device";
         Boolean testPower = Boolean.TRUE;
         String testIp = "255.255.255.255";
         int testNumIo = 2;
         
         ArrayList<String> testIoNames = new ArrayList<String>();
         testIoNames.add("TestIO1");
         testIoNames.add("TestIO2");
         
         String testType = "Test_Device";
         
         Device newDevice = new Device();
         newDevice.setId(testId);
         newDevice.setDisplayname(testDisplayName);
         newDevice.setHaspowerlogging(testPower);
         newDevice.setIpaddress(testIp);
         newDevice.setNumio(testNumIo);
         newDevice.setIonames(testIoNames);
         newDevice.setType(testType);
         
         //Check that nothing is returned when asking for device with this id
         Device test = myHistorian.getDevice(newDevice.getId());
         assertNull(test);
         
         //Test Saving to database
         myHistorian.saveDevice(newDevice);
         Device test2 = myHistorian.getDevice(testId);
         assertNotNull(test2);
         
         assertEquals(test2.getId(), testId);
         assertEquals(test2.getDisplayname(), testDisplayName);
         assertEquals(test2.getHaspowerlogging(), testPower);
         assertEquals(test2.getIpaddress(), testIp);
         assertEquals((int)test2.getNumio(), (int)testNumIo);
         assertEquals(test2.getIonames().size(), testIoNames.size());
 
         for(int i=0;i<testIoNames.size();i++) {
             assertEquals(test2.getIonames().get(i), testIoNames.get(i));
         }
         
         assertEquals(test2.getType(), testType);
         
         //Test updating in database
         String testUpdatedDisplayName = testDisplayName+"_UPDATED";
         
         test2.setDisplayname(testUpdatedDisplayName);
         
         myHistorian.updateDevice(test2);
         
         Device test3 = myHistorian.getDevice(testId);
         
         assertNotNull(test3);
         
         assertEquals(test3.getId(), testId);
         assertEquals(test3.getDisplayname(), testUpdatedDisplayName);
         assertEquals(test3.getHaspowerlogging(), testPower);
         assertEquals(test3.getIpaddress(), testIp);
         assertEquals((int)test3.getNumio(), (int)testNumIo);
         assertEquals(test3.getIonames().size(), testIoNames.size());
 
         for(int i=0;i<testIoNames.size();i++) {
             assertEquals(test3.getIonames().get(i), testIoNames.get(i));
         }
         
         assertEquals(test3.getType(), testType);
         
         //Test deleting from database
         myHistorian.deleteDevice(test3);
         
         Device test4 = myHistorian.getDevice(testId);
         
         assertNull(test4);
         
         myHistorian.shutDown();
     }
     
     public void testPowerLogMethods() {
         Historian myHistorian = new Historian();
         
         int numTests = 10;
         Date currentDate = new Date();
         
         String testId = "JUnit_Test_Dev";
         double[] testValues = new double[numTests];
         Powerlog[] testLogs = new Powerlog[numTests];
         Random r = new Random();
         
         myHistorian.deletePowerLogs(testId);
         
         for(int i=0;i<numTests;i++) {
             testValues[i] = r.nextDouble();
             testLogs[i] = new Powerlog(new PowerlogId(new Date(currentDate.getDate() + i*10000), testId),testValues[i]);
             myHistorian.savePowerlog(testLogs[i]);
         }
     
         //Test getting all logs
         ArrayList<Powerlog> test1 = myHistorian.getPowerlogs(testId);
         
         assertEquals(test1.size(), numTests);
         
         for(int i=0;i<numTests;i++) {
             Powerlog get = test1.get(i);
             assertEquals(testLogs[i].getId(), get.getId());
             assertEquals(testLogs[i].getPower(), testValues[i]);
         }
         
         //Test getting a range of logs
         
         Date from = testLogs[1].getId().getLogtime();
         Date to = testLogs[testLogs.length-2].getId().getLogtime();
         
         ArrayList<Powerlog> test2 = myHistorian.getPowerlogs(testId,from,to);
         
         for(int i=0;i<test2.size();i++) {
             Powerlog get = test2.get(i);
             Date testDate = get.getId().getLogtime();
             assertTrue(testDate.compareTo(from) >= 0);
             assertTrue(testDate.compareTo(to) <= 0);
         }
 
         //Test Deleting logs
         myHistorian.deletePowerLogs(testId);
         ArrayList<Powerlog> test3 = myHistorian.getPowerlogs(testId);
         
        assertNull(test3);
     }
 }
