 package com.byond.dm.model.dmb;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 
 import org.junit.Test;
 
 public class DMBMetadataTest extends DMBTest {
 	
 	@Test
 	public void testBasicMetadata() throws IOException {
 		DMBMetadata metadata = new DMBMetadata();
 		metadata.read(stream);
		assertEquals(499, metadata.getCompiledVersion());
		assertEquals(490, metadata.getMinimumVersion());
 	}
 }
