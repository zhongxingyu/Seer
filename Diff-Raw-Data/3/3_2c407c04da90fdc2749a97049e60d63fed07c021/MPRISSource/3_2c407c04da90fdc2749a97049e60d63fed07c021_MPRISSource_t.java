 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 package com.dmdirc.addons.mediasource_dbus;
 
 import com.dmdirc.addons.nowplaying.MediaSource;
 import com.dmdirc.addons.nowplaying.MediaSourceState;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * A media source for anything that's compatible with MRPIS.
  *
  * @author chris
  */
 public class MPRISSource implements MediaSource {
 
     /** The media source manager. */
     private final DBusMediaSource source;
     /** The service name. */
     private final String service;
     /** The name of the source. */
     private final String name;
     /** A temporary cache of track data. */
     private Map<String, String> data;
 
     /**
      * Creates a new MPRIS source for the specified service name.
      *
      * @param source The manager which owns this source
      * @param service The service name of the MRPIS service
      */
     public MPRISSource(final DBusMediaSource source, final String service) {
         this.source = source;
         this.service = service;
 
         final List<String> info = source.doDBusCall("org.mpris." + service, "/",
                 "org.freedesktop.MediaPlayer.Identity");
 
         if (info.isEmpty()) {
             throw new IllegalArgumentException("No service with that name found");
         }
 
         this.name = info.get(0).replace(' ', '_');
     }
 
     /** {@inheritDoc} */
     @Override
     public MediaSourceState getState() {
         final char[] status = getStatus();
 
         if (status == null) {
             data = null;
             return MediaSourceState.CLOSED;
         }
 
         data = getTrackInfo();
 
         if (status[0] == '0') {
             return MediaSourceState.PLAYING;
         } else if (status[0] == '1') {
             return MediaSourceState.PAUSED;
         } else if (status[0] == '2') {
             return MediaSourceState.STOPPED;
         } else {
             return MediaSourceState.NOTKNOWN;
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public String getAppName() {
         return name;
     }
 
     /**
      * Utility method to return the value of the specified key if it exists,
      * or "Unknown" if it doesn't.
      *
      * @param key The key to be retrieved
      * @return The value of the specified key or "Unknown".
      */
     protected String getData(final String key) {
        return data == null || !data.containsKey(key) || data.get(key) == null
                ? "Unknown" : data.get(key);
     }
 
     /** {@inheritDoc} */
     @Override
     public String getArtist() {
         return getData("artist");
     }
 
     /** {@inheritDoc} */
     @Override
     public String getTitle() {
         return getData("title");
     }
 
     /** {@inheritDoc} */
     @Override
     public String getAlbum() {
         return getData("album");
     }
 
     /** {@inheritDoc} */
     @Override
     public String getLength() {
         return getData("time");
     }
 
     /** {@inheritDoc} */
     @Override
     public String getTime() {
         return "Unknown";
     }
 
     /** {@inheritDoc} */
     @Override
     public String getFormat() {
         return "Unknown";
     }
 
     /** {@inheritDoc} */
     @Override
     public String getBitrate() {
         return getData("audio-bitrate");
     }
 
     /**
      * Retrieves a map of track information from the service.
      *
      * @return A map of metadata returned by the MPRIS service
      */
     protected Map<String, String> getTrackInfo() {
         // If only there were a standard...
         final List<String> list = source.doDBusCall("org.mpris." + service,
                 "/Player", "org.freedesktop.MediaPlayer.GetMetadata");
         list.addAll(source.doDBusCall("org.mpris." + service,
                 "/Player", "org.freedesktop.MediaPlayer.GetMetaData"));
         return DBusMediaSource.parseDictionary(list);
     }
 
     /**
      * Retrieves the 'status' result from the MPRIS service.
      *
      * @return The returned status or null if the service isn't running
      */
     protected char[] getStatus() {
         if (source.doDBusCall("org.mpris." + service, "/",
                 "org.freedesktop.MediaPlayer.Identity").isEmpty()) {
             // Calling dbus-send can seemingly start applications that have
             // quit (not entirely sure how), so check that it's running first.
             return null;
         }
 
         final List<String> res = DBusMediaSource.getInfo(new String[]{
             "/usr/bin/dbus-send", "--print-reply", "--dest=org.mpris." + service,
             "/Player", "org.freedesktop.MediaPlayer.GetStatus"
         });
 
         if (res.isEmpty()) {
             return null;
         }
 
         final char[] result = new char[4];
         int i = 0;
 
         for (String line : res) {
             final String tline = line.trim();
 
             if (tline.startsWith("int32")) {
                 result[i++] = tline.charAt(tline.length() - 1);
             }
         }
 
         return result;
     }
 
 }
