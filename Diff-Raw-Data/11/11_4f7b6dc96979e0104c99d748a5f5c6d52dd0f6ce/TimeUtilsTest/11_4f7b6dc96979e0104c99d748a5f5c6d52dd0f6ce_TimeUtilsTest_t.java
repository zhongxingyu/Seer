 /*
  * #%L
  * Bitrepository Core
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.common.utils;
 
 import java.util.Date;
 import org.bitrepository.common.TestValidationUtils;
 import org.jaccept.structure.ExtendedTestCase;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 public class TimeUtilsTest extends ExtendedTestCase {
     @Test(groups = { "regressiontest" })
     public void utilityTester() throws Exception {
         addDescription("Test that the utility class is a proper utility class.");
         TestValidationUtils.validateUtilityClass(TimeUtils.class);
     }
 
     @Test(groups = {"regressiontest"})
     public void timeTester() throws Exception {
         addDescription("Tests the TimeUtils. Pi days = 271433605 milliseconds");
         addStep("Test that milliseconds can be converted into human readable seconds", 
                 "Pi days % minutes");
         long millis = 271433605;
         String millisInSec = TimeUtils.millisecondsToSeconds(millis % 60000);
         String expectedSec = "53 seconds";
         Assert.assertTrue(millisInSec.startsWith(expectedSec));
         
         addStep("Test that milliseconds can be converted into human readable minutes.", 
                 "Pi days % hours");
         String millisInMin = TimeUtils.millisecondsToMinutes(millis % 3600000);
         String expectedMin = "23 minutes";
         Assert.assertTrue(millisInMin.startsWith(expectedMin));
         
         addStep("Test that milliseconds can be converted into human readable hours.", 
                 "Pi days % days");
         String millisInHour = TimeUtils.millisecondsToHours(millis % (3600000*24));
         String expectedHours = "3 hours";
         Assert.assertTrue(millisInHour.startsWith(expectedHours));
         
         addStep("Test that milliseconds can be converted into human readable minutes.", 
                 "Pi days");
         String millisInDay = TimeUtils.millisecondsToDays(millis);
         String expectedDays = "3 days";
         Assert.assertTrue(millisInDay.startsWith(expectedDays));
         
         addStep("Test the human readable output.", "");
         String human = TimeUtils.millisecondsToHuman(millis);
         Assert.assertTrue(human.contains(expectedSec), human);
         Assert.assertTrue(human.contains(expectedMin), human);
         Assert.assertTrue(human.contains(expectedHours), human);
         Assert.assertTrue(human.contains(expectedDays), human);
     }
 
 
     @Test(groups = {"regressiontest"})
     public void shortDateTest() {
         Date date = new Date(1360054029256L);
         String shortDateString = TimeUtils.shortDate(date);
        Assert.assertEquals(shortDateString, "05.02.13 09:47");
     }
 }
