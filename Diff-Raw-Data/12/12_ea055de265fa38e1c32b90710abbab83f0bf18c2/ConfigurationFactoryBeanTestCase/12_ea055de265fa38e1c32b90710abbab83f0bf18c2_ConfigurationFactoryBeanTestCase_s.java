 package au.com.sensis.mobile.crf.config;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.easymock.EasyMock;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
 import org.springframework.core.io.support.ResourcePatternResolver;
 
 import au.com.sensis.mobile.crf.config.jaxb.generated.ObjectFactory;
 import au.com.sensis.mobile.crf.exception.ConfigurationRuntimeException;
 import au.com.sensis.mobile.crf.exception.XmlValidationRuntimeException;
 import au.com.sensis.mobile.crf.service.ResourceResolutionWarnLogger;
 import au.com.sensis.mobile.crf.util.XmlBinder;
 import au.com.sensis.mobile.crf.util.XmlValidator;
 import au.com.sensis.mobile.crf.util.XsdXmlValidatorBean;
 import au.com.sensis.wireless.common.utils.jaxb.JaxbXMLBinderImpl;
 import au.com.sensis.wireless.test.AbstractJUnit4TestCase;
 
 /**
  * Unit test {@link ConfigurationFactoryBean}.
  * <p>
  * NOTE: this is arguably more of an integration test than a unit test since it
  * uses real {@link XmlBinder} and {@link XmlValidator} instances.
  * Haven't used the "*IntegrationTestCase" naming convention though, since they are ignored
  * by the build scripts.
  * </p>
  *
  * @author Adrian.Koh2@sensis.com.au
  */
 public class ConfigurationFactoryBeanTestCase extends
         AbstractJUnit4TestCase {
 
     private static final String CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN
         = "classpath*:/au/com/sensis/mobile/crf/*/crf-config-pattern-match*.xml";
     private static final String CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN_MATCH1
         = "/au/com/sensis/mobile/crf/config/crf-config-pattern-match1.xml";
     private static final String CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN_MATCH2
         = "/au/com/sensis/mobile/crf/config/crf-config-pattern-match2.xml";
     private static final String CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN_MATCH3
         = "/au/com/sensis/mobile/crf/config/crf-config-pattern-match3.xml";
 
     private static final String CRF_CONFIG_EMPTY_PATH_CLASSPATH_PATTERN
         = "classpath*:/au/com/sensis/mobile/crf/*/crf-config-empty-path*.xml";
     private static final String CRF_CONFIG_EMPTY_PATH_CLASSPATH_PATTERN_MATCH1
         = "/au/com/sensis/mobile/crf/config/crf-config-empty-path1.xml";
     private static final String CRF_CONFIG_EMPTY_PATH_CLASSPATH_PATTERN_MATCH2
         = "/au/com/sensis/mobile/crf/config/crf-config-empty-path2.xml";
 
     private static final String CRF_CONFIG_MULTIPLE_VALID_GROUPS
         = "/au/com/sensis/mobile/crf/config/crf-config-multiple-valid-groups.xml";
 
     private static final String CRF_CONFIG_ONE_INVALID_EXPR
         = "/au/com/sensis/mobile/crf/config/crf-config-one-invalid-expr.xml";
     private static final String CRF_CONFIG_MULTIPLE_INVALID_EXPR
         = "/au/com/sensis/mobile/crf/config/crf-config-multiple-invalid-expr.xml";
 
     private static final String CRF_CONFIG_DUPLICATE_GROUP_NAMES
         = "/au/com/sensis/mobile/crf/config/crf-config-duplicate-group-names.xml";
 
     private static final String CRF_CONFIG_DUPLICATE_CONFIG_PATHS
         = "classpath*:/au/com/sensis/mobile/crf/config/crf-config-duplicate-config-path*.xml,"
             + "/au/com/sensis/mobile/crf/config/crf-config-multiple-valid-groups.xml";
     private static final String CRF_CONFIG_DUPLICATE_CONFIG_PATH1
         = "/au/com/sensis/mobile/crf/config/crf-config-duplicate-config-path1.xml";
     private static final String CRF_CONFIG_DUPLICATE_CONFIG_PATH2
         = "/au/com/sensis/mobile/crf/config/crf-config-duplicate-config-path2.xml";
 
     private static final String CRF_CONFIG_IMPORT_GLOBAL_GROUPS_CLASSPATH_PATTERN
         = "au/com/sensis/mobile/crf/config/crf-config-import-global-groups.xml"
           + ",au/com/sensis/mobile/crf/config/crf-config-global-devices.xml"
           + ",au/com/sensis/mobile/crf/config/crf-config-global-extra-devices.xml"
           + ",au/com/sensis/mobile/crf/config/crf-config-global-image-categories.xml";
     private static final String CRF_CONFIG_IMPORT_GLOBAL_GROUPS_CLASSPATH
         = "/au/com/sensis/mobile/crf/config/crf-config-import-global-groups.xml";
 
     private static final String VALID_CSS_ROOT_DIR_CLASSPATH =
         "/au/com/sensis/mobile/crf/config/validUiResources/css";
     private static final String VALID_IMAGES_ROOT_DIR_CLASSPATH =
         "/au/com/sensis/mobile/crf/config/validUiResources/images";
     private static final String MISSING_GROUPS_CSS_ROOT_DIR_CLASSPATH =
         "/au/com/sensis/mobile/crf/config/uiResourcesMissingGroupDirs/css";
     private static final String MISSING_GROUPS_IMAGES_ROOT_DIR_CLASSPATH =
         "/au/com/sensis/mobile/crf/config/uiResourcesMissingGroupDirs/images";
     private static final String EXTRA_GROUPS_CSS_ROOT_DIR_CLASSPATH =
         "/au/com/sensis/mobile/crf/config/uiResourcesExtraGroupDirs/css";
     private static final String EXTRA_GROUPS_MAGES_ROOT_DIR_CLASSPATH =
         "/au/com/sensis/mobile/crf/config/uiResourcesExtraGroupDirs/images";
 
     private final DeploymentMetadataTestData deploymentMetadataTestData
         = new DeploymentMetadataTestData();
     private XmlBinder xmlBinder;
     private final XmlValidator xmlValidator = new XsdXmlValidatorBean();
     private final ResourcePatternResolver resourcePatternResolver
         = new PathMatchingResourcePatternResolver();
 
     private ResourcePatternResolver mockResourcePatternResolver;
     private Resource mockResource1;
     private Resource mockResource2;
     private ResourceResolutionWarnLogger mockResourceResolutionWarnLogger;
     private List<File> uiResourceRootDirectories;
     private GroupsCacheFactory mockGroupsCacheFactory;
     private GroupsCache mockGroupsCache;
     private GroupTestData groupTestData;
     private GroupImportTestData groupImportTestData;
 
     /**
      * Setup test data.
      *
      * @throws Exception
      *             Thrown if any error occurs.
      */
     @Before
     public void setUp() throws Exception {
         swapOutRealLoggerForMock(ConfigurationFactoryBean.class);
 
         setUiResourceRootDirectories(Arrays.asList(getValidUiResourcesCssRootDir(),
                 getValidUiResourcesImagesRootDir()));
         setGroupTestData(new GroupTestData());
         setGroupImportTestData(new GroupImportTestData());
         setXmlBinder(new UiConfigurationJaxbXmlBinder(
                 new JaxbXMLBinderImpl(ObjectFactory.class.getPackage().getName())));
     }
 
     private ConfigurationPaths createConfigurationPaths(
             final String mappingConfigurationClasspathPattern,
             final List<File> uiResourceRootDirectories) {
         return new ConfigurationPaths(mappingConfigurationClasspathPattern,
                 uiResourceRootDirectories);
     }
 
     @Test
     public void testConstructorWhenFileNotFound() throws Throwable {
         final String mappingConfigurationClasspath =
                 "/file does not exist on classpath";
         try {
             new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                     getResourcePatternResolver(), getXmlBinder(),
                     getXmlValidator(), getMockResourceResolutionWarnLogger(),
                     createConfigurationPaths(mappingConfigurationClasspath,
                             getUiResourceRootDirectories()), getMockGroupsCacheFactory());
             Assert.fail("ConfigurationRuntimeException expected");
         } catch (final ConfigurationRuntimeException e) {
 
             Assert
                     .assertEquals(
                             "ConfigurationRuntimeException has wrong message",
                             "Error loading config from classpath: '"
                                     + mappingConfigurationClasspath + "'", e
                                     .getMessage());
         }
     }
 
     @Test
     public void testConstructorWhenSchemaValidationFails() throws Throwable {
         for (final TestData testData : TestData.createTestDataForSchemaValidationFailure()) {
             doTestConstructorWhenSchemaValidationFails(testData);
         }
     }
 
     private void doTestConstructorWhenSchemaValidationFails(final TestData testData)
         throws IOException {
         try {
             new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                     getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                     getMockResourceResolutionWarnLogger(),
                     createConfigurationPaths(testData.getConfigFileClasspathLocation(),
                             getUiResourceRootDirectories()), getMockGroupsCacheFactory());
             Assert.fail("XmlValidationRuntimeException expected for testData: "
                     + testData);
         } catch (final XmlValidationRuntimeException e) {
 
             Assert.assertEquals("XmlValidationRuntimeException has wrong "
                     + "message for testData: " + testData, "XML at "
                     + new ClassPathResource(testData.getConfigFileClasspathLocation()).getURL()
                     + " is invalid.", e.getMessage());
 
             Assert.assertNotNull("XmlValidationRuntimeException should have a "
                     + "cause for testData: " + testData, e.getCause());
 
             Assert.assertNotNull("Cause should have a non-null message for testData: " + testData,
                     e.getCause().getMessage());
             Assert.assertTrue("Cause does not contain message matching expected regex: ["
                     + testData.getExpectedExceptionCauseMessage() + "]. Message was: ["
                     + e.getCause().getMessage() + "]",
                     e.getCause().getMessage().matches(
                             testData.getExpectedExceptionCauseMessage()));
         }
     }
 
     @Test
     public void testConstructorWhenOneGroupExpressionInvalid() throws Throwable {
 
         EasyMock.expect(getMockResourceResolutionWarnLogger().isWarnEnabled()).andReturn(
                 Boolean.TRUE).atLeastOnce();
 
         final URL configUrl = new ClassPathResource(CRF_CONFIG_ONE_INVALID_EXPR).getURL();
 
         // Don't assert the contents of the exception passed to warn, as it is
         // thrown by the
         // Group class (which we can't mock). The GroupTestCase should cover its
         // operation.
         getMockResourceResolutionWarnLogger().warn(
                 EasyMock.eq("Config at '" + configUrl + "' is possibly invalid."),
                 (Throwable) EasyMock.notNull());
 
         recordCreateGroupsCache();
 
         EasyMock.expect(getMockLogger(ConfigurationFactoryBean.class).isInfoEnabled())
             .andReturn(Boolean.FALSE).anyTimes();
 
         replay();
 
         new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                 getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                 getMockResourceResolutionWarnLogger(), createConfigurationPaths(
                         CRF_CONFIG_ONE_INVALID_EXPR, getUiResourceRootDirectories()),
                 getMockGroupsCacheFactory());
 
     }
 
     @Test
     public void testConstructorWhenMultipleGroupExpressionsInvalid() throws Throwable {
         EasyMock.expect(getMockResourceResolutionWarnLogger().isWarnEnabled()).andReturn(
                 Boolean.TRUE).atLeastOnce();
 
         final URL configUrl = new ClassPathResource(CRF_CONFIG_MULTIPLE_INVALID_EXPR).getURL();
 
         // Don't assert the contents of the exception passed to warn, as it is
         // thrown by the
         // Group class (which we can't mock). The GroupTestCase should cover its
         // operation.
         getMockResourceResolutionWarnLogger().warn(
                 EasyMock.eq("Config at '" + configUrl + "' is possibly invalid."),
                 (Throwable) EasyMock.notNull());
         getMockResourceResolutionWarnLogger().warn(
                 EasyMock.eq("Config at '" + configUrl + "' is possibly invalid."),
                 (Throwable) EasyMock.notNull());
 
         recordCreateGroupsCache();
 
         EasyMock.expect(getMockLogger(ConfigurationFactoryBean.class).isInfoEnabled())
             .andReturn(Boolean.FALSE).anyTimes();
 
         replay();
 
         new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                 getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                 getMockResourceResolutionWarnLogger(), createConfigurationPaths(
                         CRF_CONFIG_MULTIPLE_INVALID_EXPR, getUiResourceRootDirectories()),
                 getMockGroupsCacheFactory());
 
     }
 
     @Test
     public void testConstructorWhenDuplicateGroupNamesInSameConfigFile() throws Throwable {
         try {
             recordCreateGroupsCache();
 
             replay();
 
             new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                     getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                     getMockResourceResolutionWarnLogger(), createConfigurationPaths(
                             CRF_CONFIG_DUPLICATE_GROUP_NAMES, getUiResourceRootDirectories()),
                     getMockGroupsCacheFactory());
 
             Assert.fail("ConfigurationRuntimeException expected");
         } catch (final ConfigurationRuntimeException e) {
             final URL configUrl = new ClassPathResource(CRF_CONFIG_DUPLICATE_GROUP_NAMES).getURL();
             final Set<String> duplicateGroupNames = new HashSet<String>();
             duplicateGroupNames.add("iphone");
             duplicateGroupNames.add("applewebkit");
             Assert.assertEquals("ConfigurationRuntimeException has wrong message", "Config at '"
                     + configUrl + "' has duplicate group names: " + duplicateGroupNames + ". "
                     + "Some may have been via imports.", e.getMessage());
         }
     }
 
     @Test
     public void testConstructorWhenDuplicateConfigPathsInMultipleConfigFiles() throws Throwable {
         try {
             recordCreateGroupsCache();
 
             replay();
 
             new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                     getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                     getMockResourceResolutionWarnLogger(), createConfigurationPaths(
                             CRF_CONFIG_DUPLICATE_CONFIG_PATHS, getUiResourceRootDirectories()),
                     getMockGroupsCacheFactory());
 
             Assert.fail("ConfigurationRuntimeException expected");
         } catch (final ConfigurationRuntimeException e) {
             final URL configUrl1 =
                     new ClassPathResource(CRF_CONFIG_DUPLICATE_CONFIG_PATH1).getURL();
             final URL configUrl2 =
                     new ClassPathResource(CRF_CONFIG_DUPLICATE_CONFIG_PATH2).getURL();
 
             final Set<URL> duplicateConfigPathUrls = new HashSet<URL>();
             duplicateConfigPathUrls.add(configUrl1);
             duplicateConfigPathUrls.add(configUrl2);
 
             Assert.assertEquals("ConfigurationRuntimeException has wrong message",
                     "Duplicate config path of 'duplicate/config/path' found in: "
                             + duplicateConfigPathUrls, e.getMessage());
         }
     }
 
     @Test
     public void testConstructorWhenSchemaValidationSucceeds() throws Throwable {
 
 
         for (final TestData testData : TestData.createTestDataForSchemaValidationSuccess()) {
             EasyMock.expect(getMockLogger(ConfigurationFactoryBean.class).isInfoEnabled())
                 .andReturn(Boolean.FALSE).anyTimes();
 
             EasyMock.expect(getMockResourceResolutionWarnLogger().isWarnEnabled()).andReturn(
                     Boolean.FALSE).anyTimes();
 
             recordCreateGroupsCache();
 
             replay();
 
             new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                     getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                     getMockResourceResolutionWarnLogger(),
                     createConfigurationPaths(testData.getConfigFileClasspathLocation(),
                             getUiResourceRootDirectories()), getMockGroupsCacheFactory());
 
             // Explicit verify and reset since we are in a loop.
             verify();
             reset();
         }
     }
 
     @Test
     public void testConfigurationWithMultipleValidGroupsLoaded() throws Throwable {
 
         recordLoggerIsInfoEnabled(Boolean.TRUE);
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationWithMultipleValidGroups(),
                 1);
 
         recordCreateGroupsCache();
 
         replay();
         final ConfigurationFactory configurationFactory =
                 new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                         getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                         getMockResourceResolutionWarnLogger(), createConfigurationPaths(
                                 CRF_CONFIG_MULTIPLE_VALID_GROUPS, getUiResourceRootDirectories()),
                         getMockGroupsCacheFactory());
 
         final UiConfiguration actualUiConfiguration =
                 configurationFactory.getUiConfiguration("common/main.css");
         assertComplexObjectsEqual("uiConfiguration is wrong",
                 createUiConfigurationWithMultipleValidGroups(), actualUiConfiguration);
 
         Assert.assertEquals("actualUiConfiguration has wrong matchingGroupsCache",
                 getMockGroupsCache(), actualUiConfiguration.getMatchingGroupsCache());
     }
 
     private void recordCreateGroupsCache() {
         EasyMock.expect(getMockGroupsCacheFactory().createGroupsCache()).andReturn(
                 getMockGroupsCache()).atLeastOnce();
     }
 
     @Test
     public void testMultipleConfigurationsLoadedViaSinglePattern() throws Throwable {
         recordLoggerIsInfoEnabled(Boolean.TRUE);
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationForPatternMatch1(), 1);
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationForPatternMatch2(), 1);
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationForPatternMatch3(), 1);
 
         recordCreateGroupsCache();
 
         replay();
 
         final ConfigurationFactory configurationFactory =
                 new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                         getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                         getMockResourceResolutionWarnLogger(),
                         createConfigurationPaths(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN,
                                 getUiResourceRootDirectories()), getMockGroupsCacheFactory());
 
         assertComplexObjectsEqual("uiConfiguration for pattern match 1 is wrong",
                 createUiConfigurationForPatternMatch1(), configurationFactory
                         .getUiConfiguration("component/component1/main.js"));
 
         assertComplexObjectsEqual("uiConfiguration for pattern match 2 is wrong",
                 createUiConfigurationForPatternMatch2(), configurationFactory
                         .getUiConfiguration("component/component2/main.js"));
 
         assertComplexObjectsEqual("uiConfiguration for pattern match 3 is wrong",
                 createUiConfigurationForPatternMatch3(), configurationFactory
                         .getUiConfiguration("common/main.css"));
     }
 
     @Test
     public void testGetUiConfigurationWhenCacheDisabledButNoSourceChangeDetected()
         throws Throwable {
 
         recordLoggerIsInfoEnabled(Boolean.TRUE);
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationForPatternMatch1(), 1);
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationForPatternMatch2(), 1);
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationForPatternMatch3(), 1);
 
         recordCreateGroupsCache();
 
         replay();
 
         final ConfigurationFactory configurationFactory =
                 new ConfigurationFactoryBean(getCacheDisabledDeploymentMetadata(),
                         getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                         getMockResourceResolutionWarnLogger(),
                         createConfigurationPaths(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN,
                                 getUiResourceRootDirectories()), getMockGroupsCacheFactory());
 
         assertComplexObjectsEqual("uiConfiguration for pattern match 1 is wrong",
                 createUiConfigurationForPatternMatch1(), configurationFactory
                         .getUiConfiguration("component/component1/main.js"));
 
         assertComplexObjectsEqual("uiConfiguration for pattern match 2 is wrong",
                 createUiConfigurationForPatternMatch2(), configurationFactory
                         .getUiConfiguration("component/component2/main.js"));
 
         assertComplexObjectsEqual("uiConfiguration for pattern match 3 is wrong",
                 createUiConfigurationForPatternMatch3(), configurationFactory
                         .getUiConfiguration("common/main.css"));
 
     }
 
     @Test
     public void testGetUiConfigurationWhenCacheDisabledAndNewSourcesDetected() throws Throwable {
 
         final Resource foundResource1 =
                 new ClassPathResource(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN_MATCH1);
         final Resource foundResource2 =
                 new ClassPathResource(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN_MATCH2);
         final Resource foundResource3 =
                 new ClassPathResource(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN_MATCH3);
 
         recordGetResources(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN, new Resource[] {
                 foundResource1, foundResource3 }, 1);
 
         // Record get resources again corresponding to getUiConfiguration
         // detecting new sources.
         recordGetResources(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN, new Resource[] {
                 foundResource1, foundResource2, foundResource3 }, 2);
 
         recordLoggerIsInfoEnabled(Boolean.TRUE);
         recordLoggerInfo("Updated configuration detected and UiConfiguration caching disabled. "
                         + "Will reload configuration.", 1);
 
         // Note that we rely on the presence of the info logging to detect the
         // configuration being loaded twice. If you refactor to get rid of the
         // info logging, beware of this.
         final int expectedNumTimesConfigLoaded = 2;
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationForPatternMatch1(),
                 expectedNumTimesConfigLoaded);
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationForPatternMatch3(),
                 expectedNumTimesConfigLoaded);
 
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationForPatternMatch2(),
                 1);
 
         recordCreateGroupsCache();
 
         replay();
 
         final ConfigurationFactory configurationFactory =
                 new ConfigurationFactoryBean(getCacheDisabledDeploymentMetadata(),
                         getMockResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                         getMockResourceResolutionWarnLogger(),
                         createConfigurationPaths(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN,
                                 getUiResourceRootDirectories()), getMockGroupsCacheFactory());
 
         // This call should trigger the config being loaded again.
         configurationFactory.getUiConfiguration("component/component1/main.js");
     }
 
     @Test
     public void testGetUiConfigurationWhenCacheDisabledAndUpdatedSourceDetected() throws Throwable {
 
         final Resource foundResource1 =
             new ClassPathResource(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN_MATCH1);
         final Resource foundResource3 =
                 new ClassPathResource(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN_MATCH3);
 
         recordGetResources(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN,
                 new Resource[] { foundResource1, foundResource3 }, 1);
 
         // Record get resources again corresponding to getUiConfiguration
         // detecting an updated source file. This time, return a mock so we can
         // fiddle the lastModified timestamp.
         recordGetResources(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN,
                 new Resource[] { getMockResource1(), foundResource3 }, 2);
 
         EasyMock.expect(getMockResource1().getURL()).andReturn(foundResource1.getURL())
                 .atLeastOnce();
         final Long newTimestamp = new Long(5);
         EasyMock.expect(getMockResource1().lastModified()).andReturn(newTimestamp).atLeastOnce();
 
         recordLoggerIsInfoEnabled(Boolean.TRUE);
         recordLoggerInfo("Updated configuration detected and UiConfiguration caching disabled. "
                 + "Will reload configuration.", 1);
 
         // Note that we rely on the presence of the info logging to detect the
         // configuration being loaded twice. If you refactor to get rid of the
         // info logging, beware of this.
         final UiConfiguration expectedFoundResource1UiConfiguration
             = createUiConfigurationForPatternMatch1();
         recordLoggerInfo("Loaded configuration: " + expectedFoundResource1UiConfiguration, 1);
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationForPatternMatch3(), 2);
 
         // The second time the config is loaded, the foundResoure1 timestamp should be
         // different.
         expectedFoundResource1UiConfiguration.setSourceTimestamp(newTimestamp);
         recordLoggerInfo("Loaded configuration: " + expectedFoundResource1UiConfiguration, 1);
 
         recordCreateGroupsCache();
 
         replay();
 
         final ConfigurationFactory configurationFactory =
                 new ConfigurationFactoryBean(getCacheDisabledDeploymentMetadata(),
                         getMockResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                         getMockResourceResolutionWarnLogger(),
                         createConfigurationPaths(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN,
                                 getUiResourceRootDirectories()), getMockGroupsCacheFactory());
 
         // This call should trigger the config being loaded again.
         configurationFactory.getUiConfiguration("component/component1/main.js");
     }
 
     private void recordLoggerInfo(final String message, final int times) {
         getMockLogger(ConfigurationFactoryBean.class).info(message);
         EasyMock.expectLastCall().times(times);
     }
 
     private void recordLoggerIsInfoEnabled(final Boolean enabled) {
         EasyMock.expect(getMockLogger(ConfigurationFactoryBean.class).isInfoEnabled()).andReturn(
                 enabled).atLeastOnce();
     }
 
     private void recordGetResources(final String resourcesPattern, final Resource[] foundResources,
             final int times) throws IOException {
         EasyMock.expect(getMockResourcePatternResolver().getResources(resourcesPattern)).andReturn(
                 foundResources).times(times);
 
     }
 
     @Test
     public void testMultipleConfigurationsLoadedViaCommaSeparatedPatterns() throws Throwable {
         recordLoggerIsInfoEnabled(Boolean.TRUE);
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationForPatternMatch1(), 1);
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationForPatternMatch2(), 1);
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationForPatternMatch3(), 1);
 
         recordCreateGroupsCache();
 
         replay();
 
         final ConfigurationFactory configurationFactory =
                 new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                         getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                         getMockResourceResolutionWarnLogger(),
                         createConfigurationPaths(
                             CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN_MATCH1 + ", "
                                     + CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN_MATCH2 + ", "
                                     + CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN_MATCH3,
                                     getUiResourceRootDirectories()), getMockGroupsCacheFactory());
 
         assertComplexObjectsEqual("uiConfiguration for pattern match 1 is wrong",
                 createUiConfigurationForPatternMatch1(), configurationFactory
                         .getUiConfiguration("component/component1/main.js"));
 
         assertComplexObjectsEqual("uiConfiguration for pattern match 2 is wrong",
                 createUiConfigurationForPatternMatch2(), configurationFactory
                         .getUiConfiguration("component/component2/main.js"));
 
         assertComplexObjectsEqual("uiConfiguration for pattern match 3 is wrong",
                 createUiConfigurationForPatternMatch3(), configurationFactory
                         .getUiConfiguration("common/main.css"));
     }
 
     @Test
     public void testMultipleConfigurationsLoadedWhenTwoConfigPathsEmpty() throws Throwable {
         try {
             EasyMock.expect(getMockLogger(ConfigurationFactoryBean.class).isInfoEnabled())
                     .andReturn(Boolean.FALSE).anyTimes();
 
             recordCreateGroupsCache();
 
             replay();
 
             new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                     getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                     getMockResourceResolutionWarnLogger(),
                     createConfigurationPaths(CRF_CONFIG_EMPTY_PATH_CLASSPATH_PATTERN,
                             getUiResourceRootDirectories()), getMockGroupsCacheFactory());
 
             Assert.fail("ConfigurationRuntimeException expected");
         } catch (final ConfigurationRuntimeException e) {
             final URL config1Url =
                     new ClassPathResource(CRF_CONFIG_EMPTY_PATH_CLASSPATH_PATTERN_MATCH1).getURL();
             final URL config2Url =
                     new ClassPathResource(CRF_CONFIG_EMPTY_PATH_CLASSPATH_PATTERN_MATCH2).getURL();
             Assert.assertTrue("ConfigurationRuntimeException has wrong message prefix",
            		e.getMessage().startsWith("Multiple configurations with a default (empty) "
                             + "config path were found. Only one is allowed: "));
            
			Assert.assertTrue("e.getMessage() does not contain config1Url", 
					e.getMessage().contains(config1Url.toString()));
			Assert.assertTrue("e.getMessage() does not contain config2Url", 
					e.getMessage().contains(config2Url.toString()));
         }
 
     }
 
     @Test
     public void testNoConfigurationWithDefaultPath() throws Throwable {
         try {
             new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                     getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                     getMockResourceResolutionWarnLogger(),
                     createConfigurationPaths(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN_MATCH1,
                             getUiResourceRootDirectories()), getMockGroupsCacheFactory());
 
             Assert.fail("ConfigurationRuntimeException expected");
         } catch (final ConfigurationRuntimeException e) {
 
             Assert.assertEquals("ConfigurationRuntimeException has wrong message",
                     "No configuration file with a default (empty) config path was found.", e
                             .getMessage());
         }
     }
 
     @Test
     public void testConfigurationWhenGroupsNotFoundAsDirs() throws Throwable {
 
         setUiResourceRootDirectories(Arrays.asList(getMissingGroupsUiResourcesCssRootDir(),
                 getMissingGroupsUiResourcesImagesRootDir()));
 
         EasyMock.expect(getMockResourceResolutionWarnLogger().isWarnEnabled()).andReturn(
                 Boolean.TRUE);
         getMockResourceResolutionWarnLogger().warn(
                 "No group directories found for groups: [iphone, android-os, applewebkit, L, M] "
                         + "for UiConfiguration loaded from: "
                         + (new ClassPathResource(CRF_CONFIG_MULTIPLE_VALID_GROUPS)).getURL()
                         + ". Searched directories: " + getUiResourceRootDirectories());
 
         recordLoggerIsInfoEnabled(Boolean.TRUE);
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationWithMultipleValidGroups(),
                 1);
 
         recordCreateGroupsCache();
 
         replay();
         final ConfigurationFactory configurationFactory =
                 new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                         getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                         getMockResourceResolutionWarnLogger(),
                         createConfigurationPaths(CRF_CONFIG_MULTIPLE_VALID_GROUPS,
                                 getUiResourceRootDirectories()), getMockGroupsCacheFactory());
 
         assertComplexObjectsEqual("uiConfiguration is wrong",
                 createUiConfigurationWithMultipleValidGroups(), configurationFactory
                         .getUiConfiguration("common/main.css"));
     }
 
     @Test
     public void testConfigurationWhenDirsNotFoundAsGroups() throws Throwable {
 
         setUiResourceRootDirectories(Arrays.asList(getExtraGroupsUiResourcesCssRootDir(),
                 getExtraGroupsUiResourcesImagesRootDir()));
 
         EasyMock.expect(getMockResourceResolutionWarnLogger().isWarnEnabled()).andReturn(
                 Boolean.TRUE);
         getMockResourceResolutionWarnLogger().warn(
                 "Found group directories that are not configured in any config file: "
                 + Arrays.asList(getExtraCssGroupDir(), getExtraImagesGroupDir()));
 
         recordLoggerIsInfoEnabled(Boolean.TRUE);
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationWithMultipleValidGroups(),
                 1);
 
         recordCreateGroupsCache();
 
         replay();
         final ConfigurationFactory configurationFactory =
             new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                     getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                     getMockResourceResolutionWarnLogger(),
                     createConfigurationPaths(CRF_CONFIG_MULTIPLE_VALID_GROUPS,
                             getUiResourceRootDirectories()), getMockGroupsCacheFactory());
 
         assertComplexObjectsEqual("uiConfiguration is wrong",
                 createUiConfigurationWithMultipleValidGroups(), configurationFactory
                 .getUiConfiguration("common/main.css"));
     }
 
     @Test
     public void testImportGlobalGroups() throws Throwable {
         recordLoggerIsInfoEnabled(Boolean.TRUE);
 
         final ClassPathResource sourceClassPathResource =
                 new ClassPathResource(CRF_CONFIG_IMPORT_GLOBAL_GROUPS_CLASSPATH);
 
         recordLoggerInfo("Resolving import for " + sourceClassPathResource.getURL() + ": "
                 + getGroupImportTestData().createAndroidOsImportFromDefaultNamespace(), 1);
         recordLoggerInfo("Resolving import for " + sourceClassPathResource.getURL() + ": "
                 + getGroupImportTestData().createiPadImportFromNonDefaultNamespace(), 1);
         recordLoggerInfo("Resolving import for " + sourceClassPathResource.getURL() + ": "
                 + getGroupImportTestData().createImageCategoriesGroupImport(), 1);
 
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationForGlobalDevices(), 1);
         recordLoggerInfo("Loaded configuration: " + createUiConfigurationForGlobalExtraDevices(),
                 1);
         recordLoggerInfo(
                 "Loaded configuration: " + createUiConfigurationForGlobalImageCategories(), 1);
         recordLoggerInfo(
                 "Loaded configuration: " + createUiConfigurationWhenGlobalGroupsImported(), 1);
 
         recordCreateGroupsCache();
 
         replay();
 
         final ConfigurationFactory configurationFactory =
                 new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                         getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                         getMockResourceResolutionWarnLogger(), createConfigurationPaths(
                                 CRF_CONFIG_IMPORT_GLOBAL_GROUPS_CLASSPATH_PATTERN,
                                 getUiResourceRootDirectories()), getMockGroupsCacheFactory());
 
         assertComplexObjectsEqual("uiConfiguration for pattern match 1 is wrong",
                 createUiConfigurationWhenGlobalGroupsImported(), configurationFactory
                         .getUiConfiguration("main.js"));
     }
 
     @Test
     public void testImportGlobalGroupsFromInvalidConfigPath() throws Throwable {
         recordLoggerIsInfoEnabled(Boolean.FALSE);
 
         recordCreateGroupsCache();
 
         replay();
 
         try {
             new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                     getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                     getMockResourceResolutionWarnLogger(), createConfigurationPaths(
                             "/au/com/sensis/mobile/crf/config/"
                                     + "crf-config-import-from-unknown-config-path.xml",
                             getUiResourceRootDirectories()), getMockGroupsCacheFactory());
 
             Assert.fail("ConfigurationRuntimeException expected");
         } catch (final ConfigurationRuntimeException e) {
 
             Assert.assertEquals("ConfigurationRuntimeException has wrong message",
                     "No global UiConfiguration found with a configPath "
                             + "matching requested import path: 'global/i-don't-exist'", e
                             .getMessage());
         }
 
     }
 
     @Test
     public void testImportGlobalGroupsFromAnotherGlobalGroupsConfigPath() throws Throwable {
         recordCreateGroupsCache();
 
         replay();
 
         final String configFileClasspathPattern = "/au/com/sensis/mobile/crf/config/"
                 + "crf-config-global-imports-another-global.xml,"
                 + CRF_CONFIG_MULTIPLE_VALID_GROUPS;
         try {
             new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                     getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                     getMockResourceResolutionWarnLogger(), createConfigurationPaths(
                             configFileClasspathPattern,
                             getUiResourceRootDirectories()), getMockGroupsCacheFactory());
 
             Assert.fail("ConfigurationRuntimeException expected");
         } catch (final ConfigurationRuntimeException e) {
 
             final ClassPathResource sourceClassPathResource =
                 new ClassPathResource("/au/com/sensis/mobile/crf/config/"
                         + "crf-config-global-imports-another-global.xml");
 
             Assert.assertEquals("ConfigurationRuntimeException has wrong message",
                     "Illegal for global UiConfiguration at '" + sourceClassPathResource.getURL()
                     + "' to import from another global config path of 'global/you should not "
                     + "import another global'. Note that global configs have a config path "
                     + "starting with " + UiConfiguration.GLOBAL_CONFIG_PATH_PREFIX, e.getMessage());
         }
 
     }
 
     @Test
     public void testImportNonGlobalGroupsFromAnotherNonGlobalGroupsConfigPath() throws Throwable {
         recordCreateGroupsCache();
 
         replay();
 
         final String configFileClasspathPattern = "/au/com/sensis/mobile/crf/config/"
             + "crf-config-non-global-imports-another-non-global.xml,"
             + CRF_CONFIG_MULTIPLE_VALID_GROUPS;
         try {
             new ConfigurationFactoryBean(getCacheEnabledDeploymentMetadata(),
                     getResourcePatternResolver(), getXmlBinder(), getXmlValidator(),
                     getMockResourceResolutionWarnLogger(), createConfigurationPaths(
                             configFileClasspathPattern,
                             getUiResourceRootDirectories()), getMockGroupsCacheFactory());
 
             Assert.fail("ConfigurationRuntimeException expected");
         } catch (final ConfigurationRuntimeException e) {
 
             final ClassPathResource sourceClassPathResource =
                 new ClassPathResource("/au/com/sensis/mobile/crf/config/"
                         + "crf-config-non-global-imports-another-non-global.xml");
 
             Assert.assertEquals("ConfigurationRuntimeException has wrong message",
                     "Illegal for non-global UiConfiguration at '" + sourceClassPathResource.getURL()
                     + "' to import from another non-global config path of "
                     + "'you should not import a non-global config'"
                     + ". Note that global configs have a config path "
                     + "starting with " + UiConfiguration.GLOBAL_CONFIG_PATH_PREFIX, e.getMessage());
         }
 
     }
 
     private UiConfiguration createUiConfigurationWithMultipleValidGroups() throws IOException {
         final UiConfiguration uiConfiguration = new UiConfiguration();
 
         final ClassPathResource sourceClassPathResource =
                 new ClassPathResource(CRF_CONFIG_MULTIPLE_VALID_GROUPS);
         uiConfiguration.setSourceUrl(sourceClassPathResource.getURL());
         uiConfiguration.setSourceTimestamp(sourceClassPathResource.lastModified());
 
         uiConfiguration.setConfigPath(StringUtils.EMPTY);
         final Group group1 = getGroupTestData().createIPhoneGroup();
         final Group group2 = getGroupTestData().createAndroidGroup();
         final Group group3 = getGroupTestData().createIpadGroup();
         final Group group4 = getGroupTestData().createAppleWebkitGroup();
         final Group group5 = getGroupTestData().createLargeImageCategoryGroup();
         final Group group6 = getGroupTestData().createMediumGroup();
 
         final Groups groups = new Groups();
         groups.setGroups(new Group[] { group1, group2, group3, group4, group5, group6 });
         groups.setDefaultGroup(getGroupTestData().createDefaultGroup());
 
         uiConfiguration.setGroups(groups);
 
         uiConfiguration.setMatchingGroupsCache(getMockGroupsCache());
 
         return uiConfiguration;
     }
 
     private UiConfiguration createUiConfigurationForPatternMatch1() throws IOException {
         final UiConfiguration uiConfiguration = new UiConfiguration();
 
         final ClassPathResource sourceClassPathResource =
                 new ClassPathResource(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN_MATCH1);
         uiConfiguration.setSourceUrl(sourceClassPathResource.getURL());
         uiConfiguration.setSourceTimestamp(sourceClassPathResource.lastModified());
 
         uiConfiguration.setConfigPath("component/component1");
 
         final Group group1 = getGroupTestData().createIPhoneGroup();
         final Group group2 = getGroupTestData().createIpadGroup();
         final Group group3 = getGroupTestData().createAppleWebkitGroup();
         final Group group4 = getGroupTestData().createAndroidGroup();
         final Group group5 = getGroupTestData().createLargeImageCategoryGroup();
         final Group group6 = getGroupTestData().createMediumGroup();
 
 
         final Groups groups = new Groups();
         groups.setGroups(new Group[] { group1, group2, group3, group4, group5, group6 });
         groups.setDefaultGroup(getGroupTestData().createDefaultGroup());
 
         uiConfiguration.setGroups(groups);
 
         return uiConfiguration;
     }
 
     private UiConfiguration createUiConfigurationForPatternMatch2() throws IOException {
         final UiConfiguration uiConfiguration = new UiConfiguration();
 
         final ClassPathResource sourceClassPathResource =
                 new ClassPathResource(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN_MATCH2);
         uiConfiguration.setSourceUrl(sourceClassPathResource.getURL());
         uiConfiguration.setSourceTimestamp(sourceClassPathResource.lastModified());
 
         uiConfiguration.setConfigPath("component/component2");
 
         final Group group1 = getGroupTestData().createIPhoneGroup();
         final Group group2 = getGroupTestData().createIpadGroup();
         final Group group3 = getGroupTestData().createAppleWebkitGroup();
         final Group group4 = getGroupTestData().createAndroidGroup();
         final Group group5 = getGroupTestData().createLargeImageCategoryGroup();
         final Group group6 = getGroupTestData().createMediumGroup();
 
         final Groups groups = new Groups();
         groups.setGroups(new Group[] { group1, group2, group3, group4, group5, group6 });
         groups.setDefaultGroup(getGroupTestData().createDefaultGroup());
 
         uiConfiguration.setGroups(groups);
 
         return uiConfiguration;
     }
 
     private UiConfiguration createUiConfigurationForPatternMatch3() throws IOException {
         final UiConfiguration uiConfiguration = new UiConfiguration();
 
         final ClassPathResource sourceClassPathResource =
                 new ClassPathResource(CRF_CONFIG_MULTIPLE_FILES_CLASSPATH_PATTERN_MATCH3);
         uiConfiguration.setSourceUrl(sourceClassPathResource.getURL());
         uiConfiguration.setSourceTimestamp(sourceClassPathResource.lastModified());
 
         uiConfiguration.setConfigPath(StringUtils.EMPTY);
 
         final Groups groups = new Groups();
         groups.setDefaultGroup(getGroupTestData().createDefaultGroup());
 
         uiConfiguration.setGroups(groups);
 
         return uiConfiguration;
     }
 
     private UiConfiguration createUiConfigurationWhenGlobalGroupsImported() throws IOException {
         final UiConfiguration uiConfiguration = new UiConfiguration();
 
         final ClassPathResource sourceClassPathResource =
                 new ClassPathResource(CRF_CONFIG_IMPORT_GLOBAL_GROUPS_CLASSPATH);
         uiConfiguration.setSourceUrl(sourceClassPathResource.getURL());
         uiConfiguration.setSourceTimestamp(sourceClassPathResource.lastModified());
 
         uiConfiguration.setConfigPath(StringUtils.EMPTY);
 
         final Group group1 = getGroupTestData().createIPhoneGroup();
         final Group group2 = getGroupTestData().createAndroidGroup();
         final Group group3 = getGroupTestData().createIpadGroup();
         final Group group4 = getGroupTestData().createAppleWebkitGroup();
         final Group group5 = getGroupTestData().createLargeImageCategoryGroup();
         final Group group6 = getGroupTestData().createMediumGroup();
 
         final Groups groups = new Groups();
         groups.setGroups(new Group[] { group1, group2, group3, group4, group5, group6 });
         groups.setDefaultGroup(getGroupTestData().createDefaultGroup());
 
         uiConfiguration.setGroups(groups);
 
         return uiConfiguration;
     }
 
     private UiConfiguration createUiConfigurationForGlobalDevices() throws IOException {
         final UiConfiguration uiConfiguration = new UiConfiguration();
 
         final ClassPathResource sourceClassPathResource =
             new ClassPathResource("/au/com/sensis/mobile/crf/config/crf-config-global-devices.xml");
         uiConfiguration.setSourceUrl(sourceClassPathResource.getURL());
         uiConfiguration.setSourceTimestamp(sourceClassPathResource.lastModified());
 
         uiConfiguration.setConfigPath("global/devices");
 
         final Group group1 = getGroupTestData().createAndroidGroup();
         final Group group2 = getGroupTestData().createIpadGroup();
 
         final Groups groups = new Groups();
         groups.setGroups(new Group[] { group1, group2});
         groups.setDefaultGroup(getGroupTestData().createDefaultGroup());
 
         uiConfiguration.setGroups(groups);
 
         return uiConfiguration;
     }
 
     private UiConfiguration createUiConfigurationForGlobalExtraDevices() throws IOException {
         final UiConfiguration uiConfiguration = new UiConfiguration();
 
         final ClassPathResource sourceClassPathResource =
                 new ClassPathResource("/au/com/sensis/mobile/crf/config/"
                         + "crf-config-global-extra-devices.xml");
         uiConfiguration.setSourceUrl(sourceClassPathResource.getURL());
         uiConfiguration.setSourceTimestamp(sourceClassPathResource.lastModified());
 
         uiConfiguration.setConfigPath("global/extraDevices");
 
         final Group group1 = getGroupTestData().createIpadGroup();
 
         final Groups groups = new Groups();
         groups.setGroups(new Group[] { group1 });
         groups.setDefaultGroup(getGroupTestData().createDefaultGroup());
 
         uiConfiguration.setGroups(groups);
 
         return uiConfiguration;
     }
 
     private UiConfiguration createUiConfigurationForGlobalImageCategories() throws IOException {
         final UiConfiguration uiConfiguration = new UiConfiguration();
 
         final ClassPathResource sourceClassPathResource =
             new ClassPathResource("/au/com/sensis/mobile/crf/config/"
                     + "crf-config-global-image-categories.xml");
         uiConfiguration.setSourceUrl(sourceClassPathResource.getURL());
         uiConfiguration.setSourceTimestamp(sourceClassPathResource.lastModified());
 
         uiConfiguration.setConfigPath("global/imageCategories");
 
         final Group group1 = getGroupTestData().createLargeImageCategoryGroup();
         final Group group2 = getGroupTestData().createMediumGroup();
 
         final Groups groups = new Groups();
         groups.setGroups(new Group[] { group1, group2 });
         groups.setDefaultGroup(getGroupTestData().createDefaultGroup());
 
         uiConfiguration.setGroups(groups);
 
         return uiConfiguration;
     }
 
     private static final class TestData {
 
         private final String configFileClasspathLocation;
         private String expectedExceptionCauseMessage;
 
         private TestData(final String configFileClasspathLocation,
                 final String expectedExceptionCauseMessage) {
             this.configFileClasspathLocation = configFileClasspathLocation;
             this.expectedExceptionCauseMessage = expectedExceptionCauseMessage;
         }
 
         private TestData(final String configFileClasspathLocation) {
             this.configFileClasspathLocation = configFileClasspathLocation;
         }
 
         private static TestData [] createTestDataForSchemaValidationFailure() {
             // NOTE: the expectedExceptionCauseMessage is tied to the schema validator
             // implementation. We'd rather not do this but testing against the explicit
             // messages produces stronger tests than simply testing that an exception
             // was thrown.
             return new TestData [] {
                 new TestData("/au/com/sensis/mobile/crf/config/crf-config-no-namespace.xml",
                         ".*Cannot find the declaration of element 'ui-configuration'.*"),
                 new TestData("/au/com/sensis/mobile/crf/config/crf-config-no-groups-element.xml",
                         ".*The content of element 'crf:ui-configuration' "
                             + "is not complete. One of '\\{.*groups\\}' is expected.*"),
                 new TestData("/au/com/sensis/mobile/crf/config/crf-config-groups-element-empty.xml",
                         ".*content of element 'groups' is not "
                         + "complete. One of '\\{.*group,.*default-group\\}' is expected.*"),
             };
         }
 
         private static TestData [] createTestDataForSchemaValidationSuccess() {
             return new TestData [] {
                     new TestData(
                         "/au/com/sensis/mobile/crf/config/crf-config-only-default-group.xml"),
                     new TestData(CRF_CONFIG_MULTIPLE_VALID_GROUPS),
             };
         }
 
         /**
          * @return the configFileClasspathLocation
          */
         private String getConfigFileClasspathLocation() {
             return configFileClasspathLocation;
         }
 
         /**
          * @return the expectedExceptionCauseMessage
          */
         private String getExpectedExceptionCauseMessage() {
             return expectedExceptionCauseMessage;
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public String toString() {
             return new ToStringBuilder(this)
                 .append("configFileClasspathLocation", getConfigFileClasspathLocation())
                 .append("expectedExceptionCauseMessage", getExpectedExceptionCauseMessage())
                 .toString();
         }
 
 
 
 
     }
 
     private XmlValidator getXmlValidator() {
         return xmlValidator;
     }
 
     private XmlBinder getXmlBinder() {
         return xmlBinder;
     }
 
     /**
      * @param xmlBinder the xmlBinder to set
      */
     private void setXmlBinder(final XmlBinder xmlBinder) {
         this.xmlBinder = xmlBinder;
     }
 
     private ResourcePatternResolver getResourcePatternResolver() {
         return resourcePatternResolver;
     }
 
     private DeploymentMetadataTestData getDeploymentMetadataTestData() {
         return deploymentMetadataTestData;
     }
 
     private DeploymentMetadata getCacheEnabledDeploymentMetadata() {
         return getDeploymentMetadataTestData()
                 .createCacheUiConfigurationEnabledDeploymentMetadata();
     }
 
     private DeploymentMetadata getCacheDisabledDeploymentMetadata() {
         return getDeploymentMetadataTestData()
                 .createCacheUiConfigurationDisabledDeploymentMetadata();
     }
 
     public ResourcePatternResolver getMockResourcePatternResolver() {
         return mockResourcePatternResolver;
     }
 
     public void setMockResourcePatternResolver(
             final ResourcePatternResolver mockResourcePatternResolver) {
         this.mockResourcePatternResolver = mockResourcePatternResolver;
     }
 
     public Resource getMockResource1() {
         return mockResource1;
     }
 
     public void setMockResource1(final Resource mockResource1) {
         this.mockResource1 = mockResource1;
     }
 
     public Resource getMockResource2() {
         return mockResource2;
     }
 
     public void setMockResource2(final Resource mockResource2) {
         this.mockResource2 = mockResource2;
     }
 
     /**
      * @return the mockResourceResolutionWarnLogger
      */
     public ResourceResolutionWarnLogger getMockResourceResolutionWarnLogger() {
         return mockResourceResolutionWarnLogger;
     }
 
     /**
      * @param mockResourceResolutionWarnLogger the mockResourceResolutionWarnLogger to set
      */
     public void setMockResourceResolutionWarnLogger(
             final ResourceResolutionWarnLogger mockResourceResolutionWarnLogger) {
         this.mockResourceResolutionWarnLogger = mockResourceResolutionWarnLogger;
     }
 
     /**
      * @return the uiResourceRootDirectories
      */
     private List<File> getUiResourceRootDirectories() {
         return uiResourceRootDirectories;
     }
 
     /**
      * @param uiResourceRootDirectories the uiResourceRootDirectories to set
      */
     private void setUiResourceRootDirectories(final List<File> uiResourceRootDirectories) {
         this.uiResourceRootDirectories = uiResourceRootDirectories;
     }
 
     private File getValidUiResourcesCssRootDir() throws Exception {
         return new File(getClass().getResource(VALID_CSS_ROOT_DIR_CLASSPATH).toURI());
     }
 
     private File getValidUiResourcesImagesRootDir() throws Exception {
         return new File(getClass().getResource(VALID_IMAGES_ROOT_DIR_CLASSPATH).toURI());
     }
 
     private File getMissingGroupsUiResourcesCssRootDir() throws Exception {
         return new File(getClass().getResource(MISSING_GROUPS_CSS_ROOT_DIR_CLASSPATH).toURI());
     }
 
     private File getMissingGroupsUiResourcesImagesRootDir() throws Exception {
         return new File(getClass().getResource(MISSING_GROUPS_IMAGES_ROOT_DIR_CLASSPATH).toURI());
     }
 
     private File getExtraGroupsUiResourcesCssRootDir() throws Exception {
         return new File(getClass().getResource(EXTRA_GROUPS_CSS_ROOT_DIR_CLASSPATH).toURI());
     }
 
     private File getExtraCssGroupDir() throws Exception {
         return new File(getExtraGroupsUiResourcesCssRootDir(), "thub");
     }
 
     private File getExtraGroupsUiResourcesImagesRootDir() throws Exception {
         return new File(getClass().getResource(EXTRA_GROUPS_MAGES_ROOT_DIR_CLASSPATH).toURI());
     }
 
     private File getExtraImagesGroupDir() throws Exception {
         return new File(getExtraGroupsUiResourcesImagesRootDir(), "thub");
     }
 
     /**
      * @return the mockGroupsCacheFactory
      */
     public GroupsCacheFactory getMockGroupsCacheFactory() {
         return mockGroupsCacheFactory;
     }
 
     /**
      * @param mockGroupsCacheFactory the mockGroupsCacheFactory to set
      */
     public void setMockGroupsCacheFactory(final GroupsCacheFactory mockGroupsCacheFactory) {
         this.mockGroupsCacheFactory = mockGroupsCacheFactory;
     }
 
     /**
      * @return the mockGroupsCache
      */
     public GroupsCache getMockGroupsCache() {
         return mockGroupsCache;
     }
 
     /**
      * @param mockGroupsCache the mockGroupsCache to set
      */
     public void setMockGroupsCache(final GroupsCache mockGroupsCache) {
         this.mockGroupsCache = mockGroupsCache;
     }
 
     /**
      * @return the groupTestData
      */
     private GroupTestData getGroupTestData() {
         return groupTestData;
     }
 
     /**
      * @param groupTestData the groupTestData to set
      */
     private void setGroupTestData(final GroupTestData groupTestData) {
         this.groupTestData = groupTestData;
     }
 
     /**
      * @return the groupImportTestData
      */
     private GroupImportTestData getGroupImportTestData() {
         return groupImportTestData;
     }
 
     /**
      * @param groupImportTestData the groupImportTestData to set
      */
     private void setGroupImportTestData(final GroupImportTestData groupImportTestData) {
         this.groupImportTestData = groupImportTestData;
     }
 }
