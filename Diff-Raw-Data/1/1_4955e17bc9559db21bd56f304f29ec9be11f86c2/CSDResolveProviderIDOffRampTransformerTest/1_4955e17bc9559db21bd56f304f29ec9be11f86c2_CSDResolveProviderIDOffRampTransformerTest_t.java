 /* This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
 package org.jembi.rhea.transformers;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jembi.Util;
 import org.junit.Test;
 import org.mule.api.MuleMessage;
 import org.mule.api.transformer.TransformerException;
 import org.mule.api.transport.PropertyScope;
 
 public class CSDResolveProviderIDOffRampTransformerTest {
 	private static final String EXPECTED_REQUEST_BY_ID =
 		"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
 		"<csd:careServicesRequest xmlns:csd='urn:ihe:iti:csd:2013' xmlns='urn:ihe:iti:csd:2013'>\n" +
 		"    <function uuid=\"4e8bbeb9-f5f5-11e2-b778-0800200c9a66\">\n" +
 		"        <requestParams>\n" +
 		"            <id oid=\"%s\"/>\n" +
 		"            <otherID/>\n" +
 		"            <commonName/>\n" +
 		"            <type/>\n" +
 		"            <addressLine/>\n" +
 		"            <record/>\n" +
 		"            <start/>\n" +
 		"            <max/>\n" +
 		"        </requestParams>\n" +
 		"    </function>\n" +
 		"</csd:careServicesRequest>";
 
 	private static final String EXPECTED_REQUEST_BY_OTHERID =
 		"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
 		"<csd:careServicesRequest xmlns:csd='urn:ihe:iti:csd:2013' xmlns='urn:ihe:iti:csd:2013'>\n" +
 		"    <function uuid=\"4e8bbeb9-f5f5-11e2-b778-0800200c9a66\">\n" +
 		"        <requestParams>\n" +
 		"            <id/>\n" +
 		"            <otherID assigningAuthorityName=\"%s\" code=\"%s\"/>\n" +
 		"            <commonName/>\n" +
 		"            <type/>\n" +
 		"            <addressLine/>\n" +
 		"            <record/>\n" +
 		"            <start/>\n" +
 		"            <max/>\n" +
 		"        </requestParams>\n" +
 		"    </function>\n" +
 		"</csd:careServicesRequest>";
 
 	@Test
 	public void testTransformMessage_NID2EPID() throws TransformerException {
 		String id = "1234567890";
 		MuleMessage mockMessage = buildMockMessage(id, "NID", "EPID");
 		CSDResolveProviderIDOffRampTransformer transformer = new CSDResolveProviderIDOffRampTransformer();
 		CSDResolveProviderIDOffRampTransformer.ASSIGNING_AUTHORITY_OIDS = null;
 
 		transformer.setAssigningAuthorityOIDS("NID:1234");
 		Object res = transformer.transform(mockMessage, "");
 
 		assertNotNull(res);
 		assertTrue(res instanceof String);
 		assertEquals(Util.trimXML(String.format(EXPECTED_REQUEST_BY_OTHERID, "1234", id)), Util.trimXML((String)res));
 		verify(mockMessage).setProperty(
 			CSDResolveProviderIDOffRampTransformer.SESSIONVAR_PROVIDER_TARGETIDTYPE,
 			CSDResolveProviderIDOffRampTransformer.UNIVERSAL_TARGETIDTYPE,
 			PropertyScope.SESSION
 		);
 	}
 
 	@Test
 	public void testTransformMessage_EPID2NID() throws TransformerException {
 		String id = "1234567890";
 		MuleMessage mockMessage = buildMockMessage(id, "EPID", "NID");
 		CSDResolveProviderIDOffRampTransformer transformer = new CSDResolveProviderIDOffRampTransformer();
 		CSDResolveProviderIDOffRampTransformer.ASSIGNING_AUTHORITY_OIDS = null;
 
 		transformer.setAssigningAuthorityOIDS("NID:1234");
 		Object res = transformer.transform(mockMessage, "");
 
 		assertNotNull(res);
 		assertTrue(res instanceof String);
 		assertEquals(Util.trimXML(String.format(EXPECTED_REQUEST_BY_ID, id)), Util.trimXML((String)res));
 		verify(mockMessage).setProperty(
 			CSDResolveProviderIDOffRampTransformer.SESSIONVAR_PROVIDER_TARGETIDTYPE,
 			"1234",
 			PropertyScope.SESSION
 		);
 	}
 
 	@Test
 	public void testIdTypeOIDSProperties() throws TransformerException {
 		String id = "1234567890";
 		MuleMessage mockMessage = buildMockMessage(id, "OTHER-ID", "THIRD-ID");
 		CSDResolveProviderIDOffRampTransformer transformer = new CSDResolveProviderIDOffRampTransformer();
 		CSDResolveProviderIDOffRampTransformer.ASSIGNING_AUTHORITY_OIDS = null;
 
 		transformer.setAssigningAuthorityOIDS("NID:1234,OTHER-ID:2345,THIRD-ID:3456");
 		Object res = transformer.transform(mockMessage, "");
 		assertNotNull(res);
 		assertTrue(res instanceof String);
 		assertEquals(Util.trimXML(String.format(EXPECTED_REQUEST_BY_OTHERID, "2345", id)), Util.trimXML((String)res));
 		verify(mockMessage).setProperty(CSDResolveProviderIDOffRampTransformer.SESSIONVAR_PROVIDER_TARGETIDTYPE, "3456", PropertyScope.SESSION);
 	}
 
 	@Test
 	public void testIdTypeOIDSProperties_invalid() throws TransformerException {
 		MuleMessage mockMessage = buildMockMessage("1234567890", "NID", "EPID");
 		CSDResolveProviderIDOffRampTransformer transformer = new CSDResolveProviderIDOffRampTransformer();
 		CSDResolveProviderIDOffRampTransformer.ASSIGNING_AUTHORITY_OIDS = null;
 
 		transformer.setAssigningAuthorityOIDS("This is invalid");
 		try {
 			transformer.transform(mockMessage, "");
			fail();
 		} catch (TransformerException ex) {
 			//expected
 		}
 	}
 	
 	private MuleMessage buildMockMessage(String id, String idType, String targetIdType) {
 		Map<String, String> params = new HashMap<>();
 		params.put("id", id);
 		params.put("idType", idType);
 		params.put("targetIdType", targetIdType);
 		return Util.buildMockMuleResponse(true, params);
 	}
 }
