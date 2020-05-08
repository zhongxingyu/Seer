 package com.max.algs.concurrency.lock;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 
 public final class PetersonLock implements ILock {
 
 	
 	private volatile AtomicBoolean[] flags = {new AtomicBoolean(false), new AtomicBoolean(false)};
 	
 	@Override
 	public void lock() throws InterruptedException {
 		
 		int cur = ((int)Thread.currentThread().getId()) & 1;
 		int other = (cur+1) & 1;
 		
		while( true ){
			setFlag(cur, true);
 			
			if( ! getFlag(other) ){
				break;
			}
			setFlag(cur, false);
 		}
		
	}
	
	boolean getFlag(int index){
		return flags[index].get();
 	}
 	
	void setFlag(int index, boolean value){
		flags[index].set(value);
	}
 
 	@Override
 	public void unlock() {		
 		int cur = ((int)Thread.currentThread().getId()) & 1;		
		setFlag(cur, false);
 	}
 
 	
 
 }
