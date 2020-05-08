 /*
  * Copyright (c) 2012 Alvin R. de Leon. All Rights Reserved.
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
 
 package ard.piraso.ui.base;
 
 import ard.piraso.ui.base.cookie.*;
 import ard.piraso.ui.io.IOEntryEvent;
 import ard.piraso.ui.io.IOEntryLifecycleListener;
 import ard.piraso.ui.io.IOEntryReader;
 import org.openide.util.lookup.InstanceContent;
 import org.openide.windows.TopComponent;
 
 /**
  *
  * @author adeleon
  */
 public class IOEntryReaderActionProvider implements IOEntryLifecycleListener {
     
     private IOEntryReader reader;
     
     private InstanceContent content;
 
     private IOEntryReaderSaveCookie saveCookie;
     
     private IOEntryReaderStartCookie startCookie;
     
     private IOEntryReaderStopCookie stopCookie;
 
     private boolean savable;
     
     public IOEntryReaderActionProvider(TopComponent topComponent, IOEntryReader reader, InstanceContent content) {
         this.reader = reader;
         this.content = content;
         
         this.startCookie = new IOEntryReaderStartCookie(reader);
         this.stopCookie = new IOEntryReaderStopCookie(reader);
         this.saveCookie = new IOEntryReaderSaveCookie(reader, topComponent);
 
         content.add(startCookie);
         setReader(reader);
     }
 
     public void setReader(IOEntryReader reader) {
         if(this.reader != null) {
             this.reader.removeLiveCycleListener(this);
         }
 
         this.reader = reader;
         setSavable(false);
         this.startCookie.setReader(reader);
         this.stopCookie.setReader(reader);
        this.saveCookie.setReader(reader);
         this.reader.addLiveCycleListener(this);
     }
 
     public StartCookie getStartCookie() {
         return startCookie;
     }
 
     public StopCookie getStopCookie() {
         return stopCookie;
     }
 
     public IOEntryReaderSaveCookie getSaveCookie() {
         return saveCookie;
     }
 
     public void setSavable(boolean value) {
         if(savable == value) {
             return;
         }
         savable = value;
 
         if(savable) {
             content.add(saveCookie);
         } else {
             content.remove(saveCookie);
         }
     }
 
     @Override
     public void started(IOEntryEvent evt) {
         content.remove(startCookie);
         content.add(stopCookie);
     }
 
     @Override
     public void stopped(IOEntryEvent evt) {
         if(reader.isRestartable()) {
             content.add(startCookie);
         }
 
         content.remove(stopCookie);
     }
 }
