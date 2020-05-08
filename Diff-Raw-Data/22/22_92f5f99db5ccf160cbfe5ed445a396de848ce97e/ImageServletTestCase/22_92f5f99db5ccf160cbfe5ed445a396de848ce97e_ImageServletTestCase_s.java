 package au.com.sensis.mobile.crf.presentation;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.easymock.EasyMock;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.mock.web.MockHttpSession;
 import org.springframework.mock.web.MockServletConfig;
 import org.springframework.web.context.WebApplicationContext;
 
 import au.com.sensis.mobile.crf.config.DeploymentMetadataTestData;
 import au.com.sensis.mobile.crf.config.GroupTestData;
 import au.com.sensis.mobile.crf.presentation.ImageServlet.ImageServletDependencies;
 import au.com.sensis.mobile.crf.service.Resource;
 import au.com.sensis.mobile.crf.service.ResourceBean;
 import au.com.sensis.mobile.crf.service.ResourcePathTestData;
 import au.com.sensis.mobile.crf.service.ResourceResolverEngine;
 import au.com.sensis.mobile.crf.util.FileIoFacade;
 import au.com.sensis.mobile.crf.util.FileIoFacadeFactory;
 import au.com.sensis.wireless.common.volantis.devicerepository.api.Device;
 import au.com.sensis.wireless.test.AbstractJUnit4TestCase;
 import au.com.sensis.wireless.web.mobile.MobileContext;
 
 /**
  * Unit test {@link ImageServlet}.
  *
  * @author Adrian.Koh2@sensis.com.au
  */
 public class ImageServletTestCase extends AbstractJUnit4TestCase {
 
     private static final String IMAGES_CLIENT_PATH_PREFIX = "/uiresources/";
 
     private static final String MIME_TYPE = "image/png";
 
     private static final String BEAN_NAME = "imageServletDependencies";
 
     private ImageServlet objectUnderTest;
 
     private HttpServletRequest mockHttpServletRequest;
     private HttpServletResponse mockHttpServletResponse;
     private MockHttpSession springMockHttpSession;
     private MobileContext mockMobileContext;
     private ResourceResolverEngine mockResourceResolverEngine;
     private Device mockDevice;
     private ServletOutputStream mockServletOutputStream;
     private FileIoFacade mockFileIoFacade;
     private ServletContext mockServletContext;
     private MockServletConfig springMockServletConfig;
     private WebApplicationContext mockWebApplicationContext;
     private final ResourcePathTestData resourcePathTestData = new ResourcePathTestData();
     private File expectedMappedFile;
     private ImageServletDependencies imageServletDependencies;
     private final GroupTestData groupTestData = new GroupTestData();
     private final DeploymentMetadataTestData deploymentMetadataTestData
         = new DeploymentMetadataTestData();
 
     /**
      * Setup test data.
      *
      * @throws Exception
      *             Thrown if any error occurs.
      */
     @Before
     public void setUp() throws Exception {
         FileIoFacadeFactory
                 .changeDefaultFileIoFacadeSingleton(getMockFileIoFacade());
         setSpringMockServletConfig(new MockServletConfig(
                 getMockServletContext()));
         setSpringMockHttpSession(new MockHttpSession());
         setObjectUnderTest(new ImageServlet());
 
         getObjectUnderTest().setImageServletDependenciesBeanName(
                 BEAN_NAME);
 
         // For the purposes of this test case, the expectedMappedFile must be a
         // real file so we
         // set it to the class file of this testcase.
         final String expectedMappedFileOnClasspath =
                 "/" + getClass().getName().replaceAll("\\.", "/") + ".class";
         setExpectedMappedFile(new File(getClass().getResource(
                 expectedMappedFileOnClasspath).toURI()));
 
         setImageServletDependencies(new ImageServletDependencies(
                 getMockResourceResolverEngine(), IMAGES_CLIENT_PATH_PREFIX,
                 getDeploymentMetadataTestData().createDevDeploymentMetadata()));
     }
 
     /**
      * Tear down test data.
      *
      * @throws Exception Thrown if any error occurs.
      */
     @After
     public void tearDown() throws Exception {
         FileIoFacadeFactory.restoreDefaultFileIoFacadeSingleton();
     }
 
     @Test
     public void testDoGetWhenRequestStartsWithExpectedPrefixAndFileExists()
             throws Throwable {
 
         recordGetImageServletDependencies();
 
         recordGetDevice();
 
         recordGetRequestUri(getRequestedResourcePath());
 
         recordGetResource(
                 getResourcePathTestData().getMapComponentRequestedImageResourcePath(),
                 getResource());
 
         recordSetResponseContentType();
         recordSetResponseContentLength();
         recordSetResponseLastModified();
 
         recordetServletOutputStream();
 
         recordWriteMappedFileToOutputStream();
 
         replay();
 
         getObjectUnderTest().init(getSpringMockServletConfig());
 
         getObjectUnderTest().doGet(getMockHttpServletRequest(),
                 getMockHttpServletResponse());
 
     }
 
     @Test
     public void testDoGetWhenRequestStartsWithExpectedPrefixAndFileDoesNotExist()
             throws Throwable {
 
         recordGetImageServletDependencies();
 
         recordGetDevice();
 
         recordGetRequestUri(getRequestedResourcePath());
 
         recordGetResource(getResourcePathTestData()
                 .getMapComponentRequestedImageResourcePath(), null);
 
         recordSetHttp404ErrorResponse();
 
         replay();
 
         getObjectUnderTest().init(getSpringMockServletConfig());
 
         getObjectUnderTest().doGet(getMockHttpServletRequest(),
                 getMockHttpServletResponse());
 
     }
 
     @Test
     public void testDoGetWhenRequestDoesNotStartWithExpectedPrefix()
             throws Throwable {
 
         recordGetImageServletDependencies();
 
         recordGetRequestUri(getResourcePathTestData()
                 .getMapComponentRequestedImageResourcePath());
 
         replay();
 
         getObjectUnderTest().init(getSpringMockServletConfig());
 
         try {
             getObjectUnderTest().doGet(getMockHttpServletRequest(),
                     getMockHttpServletResponse());
 
             Assert.fail("ServletException expected");
         } catch (final ServletException e) {
 
             Assert.assertEquals("ServletException has wrong message",
                 "Requests for abstract images should have a requestUri starting with '"
                        + IMAGES_CLIENT_PATH_PREFIX
                         + "'. However, requestUri is '"
                         + getResourcePathTestData()
                                 .getMapComponentRequestedImageResourcePath()
                         + "'", e.getMessage());
         }
 
     }
 
     private void recordSetHttp404ErrorResponse() {
         getMockHttpServletResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
     }
 
     private String getRequestedResourcePath() {
        return IMAGES_CLIENT_PATH_PREFIX
                + getDeploymentMetadataTestData().createDevDeploymentMetadata().getVersion()
                + "/images/"
                 + getResourcePathTestData().getMapComponentRequestedImageResourcePath();
     }
 
     private Resource getResource() {
         return new ResourceBean(getRequestedResourcePath(),
                 getExpectedMappedFile().getName(), getExpectedMappedFile()
                         .getParentFile(), getGroupTestData().createDefaultGroup());
     }
 
     private void recordGetImageServletDependencies() {
         EasyMock.expect(
                 getMockServletContext().getAttribute(
                         WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE))
                 .andReturn(getMockWebApplicationContext());
 
         EasyMock.expect(getMockWebApplicationContext().getBean(BEAN_NAME))
                 .andReturn(getImageServletDependencies()).atLeastOnce();
     }
 
     private void recordGetDevice() {
 
         EasyMock.expect(getMockHttpServletRequest().getSession()).andReturn(
                 getSpringMockHttpSession()).atLeastOnce();
         getSpringMockHttpSession().setAttribute(
                 MobileContext.MOBILE_CONTEXT_KEY, getMockMobileContext());
 
         EasyMock.expect(getMockMobileContext().getDevice()).andReturn(
                 getMockDevice()).atLeastOnce();
     }
 
     private void recordWriteMappedFileToOutputStream() throws IOException {
         getMockFileIoFacade().writeFileAndCloseStream(getExpectedMappedFile(),
                 getMockServletOutputStream());
     }
 
     private void recordetServletOutputStream() throws IOException {
         EasyMock.expect(getMockHttpServletResponse().getOutputStream())
                 .andReturn(getMockServletOutputStream());
     }
 
     private void recordSetResponseLastModified() {
         getMockHttpServletResponse().setDateHeader("Last-Modified",
                 getExpectedMappedFile().lastModified());
     }
     private void recordSetResponseContentLength() {
         getMockHttpServletResponse().setContentLength(
                 (int) getExpectedMappedFile().length());
     }
 
     private void recordSetResponseContentType() {
         EasyMock.expect(
                 getMockServletContext().getMimeType(
                         getExpectedMappedFile().getPath()))
                 .andReturn(MIME_TYPE);
         getMockHttpServletResponse().setContentType(MIME_TYPE);
     }
 
     private void recordGetResource(
             final String requestedResourcePath,
             final Resource resource) throws IOException {
         EasyMock.expect(
                 getMockResourceResolverEngine().getResource(getMockDevice(),
                         requestedResourcePath)).andReturn(resource);
 
     }
 
     private void recordGetRequestUri(final String requestUri) {
         EasyMock.expect(getMockHttpServletRequest().getRequestURI())
                 .andReturn(requestUri).atLeastOnce();
     }
 
     private ImageServlet getObjectUnderTest() {
         return objectUnderTest;
     }
 
     private void setObjectUnderTest(final ImageServlet objectUnderTest) {
         this.objectUnderTest = objectUnderTest;
     }
 
     public HttpServletRequest getMockHttpServletRequest() {
         return mockHttpServletRequest;
     }
 
     public void setMockHttpServletRequest(final HttpServletRequest mockHttpServletRequest) {
         this.mockHttpServletRequest = mockHttpServletRequest;
     }
 
     public HttpServletResponse getMockHttpServletResponse() {
         return mockHttpServletResponse;
     }
 
     public void setMockHttpServletResponse(
             final HttpServletResponse mockHttpServletResponse) {
         this.mockHttpServletResponse = mockHttpServletResponse;
     }
 
     public ResourceResolverEngine getMockResourceResolverEngine() {
         return mockResourceResolverEngine;
     }
 
     public void setMockResourceResolverEngine(
             final ResourceResolverEngine mockResourceResolverEngine) {
         this.mockResourceResolverEngine = mockResourceResolverEngine;
     }
 
     public Device getMockDevice() {
         return mockDevice;
     }
 
     public void setMockDevice(final Device mockDevice) {
         this.mockDevice = mockDevice;
     }
 
     private ResourcePathTestData getResourcePathTestData() {
         return resourcePathTestData;
     }
 
     public FileIoFacade getMockFileIoFacade() {
         return mockFileIoFacade;
     }
 
     public void setMockFileIoFacade(final FileIoFacade mockFileIoFacade) {
         this.mockFileIoFacade = mockFileIoFacade;
     }
 
     public ServletOutputStream getMockServletOutputStream() {
         return mockServletOutputStream;
     }
 
     public void setMockServletOutputStream(
             final ServletOutputStream mockServletOutputStream) {
         this.mockServletOutputStream = mockServletOutputStream;
     }
 
     public ServletContext getMockServletContext() {
         return mockServletContext;
     }
 
     public void setMockServletContext(final ServletContext mockServletContext) {
         this.mockServletContext = mockServletContext;
     }
 
     private MockServletConfig getSpringMockServletConfig() {
         return springMockServletConfig;
     }
 
     private void setSpringMockServletConfig(
             final MockServletConfig springMockServletConfig) {
         this.springMockServletConfig = springMockServletConfig;
     }
 
     private File getExpectedMappedFile() {
         return expectedMappedFile;
     }
 
     private void setExpectedMappedFile(final File expectedMappedFile) {
         this.expectedMappedFile = expectedMappedFile;
     }
 
     public WebApplicationContext getMockWebApplicationContext() {
         return mockWebApplicationContext;
     }
 
     public void setMockWebApplicationContext(
             final WebApplicationContext mockWebApplicationContext) {
         this.mockWebApplicationContext = mockWebApplicationContext;
     }
 
     public MobileContext getMockMobileContext() {
         return mockMobileContext;
     }
 
     public void setMockMobileContext(final MobileContext mockMobileContext) {
         this.mockMobileContext = mockMobileContext;
     }
 
     private MockHttpSession getSpringMockHttpSession() {
         return springMockHttpSession;
     }
 
     private void setSpringMockHttpSession(final MockHttpSession springMockHttpSession) {
         this.springMockHttpSession = springMockHttpSession;
     }
 
     private ImageServletDependencies getImageServletDependencies() {
         return imageServletDependencies;
     }
 
     private void setImageServletDependencies(
             final ImageServletDependencies imageServletDependencies) {
         this.imageServletDependencies = imageServletDependencies;
     }
 
     /**
      * @return the groupTestData
      */
     private GroupTestData getGroupTestData() {
         return groupTestData;
     }
 
     /**
      * @return the deploymentMetadataTestData
      */
     public DeploymentMetadataTestData getDeploymentMetadataTestData() {
         return deploymentMetadataTestData;
     }
 }
