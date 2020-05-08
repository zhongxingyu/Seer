 package net.gageot.coverage;
 
import static org.junit.Assert.assertEquals;

import org.junit.*;
 
 public class ImplTestIntegration {
 	@Test
 	public void should_test_integration() {
		assertEquals("Api", new Api().toBeCoveredByIt());
		assertEquals("Impl", new Impl().toBeCoveredByIt());
 	}
 }
