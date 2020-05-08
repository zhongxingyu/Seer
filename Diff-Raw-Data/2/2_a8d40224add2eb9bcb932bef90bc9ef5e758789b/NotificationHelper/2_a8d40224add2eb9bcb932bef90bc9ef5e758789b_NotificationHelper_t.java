 package com.financial.tools.recorderserver.util;
 
 import java.io.IOException;
 
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.android.gcm.server.Message;
 import com.google.android.gcm.server.Message.Builder;
 import com.google.android.gcm.server.Result;
 import com.google.android.gcm.server.Sender;
 
 /**
  * Helper class used to push notification to device via GCM(Google Cloud
  * Message) service. Only available for Android.
  * 
  * @author eortwyz
  * 
  */
 public class NotificationHelper {
 
 	private static final String ERROR_CODE_NOT_REGISTERED = "NotRegistered";
 
 	private static final String GCM_MESSAGE_TITLE_KEY = "title";
 
 	private static final String GCM_MESSAGE_BODY_KEY = "message";
 
 	private static final String GCM_KEY = "AIzaSyDm5fOxjtm38z6oSxdzlwttQo0clbrKFC4";
 
 	private static final int RETRY_TIMES = 3;
 
 	private static Logger logger = LoggerFactory.getLogger(NotificationHelper.class);
 
 	public static boolean sendNotification(String deviceRegId, NotificationType notificationType,
 			String notificationMessage, int timeToLive) {
 		if (StringUtils.isEmpty(deviceRegId)) {
 			logger.warn("deviceRegId:{} doesn't exist.", deviceRegId);
 			return false;
 		}
 		logger.debug("send notification message: {} to deviceRegId: {}.", notificationMessage, deviceRegId);
 		Builder messageBuilder = new Message.Builder();
 		if (timeToLive > 0) {
 			messageBuilder.timeToLive(timeToLive);
 		}
		Message message = messageBuilder.delayWhileIdle(false)
 				.addData(GCM_MESSAGE_TITLE_KEY, notificationType.getTitle())
 				.addData(GCM_MESSAGE_BODY_KEY, notificationMessage).build();
 		Sender sender = new Sender(GCM_KEY);
 		try {
 			Result result = sender.send(message, deviceRegId, RETRY_TIMES);
 			logger.debug("notification send to deviceRegId: {}, result: {}.", deviceRegId, result);
 
 			if (result.getErrorCodeName() != null && result.getErrorCodeName().equals(ERROR_CODE_NOT_REGISTERED)) {
 				return false;
 			}
 		} catch (IOException e) {
 			logger.error("Can't send notification to deviceRegId: {}", deviceRegId);
 			return false;
 		}
 
 		return true;
 	}
 }
