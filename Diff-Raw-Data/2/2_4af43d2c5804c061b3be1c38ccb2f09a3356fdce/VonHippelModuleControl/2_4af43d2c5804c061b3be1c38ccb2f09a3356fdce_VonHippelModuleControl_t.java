 package com.buglabs.bug.module.vonhippel;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.microedition.io.CommConnection;
 import javax.microedition.io.Connector;
 
 import com.buglabs.bug.jni.vonhippel.VonHippel;
 
 public class VonHippelModuleControl {
 	
 	private VonHippel vhDevice;
 	private int slotId;
 	private CommConnection cc;
 	public VonHippelModuleControl(VonHippel vh, int slotId){
 		vhDevice = vh;
 		try {
 			cc = (CommConnection) Connector.open("comm:/dev/ttymxc/"+ slotId+ ";baudrate=9600;bitsperchar=8;stopbits=1;parity=none;autocts=off;autorts=off;blocking=off",Connector.READ_WRITE, true);
 		} catch (IOException e) {
 			System.err.println("VonHippelModuleControl: Error opening serial connection...");
 			e.printStackTrace();
 		}
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
 		if (vhDevice != null) {
 			return vhDevice.ioctl_BMI_VH_GETSTAT();
 		}
 		return -1;
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
 
 	public RS232Configuration getRS232Configuration() {
		return null;
 	}
 
 	public void setRS232Configuration(RS232Configuration config){
 		/*this.serialConfig = config;
 		try {
 			cc.close();
 			String configstring = 
 		    
 					"comm:/dev/ttymxc/"+ slotId + 
 					";baudrate="+config.getBaudrate()+";" +
 					"bitsperchar="+config.getBitsperchar()+";" +
 					"stopbits="+config.getStopbits()+";"+
 					"parity="+config.getParity();
 					if (config.isAutocts()){
 						configstring+="autocts=on";
 					}
 					else 
 						configstring+="autocts=off";
 					if (config.is                                                                                                                                                                                                                                                               )
 			"autocts=off;" +
 					"autorts=off;" +
 					"blocking=off",
 					Connector.READ_WRITE, true);
 					
 					cc = (CommConnection) Connector.open(
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 		
 	}
 
 }
