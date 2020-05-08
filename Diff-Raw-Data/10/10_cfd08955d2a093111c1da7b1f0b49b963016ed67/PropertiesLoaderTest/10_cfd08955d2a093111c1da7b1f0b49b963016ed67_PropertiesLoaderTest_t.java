 package org.techhub.techq.util;
 
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

 import org.junit.Test;
 
 
 public class PropertiesLoaderTest {
 
 	@Test
 	public void testGetKey() throws Exception {
		PropertiesLoader loader = new PropertiesLoader("target/test-classes/webapp_test.properties");
 		assertThat(loader.get("document.root"), is("/var/www/techq"));
 		assertThat(loader.get("default.document"), is("index.html"));
 	}
 }
