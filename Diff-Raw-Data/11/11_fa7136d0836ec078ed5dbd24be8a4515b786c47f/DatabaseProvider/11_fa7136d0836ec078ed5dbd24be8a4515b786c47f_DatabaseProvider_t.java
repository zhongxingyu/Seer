 /*
  * Copyright 2012 the original author or authors.
  * Copyright 2012 SorcerSoft.org.
  * Copyright 2013 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package sorcer.core.provider.dbp;
 
 import java.io.File;
 import java.io.InvalidObjectException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.jini.id.Uuid;
 import net.jini.id.UuidFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import sorcer.config.ConfigEntry;
 import sorcer.core.provider.StorageManagement;
 import sorcer.service.Context;
 import sorcer.service.ContextException;
 import sorcer.service.DatabaseStorer;
 import sorcer.service.Exertion;
 import sorcer.service.Identifiable;
 import sorcer.util.bdb.objects.SorcerDatabase;
 import sorcer.util.bdb.objects.SorcerDatabaseViews;
 import sorcer.util.bdb.objects.Store;
 import sorcer.util.bdb.objects.UuidKey;
 import sorcer.util.bdb.objects.UuidObject;
 import sorcer.util.bdb.sdb.SdbUtil;
 
 import com.sleepycat.collections.StoredMap;
 import com.sleepycat.collections.StoredValueSet;
 import com.sleepycat.je.DatabaseException;
 
 import static sorcer.core.SorcerConstants.P_PROVIDER_NAME;
 
 @SuppressWarnings({ "rawtypes", "unchecked" })
 public class DatabaseProvider implements DatabaseStorer, IDatabaseProvider {
     private static final Logger logger = LoggerFactory.getLogger(DatabaseProvider.class);
 
 	private SorcerDatabase db;
 
 	private SorcerDatabaseViews views;
 
     @ConfigEntry(P_PROVIDER_NAME)
     private String providerName;
 
 	public DatabaseProvider() {
         super();
 	}
 
 	public Uuid store(Object object) {
 		Object obj = object;
 		if (!(object instanceof Identifiable)) {
 			obj = new UuidObject(object);
 		}
 		PersistThread pt = new PersistThread(obj);
 		pt.start();
 		return pt.getUuid();	
 	}
 	
 	public Uuid update(Uuid uuid, Object object) throws InvalidObjectException {
 		Object uuidObject = object;
 		if (!(object instanceof Identifiable)) {
 			uuidObject = new UuidObject(uuid, object);
 		}
 			UpdateThread ut = new UpdateThread(uuid, uuidObject);
 			ut.start();
 			return ut.getUuid();
 	}
 	
 	public Uuid update(URL url, Object object) throws InvalidObjectException {
 		Object uuidObject = object;
 		if (!(object instanceof Identifiable)) {
 			uuidObject = new UuidObject(SdbUtil.getUuid(url), object);
 		}
 		UpdateThread ut = new UpdateThread(url, uuidObject);
 		ut.start();
 		return ut.getUuid();
 	}
 
 	public Object getObject(Uuid uuid) {
 		StoredMap<UuidKey, UuidObject> uuidObjectMap = views.getUuidObjectMap();
 		UuidObject uuidObj = uuidObjectMap.get(new UuidKey(uuid));
 		return uuidObj.getObject();
 	}
 	
 	public Context getContext(Uuid uuid) {
 		StoredMap<UuidKey, Context> cxtMap = views.getContextMap();
 		return cxtMap.get(new UuidKey(uuid));
 	}
 	
 	public Exertion getExertion(Uuid uuid) {
 		StoredMap<UuidKey, Exertion> xrtMap = views.getExertionMap();
 		return xrtMap.get(new UuidKey(uuid));
 	}
 
 	protected class PersistThread extends Thread {
 
 		Object object;
 		Uuid uuid;
 
 		public PersistThread(Object object) {
 			this.object = object;
 			this.uuid = (Uuid)((Identifiable)object).getId();
 		}
 
 		@SuppressWarnings("unchecked")
 		public void run() {
 			StoredValueSet storedSet = null;
 			if (object instanceof Context) {
 				storedSet = views.getContextSet();
 				storedSet.add(object);
 			} else if (object instanceof Exertion) {
 				storedSet = views.getExertionSet();
 				storedSet.add(object);
 			} else if (object instanceof UuidObject) {
 				storedSet = views.getUuidObjectSet();
 				storedSet.add(object);
 			}
 		}
 		
 		public Uuid getUuid() {
 			return uuid;
 		}
 	}
 
 	protected class UpdateThread extends Thread {
 
 		Object object;
 		Uuid uuid;
 
 		public UpdateThread(Uuid uuid, Object object) throws InvalidObjectException {
 			this.uuid = uuid;
 			this.object = object;
 		}
 
 		public UpdateThread(URL url, Object object) throws InvalidObjectException {
 			this.object = object;
 			this.uuid = SdbUtil.getUuid(url);
 		}
 		
 		public void run() {
 			StoredMap storedMap = null;
 			if (object instanceof Context) {
 				storedMap = views.getContextMap();
 				storedMap.replace(new UuidKey(uuid), object);
 			} else if (object instanceof Exertion) {
 				storedMap = views.getExertionMap();
 				storedMap.replace(new UuidKey(uuid), object);
 			} else if (object instanceof Object) {
 				storedMap = views.getUuidObjectMap();
 				storedMap.replace(new UuidKey(uuid), object);
 			}
 		}
 		
 		public Uuid getUuid() {
 			return uuid;
 		}
 	}
 	
 	protected class DeleteThread extends Thread {
 
 		Uuid uuid;
 		Store storeType;
 		
 		public DeleteThread(Uuid uuid, Store storeType) {
 			this.uuid = uuid;
 			this.storeType = storeType;
 		}
 
 		public void run() {
 			StoredMap storedMap = getStoredMap(storeType);
 			storedMap.remove(new UuidKey(uuid));
 		}
 		
 		public Uuid getUuid() {
 			return uuid;
 		}
 	}
 	
 	public Context contextStore(Context context) throws RemoteException,
 			ContextException, MalformedURLException {
 		Object object = context.getValue(object_stored);		
 		Uuid uuid = store(object);
 		Store type = getStoreType(object);
 		URL sdbUrl = getDatabaseURL(type, uuid);
 		if (context.getReturnPath() != null)
 			context.putOutValue(context.getReturnPath().path, sdbUrl);
 
 		context.putOutValue(object_url, sdbUrl);
 		context.putOutValue(store_size, getStoreSize(type));
 		
 		return context;
 	}
 
 	public URL getDatabaseURL(Store storeType, Uuid uuid) throws MalformedURLException {
 		String pn = providerName;
 		if (pn == null || pn.length() == 0 || pn.equals("*"))
 			pn = "";
 		else
 			pn = "/" + pn;
 		return new URL("sos://" + IDatabaseProvider.class.getName() + pn + "#"
 				+ storeType + "=" + uuid);
 	}
 	
 	public URL getSdbUrl() throws MalformedURLException, RemoteException {
 		String pn = providerName;
 		if (pn == null || pn.length() == 0 || pn.equals("*"))
 			pn = "";
 		else
 			pn = "/" + pn;
 		return new URL("sos://" + IDatabaseProvider.class.getName() + pn);
 	}
 	
 	public int size(Store storeType) {
 		StoredValueSet storedSet = getStoredSet(storeType);
 		return storedSet.size();
 	}
 	
 	public Uuid deleteURL(URL url) {
 		Store storeType = SdbUtil.getStoreType(url);
 		Uuid id = SdbUtil.getUuid(url);
 		DeleteThread dt = new DeleteThread(id, storeType);
 		dt.start();
 		id = dt.getUuid();
 		return id;
 	}
 
 	public Object retrieve(URL url) {
 		Store storeType = SdbUtil.getStoreType(url);
 		Uuid uuid = SdbUtil.getUuid(url);
 		return retrieve(uuid, storeType);
 	}
 	
 	public Object retrieve(Uuid uuid, Store storeType) {
 		Object obj = null;
 		if (storeType == Store.context)
 			obj = getContext(uuid);
 		else if (storeType == Store.exertion)
 			obj = getExertion(uuid);
 		else if (storeType == Store.object)
 			obj = getObject(uuid);
 		
 		return obj;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see sorcer.core.StorageMangement#rertieve(sorcer.service.Context)
 	 */
 	@Override
 	public Context contextRetrieve(Context context) throws RemoteException,
 			ContextException {
 		Store storeType = (Store) context.getValue(object_type);
 		Uuid uuid = null;
 		Object id = context.getValue(object_uuid);
 			if (id instanceof String) {
 				uuid = UuidFactory.create((String)id);
 			} else if (id instanceof Uuid) {
 				uuid = (Uuid)id;
 			} else {
 				throw new ContextException("No valid stored object Uuid: " + id);
 			}
 				
 		Object obj = retrieve(uuid, storeType);
 		if (context.getReturnPath() != null)
 			context.putOutValue(context.getReturnPath().path, obj);
 		
 		// default returned path
 		context.putOutValue(object_retrieved, obj);
 		return context;
 	}
 
 	/* (non-Javadoc)
 	 * @see sorcer.core.StorageManagement#update(sorcer.service.Context)
 	 */
 	@Override
 	public Context contextUpdate(Context context) throws RemoteException,
 			ContextException, MalformedURLException, InvalidObjectException {
 		Object object = context.getValue(object_updated);
 		Object id = context.getValue(object_uuid);
 		Uuid uuid = null;
 		if (id instanceof String) {
 			uuid = UuidFactory.create((String)id);
 		} else if (id instanceof Uuid) {
 			uuid = (Uuid)id;
 		} else {
 			throw new ContextException("Wrong update object Uuid: " + id);
 		}
 		uuid = update(uuid, object);
 		Store type = getStoreType(object);
 		URL sdbUrl = getDatabaseURL(type, uuid);
 		if (context.getReturnPath() != null)
 			context.putOutValue(context.getReturnPath().path, sdbUrl);
 
 		context.putOutValue(object_url, sdbUrl);
 		context.remove(object_updated);
 		context.putOutValue(store_size, getStoreSize(type));
 		return context;
 	}
 	
 	/* (non-Javadoc)
 	 * @see sorcer.core.StorageManagement#contextList(sorcer.service.Context)
 	 */
 	@Override
 	public Context contextList(Context context) throws RemoteException,
 			ContextException, MalformedURLException {
 		List<String> content = list((Store) context.getValue(StorageManagement.store_type));
 		context.putValue(StorageManagement.store_content_list, content);
 		return context;
 	}
 	
 	public List<String> list(Store storeType) {
 		StoredValueSet storedSet = getStoredSet(storeType);
 		List<String> contents = new ArrayList<String>(storedSet.size());
 		Iterator it = storedSet.iterator();
 		while(it.hasNext()) {
 			contents.add(it.next().toString());
 		}
 		return contents;
 	}
 	
 	public List<String> list(URL url) {
 		return list(SdbUtil.getStoreType(url));
 	}
 
 	/* (non-Javadoc)
 	 * @see sorcer.core.StorageManagement#contextClear(sorcer.service.Context)
 	 */
 	@Override
 	public Context contextClear(Context context) throws RemoteException,
 			ContextException, MalformedURLException {
 		Store type = (Store)context.getValue(StorageManagement.store_type);
 		context.putValue(store_size, clear(type));
 		return context;
 	}
 	
 	public int clear(Store type) throws RemoteException,
 			ContextException, MalformedURLException {
 		StoredValueSet storedSet = getStoredSet(type);
 		int size = storedSet.size();
 		storedSet.clear();
 		return size;
 	}
 
     @ConfigEntry("dbHome")
     private String dbHome;
 
 	protected void setupDatabase() throws DatabaseException, RemoteException {
 		logger.info("dbHome: " + dbHome);
 		if (dbHome == null || dbHome.length() == 0) {
 			logger.info("No provider's database created");
 			destroy();
 			return;
 		}
 
 		File dbHomeFile = null;
 		dbHomeFile = new File(dbHome);
 		if (!dbHomeFile.isDirectory() && !dbHomeFile.exists()) {
 			boolean done = dbHomeFile.mkdirs();
 			if (!done) {
 				logger.error("Not able to create session database home: {}",
                         dbHomeFile.getAbsolutePath());
 				destroy();
 				return;
 			}
 		}
 		logger.info("Opening provider's BDBJE in: {}"
 				, dbHomeFile.getAbsolutePath());
 		db = new SorcerDatabase(dbHome);
 		views = new SorcerDatabaseViews(db);
 	}
 	
 	/**
 	 * Destroy the service, if possible, including its persistent storage.
 	 * 
 	 * @see sorcer.core.provider.Provider#destroy()
 	 */
 	public void destroy() throws RemoteException {
 		try {
 			if (db != null) {
 				db.close();
 			}
 		} catch (DatabaseException e) {
 			logger.error("Failed to close provider's database",
                     e.getMessage());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see sorcer.core.provider.StorageManagement#delete(sorcer.service.Context)
 	 */
 	@Override
 	public Context contextDelete(Context context) throws RemoteException,
 			ContextException, MalformedURLException {
 		Object deletedObject = context
 				.getValue(StorageManagement.object_deleted);
 		if (deletedObject instanceof URL) {
 			context.putValue(StorageManagement.object_url, (URL)deletedObject);
 		} else {
 			Uuid id = (Uuid) ((Identifiable) deletedObject).getId();
 			context.putValue(StorageManagement.object_url,
 					getDatabaseURL(getStoreType(deletedObject), id));
 		}
 		delete(deletedObject);
 		return context;
 	}
 	
 	public StoredMap getStoredMap(Store storeType) {
 		StoredMap storedMap = null;
 		if (storeType == Store.context) {
 			storedMap = views.getContextMap();
 		} else if (storeType == Store.exertion) {
 			storedMap = views.getExertionMap();
 		} else if (storeType == Store.object) {
 			storedMap = views.getUuidObjectMap();
 		}
 		return storedMap;
 	}
 	
 	public StoredValueSet getStoredSet(Store storeType) {
 		StoredValueSet storedSet = null;
 		if (storeType == Store.context) {
 			storedSet = views.getContextSet();
 		} else if (storeType == Store.exertion) {
 			storedSet = views.getExertionSet();
 		} else if (storeType == Store.object) {
 			storedSet = views.getUuidObjectSet();
 		}
 		return storedSet;
 	}
 	
 	public Uuid delete(Object object) {
 		if (object instanceof URL) {
 			return deleteURL((URL)object);
 		} else if (object instanceof Identifiable) 
 			return deleteIdentifiable(object);
 		return null;
 	}
 	
 	public Uuid deleteIdentifiable(Object object) {
 		Uuid id = (Uuid) ((Identifiable) object).getId();
 		DeleteThread dt = null;
 		if (object instanceof Context) {
 			dt = new DeleteThread(id, Store.context);
 		} else if (object instanceof Exertion) {
 			dt = new DeleteThread(id, Store.exertion);
 		} else {
 			dt = new DeleteThread(id, Store.object);			
 		}
 		dt.start();
 		id = dt.getUuid();
 		return id;
 	}
 	
 	private int getStoreSize(Store type) {
 		if (type == Store.context) {
 			return views.getContextSet().size();
 		} else if (type == Store.exertion) {
 			return views.getExertionSet().size();
 		} else {
 			return views.getUuidObjectSet().size();
 		}
 	}
 	
 	private Store getStoreType(Object object) {
 		Store type = Store.object;
 		if (object instanceof Context) {
 			type = Store.context;
 		} else if (object instanceof Exertion) {
 			type = Store.exertion;
 		} 
 		return type;
 	}
 
 	/* (non-Javadoc)
 	 * @see sorcer.core.provider.StorageManagement#contextSize(sorcer.service.Context)
 	 */
 	@Override
 	public Context contextSize(Context context) throws RemoteException,
 			ContextException, MalformedURLException {
 		Store type = (Store)context.getValue(StorageManagement.store_type);
 		if (context.getReturnPath() != null)
 			context.putOutValue(context.getReturnPath().path, getStoreSize(type));
 		context.putOutValue(store_size, getStoreSize(type));
 		return context;
 	}
 
 	/* (non-Javadoc)
 	 * @see sorcer.core.provider.StorageManagement#contextRecords(sorcer.service.Context)
 	 */
 	@Override
 	public Context contextRecords(Context context) throws RemoteException,
 			ContextException, MalformedURLException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
     @Override
     public URL storeObject(Object object) {
         Uuid uuid = store(object);
         Store type = getStoreType(object);
         try {
             return getDatabaseURL(type, uuid);
         } catch (MalformedURLException e) {
            throw new IllegalStateException("Couldn't parse my own URL", e);
         }
     }
 
     /**
      * the same as #update() but hide requirement on Uuid class
      */
     @Override
     public void updateObject(URL url, Object object) throws InvalidObjectException {
         update(url, object);
     }
 
     @Override
     public void deleteObject(URL url) {
         deleteURL(url);
     }
 }
