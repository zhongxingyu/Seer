 package org.techhub.techq.util;
 
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
 import org.junit.Test;
 
 
 public class PropertiesLoaderTest {
 
 	@Test
 	public void testGetKey() throws Exception {
		PropertiesLoader loader = new PropertiesLoader("webapp_test");
 		assertThat(loader.get("document.root"), is("/var/www/techq"));
 		assertThat(loader.get("default.document"), is("index.html"));
 	}
 }
