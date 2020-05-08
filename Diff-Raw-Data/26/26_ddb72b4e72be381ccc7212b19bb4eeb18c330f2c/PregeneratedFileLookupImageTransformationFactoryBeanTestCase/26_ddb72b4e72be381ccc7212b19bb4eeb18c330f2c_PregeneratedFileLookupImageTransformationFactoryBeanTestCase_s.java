 package au.com.sensis.mobile.crf.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.log4j.Level;
 import org.easymock.EasyMock;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.core.io.ClassPathResource;
 
 import au.com.sensis.mobile.crf.exception.ContentRenderingFrameworkRuntimeException;
 import au.com.sensis.mobile.crf.util.ImageTransformationFactory.ImageFormat;
 import au.com.sensis.mobile.crf.util.ImageTransformationFactory.ImageTransformationParameters;
 import au.com.sensis.wireless.test.AbstractJUnit4TestCase;
 
 /**
  * Unit test {@link PregeneratedFileLookupImageTransformationFactoryBean}.
  *
  * @author Adrian.Koh2@sensis.com.au
  */
 public class PregeneratedFileLookupImageTransformationFactoryBeanTestCase extends AbstractJUnit4TestCase {
 
     private static final int REQUESTED_DEVICE_PERCENT_SCALED_WIDTH = 20;
     private static final int DEVICE_PIXEL_WIDTH = 480;
     private static final int VERY_LARGE_DEVICE_PIXEL_WIDTH = 4200;
     private static final int DEVICE_PERCENT_SCALING_OUTPUT_IMAGE_PIXEL_WIDTH = 90;
     private static final int DEVICE_PERCENT_SCALING_OUTPUT_IMAGE_PIXEL_HEIGHT = 67;
 
     private static final int REQUESTED_ABSOLUTE_SCALED_WIDTH = 195;
     private static final int ABSOLUTE_WIDTH_SCALING_OUTPUT_PIXEL_WIDTH = 190;
     private static final int ABSOLUTE_WIDTH_SCALING_OUTPUT_PIXEL_HEIGHT = 142;
 
     private static final int SOURCE_IMAGE_PIXEL_WIDTH = 800;
     private static final int SOURCE_IMAGE_PIXEL_HEIGHT = 600;
 
     private static final String IMAGE_GENERATION_LAST_RUN_PROPERTIES_CLASSPATH
        = "/au/com/sensis/mobile/crf/util/gimages-last-run.properties";
     private static final String INVALID_IMAGE_GENERATION_LAST_RUN_PROPERTIES_CLASSPATH
        = "/au/com/sensis/mobile/crf/util/invalid-gimages-last-run.properties";
     private static final String MISSING_IMAGE_GENERATION_LAST_RUN_PROPERTIES_CLASSPATH
        = "/au/com/sensis/mobile/crf/util/missing-gimages-last-run.properties";
 
     private PregeneratedFileLookupImageTransformationFactoryBean objectUnderTest;
 
     private File sourceImage;
     private File outputImageBaseDir;
 
     private String devicePercentWidthScalingOutputImageSubDir;
     private File devicePercentWidthScalingOutputImage;
 
     private String absolutePixelWidthScalingOutputImageSubDir;
     private File absolutePixelWidthScalingOutputImage;
 
     private String noScalingOutputImageSubDir;
     private File noScalingOutputImage;
 
     private FileIoFacade mockFileIoFacade;
     private ImageReader mockImageReader;
     private ProcessStarter mockProcessStarter;
     private Process mockProcess;
 
     /**
      * Setup test data.
      *
      * @throws Exception
      *             Thrown if any error occurs.
      */
     @Before
     public void setUp() throws Exception {
         final ClassPathResource classPathResource = new ClassPathResource(
                 IMAGE_GENERATION_LAST_RUN_PROPERTIES_CLASSPATH);
 
         setObjectUnderTest(new PregeneratedFileLookupImageTransformationFactoryBean(
                 getMockImageReader(), classPathResource.getFile()));
 
         FileIoFacadeFactory.changeDefaultFileIoFacadeSingleton(getMockFileIoFacade());
 
         final ClassPathResource myPackageDirClassPathResource =
                 new ClassPathResource("/"
                         + getClass().getPackage().getName().replaceAll("\\.", "/"));
         final File myPackageDir = new File(myPackageDirClassPathResource.getURI());
 
         setupSourceImage(myPackageDir);
 
         setOutputImageBaseDir(myPackageDir);
 
         setDevicePercentWidthScalingOutputImageSubDir("w"
                 + DEVICE_PERCENT_SCALING_OUTPUT_IMAGE_PIXEL_WIDTH);
 
         setDevicePercentWidthScalingOutputImage(new File(getOutputImageBaseDir(),
                 getDevicePercentWidthScalingOutputImageSubDir()
                 + "/h" + DEVICE_PERCENT_SCALING_OUTPUT_IMAGE_PIXEL_HEIGHT
                 + "/myInputImage.png"));
 
         setAbsolutePixelWidthScalingOutputImageSubDir("w"
                 + ABSOLUTE_WIDTH_SCALING_OUTPUT_PIXEL_WIDTH);
 
         setAbsolutePixelWidthScalingOutputImage(new File(getOutputImageBaseDir(),
                 getAbsolutePixelWidthScalingOutputImageSubDir()
                 + "/h" + ABSOLUTE_WIDTH_SCALING_OUTPUT_PIXEL_HEIGHT
                 + "/myInputImage.png"));
 
         setNoScalingOutputImageSubDir("w" + SOURCE_IMAGE_PIXEL_WIDTH);
 
         setNoScalingOutputImage(new File(getOutputImageBaseDir(),
                 getNoScalingOutputImageSubDir()
                 + "/h" + SOURCE_IMAGE_PIXEL_HEIGHT
                 + "/myInputImage.gif"));
 
     }
 
     private void setupSourceImage(final File myPackageDir) throws IOException {
         setSourceImage(new File(myPackageDir, "myInputImage.png"));
         final boolean sourceImageCreated = getSourceImage().createNewFile();
         if (!sourceImageCreated) {
             throw new RuntimeException("Could not create file to setup test: " + getSourceImage());
         }
     }
 
     /**
      * Tear down test data.
      *
      * @throws Exception Thrown if any error occurs.
      */
     @After
     public void tearDown() throws Exception {
         FileIoFacadeFactory.restoreDefaultFileIoFacadeSingleton();
 
         getSourceImage().delete();
     }
 
     @Test
     public void testTransformImageWhenDevicePercentWidthScalingRequested() throws Throwable {
 
         recordReadSourceImageAttributes();
 
         final File outputImageDir =
                 new File(getOutputImageBaseDir(), getDevicePercentWidthScalingOutputImageSubDir());
 
         recordOutputImageDirIsDirectory(outputImageDir);
 
         final File[] foundFiles = { getDevicePercentWidthScalingOutputImage() };
         EasyMock.expect(
                 getMockFileIoFacade().list(outputImageDir, getSourceImage().getName(), "h*"))
                 .andReturn(foundFiles);
 
         replay();
 
         final TransformedImageAttributes actualImage =
                 getObjectUnderTest().transformImage(getSourceImage(), getOutputImageBaseDir(),
                         createPercentWidthImageTransformationParameters());
 
         Assert.assertEquals("actualImage is wrong", createExpectedTransformedImageAttributes(
                 getDevicePercentWidthScalingOutputImage(),
                 DEVICE_PERCENT_SCALING_OUTPUT_IMAGE_PIXEL_WIDTH,
                 DEVICE_PERCENT_SCALING_OUTPUT_IMAGE_PIXEL_HEIGHT), actualImage);
     }
 
     @Test
     public void testTransformImageWhenHeightDirectoryValueInvalid() throws Throwable {
 
         setDevicePercentWidthScalingOutputImage(new File(getOutputImageBaseDir(),
                 getDevicePercentWidthScalingOutputImageSubDir() + "/h100trailing text"
                         + "/myInputImage.png"));
 
         recordReadSourceImageAttributes();
 
         final File outputImageDir =
                 new File(getOutputImageBaseDir(), getDevicePercentWidthScalingOutputImageSubDir());
 
         recordOutputImageDirIsDirectory(outputImageDir);
 
         final File[] foundFiles = { getDevicePercentWidthScalingOutputImage() };
         EasyMock.expect(
                 getMockFileIoFacade().list(outputImageDir, getSourceImage().getName(), "h*"))
                 .andReturn(foundFiles);
 
         replay();
 
         try {
             getObjectUnderTest().transformImage(getSourceImage(), getOutputImageBaseDir(),
                     createPercentWidthImageTransformationParameters());
 
             Assert.fail("ImageCreationException expected");
         } catch (final ImageCreationException e) {
 
             Assert.assertEquals("ImageCreationException has wrong message", "The found image '"
                     + getDevicePercentWidthScalingOutputImage()
                     + "' has an invalid height specified in its path.", e.getMessage());
         }
 
     }
 
     @Test
     public void testTransformImageWhenAbsolutePixelWidthScalingRequested() throws Throwable {
 
         recordReadSourceImageAttributes();
 
         final File outputImageDir =
                 new File(getOutputImageBaseDir(), getAbsolutePixelWidthScalingOutputImageSubDir());
         recordOutputImageDirIsDirectory(outputImageDir);
 
         final File[] foundFiles = { getAbsolutePixelWidthScalingOutputImage() };
         EasyMock.expect(
                 getMockFileIoFacade().list(outputImageDir, getSourceImage().getName(), "h*"))
                 .andReturn(foundFiles);
 
         replay();
 
         final TransformedImageAttributes actualImage =
                 getObjectUnderTest().transformImage(getSourceImage(), getOutputImageBaseDir(),
                         createAbsolutePixelWidthImageTransformationParameters());
 
         Assert.assertEquals("actualImage is wrong", createExpectedTransformedImageAttributes(
                 getAbsolutePixelWidthScalingOutputImage(),
                 ABSOLUTE_WIDTH_SCALING_OUTPUT_PIXEL_WIDTH,
                 ABSOLUTE_WIDTH_SCALING_OUTPUT_PIXEL_HEIGHT), actualImage);
     }
 
     @Test
     public void testTransformImageWhenDimensionsPreservingTransformRequested() throws Throwable {
 
         recordReadSourceImageAttributes();
 
         final File outputImageDir =
                 new File(getOutputImageBaseDir(), getNoScalingOutputImageSubDir());
         recordOutputImageDirIsDirectory(outputImageDir);
 
         final File[] foundFiles = { getNoScalingOutputImage() };
         EasyMock.expect(
                 getMockFileIoFacade().list(outputImageDir, getNoScalingOutputImage().getName(),
                         "h*")).andReturn(foundFiles);
 
         replay();
 
         final TransformedImageAttributes actualImage =
                 getObjectUnderTest().transformImage(getSourceImage(), getOutputImageBaseDir(),
                         createNoScalingImageTransformationParameters());
 
         Assert.assertEquals("actualImage is wrong", createExpectedTransformedImageAttributes(
                 getNoScalingOutputImage(), SOURCE_IMAGE_PIXEL_WIDTH, SOURCE_IMAGE_PIXEL_HEIGHT),
                 actualImage);
     }
 
     @Test
     public void testTransformImageWhenNoTransformRequired() throws Throwable {
 
         recordReadSourceImageAttributes();
 
         replay();
 
         final TransformedImageAttributes actualImage =
             getObjectUnderTest().transformImage(getSourceImage(), getOutputImageBaseDir(),
                     createNoImageTransformationRequiredParameters());
 
         Assert.assertEquals("actualImage is wrong", createExpectedTransformedImageAttributes(
                 getSourceImage(), SOURCE_IMAGE_PIXEL_WIDTH, SOURCE_IMAGE_PIXEL_HEIGHT),
                 actualImage);
     }
 
     @Test
     public void testTransformImageWhenScalingUpLimitedToOriginalSize() throws Throwable {
         recordReadSourceImageAttributes();
 
         final File outputImageDir =
                 new File(getOutputImageBaseDir(), getNoScalingOutputImageSubDir());
         recordOutputImageDirIsDirectory(outputImageDir);
 
         final File[] foundFiles = { getNoScalingOutputImage() };
         EasyMock.expect(
                 getMockFileIoFacade().list(outputImageDir, getSourceImage().getName(), "h*"))
                 .andReturn(foundFiles);
 
         replay();
 
         final TransformedImageAttributes actualImage =
                 getObjectUnderTest().transformImage(getSourceImage(), getOutputImageBaseDir(),
                         createImageTransformationParametersWhenCalculatedWidthExceedsSource());
 
         Assert.assertEquals("actualImage is wrong", createExpectedTransformedImageAttributes(
                 getNoScalingOutputImage(),
                 SOURCE_IMAGE_PIXEL_WIDTH,
                 SOURCE_IMAGE_PIXEL_HEIGHT), actualImage);
     }
 
     @Test
     public void testTransformImageWhenMultiplePregeneratedImagesFound() throws Throwable {
 
         swapOutRealLoggerForMock(PregeneratedFileLookupImageTransformationFactoryBean.class);
 
         recordReadSourceImageAttributes();
 
         final File outputImageDir =
                 new File(getOutputImageBaseDir(), getDevicePercentWidthScalingOutputImageSubDir());
         recordOutputImageDirIsDirectory(outputImageDir);
 
         final File[] foundFiles =
                 {
                         getDevicePercentWidthScalingOutputImage(),
                         new File(getOutputImageBaseDir(),
                                 getDevicePercentWidthScalingOutputImageSubDir() + "/h"
                                         + (DEVICE_PERCENT_SCALING_OUTPUT_IMAGE_PIXEL_HEIGHT + 1)
                                         + "/myInputImage.png") };
         EasyMock.expect(
                 getMockFileIoFacade().list(outputImageDir, getSourceImage().getName(), "h*"))
                 .andReturn(foundFiles);
 
         EasyMock.expect(
                 getMockLogger(PregeneratedFileLookupImageTransformationFactoryBean.class)
                         .isEnabledFor(Level.WARN)).andReturn(Boolean.TRUE);
 
         getMockLogger(PregeneratedFileLookupImageTransformationFactoryBean.class).warn(
                 "Multiple images found with the same width: " + ArrayUtils.toString(foundFiles)
                         + ". Returning the first one. ");
 
         replay();
 
         final TransformedImageAttributes actualImage =
                 getObjectUnderTest().transformImage(getSourceImage(), getOutputImageBaseDir(),
                         createPercentWidthImageTransformationParameters());
 
         Assert.assertEquals("actualImage is wrong", createExpectedTransformedImageAttributes(
                 getDevicePercentWidthScalingOutputImage(),
                 DEVICE_PERCENT_SCALING_OUTPUT_IMAGE_PIXEL_WIDTH,
                 DEVICE_PERCENT_SCALING_OUTPUT_IMAGE_PIXEL_HEIGHT), actualImage);
     }
 
     @Test
     public void testTransformImageWhenPregeneratedImageWidthDirectoryNotFound() throws Throwable {
         recordReadSourceImageAttributes();
 
         final File outputImageDir =
                 new File(getOutputImageBaseDir(), getDevicePercentWidthScalingOutputImageSubDir());
 
         recordOutputImageDirIsNotDirectory(outputImageDir);
 
         replay();
 
         try {
             getObjectUnderTest().transformImage(getSourceImage(), getOutputImageBaseDir(),
                     createPercentWidthImageTransformationParameters());
 
             Assert.fail("ImageCreationException expected");
         } catch (final ImageCreationException e) {
 
             Assert.assertEquals("ImageCreationException has wrong message", getSourceImage()
                     .getName()
                     + " could not be found under "
                     + outputImageDir
                     + " because the directory does not exist.", e.getMessage());
         }
     }
 
     private void recordOutputImageDirIsDirectory(final File outputImageDir) {
         EasyMock.expect(getMockFileIoFacade().isDirectory(outputImageDir))
                 .andReturn(Boolean.TRUE);
     }
 
     private void recordOutputImageDirIsNotDirectory(final File outputImageDir) {
         EasyMock.expect(getMockFileIoFacade().isDirectory(outputImageDir))
         .andReturn(Boolean.FALSE);
     }
 
     @Test
     public void testTransformImageWhenPregeneratedImageNotFound() throws Throwable {
         recordReadSourceImageAttributes();
 
         final File outputImageDir =
             new File(getOutputImageBaseDir(), getDevicePercentWidthScalingOutputImageSubDir());
         recordOutputImageDirIsDirectory(outputImageDir);
 
         final File[] foundFiles = new File[] {};
         EasyMock.expect(
                 getMockFileIoFacade().list(outputImageDir, getSourceImage().getName(), "h*"))
                 .andReturn(foundFiles);
 
         replay();
 
         try {
             getObjectUnderTest().transformImage(getSourceImage(), getOutputImageBaseDir(),
                     createPercentWidthImageTransformationParameters());
 
             Assert.fail("ImageCreationException expected");
         } catch (final ImageCreationException e) {
 
             Assert.assertEquals("ImageCreationException has wrong message",
                     getSourceImage().getName()
                     + " could not be found under " + outputImageDir, e.getMessage());
         }
     }
 
     private void recordReadSourceImageAttributes() {
         EasyMock.expect(getMockImageReader().readImageAttributes(getSourceImage())).andReturn(
                 createSourceImageAttributes());
     }
 
     private ImageTransformationParameters createPercentWidthImageTransformationParameters() {
         final ImageTransformationParametersBean parametersBean =
                 new ImageTransformationParametersBean();
 
         parametersBean.setDeviceImagePercentWidth(REQUESTED_DEVICE_PERCENT_SCALED_WIDTH);
         parametersBean.setDevicePixelWidth(DEVICE_PIXEL_WIDTH);
         parametersBean.setOutputImageFormat(ImageFormat.PNG);
         return parametersBean;
     }
 
     private ImageTransformationParameters
         createImageTransformationParametersWhenCalculatedWidthExceedsSource() {
 
         final ImageTransformationParametersBean parametersBean =
             new ImageTransformationParametersBean();
 
         parametersBean.setDeviceImagePercentWidth(REQUESTED_DEVICE_PERCENT_SCALED_WIDTH);
         parametersBean.setDevicePixelWidth(VERY_LARGE_DEVICE_PIXEL_WIDTH);
         parametersBean.setOutputImageFormat(ImageFormat.PNG);
         return parametersBean;
     }
 
     private ImageTransformationParameters createAbsolutePixelWidthImageTransformationParameters() {
         final ImageTransformationParametersBean parametersBean =
             new ImageTransformationParametersBean();
 
         parametersBean.setAbsolutePixelWidth(REQUESTED_ABSOLUTE_SCALED_WIDTH);
         parametersBean.setDevicePixelWidth(DEVICE_PIXEL_WIDTH);
         parametersBean.setOutputImageFormat(ImageFormat.PNG);
         return parametersBean;
     }
 
     private ImageTransformationParameters createNoScalingImageTransformationParameters() {
         final ImageTransformationParametersBean parametersBean =
             new ImageTransformationParametersBean();
 
         parametersBean.setDevicePixelWidth(DEVICE_PIXEL_WIDTH);
         parametersBean.setOutputImageFormat(ImageFormat.GIF);
         parametersBean.setBackgroundColor("#FFF");
         return parametersBean;
     }
 
     private ImageTransformationParameters createNoImageTransformationRequiredParameters() {
         final ImageTransformationParametersBean parametersBean =
             new ImageTransformationParametersBean();
 
         parametersBean.setDevicePixelWidth(DEVICE_PIXEL_WIDTH);
         parametersBean.setOutputImageFormat(ImageFormat.PNG);
         return parametersBean;
     }
 
     private ImageAttributes createSourceImageAttributes() {
         final ImageAttributesBean imageAttributesBean = new ImageAttributesBean();
 
         imageAttributesBean.setImageFile(getSourceImage());
         imageAttributesBean.setPixelWidth(SOURCE_IMAGE_PIXEL_WIDTH);
         imageAttributesBean.setPixelHeight(SOURCE_IMAGE_PIXEL_HEIGHT);
 
         return imageAttributesBean;
     }
 
     private TransformedImageAttributes createExpectedTransformedImageAttributes(
             final File outputImage, final int pixelWidth, final int pixelHeight) {
         final TransformedImageAttributesBean imageBean = new TransformedImageAttributesBean();
 
         final ImageAttributesBean imageAttributesBean = new ImageAttributesBean();
         imageAttributesBean.setImageFile(outputImage);
         imageAttributesBean.setPixelWidth(pixelWidth);
         imageAttributesBean.setPixelHeight(pixelHeight);
 
         imageBean.setSourceImageAttributes(createSourceImageAttributes());
         imageBean.setOutputImageAttributes(imageAttributesBean);
 
         return imageBean;
     }
 
     @Test
     public void testSourceImageFileNotReadable() throws Throwable {
         final File[] unreadableSourceImageFiles = new File[] { null, new File("i cannot be read") };
         for (final File unreadableFile : unreadableSourceImageFiles) {
             try {
                 getObjectUnderTest().transformImage(unreadableFile, getOutputImageBaseDir(),
                         createPercentWidthImageTransformationParameters());
 
                 Assert.fail("ImageCreationException expected for file: '" + unreadableFile + "'");
 
             } catch (final ImageCreationException e) {
 
                 Assert.assertEquals("ImageCreationException has wrong message",
                         "Cannot read source image file: '" + unreadableFile + "'", e.getMessage());
             }
         }
 
     }
 
     @Test
     public void testBaseOutputImageDirInvalid() throws Throwable {
         final File[] invalidDirs = new File[] { null, new File("i am not a dir") };
         for (final File invalidDir : invalidDirs) {
             try {
                 getObjectUnderTest().transformImage(getSourceImage(), invalidDir,
                         createPercentWidthImageTransformationParameters());
 
                 Assert.fail("ImageCreationException expected for file: '" + invalidDir + "'");
 
             } catch (final ImageCreationException e) {
 
                 Assert.assertEquals("ImageCreationException has wrong message",
                         "Base output image dir is not a directory: '" + invalidDir + "'", e
                                 .getMessage());
             }
         }
 
     }
 
     @Test
     public void testConstructorWhenImageGenerationLastRunPropertiesCannotBeloaded()
             throws Throwable {
 
         final File imageGenerationLastRunPropertiesFile = new File("I don't exist");
         try {
             new PregeneratedFileLookupImageTransformationFactoryBean(getMockImageReader(),
                     imageGenerationLastRunPropertiesFile);
 
             Assert.fail("ContentRenderingFrameworkRuntimeException expected");
         } catch (final ContentRenderingFrameworkRuntimeException e) {
 
             Assert.assertEquals("ContentRenderingFrameworkRuntimeException has wrong message",
                     "Properties file '" + imageGenerationLastRunPropertiesFile
                             + "' could not be loaded. " + "Have you run the image pre-generation "
                             + "script as part of your build?", e.getMessage());
         }
 
     }
 
     @Test
     public void testConstructorWhenMissingImageWidthPixelsIntervalProperty() throws Throwable {
 
         final ClassPathResource classPathResource =
                 new ClassPathResource(MISSING_IMAGE_GENERATION_LAST_RUN_PROPERTIES_CLASSPATH);
         try {
 
             new PregeneratedFileLookupImageTransformationFactoryBean(getMockImageReader(),
                     classPathResource.getFile());
 
             Assert.fail("ContentRenderingFrameworkRuntimeException expected");
         } catch (final ContentRenderingFrameworkRuntimeException e) {
 
             Assert.assertEquals("ContentRenderingFrameworkRuntimeException has wrong message",
                     "Loaded property 'pixelsIncrement' must be an integer. Was: 'null'", e
                             .getMessage());
         }
     }
 
     @Test
     public void testConstructorWhenInvalidImageWidthPixelsIntervalProperty() throws Throwable {
 
         final ClassPathResource classPathResource =
                 new ClassPathResource(INVALID_IMAGE_GENERATION_LAST_RUN_PROPERTIES_CLASSPATH);
         try {
 
             new PregeneratedFileLookupImageTransformationFactoryBean(getMockImageReader(),
                     classPathResource.getFile());
 
             Assert.fail("ContentRenderingFrameworkRuntimeException expected");
         } catch (final ContentRenderingFrameworkRuntimeException e) {
 
             Assert.assertEquals("ContentRenderingFrameworkRuntimeException has wrong message",
                     "Loaded property 'pixelsIncrement' must be an integer. Was: '10m'", e
                             .getMessage());
         }
 
     }
 
     @Test
     public void testInfoMessageWhenImageGenerationLastRunPropertiesSuccessfullyBeloaded()
             throws Throwable {
 
         final ClassPathResource classPathResource =
                 new ClassPathResource(IMAGE_GENERATION_LAST_RUN_PROPERTIES_CLASSPATH);
 
         swapOutRealLoggerForMock(PregeneratedFileLookupImageTransformationFactoryBean.class);
 
         EasyMock.expect(
                 getMockLogger(PregeneratedFileLookupImageTransformationFactoryBean.class)
                         .isInfoEnabled()).andReturn(Boolean.TRUE);
 
         getMockLogger(PregeneratedFileLookupImageTransformationFactoryBean.class).info(
                 "Loaded properties from '" + classPathResource.getFile() + "': "
                         + createExpectedImageGenerationLastRunProperties());
 
         replay();
 
         new PregeneratedFileLookupImageTransformationFactoryBean(getMockImageReader(),
                 classPathResource.getFile());
 
     }
 
     private Properties createExpectedImageGenerationLastRunProperties() {
         final Properties properties = new Properties();
         properties.put("pixelsIncrement", "10");
         return properties;
     }
 
     /**
      * @return the objectUnderTest
      */
     private PregeneratedFileLookupImageTransformationFactoryBean getObjectUnderTest() {
         return objectUnderTest;
     }
 
     /**
      * @param objectUnderTest
      *            the objectUnderTest to set
      */
     private void setObjectUnderTest(
             final PregeneratedFileLookupImageTransformationFactoryBean objectUnderTest) {
 
         this.objectUnderTest = objectUnderTest;
     }
 
     /**
      * @return the outputImageBaseDir
      */
     private File getOutputImageBaseDir() {
         return outputImageBaseDir;
     }
 
     /**
      * @param outputImageBaseDir the outputImageBaseDir to set
      */
     private void setOutputImageBaseDir(final File outputImageBaseDir) {
         this.outputImageBaseDir = outputImageBaseDir;
     }
 
     /**
      * @return the sourceImage
      */
     private File getSourceImage() {
         return sourceImage;
     }
 
     /**
      * @param sourceImage the sourceImage to set
      */
     private void setSourceImage(final File sourceImage) {
         this.sourceImage = sourceImage;
     }
 
     /**
      * @return the mockFileIoFacade
      */
     public FileIoFacade getMockFileIoFacade() {
         return mockFileIoFacade;
     }
 
     /**
      * @param mockFileIoFacade the mockFileIoFacade to set
      */
     public void setMockFileIoFacade(final FileIoFacade mockFileIoFacade) {
         this.mockFileIoFacade = mockFileIoFacade;
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
 
     /**
      * @return the mockProcessStarter
      */
     public ProcessStarter getMockProcessStarter() {
         return mockProcessStarter;
     }
 
     /**
      * @param mockProcessStarter the mockProcessStarter to set
      */
     public void setMockProcessStarter(final ProcessStarter mockProcessStarter) {
         this.mockProcessStarter = mockProcessStarter;
     }
 
     /**
      * @return the mockProcess
      */
     public Process getMockProcess() {
         return mockProcess;
     }
 
     /**
      * @param mockProcess the mockProcess to set
      */
     public void setMockProcess(final Process mockProcess) {
         this.mockProcess = mockProcess;
     }
 
     /**
      * @return the devicePercentWidthScalingOutputImageSubDir
      */
     private String getDevicePercentWidthScalingOutputImageSubDir() {
         return devicePercentWidthScalingOutputImageSubDir;
     }
 
     /**
      * @param devicePercentWidthScalingOutputImageSubDir
      *            the devicePercentWidthScalingOutputImageSubDir to set
      */
     private void setDevicePercentWidthScalingOutputImageSubDir(
             final String devicePercentWidthScalingOutputImageSubDir) {
         this.devicePercentWidthScalingOutputImageSubDir =
                 devicePercentWidthScalingOutputImageSubDir;
     }
 
     /**
      * @return the devicePercentWidthScalingOutputImage
      */
     private File getDevicePercentWidthScalingOutputImage() {
         return devicePercentWidthScalingOutputImage;
     }
 
     /**
      * @param devicePercentWidthScalingOutputImage the devicePercentWidthScalingOutputImage to set
      */
     private void setDevicePercentWidthScalingOutputImage(
             final File devicePercentWidthScalingOutputImage) {
         this.devicePercentWidthScalingOutputImage = devicePercentWidthScalingOutputImage;
     }
 
     /**
      * @return the absolutePixelWidthScalingOutputImageSubDir
      */
     private String getAbsolutePixelWidthScalingOutputImageSubDir() {
         return absolutePixelWidthScalingOutputImageSubDir;
     }
 
     /**
      * @param absolutePixelWidthScalingOutputImageSubDir
      *            the absolutePixelWidthScalingOutputImageSubDir to set
      */
     private void setAbsolutePixelWidthScalingOutputImageSubDir(
             final String absolutePixelWidthScalingOutputImageSubDir) {
         this.absolutePixelWidthScalingOutputImageSubDir =
                 absolutePixelWidthScalingOutputImageSubDir;
     }
 
     /**
      * @return the absolutePixelWidthScalingOutputImage
      */
     private File getAbsolutePixelWidthScalingOutputImage() {
         return absolutePixelWidthScalingOutputImage;
     }
 
     /**
      * @param absolutePixelWidthScalingOutputImage the absolutePixelWidthScalingOutputImage to set
      */
     private void setAbsolutePixelWidthScalingOutputImage(
             final File absolutePixelWidthScalingOutputImage) {
         this.absolutePixelWidthScalingOutputImage = absolutePixelWidthScalingOutputImage;
     }
 
     /**
      * @return the noScalingOutputImageSubDir
      */
     private String getNoScalingOutputImageSubDir() {
         return noScalingOutputImageSubDir;
     }
 
     /**
      * @param noScalingOutputImageSubDir the noScalingOutputImageSubDir to set
      */
     private void setNoScalingOutputImageSubDir(final String noScalingOutputImageSubDir) {
         this.noScalingOutputImageSubDir = noScalingOutputImageSubDir;
     }
 
     /**
      * @return the noScalingOutputImage
      */
     private File getNoScalingOutputImage() {
         return noScalingOutputImage;
     }
 
     /**
      * @param noScalingOutputImage the noScalingOutputImage to set
      */
     private void setNoScalingOutputImage(final File noScalingOutputImage) {
         this.noScalingOutputImage = noScalingOutputImage;
     }
 }
