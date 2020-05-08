 /*
  * Copyright (c) 2006-2014 DMDirc Developers
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
 
 package com.dmdirc.addons.mediasource_linux_title;
 
 import com.dmdirc.addons.nowplaying.MediaSource;
 import com.dmdirc.addons.nowplaying.MediaSourceState;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.util.io.StreamUtils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 /**
  * Provides a media source for Linux players using the `xwininfo` command.
  */
 public class TitleMediaSource implements MediaSource {
 
     /** The command to use to get the title. */
     private final String command;
     /** The name of the player we're retrieving. */
     private final String name;
 
     /**
      * Creates a new title media source.
      *
      * @param command The command to be executed
      * @param name    The name of the media source
      */
     public TitleMediaSource(final String command, final String name) {
         this.command = command;
         this.name = name;
     }
 
     @Override
     public MediaSourceState getState() {
         if (getInfo().isEmpty()) {
             return MediaSourceState.CLOSED;
         } else if (getInfo().indexOf('-') == -1) {
             return MediaSourceState.STOPPED;
         } else {
             return MediaSourceState.PLAYING;
         }
     }
 
     @Override
     public String getAppName() {
         return name;
     }
 
     @Override
     public String getArtist() {
         return getInfo().split("–", 2)[0].trim();
     }
 
     @Override
     public String getTitle() {
         final String[] info = getInfo().split("–", 2);
 
         if (info.length >= 2) {
             return info[1].trim();
         } else {
             return "";
         }
     }
 
     @Override
     public String getAlbum() {
         return "";
     }
 
     @Override
     public String getLength() {
         return "";
     }
 
     @Override
     public String getTime() {
         return "";
     }
 
     @Override
     public String getFormat() {
         return "";
     }
 
     @Override
     public String getBitrate() {
         return "";
     }
 
     private String getInfo() {
         InputStreamReader reader = null;
         BufferedReader input = null;
         Process process;
 
         try {
             final String[] args = new String[]{
                 "/bin/bash", "-c", "xwininfo -root -tree | " + command
             };
 
             process = Runtime.getRuntime().exec(args);
 
             reader = new InputStreamReader(process.getInputStream());
             input = new BufferedReader(reader);
 
            String line = "";

            while ((line = input.readLine()) != null) {
                 return line;
             }
         } catch (IOException ex) {
             Logger.userError(ErrorLevel.LOW, "Unable to retrieve media source info",
                     ex);
         } finally {
             StreamUtils.close(reader);
             StreamUtils.close(input);
         }
 
         return "";
     }
 
 }
