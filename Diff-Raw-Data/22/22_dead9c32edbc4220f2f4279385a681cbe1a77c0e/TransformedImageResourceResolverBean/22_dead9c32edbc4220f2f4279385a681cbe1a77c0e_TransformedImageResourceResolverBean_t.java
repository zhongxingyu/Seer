 package au.com.sensis.mobile.crf.service;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import au.com.sensis.devicerepository.Device;
 import au.com.sensis.mobile.crf.config.Group;
 import au.com.sensis.mobile.crf.exception.ResourceResolutionRuntimeException;
 import au.com.sensis.mobile.crf.util.FileIoFacadeFactory;
 import au.com.sensis.mobile.crf.util.ImageAttributes;
 import au.com.sensis.mobile.crf.util.ImageReader;
 import au.com.sensis.mobile.crf.util.ImageTransformationFactory;
 import au.com.sensis.mobile.crf.util.ImageTransformationParametersBean;
 import au.com.sensis.mobile.crf.util.TransformedImageAttributes;
 
 /**
  * {@link ResourceResolver} that resolves abstract image paths to real image paths.
  *
  * @author Adrian.Koh2@sensis.com.au
  */
 public class TransformedImageResourceResolverBean extends AbstractSingleResourceResolver {
 
     private static final Logger LOGGER =
             Logger.getLogger(TransformedImageResourceResolverBean.class);
 
     /**
      * Name of the (optional) device property that specifies an extra ratio to apply to a
      * scaled image. This originated from the iphone 4 retina display. Even though the device
      * repository pixel width of the screen is 320, we actually have to apply an extra ratio
      * for the (real) 640 pixel wide retina display.
      */
     public static final String IMAGE_RATIO_DEVICE_PROPERTY_NAME = "custom.crf.image.ratio";
 
     private static final String PROPERTIES_FILE_EXTENSION = ".properties";
 
     private static final String WIDTH_PROPERTY_NAME = "width";
     private static final String BACKGROUND_COLOR_PROPERTY_NAME = "background.color";
 
     private final String [] fileExtensionWildcards;
     private final String [] excludedFileExtensionWildcards;
 
     private au.com.sensis.mobile.crf.util.PropertiesLoader propertiesLoader;
     private ImageTransformationFactory imageTransformationFactory;
     private final String imageFormatDeviceRepositoryPropertyName;
     private ImageReader imageReader;
 
     /**
      * Constructor.
      *
      * @param commonParams
      *            Holds the common parameters used in constructing all
      *            {@link ResourceResolver}s.
      * @param abstractResourceExtension
      *            Extension of resources (eg. "css" or "crf") that this class
      *            knows how to resolve.
      * @param rootResourcesDir
      *            Root directory where the real resources that this resolver
      *            handles are stored.
      * @param fileExtensionWildcards
      *            Array of image file extensions to match. Wildcards supported
      *            are '*' as per standard Unix/Windows command line semantics.
      * @param imageFormatDeviceRepositoryPropertyName Name of the property in the
      *            device repository that we will use to control the output image format.
      */
     public TransformedImageResourceResolverBean(
             final ResourceResolverCommonParamHolder commonParams,
             final String abstractResourceExtension, final File rootResourcesDir,
             final String[] fileExtensionWildcards,
             final String imageFormatDeviceRepositoryPropertyName) {
 
         super(commonParams, abstractResourceExtension, rootResourcesDir);
 
         validateFileExtensionWildcards(fileExtensionWildcards);
 
         this.fileExtensionWildcards = fileExtensionWildcards;
         excludedFileExtensionWildcards = new String [] { "properties", "*.md5" };
 
         this.imageFormatDeviceRepositoryPropertyName = imageFormatDeviceRepositoryPropertyName;
     }
 
     private void validateFileExtensionWildcards(
             final String[] fileExtensionWildcards) {
 
         if ((fileExtensionWildcards == null)
                 || (fileExtensionWildcards.length == 0)
                 || containsBlanks(fileExtensionWildcards)) {
 
             throw new IllegalArgumentException(
                     "fileExtensionWildcards must be an array of non-blank Strings but was: '"
                             + ArrayUtils.toString(fileExtensionWildcards) + "'");
         }
     }
 
     private boolean containsBlanks(final String[] fileExtensionWildcards) {
         for (final String wildcard : fileExtensionWildcards) {
             if (StringUtils.isBlank(wildcard)) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Invoked by {@link #resolve(String, Device)} if
      * {@link #isRecognisedAbstractResourceRequest(String)} returns true.
      *
      * @param requestedResourcePath
      *            Requested path. eg. /WEB-INF/view/jsp/detal/bdp.crf.
      * @param device
      *            {@link Device} to perform the path mapping for.
      * @return List of {@link Resource}s containing the results. If no resources
      *         can be resolved, an empty list is returned. May not be null.
      */
     @Override
     protected List<Resource> doResolve(final String requestedResourcePath, final Device device) {
 
         final FoundImageFiles imageFiles = findImageFiles(requestedResourcePath, device);
 
         try {
             return processFoundImageFiles(requestedResourcePath, device, imageFiles);
         } catch (final Exception e) {
             if (getResourceResolutionWarnLogger().isWarnEnabled()) {
                 getResourceResolutionWarnLogger().warn(
                         "Error resolving requested resource: '"
                                 + requestedResourcePath
                                 + "' for device " + device, e);
             }
             return new ArrayList<Resource>();
         }
     }
 
     private List<Resource> processFoundImageFiles(final String requestedResourcePath,
             final Device device, final FoundImageFiles imageFiles) throws IOException {
 
         if (imageFiles.getFoundFiles().length > 0) {
 
             final Resource foundResource =
                     createImageResource(requestedResourcePath, device, imageFiles.getGroup(),
                             imageFiles);
 
             return Arrays.asList(foundResource);
         } else {
             return new ArrayList<Resource>();
         }
     }
 
     private Properties getImageProperties(final List<File> imagePropertyFiles) {
 
         Properties imageProperties = new Properties();
 
         for (final File propertyFile : imagePropertyFiles) {
             imageProperties =
                     accumulateImageProperties(imageProperties,
                             propertyFile);
         }
 
         return imageProperties;
     }
 
     private List<File> getImagePropertyFiles(
             final String requestedResourcePath, final Device device) {
 
         final List<File> propertyFiles = new ArrayList<File>();
 
         final Iterator<Group> matchingGroupIterator =
                 getMatchingGroupIterator(device, requestedResourcePath);
 
         while (matchingGroupIterator.hasNext()) {
             final String newResourcePathMinusExtension =
                     createNewResourcePath(requestedResourcePath, matchingGroupIterator.next());
 
             final File propertiesFile =
                     new File(getRootResourcesDir(), newResourcePathMinusExtension
                             + PROPERTIES_FILE_EXTENSION);
             if (FileIoFacadeFactory.getFileIoFacadeSingleton().fileExists(propertiesFile)) {
                 propertyFiles.add(propertiesFile);
             }
         }
 
         return propertyFiles;
     }
 
     private FoundImageFiles findImageFiles(final String requestedResourcePath,
             final Device device) {
 
         File[] matchedFiles = new File[] {};
         final Iterator<Group> matchingGroupIterator =
                 getMatchingGroupIterator(device, requestedResourcePath);
         Group currGroup = null;
 
         while (matchingGroupIterator.hasNext()) {
 
             currGroup = matchingGroupIterator.next();
 
             debugLogCheckingGroup(requestedResourcePath, currGroup);
 
             final String newResourcePathMinusExtension =
                     createNewResourcePath(requestedResourcePath, currGroup);
 
             debugLogCheckingForImagesIn(newResourcePathMinusExtension);
 
             matchedFiles =
                     FileIoFacadeFactory.getFileIoFacadeSingleton().list(getRootResourcesDir(),
                             newResourcePathMinusExtension, getFileExtensionWildcards(),
                             getExcludedFileExtensionWildcards());
 
             if (matchedFiles.length > 0) {
                 debugLogFoundImages(matchedFiles);
                 break;
             }
         }
 
         return new FoundImageFiles(matchedFiles, currGroup);
 
     }
 
     private Properties accumulateImageProperties(final Properties imageProperties,
             final File propertiesFile) {
 
         debugLogAccumulatingImagePropertiesFromFile(propertiesFile);
 
         Properties foundProperties = new Properties();
         try {
             foundProperties = getPropertiesLoader().loadPropertiesNotNull(propertiesFile);
             debugLogFoundProperties(foundProperties);
         } catch (final IOException e) {
             throw new ResourceResolutionRuntimeException("Could not load properties file: "
                     + propertiesFile);
         }
 
         // The order here is important. We want properties from earlier files to take precedence.
         foundProperties.putAll(imageProperties);
 
         return foundProperties;
     }
 
     private File createBaseTargetImageDir(final File sourceImage) {
         return sourceImage.getParentFile();
     }
 
     private ImageTransformationParametersBean createImageTransformationParametersBean(
             final Device device, final Properties imageProperties,
             final File foundFile) {
 
         final ImageTransformationParametersBean result = new ImageTransformationParametersBean();
 
         setDeviceImagePercentWidthIfRequired(result, device, imageProperties);
         setAbsolutePixelWidthIfRequired(result, device, imageProperties);
 
         // Note that for some devices, the pixelsX value will include the
         // scrollbar, for others
         // it will not. Regardless, the only handle that can be used to control
         // the image size
         // is the width property.
         result.setDevicePixelWidth(device.getPixelsX());
 
         result.setOutputImageFormat(getOutputImageFormatParameter(device,
                 foundFile));
         result.setBackgroundColor(getBackgroundColorParameter(imageProperties));
 
         if (getLogger().isDebugEnabled()) {
             getLogger().debug("ImageTransformationParametersBean: " + result);
         }
 
         return result;
     }
 
     private String getBackgroundColorParameter(final Properties imageProperties) {
         if (imageProperties.getProperty(BACKGROUND_COLOR_PROPERTY_NAME) != null) {
             return imageProperties.getProperty(BACKGROUND_COLOR_PROPERTY_NAME).trim();
         } else {
             return null;
         }
     }
 
     private ImageTransformationFactory.ImageFormat getOutputImageFormatParameter(
             final Device device, final File foundFile) {
 
         if (ImageTransformationFactory.ImageFormat.GIF.hasExtension(foundFile)) {
             // We can't necessarily transform GIF to PNG, especially if the GIF is an animated
             // GIF so in this case, we ignore the device repository.
             return ImageTransformationFactory.ImageFormat.GIF;
         }
 
        if ("PNG".equals(device.preferredImageType())) {
             return ImageTransformationFactory.ImageFormat.PNG;
         }
 
         // Default format.
         return ImageTransformationFactory.ImageFormat.GIF;
     }
 
     private void setDeviceImagePercentWidthIfRequired(
             final ImageTransformationParametersBean parametersBean, final Device device,
             final Properties imageProperties) {
 
         final String rawPropertyValue = imageProperties.getProperty(WIDTH_PROPERTY_NAME);
 
         if (StringUtils.isNotBlank(rawPropertyValue) && rawPropertyValue.trim().endsWith("%")) {
 
             final Integer percentWidth = parseNonBlankPercentWidthProperty(rawPropertyValue);
 
             if (percentWidth <= 0) {
                 throw new ResourceResolutionRuntimeException("Could not parse width property: '"
                         + rawPropertyValue
                         + "'. Must be a % or px value greater than 0. eg. 80% or 100px");
             }
 
             parametersBean.setDeviceImagePercentWidth(percentWidth * getImageRatio(device));
         }
     }
 
     private Integer parseNonBlankPercentWidthProperty(final String rawPropertyValue) {
         try {
             return Integer.valueOf(rawPropertyValue.replaceFirst("%\\s*$", StringUtils.EMPTY));
         } catch (final NumberFormatException e) {
             throw new ResourceResolutionRuntimeException("Could not parse width property: '"
                     + rawPropertyValue
                     + "'. Must be a % or px value greater than 0. eg. 80% or 100px", e);
         }
     }
 
     private Integer getImageRatio(final Device device) {
         Integer imageRatio = device.getPropertyAsInteger(IMAGE_RATIO_DEVICE_PROPERTY_NAME);
         if (imageRatio == null) {
             imageRatio = 1;
         }
         return imageRatio;
     }
 
     private void setAbsolutePixelWidthIfRequired(
             final ImageTransformationParametersBean parametersBean, final Device device,
             final Properties imageProperties) {
 
         final String rawPropertyValue = imageProperties.getProperty(WIDTH_PROPERTY_NAME);
 
         if (StringUtils.isNotBlank(rawPropertyValue) && rawPropertyValue.trim().endsWith("px")) {
 
             final Integer absolutePixelWidth =
                     parseNonBlankAbsolutePixelWidthProperty(rawPropertyValue);
 
             if (absolutePixelWidth <= 0) {
                 throw new ResourceResolutionRuntimeException("Could not parse width property: '"
                         + rawPropertyValue
                         + "'. Must be a % or px value greater than 0. eg. 80% or 100px");
             }
 
             parametersBean.setAbsolutePixelWidth(absolutePixelWidth * getImageRatio(device));
         }
 
     }
 
     private Integer parseNonBlankAbsolutePixelWidthProperty(final String rawPropertyValue) {
         try {
             return Integer.valueOf(rawPropertyValue.replaceFirst("\\s*px\\s*$", StringUtils.EMPTY));
         } catch (final NumberFormatException e) {
             throw new ResourceResolutionRuntimeException("Could not parse width property: '"
                     + rawPropertyValue + "'", e);
         }
     }
 
     private Resource createImageResource(final String requestedResourcePath, final Device device,
             final Group group, final FoundImageFiles imageFiles) throws IOException {
 
         warnIfMultipleResourcesWithExtensionsFound(requestedResourcePath, imageFiles
                 .getFoundFiles());
 
         final File foundFile = imageFiles.getFoundFiles()[0];
 
         if (foundFile.getPath().endsWith(Resource.DOT_NULL_EXTENSION)) {
             return createImageResourceFromExactFileFound(requestedResourcePath, group, foundFile);
         }
 
         final List<File> imagePropertyFiles = getImagePropertyFiles(requestedResourcePath, device);
         if (imagePropertyFiles.isEmpty()) {
             return createImageResourceFromExactFileFound(requestedResourcePath, group, foundFile);
 
         } else {
             return createImageResourceByTransformingFoundFile(requestedResourcePath, device, group,
                     foundFile, imagePropertyFiles);
         }
 
     }
 
     private Resource createImageResourceByTransformingFoundFile(final String requestedResourcePath,
             final Device device, final Group group, final File foundFile,
             final List<File> imagePropertyFiles) throws IOException {
 
         final Properties imageProperties =
                 getImageProperties(imagePropertyFiles);
 
         final ImageTransformationParametersBean transformationParametersBean =
                 createImageTransformationParametersBean(device, imageProperties,
                         foundFile);
 
         final TransformedImageAttributes transformedImageAttributes =
                 getImageTransformationFactory().transformImage(foundFile,
                         createBaseTargetImageDir(foundFile), transformationParametersBean);
 
         logWarningIfUpScaledImage(device, transformedImageAttributes);
 
         final String relativeOutputImagePath =
                 getResourcePathRelativeTo(transformedImageAttributes.getOutputImageAttributes()
                         .getFile(), getRootResourcesDir());
 
         final ImageResourceBean resource =
                 new ImageResourceBean(requestedResourcePath, relativeOutputImagePath,
                         getRootResourcesDir(), group);
         resource.setImageWidth(transformedImageAttributes.getOutputImageAttributes()
                 .getPixelWidth());
         resource.setImageHeight(transformedImageAttributes.getOutputImageAttributes()
                 .getPixelHeight());
         return resource;
     }
 
     private void logWarningIfUpScaledImage(final Device device,
             final TransformedImageAttributes transformedImageAttributes) {
 
         final ImageAttributes sourceImageAttributes =
                 transformedImageAttributes.getSourceImageAttributes();
         final ImageAttributes outputImageAttributes =
                 transformedImageAttributes.getOutputImageAttributes();
 
         if (((outputImageAttributes.getPixelWidth() > sourceImageAttributes.getPixelWidth())
                 || (outputImageAttributes.getPixelHeight()
                         > sourceImageAttributes.getPixelHeight()))
                         && getResourceResolutionWarnLogger().isWarnEnabled()) {
 
             getResourceResolutionWarnLogger().warn(
                     "Scaled image up for device " + device + ". This may produce "
                             + "unacceptable image quality. Source image attributes: '"
                             + sourceImageAttributes + "'. Output image attributes: '"
                             + outputImageAttributes + "'");
         }
 
     }
 
     private Resource createImageResourceFromExactFileFound(final String requestedResourcePath,
             final Group group, final File foundFile) {
         if (foundFile.getPath().endsWith(Resource.DOT_NULL_EXTENSION)) {
             return new ImageResourceBean(requestedResourcePath, getResourcePathRelativeTo(
                     foundFile, getRootResourcesDir()), getRootResourcesDir(), group);
 
         } else {
             final ImageAttributes sourceImageAttributes =
                     getImageReader().readImageAttributes(foundFile);
 
             final ImageResourceBean imageResourceBean =
                     new ImageResourceBean(requestedResourcePath, getResourcePathRelativeTo(
                             foundFile, getRootResourcesDir()), getRootResourcesDir(), group);
             imageResourceBean.setImageWidth(sourceImageAttributes.getPixelWidth());
             imageResourceBean.setImageHeight(sourceImageAttributes.getPixelHeight());
 
             return imageResourceBean;
         }
     }
 
     private String getResourcePathRelativeTo(final File file, final File rootResourcesDir) {
         String relativePath = StringUtils.removeStart(file.getPath(), rootResourcesDir.getPath());
         relativePath = relativePath.replaceFirst("^\\\\", StringUtils.EMPTY);
         relativePath = relativePath.replaceFirst("^/", StringUtils.EMPTY);
         relativePath = relativePath.replaceAll("\\\\", "/");
         return relativePath;
     }
 
     private void warnIfMultipleResourcesWithExtensionsFound(
             final String requestedResourcePath,
             final File[] matchedFiles) {
 
         if ((matchedFiles.length > 1)
                 && getResourceResolutionWarnLogger().isWarnEnabled()) {
             getResourceResolutionWarnLogger().warn(
                     "Requested resource '"
                             + requestedResourcePath
                             + "' resolved to multiple real resources with extensions matching "
                             + ArrayUtils.toString(getFileExtensionWildcards())
                             + ". Will only use the first resource. Total found: "
                             + nonEmptyArrayToString(matchedFiles)
                             + ".");
         }
     }
 
     private String nonEmptyArrayToString(
             final File[] matchedFiles) {
         final StringBuilder stringBuilder = new StringBuilder();
         stringBuilder.append("[");
         int i = 0;
         for (final File currFile : matchedFiles) {
             stringBuilder.append(currFile);
             if (i < (matchedFiles.length - 1)) {
                 stringBuilder.append(", ");
             }
             i++;
         }
         stringBuilder.append("]");
         return stringBuilder.toString();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected String getDebugResourceTypeName() {
         return "image";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected Logger getLogger() {
         return LOGGER;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected String getRealResourcePathExtension() {
         return StringUtils.EMPTY;
     }
 
     private String[] getFileExtensionWildcards() {
         return fileExtensionWildcards;
     }
 
     private void debugLogCheckingForImagesIn(final String newResourcePathMinusExtension) {
         if (getLogger().isDebugEnabled()) {
             getLogger().debug("Checking for images with base path of : '"
                     + newResourcePathMinusExtension + "'");
         }
     }
 
     private void debugLogFoundImages(final File [] images) {
         if (getLogger().isDebugEnabled()) {
             getLogger().debug("Found images: '"
                     + ArrayUtils.toString(images) + "'");
         }
     }
 
     private void debugLogAccumulatingImagePropertiesFromFile(final File propertiesFile) {
         if (getLogger().isDebugEnabled()) {
             getLogger().debug("Accumulating properties from '" + propertiesFile + "'");
         }
     }
 
     private void debugLogFoundProperties(final Properties foundProperties) {
         if (getLogger().isDebugEnabled()) {
             getLogger().debug(
                     "Found properties: " + foundProperties);
         }
     }
 
     /**
      * @return the propertiesLoader
      */
     private au.com.sensis.mobile.crf.util.PropertiesLoader getPropertiesLoader() {
         return propertiesLoader;
     }
 
     /**
      * @return the scaledImageFactory
      */
     private ImageTransformationFactory getImageTransformationFactory() {
         return imageTransformationFactory;
     }
 
     /**
      * @param propertiesLoader
      *            the propertiesLoader to set
      */
     public void setPropertiesLoader(
             final au.com.sensis.mobile.crf.util.PropertiesLoader propertiesLoader) {
         this.propertiesLoader = propertiesLoader;
     }
 
     /**
      * @param imageTransformationFactory
      *            the scaledImageFactory to set
      */
     public void setImageTransformationFactory(
             final ImageTransformationFactory imageTransformationFactory) {
         this.imageTransformationFactory = imageTransformationFactory;
     }
 
     private final class FoundImageFiles {
         private final File [] foundFiles;
         private final Group group;
 
         private FoundImageFiles(final File[] foundFiles, final Group group) {
             this.foundFiles = foundFiles;
             this.group = group;
         }
 
         /**
          * @return the foundFiles
          */
         private File[] getFoundFiles() {
             return foundFiles;
         }
 
         /**
          * @return the group
          */
         private Group getGroup() {
             return group;
         }
 
     }
 
     /**
      * @return the excludedFileExtensionWildcards
      */
     public String[] getExcludedFileExtensionWildcards() {
         return excludedFileExtensionWildcards;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected String getResourceSubDirName() {
         return "images";
     }
 
     /**
      * @return the imageFormatDeviceRepositoryPropertyName
      */
     private String getImageFormatDeviceRepositoryPropertyName() {
         return imageFormatDeviceRepositoryPropertyName;
     }
 
     /**
      * @return the imageReader
      */
     private ImageReader getImageReader() {
         return imageReader;
     }
 
     /**
      * @param imageReader the imageReader to set
      */
     public void setImageReader(final ImageReader imageReader) {
         this.imageReader = imageReader;
     }
 }
