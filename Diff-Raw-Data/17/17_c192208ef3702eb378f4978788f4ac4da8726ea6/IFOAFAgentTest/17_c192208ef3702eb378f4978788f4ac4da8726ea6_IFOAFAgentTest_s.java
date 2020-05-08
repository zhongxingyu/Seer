 package to.networld.scrawler.tests;
 
 import java.net.URL;
 import java.util.Vector;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import to.networld.scrawler.foaf.Person;
 import to.networld.scrawler.interfaces.IFOAFPerson;
 
 /**
  * @author Alex Oberhauser
  *
  */
 public class IFOAFAgentTest {
 	private IFOAFPerson testAgent;
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
		this.testAgent = new Person(new URL("http://devnull.networld.to/foaf.rdf"));
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 		this.testAgent = null;
 	}
	
 	/**
 	 * XXX: If the FOAF file changes than maybe the test fails. Please be patient.
 	 */
 	@Test
 	public void testPersonalFOAFFileFirst() {
 		Assert.assertEquals("Alex Oberhauser", this.testAgent.getName());
 		Assert.assertEquals("Male", this.testAgent.getGender());
		Assert.assertEquals("http://obale.myopenid.com/", this.testAgent.getOpenid());
		Assert.assertEquals("http://devnull.networld.to/", this.testAgent.getWebsite());
		Assert.assertEquals("http://networld.to/", this.testAgent.getWeblog());
		Assert.assertEquals("http://informatik.uibk.ac.at", this.testAgent.getSchoolHomepage());
		Assert.assertEquals("http://sti2.at", this.testAgent.getWorkplaceHomepage());
		Assert.assertEquals("[47.263348, 11.346295]", this.testAgent.getLocation().toString());
 		Assert.assertEquals("http://scubadive.networld.to/padi.rdf#AOW", this.testAgent.getDiveCertificate());
		
 		Vector<String> friendVector = this.testAgent.getKnownAgents();
 		for ( String entry : friendVector ) {
 			Assert.assertNotNull(entry);
 		}
 	}
 }
