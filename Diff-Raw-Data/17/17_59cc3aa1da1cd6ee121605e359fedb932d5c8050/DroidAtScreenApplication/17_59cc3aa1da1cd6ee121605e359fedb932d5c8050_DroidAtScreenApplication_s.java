 package com.ribomation.droidAtScreen;
 
 import com.ribomation.droidAtScreen.cmd.*;
 import com.ribomation.droidAtScreen.dev.AndroidDevice;
 import com.ribomation.droidAtScreen.dev.AndroidDeviceListener;
 import com.ribomation.droidAtScreen.dev.AndroidDeviceManager;
 import com.ribomation.droidAtScreen.gui.ApplicationFrame;
 import com.ribomation.droidAtScreen.gui.DeviceFrame;
 import org.apache.log4j.Logger;
 
 import javax.swing.*;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 import java.util.prefs.BackingStoreException;
 import java.util.prefs.Preferences;
 
 /**
  * Main entry point of this application
  *
  * @user jens
  * @date 2010-jan-17 11:00:39
  */
 public class DroidAtScreenApplication implements Application, AndroidDeviceListener {
     private Logger                          log = Logger.getLogger(DroidAtScreenApplication.class);
     private AndroidDeviceManager            deviceManager;
     private ApplicationFrame                appFrame;
     private Preferences                     appPreferences;
     private List<AndroidDeviceListener>     deviceListeners = new ArrayList<AndroidDeviceListener>();
     private final String                    appPropertiesPath = "/META-INF/maven/com.ribomation/droidAtScreen/pom.properties";
     private String                          appName = "Droid@Screen";
     private String                          appVersion = "0.0";
     private Map<String, DeviceFrame>        devices = new HashMap<String, DeviceFrame>();
 
 
     public static void main(String[] args) {
         DroidAtScreenApplication    app = new DroidAtScreenApplication();
         app.parseArgs(args);
         app.initProperties();
         app.initCommands();
         app.initGUI();
         app.initAndroid();
         app.run();
         app.postStart();
     }
 
     private void parseArgs(String[] args) {
         log.debug("parseArgs: " + Arrays.toString(args));
     }
 
     private void initProperties() {
         log.debug("initProperties");
         
         InputStream is = this.getClass().getResourceAsStream(appPropertiesPath);
         if (is != null) {
             try {
                 Properties prp = new Properties();
                 prp.load(is);
                 appVersion = prp.getProperty("version", appVersion);
             } catch (IOException e) {
                 log.debug("Missing classpath resource: "+appPropertiesPath, e);
             }
         }
 
         try {
             log.debug("--- Preferences ---");
             Preferences prefs = getPreferences();
             for (String key : prefs.keys()) {
                 log.debug(String.format("%s: %s", key, prefs.get(key, "[none]")));
             }
             log.debug("--- END ---");
         } catch (BackingStoreException e) {log.warn("Failed to list prefs",e);}
     }
 
     private void initCommands() {
         log.debug("initCommands");
         Command.setApplication(this);
     }
 
     private void initAndroid() {
         log.debug("initAndroid");
         deviceManager = new AndroidDeviceManager();
         deviceManager.addAndroidDeviceListener(this);
     }
 
     private void initGUI() {
         log.debug("initGUI");
         appFrame = new ApplicationFrame(this);
         appFrame.initGUI();
     }
 
     private void run() {
         log.debug("run");        
 //        GuiUtil.placeInUpperLeftScreen(getAppFrame());
         getAppFrame().setVisible(true);
     }
 
     private void postStart() {
         log.debug("postStart");
         
         AdbExePathCommand adbCmd = Command.find(AdbExePathCommand.class);
         if (adbCmd.isDefined()) {
             setAdbExecutablePath( adbCmd.getFile() );
             return;
         }
 
         String adbExe = "/platform-tools/adb" + (System.getProperty("os.name","").toLowerCase().startsWith("windows") ? ".exe" : "");
         File   adbFile = new File(System.getenv("ANDROID_HOME") + adbExe);
         if (adbFile.isFile()) {
             adbCmd.setPreferenceValue(adbFile.getAbsolutePath());
             setAdbExecutablePath(adbFile.getAbsoluteFile());
             return;
         }
 
         adbFile = new File(System.getenv("ANDROID_SDK_HOME") + adbExe);
         if (adbFile.isFile()) {
             adbCmd.setPreferenceValue(adbFile.getAbsolutePath());
             setAdbExecutablePath(adbFile.getAbsoluteFile());
             return;
         }
 
         adbCmd.execute();
     }
 
 
     // --------------------------------------------
     // AndroidDeviceManager
     // --------------------------------------------
 
     @Override
     public void connected(final AndroidDevice dev) {
         log.debug("connected: dev="+dev);
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() { addDevice(dev); }
         });
     }
 
     @Override
     public void disconnected(final AndroidDevice dev) {
         log.debug("disconnected: dev="+dev);
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() { removeDevice(dev); }
         });
     }
 
     public void addDevice(AndroidDevice dev) {
         getAppFrame().getStatusBar().message("Connected to " + dev.getName());
         DeviceFrame frame = new DeviceFrame(this, dev, isPortrait(), isUpsideDown(), getScale(), getFrameRate());
         devices.put(frame.getName(), frame);
         fireDeviceConnected(dev);
         frame.setVisibleEnabled(true);
     }
 
     public void removeDevice(AndroidDevice dev) {
         getAppFrame().getStatusBar().message("Disconnected from " + dev.getName());
         fireDeviceDisconnected(dev);
         DeviceFrame frame = devices.remove(dev.getName());
         if (frame == null) return;
         frame.setVisibleEnabled(false);
         frame.dispose();
     }
 
 
 //    @Override
 //    public void showDevice(AndroidDevice dev) {
 //        log.debug("showDevice: "+dev);
 //        try {
 //            DeviceFrame devFrame = new DeviceFrame(this, dev, isPortrait(), isUpsideDown(), getScale(), getFrameRate());
 ////            getAppFrame().getDevices().add(devFrame);
 ////            getAppFrame().setMinimumSize(getAppFrame().getDevices().getPreferredSize());
 ////            getAppFrame().pack();
 ////            getAppFrame().repaint();
 //
 ////            GuiUtil.placeInCenterScreen(devFrame);
 //            devFrame.setVisible(true);
 //
 //
 //            devices.put(devFrame.getName(), devFrame);
 //        } catch (Exception e) {
 //            log.debug("Failed showing device", e);
 //            fireDeviceDisconnected(dev);
 //
 //            String title = "Device failure";
 //            String msg   = "Failed to show device: " + e.getMessage();
 //            if (msg.lastIndexOf("device offline") > 0) {
 //                title = "Device offline";
 //                msg   = "The ADB claims the device is offline. Please, unplug/replug the device and/or restart this application.";
 //            }
 //
 //            JOptionPane.showMessageDialog(getAppFrame(), msg, title, JOptionPane.ERROR_MESSAGE);
 //        }
 //    }
 
 
 //    @Override
 //    public void hideDevice(DeviceFrame dev) {
 //        hideDevice(dev, true);
 //    }
 //
 //    @Override
 //    public void hideDevice(DeviceFrame dev, boolean doDispose) {
 //        log.debug("hideDevice: "+dev.getName());
 //        DeviceFrame deviceFrame = devices.remove(dev.getName());
 //        if (deviceFrame != null) {
 //            log.debug("Disposing devFrame: " + deviceFrame.getDevice());
 //            if ( doDispose) {
 //                deviceFrame.dispose();
 //            }
 //        }
 //    }
 
 //    public void hideDevice(AndroidDevice dev) {
 //        log.debug("hideDevice: "+dev);
 //
 //        for (DeviceFrame df : new ArrayList<DeviceFrame>(devices.values())) {
 //            if (df.getName().startsWith(dev.getName())) {
 //                hideDevice(df, true);
 //            }
 //        }
 //    }
 
 //    public void updateDevice(AndroidDevice dev) {
 //        log.debug("updateDevice: "+dev);
 //        hideDevice(dev);
 //        showDevice(dev);
 //    }
 
     @Override
     public DeviceFrame getSelectedDevice() {
        String  devName = (String) getAppFrame().getDeviceList().getSelectedItem();
         DeviceFrame frame = devices.get(devName);
        if (frame == null) {
            throw new RuntimeException("No DeviceFrame with name=" + devName);
        }
         return frame;
     }
 
     @Override
     public Map<String, DeviceFrame> getDevices() {
         return devices;
     }
 
 
     //    @Override
 //    public AndroidDevice getSelectedDevice() {
 //        String  devName = (String) getAppFrame().getDeviceList().getSelectedItem();
 //
 //        if (devName != null) {
 //            if (devices.containsKey(devName)) {
 //                return devices.get(devName).getDevice();
 //            }
 //            if (deviceManager.getDevices().containsKey(devName)) {
 //                return deviceManager.getDevices().get(devName);
 //            }
 //        }
 //
 //        return null;
 //    }
 
     // --------------------------------------------
     // AndroidDeviceListener
     // --------------------------------------------
 
     @Override
     public void addAndroidDeviceListener(AndroidDeviceListener listener) {
         deviceListeners.add(listener);
     }
 
     public void fireDeviceConnected(AndroidDevice dev) {
         for (AndroidDeviceListener listener : deviceListeners) {
             listener.connected(dev);
         }
     }
 
     public void fireDeviceDisconnected(AndroidDevice dev) {
         for (AndroidDeviceListener listener : deviceListeners) {
             listener.disconnected(dev);
         }
     }
 
 
     // --------------------------------------------
     // Application
     // --------------------------------------------
 
     @Override
     public String getName() {
         return appName;
     }
 
     @Override
     public String getVersion() {
         return appVersion;
     }
 
     @Override
     public ApplicationFrame getAppFrame() {
         return appFrame;
     }
 
     @Override
     public Preferences getPreferences() {
         if (appPreferences == null) {
             appPreferences = Preferences.userNodeForPackage(this.getClass());
         }
         return appPreferences;
     }
 
     @Override
     public void savePreferences() {
         try {
             getPreferences().flush();
         } catch (BackingStoreException e) {
             log.info("Failed to flush app preferences", e);
         }
     }
 
     @Override
     public void destroyPreferences() {
         if (appPreferences != null) {
             try {
                 appPreferences.removeNode();
                 appPreferences = null;
             } catch (BackingStoreException e) {
                 log.error("Failed to destroy application properties.", e);
             }
         }
     }
 
     @Override
     public void setAdbExecutablePath(File value) {
         log.debug("setAdbExecutablePath: " + value);
         deviceManager.setAdbExecutable(value);
     }
 
     @Override
     public void setSkipEmulator(boolean value) {
         log.debug("setSkipEmulator: " + value);
     }
 
     @Override
     public void setAutoShow(boolean value) {
         log.debug("setAutoShow: " + value);
     }
 
     @Override
     public void setLandscapeMode(boolean value) {
         log.debug("setLandscapeMode: " + value);
         getSelectedDevice().setLandscapeMode(value);
     }
 
     @Override
     public void setUpsideDown(boolean value) {
         log.debug("setUpsideDown: " + value);
         getSelectedDevice().setUpsideDown(value);
     }
 
     @Override
     public void setFrameRate(int value) {
         log.debug("setFrameRate: " + value);
         getSelectedDevice().setFrameRate(value);
     }
 
     @Override
     public void setScale(int value) {
         log.debug("setScale: " + value);
         getSelectedDevice().setScale(value);
     }
 
 
 
 
     public boolean isAutoShow() {
         return Command.<CheckBoxCommand>find(AutoShowCommand.class).isSelected();
     }
 
     public boolean isSkipEmulator() {
         return Command.<CheckBoxCommand>find(SkipEmulatorCommand.class).isSelected();
     }
 
     public boolean isPortrait() {
         return !Command.<CheckBoxCommand>find(OrientationCommand.class).isSelected();
     }
 
     public boolean isUpsideDown() {
         return Command.<CheckBoxCommand>find(UpsideDownCommand.class).isSelected();
     }
 
     public int  getScale() {
         return Command.<ScaleCommand>find(ScaleCommand.class).getScale();
     }
 
     public int  getFrameRate() {
         return Command.<FrameRateCommand>find(FrameRateCommand.class).getRate();
     }
 
 }
