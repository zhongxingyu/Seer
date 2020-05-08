 /**
  *
  * Copyright (C) 2010 markw <mark@wolfe.id.au>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package au.id.wolfe.riak.log4j;
 
 import com.basho.riak.client.RiakClient;
 import com.basho.riak.client.RiakObject;
 import com.google.common.collect.ImmutableMap;
 import org.apache.log4j.spi.LoggingEvent;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Map;
 
 /**
  * @author Mark Wolfe (<A HREF="mailto:mark@wolfe.id.au">mark@wolfe.id.au</A>)
  */
 public class RiakAppender extends org.apache.log4j.AppenderSkeleton
         implements org.apache.log4j.Appender {
 
    //private final Log log = LogFactory.getLog(getClass());
 
     ObjectMapper mapper = new ObjectMapper();
 
     String url;
 
     RiakClient riakClient;
 
     String bucket;
 
     @Override
     protected void append(LoggingEvent event) {
 
         //String resultString = null;
 
         StringWriter jsonWriter = new StringWriter();
 
         Map<String, Object> untyped = new ImmutableMap.Builder<String, Object>()
                 .put("thread", event.getThreadName())
                 .put("class", event.getFQNOfLoggerClass())
                 .put("level", event.getLevel())
                 .put("message", event.getMessage())
                 .put("timestamp", event.getTimeStamp())
                 .build();
 
         try {
             mapper.writeValue(jsonWriter, untyped);
 
             getRiakClient().store(new RiakObject(bucket, Long.toString(event.getTimeStamp()), jsonWriter.toString()));
 
         } catch (IOException e) {
             // ignore
         }
 
 
 /*        try {
             resultString = new JSONStringer()
                     .object()
                     .key("thread")
                     .value(event.getThreadName())
                     .key("class")
                     .value(event.getFQNOfLoggerClass())
                     .key("level")
                     .value(event.getLevel())
                     .key("message")
                     .value(event.getMessage())
                     .key("timestamp")
                     .value(event.getTimeStamp())
                     .endObject()
                     .toString();
 
             getRiakClient().store(new RiakObject(bucket, Long.toString(event.getTimeStamp()), resultString));
 
         } catch (JSONException e) {
             log.error(e.getMessage(), e);
         }*/
 
 
 /*
         if (event.getThrowableInformation() != null){
             JSONArray jsonArray = new JSONArray(event.getThrowableInformation().getThrowableStrRep())
         }
 */
     }
 
     private RiakClient getRiakClient() {
         if (riakClient == null) {
             riakClient = new RiakClient(url);
         }
         return riakClient;
     }
 
     public String getUrl() {
         return url;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
 
     public String getBucket() {
         return bucket;
     }
 
     public void setBucket(String bucket) {
         this.bucket = bucket;
     }
 
     public void close() {
         // Not required
     }
 
     public boolean requiresLayout() {
         return false;
     }
 }
