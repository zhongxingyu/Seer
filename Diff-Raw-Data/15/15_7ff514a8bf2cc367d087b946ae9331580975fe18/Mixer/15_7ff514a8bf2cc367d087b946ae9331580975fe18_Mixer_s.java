 // ========================================================================
 // Copyright 2008-2009 NEXCOM Systems
 // ------------------------------------------------------------------------
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at 
 // http://www.apache.org/licenses/LICENSE-2.0
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 // ========================================================================
 
 package org.cipango.media;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.mortbay.log.Log;
 
 /**
  * Mix mono channel signed 16 bits big endian streams to one stream.
  * 
  * @author yohann
  *
  */
 public class Mixer implements Runnable {
 
     private List<InputStream> _inputStreams;
     private OutputStream _outputStream;
     private boolean _running;
 
     public Mixer(OutputStream outputStream)
     {
         _outputStream = outputStream;
         _inputStreams = new ArrayList<InputStream>();
         _running = true;
     }
 
     @Override
     public void run()
     {
         try
         {
             while (_running)
             {
                 float sum = 0;
                 int numberOfStreams = _inputStreams.size();
                 if (numberOfStreams < 1)
                     break;
                 InputStream streamTerminated = null;
                 for (InputStream inputStream: _inputStreams)
                 {
                     int firstByte, secondByte;
                     if ((firstByte = inputStream.read()) < 0)
                     {
                         // FIXME if two streams terminate at the same time
                         streamTerminated = inputStream;
                         break;
                     }
                     if ((secondByte = inputStream.read()) < 0)
                     {
                         streamTerminated = inputStream;
                         break;
                     }
                     // little-endian input
                    sum += (secondByte << 8) | firstByte;
                 }
                 if (streamTerminated != null)
                     _inputStreams.remove(streamTerminated);
                 int roundedSum = Math.round(sum);
                 // little-endian output
                 _outputStream.write(roundedSum);
                 _outputStream.write(roundedSum >> 8);
             }
             _outputStream.close();
             _running = false;
         }
         catch (IOException e)
         {
             Log.warn("IOException", e);
         }
     }
 
     public void stop()
     {
         _running = false;
     }
 
     public boolean isRunning()
     {
         return _running;
     }
 
     public void addInputStream(InputStream inputStream)
     {
         _inputStreams.add(inputStream);
     }
 
     public void removeInputStream(InputStream inputStream)
     {
         _inputStreams.remove(inputStream);
     }
 
 }
