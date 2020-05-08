 package org.kalibro.service;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 import org.kalibro.tests.UnitTest;
 
 public class KalibroImplTest extends UnitTest {
 
 	@Test
 	public void shouldGetVersion() {
		assertEquals("1.0", new KalibroImpl().version());
 	}
 }
