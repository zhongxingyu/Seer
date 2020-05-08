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
 
 import com.buglabs.bug.jni.vonhippel.VonHippel;
 import com.buglabs.bug.module.vonhippel.pub.IVonHippelModuleControl;
 import com.buglabs.module.IModuleLEDController;
 
 public class VonHippelModuleControl implements IVonHippelModuleControl, IModuleLEDController{
 	private VonHippel vhDevice;
 	private final int slotId;
 
 
 	public VonHippelModuleControl(VonHippel vh, int slotId) {
 		vhDevice = vh;
 		this.slotId = slotId;
 
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
  
 
 	public void writeADC(int control) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_ADCWR(control);
 		}
 	}
 	
 	public int readADC() throws IOException {
 		if (vhDevice != null) {
 			return vhDevice.ioctl_BMI_VH_ADCRD();
 		}
 		return -1;
 	}
 
 	public void writeDAC(int digital) throws IOException {
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_DACWR(digital);
 		}
 	}
 	
 	public int readDAC(int channel) throws IOException {
 		if (vhDevice != null) {
 			return vhDevice.ioctl_BMI_VH_DACRD(channel);
 		}
 		return -1;
 	}
 
 	public int getRDACResistance() throws IOException {
 		if (vhDevice != null) {
 			return vhDevice.ioctl_BMI_VH_RDRDAC();
 		}
 		return -1;
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
 		if (vhDevice != null) {
 			vhDevice.ioctl_BMI_VH_SETRDAC(resistance);
 		}
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
 
 }
