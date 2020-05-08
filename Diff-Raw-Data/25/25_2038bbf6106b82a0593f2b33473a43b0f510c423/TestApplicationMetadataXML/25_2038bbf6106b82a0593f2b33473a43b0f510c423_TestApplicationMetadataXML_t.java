 /**
  * Tests for the ApplicationMetadataXML class.
  * <p>
  * Source code in 3rd-party is licensed and owned by their respective
  * copyright holders.
  * <p>
  * All other source code is copyright Tresys Technology and licensed as below.
  * <p>
  * Copyright (c) 2012 Tresys Technology LLC, Columbia, Maryland, USA
  * <p>
  * This software was developed by Tresys Technology LLC
  * with U.S. Government sponsorship.
  * <p>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p>
  *    http://www.apache.org/licenses/LICENSE-2.0
  * <p>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
package com.tresys.jalop.producer;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
import org.w3c.dom.Document;

 import mockit.*;
 import mockit.integration.junit4.*;
 
 import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
 
 import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.JournalMetadataType;
 import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.LoggerType;
 import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.SyslogType;
 import com.tresys.jalop.common.JALException;
 import com.tresys.jalop.producer.ApplicationMetadataXML;
 import com.tresys.jalop.producer.ApplicationMetadataXML.XorAesType;
 
 public class TestApplicationMetadataXML {
 	public class ApplicationMetadataXMLImpl extends ApplicationMetadataXML {
 		public ApplicationMetadataXMLImpl() {
 			super();
 		}
 		
 		public ApplicationMetadataXMLImpl(LoggerType logger) throws Exception {
 			super(logger);
 		}
 		
 		public ApplicationMetadataXMLImpl(SyslogType syslog) throws Exception {
 			super(syslog);
 		}
 		
 		public ApplicationMetadataXMLImpl(String custom) throws Exception {
 			super(custom);
 		}
 	}
 	
 	@Test
 	public void testApplicationMetadataHasLoggerConstructor() throws Exception {
 		LoggerType logger = new LoggerType();
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl(logger);
 		assertNotNull(appMetaXML);
 	}
 	
 	@Test
 	public void testApplicationMetadataHasSyslogConstructor() throws Exception {
 		SyslogType syslog = new SyslogType();
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl(syslog);
 		assertNotNull(appMetaXML);
 	}
 	
 	@Test
 	public void testApplicationMetadataHasCustomConstructor() throws Exception {
 		String custom = "<custom>message</custom>";
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl(custom);
 		assertNotNull(appMetaXML);
 	}
 	
 	@Test
 	public void testApplicationMetadataXMLGettersAndSettersWork() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		appMetaXML.setEventId("event");
 		assertEquals("event", appMetaXML.getEventId());
 		
 		JournalMetadataType jmt = new JournalMetadataType();
 		assertNotNull(jmt);
 		appMetaXML.setJournalMetadata(jmt);
 		assertEquals(jmt, appMetaXML.getJournalMetadata());
 		
 		appMetaXML.prepareSend("hostname", "app");
 		String jid = appMetaXML.getJID();
 		assertNotNull(jid);
 	}
 	
 	@Test(expected = JALException.class)
 	public void testApplicationMetadataXMLLoggerConstructorThrowsJALExceptionOnNull() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl((LoggerType)null);
 	}
 	
 	@Test(expected = JALException.class)
 	public void testApplicationMetadataXMLSyslogConstructorThrowsJALExceptionOnNull() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl((SyslogType)null);
 	}
 	
 	@Test(expected = JALException.class)
 	public void testApplicationMetadataXMLCustomConstructorThrowsJALExceptionOnNull() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl((String)null);
 	}
 	
 	@Test
 	public void testCreateXorAes128TypeReturnsValidType() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		assertEquals(JAXBElement.class, appMetaXML.createXorAesType(new byte[16], new byte[16], ApplicationMetadataXML.XorAesType.AES128CBC).getClass());
 	}
 	
 	@Test
 	public void testCreateXorAes192TypeReturnsValidType() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		assertEquals(JAXBElement.class, appMetaXML.createXorAesType(new byte[16], new byte[24], ApplicationMetadataXML.XorAesType.AES192CBC).getClass());
 	}
 	
 	@Test
 	public void testCreateXorAes256TypeReturnsValidType() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		assertEquals(JAXBElement.class, appMetaXML.createXorAesType(new byte[16], new byte[32], ApplicationMetadataXML.XorAesType.AES256CBC).getClass());
 	}
 	
 	@Test
 	public void testCreateXorAesXorTypeReturnsValidType() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		assertEquals(JAXBElement.class, appMetaXML.createXorAesType(null, new byte[4], ApplicationMetadataXML.XorAesType.XorECB).getClass());
 	}
 	
 	@Test(expected = JALException.class)
 	public void testCreateXorAesTypeReturnsJALExceptionWhenTypeNull() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		appMetaXML.createXorAesType(new byte[16], new byte[16], null);
 	}
 	
 	@Test(expected = JALException.class)
 	public void testCreateXorAesType128ReturnsJALExceptionWhenIVNull() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		appMetaXML.createXorAesType(null, new byte[16], XorAesType.AES128CBC);
 	}
 	
 	@Test(expected = JALException.class)
 	public void testCreateXorAesType192ReturnsJALExceptionWhenIVNull() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		appMetaXML.createXorAesType(null, new byte[24], XorAesType.AES192CBC);
 	}
 	
 	@Test(expected = JALException.class)
 	public void testCreateXorAesType256ReturnsJALExceptionWhenIVNull() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		appMetaXML.createXorAesType(null, new byte[32], XorAesType.AES256CBC);
 	}
 	
 	@Test(expected = JALException.class)
 	public void testCreateXorAesTypeReturnsJALExceptionWhenKeyNullAndTypeAES() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		appMetaXML.createXorAesType(new byte[16], null, XorAesType.AES128CBC);
 	}
 	
 	@Test(expected = JALException.class)
 	public void testCreateXorAesTypeReturnsJALExceptionWhenKeyNullAndTypeXor() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		appMetaXML.createXorAesType(null, null, XorAesType.AES128CBC);
 	}
 	
 	@Test(expected = JALException.class)
 	public void testCreateXorAesType128ReturnsJALExceptionWithBadIVLength() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		appMetaXML.createXorAesType(new byte[1], new byte[16], XorAesType.AES128CBC);
 	}
 	
 	@Test(expected = JALException.class)
 	public void testCreateXorAesType128ReturnsJALExceptionWithBadKeyLength() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		appMetaXML.createXorAesType(new byte[16], new byte[1], XorAesType.AES128CBC);
 	}
 	
 	@Test(expected = JALException.class)
 	public void testCreateXorAesType192ReturnsJALExceptionWithBadIVLength() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		appMetaXML.createXorAesType(new byte[1], new byte[24], XorAesType.AES192CBC);
 	}
 	
 	@Test(expected = JALException.class)
 	public void testCreateXorAesType192ReturnsJALExceptionWithBadKeyLength() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		appMetaXML.createXorAesType(new byte[16], new byte[1], XorAesType.AES192CBC);
 	}
 	
 	@Test(expected = JALException.class)
 	public void testCreateXorAesType256ReturnsJALExceptionWithBadIVLength() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		appMetaXML.createXorAesType(new byte[1], new byte[32], XorAesType.AES256CBC);
 	}
 	
 	@Test(expected = JALException.class)
 	public void testCreateXorAesType256ReturnsJALExceptionWithBadKeyLength() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		appMetaXML.createXorAesType(new byte[16], new byte[1], XorAesType.AES256CBC);
 	}
 	
 	@Test(expected = JALException.class)
 	public void testCreateXorAesTypeXorReturnsJALExceptionWithBadKeyLength() throws Exception {
 		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
 		assertNotNull(appMetaXML);
 		appMetaXML.createXorAesType(null, new byte[1], XorAesType.XorECB);
 	}
	
	@Test
	public void testMarshalWorks() throws Exception {
		LoggerType logger = new LoggerType();

		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl(logger);
		appMetaXML.prepareSend("Host Name", "Application Name");
		assertNotNull(appMetaXML);
		Document doc = appMetaXML.marshal();
		assertTrue(doc instanceof Document);
	}
	
	@Test(expected = JAXBException.class)
	public void testMarshalThrowsException() throws Exception {
		ApplicationMetadataXMLImpl appMetaXML = new ApplicationMetadataXMLImpl();
		appMetaXML.marshal();
	}
 }
