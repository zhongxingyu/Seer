 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.nsi.v2;
 
 import static nl.surfnet.bod.nsi.NsiHelper.generateCorrelationId;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.hamcrest.Matchers.nullValue;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.XMLConstants;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.Endpoint;
 import javax.xml.ws.Holder;
 import javax.xml.ws.handler.MessageContext;
 
import com.google.common.base.Joiner;

 import nl.surfnet.bod.domain.NsiVersion;
 import nl.surfnet.bod.nsi.NsiHelper;
 import nl.surfnet.bod.nsi.v2.SoapReplyListener.Message;
 import nl.surfnet.bod.service.DatabaseTestHelper;
 import nl.surfnet.bod.support.BodManagerWebDriver;
 import nl.surfnet.bod.support.BodWebDriver;
 import nl.surfnet.bod.support.SeleniumWithSingleSetup;
 import nl.surfnet.bod.util.XmlUtils;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.ogf.schemas.nsi._2013._07.connection.provider.ConnectionProviderPort;
 import org.ogf.schemas.nsi._2013._07.connection.provider.ConnectionServiceProvider;
 import org.ogf.schemas.nsi._2013._07.connection.provider.ServiceException;
 import org.ogf.schemas.nsi._2013._07.connection.types.GenericConfirmedType;
 import org.ogf.schemas.nsi._2013._07.connection.types.ProvisionStateEnumType;
 import org.ogf.schemas.nsi._2013._07.connection.types.QuerySummaryConfirmedType;
 import org.ogf.schemas.nsi._2013._07.connection.types.QuerySummaryResultType;
 import org.ogf.schemas.nsi._2013._07.connection.types.ReservationRequestCriteriaType;
 import org.ogf.schemas.nsi._2013._07.connection.types.ReservationStateEnumType;
 import org.ogf.schemas.nsi._2013._07.connection.types.ReserveConfirmedType;
 import org.ogf.schemas.nsi._2013._07.connection.types.ScheduleType;
 import org.ogf.schemas.nsi._2013._07.framework.headers.CommonHeaderType;
 import org.ogf.schemas.nsi._2013._07.services.point2point.EthernetVlanType;
 import org.ogf.schemas.nsi._2013._07.services.types.DirectionalityType;
 import org.ogf.schemas.nsi._2013._07.services.types.StpType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.io.ClassPathResource;
 
 public class NsiV2ReservationTestSelenium extends SeleniumWithSingleSetup {
 
   private static final Logger LOG = LoggerFactory.getLogger(NsiV2ReservationTestSelenium.class);
 
   private static final String WSDL_LOCATION = "/wsdl/2.0/ogf_nsi_connection_provider_v2_0.wsdl";
   private static final String ENDPOINT = BodWebDriver.URL_UNDER_TEST + "/nsi/v2/provider";
 
   private static final int REPLY_SERVER_PORT = 31337;
   private static final String REPLY_ADDRESS = "http://localhost:" + REPLY_SERVER_PORT + "/requester";
 
   private SoapReplyListener soapReplyListener;
   private Endpoint soapReplyListenerEndpoint;
   private DateTime startTime;
   private DateTime endTime;
   private ConnectionProviderPort connectionServiceProviderPort;
   private List<String> virtualPortIds;
   private StpType sourceStp;
   private StpType destStp;
 
   private NsiHelper nsiHelper = new NsiHelper("surfnet.nl", "surfnet.nl:1990", "urn:nl:surfnet:diensten:bod");
   private ConnectionsV2 connectionsV2 = new ConnectionsV2(nsiHelper);
 
   @Override
   public void setupInitialData() {
     getNocDriver().createNewApiBasedPhysicalResourceGroup(GROUP_SURFNET, ICT_MANAGERS_GROUP, "test@example.com");
     getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, "First port", GROUP_SURFNET);
     getNocDriver().linkPhysicalPort(NMS_PORT_ID_2, "Second port", GROUP_SURFNET);
 
     getWebDriver().clickLinkInLastEmail();
 
     getUserDriver().requestVirtualPort("Selenium users");
     getUserDriver().selectInstituteAndRequest(GROUP_SURFNET, 1200, "port 1");
     getWebDriver().clickLinkInLastEmail();
     getManagerDriver().createVirtualPort("First port");
 
     getUserDriver().requestVirtualPort("Selenium users");
     getUserDriver().selectInstituteAndRequest(GROUP_SURFNET, 1200, "port 2");
     getWebDriver().clickLinkInLastEmail();
     getManagerDriver().createVirtualPort("Second port");
   }
 
   @Before
   public void setUp() throws Exception {
     soapReplyListener = new SoapReplyListener();
     soapReplyListenerEndpoint = Endpoint.publish(REPLY_ADDRESS, soapReplyListener);
 
     startTime = DateTime.now().plusHours(1);
     endTime = DateTime.now();
     String oauthToken = "f00f";
 
     connectionServiceProviderPort = createPort(oauthToken);
     virtualPortIds = getUserDriver().getVirtualPortIds(NsiVersion.TWO);
 
     System.err.println("!!! vpIds " + virtualPortIds);
 
    String ids = Joiner.on("-").join(virtualPortIds);
 
    getWebDriver().takeScreenshot(new File(new File("target/selenium/screenshots"), "debug-" + ids + ".png"));
 
     assertTrue("We need at least two portDefinitions to be able to continue", virtualPortIds.size() >= 2);
     sourceStp = connectionsV2.toStpType(virtualPortIds.get(0));
     destStp = connectionsV2.toStpType(virtualPortIds.get(1));
   }
 
   @After
   public void tearDown() {
     soapReplyListenerEndpoint.stop();
     soapReplyListener = null;
   }
 
   @After
   public void dropReservations() {
     DatabaseTestHelper.deleteReservationsFromSeleniumDatabase();
   }
 
   @Test
   public void reserveAndProvision() throws Exception {
     String description = "NSI v2 Reservation";
     String globalReservationId = nsiHelper.generateGlobalReservationId();
     ReservationRequestCriteriaType criteria = new ReservationRequestCriteriaType()
       .withSchedule(new ScheduleType()
         .withStartTime(XmlUtils.toGregorianCalendar(startTime))
         .withEndTime(XmlUtils.toGregorianCalendar(endTime)))
       .withServiceType("http://services.ogf.org/nsi/2013/07/descriptions/EVTS.A-GOLE");
     System.err.println("!!!! source " + sourceStp);
     System.err.println("!!!! dest " + destStp);
     ConnectionsV2.addPointToPointService(criteria.getAny(), new EthernetVlanType()
         .withCapacity(100)
         .withDirectionality(DirectionalityType.BIDIRECTIONAL)
         .withSourceSTP(sourceStp).withSourceVLAN(BodManagerWebDriver.DEFAULT_VIRTUAL_PORT_VLAN_ID)
         .withDestSTP(destStp).withDestVLAN(BodManagerWebDriver.DEFAULT_VIRTUAL_PORT_VLAN_ID));
 
     // Initial reserve
     String reserveCorrelationId = generateCorrelationId();
     Holder<String> connectionId = new Holder<>(null);
     connectionServiceProviderPort.reserve(connectionId, globalReservationId, description, criteria, createHeader(reserveCorrelationId));
     assertThat(connectionId.value, is(notNullValue()));
 
     Message<ReserveConfirmedType> reserveConfirmed = soapReplyListener.getLastReply(Converters.RESERVE_CONFIRMED_CONVERTER);
     assertThat("reserve confirmed correlation id", reserveConfirmed.header.getCorrelationId(), is(reserveCorrelationId));
     assertThat("connection id must match", reserveConfirmed.body.getConnectionId(), is(connectionId.value));
     assertThat("global reservation id must match", reserveConfirmed.body.getGlobalReservationId(), is(globalReservationId));
 
 
     // Reserve commit
     String reserveCommitCorrelationId = generateCorrelationId();
     connectionServiceProviderPort.reserveCommit(connectionId.value, createHeader(reserveCommitCorrelationId));
 
     Message<GenericConfirmedType> reserveCommitConfirmed = soapReplyListener.getLastReply(Converters.RESERVE_COMMIT_CONFIRMED_CONVERTER);
     assertThat("reserve commit confirmed correlation id", reserveCommitConfirmed.header.getCorrelationId(), is(reserveCommitCorrelationId));
 
 
     // perform query, assert that reservation_state == reserve_start
     connectionServiceProviderPort.querySummary(Arrays.asList(connectionId.value), Collections.<String>emptyList(), createHeader(generateCorrelationId()));
     Message<QuerySummaryConfirmedType> querySummaryConfirmed = soapReplyListener.getLastReply(Converters.QUERY_SUMMARY_CONFIRMED_CONVERTER);
 
     List<QuerySummaryResultType> result = querySummaryConfirmed.body.getReservation();
     assertThat(result, hasSize(1));
     assertThat(result.get(0).getConnectionStates().getReservationState(), is(ReservationStateEnumType.RESERVE_START));
 
 
     // do a provision, assert that we receive a provisionConfirmed message
     String provisionCorrelationId = generateCorrelationId();
     connectionServiceProviderPort.provision(connectionId.value, createHeader(provisionCorrelationId));
     Message<GenericConfirmedType> provisionConfirmed = soapReplyListener.getLastReply(Converters.PROVISION_CONFIRMED_CONVERTER);
     assertThat("provision confirmed correlation id", provisionConfirmed.header.getCorrelationId(), is(provisionCorrelationId));
 
 
     // query again, see that provisionState is now 'provisioned'
     connectionServiceProviderPort.querySummary(Arrays.asList(connectionId.value), Collections.<String>emptyList(), createHeader(generateCorrelationId()));
     querySummaryConfirmed = soapReplyListener.getLastReply(Converters.QUERY_SUMMARY_CONFIRMED_CONVERTER);
     result = querySummaryConfirmed.body.getReservation();
     assertThat(result, hasSize(1));
     assertThat(result.get(0).getConnectionStates().getProvisionState(), is(ProvisionStateEnumType.PROVISIONED));
   }
 
   @Test
   public void idempotency() throws Exception {
     String globalReservationId = nsiHelper.generateGlobalReservationId();
     String description = "NSI v2 Reservation";
     ReservationRequestCriteriaType criteria = new ReservationRequestCriteriaType()
       .withSchedule(new ScheduleType()
         .withStartTime(XmlUtils.toGregorianCalendar(startTime))
         .withEndTime(XmlUtils.toGregorianCalendar(endTime)))
       .withServiceType("http://services.ogf.org/nsi/2013/07/descriptions/EVTS.A-GOLE");
     System.err.println("!!!! source " + sourceStp);
     System.err.println("!!!! dest " + destStp);
     ConnectionsV2.addPointToPointService(criteria.getAny(), new EthernetVlanType()
       .withCapacity(100)
       .withDirectionality(DirectionalityType.BIDIRECTIONAL)
       .withSourceSTP(sourceStp).withSourceVLAN(BodManagerWebDriver.DEFAULT_VIRTUAL_PORT_VLAN_ID)
       .withDestSTP(destStp).withDestVLAN(BodManagerWebDriver.DEFAULT_VIRTUAL_PORT_VLAN_ID));
 
     // Initial reserve
     String reserveCorrelationId = generateCorrelationId();
     Holder<String> connectionId1 = new Holder<>(null);
     connectionServiceProviderPort.reserve(connectionId1, globalReservationId, description, criteria, createHeader(reserveCorrelationId));
     assertThat(connectionId1.value, is(notNullValue()));
 
     Message<ReserveConfirmedType> reserveConfirmed1 = soapReplyListener.getLastReply(Converters.RESERVE_CONFIRMED_CONVERTER);
     assertThat("reserve confirmed correlation id", reserveConfirmed1.header.getCorrelationId(), is(reserveCorrelationId));
 
     // Reserve with same content and correlation id.
     Holder<String> connectionId2 = new Holder<>(null);
     connectionServiceProviderPort.reserve(connectionId2, globalReservationId, description, criteria, createHeader(reserveCorrelationId));
     assertThat(connectionId2.value, is(notNullValue()));
 
     Message<ReserveConfirmedType> reserveConfirmed2 = soapReplyListener.getLastReply(Converters.RESERVE_CONFIRMED_CONVERTER);
     assertThat("reserve confirmed correlation id", reserveConfirmed2.header.getCorrelationId(), is(reserveCorrelationId));
 
     assertThat(connectionId1.value, is(connectionId2.value));
     assertThat(Converters.RESERVE_CONFIRMED_CONVERTER.toXmlString(reserveConfirmed1.body), is(Converters.RESERVE_CONFIRMED_CONVERTER.toXmlString(reserveConfirmed2.body)));
 
     // perform query, assert that there is only a single reservation
     connectionServiceProviderPort.querySummary(null, Arrays.asList(globalReservationId), createHeader(generateCorrelationId()));
     Message<QuerySummaryConfirmedType> querySummaryConfirmed = soapReplyListener.getLastReply(Converters.QUERY_SUMMARY_CONFIRMED_CONVERTER);
 
     List<QuerySummaryResultType> result = querySummaryConfirmed.body.getReservation();
     assertThat(result, hasSize(1));
     assertThat(result.get(0).getConnectionId(), is(connectionId1.value));
 
     // Reserve with same correlation id but different content should generate an error response!
     criteria.setServiceType("changedServiceType");
 
     Holder<String> connectionId3 = new Holder<>(null);
     try {
       connectionServiceProviderPort.reserve(connectionId3, globalReservationId, description, criteria, createHeader(reserveCorrelationId));
       fail("ServiceExceptione expected");
     } catch (ServiceException expected) {
       assertThat(connectionId3.value, is(nullValue()));
       assertThat(expected.getFaultInfo().getErrorId(), is("100"));
       assertThat(expected.getFaultInfo().getText(), is("PAYLOAD_ERROR"));
     }
   }
 
   @Test
   public void nsiTopologyShouldBeValidAgainstCurrentXSD() throws Exception {
 
     final String url = BodWebDriver.URL_UNDER_TEST + "/nsi-topology";
     HttpClient httpclient = new DefaultHttpClient();
 
     SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
     URL schemaFileUrl = this.getClass().getResource("/topology/nsi-ext.xsd");
     Schema schema = schemaFactory.newSchema(new File(schemaFileUrl.toURI()));
 
     try {
       HttpGet httpget = new HttpGet(url);
       HttpResponse response = httpclient.execute(httpget);
       HttpEntity entity = response.getEntity();
 
       Header header = response.getLastHeader("Content-Type");
       assertTrue("application/xml;charset=UTF-8".equals(header.getValue()));
 
       String xml = EntityUtils.toString(entity);
       assertTrue(xml.length() > 0);
       LOG.debug("Response content: " + xml);
       Validator validator = schema.newValidator();
       validator.validate(new StreamSource(new StringReader(xml)));
     } finally {
       httpclient.getConnectionManager().shutdown();
     }
   }
 
   private Holder<CommonHeaderType> createHeader(final String correlationId){
     return new Holder<>(new CommonHeaderType()
         .withProtocolVersion("2.0")
         .withRequesterNSA("urn:ogf:network:nsa:foo")
         .withProviderNSA(nsiHelper.getUrnProviderNsaV2())
         .withReplyTo(REPLY_ADDRESS)
         .withCorrelationId(correlationId)
     );
   }
 
   private ConnectionProviderPort createPort(String oauthToken) {
     ConnectionProviderPort port = new ConnectionServiceProvider(wsdlUrl()).getConnectionServiceProviderPort();
     Map<String,Object> requestContext = ((BindingProvider) port).getRequestContext();
     requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT);
     requestContext.put(MessageContext.HTTP_REQUEST_HEADERS, Collections.singletonMap("Authorization", Collections.singletonList("bearer " + oauthToken)));
     return port;
   }
 
 
   private URL wsdlUrl() {
     try {
       return new ClassPathResource(WSDL_LOCATION).getURL();
     } catch (IOException e) {
       throw new RuntimeException("Could not find the requester wsdl", e);
     }
   }
 }
