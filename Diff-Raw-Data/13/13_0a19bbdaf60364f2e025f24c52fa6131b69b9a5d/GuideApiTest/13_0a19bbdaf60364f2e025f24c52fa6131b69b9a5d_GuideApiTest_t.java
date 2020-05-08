 /**
  *  This file is part of MythTV for Android
  *
  *  MythTV for Android is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  MythTV for Android is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with MythTV for Android.  If not, see <http://www.gnu.org/licenses/>.
  *
  * This software can be found at <https://github.com/MythTV-Android/MythTV-Service-API/>
  *
  */
 package org.mythtv.services.api.test.v027;
 
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.junit.Test;
 import org.mythtv.services.api.ETagInfo;
 import org.mythtv.services.api.v027.GuideOperations;
 import org.mythtv.services.api.v027.beans.Program;
 import org.mythtv.services.api.v027.beans.ProgramGuide;
 import org.springframework.http.ResponseEntity;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author Sebastien Astie
  */
 public class GuideApiTest extends BaseApiTester {
     private GuideOperations operations;
 
     private DateTime now;
     private DateTime tomorrow;
     private int iconsize;
     private int chanid;
 
     public void setUp() throws Exception {
         super.setUp();
         now = new DateTime();
         tomorrow = now.plus( Period.days(1) );
         iconsize = Integer.parseInt( properties.getProperty(
                "MythServicesServiceTemplate.GuideOperationsTest.IconSize", "512" ) );
        chanid = Integer.parseInt( properties.getProperty( "MythServicesServiceTemplate.GuideOperationsTest.ChannelId",
                 "2502" ) );
     }
 
     @Override
     protected void prepareOperations() {
         operations = mythservices.guideOperations();
     }
 
     @Test
     public void testGetChannelIcon() throws Exception {
         ResponseEntity<String> channelIcon = operations.getChannelIcon(chanid, iconsize, null, ETagInfo.createEmptyETag());
         assertNotNull(channelIcon);
         assertNotNull(channelIcon.getBody());
     }
 
     @Test
     public void testGetProgramDetails() throws Exception {
         ResponseEntity<ProgramGuide> programGuide = operations.getProgramGuide(now, tomorrow, chanid, 1, true, ETagInfo.createEmptyETag());
         assertNotNull(programGuide);
         ProgramGuide res = programGuide.getBody();
         assertNotNull(res);
         assertNotNull(res.getChannels());
         assertTrue(res.getChannels().length > 0);
         assertNotNull(res.getChannels()[0].getPrograms());
         assertNotNull(res.getChannels()[0].getPrograms());
         assertTrue(res.getChannels()[0].getPrograms().length > 0);
         DateTime startTime = res.getChannels()[0].getPrograms()[0].getStartTime();
         ResponseEntity<Program> programDetails = operations.getProgramDetails(chanid, startTime, ETagInfo.createEmptyETag());
         assertNotNull(programDetails);
         Program p = programDetails.getBody();
         assertNotNull(p);
     }
 
     @Test
     public void testGetProgramGuide() throws Exception {
         ResponseEntity<ProgramGuide> programGuide = operations.getProgramGuide(now, tomorrow, chanid, 1, true, ETagInfo.createEmptyETag());
         assertNotNull(programGuide);
         ProgramGuide res = programGuide.getBody();
         assertNotNull(res);
         assertNotNull(res.getChannels());
         assertTrue(res.getChannels().length > 0);
         assertNotNull(res.getChannels()[0].getPrograms());
         assertNotNull(res.getChannels()[0].getPrograms());
         assertTrue(res.getChannels()[0].getPrograms().length > 0);
     }
 }
