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
 import com.dmdirc.addons.nowplaying.MediaSourceManager;
 import com.dmdirc.plugins.Plugin;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Provides a media source for dbus players.
  */
 public class DBusMediaSource extends Plugin implements MediaSourceManager {
 
     /** The sources used by this media source. */
     private List<MediaSource> sources;
     /** A map of discovered mpris sources. */
     private final Map<String, MediaSource> mprisSources = new HashMap<String, MediaSource>();
     /** The path to qdbus. */
     private String qdbus;
        
     /**
      * Creates a new instance of DBusMediaSource.
      */
     public DBusMediaSource() {
         super();
     }
     
     /** {@inheritDoc} */
     @Override
     public void onLoad() {
         sources = new ArrayList<MediaSource>(Arrays.asList(new MediaSource[]{
             new BansheeSource(this),
         }));
 
         if (new File("/usr/bin/qdbus").exists()) {
             qdbus = "/usr/bin/qdbus";
         } else if (new File("/bin/qdbus").exists()) {
             qdbus = "/bin/qdbus";
         }
     }
     
     /** {@inheritDoc} */
     @Override
     public void onUnload() {
         // Do nothing
     }
 
     /** {@inheritDoc} */
     @Override
     public List<MediaSource> getSources() {
         for (String mpris : doDBusCall("org.mpris.*", "/", "/")) {
            try {
                final String service = mpris.substring(10);

                if (!mprisSources.containsKey(service)) {
                    mprisSources.put(service, new MPRISSource(this, service));
                    sources.add(mprisSources.get(service));
                }
            } catch (IllegalArgumentException ex) {
                // The service either stopped after the initial call and before
                // we created an MRPIS Source, or otherwise doesn't correctly
                // implement MPRIS. Either way, ignore it.
             }
         }
 
         return sources;
     }
 
     /**
      * Performs a dbus call.
      *
      * @param service The name of the service
      * @param iface The name of the interface
      * @param method The name of the method
      * @param args Any arguments to the method
      * @return A list of output (one entry per line)
      */
     public List<String> doDBusCall(final String service, final String iface,
             final String method, final String ... args) {
         final String[] exeArgs = new String[4 + args.length];
         exeArgs[0] = qdbus;
         exeArgs[1] = service;
         exeArgs[2] = iface;
         exeArgs[3] = method;
 
         for (int i = 0; i < args.length; i++) {
             exeArgs[4 + i] = args[i];
         }
 
         return getInfo(exeArgs);
     }
 
     /**
      * Executes the specified command and arguments and returns the results.
      *
      * @param args The command/arguments to be executed
      * @return The output of the specified command
      */
     protected static List<String> getInfo(final String[] args) {
         final ArrayList<String> result = new ArrayList<String>();
 
         InputStreamReader reader;
         BufferedReader input;
         Process process;
 
         try {
             process = Runtime.getRuntime().exec(args);
 
             reader = new InputStreamReader(process.getInputStream());
             input = new BufferedReader(reader);
 
             String line = "";
 
             while ((line = input.readLine()) != null) {
                 result.add(line);
             }
 
             reader.close();
             input.close();
             process.destroy();
         } catch (IOException ex) {
             ex.printStackTrace();
         }
 
         return result;
     }
 
     /**
      * Parses a dbus dictionary into a {@link Map}.
      *
      * @param lines The lines to be parsed as a dictionary
      * @return A map corresponding to the specified dictionary
      */
     protected static Map<String, String> parseDictionary(final List<String> lines) {
         final Map<String, String> res = new HashMap<String, String>();
 
         for (String line : lines) {
             final int index = line.indexOf(':');
 
             if (index == -1 || index >= line.length() - 2) {
                 continue;
             }
             
             final String key = line.substring(0, index);
             final String value = line.substring(index + 2);
 
             res.put(key, value);
         }
 
         return res;
     }
 
 }
