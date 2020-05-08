 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.communicator.custom.data;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.UUID;
 
 import android.accounts.Account;
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.RemoteException;
 import android.util.Log;
 import android.widget.Toast;
 import eu.trentorise.smartcampus.ac.AACException;
 import eu.trentorise.smartcampus.ac.SCAccessProvider;
 import eu.trentorise.smartcampus.android.common.GlobalConfig;
 import eu.trentorise.smartcampus.communicator.R;
 import eu.trentorise.smartcampus.communicator.custom.NotificationProcessor;
 import eu.trentorise.smartcampus.communicator.model.Channel;
 import eu.trentorise.smartcampus.communicator.model.CommunicatorConstants;
 import eu.trentorise.smartcampus.communicator.model.CommunicatorConstants.ORDERING;
 import eu.trentorise.smartcampus.communicator.model.EntityObject;
 import eu.trentorise.smartcampus.communicator.model.LabelObject;
 import eu.trentorise.smartcampus.communicator.model.Notification;
 import eu.trentorise.smartcampus.communicator.model.NotificationFilter;
 import eu.trentorise.smartcampus.communicator.model.Preference;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
 import eu.trentorise.smartcampus.storage.BatchModel;
 import eu.trentorise.smartcampus.storage.BatchModel.DeleteModel;
 import eu.trentorise.smartcampus.storage.DataException;
 import eu.trentorise.smartcampus.storage.StorageConfigurationException;
 import eu.trentorise.smartcampus.storage.db.StorageConfiguration;
 import eu.trentorise.smartcampus.storage.sync.SyncStorage;
 import eu.trentorise.smartcampus.storage.sync.SyncUpdateModel;
 
 public class CommunicatorHelper {
 
 	private static final LabelObject LABEL_ALL = new LabelObject("-1","All labels", "0");
 
 	private static final boolean testing = false;
 	
 	private static CommunicatorHelper instance = null;
 
 	private static SCAccessProvider accessProvider = null; 
 	
 
 //	private SyncManager mSyncManager;
 	private static Context mContext;
 	private StorageConfiguration sc = null;
 //	private SyncStorageConfiguration config = null;
 	private CommSyncStorage storage = null;
 
 	private Map<String,Channel> channelMap;
 	private Integer unread = null;
 	private Preference preferences = null;
 	private Map<String,LabelObject> labelMap = null;
 	private boolean loaded = false;
 	
 	public static String mToken;
 
 	public static void init(Context ctx) {
 		mContext = ctx;
 		if (instance == null) instance = new CommunicatorHelper(mContext);
 		
 		//accessProvider = SCAccessProvider.getInstance(mContext);
 //		initTmpData(mContext);
 //		instance.funnelMap = tmpFunnels;
 //		instance.preferences = tmpPrefs;
 	}
 
 	public static String getAuthToken() throws AACException {
		mToken = CommunicatorHelper.getAccessProvider().readToken(
				mContext);
 		return mToken; //HomeActivity.userAuthToken;//getAccessProvider().readToken(instance.mContext);//adToken(instance.mContext, null);
 	}
 
 	private static CommunicatorHelper getInstance() throws DataException {
 		if (instance == null) throw new DataException("CommuncatorHelper is not initialized");
 		
 		return instance;
 	}
 
 	public static SCAccessProvider getAccessProvider() {
 		if(accessProvider == null)
 			accessProvider = SCAccessProvider.getInstance(mContext);
 		return accessProvider;
 	}
 
 	public static SyncStorage getSyncStorage() throws DataException {
 		return getInstance().storage;
 	}
 	protected CommunicatorHelper(Context mContext) {
 		super();
 		this.mContext = mContext;
 //		this.mSyncManager = new SyncManager(mContext, CommSyncStorageService.class);
 		this.accessProvider = SCAccessProvider.getInstance(mContext);
 		this.sc = new CommunicatorStorageConfiguration();
 		this.storage = new CommSyncStorage(mContext, Constants.APP_TOKEN, Constants.SYNC_DB_NAME, 2, sc);
 	}
 
 	public static void start(boolean local) throws RemoteException, DataException, StorageConfigurationException, ConnectionException, ProtocolException, SecurityException, AACException, NameNotFoundException {
 		if (testing) {
 			Collection<Preference> coll = getInstance().storage.getObjects(Preference.class);
 			if (coll.isEmpty()) {
 				initTmpData(getInstance().mContext);
 				Preference prefs = new Preference();
 				prefs.setLabels(new ArrayList<LabelObject>(tmpLabels.values()));
 				getInstance().preferences = getInstance().storage.create(prefs);
 				for (Channel f : tmpChannels.values()) {
 					getInstance().storage.update(f,true);
 				}
 				for (Notification n : tmp) {
 					Notification nnn = getInstance().storage.create(n);
 				}
 			} else {
 				getInstance().preferences = coll.iterator().next();
 			}
 			Collection<Channel> channelColl = getInstance().storage.getObjects(Channel.class);
 			
 			getInstance().channelMap = new TreeMap<String, Channel>();
 			if (channelColl != null && !channelColl.isEmpty()) {
 				for (Channel f : channelColl) {
 					getInstance().channelMap.put(f.getId(), f);
 				}
 			}
 			getInstance().loaded = true;
 		} else if (!local) {
 			getInstance().loadData();
 		} 
 		if (getInstance().loaded) {
 			updateSyncAdapter(getPreferences());
 		} else {
 	        ContentResolver.setSyncAutomatically(new Account(eu.trentorise.smartcampus.ac.Constants.getAccountName(getInstance().mContext), eu.trentorise.smartcampus.ac.Constants.getAccountType(getInstance().mContext)), "eu.trentorise.smartcampus.communicator", true);
 	        ContentResolver.addPeriodicSync(new Account(eu.trentorise.smartcampus.ac.Constants.getAccountName(getInstance().mContext), eu.trentorise.smartcampus.ac.Constants.getAccountType(getInstance().mContext)), "eu.trentorise.smartcampus.communicator", new Bundle(), Preference.DEF_SYNC_PERIOD*60);
 		}
 	}
 
 	private void loadData() throws DataException, StorageConfigurationException, ConnectionException, ProtocolException, SecurityException, RemoteException, AACException {
 		if (loaded) return;
 		Collection<Channel> channelColl = null;
 
 		Collection<Preference> coll = storage.getObjects(Preference.class);
 		if (coll == null || coll.isEmpty()) {
 			storage.synchronize(getAuthToken(), GlobalConfig.getAppUrl(getInstance().mContext), Constants.SYNC_SERVICE);
 			coll = storage.getObjects(Preference.class);
 		}
 		if (coll == null || coll.isEmpty()) {
 			preferences = storage.create(new Preference());
 		} else {
 			preferences = coll.iterator().next();
 		}
 		
 		channelColl = storage.getObjects(Channel.class);
 
 		channelMap = new TreeMap<String, Channel>();
 		if (channelColl != null && !channelColl.isEmpty()) {
 			for (Channel f : channelColl) {
 				channelMap.put(f.getId(), f);
 			}
 		}
 		loaded = true;
 	}
 
 	public static void synchronize() throws RemoteException, DataException, StorageConfigurationException, SecurityException, ConnectionException, ProtocolException, AACException {
 		getInstance().unread = null;
 		getInstance().storage.synchronize(getAuthToken(), GlobalConfig.getAppUrl(getInstance().mContext), Constants.SYNC_SERVICE);
 //		getInstance().mSyncManager.synchronize(getAuthToken(),
 //				Constants.APP_TOKEN);
 	}
 	public static void synchronizeInBG() throws RemoteException, DataException, StorageConfigurationException, SecurityException, ConnectionException, ProtocolException, NameNotFoundException {
 		getInstance().unread = null;
         ContentResolver.requestSync(new Account(eu.trentorise.smartcampus.ac.Constants.getAccountName(getInstance().mContext), eu.trentorise.smartcampus.ac.Constants.getAccountType(getInstance().mContext)), "eu.trentorise.smartcampus.communicator", new Bundle());
 		//getInstance().mSyncManager.synchronize(getAuthToken(), Constants.APP_TOKEN);
 	}
 
 	public static void destroy() throws DataException {
 //		getInstance().mSyncManager.disconnect();
 	}
 
 	public static void endAppFailure(Activity activity, int id) {
 		Toast.makeText(activity, activity.getResources().getString(id),
 				Toast.LENGTH_LONG).show();
 		activity.finish();
 	}
 
 	public static void showFailure(Activity activity, int id) {
 		Toast.makeText(activity, activity.getResources().getString(id),
 				Toast.LENGTH_LONG).show();
 	}
 
 	public static List<Notification> getNotifications(NotificationFilter filter, int position, int size, long since) {
 		try {
 			Collection<Notification> collection = getRawNotifications(filter, position, size, since);
 			
 			if (collection.size() > 0) {
 				NotificationProcessor processor = null;
 				List<Notification> list = new ArrayList<Notification>();
 				for (Notification n : collection) {
 					if (n.getType() == null) {
 						continue;
 					}
 					
 					if ((processor = CommunicatorConstants.getNotificationProcessor(getInstance().mContext, n.getType())) != null) {
 						processor.processMessage(getInstance().mContext, n);
 					}
 					list.add(n);
 				}
 				return list;
 			}
 			return Collections.emptyList();
 		} catch (Exception e) {
 			return Collections.emptyList();
 		}
 	}
 
 	/**
 	 * @param filter
 	 * @param position
 	 * @param size
 	 * @param since
 	 * @return
 	 * @throws DataException
 	 * @throws StorageConfigurationException
 	 */
 	private static Collection<Notification> getRawNotifications(NotificationFilter filter, int position, int size, long since)
 			throws DataException, StorageConfigurationException {
 		List<String> params = new ArrayList<String>();
 		String query = createQuery(filter, since, params); 
 		
 		Collection<Notification> collection = null;
 		if (filter.getOrdering() == null || filter.getOrdering().equals(ORDERING.ORDER_BY_ARRIVAL)) {
 			collection = getInstance().storage.query(Notification.class, query, params.toArray(new String[params.size()]), position, size, "timestamp DESC");
 		} 
 		else if (filter.getOrdering().equals(ORDERING.ORDER_BY_TITLE)) {
 			collection = getInstance().storage.query(Notification.class, query, params.toArray(new String[params.size()]), position, size, "title ASC");
 		} else {
 			// TODO: sort!
 			collection = getInstance().storage.query(Notification.class, query, params.toArray(new String[params.size()]), position, size);
 		}
 		return collection;
 	}
 
 	private static String createQuery(NotificationFilter filter, long since, List<String> params) {
 		String query = "";
 		if (filter.isReaded() != null && filter.isReaded()) {
 			query += "readed > 0";
 		} else if (filter.isReaded() != null && !filter.isReaded()) {
 			query += "readed = 0";
 		}
 		if (filter.isStarred()!= null && filter.isStarred()) {
 			query += (query.length() > 0 ? " AND ":"")+"starred > 0";
 		} else if (filter.isStarred()!= null && !filter.isStarred()) {
 			query += (query.length() > 0 ? " AND ":"")+"starred = 0";
 		}
 		if (filter.getChannelId() != null) {
 			query += (query.length() > 0 ? " AND ":"")+"channelIds LIKE '%\""+filter.getChannelId()+"\"%'";
 		}
 		if (filter.getLabelId() != null) {
 			LabelObject label = getLabel(filter.getLabelId());
 			if (label != null) {
 				query += (query.length() > 0 ? " AND ":"")+"labelIds LIKE '%\""+filter.getLabelId()+"\"%'";
 			}
 		}
 		if (filter.getSearchText() != null && filter.getSearchText().length()!=0) {
 			query += (query.length() > 0 ? " AND ":"")+"(notifications MATCH ?)";
 			params.add(filter.getSearchText());
 		} 
 		
 		if (since > 0) {
 			query += (query.length() > 0 ? " AND ":"")+"(timestamp > "+since+")";
 		}
 		return query;
 	}
 
 	
 	private static List<Notification> tmp = new ArrayList<Notification>();
 	private static Map<String,LabelObject> tmpLabels = new TreeMap<String,LabelObject>();
 	private static Map<String,Channel> tmpChannels = new TreeMap<String,Channel>();
 	private static Preference tmpPrefs = new Preference();
 	
 	private static void initTmpData(Context mContext) {
 		for (int i = 0; i < 10; i++) {
 			Notification n = new Notification();
 			n.setContent(new HashMap<String, Object>());
 			n.setDescription("Some description: "+i);
 			n.setEntities(Arrays.asList(new EntityObject[0]));
 			n.setChannelIds(Collections.singletonList(""+(int)(Math.random()*5)));
 			n.setId(""+i);
 			n.setStarred(i < 5 ? true:false);
 			n.setReaded(i > 5 ? true:false);
 			
 			List<String> labels = new ArrayList<String>();
 			labels.add(""+(i%5));
 			labels.add(""+((i+2)%5));
 			n.setLabelIds(labels);
 			
 			n.setTimestamp(((Double)(Math.random()*System.currentTimeMillis())).longValue());
 			n.setTitle("Some title:"+i);
 			tmp.add(n);
 		}
 		
 		for (int i = 0; i < 5; i++) {
 			LabelObject lo = new LabelObject(""+i,"Label "+i, "#FFCCC"+i+"00");
 			tmpLabels.put(lo.getId(),lo);
 		}
 		Channel f = null;
 		String[] sourceTypes = mContext.getResources().getStringArray(R.array.channel_type_sourcetypes);
 		for (int i = 0; i < sourceTypes.length; i++) {
 			f = new Channel();
 			f.setId(""+i);
 			f.setSourceType(sourceTypes[i]);
 			f.setTitle(CommunicatorConstants.getChannelTypeLabel(mContext, f.getSourceType()));
 			tmpChannels.put(f.getId(),f);
 		}
 		
 		tmpPrefs.setLabels(new ArrayList<LabelObject>(tmpLabels.values()));
 	}
 	
 	public static List<Channel> getChannels(boolean feed) {
 		List<Channel> list = new ArrayList<Channel>();
 		try {
 			for (Channel c : getInstance().channelMap.values()) {
 				if (c.isFeed() == feed) list.add(c);
 			}
 		} catch (Exception e) {
 			return Collections.emptyList();
 		}
 		return list;
 	}
 
 	public static Channel getChannel(String channelId) {
 		try {
 			return getInstance().channelMap.get(channelId);
 		} catch (DataException e) {
 			return null;
 		}
 	}
 
 	public static List<String> getLabelsForSelector() {
 		List<String> result = new ArrayList<String>();
 		result.add(LABEL_ALL.getName());
 		for (LabelObject l : getLabels()) result.add(l.getName());
 		return result;
 	}
 
 	private Map<String,LabelObject> getLabelMap() {
 		if (labelMap == null) {
 			labelMap = new TreeMap<String, LabelObject>();
 			for (LabelObject lo : preferences.getLabels()) {
 				labelMap.put(lo.getId(), lo);
 			}
 		}
 		return labelMap;
 	}
 	
 	public static List<LabelObject> getLabels() {
 		
 		try {
 			return new ArrayList<LabelObject>(getInstance().getLabelMap().values());
 		} catch (Exception e) {
 			return Collections.emptyList();
 		}
 	}
 
 	public static LabelObject getLabel(String id) {
 		try {
 			return getInstance().getLabelMap().get(id);
 		} catch (DataException e) {
 			return null;
 		}
 	}
 
 
 	public static LabelObject getLabelByName(String labelName) {
 		try {
 			for (LabelObject l : getInstance().getLabelMap().values()) {
 				if (l.getName().equals(labelName)) return l;
 			}
 		} catch (DataException e) {
 		}
 		return null;
 	}
 
 	public static CharSequence[] getLabelNames() {
 		List<CharSequence> names = new ArrayList<CharSequence>();
 		try {
 			for (LabelObject l : getInstance().getLabelMap().values()) {
 				if (!l.getName().equals(LABEL_ALL)) names.add(l.getName());
 			}
 		} catch (DataException e) {
 			return new CharSequence[0];
 		}
 		return names.toArray(new CharSequence[names.size()]);
 	}
 
 	public static LabelObject createLabel(String nameText, int color) throws DataException, StorageConfigurationException {
 		String id = UUID.randomUUID().toString();
 		LabelObject l = new LabelObject(id,nameText,"#"+Integer.toHexString(color));
 		List<LabelObject> list = getInstance().preferences.getLabels();
 		if (list == null) list = new ArrayList<LabelObject>();
 		list.add(l);
 		getInstance().preferences.setLabels(list);
 		getInstance().storage.update(getInstance().preferences, false);
 		getInstance().getLabelMap().put(id, l);
 		return l;
 	}
 
 	public static LabelObject updateLabel(LabelObject label, String nameText, int color) throws DataException, StorageConfigurationException {
 
 		List<LabelObject> list = getInstance().preferences.getLabels();
 		for (LabelObject l : list) {
 			if (l.getId().equals(label.getId())) {
 				l.setName(nameText);
 				l.setColor("#"+Integer.toHexString(color));
 				getInstance().preferences.setLabels(list);
 				getInstance().storage.update(getInstance().preferences, false);
 				getInstance().getLabelMap().put(l.getId(), l);
 				return l;
 			}
 		}
 		return label;
 	}
 
 	public static void assignLabel(Notification content, CharSequence name) throws DataException, StorageConfigurationException {
 		LabelObject label = getLabelByName(name.toString());
 		if (content.getLabelIds()==null) content.setLabelIds(new ArrayList<String>());
 		if (!content.getLabelIds().contains(label.getId())) content.getLabelIds().add(label.getId());
 		Notification n = getInstance().storage.getObjectById(content.getId(), Notification.class);
 		n.setLabelIds(content.getLabelIds());
 		getInstance().storage.update(n, false);
 	}
 
 	public static void removeNotification(Notification content) throws DataException, StorageConfigurationException {
 		getInstance().storage.delete(content.getId(), Notification.class);
 	}
 
 	public static void toggleRead(Notification content) throws DataException, StorageConfigurationException {
 		getInstance().unread = null;
 		content.setReaded(!content.isReaded());
 		Notification n = getInstance().storage.getObjectById(content.getId(), Notification.class);
 		n.setReaded(content.isReaded());
 		getInstance().storage.update(n, false);
 	}
 
 	public static void toggleStar(Notification content) throws DataException, StorageConfigurationException {
 		content.setStarred(!content.isStarred());
 		Notification n = getInstance().storage.getObjectById(content.getId(), Notification.class);
 		n.setStarred(content.isStarred());
 		getInstance().storage.update(n, false);
 	}
 
 	public static void markAllAsRead(NotificationFilter filter) throws DataException, StorageConfigurationException {
 		getInstance().unread = null;
 		filter.setReaded(false);
 		List<BatchModel> list = new ArrayList<BatchModel>();
 		for (Notification n : getRawNotifications(filter, 0, -1, 0)) {
 			n.setReaded(true);
 			list.add(new SyncUpdateModel.UpdateModel(n, false, true));
 		}
 		getInstance().storage.batch(list);
 	}
 	public static void deleteAll(NotificationFilter filter) throws DataException, StorageConfigurationException {
 		getInstance().unread = null;
 		List<BatchModel> list = new ArrayList<BatchModel>();
 		
 		List<String> params = new ArrayList<String>();
 		String where = createQuery(filter, 0, params); 
 
 		String query = "SELECT id FROM notifications";
 		if (where != null && where.length() > 0) {
 			query += " WHERE "+where;
 		}
 		
 		Cursor c = getInstance().storage.rawQuery(query, params.toArray(new String[params.size()]));
 		if (c != null) {
 			c.moveToFirst();
 			for (int i = 0; i < c.getCount(); i++) {
 				list.add(new DeleteModel(c.getString(0),Notification.class));
 				c.moveToNext();
 			}
 		}
 		getInstance().storage.batch(list);
 	}
 
 	public static Integer getUnreadCount() {
 		try {
 			return getInstance().unread;
 		} catch (DataException e) {
 			return null;
 		}
 	}
 	public static Integer readUnreadCount() {
 		try {
 			getInstance().unread = queryUnread();
 			return getInstance().unread;
 		} catch (DataException e) {
 			return null;
 		}
 	}
 
 	public static void resetUnread() {
 		try {
 			getInstance().unread = null;
 		} catch (DataException e) {
 			// do nothing
 		}
 	}
 	
 	private static Integer queryUnread() {
 		try {
 			Cursor c = getInstance().storage.rawQuery("SELECT COUNT(*) FROM notifications WHERE readed = 0", null);
 			if (c != null && c.getCount() > 0) {
 				c.moveToNext();
 				return c.getInt(0);
 			}  
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	public static boolean removeLabel(LabelObject content) throws DataException, StorageConfigurationException {
 		List<LabelObject> list = getInstance().preferences.getLabels();
 		for (int i = 0; i < list.size(); i++) {
 			LabelObject l = list.get(i);
 			if (l.getId().equals(content.getId())) {
 				list.remove(i);
 				getInstance().preferences.setLabels(list);
 				getInstance().storage.update(getInstance().preferences, false);
 				getInstance().getLabelMap().remove(l.getId());
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static boolean removeChannel(Channel content) throws DataException, StorageConfigurationException, RemoteException, SecurityException, ConnectionException, ProtocolException, AACException {
 		getInstance().storage.delete(content.getId(), Channel.class);
 		boolean result = getInstance().channelMap.remove(content.getId()) != null;
 		synchronize();
 		return result;
 	}
 
 	public static Preference getPreferences() {
 		try {
 			return getInstance().preferences;
 		} catch (DataException e) {
 			Log.e(CommunicatorHelper.class.getName(), ""+e.getMessage());
 			return new Preference();
 		}
 	}
 
 	public static boolean saveChannel(Channel channel) throws DataException, StorageConfigurationException, RemoteException, SecurityException, ConnectionException, ProtocolException, AACException {
 		if (channel.getId() == null) {
 			Channel newChannel = getInstance().storage.create(channel);
 			getInstance().channelMap.put(newChannel.getId(), newChannel);
 		} else {
 			getInstance().storage.update(channel, false);
 			getInstance().channelMap.put(channel.getId(), channel);
 		}
 		synchronize();
 		return true;
 	}
 
 	public static void updatePrefs(Preference prefs) {
 		try {
 			getInstance().preferences = prefs;
 			getInstance().storage.update(getInstance().preferences, false);
 			updateSyncAdapter(prefs);
 		} catch (Exception e) {
 			Log.e(CommunicatorHelper.class.getName(),"Failed to store preferences: "+e.getMessage());
 		}
 	}
 
 	private static void updateSyncAdapter(Preference prefs) throws DataException, NameNotFoundException {
 		if (getPreferences().isSynchronizeAutomatically()) {
 	        ContentResolver.setSyncAutomatically(new Account(eu.trentorise.smartcampus.ac.Constants.getAccountName(getInstance().mContext), eu.trentorise.smartcampus.ac.Constants.getAccountType(getInstance().mContext)), "eu.trentorise.smartcampus.communicator", true);
 	        ContentResolver.addPeriodicSync(new Account(eu.trentorise.smartcampus.ac.Constants.getAccountName(getInstance().mContext), eu.trentorise.smartcampus.ac.Constants.getAccountType(getInstance().mContext)), "eu.trentorise.smartcampus.communicator", new Bundle(), getInstance().preferences.getSyncPeriod()*60);
 		} else {
 	        ContentResolver.setSyncAutomatically(new Account(eu.trentorise.smartcampus.ac.Constants.getAccountName(getInstance().mContext), eu.trentorise.smartcampus.ac.Constants.getAccountType(getInstance().mContext)), "eu.trentorise.smartcampus.communicator", false);
 		}
 		
 	}
 }
