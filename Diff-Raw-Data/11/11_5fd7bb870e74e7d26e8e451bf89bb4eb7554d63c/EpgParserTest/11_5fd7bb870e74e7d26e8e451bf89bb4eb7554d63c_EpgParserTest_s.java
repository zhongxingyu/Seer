 /* $Id$
  * 
  * Copyright (c) 2005, Henrik Niehaus & Lazy Bones development team
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice, 
  *    this list of conditions and the following disclaimer in the documentation 
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the project (Lazy Bones) nor the names of its 
  *    contributors may be used to endorse or promote products derived from this 
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package org.hampelratte.svdrp.util;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.hampelratte.svdrp.responses.highlevel.EPGEntry;
 import org.hampelratte.svdrp.responses.highlevel.Stream;
 import org.junit.Test;
 
 public class EpgParserTest {
 
     private static final String epgData = 
         "C S19.2E-133-5-1793 Channel Name\n" + 
         "E 12667 1274605200 7200 50 FF\n" + 
         "T Program Title\n" + 
         "D Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua.|Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquid ex ea commodi consequat. Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum..\n" +
         "S Lorem ipsum dolor sit amet...\n" +
         "X 1 03 deu 16:9\n" + 
         "X 2 03 deu stereo\n" + 
         "X 3 01 deu \n" + 
         "X 2 03 deu ohne Audiodeskription\n" +
         "V 1274605200\n" +
         "e\nc";
     
     @Test
     public void testChannelParsing() {
         List<EPGEntry> entries = EPGParser.parse(epgData);
         EPGEntry first = entries.get(0);
         
         assertEquals("S19.2E-133-5-1793", first.getChannelID());
         assertEquals("Channel Name", first.getChannelName());
         
         String epgDataWithoutChannelName = 
             "C S19.2E-133-5-1793\n" + 
             "E 12667 1274605200 7200 50 FF\n" + 
             "T Program Title\n" + 
             "D Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquid ex ea commodi consequat. Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum..\n" + 
             "e\nc";
     
         entries = EPGParser.parse(epgDataWithoutChannelName);
         first = entries.get(0);
         
         assertEquals("S19.2E-133-5-1793", first.getChannelID());
         assertNull(first.getChannelName());
     }
     
     @Test
     public void testEntryParsing() {
         List<EPGEntry> entries = EPGParser.parse(epgData);
         EPGEntry first = entries.get(0);
 
         assertEquals(12667, first.getEventID());                    // event id
         Calendar calStartTime = GregorianCalendar.getInstance();    // start time
         calStartTime.setTimeInMillis(1274605200 * 1000L);
         assertEquals(calStartTime, first.getStartTime());
         Calendar calEndTime = (Calendar) calStartTime.clone();      // end time
         calEndTime.add(Calendar.SECOND, 7200);
         assertEquals(calEndTime, first.getEndTime());
         assertEquals("50", first.getTableID());                     // table id
         assertEquals("FF", first.getVersion());                     // version
         
         String epgDataWithoutVersion = 
             "C S19.2E-133-5-1793\n" + 
             "E 12667 1274605200 7200 50\n" + 
             "T Program Title\n" + 
             "D Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquid ex ea commodi consequat. Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum..\n" +
             "e\nc";
         
         entries = EPGParser.parse(epgDataWithoutVersion);
         first = entries.get(0);
         assertEquals("", first.getVersion());
     }
     
     @Test
     public void testTitleParsing() {
         List<EPGEntry> entries = EPGParser.parse(epgData);
         EPGEntry first = entries.get(0);
         assertEquals("Program Title", first.getTitle());
     }
     
     @Test
     public void testVpsTime() {
         List<EPGEntry> entries = EPGParser.parse(epgData);
         EPGEntry first = entries.get(0);
         Calendar vpsTime = GregorianCalendar.getInstance();
         vpsTime.setTimeInMillis(1274605200 * 1000L);
         assertEquals(vpsTime, first.getVpsTime());
     }
     
     @Test
     public void testDescription() {
         List<EPGEntry> entries = EPGParser.parse(epgData);
         EPGEntry first = entries.get(0);
         String expected = "Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua.\nUt enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquid ex ea commodi consequat. Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum..";        
         assertEquals(expected, first.getDescription());
         
     }
     
     @Test
     public void testShortDescription() {
         List<EPGEntry> entries = EPGParser.parse(epgData);
         EPGEntry first = entries.get(0);
         String expected = "Lorem ipsum dolor sit amet...";
         assertEquals(expected, first.getShortText());
     }
     
     @Test 
     public void testStreamTypes() {
         List<EPGEntry> entries = EPGParser.parse(epgData);
         EPGEntry entry = entries.get(0);
         
         assertEquals(4, entry.getStreams().size());
         assertEquals(3, entry.getAudioStreams().size());
         
         Stream video16_9 = entry.getStreams().get(0);
         assertEquals(video16_9.getContent(), Stream.CONTENT.VIDEO);
         assertEquals(video16_9.getType(), 3);
         assertEquals(video16_9.getLanguage(), "deu");
        assertEquals(video16_9.getLanguage(), "16:9");
         
         Stream audioStereo = entry.getStreams().get(1);
         assertEquals(audioStereo.getContent(), Stream.CONTENT.AUDIO);
         assertEquals(audioStereo.getType(), 3);
         assertEquals(audioStereo.getLanguage(), "deu");
        assertEquals(audioStereo.getLanguage(), "stereo");
         
         Stream audioNoDesc = entry.getStreams().get(2);
         assertEquals(audioNoDesc.getContent(), Stream.CONTENT.AUDIO);
         assertEquals(audioNoDesc.getType(), 1);
         assertEquals(audioNoDesc.getLanguage(), "deu");
        assertEquals(audioNoDesc.getLanguage(), "N/A");
         
         Stream audioWoDesc = entry.getStreams().get(3);
         assertEquals(audioWoDesc.getContent(), Stream.CONTENT.AUDIO);
         assertEquals(audioWoDesc.getType(), 3);
         assertEquals(audioWoDesc.getLanguage(), "deu");
        assertEquals(audioWoDesc.getLanguage(), "ohne Audiodeskription");
     }
 }
 
