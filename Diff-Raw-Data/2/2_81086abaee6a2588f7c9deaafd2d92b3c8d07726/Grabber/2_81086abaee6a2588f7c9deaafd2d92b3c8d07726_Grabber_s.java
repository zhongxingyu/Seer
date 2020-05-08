 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package fr.prima.gsp.demo;
 
 import com.sun.jna.Native;
 import com.sun.jna.Pointer;
 import fr.prima.gsp.framework.Assembly;
 import fr.prima.gsp.framework.ModuleParameter;
 import fr.prima.gsp.framework.spi.AbstractModuleEnablable;
 import fr.prima.videoserviceclient.BufferedImageSourceListener;
 import java.awt.image.BufferedImage;
 import java.awt.image.WritableRaster;
 import java.nio.ByteBuffer;
 
 /**
  *
  * @author emonet
  */
 public class Grabber extends AbstractModuleEnablable implements BufferedImageSourceListener {
     
     @ModuleParameter(initOnly=true)
     public Assembly assembly;
 
     public void stopped() {
         // BufferedImageSourceListener method
     }
 
     public synchronized void bufferedImageReceived(BufferedImage image, ByteBuffer imageDataOrNull) {
         if (!isEnabled()) return;
         tick();
         output(image);
         Pointer imageDataPointer;
         if (imageDataOrNull != null && imageDataOrNull.isDirect()) {
             imageDataPointer = Native.getDirectBufferPointer(imageDataOrNull);
         } else {
             WritableRaster raster = image.getRaster();
             int bufferSize = raster.getWidth() * raster.getHeight() * raster.getNumDataElements();
             ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
             buffer.put((byte[]) raster.getDataElements(0, 0, raster.getWidth(), raster.getHeight(), null));
             imageDataPointer = Native.getDirectBufferPointer(buffer);
         }
        outputRaw(imageDataPointer, image.getWidth(), image.getWidth(), image.getHeight(), 24);
     }
     // Type = 24 for BGR
     private void outputRaw(Pointer data, int w, int h, int widthStep, int type) {
         emitEvent(data, w, h, widthStep, type);
     }
     private void output(BufferedImage im) {
         emitEvent(im);
     }
     private void tick() {
         emitEvent();
     }
 
     // helpers for pipeline destruction
     public void closePipeline() {assembly.stop();}
     public void closePipeline1(Object o1) {assembly.stop();}
     public void closePipeline2(Object o1, Object o2) {assembly.stop();}
     public void closePipeline3(Object o1, Object o2, Object o3) {assembly.stop();}
     public void closePipeline4(Object o1, Object o2, Object o3, Object o4) {assembly.stop();}
 
 }
