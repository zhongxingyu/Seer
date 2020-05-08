 package com.cargosmart.b2b.edi.input;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.cargosmart.b2b.edi.common.Document;
 import com.cargosmart.b2b.edi.common.Segment;
 import com.cargosmart.b2b.edi.common.Transaction;
 import com.cargosmart.b2b.edi.common.edifact.EdifactGroupEnvelope;
 import com.cargosmart.b2b.edi.common.edifact.EdifactInterchangeEnvelope;
 
 public class EdifactBuilderTest {
 
 	private static String edifactStr;
 	private Document doc;
 
 	@BeforeClass
 	public static void onlyOnce() throws Exception {
 		InputStream in = EdifactBuilderTest.class
 				.getResourceAsStream("/invoice_d97b.txt");
 		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 		char[] buffer = new char[1024];
 		int nRead;
 		StringBuilder content = new StringBuilder();
 		while ((nRead = reader.read(buffer, 0, 1024)) != -1) {
 			content.append(buffer, 0, nRead);
 		}
 		edifactStr = content.toString();
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		EdifactBuilder builder = new EdifactBuilder();
 		doc = builder.buildDocument(edifactStr);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testBuildDocumentString() {
 		assertEquals("+", doc.getElementSeparator());
 		assertEquals(":", doc.getSubElementSeparator());
 		assertEquals("'\n", doc.getSegmentSeparator());
 		assertEquals("?", doc.getReleaseCharacter());
 		assertNotNull(doc.getInterchangeEnvelope());
 		assertEquals(1, doc.getSegment("BGM").size());
 	}
 	
 	@Test
 	public void testBuildInterchangeSegment() {
 		EdifactInterchangeEnvelope interchange = (EdifactInterchangeEnvelope)doc.getInterchangeEnvelope();
 		assertEquals("1", interchange.getVersion());
 		assertEquals("UNOA", interchange.getField(1).getField(1).getValue());
 		assertEquals("005435656", interchange.getSenderId());
 		assertEquals("1", interchange.getSenderQualifier());
 		assertEquals("006415160", interchange.getReceiverId());
 		assertEquals("1", interchange.getReceiverQualifier());
 		assertEquals("00000000000778", interchange.getControlNumber());
 	}
 	
 	@Test
 	public void testBuildGroupSegment() {
 		EdifactGroupEnvelope group = (EdifactGroupEnvelope) doc.getInterchangeEnvelope().getGroups().get(0);
 		assertEquals(null, group.getFunctionalCode());
 		assertEquals(null, group.getControlNumber());
 		assertEquals(null, group.getVersion());
 		assertEquals(null, group.getSenderCode());
 		assertEquals(null, group.getReceiverCode());
 	}
 	
 	@Test
 	public void testBuildTransaction() {
 		Transaction txn = doc.getInterchangeEnvelope().getGroups().get(0).getTransactions().get(0);
 		assertNotNull(txn);
 		assertEquals("00000000000117", txn.getControlNumber());
 		assertEquals("INVOIC", txn.getType());
 	}
 
 	@Test
 	public void testSegment() {
 		Transaction txn = doc.getInterchangeEnvelope().getGroups().get(0).getTransactions().get(0);
 		Segment seg = txn.getSegments("BGM").get(0);
 		assertEquals("380", seg.getField(1).getField(1).getValue());
 		
 		List<Segment> segments = txn.getSegments("NAD");
 		assertEquals("BY", segments.get(0).getField(1).getField(1).getValue());
 		assertEquals("792820524", segments.get(0).getField(2).getField(1).getValue());
 		assertEquals("", segments.get(0).getField(2).getField(2).getValue());
		assertEquals("GENERAL WIDGET COMPANY", segments.get(1).getField(4).getField(1).getValue());
 	}
 }
