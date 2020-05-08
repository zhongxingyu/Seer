 package org.eclipse.emf.emfstore.client.ui.common;
 
 import java.util.concurrent.Callable;
 
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.swt.widgets.Display;
 
 public class RunInUI {
 
	private static RunInUI runInUI;
 
 	private RunInUI() {
 
 	}
 
 	public static class WithException {
 
 		private static RunInUI runInUI = new RunInUI();
 
 		public static <T> T runWithResult(final Callable<T> callable) throws EmfStoreException {
 			return runInUI.new RunInUIThreadWithResult<T>() {
 				@Override
 				public T doRun() throws EmfStoreException {
 					try {
 						return callable.call();
 					} catch (EmfStoreException e) {
 						throw e;
 					} catch (Exception e) {
 						throw new EmfStoreException(e.getMessage(), e);
 					}
 				}
 			}.execute();
 		}
 
 		public static void run(final Callable<Void> callable) throws EmfStoreException {
 			runInUI.new RunInUIThread() {
 				@Override
 				public Void doRun() throws EmfStoreException {
 					try {
 						callable.call();
 						return null;
 					} catch (Exception e) {
 						throw new EmfStoreException(e.getMessage());
 					}
 				}
 			}.execute();
 		}
 	}
 
 	// public static class _ {
 
 	// private static RunInUI runInUI = new RunInUI();
 
 	/*
 	 * TODO: provider additional methods with Display parameter
 	 */
 
 	public static void run(final Callable<Void> callable) {
 		try {
 			runInUI.new RunInUIThread() {
 				@Override
 				public Void doRun() throws EmfStoreException {
 					try {
 						callable.call();
 						return null;
 					} catch (Exception e) {
 						throw new EmfStoreException(e.getMessage());
 					}
 				}
 			}.execute();
 		} catch (EmfStoreException e) {
 			// ignore
 		}
 	}
 
 	public static <T> T runWithResult(final Callable<T> callable) {
 		try {
 			return runInUI.new RunInUIThreadWithResult<T>() {
 				@Override
 				public T doRun() throws EmfStoreException {
 					try {
 						return callable.call();
 					} catch (Exception e) {
 						throw new EmfStoreException(e.getMessage());
 					}
 				}
 			}.execute();
 		} catch (EmfStoreException e) {
 			// ignore
 		}
 
 		return null;
 	}
 
 	// }
 
 	/**
 	 * A simple wrapper for UI calls that has the same effect as calling {@link Display#syncExec(Runnable)} and is
 	 * only provided for aesthetic reasons.
 	 * 
 	 * @author emueller
 	 * 
 	 * @see RunInUIThreadWithResult
 	 */
 	private abstract class RunInUIThread extends RunInUIThreadWithResult<Void> {
 
 		public RunInUIThread() {
 			super(Display.getDefault());
 		}
 
 		/**
 		 * Constructor.
 		 * 
 		 * @param display
 		 *            the {@link Display} that will be used to execute the wrapped call
 		 */
 		public RunInUIThread(Display display) {
 			super(display);
 		}
 
 		/**
 		 * 
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.emfstore.client.ui.common.RunInUIThreadWithResult#doRun(org.eclipse.swt.widgets.Shell)
 		 */
 		@Override
 		public abstract Void doRun() throws EmfStoreException;
 	}
 
 	/**
 	 * A simple wrapper for UI calls that has the same effect as calling {@link Display#syncExec(Runnable)} but
 	 * can return a value.
 	 * 
 	 * @author emueller
 	 * 
 	 * @param <T> the return type of the wrapped call
 	 * @see RunInUIThread
 	 */
 	private abstract class RunInUIThreadWithResult<T> {
 
 		private T returnValue;
 		private final Display display;
 		private EmfStoreException exception;
 
 		/**
 		 * Default constructor.
 		 */
 		public RunInUIThreadWithResult() {
 			display = Display.getDefault();
 		}
 
 		/**
 		 * Constructor.
 		 * 
 		 * @param display
 		 *            the {@link Display} that will be used to execute the wrapped call
 		 */
 		public RunInUIThreadWithResult(Display display) {
 			this.display = display;
 		}
 
 		/**
 		 * Executes the wrapper.
 		 * 
 		 * @return the return value of the wrapped call
 		 * 
 		 * @throws EmfStoreException
 		 *             in case an error occurs
 		 */
 		public T execute() throws EmfStoreException {
 
 			returnValue = null;
 
 			display.syncExec(new Runnable() {
 
 				public void run() {
 					try {
 						returnValue = RunInUIThreadWithResult.this.doRun();
 					} catch (EmfStoreException e) {
 						exception = e;
 					}
 				}
 			});
 
 			if (exception != null) {
 				throw exception;
 			}
 
 			return returnValue;
 		}
 
 		/**
 		 * Invokes the wrapped call and must be implemented by clients.
 		 * 
 		 * @param shell
 		 *            the shell that is used during the execution
 		 * @return an optional return value that may be returned by clients
 		 * 
 		 * @throws EmfStoreException in case an error occurs
 		 */
 		public abstract T doRun() throws EmfStoreException;
 	}
 
 }
