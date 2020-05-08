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
 package eu.trentorise.smartcampus.dt.custom.data;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 import android.accounts.Account;
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.database.Cursor;
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 import android.os.RemoteException;
 import android.util.Log;
 import android.widget.Toast;
 import eu.trentorise.smartcampus.ac.SCAccessProvider;
 import eu.trentorise.smartcampus.ac.authenticator.AMSCAccessProvider;
 import eu.trentorise.smartcampus.ac.model.UserData;
 import eu.trentorise.smartcampus.android.common.GlobalConfig;
 import eu.trentorise.smartcampus.android.common.LocationHelper;
 import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
 import eu.trentorise.smartcampus.android.common.tagging.SuggestionHelper;
 import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
 import eu.trentorise.smartcampus.dt.model.BaseDTObject;
 import eu.trentorise.smartcampus.dt.model.EventObject;
 import eu.trentorise.smartcampus.dt.model.ObjectFilter;
 import eu.trentorise.smartcampus.dt.model.POIObject;
 import eu.trentorise.smartcampus.dt.model.StepObject;
 import eu.trentorise.smartcampus.dt.model.StoryObject;
 import eu.trentorise.smartcampus.dt.model.UserEventObject;
 import eu.trentorise.smartcampus.dt.model.UserPOIObject;
 import eu.trentorise.smartcampus.dt.model.UserStoryObject;
 import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
 import eu.trentorise.smartcampus.protocolcarrier.common.Constants.Method;
 import eu.trentorise.smartcampus.protocolcarrier.custom.MessageRequest;
 import eu.trentorise.smartcampus.protocolcarrier.custom.MessageResponse;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
 import eu.trentorise.smartcampus.storage.DataException;
 import eu.trentorise.smartcampus.storage.StorageConfigurationException;
 import eu.trentorise.smartcampus.storage.db.StorageConfiguration;
 import eu.trentorise.smartcampus.storage.remote.RemoteStorage;
 import eu.trentorise.smartcampus.storage.sync.SyncStorage;
 import eu.trentorise.smartcampus.storage.sync.SyncStorageWithPaging;
 import eu.trentorise.smartcampus.storage.sync.Utils;
 
 public class DTHelper {
 
 	private static DTHelper instance = null;
 
 	private static SCAccessProvider accessProvider = new AMSCAccessProvider();
 
 	//private SyncManager mSyncManager;
 	private Context mContext;
 	//private SyncStorageConfiguration config = null;
 	private SyncStorageWithPaging storage = null;
 	private static RemoteStorage remoteStorage = null;
 
 	private ProtocolCarrier mProtocolCarrier = null;
 
 	private static LocationHelper mLocationHelper;
 	
 	private boolean syncInProgress = false;
 	private SherlockFragmentActivity rootActivity = null;
 
 	public static void init(Context mContext) {
 		if (instance == null)
 			instance = new DTHelper(mContext);
 	}
 
 	public static SCAccessProvider getAccessProvider() {
 		return accessProvider;
 	}
 
 	public static String getAuthToken() {
 		return getAccessProvider().readToken(instance.mContext, null);
 	}
 
 	private static DTHelper getInstance() throws DataException {
 		if (instance == null)
 			throw new DataException("DTHelper is not initialized");
 		return instance;
 	}
 
 	protected DTHelper(Context mContext) {
 		super();
 		this.mContext = mContext;
 		//this.mSyncManager = new SyncManager(mContext, DTSyncStorageService.class);
 		StorageConfiguration sc = new DTStorageConfiguration();
 		//this.config = new SyncStorageConfiguration(sc, GlobalConfig.getAppUrl(mContext), Constants.SYNC_SERVICE, Constants.SYNC_INTERVAL);
 		if (Utils.getDBVersion(mContext, Constants.APP_TOKEN) != 3) {
 			Utils.writeObjectVersion(mContext, Constants.APP_TOKEN, 0);
 		}
 		this.storage = new SyncStorageWithPaging(mContext, Constants.APP_TOKEN, Constants.SYNC_DB_NAME, 3, sc);
 		this.mProtocolCarrier = new ProtocolCarrier(mContext, Constants.APP_TOKEN);
 
 		// LocationManager locationManager = (LocationManager)
 		// mContext.getSystemService(Context.LOCATION_SERVICE);
 		// locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
 		// 0, 0, new DTLocationListener());
 		// locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
 		// 0, 0, new DTLocationListener());
 		// locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 		// 0, 0, new DTLocationListener());
 		setLocationHelper(new LocationHelper(mContext));
 	}
 
 	/**
 	 * @return true if the DB synchronization is required: the last synchronization
 	 * happened more than {@link Constants#SYNC_INTERVAL} minutes ago.
 	 * @throws DataException 
 	 * @throws NameNotFoundException 
 	 */
 	public static boolean syncRequired() throws DataException, NameNotFoundException {
 		if (getInstance().syncInProgress) return true;
 		return System.currentTimeMillis()-Utils.getLastObjectSyncTime(getInstance().mContext, Constants.APP_TOKEN) > Constants.SYNC_INTERVAL*60*1000;
 	}
	
	public static SherlockFragmentActivity start(SherlockFragmentActivity activity) throws RemoteException, DataException, StorageConfigurationException, SecurityException,
			ConnectionException, ProtocolException, NameNotFoundException {
		getInstance().rootActivity = activity;
 		try {
 			String authority = Constants.getAuthority(getInstance().mContext);
 			Account account = new Account(eu.trentorise.smartcampus.ac.Constants.getAccountName(getInstance().mContext), eu.trentorise.smartcampus.ac.Constants.getAccountType(getInstance().mContext));
 
 			ContentResolver.setIsSyncable(account, authority, 1);
 			ContentResolver.setSyncAutomatically(account, authority, true);
 			ContentResolver.addPeriodicSync(account, authority, new Bundle(),Constants.SYNC_INTERVAL*60);

 			if (getInstance().syncInProgress) return null;
 
 			if (Utils.getObjectVersion(getInstance().mContext, Constants.APP_TOKEN) <= 0) {
 				Utils.writeObjectVersion(getInstance().mContext, Constants.APP_TOKEN, 1L);
 			} 
 
 			getInstance().syncInProgress = true;
 			getInstance().storage.synchronize(getAuthToken(), GlobalConfig.getAppUrl(getInstance().mContext), Constants.SYNC_SERVICE);
 			
 		} finally {
 			getInstance().syncInProgress = false;
 		}
 		return getInstance().rootActivity;
 	}
 
 	public static void synchronize() throws RemoteException, DataException, StorageConfigurationException, SecurityException, ConnectionException, ProtocolException {
 		getInstance().storage.synchronize(getAuthToken(), GlobalConfig.getAppUrl(getInstance().mContext), Constants.SYNC_SERVICE);
 //        ContentResolver.requestSync(new Account(eu.trentorise.smartcampus.ac.Constants.ACCOUNT_NAME, eu.trentorise.smartcampus.ac.Constants.ACCOUNT_TYPE), "eu.trentorise.smartcampus.dt", new Bundle());
 	}
 
 	public static void destroy() {
 		try {
 			String authority = Constants.getAuthority(getInstance().mContext);
 			Account account = new Account(eu.trentorise.smartcampus.ac.Constants.getAccountName(getInstance().mContext), eu.trentorise.smartcampus.ac.Constants.getAccountType(getInstance().mContext));
 			ContentResolver.removePeriodicSync(account, authority, new Bundle());
 			ContentResolver.setSyncAutomatically(account, authority, false);
 			ContentResolver.setIsSyncable(account, authority, 0);
 		} catch (Exception e) {
 			Log.e(DTHelper.class.getName(), "Failed destroy: "+e.getMessage());
 		}
 	}
 
 	// public static Collection<POIObject> getAllPOI() throws DataException,
 	// StorageConfigurationException, ConnectionException, ProtocolException,
 	// SecurityException {
 	// if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 	// return getInstance().storage.getObjects(POIObject.class);
 	// } else {
 	// return Collections.emptyList();
 	// }
 	// }
 	public static String[] getAllPOITitles() {
 		Cursor cursor = null;
 		try {
 			cursor = getInstance().storage.rawQuery("select title from pois", null);
 			if (cursor != null) {
 				String[] result = new String[cursor.getCount()];
 				cursor.moveToFirst();
 				int i = 0;
 				while (cursor.getPosition() < cursor.getCount()) {
 					result[i] = cursor.getString(0);
 					cursor.moveToNext();
 					i++;
 				}
 				return result;
 			}
 		} catch (Exception e) {
 			Log.e(DTHelper.class.getName(), "" + e.getMessage());
 		} finally {
 			try {
 				getInstance().storage.cleanCursor(cursor);
 			} catch (DataException e) {
 			}
 		}
 		return new String[0];
 	}
 
 	public static POIObject findPOIByTitle(String text) {
 		try {
 			Collection<POIObject> poiCollection = getInstance().storage.query(POIObject.class, "title = ?", new String[] { text });
 			if (poiCollection.size() > 0)
 				return poiCollection.iterator().next();
 			return null;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public static POIObject findPOIById(String poiId) {
 		try {
 			POIObject poi = getInstance().storage.getObjectById(poiId, POIObject.class);
 			return poi;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * return true if the object was created and false if updated
 	 * 
 	 * @param poi
 	 * @return
 	 * @throws DataException
 	 * @throws ConnectionException
 	 * @throws ProtocolException
 	 * @throws SecurityException
 	 * @throws RemoteException
 	 * @throws StorageConfigurationException
 	 */
 	/*
 	 * public static boolean savePOI(POIObject poi) throws DataException,
 	 * ConnectionException, ProtocolException, SecurityException,
 	 * RemoteException, StorageConfigurationException { String requestService =
 	 * null; Method method = null; Boolean result = null; if (poi.getId() ==
 	 * null) { if (poi.createdByUser()) requestService = Constants.SERVICE +
 	 * "/eu.trentorise.smartcampus.dt.model.UserPOIObject"; else throw new
 	 * DataException("cannot create service object"); method = Method.POST;
 	 * result = true; } else { if (poi.createdByUser()) requestService =
 	 * Constants.SERVICE + "/eu.trentorise.smartcampus.dt.model.UserPOIObject/"
 	 * + poi.getId(); else requestService = Constants.SERVICE +
 	 * "/eu.trentorise.smartcampus.dt.model.ServicePOIObject/" + poi.getId();
 	 * method = Method.PUT; result = false; } MessageRequest request = new
 	 * MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), requestService);
 	 * request.setMethod(method); String json =
 	 * eu.trentorise.smartcampus.android.common.Utils.convertToJSON(poi);
 	 * request.setBody(json);
 	 * 
 	 * // getRemote(instance.mContext, instance.token).create(poi);
 	 * 
 	 * synchronize(); return result; }
 	 */
 
 	/**
 	 * return the POI created or updated. Null if is not created
 	 * 
 	 * @param poi
 	 * @return
 	 * @throws DataException
 	 * @throws ConnectionException
 	 * @throws ProtocolException
 	 * @throws SecurityException
 	 * @throws RemoteException
 	 * @throws StorageConfigurationException
 	 */
 
 	public static POIObject savePOI(POIObject poi) throws DataException, ConnectionException, ProtocolException, SecurityException,
 			RemoteException, StorageConfigurationException {
 		String requestService = null;
 		Method method = null;
 		POIObject poiReturn = null;
 		// Boolean result = null;
 		if (poi.getId() == null) {
 			if (poi.createdByUser())
 				requestService = Constants.SERVICE + "/eu.trentorise.smartcampus.dt.model.UserPOIObject";
 			else
 				throw new DataException("cannot create service object");
 			method = Method.POST;
 			// result = true;
 		} else {
 			if (poi.createdByUser())
 				requestService = Constants.SERVICE + "/eu.trentorise.smartcampus.dt.model.UserPOIObject/" + poi.getId();
 			else
 				requestService = Constants.SERVICE + "/eu.trentorise.smartcampus.dt.model.ServicePOIObject/" + poi.getId();
 			method = Method.PUT;
 			// result = false;
 		}
 		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), requestService);
 		request.setMethod(method);
 		String json = eu.trentorise.smartcampus.android.common.Utils.convertToJSON(poi);
 		request.setBody(json);
 
 		MessageResponse msg = getInstance().mProtocolCarrier.invokeSync(request, Constants.APP_TOKEN, getAuthToken());
 		// getRemote(instance.mContext, instance.token).create(poi);
 		poiReturn = eu.trentorise.smartcampus.android.common.Utils.convertJSONToObject(msg.getBody(), POIObject.class);
 		synchronize();
 		return poiReturn;
 	}
 
 	/**
 	 * return true in case of create and false in case of update
 	 * 
 	 * @param event
 	 * @return
 	 * @throws RemoteException
 	 * @throws DataException
 	 * @throws StorageConfigurationException
 	 * @throws ConnectionException
 	 * @throws ProtocolException
 	 * @throws SecurityException
 	 */
 	public static Boolean saveEvent(EventObject event) throws RemoteException, DataException, StorageConfigurationException,
 			ConnectionException, ProtocolException, SecurityException {
 		String requestService = null;
 		Method method = null;
 		Boolean result = null;
 		if (event.getId() == null) {
 			if (event.createdByUser())
 				requestService = Constants.SERVICE + "/eu.trentorise.smartcampus.dt.model.UserEventObject";
 			else
 				throw new DataException("cannot create service object");
 			method = Method.POST;
 			result = true;
 		} else {
 			if (event.createdByUser())
 				requestService = Constants.SERVICE + "/eu.trentorise.smartcampus.dt.model.UserEventObject/" + event.getId();
 			else
 				requestService = Constants.SERVICE + "/eu.trentorise.smartcampus.dt.model.ServiceEventObject/" + event.getId();
 			method = Method.PUT;
 			result = false;
 		}
 		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), requestService);
 		request.setMethod(method);
 		String json = eu.trentorise.smartcampus.android.common.Utils.convertToJSON(event);
 		request.setBody(json);
 
 		getInstance().mProtocolCarrier.invokeSync(request, Constants.APP_TOKEN, getAuthToken());
 		// getRemote(instance.mContext, instance.token).create(poi);
 		synchronize();
 		return result;
 	}
 
 	public static Collection<BaseDTObject> getMostPopular() throws DataException, StorageConfigurationException, ConnectionException,
 			ProtocolException, SecurityException {
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			Collection<POIObject> pois = getInstance().storage.getObjects(POIObject.class);
 			ArrayList<BaseDTObject> list = new ArrayList<BaseDTObject>(pois);
 			if (pois.size() > 20) {
 				return list.subList(0, 20);
 			}
 			return list;
 		} else {
 			ObjectFilter filter = new ObjectFilter();
 			filter.setLimit(20);
 			return getRemote(instance.mContext, getAuthToken()).searchObjects(filter, BaseDTObject.class);
 		}
 	}
 
 	public static Collection<POIObject> getPOIByCategory(int position, int size, String... inCategories) throws DataException,
 			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
 
 		if (inCategories == null || inCategories.length == 0)
 			return Collections.emptyList();
 		
 		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));
 		
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			List<String> nonNullCategories = new ArrayList<String>();
 			String where = "";
 			for (int i = 0; i < categories.length; i++) {
 				if (where.length() > 0)
 					where += " or ";
 				if (categories[i] != null) {
 					nonNullCategories.add(categories[i]);
 					where += " type = ?";
 				} else {
 					where += " type is null";
 				}
 			}
 			return getInstance().storage.query(POIObject.class, where, nonNullCategories.toArray(new String[nonNullCategories.size()]),
 					position, size, "title ASC");
 		} else {
 			System.err.println("READING REMOTELY");
 			ArrayList<POIObject> result = new ArrayList<POIObject>();
 			for (String category : categories) {
 				ObjectFilter filter = new ObjectFilter();
 				filter.setSkip(position);
 				filter.setLimit(size);
 				filter.setType(category);
 				result.addAll(getRemote(instance.mContext, getAuthToken()).searchObjects(filter, POIObject.class));
 			}
 			return result;
 		}
 	}
 
 	public static Collection<POIObject> searchPOIs(int position, int size, String text) throws DataException,
 			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			if (text == null || text.trim().length() == 0) {
 				return getInstance().storage.getObjects(POIObject.class);
 			}
 			return getInstance().storage.query(POIObject.class, "pois MATCH ?", new String[] { text }, position, size, "title ASC");
 		} else {
 			ObjectFilter filter = new ObjectFilter();
 			Map<String, Object> criteria = new HashMap<String, Object>(1);
 			criteria.put("text", text);
 			filter.setCriteria(criteria);
 			filter.setSkip(position);
 			filter.setLimit(size);
 			return getRemote(instance.mContext, getAuthToken()).searchObjects(filter, POIObject.class);
 		}
 	}
 
 	public static Collection<POIObject> searchPOIsByCategory(int position, int size, String text, String... inCategories)
 			throws DataException, StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
 
 		if (inCategories == null || inCategories.length == 0)
 			return Collections.emptyList();
 		
 		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));
 		
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			List<String> nonNullCategories = new ArrayList<String>();
 			String where = "";
 			for (int i = 0; i < categories.length; i++) {
 				if (where.length() > 0)
 					where += " or ";
 				if (categories[i] != null) {
 					nonNullCategories.add(categories[i]);
 					where += " type = ?";
 				} else {
 					where += " type is null";
 				}
 			}
 			if (where.length() > 0) {
 				where = "(" + where + ")";
 			}
 			List<String> parameters = nonNullCategories;
 
 			if (text != null) {
 				where += "and ( pois MATCH ? )";
 				parameters.add(text);
 			}
 			return getInstance().storage.query(POIObject.class, where, parameters.toArray(new String[parameters.size()]), position, size,
 					"title ASC");
 		} else {
 			ArrayList<POIObject> result = new ArrayList<POIObject>();
 			for (String category : categories) {
 				ObjectFilter filter = new ObjectFilter();
 				filter.setType(category);
 				filter.setSkip(position);
 				filter.setLimit(size);
 				result.addAll(getRemote(instance.mContext, getAuthToken()).searchObjects(filter, POIObject.class));
 			}
 			return result;
 		}
 	}
 
 	public static Collection<EventObject> searchEventsByCategory(int position, int size, String text, String... inCategories)
 			throws DataException, StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
 
 		if (inCategories == null || inCategories.length == 0)
 			return Collections.emptyList();
 		
 		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));
 
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			List<String> nonNullCategories = new ArrayList<String>();
 			String where = "";
 			for (int i = 0; i < categories.length; i++) {
 				if (where.length() > 0)
 					where += " or ";
 				if (categories[i] != null) {
 					nonNullCategories.add(categories[i]);
 					where += " type = ?";
 				} else {
 					where += " type is null";
 				}
 			}
 			if (where.length() > 0) {
 				where = "(" + where + ")";
 			}
 			List<String> parameters = nonNullCategories;
 
 			if (text != null) {
 				where += "AND ( events MATCH ? ) AND fromTime > " + getCurrentDateTime();
 				parameters.add(text);
 			}
 			return getInstance().storage.query(EventObject.class, where, parameters.toArray(new String[parameters.size()]), position, size,
 					"fromTime ASC");
 		} else {
 			System.err.println("READING REMOTELY");
 			ArrayList<EventObject> result = new ArrayList<EventObject>();
 			for (String category : categories) {
 				ObjectFilter filter = new ObjectFilter();
 				filter.setType(category);
 				filter.setSkip(position);
 				filter.setLimit(size);
 				result.addAll(getRemote(instance.mContext, getAuthToken()).searchObjects(filter, EventObject.class));
 			}
 			return result;
 		}
 	}
 
 	private static long getCurrentDateTime() {
 		Calendar c = Calendar.getInstance();
 		c.set(Calendar.HOUR_OF_DAY, 23);
 		c.set(Calendar.MINUTE, 59);
 		c.add(Calendar.DATE, -1);
 		return c.getTimeInMillis();
 	}
 
 	public static Collection<StoryObject> searchStoriesByCategory(int position, int size, String text, String... inCategories)
 			throws DataException, StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
 
 		if (inCategories == null || inCategories.length == 0)
 			return Collections.emptyList();
 		
 		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));
 
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			List<String> nonNullCategories = new ArrayList<String>();
 			String where = "";
 			for (int i = 0; i < categories.length; i++) {
 				if (where.length() > 0)
 					where += " or ";
 				if (categories[i] != null) {
 					nonNullCategories.add(categories[i]);
 					where += " type = ?";
 				} else {
 					where += " type is null";
 				}
 			}
 			if (where.length() > 0) {
 				where = "(" + where + ")";
 			}
 			List<String> parameters = nonNullCategories;
 
 			if (text != null) {
 				where += "and ( stories MATCH ? )";
 				parameters.add(text);
 			}
 			return getInstance().storage.query(StoryObject.class, where, parameters.toArray(new String[parameters.size()]), position, size,
 					"title ASC");
 		} else {
 			ArrayList<StoryObject> result = new ArrayList<StoryObject>();
 			for (String category : categories) {
 				ObjectFilter filter = new ObjectFilter();
 				filter.setType(category);
 				filter.setSkip(position);
 				filter.setLimit(size);
 				result.addAll(getRemote(instance.mContext, getAuthToken()).searchObjects(filter, StoryObject.class));
 			}
 			return result;
 		}
 	}
 
 	public static Collection<EventObject> getEventsByCategories(int position, int size, String... inCategories) throws DataException,
 			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
 		
 		if (inCategories == null || inCategories.length == 0)
 			return Collections.emptyList();
 		
 		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));
 
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			List<String> nonNullCategories = new ArrayList<String>();
 			String where = "";
 			for (int i = 0; i < categories.length; i++) {
 				if (where.length() > 0)
 					where += " or ";
 				if (categories[i] != null) {
 					nonNullCategories.add(categories[i]);
 					where += " type = ?";
 				} else {
 					where += " type is null";
 				}
 			}
 			if (where.length() > 0) {
 				where = "(" + where + ")";
 			}
 			where += "AND fromTime > " + getCurrentDateTime();
 			return getInstance().storage.query(EventObject.class, where, nonNullCategories.toArray(new String[nonNullCategories.size()]),
 					position, size, "fromTime ASC");
 		} else {
 			ArrayList<EventObject> result = new ArrayList<EventObject>();
 			for (String category : categories) {
 				ObjectFilter filter = new ObjectFilter();
 				filter.setType(category);
 				filter.setSkip(position);
 				filter.setLimit(size);
 				result.addAll(getRemote(instance.mContext, getAuthToken()).searchObjects(filter, EventObject.class));
 			}
 			return result;
 		}
 	}
 
 	public static Collection<EventObject> searchMyEvents(int position, int size, String text) throws DataException,
 			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			if (text == null || text.trim().length() == 0) {
 				return getInstance().storage.getObjects(EventObject.class);
 			}
 			return getInstance().storage.query(EventObject.class, "events MATCH ? AND attending IS NOT NULL ",
 					new String[] { text }, position, size, "fromTime ASC");
 		} else {
 			ObjectFilter filter = new ObjectFilter();
 			Map<String, Object> criteria = new HashMap<String, Object>(1);
 			criteria.put("text", text);
 			filter.setCriteria(criteria);
 			filter.setSkip(position);
 			filter.setLimit(size);
 			return getRemote(instance.mContext, getAuthToken()).searchObjects(filter, EventObject.class);
 		}
 	}
 	
 	
 	
 	public static Collection<EventObject> searchEvents(int position, int size, String text) throws DataException,
 			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			if (text == null || text.trim().length() == 0) {
 				return getInstance().storage.getObjects(EventObject.class);
 			}
 			return getInstance().storage.query(EventObject.class, "events MATCH ? AND fromTime > " + getCurrentDateTime(),
 					new String[] { text }, position, size, "fromTime ASC");
 		} else {
 			ObjectFilter filter = new ObjectFilter();
 			Map<String, Object> criteria = new HashMap<String, Object>(1);
 			criteria.put("text", text);
 			filter.setCriteria(criteria);
 			filter.setSkip(position);
 			filter.setLimit(size);
 			return getRemote(instance.mContext, getAuthToken()).searchObjects(filter, EventObject.class);
 		}
 	}
 	
 	public static Collection<EventObject> searchTodayEvents(int position, int size, String text) throws DataException,
 	StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
 		//Date now = new Date();  
 		Calendar cal = Calendar.getInstance();  
 		//cal.setTime(now);
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 
 		cal.add(Calendar.DAY_OF_YEAR, 1);   
 		Date tomorrow = cal.getTime(); 
 		
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			return getInstance().storage.query(
 					EventObject.class,
 					" fromTime > "
 							+ getCurrentDateTime() +" AND fromTime < " + tomorrow.getTime(),
 					null , position, size, "fromTime ASC");
 		} else {
 			ObjectFilter filter = new ObjectFilter();
 			Map<String, Object> criteria = new HashMap<String, Object>(1);
 			criteria.put("text", text);
 			filter.setCriteria(criteria);
 			filter.setSkip(position);
 			filter.setLimit(size);
 			return getRemote(instance.mContext, getAuthToken()).searchObjects(
 					filter, EventObject.class);
 		}
 	}
 
 	public static Collection<EventObject> getEventsByPOI(int position, int size, String poiId) throws DataException,
 			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			return getInstance().storage.query(EventObject.class, "poiId = ? AND fromTime > " + getCurrentDateTime(),
 					new String[] { poiId }, position, size, "fromTime ASC");
 		} else {
 			ObjectFilter filter = new ObjectFilter();
 			Map<String, Object> criteria = new HashMap<String, Object>(1);
 			criteria.put("poiId", poiId);
 			filter.setCriteria(criteria);
 			filter.setSkip(position);
 			filter.setLimit(size);
 			return getRemote(instance.mContext, getAuthToken()).searchObjects(filter, EventObject.class);
 		}
 	}
 
 	public static Collection<EventObject> getMyEvents(int position, int size) throws DataException, StorageConfigurationException,
 			ConnectionException, ProtocolException, SecurityException {
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			return getInstance().storage.query(EventObject.class, "attending IS NOT NULL", null, position, size, "fromTime DESC");
 		} else {
 			ObjectFilter filter = new ObjectFilter();
 			filter.setMyObjects(true);
 			filter.setSkip(position);
 			filter.setLimit(size);
 			return getRemote(instance.mContext, getAuthToken()).searchObjects(filter, EventObject.class);
 		}
 	}
 
 	public static List<SemanticSuggestion> getSuggestions(CharSequence suggest) throws ConnectionException, ProtocolException,
 			SecurityException, DataException {
 		return SuggestionHelper.getSuggestions(suggest, getInstance().mContext, GlobalConfig.getAppUrl(getInstance().mContext), getAuthToken(), Constants.APP_TOKEN);
 	}
 
 	private static RemoteStorage getRemote(Context mContext, String token) throws ProtocolException, DataException {
 		if (remoteStorage == null) {
 			remoteStorage = new RemoteStorage(mContext, Constants.APP_TOKEN);
 		}
 		remoteStorage.setConfig(token, GlobalConfig.getAppUrl(getInstance().mContext), Constants.SERVICE);
 		return remoteStorage;
 	}
 
 	public static void endAppFailure(Activity activity, int id) {
 		Toast.makeText(activity, activity.getResources().getString(id), Toast.LENGTH_LONG).show();
 		activity.finish();
 	}
 
 	public static void showFailure(Activity activity, int id) {
 		Toast.makeText(activity, activity.getResources().getString(id), Toast.LENGTH_LONG).show();
 	}
 
 	public static boolean deleteEvent(EventObject eventObject) throws DataException, ConnectionException, ProtocolException,
 			SecurityException, RemoteException, StorageConfigurationException {
 		if (eventObject.createdByUser()) {
 			getRemote(instance.mContext, getAuthToken()).delete(eventObject.getId(), UserEventObject.class);
 			synchronize();
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean deletePOI(POIObject poiObject) throws DataException, ConnectionException, ProtocolException, SecurityException,
 			RemoteException, StorageConfigurationException {
 		if (poiObject.createdByUser()) {
 			getRemote(instance.mContext, getAuthToken()).delete(poiObject.getId(), UserPOIObject.class);
 			synchronize();
 			return true;
 		}
 		return false;
 	}
 
 	public static int rate(BaseDTObject event, int rating) throws ConnectionException, ProtocolException, SecurityException, DataException,
 			RemoteException, StorageConfigurationException {
 		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), Constants.SERVICE + "/objects/" + event.getId() + "/rate");
 		request.setMethod(Method.PUT);
 		String query = "rating=" + rating;
 		request.setQuery(query);
 		String response = getInstance().mProtocolCarrier.invokeSync(request, Constants.APP_TOKEN, getAuthToken()).getBody();
 		synchronize();
 		return Integer.parseInt(response);
 	}
 
 	public static EventObject attend(BaseDTObject event) throws ConnectionException, ProtocolException, SecurityException, DataException,
 			RemoteException, StorageConfigurationException {
 		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), Constants.SERVICE + "/objects/" + event.getId() + "/attend");
 		request.setMethod(Method.PUT);
 		String response = getInstance().mProtocolCarrier.invokeSync(request, Constants.APP_TOKEN, getAuthToken()).getBody();
 		synchronize();
 		EventObject result = eu.trentorise.smartcampus.android.common.Utils.convertJSONToObject(response, EventObject.class);
 		return result;
 	}
 
 	public static EventObject notAttend(BaseDTObject event) throws ConnectionException, ProtocolException, SecurityException,
 			DataException, RemoteException, StorageConfigurationException {
 		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), Constants.SERVICE + "/objects/" + event.getId() + "/notAttend");
 		request.setMethod(Method.PUT);
 		String response = getInstance().mProtocolCarrier.invokeSync(request, Constants.APP_TOKEN, getAuthToken()).getBody();
 		synchronize();
 		EventObject result = eu.trentorise.smartcampus.android.common.Utils.convertJSONToObject(response, EventObject.class);
 		return result;
 	}
 
 	public static BaseDTObject findEventByEntityId(Long entityId) throws DataException, StorageConfigurationException, ConnectionException,
 			ProtocolException, SecurityException {
 		return findDTObjectByEntityId(EventObject.class, entityId);
 	}
 
 	public static BaseDTObject findPOIByEntityId(Long entityId) throws DataException, StorageConfigurationException, ConnectionException,
 			ProtocolException, SecurityException {
 		return findDTObjectByEntityId(POIObject.class, entityId);
 	}
 
 	private static BaseDTObject findDTObjectByEntityId(Class<? extends BaseDTObject> cls, Long entityId) throws DataException,
 			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			String where = "entityId = " + entityId;
 			Collection<? extends BaseDTObject> coll = getInstance().storage.query(cls, where, null);
 			if (coll != null && coll.size() == 1)
 				return coll.iterator().next();
 		}
 
 		ObjectFilter filter = new ObjectFilter();
 		Map<String, Object> criteria = new HashMap<String, Object>();
 		criteria.put("entityId", entityId);
 		filter.setCriteria(criteria);
 		Collection<? extends BaseDTObject> coll = getRemote(instance.mContext, getAuthToken()).searchObjects(filter, cls);
 		if (coll != null && coll.size() == 1)
 			return coll.iterator().next();
 		return null;
 
 	}
 
 	public static Boolean saveStory(StoryObject storyObject) throws RemoteException, DataException, StorageConfigurationException,
 			ConnectionException, ProtocolException, SecurityException {
 		String requestService = null;
 		Method method = null;
 		Boolean result = null;
 		if (storyObject.getId() == null) {
 			// create
 			requestService = Constants.SERVICE + "/eu.trentorise.smartcampus.dt.model.UserStoryObject";
 			method = Method.POST;
 			result = true;
 		} else {
 			// update
 			requestService = Constants.SERVICE + "/eu.trentorise.smartcampus.dt.model.UserStoryObject/" + storyObject.getId();
 			method = Method.PUT;
 			result = false;
 		}
 		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), requestService);
 		request.setMethod(method);
 		String json = eu.trentorise.smartcampus.android.common.Utils.convertToJSON(storyObject);
 		request.setBody(json);
 
 		getInstance().mProtocolCarrier.invokeSync(request, Constants.APP_TOKEN, getAuthToken());
 		// getRemote(instance.mContext, instance.token).create(poi);
 		synchronize();
 		return result;
 	}
 
 	public static Collection<StoryObject> getStoryByCategory(int position, int size, String... inCategories) throws DataException,
 			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
 
 		if (inCategories == null || inCategories.length == 0)
 			return Collections.emptyList();
 		
 		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));
 
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			List<String> nonNullCategories = new ArrayList<String>();
 			String where = "";
 			for (int i = 0; i < categories.length; i++) {
 				if (where.length() > 0)
 					where += " or ";
 				if (categories[i] != null) {
 					nonNullCategories.add(categories[i]);
 					where += " type = ?";
 				} else {
 					where += " type is null";
 				}
 			}
 			if (where.length() > 0) {
 				where = "(" + where + ")";
 			}
 			return getInstance().storage.query(StoryObject.class, where, nonNullCategories.toArray(new String[nonNullCategories.size()]),
 					position, size, "title ASC");
 		} else {
 			ArrayList<StoryObject> result = new ArrayList<StoryObject>();
 			for (String category : categories) {
 				ObjectFilter filter = new ObjectFilter();
 				filter.setType(category);
 				filter.setSkip(position);
 				filter.setLimit(size);
 				result.addAll(getRemote(instance.mContext, getAuthToken()).searchObjects(filter, StoryObject.class));
 			}
 			return result;
 		}
 	}
 
 	public static Collection<StoryObject> searchStories(int position, int size, String text) throws DataException,
 			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
 
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			if (text == null || text.trim().length() == 0) {
 				return getInstance().storage.getObjects(StoryObject.class);
 			}
 			return getInstance().storage.query(StoryObject.class, "stories MATCH ? ", new String[] { text }, position, size, "title ASC");
 		} else {
 			ObjectFilter filter = new ObjectFilter();
 			Map<String, Object> criteria = new HashMap<String, Object>(1);
 			criteria.put("text", text);
 			filter.setCriteria(criteria);
 			filter.setSkip(position);
 			filter.setLimit(size);
 			return getRemote(instance.mContext, getAuthToken()).searchObjects(filter, StoryObject.class);
 		}
 	}
 
 	public static Boolean deleteStory(StoryObject storyObject) throws DataException, ConnectionException, ProtocolException,
 			SecurityException, RemoteException, StorageConfigurationException {
 		getRemote(instance.mContext, getAuthToken()).delete(storyObject.getId(), UserStoryObject.class);
 		synchronize();
 		return true;
 	}
 
 	public static BaseDTObject findStoryByEntityId(Long storyId) throws DataException, StorageConfigurationException, ConnectionException,
 			ProtocolException, SecurityException {
 		return findDTObjectByEntityId(StoryObject.class, storyId);
 	}
 
 	public static ArrayList<POIObject> getPOIBySteps(List<StepObject> steps) throws DataException, StorageConfigurationException,
 			ConnectionException, ProtocolException, SecurityException {
 
 		// usare findpoibyid nella lista steps
 
 		ArrayList<POIObject> poiList = new ArrayList<POIObject>();
 		for (StepObject step : steps) {
 			POIObject poiStep = findPOIById(step.getPoiId());
 			poiList.add(poiStep);
 		}
 		return poiList;
 
 	}
 
 	public static StoryObject addToMyStories(BaseDTObject story) throws ConnectionException, ProtocolException, SecurityException,
 			DataException, RemoteException, StorageConfigurationException {
 		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), Constants.SERVICE + "/objects/" + story.getId() + "/attend");
 		request.setMethod(Method.PUT);
 		String response = getInstance().mProtocolCarrier.invokeSync(request, Constants.APP_TOKEN, getAuthToken()).getBody();
 		synchronize();
 		StoryObject result = eu.trentorise.smartcampus.android.common.Utils.convertJSONToObject(response, StoryObject.class);
 		return result;
 	}
 
 	public static StoryObject removeFromMyStories(BaseDTObject story) throws ConnectionException, ProtocolException, SecurityException,
 			DataException, RemoteException, StorageConfigurationException {
 		MessageRequest request = new MessageRequest(GlobalConfig.getAppUrl(getInstance().mContext), Constants.SERVICE + "/objects/" + story.getId() + "/notAttend");
 		request.setMethod(Method.PUT);
 		String response = getInstance().mProtocolCarrier.invokeSync(request, Constants.APP_TOKEN, getAuthToken()).getBody();
 		synchronize();
 		StoryObject result = eu.trentorise.smartcampus.android.common.Utils.convertJSONToObject(response, StoryObject.class);
 		return result;
 	}
 
 	public static Collection<StoryObject> getMyStories(int position, int size) throws DataException, StorageConfigurationException,
 			ConnectionException, ProtocolException, SecurityException {
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			return getInstance().storage.query(StoryObject.class, "attending IS NOT NULL", null, position, size, "title ASC");
 		} else {
 			ObjectFilter filter = new ObjectFilter();
 			filter.setMyObjects(true);
 			filter.setSkip(position);
 			filter.setLimit(size);
 			return getRemote(instance.mContext, getAuthToken()).searchObjects(filter, StoryObject.class);
 		}
 	}
 
 	public static Collection<StoryObject> searchMyStories(int position, int size, String text) throws DataException,
 			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
 		if (Utils.getObjectVersion(instance.mContext, Constants.APP_TOKEN) > 0) {
 			if (text == null || text.trim().length() == 0) {
 				return getInstance().storage.getObjects(StoryObject.class);
 			}
 			return getInstance().storage.query(StoryObject.class, "stories MATCH ? AND attending IS NOT NULL ",
 					new String[] { text }, position, size, "title ASC");
 		} else {
 			ObjectFilter filter = new ObjectFilter();
 			Map<String, Object> criteria = new HashMap<String, Object>(1);
 			criteria.put("text", text);
 			filter.setCriteria(criteria);
 			filter.setSkip(position);
 			filter.setLimit(size);
 			return getRemote(instance.mContext, getAuthToken()).searchObjects(filter, StoryObject.class);
 		}
 	}
 	public static LocationHelper getLocationHelper() {
 		return mLocationHelper;
 	}
 
 	public static void setLocationHelper(LocationHelper mLocationHelper) {
 		DTHelper.mLocationHelper = mLocationHelper;
 	}
 
 	public class DTLocationListener implements LocationListener {
 		@Override
 		public void onLocationChanged(Location location) {
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 		}
 	}
 
 	public static SyncStorage getSyncStorage() throws DataException {
 		return getInstance().storage;
 	}
 
 	
 	
 	public static boolean isOwnedObject(BaseDTObject obj) {
 		if (obj.getId() == null) return true;
 		UserData p=null;
 		try {
 			p = accessProvider.readUserData(getInstance().mContext, null);
 		} catch (DataException e) {
 
 		}
 		if (p != null) return p.getUserId().equals(obj.getCreatorId());
 		return false;
 	}
 }
