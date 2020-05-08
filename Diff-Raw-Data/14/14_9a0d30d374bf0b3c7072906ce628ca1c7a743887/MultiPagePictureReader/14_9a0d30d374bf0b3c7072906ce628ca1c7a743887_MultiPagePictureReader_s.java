 package de.cismet.tools.gui;
 
 import com.sun.media.jai.codec.FileSeekableStream;
 import com.sun.media.jai.codec.ImageCodec;
 import com.sun.media.jai.codec.ImageDecoder;
 import com.sun.media.jai.codec.SeekableStream;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.awt.image.RenderedImage;
 import java.lang.ref.SoftReference;
 import javax.media.jai.RenderedImageAdapter;
 
 public class MultiPagePictureReader {
 
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MultiPagePictureReader.class);
     private static final String CODEC_JPEG = "jpeg";
     private static final String CODEC_TIFF = "tiff";
     private final ImageDecoder decoder;
     private final int pageCount;
     private final SoftReference<BufferedImage>[] cache;
 
     private final String getCodecString(File imageFile) {
         final String filename = imageFile.getName().toLowerCase();
         final String extension = filename.substring(filename.lastIndexOf(".") + 1);
         if (extension.matches("(tiff|tif)")) {
             return CODEC_TIFF;
         } else if (extension.matches("(jpg|jpeg)")) {
             return CODEC_JPEG;
         }
         return null;
     }
 
     private final BufferedImage getFormCache(int position) {
         final SoftReference<BufferedImage> cacheItem = cache[position];
         if (cacheItem != null) {
             return cacheItem.get();
         }
         return null;
     }
 
     private final void addToCache(int position, BufferedImage image) {
         final SoftReference<BufferedImage> newCacheItem = new SoftReference<BufferedImage>(image);
         cache[position] = newCacheItem;
     }
 
     public MultiPagePictureReader(File imageFile) throws IOException {
         if (imageFile != null && imageFile.isFile()) {
             final String codec = getCodecString(imageFile);
             if (codec != null) {
                 final SeekableStream ss = new FileSeekableStream(imageFile);
                 decoder = ImageCodec.createImageDecoder(codec, ss, null);
                 pageCount = decoder.getNumPages();
                 cache = new SoftReference[pageCount];
                 for (int i = 0; i < cache.length; ++i) {
                     cache[i] = new SoftReference<BufferedImage>(null);
                 }
             } else {
                 throw new IOException("Unsupported filetype: " + imageFile.getAbsolutePath() + " is not a tiff or jpeg file!");
             }
         } else {
             throw new IOException("Could not open file: " + imageFile);
         }
     }
 
     public final int getNumberOfPages() throws IOException {
         return pageCount;
     }
 
     public final BufferedImage loadPage(int page) throws IOException {
         if (page > -1 && page < pageCount) {
             BufferedImage result = getFormCache(page);
             if (result == null) {
                 final RenderedImage renderImage = decoder.decodeAsRenderedImage(page);
                 final RenderedImageAdapter imageAdapter = new RenderedImageAdapter(renderImage);
                 result = imageAdapter.getAsBufferedImage();
                addToCache(page, result);
             }
             return result;
         } else {
             throw new IOException("Could not find page " + page + " in file. Range is [0.." + (pageCount - 1) + "].");
         }
     }
 
     public final void close() {
         try {
             decoder.getInputStream().close();
         } catch (IOException ex) {
             log.warn(ex, ex);
         }
     }
 }
 
