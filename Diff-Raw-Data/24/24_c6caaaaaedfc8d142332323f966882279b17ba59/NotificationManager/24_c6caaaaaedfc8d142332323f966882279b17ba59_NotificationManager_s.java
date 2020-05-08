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
 package eu.trentorise.smartcampus.communicatorservice.manager;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import eu.trentorise.smartcampus.communicator.model.AppAccount;
 import eu.trentorise.smartcampus.communicator.model.CloudToPushType;
 import eu.trentorise.smartcampus.communicator.model.Notification;
 import eu.trentorise.smartcampus.communicatorservice.exceptions.NoUserAccountGCM;
 import eu.trentorise.smartcampus.communicatorservice.filter.NotificationFilter;
 import eu.trentorise.smartcampus.communicatorservice.manager.pushservice.PushServiceCloud;
 import eu.trentorise.smartcampus.communicatorservice.manager.pushservice.impl.ApplePushNotificationServiceManager;
 import eu.trentorise.smartcampus.communicatorservice.manager.pushservice.impl.GoogleCloudMessengerManager;
 import eu.trentorise.smartcampus.communicatorservice.storage.CommunicatorStorage;
 import eu.trentorise.smartcampus.presentation.common.exception.DataException;
 import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
 
 @Component
 public class NotificationManager {
 
 	@Autowired
 	CommunicatorStorage storage;
 
 	@Autowired
 	AppAccountManager appAccountManager;
 
 	@Autowired
 	GoogleCloudMessengerManager googleManager;
 
 	@Autowired
 	ApplePushNotificationServiceManager appleManager;
 
	public void create(Notification notification, Boolean isPush)
 			throws NotFoundException, DataException {
 		if (notification.getAuthor() == null)
 			throw new DataException("No Author in this notification");
 
 		storage.storeObject(notification);
		if (isPush) {
 
 			List<AppAccount> listApp = appAccountManager
					.getAppAccounts(notification.getType());
 			// if app registered before it can send,otherwise doesn't send in
 			// push
 			if (!listApp.isEmpty()) {
 				// check the first appAccount
 				AppAccount appAccount = listApp.get(0);
 				// get the account could type
 				List<CloudToPushType> listType = appAccount
 						.getCloudToPushTypeConfigured();
 
 				// send on all the cloud if a single app has more account
 				for (CloudToPushType cpt : listType) {
 					PushServiceCloud servicePush = null;
 
 					switch (cpt) {
 					case GOOGLE: {
 						servicePush = googleManager;
 						break;
 					}
 					case APPLE: {
 						servicePush = appleManager;
 						break;
 					}
 					case WINDOWSPHONE: {
 						break;
 					}
 
 					}
 
 					try {
 						servicePush.sendToCloud(notification);
 					} catch (NotFoundException e) {
 						throw new NotFoundException(e);
 					} catch (NoUserAccountGCM e) {
 						e.printStackTrace();
 					}
 				}
 			}
		}
 	}
 
 	public boolean deleteByApp(String id, String capp)
 			throws NotFoundException, DataException {
 		storage.deleteObject(storage.getObjectByIdAndApp(id, capp,
 				Notification.class));
 		return true;
 	}
 
 	public boolean deleteByUser(String id, String userId)
 			throws NotFoundException, DataException {
 		storage.deleteObject(storage.getObjectByIdAndUser(id, userId,
 				Notification.class));
 		return true;
 	}
 
 	public boolean deleteById(String id) throws NotFoundException,
 			DataException {
 		storage.deleteObject(storage.getObjectById(id, Notification.class));
 		return true;
 	}
 
 	public List<Notification> get(String userId, String capp, Long since,
 			Integer position, Integer count, NotificationFilter filter)
 			throws DataException {
 		return storage.searchNotifications(userId, capp, since, position,
 				count, filter);
 	}
 
 	public Notification getById(String id) throws NotFoundException,
 			DataException {
 		return storage.getObjectById(id, Notification.class);
 	}
 
 	public Notification getByIdAndApp(String id, String capp)
 			throws NotFoundException, DataException {
 		return storage.getObjectByIdAndApp(id, capp, Notification.class);
 	}
 
 	public Notification getByIdAndUser(String id, String userId)
 			throws NotFoundException, DataException {
 		return storage.getObjectByIdAndUser(id, userId, Notification.class);
 	}
 
 	/**
 	 * set starred value to given value
 	 * 
 	 * @param id
 	 * @param starredStatus
 	 * @throws NotFoundException
 	 * @throws DataException
 	 */
 	public void starredByApp(String id, String capp, boolean starredStatus)
 			throws NotFoundException, DataException {
 		changeStarredStatusByApp(id, capp, starredStatus);
 	}
 
 	public void starredByUser(String id, String userid, boolean starredStatus)
 			throws NotFoundException, DataException {
 		changeStarredStatusByUser(id, userid, starredStatus);
 	}
 
 	public void starredById(String id, boolean starredStatus)
 			throws NotFoundException, DataException {
 		changeStarredStatusById(id, starredStatus);
 	}
 
 	/**
 	 * set starred value to true
 	 * 
 	 * @param id
 	 *            notification id
 	 * @throws NotFoundException
 	 * @throws DataException
 	 */
 	public void starredByApp(String id, String capp) throws NotFoundException,
 			DataException {
 		changeStarredStatusByApp(id, capp, true);
 	}
 
 	public void starredByUser(String id, String userId)
 			throws NotFoundException, DataException {
 		changeStarredStatusByUser(id, userId, true);
 	}
 
 	private void changeStarredStatusByUser(String id, String userId,
 			boolean starred) throws NotFoundException, DataException {
 		Notification notification = storage.getObjectByIdAndUser(id, userId,
 				Notification.class);
 		notification.setStarred(starred);
 		storage.storeObject(notification);
 	}
 
 	private void changeStarredStatusByApp(String id, String capp,
 			boolean starred) throws NotFoundException, DataException {
 		Notification notification = storage.getObjectByIdAndApp(id, capp,
 				Notification.class);
 		notification.setStarred(starred);
 		storage.storeObject(notification);
 	}
 
 	private void changeStarredStatusById(String id, boolean starred)
 			throws NotFoundException, DataException {
 		Notification notification = storage.getObjectById(id,
 				Notification.class);
 		notification.setStarred(starred);
 		storage.storeObject(notification);
 	}
 
 	public void updateLabelsByApp(String id, String capp, List<String> labelIds)
 			throws NotFoundException, DataException {
 		Notification notification = storage.getObjectByIdAndApp(id, capp,
 				Notification.class);
 		notification.setLabelIds(labelIds);
 		storage.storeObject(notification);
 	}
 
 	public void updateLabelsById(String id, List<String> labelIds)
 			throws NotFoundException, DataException {
 		Notification notification = storage.getObjectById(id,
 				Notification.class);
 		notification.setLabelIds(labelIds);
 		storage.storeObject(notification);
 	}
 
 	public void updateLabelsByUser(String id, String userid,
 			List<String> labelIds) throws NotFoundException, DataException {
 		Notification notification = storage.getObjectByIdAndUser(id, userid,
 				Notification.class);
 		notification.setLabelIds(labelIds);
 		storage.storeObject(notification);
 	}
 
 	public void deleteUserMessages(String userId) throws DataException {
 		storage.deleteObjectsPermanently(Notification.class, userId);
 	}
 
 }
