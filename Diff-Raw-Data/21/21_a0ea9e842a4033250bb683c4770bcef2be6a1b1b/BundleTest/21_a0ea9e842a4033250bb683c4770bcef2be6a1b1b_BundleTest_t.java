 package models;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 import play.test.UnitTest;
 import util.TestHelper;
 import util.XStreamHelper;
 
 public class BundleTest extends UnitTest {
 
 
 	@Test 
 	public void testGetModule() {
 		
 		Bundle c = new Bundle();
 		c.services.add(new Service("a"));
 		c.services.add(new Service("b"));
 		c.services.add(new Service("c"));
 		
 		Service m = c.getService("b");
 		assertEquals("b", m.name );
 		
		/*
		 * looking up a wrong name result in a null 
		 */
		assertNull( c.getService("wrong name") );
 	}
 
 	@Test 
 	public void testFromXML() {
 		String xml = 
 			"<bundle name='the-bundle-name' version='9.99'>" +
 			 	"<def ></def>" +
 			
 				"<service name='primero' ><title>uno</title></service>" +
 				"<service name='segundo' ><title>dos</title></service>" +
 				"<service name='tercero' ><title>tres</title></service>" +
 			"</bundle>";
 		
 		Bundle bundle = XStreamHelper.fromXML(xml);
 		
 		assertNotNull(bundle);
 		assertEquals(bundle.name, "the-bundle-name");
 		assertEquals(bundle.version, "9.99");
 		assertNotNull(bundle.def);
 		
 		assertEquals("uno", bundle.services.get(0).title);
 		assertEquals("dos", bundle.services.get(1).title);
 		assertEquals("tres", bundle.services.get(2).title);
 
 		assertEquals("primero", bundle.services.get(0).name);
 		assertEquals("segundo", bundle.services.get(1).name);
 		assertEquals("tercero", bundle.services.get(2).name);
 
 	}
 	
 	@Test
 	public void testGetGroups() {
 		
 		Service m1 = new Service();
 		m1.group = "G1";
 
 		Service m2 = new Service();
 		m2.group = "G2";
 		
 		Service m3 = new Service();
 		m3.group = " G2 ";
 
 		Service m4 = new Service();
 
 		Service m5 = new Service();
 		m5.group = "";
 		
 		Bundle c = new  Bundle();
 		c.services = new ArrayList<Service>();
 		c.services.add(m1);
 		c.services.add(m2);
 		c.services.add(m3);
 		c.services.add(m4);
 		c.services.add(m5);
 		
 		assertArrayEquals( new String[]{"G1","G2",""}, c.getGroups().toArray());
 
 		assertArrayEquals( new Service[]{m1}, c.getServicesByGroup("G1").toArray() );
 		assertArrayEquals( new Service[]{m2,m3}, c.getServicesByGroup("G2").toArray() );
 		assertArrayEquals( new Service[]{m4,m5}, c.getServicesByGroup("").toArray() );
 		assertArrayEquals( new Service[]{}, c.getServicesByGroup("XX").toArray() );
 		
 		try {
 			c.getServicesByGroup(null);
 			fail("Argument null is not supported");
 		}
 		catch( Exception e ) { /* ok */  }
 	} 
 
 	@Test
 	public void testBundleReadProperties() { 
 		Bundle bundle = new Bundle();
 		bundle.readProperties( TestHelper.file("/bundle.properties"), "TEST" );
 		
 		assertEquals( "1", bundle.properties.get("alpha") );
 		assertEquals( "2", bundle.properties.get("beta") );
 		assertEquals( "3", bundle.properties.get("delta") );
 
 		bundle = new Bundle();
 		bundle.readProperties( TestHelper.file("/bundle.properties"), "DEMO" );
 		
 		assertEquals( "1", bundle.properties.get("alpha") );
 		assertEquals( "2", bundle.properties.get("beta") );
 		assertEquals( "4", bundle.properties.get("delta") );
 		
 	}
 	
 }
