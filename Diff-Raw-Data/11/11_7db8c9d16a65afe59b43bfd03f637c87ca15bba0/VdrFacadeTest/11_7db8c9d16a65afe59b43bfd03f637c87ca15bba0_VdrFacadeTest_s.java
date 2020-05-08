 /*
  * Copyright (c) Henrik Niehaus
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
 package org.hampelratte.svdrp;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.text.ParseException;
 import java.util.List;
 
 import org.hampelratte.svdrp.mock.Server;
 import org.hampelratte.svdrp.responses.highlevel.Channel;
 import org.hampelratte.svdrp.responses.highlevel.Recording;
 import org.hampelratte.svdrp.responses.highlevel.Timer;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 public class VdrFacadeTest {
 
     private static Server server;
 
     private static VDR vdr;
 
     @BeforeClass
     public static void startMockServer() throws IOException, InterruptedException {
         server = new Server();
         server.loadWelcome("welcome-1.6.0_2-utf_8.txt");
         server.loadRecordings("lstr.txt");
         server.loadTimers("lstt.txt");
         new Thread(server).start();
 
         // wait for the server
         Thread.sleep(1000);
 
         vdr = new VDR("localhost", 2001, 5000);
     }
 
 
     @Test
     public void testGetTimers() throws UnknownHostException, IOException {
         List<Timer> timers = vdr.getTimers();
         assertEquals(4, timers.size());
 
         server.loadTimers("lstt_no_timers.txt");
         timers = vdr.getTimers();
         assertEquals(0, timers.size());
     }
 
     @Test
     public void testGetTimer() throws UnknownHostException, IOException {
         server.loadTimers("lstt.txt");
         Timer timer = vdr.getTimer(1);
         assertEquals("Chronik einer Entmietüng", timer.getTitle());
     }
 
     @Test(expected = RuntimeException.class)
     public void testGetTimerException() throws UnknownHostException, IOException {
         server.loadTimers("lstt_no_timers.txt");
         vdr.getTimer(1);
     }
 
     // @Test
     // public void testModifyTimer() throws UnknownHostException, IOException {
     // server.loadTimers("lstt.txt");
     //
     // Timer timer = vdr.getTimer(1);
     // timer.setTitle("ABCDEF");
     // Response response = vdr.modifyTimer(timer.getID(), timer);
     // assertEquals(250, response.getCode());
     //
     // timer = vdr.getTimer(1);
     // assertEquals("ABCDEF", timer.getTitle());
     // }
 
     @Test
     public void testDeleteTimer() throws UnknownHostException, IOException {
         server.loadTimers("lstt.txt");
         Response response = vdr.deleteTimer(1);
         assertEquals(250, response.getCode());
 
         List<Timer> timers = vdr.getTimers();
         assertEquals(3, timers.size());
 
         response = vdr.deleteTimer(1);
        assertEquals(501, response.getCode());
 
         response = vdr.deleteTimer(2);
         assertEquals(250, response.getCode());
     }
 
     @Test
     public void testGetRecordings() throws UnknownHostException, IOException {
         List<Recording> recordings = vdr.getRecordings();
         assertEquals(7, recordings.size());
 
         server.loadRecordings("lstr_no_recordings.txt");
         recordings = vdr.getRecordings();
         assertEquals(0, recordings.size());
     }
 
     @Test
     public void testGetRecordingDetails() throws UnknownHostException, IOException, ParseException {
         server.loadRecordings("lstr.txt");
         List<Recording> recordings = vdr.getRecordings();
 
         Recording rec = vdr.getRecordingDetails(recordings.get(0));
         assertEquals("Tagesschau", rec.getTitle());
         assertEquals(
                 "* Bundeskanzlerin Merkel zur Zukunft der schwarz-gelben Koalition * Gewerkschaften fordern höhere Gehälter * Sport: Bundesliga, Basketball-WM und Formel 1",
                 rec.getDescription());
         assertEquals(0, rec.getPriority());
         assertEquals(0, rec.getLifetime());
     }
 
     @Test(expected = RuntimeException.class)
     public void testGetRecordingDetailsException() throws UnknownHostException, IOException, ParseException {
         Recording fake = new Recording();
         fake.setNumber(123456);
         vdr.getRecordingDetails(fake);
     }
 
     // @Test
     // public void testGetEpg() throws UnknownHostException, IOException {
     // List<EPGEntry> epg = vdr.getEpg();
     // assertEquals(10, epg.size());
     // }
 
     @Test
     public void testGetChannels() throws UnknownHostException, IOException, ParseException {
         server.loadChannelsConf("channels-mixed.conf");
         List<Channel> channels = vdr.getChannels();
         assertEquals(57, channels.size());
     }
 
     @Test(expected = RuntimeException.class)
     public void testGetChannelsException() throws UnknownHostException, IOException, ParseException {
         server.loadChannelsConf("channels-empty.conf");
         vdr.getChannels();
     }
 
     @AfterClass
     public static void shutdownServer() throws IOException, InterruptedException {
         server.shutdown();
     }
 }
 
