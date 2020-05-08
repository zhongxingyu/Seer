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
  * Name: DeviceAdd.java
  * Project: SensorAct-VPDS
  * Version: 1.0
  * Date: 2012-04-14
  * Author: Pandarasamy Arjunan
  */
 package edu.pc3.sensoract.vpds.api;
 
 import java.util.Iterator;
 import java.util.List;
 
 import edu.pc3.sensoract.vpds.api.request.DeviceAddFormat;
 import edu.pc3.sensoract.vpds.api.request.DeviceAddFormat.DeviceActuator;
 import edu.pc3.sensoract.vpds.api.request.DeviceAddFormat.DeviceChannel;
 import edu.pc3.sensoract.vpds.api.request.DeviceAddFormat.DeviceFormat;
 import edu.pc3.sensoract.vpds.api.request.DeviceAddFormat.DeviceSensor;
 import edu.pc3.sensoract.vpds.constants.Const;
 import edu.pc3.sensoract.vpds.enums.ErrorType;
 import edu.pc3.sensoract.vpds.exceptions.InvalidJsonException;
 
 /**
  * device/add API: Adds a new device to the repository.
  * 
  * @author Pandarasamy Arjunan
  * @version 1.0
  */
 public class DeviceAdd extends SensorActAPI {
 
 	/**
 	 * Validates device profile's channel attributes. If validation fails, sends
 	 * corresponding failure message to the caller.
 	 * 
 	 * @param listChannels
 	 *            List of channel objects to validate
 	 * @param sensorIndex
 	 *            Index of the sensor that contains this channel list
 	 */
 	private void validateChannels(final List<DeviceChannel> listChannels,
 			final int sensorIndex) {
 
 		if (null == listChannels) {
 			validator.addError(Const.PARAM_CHANNELS, Const.PARAM_DEVICEPROFILE
 					+ "." + Const.PARAM_SENSORS + "[" + sensorIndex + "]."
 					+ Const.PARAM_CHANNELS + Const.MSG_REQUIRED);
 			return;
 		}
 
 		if (0 == listChannels.size()) {
 			validator.addError(Const.PARAM_CHANNELS, Const.PARAM_DEVICEPROFILE
 					+ "." + Const.PARAM_SENSORS + "[" + sensorIndex + "]."
 					+ Const.PARAM_CHANNELS + Const.MSG_EMPTY);
 			return;
 		}
 
 		Iterator<DeviceChannel> iteratorChannels = listChannels.iterator();
 		int channelIndex = 1;
 		while (iteratorChannels.hasNext()) {
 			DeviceChannel channel = iteratorChannels.next();
 			validator.validateDeviceProfileChannelName(channel.name,
 					sensorIndex, channelIndex);
 			validator.validateDeviceProfileChannelType(channel.type,
 					sensorIndex, channelIndex);
 			validator.validateDeviceProfileChannelUnit(channel.unit,
 					sensorIndex, channelIndex);
 			validator.validateDeviceProfileChannelSamplingPeriod(
 					channel.samplingperiod, sensorIndex, channelIndex);
 			++channelIndex;
 		}
 
 	}
 
 	/**
 	 * Validates device profile's sensor attributes. If validation fails, sends
 	 * corresponding failure message to the caller.
 	 * 
 	 * @param listSensors
 	 *            List of sensor objects to validate
 	 */
 	private void validateSensors(final List<DeviceSensor> listSensors) {
 
 		if (0 == listSensors.size()) {
 			validator.addError(Const.PARAM_SENSORS, Const.PARAM_DEVICEPROFILE
 					+ "." + Const.PARAM_SENSORS + Const.MSG_EMPTY);
 			return;
 		}
 
 		Iterator<DeviceSensor> iteratorSensors = listSensors.iterator();
 		int sensorIndex = 1;
 		while (iteratorSensors.hasNext()) {
 			DeviceSensor sensor = iteratorSensors.next();
 			validator.validateDeviceProfileSensorName(sensor.name, sensorIndex);
 			validator.validateDeviceProfileSensorId(sensor.sid, sensorIndex);
 			validateChannels(sensor.channels, sensorIndex);
 			++sensorIndex;
 		}
 	}
 
 	/**
 	 * Validates device profile's actuator attributes. If validation fails,
 	 * sends corresponding failure message to the caller.
 	 * 
 	 * @param listActuators
 	 *            List of actuator objects to validate
 	 */
 	private void validateActuators(final List<DeviceActuator> listActuators) {
 
 		if (0 == listActuators.size()) {
 			validator.addError(Const.PARAM_ACTUATORS, Const.PARAM_DEVICEPROFILE
 					+ "." + Const.PARAM_ACTUATORS + Const.MSG_EMPTY);
 			return;
 		}
 
 		Iterator<DeviceActuator> iteratorActuators = listActuators.iterator();
 		int actuatorIndex = 1;
 		while (iteratorActuators.hasNext()) {
 			DeviceActuator actuator = iteratorActuators.next();
 			validator.validateDeviceProfileActuatorName(actuator.name,
 					actuatorIndex);
 			++actuatorIndex;
 		}
 	}
 
 	/**
 	 * Validates device profile attributes. If validation fails, sends
 	 * corresponding failure message to the caller.
 	 * 
 	 * @param newDevice
 	 *            Device profile object to validate
 	 * 
 	 * @param apiname
 	 *            Name of the API
 	 */
 	protected void validateRequest(final DeviceAddFormat newDevice,
 			final String apiname) {
 
 		validator.validateSecretKey(newDevice.secretkey);
 
 		if (null == newDevice.deviceprofile) {
 			validator.addError(Const.PARAM_DEVICEPROFILE,
 					Const.PARAM_DEVICEPROFILE + Const.MSG_REQUIRED);
 		} else {
 
 			DeviceFormat deviceProfile = newDevice.deviceprofile;
 			List sensors = deviceProfile.sensors;
 			List actuators = deviceProfile.actuators;
 
			if(Const.API_DEVICE_ADD.equalsIgnoreCase(apiname)) {
				validator.validateDeviceProfileDeviceName(deviceProfile.devicename);	
			}
			if(Const.API_DEVICE_TEMPLATE_ADD.equalsIgnoreCase(apiname)) {
				validator.validateDeviceProfileTemplateName(deviceProfile.templatename);	
			}			
			
 			validator.validateDeviceProfileLocation(deviceProfile.location);
 			validator.validateDeviceProfileTags(deviceProfile.tags);
 			validator.validateDeviceProfileLatitude(deviceProfile.latitude);
 			validator.validateDeviceProfileLongitude(deviceProfile.longitude);
 
 			if (null != actuators && actuators.size() > 0) {
 				validator.validateDeviceProfileIP(deviceProfile.IP);
 			}
 
 			if (null == sensors && null == actuators) {
 				validator.addError(Const.PARAM_DEVICEPROFILE,
 						Const.PARAM_DEVICEPROFILE + "." + Const.PARAM_SENSORS
 								+ " or " + Const.PARAM_DEVICEPROFILE + "."
 								+ Const.PARAM_ACTUATORS + Const.MSG_REQUIRED);
 			} else {
 				if (null != actuators) {
 					validateActuators(actuators);
 				}
 				if (null != sensors) {
 					validateSensors(sensors);
 				}
 			}
 		}
 
 		if (validator.hasErrors()) {
 			response.sendFailure(apiname, ErrorType.VALIDATION_FAILED,
 					validator.getErrorMessages());
 		}
 
 	}
 
 	/**
 	 * Services the device/add API.
 	 * 
 	 * Adds a new device to the repository. Sends success or failure, in case of
 	 * any error, response message in Json to the caller.
 	 * 
 	 * @param deviceAddJson
 	 *            Device add request attributes in Json string
 	 */
 	public void doProcess(final String deviceAddJson) {
 
 		try {
 			DeviceAddFormat newDevice = convertToRequestFormat(deviceAddJson,
 					DeviceAddFormat.class);
 
 			validateRequest(newDevice, Const.API_DEVICE_ADD);
 
 			if (!userProfile.isRegisteredSecretkey(newDevice.secretkey)) {
 				response.sendFailure(Const.API_DEVICE_ADD,
 						ErrorType.UNREGISTERED_SECRETKEY, newDevice.secretkey);
 			}
 
 			if (deviceProfile.isDeviceExists(newDevice)) {
 				response.sendFailure(Const.API_DEVICE_ADD,
 						ErrorType.DEVICE_ALREADYEXISTS,
 						newDevice.deviceprofile.devicename);
 			}
 			deviceProfile.addDevice(newDevice);
 			response.SendSuccess(Const.API_DEVICE_ADD, Const.DEVICE_ADDED,
 					newDevice.deviceprofile.devicename);
 
 		} catch (InvalidJsonException e) {
 			response.sendFailure(Const.API_DEVICE_ADD, ErrorType.INVALID_JSON,
 					e.getMessage());
 		} catch (Exception e) {
 			response.sendFailure(Const.API_DEVICE_ADD, ErrorType.SYSTEM_ERROR,
 					e.getMessage());
 		}
 
 	}
 }
