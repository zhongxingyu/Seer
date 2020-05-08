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
 import java.util.List;
 
 import com.buglabs.bug.jni.vonhippel.VonHippel;
 import com.buglabs.bug.module.vonhippel.VonHippelModuleControl;
 import com.buglabs.services.ws.IWSResponse;
 import com.buglabs.services.ws.PublicWSDefinition;
 import com.buglabs.services.ws.PublicWSProvider2;
 import com.buglabs.services.ws.WSResponse;
 import com.buglabs.util.SelfReferenceException;
 import com.buglabs.util.XmlNode;
 
 public class VonHippelWS implements PublicWSProvider2 {
 
 	
 	private String serviceName = "VonHippel";
 	private VonHippelModuleControl vhctl;
 
 	public VonHippelWS(VonHippelModuleControl vhctl) {
 		this.vhctl = vhctl;
 	}
 
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
 
 	public IWSResponse execute(int operation, String input) {
 		if (operation == PublicWSProvider2.GET) {
 			return new WSResponse(getGPIOXml(), "text/xml");
 		}
 		if (operation == PublicWSProvider2.PUT) {
 			
 		}
 		return null;
 	}
 
 	private Object getGPIOXml() {
 		XmlNode root = new XmlNode("Status");
 		try {
 			//gpio.  style = <GPIO>
 			//                <Pin number="0">0</Pin>
 			// 			      <Pin number="1">0</Pin> ...
 			XmlNode gpio = new XmlNode("GPIO");
 			root.addChildElement(gpio);
 			XmlNode pin0 = new XmlNode("Pin", Integer.toString(vhctl.getStatus()>>>8 & 1));
 			XmlNode pin1 = new XmlNode("Pin", Integer.toString(vhctl.getStatus()>>>9 & 1));
 			XmlNode pin2 = new XmlNode("Pin", Integer.toString(vhctl.getStatus()>>>10 & 1));
 			XmlNode pin3 = new XmlNode("Pin", Integer.toString(vhctl.getStatus()>>>11 & 1));
 			pin0.addAttribute("number", "0");
 			pin1.addAttribute("number", "1");
 			pin2.addAttribute("number", "2");
 			pin3.addAttribute("number", "3");
 			gpio.addChildElement(pin0);
 			gpio.addChildElement(pin1);
 			gpio.addChildElement(pin2);
 			gpio.addChildElement(pin3);
 			//iox.  style = <IOX>
 			//                <Pin number="0">0</Pin>
 			// 			      <Pin number="1">0</Pin> ...
 			XmlNode iox = new XmlNode("IOX");
 			root.addChildElement(iox);
			XmlNode ioxpin0 = new XmlNode("Pin", Integer.toString(vhctl.getStatus()>>>12 & 1));
			XmlNode ioxpin1 = new XmlNode("Pin", Integer.toString(vhctl.getStatus()>>>13 & 1));
			XmlNode ioxpin2 = new XmlNode("Pin", Integer.toString(vhctl.getStatus()>>>14 & 1));
			XmlNode ioxpin3 = new XmlNode("Pin", Integer.toString(vhctl.getStatus()>>>15 & 1));
			XmlNode ioxpin4 = new XmlNode("Pin", Integer.toString(vhctl.getStatus()>>>16 & 1));
			XmlNode ioxpin5 = new XmlNode("Pin", Integer.toString(vhctl.getStatus()>>>17 & 1));
 			ioxpin0.addAttribute("number", "0");
 			ioxpin1.addAttribute("number", "1");
 			ioxpin2.addAttribute("number", "2");
 			ioxpin3.addAttribute("number", "3");
 			ioxpin4.addAttribute("number", "4");
 			ioxpin5.addAttribute("number", "5");
 			iox.addChildElement(ioxpin0);
 			iox.addChildElement(ioxpin1);
 			iox.addChildElement(ioxpin2);
 			iox.addChildElement(ioxpin3);
 			iox.addChildElement(ioxpin4);
 			iox.addChildElement(ioxpin5);
 		} catch (SelfReferenceException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return root.toString();
 	}
 
 	public String getDescription() {
 		return "Returns status of various points (GPIO, IOX) on Von Hippel module";
 	}
 
 	public String getPublicName() {
 		return serviceName;
 	}
 
 	public void setPublicName(String name) {
 		serviceName = name;
 	}
 }
