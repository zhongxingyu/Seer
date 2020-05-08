 /*
  * Created on May 23, 2005
  *
  */
 package fedora.test.integration;
 
 import junit.extensions.TestSetup;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.custommonkey.xmlunit.SimpleXpathEngine;
 
 import fedora.client.FedoraClient;
 import fedora.server.access.FedoraAPIA;
 import fedora.server.types.gen.MIMETypedStream;
 import fedora.server.types.gen.RepositoryInfo;
 import fedora.server.types.gen.ObjectProfile;
 import fedora.server.types.gen.DatastreamDef;
 import fedora.server.types.gen.ObjectMethodsDef;
 import fedora.server.types.gen.Property;
 import fedora.server.types.gen.MethodParmDef;
 import fedora.test.FedoraServerTestCase;
 import fedora.test.FedoraServerTestSetup;
 
 /**
  * @author Sandy Payette
  *
  */
 public class TestAPIA extends FedoraServerTestCase {
 	
 	public static final String BASE_URL = "http://localhost:8080/fedora";
 	private static FedoraClient client;
     private FedoraAPIA apia;
     
     public static Test suite() {
 		TestSuite suite = new TestSuite("Unit test for APIA");
 		suite.addTestSuite(TestAPIA.class);
 		TestSetup wrapper = new TestSetup(suite) {
             public void setUp() throws Exception {
                 TestIngestDemoObjects.ingestDemoObjects();
                 SimpleXpathEngine.registerNamespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
                 SimpleXpathEngine.registerNamespace("dc", "http://purl.org/dc/elements/1.1/");
 				SimpleXpathEngine.registerNamespace("uvalibadmin", "http://dl.lib.virginia.edu/bin/admin/admin.dtd/");
             }
             
             public void tearDown() throws Exception {
                 TestIngestDemoObjects.purgeDemoObjects();
                 SimpleXpathEngine.clearNamespaces();
             }
         };
         return new FedoraServerTestSetup(wrapper);
     }
     
     public void setUp() throws Exception {
 		apia = new FedoraClient(BASE_URL, getUsername(), getPassword()).getAPIA();
     }
     
     public void testDescribeRepository() throws Exception {
         RepositoryInfo describe = apia.describeRepository();
         assertTrue(!describe.getRepositoryName().equals(""));
     }
     
 	public void testGetObjectProfile() throws Exception {
 		ObjectProfile profile = apia.getObjectProfile("demo:11", null);
 		assertEquals("demo:11", profile.getPid());
 		assertEquals("UVA_MRSID_IMAGE", profile.getObjContentModel());
 		assertEquals("O", profile.getObjType());
 		assertTrue(!profile.getObjDissIndexViewURL().equals(""));
 		assertTrue(!profile.getObjItemIndexViewURL().equals(""));
 	}
 	
 	public void testObjectHistory() throws Exception {
 		String[] timestamps = apia.getObjectHistory("demo:11");
 		assertTrue(timestamps.length > 0);
 	}
     
     public void testGetDissemination() throws Exception {
     	
 		// test dissemination of the Default Disseminator
 		MIMETypedStream diss = null;
 		diss = apia.getDissemination("demo:11", "fedora-system:3", "viewDublinCore", new Property[0], null);
 		assertEquals(diss.getMIMEType(),"text/html");
 		assertTrue(diss.getStream().length > 0);
     	
 		// test dissemination with E datastream as input to a remote bmech service (MrSID)
         diss = apia.getDissemination("demo:11", "demo:8", "getThumbnail", new Property[0], null);
 		assertEquals(diss.getMIMEType(),"image/jpeg");
 		assertTrue(diss.getStream().length > 0);
 		
 		// test dissemination using remote bmech service (MrSID) with user input parms		
 		Property[] parms = new Property[2];
 		Property p = new Property();
 		p.setName("ZOOM");
 		p.setValue("no");
 		parms[0] = p;
 		Property p2 = new Property();
 		p2.setName("SIZE");
 		p2.setValue("small");
 		parms[1] = p2;
 		diss = apia.getDissemination("demo:11", "demo:8", "getImage", parms, null);
 		assertEquals(diss.getMIMEType(),"image/jpeg");
 		assertTrue(diss.getStream().length > 0);
 		
 		// test chained dissemination using local bmech services
 		// The object contains an E datastream that is a dissemination of the local SAXON service.
 		// This datastream is input to another dissemination that uses the local FOP service.
 		diss = apia.getDissemination("demo:26", "demo:19", "getPDF", new Property[0], null);
 		assertEquals(diss.getMIMEType(),"application/pdf");
 		assertTrue(diss.getStream().length > 0);
   
     }
     
 	public void testGetDatastreamDissemination() throws Exception {
 		
 		// test for DC datastream
 		MIMETypedStream ds = null;
 		ds = apia.getDatastreamDissemination("demo:11", "DC", null);
 		assertXpathExists("/oai_dc:dc", new String(ds.getStream()));
 		
 		// test for type X datastream 		
 		ds = apia.getDatastreamDissemination("demo:11", "TECH1", null);
 		String dsXML = new String(ds.getStream(), "UTF-8");
 		assertEquals(ds.getMIMEType(),"text/xml");
 		assertTrue(ds.getStream().length > 0);		
 		assertXpathExists("//uvalibadmin:technical",dsXML);
 		assertXpathEvaluatesTo(
 			"wavelet", "/uvalibadmin:admin/uvalibadmin:technical/uvalibadmin:compression/text( )", dsXML);
 
 		// test for type E datastream 			
 		ds = apia.getDatastreamDissemination("demo:11", "DS1", null);
 		assertEquals(ds.getMIMEType(),"image/x-mrsid-image");
 		assertTrue(ds.getStream().length > 0);	
 		
 		// test for type R datastream 			
 		ds = apia.getDatastreamDissemination("demo:30", "DS1", null);
 		assertEquals(ds.getMIMEType(),"application/fedora-redirect");
 		assertTrue(ds.getStream().length > 0);
 		
 		// test for type M datastream 			
 		ds = apia.getDatastreamDissemination("demo:5", "DS1", null);
 		assertEquals(ds.getMIMEType(),"image/jpeg");
 		assertTrue(ds.getStream().length > 0);
 		     
 	}
  
 	public void testListDatastreams() throws Exception {
 		DatastreamDef[] dsDefs = apia.listDatastreams("demo:11", null);
 		assertEquals(dsDefs.length,8);
 		verifyDatastreamDefs(dsDefs, "testListDatastream: ");
 	}
 	
 	public void testListMethods() throws Exception {
 		ObjectMethodsDef[] methodDefs = apia.listMethods("demo:11", null);
		assertEquals(methodDefs.length,7);
 		verifyObjectMethods(methodDefs, "testListMethods: ");
 	}
 
 	public void verifyDatastreamDefs(DatastreamDef[] dsDefArray, String msg) throws Exception {
 	
 		String dsID = null;
 		String label = null;
 		String mimeType = null;
 		DatastreamDef dsDef = null;
 	        
 		for (int i=0; i<dsDefArray.length; i++) {
 			dsDef = dsDefArray[i];
 			dsID = dsDef.getID();
 			label = dsDef.getLabel();
 			mimeType = dsDef.getMIMEType();
 			System.out.println(msg + " datastreamDef["+i+"] "
 				 + "dsID: "+dsID);
 			System.out.println(msg + " datastreamDef["+i+"] "
 				 + "label: '"+label+"'");
 			System.out.println(msg + " datastreamDef["+i+"] "
 				 + "mimeType: "+mimeType);
 		}
 	        
 	}
 
 	public void verifyObjectMethods(ObjectMethodsDef[] methodDefsArray, String msg) throws Exception {
 	
 		String bDefPID = null;
 		String methodName = null;
 		MethodParmDef[] parms = null;
 		ObjectMethodsDef methodDef = null;
 	        
 		for (int i=0; i<methodDefsArray.length; i++) {
 			methodDef = methodDefsArray[i];
 			bDefPID = methodDef.getBDefPID();
 			methodName = methodDef.getMethodName();
 			parms = methodDef.getMethodParmDefs();
 			System.out.println(msg + " methodDef["+i+"] "
 				 + "bDefPID: "+bDefPID);
 			System.out.println(msg + " methodDef["+i+"] "
 				 + "methodName: '"+methodName+"'");
 			for (int j=0; j<parms.length; j++) {
 				MethodParmDef p = parms[j];	
 				System.out.println(msg + " methodDef["+i+"] "
 				 	+ "parmName["+j+"] "+p.getParmName());
 			}
 		}
 	        
 	}    
     
     //TODO test the rest of APIA
     
     public static void main(String[] args) {
         junit.textui.TestRunner.run(TestAPIA.class);
     }
 
 }
