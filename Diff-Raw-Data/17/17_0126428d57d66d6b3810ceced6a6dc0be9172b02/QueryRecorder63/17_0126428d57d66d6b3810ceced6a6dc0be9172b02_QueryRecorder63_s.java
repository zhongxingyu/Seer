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
 package org.syphr.mythtv.proto.impl;
 
 import java.io.IOException;
 
 import org.syphr.mythtv.proto.SocketManager;
 import org.syphr.mythtv.proto.data.Channel;
 
 public class QueryRecorder63 extends AbstractQueryRecorder
 {
     public QueryRecorder63(int recorderId, SocketManager socketManager)
     {
         super(recorderId, socketManager);
     }
 
     @Override
     public void cancelNextRecording()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void changeBrightness()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void changeChannel()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void changeColour()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void changeContrast()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void changeHue()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void checkChannel()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void checkChannelPrefix()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void fillPositionMap()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void finishRecording()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void frontendReady()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void getBrightness()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void getChannelInfo()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void getColour()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void getContrast()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void getCurrentRecording()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void getFilePosition()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void getFramerate()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void getFramesWritten()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void getFreeInputs()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void getHue()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void getInput()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void getKeyframePos()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void getMaxBitrate()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void getNextProgramInfo()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void getRecording()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void getRecordingStatus()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public boolean isRecording() throws IOException
     {
         return new Command63QueryRecorderIsRecording(getRecorderId()).send(getSocketManager());
     }
 
     @Override
     public void pause()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void setChannel()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void setInput()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void setSignalMonitoringRate()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void shouldSwitchCard()
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public boolean spawnLiveTv(String chainId, boolean pip, Channel startChannel) throws IOException
     {
        // TODO
        throw new UnsupportedOperationException();
     }
 
     @Override
    public boolean stopLiveTv()
     {
        // TODO
        throw new UnsupportedOperationException();
     }
 
     @Override
     public void toggleChannelFavorite(String channelGroup) throws IOException
     {
         // TODO
         throw new UnsupportedOperationException();
     }
 }
