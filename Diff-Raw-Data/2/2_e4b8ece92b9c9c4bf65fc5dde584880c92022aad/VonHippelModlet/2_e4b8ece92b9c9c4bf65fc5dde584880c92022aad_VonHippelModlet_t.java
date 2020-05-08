 package com.buglabs.bug.module.vonhippel;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import javax.microedition.io.CommConnection;
 import javax.microedition.io.Connector;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.util.tracker.ServiceTracker;
 
 import com.buglabs.bug.jni.common.CharDeviceUtils;
 import com.buglabs.bug.jni.vonhippel.VonHippel;
 import com.buglabs.bug.menu.pub.StatusBarUtils;
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.bug.module.vonhippel.pub.IVonHippelModuleControl;
 import com.buglabs.bug.module.vonhippel.pub.VonHippelWS;
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleLEDController;
 import com.buglabs.module.IModuleProperty;
 import com.buglabs.module.ModuleProperty; //import com.buglabs.util.RemoteOSGiServiceConstants;
 import com.buglabs.util.trackers.PublicWSAdminTracker;
 
 public class VonHippelModlet implements IModlet, IVonHippelModuleControl,
 		IModuleControl, IModuleLEDController {
 
 	private BundleContext context;
 
 	private boolean deviceOn = true;
 
 	private int slotId;
 
 	private final String moduleId;
 
 	private ServiceRegistration moduleRef;
 	private ServiceTracker wsMotionTracker, wsAccTracker;
 
 	protected static final String PROPERTY_MODULE_NAME = "moduleName";
 
 	public static final String MODULE_ID = "0007";
 
 	private final String moduleName;
 
 	private VonHippel vhDevice;
 
 	private String regionKey;
 
 	private ServiceRegistration vhModuleRef;
 
 	private ServiceRegistration ledref;
 
 	private CommConnection cc;
 
 	private static boolean icon[][] = {
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
 			{ false, false, true, true, true, true, true, true, true, true,	true, true, true, true, false, false },
 			{ false, true, false, false, false, false, false, false, false,	false, false, false, false, false, true, false },
 			{ false, true, false, true, false, true, false, true, false, true, false, true, false, true, true, false },
 			{ false, true, true, false, true, false, true, false, true, false, true, false, true, false, true, false },
 			{ false, true, false, false, false, false, false, false, false,	false, false, false, false, false, true, false },
 			{ false, true, false, true, false, false, false, true, false, true,	false, false, true, false, true, false },
 			{ false, true, false, true, false, false, false, true, false, true,	false, false, true, false, true, false },
 			{ false, true, false, true, false, false, false, true, false, true, false, false, true, false, true, false },
 			{ false, true, false, true, false, false, false, true, false, true,	true, true, true, false, true, false },
 			{ false, true, false, false, true, false, true, false, false, true,	false, false, true, false, true, false },
 			{ false, true, false, false, false, true, false, false, false,
 					true, false, false, true, false, true, false },
 			{ false, true, false, false, false, false, false, false, false,
 					false, false, false, false, false, true, false },
 			{ false, true, true, true, true, true, true, true, true, true,
 					true, true, true, true, true, false },
 			{ false, true, true, true, true, true, true, true, true, true,
 					true, true, true, true, true, false },
 			{ false, false, false, false, false, false, false, false, false,
 					false, false, false, false, false, false, false },
 			{ false, false, false, false, false, false, false, false, false,
 					false, false, false, false, false, false, false },
 			{ false, false, false, false, false, false, false, false, false,
 					false, false, false, false, false, false, false } };
 
 	public VonHippelModlet(BundleContext context, int slotId, String moduleId,
 			String moduleName) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleName = moduleName;
 		this.moduleId = moduleId;
 	}
 
 	public void start() throws Exception {
 		moduleRef = context.registerService(IModuleControl.class.getName(),
 				this, createBasicServiceProperties());
 		vhModuleRef = context.registerService(IVonHippelModuleControl.class
 				.getName(), this, createBasicServiceProperties());
 		ledref = context.registerService(IModuleLEDController.class.getName(),
 				this, createBasicServiceProperties());
 		VonHippelWS vhWS = new VonHippelWS(vhDevice);
 		wsMotionTracker = PublicWSAdminTracker.createTracker(context, vhWS);
 		// mdaccRef =
 		// context.registerService(IMDACCModuleControl.class.getName(), this,
 		// createBasicServiceProperties());
 		regionKey = StatusBarUtils.displayImage(context, icon, this
 				.getModuleName());
 	}
 
 	public void stop() throws Exception {
 		StatusBarUtils.releaseRegion(context, regionKey);
 		if (wsMotionTracker != null) {
 			wsMotionTracker.close();
 		}
 
 		if (wsAccTracker != null) {
 			wsAccTracker.close();
 		}
 		// TODO: Throw exception at some point if we encounter a failure
 		if (moduleRef != null) {
 			moduleRef.unregister();
 		}
 
 		if (vhModuleRef != null) {
 			vhModuleRef.unregister();
 		}
 		if (ledref != null) {
 			ledref.unregister();
 		}
 		if (cc !=null){
 			cc.close();
 		}
 	}
 
 	private Properties createBasicServiceProperties() {
 		Properties p = new Properties();
 		p.put("Provider", this.getClass().getName());
 		p.put("Slot", Integer.toString(slotId));
 		// p.put(RemoteOSGiServiceConstants.R_OSGi_REGISTRATION, "true");
 		return p;
 	}
 
 	public List getModuleProperties() {
 		List properties = new ArrayList();
 
 		properties
 				.add(new ModuleProperty(PROPERTY_MODULE_NAME, getModuleName()));
 		properties.add(new ModuleProperty("Slot", "" + slotId));
 		properties.add(new ModuleProperty("State", Boolean.toString(deviceOn),
 				"Boolean", true));
 
 		return properties;
 	}
 
 	public boolean setModuleProperty(IModuleProperty property) {
 		if (!property.isMutable()) {
 			return false;
 		}
 		if (property.getName().equals("State")) {
 			deviceOn = Boolean.valueOf((String) property.getValue())
 					.booleanValue();
 			return true;
 		}
 		return false;
 	}
 
 	public String getModuleName() {
 		return moduleName;
 	}
 
 	public String getModuleId() {
 		return moduleId;
 	}
 
 	public int getSlotId() {
 		return slotId;
 	}
 
 	public void setup() throws Exception {
 		int slot = slotId + 1;
 		String devnode_vh = "/dev/bmi_vh_control_m" + slot;
 		vhDevice = new VonHippel();
 		CharDeviceUtils.openDeviceWithRetry(vhDevice, devnode_vh, 2);
 	    cc = (CommConnection) Connector.open(
 				"comm:/dev/ttymxc/"+ slotId + 
				";baudrate=9600;bitsperchar=8;stopbits=1;parity=none;autocts=off;autorts=off;blocking=off",
 				Connector.READ_WRITE, true);
 	}
 
 	public int LEDGreenOff() throws IOException {
 		if (vhDevice != null) {
 			return vhDevice.ioctl_BMI_VH_GLEDOFF();
 		}
 		return -1;
 	}
 
 	public int LEDGreenOn() throws IOException {
 		if (vhDevice != null) {
 			return vhDevice.ioctl_BMI_VH_GLEDON();
 		}
 		return -1;
 	}
 
 	public int LEDRedOff() throws IOException {
 		if (vhDevice != null) {
 			return vhDevice.ioctl_BMI_VH_RLEDOFF();
 		}
 		return -1;
 	}
 
 	public int LEDRedOn() throws IOException {
 		if (vhDevice != null) {
 			return vhDevice.ioctl_BMI_VH_RLEDON();
 		}
 		return -1;
 	}
 
 	public void clearGPIO(int pin) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_CLRGPIO(pin);
 		}
 
 	}
 
 	public void clearIOX(int pin) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_CLRIOX(pin);
 		}
 
 	}
 
 	public void doADC() throws IOException {
 		//
 		throw new IOException("VonHippelModlet.doADC() is not yet implemented");
 
 	}
 
 	public void doDAC() throws IOException {
 		throw new IOException("VonHippelModlet.doDAC() is not yet implemented");
 
 	}
 
 	public int getRDACResistance() throws IOException {
 		throw new IOException(
 				"VonHippelModlet.getRDACResistance() is not yet implemented");
 	}
 
 	public int getStatus() throws IOException {
 		throw new IOException(
 				"VonHippelModlet.getStatus() is not yet implemented");
 	}
 
 	public void makeGPIOIn(int pin) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_MKGPIO_IN(pin);
 		}
 	}
 
 	public void makeGPIOOut(int pin) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_MKGPIO_OUT(pin);
 		}
 
 	}
 
 	public void makeIOXIn(int pin) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_MKIOX_IN(pin);
 		}
 
 	}
 
 	public void makeIOXOut(int pin) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_MKIOX_OUT(pin);
 		}
 
 	}
 
 	public int readADC() throws IOException {
 		throw new IOException(
 				"VonHippelModlet.readADC() is not yet implemented");
 	}
 
 	public void readDAC() throws IOException {
 		throw new IOException(
 				"VonHippelModlet.readDAC() is not yet implemented");
 
 	}
 
 	public void setGPIO(int pin) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_SETGPIO(pin);
 		}
 
 	}
 
 	public void setIOX(int pin) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_SETIOX(pin);
 		}
 
 	}
 
 	public void setRDACResistance(int resistance) throws IOException {
 		throw new IOException(
 				"VonHippelModlet.setRDACResistance(int resistance) is not yet implemented");
 	}
 
 	public int setLEDGreen(boolean state) throws IOException {
 		if (state) {
 			return LEDGreenOn();
 		} else
 			return LEDGreenOff();
 	}
 
 	public int setLEDRed(boolean state) throws IOException {
 		if (state) {
 			return LEDRedOn();
 		} else
 			return LEDRedOff();
 
 	}
 
 	public InputStream getRS232InputStream() {
 		try {
 		    return (cc.openInputStream());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		 
 		return null;
 	}
 
 	
 
 	public OutputStream getRS232OutputStream() {
 		try {
 		    return (cc.openOutputStream());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		 
 		return null;
 	}
 
 }
