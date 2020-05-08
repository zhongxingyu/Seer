 package de.sofd.viskit.test;
 
 import de.sofd.draw2d.Drawing;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map.Entry;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReader;
 import javax.imageio.stream.ImageInputStream;
 import org.dcm4che2.data.DicomObject;
 import org.dcm4che2.data.Tag;
 import org.dcm4che2.data.UID;
 import org.dcm4che2.io.DicomOutputStream;
 import org.dcm4che2.media.FileMetaInformation;
 
 /**
  * Implements getDicomObject(), getImage() as caching delegators to the (subclass-provided)
  * new methods getBackendDicomObjectKey(), getBackendDicomObject().
  *
  * @author olaf
  */
 public abstract class CachingDicomImageListViewModelElement implements DicomImageListViewModelElement {
 
     /**
      * Returns a key that uniquely identifies the DicomObject returned by getBackendDicomObject().
      * The key will be used for caching those backend DicomObjects.
      * This key should be constant under equals()/hashCode() throughout the lifetime of <i>this</i>. This method will
      * be called often, so it should operate quickly (as opposed to getBackendDicomObject()). It
      * should work without calling getBackendDicomObject().
      *
      * @return
      */
     protected abstract Object getBackendDicomObjectKey();
 
     /**
      * Extract from the backend and return the DicomObject. This method should not cache the
      * results or anything like that (this base class will do that), so it may be time-consuming.
      *
      * @return
      */
     protected abstract DicomObject getBackendDicomObject();
 
     /**
      * Same as {@link #getBackendDicomObject() }, but for the image. Default implementation
      * extracts the image from the getDicomObject().
      *
      * @return
      */
     protected BufferedImage getBackendImage() {
         Iterator it = ImageIO.getImageReadersByFormatName("DICOM");
         if (!it.hasNext()) {
             throw new IllegalStateException("The DICOM image I/O filter (from dcm4che1) must be available to read images.");
         }
 
        DicomObject dcmObj = getDicomObject();
 
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
 
             ImageReader reader = (ImageReader) it.next();
             ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
             if (null == in) {
                 throw new IllegalStateException("The DICOM image I/O filter (from dcm4che1) must be available to read images.");
             }
             try {
                 reader.setInput(in);
                 return reader.read(0);
             } finally {
                 in.close();
             }
         } catch (IOException e) {
             throw new IllegalStateException("error trying to extract image from DICOM object", e);
         }
     }
 
     private final Drawing roiDrawing = new Drawing();
 
 
     // TODO: use a utility library for the cache
 
     private static class LRUMemoryCache<K,V> extends LinkedHashMap<K,V> {
         private final int maxSize;
         public LRUMemoryCache(int maxSize) {
             this.maxSize = maxSize;
         }
         @Override
         protected boolean removeEldestEntry(Entry<K,V> eldest) {
             return this.size() > maxSize;
         }
     }
 
     // TODO: unify the two caches into one
 
     private static LRUMemoryCache<Object, DicomObject> rawDcmObjectCache
         = new LRUMemoryCache<Object, DicomObject>(50);
 
     private static LRUMemoryCache<Object, BufferedImage> rawImageCache
         = new LRUMemoryCache<Object, BufferedImage>(50);
 
 
 
     @Override
     public DicomObject getDicomObject() {
         DicomObject result = rawDcmObjectCache.get(getBackendDicomObjectKey());
         if (result == null) {
             result = getBackendDicomObject();
             rawDcmObjectCache.put(getBackendDicomObjectKey(), result);
         }
         return result;
     }
 
     @Override
     public BufferedImage getImage() {
         BufferedImage result = rawImageCache.get(getBackendDicomObjectKey());
         if (result == null) {
             result = getBackendImage();
             rawImageCache.put(getBackendDicomObjectKey(), result);
         }
         return result;
     }
 
     @Override
     public Drawing getRoiDrawing() {
         return roiDrawing;
     }
 
 }
