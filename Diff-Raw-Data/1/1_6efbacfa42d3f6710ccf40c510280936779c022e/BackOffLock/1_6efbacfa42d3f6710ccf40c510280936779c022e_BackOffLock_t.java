 package org.araqne.logstorage.engine;
 
 import java.util.Random;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Lock;
 
 public class BackOffLock {
 	private static ThreadLocal<Random> rand = new ThreadLocal<Random>() {
 		@Override
 		protected Random initialValue() {
 			return new Random();
 		}
 	};
 
 	private int min;
 	private int max;
 	private static final int MAX_WAIT = Integer.MAX_VALUE / 100;
 
 	private long to;
 	private boolean locked = false;
 
 	private Lock lock;
 
 	private int tryCnt = 0;
 
 	public BackOffLock(Lock l) {
 		this.lock = l;
 		to = -1;
 		this.min = 0;
 		this.max = MAX_WAIT;
 	}
 
 	public BackOffLock(Lock l, long time, TimeUnit unit) {
		this.lock = l;
 		to = unit.toNanos(time);
 
 		this.min = 0;
 		this.max = (int) Math.min(unit.toNanos(time) / 100, MAX_WAIT);
 	}
 
 	private long nextBackOff() {
 		int rmin = 1000;
 		int rmax = Math.max(1001, min);
 		int ni = rmin + rand.get().nextInt(rmax - rmin);
 		min = Math.min(min == 0 ? 1 : min * 2, max);
 		return ni;
 	}
 	
 	public boolean tryLock() throws InterruptedException {
 		if (tryCnt != 0) {
 			long cbo = nextBackOff();
 			to -= cbo;
 			return (locked = lock.tryLock(cbo, TimeUnit.NANOSECONDS));
 		} else {
 			return (locked = lock.tryLock());
 		}
 	}
 
 	public void setDone() {
 		to = -1;
 	}
 
 	public boolean isDone() {
 		return locked || to < 0;
 	}
 
 	public boolean hasLocked() {
 		return locked;
 	}
 
 	public void unlock() {
 		if (locked)
 			lock.unlock();
 	}
 
 }
