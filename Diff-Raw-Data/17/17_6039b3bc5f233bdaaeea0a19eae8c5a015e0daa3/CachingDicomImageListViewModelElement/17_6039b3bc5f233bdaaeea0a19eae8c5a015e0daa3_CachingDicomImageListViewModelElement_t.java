 package de.sofd.viskit.model;
 
 import java.awt.color.ColorSpace;
 import java.awt.image.BufferedImage;
 import java.awt.image.Raster;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.nio.IntBuffer;
 import java.nio.ShortBuffer;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReader;
 import javax.imageio.stream.ImageInputStream;
 import javax.swing.SwingUtilities;
 
 import org.apache.log4j.Logger;
 import org.dcm4che2.data.BasicDicomObject;
 import org.dcm4che2.data.DicomObject;
 import org.dcm4che2.data.Tag;
 import org.dcm4che2.data.UID;
 import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;
 import org.dcm4che2.io.DicomOutputStream;
 import org.dcm4che2.media.FileMetaInformation;
 
 import de.sofd.util.FloatRange;
 import de.sofd.util.Histogram;
 import de.sofd.util.IntRange;
 import de.sofd.util.NumericPriorityMap;
 import de.sofd.util.concurrent.NumericPriorityThreadPoolExecutor;
 import de.sofd.util.concurrent.PrioritizedTask;
 import de.sofd.viskit.test.windowing.RawDicomImageReader;
 
 /**
  * Implements getDicomObject(), getImage() as caching delegators to the
  * (subclass-provided) methods getImageKey(), getBackendDicomObject(), and
  * optionally getBackendImage() and getBackendDicomObjectMetaData().
  * <p>
  * Supports asynchronous mode (see {@link #isAsyncMode()},
  * {@link #setAsyncMode(boolean)}) in which, as long as the DICOM object isn't
  * loaded yet, the element's {@link #getInitializationState()
  * initializationState} will be set to UNINITIALIZED and the image will be
  * loaded in a background thread. When that's done, the initializationState will
  * be set to INITIALIZED (or to ERROR if an error occurred). A corresponding
  * property change event will be fired as specified in the
  * {@link #getInitializationState()} Javadoc. Lists that contain the element
  * will pick up that event and change their display of the corresponding cell
  * accordingly.
  * 
  * TODO: Optional caching of #getRawImage()?
  * 
  * @author olaf
  */
 public abstract class CachingDicomImageListViewModelElement extends AbstractImageListViewModelElement implements DicomImageListViewModelElement {
 
     protected int frameNumber = 0;
     protected int totalFrameNumber = -1;
 
     private final NumericPriorityMap<Object, DicomObject> dcmObjectCache;
 
     private final LRUMemoryCache<Object, BufferedImage> imageCache = Config.defaultBufferedImageCache;
 
     private static Map<Object, DicomObject> rawDicomImageMetadataCache
         = Collections.synchronizedMap(new LRUMemoryCache<Object, DicomObject>(Config.prop.getI("de.sofd.viskit.rawDicomImageMetadataCacheSize")));
 
     private static LRUMemoryCache<Object, Integer> frameCountByDcmObjectIdCache
         = new LRUMemoryCache<Object, Integer>(Config.prop.getI("de.sofd.viskit.frameCountByDcmObjectIdCacheSize"));
 
     private boolean asyncMode = false;
     
     private static final Logger logger = Logger.getLogger(CachingDicomImageListViewModelElement.class);
 
     static {
         RawDicomImageReader.registerWithImageIO();
     }
 
     private final NumericPriorityThreadPoolExecutor imageFetchingJobsExecutor;
 
     private PrioritizedTask<Object> myBackgroundLoaderTask;
 
     public CachingDicomImageListViewModelElement() {
         this(null, null);
     }
     
     public CachingDicomImageListViewModelElement(NumericPriorityMap<Object, DicomObject> dcmObjectCache, NumericPriorityThreadPoolExecutor imageFetchingJobsExecutor) {
         this.dcmObjectCache = (dcmObjectCache == null ? Config.defaultDcmObjectCache : dcmObjectCache);
         this.imageFetchingJobsExecutor = (imageFetchingJobsExecutor == null ? Config.defaultImageFetchingJobsExecutor : null);
     }
     
     /**
      * Caller must ensure to setInitializationState(UNINITIALIZED) only after getDicomObjectKey() et al. return
      * correct, final values.
      */
     @Override
     public void setInitializationState(InitializationState initializationState) {
         if (initializationState == getInitializationState()) {
             return;
         }
         if (!isAsyncMode() && initializationState == InitializationState.UNINITIALIZED) {
             throw new IllegalStateException("BUG: attempt to set UNINITIALIZED state in synchronous mode");
         }
         super.setInitializationState(initializationState);
         if (initializationState == InitializationState.UNINITIALIZED) {
             //TODO: check whether the element is already loaded and refuse to change to UNINITIALIZED if it is?
             //  Also, what if the background task is loading while the initState is externally set to INITIALIZED?
             //  Shouldn't this method really be called by list classes only (as it currently is)?
             Runnable r = new Runnable() {
                 @Override
                 public void run() {
                     logger.debug("" + getDicomObjectKey() + ": START background loading");
                     try {
                         DicomObject dcmObj;
                         //synchronized (dcmObjectCache) {  // not doing this b/c getBackendDicomObject() may block for a long time...so maybe two threads fetch the same dicom -- not a problem, right?
                         dcmObj = dcmObjectCache.get(getDicomObjectKey());
                         if (dcmObj == null) {
                             dcmObj = getBackendDicomObject();
                             dcmObjectCache.put(getDicomObjectKey(), dcmObj, getInternalEffectivePriority());
                         }
                         //}
         
                         DicomObject dcmMetadata = rawDicomImageMetadataCache.get(getDicomObjectKey());
                         if (null == dcmMetadata) {
                             dcmMetadata = new BasicDicomObject();
                             dcmObj.subSet(0, Tag.PixelData - 1).copyTo(dcmMetadata);
                             rawDicomImageMetadataCache.put(getDicomObjectKey(), dcmMetadata);
                         }
                         
                         logger.debug("" + getDicomObjectKey() + ": DONE background loading");
                         SwingUtilities.invokeLater(new Runnable() {  //TODO: this may create 100s of Runnables in a short time. use our own queue instead?
                             @Override
                             public void run() {
                                 setInitializationState(InitializationState.INITIALIZED);
                             }
                         });
                     } catch (final Exception e) {
                         logger.error("Exception background-loading " + getDicomObjectKey() + ": " +
                                      e.getLocalizedMessage() + ". Setting the model element to permanent error state.", e);
                         //TODO: support the notion of "temporary" errors, for which we would not change the initializationState?
                         SwingUtilities.invokeLater(new Runnable() {
                             @Override
                             public void run() {
                                 setInitializationState(InitializationState.ERROR);
                                 setErrorInfo(e);
                             }
                         });
                     } catch (final Error e) {
                         //Errors are normally fatal, but:
                         //- dcm4che may throw a non-fatal OOM error when trying to read a non-DICOM file (should be mostly fixed in 2.0.21; see http://www.dcm4che.org/jira/browse/DCM-338)
                         //- if we didn't log the error here, it would just be silently eaten by the RunnableFuture, and
                         //  we would have to call get() on the future at a later time (which we don't otherwise have to) just to obtain the exception
                         //Thus we catch any Error exception here, log it, and rethrow it
                         logger.error("ERROR background-loading " + getDicomObjectKey() + ": " +
                                      e.getLocalizedMessage() + ". Setting the model element to permanent error state.", e);
                         SwingUtilities.invokeLater(new Runnable() {
                             @Override
                             public void run() {
                                 setInitializationState(InitializationState.ERROR);
                                 setErrorInfo(e);
                             }
                         });
                         throw e;
                     }
                 }
             };
             myBackgroundLoaderTask = imageFetchingJobsExecutor.submitWithPriority(r, getInternalEffectivePriority());
             logger.debug("" + getDicomObjectKey() + ": QUEUED");
         }
     }
     
     /**
      * Asynchronous mode. When enabled, the initalizationState property may
      * attain the UNINITIALIZED value as long as the image is not cached, and
      * background threads will be pooled to load the images of uninitialized
      * elements.
      */
     public boolean isAsyncMode() {
         return asyncMode;
     }
     
     /**
      * Asynchronous mode. When enabled, the initalizationState property may
      * attain the UNINITIALIZED value as long as the image is not cached, and
      * background threads will be pooled to load the images of uninitialized
      * elements.
      * <p>
      * Asynchronous mode is off by default, and may be enabled or disabled
      * at any time (TODO: really? what about async=>sync changes? would have
      * to synchronously remove myBackgroundLoaderTask from the queue and set
      * the initState to INITIALIZED or ERROR, right?)
      */
     public void setAsyncMode(boolean asyncMode) {
         if (asyncMode == this.asyncMode) {
             return;
         }
         this.asyncMode = asyncMode;
         if (asyncMode && getInitializationState() == InitializationState.INITIALIZED && ! dcmObjectCache.contains(getDicomObjectKey())) {
             setInitializationState(InitializationState.UNINITIALIZED);
             logger.debug("" + getDicomObjectKey() + "=>async and wasn't cached");
         } else if (!asyncMode) {
             //async=>sync change. Need to remove myBackgroundLoaderTask from the queue, waiting for it to finish if necessary
             //  this code is somewhat beta, and will probably rarely be used
             if (null != myBackgroundLoaderTask) {
                 logger.debug("" + getDicomObjectKey() + "=>sync, null != myBLT");
                 if (!imageFetchingJobsExecutor.remove(myBackgroundLoaderTask)) {
                     //myBackgroundLoaderTask no longer queued => either still running or already done
                     try {
                         logger.debug("" + getDicomObjectKey() + "=>sync, job wasn't queued");
                         long t0 = System.currentTimeMillis();
                         if (!myBackgroundLoaderTask.isDone()) {  //test not really necessary, get() will return immediately if isDone()
                             logger.debug("" + getDicomObjectKey() + "=>sync, job wasn't done, get...");
                             myBackgroundLoaderTask.get();
                         }
                         long t1 = System.currentTimeMillis();
                         logger.debug("" + getDicomObjectKey() + "=>sync, done in " + (t1-t0) + " ms");
                     } catch (InterruptedException e) {
                         //shouldn't happen.
                     } catch (ExecutionException e) {
                         //shouldn't happen.
                     }
                 } else {
                     logger.debug("" + getDicomObjectKey() + "=>sync, job unqueued");
                 }
                 
                 ///alternative approach? =>not really. too slow and we don't want to interfere with other lists' usage of the executor
                 //myBackgroundLoaderTask.cancel(false);
                 //myBackgroundLoaderTask.get();
                 //imageFetchingJobsExecutor.purge();
                 if (initializationState == InitializationState.UNINITIALIZED) {
                     initializationState = InitializationState.INITIALIZED;
                 }
                 
                 //TODO: what if other lists display the same elements and are still in async mode? We may be removing their jobs here...
             }
         }
     }
     
     // TODO: frameNumber as c'tor parameter (we can't support later setFrameNumber() calls anyway b/c the keys would change)
     
     /**
      * set the frame number this model element represents in case of a multiframe DICOM object. Initially the first
      * frame is displayed (default). This is also the case if the DICOM object
      * is a singleframe DICOM object
      * 
      * @param frame
      */
     public void setFrameNumber(int frame) {
          int numFrames = getTotalFrameNumber(); 
          if(frame < 0 || frame >= numFrames) {
              throw new IllegalArgumentException("the frame number must be at least 0 and must not exceed "+(numFrames-1) + " (# frames in this DICOM object)");
          }
          this.frameNumber = frame;
     }
    
     @Override
     public int getFrameNumber() {
         return this.frameNumber;
     }
    
     @Override
     public int getTotalFrameNumber() {
         Object dcmKey = getDicomObjectKey();
         Integer cached = frameCountByDcmObjectIdCache.get(dcmKey);
         if (cached == null) {
             if (totalFrameNumber == -1) {
                 cached = doGetTotalFrameNumber();
                 totalFrameNumber = cached;
             } else {
                 cached = totalFrameNumber;
             }
             frameCountByDcmObjectIdCache.put(dcmKey, cached);
         }
         return cached;
     }
     
     protected int doGetTotalFrameNumber() {
         // extract the frame count from the getDicomObject() by default.
         ImageReader reader;
         int numFrames;
         ImageInputStream in;
         try {
             DicomObject dcmObj = getDicomObject();
             ByteArrayOutputStream bos = new ByteArrayOutputStream(200000);
             DicomOutputStream dos = new DicomOutputStream(bos);
             String tsuid = dcmObj.getString(Tag.TransferSyntaxUID);
             if (null == tsuid) {
                 tsuid = UID.ImplicitVRLittleEndian;
             }
             FileMetaInformation fmi = new FileMetaInformation(dcmObj);
             fmi = new FileMetaInformation(fmi.getMediaStorageSOPClassUID(), fmi.getMediaStorageSOPInstanceUID(), tsuid);
             dos.writeFileMetaInformation(fmi.getDicomObject());
             dos.writeDataset(dcmObj, tsuid);
             dos.close();
             
             reader = new DicomImageReaderSpi().createReaderInstance();
             in = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
             if (null == in) {
                 throw new IllegalStateException(
                         "The DICOM image I/O filter (from dcm4che1) must be available to read images.");
             }
             try {
                 reader.setInput(in);
                 numFrames = reader.getNumImages(true);
             } finally {
                 in.close();
             }
         }
         catch (IOException e) {
             throw new IllegalStateException("error reading DICOM object from " + getDicomObjectKey(), e);
         }
         return numFrames;
     }
 
     @Override
     public Object getImageKey() {
         return getDicomObjectKey() + "#" + frameNumber;
     }
     
     /**
      * @return the unique identifier of the DICOM object that this model element's image comes from
      */
     protected abstract Object getDicomObjectKey();
 
     /**
      * Extract from the backend and return the DicomObject. This method should not cache the
      * results or anything like that (this base class will do that), so it may be time-consuming.
      *
      * @return
      */
     protected abstract DicomObject getBackendDicomObject();
 
     /**
      * Same as {@link #getBackendDicomObject() }, but for the image. Default implementation
      * extracts the image from the getBackendDicomObject().
      *
      * @return
      */
     protected BufferedImage getBackendImage() {
         DicomObject dcmObj = getBackendDicomObject();
 
         // extract the BufferedImage from the received imageDicomObject
         ByteArrayOutputStream bos = new ByteArrayOutputStream(200000);
         DicomOutputStream dos = new DicomOutputStream(bos);
 
         try {
             String tsuid = dcmObj.getString(Tag.TransferSyntaxUID);
             if (null == tsuid) {
                 tsuid = UID.ImplicitVRLittleEndian;
             }
 
             FileMetaInformation fmi = new FileMetaInformation(dcmObj);
             fmi = new FileMetaInformation(fmi.getMediaStorageSOPClassUID(), fmi.getMediaStorageSOPInstanceUID(), tsuid);
 
             dos.writeFileMetaInformation(fmi.getDicomObject());
             dos.writeDataset(dcmObj, tsuid);
             dos.close();
 
             Iterator<?> it = ImageIO.getImageReadersByFormatName("RAWDICOM");
             if (!it.hasNext()) {
                 throw new IllegalStateException("The raw DICOM image I/O filter must be available to read images.");
             }
 
             ImageReader reader = (ImageReader) it.next();
             ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
             if (null == in) {
                 throw new IllegalStateException("The raw DICOM image I/O filter must be available to read images.");
             }
             try {
                 reader.setInput(in);
                 BufferedImage bimg = reader.read(0);
                 return bimg;
             } finally {
                 in.close();
             }
         } catch (Exception e) {
             e.printStackTrace();
             throw new IllegalStateException("error trying to extract image from DICOM object", e);
         }
     }
 
     protected FloatRange pixelValuesRange, usedPixelValuesRange;
     
     protected Histogram histogram;
 
     protected FloatRange getFloatRange(BufferedImage bimg, float min, float max, boolean isSigned) {
         Raster raster = bimg.getRaster();
         if (raster.getNumBands() != 1) {
             throw new IllegalArgumentException("source image must be grayscales");
         }
 
         for (int x = 0; x < bimg.getWidth(); x++) {
             for (int y = 0; y < bimg.getHeight(); y++) {
                 int val = raster.getSample(x, y, 0);
                 if (isSigned) {
                     val = (int)(short)val;  // will only work for 16-bit signed...
                 }
 
                 if (val < min) { min = val; }
                 if (val > max) { max = val; }
             }
         }
 
         return new FloatRange(min, max);
     }
 
     protected FloatRange getMaximumPixelRange() {
         FloatRange range = new FloatRange(200, 800);
 
         int pixelType = getPixelType(getDicomImageMetaData());
 
         // TODO: maybe use static multidimensional tables instead of nested switch statements
         switch (pixelType) {
             case RawImage.PIXEL_TYPE_UNSIGNED_BYTE:
                 range = new FloatRange(0, 255);
                 break;
             case RawImage.PIXEL_TYPE_UNSIGNED_12BIT:
                 range = new FloatRange(0, 4095);
                 break;
             case RawImage.PIXEL_TYPE_UNSIGNED_16BIT:
                 range = new FloatRange(0, 65535);
                 break;
             case RawImage.PIXEL_TYPE_SIGNED_12BIT:
                 range = new FloatRange(-2048, 2047);
                 break;
             case RawImage.PIXEL_TYPE_SIGNED_16BIT:
                 range = new FloatRange(-32768, 32767);
                 break;
         }
 
         return range;
     }
 
     @Override
     public DicomObject getDicomObject() {
         DicomObject result = dcmObjectCache.get(getDicomObjectKey());
         if (result == null) {
             if (isAsyncMode()) {
                 throw new NotInitializedException();
             }
             result = getBackendDicomObject();
             dcmObjectCache.put(getDicomObjectKey(), result, getInternalEffectivePriority());
         }
         return result;
     }
 
     public boolean isDicomMetadataCached() {
         return rawDicomImageMetadataCache.containsKey(getDicomObjectKey());
     }
 
     public boolean isDicomObjectCached() {
         return dcmObjectCache.contains(getDicomObjectKey());
     }
 
     public boolean isImageCached() {
         return imageCache.containsKey(getImageKey());
     }
 
     @Override
     public DicomObject getDicomImageMetaData() {
         DicomObject result = rawDicomImageMetadataCache.get(getDicomObjectKey());
         
         if (result == null) {
             if (isAsyncMode()) {
                 throw new NotInitializedException();
             }
             result = getBackendDicomImageMetaData();
             rawDicomImageMetadataCache.put(getDicomObjectKey(), result);
         }
         return result;
     }
 
     /**
      * Same as {@link #getBackendDicomObject() }, but for the DICOM metadata ({@link #getDicomImageMetaData() }).
      * Default implementation extracts the metadata from the getBackendDicomObject().
      *
      * @return
      */
     protected DicomObject getBackendDicomImageMetaData() {
         // even though the getDicomObject() could serve as the metadata object
         // (it's a superset of it -- essentially, it's the metadata plus the pixel data),
         // we extract the metadata subset and return that, because it will be much smaller
         // in terms of memory footprint and thus many more of these objects fit in the
         // dicomMetadataCache. Alternatively, we might also not have a dicomMetadataCache
         // at all and always return the getDicomObject() directly, relying on its cache --
         // but it contains fewer elements, and the returned complete getDicomObject()s
         // would be large and may consume large amounts of heap space depending on how
         // long the caller keeps those DicomObjects referenced
         DicomObject result = new BasicDicomObject();
         getBackendDicomObject().subSet(0, Tag.PixelData - 1).copyTo(result);  // make a deep copy so no reference to the PixelData is kept
         return result;
     }
     
     @Override
     public Histogram getHistogram() {
         if (this.histogram != null)
             return this.histogram;
         
         //init max range
         FloatRange range = getMaximumPixelRange();
         int min = (int)range.getMin();
         int max = (int)range.getMax();
 
         if (!hasRawImage() || !isRawImagePreferable()) {
             BufferedImage bimg = getImage();
             return new Histogram(bimg.getRaster(), new IntRange(min, max));
         }
             
         RawImage img = getRawImage();
 
         if (img.getPixelType() != RawImage.PIXEL_TYPE_UNSIGNED_16BIT) {
             //unsigned 8 bit or 12 bit or signed
             ShortBuffer buf = (ShortBuffer) img.getPixelData();
             return new Histogram(buf, new IntRange(min, max));
         }
 
         //unsigned 16 bit
         IntBuffer buf = (IntBuffer) img.getPixelData();
         this.histogram = new Histogram(buf, new IntRange(min, max));
         
         return this.histogram;
     }
 
     @Override
     public BufferedImage getImage() {
         BufferedImage result = imageCache.get(getImageKey());
         if (result == null) {
             if (isAsyncMode()) {
                //throw new NotInitializedException();
                //TODO: async mode doesn't work right now for the BufferedImage fallback:
                //
                //throwing the above exception would lead to the list/ImagePaintController calling
                //setInitializationState(UNINITIALIZED),
                //which will enqueue the Runnable that loads the DICOM object into the cache, but it does not
                //fill the imageCache. So, on the next call to getImage(), the exception will be rethrown and
                //the process repeats endlessly. As a workaround, we don't throw the exception and thus force
                //sync mode for the BufferedImage fallback.
                //
                //as a real solution, we should, in async mode, create the BufferedImage from the
                //background-loaded DicomObject either here or in the Runnable (after finding that the
                //RawImage isn't available).
                //
                //Best idea is probably to solve this issue along with ticket #37 and the
                //RawImage/BufferedImage unification
             }
             result = getBackendImage();
             imageCache.put(getImageKey(), result);
         }
         return result;
     }
 
     @Override
     public boolean hasRawImage() {
         return null != maybeGetProxyRawImage();
     }
 
     @Override
     public boolean isRawImagePreferable() {
         return hasRawImage();
     }
     
     @Override
     public RawImage getRawImage() {
         RawImageImpl result = (RawImageImpl) getProxyRawImage();
 
         DicomObject dicomObject = getDicomObject();
         int height = dicomObject.getInt(Tag.Columns);
         int width = dicomObject.getInt(Tag.Rows);
 
         if (result.getPixelType() != RawImage.PIXEL_TYPE_UNSIGNED_16BIT) {
             //signed
             short[] shorts = dicomObject.getShorts(Tag.PixelData);
             
             ShortBuffer tmp = ShortBuffer.wrap(shorts);
             tmp.position(height*width*frameNumber);
             result.setPixelData(tmp.slice());
         } else {
             //unsigned int
             int[] ints = dicomObject.getInts(Tag.PixelData);
             IntBuffer tmp = IntBuffer.wrap(ints);
             tmp.position(height*width*frameNumber);
             result.setPixelData(tmp.slice());
         }
         return result;
     }
     
 
     @Override
     public RawImage getProxyRawImage() {
         RawImageImpl result = maybeGetProxyRawImage();
         if (null == result) {
             throw new IllegalStateException("this model element can't provide a raw image");
         }
         return result;
     }
 
     protected RawImageImpl maybeGetProxyRawImage() {
         DicomObject imgMetadata = getDicomImageMetaData();
         
         String transferSyntaxUID = imgMetadata.getString(Tag.TransferSyntaxUID);
         //logger.debug(getImageKey());
         //logger.debug("transferSyntaxUID : " + transferSyntaxUID);
         
         //jpeg or rle compressed
         if (transferSyntaxUID != null && 
                 (transferSyntaxUID.startsWith("1.2.840.10008.1.2.4") ||
                  transferSyntaxUID.startsWith("1.2.840.10008.1.2.5")))
             return null;
         
         int pixelType = getPixelType(imgMetadata);
         if (pixelType == RawImage.PIXEL_TYPE_NOT_SUPPORTED)
             return null;
 
         int pixelFormat = getPixelFormat(imgMetadata);
         if (pixelFormat == RawImage.PIXEL_FORMAT_NOT_SUPPORTED)
             return null;
 
         int width = imgMetadata.getInt(Tag.Columns);
         int height = imgMetadata.getInt(Tag.Rows);
         
         return new RawImageImpl(width, height, pixelFormat, pixelType, null);
     }
 
     @Override
     public FloatRange getPixelValuesRange() {
         if (null == pixelValuesRange) {
             setPixelValuesRange();
         }
         return pixelValuesRange;
     }
 
     protected void setPixelValuesRange() {
         pixelValuesRange = getMaximumPixelRange();
     }
 
     @Override
     public FloatRange getUsedPixelValuesRange() {
         if (null == usedPixelValuesRange) {
             setUsedPixelValuesRange();
         }
         return usedPixelValuesRange;
     }
 
     protected void setUsedPixelValuesRange() {
         DicomObject metadata = getDicomImageMetaData();
 
         boolean isSigned = (1 == metadata.getInt(Tag.PixelRepresentation));
 
         //init max range
         FloatRange range = getMaximumPixelRange();
         float min = range.getMin();
         float max = range.getMax();
 
         if (metadata.contains(Tag.SmallestImagePixelValue) && metadata.contains(Tag.LargestImagePixelValue)) {
             min = metadata.getInt(Tag.SmallestImagePixelValue);
             max = metadata.getInt(Tag.LargestImagePixelValue);
         } else if (hasRawImage() && isRawImagePreferable()) {
             min = range.getMax();
             max = range.getMin();
             
             RawImage img = getRawImage();
             int pxCount = (img.getPixelFormat() == RawImage.PIXEL_FORMAT_RGB ? 3 : 1) * img.getWidth() * img.getHeight();
 
             if (img.getPixelType() != RawImage.PIXEL_TYPE_UNSIGNED_16BIT) {
                 //unsigned 8 bit or 12 bit or signed
                 ShortBuffer buf = (ShortBuffer) img.getPixelData();
                 for (int i = 0; i < pxCount; i++) {
                     short val = buf.get(i);
                     if (val < min) { min = val; }
                     if (val > max) { max = val; }
                 }
             } else {
                 min = range.getMax();
                 max = range.getMin();
                 
                 //unsigned 16 bit
                 IntBuffer buf = (IntBuffer) img.getPixelData();
                 for (int i = 0; i < pxCount; i++) {
                     int val = buf.get(i);
                     if (val < min) { min = val; }
                     if (val > max) { max = val; }
                 }
             }
         } else {
             BufferedImage bimg = getImage();
 
             if (bimg.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY) {
 
                 min = range.getMax();
                 max = range.getMin();
                 FloatRange range2 = getFloatRange(bimg, min, max, isSigned);
                 min = range2.getMin();
                 max = range2.getMax();
             }
             /*min = 200;
             max = 800;*/
         }
 
         if (metadata.contains(Tag.RescaleSlope) && metadata.contains(Tag.RescaleIntercept)) {
             float rscSlope = metadata.getFloat(Tag.RescaleSlope);
             float rscIntercept = metadata.getFloat(Tag.RescaleIntercept);
             min = (int) (rscSlope * min + rscIntercept);
             max = (int) (rscSlope * max + rscIntercept);
         }
         
         usedPixelValuesRange = new FloatRange(min, max);
     }
 
     protected int getPixelFormat(DicomObject dicomObject) {
         int bitsAllocated = dicomObject.getInt(Tag.BitsAllocated);
 
         int pixelFormat = (bitsAllocated == 16 ? RawImage.PIXEL_FORMAT_LUMINANCE : RawImage.PIXEL_FORMAT_NOT_SUPPORTED);
 
         return pixelFormat;
     }
 
     protected int getPixelType(DicomObject dicomObject) {
         int bitsAllocated = dicomObject.getInt(Tag.BitsAllocated);
         if (bitsAllocated <= 0) {
             return RawImage.PIXEL_TYPE_NOT_SUPPORTED;
         }
 
         int bitsStored = dicomObject.getInt(Tag.BitsStored);
         if (bitsStored <= 0) {
             return RawImage.PIXEL_TYPE_NOT_SUPPORTED;
         }
         boolean isSigned = (1 == dicomObject.getInt(Tag.PixelRepresentation));
         // TODO: return RawImage.PIXEL_TYPE_NOT_SUPPORTED; if compressed
         // TODO: support for RGB (at least don't misinterpret it as luminance)
         // TODO: account for endianness (Tag.HighBit)
         int pixelType;
         // TODO: maybe use static multidimensional tables instead of nested switch statements
 
         switch (bitsAllocated) {
             case 8:
                 if (bitsStored == 8 && !isSigned)
                     return RawImage.PIXEL_TYPE_UNSIGNED_BYTE;
                 
                 return RawImage.PIXEL_TYPE_NOT_SUPPORTED;
             case 16:
                 switch (bitsStored) {
                     case 12:
                         pixelType = (isSigned ? RawImage.PIXEL_TYPE_SIGNED_12BIT : RawImage.PIXEL_TYPE_UNSIGNED_12BIT);
                         break;
                     case 16:
                         pixelType = (isSigned ? RawImage.PIXEL_TYPE_SIGNED_16BIT : RawImage.PIXEL_TYPE_UNSIGNED_16BIT);
                         break;
                     default:
                         return RawImage.PIXEL_TYPE_NOT_SUPPORTED;
                 }
                 break;
             default:
                 return RawImage.PIXEL_TYPE_NOT_SUPPORTED;
         }
 
         return pixelType;
     }
     
     @Override
     public void setPriority(Object source, double value) {
         super.setPriority(source, value);
         double internalPrio = getInternalEffectivePriority();
         if (myBackgroundLoaderTask != null) {
             try {
                 myBackgroundLoaderTask = imageFetchingJobsExecutor.resubmitWithPriority(myBackgroundLoaderTask, internalPrio);
             } catch (IllegalArgumentException e) {
                 //myBackgroundLoaderTask isn't currently running or waiting -- no error.
             }
         }
         dcmObjectCache.setPriority(getDicomObjectKey(), internalPrio);
     }
     
     /**
      * Effective priority value to be used for the cache and executor. In those, 0
      * is the highest priority and 10 is the lowest.
      * 
      * @return
      */
     protected double getInternalEffectivePriority() {
         return 10 - getEffectivePriority();
     }
     
     @Override
     public String toString() {
         return super.toString() + ": " + getImageKey();
     }
 
 }
