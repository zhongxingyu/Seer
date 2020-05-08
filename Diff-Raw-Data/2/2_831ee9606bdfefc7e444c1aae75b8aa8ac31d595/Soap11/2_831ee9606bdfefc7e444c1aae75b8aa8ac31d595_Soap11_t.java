 /**
  * 
  * Copyright 2004 Protique Ltd
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License. 
  * 
  **/
 package org.codehaus.xfire.soap;
 
 import javax.xml.namespace.QName;
 
 /**
  * Represents the SOAP 1.2 version
  * 
  * @version $Revision$
  */
 public class Soap11 implements SoapVersion
 {
    private static final Soap11 instance = new Soap11();
 
     private final double version = 1.1;
 
     private final String namespace = "http://schemas.xmlsoap.org/soap/envelope/";
 
     private final String prefix = "env";
 
     private final String noneRole = namespace + "/role/none";
 
     private final String ultimateReceiverRole = namespace + "/role/ultimateReceiver";
 
     private final String nextRole = namespace + "/role/next";
 
     private final String soapEncodingStyle = "http://schemas.xmlsoap.org/soap/encoding/";
 
     private final QName envelope = new QName(namespace, "Envelope", prefix);
 
     private final QName header = new QName(namespace, "Header", prefix);
 
     private final QName body = new QName(namespace, "Body", prefix);
 
     public static Soap11 getInstance()
     {
         return instance;
     }
 
     public double getVersion()
     {
         return version;
     }
 
     public String getNamespace()
     {
         return namespace;
     }
 
     public String getPrefix()
     {
         return prefix;
     }
 
     public QName getEnvelope()
     {
         return envelope;
     }
 
     public QName getHeader()
     {
         return header;
     }
 
     public QName getBody()
     {
         return body;
     }
 
     public String getSoapEncodingStyle()
     {
         return soapEncodingStyle;
     }
 
     // Role URIs
     // -------------------------------------------------------------------------
     public String getNoneRole()
     {
         return noneRole;
     }
 
     public String getUltimateReceiverRole()
     {
         return ultimateReceiverRole;
     }
 
     public String getNextRole()
     {
         return nextRole;
     }
 }
