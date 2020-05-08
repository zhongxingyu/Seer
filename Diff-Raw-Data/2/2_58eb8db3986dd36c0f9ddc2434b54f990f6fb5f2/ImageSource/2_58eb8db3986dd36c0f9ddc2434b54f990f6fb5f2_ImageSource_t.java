 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.heeere.gsp.gstreamer;
 
 import com.heeere.gsp.gstreamer.utils.RGBDataAppSink;
 import com.heeere.gsp.gstreamer.utils.ImageImport;
 import fr.prima.gsp.framework.ModuleParameter;
 import fr.prima.gsp.framework.spi.AbstractModuleEnablable;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.nio.ByteBuffer;
 import java.util.concurrent.BlockingDeque;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.gstreamer.Bus;
 import org.gstreamer.ElementFactory;
 import org.gstreamer.Gst;
 import org.gstreamer.GstObject;
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
     //
     private int remainToSkip = 0; // to skip in the beginning     // IMPR: could use a seek here
     private int currentFrame = 0;
     private PlayBin2 pipe;
     private boolean firstTime = true;
     private BlockingDeque<BufferedImage> queue = new LinkedBlockingDeque<BufferedImage>(2);
     private static final BufferedImage qEnd = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
 
     @Override
     protected void initModule() {
         this.remainToSkip = skipAtInit;
         String name = "ImageSourceForGSP";
         Gst.init(name, new String[]{});
         pipe = new PlayBin2(name);
         pipe.setInputFile(new File(uri));
         FakeSink audio = (FakeSink) ElementFactory.make("fakesink", "audio-sink");
         RGBDataAppSink video = new RGBDataAppSink("rgbsink", new RGBDataAppSink.Listener() {
 
             @Override
             public void rgbFrame(int width, int height, ByteBuffer rgb) {
                 if (remainToSkip > 0) {
                     remainToSkip--;
                     return;
                 }
                 if (currentFrame % (skip+1) != 0) {
                     currentFrame++;
                     return;
                 }
                 currentFrame++;
                 int widthStep = width * 4;
                BufferedImage bi = ImageImport.createBufferedImage(width, height, widthStep, 4, new int[]{2, 1, 0}, rgb);
                 try {
                     queue.put(bi);
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
                 } catch (InterruptedException ex) {
                     Logger.getLogger(ImageSource.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
         //video.setPassDirectBuffer(true);
         pipe.setAudioSink(audio);
         pipe.setVideoSink(video);
         pipe.pause();
         pipe.getState();
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
     //
     // for non module usage
     //
     private BufferedImage lastOutput; // null if no "input()" call yet or if ended
 
     public void initSource(){
         initModule();
     }
 
     public BufferedImage getLastOutput() {
         return lastOutput;
     }
 }
