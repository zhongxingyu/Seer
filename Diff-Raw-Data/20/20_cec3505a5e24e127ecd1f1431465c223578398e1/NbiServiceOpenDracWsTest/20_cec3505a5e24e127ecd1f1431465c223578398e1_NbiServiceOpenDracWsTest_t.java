 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.service;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasSize;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.nbi.NetworkMonitoringServiceFault;
import nl.surfnet.bod.nbi.NetworkMonitoringService_v30Stub;
 
 import org.apache.xmlbeans.XmlException;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.SecurityDocument;
 import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointRequestDocument;
 import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointResponseDocument;
 import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsRequestDocument;
 import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsResponseDocument;
 
 @RunWith(MockitoJUnitRunner.class)
 public class NbiServiceOpenDracWsTest {
 
   @InjectMocks
   private NbiServiceOpenDracWs subject;
 
   @Mock
   private NetworkMonitoringService_v30Stub networkingServiceMock;
 
   @Before
   public void init() {
     subject.setEncryptedPassword("292c2cdcb5f669a8");
   }
 
   @Test
   public void findAllPhysicalPorts() throws XmlException, NetworkMonitoringServiceFault, IOException {
     QueryEndpointsResponseDocument endpointsResponse = QueryEndpointsResponseDocument.Factory.parse(new File(
         "src/test/resources/opendrac/queryEndpointsResponse.xml"));
     QueryEndpointResponseDocument endpointResponse = QueryEndpointResponseDocument.Factory.parse(new File(
         "src/test/resources/opendrac/queryEndpointResponse.xml"));
 
     when(networkingServiceMock.queryEndpoints(any(QueryEndpointsRequestDocument.class), any(SecurityDocument.class)))
         .thenReturn(endpointsResponse);
     when(networkingServiceMock.queryEndpoint(any(QueryEndpointRequestDocument.class), any(SecurityDocument.class)))
         .thenReturn(endpointResponse);
 
     List<PhysicalPort> ports = subject.findAllPhysicalPorts();
 
     assertThat(ports, hasSize(1));
   }
 
 }
