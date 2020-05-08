 package com.workshop.commerce;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class AppPropsTest {
 	
 	File dbPropFile = null;
 	File appPropFile = null;
 	
 	@Before
 	public void setUp() throws Exception {
 		dbPropFile = File.createTempFile("database", ".properties");
 		appPropFile = File.createTempFile("application", ".properties");
 		BufferedWriter dbwriter = new BufferedWriter(new FileWriter(dbPropFile));
 		BufferedWriter appwriter = new BufferedWriter(new FileWriter(appPropFile));
 		
 		dbwriter.write("db.driver=org.hsqldb.jdbcDriver");
 		dbwriter.newLine();
 		dbwriter.write("db.user=sa");
 		dbwriter.newLine();
 		dbwriter.write("db.password=");
 		dbwriter.newLine();
 		dbwriter.write("db.connection=jdbc:hsqldb:mem:demo");
		appwriter.write("json.dir=~/workshop/json");
 
 		dbwriter.flush();
 		appwriter.flush();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		if (dbPropFile != null) {
 			dbPropFile.delete();
 		}
 		 
 		if (appPropFile != null) {
 			appPropFile.delete();
 		}
 	}
 
 	@Test
 	public void testGetDbProp() {
 		AppProps props = AppProps.getInstance();
 		assertTrue(dbPropFile.exists());
 		props.load(dbPropFile);
 		assertEquals("org.hsqldb.jdbcDriver", props.getProp("db.driver"));
 		assertEquals("sa", props.getProp("db.user"));
 		assertEquals("", props.getProp("db.password"));
 		assertEquals("jdbc:hsqldb:mem:demo", props.getProp("db.connection"));
 	}
 	
 	@Test
 	public void testGetAppProp() {
 		AppProps props = AppProps.getInstance();
 		assertTrue(appPropFile.exists());
 		props.load(appPropFile);
 		assertEquals("/home/ebernie/json", props.getProp("json.dir"));
 	}
 	
 }
