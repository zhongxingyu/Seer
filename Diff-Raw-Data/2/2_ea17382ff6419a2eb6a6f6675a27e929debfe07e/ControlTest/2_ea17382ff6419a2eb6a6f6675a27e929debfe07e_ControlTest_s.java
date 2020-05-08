 /*
  * Copyright 2011 Gregory P. Moyer
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
 package org.syphr.mythtv.control;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.syphr.mythtv.control.test.Utils;
 import org.syphr.mythtv.data.Channel;
 import org.syphr.mythtv.data.Program;
 import org.syphr.mythtv.test.Settings;
 import org.syphr.mythtv.types.FrontendLocation;
 import org.syphr.mythtv.types.Key;
 import org.syphr.mythtv.types.SeekTarget;
 import org.syphr.mythtv.types.Verbose;
 import org.syphr.mythtv.util.exception.CommandException;
 import org.syphr.prom.PropertiesManager;
 
 public class ControlTest
 {
     private static final Logger LOGGER = LoggerFactory.getLogger(ControlTest.class);
 
     private static PropertiesManager<Settings> settings;
     private static Control control;
 
     @BeforeClass
     public static void setUpBeforeClass() throws IOException
     {
         settings = Settings.createSettings();
         control = Utils.connect(settings);
 
         /*
          * Make sure the tests start at the main menu.
          */
         control.jump(FrontendLocation.MAIN_MENU);
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws IOException
     {
         control.exit();
     }
 
     @Test
     public void testJump() throws IOException
     {
         control.jump(FrontendLocation.MYTH_VIDEO);
         control.jump(FrontendLocation.MAIN_MENU);
     }
 
     @Test
     public void testKey() throws IOException
     {
         control.key('m');
         control.key(Key.ESCAPE);
     }
 
     @Test(expected = CommandException.class)
     public void testPlayVolumeNotAllowed() throws IOException, CommandException
     {
         control.playVolume(10);
     }
 
     @Test(expected = CommandException.class)
     public void testPlayChannelUpNotAllowed() throws IOException, CommandException
     {
         control.playChannelUp();
     }
 
     @Test(expected = CommandException.class)
     public void testPlayChannelDownNotAllowed() throws IOException, CommandException
     {
         control.playChannelDown();
     }
 
     @Test(expected = CommandException.class)
     public void testPlayChannelNumberNotAllowed() throws IOException, CommandException
     {
         control.playChannel("10000000");
     }
 
     @Test(expected = CommandException.class)
     public void testPlayChannelIdNotAllowed() throws IOException, CommandException
     {
         control.playChannel(Integer.MAX_VALUE);
     }
 
     /*
      * This test is commented out because there is no way to retrieve a valid
      * myth:// URI or local filename through the frontend control socket.
      */
 //    @Test
 //    public void testPlayFile() throws IOException, CommandException
 //    {
 //        control.playFile("/tmp/test-file.mpg");
 //        Utils.waitSeconds(10, "play file");
 //    }
 
     @Test
     public void testPlayProgram() throws IOException, CommandException
     {
         List<Program> recordings = control.queryRecordings();
         if (recordings.isEmpty())
         {
             LOGGER.warn("Skipping play program test because no recordings were found");
             return;
         }
 
         Program recording = recordings.get(0);
         control.playProgram(recording.getChannel().getId(), recording.getRecStartTs(), false);
         Utils.waitSeconds(10, "play program");
 
         control.jump(FrontendLocation.MAIN_MENU);
     }
 
     @Test(expected = CommandException.class)
     public void testPlayProgramNotFound() throws IOException, CommandException
     {
         control.playProgram(Integer.MAX_VALUE, new Date(0), false);
     }
 
     @Test(expected = CommandException.class)
     public void testPlaySavePreviewNotAllowed() throws IOException, CommandException
     {
         control.playSavePreview();
     }
 
     @Test(expected = CommandException.class)
     public void testPlaySavePreviewFilenameNotAllowed() throws IOException, CommandException
     {
         control.playSavePreview("/tmp/preview-not-allowed-outside-playback.png");
     }
 
     @Test(expected = CommandException.class)
     public void testPlaySavePreviewFilenameSizeNotAllowed() throws IOException, CommandException
     {
         control.playSavePreview("/tmp/preview-not-allowed-outside-playback.png", 1280, 720);
     }
 
     @Test(expected = CommandException.class)
     public void testPlaySeekTargetNotAllowed() throws IOException, CommandException
     {
         control.playSeek(SeekTarget.BEGINNING);
     }
 
     @Test(expected = CommandException.class)
     public void testPlaySeekTimeNotAllowed() throws IOException, CommandException
     {
         control.playSeek(0, 0, 0);
     }
 
     @Test(expected = CommandException.class)
     public void testPlaySpeedNotAllowed() throws IOException, CommandException
     {
         control.playSpeed(0);
     }
 
     @Test(expected = CommandException.class)
     public void testPlayStopNotAllowed() throws IOException, CommandException
     {
         control.playStop();
     }
 
     @Test
     public void testPlayMusicPlay() throws IOException
     {
         // TODO
         LOGGER.warn("play music play test not implemented");
     }
 
     @Test
     public void testPlayMusicPause() throws IOException
     {
         // TODO
         LOGGER.warn("play music pause test not implemented");
     }
 
     @Test
     public void testPlayMusicStop() throws IOException
     {
         // TODO
         LOGGER.warn("play music stop test not implemented");
     }
 
     @Test
    public void testPlayMusicSetVolume(int percent) throws IOException
     {
         // TODO
         LOGGER.warn("play music setvolume test not implemented");
     }
 
     @Test
     public void testPlayMusicGetVolume() throws IOException
     {
         // TODO
         LOGGER.warn("play music getvolume test not implemented");
     }
 
     @Test
     public void testPlayMusicGetMeta() throws IOException
     {
         // TODO
         LOGGER.warn("play music getmeta test not implemented");
     }
 
     @Test
     public void testPlayMusicFile(String filename) throws IOException
     {
         // TODO
         LOGGER.warn("play music file test not implemented");
     }
 
     @Test
     public void testPlayMusicTrack(int track) throws IOException
     {
         // TODO
         LOGGER.warn("play music track test not implemented");
     }
 
     @Test
     public void testPlayMusicUrl(URL url) throws IOException
     {
         // TODO
         LOGGER.warn("play music url test not implemented");
     }
 
     @Test
     public void testQueryLocation() throws IOException
     {
         control.jump(FrontendLocation.MAIN_MENU);
         Assert.assertEquals(FrontendLocation.MAIN_MENU, control.queryLocation());
     }
 
     @Test
     public void testQueryPlaybackInfoNotPlaying() throws IOException
     {
         Assert.assertNull(control.queryPlaybackInfo());
     }
 
     @Test
     public void testQueryVolume() throws IOException
     {
         LOGGER.debug("Current volume: {}", control.queryVolume());
     }
 
     @Test
     public void testQueryRecordings() throws IOException
     {
         org.syphr.mythtv.test.Utils.printFirstFive(control.queryRecordings(),
                                                    LOGGER);
     }
 
     @Test
     public void testQueryRecording() throws IOException
     {
         List<Program> recordings = control.queryRecordings();
 
         if (recordings.isEmpty())
         {
             LOGGER.warn("Skipping query recording test since there are no recordings");
             return;
         }
 
         Program request = recordings.get(recordings.size() - 1);
         Program response = control.queryRecording(request.getChannel().getId(), request.getRecStartTs());
 
         Assert.assertEquals(request, response);
     }
 
     @Test
     public void testQueryRecordingDoesNotExist() throws IOException
     {
         Program program = control.queryRecording(Integer.MAX_VALUE, new Date(0));
         Assert.assertNull(program);
     }
 
     @Test
     public void testQueryLiveTv() throws IOException
     {
         org.syphr.mythtv.test.Utils.printFirstFive(control.queryLiveTv(),
                                                    LOGGER);
     }
 
     @Test
     public void testQueryLiveTvChannel() throws IOException
     {
         List<Program> livetv = control.queryLiveTv();
 
         if (livetv.isEmpty())
         {
             LOGGER.warn("Skipping query livetv test since there are no listings");
             return;
         }
 
         Program request = livetv.get(livetv.size() - 1);
         Program response = control.queryLiveTv(request.getChannel().getId());
 
         Assert.assertEquals(request, response);
     }
 
     @Test
     public void testQueryLiveTvChannelDoesNotExist() throws IOException
     {
         Program program = control.queryLiveTv(Integer.MAX_VALUE);
         Assert.assertNull(program);
     }
 
     @Test
     public void testQueryLoad() throws IOException
     {
         LOGGER.debug(control.queryLoad().toString());
     }
 
     @Test
     public void testQueryMemStats() throws IOException
     {
         LOGGER.debug(control.queryMemStats().toString());
     }
 
     @Test
     public void testQueryTime() throws IOException
     {
         LOGGER.debug("Frontend time: {}", control.queryTime());
     }
 
     @Test
     public void testQueryUptime() throws IOException
     {
         LOGGER.debug("Uptime: {} secs", control.queryUptime());
     }
 
     @Test
     public void testQueryVerbose() throws IOException
     {
         LOGGER.debug("Verbose options: {}", control.queryVerbose());
     }
 
     @Test
     public void testQueryVersion() throws IOException
     {
         LOGGER.debug(control.queryVersion().toString());
     }
 
     @Test
     public void testQueryChannels() throws IOException
     {
         List<Channel> allChannels = control.queryChannels();
 
         if (allChannels.isEmpty())
         {
             /*
              * Can't test any further if there are no channels.
              */
             return;
         }
 
         int thirdListSize = allChannels.size() / 3;
         List<Channel> middleThirdChannels = control.queryChannels(thirdListSize, thirdListSize);
 
         Assert.assertTrue(allChannels.containsAll(middleThirdChannels));
     }
 
     @Test
     @SuppressWarnings("serial")
     public void testSetVerbose() throws IOException, CommandException
     {
         control.setVerbose(new ArrayList<Verbose>() {{ add(Verbose.ALL); }});
         control.setVerbose(new ArrayList<Verbose>() {{ add(Verbose.IMPORTANT); add(Verbose.GENERAL); }});
     }
 
     @Test
     public void testScreenshotDefaults() throws IOException, CommandException
     {
         control.screenshot();
     }
 
     @Test
     public void testMessage() throws IOException
     {
         try
         {
             control.message("This is a test message!");
             control.key(Key.ENTER);
         }
         catch (UnsupportedOperationException e)
         {
             LOGGER.warn("Skipping message test since it is not supported by this control version");
             return;
         }
     }
 }
