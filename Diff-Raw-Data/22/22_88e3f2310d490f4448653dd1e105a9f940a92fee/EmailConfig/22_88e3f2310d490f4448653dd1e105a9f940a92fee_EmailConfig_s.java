 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements. See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.manifoldcf.crawler.connectors.email;
 
 
 /**
  * Parameters data for the Email repository connector.
  */
 public class EmailConfig {
 
     /**
      * Username
      */
     public static final String USERNAME_PARAM = "username";
 
     /**
      * Password
      */
     public static final String PASSWORD_PARAM = "password";
 
     /**
      * Protocol
      */
     public static final String PROTOCOL_PARAM = "protocol";
 
     /**
      * Server name
      */
    public static final String SERVER_PARAM = "hostserverurl";
 
     /**
      * Port
      */
     public static final String PORT_PARAM = "port";
 
     /**
      * Properties
      */
     public static final String PROPERTIES_PARAM = "properties";
 
     public static final String PROTOCOL_DEFAULT_VALUE = "SMTP";
     public static final String PORT_DEFAULT_VALUE = "25";
 
 
     public static final String NODE_PROPERTIES = "properties";
     public static final String SERVER_PROPERTY = "serverproperty";
     public static final String VALUE = "p_value";
 }
