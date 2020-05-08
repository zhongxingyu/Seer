 package au.com.sensis.mobile.crf.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.core.io.support.PropertiesLoaderUtils;
 
 import au.com.sensis.mobile.crf.exception.ContentRenderingFrameworkRuntimeException;
 
 public class PregeneratedFileLookupImageTransformationFactoryBean extends AbstractImageTransformationFactoryBean {
 
     // Not final so that we can inject a mock during testing.
     private static Logger logger =
             Logger.getLogger(PregeneratedFileLookupImageTransformationFactoryBean.class);
 
     private static final String IMAGE_WIDTH_PIXELS_INTERVAL_PROPERTY_NAME = "pixelsIncrement";
 
     /**
      * Number of pixels between each pre-generated image along the "width dimension".
      */
     private int imageWidthPixelsInterval;
 
     private Properties imageGenerationLastRunProperties;
 
     /**
      * @param imageReader
      *            {@link ImageReader} to use to read images.
      * @param imageWidthPixelsInterval
      *            Number of pixels between each pre-generated image along the
      *            "width dimension".
      */
     public PregeneratedFileLookupImageTransformationFactoryBean(final ImageReader imageReader,
             final File imageGenerationLastRunPropertiesFile) {
         super(imageReader);
 
         loadImageGenerationLastRunProperties(imageGenerationLastRunPropertiesFile);
     }
 
     private void loadImageGenerationLastRunProperties(
             final File imageGenerationLastRunPropertiesFile) {
         try {
             setImageGenerationLastRunProperties(PropertiesLoaderUtils
                     .loadProperties(new FileSystemResource(imageGenerationLastRunPropertiesFile)));
             if (logger.isInfoEnabled()) {
                 logger.info("Loaded properties from '" + imageGenerationLastRunPropertiesFile
                         + "': " + getImageGenerationLastRunProperties());
             }
         } catch (final IOException e) {
             throw new ContentRenderingFrameworkRuntimeException("Properties file '"
                     + imageGenerationLastRunPropertiesFile + "' could not be loaded. "
                     + "Have you run the image pre-generation script as part of your build?", e);
         }
 
         loadImageWidthPixelsInterval();
     }
 
     @Override
     protected TransformedImageAttributes doTransformImage(final File sourceImageFile,
             final ImageAttributes sourceImageAttributes, final File baseTargetImageDir,
             final ImageTransformationParameters imageTransformationParameters) throws IOException {
 
         final int outputImageWidth =
                 findNearestPregeneratedImageWidth(sourceImageAttributes,
                         imageTransformationParameters);
 
         final File outputImageWidthDir = new File(baseTargetImageDir, "w" + outputImageWidth);
         validateOutputImageWidthDir(outputImageWidthDir, sourceImageFile);
 
         final String outputImageFilename =
                 createOutputImageFilename(imageTransformationParameters, sourceImageFile);
         final File[] foundPregeneratedImages =
                 FileIoFacadeFactory.getFileIoFacadeSingleton().list(outputImageWidthDir,
                         outputImageFilename, "h*");
 
         validateImageFound(foundPregeneratedImages, outputImageWidthDir, sourceImageFile);
 
         logWarnIfMultipleImagesFound(foundPregeneratedImages);
 
         final File outputImage = foundPregeneratedImages[0];
         final int outputImageHeight = getPregeneratedImageHeight(outputImage);
 
         return createTransformedImageAttributes(sourceImageAttributes, foundPregeneratedImages[0],
                 outputImageWidth, outputImageHeight);
 
     }
 
     private void validateOutputImageWidthDir(final File outputImageWidthDir,
             final File sourceImageFile) {
         if (!FileIoFacadeFactory.getFileIoFacadeSingleton().isDirectory(outputImageWidthDir)) {
             throw new ImageCreationException(sourceImageFile.getName()
                     + " could not be found under " + outputImageWidthDir
                     + " because the directory does not exist.");
         }
     }
 
     private void logWarnIfMultipleImagesFound(final File[] foundPregeneratedImages) {
         if ((foundPregeneratedImages.length > 1) && logger.isEnabledFor(Level.WARN)) {
             logger.warn("Multiple images found with the same width: "
                     + ArrayUtils.toString(foundPregeneratedImages) + ". Returning the first one. ");
         }
     }
 
     private void validateImageFound(final File[] foundPregeneratedImages, final File baseOutputDir,
             final File sourceImageFile) {
         if (foundPregeneratedImages.length == 0) {
             throw new ImageCreationException(sourceImageFile.getName()
                     + " could not be found under " + baseOutputDir);
         }
     }
 
     private int findNearestPregeneratedImageWidth(final ImageAttributes sourceImageAttributes,
             final ImageTransformationParameters imageTransformationParameters) {
         final int outputImageWidth =
                 roundWidthToNearestPregenerated(calculateOutputImageWidth(
                         imageTransformationParameters, sourceImageAttributes));
 
         // Refuse to look for an image that is larger than the source image.
         if (outputImageWidth > sourceImageAttributes.getPixelWidth()) {
             return sourceImageAttributes.getPixelWidth();
         } else {
             return outputImageWidth;
         }
     }
 
     private int roundWidthToNearestPregenerated(final int calculateOutputImageWidth) {
         return calculateOutputImageWidth - (calculateOutputImageWidth % getImageWidthPixelsInterval());
     }
 
     private int getPregeneratedImageHeight(final File outputImage) {
         final File parentDir = outputImage.getParentFile();
         final String heightAsString = StringUtils.stripStart(parentDir.getName(), "h");
         try {
             return Integer.parseInt(heightAsString);
         } catch (final NumberFormatException e) {
             throw new ImageCreationException("The found image '" + outputImage
                     + "' has an invalid height specified in its path.", e);
         }
     }
 
     /**
      * @return the imageWidthPixelsInterval
      */
     private int getImageWidthPixelsInterval() {
         return imageWidthPixelsInterval;
     }
 
     private void loadImageWidthPixelsInterval() {
         final String rawImageWidthPixelsIntervalProperty =
                 getImageGenerationLastRunProperties().getProperty(
                         IMAGE_WIDTH_PIXELS_INTERVAL_PROPERTY_NAME);
         try {
             imageWidthPixelsInterval = Integer.parseInt(rawImageWidthPixelsIntervalProperty);
         } catch (final NumberFormatException e) {
             throw new ContentRenderingFrameworkRuntimeException("Loaded property '"
                     + IMAGE_WIDTH_PIXELS_INTERVAL_PROPERTY_NAME + "' must be an integer. Was: '"
                     + rawImageWidthPixelsIntervalProperty + "'", e);
         }
     }
 
     /**
      * @return the imageGenerationLastRunProperties
      */
     private Properties getImageGenerationLastRunProperties() {
         return imageGenerationLastRunProperties;
     }
 
     /**
      * @param imageGenerationLastRunProperties
      *            the imageGenerationLastRunProperties to set
      */
     private void setImageGenerationLastRunProperties(
             final Properties imageGenerationLastRunProperties) {
         this.imageGenerationLastRunProperties = imageGenerationLastRunProperties;
     }
 
 }
