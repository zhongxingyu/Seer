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
 package com.buglabs.bug.base;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import com.buglabs.bug.base.pub.IBUG20BaseControl;
 
 /**
  * Impl of IBUG20BaseControl that uses sysfs file API to controle BUGbase LEDs.
  * @author kgilmer
  *
  */
 public class BUGBaseControl implements IBUG20BaseControl {
 	/*
 	 * LEDs in sysfs look like this:
 	 * omap3bug:blue:battery omap3bug:blue:wlan omap3bug:red:battery
 	 * omap3bug:blue:bt omap3bug:green:battery omap3bug:red:wlan
 	 * omap3bug:blue:power omap3bug:green:wlan
 	 */
 	private static final String LED_ROOT = "/sys/class/leds/";
 	private static final String BRIGHTNESS = "/brightness";
 	private static final String BATTERY_BLUE_CONTROL_FILE = LED_ROOT + "omap3bug:blue:battery" + BRIGHTNESS;
 	private static final String BATTERY_RED_CONTROL_FILE = LED_ROOT + "omap3bug:red:battery" + BRIGHTNESS;
 	private static final String BATTERY_GREEN_CONTROL_FILE = LED_ROOT + "omap3bug:green:battery" + BRIGHTNESS;
 	private static final String POWER_BLUE_CONTROL_FILE = LED_ROOT + "omap3bug:blue:power" + BRIGHTNESS;
 	private static final String WLAN_GREEN_CONTROL_FILE = LED_ROOT + "omap3bug:green:wlan" + BRIGHTNESS;
 	private static final String WLAN_RED_CONTROL_FILE = LED_ROOT + "omap3bug:red:wlan" + BRIGHTNESS;
 	private static final String WLAN_BLUE_CONTROL_FILE = LED_ROOT + "omap3bug:blue:wlan" + BRIGHTNESS;
 	private static final String BT_BLUE_CONTROL_FILE = LED_ROOT + "omap3bug:blue:bt" + BRIGHTNESS;
 
 	private OutputStream batteryFH[];
 	private OutputStream powerFH[];
 	private FileOutputStream wlanFH[];
 	private FileOutputStream btFH[];
 
 	public BUGBaseControl() throws FileNotFoundException {
 		batteryFH = new FileOutputStream[3];
 		batteryFH[COLOR_BLUE] = new FileOutputStream(BATTERY_BLUE_CONTROL_FILE);
 		batteryFH[COLOR_RED] = new FileOutputStream(BATTERY_RED_CONTROL_FILE);
 		batteryFH[COLOR_GREEN] = new FileOutputStream(BATTERY_GREEN_CONTROL_FILE);
 
 		powerFH = new FileOutputStream[1];
 		powerFH[COLOR_BLUE] = new FileOutputStream(POWER_BLUE_CONTROL_FILE);
 
 		wlanFH = new FileOutputStream[3];
 		wlanFH[COLOR_BLUE] = new FileOutputStream(WLAN_BLUE_CONTROL_FILE);
 		wlanFH[COLOR_RED] = new FileOutputStream(WLAN_RED_CONTROL_FILE);
 		wlanFH[COLOR_GREEN] = new FileOutputStream(WLAN_GREEN_CONTROL_FILE);
 
		btFH = new FileOutputStream[0];
 		btFH[COLOR_BLUE] = new FileOutputStream(BT_BLUE_CONTROL_FILE);
 	}
 	
 	public void setLEDBrightness(int led, int brightness) throws IOException {
 		if (brightness > 255 || led > 3) {
 			throw new IOException("Invalid LED or brightness parameter value.");
 		}
 		
 		OutputStream [] os = getOutputStream(led);
 		
 		if (os.length > 1) {
 			throw new IOException("LED " + led + " does not support brightness");
 		}
 		
 		writeBrightness(os[0], brightness);
 	}
 
 	public void setLEDColor(int led, int color, boolean on) throws IOException {
 		if (color < 0 || color > 3) {
 			throw new IOException("Color " + color + " is not valid.");
 		}
 		
 		OutputStream [] os = getOutputStream(led);
 		
 		if (os.length != 3) {
 			throw new IOException("LED " + led + " does not allow color to be set.");
 		}
 
 		writeBrightness(os[color], on ? 1 : 0);
 	}
 
 	private void writeBrightness(OutputStream outputStream, int i) throws IOException {
 		outputStream.write(("" + i).getBytes());
 		outputStream.flush();
 	}
 
 	private OutputStream[] getOutputStream(int index) throws IOException {
 		switch (index) {
 		case 0:
 			return batteryFH;
 		case 1:
 			return powerFH;
 		case 2:
 			return wlanFH;
 		case 3:
 			return btFH;
 		default:
 			throw new IOException("LED index out of bounds: " + index);
 		}
 	}
 }
