 package location.rnd.app.test;
 
 import junit.framework.TestCase;
 import location.rnd.app.Label;
 import location.rnd.app.LocationType;
 
 public class LocationTypeTest extends TestCase {
 
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	public void testLocationType() {
 		LocationType lt = new LocationType(1, new Label("Country", "en_US"));
 		assertNotNull(lt);
 	}
 
 	public void testGetLevel() {
 		LocationType lt = new LocationType(1, new Label("Country", "en_US"));
 		assertEquals(1, lt.getLevel());
 	}
 
 	public void testGetDefaultLabel() {
 		Label label = new Label("Country", "en_US");
 		LocationType lt = new LocationType(1, label);
 		Label defaultLabel = lt.getDefaultlabel();
 		assertNotNull(defaultLabel);
 		assertEquals(label, defaultLabel);
 		
 		
 	}
 
 	public void testAddLabel() {
 		LocationType lt = new LocationType(1, new Label("Country", "en_US"));
 		Label label = new Label("Pais", "pt_BR");
 		lt.addLabel(label);
		assertEquals(1, lt.getLabels().size());
 	}
 
 	public void testGetLabels() {
 		LocationType lt = new LocationType(1, new Label("Country", "en_US"));
 		Label label = new Label("Country", "en_AU");
 		Label label2 = new Label("Country", "en_GB");
 		Label label3 = new Label("Pais", "pt_BR");
 		lt.addLabel(label);
 		lt.addLabel(label2);
 		lt.addLabel(label3);
 		
		assertEquals(3, lt.getLabels().size());
 		
		assertEquals(label,lt.getLabels().get(0));
		assertEquals(label2,lt.getLabels().get(1));
		assertEquals(label3,lt.getLabels().get(2));
 		
 	}
 
 }
