 package org.neuronbucket.kv.util;
 
 
 public class SingletonFactory<T> implements Factory<T> {
 
 	private T mSingleton;
 
 	public SingletonFactory(T singleton) {
 		mSingleton = singleton;
 	}
 
 	public T newInstance() {
 		return mSingleton;
 	}
 
 }
