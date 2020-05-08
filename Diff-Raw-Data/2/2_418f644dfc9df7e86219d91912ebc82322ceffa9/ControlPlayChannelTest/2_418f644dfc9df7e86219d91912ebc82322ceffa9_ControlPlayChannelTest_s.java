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
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.syphr.mythtv.control.test.Utils;
 import org.syphr.mythtv.data.Channel;
 import org.syphr.mythtv.data.PlaybackInfo;
 import org.syphr.mythtv.test.Settings;
 import org.syphr.mythtv.types.FrontendLocation;
 import org.syphr.mythtv.types.SeekTarget;
 import org.syphr.mythtv.util.exception.CommandException;
 import org.syphr.mythtv.util.exception.ProtocolException;
 import org.syphr.prom.PropertiesManager;
 
 public class ControlPlayChannelTest
 {
     public static final Logger LOGGER = LoggerFactory.getLogger(ControlPlayChannelTest.class);
 
     private static PropertiesManager<Settings> settings;
     private static Control control;
 
     @BeforeClass
     public static void setUpBeforeClass() throws IOException
     {
         settings = Settings.createSettings();
         control = Utils.connect(settings);
 
         control.jump(FrontendLocation.LIVE_TV);
         Utils.waitSeconds(10, "start live TV");
 
         PlaybackInfo pbInfo = control.queryPlaybackInfo();
         if (pbInfo == null)
         {
             Assert.fail("Frontend failed to start playing live TV");
             return;
         }
 
         LOGGER.debug(pbInfo.toString());
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws IOException, CommandException
     {
         control.playStop();
        Utils.waitSeconds(5, "stop playing");
 
         control.jump(FrontendLocation.MAIN_MENU);
         control.exit();
     }
 
     @Test
     public void testPlayVolume() throws IOException, CommandException
     {
         control.playVolume(50);
         Utils.waitSeconds(2, "set volume to 50%");
     }
 
     @Test(expected = ProtocolException.class)
     public void testPlayVolumeTooLow() throws IOException, CommandException
     {
         control.playVolume(-1);
         Utils.waitSeconds(2, "set volume to -1%");
     }
 
     @Test(expected = ProtocolException.class)
     public void testPlayVolumeTooHigh() throws IOException, CommandException
     {
         control.playVolume(101);
         Utils.waitSeconds(2, "set volume to 101%");
     }
 
     @Test
     public void testPlayChannelUp() throws IOException, CommandException
     {
         control.playChannelUp();
         Utils.waitSeconds(10, "channel up");
     }
 
     @Test
     public void testPlayChannelDown() throws IOException, CommandException
     {
         control.playChannelDown();
         Utils.waitSeconds(10, "channel down");
     }
 
     /*
      * This test is commented out because there is no way to retrieve a valid channel
      * number through the frontend control socket.
      */
 //    @Test
 //    public void testPlayChannel() throws IOException, CommandException
 //    {
 //        control.playChannel("1001");
 //        waitFiveSeconds("change channels (by number)");
 //    }
 
     @Test
     public void testPlayChannelId() throws IOException, CommandException
     {
         List<Channel> channels = control.queryChannels();
         if (channels.isEmpty())
         {
             LOGGER.warn("Skipping play channel ID test because no channels were found");
             return;
         }
 
         Channel channel = channels.get(0);
         control.playChannel(channel.getId());
 
         Utils.waitSeconds(10, "change channels");
     }
 
     @Test
     public void testPlaySavePreview() throws IOException, CommandException
     {
         control.playSavePreview();
     }
 
     @Test
     public void testPlaySavePreviewFilename() throws IOException, CommandException
     {
         control.playSavePreview("/tmp/preview-test-default-size.png");
     }
 
     @Test
     public void testPlaySavePreviewFilenameSize() throws IOException, CommandException
     {
         control.playSavePreview("/tmp/preview-test-1280x720.png", 1280, 720);
     }
 
     @Test
     public void testPlaySeekNamed() throws IOException, CommandException
     {
         for (SeekTarget target : SeekTarget.values())
         {
             control.playSeek(target);
             Utils.waitSeconds(10, "seek to " + target);
         }
     }
 
     @Test
     public void testPlaySeekTime() throws IOException, CommandException
     {
         control.playSeek(0, 0, 10);
         Utils.waitSeconds(10, "seek to 10 seconds past the start");
     }
 
     @Test
     public void testPlaySpeedPause() throws IOException, CommandException
     {
         control.playSpeed(0);
         Utils.waitSeconds(10, "pause");
     }
 
     @Test
     public void testPlaySpeedFast() throws IOException, CommandException
     {
         control.playSpeed(1.5f);
         Utils.waitSeconds(10, "play at 1.5x speed");
     }
 
     @Test
     public void testPlaySpeedBack() throws IOException, CommandException
     {
         control.playSpeed(-0.5f);
         Utils.waitSeconds(10, "play at -0.5x speed");
     }
 
     @Test
     public void testPlaySpeedNormal() throws IOException, CommandException
     {
         control.playSpeed(1.0f);
         Utils.waitSeconds(10, "play at normal speed");
     }
 }
