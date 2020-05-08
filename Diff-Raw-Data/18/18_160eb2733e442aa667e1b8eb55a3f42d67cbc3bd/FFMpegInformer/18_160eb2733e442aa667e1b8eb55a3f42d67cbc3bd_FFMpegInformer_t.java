 /*
  * Copyright (c) 2011 Sergey Prilukin
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package jstreamserver.utils.ffmpeg;
 
 import jstreamserver.utils.RuntimeExecutor;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Utility class which allows to read information information from audio\video inputs
  *
  * @athor Sergey Prilukin
  */
 public final class FFMpegInformer {
     public static final String INPUT_FILE_FORMAT = "-i";
 
     private RuntimeExecutor ffmpegExecutor = new RuntimeExecutor();
 
     public void setFfmpegExecutor(RuntimeExecutor ffmpegExecutor) {
         this.ffmpegExecutor = ffmpegExecutor;
     }
 
     public List<MediaInfo> getInfo(List<String> files, String ffmpegPath) throws IOException {
         final List<MediaInfo> mediaInfos = new ArrayList<MediaInfo>(files.size());
 
         ffmpegExecutor.execute(ffmpegPath, getFFMpegParams(files));
         Thread isReader = new Thread(new InputReader(ffmpegExecutor.getInputStream(), mediaInfos));
         Thread esReader = new Thread(new InputReader(ffmpegExecutor.getErrorStream(), mediaInfos));
 
         isReader.start();
         esReader.start();
 
         try {
             ffmpegExecutor.waitFor();
             isReader.join();
             esReader.join();
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         }
 
        ffmpegExecutor.destroy();
         return mediaInfos;
     }
     
    private static String[] getFFMpegParams(List<String> files) {
 
         String[] params = new String[files.size() * 2];
 
         int i = 0;
         for (String file: files) {
             params[i * 2] = INPUT_FILE_FORMAT;
             params[i * 2 + 1] = file;
 
             i++;
         }
 
         return params;
     }
 
     /**
      * Utility class which reads text lines from passed {@link InputStream}
      * And reports progress to passed {@link ProgressListener}
      */
    static class InputReader implements Runnable {
         private BufferedReader reader;
         private List<MediaInfo> mediaInfos;
         private MediaInfo currentMediaInfo;
 
         public InputReader(InputStream inputStream, List<MediaInfo> mediaInfos) {
             this.reader = new BufferedReader(new InputStreamReader(inputStream));
             this.mediaInfos = mediaInfos;
         }
 
         @Override
         public void run() {
             try {
                 String line;
                 while ((line = reader.readLine()) != null) {
                     Matcher matcher = Pattern.compile(MediaInfo.INPUT_PATTERN).matcher(line);
                     if (matcher.find()) {
                         currentMediaInfo = new MediaInfo();
                         mediaInfos.add(currentMediaInfo);
                         continue;
                     }
 
                     matcher = Pattern.compile(MediaInfo.DURATION_PATTERN).matcher(line);
                     if (matcher.find()) {
                         currentMediaInfo.setDuration(matcher.group(1));
                         currentMediaInfo.setBitrate(matcher.group(4));
                         continue;
                     }
 
                     matcher = Pattern.compile(VideoStreamInfo.PATTERN).matcher(line);
                     if (matcher.find()) {
                         VideoStreamInfo videoStreamInfo = new VideoStreamInfo();
                         videoStreamInfo.setId(matcher.group(1));
                         videoStreamInfo.setLanguage(matcher.group(3));
                         videoStreamInfo.setEncoder(matcher.group(4));
                         videoStreamInfo.setResolution(matcher.group(6));
                         videoStreamInfo.setDefaultStream(matcher.group(8) != null);
                         currentMediaInfo.getVideoStreams().add(videoStreamInfo);
                         continue;
                     }
 
                     matcher = Pattern.compile(AudioStreamInfo.PATTERN).matcher(line);
                     if (matcher.find()) {
                         AudioStreamInfo audioStreamInfo = new AudioStreamInfo();
                         audioStreamInfo.setId(matcher.group(1));
                         audioStreamInfo.setLanguage(matcher.group(3));
                         audioStreamInfo.setEncoder(matcher.group(4));
                         audioStreamInfo.setFrequency(matcher.group(6));
                         audioStreamInfo.setChannels(matcher.group(7));
                         audioStreamInfo.setDefaultStream(matcher.group(8) != null);
 
                         currentMediaInfo.getAudioStreams().add(audioStreamInfo);
                     }
                 }
             } catch (Exception e) {
                 /* ignore */
             } finally {
                 try {
                     reader.close();
                 } catch (IOException e) {
                     /* ignore */
                 }
             }
         }
     }
 }
