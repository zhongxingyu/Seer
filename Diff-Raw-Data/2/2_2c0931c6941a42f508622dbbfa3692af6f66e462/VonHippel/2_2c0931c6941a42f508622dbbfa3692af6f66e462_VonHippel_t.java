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
 package com.buglabs.bug.jni.vonhippel;
 
 import com.buglabs.bug.jni.common.CharDevice;
 
 public class VonHippel extends CharDevice {
 
 	static {
 		System.loadLibrary("VonHippel");
 	}
 
 	public native int ioctl_BMI_VH_RLEDOFF();
 
 	public native int ioctl_BMI_VH_RLEDON();
 
 	public native int ioctl_BMI_VH_GLEDOFF();
 
 	public native int ioctl_BMI_VH_GLEDON();
 
 	public native int ioctl_BMI_VH_GETSTAT();
 
 	public native int ioctl_BMI_VH_MKGPIO_OUT(int pin);
 
 	public native int ioctl_BMI_VH_MKGPIO_IN(int pin);
 
 	public native int ioctl_BMI_VH_SETGPIO(int pin);
 
 	public native int ioctl_BMI_VH_CLRGPIO(int pin);
 
 	public native int ioctl_BMI_VH_MKIOX_OUT(int pin);
 
 	public native int ioctl_BMI_VH_MKIOX_IN(int pin);
 
 	public native int ioctl_BMI_VH_SETIOX(int pin);
 
 	public native int ioctl_BMI_VH_CLRIOX(int pin);
 
 	/*
 	 * Takes an int, the rightmost bits 8 contain the resistance
 	 */
 	public native int ioctl_BMI_VH_SETRDAC(int resistance);
 
 	/*
 	 * returns an int, the rightmost bits 8 contain the resistance
 	 */
 	public native int ioctl_BMI_VH_RDRDAC();
 
 	/*
 	 * in C : struct vh_adc_wr { // see the datasheet unsigned char w1; //
 	 * VH_ADC_W1_* unsigned char w2; // VH_ADC_W2_* };
 	 */
 	public native int ioctl_BMI_VH_ADCWR(int control);
 
 	/*
 	 * returns an int, with the data returned from conversion
 	 */
 	public native int ioctl_BMI_VH_ADCRD();
 
 	/*
 	 * struct vh_dac_wr { unsigned char w1; // cmd | d[7:3] unsigned char w2; //
 	 * (d[3:0] << 4) || (VH_DAC_CH* | VH_DAC_P*) };
 	 */
 	public native int ioctl_BMI_VH_DACWR(int control);
 
 	/*
 	 * returns an int, with the voltage data returned from conversion in
 	 * rightmost bits
 	 */
	public native int ioctl_BMI_VH_DACRD(int channel);
 
 	/*
 	 * Returns an int containing addr and data first 8 bits, data 1, second 8
 	 * bits, data 2, 3rd 8 bits, addr
 	 * 
 	 * 
 	 */
 	public native int ioctl_BMI_VH_READ_SPI();
 
 	/*
 	 * Takes an int containing addr and data first 8 bits->data 1, second 8
 	 * bits->data 2, 3rd 8 bits->addr
 	 * 
 	 * 
 	 */
 	public native int ioctl_BMI_VH_WRITE_SPI(int control);
 }
