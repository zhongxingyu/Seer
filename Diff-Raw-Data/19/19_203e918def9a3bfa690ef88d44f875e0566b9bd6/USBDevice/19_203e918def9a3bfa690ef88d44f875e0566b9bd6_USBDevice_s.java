 package org.jsystemtest.mobile.core.device;
 
 import java.io.BufferedReader;
 import java.io.IOException;
import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Logger;
 import org.jsystemtest.mobile.core.ConnectionException;
 
 import com.android.ddmlib.AndroidDebugBridge;
 import com.android.ddmlib.IDevice;
 import com.android.ddmlib.InstallException;
 
 public class USBDevice extends AbstractAndroidDevice {
 
 	private final static Logger logger = Logger.getLogger(USBDevice.class);
 
 	public USBDevice(AndroidDebugBridge adb, IDevice device) throws Exception {
 		super(adb, device);
		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void connect() throws ConnectionException {
 		// Done automatically via IDeviceChangeListener
 
 	}
 
 	@Override
 	public void disconnect() {
 		// Done automatically via IDeviceChangeListener
 
 	}
 
 	/**
 	 * Install APK on device
 	 * 
 	 * @param apkLocation
 	 * @throws InstallException
 	 */
 	public void installPackage(String apkLocation, boolean reinstall) throws InstallException {
 		final String result = device.installPackage(apkLocation, reinstall);
 		if (result != null) {
 			throw new InstallException("Failed to install: " + result, null);
 		}
 
 	}
 
 	/**
 	 * Limor B
 	 * 
 	 * Execute test on device.
 	 * 
 	 * @param pakageName
 	 * @param testClassName
 	 * @param testName
	 * @throws Exception
 	 */
 	public void runTestOnDevice(String pakageName, String testClassName, String testName) throws Exception {
 
 		if (null == adbLocation || !adbLocation.exists()) {
 			throw new IOException("Can't find adb location");
 		}
 		String cmd = adbLocation.getAbsolutePath() + "\\adb -s "
 				+ device.getSerialNumber() + " shell am instrument -e class "
 				+ pakageName + "." + testClassName + "#" + testName + " "
 				+ pakageName + "/android.test.InstrumentationTestRunner";
 		logger.info("Try to run command:" + cmd);
 		Runtime run = Runtime.getRuntime();
 		Process pr = run.exec(cmd);
 		try {
 			pr.waitFor();
 			Thread.sleep(TimeUnit.SECONDS.toMillis(2));
 			BufferedReader stdInput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
 			String s;
			String allBuffer = " ";
 			while ((s = stdInput.readLine()) != null) {
				allBuffer+=s;
 			} 
			if(allBuffer.contains("Exception")){
				Exception e = new Exception(allBuffer);
 				logger.error(e);
 				throw e;
 			}
 			
			
 		} catch (InterruptedException e) {
 			// Don't care
 		}
 	}
 
 }
