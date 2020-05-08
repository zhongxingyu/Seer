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
  * Name: DeviceShare.java
  * Project: SensorAct-VPDS
  * Version: 1.0
  * Date: 2012-05-13
  * Author: Pandarasamy Arjunan
  */
 package edu.pc3.sensoract.vpds.api;
 
 import edu.pc3.sensoract.vpds.api.request.DeviceShareFormat;
 import edu.pc3.sensoract.vpds.api.request.GuardRuleAddFormat;
 import edu.pc3.sensoract.vpds.api.request.GuardRuleAssociationAddFormat;
 import edu.pc3.sensoract.vpds.api.response.DeviceProfileFormat;
 import edu.pc3.sensoract.vpds.constants.Const;
 import edu.pc3.sensoract.vpds.enums.ErrorType;
 import edu.pc3.sensoract.vpds.exceptions.InvalidJsonException;
 import edu.pc3.sensoract.vpds.guardrule.GuardRuleManager;
 import edu.pc3.sensoract.vpds.model.ShareAccessModel;
 
 /**
  * device/share API: Share device profile with others
  * 
  * @author Pandarasamy Arjunan
  * @version 1.0
  */
 public class DeviceShare extends SensorActAPI {
 
 	/**
 	 * Validates the device share request format attributes. If validation
 	 * fails, sends corresponding failure message to the caller.
 	 * 
 	 * @param deviceShareRequest
 	 *            Device share request format object
 	 */
 	private void validateRequest(final DeviceShareFormat deviceShareRequest) {
 
 		validator.validateSecretKey(deviceShareRequest.secretkey);
 		// TODO: add validation for other parameters
 
 		if (validator.hasErrors()) {
 			response.sendFailure(Const.API_DEVICE_SHARE,
 					ErrorType.VALIDATION_FAILED, validator.getErrorMessages());
 		}
 	}
 
 	private void createGuardRule(final DeviceShareFormat req) throws Exception {
 
 		GuardRuleAddFormat guardRule = new GuardRuleAddFormat();
 		GuardRuleAssociationAddFormat association = new GuardRuleAssociationAddFormat();
 
		String grName = null;
 		String accesskey = userProfile.getHashCode(req.brokername
 				+ req.username + req.email);
 
 		guardRule.secretkey = req.secretkey;
 		// TODO: what is the default priority?
 		guardRule.rule.priority = 0;
 		guardRule.rule.condition = "USER.email=='" + req.email + "'";
 		guardRule.rule.action = Const.PARAM_ALLOW;
 		// TODO: include broker name also to uniquely identify the rule name
 		guardRule.rule.name = req.device.devicename + ":" + req.username + ":";
 
 		association.secretkey = req.secretkey;
 		association.devicename = req.device.devicename;
 		association.sensorname = req.device.sensorname;
 		association.sensorid = req.device.sensorid;
 		association.actuatorname = req.device.actuatorname;
 		association.actuatorid = req.device.actuatorid;
 
 		if (req.permission.read) {
			grName = guardRule.rule.name + Const.PARAM_READ;
			guardRule.rule.name = grName;
			guardRule.rule.description = grName;
 			guardRule.rule.targetOperation = Const.PARAM_READ;
 
 			association.rulename = guardRule.rule.name;
 			GuardRuleManager.addGuardRule(guardRule);
 			GuardRuleManager.addAssociation(association);
 
 			// TODO: find duplicate share
 			ShareAccessModel share = new ShareAccessModel(accesskey,
 					req.brokername, req.username, req.email,
 					guardRule.rule.name);
 			share.save();
 		}
 
 		if (req.permission.write) {
			grName = guardRule.rule.name + Const.PARAM_WRITE;
			guardRule.rule.name = grName;
			guardRule.rule.description = grName;
 			guardRule.rule.targetOperation = Const.PARAM_WRITE;
 
 			association.rulename = guardRule.rule.name;
 			GuardRuleManager.addGuardRule(guardRule);
 			GuardRuleManager.addAssociation(association);
 
 			// TODO: find duplicate share
 			ShareAccessModel share = new ShareAccessModel(accesskey,
 					req.brokername, req.username, req.email,
 					guardRule.rule.name);
 			share.save();
 		}
 	}
 
 	private void sharedevice(DeviceShareFormat req) throws Exception {
 
 		// Step 1: Verify the device exists
 		// TODO: verify sensor/actuator also
 		DeviceProfileFormat oneDevice = deviceProfile.getDevice(req.secretkey,
 				req.device.devicename);
 		if (null == oneDevice) {
 			response.sendFailure(Const.API_DEVICE_GET,
 					ErrorType.DEVICE_NOTFOUND, req.device.devicename);
 		}
 
 		// Step 2 : Create a guard rule
 		// Step 3 : Update the table
 		createGuardRule(req);
 	}
 
 	/**
 	 * Services the device/share API.
 	 * 
 	 * @param deviceShareJson
 	 *            Device share request attributes in Json string
 	 */
 	public void doProcess(final String deviceShareJson) {
 
 		try {
 			DeviceShareFormat deviceShareRequest = convertToRequestFormat(
 					deviceShareJson, DeviceShareFormat.class);
 			validateRequest(deviceShareRequest);
 
 			if (!userProfile
 					.isRegisteredSecretkey(deviceShareRequest.secretkey)) {
 				response.sendFailure(Const.API_DEVICE_SHARE,
 						ErrorType.UNREGISTERED_SECRETKEY,
 						deviceShareRequest.secretkey);
 			}
 
 			sharedevice(deviceShareRequest);
 
 			// TODO: share device
			response.SendSuccess(Const.API_DEVICE_SHARE, Const.DEVICE_SHARED );
 
 		} catch (InvalidJsonException e) {
 			response.sendFailure(Const.API_DEVICE_SHARE,
 					ErrorType.INVALID_JSON, e.getMessage());
 		} catch (Exception e) {
 			response.sendFailure(Const.API_DEVICE_SHARE,
 					ErrorType.SYSTEM_ERROR, e.getMessage());
 		}
 	}
 
 }
