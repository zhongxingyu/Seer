 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.test.sb;
 
 // Required due to use of URL class , required by Call class
 import gov.loc.www.zing.srw.ExplainRequestType;
 import gov.loc.www.zing.srw.service.ExplainPort;
 import gov.loc.www.zing.srw.service.SRWPort;
 import gov.loc.www.zing.srw.service.SRWSampleServiceLocator;
 
 import java.net.URL;
 
 import junit.framework.TestCase;
 
 import org.apache.axis.EngineConfiguration;
 import org.apache.axis.configuration.FileProvider;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import de.escidoc.core.test.common.logger.AppLogger;
 
 /**
  * @author ROF
  * 
  */
 public class SrwRequestTest extends TestCase {
 
     protected static AppLogger log =
         new AppLogger(SrwRequestTest.class.getName());
 
     protected SRWPort srwService;
 
     protected ExplainPort explainService;
 
     private final String location =
         "http://localhost:8080/srw/search/escidoc_all";
 
     // private String location =
     // "http://beta-tc.fiz-karlsruhe.de/srw/search/tc";
     @Before
    public void initialize() throws Exception {
 
         EngineConfiguration config = new FileProvider("client.wsdd");
 
         SRWSampleServiceLocator service = new SRWSampleServiceLocator();
         URL url = new URL(location);
         srwService = service.getSRW(url);
         explainService = service.getExplainSOAP(url);
     }
 
     /**
      * The method tests the Sb service SRW-Search by using http-request.
      * 
      * @throws Exception
      *             any exception
      */
     @Ignore
     public void notestSearchByRest() throws Exception {
         for (int i = 0; i < 1; i++) {
             HttpRequester requester = new HttpRequester(location);
             String response = "";
             response =
                 requester
                     .doGet("?query=escidoc.objid%3Descidoc:345&recordPacking=string");
 
         }
     }
 
     /**
      * The method tests the SRW-Search by using http-request.
      * 
      * @throws Exception
      *             any exception
      */
     @Ignore
     public void notestSearchBySoapRequest() throws Exception {
         String soapPost =
             "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                 + "<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                 + "<SOAP:Body>"
                 + "<ExplainSOAP:ExplainOperation xmlns:ExplainSOAP=\"http://www.log.gov/zing/srw/\">"
                 + "</ExplainSOAP:ExplainOperation>" + "</SOAP:Body>"
                 + "</SOAP:Envelope>";
         HttpRequester requester = new HttpRequester(location, "mih:11311");
         if (log.isDebugEnabled()) {
             log.debug(requester.doPost("", soapPost));
         }
     }
 
     /**
      * The method tests the Sb service SRW-Search by using SOAP.
      * 
      * @throws Exception
      *             any exception
      */
     @Test
     public void testSearchBySoap() throws Exception {
         // SearchRetrieveRequestType request = new SearchRetrieveRequestType();
         // request.setQuery("escidoc.objid=escidoc:5301");
         // request.setVersion("1.1");
         // request.setRecordPacking("xml");
         // request.setMaximumRecords(new NonNegativeInteger("20"));
         // //request.setStartRecord(new PositiveInteger("1"));
         // SearchRetrieveResponseType response =
         // srwService.searchRetrieveOperation(request);
         ExplainRequestType request = new ExplainRequestType();
         request.setVersion("1.1");
         if (log.isDebugEnabled()) {
             log.debug(explainService.explainOperation(request).getVersion());
         }
     }
 
 }
