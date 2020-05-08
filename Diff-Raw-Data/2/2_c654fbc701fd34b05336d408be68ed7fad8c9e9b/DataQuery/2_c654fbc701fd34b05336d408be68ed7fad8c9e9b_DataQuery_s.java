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
  * Name: DataQuery.java
  * Project: SensorAct-VPDS 
  * Version: 1.0
  * Date: 2012-04-14
  * Author: Pandarasamy Arjunan
  */
 package edu.pc3.sensoract.vpds.api;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.persistence.TypedQuery;
 
 import controllers.Bootstrap;
 
 import play.Play;
 import play.db.jpa.JPA;
 import edu.pc3.sensoract.vpds.api.request.DataQueryFormat;
 import edu.pc3.sensoract.vpds.api.response.WaveSegmentRFormat;
 import edu.pc3.sensoract.vpds.constants.Const;
 import edu.pc3.sensoract.vpds.enums.ErrorType;
 import edu.pc3.sensoract.vpds.exceptions.InvalidJsonException;
 import edu.pc3.sensoract.vpds.guardrule.GuardRuleManager;
 import edu.pc3.sensoract.vpds.guardrule.RequestingUser;
 import edu.pc3.sensoract.vpds.model.WaveSegmentModel;
 import edu.pc3.sensoract.vpds.model.rdbms.WaveSegmentChannelRModel;
 
 /**
  * data/query API: Retrieves wavesegmetns from the repository based upong the
  * given query.
  * 
  * @author Pandarasamy Arjunan
  * @version 1.0
  */
 public class DataQuery extends SensorActAPI {
 
 	/**
 	 * Validates the query attributes. If validation fails, sends corresponding
 	 * failure message to the caller.
 	 * 
 	 * @param queryObj
 	 *            Query in object format
 	 */
 	private void validateQueryDataFormat(final DataQueryFormat queryObj) {
 
 		// TODO: Add validation for other attributes as well.
 		validator.validateUserName(queryObj.username);
 		validator.validateDeviceName(queryObj.devicename);
 
 		if (validator.hasErrors()) {
 			response.sendFailure(Const.API_DATA_QUERY,
 					ErrorType.VALIDATION_FAILED, validator.getErrorMessages());
 		}
 	}
 
 	/**
 	 * Retrieves data from the repository as per the request query and sends
 	 * back to the caller.
 	 * 
 	 * @param query
 	 *            Query in object format
 	 */
 	private void executeQuery(final DataQueryFormat queryObj) {
 
 		// TODO: add extensive query processing options
 		if (false == userProfile.isRegisteredSecretkey(queryObj.secretkey)
 				&& !shareProfile.isAccessKeyExists(queryObj.secretkey)) {
 			response.sendFailure(Const.API_DATA_QUERY,
 					ErrorType.UNREGISTERED_SECRETKEY, queryObj.secretkey);
 		}
 
 		String secretkey = Play.configuration
 				.getProperty(Const.OWNER_UPLOADKEY);
 
 		/*
 		 * String secretkey = userProfile.getSecretkey(queryObj.username); if
 		 * (null == secretkey) { response.sendFailure(Const.API_DATA_QUERY,
 		 * ErrorType.UNREGISTERED_USERNAME, ""); }
 		 */
 
 		log.info("QueryData : \n" + json.toJson(queryObj));
 
 		List<WaveSegmentModel> allWaveSegments = WaveSegmentModel
 				.q()
 				.filter("secretkey", secretkey)
 				.filter("data.dname", queryObj.devicename)
 				.filter("data.sname", queryObj.sensorname)
 				// .filter("data.sid", queryObj.sensorid)
 				.filter("data.timestamp >=", queryObj.conditions.fromtime)
 				.filter("data.timestamp <=", queryObj.conditions.totime)
 				.order("data.timestamp").fetchAll();
 		// .fetchAll();
 
 		Iterator<WaveSegmentModel> iteratorData = allWaveSegments.iterator();
 		ArrayList<String> outList = new ArrayList<String>();
 
 		while (iteratorData.hasNext()) {
 
 			WaveSegmentModel ww = iteratorData.next();
 			ww.data.timestamp = ww.data.timestamp * 1000; // for plot
 
 			// ww.data.channels.removeAll(Collections.singleton(null));;
 			// ww.data.channels.removeAll(Arrays.asList(new Object[]{null}));
 			String data = json.toJson(ww);
 			outList.add(data);
 		}
 
 		// response.SendJSON(of);
 		// System.out.println(outList.toString());
 		renderText("{\"wavesegmentArray\":" + outList.toString() + "}");
 		// response.SendJSON(outList.toString());
 	}
 
 	@SuppressWarnings("unused")
 	private void executeRQuery(final DataQueryFormat queryObj) {
 
 		// { username : "samysamy", devicename : "device1", sensorname :
 		// "sensor", secretkey : "cf7908f7b8694975aec68e0475e7cb6c", data : {
 		// dname : "device1", sname : "sensor", sid : "1", sinterval : "1",
 		// timestamp : 1234567890, channels : [ {cname: "channel1", unit : "C",
 		// readings: [1,2,3,4,5,6,7,8,9,10]}, {cname: "channel2", unit : "T",
 		// readings: [10,20,30,40,50,60,70,80,90,100]} ]} }
 
 		// TODO: add extensive query processing options
 		String secretkey = userProfile.getSecretkey(queryObj.username);
 		if (null == secretkey) {
 			response.sendFailure(Const.API_DATA_QUERY,
 					ErrorType.UNREGISTERED_USERNAME, "");
 		}
 
 		log.info("QueryDAta : \n" + json.toJson(queryObj));
 
 		// List<WaveSegmentRModel> allWaveSegments = WaveSegmentRModel.find(
 		// "bySecretkeyAndDnameAndSname", secretkey, queryObj.devicename,
 		// queryObj.sensorname).fetch();
 
 		// List<WaveSegmentRModel> allWaveSegments = WaveSegmentRModel.find(
 		// "secretkey = ? and dname = ? and sname = ? and sid = ? and " +
 		// "timestamp >= ? and timestamp <= ?",
 		// secretkey, queryObj.devicename, queryObj.sensorname,
 		// queryObj.sensorid,
 		// queryObj.conditions.fromtime,queryObj.conditions.totime)
 		// .fetch();
 
 		// for (WaveSegmentRModel ws : allWaveSegments) {
 		// wsf.add(new WaveSegmentRFormat(ws));
 		// }
 
 		// List<WaveSegmentRModel> allWaveSegments = WaveSegmentRModel
 		// .find("channels.cname = channel1")
 		// .fetch();
 
 		// List<WaveSegmentChannelRModel> allWaveSegments = WaveSegmentRModel
 		// .em()
 		// .createQuery(
 		// "select ch from wschannels ch join ch.wavesegment ws "
 		// + "where ch.cname = 'channel1' and ws.dname = 'device1'",
 		// WaveSegmentChannelRModel.class).getResultList();
 
 		String queryStr = "SELECT channel FROM wschannels channel "
 				+ "JOIN channel.wavesegment wavesegment "
 				+ "WHERE channel.cname = :cname "
 				+ "AND wavesegment.secretkey = :secretkey "
 				+ "AND wavesegment.device = :device "
 				+ "AND wavesegment.sensor = :sensor "
 				+ "AND wavesegment.sensorid = :sensorid "
 				+ "AND wavesegment.timestamp >= :fromtime "
 				+ "AND wavesegment.timestamp <= :totime ";
 
 		TypedQuery<WaveSegmentChannelRModel> query = JPA.em().createQuery(
 				queryStr, WaveSegmentChannelRModel.class);
 
 		query.setParameter("secretkey", secretkey);
 		query.setParameter("device", queryObj.devicename);
 		query.setParameter("sensor", queryObj.sensorname);
 		query.setParameter("sensorid", queryObj.sensorid);
 		query.setParameter("cname", queryObj.channelname);
 		query.setParameter("fromtime", queryObj.conditions.fromtime);
 		query.setParameter("totime", queryObj.conditions.totime);
 
 		List<WaveSegmentChannelRModel> allWaveSegments = query.getResultList();
 
 		System.out.println("ch # " + allWaveSegments.size());
 
 		List<WaveSegmentRFormat> wsf = new ArrayList<WaveSegmentRFormat>();
 		for (WaveSegmentChannelRModel ws : allWaveSegments) {
 			wsf.add(new WaveSegmentRFormat(ws));
 		}
 
 		response.sendJSON(wsf);
 	}
 
 	// modified data/query which pass through guard rule engine
 	private void readData(final DataQueryFormat query) {
 
 		String username = null;
 		String ownername = null;
 		String email = null;
 
 		username = shareProfile.getUsername(query.secretkey);
 		if (null == username) {
 			ownername = userProfile.getUsername(query.secretkey);
 			if (null == ownername) {
 				response.sendFailure(Const.API_DATA_QUERY,
 						ErrorType.UNREGISTERED_SECRETKEY, query.secretkey);
 			}
 		}
 
 		// fetch the email address of the
 		if (username != null) {
 			email = shareProfile.getEmail(username);
 			// update the ownername to fetch data
			ownername = userProfile.getUsername(query.secretkey);
 		} else { // owner
 			email = userProfile.getEmail(ownername);
 		}
 
 		RequestingUser requestingUser = new RequestingUser(email);
 
 		List<WaveSegmentModel> wsList = GuardRuleManager.read(ownername,
 				requestingUser, query.devicename, query.sensorname,
 				query.sensorid, query.conditions.fromtime,
 				query.conditions.totime);
 
 		
 		// TODO: what the hell is happening here ?? Need to change the output format
 		Iterator<WaveSegmentModel> iteratorData = wsList.iterator();
 		ArrayList<String> outList = new ArrayList<String>();
 
 		while (iteratorData.hasNext()) {
 
 			WaveSegmentModel ww = iteratorData.next();
 			ww.data.timestamp = ww.data.timestamp * 1000; // for plot
 
 			// ww.data.channels.removeAll(Collections.singleton(null));;
 			// ww.data.channels.removeAll(Arrays.asList(new Object[]{null}));
 			String data = json.toJson(ww);
 			outList.add(data);
 		}
 
 		// response.SendJSON(of);
 		// System.out.println(outList.toString());
 		renderText("{\"wavesegmentArray\":" + outList.toString() + "}");
 
 	}
 
 	// private void sendData(List<WaveSegmentModel> allWaveSegments) {
 	// }
 
 	/**
 	 * Services the querydata API. Retrieves data from the repository as per the
 	 * request query and sends back to the caller.
 	 * 
 	 * @param queryJson
 	 *            Request query in Json string
 	 */
 	public void doProcess(final String queryJson) {
 
 		try {
 			DataQueryFormat query = convertToRequestFormat(queryJson,
 					DataQueryFormat.class);
 			validateQueryDataFormat(query);
 			// executeQuery(query);
 			readData(query);
 		} catch (InvalidJsonException e) {
 			response.sendFailure(Const.API_DATA_QUERY, ErrorType.INVALID_JSON,
 					e.getMessage());
 		} catch (Exception e) {
 			response.sendFailure(Const.API_DATA_QUERY, ErrorType.SYSTEM_ERROR,
 					e.getMessage());
 		}
 	}
 
 }
