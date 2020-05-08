 package it.com.adaptavist.confluence.pageFamilyTagCloud;
 
 import com.atlassian.confluence.plugin.functest.AbstractConfluencePluginWebTestCase;
 import com.atlassian.confluence.plugin.functest.helper.IndexHelper;
 import com.atlassian.confluence.plugin.functest.helper.PageHelper;
 import com.atlassian.confluence.plugin.functest.helper.SpaceHelper;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 
 public class PageFamilyTagCloudTest extends AbstractConfluencePluginWebTestCase
 {
     private String TESTSPACE = "TESTSPACE";
     
     private static final String HEATMAP_XPATH = "//div[@class='heatmap pagefamilytagcloud']";
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
         SpaceHelper spaceHelper = getSpaceHelper();
         spaceHelper.setKey(TESTSPACE);
         spaceHelper.setName("TEST SPACE NAME");
         spaceHelper.setDescription("This is the description of the space");
         assertTrue("Error creating test space.",spaceHelper.create());
 
     }
 
     @Override
     public void tearDown() throws Exception {
         SpaceHelper spaceHelper = getSpaceHelper();
         spaceHelper.setKey(TESTSPACE);
         assertTrue("Error deleting " + TESTSPACE, spaceHelper.delete());
         super.tearDown();
     }
 
     protected long createPage(String spaceKey, String title, String content, long parentId, List<String> labels)
     {
         PageHelper helper = getPageHelper();
 
         helper.setSpaceKey(spaceKey);
         helper.setTitle(title);
         helper.setContent(content);
         if(labels != null)
             helper.setLabels(labels);
         if(parentId > 0)
             helper.setParentId(parentId);
         assertTrue(helper.create());
 
         // return the generated id for the new page
         return helper.getId();
     }
 
     private void createTestData(String macro)
     {
         long parentId = createPage(TESTSPACE,"Root Page",macro,0,Arrays.asList("rootlabel"));
         createPage(TESTSPACE,"Child Page 1","Test Page",parentId, Arrays.asList("biglabel"));
         createPage(TESTSPACE,"Child Page 2","Test Page",parentId,Arrays.asList("biglabel","smalllabel"));
         createPage(TESTSPACE,"Child Page 3","Test Page",parentId,Arrays.asList("biglabel"));
         createPage(TESTSPACE,"Child Page 4","Test Page",parentId,Arrays.asList("biglabel"));
         createPage(TESTSPACE,"Child Page 5","Test Page",parentId,Arrays.asList("biglabel"));
         createPage(TESTSPACE,"Child Page 6","Test Page",parentId,Arrays.asList("xlabel"));
         createPage(TESTSPACE,"Child Page 7","Test Page",parentId,Arrays.asList("xlabel"));
         createPage(TESTSPACE,"Child Page 8","Test Page",parentId,Arrays.asList("xlabel"));
 
         IndexHelper indexHelper = getIndexHelper();
         indexHelper.update();
 
         gotoPage("display/" + TESTSPACE + "/Root+Page");
     }
     
     protected void assertDefaultTestDataLabels(){
         String bigLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[1]");
         String rootLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[2]");
         String smallLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[3]");
         String xLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[4]");
 
         assertEquals("biglabel",bigLabel);
         assertEquals("smalllabel",smallLabel);
         assertEquals("rootlabel",rootLabel);
         assertEquals("xlabel", xLabel);
         
     }
 
     public void testBasicMacroUsage()
     {
         createTestData("{pagefamily-tagcloud}");
         assertDefaultTestDataLabels();
         
     }
 
     public void testIncludeRootPageLabels()
     {
         createTestData("{pagefamily-tagcloud:includeRootPage=true}");
 
         assertDefaultTestDataLabels();
     }
 
     public void testIncludeFalseRootPageLabels()
     {
         createTestData("{pagefamily-tagcloud:includeRootPage=false}");
 
         String bigLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[1]");
         String smallLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[2]");
         String xLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[3]");
 
         assertEquals("biglabel",bigLabel);
         assertEquals("smalllabel",smallLabel);
         assertEquals("xlabel", xLabel);
         
         assertElementNotPresentByXPath(HEATMAP_XPATH + "/ul/li[4]");
     }
 
     public void testInvalidSortByLabel()
     {
         createTestData("{pagefamily-tagcloud:sort=madeup}");
 
         // check the sorting uses count by default - not very intuitive since
         // label sort is normally the default
         String rootLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[1]");
         String smallLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[2]");
         String xLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[3]");
         String bigLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[4]");
         
         assertEquals("rootlabel",rootLabel);
         assertEquals("biglabel",bigLabel);
         assertEquals("smalllabel",smallLabel);
         assertEquals("xlabel",xLabel);
     }
 
     public void testSortByLabel()
     {
         createTestData("{pagefamily-tagcloud:sort=label}");
         
         assertDefaultTestDataLabels();
     }
 
     public void testSortByCount()
     {
         createTestData("{pagefamily-tagcloud:sort=count}");
         
         String rootLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[1]");
         String smallLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[2]");
         String xLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[3]");
         String bigLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[4]");
 
         assertEquals("rootlabel",rootLabel);
         assertEquals("biglabel",bigLabel);
         assertEquals("smalllabel",smallLabel);
         assertEquals("xlabel",xLabel);
     }
 
     public void testReverseSortByLabel()
     {
         createTestData("{pagefamily-tagcloud:sort=label|reverse=true}");
 
         
         String bigLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[4]");
         String rootLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[3]");
         String smallLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[2]");
         String xLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[1]");
 
         assertEquals("rootlabel",rootLabel);
         assertEquals("biglabel",bigLabel);
         assertEquals("smalllabel",smallLabel);
         assertEquals("xlabel",xLabel);
     }
 
     public void testReverseSortByCount()
     {
         createTestData("{pagefamily-tagcloud:sort=count|reverse=true}");
         
         String rootLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[4]");
         String smallLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[3]");
         String xLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[2]");
         String bigLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[1]");
 
         assertEquals("rootlabel",rootLabel);
         assertEquals("biglabel",bigLabel);
         assertEquals("smalllabel",smallLabel);
         assertEquals("xlabel",xLabel);
     }
 
     public void testMaxLabels()
     {
         createTestData("{pagefamily-tagcloud:max=1}");
         
         String bigLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[1]");
 
         assertEquals("biglabel",bigLabel);
         assertTextNotPresent("smalllabel");
         assertTextNotPresent("xlabel");
         assertElementNotPresentByXPath(HEATMAP_XPATH + "/ul/li[2]");
     }
 
     public void testInvalidMaxLabels(){
         createTestData("{pagefamily-tagcloud:max=-1}");
         
         assertTextPresent("The max labels param is invalid");
         assertElementNotPresentByXPath(HEATMAP_XPATH);
     }
 
     public void testNotNumberMaxLabels()
     {
         createTestData("{pagefamily-tagcloud:max=five}");
 
         assertTextPresent("The max labels param is invalid");
         assertElementNotPresentByXPath(HEATMAP_XPATH);
         
     }
 
     public void testLabelLink(){
         createTestData("{pagefamily-tagcloud:labelLink=/test-label/%label% }");
         
         String labelUrl = getElementAttributByXPath(HEATMAP_XPATH + "/ul/li[1]/a","href");
         assertEquals("/test-label/biglabel",labelUrl);
     }
     
     public void testLabelLink_XSS(){
         createTestData("{pagefamily-tagcloud:labelLink=\">Uh Oh/test-label/%label% }");
         
         assertTextNotPresent("Uh Oh");
     }
 
     public void testRootPage()
     {
         createTestData("{pagefamily-tagcloud:rootPage=Child Page 7}");
 
         String xLabel = getElementTextByXPath(HEATMAP_XPATH + "/ul/li[1]");
         
         assertEquals("xlabel",xLabel);
         assertTextNotPresent("smalllabel");
         assertTextNotPresent("biglabel");
     }
 
     public void testInvalidRootPage()
     {
         createTestData("{pagefamily-tagcloud:rootPage=Does Not Exist}");
         assertTextPresent("Could not find a page called: Does Not Exist in space");
     }
 
 }
