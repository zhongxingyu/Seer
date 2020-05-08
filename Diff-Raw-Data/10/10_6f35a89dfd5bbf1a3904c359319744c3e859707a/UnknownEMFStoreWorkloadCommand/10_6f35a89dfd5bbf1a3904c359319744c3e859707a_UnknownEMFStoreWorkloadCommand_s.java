 package org.eclipse.emf.emfstore.client.common;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.emfstore.client.model.util.WorkspaceUtil;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 
 public abstract class UnknownEMFStoreWorkloadCommand<T> {
 
 	private static final int POLLING_INTERVAL = 500;
 	private final IProgressMonitor monitor;
 	private int worked;
 
 	private static class SingletonHolder {
 		private static final ExecutorService executor = Executors.newCachedThreadPool();
 	}
 
 	/**
 	 * 
 	 * @param monitor
 	 *            the monitor that will be used to indicate that the command is in progress
 	 */
 	public UnknownEMFStoreWorkloadCommand(IProgressMonitor monitor) {
 		this.monitor = monitor;
 		worked = 0;
 	}
 
 	public T execute() throws EmfStoreException {
 
 		Future<T> future = SingletonHolder.executor.submit(new Callable<T>() {
 			public T call() throws Exception {
 				return run(monitor);
 			}
 		});
 
 		T result = null;
 		boolean resultReceived = false;
 
 		while (!resultReceived) {
 
 			try {
 				result = future.get(POLLING_INTERVAL, TimeUnit.MILLISECONDS);
 				resultReceived = true;
 			} catch (InterruptedException e) {
 				WorkspaceUtil.logException(e.getMessage(), e);
 				throw new EmfStoreException("Workload command got interrupted", e);
 			} catch (ExecutionException e) {
 				WorkspaceUtil.logException(e.getMessage(), e);
 				throw new EmfStoreException(e.getCause().getMessage());
 			} catch (TimeoutException e) {
 				// do nothing
 			}
 
 			monitor.worked(1);
 			worked++;
 		}
 
 		return result;
 	}
 
 	/**
 	 * Returns how many ticks the command has incremented the monitor
 	 * 
 	 * @return amount of ticks that were incremented
 	 */
 	public int getWorked() {
 		return worked;
 	}
 
 	public abstract T run(IProgressMonitor monitor) throws EmfStoreException;
 }
