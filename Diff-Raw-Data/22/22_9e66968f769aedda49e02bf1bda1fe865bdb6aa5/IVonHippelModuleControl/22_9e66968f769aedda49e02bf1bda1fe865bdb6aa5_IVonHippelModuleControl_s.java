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
 package com.buglabs.bug.module.vonhippel.pub;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  * 
  * @author jconnolly
  * 
  * Interface to control Von Hippel module. For complete module docmentation see:
  * http://bugcommunity.com/wiki/images/f/f2/BUG2_vonHippel_Module_Design_Spec_wiki_2008_08_17.pdf
  * 
  */
 public interface IVonHippelModuleControl {
 	//ADC bytes for control word 1 (for initiating conversion)
 	//These values should lie in the int passed into writeADC(control), and
 	//should reside in bits 0-7 (rightmost byte).
 	public static final int  VH_ADC_W1_EN   =   0xA0 & 0x000000FF;			
 	public static final int  VH_ADC_W1_CH01 =	0x00 & 0x000000FF;			// diff - 0, 1
 	public static final int  VH_ADC_W1_CH23	=	0x01 & 0x000000FF;			// diff - 2, 3
 	public static final int  VH_ADC_W1_CH10	=	0x08 & 0x000000FF;			// diff - 1, 0
 	public static final int  VH_ADC_W1_CH32	=	0x09 & 0x000000FF;			// diff - 3, 2
 	public static final int  VH_ADC_W1_CH0	=	0x10 & 0x000000FF;			// single-ended - 0
 	public static final int  VH_ADC_W1_CH1	=	0x18 & 0x000000FF;			// single-ended - 1
 	public static final int  VH_ADC_W1_CH2	=	0x11 & 0x000000FF;			// single-ended - 2
 	public static final int  VH_ADC_W1_CH3	=	0x19 & 0x000000FF;			// single-ended - 3
 	//ADC bytes for word 2 (8 bits (8-15) of control int)
 	public static final int  VH_ADC_W2_EN	=	0x80<<8 & 0x0000FF00;			// Word 2 Enable
 	public static final int  VH_ADC_W2_IM	=	0x40<<8 & 0x0000FF00;			// internal temp
 	public static final int  VH_ADC_W2_SPD	=	0x08<<8 & 0x0000FF00;			// speed 2X
 	
 	//DAC values to be passed into writeDAC
 	public static final int VH_DAC_W1_UA	=	0x00;	// update DAC A output
 	public static final int VH_DAC_W1_UB	=	0x10;	// update DAC B output
 	public static final int VH_DAC_W1_LA	=	0x40;	// load DAC A input
 	public static final int VH_DAC_W1_LB	=	0x50;	// load DAC B input
 	public static final int VH_DAC_W1_ALLA	=	0x80;	// load DAC A input, update all outputs
 	public static final int VH_DAC_W1_ALLB	=	0x90;	// load DAC B input, update all outputs
 	public static final int VH_DAC_W1_ALL	=	0xC0;	// load all inputs, update all outputs
 	public static final int VH_DAC_W1_ALLI	=	0xD0;	// load all inputs
 	public static final int VH_DAC_W1_UALL	=	0xE0;	// update all - don't send data
 	public static final int VH_DAC_W1_EC	=	0xF0;	// Extended command
 	public static final int	VH_DAC_BCH		=	0x0C;	// both channel A & B
 	public static final int	VH_DAC_CHB		=	0x08;	// channel B
 	public static final int	VH_DAC_CHA		=	0x04;	// channel A
 	public static final int	VH_DAC_PD100K	=	0x03;	// power down - 100K pull down
 	public static final int	VH_DAC_PD1K		=	0x02;	// power down - 1K pull down
 	public static final int	VH_DAC_PDF		=	0x01;	// power down - float
 	public static final int	VH_DAC_PU		=	0x00;	// power up
 	
 	//DAC values to be passed into readDAC, currently not accepting params.
 	//This needs to be factored into the library, as DACRD doesn't currently
 	//accept params either.
 	public static final int VH_DAC_W1_RDA	=	0xF1;	// Read A
 	public static final int VH_DAC_W1_RDB	=	0xF2;	// Read B
 	
 	
 	/**
 	 * Set module's Red LED off. Note that the state of the LED will also be
 	 * reflected in GPIO pin 3
 	 * 
 	 * @return value returned by ioctl.
 	 * @throws IOException
 	 */
 	public int LEDRedOff() throws IOException;
 
 	/**
 	 * Set module's Red LED on. Note that the state of the LED will also be
 	 * reflected in GPIO pin 3
 	 * 
 	 * @return value returned by ioctl.
 	 * @throws IOException
 	 */
 	public int LEDRedOn() throws IOException;
 
 	/**
 	 * Set module's Green LED off. Note that the state of the LED will also be
 	 * reflected in GPIO pin 2
 	 * 
 	 * @return value returned by ioctl.
 	 * @throws IOException
 	 */
 	public int LEDGreenOff() throws IOException;
 
 	/**
 	 * Set module's Green LED on. Note that the state of the LED will also be
 	 * reflected in GPIO pin 2
 	 * 
 	 * @return value returned by ioctl.
 	 * @throws IOException
 	 */
 	public int LEDGreenOn() throws IOException;
 
 	/**
 	 * Returns status of module. As passed up from ioctl. for information about
 	 * the format please see line 500 at
 	 * bug-linux-2.6.27.2/drivers/bmi/pims/vonhippel/bmi_vh.c
 	 * 
 	 * @return integer containing status information of GPIO and IOX bits
 	 * @throws IOException
 	 */
 	public int getStatus() throws IOException;
 
 	/**
 	 * Sets specified pin as an output pin. Note that the driver sets pins 2 and
 	 * 3 as out by default
 	 * 
 	 * @param pin
 	 *            to set as Out
 	 * @return value returned by ioctl.
 	 * @throws IOException
 	 */
 	public void makeGPIOOut(int pin) throws IOException;
 
 	/**
 	 * Sets specified pin as an in pin. Note that the driver sets pins 0 and 1
 	 * as in by default
 	 * 
 	 * @param pin
 	 *            to set as Out
 	 * @return value returned by ioctl.
 	 * @throws IOException
 	 */
 	public void makeGPIOIn(int pin) throws IOException;
 
 	/**
 	 * Sets specified pin as high
 	 * 
 	 * @param pin
 	 *            to set as high
 	 * @return value returned by ioctl.
 	 * @throws IOException
 	 */
 	public void setGPIO(int pin) throws IOException;
 
 	/**
 	 * Sets specified pin as low
 	 * 
 	 * @param pin
 	 *            to set as low
 	 * @return value returned by ioctl.
 	 * @throws IOException
 	 */
 	public void clearGPIO(int pin) throws IOException;
 
 	/**
 	 * Sets specified IOX pin as an out pin
 	 * 
 	 * @param IOX
 	 *            pin to set as Out
 	 * @return value returned by ioctl.
 	 * @throws IOException
 	 */
 	public void makeIOXOut(int pin) throws IOException;
 
 	/**
 	 * Sets specified IOX pin as an in pin
 	 * 
 	 * @param IOX
 	 *            pin to set as in
 	 * @return value returned by ioctl.
 	 * @throws IOException
 	 */
 	public void makeIOXIn(int pin) throws IOException;
 
 	/**
 	 * Sets specified IOX pin as high. Note that pin 0 will set Red LED on (not
 	 * GPIO red), 1 will set yellow LED on, 2 will set green LED on (not GPIO
 	 * LED), 3 will set blue LED on.
 	 * 
 	 * @precondition pin should be set as out
 	 * @param IOX
 	 *            pin to set as high
 	 * @return value returned by ioctl.
 	 * @throws IOException
 	 */
 	public void setIOX(int pin) throws IOException;
 
 	/**
 	 * Sets specified IOX pin as high. Note that pin 0 will set Red LED off (not
 	 * GPIO red), 1 will set yellow LED off, 2 will set green LED off (not GPIO
 	 * LED), 3 will set blue LED off.
 	 * 
 	 * @precondition pin should be set as out
 	 * @param IOX
 	 *            pin to set as low
 	 * @return value returned by ioctl.
 	 * @throws IOException
 	 */
 	public void clearIOX(int pin) throws IOException;
 
 	/**
 	 * Set RDAC to adjust LDO voltage
 	 * 
 	 * @param resistance
 	 *            value
 	 * @throws IOException
 	 */
 	public void setRDACResistance(int resistance) throws IOException;
 
 	/**
 	 * Get RDAC resistance value from ioctl
 	 * 
 	 * @param resistance
 	 *            value
 	 * @throws IOException
 	 * @returns rdac value
 	 */
 	public int getRDACResistance() throws IOException;
 
 	
 	/**
 	 * @param control the control word to be written to ADC.
 	 * Used to initiate ADC conversion and also configure
 	 * ADC device for ADC single-ended or differential, among
 	 * other things.  see bmi_vh.h and bmi_vh.c in kernel.
 	 * @throws IOException
 	 */
 	public void writeADC(int control) throws IOException;
 	
 	
 	/**
 	 * Read value after last conversion.
 	 */
 	public int readADC() throws IOException;
 	
 	/**
 	 * @param digital the control word encoded digital
 	 * input to be encoded as analog out.  See datasheet and
 	 * bmi_vh.h and bmi_vh.c in kernel.
 	 * @throws IOException
 	 */
 	public void writeDAC(int digital) throws IOException;
 	
 	/**
 	 * 
	 * Read channel A after last conversion.  This should be
	 * refactored to accept param to determine which channel 
	 * is read back.
 	 */
	public int readDAC() throws IOException;
 	
 	
 
 	
 	/**
 	 * Gets the input stream associated with the RS232 port on Von Hippel
 	 * module. This implementation is based on the javax.microedition.commports
 	 * API. The port is set up with the following parameters in
 	 * VonHippelModuleControl: baudrate=9600 bitsperchar=8 stopbits=1
 	 * parity=none autocts=off autorts=off blocking=off
 	 * 
 	 * @deprecated
 	 * @return stream associated with RS232 input (reading)
 	 */
 	public InputStream getRS232InputStream();
 
 	/**
 	 * Gets the output stream associated with the RS232 port on Von Hippel
 	 * module. This implementation is based on the javax.microedition.commports
 	 * API. The port is set up with the following parameters in
 	 * VonHippelModuleControl: baudrate=9600 bitsperchar=8 stopbits=1
 	 * parity=none autocts=off autorts=off blocking=off
 	 * @deprecated
 	 * @return stream associated with RS232 output (writing)
 	 */
 	public OutputStream getRS232OutputStream();
 
 }
