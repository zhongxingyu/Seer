 /*
  * Copyright 2006 Luca Garulli (luca.garulli--at--assetdata.it)
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
 
 package org.romaframework.core.config;
 
 /**
  * Represent a generic service can be started and stopped. The service owns also a status.
  * 
  * @author Luca
  * 
  */
 public interface Serviceable {
 	public void startup() throws RuntimeException;
 
 	public void shutdown() throws RuntimeException;
 
 	public String getStatus();
 
 	public static final String	STATUS_UNKNOWN			= "unknown";
 	public static final String	STATUS_DOWN					= "down";
 	public static final String	STATUS_STARTING			= "starting";
 	public static final String	STATUS_UP						= "up";
	public static final String	STATUS_SHUTTING_DOWN	= "shutting down";
 }
