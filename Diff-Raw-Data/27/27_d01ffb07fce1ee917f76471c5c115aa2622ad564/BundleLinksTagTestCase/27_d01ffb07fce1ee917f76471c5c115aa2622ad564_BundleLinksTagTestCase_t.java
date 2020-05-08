 package au.com.sensis.mobile.crf.presentation.tag;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.tagext.JspFragment;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.easymock.EasyMock;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.mock.web.MockJspWriter;
 import org.springframework.mock.web.MockServletContext;
 import org.springframework.web.context.WebApplicationContext;
 
 import au.com.sensis.mobile.crf.config.DeploymentMetadataTestData;
 import au.com.sensis.mobile.crf.service.Resource;
 import au.com.sensis.mobile.crf.service.ResourcePathTestData;
 import au.com.sensis.mobile.crf.service.ResourceResolutionWarnLogger;
 import au.com.sensis.mobile.crf.util.MD5Builder;
 import au.com.sensis.wireless.test.AbstractJUnit4TestCase;
 
 /**
  * Unit test {@link BundleLinksTag}.
  *
  * @author Adrian.Koh2@sensis.com.au
  */
 public class BundleLinksTagTestCase extends AbstractJUnit4TestCase {
 
     private static final String TAG_ID = "myId";
     private static final String DYN_ATTR_URI = "http://www.w3.org/2002/06/xhtml2";
 
     private BundleLinksTag objectUnderTest;
 
     private final DeploymentMetadataTestData deploymentMetadataTestData
         = new DeploymentMetadataTestData();
     private BundleTagDependencies bundleScriptsTagDependencies;
     private final ResourcePathTestData resourcePathTestData = new ResourcePathTestData();
 
     private JspWriter springMockJspWriter;
     private JspFragment mockJspFragment;
     private StringWriter stringWriter;
     private Resource mockResource1;
     private Resource mockResource2;
     private ResourceResolutionWarnLogger mockResolutionWarnLogger;
     private PageContext mockPageContext;
     private WebApplicationContext mockWebApplicationContext;
 
     private MockServletContext springMockServletContext;
 
     private BundleTagCache mockBundleLinksTagCache;
 
     private File sourceBundle1NewFile;
     private String sourceBundle1NewPath;
     private File sourceBundle2NewFile;
     private String sourceBundle2NewPath;
     private File appBundlesRootDir;
 
     /**
      * Setup test data.
      *
      * @throws Exception
      *             Thrown if any error occurs.
      */
     @Before
     public void setUp() throws Exception {
         setObjectUnderTest(new BundleLinksTag());
 
         getObjectUnderTest().setId(TAG_ID);
         getObjectUnderTest().setJspBody(getMockJspFragment());
         getObjectUnderTest().setJspContext(getMockPageContext());
 
         setStringWriter(new StringWriter());
         setSpringMockJspWriter(new MockJspWriter(getStringWriter()));
 
         setSpringMockServletContext(new MockServletContext());
 
         setSourceBundle1NewFile(new ClassPathResource(
                 "/au/com/sensis/mobile/crf/presentation/tag/sourceBundle1.css").getFile());
         setSourceBundle1NewPath(
                 "default/core/util/bundle/c21f969b5f03d33d43e04f8f136e7682/utils.css");
 
         setSourceBundle2NewFile(new ClassPathResource(
                 "/au/com/sensis/mobile/crf/presentation/tag/sourceBundle2.css").getFile());
         setSourceBundle2NewPath(
                 "nonIEPC/comp/map/bundle/c0c80a0e534cf880884b00610da878aa/map.css");
 
         setAppBundlesRootDir(new ClassPathResource(
                 "/au/com/sensis/mobile/crf/presentation/tag/").getFile());
 
         setBundleLinksTagDependencies(createBundleLinksTagDependencies());
     }
 
     private String createExpectedOutputBundleClientPath() throws NoSuchAlgorithmException {
         return getBundleLinksTagDependencies().getClientPathPrefix()
                 + createExpectedOutputBundleBasePath();
     }
 
     private File createExpectedOutputBundleFile() throws NoSuchAlgorithmException {
         return new File(getBundleLinksTagDependencies().getRootResourcesDir(),
                 createExpectedOutputBundleBasePath());
     }
 
     private String createExpectedOutputBundleBasePath() throws NoSuchAlgorithmException {
         // Yeah, we rely on the exact same class that the object under test will use but
         // how else would you code this test? We could encode the md5 explicitly in a string
         // but it wouldn't really convey what's supposed to happen.
         final MD5Builder md5Builder = new MD5Builder();
 
         md5Builder.add(getSourceBundle1NewPath());
         md5Builder.add(getSourceBundle2NewPath());
 
         return getBundleLinksTagDependencies().getDeploymentMetadata().getVersion()
             + "/appBundles/myId-" + md5Builder.getSumAsHex() + "-package.css";
     }
 
     @Test
     public void testDoTagWhenResourcesToBundleAndNoDynamicTagAttributes() throws Exception {
         getMockJspFragment().invoke(null);
 
         recordGetTagDependencies();
 
         recordCheckCachedResources(false);
 
         recordBehaviourForBundleCreation();
 
         recordBehaviourForClientSrcPathCreation();
 
         recordBehaviourForWritingScriptTag();
 
         recordUpdateCache();
 
         replay();
 
         getObjectUnderTest().addResourcesToBundle(
                 Arrays.asList(getMockResource1(), getMockResource2()));
         getObjectUnderTest().doTag();
 
         Assert.assertEquals("Script incorrectly written", "<link id=\"myId\" href=\""
                 + createExpectedOutputBundleClientPath() + "\" rel=\"stylesheet\" "
                + "type=\"text/css\" />", getStringWriter().getBuffer().toString());
 
         assertBundleFileCorrect();
     }
 
     private void recordUpdateCache() throws NoSuchAlgorithmException {
         final BundleTagCacheKeyBean keyBean = createCacheKey();
         getMockBundleLinksTagCache().put(keyBean, createExpectedOutputBundleClientPath());
     }
 
     private void recordCheckCachedResources(final boolean resourcesCached)
             throws NoSuchAlgorithmException {
         final BundleTagCacheKeyBean keyBean = createCacheKey();
         EasyMock.expect(getMockBundleLinksTagCache().contains(keyBean))
                 .andReturn(resourcesCached);
 
         if (resourcesCached) {
             EasyMock.expect(getMockBundleLinksTagCache().get(keyBean)).andReturn(
                     createExpectedOutputBundleClientPath());
         }
 
     }
 
     private BundleTagCacheKeyBean createCacheKey() {
         final BundleTagCacheKeyBean keyBean = new BundleTagCacheKeyBean(TAG_ID,
                 new Resource[] { getMockResource1(), getMockResource2() });
         return keyBean;
     }
 
     private void assertBundleFileCorrect() throws NoSuchAlgorithmException, IOException {
         Assert.assertTrue("Output bundle file not found: '" + createExpectedOutputBundleFile()
                 + "'", createExpectedOutputBundleFile().exists());
 
         FileReader expectedOutputFileReader = null;
         String actualOutputBundleContents;
         try {
             expectedOutputFileReader = new FileReader(createExpectedOutputBundleFile());
             actualOutputBundleContents = IOUtils.toString(expectedOutputFileReader);
         } finally {
             if (expectedOutputFileReader != null) {
                 expectedOutputFileReader.close();
             }
         }
 
         Assert.assertTrue("Bundle content doesn't contain bundle 1", actualOutputBundleContents
                 .contains("cssSourceBundle1"));
         Assert.assertTrue("Bundle content doesn't contain bundle 2", actualOutputBundleContents
                 .contains("cssSourceBundle2"));
     }
 
     private void recordBehaviourForWritingScriptTag() {
         EasyMock.expect(getMockPageContext().getOut())
             .andReturn(getSpringMockJspWriter())
             .atLeastOnce();
     }
 
     private void recordBehaviourForClientSrcPathCreation() {
         EasyMock.expect(getMockResource1().getNewPath()).andReturn(getSourceBundle1NewPath())
                 .atLeastOnce();
         EasyMock.expect(getMockResource2().getNewPath()).andReturn(getSourceBundle2NewPath())
                 .atLeastOnce();
 
     }
 
     private void recordBehaviourForBundleCreation() {
         EasyMock.expect(getMockResource1().getNewFile()).andReturn(getSourceBundle1NewFile());
         EasyMock.expect(getMockResource2().getNewFile()).andReturn(getSourceBundle2NewFile());
 
     }
 
     @Test
     public void testDoTagWhenResourcesToBundleAndRelDynamicTagAttribute() throws Exception {
         getMockJspFragment().invoke(null);
 
         recordGetTagDependencies();
 
         recordCheckCachedResources(false);
 
         recordBehaviourForBundleCreation();
 
         recordBehaviourForClientSrcPathCreation();
 
         recordBehaviourForWritingScriptTag();
 
         recordUpdateCache();
 
         replay();
 
         getObjectUnderTest().addResourcesToBundle(
                 Arrays.asList(getMockResource1(), getMockResource2()));
         getObjectUnderTest().setDynamicAttribute(DYN_ATTR_URI, "rel", "Alternate StyleSheet");
         getObjectUnderTest().doTag();
 
         Assert.assertEquals("Script incorrectly written", "<link id=\"myId\" href=\""
                 + createExpectedOutputBundleClientPath() + "\" rel=\"Alternate StyleSheet\" "
                + "type=\"text/css\" />", getStringWriter().getBuffer().toString());
 
         assertBundleFileCorrect();
 
     }
 
     @Test
     public void testDoTagWhenResourcesToBundleAndTypeDynamicTagAttribute() throws Exception {
         getMockJspFragment().invoke(null);
 
         recordGetTagDependencies();
 
         recordCheckCachedResources(false);
 
         recordBehaviourForBundleCreation();
 
         recordBehaviourForClientSrcPathCreation();
 
         recordBehaviourForWritingScriptTag();
 
         recordUpdateCache();
 
         replay();
 
         getObjectUnderTest().addResourcesToBundle(
                 Arrays.asList(getMockResource1(), getMockResource2()));
         getObjectUnderTest().setDynamicAttribute(DYN_ATTR_URI, "type", "text/funkycss");
         getObjectUnderTest().doTag();
 
         Assert.assertEquals("Script incorrectly written", "<link id=\"myId\" href=\""
                 + createExpectedOutputBundleClientPath()
                + "\" type=\"text/funkycss\" " + "rel=\"stylesheet\" />",
                 getStringWriter().getBuffer().toString());
 
         assertBundleFileCorrect();
     }
 
     @Test
     public void testDoTagWhenResourcesToBundleAreCached() throws Exception {
         getMockJspFragment().invoke(null);
 
         recordGetTagDependencies();
 
         recordCheckCachedResources(true);
 
         recordBehaviourForWritingScriptTag();
 
         replay();
 
         getObjectUnderTest().addResourcesToBundle(
                 Arrays.asList(getMockResource1(), getMockResource2()));
         getObjectUnderTest().doTag();
 
         Assert.assertEquals("Script incorrectly written", "<link id=\"myId\" href=\""
                 + createExpectedOutputBundleClientPath() + "\" rel=\"stylesheet\" "
                + "type=\"text/css\" />", getStringWriter().getBuffer().toString());
 
         assertBundleFileCorrect();
     }
 
     @Test
     public void testDoTagWhenNoResourcesToBundle() throws Exception {
         getMockJspFragment().invoke(null);
 
         replay();
 
         getObjectUnderTest().doTag();
 
         Assert.assertEquals("Tag should not have written anything to the page", StringUtils.EMPTY,
                 getStringWriter().getBuffer().toString());
     }
 
     private void recordGetTagDependencies() {
 
         EasyMock.expect(getMockPageContext().getServletContext()).andReturn(
                 getSpringMockServletContext()).atLeastOnce();
 
         getSpringMockServletContext().setAttribute(
                 WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                 getMockWebApplicationContext());
 
         EasyMock.expect(
                 getMockWebApplicationContext().getBean("crf.bundleLinksTagDependencies"))
                 .andReturn(getBundleLinksTagDependencies())
                 .atLeastOnce();
     }
 
     private BundleTagDependencies createBundleLinksTagDependencies() {
         return new BundleTagDependencies(
                 getDeploymentMetadataTestData().createDevDeploymentMetadata(),
                 getResourcePathTestData().getAppBundleClientPathPrefix(),
                 getMockResolutionWarnLogger(), getMockBundleLinksTagCache(),
                 getAppBundlesRootDir());
     }
 
     /**
      * @return the objectUnderTest
      */
     private BundleLinksTag getObjectUnderTest() {
         return objectUnderTest;
     }
 
     /**
      * @param objectUnderTest the objectUnderTest to set
      */
     private void setObjectUnderTest(final BundleLinksTag objectUnderTest) {
         this.objectUnderTest = objectUnderTest;
     }
 
     /**
      * @return the mockJspFragment
      */
     public JspFragment getMockJspFragment() {
         return mockJspFragment;
     }
 
     /**
      * @param mockJspFragment the mockJspFragment to set
      */
     public void setMockJspFragment(final JspFragment mockJspFragment) {
         this.mockJspFragment = mockJspFragment;
     }
 
     /**
      * @return the mockJspWriter
      */
     private JspWriter getSpringMockJspWriter() {
         return springMockJspWriter;
     }
 
     /**
      * @param springMockJspWriter the {@link MockJspWriter} to set.
      */
     private void setSpringMockJspWriter(final JspWriter springMockJspWriter) {
         this.springMockJspWriter = springMockJspWriter;
     }
 
     /**
      * @return the stringWriter
      */
     private StringWriter getStringWriter() {
         return stringWriter;
     }
 
     /**
      * @param stringWriter the stringWriter to set
      */
     private void setStringWriter(final StringWriter stringWriter) {
         this.stringWriter = stringWriter;
     }
 
     /**
      * @return the mockResource
      */
     public Resource getMockResource1() {
         return mockResource1;
     }
 
     /**
      * @param mockResource the mockResource to set
      */
     public void setMockResource1(final Resource mockResource) {
         this.mockResource1 = mockResource;
     }
 
     /**
      * @return the deploymentMetadataTestData
      */
     private DeploymentMetadataTestData getDeploymentMetadataTestData() {
         return deploymentMetadataTestData;
     }
 
     /**
      * @return the resourcePathTestData
      */
     private ResourcePathTestData getResourcePathTestData() {
         return resourcePathTestData;
     }
 
     /**
      * @return the mockResolutionWarnLogger
      */
     public ResourceResolutionWarnLogger getMockResolutionWarnLogger() {
         return mockResolutionWarnLogger;
     }
 
     /**
      * @param mockResolutionWarnLogger
      *            the mockResolutionWarnLogger to set
      */
     public void setMockResolutionWarnLogger(
             final ResourceResolutionWarnLogger mockResolutionWarnLogger) {
         this.mockResolutionWarnLogger = mockResolutionWarnLogger;
     }
 
     /**
      * @return the sourceBundle1
      */
     private File getSourceBundle1NewFile() {
         return sourceBundle1NewFile;
     }
 
     /**
      * @param sourceBundle1NewFile the sourceBundle1 to set
      */
     private void setSourceBundle1NewFile(final File sourceBundle1NewFile) {
         this.sourceBundle1NewFile = sourceBundle1NewFile;
     }
 
     /**
      * @return the appBundlesRootDir
      */
     private File getAppBundlesRootDir() {
         return appBundlesRootDir;
     }
 
     /**
      * @param appBundlesRootDir the appBundlesRootDir to set
      */
     private void setAppBundlesRootDir(final File appBundlesRootDir) {
         this.appBundlesRootDir = appBundlesRootDir;
     }
 
     /**
      * @return the mockResource2
      */
     public Resource getMockResource2() {
         return mockResource2;
     }
 
     /**
      * @param mockResource2 the mockResource2 to set
      */
     public void setMockResource2(final Resource mockResource2) {
         this.mockResource2 = mockResource2;
     }
 
     /**
      * @return the sourceBundle2NewFile
      */
     private File getSourceBundle2NewFile() {
         return sourceBundle2NewFile;
     }
 
     /**
      * @param sourceBundle2NewFile the sourceBundle2NewFile to set
      */
     private void setSourceBundle2NewFile(final File sourceBundle2NewFile) {
         this.sourceBundle2NewFile = sourceBundle2NewFile;
     }
 
     /**
      * @return the sourceBundle2NewPath
      */
     private String getSourceBundle2NewPath() {
         return sourceBundle2NewPath;
     }
 
     /**
      * @param sourceBundle2NewPath the sourceBundle2NewPath to set
      */
     private void setSourceBundle2NewPath(final String sourceBundle2NewPath) {
         this.sourceBundle2NewPath = sourceBundle2NewPath;
     }
 
     /**
      * @return the sourceBundle1NewPath
      */
     private String getSourceBundle1NewPath() {
         return sourceBundle1NewPath;
     }
 
     /**
      * @param sourceBundle1NewPath the sourceBundle1NewPath to set
      */
     private void setSourceBundle1NewPath(final String sourceBundle1NewPath) {
         this.sourceBundle1NewPath = sourceBundle1NewPath;
     }
 
     /**
      * @return the mockPageContext
      */
     public PageContext getMockPageContext() {
         return mockPageContext;
     }
 
     /**
      * @param mockPageContext the mockPageContext to set
      */
     public void setMockPageContext(final PageContext mockPageContext) {
         this.mockPageContext = mockPageContext;
     }
 
     /**
      * @return the mockWebApplicationContext
      */
     public WebApplicationContext getMockWebApplicationContext() {
         return mockWebApplicationContext;
     }
 
     /**
      * @param mockWebApplicationContext
      *            the mockWebApplicationContext to set
      */
     public void setMockWebApplicationContext(
             final WebApplicationContext mockWebApplicationContext) {
         this.mockWebApplicationContext = mockWebApplicationContext;
     }
 
     /**
      * @return the springMockServletContext
      */
     private MockServletContext getSpringMockServletContext() {
         return springMockServletContext;
     }
 
     /**
      * @param springMockServletContext the springMockServletContext to set
      */
     private void setSpringMockServletContext(final MockServletContext springMockServletContext) {
         this.springMockServletContext = springMockServletContext;
     }
 
     /**
      * @return the bundleScriptsTagDependencies
      */
     private BundleTagDependencies getBundleLinksTagDependencies() {
         return bundleScriptsTagDependencies;
     }
 
     /**
      * @param bundleScriptsTagDependencies the bundleScriptsTagDependencies to set
      */
     private void setBundleLinksTagDependencies(
             final BundleTagDependencies bundleScriptsTagDependencies) {
         this.bundleScriptsTagDependencies = bundleScriptsTagDependencies;
     }
 
     /**
      * @return the mockBundleLinksTagCache
      */
     public BundleTagCache getMockBundleLinksTagCache() {
         return mockBundleLinksTagCache;
     }
 
     /**
      * @param mockBundleLinksTagCache the mockBundleLinksTagCache to set
      */
     public void setMockBundleLinksTagCache(final BundleTagCache mockBundleLinksTagCache) {
         this.mockBundleLinksTagCache = mockBundleLinksTagCache;
     }
 }
