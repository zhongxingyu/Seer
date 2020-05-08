 package org.apache.ws.policy;
 /*
  * Copyright 2001-2004 The Apache Software Foundation.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 /**
  * WSPConstants interfaces defines some CONST VALUES that are used in the 
  * entier framework.
  * 
  */
 public interface PolicyConstants {
     
     /** */
     public static final String AND_COMPOSITE_ASSERTION = "All";
     
     /** */
     public static final String XOR_COMPOSITE_ASSERTION = "ExactlyOne";
     
     /** */
     public static final String WS_POLICY = "Policy";
     
     /** */
     public static final String WS_POLICY_PREFIX = "wsp";
     
     /** */
    public static final String WSU_NAMESPACE_PREFIX = "wsu";
    
    /** */
     public static final String WS_POLICY_REFERENCE = "PolicyReference";
     
     /** */
     public static final String WS_POLICY_NAMESPACE_URI = "http://schemas.xmlsoap.org/ws/2004/09/policy";
     
     /** */
     public static final String WSU_NAMESPACE_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
  
     /** */
     public static final String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
     
 }
