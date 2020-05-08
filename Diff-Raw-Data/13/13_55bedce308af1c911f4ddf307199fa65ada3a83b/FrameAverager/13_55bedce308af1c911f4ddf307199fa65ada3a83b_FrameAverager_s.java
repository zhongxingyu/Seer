 package edu.mbl.cdp.frameaverage;
 
 /*
  * Copyright © 2009 – 2013, Marine Biological Laboratory
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice, 
  * this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice, 
  * this list of conditions and the following disclaimer in the documentation 
  * and/or other materials provided with the distribution.
 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND 
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
  * IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
  * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * The views and conclusions contained in the software and documentation are those of 
  * the authors and should not be interpreted as representing official policies, 
  * either expressed or implied, of any organization.
  */
 import java.util.prefs.Preferences;
 import javax.swing.JFrame;
 import mmcorej.CMMCore;
 import mmcorej.TaggedImage;
 import org.micromanager.MMOptions;
 import org.micromanager.acquisition.AcquisitionWrapperEngine;
 import org.micromanager.api.DataProcessor;
 import org.micromanager.internalinterfaces.AcqSettingsListener;
 import org.micromanager.utils.ReportingUtils;
 
 /**
  * FrameAverager
  *
  * @author Amitabh Verma, averma@mbl.edu
  * @author Grant Harris, gharris@mbl.edu at the Marine Biological Laboratory,
  * Woods Hole, Mass.
  */
 public class FrameAverager {
 
     static final String METADATAKEY = "FramesAveraged";
     CMMCore core_;
     AcquisitionWrapperEngine engineWrapper_;
 //    FrameAveragerRunnable runnable;
     FrameAveragerProcessor processor;
     private FrameAveragerControls controlFrame_;
     
     public boolean debugLogEnabled_ = false;
     private boolean enabled_;
     int numberFrames;
     int[] avoidDisplayChs_ = null;
     int[] avoidEngineChs_ = null;
     // shared...
     public TaggedImage[] taggedImageArray = null;
 
     public FrameAverager(AcquisitionWrapperEngine engineWrapper, CMMCore core) {
 
         engineWrapper_ = engineWrapper;
         core_ = core;
         // 4 frames by default
         numberFrames = 4;
         setNumberFrames(numberFrames);
         getDebugOptions();
     }
     
     public void UpdateEngineAndCore() {
         engineWrapper_ = TaggedFrameAverager.getAcquisitionWrapperEngine();
         core_ = TaggedFrameAverager.getCMMCore();
     }
     
     public void getDebugOptions() {
         Preferences root = Preferences.userNodeForPackage(MMOptions.class);
         Preferences prefs = root.node(root.absolutePath() + "/" + "MMOptions");      
         debugLogEnabled_ = prefs.getBoolean("DebugLog", debugLogEnabled_);
     }
 
     public void attachRunnable() {
         Runnable setPauseAndAcquire = new FrameAveragerRunnable(this);
         // The runnable is attached to the channels for which there will be frame averaging.
         // 'Engine channels' to avoid are not included.
         if (avoidEngineChs_ == null) {
             engineWrapper_.attachRunnable(-1, -1, -1, -1, setPauseAndAcquire); // t, p, s, c
        } else {
             for (int i = 0; i < engineWrapper_.getChannels().size(); i++) {
                 for (int j = 0; j < avoidEngineChs_.length; j++) {
                    if (i != avoidEngineChs_[j]) {
                        engineWrapper_.attachRunnable(-1, -1, -1, i, setPauseAndAcquire);
                     }
                 }
             }
         }
         if (debugLogEnabled_) {
             ReportingUtils.logMessage("FrameAvg: runnable attached");
         }
     }
 
     public int getNumberFrames() {
         return numberFrames;
     }
 
     public void setNumberFrames(int numberFrames) {
         this.numberFrames = numberFrames;
         bufferArraySpace();
     }
 
     public void bufferArraySpace() {
         taggedImageArray = new TaggedImage[numberFrames];            
     }
 
     // Diplay channels include Engine channels and Virtual channels
     public void setAvoidDisplayChannels(int[] channelsToAvoid) {
         this.avoidDisplayChs_ = channelsToAvoid;
     }
 
     // Specifies channels 
     public void setAvoidEngineChannels(int[] channelsToAvoid) {
         this.avoidEngineChs_ = channelsToAvoid;
     }
 
     public void enable(boolean enableAveraging) {
         if (enableAveraging) {
             startProcessor();
             attachRunnable();
             enabled_ = true;
         } else {
             // Disable
             stopAndClearProcessor();
             stopAndClearRunnable();
             enabled_ = false;
         }
     }
 
     public void startProcessor() {
         try {
             attachDataProcessor();
         } catch (Exception ex) {
         }
     }
 
     public void attachDataProcessor() {
         processor = new FrameAveragerProcessor(this, core_);
         engineWrapper_.addImageProcessor(processor);
     }
 
     public void stopAndClearProcessor() {
         if (processor != null) {
             processor.requestStop();
         }
         try {
             engineWrapper_.removeImageProcessor(processor.getDataProcessor());
             
             if (debugLogEnabled_) {
                 ReportingUtils.logMessage("FrameAvg: processor removed");
             }
         } catch (Exception ex) {
         }
     }
 
     public void stopAndClearRunnable() {
         try {
             engineWrapper_.clearRunnables();  // Potentially dangerous - needs specific clearRunnable(X)
             
             if (debugLogEnabled_) {
                 ReportingUtils.logMessage("FrameAvg: runnable removed");
             }
         } catch (Exception ex) {
         }
     }
 
     public DataProcessor<TaggedImage> getDataProcessor() {
         return processor.getDataProcessor();
     }
 
     public JFrame getControlFrame() {
         if (controlFrame_ == null) {
             controlFrame_ = new FrameAveragerControls(this);
         }
         return controlFrame_;
     }
 
 }
