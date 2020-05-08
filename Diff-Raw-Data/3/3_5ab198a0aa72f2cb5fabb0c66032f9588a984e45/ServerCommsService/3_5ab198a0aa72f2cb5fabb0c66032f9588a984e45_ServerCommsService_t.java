 /**
  * <p>
  * <u><b>Copyright Notice</b></u>
  * </p><p>
  * The copyright in this document is the property of 
  * Bath Institute of Medical Engineering.
  * </p><p>
  * Without the written consent of Bath Institute of Medical Engineering
  * given by Contract or otherwise the document must not be copied, reprinted or
  * reproduced in any material form, either wholly or in part, and the contents
  * of the document or any method or technique available there from, must not be
  * disclosed to any other person whomsoever.
  *  </p><p>
  *  <b><i>Copyright 2013-2014 Bath Institute of Medical Engineering.</i></b>
  * --------------------------------------------------------------------------
  * 
  */
 package com.projectnocturne.services;
 
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 
 import android.util.Log;
 
 import com.projectnocturne.NocturneApplication;
 import com.projectnocturne.datamodel.Alert;
 import com.projectnocturne.datamodel.Sensor;
 import com.projectnocturne.datamodel.User;
 import com.projectnocturne.server.HttpRequestTask;
 import com.projectnocturne.server.HttpRequestTask.RequestMethod;
 import com.projectnocturne.server.RestUriFactory;
 import com.projectnocturne.server.RestUriFactory.RestUriType;
 
 public final class ServerCommsService {
 	private static final String LOG_TAG = ServerCommsService.class.getSimpleName() + "::";
 
 	public void checkUserStatus(final User obj) {
 		Log.i(NocturneApplication.LOG_TAG, ServerCommsService.LOG_TAG + "checkUserStatus()");
 
 		final List<NameValuePair> uriData = RestUriFactory.getUri(RestUriType.CHECK_USER_STATUS, obj);
 
 		if (uriData.size() == 0) {
 			Log.e(NocturneApplication.LOG_TAG, ServerCommsService.LOG_TAG + "checkUserStatus() for " + obj.username);
 			return;
 		}
 
 		final HttpRequestTask restReq = new HttpRequestTask();
 
 		restReq.execute(RequestMethod.POST.toString(), "http://androidexample.com/check_user_status", uriData);
 	}
 
 	public void sendAlert(final Alert obj) {
 		Log.i(NocturneApplication.LOG_TAG, ServerCommsService.LOG_TAG + "sendAlert() " + obj.alert_name);
 
 		final List<NameValuePair> uriData = RestUriFactory.getUri(RestUriType.SEND_ALERT, obj);
 
 		if (uriData.size() == 0) {
 			Log.e(NocturneApplication.LOG_TAG, ServerCommsService.LOG_TAG + "sendAlert() for " + obj.alert_name);
 			return;
 		}
 
 		final HttpRequestTask restReq = new HttpRequestTask();
 
 		restReq.execute(RequestMethod.POST.toString(), "http://androidexample.com/alert_from_patient", uriData);
 	}
 
 	public void sendSensorReading(final Sensor obj) {
 		Log.i(NocturneApplication.LOG_TAG, ServerCommsService.LOG_TAG + "sendSensorReading() " + obj.sensor_name);
 
 		final List<NameValuePair> uriData = RestUriFactory.getUri(RestUriType.SEND_SENSOR_READING, obj);
 
 		if (uriData.size() == 0) {
 			Log.e(NocturneApplication.LOG_TAG, ServerCommsService.LOG_TAG + "sendSensorReading() for "
 					+ obj.sensor_name);
 			return;
 		}
 
 		final HttpRequestTask restReq = new HttpRequestTask();
 
 		restReq.execute(RequestMethod.POST.toString(), "http://192.168.1.163:9999/send_sendor_reading", uriData);
 	}
 
 	public void sendSubscriptionMessage(final User obj) {
 		Log.i(NocturneApplication.LOG_TAG, ServerCommsService.LOG_TAG + "sendSubscriptionMessage() for "
 				+ obj.name_first);
 
 		final List<NameValuePair> uriData = RestUriFactory.getUri(RestUriType.SUBSCRIBETO_SERVICE, obj);
 
 		if (uriData.size() == 0) {
 			Log.e(NocturneApplication.LOG_TAG, ServerCommsService.LOG_TAG + "sendSubscriptionMessage() for "
 					+ obj.name_first);
 			return;
 		}
 
 		final HttpRequestTask restReq = new HttpRequestTask();
 
		restReq.execute(RequestMethod.POST.toString(), "http://127.0.0.1:8888//users/register", uriData);
 	}
 
 }
