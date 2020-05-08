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
 package com.buglabs.bug.bmi.pub;
 
 import com.buglabs.bug.module.pub.BMIModuleProperties;
 import com.buglabs.util.StringUtil;
 
 /**
  * A message from the BMI system. Format [moduleId] [version] [slot] [event]
  * 
  * @author ken
  * 
  */
 public class BMIMessage {
 	public static final int EVENT_INSERT = 0;
 
 	public static final int EVENT_REMOVE = 1;
 	
 	private String raw;
 
 	private int slot;
 
 	private int event;
 
 	private String moduleId;
 
 	private String version;
 
 	private BMIModuleProperties props;
 
 	public BMIMessage(String raw) {
 		this.raw = raw;
 	}
 
 	public BMIMessage(String moduleId, String version, int slot, int event) {
 		this.moduleId = moduleId;
 		this.version = version;
 		this.slot = slot;
 		this.event = event;
 	}
 	
 	/**
 	 * A BMIMessage for inserted module with parsed properties.
 	 * @param props
 	 * @param slot
 	 */
 	public BMIMessage(BMIModuleProperties props, int slot) {
 		this.props = props;
 		this.moduleId = props.getProduct_id();
 		this.version = "" + props.getRevision();
 		this.slot = slot;
 		this.event = EVENT_INSERT;
 	}
 
 	public BMIModuleProperties getBMIModuleProperties() {
 		return props;
 	}
 	
 	public void setBMIModuleProperties(BMIModuleProperties p) {
 		this.props = p;
 	}
 	
 	public int getEvent() {
 		return event;
 	}
 
 	public String getModuleId() {
 		return moduleId;
 	}
 
 	public String getRaw() {
 		return raw;
 	}
 
 	public int getSlot() {
 		return slot;
 	}
 
 	public String getVersion() {
 		return version;
 	}
 
 	/**
 	 * See http://lurcher/wiki/BMI_-_Runtime_Interface#Message_Definition
 	 * 
 	 * @return
 	 */
 	public boolean parse() {
 		String[] toks = StringUtil.split(raw, " ");
 
 		if (toks.length != 4) {
 			return false;
 		}
 
 		this.moduleId = toks[0].trim();
 		this.version = toks[1].trim();
 		try {
 			this.slot = Integer.parseInt(toks[2]);
 		} catch (NumberFormatException e) {
 			return false;
 		}
 
 		String action = toks[3].trim().toUpperCase();
 
 		if (action.equals("ADD")) {
 			this.event = BMIMessage.EVENT_INSERT;
 		} else if (action.equals("REMOVE")) {
 			this.event = BMIMessage.EVENT_REMOVE;
 		} else {
 			return false;
 		}
 
 		return true;
 	}
 
 	public String toString() {
 		String eventStr = "";
 
 		switch (event) {
 		case EVENT_INSERT:
 			eventStr = "add";
 			break;
 		case EVENT_REMOVE:
 			eventStr = "remove";
 			break;
 		}
 
 		return moduleId + " " + version + " " + slot + " " + eventStr + "\n";
 	}
 }
