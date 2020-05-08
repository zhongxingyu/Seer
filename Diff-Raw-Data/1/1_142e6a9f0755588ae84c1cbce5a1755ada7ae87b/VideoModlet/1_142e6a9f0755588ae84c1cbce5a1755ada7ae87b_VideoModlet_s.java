 /*******************************************************************************
  * Copyright (c) 2010 Bug Labs, Inc.
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
 package com.buglabs.bug.module.video.pub;
 
 import java.awt.Frame;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Properties;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.log.LogService;
 
 import com.buglabs.bug.module.pub.BMIModuleProperties;
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleProperty;
 import com.buglabs.module.ModuleProperty;
 import com.buglabs.util.LogServiceUtil;
 import com.buglabs.util.RemoteOSGiServiceConstants;
 
 /**
  * Video Modlet class.
  * 
  * @author dfindlay
  * 
  */
 public class VideoModlet implements IModlet, IVideoModuleControl, IModuleControl, IModuleDisplay {
 	private final BundleContext context;
 	private final int slotId;
 	private final String moduleId;
 	private final String moduleName;
 	private final int LCD_WIDTH = 320;
 	private final int LCD_HEIGHT = 240;
 	
 	private ServiceRegistration moduleRef;
 	private ServiceRegistration moduleDisplayServReg;
 	private LogService log;
 	private Hashtable props;
 	private boolean suspended;
 	protected static final String PROPERTY_MODULE_NAME = "moduleName";
 	private final BMIModuleProperties properties;
 	
 	public VideoModlet(BundleContext context, int slotId, String moduleId) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleId = moduleId;
 		this.properties = null;
 		this.moduleName = "VIDEO";
 		this.log = LogServiceUtil.getLogService(context);
 	}
 
 	public VideoModlet(BundleContext context, int slotId, String moduleId, BMIModuleProperties properties) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleId = moduleId;
 		this.properties = properties;
 		this.moduleName = "VIDEO";
 		this.log = LogServiceUtil.getLogService(context);
 	}
 
 	public void setup() throws Exception {
 	}
 
 	public void start() throws Exception {
 		Properties modProperties = createBasicServiceProperties();
 		modProperties.put("Power State", suspended ? "Suspended" : "Active");
 		moduleRef = context.registerService(IModuleControl.class.getName(), this, modProperties);
 
 		props = new Hashtable();
 		props.put("width", new Integer(LCD_WIDTH));
 		props.put("height", new Integer(LCD_HEIGHT));
 		props.put("Slot", "" + slotId);
 
 		videoControlServReg = context.registerService(IVideoModuleControl.class.getName(), this, createRemotableProperties(null));
 		moduleDisplayServReg = context.registerService(IModuleDisplay.class.getName(), this, createRemotableProperties(props));
 	}
 
 	public void stop() throws Exception {
 		moduleRef.unregister();
 		moduleDisplayServReg.unregister();
 	}
 
 	/**
 	 * @return A dictionary with R-OSGi enable property.
 	 */
 	private Dictionary createRemotableProperties(Dictionary ht) {
 		if (ht == null) {
 			ht = new Hashtable();
 			ht.put("Slot", "" + slotId);
 		}
 
 		ht.put(RemoteOSGiServiceConstants.R_OSGi_REGISTRATION, "true");
 
 		return ht;
 	}
 
 	private Properties createBasicServiceProperties() {
 		Properties p = new Properties();
 		p.put("Provider", this.getClass().getName());
 		p.put("Slot", Integer.toString(slotId));
 
 		if (properties != null) {
 			p.put("ModuleDescription", properties.getDescription());
 			p.put("ModuleSN", properties.getSerial_num());
 			p.put("ModuleVendorID", "" + properties.getVendor());
 			p.put("ModuleRevision", "" + properties.getRevision());
 		}
 
 		return p;
 	}
 
 	/*
 	private void updateIModuleControlProperties() {
 		if (moduleRef != null) {
 			Properties modProperties = createBasicServiceProperties();
 			modProperties.put("Power State", suspended ? "Suspended" : "Active");
 			moduleRef.setProperties(modProperties);
 		}
 	}
 	*/
 
 	public List getModuleProperties() {
 		List mprops = new ArrayList();
 		mprops.add(new ModuleProperty("Slot", "" + slotId));
 		mprops.add(new ModuleProperty("Width", "" + LCD_WIDTH));
 		mprops.add(new ModuleProperty("Height", "" + LCD_HEIGHT));
 		mprops.add(new ModuleProperty(PROPERTY_MODULE_NAME, getModuleName()));
 		mprops.add(new ModuleProperty("Power State", suspended ? "Suspended" : "Active", "String", true));
 
 		if (properties != null) {
 			mprops.add(new ModuleProperty("Module Description", properties.getDescription()));
 			mprops.add(new ModuleProperty("Module SN", properties.getSerial_num()));
 			mprops.add(new ModuleProperty("Module Vendor ID", "" + properties.getVendor()));
 			mprops.add(new ModuleProperty("Module Revision", "" + properties.getRevision()));
 		}
 
 		return mprops;
 	}
 
 	public boolean setModuleProperty(IModuleProperty property) {
 		if (!property.isMutable()) {
 			return false;
 		}
 		if (property.getName().equals("State")) {
 			return true;
 		}
 		if (property.getName().equals("Power State")) {
 			if (((String) property.getValue()).equals("Suspend")) {
 
 				try {
 					suspend();
 				} catch (IOException e) {
 					LogServiceUtil.logBundleException(log, "An error occured while changing suspend state.", e);
 				}
 			} else if (((String) property.getValue()).equals("Resume")) {
 
 				try {
 					resume();
 
 				} catch (IOException e) {
 					LogServiceUtil.logBundleException(log, "An error occured while changing suspend state.", e);
 				}
 			}
 
 		}
 
 		return false;
 	}
 
 	public int getSlotId() {
 		return slotId;
 	}
 
 	public int resume() throws IOException {
 		/*
 		int result = -1;
 
 		result = lcdcontrol.ioctl_BMI_LCD_RESUME(slotId);
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_LCD_RESUME failed");
 		}
 
 		suspended = false;
 		updateIModuleControlProperties();
 		return result;
 		*/
 		return 0;
 	}
 
 	public int suspend() throws IOException {
 		/*
 		int result = -1;
 
 		result = lcdcontrol.ioctl_BMI_LCD_SUSPEND(slotId);
 
 		if (result < 0) {
 			throw new IOException("ioctl BMI_LCD_SUSPEND failed");
 		}
 
 		suspended = true;
 		updateIModuleControlProperties();
 		return result;
 		*/
 		return 0;
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
 
 	/*
 	public int setLEDGreen(boolean state) throws IOException {
 		if (state) {
 			return lcdcontrol.ioctl_BMI_LCD_GLEDON(slotId);
 		} else {
 			return lcdcontrol.ioctl_BMI_LCD_GLEDOFF(slotId);
 		}
 	}
 
 	public int setLEDRed(boolean state) throws IOException {
 		if (state) {
 			return lcdcontrol.ioctl_BMI_LCD_RLEDON(slotId);
 		} else {
 			return lcdcontrol.ioctl_BMI_LCD_RLEDOFF(slotId);
 		}
 	}
 	*/
 }
