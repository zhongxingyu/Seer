 /**
  * 
  */
 package org.jasig.portal.web.skin;
 
 import java.io.File;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
import org.junit.After;
 import org.junit.Assert;
 import org.junit.Test;
 import org.springframework.core.io.ClassPathResource;
 
 /**
  * Test harness for {@link ResourcesAggregatorImpl}.
  * 
  * @author Nicholas Blair, npblair@wisc.edu
  *
  */
 public class ResourcesAggregatorImplTest {
 
 	@Test
 	public void testControl() throws Exception {
 		String tempPath = getTestOutputRoot() + "/skin-test1";
 
 		File outputDirectory = new File(tempPath);
 		outputDirectory.mkdirs();
 		Assert.assertTrue(outputDirectory.exists());
 
 		File skinXml = new ClassPathResource("skin-test1/skin.xml").getFile();
 		Assert.assertTrue(skinXml.exists());
 
 		ResourcesAggregatorImpl impl = new ResourcesAggregatorImpl();
 		Resources result = impl.aggregate(skinXml, outputDirectory);
 		Assert.assertEquals(1, result.getCss().size());
 		Assert.assertEquals(1, result.getJs().size());
 		Css aggrCss = result.getCss().get(0);
 		Assert.assertTrue(aggrCss.getValue().startsWith("uportal3_aggr1"));
 		Js aggrJs = result.getJs().get(0);
 		Assert.assertTrue(aggrJs.getValue().startsWith("uportal3_aggr2"));
 
 		File outputCss = new File(outputDirectory, aggrCss.getValue());
 		Assert.assertTrue(outputCss.exists());
 		String outputCssContent = FileUtils.readFileToString(outputCss);
 		Assert.assertEquals(".selector{color:red;}.otherselector{color:green;}", outputCssContent);
 		
 		File outputJs = new File(outputDirectory, aggrJs.getValue());
 		Assert.assertTrue(outputJs.exists());
 	}
 
 	/**
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testAllAbsolute() throws Exception {
 		String tempPath = getTestOutputRoot() + "/skin-testAllAbsolute";
 
 		File outputDirectory = new File(tempPath);
 		outputDirectory.mkdirs();
 		Assert.assertTrue(outputDirectory.exists());
 
 		File skinXml = new ClassPathResource("skin-testAllAbsolute/skin.xml").getFile();
 		Assert.assertTrue(skinXml.exists());
 
 		ResourcesAggregatorImpl impl = new ResourcesAggregatorImpl();
 
 		Resources result = impl.aggregate(skinXml, outputDirectory);
 		Assert.assertEquals(2, result.getCss().size());
 		Assert.assertEquals(2, result.getJs().size());
 		Assert.assertEquals("/a.css", result.getCss().get(0).getValue());
 		Assert.assertEquals("/b.css", result.getCss().get(1).getValue());
 		Assert.assertEquals("/a.js", result.getJs().get(0).getValue());
 		Assert.assertEquals("/b.js", result.getJs().get(1).getValue());
 
 		// there should only be one output file - the skin-aggr.xml 
 		Assert.assertEquals(1, outputDirectory.list().length);
 		Assert.assertEquals("uportal3_aggr.skin.xml", outputDirectory.list()[0]);
 	}
 	
 	/**
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testComplex() throws Exception {
 		String tempPath = getTestOutputRoot() + "/skin-complex/superskin";
 		
 		File outputDirectory = new File(tempPath);
 		outputDirectory.mkdirs();
 		Assert.assertTrue(outputDirectory.exists());
 
 		File skinXml = new ClassPathResource("skin-complex/superskin/skin.xml").getFile();
 		Assert.assertTrue(skinXml.exists());
 
 		ResourcesAggregatorImpl impl = new ResourcesAggregatorImpl();
 		Resources result = impl.aggregate(skinXml, outputDirectory);
 		
 		Assert.assertEquals(5, result.getCss().size());
 		// 1st element was aggregated
 		Assert.assertTrue(result.getCss().get(0).getValue().startsWith("../common/css/"));
 		Assert.assertTrue(result.getCss().get(0).getValue().contains("uportal3_aggr"));
 		
 		// 2nd is conditional
 		Assert.assertTrue(result.getCss().get(1).isConditional());
 		Assert.assertEquals("condition!", result.getCss().get(1).getConditional());
 		
 		// 3rd and 4th are absolutes
 		Assert.assertTrue(result.getCss().get(2).isAbsolute());
 		Assert.assertEquals("/ResourceServingWebapp/rs/1.css", result.getCss().get(2).getValue());
 		Assert.assertTrue(result.getCss().get(3).isAbsolute());
 		Assert.assertEquals("/ResourceServingWebapp/rs/2.css", result.getCss().get(3).getValue());
 		
 		// 5th element was aggregated and has media set
 		Assert.assertTrue(result.getCss().get(4).getValue().startsWith("css/"));
 		Assert.assertTrue(result.getCss().get(4).getValue().contains("uportal3_aggr"));
 		Assert.assertEquals("alternate", result.getCss().get(4).getMedia());
 		
 		Assert.assertEquals(4, result.getJs().size());
 		// 1st aggregated
 		Assert.assertTrue(result.getJs().get(0).getValue().startsWith("../common/js/"));
 		Assert.assertTrue(result.getJs().get(0).getValue().contains("uportal3_aggr"));
 		// 2nd absolute
 		Assert.assertTrue(result.getJs().get(1).isAbsolute());
 		Assert.assertEquals("/universal.js", result.getJs().get(1).getValue());
 		// 3rd aggregated with conditional
 		Assert.assertTrue(result.getJs().get(2).getValue().startsWith("js/"));
 		Assert.assertTrue(result.getJs().get(2).getValue().contains("uportal3_aggr"));
 		// 4th is compressed
 		Assert.assertTrue(result.getJs().get(3).getValue().contains("js/c-compressed.js"));
 		Assert.assertTrue(result.getJs().get(3).isCompressed());
 	}
 	
 	/**
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testUniversality() throws Exception {
 		String tempPath = getTestOutputRoot() + "/skin-universality/uportal3";
 		
 		File outputDirectory = new File(tempPath);
 		outputDirectory.mkdirs();
 		Assert.assertTrue(outputDirectory.exists());
 
 		File skinXml = new ClassPathResource("skin-universality/uportal3/skin.xml").getFile();
 		Assert.assertTrue(skinXml.exists());
 		
 		ResourcesAggregatorImpl impl = new ResourcesAggregatorImpl();
 		Resources result = impl.aggregate(skinXml, outputDirectory);
 		Assert.assertNotNull(result);
 		List<Css> cssList = result.getCss();
 		Assert.assertEquals(6, cssList.size());
 		List<Js> jsList = result.getJs();
 		Assert.assertEquals(1, jsList.size());
		Assert.assertTrue(new File(getTestOutputRoot() + "/skin-universality/common/css/fluid/uportal3_aggr1_A3661D3474000B0B06BC01EA644DBE07.css").exists());
		Assert.assertTrue(new File(getTestOutputRoot() + "/skin-universality/common/css/uportal3_aggr2_0A62110C5DBE25EECD978B41EE455466.css").exists());
		Assert.assertTrue(new File(getTestOutputRoot() + "/skin-universality/common/css/uportal3_aggr2_0A62110C5DBE25EECD978B41EE455466.css").exists());
		Assert.assertTrue(new File(getTestOutputRoot() + "/skin-universality/uportal3/uportal3_aggr3_3334333FF8A41D7D6BCE5C8AE5B71B4A.css").exists());
		Assert.assertTrue(new File(getTestOutputRoot() + "/skin-universality/uportal3/uportal3_aggr5_0EC69539BB6BA6C8B611BAC539C67794.css").exists());
		Assert.assertTrue(new File(getTestOutputRoot() + "/skin-universality/uportal3/uportal3_aggr6_F21DFDA90E2DFAEB81BC098A037A458C.css").exists());
		Assert.assertTrue(new File(getTestOutputRoot() + "/skin-universality/common/javascript/uportal/uportal3_aggr7_158C92140AC7355300F2708F20D66DB2.js").exists());
 	}
 
 	/**
 	 * Delete our temp directory after test execution.
 	 * @throws Exception
 	 */
	@After
 	public void cleanupTempDir() throws Exception {
 		File testOutputDirectory = new File(getTestOutputRoot());
 		FileUtils.cleanDirectory(testOutputDirectory);
 		FileUtils.deleteDirectory(testOutputDirectory);
 	}
 
 	/**
 	 * Shortcut to get a temporary directory underneath java.io.tmpdir.
 	 * 
 	 * @return
 	 */
 	private String getTestOutputRoot() {
 		String tempPath = System.getProperty("java.io.tmpdir");
 		if(!tempPath.endsWith("/")) {
 			tempPath += "/";
 		}
 		tempPath = tempPath + "resources-aggregator-impl-test-output";
 		return tempPath;
 	}
 }
