 package org.txxfu.synctest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class SyncQueue2<T> implements AbstractQueue<T> {
 
//	private Object syncLock = new Object();
 
 	private List<T> queue = new ArrayList<T>();
 
 	@Override
 	public void putObject(T obj) {
		synchronized (queue) {
 			while (queue.size() > 0) {
 				try {
					queue.wait();
 				} catch (InterruptedException e) {
 					//
 				}
 			}
 			queue.add(obj);
 			System.out.println(Thread.currentThread().getId() + " put " + obj);
			queue.notify();
 		}
 	}
 
 	@Override
 	public T getObject() {
		synchronized (queue) {
 			while (queue.size() == 0) {
 				try {
					queue.wait();
 				} catch (InterruptedException e) {
 					//
 				}
 			}
 			T obj = queue.get(0);
 			queue.clear();
 			System.out.println(Thread.currentThread().getId() + " get " + obj);
			queue.notify();
 			return obj;
 		}
 	}
 
 }
