 /*
  * Name: DataQuery.java
  * Project: SensorAct, MUC@IIIT-Delhi 
  * Version: 1.0
  * Date: 2012-04-14
  * Author: Pandarasamy Arjunan
  */
 package edu.iiitd.muc.sensoract.api;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import edu.iiitd.muc.sensoract.api.request.DataQueryFormat;
 import edu.iiitd.muc.sensoract.constants.Const;
 import edu.iiitd.muc.sensoract.enums.ErrorType;
 import edu.iiitd.muc.sensoract.exceptions.InvalidJsonException;
 import edu.iiitd.muc.sensoract.model.data.WaveSegmentModel;
 import edu.iiitd.muc.sensoract.profile.UserProfile;
 
 /**
  * data/query API: Retrieves wavesegmetns from the repository based upong the given query.
  * 
  * @author Pandarasamy Arjunan
  * @version 1.0
  */
 public class DataQuery extends SensorActAPI {
 
 	/**
 	 * Converts the data request query in Json string to object.
 	 * 
 	 * @param queryJson
 	 *            Query in Json string
 	 * @return Converted query format object
 	 * @throws InvalidJsonException
 	 *             If the Json string is not valid or not in the required
 	 *             request format
 	 * @see DataQueryFormat
 	 */
 	private DataQueryFormat convertToQueryFormat(final String queryJson)
 			throws InvalidJsonException {
 
 		DataQueryFormat queryFormat = null;
 		try {
 			queryFormat = gson.fromJson(queryJson, DataQueryFormat.class);
 		} catch (Exception e) {
 			throw new InvalidJsonException(e.getMessage());
 		}
 
 		if (null == queryFormat) {
 			throw new InvalidJsonException(Const.EMPTY_JSON);
 		}
 		return queryFormat;
 	}
 
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
		String secretkey = UserProfile.getSecretkey(queryObj.username, null);
 		if (null == secretkey) {
 			response.sendFailure(Const.API_DATA_QUERY,
 					ErrorType.UNREGISTERED_USERNAME, "");
 		}
 
 		log.info("QueryDAta : \n" + gson.toJson(queryObj));
 
 		List<WaveSegmentModel> allWaveSegments = WaveSegmentModel
 				.q()
 				.filter("secretkey", secretkey)
 				.filter("data.dname", queryObj.devicename)
 				.filter("data.sname", queryObj.sensorname)
 				// .filter("data.sid", queryObj.sensorid)
 				.filter("data.timestamp >=", queryObj.conditions.fromtime)
 				.filter("data.timestamp <=", queryObj.conditions.totime)
 				.fetchAll();
 
 		Iterator<WaveSegmentModel> iteratorData = allWaveSegments.iterator();
 		ArrayList<String> outList = new ArrayList<String>();
 
 		while (iteratorData.hasNext()) {
 
 			WaveSegmentModel ww = iteratorData.next();
 			ww.data.timestamp = ww.data.timestamp * 1000; // for plot
 
 			// ww.data.channels.removeAll(Collections.singleton(null));;
 			// ww.data.channels.removeAll(Arrays.asList(new Object[]{null}));
 			String data = gson.toJson(ww);
 			outList.add(data);
 		}
 
 		// response.SendJSON(of);
 		System.out.println(outList.toString());
 		renderText("{\"wavesegmentArray\":" + outList.toString() + "}");
 		// response.SendJSON(outList.toString());
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
 			DataQueryFormat query = convertToQueryFormat(queryJson);
 			validateQueryDataFormat(query);
 			executeQuery(query);
 		} catch (InvalidJsonException e) {
 			response.sendFailure(Const.API_DATA_QUERY, ErrorType.INVALID_JSON,
 					e.getMessage());
 		} catch (Exception e) {
 			response.sendFailure(Const.API_DATA_QUERY, ErrorType.SYSTEM_ERROR,
 					e.getMessage());
 		}
 	}
 
 }
