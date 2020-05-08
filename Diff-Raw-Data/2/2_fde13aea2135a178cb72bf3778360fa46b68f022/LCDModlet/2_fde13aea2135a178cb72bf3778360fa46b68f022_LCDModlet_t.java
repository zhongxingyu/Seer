 package com.buglabs.bug.module.lcd.pub;
 
 import java.awt.Frame;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Properties;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.util.tracker.ServiceTracker;
 
 import com.buglabs.bug.accelerometer.pub.IAccelerometerRawFeed;
 import com.buglabs.bug.accelerometer.pub.IAccelerometerSampleFeed;
 import com.buglabs.bug.accelerometer.pub.IAccelerometerSampleProvider;
 import com.buglabs.bug.jni.common.CharDevice;
 import com.buglabs.bug.jni.common.CharDeviceInputStream;
 import com.buglabs.bug.jni.common.FCNTL_H;
 import com.buglabs.bug.menu.pub.StatusBarUtils;
 import com.buglabs.bug.module.lcd.Activator;
 import com.buglabs.bug.module.lcd.accelerometer.LCDAccelerometerInputStreamProvider;
 import com.buglabs.bug.module.lcd.accelerometer.LCDAccelerometerSampleProvider;
 import com.buglabs.bug.module.motion.pub.AccelerationWS;
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleLEDController;
 import com.buglabs.module.IModuleProperty;
 import com.buglabs.module.ModuleProperty;
 import com.buglabs.util.RemoteOSGiServiceConstants;
 import com.buglabs.util.trackers.PublicWSAdminTracker;
 
 /**
  * LCD Modlet class for 1x LCD module.
  * 
  * @author kgilmer
  * 
  */
public class LCDModlet implements IModlet, ILCDModuleControl, IModuleControl, IModuleDisplay, IModuleLEDController {
 
 	private final BundleContext context;
 	private final int slotId;
 	private final String moduleId;
 	private final String moduleName;
 
 	private ServiceRegistration moduleRef;
 	private ServiceRegistration moduleDisplayServReg;
 	private final int LCD_WIDTH = 320;
 	private final int LCD_HEIGHT = 200;
 	private String regionKey;
 	private ServiceRegistration lcdControlServReg;
 
 	private static boolean icon[][] = { { false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, true, true, true, true, true, true, true, true, true, true, true, true, false, false },
 			{ false, true, false, false, false, false, false, false, false, false, false, false, false, false, true, false },
 			{ false, true, false, true, false, true, false, true, false, true, false, true, false, false, true, false },
 			{ false, true, false, false, true, false, true, false, true, false, true, false, true, false, true, false },
 			{ false, true, false, true, false, true, false, true, false, true, false, true, false, false, true, false },
 			{ false, true, false, false, true, false, true, false, true, false, true, false, true, false, true, false },
 			{ false, true, false, true, false, true, false, true, false, true, false, true, false, false, true, false },
 			{ false, true, false, false, true, false, true, false, true, false, true, false, true, false, true, false },
 			{ false, true, false, true, false, true, false, true, false, true, false, true, false, false, true, false },
 			{ false, true, false, false, true, false, true, false, true, false, true, false, true, false, true, false },
 			{ false, true, false, true, false, true, false, true, false, true, false, true, false, false, true, false },
 			{ false, true, false, false, false, false, false, false, false, false, false, false, false, false, true, false },
 			{ false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false },
 			{ false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false } };
 	
 	private LCDAccelerometerInputStreamProvider lcd_acc_is_prov;
 	private ServiceRegistration accIsProvRef;
 	private ServiceRegistration accRawFeedProvRef;
 	private ServiceRegistration accSampleProvRef;
 	private ServiceTracker wsAccTracker;
 	private CharDevice accel;
 	private CharDeviceInputStream accIs;
 	private ServiceRegistration lcdRef;
 
