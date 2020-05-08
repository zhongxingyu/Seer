 package edu.ncsu.uhp.escape.engine;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  * A thread safe queue used to contain Temporary actors.
  * 
  * @author Tyler Dodge
  * 
  */
 public class ControlledList<T> implements Iterable<T> {
 	private List<T> data;
 	private Queue<T> toBeAdded;
 	private Queue<T> toBeRemoved;
 	private Lock tempLock;
 
 	public ControlledList() {
 		tempLock = new ReentrantLock();
 		data = new ArrayList<T>();
 		toBeAdded=new LinkedList<T>();
 		toBeRemoved=new LinkedList<T>();
 	}
 
 	public boolean isEmpty() {
 		return data.isEmpty();
 	}
 
 	public void add(T actor) {
 		tempLock.lock();
 		toBeAdded.add(actor);
 		tempLock.unlock();
 	}
 
 	public void remove(T actor) {
 		tempLock.lock();
		toBeRemoved.add(actor);
 		tempLock.unlock();
 	}
 
 	public T flushAddOnce() {
 		if (toBeAdded.isEmpty())
 			return null;
 		T t = toBeAdded.remove();
 		data.add(t);
 		return t;
 	}
 
 	public T flushRemoveOnce() {
 		if (toBeRemoved.isEmpty())
 			return null;
 		T t = toBeRemoved.remove();
 		data.remove(t);
 		return t;
 	}
 
 	public void flushAdd() {
 		while (flushAddOnce() != null) {
 		}
 	}
 
 	public void flushRemove() {
 		while (flushRemoveOnce() != null) {
 		}
 	}
 
 	public void flush() {
 		flushAdd();
 		flushRemove();
 	}
 
 	public Iterator<T> iterator() {
 		return data.iterator();
 	}
 
 }
