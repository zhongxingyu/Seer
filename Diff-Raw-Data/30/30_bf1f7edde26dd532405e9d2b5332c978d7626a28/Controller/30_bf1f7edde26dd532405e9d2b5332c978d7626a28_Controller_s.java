 package com.github.dreamrec;
 
 import com.github.dreamrec.ads.AdsChannelModel;
 import com.github.dreamrec.ads.AdsManager;
 import com.github.dreamrec.ads.AdsModel;
 import com.github.dreamrec.ads.ChannelModel;
 import com.github.dreamrec.comport.ComPort;
 import com.github.dreamrec.edf.EdfWriter;
 import gnu.io.NoSuchPortException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  *
  */
 public class Controller {
 
     private Timer repaintTimer;
     private Model model;
     private MainWindow mainWindow;
     private SettingsWindow settingsWindow;
 
     public void setSettingsWindow(SettingsWindow settingsWindow) {
         this.settingsWindow = settingsWindow;
     }
 
     private ApplicationProperties applicationProperties;
     private static final Log log = LogFactory.getLog(Controller.class);
     public static int CURSOR_SCROLL_STEP = 1; //in points
     private boolean isAutoScroll = false;
     /* private HiPassPreFilter ch1PreFilter;
   private HiPassPreFilter ch2PreFilter;
   private HiPassPreFilter acc1PreFilter;
   private HiPassPreFilter acc2PreFilter;
   private HiPassPreFilter acc3PreFilter;*/
     private FrameDecoder frameDecoder;
     private AdsModel adsModel;
     private ComPort comport;
     private EdfWriter edfWriter;
     private ArrayList<AdsDataListener> adsDataListeners = new ArrayList<AdsDataListener>();
 
 
     public Controller(Model model, AdsModel adsModel, ComPort comport, ApplicationProperties applicationProperties) {
         this.model = model;
         this.adsModel = adsModel;
         this.comport = comport;
         this.applicationProperties = applicationProperties;
         repaintTimer = new Timer(applicationProperties.getRepaintDelay(), new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 updateModel();
 
 //                mainWindow.repaint();
             }
         });
         /*int hiPassBufferSize = applicationProperties.getHiPassBufferSize();
          ch1PreFilter = new HiPassPreFilter(hiPassBufferSize);
      ch2PreFilter = new HiPassPreFilter(hiPassBufferSize);
      acc1PreFilter = new HiPassPreFilter(hiPassBufferSize);
      acc2PreFilter = new HiPassPreFilter(hiPassBufferSize);
      acc3PreFilter = new HiPassPreFilter(hiPassBufferSize);
 */
     }
 
     public AdsModel getAdsModel() {
         return adsModel;
     }
 
     public void addAdsDataListener(AdsDataListener adsDataListener) {
         adsDataListeners.add(adsDataListener);
     }
 
     /*public void setMainWindow(MainWindow _mainWindow) {
         this.mainWindow = _mainWindow;
         repaintTimer = new Timer(applicationProperties.getRepaintDelay(), new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 updateModel();
                 mainWindow.repaint();
             }
         });
     }*/
 
     protected void updateModel() {
         boolean isLoffUpdated = false;
         while (frameDecoder.available()) {
             int[] frame = frameDecoder.poll();
             notifyListeners(frame);
             /*  model.addEyeData(ch1PreFilter.getFilteredValue(frame[0]));
             model.addCh2Data(ch2PreFilter.getFilteredValue(frame[1]));
             model.addAcc1Data(acc1PreFilter.getFilteredValue(frame[2]));
             model.addAcc2Data(acc2PreFilter.getFilteredValue(frame[3]));
             model.addAcc3Data(acc3PreFilter.getFilteredValue(frame[4]));*/
             /*  model.addEyeData(frame[0]);
             model.addCh2Data(frame[1]);
             model.addAcc1Data(frame[2]);
             model.addAcc2Data(frame[3]);
             model.addAcc3Data(frame[4]);*/
             /* if (!isLoffUpdated) {
                 log.info("Loff status: " + frame[frame.length - 1]);
                 isLoffUpdated = true;
             }*/
             if (!isLoffUpdated) {
                 settingsWindow.updateLoffStatus(frame[frame.length - 1]);
                 isLoffUpdated = true;
             }
             if (edfWriter.isReportUpdated()) {
                 settingsWindow.setReport(edfWriter.isRecording(), edfWriter.getReport());
             }
 
         }
         if (isAutoScroll) {
             model.setFastGraphIndexMaximum();
         }
     }
 
     private void notifyListeners(int[] frame) {
         for (AdsDataListener adsDataListener : adsDataListeners) {
             adsDataListener.onDataReceived(frame);
         }
     }
 
     public void saveToFile() {
         try {
             JFileChooser fileChooser = new DrmFileChooser(applicationProperties);
             int fileChooserState = fileChooser.showSaveDialog(mainWindow);
             if (fileChooserState == JFileChooser.APPROVE_OPTION) {
                 new DataSaveManager().saveToFile(fileChooser.getSelectedFile(), model);
             }
         } catch (ApplicationException e) {
             mainWindow.showMessage(e.getMessage());
         }
     }
 
     public void saveAsEdf() {
         try {
             JFileChooser fileChooser = new DrmFileChooser(applicationProperties);
             int fileChooserState = fileChooser.showSaveDialog(mainWindow);
             if (fileChooserState == JFileChooser.APPROVE_OPTION) {
                 new DataSaveManager().saveAsEdf(fileChooser.getSelectedFile(), model);
             }
         } catch (ApplicationException e) {
             mainWindow.showMessage(e.getMessage());
         }
     }
 
     public void readFromFile() {
         try {
             JFileChooser fileChooser = new DrmFileChooser(applicationProperties);
             int fileChooserState = fileChooser.showOpenDialog(mainWindow);
             if (fileChooserState == JFileChooser.APPROVE_OPTION) {
                 new DataSaveManager().readFromFile(fileChooser.getSelectedFile(), model);
             }
         } catch (ApplicationException e) {
             mainWindow.showMessage(e.getMessage());
         }
         mainWindow.repaint();
     }
 
     public void startRecording() {
         model.clear();
         model.setFrequency(250);
         model.setStartTime(System.currentTimeMillis());
         edfWriter = new EdfWriter(adsModel);
         this.addAdsDataListener(edfWriter);
         edfWriter.startRecording();
         settingsWindow.setReport(edfWriter.isRecording(), edfWriter.getReport());
 //        temDebugMethod();
         try {
             comport.connect(applicationProperties.getComPortName());
             frameDecoder = new FrameDecoder(adsModel.getFrameSize());
             comport.setComPortListener(frameDecoder);
             AdsManager adsManager = new AdsManager();
             comport.writeToPort(adsManager.writeModelState(adsModel));
         } catch (NoSuchPortException e) {
            String msg = "No port with the name " + applicationProperties.getComPortName() +
                    ".\nCheck Com Port settings and power connection.\nRestart application.";
             log.error(msg, e);
             JOptionPane.showMessageDialog(null, msg);
             System.exit(0);
         } catch (Exception e) {
             e.printStackTrace();
         }
         repaintTimer.start();
         isAutoScroll = true;
 
     }
 
     public void stopRecording() {
         repaintTimer.stop();
         isAutoScroll = false;
         comport.writeToPort(new AdsManager().startPinLo());
         edfWriter.stopRecording();
         settingsWindow.setReport(edfWriter.isRecording(), edfWriter.getReport());
     }
 
     public void changeXSize(int xSize) {
         model.setXSize(xSize);
         applicationProperties.setXSize(xSize);
         mainWindow.repaint();
     }
 
     public void scrollCursorForward() {
         model.moveFastGraph(model.getFastGraphIndex() + CURSOR_SCROLL_STEP * Model.DIVIDER);
         if (model.isFastGraphIndexMaximum()) {
             isAutoScroll = true;
         }
         mainWindow.repaint();
     }
 
     public void scrollCursorBackward() {
         model.moveFastGraph(model.getFastGraphIndex() - CURSOR_SCROLL_STEP * Model.DIVIDER);
         isAutoScroll = false;
         mainWindow.repaint();
     }
 
     public void moveCursor(int newPosition) {
         model.moveCursor(newPosition);
         mainWindow.repaint();
     }
 
     public void scrollSlowGraph(int scrollPosition) {
         model.moveSlowGraph(scrollPosition);
         isAutoScroll = false;
         mainWindow.repaint();
     }
 
     public void closeApplication() {
         saveAdsModelToProperties();
         applicationProperties.save();
         try {
             comport.writeToPort(new AdsManager().startPinLo());
             Thread.sleep(200);
         } catch (Exception e) {
             e.printStackTrace();
         }
         comport.disconnect();
     }
 
     private File chooseFileToSave() {
         File file = null;
         JFileChooser fileChooser = new DrmFileChooser(applicationProperties);
         int fileChooserState = fileChooser.showSaveDialog(null);
         if (fileChooserState == JFileChooser.APPROVE_OPTION) {
             file = fileChooser.getSelectedFile();
         }
         return file;
     }
 
     private void saveAdsModelToProperties() {
         applicationProperties.setSps(adsModel.getSps());
         for (int i = 0; i < adsModel.getNumberOfAdsChannels(); i++) {
             AdsChannelModel channel = adsModel.getAdsChannel(i);
             applicationProperties.setChannelDivider(i, channel.getDivider());
             applicationProperties.setChannelName(i, channel.getName());
             applicationProperties.setChannelHiPassFrequency(i, channel.getHiPassFilterFrequency());
             applicationProperties.setChannelEnabled(i, channel.isEnabled());
         }
         for (int i = 0; i < adsModel.getNumberOfAccelerometerChannels(); i++) {
             ChannelModel channel = adsModel.getAccelerometerChannel(i);
             applicationProperties.setAccelerometerDivider(channel.getDivider());
             applicationProperties.setAccelerometerName(i, channel.getName());
             applicationProperties.setAccelerometerHiPassFrequency(channel.getHiPassFilterFrequency());
             applicationProperties.setAccelerometerEnabled(channel.isEnabled());
         }
     }
 
     // temporary file for debugging Gala
     private void temDebugMethod() {
             Runnable r = new Runnable() {
             public void run() {
                 int n = 0;
                 int[] frame = new int[100000];
                 for (int i = 0; i < 100000; i++) {
                     frame[i] = i;
                 }
                 while (true) {
                     try {
                         notifyListeners(frame);
                         if (edfWriter.isReportUpdated()) {
                             settingsWindow.setReport(edfWriter.isRecording(), edfWriter.getReport());
                         }
                         Thread.sleep(200);
                         //settingsWindow.setReport(true, "Recording of Edf...  Record duration: " + n + " sec");
                         System.out.println(n);
                         n++;
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
             }
         };
         new Thread(r).start();
     }
 }
