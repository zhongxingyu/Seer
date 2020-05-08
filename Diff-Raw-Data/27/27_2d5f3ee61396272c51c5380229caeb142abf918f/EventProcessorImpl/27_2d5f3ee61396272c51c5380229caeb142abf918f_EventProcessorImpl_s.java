 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.vas.communicator.domain;
 
 import it.sayservice.platform.client.DomainUpdateListener;
 import it.sayservice.platform.core.message.Core.DomainEvent;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.bson.types.ObjectId;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import eu.trentorise.smartcampus.communicator.model.Channel;
 import eu.trentorise.smartcampus.communicator.model.Notification;
 import eu.trentorise.smartcampus.communicator.model.Preference;
 import eu.trentorise.smartcampus.presentation.common.exception.DataException;
 import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
 import eu.trentorise.smartcampus.vas.communicator.filter.NotificationFilter;
 import eu.trentorise.smartcampus.vas.communicator.storage.CommunicatorStorage;
 import eu.trentorise.smartcampus.vas.communicator.util.MailNotificationSender;
 import eu.trentorise.smartcampus.vas.communicator.util.NotificationsUtil;
 
 public class EventProcessorImpl implements DomainUpdateListener {
 
 	private static Log logger = LogFactory.getLog(EventProcessorImpl.class);
 	private static final String TYPE_SOURCE = "eu.trentorise.smartcampus.domain.communicator.AbstractSource";
 	private static final String TYPE_FEED = "eu.trentorise.smartcampus.domain.communicator.AbstractFeed";
 	private static final String EVENT_UPDATE = "update";
 
 	
 	private int maxnum = 1000;
 	
 	private static ObjectMapper mapper = new ObjectMapper();
 	@Autowired
 	CommunicatorStorage storage;
 
 	@Autowired
 	MailNotificationSender sender;
 
 	public EventProcessorImpl(int maxnum)  {
 		super();
 		this.maxnum = maxnum;
 	}
 
 	@Override
 	public void onDomainEvents(String subscriptionId, List<DomainEvent> events) {
 		for (DomainEvent event : events) {
 			if (EVENT_UPDATE.equals(event.getEventSubtype()) &&
 				(event.getAllTypesList().contains(TYPE_SOURCE) || event.getAllTypesList().contains(TYPE_FEED))) {
 				try {
 					processEvent(event);
 				} catch (Exception e) {
 					logger.error("Error processing EventObject: " + e.getMessage());
 					e.printStackTrace();
 					continue;
 				}
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void processEvent(DomainEvent event) throws JsonParseException, JsonMappingException, IOException, DataException {
 		Map<String, Object> payload = mapper.readValue(event.getPayload(), Map.class);
 		String userId = (String) payload.get("userId");
		String sourceType = (String) payload.get("sourceType");
 		List<Map<String, Object>> notificationMaps = (List<Map<String, Object>>) payload.get("notifications");
 
 		if (notificationMaps == null || notificationMaps.isEmpty()) {
 			return;
 		}
 		List<Channel> userChannels = null;
 		boolean isFeed = false;
 
 		// broadcast events
 		if (event.getAllTypesList().contains(TYPE_FEED)) {
 			userChannels = findUserFeedChannels(sourceType);
 			isFeed = true;
 		} 
 		// user events
 		else if (event.getAllTypesList().contains(TYPE_SOURCE)) {
 			userChannels = findUserSourceChannels(userId, sourceType);
 		} else {
 			return;
 		}
 		
 		// notifications mapped to mails to users
 		Map<String, Map<String,List<Notification>>> toSend = new TreeMap<String, Map<String,List<Notification>>>();
 		
 		// counters of new messages
 		Map<String, Integer> counters = new HashMap<String, Integer>();
 		
 		for (Map<String, Object> map : notificationMaps) {
 			// original message
 			Notification originalNotification = mapper.convertValue(map, Notification.class);
 			originalNotification.setId(new ObjectId().toString());
 			originalNotification.setType(sourceType);
 			originalNotification.setTimestamp(System.currentTimeMillis());
 
 			// working instance
 			Notification notification = originalNotification;
 			// notification mapped to users
 			Map<String,Notification> userNotifications = new HashMap<String, Notification>();
 			boolean found = false;
 			if (userChannels != null) {
 				// set of mails to which the notification should be delivered
 				Set<String> mailSet = new HashSet<String>();
 				for (Channel channel : userChannels) {
 					if (channel.applies(notification)) {
 						found = true;
 						if (isFeed) {
 							// check if this message is already being delivered to the user
 							notification = userNotifications.get(channel.getUser()); 
 							if (notification == null) {
 								// create a copy for this user
 								notification = originalNotification.copy(channel.getUser());
 								userNotifications.put(channel.getUser(), notification);
 							}
 						} else {
 							userNotifications.put(userId, notification);
 						}
 						// check if actions apply and the notification should not be deleted for this user
 						if (!NotificationsUtil.applyChannelActions(channel, notification, mailSet)) {
 							notification.markAsDeleted();
 						}
 						notification.setUser(channel.getUser());
 						notification.addChannelId(channel.getId());
 						notification.setLabelIds(channel.getLabelIds());
 					}
 				}
 				for (String uId : userNotifications.keySet()) {
 					Notification n = userNotifications.get(uId);
 					if (!n.markedDeleted()) {
						storage.storeObject(notification);
 						incrementUserCounter(uId, counters);
 						// check if has to send mails 
 						if (!mailSet.isEmpty()) {
 							Map<String,List<Notification>> userMailMap = toSend.get(uId);
 							if (userMailMap == null) {
 								userMailMap = new TreeMap<String, List<Notification>>();
 								toSend.put(uId, userMailMap);
 							}
 							for (String mail : mailSet) {
 								List<Notification> list = userMailMap.get(mail);
 								if (list == null) {
 									list = new ArrayList<Notification>();
 									userMailMap.put(mail, list);
 								}
 								list.add(n);
 							}
 						}
 					}
 				}
 			}
 			if (isFeed) {
 				storage.storeObject(originalNotification);
 			} else if (!found) {
 				notification.setUser(userId);
 				storage.storeObject(notification);
 				incrementUserCounter(userId, counters);
 			}
 		}	
 		
 		sendEmail(toSend);
 		cleanUpOldMessages(counters);
 	}
 
 	private void incrementUserCounter(String uId, Map<String, Integer> counters) {
 		if (counters.containsKey(uId))counters.put(uId, counters.get(uId)+1);
 		else counters.put(uId, 1);
 	}
 
 	private void cleanUpOldMessages(Map<String, Integer> counters) throws DataException {
 		if (counters == null || counters.isEmpty()) return;
 		for (String user : counters.keySet()) {
 			int newMsgs = counters.get(user);
 			int max = maxnum;
 			Preference prefs = null;
 			try {
 				prefs = storage.getObjectById(user, Preference.class);
 			} catch (NotFoundException e) {
 				// do nothing
 			}
 			if (prefs != null && prefs.getMaxMessageNumber() != null && prefs.getMaxMessageNumber() > 0) {
 				max = Math.min(maxnum, prefs.getMaxMessageNumber());
 			}
 			NotificationFilter filter = new NotificationFilter();
 			filter.setStarred(false);
 			List<Notification> old = storage.searchNotifications(user, null, null, null, filter);
 
 			if (old.size()+newMsgs > max) {
 				Collections.sort(old, new Comparator<Notification>() {
 					@Override
 					public int compare(Notification o1, Notification o2) {
 						return Long.valueOf(o1.getTimestamp()).compareTo(Long.valueOf(o2.getTimestamp()));
 					}
 				});
 				for (int i = 0; i < Math.min(newMsgs+old.size()-max,old.size()); i++) {
 					storage.deleteObjectPermanently(old.get(i));
 				}
 			}
 		}
 	}
 
 	private List<Channel> findUserFeedChannels(String sourceType) throws DataException {
 		Map<String,Object> criteriaMap = new HashMap<String, Object>();
 		return storage.searchObjects(Channel.class, criteriaMap);
 	}
 
 	private List<Channel> findUserSourceChannels(String userId, String sourceType) throws DataException {
 		Map<String,Object> criteriaMap = new HashMap<String, Object>();
 		return storage.searchObjects(Channel.class, criteriaMap, userId);
 	}
 	private void sendEmail(Map<String, Map<String, List<Notification>>> toSend) throws DataException {
 		for (String user : toSend.keySet()) {
 			Map<String, List<Notification>> map = toSend.get(user);
 			NotificationsUtil.sendEmail(map, sender);
 		}
 	}
 
 	public MailNotificationSender getSender() {
 		return sender;
 	}
 
 	public void setSender(MailNotificationSender sender) {
 		this.sender = sender;
 	}
 }
