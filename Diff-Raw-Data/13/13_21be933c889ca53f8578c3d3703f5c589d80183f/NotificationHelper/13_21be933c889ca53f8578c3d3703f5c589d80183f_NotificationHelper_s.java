 package com.gallatinsystems.notification.helper;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.gallatinsystems.notification.NotificationRequest;
 import com.gallatinsystems.notification.dao.NotificationSubscriptionDao;
 import com.gallatinsystems.notification.domain.NotificationSubscription;
 import com.google.appengine.api.taskqueue.Queue;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.TaskOptions;
 
 /**
  * Finds current notification subscriptions from the data store and spawns async
  * jobs to generate data and send the notification.
  * 
  * To use this class you must define a queue called notification
  * 
  * @author Christopher Fagiani
  */
 public class NotificationHelper {
 
 	private static final String UNSPECIFIED = "unspecified";
 	private static final String QUEUE_NAME = "notification";
 	private static final String PROCESSOR_URL = "/notificationprocessor";
 	private String notificationType;
 	private Long subscriptionEntityId;
 	private Long notificationEntityId;
 	private NotificationSubscriptionDao notificationDao;
 
 	public NotificationHelper(String type, Long subEntityId) {
 		this(type, subEntityId, subEntityId);
 	}
 
 	public NotificationHelper(String type, Long subEntityId, Long notifEntityId) {
 		notificationDao = new NotificationSubscriptionDao();
 		notificationType = type;
 		this.subscriptionEntityId = subEntityId;
 		notificationEntityId = subEntityId;
 		if (notificationEntityId == null) {
 			notificationEntityId = subscriptionEntityId;
 		}
 	}
 
 	public void execute() {
 		// find all notifications that have not yet expired
 		List<NotificationSubscription> subs = notificationDao
 				.listSubscriptions(subscriptionEntityId, notificationType, true);
 
 		// group the subs by entity id and type
 		Map<String, Map<Long, List<NotificationSubscription>>> subMap = collateSubscriptions(subs);
 		if (subMap != null) {
 			// now spawn a notification job for each notificationType/entity
 			// combo
 			Queue queue = QueueFactory.getQueue(QUEUE_NAME);
 			for (Entry<String, Map<Long, List<NotificationSubscription>>> entry : subMap
 					.entrySet()) {
 				for (Entry<Long, List<NotificationSubscription>> notifEntry : entry
 						.getValue().entrySet()) {
 					StringBuilder builder = new StringBuilder();
 					StringBuilder optBuilder = new StringBuilder();
 					if (notifEntry.getValue() != null
 							&& notifEntry.getValue().size() > 0) {
 						for (int i = 0; i < notifEntry.getValue().size(); i++) {
 							if (i > 0) {
 								builder.append(NotificationRequest.DELIMITER);
 								optBuilder
 										.append(NotificationRequest.DELIMITER);
 							}
 							builder.append(notifEntry.getValue().get(i)
 									.getNotificationDestination());
 							String opt = notifEntry.getValue().get(i)
 									.getNotificationOption();
 							optBuilder.append(opt != null ? opt : UNSPECIFIED);
 						}
 						// now dump the item on the queue
						queue.add(TaskOptions.Builder
 								.withUrl(PROCESSOR_URL)
 								.param(NotificationRequest.DEST_PARAM,
 										builder.toString())
 								.param(NotificationRequest.DEST_OPT_PARAM,
 										optBuilder.toString())
 								.param(NotificationRequest.SUB_ENTITY_PARAM,
 										notifEntry.getKey().toString())
 								.param(NotificationRequest.TYPE_PARAM,
										entry.getKey())
								.param(NotificationRequest.NOTIF_ENTITY_PARAM,
										notificationEntityId.toString()));

 					}
 				}
 			}
 		}
 	}
 
 	private Map<String, Map<Long, List<NotificationSubscription>>> collateSubscriptions(
 			List<NotificationSubscription> subs) {
 		Map<String, Map<Long, List<NotificationSubscription>>> subMap = new HashMap<String, Map<Long, List<NotificationSubscription>>>();
 		if (subs != null) {
 			for (NotificationSubscription sub : subs) {
 				Map<Long, List<NotificationSubscription>> tempMap = subMap
 						.get(sub.getNotificationType());
 				if (tempMap == null) {
 					tempMap = new HashMap<Long, List<NotificationSubscription>>();
 					subMap.put(sub.getNotificationType(), tempMap);
 				}
 				List<NotificationSubscription> tempList = tempMap.get(sub
 						.getEntityId());
 				if (tempList == null) {
 					tempList = new ArrayList<NotificationSubscription>();
 					tempMap.put(sub.getEntityId(), tempList);
 				}
 				tempList.add(sub);
 			}
 		}
 		return subMap;
 	}
 }
