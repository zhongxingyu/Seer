 package ceri.common.test;
 
 /**
  * This is a holder for a state value. Useful when testing behavior across threads.
  * For example, set a value on one thread and wait for it on another. 
  */
 public class TestState<T> {
 	private static final int DEFAULT_WAIT_MS = 1000;
 	private T value = null;
 	
 	public TestState() {	
 	}
 	
 	public TestState(T value) {
 		set(value);
 	}
 	
 	public synchronized T set(T value) {
 		T old = value;
 		this.value = value;
 		notifyAll();
 		return old;
 	}
 	
 	public synchronized T get() {
 		return value;
 	}
 	
 	public synchronized T waitFor(T value) {
 		return waitFor(value, DEFAULT_WAIT_MS);
 	}
 	
 	public synchronized T waitFor(T value, long ms) {
 		try {
 			long t = System.currentTimeMillis() + ms;
 			while (!equalsValue(value) && (ms == 0 || System.currentTimeMillis() < t)) {
				wait(t - System.currentTimeMillis());
 			}
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 		return get();
 	}
 	
 	private boolean equalsValue(T value) {
 		if (this.value == value) return true;
 		if (this.value == null) return false;
 		return this.value.equals(value);
 	}
 	
 }
