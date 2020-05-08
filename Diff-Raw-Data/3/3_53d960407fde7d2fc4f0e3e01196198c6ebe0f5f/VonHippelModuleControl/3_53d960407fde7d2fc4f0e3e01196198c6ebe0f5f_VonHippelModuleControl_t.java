 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.bug.module.vonhippel;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.microedition.io.CommConnection;
 import javax.microedition.io.Connector;
 
 import com.buglabs.bug.jni.vonhippel.VonHippel;
 import com.buglabs.bug.module.vonhippel.pub.IVonHippelModuleControl;
 import com.buglabs.bug.module.vonhippel.pub.IVonHippelSerialPort;
 import com.buglabs.module.IModuleLEDController;
 
 public class VonHippelModuleControl implements IVonHippelModuleControl, IModuleLEDController, IVonHippelSerialPort {
 	private VonHippel vhDevice;
 	private CommConnection cc;
 	private InputStream inputStream;
 	private OutputStream outputStream;
 	private final int slotId;
 
 	private String baudrate;
 	private int bitsPerChar;
 	private String stopBits;
 	private String parity;
 	private boolean autoCTS;
 	private boolean autoRTS;
 	private boolean blocking;
 
 	public VonHippelModuleControl(VonHippel vh, int slotId) {
 		vhDevice = vh;
 		this.slotId = slotId;
 
 		// load defaults for comm port.
 		baudrate = "9600";
 		bitsPerChar = 8;
 		stopBits = "1";
 		parity = "none";
 		autoCTS = false;
 		autoRTS = false;
 		blocking = false;
 	}
 
 	/**
 	 * Close input and output streams.  
 	 */
 	protected void dispose() {
 		try {
 			if (inputStream != null) {
 				inputStream.close();
 			}
 			
 			if (outputStream != null) {
 				outputStream.close();
 			}
 		} catch (IOException e) {
 			//Disregard exception
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
 		throw new IOException("VonHippelModlet.getRDACResistance() is not yet implemented");
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
 		throw new IOException("VonHippelModlet.readADC() is not yet implemented");
 	}
 
 	public void readDAC() throws IOException {
 		throw new IOException("VonHippelModlet.readDAC() is not yet implemented");
 
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
 		throw new IOException("VonHippelModlet.setRDACResistance(int resistance) is not yet implemented");
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
 			if (cc == null) {
 				cc = (CommConnection) Connector.open(getCommString(), Connector.READ_WRITE, true);
 			}
 			if (inputStream == null) {
 				inputStream = cc.openInputStream();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return inputStream;
 	}
 
 	public OutputStream getRS232OutputStream() {
 		try {
 			if (cc == null) {
 				cc = (CommConnection) Connector.open(getCommString(), Connector.READ_WRITE, true);
 			}
 			if (outputStream == null) {
 				outputStream = cc.openOutputStream();
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return outputStream;
 	}
 
 	public InputStream getSerialInputStream() throws IOException {
 		if (cc == null) {
 			cc = (CommConnection) Connector.open(getCommString(), Connector.READ_WRITE, true);
 		}
 		if (inputStream == null) {
 			inputStream = cc.openInputStream();
 		}
 
 		return inputStream;
 	}
 
 	public OutputStream getSerialOutputStream() throws IOException {
 		try {
 			if (cc == null) {
 				cc = (CommConnection) Connector.open(getCommString(), Connector.READ_WRITE, true);
 			}
 			if (outputStream == null) {
 				outputStream = cc.openOutputStream();
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return outputStream;
 	}
 
 	/**
 	 * @return
 	 */
 	private String getCommString() {
 		// return "comm:/dev/ttymxc/" + slotId +
 		// ";baudrate=9600;bitsperchar=8;stopbits=1;parity=none;autocts=off;autorts=off;blocking=off";
 		return "comm:/dev/ttymxc/" + slotId + ";baudrate=" + baudrate + ";bitsperchar=" + bitsPerChar + ";stopbits=" + stopBits + ";parity=" + parity + ";autocts=" + boolToStr(autoCTS)
 				+ ";autorts=" + boolToStr(autoRTS) + ";blocking=" + boolToStr(blocking);
 	}
 
 	/**
 	 * @param val
 	 * @return comm string friendly formatting of boolean value
 	 */
 	private String boolToStr(boolean val) {
 		if (val) {
 			return "on";
 		}
 		
 		return "off";
 	}
 	
 	private void checkOpen() throws IOException {
		if (isInputStreamOpen() || isOutputStreamOpen())
			throw new IOException("Serial port connection has already been created.  Unable to set parameters.");
 	}
 
 	public String getBaudrate() {
 		return baudrate;
 	}
 
 	public void setBaudrate(String baudrate) throws IOException {
 		checkOpen();
 		this.baudrate = baudrate;
 	}
 
 	public int getBitsPerChar() {
 		return bitsPerChar;
 	}
 	public String getStopBits() {
 		return stopBits;
 	}
 
 	public void setStopBits(String stopBits) throws IOException {
 		checkOpen();
 		this.stopBits = stopBits;
 	}
 
 	public String getParity() {
 		return parity;
 	}
 
 	public void setParity(String parity) throws IOException {
 		checkOpen();
 		this.parity = parity;
 	}
 
 	public boolean getAutoCTS() {
 		return autoCTS;
 	}
 
 	public void setAutoCTS(boolean autoCTS) throws IOException {
 		checkOpen();
 		this.autoCTS = autoCTS;
 	}
 
 	public boolean getAutoRTS() {
 		return autoRTS;
 	}
 
 	public void setAutoRTS(boolean autoRTS) throws IOException {
 		checkOpen();
 		this.autoRTS = autoRTS;
 	}
 
 	public boolean getBlocking() {
 		return blocking;
 	}
 
 	public void setBlocking(boolean blocking) throws IOException {
 		checkOpen();
 		this.blocking = blocking;
 	}
 
 	public boolean isInputStreamOpen() {
 		return inputStream != null;
 	}
 
 	public boolean isOutputStreamOpen() {
 		return outputStream != null;
 	}
 
 	public void setBitsPerChar(int bitsPerChar) throws IOException {
 		checkOpen();
 		this.bitsPerChar = bitsPerChar;
 	}
 }
