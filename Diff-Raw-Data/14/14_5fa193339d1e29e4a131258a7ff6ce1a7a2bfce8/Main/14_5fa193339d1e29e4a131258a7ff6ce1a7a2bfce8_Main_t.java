 /*
  * Copyright 2011-2012 Gregory P. Moyer
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
 package org.syphr.mythtv.apps.informant;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.URI;
 import java.net.URL;
 import java.util.Date;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.syphr.mythtv.api.backend.AutomaticProtocol;
 import org.syphr.mythtv.data.Channel;
 import org.syphr.mythtv.data.Program;
 import org.syphr.mythtv.data.TunerStatus;
 import org.syphr.mythtv.data.VideoListChange;
 import org.syphr.mythtv.protocol.ConnectionType;
 import org.syphr.mythtv.protocol.EventLevel;
 import org.syphr.mythtv.protocol.InvalidProtocolVersionException;
 import org.syphr.mythtv.protocol.events.BackendEventListener;
 import org.syphr.mythtv.protocol.events.BackendEventListener63;
 import org.syphr.mythtv.protocol.events.BackendEventListener68;
 import org.syphr.mythtv.protocol.events.SystemEvent;
 import org.syphr.mythtv.protocol.events.SystemEventData;
 
 public class Main
 {
     public static void main(String[] args)
     {
         CommandLineParser parser = new PosixParser();
         CommandLine cl;
         try
         {
             cl = parser.parse(InformantOption.getOptions(), args);
 
             if (InformantOption.HELP.hasOption(cl))
             {
                 dumpUsage(null);
             }
 
             EventLevel level = EventLevel.ALL;
             if (InformantOption.EVENT_LEVEL.hasOption(cl))
             {
                 try
                 {
                     level = EventLevel.valueOf(InformantOption.EVENT_LEVEL.getValue(cl).toUpperCase());
                 }
                 catch (IllegalArgumentException e)
                 {
                     dumpUsage("Invalid event level.");
                     return; // appease javac
                 }
             }
 
             try
             {
                 AutomaticProtocol protocol = connect(cl);
 
                 try
                 {
                     protocol.mythProtoVersion();
                     protocol.ann(ConnectionType.MONITOR,
                                  InetAddress.getLocalHost().getHostName(),
                                  level);
 
                     System.out.println("Listening for backend events at level " + level);
                     System.out.println("Hit [ENTER] to quit.");
                     System.out.println();
 
                     BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                     input.readLine();
                 }
                 finally
                 {
                     protocol.done();
                 }
             }
             catch (InvalidProtocolVersionException e)
             {
                 System.out.println("Host speaks unsupported protocol version "
                         + e.getSupportedVersionStr());
                 System.exit(1);
             }
             catch (IOException e)
             {
                 System.out.println("Connection with remote host failed.");
                 System.out.println(e.getMessage());
                 System.exit(1);
             }
         }
         catch (ParseException e)
         {
             dumpUsage("Failed to parse command line: " + e.getMessage());
         }
     }
 
     private static AutomaticProtocol connect(CommandLine cl) throws IOException, ParseException
     {
         AutomaticProtocol protocol = new AutomaticProtocol();
         protocol.addBackendEventListener(eventListener63);
         protocol.addBackendEventListener(eventListener68);
 
        int port = InformantOption.PORT.hasOption(cl)
                ? InformantOption.PORT.getNumberValue(cl).intValue()
                 : 6543;
        long timeout = InformantOption.TIMEOUT.hasOption(cl)
                ? InformantOption.TIMEOUT.getNumberValue(cl).longValue()
                 : TimeUnit.SECONDS.toMillis(10);
 
        protocol.connect(InformantOption.HOST.getValue(cl), port, timeout);
 
         return protocol;
     }
 
     private static void dumpUsage(String errorMsg)
     {
         boolean error = errorMsg != null;
 
         if (error)
         {
             System.out.println(errorMsg);
             System.out.println();
         }
 
         new HelpFormatter().printHelp(Main.class.getName() + " [OPTIONS]",
                                       InformantOption.getOptions());
 
         System.exit(error ? 1 : 0);
     }
 
     private static BackendEventListener eventListener63 = new BackendEventListener63()
     {
         @Override
         public void clearSettingsCache()
         {
             System.out.println("Cleared settings cache");
         }
 
         @Override
         public void commFlagRequest(Channel channel, Date startTime)
         {
             System.out.println("Commercial flagging request: " + channel + ", " + startTime);
         }
 
         @Override
         public void doneRecording(int recorder, long seconds, long frames)
         {
             System.out.println("Done recording: "
                     + recorder
                     + ", "
                     + seconds
                     + " secs, "
                     + frames
                     + " frames");
         }
 
         @Override
         public void downloadFileUpdate(URL remoteUrl,
                                        URI localUri,
                                        long bytesReceived,
                                        long bytesTotal)
         {
             System.out.println("Download file update: "
                     + remoteUrl
                     + ", "
                     + localUri
                     + ", "
                     + bytesReceived
                     + "B, "
                     + bytesTotal
                     + "B");
         }
 
         @Override
         public void downloadFileFinshed(URL remoteUrl,
                                         URI localUri,
                                         long bytesTotal,
                                         String errorText,
                                         int errorCode)
         {
             System.out.println("Download file finished: "
                     + remoteUrl
                     + ", "
                     + localUri
                     + ", "
                     + bytesTotal
                     + "B, "
                     + errorText
                     + ", "
                     + errorCode);
         }
 
         @Override
         public void generatedPixmap(Channel channel,
                                     Date timestamp,
                                     String comment,
                                     Date timestamp2,
                                     long num1,
                                     long num2,
                                     byte[] bytes)
         {
             System.out.println("Generated pixmap: "
                     + channel
                     + ", "
                     + timestamp
                     + ", "
                     + comment
                     + ", "
                     + timestamp2
                     + ", "
                     + num1
                     + ", "
                     + num2);
         }
 
         @Override
         public void liveTvChainUpdate(String chainId)
         {
             System.out.println("Live TV chain update: " + chainId);
         }
 
         @Override
         public void liveTvWatch(int recorder, boolean recordingIsActive)
         {
             System.out.println("Live TV watch: recorder "
                     + recorder
                     + ", active "
                     + recordingIsActive);
         }
 
         @Override
         public void masterUpdateProgInfo(Channel channel, Date startTime)
         {
             System.out.println("Master update program info: " + channel + ", " + startTime);
         }
 
         @Override
         public void recordingListChangeNone()
         {
             System.out.println("Recording list change - EMPTY");
         }
 
         @Override
         public void recordingListChangeAdd(Channel channel, Date startTime)
         {
             System.out.println("Recording list change - ADD: " + channel + ", " + startTime);
         }
 
         @Override
         public void recordingListChangeUpdate(Program program)
         {
             System.out.println("Recording list change - UPDATE: " + program);
         }
 
         @Override
         public void recordingListChangeDelete(Channel channel, Date startTime)
         {
             System.out.println("Recording list change - DELETE: " + channel + ", " + startTime);
         }
 
         @Override
         public void scheduleChange()
         {
             System.out.println("Schedule changed");
         }
 
         @Override
         public void signalMessage(int recorder, String message)
         {
             System.out.println("Signal: recorder " + recorder + ", " + message);
         }
 
         @Override
         public void signalTunerStatus(int recorder, TunerStatus tunerStatus)
         {
             System.out.println("Signal: recorder " + recorder + ", " + tunerStatus);
         }
 
         @Override
         public void systemEvent(SystemEvent event, Map<SystemEventData, String> data)
         {
             System.out.println("System event: " + event + ", " + data);
         }
 
         @Override
         public void updateFileSize(Channel channel, Date startTime, long size)
         {
             System.out.println("Update file size: " + channel + ", " + startTime + ", " + size);
         }
 
         @Override
         public void videoListChange()
         {
             System.out.println("Video list changed");
         }
 
         @Override
         public void resetIdleTime()
         {
             System.out.println("Reset idle time");
         }
     };
 
     private static BackendEventListener eventListener68 = new BackendEventListener68()
     {
         @Override
         public void videoListChange(VideoListChange... changes)
         {
             if (changes.length == 0)
             {
                 System.out.println("Video list update - no changes");
                 return;
             }
 
             System.out.println("Video list changes: ");
             for (VideoListChange change : changes)
             {
                 System.out.println("\tID: " + change.getId() + "; Type: " + change.getType());
             }
         }
     };
 }
