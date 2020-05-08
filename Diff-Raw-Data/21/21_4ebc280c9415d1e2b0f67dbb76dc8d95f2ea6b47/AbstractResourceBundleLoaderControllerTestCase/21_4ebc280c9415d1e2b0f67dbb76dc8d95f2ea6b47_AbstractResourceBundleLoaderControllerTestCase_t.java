 package au.com.sensis.mobile.web.component.core.bundle;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import junitx.util.PrivateAccessor;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.easymock.EasyMock;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.web.servlet.ModelAndView;
 
 import au.com.sensis.wireless.test.AbstractJUnit4TestCase;
 
 /**
  * Base class for all subclasses of {@link AbstractResourceBundleLoaderController}.
  *
  * @author Adrian.Koh2@sensis.com.au
  */
 public abstract class AbstractResourceBundleLoaderControllerTestCase
     extends AbstractJUnit4TestCase {
 
     private static final String RESOURCE_NAME_REQUEST_URI_PREFIX = "/js/";
 
     private AbstractResourceBundleLoaderController objectUnderTest;
 
     private ResourceBundleLoader mockResourceBundleLoader;
     private HttpServletRequest mockHttpServletRequest;
     private HttpSession mockHttpSession;
     private HttpServletResponse mockHttpServletResponse;
     private ModelAndView mockModelAndView;
     private SimpleFeatureEnablementRegistryBean simpleFeatureEnablementRegistryBean;
 
     /**
      * Setup test data.
      *
      * @throws Exception Thrown if any error occurs.
      */
     @Before
     public final void baseSetUp() throws Exception {
         objectUnderTest = createObjectUnderTest();
         getObjectUnderTest().setResourceBundleLoader(getMockResourceBundleLoader());
         getObjectUnderTest().setResourceNameRequestUriPrefix(RESOURCE_NAME_REQUEST_URI_PREFIX);
 
         simpleFeatureEnablementRegistryBean = new SimpleFeatureEnablementRegistryBean();
         objectUnderTest.setFeatureEnablementRegistry(simpleFeatureEnablementRegistryBean);
     }
 
     protected abstract AbstractResourceBundleLoaderController createObjectUnderTest();
 
     protected AbstractResourceBundleLoaderController getObjectUnderTest() {
         return objectUnderTest;
     }
 
     @Test
     public void testIsBypassClientCacheRequested() throws Throwable {
         for (final BypassClientCacheRequestedTestData testData : BypassClientCacheRequestedTestData
                 .getIsBundleExploderRequestedTestData()) {
 
 
             getSimpleFeatureEnablementRegistryBean().setBypassClientCacheEnabled(
                     testData.isAllowBypassClientCacheRequestParam());
 
             if (testData.isAllowBypassClientCacheRequestParam()) {
                 if (testData.isBypassClientCacheRequestParamSet()) {
                     recordBypassClientCacheRequested();
                 } else {
                     recordBypassClientCacheNotRequested();
 
                 }
             }
 
             replay();
 
             final Boolean actualOutcome = (Boolean) PrivateAccessor.invoke(getObjectUnderTest(),
                     "isByassClientCacheRequested", new Class [] {HttpServletRequest.class},
                     new Object [] {getMockHttpServletRequest()});
 
             Assert.assertEquals("isByassClientCacheRequested returned wrong value for testData: "
                     + testData, testData.isExpectedOutcome(), actualOutcome);
 
             // Reset mocks prior to next iteration.
             getHelper().reset();
             setReplayed(false);
         }
     }
 
     protected final void recordBypassClientCacheNotRequested() {
         EasyMock.expect(
                 getMockHttpServletRequest().getSession()).andReturn(getMockHttpSession());
         EasyMock.expect(
                 getMockHttpSession().getAttribute(
                         AbstractResourceBundleLoaderController
                             .BYPASS_CLIENT_CACHE_SESSION_KEY))
                 .andReturn(Boolean.FALSE).atLeastOnce();
     }
 
     protected final void recordBypassClientCacheRequested() {
         EasyMock.expect(
                 getMockHttpServletRequest().getSession()).andReturn(getMockHttpSession());
         EasyMock.expect(
                 getMockHttpSession().getAttribute(
                         AbstractResourceBundleLoaderController
                             .BYPASS_CLIENT_CACHE_SESSION_KEY))
                 .andReturn(Boolean.TRUE).atLeastOnce();
     }
 
     @Test
     public void testExtractResourceNameRequestedWhenNotNull() {
         getObjectUnderTest().setResourceNameRequestUriPrefix(RESOURCE_NAME_REQUEST_URI_PREFIX);
 
         final String resourceName = recordExtractResourceNameRequested();
 
         replay();
 
         Assert.assertEquals("wrong resource name extracted", resourceName,
                 getObjectUnderTest().extractResourceNameRequested(
                         getMockHttpServletRequest()));
 
     }
 
     protected final String recordExtractResourceNameRequested() {
         EasyMock.expect(getMockHttpServletRequest().getRequestURI()).andReturn(
             "http://somewhere.com/comp/js/myresource.js").atLeastOnce();
 
         return "myresource.js";
 
     }
 
     @Test
     public void testExtractResourceNameRequestedWhenNull() {
         getObjectUnderTest().setResourceNameRequestUriPrefix(RESOURCE_NAME_REQUEST_URI_PREFIX);
 
         EasyMock.expect(getMockHttpServletRequest().getRequestURI()).andReturn(
                 "http://somewhere.com/comp/jsexploded/myresource.js");
 
         replay();
 
         Assert.assertEquals("wrong resource name extracted", StringUtils.EMPTY,
                 getObjectUnderTest().extractResourceNameRequested(
                         getMockHttpServletRequest()));
 
     }
 
     @Test
    public void testExtractResourceNameRequestedWhenContainsUrlEncodedCookie() {
         getObjectUnderTest().setResourceNameRequestUriPrefix(RESOURCE_NAME_REQUEST_URI_PREFIX);
 
        final String resourceName = recordExtractResourceNameRequestedWithUrlEncodedCookie();
 
         replay();
 
         Assert.assertEquals("wrong resource name extracted", resourceName,
                 getObjectUnderTest().extractResourceNameRequested(
                         getMockHttpServletRequest()));
 
     }
 
    protected final String recordExtractResourceNameRequestedWithUrlEncodedCookie() {
         EasyMock.expect(getMockHttpServletRequest().getRequestURI()).andReturn(
                 "http://somewhere.com/comp/js/myresource.js"
                        + ";mycookiename=mycookievalue")
                 .atLeastOnce();
 
         return "myresource.js";
 
     }
 
     @Test
     public void testExtractResourceNameRequestedWhenRequestUriIsNull() {
         getObjectUnderTest().setResourceNameRequestUriPrefix(RESOURCE_NAME_REQUEST_URI_PREFIX);
 
         EasyMock.expect(getMockHttpServletRequest().getRequestURI()).andReturn(
                 null);
 
         replay();
 
         Assert.assertEquals("wrong resource name extracted", StringUtils.EMPTY,
                 getObjectUnderTest().extractResourceNameRequested(
                         getMockHttpServletRequest()));
 
     }
 
     /**
      * @return the mockHttpServletRequest
      */
     public HttpServletRequest getMockHttpServletRequest() {
         return mockHttpServletRequest;
     }
 
     /**
      * @param mockHttpServletRequest the mockHttpServletRequest to set
      */
     public void setMockHttpServletRequest(final HttpServletRequest mockHttpServletRequest) {
         this.mockHttpServletRequest = mockHttpServletRequest;
     }
 
     /**
      * @return the mockHttpServletResponse
      */
     public HttpServletResponse getMockHttpServletResponse() {
         return mockHttpServletResponse;
     }
 
     /**
      * @param mockHttpServletResponse the mockHttpServletResponse to set
      */
     public void setMockHttpServletResponse(
             final HttpServletResponse mockHttpServletResponse) {
         this.mockHttpServletResponse = mockHttpServletResponse;
     }
 
     /**
      * @return the mockModelAndView
      */
     public ModelAndView getMockModelAndView() {
         return mockModelAndView;
     }
 
     /**
      * @param mockModelAndView the mockModelAndView to set
      */
     public void setMockModelAndView(final ModelAndView mockModelAndView) {
         this.mockModelAndView = mockModelAndView;
     }
 
     /**
      * @return the mockResourceBundleLoader
      */
     public ResourceBundleLoader getMockResourceBundleLoader() {
         return mockResourceBundleLoader;
     }
 
     /**
      * @param mockResourceBundleLoader the mockResourceBundleLoader to set
      */
     public void setMockResourceBundleLoader(
             final ResourceBundleLoader mockResourceBundleLoader) {
         this.mockResourceBundleLoader = mockResourceBundleLoader;
     }
 
     /**
      * @return the mockHttpSession
      */
     public HttpSession getMockHttpSession() {
         return mockHttpSession;
     }
 
     /**
      * @param mockHttpSession the mockHttpSession to set
      */
     public void setMockHttpSession(final HttpSession mockHttpSession) {
         this.mockHttpSession = mockHttpSession;
     }
 
     private static final class BypassClientCacheRequestedTestData {
         private final boolean allowBypassClientCacheRequestParam;
         private final boolean bypassClientCacheRequestParamSet;
         private final boolean expectedOutcome;
 
         private BypassClientCacheRequestedTestData(
                 final boolean allowBundleExploderRequestParam,
                 final boolean bundleExploderRequestParamSet,
                 final boolean expectedOutcome) {
             allowBypassClientCacheRequestParam =
                     allowBundleExploderRequestParam;
             bypassClientCacheRequestParamSet = bundleExploderRequestParamSet;
             this.expectedOutcome = expectedOutcome;
         }
 
         private static BypassClientCacheRequestedTestData
             [] getIsBundleExploderRequestedTestData() {
             return new BypassClientCacheRequestedTestData [] {
                     new BypassClientCacheRequestedTestData(true, true, true),
                     new BypassClientCacheRequestedTestData(true, false, false),
                     new BypassClientCacheRequestedTestData(false, true, false),
                     new BypassClientCacheRequestedTestData(false, false, false)
             };
         }
 
         /**
          * @return the allowBypassClientCacheRequestParam
          */
         public boolean isAllowBypassClientCacheRequestParam() {
             return allowBypassClientCacheRequestParam;
         }
 
 
         /**
          * @return the expectedOutcome
          */
         public boolean isExpectedOutcome() {
             return expectedOutcome;
         }
 
         /**
          * @return the bypassClientCacheRequestParamSet
          */
         public boolean isBypassClientCacheRequestParamSet() {
             return bypassClientCacheRequestParamSet;
         }
 
         @Override
         public String toString() {
             return new ToStringBuilder(this)
                 .append("allowBypassClientCacheRequestParam",
                         isAllowBypassClientCacheRequestParam())
                 .append("bypassClientCacheRequestParamSet", isBypassClientCacheRequestParamSet())
                 .append("expectedOutcome", isExpectedOutcome()).toString();
         }
     }
 
     /**
      * @return the simpleFeatureEnablementRegistryBean
      */
     public SimpleFeatureEnablementRegistryBean getSimpleFeatureEnablementRegistryBean() {
         return simpleFeatureEnablementRegistryBean;
     }
 }
