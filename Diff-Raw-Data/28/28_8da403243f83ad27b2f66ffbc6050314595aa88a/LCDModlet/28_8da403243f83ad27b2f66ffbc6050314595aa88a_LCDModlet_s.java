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
 package com.buglabs.bug.module.lcd.pub;
 
 import java.awt.Frame;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Dictionary;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 
 import com.buglabs.bug.bmi.api.AbstractBUGModlet;
 import com.buglabs.bug.bmi.sysfs.BMIDevice;
 import com.buglabs.bug.dragonfly.module.IModuleControl;
import com.buglabs.bug.dragonfly.module.IModuleLEDController;
 import com.buglabs.bug.module.lcd.AccelerationWS;
 import com.buglabs.bug.module.lcd.ML8953AccelerometerImplementation;
 import com.buglabs.services.ws.PublicWSProvider;
 
 /**
  * LCD Modlet class for LCD module.
  * 
  * @author kgilmer
  * 
  */
 public class LCDModlet extends AbstractBUGModlet implements ILCDModuleControl, IModuleDisplay {
 	private final int LCD_WIDTH = 320;
 	private final int LCD_HEIGHT = 200;
 
 	private boolean suspended;
 
 	/**
 	 * TODO change this to use the sysfs API by loading the lcd brightness BMI
 	 * device from the service registry.
 	 */
 	private final String BRIGHTNESS_SYSFS = "/sys/class/backlight/omap-backlight/brightness";
 	private ServiceRegistration moduleRef;
 
 	public LCDModlet(BundleContext context, int slotId, String moduleId, BMIDevice properties2) {
 		super(context, moduleId, properties2, "LCD");
 	}
 
 	public void setup() throws Exception {
 		// TODO: no ioctl's for 2.0. Perhaps we want to poll sysfs for initial
 		// info here?
 	}
 
 	public void start() throws Exception {
 		Dictionary modProperties = getCommonProperties();
 		modProperties.put("Power State", suspended ? "Suspended" : "Active");
 		moduleRef = context.registerService(IModuleControl.class.getName(), this, modProperties);
 
 		ML8953AccelerometerImplementation ml8953Control = new ML8953AccelerometerImplementation();
 
 		registerService(IML8953Accelerometer.class.getName(), ml8953Control, modProperties);
 		registerService(PublicWSProvider.class.getName(), new AccelerationWS(ml8953Control, getLog()), modProperties);
 		registerService(ILCDModuleControl.class.getName(), this, createExtendedServiceProperties());
 		registerService(IModuleDisplay.class.getName(), LCDModlet.this, createExtendedServiceProperties());
 	}
 
 	public void stop() throws Exception {
 		moduleRef.unregister();
 		super.stop();
 	}
 
 	/**
 	 * @return
 	 */
 	private Dictionary createExtendedServiceProperties() {
 		Dictionary p = getCommonProperties();
 
 		p.put("width", new Integer(LCD_WIDTH));
 		p.put("height", new Integer(LCD_HEIGHT));
 
 		return p;
 	}
 
 	/**
 	 * 
 	 */
 	private void updateIModuleControlProperties() {
 		if (moduleRef != null) {
 			Dictionary modProperties = getCommonProperties();
 			modProperties.put("Power State", suspended ? "Suspended" : "Active");
 			moduleRef.setProperties(modProperties);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.buglabs.bug.dragonfly.module.IModuleControl#resume()
 	 */
 	public int resume() throws IOException {
 		getBMIDevice().resume();
 
 		suspended = false;
 		updateIModuleControlProperties();
 		return 1;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.buglabs.bug.dragonfly.module.IModuleControl#suspend()
 	 */
 	public int suspend() throws IOException {
 		getBMIDevice().suspend();
 
 		suspended = true;
 		updateIModuleControlProperties();
 		return 1;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.buglabs.bug.module.lcd.pub.IModuleDisplay#getFrame()
 	 */
 	public Frame getFrame() {
 		Frame frame = new Frame();
 		frame.setSize(LCD_WIDTH, LCD_HEIGHT);
 		frame.setResizable(false);
 		frame.setVisible(true);
 		return frame;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.buglabs.bug.module.lcd.pub.ILCDModuleControl#setBackLight(int)
 	 */
 	public int setBackLight(int val) throws IOException {
 		if (val < 0 || val > 100) {
			throw new IllegalArgumentException();
 		}
 
 		return writeToSysfs(new File(BRIGHTNESS_SYSFS), String.valueOf(val));
 
 	}
 
 	/**
 	 * TODO: replace this with sysfs API.
 	 * 
	 * @param sysfsEntry
	 * @param value
	 * @return
	 * @throws IOException
 	 */
 	private int writeToSysfs(File sysfsEntry, String value) throws IOException {
 		BufferedWriter out;
 
 		out = new BufferedWriter(new FileWriter(sysfsEntry));
 		out.write(String.valueOf(value));
 		out.close();
		return 1;
 	}
 
 	@Override
 	public boolean isSuspended() {
 		return suspended;
 	}
 }
