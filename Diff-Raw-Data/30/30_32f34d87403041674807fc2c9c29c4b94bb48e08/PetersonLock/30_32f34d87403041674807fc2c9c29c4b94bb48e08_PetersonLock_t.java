 package com.max.algs.concurrency.lock;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 

 public final class PetersonLock implements ILock {
 
 	
 	private volatile AtomicBoolean[] flags = {new AtomicBoolean(false), new AtomicBoolean(false)};
 	
	private volatile int turn = -1;
	
	
 	@Override
 	public void lock() throws InterruptedException {
 		
 		int cur = ((int)Thread.currentThread().getId()) & 1;
 		int other = (cur+1) & 1;
 		
		flags[cur].set(true);
		turn = other;
		
		while( flags[other].get() && turn == other ){
 			
 		}
 	}
 	
 
 	@Override
 	public void unlock() {		
 		int cur = ((int)Thread.currentThread().getId()) & 1;		
		flags[cur].set(false);
 	}
 
 	
 
 }
