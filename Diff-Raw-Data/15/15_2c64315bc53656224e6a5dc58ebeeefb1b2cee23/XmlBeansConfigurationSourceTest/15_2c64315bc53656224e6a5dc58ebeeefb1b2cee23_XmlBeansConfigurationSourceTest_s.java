 /*
  * Copyright 2012 the original author or authors.
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
 
 package org.brekka.stillingar.xmlbeans;
 
import static org.junit.Assert.*;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.net.URI;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 import java.util.concurrent.TimeUnit;
 
 import net.iharder.Base64;
 
 import org.apache.xmlbeans.XmlOptions;
 import org.brekka.stillingar.api.ValueConfigurationException;
 import org.brekka.stillingar.core.conversion.ConversionManager;
 import org.brekka.stillingar.core.dom.DefaultNamespaceContext;
 import org.brekka.xml.stillingar.test.v1.ConfigurationDocument;
 import org.brekka.xml.stillingar.test.v1.ConfigurationDocument.Configuration.CompanyX;
 import org.brekka.xml.stillingar.test.v1.ConfigurationDocument.Configuration.CompanyY;
 import org.brekka.xml.stillingar.test.v1.ConfigurationDocument.Configuration.FeatureFlag;
 import org.joda.time.Period;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Test of XmlBeansConfigurationSource
  *
  * @author Andrew Taylor (andrew@brekka.org)
  */
 public class XmlBeansConfigurationSourceTest {
     
     private XmlBeansConfigurationSource configurationSource;
     
     @Before
     public void setup() throws Exception {
         XmlOptions opts = new XmlOptions();
         opts.setLoadStripComments();
         ConfigurationDocument document = ConfigurationDocument.Factory.parse(getClass().getResourceAsStream("TestConfiguration.xml"), opts);
         DefaultNamespaceContext namespaceContext = new DefaultNamespaceContext(
             "c", "http://brekka.org/xml/stillingar/test/v1",
             "b", "http://www.springframework.org/schema/beans"
         );
         configurationSource = new XmlBeansConfigurationSource(document, namespaceContext, new ConversionManager(XmlBeansConfigurationSourceLoader.prepareConverters()));
     }
 
     /**
      * Test method for {@link org.brekka.stillingar.xmlbeans.XmlBeansConfigurationSource#isAvailable(java.lang.Class)}.
      */
     @Test
     public void testIsAvailableClassTrue() {
         assertTrue(configurationSource.isAvailable(CompanyY.class));
     }
     
     @Test
     public void testIsAvailableClassFalse() {
         assertFalse(configurationSource.isAvailable(CompanyX.class));
     }
     
     /**
      * Test method for {@link org.brekka.stillingar.xmlbeans.XmlBeansConfigurationSource#isAvailable(java.lang.String)}.
      */
     @Test
     public void testIsAvailableXPathTrue() {
         assertTrue(configurationSource.isAvailable("//c:CompanyY"));
     }
     
     @Test
     public void testIsAvailableXPathFalse() {
         assertFalse(configurationSource.isAvailable("//c:CompanyX"));
     }
 
     /**
      * Test method for {@link org.brekka.stillingar.xmlbeans.XmlBeansConfigurationSource#retrieve(java.lang.Class)}.
      */
     @Test
     public void testRetrieveClass() {
         CompanyY companyY = configurationSource.retrieve(CompanyY.class);
         assertNotNull(companyY);
         assertEquals("http://example.org/CompanyY", companyY.getWarehouseWebService().getURL());
     }
 
     /**
      * Test method for {@link org.brekka.stillingar.xmlbeans.XmlBeansConfigurationSource#retrieve(java.lang.String, java.lang.Class)}.
      */
     @Test
     public void testRetrieveXPath() {
         CompanyY companyY = configurationSource.retrieve("//c:CompanyY", CompanyY.class);
         assertNotNull(companyY);
         assertEquals("http://example.org/CompanyY", companyY.getWarehouseWebService().getURL());
     }
     
     @Test
     public void testRetrieveXPathIndex() {
         String keyword = configurationSource.retrieve("//c:Fraud/c:Keyword[2]", String.class);
         assertEquals("KeywordB", keyword);
     }
     
     @Test
     public void testRetrieveXPathAttrSelector() {
         Boolean flag = configurationSource.retrieve("//c:FeatureFlag[@key='TURBO']", Boolean.class);
         assertEquals(Boolean.TRUE, flag);
     }
     
     @Test
     public void testRetrieveXPathAttrSelectorElem() {
         String message = configurationSource.retrieve("//c:MOTD[@id='1']//c:Message", String.class);
         //call twice sometime it behaves differently on the second call
         message = configurationSource.retrieve("//c:MOTD[@id='1']//c:Message", String.class);
         assertEquals("Test message", message);
     }
 
     /**
      * Test method for {@link org.brekka.stillingar.xmlbeans.XmlBeansConfigurationSource#retrieveList(java.lang.Class)}.
      */
     @Test
     public void testRetrieveListClass() {
         List<FeatureFlag> list = configurationSource.retrieveList(FeatureFlag.class);
         assertNotNull(list);
         assertTrue(list.size() == 2);
     }
 
     /**
      * Test method for {@link org.brekka.stillingar.xmlbeans.XmlBeansConfigurationSource#retrieveList(java.lang.String, java.lang.Class)}.
      */
     @Test
     public void testRetrieveListXPath() {
         List<String> list = configurationSource.retrieveList("//c:Fraud/c:Keyword", String.class);
         assertNotNull(list);
         assertEquals(Arrays.asList("KeywordA", "KeywordB", "KeywordC"), list);
     }
     
     @Test
     public void testRetrieveShort() {
         assertEquals(Short.valueOf((short) 169), configurationSource.retrieve("//c:Scale", Short.class));
     }
     
     @Test
     public void testRetrieveByte() {
         assertEquals(Byte.valueOf((byte) 126), configurationSource.retrieve("//c:Flag", Byte.class));
     }
     
     @Test
     public void testRetrieveInt() {
         assertEquals(Integer.valueOf(42), configurationSource.retrieve("//c:MaxQuantity", Integer.class));
     }
     
     @Test
     public void testRetrieveLong() {
         assertEquals(Long.valueOf(85697458963323L), configurationSource.retrieve("//c:Length", Long.class));
     }
     
     @Test
     public void testRetrieveFloat() {
         assertEquals(Float.valueOf(0.89f), configurationSource.retrieve("//c:TriggerFactor", Float.class));
     }
     
     @Test
     public void testRetrieveDecimal() {
         assertEquals(new BigDecimal("50000.73"), configurationSource.retrieve("//c:MaxAmount", BigDecimal.class));
     }
     
     @Test
     public void testRetrieveInteger() {
         assertEquals(new BigInteger("33543"), configurationSource.retrieve("//c:Factor", BigInteger.class));
     }
     
     @Test
     public void testRetrieveBoolean() throws Exception {
         assertTrue(configurationSource.retrieve("//c:Fraud//c:Enabled", Boolean.class));
     }
     
     @Test // P5Y2M10D
     public void testRetrievePeriod() throws Exception {
         assertEquals(new Period(5, 2, 0, 10, 0, 0, 0, 0), configurationSource.retrieve("//c:LockDuration", Period.class));
     }
     
     
     @Test
     public void testRetrieveBytes() throws Exception {
         byte[] expected = Base64.decode("U3RpbGxpbmdhcg==");
         assertTrue(Arrays.equals(expected, configurationSource.retrieve("//c:PublicKey", byte[].class)));
     }
     
     @Test
     public void testRetrieveDate() throws Exception {
         Date expected = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse("2012-12-31T12:00:00");
         assertEquals(expected, configurationSource.retrieve("//c:Expires", Date.class));
     }
     
     @Test
     public void testRetrieveCalendar() throws Exception {
         Date expected = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse("2012-12-31T12:00:00");
         assertEquals(expected, configurationSource.retrieve("//c:Expires", Calendar.class).getTime());
     }
     
     
     @Test
     public void testRetrieveURI() throws Exception {
         assertEquals(new URI("http://example.org/CompanyY"), configurationSource.retrieve("//c:CompanyY//c:URL", URI.class));
     }
     
     @Test
     public void testRetrieveUUID() throws Exception {
         assertEquals(UUID.fromString("64829ee9-d265-47bb-8fb4-4ab4ada0cdfc"), configurationSource.retrieve("//c:MOTD//c:ID", UUID.class));
     }
     
     @Test
     public void testRetrieveEnum() {
         assertEquals(TimeUnit.DAYS, configurationSource.retrieve("//c:Security/c:TimeUnit", TimeUnit.class));
     }
 
     @Test
     public void testRetrieveElement() throws Exception {
         Element element = configurationSource.retrieve("//c:Rules", Element.class);
         assertEquals("Rules", element.getNodeName());
         assertEquals(1, element.getElementsByTagNameNS("*", "Transaction").getLength());
         assertEquals(1, element.getElementsByTagNameNS("*", "Fraud").getLength());
     }
     
     @Test
     public void testRetrieveDocument() throws Exception {
         Document document = configurationSource.retrieve("//c:ApplicationContext/b:beans", Document.class);
         assertEquals("beans", document.getDocumentElement().getLocalName());
     }
     
     @Test(expected=ValueConfigurationException.class)
     public void testRetrieveIncorrectType() throws Exception {
         configurationSource.retrieve("//c:MaxQuantity", URL.class);
     }
     
 }
