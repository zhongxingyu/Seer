 package com.cargosmart.b2b.edi.common.x12;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import com.cargosmart.b2b.edi.common.Document;
 import com.cargosmart.b2b.edi.common.Segment;
 import com.cargosmart.b2b.edi.common.x12.X12Document;
 import com.cargosmart.b2b.edi.common.x12.X12InterchangeEnvelope;
 
 public class DocumentTest {
 
 	@Test
 	public void testSetInterchangeEnvelope() {
 		Document doc = new X12Document();
 		X12InterchangeEnvelope interchange = new X12InterchangeEnvelope(new Segment(new String[16]));
 		doc.setInterchangeEnvelope(interchange);
 		
 		assertEquals(interchange, doc.getInterchangeEnvelope());
 		assertEquals(doc, interchange.getDocument());
 		
 	}
 
 }
