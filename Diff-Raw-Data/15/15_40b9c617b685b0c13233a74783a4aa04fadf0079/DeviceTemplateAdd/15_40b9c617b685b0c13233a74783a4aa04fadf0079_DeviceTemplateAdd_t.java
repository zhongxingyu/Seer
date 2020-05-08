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
  * Name: DeviceTemplateAdd.java
  * Project: SensorAct-VPDS
  * Version: 1.0
  * Date: 2012-04-14
  * Author: Pandarasamy Arjunan
  */
 package edu.pc3.sensoract.vpds.api;
 
 import edu.pc3.sensoract.vpds.api.request.DeviceAddFormat;
 import edu.pc3.sensoract.vpds.constants.Const;
 import edu.pc3.sensoract.vpds.enums.ErrorType;
 import edu.pc3.sensoract.vpds.exceptions.InvalidJsonException;
 
 /**
  * device/add API: Adds a new device profile in the repository.
  * 
  * @author Pandarasamy Arjunan
  * @version 1.0
  */
 public class DeviceTemplateAdd extends DeviceAdd {
 
 	/**
 	 * Services the device/template/add API.
 	 * 
 	 * Adds a new device template to the repository. Sends success or failure,
 	 * in case of any error, response message in Json to the caller.
 	 * 
 	 * @param templateAddJson
 	 *            Device add request attributes in Json string
 	 */
 	public void doProcess(final String templateAddJson) {
 
 		try {
 			DeviceAddFormat newDevice = convertToRequestFormat(templateAddJson,
 					DeviceAddFormat.class);
 
 			validateRequest(newDevice, Const.API_DEVICE_TEMPLATE_ADD);
 
 			if (!userProfile.isRegisteredSecretkey(newDevice.secretkey)) {
 				response.sendFailure(Const.API_DEVICE_TEMPLATE_ADD,
 						ErrorType.UNREGISTERED_SECRETKEY, newDevice.secretkey);
 			}
 
 			if (deviceProfile.isDeviceTemplateExists(newDevice)) {
 				response.sendFailure(Const.API_DEVICE_TEMPLATE_ADD,
 						ErrorType.DEVICE_TEMPLATE_ALREADYEXISTS,
						newDevice.deviceprofile.templatename);
 			}
 			deviceProfile.addDeviceTemplate(newDevice);
 			response.SendSuccess(Const.API_DEVICE_TEMPLATE_ADD,
					Const.DEVICE_TEMPLATE_ADDED, newDevice.deviceprofile.templatename);
 
 		} catch (InvalidJsonException e) {
 			response.sendFailure(Const.API_DEVICE_TEMPLATE_ADD,
 					ErrorType.INVALID_JSON, e.getMessage());
 		} catch (Exception e) {
 			response.sendFailure(Const.API_DEVICE_TEMPLATE_ADD,
 					ErrorType.SYSTEM_ERROR, e.getMessage());
 		}
 
 	}
 }
