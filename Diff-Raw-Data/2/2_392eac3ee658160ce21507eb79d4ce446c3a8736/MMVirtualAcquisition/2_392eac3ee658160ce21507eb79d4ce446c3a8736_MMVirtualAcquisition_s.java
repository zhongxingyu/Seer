 package org.micromanager.acquisition;
 
 import ij.CompositeImage;
 import ij.ImagePlus;
 import ij.gui.ImageWindow;
 import ij.process.ImageStatistics;
 import java.awt.Color;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import mmcorej.TaggedImage;
 import org.json.JSONObject;
 import org.micromanager.metadata.AcquisitionData;
 import org.micromanager.utils.JavaUtils;
 import org.micromanager.utils.MDUtils;
 import org.micromanager.utils.MMScriptException;
 import org.micromanager.utils.NumberUtils;
 import org.micromanager.utils.ReportingUtils;
 
 /**
  *
  * @author arthur
  */
 public class MMVirtualAcquisition implements AcquisitionInterface {
 
    private final String dir_;
    private final String name_;
    MMImageCache imageCache_;
    private int numChannels_;
    private int depth_;
    private int numFrames_;
    private int height_;
    private int numSlices_;
    private int width_;
    private boolean initialized_;
    private ImagePlus hyperImage_;
    private Map<String,String>[] displaySettings_;
    private AcquisitionVirtualStack virtualStack_;
    private String pixelType_;
    private ImageFileManagerInterface imageFileManager_;
    private Map<String,String> summaryMetadata_ = null;
    private final boolean newData_;
    private Map<String,String> systemMetadata_ = null;
 
    public MMVirtualAcquisition(String name, String dir, boolean newData) {
       name_ = name;
       dir_ = dir;
       newData_ = newData;
    }
 
    public void setDimensions(int frames, int channels, int slices) throws MMScriptException {
       if (initialized_) {
          throw new MMScriptException("Can't change dimensions - the acquisition is already initialized");
       }
       if (summaryMetadata_ == null) {
          summaryMetadata_ = new HashMap<String,String>();
       }
       numFrames_ = frames;
       numChannels_ = channels;
       numSlices_ = slices;
       displaySettings_ = new Map[channels];
       for (int i = 0; i < channels; ++i) {
          displaySettings_[i] = new HashMap<String,String>();
       }
 
       MDUtils.put(summaryMetadata_,"Channels",numChannels_);
       MDUtils.put(summaryMetadata_,"Slices",numSlices_);
       MDUtils.put(summaryMetadata_,"Frames",numFrames_);
    }
 
    public void setImagePhysicalDimensions(int width, int height, int depth) throws MMScriptException {
       width_ = width;
       height_ = height;
       depth_ = depth;
       int type = 0;
 
       if (depth_ == 1)
          type = ImagePlus.GRAY8;
       if (depth_ == 2)
          type = ImagePlus.GRAY16;
       if (depth_ == 4)
         type = ImagePlus.GRAY8;
       if (depth_ == 8)
          type = 64;
 
       summaryMetadata_.put("Width", NumberUtils.intToCoreString(width_));
       summaryMetadata_.put("Height", NumberUtils.intToCoreString(height_));
       try {
          MDUtils.setImageType(summaryMetadata_, type);
       } catch (Exception ex) {
          ReportingUtils.logError(ex);
       }
    }
 
    public int getChannels() {
       return numChannels_;
    }
 
    public int getDepth() {
       return depth_;
    }
 
    public int getFrames() {
       return numFrames_;
    }
 
    public int getHeight() {
       return height_;
    }
 
    public int getSlices() {
       return numSlices_;
    }
 
    public int getWidth() {
       return width_;
    }
 
    public boolean isInitialized() {
       return initialized_;
    }
 
    public void close() {
       //compositeImage_.hide();
       initialized_ = false;
       imageFileManager_.finishWriting();
    }
 
    public void closeImage5D() {
       throw new UnsupportedOperationException("Not supported yet.");
    }
 
    public AcquisitionData getAcqData() {
       throw new UnsupportedOperationException("Not supported.");
    }
 
    public String getProperty(String propertyName) throws MMScriptException {
       throw new UnsupportedOperationException("Not supported yet.");
    }
 
    public String getProperty(int frame, int channel, int slice, String propName) throws MMScriptException {
       throw new UnsupportedOperationException("Not supported yet.");
    }
 
    public boolean hasActiveImage5D() {
       throw new UnsupportedOperationException("Not supported yet.");
    }
 
    public void initialize() throws MMScriptException {
       imageFileManager_ = new DefaultImageFileManager(dir_, newData_, summaryMetadata_, systemMetadata_);
       imageCache_ = new MMImageCache(imageFileManager_);
       if (!newData_) {
          try {
             summaryMetadata_ = imageFileManager_.getSummaryMetadata();
             width_ = MDUtils.getWidth(summaryMetadata_);
             height_ = MDUtils.getHeight(summaryMetadata_);
             pixelType_ = MDUtils.getPixelType(summaryMetadata_);
             numSlices_ = MDUtils.getInt(summaryMetadata_, "Slices");
             numFrames_ = MDUtils.getInt(summaryMetadata_, "Frames");
             numChannels_ = MDUtils.getInt(summaryMetadata_, "Channels");
          } catch (Exception ex) {
             ReportingUtils.logError(ex);
          }
       }
       virtualStack_ = new AcquisitionVirtualStack(width_, height_, null, dir_, imageCache_, numChannels_ * numSlices_ * numFrames_);
       try {
          virtualStack_.setType(MDUtils.getImageType(summaryMetadata_));
       } catch (Exception ex) {
          ReportingUtils.logError(ex);
       }
       initialized_ = true;
    }
 
    public void insertImage(Object pixels, int frame, int channel, int slice) throws MMScriptException {
       throw new UnsupportedOperationException("Not supported yet.");
    }
 
    public void insertImage(TaggedImage taggedImg) throws MMScriptException {
 
       virtualStack_.insertImage(taggedImg);
       if (hyperImage_ == null) {
          show();
       }
       Map<String,String> md = taggedImg.md;
       
       if (numChannels_ > 1) {
          ((CompositeImage) hyperImage_).setChannelsUpdated();
       }
 
       if (hyperImage_.getFrame() == 1) {
          int middleSlice = 1 + hyperImage_.getNSlices() / 2;
          if (hyperImage_.getSlice() == middleSlice) {
             ImageStatistics stat = hyperImage_.getStatistics();
             hyperImage_.setDisplayRange(stat.min, stat.max);
             hyperImage_.updateAndDraw();
          }
       }
 
       try {
          if ((hyperImage_.getFrame() - 1) > (MDUtils.getFrame(md) - 2)) {
             hyperImage_.setPosition(1 + MDUtils.getChannelIndex(md), 1 + MDUtils.getSlice(md), 1 + MDUtils.getFrame(md));
          }
       } catch (Exception e) {
          ReportingUtils.logError(e);
       }
    }
 
    public void show() {
 
       
       ImagePlus imgp = new ImagePlus(dir_, virtualStack_);
       virtualStack_.setImagePlus(imgp);
       imgp.setDimensions(numChannels_, numSlices_, numFrames_);
       if (numChannels_ > 1) {
          hyperImage_ = new CompositeImage(imgp, CompositeImage.COMPOSITE);
       } else {
          hyperImage_ = imgp;
          imgp.setOpenAsHyperStack(true);
       }
       updateChannelColors();
       hyperImage_.show();
       ImageWindow win = hyperImage_.getWindow();
       HyperstackControls hc = new HyperstackControls(this);
       win.add(hc);
       win.pack();
 
       if (!newData_) {
          for (Map<String,String> md:imageFileManager_.getImageMetadata()) {
             virtualStack_.rememberImage(md);
          }
          ((CompositeImage) hyperImage_).setChannelsUpdated();
          hyperImage_.updateAndDraw();
       }
    }
 
    public void setChannelColor(int channel, int rgb) throws MMScriptException {
       displaySettings_[channel].put("ChannelColor", String.format("%d", rgb));
    }
 
    public void setChannelContrast(int channel, int min, int max) throws MMScriptException {
       displaySettings_[channel].put("ChannelContrastMin", String.format("%d", min));
       displaySettings_[channel].put("ChannelContrastMax", String.format("%d", max));
    }
 
    public void setChannelName(int channel, String name) throws MMScriptException {
       displaySettings_[channel].put("ChannelName", name);
    }
 
    public Map<String,String> getCurrentMetadata() {
       int index = getCurrentFlatIndex();
       return virtualStack_.getTaggedImage(index).md;
    }
 
    private int getCurrentFlatIndex() {
       return hyperImage_.getCurrentSlice();
    }
 
    public ImagePlus getImagePlus() {
       return hyperImage_;
    }
 
    public void setComment(String comment) throws MMScriptException {
       throw new UnsupportedOperationException("Not supported yet.");
    }
 
    public void setContrastBasedOnFrame(int frame, int slice) throws MMScriptException {
       throw new UnsupportedOperationException("Not supported yet.");
    }
 
    public void setProperty(String propertyName, String value) throws MMScriptException {
       throw new UnsupportedOperationException("Not supported yet.");
    }
 
    public void setProperty(int frame, int channel, int slice, String propName, String value) throws MMScriptException {
       throw new UnsupportedOperationException("Not supported yet.");
    }
 
    public void setRootDirectory(String dir) throws MMScriptException {
       throw new UnsupportedOperationException("Not supported yet.");
    }
 
    public void setSystemProperties(Map<String,String> md) throws MMScriptException {
       systemMetadata_ = md;
    }
 
    public void setSystemState(int frame, int channel, int slice, JSONObject state) throws MMScriptException {
       throw new UnsupportedOperationException("Not supported.");
    }
 
    public boolean windowClosed() {
       return false;
    }
 
    void showFolder() {
       if (dir_.length() != 0) {
          try {
             if (JavaUtils.isWindows()) {
                Runtime.getRuntime().exec("Explorer /n,/select," + dir_);
             } else if (JavaUtils.isMac()) {
                Runtime.getRuntime().exec("open " + dir_);
             }
          } catch (IOException ex) {
             ReportingUtils.logError(ex);
          }
       }
    }
 
    private void updateChannelColors() {
       if (hyperImage_ instanceof CompositeImage) {
          if (displaySettings_ != null) {
             CompositeImage compositeImage = (CompositeImage) hyperImage_;
             for (int channel = 0; channel < compositeImage.getNChannels(); ++channel) {
                int color = Integer.parseInt(displaySettings_[channel].get("ChannelColor"));
                Color col = new Color(color);
                compositeImage.setChannelLut(compositeImage.createLutFromColor(col), 1 + channel);
             }
          }
       }
    }
 
    public void setSummaryProperties(Map<String,String> md) {
       summaryMetadata_ = md;
    }
 
    public void setSystemState(Map<String,String> md) {
       systemMetadata_ = md;
    }
 }
