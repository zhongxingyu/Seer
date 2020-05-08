 /*
  * Copyright 2010-2011 Ning, Inc.
  *
  * Ning licenses this file to you under the Apache License, version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License.  You may obtain a copy of the License at:
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 
 package com.ning.metrics.serialization.writer;
 
 import com.ning.metrics.serialization.event.Event;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 
 public class StubEventHandler implements EventHandler
 {
     private final EventWriter eventWriter;
 
     public StubEventHandler()
     {
         this.eventWriter = new StubEventWriter();
     }
 
     public StubEventHandler(EventWriter eventWriter)
     {
         this.eventWriter = eventWriter;
     }
 
     @Override
     public void handle(File file, CallbackHandler handler)
     {
         try {
             ArrayList<Event> events = new ArrayList<Event>();
 
             ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
 
             while (objectInputStream.read() != -1) {
                 Event e = (Event) objectInputStream.readObject();
                 events.add(e);
             }
             objectInputStream.close();
 
             for (Event event : events) {
                 eventWriter.write(event);
             }
 
             eventWriter.forceCommit();
            handler.onSuccess(file);
         }
         catch (IOException e) {
             handler.onError(e, file);
         }
         catch (ClassNotFoundException e) {
             handler.onError(e, file);
         }
     }
 }
