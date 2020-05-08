 package org.neuronbucket.kv.util;
 
 
 public class SingletonFactory<T> implements Factory<T> {
 
	public static <T> SingletonFactory<T> newInstance(T singleton) {
		return new SingletonFactory<T>(singleton);
	}

 	private T mSingleton;
 
 	public SingletonFactory(T singleton) {
 		mSingleton = singleton;
 	}
 
 	public T newInstance() {
 		return mSingleton;
 	}
 
 }
