 package com.szas.server.gwt.client;
 
 import java.util.ArrayList;
 
 public class RemoteDAOImpl<T extends Tuple> implements RemoteDAO<T> {
 	int timestamp = 0;
 	
 	@Override
 	public long getTimestamp() {
 		return timestamp;
 	}
 	
 	private int getNextTimestamp() {
 		timestamp +=1;
 		return timestamp -1;
 	}
 
 	ArrayList<RemoteTuple<T>> elements = 
 		new ArrayList<RemoteTuple<T>>();
 	
 	@Override
 	public ArrayList<T> getAll() {
 		ArrayList<T> ret = new ArrayList<T>();
 		for (RemoteTuple<T> localTuple : elements) {
 			if (localTuple.isDeleted())
 				continue;
 			T element = localTuple.getElement();
 			ret.add(element);
 		}
 		return ret;
 	}
 
 	@Override
 	public void insert(T element) {
 		RemoteTuple<T> remoteTuple = new RemoteTuple<T>();
 		remoteTuple.setTimestamp(getNextTimestamp());
 		remoteTuple.setDeleted(false);
 		remoteTuple.setElement(element);
 		elements.add(remoteTuple);
 	}
 
 	@Override
 	public void delete(T element) {
 		for (RemoteTuple<T> remoteTuple : elements) {
 			T listElement = remoteTuple.getElement();
 			if (! listElement.equals(element)) 
 				continue;
 			remoteTuple.setTimestamp(getNextTimestamp());
 			remoteTuple.setDeleted(true);
 			return;
 		}
 	}
 
 	@Override
 	public void update(T element) {
 		for (RemoteTuple<T> remoteTuple : elements) {
 			T listElement = remoteTuple.getElement();
 			if (! listElement.equals(element)) 
 				continue;
 			remoteTuple.setTimestamp(getNextTimestamp());
 			return;
 		}
 	}
 
 	@Override
 	public ArrayList<RemoteTuple<T>> syncElements(ArrayList<LocalTuple<T>> elements, long lastTimestamp) {
 		for (LocalTuple<T> localTuple : elements) {
 			T localElement = localTuple.getElement();
			boolean found = false;
 			for (RemoteTuple<T> remoteTuple : this.elements) {
 				T remoteElement = remoteTuple.getElement();
 				if (localElement.getId() != remoteElement.getId()) 
 					continue;
 				if (remoteTuple.getTimestamp() > lastTimestamp) {
 					// inserting was synced before changes
 					// TODO return exception
					found = true;
 					break;
 				}
 				remoteTuple.setElement(localElement);
				remoteTuple.setDeleted(localTuple.getStatus() == LocalTuple.Status.DELETING);
				remoteTuple.setTimestamp(getNextTimestamp());
				found = true;
 				break;
 			}
			if (!found) {
				RemoteTuple<T> remoteTuple = new RemoteTuple<T>();
				remoteTuple.setElement(localElement);
				remoteTuple.setDeleted(localTuple.getStatus() == LocalTuple.Status.DELETING);
				remoteTuple.setTimestamp(getNextTimestamp());
				this.elements.add(remoteTuple);
			}
 		}
 		
 		ArrayList<RemoteTuple<T>> ret = 
 			new ArrayList<RemoteTuple<T>>();
 		for (RemoteTuple<T> remoteTuple : this.elements) {
 			if (remoteTuple.getTimestamp() <= lastTimestamp)
 				continue;
 			ret.add(remoteTuple);
 		}
 		return ret;
 	}
 
 
 	@Override
 	public ArrayList<Object> syncUnknownElements(ArrayList<Object> elements, long lastTimestamp) {
 		ArrayList<LocalTuple<T>> knownElements = 
 			new ArrayList<LocalTuple<T>>();
 		ArrayList<Object> ret = 
 			new ArrayList<Object>();
 		
 		for (Object element : elements) {
 			// TODO throw some exception
 			knownElements.add((LocalTuple<T>) element);
 		}
 		
 		ArrayList<RemoteTuple<T>> returnList =
 			syncElements(knownElements, lastTimestamp);
 		for (RemoteTuple<T> element: returnList) {
 			ret.add((Object)element);
 		}
 		return ret;
 	}
 
 }
