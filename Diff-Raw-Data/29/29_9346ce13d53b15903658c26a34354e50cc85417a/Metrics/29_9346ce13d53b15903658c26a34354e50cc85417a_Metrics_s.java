 /*
  * Copyright 2011-2013 Tyler Blair. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of
  *       conditions and the following disclaimer.
  *
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list
  *       of conditions and the following disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and contributors and should not be interpreted as representing official policies,
  * either expressed or implied, of anybody else.
  */
package org.mcstats;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;
 
 public class Metrics {
 
     /**
      * The current revision number
      */
     private final static Integer REVISION = null;
 
     /**
      * The base url of the metrics domain
      */
     private static final String BASE_URL = "";
 
     /**
      * The url used to report a server's status
      */
     private static final String REPORT_URL = "";
 
     /**
      * Interval of time to ping (in minutes)
      */
     private static final Integer PING_INTERVAL = null;
 
     /**
      * The plugin this metrics submits for
      */
     private final Plugin plugin;
 
     /**
      * All of the custom graphs to submit to metrics
      */
     private final Set<Graph> graphs = null;
 
     /**
      * The plugin configuration file
      */
     private final YamlConfiguration configuration;
 
     /**
      * The plugin configuration file
      */
     private final File configurationFile;
 
     /**
      * Unique server id
      */
     private final String guid;
 
     /**
      * Debug mode
      */
     private final boolean debug;
 
     /**
      * Lock for synchronization
      */
     private final Object optOutLock = null;
 
     /**
      * The scheduled task
      */
     private volatile BukkitTask task = null;
 
     public Metrics(final Plugin plugin) throws IOException {
         
     }
 
     /**
      * Construct and create a Graph that can be used to separate specific plotters to their own graphs on the metrics
      * website. Plotters can be added to the graph object returned.
      *
      * @param name The name of the graph
      * @return Graph object created. Will never return NULL under normal circumstances unless bad parameters are given
      */
     public Graph createGraph(final String name) {
         
     }
 
     /**
      * Add a Graph object to BukkitMetrics that represents data for the plugin that should be sent to the backend
      *
      * @param graph The name of the graph
      */
     public void addGraph(final Graph graph) {
        
     }
 
     /**
      * Start measuring statistics. This will immediately create an async repeating task as the plugin and send the
      * initial data to the metrics backend, and then after that it will post in increments of PING_INTERVAL * 1200
      * ticks.
      *
      * @return True if statistics measuring is running, otherwise false.
      */
     public boolean start() {
         
     }
 
     /**
      * Has the server owner denied plugin metrics?
      *
      * @return true if metrics should be opted out of it
      */
     public boolean isOptOut() {
         
     }
 
     /**
      * Enables metrics for the server by setting "opt-out" to false in the config file and starting the metrics task.
      *
      * @throws java.io.IOException
      */
     public void enable() throws IOException {
         
     }
 
     /**
      * Disables metrics for the server by setting "opt-out" to true in the config file and canceling the metrics task.
      *
      * @throws java.io.IOException
      */
     public void disable() throws IOException {
         
     }
 
     /**
      * Gets the File object of the config file that should be used to store data such as the GUID and opt-out status
      *
      * @return the File object for the config file
      */
     public File getConfigFile() {
         
     }
 
     /**
      * Generic method that posts a plugin to the metrics website
      */
     private void postPlugin(final boolean isPing) throws IOException {
         
     }
 
     /**
      * GZip compress a string of bytes
      *
      * @param input
      * @return
      */
     public static byte[] gzip(String input) {
         
     }
 
     /**
      * Check if mineshafter is present. If it is, we need to bypass it to send POST requests
      *
      * @return true if mineshafter is installed on the server
      */
     private boolean isMineshafterPresent() {
         
     }
 
     /**
      * Appends a json encoded key/value pair to the given string builder.
      *
      * @param json
      * @param key
      * @param value
      * @throws UnsupportedEncodingException
      */
     private static void appendJSONPair(StringBuilder json, String key, String value) throws UnsupportedEncodingException {
         
     }
 
     /**
      * Escape a string to create a valid JSON string
      *
      * @param text
      * @return
      */
     private static String escapeJSON(String text) {
         
     }
 
     /**
      * Encode text as UTF-8
      *
      * @param text the text to encode
      * @return the encoded text, as UTF-8
      */
     private static String urlEncode(final String text) throws UnsupportedEncodingException {
     }
 
     /**
      * Represents a custom graph on the website
      */
     public static class Graph {
 
         /**
          * The graph's name, alphanumeric and spaces only :) If it does not comply to the above when submitted, it is
          * rejected
          */
         private final String name;
 
         /**
          * The set of plotters that are contained within this graph
          */
         private final Set<Plotter> plotters = null;
 
         private Graph(final String name) {
         }
 
         /**
          * Gets the graph's name
          *
          * @return the Graph's name
          */
         public String getName() {
         }
 
         /**
          * Add a plotter to the graph, which will be used to plot entries
          *
          * @param plotter the plotter to add to the graph
          */
         public void addPlotter(final Plotter plotter) {
         }
 
         /**
          * Remove a plotter from the graph
          *
          * @param plotter the plotter to remove from the graph
          */
         public void removePlotter(final Plotter plotter) {
         }
 
         /**
          * Gets an <b>unmodifiable</b> set of the plotter objects in the graph
          *
          * @return an unmodifiable {@link java.util.Set} of the plotter objects
          */
         public Set<Plotter> getPlotters() {
         }
 
         @Override
         public int hashCode() {
         }
 
         @Override
         public boolean equals(final Object object) {
  
         }
 
         /**
          * Called when the server owner decides to opt-out of BukkitMetrics while the server is running.
          */
         protected void onOptOut() {
         }
     }
 
     /**
      * Interface used to collect custom data for a plugin
      */
     public static abstract class Plotter {
 
         /**
          * The plot's name
          */
         private final String name;
 
         /**
          * Construct a plotter with the default plot name
          */
         public Plotter() {
         }
 
         /**
          * Construct a plotter with a specific plot name
          *
          * @param name the name of the plotter to use, which will show up on the website
          */
         public Plotter(final String name) {
         }
 
         /**
          * Get the current value for the plotted point. Since this function defers to an external function it may or may
          * not return immediately thus cannot be guaranteed to be thread friendly or safe. This function can be called
          * from any thread so care should be taken when accessing resources that need to be synchronized.
          *
          * @return the current value for the point to be plotted.
          */
         public abstract int getValue();
 
         /**
          * Get the column name for the plotted point
          *
          * @return the plotted point's column name
          */
         public String getColumnName() {
         }
 
         /**
          * Called after the website graphs have been updated
          */
         public void reset() {
         }
 
         @Override
         public int hashCode() {
         }
 
         @Override
         public boolean equals(final Object object) {
             
         }
     }
 }
