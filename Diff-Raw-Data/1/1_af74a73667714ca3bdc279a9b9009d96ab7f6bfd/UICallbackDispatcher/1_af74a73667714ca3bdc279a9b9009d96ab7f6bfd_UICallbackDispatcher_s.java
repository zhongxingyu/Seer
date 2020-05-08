 /*******************************************************************************
  * Copyright (c) 2007, 2012 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.core.uiprocess;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.core.runtime.jobs.ProgressProvider;
 
 import org.eclipse.riena.core.exception.IExceptionHandlerManager;
 import org.eclipse.riena.core.service.Service;
 import org.eclipse.riena.internal.ui.core.Activator;
 
 /**
  * This class is used in conjunction with {@link UIProcess} and provides a {@link IProgressMonitor} that just synchronizes and delegates to another instance of
  * {@link IProgressMonitor}. Serialization is done by a {@link IUISynchronizer}.
  * 
  */
 public class UICallbackDispatcher extends ProgressProvider implements IUIMonitorContainer {
 
 	private final IUISynchronizer syncher;
 	private final List<IUIMonitor> uiMonitors;
 	private final ProcessInfo pInfo;
 	private ThreadSwitcher threadSwitcher;
 	private JobChangeAdapter jobListener;
 
 	/**
 	 * Creates a instance of {@link UICallbackDispatcher}.
 	 * 
 	 * @param syncher
 	 *            - the {@link IUISynchronizer} for the widget toolkit
 	 */
 	public UICallbackDispatcher(final IUISynchronizer syncher) {
 		this.uiMonitors = Collections.synchronizedList(new ArrayList<IUIMonitor>());
 		this.syncher = syncher;
 		this.pInfo = new ProcessInfo();
 	}
 
 	public ProcessInfo getProcessInfo() {
 		return pInfo;
 	}
 
 	/**
 	 * @see IUIMonitorContainer#addUIMonitor(IUIMonitor)
 	 */
 	public void addUIMonitor(final IUIMonitor uiMontitor) {
 		if (uiMonitors.contains(uiMontitor)) {
 			return;
 		}
 		final IProcessInfoAware processInfoAware = (IProcessInfoAware) uiMontitor.getAdapter(IProcessInfoAware.class);
 		if (processInfoAware != null) {
 			processInfoAware.setProcessInfo(pInfo);
 		}
 		this.uiMonitors.add(uiMontitor);
 	}
 
 	/**
 	 * @return the synchronizer that serializes to the UI-Thread
 	 */
 	protected final IUISynchronizer getSyncher() {
 		return syncher;
 	}
 
 	@Override
 	public final IProgressMonitor createMonitor(final Job job) {
 		threadSwitcher = createThreadSwitcher();
 		observeJob(job);
 		return threadSwitcher;
 	}
 
 	public ThreadSwitcher createThreadSwitcher() {
 		return new ThreadSwitcher(createWrappedMonitor());
 	}
 
 	private void observeJob(final Job job) {
 		if (jobListener == null) {
 			jobListener = new JobChangeAdapter() {
 
 				@Override
 				public void done(final IJobChangeEvent event) {
 					super.done(event);
 					jobDone(job);
 					job.removeJobChangeListener(this);
 					jobListener = null;
 				}
 			};
 			job.addJobChangeListener(jobListener);
 		}
 	}
 
 	private List<IUIMonitor> getMonitors() {
 		return new ArrayList<IUIMonitor>(uiMonitors);
 	}
 
 	/**
 	 * Creates the wrapped {@link IProgressMonitor} which will by serialized to the UI-Thread.
 	 * 
 	 * @return The wrapping monitor with a delegate inside
 	 */
 	protected IProgressMonitor createWrappedMonitor() {
 		/**
 		 * Default implementation of a monitor just delegating to the uiProcess Methods. This monitor gets called on the ui-Thread as a delegate of the
 		 * ThreadSwitcher
 		 */
 		return new NullProgressMonitor() {
 			@Override
 			public void beginTask(final String name, final int totalWork) {
 				pInfo.setMaxProgress(totalWork);
 				for (final IUIMonitor monitor : getMonitors()) {
 					monitor.initialUpdateUI(totalWork);
 				}
 			}
 
 			@Override
 			public void worked(final int work) {
 				for (final IUIMonitor monitor : getMonitors()) {
 					monitor.updateProgress(work);
 				}
 			}
 
 			@Override
 			public void done() {
 			}
 
 		};
 	}
 
 	private void jobDone(final Job job) {
 		synchronize(new Runnable() {
 
 			public void run() {
 				for (final IUIMonitor monitor : getMonitors()) {
 					try {
 						monitor.finalUpdateUI();
 					} catch (final Exception e) {
 						Service.get(Activator.getDefault().getContext(), IExceptionHandlerManager.class).handleException(e);
 					}
 				}
 			}
 		});
 	}
 
 	/**
 	 * This implementation of the ProgressMonitor delegates to another ProgressMonitor and serializes to the UI-Thread of the underlying ui technology.
 	 */
 	private final class ThreadSwitcher extends NullProgressMonitor {
 
 		private final IProgressMonitor delegate;
 
 		public ThreadSwitcher(final IProgressMonitor wrappedMonitor) {
 			this.delegate = wrappedMonitor;
 		}
 
 		@Override
 		public void beginTask(final String name, final int totalWork) {
 			synchronize(new Runnable() {
 
 				public void run() {
 					delegate.beginTask(name, totalWork);
 				}
 			});
 		}
 
 		@Override
 		public void worked(final int work) {
 			synchronize(new Runnable() {
 
 				public void run() {
 					delegate.worked(work);
 				}
 			});
 		}
 
 		@Override
 		public void done() {
 		}
 
 	}
 
 	private void synchronize(final Runnable runnable) {
 		syncher.syncExec(runnable);
 	}
 
 }
