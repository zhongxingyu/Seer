 package jcommon.extract;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import jcommon.Arch;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import jcommon.OSFamily;
 import jcommon.Path;
 import jcommon.Sys;
 import jcommon.extract.processors.FileProcessor;
 import jcommon.extract.processors.LibraryProcessor;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author David Hoyt <dhoyt@hoytsoft.org>
  */
 public class ResourcesTest {
 	//<editor-fold defaultstate="collapsed" desc="Setup">
 	public ResourcesTest() {
 	}
 
 	@BeforeClass
 	public static void setUpClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() {
 		//assertTrue("These unit tests require Windows to complete", Sys.isOSFamily(OSFamily.Windows));
 	}
 
 	@After
 	public void tearDown() {
 	}
 	//</editor-fold>
 
 	@Test
 	public void testVariables() {
 		String tmpDir = Path.tempDirectory;
 		String homeDir = Path.homeDirectory;
 
 		IVariableProcessor varproc = VariableProcessorFactory.newInstance();
 		assertEquals("/" + tmpDir + "/tmp/" + tmpDir + "/" + homeDir + "/tmp", varproc.process("/${tmp}/tmp/${tmp}/${home}/tmp"));
 
 		assertEquals("/home/", varproc.process("/home/"));
 		assertEquals(tmpDir + "/", varproc.process("${tmp}/"));
 		assertEquals("/" + tmpDir + "/", varproc.process("/${tmp}/"));
 		assertEquals(homeDir + "/", varproc.process("${home}/"));
 		assertEquals("/" + homeDir + "/", varproc.process("/${home}/"));
 	}
 
 	@Test
 	public void testResourceProcessorFactory() {
 		assertFalse(ResourceProcessorFactory.getDefaultProcessors().isEmpty());
 
 		final ResourceProcessorFactory factory = ResourceProcessorFactory.newInstance();
 		assertNotNull(factory);
 
 		assertFalse(factory.addProcessors(ResourcesTest.class));
 	}
 
 	@Test
 	public void testRead() {
 		Resources r1 = Resources.newInstance(
         Sys.createPlatformPackageResourcePrefix("resources.extraction") + "test.xml"
     );
 		assertNotNull(r1);
 
 		assertEquals(1, r1.getTotalPackageCount());
 		assertEquals(7, r1.getTotalResourceCount());
 
 		Resources r2 = Resources.newInstance(
 			Package.newInstance("test", ".")
 		);
 		assertEquals(1, r2.getTotalPackageCount());
 		assertEquals(0, r2.getTotalResourceCount());
 
 		Resources r3 = Resources.newInstance(
 			Package.newInstance("test", "."),
 			Package.newInstance("test", ".")
 		);
 		assertEquals(2, r3.getTotalPackageCount());
 		assertEquals(0, r3.getTotalResourceCount());
 
 		Resources r4 = Resources.newInstance(
 			Package.newInstance(
 				"test",
 				".",
 				ResourceProcessorFactory.newProcessor(FileProcessor.class),
 				ResourceProcessorFactory.newProcessor(FileProcessor.class)
 			),
 			Package.newInstance(
 				"test",
 				".",
 				ResourceProcessorFactory.newProcessor(LibraryProcessor.class),
 				ResourceProcessorFactory.newProcessor(LibraryProcessor.class)
 			)
 		);
 		assertEquals(2, r4.getTotalPackageCount());
 		assertEquals(4, r4.getTotalResourceCount());
 	}
 
 	@Test
 	public void testExtract() throws InterruptedException, ExecutionException {
     if (OSFamily.getSystemOSFamily() != OSFamily.Windows)
       return;
 
 		assertNotNull(Sys.createPlatformName());
 
 		final IVariableProcessor varproc = VariableProcessorFactory.newInstance();
 		final Resources r = Resources.newInstance(
 			varproc,
 			Sys.createPlatformPackageResourceName("resources.extraction", "test.xml")
 		);
 		assertNotNull(r);
 
 		assertEquals("test", varproc.findValue(VariableProcessorFactory.VAR_PKG));
     switch (Arch.getSystemArch()) {
       case x86:
         assertEquals("resources.extraction.windows.x86", varproc.findValue("saved_pkg"));
         break;
       case x86_64:
         assertEquals("resources.extraction.windows.x86_64", varproc.findValue("saved_pkg"));
         break;
     }
 
 		Future f = r.extract(new ResourceProgressListenerAdapter() {
 			@Override
 			public void begin(int totalNumberOfResources, int totalNumberOfPackages, long totalNumberOfBytes, long startTime) {
 				System.out.println("Extraction beginning...");
 			}
 
 			@Override
 			public void reportResourceComplete(final IResourceProcessor resource, final IResourcePackage pkg, final int totalNumberOfResources, final int totalNumberOfPackages, final long totalNumberOfBytes, final long numberOfBytesCompleted, final int numberOfResourcesCompleted, final int numberOfPackagesCompleted, final long startTime, final long duration, final String message) {
 				System.out.println(numberOfPackagesCompleted + "/" + totalNumberOfPackages + " packages completed");
 				System.out.println(numberOfResourcesCompleted + "/" + totalNumberOfResources + " resources completed");
 				System.out.println(numberOfBytesCompleted + "/" + totalNumberOfBytes + " bytes completed");
 				System.out.println();
 			}
 
 			@Override
 			public void reportPackageComplete(final IResourcePackage pkg, final int totalNumberOfResources, final int totalNumberOfPackages, final long totalNumberOfBytes, final long numberOfBytesCompleted, final int numberOfResourcesCompleted, final int numberOfPackagesCompleted, final long startTime, final long duration, final String message) {
 				System.out.println(numberOfPackagesCompleted + "/" + totalNumberOfPackages + " packages completed");
 				System.out.println(numberOfResourcesCompleted + "/" + totalNumberOfResources + " resources completed");
 				System.out.println(numberOfBytesCompleted + "/" + totalNumberOfBytes + " bytes completed");
 				System.out.println();
 			}
 
 			@Override
 			public void end(boolean success, int totalNumberOfResources, int totalNumberOfPackages, long totalNumberOfBytes, long numberOfBytesCompleted, int numberOfResourcesCompleted, int numberOfPackagesCompleted, long startTime, long endTime) {
 				if (success)
 					System.out.println("Operation success");
 				else
 					System.out.println("Operation failed");
 				System.out.println(numberOfPackagesCompleted + "/" + totalNumberOfPackages + " packages completed");
 				System.out.println(numberOfResourcesCompleted + "/" + totalNumberOfResources + " resources completed");
 				System.out.println(numberOfBytesCompleted + "/" + totalNumberOfBytes + " bytes completed");
 				System.out.println();
 			}
 
 			@Override
 			public void error(Throwable exception, String message) {
 				assertTrue(false);
 			}
 		});
 
 		//Wait for operation to complete
 		Object o = f.get();
 		assertTrue(f.isDone() && !f.isCancelled());
 
 		assertTrue(Path.combine(Path.tempDirectory, "jcommon/test/test/test.dll").exists());
 		assertTrue(Path.combine(Path.tempDirectory, "jcommon/test/test/test2.txt").exists());
 
 		assertEquals("test", r.getName());
 		
 		assertFalse(Registry.isEmpty());
 		assertTrue(Registry.hasReference("test"));
 	}
 }
