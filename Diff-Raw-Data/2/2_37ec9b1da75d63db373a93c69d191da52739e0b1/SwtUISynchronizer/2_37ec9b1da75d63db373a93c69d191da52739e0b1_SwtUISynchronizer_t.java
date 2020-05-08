 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.uiprocess;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.exception.IExceptionHandlerManager;
 import org.eclipse.riena.core.service.Service;
 import org.eclipse.riena.ui.core.uiprocess.IUISynchronizer;
 
 /**
  * Serializes a runnable to the SWT-Thread
  * 
  */
 public class SwtUISynchronizer implements IUISynchronizer {
 
 	private static List<FutureSyncLatch> syncJobs = Collections.synchronizedList(new ArrayList<FutureSyncLatch>());
 	private static List<Runnable> asyncJobs = Collections.synchronizedList(new ArrayList<Runnable>());
 	private static Thread displayObserver;
 	private static AtomicBoolean workbenchShutdown = new AtomicBoolean(false);
 	private final Display display;
 
 	public SwtUISynchronizer() {
 		// we try to remember the display. This is a hack for Riena on RAP.
 		this.display = Display.getCurrent();
 	}
 
 	/**
 	 * @see IUISynchronizer#syncExec(Runnable)
 	 */
 	public void syncExec(final Runnable runnable) {
 		execute(new SyncExecutor(), runnable);
 	}
 
 	/**
 	 * @see IUISynchronizer#asyncExec(Runnable)
 	 */
 	public void asyncExec(final Runnable runnable) {
 		execute(new ASyncExecutor(), runnable);
 	}
 
 	/*
 	 * Executes the given runnable using the executor. First checks if there is
 	 * a display available.
 	 */
 	private void execute(final Executor executor, final Runnable runnable) {
 		if (isWorkbenchShutdown()) {
 			return;
 		}
 		if (!hasDisplay()) {
 			waitForDisplay(15000);
 		}
 		final Display currentDisplay = getDisplay();
 		if (executeOnDisplay(executor, runnable, currentDisplay)) {
 			return;
 		}
 
 		if (currentDisplay == null || getDisplay().isDisposed()) {
 			if (isSyncExecutor(executor)) {
 				waitForDisplayInitialisation(runnable);
 			} else {
 				queueRunnable(executor, runnable);
 			}
 
 		}
 
 	}
 
 	private void startObserver() {
 		synchronized (SwtUISynchronizer.class) {
 			if (displayObserver == null) {
 				displayObserver = new DisplayObserver();
 				displayObserver.start();
 			}
 		}
 	}
 
 	private void queueRunnable(final Executor executor, final Runnable runnable) {
 		synchronized (SwtUISynchronizer.class) {
 			asyncJobs.add(runnable);
 		}
 		startObserver();
 	}
 
 	private class DisplayObserver extends Thread {
 
 		@Override
 		public void run() {
 
 			// TODO [ev] I think there are two problems with this code:
 			//   (a) getDisplay().isDisposed() will NPE when getDisplay() == null
 			//   (b) if the display is disposed, we won't get another one. What happens with
 			//       the things that need to run?
 			while (PlatformUI.isWorkbenchRunning() && getDisplay() == null
 					|| (getDisplay() != null && getDisplay().isDisposed())) {
 				try {
 					Thread.sleep(50);
 				} catch (final InterruptedException e) {
 					getLogger().log(LogService.LOG_ERROR, e.getMessage());
 				}
 			}
 
 			synchronized (SwtUISynchronizer.class) {
 
 				// notify job waiters (syncExec)
 				final Iterator<FutureSyncLatch> syncIter = syncJobs.iterator();
 				while (syncIter.hasNext()) {
 					final SwtUISynchronizer.FutureSyncLatch futureSyncLatch = syncIter.next();
 					futureSyncLatch.countDown();
 					syncIter.remove();
 				}
 
 				if (getDisplay() == null) {
 					return;
 				}
 
 				// execute jobs (asyncExec)
 				final Iterator<Runnable> asyncIter = asyncJobs.iterator();
 				while (asyncIter.hasNext()) {
 					final Runnable next = asyncIter.next();
 					if (!isWorkbenchShutdown()) {
 						new ASyncExecutor().execute(getDisplay(), next);
 					}
 					asyncIter.remove();
 
 				}
 				displayObserver = null;
 			}
 
 		}
 	}
 
 	private void waitForDisplayInitialisation(final Runnable job) {
 		final FutureSyncLatch latch = new FutureSyncLatch(1, job);
 		synchronized (SwtUISynchronizer.class) {
 			syncJobs.add(latch);
 		}
 		startObserver();
 		try {
 
 			latch.await();
 		} catch (final InterruptedException e) {
 			getLogger().log(LogService.LOG_ERROR, e.getMessage());
 		}
 	}
 
 	private boolean isSyncExecutor(final Executor executor) {
 		return executor instanceof SyncExecutor;
 	}
 
 	private class FutureSyncLatch extends CountDownLatch {
 
 		private final Runnable job;
 
 		public FutureSyncLatch(final int count, final Runnable job) {
 			super(count);
 			this.job = job;
 		}
 
 		@Override
 		public void await() throws InterruptedException {
 			super.await();
 			if (!isWorkbenchShutdown()) {
 				new SyncExecutor().execute(getDisplay(), job);
 			}
 		}
 
 	}
 
 	private boolean executeOnDisplay(final Executor executor, final Runnable runnable, final Display display) {
 		if (null != display && !display.isDisposed()) {
 			executor.execute(display, runnable);
 			return true;
 		}
 		return false;
 	}
 
 	public Display getDisplay() {
 		if (display != null) {
 			return display;
 		}
 
 		return PlatformUI.getWorkbench().getDisplay();
 	}
 
 	private Logger getLogger() {
 		return Log4r.getLogger(org.eclipse.riena.internal.ui.swt.Activator.getDefault(), SwtUISynchronizer.class);
 	}
 
 	protected boolean hasDisplay() {
 		return display != null || PlatformUI.isWorkbenchRunning() && PlatformUI.getWorkbench().getDisplay() != null;
 	}
 
 	/**
 	 * Wait for display in 500ms increments, up to timeoutMs
 	 * 
 	 * @param timeoutMs
 	 *            time out in ms (positive)
 	 */
 	private void waitForDisplay(final int timeoutMs) {
 
 		Assert.isTrue(timeoutMs >= 0);
 
 		int time = 0;
 
 		do {
 			try {
 				Thread.sleep(500);
 				time += 500;
 			} catch (final InterruptedException e) {
 				return;
 			}
 		} while (time < timeoutMs && !hasDisplay());
 	}
 
 	private interface Executor {
 		void execute(Display display, Runnable runnable);
 	}
 
 	private static class SyncExecutor implements Executor {
 		public void execute(final Display display, final Runnable runnable) {
 			display.syncExec(runnable);
 		}
 	}
 
 	private static class ASyncExecutor implements Executor {
 		public void execute(final Display display, final Runnable runnable) {
 			display.asyncExec(runnable);
 		}
 	}
 
 	/**
 	 * @return if true if the workbench has been shutdown
 	 */
 	public static boolean isWorkbenchShutdown() {
 		return workbenchShutdown.get();
 	}
 
 	/**
 	 * @param workbenchShutdown
 	 *            the workbenchShutdown to set
 	 */
 	public static void setWorkbenchShutdown(final boolean workbenchShutdown) {
 		SwtUISynchronizer.workbenchShutdown.set(workbenchShutdown);
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public void readAndDispatch(final Callable<Boolean> condition) {
 		Assert.isNotNull(condition);
 
 		final Display currentDisplay = Display.getCurrent();
 
 		// dispatch events
 		try {
			while (!(condition.call() || isWorkbenchShutdown() || currentDisplay.isDisposed())) {
 				if (!currentDisplay.readAndDispatch()) {
 					currentDisplay.sleep();
 				}
 			}
 		} catch (final Exception e) {
 			Service.get(IExceptionHandlerManager.class).handleException(e);
 		}
 
 	}
 }