 	public LCDModlet(BundleContext context, int slotId, String moduleId) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleId = moduleId;
 		this.moduleName = "LCD";
 	}
 
 	public void setup() throws Exception {
 		String lcd_accel_devnode = "/dev/bmi_lcd_acc_m" + (slotId + 1);
 		accel = new CharDevice();
 		int result = accel.open(lcd_accel_devnode, FCNTL_H.O_RDWR);
 		if (result >= 0) {
 			accIs = new CharDeviceInputStream(accel);
 			lcd_acc_is_prov = new LCDAccelerometerInputStreamProvider(accIs);
 		}
 	}
 
 	public void start() throws Exception {
 		moduleRef = context.registerService(IModuleControl.class.getName(), this, null);
 		lcdRef = context.registerService(IModuleLEDController.class.getName(), this, createRemotableProperties(null));
 		Dictionary props = new Hashtable();
 		props.put("width", new Integer(LCD_WIDTH));
 		props.put("height", new Integer(LCD_HEIGHT));
 
 		moduleDisplayServReg = context.registerService(IModuleDisplay.class.getName(), this, createRemotableProperties(props));
 		lcdControlServReg = context.registerService(ILCDModuleControl.class.getName(), this, createRemotableProperties(null));
 
 		if (lcd_acc_is_prov != null) {
 			lcd_acc_is_prov.start();
 			accIsProvRef = context.registerService(IAccelerometerSampleFeed.class.getName(), lcd_acc_is_prov, createRemotableProperties(createBasicServiceProperties()));
 			accRawFeedProvRef = context.registerService(IAccelerometerRawFeed.class.getName(), lcd_acc_is_prov, createRemotableProperties(createBasicServiceProperties()));
 
 			LCDAccelerometerSampleProvider accsp = new LCDAccelerometerSampleProvider(lcd_acc_is_prov);
 
 			accSampleProvRef = context.registerService(IAccelerometerSampleProvider.class.getName(), accsp, createRemotableProperties(createBasicServiceProperties()));
 			AccelerationWS accWs = new AccelerationWS(accsp);
 			wsAccTracker = PublicWSAdminTracker.createTracker(context, accWs);
 		}
 
 		regionKey = StatusBarUtils.displayImage(context, icon, this.getModuleName());
 	}
 
 	public void stop() throws Exception {
 		StatusBarUtils.releaseRegion(context, regionKey);
 
 		if (lcd_acc_is_prov != null) {
 			lcd_acc_is_prov.interrupt();
 			wsAccTracker.close();
 			accIsProvRef.unregister();
 			accRawFeedProvRef.unregister();
 			accSampleProvRef.unregister();
 		}
 
 		moduleRef.unregister();
 		lcdRef.unregister();
 		moduleDisplayServReg.unregister();
 		lcdControlServReg.unregister();
 	}
 
 	/**
 	 * @return A dictionary with R-OSGi enable property.
 	 */
 	private Dictionary createRemotableProperties(Dictionary ht) {
 		if (ht == null) {
 			ht = new Hashtable();
 		}
 
 		ht.put(RemoteOSGiServiceConstants.R_OSGi_REGISTRATION, "true");
 
 		return ht;
 	}
 
 	private Properties createBasicServiceProperties() {
 		Properties p = new Properties();
 		p.put("Provider", this.getClass().getName());
 		p.put("Slot", Integer.toString(slotId));
 		return p;
 	}
 
 	public List getModuleProperties() {
 		List properties = new ArrayList();
 
 		properties.add(new ModuleProperty("Slot", "" + slotId));
 		properties.add(new ModuleProperty("Width", "" + LCD_WIDTH));
 		properties.add(new ModuleProperty("Height", "" + LCD_HEIGHT));
 
 		return properties;
 	}
 
 	public boolean setModuleProperty(IModuleProperty property) {
 		return false;
 	}
 
 	public int getSlotId() {
 		return slotId;
 	}
 
 	public Frame getFrame() {
 		Frame frame = new Frame();
 		frame.setSize(LCD_WIDTH, LCD_HEIGHT);
 		frame.setResizable(false);
 		frame.setVisible(true);
 		return frame;
 	}
 
 	public String getModuleId() {
 		return moduleId;
 	}
 
 	public String getModuleName() {
 		return moduleName;
 	}
 
 	public int setLEDGreen(boolean state) throws IOException {
 		if (state) {
 			return Activator.getInstance().getLCDControl().ioctl_BMI_LCD_GLEDON(slotId);
 		} else {
 			return Activator.getInstance().getLCDControl().ioctl_BMI_LCD_GLEDOFF(slotId);
 		}
 	}
 
 	public int setLEDRed(boolean state) throws IOException {
 		if (state) {
 			return Activator.getInstance().getLCDControl().ioctl_BMI_LCD_RLEDON(slotId);
 		} else {
 			return Activator.getInstance().getLCDControl().ioctl_BMI_LCD_RLEDOFF(slotId);
 		}
 	}
 
 	public int getStatus() throws IOException {
 		return Activator.getInstance().getLCDControl().ioctl_BMI_LCD_GETSTAT(slotId);
 	}
 
 	public int disable() throws IOException {
 		return Activator.getInstance().getLCDControl().ioctl_BMI_LCD_SETRST(slotId);
 	}
 
 	public int enable() throws IOException {
 		return Activator.getInstance().getLCDControl().ioctl_BMI_LCD_CLRRST(slotId);
 	}
 	
 	public int setBlackLight(int val) throws IOException {
 		return setBackLight(val);
 	}
 
 	public int setBackLight(int val) throws IOException {
 		return Activator.getInstance().getLCDControl().ioctl_BMI_LCD_SET_BL(slotId, val);
 	}
 }
