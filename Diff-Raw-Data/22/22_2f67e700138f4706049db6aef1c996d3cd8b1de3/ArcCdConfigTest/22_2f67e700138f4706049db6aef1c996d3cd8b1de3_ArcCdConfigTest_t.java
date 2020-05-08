 /**
  * 
  */
 package org.opf_labs.arc_cd.config;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.opf_labs.arc_cd.config.ArcCdConfig;
 
 /**
  * TODO JavaDoc for ArcCdConfigTest.</p>
  * TODO Tests for ArcCdConfigTest.</p>
  * TODO Implementation for ArcCdConfigTest.</p>
  * 
  * @author  <a href="mailto:carl@openplanetsfoundation.org">Carl Wilson</a>.</p>
  *          <a href="https://github.com/carlwilson">carlwilson AT github</a>.</p>
  * @version 0.1
  * 
  * Created 29 Aug 2013:18:38:23
  */
 public class ArcCdConfigTest {
 	
 	/**
 	 * Test method for {@link org.opf_labs.arc_cd.config.ArcCdConfig#getCollectionRoot()}.
 	 */
 	@Test
 	public void testGetCollectionRoot() {
 		// Should be made up of the default parts
 		assertTrue(ArcCdConfig.getDefault().getCollectionRoot().startsWith(ArcCdConfig.getDefaultRoot()));
 		assertTrue(ArcCdConfig.getDefault().getCollectionRoot().endsWith(ArcCdConfig.getDefaultName()));
 	}
 
 	/**
 	 * Test method for {@link org.opf_labs.arc_cd.config.ArcCdConfig#getArchiveRoot()}.
 	 */
 	@Test
 	public void testGetArchiveRoot() {
 		assertTrue("Expected " + ArcCdConfig.getDefault().getArchiveRoot() + ".equals(" + ArcCdConfig.getDefaultRoot() + ") == " + true, ArcCdConfig.getDefault().getArchiveRoot().equals(ArcCdConfig.getDefaultRoot()));
 	}
 
 	/**
 	 * Test method for {@link org.opf_labs.arc_cd.config.ArcCdConfig#getName()}.
 	 */
 	@Test
 	public void testGetName() {
 		assertTrue(ArcCdConfig.getDefault().getName().equals(ArcCdConfig.getDefaultName()));
 	}
 
 	/**
 	 * Test method for {@link org.opf_labs.arc_cd.config.ArcCdConfig#getDefault()}.
 	 */
 	@Test
 	public void testFromDefault() {
 		assertTrue(ArcCdConfig.getDefault() == ArcCdConfig.getDefault());
 	}
 
 	/**
 	 * Test method for {@link org.opf_labs.arc_cd.config.ArcCdConfig#fromValues(boolean, java.lang.String, java.lang.String, java.lang.String)}.
 	 */
 	@Test
 	public void testFromValues() {
 		ArcCdConfig config = ArcCdConfig.fromValues(false, "newRoot", "newName", "newCdr");
 		assertFalse(config == ArcCdConfig.getDefault());
 	}
 
 }
