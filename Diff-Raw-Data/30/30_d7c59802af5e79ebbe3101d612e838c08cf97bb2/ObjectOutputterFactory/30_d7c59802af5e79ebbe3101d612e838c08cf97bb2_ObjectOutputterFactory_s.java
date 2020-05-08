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
 import com.ning.metrics.serialization.event.EventSerializer;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 public class ObjectOutputterFactory
 {
    // TODO Does this actually work!? This is using generics in crazy ways!
    public static <T extends Event> ObjectOutputter<T> createObjectOutputter(FileOutputStream out, SyncType type, int batchSize) throws IOException
     {
         return createObjectOutputter(out, type, batchSize, new ObjectOutputEventSerializer<T>());
     }
 
     /**
     * @param out
     * @param type
     * @param batchSize
      * @param eventSerializer does not have to be tied to 'out'. We will call eventSerializer.open(out) later.
     * If eventSerializer == null, it's the same as calling the default createObjectOutputter()
     * @param <T>
     * @return
     * @throws IOException
      */
    public static <T extends Event> ObjectOutputter<T> createObjectOutputter(FileOutputStream out, SyncType type, int batchSize, EventSerializer<T> eventSerializer) throws IOException
     {
         if (eventSerializer == null) {
             return createObjectOutputter(out, type, batchSize);
         }
 
         switch (type) {
             case NONE:
                 return new DefaultObjectOutputter<T>(out, eventSerializer);
             case FLUSH:
                 return new FlushingObjectOutputter<T>(out, eventSerializer, batchSize);
             case SYNC:
                 return new SyncingObjectOutputter<T>(out, eventSerializer, batchSize);
         }
 
        throw new IllegalArgumentException("unable to construct ObjectOutputter given type");
     }
 }
