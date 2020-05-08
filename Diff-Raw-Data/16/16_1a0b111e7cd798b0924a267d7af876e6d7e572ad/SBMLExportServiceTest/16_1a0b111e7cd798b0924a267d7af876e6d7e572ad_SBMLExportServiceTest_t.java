 package uk.ed.ac.inf.Metabolic.sbmlexport;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.pathwayeditor.businessobjectsAPI.IMap;
 import org.pathwayeditor.businessobjectsAPI.IRootMapObject;
 import org.pathwayeditor.contextadapter.publicapi.ExportServiceException;
 import org.pathwayeditor.contextadapter.publicapi.IContext;
 @RunWith(JMock.class)
 public class SBMLExportServiceTest {
 
 	Mockery mockery = new JUnit4Mockery();
 	
 	final IContext context = mockery.mock(IContext.class);
 	final IMap map = mockery.mock(IMap.class);
 	final IRootMapObject rmo = mockery.mock(IRootMapObject.class);
 	final ISBMLGenerator generator = mockery.mock(ISBMLGenerator.class);
 	SBMLExportService service;
     File NONEXISTENT = new File ("??");
     File EXISTENT;
    static boolean canRun = false; // check this is true b4 running export tests.
    
     
    //uk.ac.ed.inf.csb.Metabolic
     @BeforeClass 
     public static void loadNativeLibraries () throws Exception {
     	canRun = LibSBMLConfigManager.configure();
     	
     }
  
 	@Before
 	public void setUp() throws Exception {
 		service = new SBMLExportService(context);
 		 EXISTENT = new File ("SBMLoutput");
 	
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		EXISTENT.delete();
 	}
 
 	@Test(expected=IllegalArgumentException.class) 
 	public void testExportMapThrowsIllegalArgumentIfmapisNull() throws Throwable {
         service.exportMap(null, new File("anything"));
 	}
 	
 	@Test(expected=IllegalArgumentException.class) 
 	public void testExportMapThrowsIllegalArgumentIffileisNull() throws Throwable {
         service.exportMap(map, null);
 	}
 	
 	@Test(expected=ExportServiceException.class) 
 	public void testExportMapThrowsRuntimeExeptionIffileDoesnotExist() throws Throwable {
 		mockery.checking(new Expectations () {
 			{one(map).getTheSingleRootMapObject();}
 			{will(returnValue(rmo));}
 		});
         service.exportMap(map, NONEXISTENT);
 	}
 	
 	@Test
 	public void testExportMap() throws Throwable {
 		if(!canRun){
 			return;
 		}
 		mockery.checking(new Expectations () {
 			{atLeast(1).of(map).getTheSingleRootMapObject();}
 			{will(returnValue(rmo));}
 		//	{one(generator).generateSBMLModel(rmo);}
 			
 		});
 		EXISTENT.createNewFile();
         service.exportMap(map, EXISTENT);
 	}
  
 	@Test
 	public void testGetCode() {
 		assertEquals(service.TYPECODE, service.getCode());
 	}
 
 	@Test
 	public void testGetContext() {
 		assertEquals(context, service.getContext());
 	}
 
 	@Test
 	public void testGetDisplayName() {
 		assertEquals(service.DISPLAY_NAME, service.getDisplayName());
 	}
 
 	@Test
 	public void testGetRecommendedSuffix() {
 		assertEquals("sbml", service.getRecommendedSuffix());
 	}
 
 	@Test
 	public void testToString() {
 		assertTrue(service.toString().contains(service.DISPLAY_NAME));
 		assertTrue(service.toString().contains(service.TYPECODE));
 	}
 
 }
