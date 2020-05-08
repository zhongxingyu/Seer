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
     private static final Logger LOGGER = LoggerFactory.getLogger(ControlPlayChannelTest.class);
 
     private static PropertiesManager<Settings> settings;
     private static Control control;
 
     @BeforeClass
     public static void setUpBeforeClass() throws IOException
     {
         settings = Settings.createSettings();
         control = Utils.connect(settings);
 
         control.jump(FrontendLocation.LIVE_TV);
        waitSeconds(10, "start live TV");
 
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
         waitSeconds(5, "stop playing");
 
         control.jump(FrontendLocation.MAIN_MENU);
         control.exit();
     }
 
     @Test
     public void testPlayVolume() throws IOException, CommandException
     {
         /*
          * Set and check the volume twice to avoid false positives due to the test machine
          * always having the same volume.
          */
 
         control.playVolume(50);
         waitSeconds(2, "set volume to 50%");
         Assert.assertEquals(50, control.queryVolume());
 
         control.playVolume(25);
         waitSeconds(2, "set volume to 25%");
         Assert.assertEquals(25, control.queryVolume());
     }
 
    @Test(expected = ProtocolException.class)
     public void testPlayVolumeTooLow() throws IOException, CommandException
     {
         control.playVolume(-1);
         waitSeconds(2, "set volume to -1%");
     }
 
    @Test(expected = ProtocolException.class)
     public void testPlayVolumeTooHigh() throws IOException, CommandException
     {
         control.playVolume(101);
         waitSeconds(2, "set volume to 101%");
     }
 
     @Test
     public void testPlayChannelUp() throws IOException, CommandException
     {
         control.playChannelUp();
        waitSeconds(10, "channel up");
     }
 
     @Test
     public void testPlayChannelDown() throws IOException, CommandException
     {
         control.playChannelDown();
        waitSeconds(10, "channel down");
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
 
        waitSeconds(10, "change channels");
     }
 
     @Test
     public void testPlaySavePreview()
     {
         // TODO
     }
 
     @Test
     public void testPlaySavePreviewFilename()
     {
         // TODO
     }
 
     @Test
     public void testPlaySavePreviewFilenameDimensions()
     {
         // TODO
     }
 
     @Test
     public void testPlaySeekNamed() throws IOException, CommandException
     {
         for (SeekTarget target : SeekTarget.values())
         {
             control.playSeek(target);
            waitSeconds(10, "seek to " + target);
         }
     }
 
     @Test
     public void testPlaySeekTime() throws IOException, CommandException
     {
         control.playSeek(0, 0, 10);
        waitSeconds(10, "seek to 10 seconds past the start");
     }
 
     @Test
     public void testPlaySpeedPause()
     {
         // TODO
     }
 
     private static void waitSeconds(int seconds, String message)
     {
         try
         {
             Thread.sleep(seconds * 1000);
         }
         catch (InterruptedException e)
         {
             LOGGER.warn("Interrupted while waiting for frontend to " + message, e);
         }
     }
 }
