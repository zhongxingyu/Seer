 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.heeere.gsp.gstreamer;
 
 import com.heeere.gsp.gstreamer.utils.ImageImport;
 import com.heeere.gsp.gstreamer.utils.ImageUtils;
 import com.heeere.gsp.gstreamer.utils.RGBDataAppSink;
 import fr.prima.gsp.framework.ModuleParameter;
 import fr.prima.gsp.framework.spi.AbstractModuleEnablable;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.ByteBuffer;
 import java.util.concurrent.BlockingDeque;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.gstreamer.*;
 import org.gstreamer.elements.FakeSink;
 import org.gstreamer.elements.PlayBin2;
 
 /**
  *
  * @author twilight
  */
 public class ImageSource extends AbstractModuleEnablable {
 
     @ModuleParameter
     public String uri = null;
     @ModuleParameter
     public int skip = 0;
     @ModuleParameter
     public int skipAtInit = 0;
     @ModuleParameter
     public boolean doRgb = false;
     @ModuleParameter
     public int width = -1;
     @ModuleParameter
     public int height = -1;
     @ModuleParameter
     public boolean preserveAspectRatio = true;
     @ModuleParameter
     public boolean rgbInsteadOfBgr = false; // by default we output bgr images as it is the preferred thing for opencv
     @ModuleParameter
     public String cameraFileNameFormat = "/dev/video%d";
     //
     private int remainToSkip = 0; // to skip in the beginning     // IMPR: could use a seek here
     private int currentFrame = 0;
     private Pipeline pipe;
     private boolean firstTime = true;
     private BlockingDeque<BufferedImage> queue = new LinkedBlockingDeque<BufferedImage>(2);
     private BlockingDeque<ByteBufferAndSize> rgbQueue = new LinkedBlockingDeque<ByteBufferAndSize>(3);
     private static final BufferedImage qEnd = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
 
     private boolean isInteger(String dev) {
         try {
             Integer.parseInt(dev);
             return true;
         } catch (NumberFormatException e) {
             return false;
         }
     }
 
     private static class ByteBufferAndSize {
 
         ByteBuffer byteBuffer;
         int width;
         int height;
 
         public ByteBufferAndSize(ByteBuffer byteBuffer, int width, int height) {
             this.byteBuffer = ByteBuffer.allocateDirect(width * height * 3);
             this.byteBuffer.put(byteBuffer);
             this.width = width;
             this.height = height;
         }
     }
 
     @Override
     protected void initModule() {
         if (this.width==-1 && this.height!=-1) {
             System.err.println("");
             String msg = "In ImageSource, cannot specify only the height (you can specify the width or both but not just the height)";
             System.err.println("ERROR: "+msg);
             throw new IllegalArgumentException(msg);
         }
         this.remainToSkip = skipAtInit;
         String name = "ImageSourceForGSP";
         Gst.init(name, new String[]{});
         FakeSink audio = (FakeSink) ElementFactory.make("fakesink", "audio-sink");
         RGBDataAppSink video = new RGBDataAppSink("rgbsink", width, height, preserveAspectRatio, rgbInsteadOfBgr, new RGBDataAppSink.Listener() {
 
             @Override
             public void rgbFrame(int width, int height, ByteBuffer rgb) {
                 if (remainToSkip > 0) {
                     remainToSkip--;
                     return;
                 }
                 if (currentFrame % (skip + 1) != 0) {
                     currentFrame++;
                     return;
                 }
                 currentFrame++;
                 int widthStep = width * 3;
                 BufferedImage wrapper = ImageImport.createBufferedImage(width, height, widthStep, 3, new int[]{2, 1, 0}, rgb);
                 BufferedImage bi = ImageUtils.createOptimized(width, height);
                 Graphics2D g = bi.createGraphics();
                 g.drawImage(wrapper, 0, 0, null);
                 g.dispose();
                 if (doRgb) {
                     try {
                         rgbQueue.put(new ByteBufferAndSize(rgb, width, height));
                     } catch (InterruptedException ex) {
                         Logger.getLogger(ImageSource.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
                 try {
                     queue.put(bi);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(ImageSource.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
         if (uri.startsWith("camera:")) {
             String dev = uri.replaceFirst("^camera:(//)?", "");
             if (isInteger(dev)) {
                 dev = String.format(cameraFileNameFormat, Integer.parseInt(dev));
             }
             pipe = new Pipeline("CameraSource");
             //System.err.println("Trying to use a v4l2 camera '" + dev + "'");
             Element e = ElementFactory.make("v4l2src", "V4L2SrcSource");
             e.set("device", dev);
             pipe.addMany(e, video);
             Pipeline.linkMany(e, video);
         } else if (uri.contains(":")) {
             PlayBin2 p = new PlayBin2(name);
             pipe = p;
             try {
                 p.setURI(new URI(uri));
             } catch (URISyntaxException ex) {
                 Logger.getLogger(ImageSource.class.getName()).log(Level.SEVERE, null, ex);
             }
             p.setAudioSink(audio);
             p.setVideoSink(video);
         } else {
             PlayBin2 p = new PlayBin2(name);
             pipe = p;
             p.setInputFile(new File(uri));
             p.setAudioSink(audio);
             p.setVideoSink(video);
         }
         pipe.getBus().connect(new Bus.ERROR() {
 
             public void errorMessage(GstObject go, int i, String message) {
                 System.err.println("GSTREAMER ERROR: " + message);
                 try {
                     queue.put(qEnd);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(ImageSource.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
         pipe.getBus().connect(new Bus.EOS() {
 
             @Override
             public void endOfStream(GstObject go) {
                 try {
                     queue.put(qEnd);
                     pipe.stop();
                 } catch (InterruptedException ex) {
                     Logger.getLogger(ImageSource.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
         video.setPassDirectBuffer(true);
         pipe.pause();
         pipe.getState();
     }
 
     @Override
     protected void stopModule() {
         if (pipe != null) {
             new Thread(new Runnable() {
 
                 public void run() {
                     pipe.stop();
                 }
             }).start();
             do {
                 queue.clear();
                 rgbQueue.clear();
             } while (pipe.getState(100) != State.NULL);
         }
     }
 
     public void input() {
         if (!isEnabled()) {
             return;
         }
         if (firstTime) {
             pipe.play();
             pipe.getState();
             firstTime = false;
         }
         try {
             lastOutput = queue.take();
             if (lastOutput == qEnd) {//queue.isEmpty() && pipe.queryPosition().toMillis() >= pipe.queryDuration().toMillis()) {
                 lastOutput = null;
                 end();
                 return;
             }
             if (doRgb) {
                 ByteBufferAndSize bbs = rgbQueue.take();
                 rgb(bbs.byteBuffer, bbs.width, bbs.height);
             }
             output(lastOutput);
         } catch (InterruptedException ex) {
             Logger.getLogger(ImageSource.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     private void end() {
         emitEvent();
     }
 
     private void output(BufferedImage lastOutput) {
         emitEvent(lastOutput);
     }
 
     private void rgb(ByteBuffer byteBuffer, int width, int height) {
         emitEvent(byteBuffer, width, height);
     }
     //
     // for non module usage
     //
     private BufferedImage lastOutput; // null if no "input()" call yet or if ended
 
     public void initSource() {
         initModule();
     }
 
     public BufferedImage getLastOutput() {
         return lastOutput;
     }
 }
