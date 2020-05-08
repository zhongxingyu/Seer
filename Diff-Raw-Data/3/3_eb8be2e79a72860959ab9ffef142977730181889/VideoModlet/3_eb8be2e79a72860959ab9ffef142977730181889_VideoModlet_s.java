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
 package com.buglabs.bug.module.video;
 
 import java.awt.Frame;
 import java.awt.Point;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.log.LogService;
 
 import com.buglabs.bug.module.pub.BMIModuleProperties;
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.bug.module.video.pub.IVideoModuleControl;
 import com.buglabs.module.IModuleControl;
 import com.buglabs.module.IModuleProperty;
 import com.buglabs.module.ModuleProperty;
 import com.buglabs.services.ws.IWSResponse;
 import com.buglabs.services.ws.PublicWSDefinition;
 import com.buglabs.services.ws.PublicWSProvider;
 import com.buglabs.services.ws.PublicWSProviderWithParams;
 import com.buglabs.services.ws.PublicWSProvider2;
 import com.buglabs.services.ws.WSResponse;
 import com.buglabs.util.LogServiceUtil;
 import com.buglabs.util.RemoteOSGiServiceConstants;
 import com.buglabs.util.SelfReferenceException;
 import com.buglabs.util.XmlNode;
 import com.buglabs.bug.sysfs.BMIDeviceHelper;
 import com.buglabs.bug.sysfs.VideoOutDevice;
 
 /**
  * Video Modlet class.
  * 
  * @author dfindlay
  * 
  */
 public class VideoModlet implements IModlet, IVideoModuleControl, IModuleControl, com.buglabs.bug.module.lcd.pub.IModuleDisplay, PublicWSProviderWithParams {
 	private final BundleContext context;
 	private final int slotId;
 	private final String moduleId;
 	private final String moduleName;
 	private String serviceName = "Video";
 	// TODO requires driver (or something else?)to expose size before we can ditch the hardcoded frame size
 	private final int LCD_WIDTH = 320;
 	private final int LCD_HEIGHT = 240;
 	
 	private ServiceRegistration moduleRef;
 	private ServiceRegistration moduleDisplayServReg;
 	private ServiceRegistration videoControlServReg;
 	private LogService log;
 	private Hashtable props;
 	private boolean suspended;
 	protected static final String PROPERTY_MODULE_NAME = "moduleName";
 	private final BMIModuleProperties properties;
 	private ServiceRegistration wsReg;
 	
 	private final VideoOutDevice videoOutDevice;
 	
 	public VideoModlet(BundleContext context, int slotId, String moduleId) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleId = moduleId;
 
 		this.properties = null;
 		this.moduleName = "VIDEO";
 		this.log = LogServiceUtil.getLogService(context);
 		System.out.println("Asking for video out device");
 		this.videoOutDevice = (VideoOutDevice) BMIDeviceHelper.getDevice(slotId);
 	}
 
 	public VideoModlet(BundleContext context, int slotId, String moduleId, BMIModuleProperties properties) {
 		this.context = context;
 		this.slotId = slotId;
 		this.moduleId = moduleId;
 		this.properties = properties;
 		this.moduleName = "VIDEO";
 		this.log = LogServiceUtil.getLogService(context);
 		System.out.println("Asking for video out device");
 		this.videoOutDevice = (VideoOutDevice) BMIDeviceHelper.getDevice(slotId);
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
 		moduleDisplayServReg = context.registerService(com.buglabs.bug.module.lcd.pub.IModuleDisplay.class.getName(), this, createRemotableProperties(props));
 		wsReg = context.registerService(PublicWSProvider.class.getName(), this, null);
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
 			if (properties.getDescription() != null) {
 				p.put("ModuleDescription", properties.getDescription());
 			}
 			if (properties.getSerial_num() != null) {
 				p.put("ModuleSN", properties.getSerial_num());
 			}
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
 			moduleRef.setProperties(modProperties);	public Point getResolution() {
 
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
 			if (properties.getDescription() != null) {
 				mprops.add(new ModuleProperty("ModuleDescription", properties.getDescription()));
 			}
 			if (properties.getSerial_num() != null) {
 				mprops.add(new ModuleProperty("ModuleSN", properties.getSerial_num()));
 			}
 			
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
 		return videoOutDevice.resume() ? 1 : 0;
 	}
 
 	public int suspend() throws IOException {
 		return videoOutDevice.suspend() ? 1 : 0;
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
 
 	public boolean isVGA() {
 		return videoOutDevice.isVGA();
 	}
 
 	public boolean isDVI() {
 		return videoOutDevice.isDVI();
 	}
 
 	public boolean setVGA() {
 		return videoOutDevice.setVGA();
 	}
 
 	public boolean setDVI() {
 		return videoOutDevice.setDVI();
 	}
 
 	@Override
 	public PublicWSDefinition discover(int operation) {
 		if (operation == PublicWSProvider2.GET) {
 			return new PublicWSDefinition() {
 
 				public List getParameters() {
 					return null;
 				}
 
 				public String getReturnType() {
 					return "text/xml";
 				}
 			};
 		}
 
 		return null;
 	}
 
 	@Override
 	public IWSResponse execute(int operation, String input) {
 		// not called because we implement the extended one below
 		return null;
 	}
 
 	@Override
 	public IWSResponse execute(int operation, String input, Map get, Map post) {
 		if (get.containsKey("suspend")) {
 			videoOutDevice.suspend();
 		}
 		if (get.containsKey("resume")) {
 			videoOutDevice.resume();
 		}
 		if (get.containsKey("dvi")) {
 			videoOutDevice.setDVI();
 		}
 		if (get.containsKey("vga")) {
 			videoOutDevice.setVGA();
 		}
 		
 		for (Object key : get.keySet()) {
 			System.out.println(key + "=" + get.get(key));
 		}
 		System.out.println("post map");
 		for (Object key : post.keySet()) {
 			System.out.println(key + "=" + post.get(key));
 		}
 		
 		if (operation == PublicWSProvider2.GET) {
 			return new WSResponse(getVideoInfoXml(), "text/xml");
 		}
 		return null;
 	}
 
 	@Override
 	public String getPublicName() {
 		return serviceName;
 	}
 
 	@Override
 	public String getDescription() {
 		return "This service can return video display information.";
 
 	}
 
 	@Override
 	public void setPublicName(String name) {
 		serviceName = name;
 	}
 	
 	private String getVideoInfoXml() {
 		XmlNode root = new XmlNode("VideoInfo");
 		try {
 			root.addChildElement(new XmlNode("Mode", isVGA() ? "VGA" : "DVI"));
 			root.addChildElement(new XmlNode("Resolution", getResolution()));
 
 		} catch (SelfReferenceException e) {
 			log.log(LogService.LOG_ERROR, "Xml error", e);
 		}
 		return root.toString();
 	}
 
 	@Override
 	public String getResolution() {
 		return videoOutDevice.getResolution();
 	}
 
 }
