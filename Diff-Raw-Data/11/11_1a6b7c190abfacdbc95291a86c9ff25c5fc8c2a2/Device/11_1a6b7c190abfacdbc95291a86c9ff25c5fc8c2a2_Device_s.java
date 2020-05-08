 /**
  * Copyright 2012 Alex Jones
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  *
  * @author unclealex72
  *
  */
 
 package uk.co.unclealex.music.configuration;
 
 import org.hibernate.validator.constraints.NotEmpty;
 
 import uk.co.unclealex.music.configuration.json.CowonX7DeviceBean;
 import uk.co.unclealex.music.configuration.json.FileSystemDeviceBean;
 import uk.co.unclealex.music.configuration.json.IpodDeviceBean;
 
 import com.fasterxml.jackson.annotation.JsonSubTypes;
 import com.fasterxml.jackson.annotation.JsonTypeInfo;
 import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
 
 /**
  * An interface that represents a type of external music device.
  * 
  * @author alex
  * 
  */
 @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "type")
 @JsonSubTypes({
     @JsonSubTypes.Type(value = IpodDeviceBean.class, name = "ipod"),
     @JsonSubTypes.Type(value = CowonX7DeviceBean.class, name = "x7"),
     @JsonSubTypes.Type(value = FileSystemDeviceBean.class, name = "hd") })
 public interface Device {
 
   /**
    * Get the name of this device.
    * 
    * @return The name of this device.
    */
   @NotEmpty
   public String getName();
 
   /**
    * Get the unique UUID of this device. The device will have a symbolic link at
    * <code>/dev/disks/by-uuid/UUID</code> that points to where the device is
    * located.
    * 
    * @return The unique UUID of this device.
    */
   public String getUuid();
 
   /**
    * Accept a {@link DeviceVisitor}
    * 
    * @param deviceVisitor
    *          The device visitor to accept.
    * @return The value returned by the {@link DeviceVisitor}s
    *         <code>visit()</code> method.
    */
   public <R> R accept(DeviceVisitor<R> deviceVisitor);
 
 }
