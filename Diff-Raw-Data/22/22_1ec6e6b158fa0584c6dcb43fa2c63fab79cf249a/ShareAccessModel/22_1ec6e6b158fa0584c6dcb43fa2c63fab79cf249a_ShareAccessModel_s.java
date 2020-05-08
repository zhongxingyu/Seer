 /*
  * Copyright (c) 2012, Indraprastha Institute of Information Technology,
  * Delhi (IIIT-D) and The Regents of the University of California.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above
  *    copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided
  *    with the distribution.
  * 3. Neither the names of the Indraprastha Institute of Information
  *    Technology, Delhi and the University of California nor the names
  *    of their contributors may be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE IIIT-D, THE REGENTS, AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE IIITD-D, THE REGENTS
  * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  *
  */
 /*
  * Name: DeviceActuatorModel.java
  * Project: SensorAct-VPDS
  * Version: 1.0
  * Date: 2012-04-14
  * Author: Pandarasamy Arjunan
  */
 package edu.pc3.sensoract.vpds.model;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import play.Play;
 import play.modules.morphia.Model;
 
 import com.google.code.morphia.annotations.Entity;
 
 import edu.pc3.sensoract.vpds.api.SensorActAPI;
 import edu.pc3.sensoract.vpds.api.request.DeviceShareFormat;
 import edu.pc3.sensoract.vpds.api.request.GuardRuleAddFormat;
 import edu.pc3.sensoract.vpds.api.request.GuardRuleAssociationAddFormat;
 import edu.pc3.sensoract.vpds.api.request.GuardRuleDeleteFormat;
 import edu.pc3.sensoract.vpds.constants.Const;
 import edu.pc3.sensoract.vpds.guardrule.GuardRuleManager;
 
 /**
  * Model class for device profile (Actuator) management
  * 
  * @author Pandarasamy Arjunan
  * @version 1.0
  */
 @Entity(value = "ShareAccessModel", noClassnameStored = true)
 public class ShareAccessModel extends Model {
 
 	public static class SharedDevice {
 		public String devicename = null;
 		public String sensorname = null;
 		public String sensorid = null;
 		public String actuatorname = null;
 		public String actuatorid = null;
 		public boolean read = false;
 		public boolean write = false;
 		public String guardrulename = null;
 	}
 
 	public String accesskey = null; // created by the broker
 									// hash(broker+username+email);
 	public String brokername = null;
 	public String username = null;
 	public String email = null;
 
 	public List<SharedDevice> shared = new ArrayList<SharedDevice>();
 
 	private ShareAccessModel(final DeviceShareFormat shareReq) {
 		this.accesskey = shareReq.secretkey;
 		this.brokername = shareReq.brokername;
 		this.username = shareReq.username;
 		this.email = shareReq.email;
 		// this.addNewSharedDevice(shareReq);
 	}
 
 	private static SharedDevice newSharedDevice(final DeviceShareFormat shareReq) {
 
 		SharedDevice sharedDevice = new SharedDevice();
 		sharedDevice.devicename = shareReq.share.devicename;
 		sharedDevice.sensorname = shareReq.share.sensorname;
 		sharedDevice.sensorid = shareReq.share.sensorid;
 		sharedDevice.actuatorname = shareReq.share.actuatorname;
 		sharedDevice.actuatorid = shareReq.share.actuatorid;
 		return sharedDevice;
 
 	}
 
 	private static String getNewGuardRuleName(final DeviceShareFormat shareReq,
 			final String operation) {
 
 		// TODO: include broker name also to uniquely identify the rule name
 		String guardRuleName = shareReq.share.devicename + ":"
 				+ shareReq.username + ":";
 
 		return guardRuleName + operation + new Date().getTime();
 	}
 
 	private void delExistingSharedDevice(final DeviceShareFormat shareReq) {
 
 		Iterator<SharedDevice> iterator = this.shared.iterator();
 		while (iterator.hasNext()) {
 			SharedDevice sDevice = iterator.next();
 			if (sDevice.devicename != null
 					&& sDevice.devicename
 							.equalsIgnoreCase(shareReq.share.devicename)
 					&& (sDevice.sensorname != null
 							&& sDevice.sensorname
 									.equalsIgnoreCase(shareReq.share.sensorname)
 							&& sDevice.sensorid != null && sDevice.sensorid
 								.equalsIgnoreCase(shareReq.share.sensorid))
 					|| (sDevice.actuatorname != null
 							&& sDevice.actuatorname
 									.equalsIgnoreCase(shareReq.share.actuatorname)
 							&& sDevice.actuatorid != null && sDevice.actuatorid
 								.equalsIgnoreCase(shareReq.share.actuatorid))) {
 				remvoeGuardRuleAndAssociation(sDevice.guardrulename);
 				iterator.remove();
 			}
 		}
 
 	}
 
 	public static ShareAccessModel getSharedAccess(final String brokername,
 			final String username, final String email) {
 
 		List<ShareAccessModel> shared = ShareAccessModel.q()
 				.filter("brokername", brokername).filter("username", username)
 				.filter("email", email).fetchAll();
 		if (null == shared || shared.size() != 1) {
 			return null;
 		}
 		return shared.get(0);
 	}
 
 	public static void updateShareAccessModel(final DeviceShareFormat shareReq)
 			throws Exception {
 
 		ShareAccessModel sharedAccess = ShareAccessModel.getSharedAccess(
 				shareReq.brokername, shareReq.username, shareReq.email);
 
 		if (null == sharedAccess) { // add new shared access for this user
 			sharedAccess = new ShareAccessModel(shareReq);
 		} else {
 			// delete all the existing share corresponding to the req.device,
 			// sensor|actuator
 			sharedAccess.delExistingSharedDevice(shareReq);
 		}
 
 		if (null == sharedAccess.shared) {
 			sharedAccess.shared = new ArrayList<SharedDevice>();
 		}
 
 		SharedDevice sharedDevice = newSharedDevice(shareReq);
 
 		if (shareReq.share.read) {
 			sharedDevice.read = true;
 			sharedDevice.guardrulename = getNewGuardRuleName(shareReq,
 					Const.PARAM_READ);
 			sharedAccess.shared.add(sharedDevice);
 			addGuardRuleAndAssociation(shareReq, sharedDevice.guardrulename,
 					Const.PARAM_READ);
 		}
 
 		if (shareReq.share.write) {
 			sharedDevice.write = true;
 			sharedDevice.guardrulename = getNewGuardRuleName(shareReq,
 					Const.PARAM_WRITE);
 			sharedAccess.shared.add(sharedDevice);
 			addGuardRuleAndAssociation(shareReq, sharedDevice.guardrulename,
 					Const.PARAM_WRITE);
 		}
		
		sharedAccess.save();
 
 	}
 
 	private void remvoeGuardRuleAndAssociation(final String guardRuleName) {
 
 		GuardRuleDeleteFormat gDel = new GuardRuleDeleteFormat();
 		gDel.secretkey = Play.configuration.getProperty(Const.OWNER_OWNERKEY);
 		gDel.name = guardRuleName;
		System.out.println("deleting guardrule and asso " + gDel.name);
 
 		GuardRuleManager.deleteGuardRule(gDel);
 		GuardRuleManager.deleteRuleAssociations(gDel.secretkey, gDel.name);
 	}
 
 	private static void addGuardRuleAndAssociation(
 			final DeviceShareFormat shareReq, final String guardRuleName,
 			final String targetOperation) {
 
 		GuardRuleAddFormat guardRule = new GuardRuleAddFormat();
 		GuardRuleAssociationAddFormat association = new GuardRuleAssociationAddFormat();
 
 		guardRule.secretkey = shareReq.secretkey;
 		guardRule.rule.priority = 0; // TODO: what is the default priority?
 		guardRule.rule.condition = "USER.email=='" + shareReq.email + "'";
 		guardRule.rule.action = Const.PARAM_ALLOW;
 		guardRule.rule.targetOperation = targetOperation;
 		guardRule.rule.name = guardRuleName;
 		guardRule.rule.description = guardRule.rule.name;
 		GuardRuleManager.addGuardRule(guardRule);
 
 		association.secretkey = shareReq.secretkey;
 		association.rulename = guardRule.rule.name;
 		association.devicename = shareReq.share.devicename;
 		association.sensorname = shareReq.share.sensorname;
 		association.sensorid = shareReq.share.sensorid;
 		association.actuatorname = shareReq.share.actuatorname;
 		association.actuatorid = shareReq.share.actuatorid;
 		GuardRuleManager.addAssociation(association);
 	}
 
 	ShareAccessModel() {
 	}
 }
