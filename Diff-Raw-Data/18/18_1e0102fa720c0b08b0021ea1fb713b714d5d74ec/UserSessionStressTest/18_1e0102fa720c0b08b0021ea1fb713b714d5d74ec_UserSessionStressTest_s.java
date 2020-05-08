 package com.mymed.tests.stress;
 
 /**
 * Perform the stress test on the session $ user tables. This test uses only a
 * maximum of two threads: one to create and insert the elements in the
 * database, the other to delete the entry from the database.
  * <p>
  * This class accepts no parameters, or two parameters on the command line. If
  * only one parameter, or more than two parameters are passed, it will be
  * treated as no parameters at all are passed.
  * <p>
  * If two parameters are passed, the first one controls whatever to run also the
  * deletion thread, the second one controls the number of elements that will be
  * created:
  * <ol>
  * <li><code>args0</code>: must be a boolean value, <code>true</code> or
  * <code>false</code></li>
  * <li><code>args1</code>: must be a positive integer more than 0
  * </ol>
  * 
  * @author Milo Casagrande
  * 
  */
 public class UserSessionStressTest {
 	public static void main(final String[] args) {
 		final SessionThread sessionThread;
 		final UserThread userThread;
 
 		if (args.length != 0 && args[0] != null) {
 			final boolean remove = Boolean.valueOf(args[0]);
 			final int maxElements = Math.abs(Integer.parseInt(args[1]));
 
 			sessionThread = new SessionThread(remove, maxElements);
 			userThread = new UserThread(remove, maxElements);
 		} else {
 			sessionThread = new SessionThread();
 			userThread = new UserThread();
 		}
 
 		userThread.start();
 		sessionThread.start();
 	}
 }
