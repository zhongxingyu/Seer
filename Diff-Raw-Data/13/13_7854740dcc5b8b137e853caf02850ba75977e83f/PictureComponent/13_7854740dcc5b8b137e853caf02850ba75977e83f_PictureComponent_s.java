 package com.tngtech.internal.perceptual.components;
 
 import com.google.common.collect.Sets;
 import com.tngtech.internal.perceptual.PerceptualPipeline;
 import com.tngtech.internal.perceptual.data.events.PictureData;
 import com.tngtech.internal.perceptual.listeners.PictureListener;
 
 import java.awt.*;
 import java.awt.image.*;
 import java.util.Set;
 
 public class PictureComponent implements PerceptualQueryComponent {
 
     private final Set<PictureListener> pictureListeners;
 
     private boolean pictureAvailable;
     private int[] pictureDimensions;
     private int[] pictureBuffer;
 
     private short[] depth;
 
     public PictureComponent() {
         pictureListeners = Sets.newLinkedHashSet();
 
         pictureDimensions = new int[2];
         pictureBuffer = null;
 
         depth = new short[640 * 480];
     }
 
     @Override
     public void queryFeatures(PerceptualPipeline pipeline) {
         if (!isPictureNeeded()) {
             return;
         }
 
         if (isSizeAvailable()) {
             queryPicture(pipeline);
         } else {
             querySize(pipeline);
         }
     }
 
     private void querySize(PerceptualPipeline pipeline) {
 
         pictureAvailable = pipeline.QueryRGBSize(pictureDimensions);
     }
 
     private void queryPicture(PerceptualPipeline pipeline) {
         pictureAvailable = pipeline.QueryRGB(pictureBuffer);
     }
 
     private boolean isPictureNeeded() {
         return pictureListeners.size() != 0;
     }
 
     private boolean isSizeAvailable() {
         return pictureBuffer != null;
     }
 
     @Override
     public void processFeatures() {
         if (!isPictureNeeded() || !pictureAvailable) {
             return;
         }
 
         if (!isSizeAvailable()) {
             determineSize();
         } else {
             determinePicture();
         }
     }
 
     private void determineSize() {
         pictureBuffer = new int[pictureDimensions[0] * pictureDimensions[1]];
     }
 
     private void determinePicture() {
         BufferedImage image = getBufferedImage(pictureBuffer);
         invokePictureListeners(new PictureData(image));
     }
 
     private BufferedImage getBufferedImage(int[] pictureBuffer) {
        int[] bitMasks = new int[]{0xFF0000, 0xFF00, 0xFF, 0xFF000000};
        SinglePixelPackedSampleModel sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, pictureDimensions[0], pictureDimensions[1], bitMasks);
        DataBufferInt db = new DataBufferInt(pictureBuffer, pictureBuffer.length);
        WritableRaster wr = Raster.createWritableRaster(sm, db, new Point());
        BufferedImage image = new BufferedImage(ColorModel.getRGBdefault(), wr, false, null);

        return image;
     }
 
     public void addPictureListener(PictureListener pictureListener) {
         if (!pictureListeners.contains(pictureListener)) {
             pictureListeners.add(pictureListener);
         }
     }
 
     public void removePictureListener(PictureListener pictureListener) {
         if (pictureListeners.contains(pictureListener)) {
             pictureListeners.remove(pictureListener);
         }
     }
 
     private void invokePictureListeners(PictureData data) {
         for (PictureListener listener : pictureListeners) {
             listener.onImage(data);
         }
     }
 }
