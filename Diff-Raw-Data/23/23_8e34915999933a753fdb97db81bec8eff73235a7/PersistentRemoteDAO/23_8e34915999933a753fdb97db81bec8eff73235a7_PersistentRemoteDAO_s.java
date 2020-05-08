 package com.szas.server;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import com.szas.sync.ContentObserverProviderImpl;
 import com.szas.sync.Tuple;
 import com.szas.sync.WrongObjectThrowable;
 import com.szas.sync.local.LocalTuple;
 import com.szas.sync.remote.RemoteDAO;
 import com.szas.sync.remote.RemoteTuple;
 
 public class PersistentRemoteDAO<T extends Tuple> extends ContentObserverProviderImpl implements RemoteDAO<T> {
 
 	private static final Logger log =
 		Logger.getLogger(PersistentRemoteDAO.class.getName());
 
 	private static final long serialVersionUID = 1L;
 
 	private Class<T> tupleClass;
 
 	public PersistentRemoteDAO(Class<T> tupleClass) {
 		this.tupleClass = tupleClass;
 	}
 
 	@Override
 	public long getTimestamp() {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			Query query = pm.newQuery(PersistentRemoteTuple.class);
 			query.setFilter("className == currentClassName");
 			query.setOrdering("timestamp desc");
 			query.setRange(0,1);
			query.declareParameters("String lastNameParam");
 			@SuppressWarnings("unchecked")
 			List<PersistentRemoteTuple> results =
 				(List<PersistentRemoteTuple>) query.execute(tupleClass.getName());
 			if (results == null || results.isEmpty())
 				return -1;
 			Iterator<PersistentRemoteTuple> iterator = results.iterator();
 			for (;iterator.hasNext();) {
 				PersistentRemoteTuple persistentRemoteTuple =
 					iterator.next();
 				return persistentRemoteTuple.getTimestamp();
 			}
 		} finally {
 			pm.close();
 		}
 		return -1;
 	}
 
 	private long getNextTimestamp() {
 		return getTimestamp() + 1;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public ArrayList<T> getAll() {
 		ArrayList<T> ret = new ArrayList<T>();
 
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			Query query = pm.newQuery(PersistentRemoteTuple.class);
			query.setFilter("className == currentClassName");
			query.setFilter("deleted == false");
			query.declareParameters("String lastNameParam");
 			List<PersistentRemoteTuple> results =
 				(List<PersistentRemoteTuple>) query.execute(tupleClass.getName());
 			if (results == null || results.isEmpty())
 				return ret;
 
 			for (PersistentRemoteTuple remoteTuple : results) {
 				if (remoteTuple.isDeleted())
 					continue;
 				T element = (T) remoteTuple.getElement();
 				ret.add(element);
 			}
 		} finally {
 			pm.close();
 		}
 		return ret;
 	}
 
 	@Override
 	public void insert(T element) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			long nextTimestamp = getNextTimestamp();
 
 			PersistentRemoteTuple remoteTuple = new PersistentRemoteTuple();
 			remoteTuple.setDeleted(false);
 			remoteTuple.setElement(element);
 			remoteTuple.setTimestamp(nextTimestamp);
 			remoteTuple.setInsertionTimestamp(nextTimestamp);
 			remoteTuple.setClassName(tupleClass.getName());
 			remoteTuple.setId(element.getId());
 
 			pm.makePersistent(remoteTuple);
 			notifyContentObservers(false);
 		} finally {
 			pm.close();
 		}
 	}
 
 	@Override
 	public void delete(T element) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			Query query = pm.newQuery(PersistentRemoteTuple.class);
 			query.setFilter("className == currentClassName && id == myId");
			query.declareParameters("String lastNameParam, long myId");
 			@SuppressWarnings("unchecked")
 			List<PersistentRemoteTuple> results =
 				(List<PersistentRemoteTuple>) query.execute(tupleClass.getName(),element.getId());
 
 			if (results == null || results.isEmpty())
 				return;
 			PersistentRemoteTuple remoteTuple = results.get(0);
 			remoteTuple.setTimestamp(getNextTimestamp());
 			remoteTuple.setDeleted(true);
 			notifyContentObservers(false);
 		} finally {
 			pm.close();
 		}
 	}
 
 	@Override
 	public void update(T element) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			Query query = pm.newQuery(PersistentRemoteTuple.class);
 			query.setFilter("className == currentClassName && id == myId");
			query.declareParameters("String lastNameParam, long myId");
 			@SuppressWarnings("unchecked")
 			List<PersistentRemoteTuple> results =
 				(List<PersistentRemoteTuple>) query.execute(tupleClass.getName(),element.getId());
 
 			if (results == null || results.isEmpty())
 				return;
 			PersistentRemoteTuple remoteTuple = results.get(0);
 			remoteTuple.setElement(element);
 			remoteTuple.setTimestamp(getNextTimestamp());
 			notifyContentObservers(false);
 		} finally {
 			pm.close();
 		}
 	}
 
 	private void syncElement(LocalTuple<T> localTuple, long lastTimestamp) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			T localElement = localTuple.getElement();
 
 			if (localTuple.getStatus() ==  LocalTuple.Status.SYNCED) {
 				log.severe("Client should not send synced elements");
 				return;
 			}
 
 			Query query = pm.newQuery(PersistentRemoteTuple.class);
 			query.setFilter("className == currentClassName && id == myId");
			query.declareParameters("String lastNameParam, long myId");
 			query.setRange(0, 1);
 
 			@SuppressWarnings("unchecked")
 			List<PersistentRemoteTuple> results =
 				(List<PersistentRemoteTuple>) query.execute(tupleClass.getName(),localElement.getId());
 
 			if (results != null && results.size() > 0) {
 				// not found
 				PersistentRemoteTuple remoteTuple = results.get(0);
 				if (remoteTuple.getTimestamp() > lastTimestamp) {
 					log.warning("Item changed on server");
 					return;
 				}
 				if (remoteTuple.isDeleted()) {
 					log.severe("Item already deleted on server");
 					return;
 				}
 				if (localTuple.getStatus() ==  LocalTuple.Status.INSERTING) {
 					log.warning("Already inserted");
 					return;
 				}
 				log.info("Updating/Deleting element");
 				remoteTuple.setDeleted(localTuple.getStatus() == LocalTuple.Status.DELETING);
 				remoteTuple.setElement(localElement);
 				remoteTuple.setTimestamp(getNextTimestamp());
 			} else {
 				if (localTuple.getStatus() ==  LocalTuple.Status.UPDATING) {
 					log.warning("Nothing to update");
 					return;
 				}
 				if (localTuple.getStatus() ==  LocalTuple.Status.DELETING) {
 					log.severe("Nothing to delete");
 					return;
 				}
 				if (localTuple.getStatus() ==  LocalTuple.Status.INSERTING) {
 					log.info("inserting");
 
 					long nextTimestamp = getNextTimestamp();
 
 					PersistentRemoteTuple remoteTuple = new PersistentRemoteTuple();
 					remoteTuple.setDeleted(false);
 					remoteTuple.setElement(localElement);
 					remoteTuple.setTimestamp(nextTimestamp);
 					remoteTuple.setInsertionTimestamp(nextTimestamp);
 					remoteTuple.setClassName(tupleClass.getName());
 					remoteTuple.setId(localElement.getId());
 
 					pm.makePersistent(remoteTuple);
 				}
 			}
 
 		} finally {
 			pm.close();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public ArrayList<RemoteTuple<T>> syncElements(ArrayList<LocalTuple<T>> elements, long lastTimestamp) {
 		ArrayList<RemoteTuple<T>> ret = 
 			new ArrayList<RemoteTuple<T>>();
 
 		for (LocalTuple<T> localTuple : elements) {
 			syncElement(localTuple, lastTimestamp);
 		}
 
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			Query query = pm.newQuery(PersistentRemoteTuple.class);
			query.setFilter("className == currentClassName");
			query.setFilter("timestamp > lastTimestamp");
			query.declareParameters("String lastNameParam, long lastTimestamp");
 			List<PersistentRemoteTuple> results =
 				(List<PersistentRemoteTuple>) query.execute(tupleClass.getName(),lastTimestamp);
 
 
 			for (PersistentRemoteTuple remoteTuple : results) {
 				if (remoteTuple.isDeleted()) {
 					if (remoteTuple.getInsertionTimestamp() > lastTimestamp)
 						continue;
 				}
 				RemoteTuple<T> remoteTuple2 = new RemoteTuple<T>();
 				remoteTuple2.setDeleted(remoteTuple.isDeleted());
 				remoteTuple2.setElement((T) remoteTuple.getElement());
 				remoteTuple2.setTimestamp(remoteTuple.getTimestamp());
 				ret.add(remoteTuple2);
 			}
 			notifyContentObservers(true);
 		} finally {
 			pm.close();
 		}
 		return ret;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public ArrayList<Object> syncUnknownElements(ArrayList<Object> elements, long lastTimestamp) throws WrongObjectThrowable {
 		ArrayList<LocalTuple<T>> knownElements = 
 			new ArrayList<LocalTuple<T>>();
 		ArrayList<Object> ret = 
 			new ArrayList<Object>();
 
 		for (Object element : elements) {
 			try {
 				knownElements.add((LocalTuple<T>) element);
 			} catch (ClassCastException e) {
 				throw new WrongObjectThrowable();
 			}
 		}
 
 		ArrayList<RemoteTuple<T>> returnList =
 			syncElements(knownElements, lastTimestamp);
 		for (RemoteTuple<T> element: returnList) {
 			ret.add((Object)element);
 		}
 		return ret;
 	}
 }
