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
  * Name: DeviceActuate.java
  * Project: SensorAct-VPDS
  * Version: 1.0
  * Date: 2012-07-10
  * Author: Manaswi Saha
  */
 package edu.pc3.sensoract.vpds.api;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.pc3.sensoract.vpds.api.request.TaskletAddFormat;
 import edu.pc3.sensoract.vpds.api.request.TaskletExecuteFormat;
 import edu.pc3.sensoract.vpds.constants.Const;
 import edu.pc3.sensoract.vpds.enums.ErrorType;
 import edu.pc3.sensoract.vpds.model.TaskletModel;
 import edu.pc3.sensoract.vpds.tasklet.TaskletScheduler;
 import edu.pc3.sensoract.vpds.util.TaskletParamValidator;
 
 /**
  * device/actuate API: Actuates a device at a particular time.
  * 
  * @author Manaswi Saha
  * @version 1.1
  */
 
 public class DeviceActuate extends SensorActAPI {
 
 	TaskletParamValidator taskletvalidator = new TaskletParamValidator();
 
 	/**
 	 * Validates actuateDeviceJson request attributes. If validation fails,
 	 * sends corresponding failure message to the caller.
 	 * 
 	 * @param actuateDeviceJson
 	 *            actuation parameters object to validate
 	 */
 
 	private void validateRequest(final TaskletAddFormat tasklet) {
 
 		taskletvalidator.validateSecretKey(tasklet.secretkey);
 		taskletvalidator.validateTaskletName(tasklet.taskletname);
 		taskletvalidator.validateTaskletDesc(tasklet.desc);
 
 		taskletvalidator.validateParam(tasklet.param);
 		taskletvalidator.validateInput(tasklet.input);
 		taskletvalidator.validateEmail(tasklet.email);
 
 		taskletvalidator.validateWhen(tasklet.when);
 		taskletvalidator.validateExecute(tasklet.execute);
 
 		if (taskletvalidator.hasErrors()) {
 			response.sendFailure(Const.API_DEVICE_ACTUATE,
 					ErrorType.VALIDATION_FAILED,
 					taskletvalidator.getErrorMessages());
 		}
 
 	}
 
 	/**
 	 * Services the actuate API.
 	 * 
 	 * @param actuateDeviceJson
 	 *            actuation parameters in json format
 	 */
 	public final void doProcess(final String actuateDeviceJson) {
 
 		try {
 			TaskletAddFormat tasklet = convertToRequestFormat(
 					actuateDeviceJson, TaskletAddFormat.class);
 
 			validateRequest(tasklet);
 			SensorActAPI.taskletAdd.preProcessTasklet(tasklet);
 			System.out.println("TaskletType:" + tasklet.tasklet_type);
 			System.out.println("Actuation Request Added!");
 
			System.out.println("VPDSUsername:" + tasklet.secretkey);
			
 			String username = null;
 			if (userProfile.isRegisteredSecretkey(tasklet.secretkey)) {
 				username = userProfile.getUsername(tasklet.secretkey);
 			}
 			if (shareProfile.isAccessKeyExists(tasklet.secretkey)) {
 				username = shareProfile.getUsername(tasklet.secretkey);
				System.out.println("VPDSUsername:" + username);
 			}
 			if (null == username) {
				response.sendFailure(Const.API_DEVICE_ACTUATE,
 						ErrorType.UNREGISTERED_SECRETKEY, tasklet.secretkey);
 			}
 
 			// Get tasklet from the schedule
 			TaskletModel taskToSchedule = new TaskletModel(tasklet);
 
 			String taskletId = TaskletScheduler.scheduleTasklet(username,
 					taskToSchedule);
 
 			if (null == taskletId) {
 				response.sendFailure(Const.API_DEVICE_ACTUATE,
 						ErrorType.TASKLET_FAILED_TO_SCHEDULE,
 						tasklet.taskletname);
 			} else if (taskletId.equals(Const.TASKLET_ALREADY_SCHEDULED)) {
 				response.sendFailure(Const.API_DEVICE_ACTUATE,
 						ErrorType.TASKLET_ALREADY_SCHEDULED,
 						tasklet.taskletname);
 			} else {
 				response.SendSuccess(Const.API_DEVICE_ACTUATE,
 						Const.TASKLET_SCHEDULED, taskletId);
 			}
 
 		} catch (Exception e) {
 			response.sendFailure(Const.API_DEVICE_ACTUATE,
 					ErrorType.SYSTEM_ERROR, e.getMessage());
 		}
 	}
 }
