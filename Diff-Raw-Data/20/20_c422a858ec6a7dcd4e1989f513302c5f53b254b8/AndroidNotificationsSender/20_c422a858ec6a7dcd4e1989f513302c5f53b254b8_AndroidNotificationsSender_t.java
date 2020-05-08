 package com.idega.mobile.notifications.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import com.google.android.gcm.server.Constants;
 import com.google.android.gcm.server.Message;
 import com.google.android.gcm.server.Message.Builder;
 import com.google.android.gcm.server.MulticastResult;
 import com.google.android.gcm.server.Result;
 import com.google.android.gcm.server.Sender;
 import com.idega.idegaweb.IWMainApplicationSettings;
 import com.idega.mobile.MobileConstants;
 import com.idega.mobile.bean.Notification;
 import com.idega.mobile.data.NotificationSubscription;
 import com.idega.mobile.notifications.NotificationsSender;
 import com.idega.util.CoreUtil;
 import com.idega.util.ListUtil;
 import com.idega.util.StringUtil;
 import com.idega.util.datastructures.map.MapUtil;
 
 @Service
 @Scope(BeanDefinition.SCOPE_SINGLETON)
 public class AndroidNotificationsSender extends NotificationsSender {
 
 	@Override
 	public boolean doSendNotification(Notification notification, Map<Locale, String> messages,
 			Map<Locale, List<NotificationSubscription>> groupedSubscriptions) {
 
 		IWMainApplicationSettings settings = getApplication().getSettings();
 		String apiKey = settings.getProperty("google_api_key");
 		if (StringUtil.isEmpty(apiKey)) {
 			getLogger().warning("Google's API key is not provided");
 			return false;
 		}
 
		int retries = Integer.valueOf(settings.getProperty("mun.android_notif_retries", "5"));
 
 		Sender sender = new Sender(apiKey);
 		for (Locale locale: groupedSubscriptions.keySet()) {
 			List<NotificationSubscription> localizedSubscriptions = groupedSubscriptions.get(locale);
 			if (ListUtil.isEmpty(localizedSubscriptions))
 				continue;
 
 			String msg = messages.get(locale);
 			if (StringUtil.isEmpty(msg))
 				continue;
 
 			List<String> devices = new ArrayList<String>();
 			for (NotificationSubscription subscription: localizedSubscriptions)
 				devices.add(subscription.getToken());
 			if (ListUtil.isEmpty(devices))
 				continue;
 
 			Builder msgBuilder = new Message.Builder();
 			Map<String, String> data = notification.getDictionaries();
 			if (!MapUtil.isEmpty(data)) {
 				for (Map.Entry<String, String> dataEntry: data.entrySet()) {
 					msgBuilder.addData(dataEntry.getKey(), dataEntry.getValue());
 				}
 			}
 			msgBuilder.addData("alert", msg);
 			Message message = msgBuilder.build();
 
 			MulticastResult multicastResult = null;
 			try {
 				multicastResult = sender.send(message, devices, retries);
 
 				List<Result> results = multicastResult.getResults();
 				if (!ListUtil.isEmpty(results)) {
 					for (Result result: results) {
 						if (result.getMessageId() != null) {
 							String canonicalRegId = result.getCanonicalRegistrationId();
 							if (canonicalRegId != null) {
 								// same device has more than one registration ID: update database
 							}
 						} else {
 							String error = result.getErrorCodeName();
 							if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
 								// application has been removed from device - unregister database
 							}
 						}
 					}
 				}
 			} catch (Exception e) {
 				String error = "Error sending " + message + " to " + devices + " with " + retries + " retries. Result report: " + multicastResult;
 				getLogger().log(Level.WARNING, error, e);
 				CoreUtil.sendExceptionNotification(error, e);
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	public String getSupportedDeviceType() {
 		return MobileConstants.DEVICE_ANDROID;
 	}
 
 	@Override
 	public String getKeyStore() {
 		return getApplication().getSettings().getProperty("android_keystore");
 	}
 
 	@Override
 	public String getPassword() {
 		return getApplication().getSettings().getProperty("android_keystore_psw");
 	}
 
 }
