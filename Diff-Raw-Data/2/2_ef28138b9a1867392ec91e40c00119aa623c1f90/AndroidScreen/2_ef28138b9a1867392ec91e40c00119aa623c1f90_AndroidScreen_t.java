 package com.quanleimu.screenshot;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.imageio.ImageIO;
 import javax.swing.SwingUtilities;
 
 import java.util.Date;
 import java.text.SimpleDateFormat; 
 
 import com.android.ddmlib.AndroidDebugBridge;
 import com.android.ddmlib.IDevice;
 import com.android.ddmlib.RawImage;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.lang.Throwable;
 
 public class AndroidScreen {
    private final Logger logger = LoggerFactory.getLogger(AndroidScreen.class);
     public static final int CONNECTING_PAUSE = 200;
     public static final int WAITING_PAUSE = 50;
 	private String sdkPath;
     private AndroidDebugBridge bridge;
     private IDevice device;
     private final Map<String, IDevice> devices = new HashMap<String, IDevice>();
 
     public void run() throws Exception {
     	initBridge();
     	sleep(5 * 1000);
     	while (true) {
             
             IDevice[] devices = bridge.getDevices();
             for (IDevice d : devices) {
             	if (d != null) {
             		setDevice(d);
                 	saveScreen(d);
                 	clickScreen(d);
                 	sendKeyCode(d);//KEYCODE_BACK
                 	sleep(WAITING_PAUSE);
                 } else {
                     sleep(CONNECTING_PAUSE);
                 }
             }
             //break;
         }
     }
     
     private void saveScreen(IDevice d) throws Exception {
     	
     	String errLockFile = "bxtestcase_err.lock";
     	String lockDir = checkStatusFile(d, errLockFile);
     	if (lockDir != null) {
     		BXOutputReceiver rev = new BXOutputReceiver();
     		Image img = fetchScreen();
     		if (img != null) {
     			saveImage(img);
     			logger.debug("image saved");
     		}
     		Date nowTime=new Date();
     		SimpleDateFormat time=new SimpleDateFormat("yyyyMMdd");
         	BXOutputReceiver log = new BXOutputReceiver();
         	log.logFile = "logs/logcat/test_" + d.toString() + "_" + time.format(nowTime) + ".log";
     		d.executeShellCommand("logcat -d", log);
     		sleep(WAITING_PAUSE);
     		d.executeShellCommand("logcat -c", rev);
     	}
     }
     
     private void clickScreen(IDevice d) throws Exception {
     	String statusFile = "baixing_waiting_click.lock";
     	String retString = checkStatusFile(d, statusFile);
     	
     	if (retString != null) {
     		String sParam = parseStatusParam(retString);
     		if (sParam == null) return;
     		String[] params = sParam.split(",");
     		BXOutputReceiver rev = new BXOutputReceiver();
     		if (d.toString().equals("015d18844854041c") || d.toString().equals("015d1458a51c0c0e")) {
 	    		d.executeShellCommand("sendevent /dev/input/event0 3 53 " + params[0], rev);
 	    		d.executeShellCommand("sendevent /dev/input/event0 3 54 " + params[1], rev);
 	    		d.executeShellCommand("sendevent /dev/input/event0 3 57 1", rev); //touch
 	    		d.executeShellCommand("sendevent /dev/input/event0 0 0 0", rev);
 	    		d.executeShellCommand("sendevent /dev/input/event0 3 57 0", rev); //untouch
 	    		d.executeShellCommand("sendevent /dev/input/event0 0 0 0", rev);
     		} else {
 	    		d.executeShellCommand("sendevent /dev/input/event0 3 0 " + params[0], rev);
 	    		d.executeShellCommand("sendevent /dev/input/event0 3 1 " + params[1], rev);
 	    		d.executeShellCommand("sendevent /dev/input/event0 1 330 1", rev); //touch
 	    		d.executeShellCommand("sendevent /dev/input/event0 0 0 0", rev);
 	    		d.executeShellCommand("sendevent /dev/input/event0 1 330 0", rev); //untouch
 	    		d.executeShellCommand("sendevent /dev/input/event0 0 0 0", rev);
     		}
     		logger.debug(d.toString() + " sendevent:" + rev.revString + "p:x" + params[0] + ":y" + params[1]);
     	}
     }
     
     private void sendKeyCode(IDevice d) throws Exception {
     	String statusFile = "baixing_waiting_sendkey.lock";
     	String retString = checkStatusFile(d, statusFile);
     	if (retString != null) {
     		String sParam = parseStatusParam(retString);
     		if (sParam == null) return;
     		BXOutputReceiver rev = new BXOutputReceiver();
     		d.executeShellCommand("input keyevent " + sParam, rev);
     		logger.debug(d.toString() + " sendevent:" + rev.revString + "p:" + sParam);
     	}
     }
     
     private String checkStatusFile(IDevice d, String lockFile) throws Exception {
     	BXOutputReceiver rev = new BXOutputReceiver();
     	String lockDir = "/mnt/sdcard/Athrun/";
     	d.executeShellCommand("ls " + lockDir, rev);
     	//logger.debug(d.toString() + "recieved:" + rev.revString);
     	//String errLockFile = d.toString() + "_err.lock";
     	
     	if (rev.revString.indexOf("No such file or directory") > 0) {
     		//logger.info("ls " + lockDir + " ", rev.revString);
     		lockDir = "/sdcard/Athrun/";
     		rev.revString = "";
         	d.executeShellCommand("ls " + lockDir, rev);
     	}
 
 		//logger.info("ls " + lockDir + " 2 ", rev.revString);
     	if (rev.revString.indexOf(lockFile) > 0) {
     		rev.revString = "";
     		d.executeShellCommand("cat " + lockDir + lockFile, rev);
     		String retString = rev.revString;
     		logger.debug(d.toString() + " cat:" + lockDir + lockFile + " rev:" + retString);
     		d.executeShellCommand("rm " + lockDir + lockFile + "*", rev);
     		return retString;
     	}
     	return null;
     }
     
     private String parseStatusParam(String retString) {
     	String mark = "lock_params:";
     	int start = retString.indexOf(mark);
     	if (start > 0) {
     		return retString.substring(start + mark.length()).trim();
     	}
     	return null;
     }
     
 	private void initBridge() {
 		Properties p = new Properties();
 		InputStream in = null;
 		FileInputStream fi = null;
 		//System.out.println(getJarFolder() + "local.properties");
 		try {
 			fi = new FileInputStream(getJarFolder() + "local.properties");
 			in = new BufferedInputStream(fi);
 			p.load(in);
 			sdkPath = p.getProperty("sdk.dir");
 		} catch (Throwable tr) { 
 			logger.error("local.properties not found, use {user.home}/android-sdk-macosx");
         } finally {
         	if (fi != null) {
         		try {
         			fi.close();
         		} catch (Throwable tr) {  
                 }
         	}
         	if (in != null) {
         		try {  
         			in.close(); 
                 } catch (Throwable tr) {  
                 } 
         	}
         }
 		if (sdkPath == null || sdkPath.length() == 0) {
 			sdkPath = System.getProperty("user.home") + "/android-sdk-macosx";
 		}
 		
 		if (!AndroidSdkHelper.validatePath(sdkPath)) {
             logger.error("Android SDK is not properly configured.");
             return;
         }
 		System.out.println("initBridge");
         AndroidDebugBridge.init(false);
         System.out.println("create bridge");
         logger.trace("create bridge");
         String adbPath = sdkPath + File.separator + "platform-tools" + File.separator + "adb";
         bridge = AndroidDebugBridge.createBridge(adbPath, true);
         logger.trace("bridge is created");
 
         AndroidDebugBridge.addDeviceChangeListener(new AndroidDebugBridge.IDeviceChangeListener() {
 
             @Override
             public void deviceConnected(IDevice device) {
                 logger.debug("deviceConnected: {}", device + "." + device.toString());
                 addDevice(device);
                 devices.put(device.toString(), device);
             }
 
             @Override
             public void deviceDisconnected(IDevice device) {
                 logger.debug("deviceDisconnected: {}", device);
                 removeDeviceByName(device.toString());
                 //removeDevice(device);
             }
 
             @Override
             public void deviceChanged(IDevice device, int changeMask) {
                 logger.debug("deviceChanged: {} - {}", device, changeMask);
             }
 
         });
     }
 	
 	private Image fetchScreen() {
         final IDevice d = device;
         Image image = null;
         if (d != null) {
             try {
                 RawImage screenshot = device.getScreenshot();
 
                 if (screenshot != null) {
                     image = renderImage(screenshot);
                 }
 
             } catch (Exception ex) {
                 logger.error("", ex);
             } 
         }
         return image;
     }
 	
 	private Image renderImage(RawImage screenshot) {
         BufferedImage image = new BufferedImage(screenshot.width, screenshot.height, BufferedImage.TYPE_INT_RGB);
 
         int index = 0;
         int indexInc = screenshot.bpp >> 3;
 
         int value;
 
         for (int y = 0; y < screenshot.height; y++) {
             for (int x = 0; x < screenshot.width; x++, index += indexInc) {
                 value = screenshot.getARGB(index);
                 image.setRGB(x, y, value);
             }
         }
 
         return image;
     }
 	
 	void saveImage(Image img) {
 		Date nowTime=new Date();
 		SimpleDateFormat time=new SimpleDateFormat("yyyyMMddHHmmss"); 
 		String filePath = "logs/screen/" + time.format(nowTime) + "_" + device.toString() + "_" + String.valueOf((int)(Math.random() * 10000)) + ".png";
         File target = new java.io.File(filePath);
         logger.debug("img path:" + target.getAbsolutePath());
         try {
             ImageIO.write((RenderedImage) img, "PNG", target);
             logger.info("status.saved", target.getName());
         } catch (IOException ex) {
             logger.error("Cannot save image to file.", ex);
             logger.info("error.save.image", target.getPath());
         }
     }
 	
 	private void addDevice(IDevice device) {
         
         if (this.device == null) {
            setDevice(device);
         }
     }
 
     private void removeDevice(IDevice device) {
         if (this.device.equals(device)) {
             IDevice[] devices = bridge.getDevices();
             if (devices.length == 0) {
                 setDevice(null);
             } else {
                 setDevice(devices[0]);
             }
         }
     }
 
     private void setDevice(IDevice device) {
         this.device = device;
     }
     
     public void connectTo(String str) {
         if (StringUtils.isBlank(str)) {
             return ;
         }
         IDevice[] devices = bridge.getDevices();
         for (IDevice d : devices) {
             if (str.equals(d.toString())) {
                 setDevice(d);
             }
         }
     }
     
     void removeDeviceByName(final String deviceStr) {
     	if (devices.containsKey(deviceStr)) {
             try {
                 SwingUtilities.invokeAndWait(new Runnable() {
                     @Override
                     public void run() {
                         IDevice d = devices.get(deviceStr);
                         devices.remove(deviceStr);
                         removeDevice(d);
                     }
                 });
             } catch (Exception ignore) {
             }
         }
     }
     
     private void sleep(int t) {
         try {
             Thread.sleep(t);
         } catch (InterruptedException ignore) {
         }
     }
     
     private String getJarFolder() {
         // get name and path
         String name = getClass().getName().replace('.', '/');
         name = getClass().getResource("/" + name + ".class").toString();
         //System.out.println(name);
         // remove junk
         name = name.substring(0, name.indexOf(".jar"));
         int offset  = 1;//TODO in windows -1;
         name = name.substring(name.lastIndexOf(':') + offset, name.lastIndexOf('/')+1).replace('%', ' ');
         // remove escape characters
         String s = "";
         for (int k=0; k<name.length(); k++) {
           s += name.charAt(k);
           if (name.charAt(k) == ' ') k += 2;
         }
         // replace '/' with system separator char
         return s.replace('/', File.separatorChar);
       }
 }
