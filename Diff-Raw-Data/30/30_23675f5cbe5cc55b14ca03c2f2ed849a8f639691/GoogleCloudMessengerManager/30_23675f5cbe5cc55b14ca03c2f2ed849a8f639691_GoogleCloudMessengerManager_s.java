 package eu.trentorise.smartcampus.communicatorservice.manager.pushservice.impl;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 import com.google.android.gcm.server.Constants;
 import com.google.android.gcm.server.Message;
 import com.google.android.gcm.server.Result;
 import com.google.android.gcm.server.Sender;
 
 import eu.trentorise.smartcampus.communicator.model.AppAccount;
 import eu.trentorise.smartcampus.communicator.model.CloudToPushType;
 import eu.trentorise.smartcampus.communicator.model.Configuration;
 import eu.trentorise.smartcampus.communicator.model.Notification;
 import eu.trentorise.smartcampus.communicator.model.UserAccount;
 import eu.trentorise.smartcampus.communicatorservice.exceptions.NoUserAccountGCM;
 import eu.trentorise.smartcampus.communicatorservice.manager.AppAccountManager;
 import eu.trentorise.smartcampus.communicatorservice.manager.UserAccountManager;
 import eu.trentorise.smartcampus.communicatorservice.manager.pushservice.PushServiceCloud;
 import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
 
 @Component
 public class GoogleCloudMessengerManager implements PushServiceCloud {
 
 	private static final Logger logger = Logger
 			.getLogger(GoogleCloudMessengerManager.class);
 	@Autowired
 	AppAccountManager appAccountManager;
 
 	@Autowired
 	UserAccountManager userAccountManager;
 
 	@Autowired
 	@Value("${gcm.sender.key}")
 	private String gcm_sender_key;
 
 	@Autowired
 	@Value("${gcm.sender.id}")
 	private String gcm_sender_id;
 
 	@Autowired
 	@Value("${gcm.registration.id.default.key}")
 	private String gcm_registration_id_default_key;
 
 	@Autowired
 	@Value("${gcm.registration.id.default.value}")
 	private String gcm_registration_id_default_value;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.trentorise.smartcampus.vas.communicator.manager.pushservice.
 	 * PushServiceCloud
 	 * #sendToCloud(eu.trentorise.smartcampus.communicator.model.Notification)
 	 */
 	@Override
 	public boolean sendToCloud(Notification notification)
 			throws NotFoundException, NoUserAccountGCM {
 
 		// in default case is the system messenger that send
 		String senderId = null;
 		Sender sender = null;
 		String senderAppName = notification.getAuthor().getAppId();
 
 		AppAccount appAccount;
 		List<AppAccount> listApp = appAccountManager
 				.getAppAccounts(senderAppName);
 
 		appAccount = listApp.get(0);
 
 		List<Configuration> listConfApp = appAccount.getConfigurations();
 		Iterator<Configuration> indexConf = listConfApp.iterator();
 
 		while (indexConf.hasNext() && senderId == null) {
 			Configuration conf = indexConf.next();
			if (CloudToPushType.GOOGLE.compareTo(conf.getKey()) == 0) {
 				senderId = conf.get(gcm_sender_id) ;
 			} else {
 				throw new NotFoundException();
 			}
 		}
 
 		sender = new Sender(senderId);
 
 		String devices = "";
 
 		List<UserAccount> listUserAccount = userAccountManager
 				.findByUserIdAndAppName(notification.getUser(), senderAppName);
 
 		if (!listUserAccount.isEmpty()) {
 
 			UserAccount userAccountSelected = listUserAccount.get(0);
 			Configuration configurationSelected = new Configuration();
 
 			List<Configuration> listConfUser = userAccountSelected
 					.getConfigurations();
 			if(listConfUser!=null && !listConfUser.isEmpty()){
 				for (Configuration index : listConfUser) {
 					if (CloudToPushType.GOOGLE.compareTo(index.getKey()) == 0) {
 						devices = index.get(
 								gcm_registration_id_default_key);
 						configurationSelected = index;
 					}
 				}
 				Message message = new Message.Builder()
 				.collapseKey("1")
 				.timeToLive(3)
 				.delayWhileIdle(true)
 				.addData(notification.getTitle(),
 						notification.getDescription()).build();
 		Result result;
 		try {
 
 			result = sender.send(message, devices, 5);
 
 			System.out.println(result.toString());
 
 			if (result.getMessageId() != null) {
 				String canonicalRegId = result.getCanonicalRegistrationId();
 				if (canonicalRegId != null) {
 					// update new registrationid in my database
 					configurationSelected.remove(
 							gcm_registration_id_default_key);
 					configurationSelected
 							.putPrivate(gcm_registration_id_default_key,
 									canonicalRegId);
 					userAccountManager.update(userAccountSelected);
 					return true;
 				} else {
 					return true;
 				}
 			} else {
 				String error = result.getErrorCodeName();
 				if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
 					// remove appconfigutaion on this user account
 					userAccountSelected.getConfigurations().remove(
 							configurationSelected);
 					return false;
 				}
 			}
 
 		} catch (Exception e) {
 			logger.error(e.getMessage() + senderId);
 			e.printStackTrace();
 			return false;
 		}
 			}
 
 			
 		}else{
 			throw new NoUserAccountGCM("The user is not register for receive push notification");
 		}
 		return false;
 	}
 }
