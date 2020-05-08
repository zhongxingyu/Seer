 package test.pete.metrics.installability;
 
 import static org.junit.Assert.assertEquals;
 
 import java.nio.file.Paths;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import pete.metrics.installability.application.DeploymentPackageAnalyzer;
 import pete.reporting.ReportEntry;
 
 public class DeploymentPackageTests {
 
 	private DeploymentPackageAnalyzer sut;
 
 	private final String resourcePaths = System.getProperty("user.dir")
 			+ "/src/test/resources/installability/";
 
 	@Before
 	public void setUp() {
 		sut = new DeploymentPackageAnalyzer();
 	}
 
 	@Test
 	public void testNonArchive() {
 		ReportEntry result = sut
 				.analyzeFile(Paths.get(resourcePaths + "empty"));
 		assertEquals(0, getPackageComplexity(result));
 		assertEquals(0, getEffortOfPackageConstruction(result));
 		assertEquals(0, getDescriptorComplexity(result));
 	}
 
 	@Test
 	public void testArchiveWithXmlDescriptor() {
 		ReportEntry result = sut.analyzeFile(Paths.get(resourcePaths
 				+ "onlyXmlDescriptor.zip"));
 		assertEquals(14, getPackageComplexity(result));
 		assertEquals(2, getEffortOfPackageConstruction(result));
 		assertEquals(12, getDescriptorComplexity(result));
 	}
 
 	@Test
 	public void testArchiveWithTextFile() {
 		ReportEntry result = sut.analyzeFile(Paths.get(resourcePaths
 				+ "onlyManifest.zip"));
 		assertEquals(7, getPackageComplexity(result));
 		assertEquals(2, getEffortOfPackageConstruction(result));
 		assertEquals(5, getDescriptorComplexity(result));
 	}
 
 	@Test
 	public void testArchiveWithXmlAndText() {
 		ReportEntry result = sut.analyzeFile(Paths.get(resourcePaths
 				+ "xmlAndText.zip"));
 		assertEquals(20, getPackageComplexity(result));
 		assertEquals(3, getEffortOfPackageConstruction(result));
 		assertEquals(17, getDescriptorComplexity(result));
 	}
 
 	@Test
 	public void testArchiveWithXmlAndTextAndIgnores() {
 		ReportEntry result = sut.analyzeFile(Paths.get(resourcePaths
 				+ "xmlAndTextAndIgnores.zip"));
 		assertEquals(20, getPackageComplexity(result));
 		assertEquals(3, getEffortOfPackageConstruction(result));
 		assertEquals(17, getDescriptorComplexity(result));
 	}
 
 	@Test
 	public void testNestedArchives() {
 		ReportEntry result = sut.analyzeFile(Paths.get(resourcePaths
 				+ "nestedArchives.zip"));
 		assertEquals(40, getPackageComplexity(result));
 		assertEquals(6, getEffortOfPackageConstruction(result));
 		assertEquals(34, getDescriptorComplexity(result));
 	}
 
 	@Test
 	public void testComplexArchive() {
 		ReportEntry result = sut.analyzeFile(Paths.get(resourcePaths
 				+ "Sequence.jar"));
 		assertEquals(27, getPackageComplexity(result));
 		assertEquals(5, getEffortOfPackageConstruction(result));
 		assertEquals(22, getDescriptorComplexity(result));
 
 		result = sut.analyzeFile(Paths.get(resourcePaths
 				+ "sun-http-binding.jar"));
 		assertEquals(25, getPackageComplexity(result));
 		assertEquals(6, getEffortOfPackageConstruction(result));
 		assertEquals(19, getDescriptorComplexity(result));
 
 		result = sut.analyzeFile(Paths.get(resourcePaths
 				+ "SequenceApplication.zip"));
 		assertEquals(95, getPackageComplexity(result));
 		assertEquals(15, getEffortOfPackageConstruction(result));
 		assertEquals(80, getDescriptorComplexity(result));
 	}
 
 	private int getPackageComplexity(ReportEntry entry) {
 		if (entry != null) {
 			return Integer
 					.parseInt(entry.getVariableValue("packageComplexity"));
 		} else {
 			return 0;
 		}
 	}
 
 	private int getEffortOfPackageConstruction(ReportEntry entry) {
 		if (entry != null) {
 			return Integer.parseInt(entry.getVariableValue("EPC"));
 		} else {
 			return 0;
 		}
 	}
 
 	private int getDescriptorComplexity(ReportEntry entry) {
 		if (entry != null) {
			return Integer.parseInt(entry.getVariableValue("DDC"));
 		} else {
 			return 0;
 		}
 	}
 
 }
