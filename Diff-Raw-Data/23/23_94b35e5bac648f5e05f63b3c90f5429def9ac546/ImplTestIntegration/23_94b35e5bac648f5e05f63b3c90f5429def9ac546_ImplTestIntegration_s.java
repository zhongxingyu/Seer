 package net.gageot.coverage;
 
import junit.framework.Assert;
import org.junit.Test;
 
 public class ImplTestIntegration {
 	@Test
 	public void should_test_integration() {
		Assert.assertEquals("Api", new Api().toBeCoveredByIt());
		Assert.assertEquals("Impl", new Impl().toBeCoveredByIt());
 	}
 }
