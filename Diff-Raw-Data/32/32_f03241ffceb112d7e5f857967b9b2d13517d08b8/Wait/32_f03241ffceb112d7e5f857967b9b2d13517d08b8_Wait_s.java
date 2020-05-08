 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2009, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 
 package org.jboss.test.selenium.waiting;
 
 import java.util.Vector;
 import java.util.concurrent.atomic.AtomicReference;
 
 /**
  * <p>
  * Implementation of waiting to satisfy condition.
  * </p>
  * 
  * <p>
  * This class constructs instances of Waiting by factory methods but
  * provides its functionality (defaulted waiting) by static method until.
  * </p>
  * 
  * <p>
  * Other factories (getDefault(), timeout(long), interval(long)) serves as simplified way to build new configured 
  * instance, which can simple run waiting loop.
  * </p>
  * 
  * <p>
  * Class is intended to be used like these:
  * </p>
  * 
  * <ol>
  * <li>
  * <h3>direct waiting loop</h3>
  * <pre>
  * Wait.until(new Condition()) {
  *     public boolean isTrue() {
  *         return ...;
  * 	   }
  * }
  * </pre>
  * </li>
  * 
  * <li>
  * <h3>direct waiting loop with parameters</h3>
  * <pre>
  * Wait.interval(100).timeout(3000).until(new Not()) {
  *     public boolean not() {
  *         return ...;
  *     }
  * }
  * </pre>
  * </li>
  * 
  * <li>
  * <h3>save settings and run configured wait loop</h3>
  * <pre>
  * final String locator1 = &quot;...&quot;;
  * final String locator2 = &quot;...&quot;;
  * 
  * ...
  * 
  * Condition locatorsEqual = new Condition() {
  *     public boolean isTrue() {
  *         return selenium.equals(locator1, locator2);
  *     }
  * };
  * 
  * Waiting waitResponse = Wait.interval(1000).timeout(30000);
  * 			
  * ...
  * 
  * waitResponse.until(locatorsEqual); // some usage
  * 
  * ...
  * 
  * waitResponse.until(locatorsEqual); // other usage
  * </pre>
  * </li>
  * </ol>
  * 
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class Wait {
 
 	/**
 	 * Default waiting interval
 	 */
 	public static final long DEFAULT_INTERVAL = com.thoughtworks.selenium.Wait.DEFAULT_INTERVAL;
 	/**
 	 * Default timeout of waiting
 	 */
 	public static final long DEFAULT_TIMEOUT = com.thoughtworks.selenium.Wait.DEFAULT_TIMEOUT;
 
 	/**
 	 * Class cannot be constructed in standard way, use factories method instead
 	 * since it has static members only and no other purposes.
 	 */
 	private Wait() {
 	}
 
 	/**
 	 * Constructs default preset instance of waiting (@see Waiting).
 	 * 
 	 * @return default Waiting instance
 	 */
 	public static Waiting getDefault() {
 		return Waiting.getInstance();
 	}
 
 	/**
 	 * Constructs preset instance of waiting (@see Waiting) with given interval.
 	 * 
 	 * @param interval
 	 *            in miliseconds that will be preset to Waiting
 	 * @return Waiting instance initialized with given interval
 	 */
 	public static Waiting interval(long interval) {
 		return getDefault().interval(interval);
 	}
 
 	/**
 	 * Constructs preset instance of waiting (@see Waiting) with given timeout.
 	 * 
 	 * @param timeout
 	 *            in miliseconds that will be preset to Waiting
 	 * @return Waiting instance initialized with given timeout
 	 */
 	public static Waiting timeout(long timeout) {
 		return getDefault().timeout(timeout);
 	}
 
 	/**
 	 * Constructs preset instance of waiting (@see Waiting) with given throwable
 	 * failure, which indicates timeout.
 	 * 
 	 * If failure is set to null, timeout will not result to failure!
 	 * 
 	 * @param failure
 	 *            throwable that will be thrown in case of waiting timeout or
 	 *            null if waiting timeout shouldn't result to failure
 	 * @return Waiting instance initialized with given failure
 	 */
 	public static Waiting failWith(Throwable failure) {
 		return getDefault().failWith(failure);
 	}
 
 	/**
 	 * Constructs preset instance of waiting (@see Waiting) with failures
 	 * initialized to AssertionError with given message.
 	 * 
 	 * @param failMessage
 	 *            message which will be set to thrown AssertionError
 	 * @return Waiting instance initialized with failures using AssertionError
 	 *         with given message
 	 */
 	public static Waiting failWith(String failMessage) {
 		return getDefault().failWith(failMessage);
 	}
 
 	/**
 	 * Constructs preset instance of waiting (@see Waiting) with no failure.
 	 * 
 	 * Waiting timeout with this preset dont result to failure!
 	 * 
 	 * @return Waiting instance initialized with no failure
 	 */
 	public static Waiting dontFail() {
 		return getDefault().dontFail();
 	}
 
 	/**
 	 * Will wait for default amount of time. Default timeout specified in
 	 * Wait.DEFAULT_TIMEOUT
 	 */
 	public static void waitForTimeout() {
 		getDefault().waitForTimeout();
 	}
 	
 	/**
 	 * Waits until Retrieve's implementation doesn't retrieve value other
 	 * than oldValue.
 	 * 
 	 * @param <T>
 	 *            type of value what we are waiting for change
 	 * @param oldValue
 	 *            value that we are waiting for change
 	 * @param retrieve
 	 *            implementation of retrieving actual value
 	 */
 	public static <T> void waitForChange(T oldValue, Retrieve<T> retrieve) {
 		getDefault().waitForChangeAndReturn(oldValue, retrieve);
 	}
 	
 	/**
 	 * Waits until Retrieve's implementation doesn't retrieve value other
 	 * than oldValue and this new value returns.
 	 * 
 	 * @param <T>
 	 *            type of value what we are waiting for change
 	 * @param oldValue
 	 *            value that we are waiting for change
 	 * @param retrieve
 	 *            implementation of retrieving actual value
 	 * @return new retrieved value
 	 */
 	public static <T> T waitForChangeAndReturn(T oldValue, Retrieve<T> retrieve) {
 		return getDefault().waitForChangeAndReturn(oldValue, retrieve);
 	}
 
 	/**
 	 * Will wait for specified amount of time.
 	 * 
 	 * Timeout is specified in miliseconds
 	 * 
 	 * @param timeoutInMilis
 	 *            time to wait in miliseconds
 	 */
 	public static void waitFor(long timeoutInMilis) {
 		getDefault().timeout(timeoutInMilis).waitForTimeout();
 	}
 
 	/**
 	 * Stars loop waiting to satisfy condition with default timeout and
 	 * interval.
 	 * 
 	 * @param condition
 	 *            what wait for to be satisfied
 	 */
 	public static void until(Condition condition) {
 		getDefault().until(condition);
 	}
 
 	/**
 	 * Class intented to construct by factories in Wait class.
 	 * 
 	 * Implementation of waiting for satisfaction of condition.
 	 */
 	public static class Waiting {
 		/**
 		 * Interval between tries to test condition for satisfaction
 		 */
 		private long interval = DEFAULT_INTERVAL;
 		/**
 		 * Timeout of whole waiting procedure
 		 */
 		private long timeout = DEFAULT_TIMEOUT;
 
 		/**
 		 * Failure indicates waiting timeout.
 		 * 
 		 * If is set to null, no failure will be thrown after timeout.
 		 */
 		private Throwable failure = new AssertionError();
 
 		/**
 		 * Singleton
 		 */
 		private static final AtomicReference<Waiting> singleton = new AtomicReference<Waiting>(null);
 
 		/**
 		 * Factory method
 		 * 
 		 * @return singleton
 		 */
 		private static Waiting getInstance() {
 			if (singleton.get() == null) {
 				singleton.compareAndSet(null, new Waiting());
 			}
 			return singleton.get();
 		}
 
 		/**
 		 * This class cannot be constructed directly
 		 */
 		private Waiting() {
 		}
 
 		/**
 		 * Sets condition testing interval to this instance and return it.
 		 * 
 		 * @param interval
 		 *            in miliseconds that will be preset to Waiting
 		 * @return Waiting instance initialized with given interval
 		 */
 		public Waiting interval(long interval) {
 			if (interval == this.interval) {
 				return this;
 			}
 			Waiting copy = this.copy();
 			copy.interval = interval;
 			return copy;
 		}
 
 		/**
 		 * Sets waiting timeout to this instance and return it.
 		 * 
 		 * @param timeout
 		 *            in miliseconds that will be preset to Waiting
 		 * @return Waiting instance configured with given timeout
 		 */
 		public Waiting timeout(long timeout) {
 			if (timeout == this.timeout) {
 				return this;
 			}
 			Waiting copy = this.copy();
 			copy.timeout = timeout;
 			return copy;
 		}
 
 		/**
 		 * Sets failure of timeout to given throwable and return it.
 		 * 
 		 * If failure is set to null, timeout will not result to failure!
 		 * 
 		 * @param failure
 		 *            throwable that will be thrown in case of waiting timeout
 		 *            or null if waiting timeout shouldn't result to failure
 		 * @return Waiting instance initialized with given failure
 		 */
 		public Waiting failWith(Throwable failure) {
 			if (failure == null) {
 				if (this.failure == null) {
 					return this;
 				}
 			} else {
 				if (failure.equals(this.failure)) {
 					return this;
 				}
 			}
 			Waiting copy = this.copy();
 			copy.failure = failure;
 			return copy;
 		}
 
 		/**
 		 * Sets failure of timeout to AssertionError with given failMessage.
 		 * 
 		 * @param failMessage
 		 *            message which will be set to thrown AssertionError
 		 * @return Waiting instance initialized with failures using
 		 *         AssertionError with given message
 		 */
 		public Waiting failWith(String failMessage) {
 			return failWith(new AssertionError(failMessage));
 		}
 
 		/**
 		 * Sets no failure after waiting timeout.
 		 * 
 		 * Waiting timeout with this preset dont result to failure!
 		 * 
 		 * @return Waiting instance initialized with no failure
 		 */
 		public Waiting dontFail() {
 			return failWith((Throwable) null);
 		}
 
 		/**
		 * <p>Stars loop waiting to satisfy condition.</p>
		 * 
		 * <p>The condition will be tested</p>
		 *  
		 * <ul>
		 * <li>on the start,</li>
		 * <li>every time interval after last try</li>
		 * <li>and also once after timeout when finishes interval since last try before timeout.</li>
		 * </ul>
		 * 
		 * <p>Scheme:</p>
		 * 
		 * <p><pre>S ..int.. T ..int.. T ..int1..timeout..int2.. L</pre></p>
		 * 
		 * <p>
		 * <div>S - starting try</div>
		 * <div>T - try within intervals</div>
		 * <div>L - last try after timeout</div>
		 * <div>int - one interval</div>
		 * <div>int = int1 + int2</div>
		 * </p>
 		 * 
 		 * @param condition
 		 *            what wait for to be satisfied
 		 */
 		public void until(Condition condition) {
 			long start = System.currentTimeMillis();
 			long end = start + timeout;
 			while (System.currentTimeMillis() < end) {
				if (condition.isTrue())
					return;
 				try {
 					Thread.sleep(interval);
 				} catch (InterruptedException e) {
 					throw new RuntimeException(e);
 				}
				if (System.currentTimeMillis() >= end) {
					if (condition.isTrue())
						return;
				}
 			}
 			fail();
 		}
 
 		/**
 		 * Waits for predefined amount of time.
 		 */
 		public void waitForTimeout() {
 			try {
 				Thread.sleep(timeout);
 			} catch (InterruptedException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		
 		/**
 		 * Waits until Retrieve's implementation doesn't retrieve value other
 		 * than oldValue.
 		 * 
 		 * @param <T>
 		 *            type of value what we are waiting for change
 		 * @param oldValue
 		 *            value that we are waiting for change
 		 * @param retrieve
 		 *            implementation of retrieving actual value
 		 */
 		public <T> void waitForChange(T oldValue, Retrieve<T> retrieve) {
 			waitForChangeAndReturn(oldValue, retrieve);
 		}
 		
 		/**
 		 * Waits until Retrieve's implementation doesn't retrieve value other
 		 * than oldValue and this new value returns.
 		 * 
 		 * @param <T>
 		 *            type of value what we are waiting for change
 		 * @param oldValue
 		 *            value that we are waiting for change
 		 * @param retrieve
 		 *            implementation of retrieving actual value
 		 * @return new retrieved value
 		 */
 		public <T> T waitForChangeAndReturn(final T oldValue, final Retrieve<T> retrieve) {
 			final Vector<T> vector = new Vector<T>(1);
 
 			this.until(new Condition() {
 				public boolean isTrue() {
 					vector.add(0, retrieve.retrieve());
 					if (oldValue == null) {
 						return vector.get(0) != null;
 					}
 					return !oldValue.equals(vector.get(0));
 				}
 			});
 
 			return vector.get(0);
 		}
 
 		/**
 		 * Tries to fail by throwing 'failure' throwable.
 		 * 
 		 * If failure is instance of RuntimeException, will be directly thrown.
 		 * Otherwise will be failure clothe to RuntimeException.
 		 * 
 		 * If failure is null, method wont fail.
 		 */
 		private void fail() {
 			if (failure != null) {
 				if (failure instanceof RuntimeException) {
 					throw (RuntimeException) failure;
 				} else {
 					throw new RuntimeException(failure);
 				}
 			}
 		}
 
 		/**
 		 * This methods helps to make copies of current instance.
 		 * 
 		 * @return copy of current instance
 		 */
 		private Waiting copy() {
 			Waiting copy = new Waiting();
 			copy.interval = this.interval;
 			copy.timeout = this.timeout;
 			copy.failure = this.failure;
 			return copy;
 		}
 	}
 }
