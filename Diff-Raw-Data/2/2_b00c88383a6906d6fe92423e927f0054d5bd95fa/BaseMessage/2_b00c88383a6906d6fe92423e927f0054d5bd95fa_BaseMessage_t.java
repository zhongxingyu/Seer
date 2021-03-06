 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.geronimo.gshell.whisper.message;
 
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.apache.geronimo.gshell.yarn.ReflectionToStringBuilder;
 import org.apache.geronimo.gshell.yarn.ToStringStyle;
 
 /**
  * Support for {@link Message} implementations.
  *
  * @version $Rev$ $Date$
  */
 public class BaseMessage
     implements Message
 {
     private static final AtomicLong SEQUENCE_GENERATOR = new AtomicLong(0);
 
     private ID id = LongMessageID.generate();
 
     private ID cid;
 
     private Long sequence = SEQUENCE_GENERATOR.getAndIncrement();
 
     private long timestamp = System.currentTimeMillis();
 
     protected BaseMessage() {}
 
     public int hashCode() {
         return getId().hashCode();
     }
 
     public String toString() {
         return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
     }
 
     public ID getId() {
         return id;
     }
     
     public ID getCorrelationId() {
         return cid;
     }
 
     public void setCorrelationId(final ID id) {
        assert id != null;
         
         this.cid = id;
     }
 
     public long getTimestamp() {
         return timestamp;
     }
 
     public long getSequence() {
         return sequence;
     }
 }
