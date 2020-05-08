 /*
  *
  **************************************************************************************************************
  * Copyright (C) 2013 by Cognitive Medical Systems, Inc
  * (http://www.cognitivemedciine.com) * * Licensed under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in compliance *
  * with the License. You may obtain a copy of the License at * *
  * http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable
  * law or agreed to in writing, software distributed under the License is *
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. * See the License for the specific language
  * governing permissions and limitations under the License. *
  **************************************************************************************************************
  *
  **************************************************************************************************************
  * Socratic Grid contains components to which third party terms apply. To comply
  * with these terms, the following * notice is provided: * * TERMS AND
  * CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION * Copyright (c) 2008,
  * Nationwide Health Information Network (NHIN) Connect. All rights reserved. *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that * the following conditions are met:
  * - Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the *     following disclaimer. * - Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the *     following disclaimer in the documentation and/or
  * other materials provided with the distribution. * - Neither the name of the
  * NHIN Connect Project nor the names of its contributors may be used to endorse
  * or *     promote products derived from this software without specific prior
  * written permission. * * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS
  * AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED * WARRANTIES, INCLUDING,
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
  * OR CONTRIBUTORS BE LIABLE FOR * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, *
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION HOWEVER * CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, * EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. * * END OF TERMS AND CONDITIONS *
  **************************************************************************************************************
  */
 package org.socraticgrid.mdwslib;
 
 import gov.va.medora.mdws.emrsvc.ArrayOfTaggedText;
 import gov.va.medora.mdws.emrsvc.DataSourceArray;
 import gov.va.medora.mdws.emrsvc.EmrSvc;
 import gov.va.medora.mdws.emrsvc.EmrSvcSoap;
 import gov.va.medora.mdws.emrsvc.PatientTO;
 import gov.va.medora.mdws.emrsvc.TaggedText;
 import gov.va.medora.mdws.emrsvc.TaggedTextArray;
 import gov.va.medora.mdws.emrsvc.UserTO;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 
 import java.net.URL;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.BindingProvider;
 
 
 /**
  *
  * @author Jerry Goodnough
  */
 public class GetNhinDataSource
     implements org.socraticgrid.patientdataservices.DataSource
 {
 
     private static Logger logger = Logger.getLogger(GetNhinDataSource.class
             .getName());
     private static String returnStart =
         "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
         "<TaggedTextArray xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://mdws.medora.va.gov/EmrSvc\">" +
         "<count>";
     private long maximumConnectionLifeTime = 1000 * 300;
     private Map<String, String> domainRemap;
     private boolean useMappedDomainsOnly = false;
     private String password;
     private String userId;
     private String mdwsEndpoint;
     private String siteId;
     private ServiceInfo serviceInstance;
 
     /**
      * Get the value of maximumConnectionLifeTime in Milliseconds defaults to 5
      * Minutes (1000 * 60 * 5 = 300000)
      *
      * @return the value of maximumConnectionLifeTime
      */
     public long getMaximumConnectionLifeTime()
     {
         return maximumConnectionLifeTime;
     }
 
     /**
      * Set the value of maximumConnectionLifeTime in Milliseconds
      *
      * @param maximumConnectionLifeTime new value of maximumConnectionLifeTime
      */
     public void setMaximumConnectionLifeTime(long maximumConnectionLifeTime)
     {
         this.maximumConnectionLifeTime = maximumConnectionLifeTime;
     }
 
     /**
      * Get the value of domainRemap. This map is used to allow external domain
      * names to be changed into ones that are recognized by MDWS. If the
      * property useMappedDomainsOnly is true this map is also used to filter
      * domain requests
      *
      * @return the value of domainRemap
      */
     public Map<String, String> getDomainRemap()
     {
         return domainRemap;
     }
 
     /**
      * Set the value of domainRemap, this map is used to allow external domain
      * names to be changed into ones that are recognized by MDWS. If the
      * property useMappedDomainsOnly is true this map is also used to filter
      * domain requests
      *
      * @param domainRemap new value of domainRemap
      */
     public void setDomainRemap(Map<String, String> domainRemap)
     {
         this.domainRemap = domainRemap;
     }
 
     /**
      * Get the value of useMappedDomainsOnly. When true only domain names set in the DomainRemap will be allowed.
      *
      * @return the value of useMappedDomainsOnly
      */
     public boolean isUseMappedDomains()
     {
         return useMappedDomainsOnly;
     }
 
     /**
      * Set the value of useMappedDomainsOnly
      *
      * @param useMappedDomainsOnly new value of useMappedDomainsOnly
      */
     public void setUseMappedDomainsOnly(boolean useMappedDomainsOnly)
     {
         this.useMappedDomainsOnly = useMappedDomainsOnly;
     }
 
     /**
      * Get the value of password
      *
      * @return the value of password
      */
     public String getPassword()
     {
         return password;
     }
 
     /**
      * Set the value of password
      *
      * @param password new value of password
      */
     public void setPassword(String password)
     {
         this.password = password;
     }
 
     /**
      * Get the value of userId
      *
      * @return the value of userId
      */
     public String getUserId()
     {
         return userId;
     }
 
     /**
      * Set the value of userId
      *
      * @param userId new value of userId
      */
     public void setUserId(String userId)
     {
         this.userId = userId;
     }
 
     /**
      * Get the value of mdwsEndpoint
      *
      * @return the value of mdwsEndpoint
      */
     public String getMdwsEndpoint()
     {
         return mdwsEndpoint;
     }
 
     /**
      * Set the value of mdwsEndpoint
      *
      * @param mdwsEndpoint new value of mdwsEndpoint
      */
     public void setMdwsEndpoint(String mdwsEndpoint)
     {
         this.mdwsEndpoint = mdwsEndpoint;
     }
 
     /**
      * Get the value of siteId
      *
      * @return the value of siteId
      */
     public String getSiteId()
     {
         return siteId;
     }
 
     /**
      * Set the value of siteId
      *
      * @param siteId new value of siteId
      */
     public void setSiteId(String siteId)
     {
         this.siteId = siteId;
     }
 
     /**
      *
      * @param domain
      * @return
      */
     @Override
     public boolean isDomainSupported(String domain)
     {
 
         if (this.useMappedDomainsOnly)
         {
 
             if (this.domainRemap != null)
             {
                 return domainRemap.containsKey(domain);
             }
             else
             {
                 logger.warning(
                     "GetNHinDataSource bean is configured to require mapped domains, but no mappings exist.");
 
                 return false;
             }
         }
         else
         {
             return true;
         }
     }
 
     private ServiceInfo getService(Properties props)
     {
 
         if (isServiceStale(props))
         {
             serviceInstance = createService(props);
 
         }
 
         return serviceInstance;
     }
 
     private ServiceInfo createService(Properties props)
     {
         ServiceInfo srv = new ServiceInfo();
 
 
         try
         {
             URL url = GetNhinDataSource.class.getResource(
                     "/META-INF/wsdl/EmrSvc.asmx.wsdl");
             EmrSvc emrService = new EmrSvc(url,
                     new QName("http://mdws.medora.va.gov/EmrSvc", "EmrSvc"));
             srv.service = emrService.getEmrSvcSoap();
         }
         catch (Exception e)
         {
             logger.severe(e.getMessage());
 
             return null;
         }
         // Create the service
         // EmrSvc emrService = new EmrSvc();
         // Get the Soap Endpoint
 
         ((BindingProvider) srv.service).getRequestContext().put(
             BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
 
         // Bind the Address
         ((BindingProvider) srv.service).getRequestContext().put(
             BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.mdwsEndpoint);
 
 
         // Set up user and password
         if ((props != null) && (props.containsKey("MDWSUserId")))
         {
             srv.userId = props.getProperty("MDWSUserId");
         }
         else
         {
             srv.userId = this.userId;
         }
 
         if ((props != null) && (props.containsKey("MDWSPassword")))
         {
             srv.password = props.getProperty("MDWSPassword");
         }
         else
         {
             srv.password = this.password;
         }
 
         if (this.loginToSite(srv))
         {
             return srv;
         }
         else
         {
             logger.warning("Failed MDWS login on endpoint " +
                 this.mdwsEndpoint + " for user " + srv.userId);
 
             return null;
         }
     }
 
     private boolean isServiceStale(Properties props)
     {
 
         if (serviceInstance != null)
         {
 
             // First we check the time out
             if ((System.currentTimeMillis() - serviceInstance.lastUsed) >
                     maximumConnectionLifeTime)
             {
                 return true;
             }
             else
             {
                if (props != null)
                 {
                    // OK Service has been around long enough
                    if (props.containsKey("MDWSUserId"))
                     {
                        String propPass = props.getProperty("MDWSUserId");

                        if (propPass.compareTo(serviceInstance.userId) != 0)
                        {
                            return true;
                        }
                     }
                 }
             }
         }
         else
         {
 
             return true;
         }
 
         return false;
     }
 
     /**
      *
      * @param domain
      * @param id
      * @param props MDWSUserId may be passed to change the user Id, MDWSPassword
      * may be used to set the password
      * @return
      */
     public InputStream getData(String domain, String id, Properties props)
     {
 
 
         StringBuilder sb = null;
 
 
         ServiceInfo srvInfo = getService(props);
 
         if (srvInfo != null)
         {
 
             // Check if we need to select the patient
             if (srvInfo.id.compareTo(id) != 0)
             {
 
                 PatientTO selectedPatient = srvInfo.service.select(id);
 
                 if (selectedPatient.getFault() != null)
                 {
 
                     // We can't select the requested patient
                     logger.warning("MDWS SELECT: PatientId " + id +
                         " not found. Message = " +
                         selectedPatient.getFault().getMessage());
                     srvInfo.id = "";
                 }
                 else
                 {
                     srvInfo.id = id;
                     srvInfo.lastUsed = System.currentTimeMillis();
                 }
             }
 
             if (!srvInfo.id.isEmpty())
             {
 
                 String targetDomain = getTargetDomain(domain);
 
                 if (targetDomain != null)
                 {
                     sb = new StringBuilder();
 
                     TaggedTextArray foundData = srvInfo.service.getNhinData(
                             targetDomain);
                     logger.fine("TOTAL foundData= " + foundData.getCount());
 
                     ArrayOfTaggedText foundDataArray = foundData.getResults();
                     List<TaggedText> foundDataList =
                         foundDataArray.getTaggedText();
 
                     sb.append(returnStart);
                     sb.append(Integer.toString(foundData.getCount()));
                     sb.append("</count><results>");
                     sb.append("<TaggedText><tag>");
                     sb.append(siteId);
                     sb.append("</tag><text>");
 
                     for (TaggedText tag : foundDataList)
                     {
                         sb.append(tag.getText());
                     }
 
                     sb.append(
                         "</text></TaggedText></results></TaggedTextArray>");
                     srvInfo.lastUsed = System.currentTimeMillis();
                 }
                 else
                 {
                     logger.warning("Invalid Domain passed, domain = " + domain);
                 }
             }
 
         }
 
 
         if (sb == null)
         {
 
             sb = new StringBuilder();
             sb.append(returnStart);
             sb.append("0");
 
             sb.append("</count><results>");
             sb.append("<TaggedText><tag>");
             sb.append(siteId);
             sb.append("</tag><text>");
             sb.append("</text></TaggedText></results></TaggedTextArray>");
 
         }
 
         return new ByteArrayInputStream(sb.toString().getBytes());
     }
 
     private String getTargetDomain(String domain)
     {
         String targetDomain = null;
 
         if (this.useMappedDomainsOnly)
         {
 
             if (this.domainRemap.containsKey(domain))
             {
                 targetDomain = (String) this.domainRemap.get(domain);
             }
             else
             {
                 logger.warning(domain + " is not mapped to a MDWS domain");
             }
         }
         else if (this.domainRemap != null)
         {
 
             if (this.domainRemap.containsKey(domain))
             {
                 targetDomain = (String) this.domainRemap.get(domain);
             }
             else
             {
                 targetDomain = domain;
             }
         }
         else
         {
             targetDomain = domain;
         }
 
         return targetDomain;
     }
 
     private boolean loginToSite(ServiceInfo serviceInfo)
     {
 
         // -------
         // CONNECT
         // -------
         DataSourceArray dataSourceArray = serviceInfo.service.connect(siteId);
 
 
         if (dataSourceArray.getFault() == null)
         {
             logger.fine("MDWS CONNECT successful");
         }
         else
         {
             logger.warning("MDWS CONNECT failed with: " +
                 dataSourceArray.getFault().getStackTrace());
 
             return false;
         }
 
         // -------
         // LOGIN
         // -------
         UserTO userTO = serviceInfo.service.login(serviceInfo.userId,
                 serviceInfo.password, "");
 
         logger.fine("userTO.getName(): " + userTO.getName());
 
         if (userTO.getFault() == null)
         {
 
             logger.fine("Login successful");
 
         }
         else
         {
             logger.warning("MDWS LOGIN failed with: " +
                 userTO.getFault().getInnerMessage());
 
             return false;
         }
 
         return true;
     }
 
     private class ServiceInfo
     {
 
         public EmrSvcSoap service;
         public long lastUsed = 0;
         public String userId;
         public String password;
         public String id = "";
     }
 }
