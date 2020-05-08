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
  * Name: DataUploadWaveSegment.java
  * Project: SensorAct-VPDS 
  * Version: 1.0
  * Date: 2012-04-14
  * Author: Pandarasamy Arjunan
  */
 package edu.pc3.sensoract.vpds.api;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Observer;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
import play.Play;
 import play.data.validation.Error;
 import edu.pc3.sensoract.vpds.api.request.WaveSegmentFormat;
 import edu.pc3.sensoract.vpds.constants.Const;
 import edu.pc3.sensoract.vpds.enums.ErrorType;
 import edu.pc3.sensoract.vpds.exceptions.InvalidJsonException;
 import edu.pc3.sensoract.vpds.model.WaveSegmentModel;
 import edu.pc3.sensoract.vpds.model.rdbms.WaveSegmentRModel;
 import edu.pc3.sensoract.vpds.tasklet.DeviceEvent;
 import edu.pc3.sensoract.vpds.tasklet.DeviceEventListener;
 import edu.pc3.sensoract.vpds.util.SensorActLogger;
 
 /**
  * data/upload/wavesegment API: Uploads the wave segments sent by a device to
  * repository.
  * 
  * @author Pandarasamy Arjunan
  * @version 1.0
  */
 public class DataUploadWaveSegment extends SensorActAPI {
 
 	private static int WaveSegmentSize = 5;
 	private static boolean isSendResponseEnabled = true;
 	private static Logger uploadLog = Logger.getLogger("UploadLogger");
 
 	private HashMap<String, ArrayList<WaveSegmentFormat>> hashmapWaveSegments = new HashMap<String, ArrayList<WaveSegmentFormat>>();
 
 	public DataUploadWaveSegment() {
 		super();
 	}
 
 	/**
 	 * Sends error message to the callel.
 	 * 
 	 * @param errorType
 	 *            ErrorType object contains the status code and message
 	 * @param msg
 	 *            Error message
 	 */
 	private void sendError(ErrorType errorType, String msg) {
 		if (isSendResponseEnabled) {
 			response.sendFailure(Const.API_DATA_UPLOAD_WAVESEGMENT, errorType,
 					msg);
 		} else {
 			response.sendEmpty(); // to complete the request
 		}
 	}
 
 	private void validateWaveSegmentData(final WaveSegmentFormat waveSegment) {
 
 		WaveSegmentFormat.DeviceData data = waveSegment.data;
 
 		if (null == data) {
 			validator.addError(Const.PARAM_WS_DATA, Const.PARAM_WS_DATA
 					+ Const.MSG_REQUIRED);
 			return;
 		}
 
 		validator.validateWaveSegmentDeviceName(data.dname);
 		validator.validateWaveSegmentSensorName(data.sname);
 		validator.validateWaveSegmentSensorId(data.sid);
 		//validator.validateWaveSegmentSInterval(data.sinterval);
 		validator.validateWaveSegmentTimestamp(data.timestamp);
 
 		if (null == data.channels || data.channels.isEmpty()) {
 			validator.addError(Const.PARAM_WS_CHANNELS, Const.PARAM_WS_DATA
 					+ "." + Const.PARAM_WS_CHANNELS + Const.MSG_REQUIRED);
 			return;
 		}
 
 		for (int cIndex = 0; cIndex < data.channels.size(); ++cIndex) {
 			WaveSegmentFormat.Channels channel = data.channels.get(cIndex);
 			validator.validateWaveSegmentChannelName(channel.cname, cIndex);
 			validator.validateWaveSegmentChannelUnit(channel.unit, cIndex);
 
 			if (null == channel.readings || channel.readings.isEmpty()) {
 				validator.addError(Const.PARAM_WS_READINGS, Const.PARAM_WS_DATA
 						+ "." + Const.PARAM_WS_CHANNELS + "[" + cIndex + "]."
 						+ Const.PARAM_WS_READINGS + Const.MSG_REQUIRED);
 				return;
 			}
 
 			// TODO: check the type of data and any error in each readings
 		}
 
 	}
 
 	/**
 	 * Validates the wave segment attributes. If validation fails, sends
 	 * corresponding failure message, if enabled, to the caller.
 	 * 
 	 * @param waveSegment
 	 *            Wave segment of a sensor sent by a device
 	 */
 	private void validateWaveSegment(final WaveSegmentFormat waveSegment) {
 
 		// TODO: Add validation for other attributes as well.
 		validator.validateSecretKey(waveSegment.secretkey);
 		validateWaveSegmentData(waveSegment);
 
 		if (validator.hasErrors()) {
 			sendError(ErrorType.VALIDATION_FAILED, validator.getErrorMessages());
 		}
 	}
 
 	private void verifyWaveSegment(final WaveSegmentFormat waveSegment) {
		
		String secretkey = Play.configuration
				.getProperty(Const.OWNER_UPLOADKEY);
 
		if (!secretkey.equals(waveSegment.secretkey)) {
 			response.sendFailure(Const.API_DATA_UPLOAD_WAVESEGMENT,
 					ErrorType.UNREGISTERED_SECRETKEY, waveSegment.secretkey);
 
 		}
 		
 		// TODO: verifty the device parameters
 	}
 
 	/**
 	 * Store the wave segment to the repository.
 	 * 
 	 * @param waveSegment
 	 *            Wave segment of a sensor sent by a device
 	 */
 	public void persistWaveSegment(final WaveSegmentFormat waveSegment) {
 
 		// WaveSegmentRModel ws = new WaveSegmentRModel(waveSegment);
 		// ws.save();
 
 		WaveSegmentModel waveSegmentModel = new WaveSegmentModel(waveSegment);
 		waveSegmentModel.save();
 
 		// System.out.println(System.currentTimeMillis()/1000 + " "
 		// + waveSegment.data.sid + " notifing... " +
 		// waveSegment.data.timestamp);
 		deviceEvent.notifyWaveSegmentArrived(waveSegment);
 		// System.out.println(System.currentTimeMillis()/1000 + " "
 		// + waveSegment.data.sid + " notified...");
 
 	}
 
 	/**
 	 * Apply pre-insert merge operation on wave segments. A collections of
 	 * continuous wave segments will be combined to persist in the repository.
 	 * 
 	 * @param waveSegment
 	 *            Wave segment of a sensor sent by a device
 	 */
 	public final void applyPreInsertMerge(final WaveSegmentFormat waveSegment) {
 
 		String hashKey = waveSegment.secretkey;
 		hashKey = hashKey.concat(waveSegment.data.dname)
 				.concat(waveSegment.data.sname)
 				.concat("" + waveSegment.data.sid);
 
 		ArrayList<WaveSegmentFormat> pendingWaveSegmentsList = hashmapWaveSegments
 				.get(hashKey);
 
 		if (null == pendingWaveSegmentsList) {
 			pendingWaveSegmentsList = new ArrayList<WaveSegmentFormat>();
 		}
 
 		// TODO: Handle missing data and timestamp based wave segment creation
 		if (pendingWaveSegmentsList.size() < WaveSegmentSize) {
 			pendingWaveSegmentsList.add(0, waveSegment);
 			hashmapWaveSegments.put(hashKey, pendingWaveSegmentsList);
 
 			log.info(Const.API_DATA_UPLOAD_WAVESEGMENT, " "
 					+ waveSegment.data.timestamp + " buffer "
 					+ hashmapWaveSegments.get(hashKey).size() + " " + hashKey);
 
 			// renderText(hashKey + " count : "
 			// + hashmapWaveSegments.get(hashKey).size());
 			return;
 		}
 
 		// TODO: Assumes that all WaveSegments are identical format
 		// Merge all pending WaveSegments with the current one
 		long oldestTimestamp = 0;
 		for (WaveSegmentFormat pendingPublishData : pendingWaveSegmentsList) {
 
 			oldestTimestamp = pendingPublishData.data.timestamp;
 			int channelIndex = 0;
 			for (WaveSegmentFormat.Channels channel : waveSegment.data.channels) {
 				channel.readings
 						.addAll(0, pendingPublishData.data.channels
 								.get(channelIndex).readings);
 				++channelIndex;
 			}
 		}
 
 		long currentTimestamp = waveSegment.data.timestamp;
 		waveSegment.data.timestamp = oldestTimestamp;
 
 		persistWaveSegment(waveSegment);
 
 		log.info(Const.API_DATA_UPLOAD_WAVESEGMENT, " " + currentTimestamp
 				+ " stored " + hashmapWaveSegments.get(hashKey).size() + " "
 				+ hashKey);
 
 		hashmapWaveSegments.remove(hashKey);
 
 		if (isSendResponseEnabled) {
 			response.SendSuccess(Const.API_DATA_UPLOAD_WAVESEGMENT,
 					Const.UPLOAD_WAVESEGMENT_SUCCESS);
 		} else {
 			response.sendEmpty(); // to terminate the process
 		}
 
 	}
 
 	/**
 	 * Services the upload/wavesegment API. Received sensor readings in wave
 	 * segment is persisted in the repository.
 	 * 
 	 * @param waveSegmentJson
 	 *            Wave segment of a sensor sent by a device in json string
 	 */
 	public final void doProcess(final String waveSegmentJson) {
 
 		try {
 			WaveSegmentFormat newWaveSegment = convertToRequestFormat(
 					waveSegmentJson, WaveSegmentFormat.class);
 			
 
 			long t1 = new java.util.Date().getTime();
 			
 			validateWaveSegment(newWaveSegment);
			verifyWaveSegment(newWaveSegment);
 			long t2 = new java.util.Date().getTime();
 			
 			
 			// userProfile.checkRegisteredSecretkey(newWaveSegment.secretkey,
 			// Const.API_UPLOAD_WAVESEGMENT);
 			persistWaveSegment(newWaveSegment);
 			
 			long t3 = new java.util.Date().getTime();
 			
 			System.out.println("waveseg t2-t1 : " + (t2-t1) + "  t3-t2 : " + (t3-t2));
 			
 			// applyPreInsertMerge(newWaveSegment);
 		} catch (InvalidJsonException e) {
 			sendError(ErrorType.INVALID_JSON, e.getMessage());
 		} catch (Exception e) {
 			e.printStackTrace();
 			sendError(ErrorType.SYSTEM_ERROR, e.getMessage());
 		}
 	}
 }
