 package au.com.sensis.mobile.crf.service;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.easymock.EasyMock;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import au.com.sensis.devicerepository.Device;
 import au.com.sensis.mobile.crf.config.DeploymentMetadata;
 import au.com.sensis.mobile.crf.config.Group;
 import au.com.sensis.mobile.crf.exception.ResourceResolutionRuntimeException;
 import au.com.sensis.mobile.crf.util.ImageAttributes;
 import au.com.sensis.mobile.crf.util.ImageAttributesBean;
 import au.com.sensis.mobile.crf.util.ImageReader;
 import au.com.sensis.mobile.crf.util.ImageTransformationFactory;
 import au.com.sensis.mobile.crf.util.ImageTransformationFactory.ImageTransformationParameters;
 import au.com.sensis.mobile.crf.util.ImageTransformationParametersBean;
 import au.com.sensis.mobile.crf.util.TransformedImageAttributes;
 import au.com.sensis.mobile.crf.util.TransformedImageAttributesBean;
 
 /**
  * Unit test {@link TransformedImageResourceResolverBean}.
  *
  * @author Adrian.Koh2@sensis.com.au
  */
 public class TransformedImageResourceResolverBeanTestCase extends AbstractResourceResolverTestCase {
 
     private static final int OUTPUT_IMAGE_PIXEL_HEIGHT = 60;
     private static final int OUTPUT_IMAGE_PIXEL_WIDTH = 90;
     private static final String[] MATCHED_IMAGE_FILE_EXTENSION_WILDCARDS = new String[] { "*" };
     private static final String[] EXCLUDED_IMAGE_FILE_EXTENSION_WILDCARDS
     = new String[] { "properties", "*.md5" };
 
     private static final int SOURCE_IMAGE_PIXEL_WIDTH = 800;
     private static final int SOURCE_IMAGE_PIXEL_HEIGHT = 600;
     private static final String BACKGROUND_COLOR = "#FFF";
     private static final String IMAGE_FORMAT_DEVICE_REPOSITORY_PROPERTY_NAME = "preferredimagetype";
 
     private Device mockDevice;
     private TransformedImageResourceResolverBean objectUnderTest;
     private au.com.sensis.mobile.crf.util.PropertiesLoader mockPropertiesLoader;
     private ImageTransformationFactory mockImageTransformationFactory;
     private ImageReader mockImageReader;
 
     /**
      * Setup test data.
      *
      * @throws Exception
      *             Thrown if any error occurs.
      */
     @Before
     public void setUp() throws Exception {
 
         setObjectUnderTest(new TransformedImageResourceResolverBean(
                 getResourceResolverCommonParamHolder(), getResourcePathTestData()
                 .getAbstractImageExtensionWithLeadingDot(), getResourcesRootDir(),
                 MATCHED_IMAGE_FILE_EXTENSION_WILDCARDS,
                 IMAGE_FORMAT_DEVICE_REPOSITORY_PROPERTY_NAME));
 
         getObjectUnderTest().setPropertiesLoader(getMockPropertiesLoader());
         getObjectUnderTest().setImageTransformationFactory(getMockImageTransformationFactory());
         getObjectUnderTest().setImageReader(getMockImageReader());
 
     }
 
     @Override
     protected TransformedImageResourceResolverBean createWithAbstractResourceExtension(
             final String abstractResourceExtension) {
 
         final TransformedImageResourceResolverBean resolverBean =
                 new TransformedImageResourceResolverBean(getResourceResolverCommonParamHolder(),
                         abstractResourceExtension, getResourcesRootDir(),
                         MATCHED_IMAGE_FILE_EXTENSION_WILDCARDS,
                         IMAGE_FORMAT_DEVICE_REPOSITORY_PROPERTY_NAME);
 
         resolverBean.setPropertiesLoader(getMockPropertiesLoader());
         resolverBean.setImageTransformationFactory(getMockImageTransformationFactory());
         resolverBean.setImageReader(getMockImageReader());
 
         return resolverBean;
     }
 
     @Override
     protected TransformedImageResourceResolverBean createWithResourceResolutionWarnLogger(
             final ResourceResolutionWarnLogger resourceResolutionWarnLogger) {
 
         final ResourceResolverCommonParamHolder commonParams =
                 new ResourceResolverCommonParamHolder(
                         resourceResolutionWarnLogger, getDeploymentMetadata(),
                         getMockConfigurationFactory(), getMockResourceCache());
 
         return new TransformedImageResourceResolverBean(commonParams,
                 getResourcePathTestData().getCssExtensionWithoutLeadingDot(), getResourcesRootDir(),
                 MATCHED_IMAGE_FILE_EXTENSION_WILDCARDS,
                 IMAGE_FORMAT_DEVICE_REPOSITORY_PROPERTY_NAME);
     }
 
     @Override
     protected TransformedImageResourceResolverBean createWithRootResourcesDir(
             final File rootResourcesDir) {
 
         return new TransformedImageResourceResolverBean(getResourceResolverCommonParamHolder(),
                 getResourcePathTestData().getCssExtensionWithoutLeadingDot(), rootResourcesDir,
                 MATCHED_IMAGE_FILE_EXTENSION_WILDCARDS,
                 IMAGE_FORMAT_DEVICE_REPOSITORY_PROPERTY_NAME);
     }
 
     @Override
     protected TransformedImageResourceResolverBean createWithDeploymentMetadata(
             final DeploymentMetadata deploymentMetadata) {
 
         final ResourceResolverCommonParamHolder commonParams =
                 new ResourceResolverCommonParamHolder(
                         getMockResourceResolutionWarnLogger(), deploymentMetadata,
                         getMockConfigurationFactory(), getMockResourceCache());
 
         return new TransformedImageResourceResolverBean(commonParams,
                 getResourcePathTestData().getAbstractImageExtensionWithLeadingDot(),
                 getResourcesRootDir(), MATCHED_IMAGE_FILE_EXTENSION_WILDCARDS,
                 IMAGE_FORMAT_DEVICE_REPOSITORY_PROPERTY_NAME);
     }
 
     @Test
     public void testConstructorWhenFileExtensionWildcardsIsInvalid()
             throws Throwable {
 
         final List<String []> testVaues = Arrays.asList(
                 null,
                 new String [] {},
                 new String [] { null },
                 new String [] { StringUtils.EMPTY },
                 new String [] { " " },
                 new String [] { "  " }
                 );
 
         for (final String [] testValue : testVaues) {
             try {
                 new TransformedImageResourceResolverBean(getResourceResolverCommonParamHolder(),
                         getResourcePathTestData().getCssExtensionWithoutLeadingDot(),
                         getResourcesRootDir(), testValue,
                         IMAGE_FORMAT_DEVICE_REPOSITORY_PROPERTY_NAME);
 
                 Assert.fail("IllegalArgumentException expected for testValue: '"
                         + ArrayUtils.toString(testValue) + "'");
             } catch (final IllegalArgumentException e) {
 
                 Assert.assertEquals("IllegalArgumentException has wrong message",
                         "fileExtensionWildcards must be an array of non-blank Strings but was: '"
                                 + ArrayUtils.toString(testValue) + "'", e.getMessage());
             }
         }
 
     }
 
     @Test
     public void testResolveWhenSingleResourceFoundAndPngOutputPreferredAndSourceIsPng()
             throws Throwable {
 
         final String[] testValues =
             { getResourcePathTestData().getAbstractImageExtensionWithLeadingDot(),
                 getResourcePathTestData().getAbstractImageExtensionWithoutLeadingDot() };
 
         for (final String testValue : testValues) {
             setObjectUnderTest(createWithAbstractResourceExtension(testValue));
 
             recordGetMatchingGroups();
             final ResourceCacheKey resourceCacheKey = createResourceCacheKey();
             recordCheckResourceCache(resourceCacheKey, Boolean.FALSE);
 
             recordGetMatchingGroupIterator(2);
 
             recordListIphoneFilesByExtension(getSingleMatchedPngImageArray());
 
             recordIphoneImagePropertiesFileExists(Boolean.TRUE);
             recordAppleImagePropertiesFileExists(Boolean.TRUE);
 
             recordLoadIphoneGroupImageProperties(createIphoneGroupPercentWidthImageProperties());
             recordLoadAppleGroupImageProperties(createAppleGroupPercentWidthImageProperties());
 
             recordGetPNGPreferredImageType();
 
             recordGetDeviceAttributesForImageScaling();
 
             recordGetImageRatioDeviceProperty(null);
             recordTransformPngImage(createPercentWidthImageTransformationParameters(
                     1), SOURCE_IMAGE_PIXEL_WIDTH, SOURCE_IMAGE_PIXEL_HEIGHT,
                     OUTPUT_IMAGE_PIXEL_WIDTH, OUTPUT_IMAGE_PIXEL_HEIGHT,
                     getMappedScaledIphoneGroupPngImageResourcePath());
 
             recordPutResourceCache(resourceCacheKey,
                     getMappedScaledIphoneGroupPngImageResourcePath());
 
             replay();
 
             final List<Resource> actualResources =
                     getObjectUnderTest().resolve(
                             getResourcePathTestData().getRequestedImageResourcePath(),
                             getMockDevice());
 
             // Explicit verify since we are in a loop.
             verify();
 
             final List<Resource> expectedResources =
                     Arrays.asList(getMappedScaledIphoneGroupPngImageResourcePath());
             assertComplexObjectsEqual("actualResources is wrong", expectedResources,
                     actualResources);
 
             assertResourceResolutionTreeUpdated(expectedResources);
 
             // Explicit reset since we are in a loop.
             reset();
         }
 
     }
 
     @Test
     public void testResolveWhenSingleResourceFoundAndPngOutputPreferredAndSourceIsGif()
             throws Throwable {
 
         final String[] testValues =
             { getResourcePathTestData().getAbstractImageExtensionWithLeadingDot(),
                 getResourcePathTestData().getAbstractImageExtensionWithoutLeadingDot() };
 
         for (final String testValue : testValues) {
             setObjectUnderTest(createWithAbstractResourceExtension(testValue));
 
             recordGetMatchingGroups();
             final ResourceCacheKey resourceCacheKey = createResourceCacheKey();
             recordCheckResourceCache(resourceCacheKey, Boolean.FALSE);
 
             recordGetMatchingGroupIterator(2);
 
             recordListIphoneFilesByExtension(getSingleMatchedGifImageArray());
 
             recordIphoneImagePropertiesFileExists(Boolean.TRUE);
             recordAppleImagePropertiesFileExists(Boolean.TRUE);
 
             recordLoadIphoneGroupImageProperties(createIphoneGroupGifPercentWidthImageProperties());
             recordLoadAppleGroupImageProperties(createAppleGroupPercentWidthImageProperties());
 
             recordGetDeviceAttributesForImageScaling();
 
             recordGetImageRatioDeviceProperty(null);
             recordTransformGifImage(createPercentWidthGifImageTransformationParameters(1),
                     SOURCE_IMAGE_PIXEL_WIDTH, SOURCE_IMAGE_PIXEL_HEIGHT, OUTPUT_IMAGE_PIXEL_WIDTH,
                     OUTPUT_IMAGE_PIXEL_HEIGHT, getMappedScaledIphoneGroupGifImageResourcePath());
 
             recordPutResourceCache(resourceCacheKey,
                     getMappedScaledIphoneGroupGifImageResourcePath());
 
             replay();
 
             final List<Resource> actualResources =
                     getObjectUnderTest().resolve(
                             getResourcePathTestData().getRequestedImageResourcePath(),
                             getMockDevice());
 
             // Explicit verify since we are in a loop.
             verify();
 
             final List<Resource> expectedResources =
                     Arrays.asList(getMappedScaledIphoneGroupGifImageResourcePath());
             assertComplexObjectsEqual("actualResources is wrong", expectedResources,
                     actualResources);
 
             assertResourceResolutionTreeUpdated(expectedResources);
 
             // Explicit reset since we are in a loop.
             reset();
         }
 
     }
 
     @Test
     public void testResolveWhenSingleResourceFoundAndJpegOutputPreferred()
             throws Throwable {
 
         final String[] testValues =
             { getResourcePathTestData().getAbstractImageExtensionWithLeadingDot(),
                 getResourcePathTestData().getAbstractImageExtensionWithoutLeadingDot() };
 
         for (final String testValue : testValues) {
             setObjectUnderTest(createWithAbstractResourceExtension(testValue));
 
             recordGetMatchingGroups();
             final ResourceCacheKey resourceCacheKey = createResourceCacheKey();
             recordCheckResourceCache(resourceCacheKey, Boolean.FALSE);
 
             recordGetMatchingGroupIterator(2);
 
             recordListIphoneFilesByExtension(getSingleMatchedPngImageArray());
 
             recordIphoneImagePropertiesFileExists(Boolean.TRUE);
             recordAppleImagePropertiesFileExists(Boolean.TRUE);
 
             recordLoadIphoneGroupImageProperties(createIphoneGroupGifPercentWidthImageProperties());
             recordLoadAppleGroupImageProperties(createAppleGroupPercentWidthImageProperties());
 
             recordGetJpegPreferredImageType();
 
             recordGetDeviceAttributesForImageScaling();
 
             recordGetImageRatioDeviceProperty(null);
             recordTransformPngImage(createPercentWidthGifImageTransformationParameters(1),
                     SOURCE_IMAGE_PIXEL_WIDTH, SOURCE_IMAGE_PIXEL_HEIGHT, OUTPUT_IMAGE_PIXEL_WIDTH,
                     OUTPUT_IMAGE_PIXEL_HEIGHT, getMappedScaledIphoneGroupGifImageResourcePath());
 
             recordPutResourceCache(resourceCacheKey,
                     getMappedScaledIphoneGroupGifImageResourcePath());
 
             replay();
 
             final List<Resource> actualResources =
                     getObjectUnderTest().resolve(
                             getResourcePathTestData().getRequestedImageResourcePath(),
                             getMockDevice());
 
             // Explicit verify since we are in a loop.
             verify();
 
             final List<Resource> expectedResources =
                     Arrays.asList(getMappedScaledIphoneGroupGifImageResourcePath());
             assertComplexObjectsEqual("actualResources is wrong", expectedResources,
                     actualResources);
 
             assertResourceResolutionTreeUpdated(expectedResources);
 
             // Explicit reset since we are in a loop.
             reset();
         }
 
     }
 
     @Test
     public void testResolveWhenSingleResourceFoundAndNullPreferredOutputFormat()
             throws Throwable {
 
         final String[] testValues =
             { getResourcePathTestData().getAbstractImageExtensionWithLeadingDot(),
                 getResourcePathTestData().getAbstractImageExtensionWithoutLeadingDot() };
 
         for (final String testValue : testValues) {
             setObjectUnderTest(createWithAbstractResourceExtension(testValue));
 
             recordGetMatchingGroups();
             final ResourceCacheKey resourceCacheKey = createResourceCacheKey();
             recordCheckResourceCache(resourceCacheKey, Boolean.FALSE);
 
             recordGetMatchingGroupIterator(2);
 
             recordListIphoneFilesByExtension(getSingleMatchedPngImageArray());
 
             recordIphoneImagePropertiesFileExists(Boolean.TRUE);
             recordAppleImagePropertiesFileExists(Boolean.TRUE);
 
             recordLoadIphoneGroupImageProperties(createIphoneGroupGifPercentWidthImageProperties());
             recordLoadAppleGroupImageProperties(createAppleGroupPercentWidthImageProperties());
 
             recordGetNullPreferredImageType();
 
             recordGetDeviceAttributesForImageScaling();
 
             recordGetImageRatioDeviceProperty(null);
             recordTransformPngImage(createPercentWidthGifImageTransformationParameters(1),
                     SOURCE_IMAGE_PIXEL_WIDTH, SOURCE_IMAGE_PIXEL_HEIGHT, OUTPUT_IMAGE_PIXEL_WIDTH,
                     OUTPUT_IMAGE_PIXEL_HEIGHT, getMappedScaledIphoneGroupGifImageResourcePath());
 
             recordPutResourceCache(resourceCacheKey,
                     getMappedScaledIphoneGroupGifImageResourcePath());
 
             replay();
 
             final List<Resource> actualResources =
                     getObjectUnderTest().resolve(
                             getResourcePathTestData().getRequestedImageResourcePath(),
                             getMockDevice());
 
             // Explicit verify since we are in a loop.
             verify();
 
             final List<Resource> expectedResources =
                     Arrays.asList(getMappedScaledIphoneGroupGifImageResourcePath());
             assertComplexObjectsEqual("actualResources is wrong", expectedResources,
                     actualResources);
 
             assertResourceResolutionTreeUpdated(expectedResources);
 
             // Explicit reset since we are in a loop.
             reset();
         }
 
     }
 
     @Test
     public void testResolveWhenSingleResourceFoundAndGifOutputPreferred()
             throws Throwable {
 
         final String[] testValues =
             { getResourcePathTestData().getAbstractImageExtensionWithLeadingDot(),
                 getResourcePathTestData().getAbstractImageExtensionWithoutLeadingDot() };
 
         for (final String testValue : testValues) {
             setObjectUnderTest(createWithAbstractResourceExtension(testValue));
 
             recordGetMatchingGroups();
             final ResourceCacheKey resourceCacheKey = createResourceCacheKey();
             recordCheckResourceCache(resourceCacheKey, Boolean.FALSE);
 
             recordGetMatchingGroupIterator(2);
 
             recordListIphoneFilesByExtension(getSingleMatchedPngImageArray());
 
             recordIphoneImagePropertiesFileExists(Boolean.TRUE);
             recordAppleImagePropertiesFileExists(Boolean.TRUE);
 
             recordLoadIphoneGroupImageProperties(createIphoneGroupGifPercentWidthImageProperties());
             recordLoadAppleGroupImageProperties(createAppleGroupPercentWidthImageProperties());
 
             recordGetGifPreferredImageType();
 
             recordGetDeviceAttributesForImageScaling();
 
             recordGetImageRatioDeviceProperty(null);
             recordTransformPngImage(createPercentWidthGifImageTransformationParameters(
                     1), SOURCE_IMAGE_PIXEL_WIDTH, SOURCE_IMAGE_PIXEL_HEIGHT,
                     OUTPUT_IMAGE_PIXEL_WIDTH, OUTPUT_IMAGE_PIXEL_HEIGHT,
                     getMappedScaledIphoneGroupGifImageResourcePath());
 
             recordPutResourceCache(resourceCacheKey,
                     getMappedScaledIphoneGroupGifImageResourcePath());
 
             replay();
 
             final List<Resource> actualResources =
                     getObjectUnderTest().resolve(
                             getResourcePathTestData().getRequestedImageResourcePath(),
                             getMockDevice());
 
             // Explicit verify since we are in a loop.
             verify();
 
             final List<Resource> expectedResources =
                     Arrays.asList(getMappedScaledIphoneGroupGifImageResourcePath());
             assertComplexObjectsEqual("actualResources is wrong", expectedResources,
                     actualResources);
 
             assertResourceResolutionTreeUpdated(expectedResources);
 
             // Explicit reset since we are in a loop.
             reset();
         }
 
     }
 
     private void recordGetGifPreferredImageType() {
        EasyMock.expect(getMockDevice().preferredImageType()).andReturn("GIF");
     }
 
     private void recordGetPNGPreferredImageType() {
        EasyMock.expect(getMockDevice().preferredImageType()).andReturn("PNG");
     }
 
     private void recordGetJpegPreferredImageType() {
        EasyMock.expect(getMockDevice().preferredImageType()).andReturn("JPEG");
     }
 
     private void recordGetNullPreferredImageType() {
        EasyMock.expect(getMockDevice().preferredImageType()).andReturn(null);
     }
 
     @Test
     public void testResolveWhenSingleResourceFoundAndScaledUp()
             throws Throwable {
 
         final String[] testValues =
             { getResourcePathTestData().getAbstractImageExtensionWithLeadingDot(),
                 getResourcePathTestData().getAbstractImageExtensionWithoutLeadingDot() };
 
         for (final String testValue : testValues) {
             setObjectUnderTest(createWithAbstractResourceExtension(testValue));
 
             recordGetMatchingGroups();
             final ResourceCacheKey resourceCacheKey = createResourceCacheKey();
             recordCheckResourceCache(resourceCacheKey, Boolean.FALSE);
 
             recordGetMatchingGroupIterator(2);
 
             recordListIphoneFilesByExtension(getSingleMatchedPngImageArray());
 
             recordIphoneImagePropertiesFileExists(Boolean.TRUE);
             recordAppleImagePropertiesFileExists(Boolean.TRUE);
 
             recordLoadIphoneGroupImageProperties(createIphoneGroupPercentWidthImageProperties());
             recordLoadAppleGroupImageProperties(createAppleGroupPercentWidthImageProperties());
 
             recordGetPNGPreferredImageType();
 
             recordGetDeviceAttributesForImageScaling();
 
             recordGetImageRatioDeviceProperty(null);
             recordTransformPngImage(createPercentWidthImageTransformationParameters(
                     1), SOURCE_IMAGE_PIXEL_WIDTH, SOURCE_IMAGE_PIXEL_HEIGHT,
                     SOURCE_IMAGE_PIXEL_WIDTH * 2, SOURCE_IMAGE_PIXEL_HEIGHT * 2,
                     getMappedScaledIphoneGroupPngImageResourcePath());
 
             recordWarnScaledImageUp(SOURCE_IMAGE_PIXEL_WIDTH * 2, SOURCE_IMAGE_PIXEL_HEIGHT * 2);
 
             recordPutResourceCache(resourceCacheKey,
                     getMappedScaledIphoneGroupPngImageResourcePath());
 
             replay();
 
             final List<Resource> actualResources =
                     getObjectUnderTest().resolve(
                             getResourcePathTestData().getRequestedImageResourcePath(),
                             getMockDevice());
 
             // Explicit verify since we are in a loop.
             verify();
 
             final List<Resource> expectedResources =
                     Arrays.asList(getMappedScaledIphoneGroupPngImageResourcePath());
             assertComplexObjectsEqual("actualResources is wrong", expectedResources,
                     actualResources);
 
             assertResourceResolutionTreeUpdated(expectedResources);
 
             // Explicit reset since we are in a loop.
             reset();
         }
 
     }
 
     private void recordWarnScaledImageUp(final int outputPixelWidthGreaterThanSourceWidth,
             final int outputPixelWidthGreaterThanSourceHeight) {
 
         EasyMock.expect(getMockResourceResolutionWarnLogger().isWarnEnabled()).andReturn(
                 Boolean.TRUE);
 
         final TransformedImageAttributes transformedImageAttributes =
                 createTransformedImageAttributes(SOURCE_IMAGE_PIXEL_WIDTH,
                         SOURCE_IMAGE_PIXEL_HEIGHT, outputPixelWidthGreaterThanSourceWidth,
                         outputPixelWidthGreaterThanSourceHeight,
                         getMappedScaledIphoneGroupPngImageResourcePath());
 
         getMockResourceResolutionWarnLogger().warn(
                 "Scaled image up for device " + getMockDevice() + ". This may produce "
                         + "unacceptable image quality. Source image attributes: '"
                         + transformedImageAttributes.getSourceImageAttributes()
                         + "'. Output image attributes: '"
                         + transformedImageAttributes.getOutputImageAttributes() + "'");
     }
 
     @Test
     public void testResolveWhenSingleResourceFoundAndAbsolutePixelWidth()
             throws Throwable {
 
         final String[] testValues =
             { getResourcePathTestData().getAbstractImageExtensionWithLeadingDot(),
                 getResourcePathTestData().getAbstractImageExtensionWithoutLeadingDot() };
 
         for (final String testValue : testValues) {
             setObjectUnderTest(createWithAbstractResourceExtension(testValue));
 
             recordGetMatchingGroups();
             final ResourceCacheKey resourceCacheKey = createResourceCacheKey();
             recordCheckResourceCache(resourceCacheKey, Boolean.FALSE);
 
             recordGetMatchingGroupIterator(2);
 
             recordListIphoneFilesByExtension(getSingleMatchedPngImageArray());
 
             recordIphoneImagePropertiesFileExists(Boolean.TRUE);
             recordAppleImagePropertiesFileExists(Boolean.TRUE);
 
             final int scaledImagePixelWidth = 180;
             recordLoadIphoneGroupImageProperties(createIphoneGroupAbsoluteWidthImageProperties(
                     scaledImagePixelWidth));
             recordLoadAppleGroupImageProperties(createAppleGroupPercentWidthImageProperties());
 
             recordGetPNGPreferredImageType();
 
             recordGetDeviceAttributesForImageScaling();
 
             recordGetImageRatioDeviceProperty(null);
 
             recordTransformPngImage(createAbsoluteWidthImageTransformationParameters(
                     scaledImagePixelWidth, 1),
                     SOURCE_IMAGE_PIXEL_WIDTH, SOURCE_IMAGE_PIXEL_HEIGHT,
                     scaledImagePixelWidth, OUTPUT_IMAGE_PIXEL_HEIGHT,
                     getMappedScaledIphoneGroupPngImageResourcePath());
 
 
             recordPutResourceCache(resourceCacheKey,
                     getMappedScaledIphoneGroupPngImageResourcePath());
 
             replay();
 
             final List<Resource> actualResources =
                     getObjectUnderTest().resolve(
                             getResourcePathTestData().getRequestedImageResourcePath(),
                             getMockDevice());
 
             // Explicit verify since we are in a loop.
             verify();
 
             final List<Resource> expectedResources =
                     Arrays.asList(getMappedScaledIphoneGroupPngImageResourcePath());
             assertComplexObjectsEqual("actualResources is wrong", expectedResources,
                     actualResources);
 
             assertResourceResolutionTreeUpdated(expectedResources);
 
             // Explicit reset since we are in a loop.
             reset();
         }
 
     }
 
     @Test
     public void testResolveWhenSingleResourceFoundAndImageRatioNotNull()
             throws Throwable {
 
         final String[] testValues =
             { getResourcePathTestData().getAbstractImageExtensionWithLeadingDot(),
                 getResourcePathTestData().getAbstractImageExtensionWithoutLeadingDot() };
 
         for (final String testValue : testValues) {
             setObjectUnderTest(createWithAbstractResourceExtension(testValue));
 
             recordGetMatchingGroups();
             final ResourceCacheKey resourceCacheKey = createResourceCacheKey();
             recordCheckResourceCache(resourceCacheKey, Boolean.FALSE);
 
             recordGetMatchingGroupIterator(2);
 
             recordListIphoneFilesByExtension(getSingleMatchedPngImageArray());
 
             recordIphoneImagePropertiesFileExists(Boolean.TRUE);
             recordAppleImagePropertiesFileExists(Boolean.TRUE);
 
             recordLoadIphoneGroupImageProperties(createIphoneGroupPercentWidthImageProperties());
             recordLoadAppleGroupImageProperties(createAppleGroupPercentWidthImageProperties());
 
             recordGetPNGPreferredImageType();
 
             recordGetDeviceAttributesForImageScaling();
 
             recordGetImageRatioDeviceProperty(2);
             recordTransformPngImage(createPercentWidthImageTransformationParameters(
                     2), SOURCE_IMAGE_PIXEL_WIDTH, SOURCE_IMAGE_PIXEL_HEIGHT,
                     OUTPUT_IMAGE_PIXEL_WIDTH, OUTPUT_IMAGE_PIXEL_HEIGHT,
                     getMappedScaledIphoneGroupPngImageResourcePath());
 
 
             recordPutResourceCache(resourceCacheKey,
                     getMappedScaledIphoneGroupPngImageResourcePath());
 
             replay();
 
             final List<Resource> actualResources =
                     getObjectUnderTest().resolve(
                             getResourcePathTestData().getRequestedImageResourcePath(),
                             getMockDevice());
 
             // Explicit verify since we are in a loop.
             verify();
 
             final List<Resource> expectedResources =
                     Arrays.asList(getMappedScaledIphoneGroupPngImageResourcePath());
             assertComplexObjectsEqual("actualResources is wrong", expectedResources,
                     actualResources);
 
             assertResourceResolutionTreeUpdated(expectedResources);
 
             // Explicit reset since we are in a loop.
             reset();
         }
     }
 
     @Test
     public void testResolveWhenMappingPerformedAndImagePropertiesInvalid() throws Throwable {
 
         final String[] testValues =
             { getResourcePathTestData().getAbstractImageExtensionWithLeadingDot(),
                 getResourcePathTestData().getAbstractImageExtensionWithoutLeadingDot() };
 
         for (final String testValue : testValues) {
             doTestResolveWhenMappingPerformedAndImagePropertiesInvalid(testValue);
         }
 
     }
 
     private void doTestResolveWhenMappingPerformedAndImagePropertiesInvalid(
             final String abstractResourceExtension) throws IOException {
 
         final Properties[] testPropertiesArray =
                 new Properties[] { createIphoneGroupImagePropertiesWithPercentWidthNotAnInteger(),
                 createIphoneGroupImagePropertiesWithPercentWidthZero(),
                 createIphoneGroupImagePropertiesWithPercentWidthNegative(),
                 createIphoneGroupImagePropertiesWithAbsoluteWidthNotAnInteger(),
                 createIphoneGroupImagePropertiesWithAbsoluteWidthZero(),
                 createIphoneGroupImagePropertiesWithAbsoluteWidthNegative() };
 
         for (final Properties testProperties : testPropertiesArray) {
 
             setObjectUnderTest(createWithAbstractResourceExtension(abstractResourceExtension));
 
             recordGetMatchingGroups();
             final ResourceCacheKey resourceCacheKey = createResourceCacheKey();
             recordCheckResourceCache(resourceCacheKey, Boolean.FALSE);
 
             recordGetMatchingGroupIterator(2);
 
             recordListIphoneFilesByExtension(getSingleMatchedPngImageArray());
 
             recordIphoneImagePropertiesFileExists(Boolean.TRUE);
             recordAppleImagePropertiesFileExists(Boolean.TRUE);
 
             recordLoadIphoneGroupImageProperties(testProperties);
             recordLoadAppleGroupImageProperties(createAppleGroupPercentWidthImageProperties());
 
             recordLogWarningExceptionEncountered();
 
             recordPutEmptyResultsIntoResourceCache(resourceCacheKey);
 
             recordLogWarningIfEmptyResolvedResources(getResourcePathTestData()
                     .getRequestedImageResourcePath());
 
             replay();
 
             final List<Resource> actualResources =
                     getObjectUnderTest().resolve(
                             getResourcePathTestData().getRequestedImageResourcePath(),
                             getMockDevice());
 
             // Explicit verify since we are in a loop.
             verify();
 
             final List<Resource> expectedResources = new ArrayList<Resource>();
             assertComplexObjectsEqual("actualResources is wrong", expectedResources,
                     actualResources);
 
             assertResourceResolutionTreeUpdated(expectedResources);
 
             // Explicit reset since we are in a loop.
             reset();
         }
     }
 
     private void recordGetImageRatioDeviceProperty(final Integer ratio) {
 
         EasyMock.expect(
                 getMockDevice().getPropertyAsInteger(
                         TransformedImageResourceResolverBean.IMAGE_RATIO_DEVICE_PROPERTY_NAME))
                         .andReturn(ratio);
     }
 
     @Test
     public void testResolveWhenMappingPerformedAndDotNullResourceFound() throws Throwable {
 
         final String[] testValues =
             { getResourcePathTestData().getAbstractImageExtensionWithLeadingDot(),
                 getResourcePathTestData().getAbstractImageExtensionWithoutLeadingDot() };
 
         for (final String testValue : testValues) {
             setObjectUnderTest(createWithAbstractResourceExtension(testValue));
 
             recordGetMatchingGroups();
             final ResourceCacheKey resourceCacheKey = createResourceCacheKey();
             recordCheckResourceCache(resourceCacheKey, Boolean.FALSE);
 
             recordGetMatchingGroupIterator(1);
 
             recordListIphoneFilesByExtension(getSingleMatchedDotNullImageArray());
 
             recordPutResourceCache(resourceCacheKey,
                     getMappedIPhoneDotNullImageResourcePath());
 
             replay();
 
             final List<Resource> actualResources =
                     getObjectUnderTest().resolve(
                             getResourcePathTestData().getRequestedImageResourcePath(),
                             getMockDevice());
 
             // Explicit verify since we are in a loop.
             verify();
 
             final List<Resource> expectedResources =
                     Arrays.asList(getMappedIPhoneDotNullImageResourcePath());
             assertComplexObjectsEqual("actualResources is wrong", expectedResources,
                     actualResources);
 
             assertResourceResolutionTreeUpdated(expectedResources);
 
             // Explicit reset since we are in a loop.
             reset();
         }
 
     }
 
     @Test
     public void testResolveWhenMappingPerformedAndImageTransformationPropertiesEmpty()
             throws Throwable {
 
         final String[] testValues =
             { getResourcePathTestData().getAbstractImageExtensionWithLeadingDot(),
                 getResourcePathTestData().getAbstractImageExtensionWithoutLeadingDot() };
 
         for (final String testValue : testValues) {
             setObjectUnderTest(createWithAbstractResourceExtension(testValue));
 
             recordGetMatchingGroups();
             final ResourceCacheKey resourceCacheKey = createResourceCacheKey();
             recordCheckResourceCache(resourceCacheKey, Boolean.FALSE);
 
             recordGetMatchingGroupIterator(2);
 
             recordListIphoneFilesByExtension(getSingleMatchedPngImageArray());
 
             recordIphoneImagePropertiesFileExists(Boolean.TRUE);
             recordAppleImagePropertiesFileExists(Boolean.TRUE);
 
             recordLoadIphoneGroupImageProperties(new Properties());
             recordLoadAppleGroupImageProperties(new Properties());
 
             recordGetPNGPreferredImageType();
 
             recordGetDeviceAttributesForImageScaling();
 
             recordTransformPngImage(createImageTransformationParametersPreserveDimensions(),
                     SOURCE_IMAGE_PIXEL_WIDTH, SOURCE_IMAGE_PIXEL_HEIGHT,
                     SOURCE_IMAGE_PIXEL_WIDTH, SOURCE_IMAGE_PIXEL_HEIGHT,
                     getMappedScaledIphoneGroupPngImageResourcePath());
 
             recordPutResourceCache(resourceCacheKey,
                     getMappedScaledIphoneGroupPngImageResourcePath());
 
             replay();
 
             final List<Resource> actualResources =
                     getObjectUnderTest().resolve(
                             getResourcePathTestData().getRequestedImageResourcePath(),
                             getMockDevice());
 
             // Explicit verify since we are in a loop.
             verify();
 
             final List<Resource> expectedResources =
                     Arrays.asList(getMappedScaledIphoneGroupPngImageResourcePath());
             assertComplexObjectsEqual("actualResources is wrong", expectedResources,
                     actualResources);
 
             assertResourceResolutionTreeUpdated(expectedResources);
 
             // Explicit reset since we are in a loop.
             reset();
         }
 
     }
     @Test
     public void testResolveWhenNoImageTransformationPropertiesFilesAndGifSourceAndPreferPngOutput()
             throws Throwable {
 
         final String[] testValues =
             { getResourcePathTestData().getAbstractImageExtensionWithLeadingDot(),
                 getResourcePathTestData().getAbstractImageExtensionWithoutLeadingDot() };
 
         for (final String testValue : testValues) {
             setObjectUnderTest(createWithAbstractResourceExtension(testValue));
 
             recordGetMatchingGroups();
             final ResourceCacheKey resourceCacheKey = createResourceCacheKey();
             recordCheckResourceCache(resourceCacheKey, Boolean.FALSE);
 
             recordGetMatchingGroupIterator(2);
 
             recordListIphoneFilesByExtension(getSingleMatchedGifImageArray());
 
             recordIphoneImagePropertiesFileExists(Boolean.FALSE);
             recordAppleImagePropertiesFileExists(Boolean.FALSE);
 
             EasyMock.expect(getMockImageReader().readImageAttributes(
                     getMappedIPhoneGifImageResourcePath().getNewFile()))
                     .andReturn(createIphoneGifImageAttributes());
 
             recordPutResourceCache(resourceCacheKey,
                     getMappedIPhoneGifImageResourcePath());
 
             replay();
 
             final List<Resource> actualResources =
                     getObjectUnderTest().resolve(
                             getResourcePathTestData().getRequestedImageResourcePath(),
                             getMockDevice());
 
             // Explicit verify since we are in a loop.
             verify();
 
             final List<Resource> expectedResources =
                     Arrays.asList(getMappedIPhoneGifImageResourcePath());
             assertComplexObjectsEqual("actualResources is wrong", expectedResources,
                     actualResources);
 
             assertResourceResolutionTreeUpdated(expectedResources);
 
             // Explicit reset since we are in a loop.
             reset();
         }
     }
 
     private ImageAttributes createIphoneGifImageAttributes() {
         final ImageAttributesBean imageAttributesBean = new ImageAttributesBean();
 
         imageAttributesBean.setImageFile(getMappedIPhoneGifImageResourcePath().getNewFile());
         imageAttributesBean.setPixelWidth(SOURCE_IMAGE_PIXEL_WIDTH);
         imageAttributesBean.setPixelHeight(SOURCE_IMAGE_PIXEL_HEIGHT);
 
         return imageAttributesBean;
     }
 
     private void recordAppleImagePropertiesFileExists(final Boolean exists) {
         EasyMock.expect(getMockFileIoFacade().fileExists(
                 getMappedAppleGroupImagePropertiesResourcePath().getNewFile()))
                 .andReturn(exists);
     }
 
     private void recordIphoneImagePropertiesFileExists(final Boolean exists) {
         EasyMock.expect(getMockFileIoFacade().fileExists(
                 getMappedIPhoneImagePropertiesResourcePath().getNewFile()))
                 .andReturn(exists);
     }
 
     private void recordTransformPngImage(
             final ImageTransformationParameters imageTransformationParameters,
             final int sourcePixelWidth, final int sourcePixelHeight,
             final int outputPixelWidth, final int outputPixelHeight,
             final Resource mappedResource) {
 
         EasyMock.expect(
                 getMockImageTransformationFactory().transformImage(
                         getMappedIphoneGroupPngImageResourcePath().getNewFile(),
                         getMappedIphoneGroupPngImageResourcePath().getNewFile().getParentFile(),
                         imageTransformationParameters)).andReturn(
                                 createTransformedImageAttributes(sourcePixelWidth, sourcePixelHeight,
                                         outputPixelWidth, outputPixelHeight, mappedResource));
     }
 
     private void recordTransformGifImage(
             final ImageTransformationParameters imageTransformationParameters,
             final int sourcePixelWidth, final int sourcePixelHeight, final int outputPixelWidth,
             final int outputPixelHeight, final Resource mappedResource) {
 
         EasyMock.expect(
                 getMockImageTransformationFactory().transformImage(
                         getMappedIPhoneGroupGifImageResourcePath().getNewFile(),
                         getMappedIPhoneGroupGifImageResourcePath().getNewFile().getParentFile(),
                         imageTransformationParameters)).andReturn(
                                 createTransformedImageAttributes(sourcePixelWidth, sourcePixelHeight,
                                         outputPixelWidth, outputPixelHeight, mappedResource));
     }
 
     private void recordGetDeviceAttributesForImageScaling() {
         EasyMock.expect(getMockDevice().getPixelsX()).andReturn(new Integer(450));
     }
 
     private void recordLoadIphoneGroupImageProperties(final Properties properties)
             throws IOException {
 
         EasyMock.expect(
                 getMockPropertiesLoader().loadPropertiesNotNull(
                         getMappedIPhoneImagePropertiesResourcePath().getNewFile())).andReturn(
                                 properties);
     }
 
     private void recordLoadAppleGroupImageProperties(final Properties properties)
             throws IOException {
 
         EasyMock.expect(
                 getMockPropertiesLoader().loadPropertiesNotNull(
                         getMappedAppleGroupImagePropertiesResourcePath().getNewFile())).andReturn(
                                 properties);
     }
 
     private Resource getMappedScaledIphoneGroupPngImageResourcePath() {
         return getResourcePathTestData().getMappedScaledIphoneGroupPngImageResourcePath();
     }
 
     private Resource getMappedScaledIphoneGroupGifImageResourcePath() {
         return getResourcePathTestData().getMappedScaledIphoneGroupGifImageResourcePath();
     }
 
     private TransformedImageAttributes createTransformedImageAttributes(final int sourcePixelWidth,
             final int sourcePixelHeight, final int outputPixelWidth,
             final int outputPixelHeight, final Resource mappedResource) {
 
         final TransformedImageAttributesBean transformedImageAttributesBean =
                 new TransformedImageAttributesBean();
 
         final ImageAttributesBean sourceImageAttributesBean = new ImageAttributesBean();
         sourceImageAttributesBean.setImageFile(getMappedIphoneGroupPngImageResourcePath()
                 .getNewFile());
         sourceImageAttributesBean.setPixelWidth(sourcePixelWidth);
         sourceImageAttributesBean.setPixelHeight(sourcePixelHeight);
 
         transformedImageAttributesBean.setSourceImageAttributes(sourceImageAttributesBean);
 
         final ImageAttributesBean outputImageAttributesBean = new ImageAttributesBean();
         outputImageAttributesBean.setImageFile(mappedResource
                 .getNewFile());
         outputImageAttributesBean.setPixelWidth(outputPixelWidth);
         outputImageAttributesBean.setPixelHeight(outputPixelHeight);
 
         transformedImageAttributesBean.setOutputImageAttributes(outputImageAttributesBean);
 
         return transformedImageAttributesBean;
     }
 
     private ImageTransformationParameters createPercentWidthImageTransformationParameters(
             final int imageRatio) {
 
         final ImageTransformationParametersBean parametersBean =
                 new ImageTransformationParametersBean();
         parametersBean.setDeviceImagePercentWidth(20 * imageRatio);
         parametersBean.setDevicePixelWidth(450);
 
         parametersBean.setOutputImageFormat(ImageTransformationFactory.ImageFormat.PNG);
         return parametersBean;
 
     }
 
     private ImageTransformationParameters createPercentWidthGifImageTransformationParameters(
             final int imageRatio) {
 
         final ImageTransformationParametersBean parametersBean =
                 new ImageTransformationParametersBean();
         parametersBean.setDeviceImagePercentWidth(20 * imageRatio);
         parametersBean.setDevicePixelWidth(450);
         parametersBean.setBackgroundColor(BACKGROUND_COLOR);
 
         parametersBean.setOutputImageFormat(ImageTransformationFactory.ImageFormat.GIF);
         return parametersBean;
 
     }
 
     private ImageTransformationParameters createAbsoluteWidthImageTransformationParameters(
             final int pixelWidth, final int imageRatio) {
 
         final ImageTransformationParametersBean parametersBean =
                 new ImageTransformationParametersBean();
         parametersBean.setAbsolutePixelWidth(pixelWidth * imageRatio);
         parametersBean.setDevicePixelWidth(450);
 
         parametersBean.setOutputImageFormat(ImageTransformationFactory.ImageFormat.PNG);
         return parametersBean;
 
     }
 
     private ImageTransformationParameters createImageTransformationParametersPreserveDimensions() {
         final ImageTransformationParametersBean parametersBean =
                 new ImageTransformationParametersBean();
 
         parametersBean.setDevicePixelWidth(450);
         parametersBean.setOutputImageFormat(ImageTransformationFactory.ImageFormat.PNG);
 
         return parametersBean;
 
     }
 
     private Properties createIphoneGroupPercentWidthImageProperties() {
         final Properties properties = new Properties();
         properties.setProperty("width", "20%");
         return properties;
     }
 
     private Properties createIphoneGroupGifPercentWidthImageProperties() {
         final Properties properties = new Properties();
         properties.setProperty("width", "20%");
         properties.setProperty("background.color", BACKGROUND_COLOR);
         return properties;
     }
 
     private Properties createIphoneGroupAbsoluteWidthImageProperties(final int pixelWidth) {
         final Properties properties = new Properties();
         properties.setProperty("width", pixelWidth + "px");
         return properties;
     }
 
     private Properties createIphoneGroupImagePropertiesWithPercentWidthNotAnInteger() {
         final Properties properties = new Properties();
         properties.setProperty("width", "I'm not a number%");
         return properties;
     }
 
     private Properties createIphoneGroupImagePropertiesWithPercentWidthZero() {
         final Properties properties = new Properties();
         properties.setProperty("width", "0%");
         return properties;
     }
 
     private Properties createIphoneGroupImagePropertiesWithPercentWidthNegative() {
         final Properties properties = new Properties();
         properties.setProperty("width", "-1%");
         return properties;
     }
 
     private Properties createIphoneGroupImagePropertiesWithAbsoluteWidthNotAnInteger() {
         final Properties properties = new Properties();
         properties.setProperty("width", "I'm not a number px");
         return properties;
     }
 
     private Properties createIphoneGroupImagePropertiesWithAbsoluteWidthZero() {
         final Properties properties = new Properties();
         properties.setProperty("width", "0px");
         return properties;
     }
 
     private Properties createIphoneGroupImagePropertiesWithAbsoluteWidthNegative() {
         final Properties properties = new Properties();
         properties.setProperty("width", "-1px");
         return properties;
     }
 
     private Properties createAppleGroupPercentWidthImageProperties() {
         final Properties properties = new Properties();
         properties.setProperty("width", "40%");
         return properties;
     }
 
     private void recordGetMatchingGroups() {
 
         EasyMock.expect(
                 getMockConfigurationFactory().getUiConfiguration(
                         getResourcePathTestData().getRequestedImageResourcePath()))
                         .andReturn(getMockUiConfiguration()).atLeastOnce();
 
         final Group[] matchingGroups =
                 new Group[] { getGroupTestData().createIPhoneGroup(),
                 getGroupTestData().createAppleGroup() };
 
         EasyMock.expect(getMockUiConfiguration().matchingGroups(getMockDevice())).andReturn(
                 matchingGroups);
 
     }
 
     private ResourceCacheKey createResourceCacheKey() {
         final ResourceCacheKey resourceCacheKey =
                 new ResourceCacheKeyBean(getResourcePathTestData().getRequestedImageResourcePath(),
                         new Group[] { getGroupTestData().createIPhoneGroup(),
                     getGroupTestData().createAppleGroup() });
         return resourceCacheKey;
     }
 
     private File[] getSingleMatchedPngImageArray() {
         return new File[] {
                 getMappedIPhonePngImageResourcePath().getNewFile() };
     }
 
     private File[] getSingleMatchedGifImageArray() {
         return new File[] {
                 getMappedIPhoneGifImageResourcePath().getNewFile() };
     }
 
     private File[] getSingleMatchedDotNullImageArray() {
         return new File[] {
                 getMappedIPhoneDotNullImageResourcePath().getNewFile() };
     }
 
     private File[] getMultipleMatchedImageArray() {
         return new File[] {
                 getMappedIPhonePngImageResourcePath().getNewFile(),
                 getMappedIPhoneGroupGifImageResourcePath().getNewFile() };
     }
 
     private Resource getMappedIPhonePngImageResourcePath() {
         return getResourcePathTestData().getMappedIphoneGroupPngImageResourcePath();
     }
 
     private Resource getMappedIPhoneImagePropertiesResourcePath() {
         return getResourcePathTestData().getMappedIphoneGroupImagePropertiesResourcePath();
     }
 
     private Resource getMappedIPhoneGifImageResourcePath() {
         return getResourcePathTestData().getMappedIphoneGroupGifImageResourcePath();
     }
 
     private Resource getMappedIPhoneDotNullImageResourcePath() {
         return getResourcePathTestData().getMappedIphoneGroupDotNullImageResourcePath();
     }
 
     private Resource getMappedIPhoneGroupGifImageResourcePath() {
         return getResourcePathTestData().getMappedIphoneGroupGifImageResourcePath();
     }
 
     @Test
     public void testResolveWhenMultipleResourcesFound() throws Throwable {
 
         final String[] testValues = {
                 getResourcePathTestData().getAbstractImageExtensionWithLeadingDot(),
                 getResourcePathTestData().getAbstractImageExtensionWithoutLeadingDot() };
 
         for (final String testValue : testValues) {
             setObjectUnderTest(createWithAbstractResourceExtension(testValue));
 
             recordGetMatchingGroups();
             final ResourceCacheKey resourceCacheKey = createResourceCacheKey();
             recordCheckResourceCache(resourceCacheKey, Boolean.FALSE);
 
             recordGetMatchingGroupIterator(2);
 
             recordListIphoneFilesByExtension(getMultipleMatchedImageArray());
 
             recordLogWarningResolveToSingleFoundMultipleResources();
 
             recordIphoneImagePropertiesFileExists(Boolean.TRUE);
             recordAppleImagePropertiesFileExists(Boolean.TRUE);
 
             recordLoadIphoneGroupImageProperties(createIphoneGroupPercentWidthImageProperties());
             recordLoadAppleGroupImageProperties(createAppleGroupPercentWidthImageProperties());
 
             recordGetPNGPreferredImageType();
 
             recordGetDeviceAttributesForImageScaling();
 
             recordGetImageRatioDeviceProperty(null);
 
             recordTransformPngImage(createPercentWidthImageTransformationParameters(
                     1), SOURCE_IMAGE_PIXEL_WIDTH, SOURCE_IMAGE_PIXEL_HEIGHT,
                     OUTPUT_IMAGE_PIXEL_WIDTH, OUTPUT_IMAGE_PIXEL_HEIGHT,
                     getMappedScaledIphoneGroupPngImageResourcePath());
 
             recordPutResourceCache(resourceCacheKey,
                     getMappedScaledIphoneGroupPngImageResourcePath());
 
             replay();
 
             final List<Resource> actualResources =
                     getObjectUnderTest().resolve(
                             getResourcePathTestData().getRequestedImageResourcePath(),
                             getMockDevice());
 
             // Explicit verify since we are in a loop.
             verify();
 
             final List<Resource> expectedResources
             = Arrays.asList(getMappedScaledIphoneGroupPngImageResourcePath());
             assertComplexObjectsEqual("actualResources is wrong",
                     expectedResources,
                     actualResources);
 
             assertResourceResolutionTreeUpdated(expectedResources);
 
             // Explicit reset since we are in a loop.
             reset();
         }
 
     }
 
     private void recordGetMatchingGroupIterator(final int times) {
 
         final List<Group> matchingGroups =
                 Arrays.asList(getGroupTestData().createIPhoneGroup(), getGroupTestData()
                         .createAppleGroup());
 
         for (int i = 0; i < times; i++) {
             // Explicitly record twice so that a new iterator instance is
             // returned each time.
             EasyMock.expect(getMockUiConfiguration().matchingGroupIterator(getMockDevice()))
             .andReturn(matchingGroups.iterator());
         }
 
     }
 
     private void recordLogWarningResolveToSingleFoundMultipleResources() {
 
         EasyMock.expect(getMockResourceResolutionWarnLogger().isWarnEnabled()).andReturn(
                 Boolean.TRUE);
 
         getMockResourceResolutionWarnLogger().warn(
                 "Requested resource '" + getResourcePathTestData().getRequestedImageResourcePath()
                 + "' resolved to multiple real resources with extensions matching "
                 + ArrayUtils.toString(MATCHED_IMAGE_FILE_EXTENSION_WILDCARDS)
                 + ". Will only use the first resource. Total found: ["
                 + getMappedIPhonePngImageResourcePath().getNewFile() + ", "
                 + getMappedIPhoneGroupGifImageResourcePath().getNewFile() + "].");
     }
 
     private void recordLogWarningExceptionEncountered() {
 
         EasyMock.expect(getMockResourceResolutionWarnLogger().isWarnEnabled()).andReturn(
                 Boolean.TRUE);
 
         getMockResourceResolutionWarnLogger().warn(
                 EasyMock.eq("Error resolving requested resource: '"
                         + getResourcePathTestData().getRequestedImageResourcePath()
                         + "' for device " + getMockDevice()),
                         EasyMock.isA(ResourceResolutionRuntimeException.class));
     }
 
     @Test
     public void testResolveWhenMappingPerformedAndResourceDoesNotExist()
             throws Throwable {
 
         final String[] testValues = {
                 getResourcePathTestData().getAbstractImageExtensionWithLeadingDot(),
                 getResourcePathTestData().getAbstractImageExtensionWithoutLeadingDot() };
 
         for (final String testValue : testValues) {
             setObjectUnderTest(createWithAbstractResourceExtension(testValue));
 
             recordGetMatchingGroups();
             final ResourceCacheKey resourceCacheKey = createResourceCacheKey();
             recordCheckResourceCache(resourceCacheKey, Boolean.FALSE);
 
             recordGetMatchingGroupIterator(1);
 
             recordListIphoneFilesByExtension(new File[] {});
             recordListAppleFilesByExtension(new File[] {});
 
             recordPutEmptyResultsIntoResourceCache(resourceCacheKey);
             recordLogWarningIfEmptyResolvedResources(
                     getResourcePathTestData().getRequestedImageResourcePath());
 
             replay();
 
             final List<Resource> actualResources =
                     getObjectUnderTest().resolve(
                             getResourcePathTestData().getRequestedImageResourcePath(),
                             getMockDevice());
 
             // Explicit verify since we are in a loop.
             verify();
 
             Assert.assertNotNull("actualResources should not be null",
                     actualResources);
             Assert.assertTrue("actualResources should be empty",
                     actualResources.isEmpty());
 
 
             // Explicit reset since we are in a loop.
             reset();
         }
 
     }
 
     @Test
     public void testResolveWhenMappingPerformedAndResourcesFromCache() throws Throwable {
 
         final String[] testValues = {
                 getResourcePathTestData().getAbstractImageExtensionWithLeadingDot(),
                 getResourcePathTestData().getAbstractImageExtensionWithoutLeadingDot() };
 
         for (final String testValue : testValues) {
             setObjectUnderTest(createWithAbstractResourceExtension(testValue));
 
             recordGetMatchingGroups();
             final ResourceCacheKey resourceCacheKey = createResourceCacheKey();
             recordCheckResourceCache(resourceCacheKey, Boolean.TRUE);
 
             recordGetFromResourceCache(resourceCacheKey);
 
             replay();
 
             final List<Resource> actualResources =
                     getObjectUnderTest().resolve(
                             getResourcePathTestData().getRequestedImageResourcePath(),
                             getMockDevice());
 
             // Explicit verify since we are in a loop.
             verify();
 
             final List<Resource> expectedResources
             = Arrays.asList(getMappedIphoneGroupPngImageResourcePath());
             assertComplexObjectsEqual("actualResources is wrong",
                     expectedResources,
                     actualResources);
 
             assertResourceResolutionTreeUpdated(expectedResources);
 
             // Explicit reset since we are in a loop.
             reset();
         }
 
     }
 
     private void recordGetFromResourceCache(final ResourceCacheKey resourceCacheKey) {
         final ResourceCacheEntryBean resourceCacheEntryBean =
                 new ResourceCacheEntryBean(
                         new Resource[] { getMappedIphoneGroupPngImageResourcePath() },
                         ResourceCache.DEFAULT_RESOUCRES_NOT_FOUND_MAX_REFRESH_COUNT,
                         ResourceCache.DEFAULT_RESOURCES_NOT_FOUND_REFRESH_COUNT_UPDATE_MILLISECONDS);
         EasyMock.expect(getMockResourceCache().get(resourceCacheKey)).andReturn(
                 resourceCacheEntryBean);
     }
 
     private void recordListIphoneFilesByExtension(final File[] files) {
 
         EasyMock.expect(
                 getMockFileIoFacade().list(EasyMock.eq(getRootResourcesDir()),
                         EasyMock.eq(getMappedIphoneGroupImageResourcePath().getNewPath()),
                         EasyMock.aryEq(MATCHED_IMAGE_FILE_EXTENSION_WILDCARDS),
                         EasyMock.aryEq(EXCLUDED_IMAGE_FILE_EXTENSION_WILDCARDS))).andReturn(files);
 
     }
 
     private void recordListAppleFilesByExtension(final File[] files) {
 
         EasyMock.expect(
                 getMockFileIoFacade().list(EasyMock.eq(getRootResourcesDir()),
                         EasyMock.eq(getMappedAppleGroupImageResourcePath().getNewPath()),
                         EasyMock.aryEq(MATCHED_IMAGE_FILE_EXTENSION_WILDCARDS),
                         EasyMock.aryEq(EXCLUDED_IMAGE_FILE_EXTENSION_WILDCARDS))).andReturn(files);
 
     }
 
     private Resource getMappedIphoneGroupImageResourcePath() {
 
         return getResourcePathTestData().getMappedIphoneGroupImageResourcePath();
     }
 
     private Resource getMappedAppleGroupImageResourcePath() {
 
         return getResourcePathTestData().getMappedAppleGroupImageResourcePath();
     }
 
     private Resource getMappedAppleGroupImagePropertiesResourcePath() {
 
         return getResourcePathTestData().getMappedAppleGroupImagePropertiesResourcePath();
     }
 
     private Resource getMappedIphoneGroupPngImageResourcePath() {
 
         return getResourcePathTestData().getMappedIphoneGroupPngImageResourcePath();
     }
 
     private File getRootResourcesDir() {
         return getResourcePathTestData().getRootResourcesPath();
     }
 
     @Test
     public void testResolveWhenNoMappingPerformed() throws Throwable {
 
         final List<Resource> actualResources =
                 getObjectUnderTest().resolve(
                         getResourcePathTestData().getRequestedCssResourcePath(),
                         getMockDevice());
 
         Assert.assertNotNull("actualResources should not be null",
                 actualResources);
         Assert.assertTrue("actualResources should be empty",
                 actualResources.isEmpty());
 
     }
 
     @Test
     public void testSupportsWhenTrue() throws Throwable {
 
         Assert.assertTrue("supports should be true",
                 getObjectUnderTest().supports(
                         getResourcePathTestData().getRequestedImageResourcePath()));
     }
 
     @Test
     public void testSupportsWhenFalse() throws Throwable {
 
         Assert.assertFalse("supports should be false",
                 getObjectUnderTest().supports(
                         getResourcePathTestData().getRequestedCssResourcePath()));
     }
 
     /**
      * @return the objectUnderTest
      */
     private TransformedImageResourceResolverBean getObjectUnderTest() {
         return objectUnderTest;
     }
 
     /**
      * @param objectUnderTest the objectUnderTest to set
      */
     private void setObjectUnderTest(final TransformedImageResourceResolverBean objectUnderTest) {
         this.objectUnderTest = objectUnderTest;
     }
 
     /**
      * @return the mockDevice
      */
     @Override
     public Device getMockDevice() {
 
         return mockDevice;
     }
 
     /**
      * @param mockDevice  the mockDevice to set
      */
     @Override
     public void setMockDevice(final Device mockDevice) {
 
         this.mockDevice = mockDevice;
     }
 
     /**
      * @return the mockPropertiesLoader
      */
     public au.com.sensis.mobile.crf.util.PropertiesLoader getMockPropertiesLoader() {
         return mockPropertiesLoader;
     }
 
     /**
      * @param mockPropertiesLoader the mockPropertiesLoader to set
      */
     public void setMockPropertiesLoader(
             final au.com.sensis.mobile.crf.util.PropertiesLoader mockPropertiesLoader) {
         this.mockPropertiesLoader = mockPropertiesLoader;
     }
 
     /**
      * @return the mockScaledImageFactory
      */
     public ImageTransformationFactory getMockImageTransformationFactory() {
         return mockImageTransformationFactory;
     }
 
     /**
      * @param mockImageTransformationFactory
      *            the mockScaledImageFactory to set
      */
     public void setMockImageTransformationFactory(
             final ImageTransformationFactory mockImageTransformationFactory) {
         this.mockImageTransformationFactory = mockImageTransformationFactory;
     }
 
     /**
      * @return the mockImageReader
      */
     public ImageReader getMockImageReader() {
         return mockImageReader;
     }
 
     /**
      * @param mockImageReader the mockImageReader to set
      */
     public void setMockImageReader(final ImageReader mockImageReader) {
         this.mockImageReader = mockImageReader;
     }
 }
