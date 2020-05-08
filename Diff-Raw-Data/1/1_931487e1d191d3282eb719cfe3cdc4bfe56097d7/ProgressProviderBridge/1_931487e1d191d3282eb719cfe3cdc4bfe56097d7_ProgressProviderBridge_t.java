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
 package org.eclipse.riena.ui.core.uiprocess;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.internal.jobs.JobManager;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.core.runtime.jobs.ProgressProvider;
 
 import org.eclipse.riena.core.singleton.SingletonProvider;
 
 /**
  * A job can be presented by several instances of {@link ProgressProvider}. This
  * one delegates to those providers.
  */
 @SuppressWarnings("restriction")
 public class ProgressProviderBridge extends ProgressProvider {
 
 	private static final SingletonProvider<ProgressProviderBridge> PPB = new SingletonProvider<ProgressProviderBridge>(
 			ProgressProviderBridge.class);
 
 	private IProgressVisualizerLocator visualizerLocator;
 	private final Map<Job, IProgressVisualizer> jobToVisualizer = new HashMap<Job, IProgressVisualizer>();
 	private final Map<Job, UIProcess> jobUiProcess;
 
 	public ProgressProviderBridge() {
 		jobUiProcess = Collections.synchronizedMap(new HashMap<Job, UIProcess>());
 		registerJobChangeListener();
 	}
 
 	/**
 	 * Registers an internal observer at the {@link JobManager}
 	 * 
 	 * @since 3.0
 	 */
 	protected void registerJobChangeListener() {
 		Job.getJobManager().addJobChangeListener(new JobObserver());
 	}
 
 	/**
 	 * 
 	 * @return the singleton instance of {@link ProgressProviderBridge}
 	 */
 	public static ProgressProviderBridge instance() {
 		return PPB.getInstance();
 	}
 
 	/**
 	 * Sets the global {@link IProgressVisualizerLocator}
 	 */
 	public void setVisualizerFactory(final IProgressVisualizerLocator visualizerLocator) {
 		this.visualizerLocator = visualizerLocator;
 	}
 
 	@Override
 	public IProgressMonitor createMonitor(final Job job) {
 		final ProgressProvider provider = queryProgressProvider(job);
 		return provider.createMonitor(job);
 	}
 
 	/**
 	 * Locates a {@link ProgressProvider} for the given {@link Job}.The instance
 	 * will be of type {@link UICallbackDispatcher} which dispatches job state
 	 * changes to riena API process listeners.
 	 */
 	private ProgressProvider queryProgressProvider(final Job job) {
 		UIProcess uiprocess = jobUiProcess.get(job);
 		final Object context = getContext(job);
 		if (uiprocess == null) {
 			uiprocess = createDefaultUIProcess(job);
 			registerMapping(job, uiprocess);
 		}
 		final UICallbackDispatcher dispatcher = (UICallbackDispatcher) uiprocess.getAdapter(UICallbackDispatcher.class);
 
 		IProgressVisualizer progressVisualizer = jobToVisualizer.get(job);
 		if (progressVisualizer == null) {
 			progressVisualizer = visualizerLocator.getProgressVisualizer(context);
 			jobToVisualizer.put(job, progressVisualizer);
 			dispatcher.addUIMonitor(progressVisualizer);
 		}
 		return dispatcher;
 	}
 
 	private Object getContext(final Job job) {
 		return job.getProperty(UIProcess.PROPERTY_CONTEXT);
 	}
 
 	/**
 	 * Create a default instance of {@link UIProcess} wrapping the given
 	 * {@link Job}
 	 */
 	private UIProcess createDefaultUIProcess(final Job job) {
 		return new UIProcess(job);
 	}
 
 	/**
 	 * Registers a mapping {@link Job} -> {@link UIProcess}
 	 */
 	public synchronized void registerMapping(final Job job, final UIProcess process) {
 		jobUiProcess.put(job, process);
 	}
 
 	/**
 	 * Unregisters a mapping {@link Job} -> {@link UIProcess}
 	 */
 	public synchronized void unregisterMapping(final Job job) {
 		jobUiProcess.remove(job);
 	}
 
 	/**
 	 * 
 	 * @return a list of all registered {@link UIProcess} instances
 	 * @since 3.0
 	 */
 	public List<UIProcess> getRegisteredUIProcesses() {
 		return new ArrayList<UIProcess>(jobUiProcess.values());
 	}
 
 	/**
 	 * Observes the state of jobs. Scheduled jobs will be assigned to an
 	 * instance of {@link UIProcess}. To avoid memory leaks jobs need to be
 	 * unregistered from internal mappings.
 	 */
 	private final class JobObserver extends JobChangeAdapter {
 
 		@Override
 		public void scheduled(final IJobChangeEvent event) {
 			final Job job = event.getJob();
 			if (jobUiProcess.get(job) == null) {
 				createDefaultUIProcess(job);
 			}
 		}
 
 		@Override
 		public void done(final IJobChangeEvent event) {
 			final Job job = event.getJob();
 			jobToVisualizer.remove(job);
			unregisterMapping(job);
 		}
 
 	}
 }
