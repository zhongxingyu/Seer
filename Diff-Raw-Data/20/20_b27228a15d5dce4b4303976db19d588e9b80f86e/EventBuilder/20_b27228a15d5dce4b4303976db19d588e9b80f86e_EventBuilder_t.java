 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.flume.event;
 
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.google.common.collect.Maps;
 import com.google.common.primitives.Ints;
 import com.google.common.primitives.Longs;
 import org.apache.flume.Event;
 
 public class EventBuilder {
 
   /**
    * Instantiate an Event instance based on the provided body and headers.
    * If <code>headers</code> is <code>null</code>, then it is ignored.
    * @param body
    * @param headers
    * @return
    */
   public static Event withBody(byte[] body, Map<String, String> headers) {
     Event event = new SimpleEvent();
 
     if(body == null) {
       body = new byte[0];
     }
     event.setBody(body);
 
     if (headers != null) {
       event.setHeaders(new HashMap<String, String>(headers));
     }
 
     return event;
   }
 
   public static Event withBody(byte[] body) {
     return withBody(body, null);
   }
 
   public static Event withBody(long body) {
     Map<String, String> headers = Maps.newHashMap();
     headers.put(Event.bodyType, "long");
     return withBody(Longs.toByteArray(body), headers);
   }
 
   public static Event withBody(String body, Charset charset,
       Map<String, String> headers) {
 
      Map<String, String> newHeaders = null;

      if (headers == null ) newHeaders = Maps.newHashMap();
      else newHeaders = Maps.newHashMap(headers);

      newHeaders.put(Event.bodyType, "string");
      newHeaders.put(Event.bodyCharset, charset.name());
 
     return withBody(body.getBytes(charset), newHeaders);
   }
 
   public static Event withBody(String body, Charset charset) {
     return withBody(body, charset, null);
   }
 
 }
