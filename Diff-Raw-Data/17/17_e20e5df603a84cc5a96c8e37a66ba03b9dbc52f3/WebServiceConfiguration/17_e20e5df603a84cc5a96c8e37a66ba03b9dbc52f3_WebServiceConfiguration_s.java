 /*
  * ====================
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 2011 Tirasa. All rights reserved.
  *
  * The contents of this file are subject to the terms of the Common Development
  * and Distribution License("CDDL") (the "License").  You may not use this file
  * except in compliance with the License.
  *
  * You can obtain a copy of the License at
  * http://connid.googlecode.com/svn/trunk/legal/license.txt
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * When distributing the Covered Code, include this CDDL Header Notice in each
  * file and include the License file at connid/legal/license.txt.
  * If applicable, add the following below this CDDL Header, with the fields
  * enclosed by brackets [] replaced by your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  * ====================
  */
 package org.connid.bundles.soap;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import org.identityconnectors.framework.spi.AbstractConfiguration;
 import org.identityconnectors.framework.spi.ConfigurationProperty;
 import org.identityconnectors.common.StringUtil;
 
 /**
  * Extends the {@link AbstractConfiguration} class to provide all the necessary parameters to initialize the WebService
  * Connector.
  */
 public class WebServiceConfiguration extends AbstractConfiguration {
 
     /*
      * Web Service Endpoint.
      */
     private String endpoint = null;
 
     /*
      * Public Web Service interface class
      */
     private String servicename = null;
 
     /*
      * Connection timeout
      */
     private String connectionTimeout = "30";
 
     /*
      * Receive timeout
      */
     private String receiveTimeout = "60";
 
     /*
      * Receive timeout
      */
     private String soapActionUriPrefix = null;
 
     /**
      * Accessor for the example property. Uses ConfigurationProperty annotation to provide property metadata to the
      * application.
      */
     @ConfigurationProperty(displayMessageKey = "ENDPOINT_DISPLAY",
     helpMessageKey = "ENDPOINT_HELP",
    confidential = false)
     public String getEndpoint() {
         return endpoint;
     }
 
     /**
      * Setter for the example property.
      */
     public void setEndpoint(String endpoint) {
         this.endpoint = endpoint;
     }
 
     /**
      * Accessor for the example property. Uses ConfigurationProperty annotation to provide property metadata to the
      * application.
      */
     @ConfigurationProperty(displayMessageKey = "CLASSNAME_DISPLAY",
    helpMessageKey = "CLASSNAME_HELP", confidential = false, order = 1)
     public String getServicename() {
         return servicename;
     }
 
     public void setServicename(String classname) {
         this.servicename = classname;
     }
 
     @ConfigurationProperty(displayMessageKey = "CONNECTIONTIMEOUT_DISPLAY",
    helpMessageKey = "CONNECTIONTIMEOUT_HELP", confidential = false, order = 2)
     public String getConnectionTimeout() {
         return connectionTimeout;
     }
 
     public void setConnectionTimeout(String connectionTimeout) {
         this.connectionTimeout = connectionTimeout;
     }
 
     @ConfigurationProperty(displayMessageKey = "RECEIVETIMEOUT_DISPLAY",
    helpMessageKey = "RECEIVETIMEOUT_HELP", confidential = false, order = 3)
     public String getReceiveTimeout() {
         return receiveTimeout;
     }
 
     public void setReceiveTimeout(String receiveTimeout) {
         this.receiveTimeout = receiveTimeout;
     }
 
     @ConfigurationProperty(displayMessageKey = "SOAPACTION_DISPLAY",
    helpMessageKey = "SOAPACTION_HELP", confidential = false, order = 4)
     public String getSoapActionUriPrefix() {
         return soapActionUriPrefix;
     }
 
     public void setSoapActionUriPrefix(String soapActionUriPrefix) {
         this.soapActionUriPrefix = soapActionUriPrefix;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate() {
         // Check if endpoint has been specified.
         if (StringUtil.isBlank(endpoint)) {
             throw new IllegalArgumentException(
                     "Endpoint cannot be null or empty.");
         }
 
         // Check if servicename has been specified.
         if (StringUtil.isBlank(servicename)) {
             throw new IllegalArgumentException(
                     "Service name cannot be null or empty.");
         }
 
         // Check if servicename has been specified.
         if (StringUtil.isBlank(connectionTimeout)) {
             connectionTimeout = "30";
         }
 
         try {
             Long.parseLong(connectionTimeout);
         } catch (NumberFormatException e) {
             throw new IllegalArgumentException(
                     "The specified connection timeout is not valid.");
         }
 
         if (StringUtil.isBlank(receiveTimeout)) {
             receiveTimeout = "60";
         }
 
         try {
             Long.parseLong(receiveTimeout);
         } catch (NumberFormatException e) {
             throw new IllegalArgumentException(
                     "The specified receive timeout is not valid.");
         }
 
         try {
            // Check if the specified enpoint is a well-formed URL.
             new URL(endpoint);
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException(
                     "The specified endpoint is not a valid URL.");
         }
     }
 }
