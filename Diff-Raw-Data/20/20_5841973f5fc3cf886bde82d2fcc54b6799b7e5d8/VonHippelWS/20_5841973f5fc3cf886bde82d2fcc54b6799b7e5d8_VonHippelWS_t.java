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
 
 import java.util.List;
 
 import com.buglabs.bug.jni.vonhippel.VonHippel;
 import com.buglabs.services.ws.IWSResponse;
 import com.buglabs.services.ws.PublicWSDefinition;
import com.buglabs.services.ws.PublicWSProvider2;
 import com.buglabs.services.ws.WSResponse;
 import com.buglabs.util.XmlNode;
 
public class VonHippelWS implements PublicWSProvider2 {
 
 	private VonHippel vhioctl;
	private String serviceName = "VonHippel";
 
 	public VonHippelWS(VonHippel vhioctl) {
 		this.vhioctl = vhioctl;
 
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
 			return new WSResponse(getAccelerationXml(), "text/xml");
 		}
 		return null;
 	}
 
 	private Object getAccelerationXml() {
 		XmlNode root = new XmlNode("Status");
 
 		// do some magic here to read the state of the VH module and display it
 		return root.toString();
 	}
 
 	public String getDescription() {
 		return "Returns status of pins on Von Hippel module";
 	}
 
 	public String getPublicName() {
		return serviceName;
	}

	public void setPublicName(String name) {
		serviceName = name;
 	}
 }
