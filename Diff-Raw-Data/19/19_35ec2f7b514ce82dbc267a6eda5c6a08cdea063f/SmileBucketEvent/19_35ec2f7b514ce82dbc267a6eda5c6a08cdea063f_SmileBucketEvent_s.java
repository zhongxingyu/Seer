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
 
 package com.ning.metrics.serialization.event;
 
 import com.ning.metrics.serialization.smile.SmileBucket;
 import com.ning.metrics.serialization.smile.SmileBucketDeserializer;
 import com.ning.metrics.serialization.smile.SmileBucketSerializer;
 import com.ning.metrics.serialization.smile.SmileOutputStream;
 import org.joda.time.DateTime;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 import java.nio.charset.Charset;
 
 /**
  * A meta-event wrapping a collector of Smile events of the same type and same granularity.
  * This is used for serializing, to leverage compression (back-references) that Smile offers.
  */
 public class SmileBucketEvent implements Event
 {
     private String eventName;
     private Granularity granularity;
     private String suffixOutputPath = ""; // Not necessarily set, and avoid triggering an NPE in serialization methods
     private SmileBucket bucket;
 
     private SmileOutputStream eventStream = null;
     private static final Charset CHARSET = Charset.forName("UTF-8");
 
     public SmileBucketEvent(String eventName, Granularity granularity, SmileBucket bucket)
     {
         this(eventName, granularity, "", bucket);
     }
 
     @Deprecated
     public SmileBucketEvent()
     {
     }
 
     public SmileBucketEvent(String eventName, Granularity granularity, String baseOutputDir, SmileBucket bucket)
     {
         this.eventName = eventName;
         this.granularity = granularity;
         this.suffixOutputPath = baseOutputDir;
         this.bucket = bucket;
     }
 
     public SmileBucket getBucket()
     {
         return bucket;
     }
 
     @Override
     public DateTime getEventDateTime()
     {
        throw new RuntimeException("Events in SmileBucket have different timestamps!");
     }
 
     @Override
     public String getName()
     {
         return eventName;
     }
 
     @Override
     public Granularity getGranularity()
     {
         return granularity;
     }
 
     @Override
     public String getVersion()
     {
         return null;
     }
 
     @Override
     public String getOutputDir(String prefix)
     {
         if (suffixOutputPath.length() == 0) {
             // Add a safeguard here - if it's not set, the caller must be doing something wrong
             throw new RuntimeException("suffixOutputPath not set, events not properly grouped. See SmileEnvelopeEventsToSmileBucketEvents class.");
         }
         else {
             return String.format("%s%s", prefix, suffixOutputPath);
         }
     }
 
     /**
      * Create a Hadoop-friendly serializable object representing this bucket event.
      *
      * @return Object representing the data, compressed Smile version of the events
      */
     @Override
     public Object getData()
     {
         if (eventStream == null) {
             // Start with 16kB buffer
             eventStream = new SmileOutputStream(eventName, 16384);
 
             try {
                 SmileBucketSerializer.serialize(bucket, eventStream);
             }
             catch (IOException e) {
                 return null;
             }
         }
 
         return eventStream;
     }
 
     /**
      * Serialize an event to a byte array.
      * This method is optional, methods relying on this call should handle gracefully null.
      *
      * @return byte array representation of an event, can return null
      */
     @Override
     public byte[] getSerializedEvent()
     {
         return ((ByteArrayOutputStream) getData()).toByteArray();
     }
 
     @Override
     public void writeExternal(ObjectOutput objectOutput) throws IOException
     {
         int eventNameLength = eventName.length();
         objectOutput.writeInt(eventNameLength);
         objectOutput.write(eventName.getBytes(CHARSET));
 
         int granularityLength = granularity.toString().length();
         objectOutput.writeInt(granularityLength);
         objectOutput.write(granularity.toString().getBytes(CHARSET));
 
         objectOutput.writeInt(suffixOutputPath.length());
         objectOutput.write(suffixOutputPath.getBytes(CHARSET));
 
         byte[] data = getSerializedEvent();
         int dataLen = data.length;
 
         objectOutput.writeInt(dataLen);
         objectOutput.write(data);
     }
 
     @Override
     public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException
     {
         int eventNameLength = objectInput.readInt();
         byte[] eventNameData = new byte[eventNameLength];
         objectInput.readFully(eventNameData);
         eventName = new String(eventNameData, CHARSET);
 
         int granularityLength = objectInput.readInt();
         byte[] granularityData = new byte[granularityLength];
         objectInput.readFully(granularityData);
         try {
             granularity = Granularity.valueOf(new String(granularityData, CHARSET));
         }
         catch (IllegalArgumentException e) {
             granularity = Granularity.HOURLY;
         }
 
         int suffixOutputPathLength = objectInput.readInt();
         byte[] suffixOuputPathData = new byte[suffixOutputPathLength];
         objectInput.readFully(suffixOuputPathData);
         suffixOutputPath = new String(suffixOuputPathData, CHARSET);
 
         int dataLen = objectInput.readInt();
         byte[] data = new byte[dataLen];
         objectInput.readFully(data);
 
         bucket = SmileBucketDeserializer.deserialize(new ByteArrayInputStream(data));
     }
 
     public int getNumberOfEvent()
     {
         return bucket.size();
     }
 }
