 /*
  * Copyright (c) 2011. Piraso Alvin R. de Leon. All Rights Reserved.
  *
  * See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The Piraso licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ard.piraso.ui.io.impl;
 
 import ard.piraso.api.Preferences;
 import ard.piraso.api.io.EntryReadListener;
 import ard.piraso.client.net.HttpPirasoEntryReader;
 import ard.piraso.ui.io.IOEntrySource;
 import org.apache.http.client.HttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author adleon
  */
 public class HttpEntrySource implements IOEntrySource {
     
     private static final Logger LOG = Logger.getLogger(HttpEntrySource.class.getName());
         
     private HttpPirasoEntryReader reader;

     private boolean stopped;
     
     public HttpEntrySource(Preferences preferences, String uri) {
         this(preferences, uri, null);
     }
     
     public HttpEntrySource(Preferences preferences, String uri, String watchedAddr) {
        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager();
        manager.setDefaultMaxPerRoute(2);
        manager.setMaxTotal(2);

        HttpClient client = new DefaultHttpClient(manager);
        HttpContext context = new BasicHttpContext();
         this.reader = new HttpPirasoEntryReader(client, context);
         
         reader.setUri(uri);
         reader.getStartHandler().setPreferences(preferences);
         
         if(watchedAddr != null) {
             reader.getStartHandler().setWatchedAddr(watchedAddr);
         }
     }
 
     @Override
     public void start() {
         try {
             reader.start();
         } catch (Exception ex) {
             LOG.log(Level.SEVERE, ex.getMessage(), ex);
         } finally {
             stopped = true;
         }
     }
 
     @Override
     public void stop() {
         try {
             reader.stop();
         } catch (IOException ex) {
             LOG.log(Level.SEVERE, ex.getMessage(), ex);
         }
     }
 
     @Override
     public String getId() {
         return reader.getStartHandler().getId();
     }
 
     @Override
     public boolean isStopped() {
         return stopped;
     }
 
     @Override
     public List<EntryReadListener> getListeners() {
         return reader.getStartHandler().getListeners();
     }
 
     @Override
     public void addListener(EntryReadListener listener) {
         reader.getStartHandler().addListener(listener);
     }
 
     @Override
     public void removeListener(EntryReadListener listener) {
         reader.getStartHandler().removeListener(listener);
     }
 
     @Override
     public void clearListeners() {
         reader.getStartHandler().clearListeners();
     }  
     
     @Override
     public String getWatchedAddr() {
         return reader.getStartHandler().getWatchedAddr();
     }
 }
