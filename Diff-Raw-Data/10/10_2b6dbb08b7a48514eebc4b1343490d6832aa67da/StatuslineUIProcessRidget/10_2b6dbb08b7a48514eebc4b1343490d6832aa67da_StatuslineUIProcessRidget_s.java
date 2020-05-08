 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TimerTask;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.swt.widgets.Display;
 
 import org.eclipse.riena.core.wire.InjectExtension;
 import org.eclipse.riena.core.wire.Wire;
 import org.eclipse.riena.internal.ui.ridgets.swt.uiprocess.DefaultProcessDetailComparator;
 import org.eclipse.riena.internal.ui.ridgets.swt.uiprocess.IProcessDetailComparatorExtension;
 import org.eclipse.riena.internal.ui.ridgets.swt.uiprocess.ProcessDetail;
 import org.eclipse.riena.internal.ui.ridgets.swt.uiprocess.TimerUtil;
 import org.eclipse.riena.ui.core.IDisposable;
 import org.eclipse.riena.ui.core.uiprocess.IProgressVisualizer;
 import org.eclipse.riena.ui.core.uiprocess.ProcessInfo;
 import org.eclipse.riena.ui.core.uiprocess.UIProcess;
 import org.eclipse.riena.ui.ridgets.AbstractRidget;
 import org.eclipse.riena.ui.ridgets.IContextUpdateListener;
 import org.eclipse.riena.ui.ridgets.IStatuslineUIProcessRidget;
 import org.eclipse.riena.ui.ridgets.IVisualContextManager;
 import org.eclipse.riena.ui.ridgets.swt.AbstractSWTRidget;
 import org.eclipse.riena.ui.swt.Statusline;
 import org.eclipse.riena.ui.swt.StatuslineUIProcess;
 import org.eclipse.riena.ui.swt.uiprocess.ProcessState;
 import org.eclipse.riena.ui.swt.uiprocess.ProgressInfoDataObject;
 
 /**
  * Controls the {@link StatuslineUIProcess} (part of {@link Statusline}).
  */
 public class StatuslineUIProcessRidget extends AbstractRidget implements IStatuslineUIProcessRidget {
 
 	private StatuslineUIProcess uiControl;
 
 	// the manager organizing all data
 	private final ProcessDetailManager processManager = new ProcessDetailManager();
 
 	// the task triggering updates for pending processes
 	private TimerTask timedTrigger;
 
 	private IVisualContextManager contextLocator;
 
 	private Display display;
 
 	public StatuslineUIProcessRidget() {
 		Wire.instance(processManager).andStart(Activator.getDefault().getContext());
 		buildTrigger();
 	}
 
 	/**
 	 * creates the update trigger
 	 */
 	private void buildTrigger() {
 		timedTrigger = new TimedTrigger();
 	}
 
 	/**
 	 * @return the data manager
 	 */
 	protected ProcessDetailManager getProcessManager() {
 		return processManager;
 	}
 
 	/**
 	 * a filter for {@link ProcessDetail} instances
 	 */
 	interface IProcessDetailFilter {
 
 		/**
 		 * @param procDetail
 		 *            a {@link ProcessDetail} instance
 		 * @return true if we accept the detail
 		 */
 		boolean accept(ProcessDetail procDetail);
 
 	}
 
 	/**
 	 * manages data of all known {@link UIProcess} instances
 	 */
 	public static class ProcessDetailManager {
 
 		private final DefaultProcessDetailComparator defaultProcessDetailComparator = new DefaultProcessDetailComparator();
 		private final List<ProcessDetail> processDetails = Collections.synchronizedList(new ArrayList<ProcessDetail>());
 		private Comparator<ProcessDetail> processDetailComparator;
 
 		@InjectExtension(min = 0, max = 1)
 		public void update(final IProcessDetailComparatorExtension extension) {
 			if (extension != null) {
 				processDetailComparator = extension.createComparator();
 			}
 		}
 
 		private Comparator<ProcessDetail> getProcessDetailComparator() {
 			return processDetailComparator != null ? processDetailComparator : defaultProcessDetailComparator;
 		}
 
 		void register(final ProcessDetail detail) {
 			processDetails.add(detail);
 			sort();
 		}
 
 		void unregister(final ProcessDetail detail) {
 			processDetails.remove(detail);
 			sort();
 		}
 
 		private void sort() {
 			Collections.sort(processDetails, getProcessDetailComparator());
 		}
 
 		/**
 		 * 
 		 * @return all detail information about running visualizers
 		 */
 		List<ProcessDetail> getProcessDetails() {
 			return Collections.unmodifiableList(processDetails);
 		}
 
 		List<ProgressInfoDataObject> getPidos(final IProcessDetailFilter pidoFilter) {
 			final List<ProgressInfoDataObject> pidos = new ArrayList<ProgressInfoDataObject>();
 			for (final ProcessDetail procDetail : processDetails) {
 				if (pidoFilter.accept(procDetail)) {
 					pidos.add(procDetail.toPido());
 				}
 			}
 			return pidos;
 		}
 
 		/**
 		 * 
 		 * @return the {@link ProcessDetail} of the {@link IProgressVisualizer}
 		 *         currently visualized in the {@link Statusline}
 		 */
 		ProcessDetail getStatuslineRelevant() {
 			if (processDetails.size() > 0) {
 				return getProcessDetails().get(0);
 			}
 			return null;
 		}
 
 		/**
 		 * 
 		 * @see #getStatuslineRelevant() should the visualizer be shown in the
 		 *      {@link Statusline}?
 		 * 
 		 * @param visualizer
 		 * @return true if it should be visualized in the {@link Statusline}
 		 */
 		boolean isRelevantForStatusline(final IProgressVisualizer visualizer) {
 			return visualizer != null && visualizer.equals(getStatuslineRelevant().getVisualizer());
 		}
 
 		/**
 		 * convenience method
 		 * 
 		 * @return the {@link IProgressVisualizer}s of all registerd
 		 *         {@link ProcessDetail}s
 		 */
 		List<IProgressVisualizer> getAlVisualizers() {
 			final List<IProgressVisualizer> visualizers = new ArrayList<IProgressVisualizer>(processDetails.size());
 			for (final ProcessDetail pDetail : processDetails) {
 				visualizers.add(pDetail.getVisualizer());
 			}
 			return visualizers;
 		}
 
 		/**
 		 * @param visualizer
 		 * @return the {@link ProcessDetail} for the given
 		 *         {@link IProgressVisualizer}
 		 */
 		ProcessDetail detailForVisualizer(final IProgressVisualizer visualizer) {
 			if (visualizer == null) {
 				return null;
 			}
 			for (final ProcessDetail pDetail : processDetails) {
 				if (visualizer.equals(pDetail.getVisualizer())) {
 					return pDetail;
 				}
 			}
 			// nothing found :-/
 			return null;
 		}
 
 		/**
 		 * 
 		 * @return a list containing all <strong>pending</strong>
 		 *         {@link ProcessDetail} instances.
 		 */
 		List<ProcessDetail> getPending() {
 			final List<ProcessDetail> pending = new ArrayList<ProcessDetail>();
 			for (final ProcessDetail pDetail : processDetails) {
 				if (pDetail.isPending()) {
 					pending.add(pDetail);
 				}
 			}
 			return pending;
 		}
 
 		void updatePending() {
 			// be carful: we are not on the ui thread! sync!
 			for (final ProcessDetail pendingDetail : getPending()) {
 				// force a step
 				pendingDetail.triggerPending();
 			}
 		}
 
 		/**
 		 * updates the {@link ProcessDetail} holding the visualizer.
 		 * 
 		 * @param visualizer
 		 *            the visualizer which is progressed
 		 * @param progress
 		 *            the number of work units done by the {@link UIProcess}
 		 */
 		public void saveProgress(final IProgressVisualizer visualizer, final int progress) {
 			// anybody there?
 			final ProcessDetail pDetail = detailForVisualizer(visualizer);
 			Assert.isNotNull(pDetail, "no ProcessDetail for visualizer " + String.valueOf(visualizer)); //$NON-NLS-1$
 			// pending?
 			if (pDetail.isPending()) {
 				// no more!
 				pDetail.setState(ProcessState.RUNNING);
 			}
 
 			int newProgress = 0;
 
 			if (ProcessInfo.ProgresStrategy.UNIT.equals(visualizer.getProcessInfo().getProgresStartegy())) {
 				newProgress = pDetail.getProgress() + progress;
 			} else {
 				newProgress = progress;
 			}
 			pDetail.setProgress(newProgress);
 
 		}
 	}
 
 	/**
 	 * standard ridget code
 	 */
 
 	public StatuslineUIProcess getUIControl() {
 		return uiControl;
 	}
 
 	public void setUIControl(final Object uiControl) {
 		// type check
 		checkUIControl(uiControl);
 		// unbind predecessor
 		unbindUIControl();
 		// real set ..
 		this.uiControl = StatuslineUIProcess.class.cast(uiControl);
 		//bind the control
 		bindUIControl();
 	}
 
 	protected void checkUIControl(final Object uiControl) {
 		AbstractSWTRidget.assertType(uiControl, StatuslineUIProcess.class);
 	}
 
	protected synchronized void bindUIControl() {
 		final StatuslineUIProcess control = getUIControl();
 		if (control != null) {
 			display = control.getDisplay();
 		}
 	}
 
	protected synchronized void unbindUIControl() {
 		display = null;
 	}
 
 	public String getToolTipText() {
 		return ""; //$NON-NLS-1$
 	}
 
 	public boolean hasFocus() {
 		return false;
 	}
 
 	public boolean isFocusable() {
 		return false;
 	}
 
 	public boolean isVisible() {
 		return true;
 	}
 
 	public boolean isEnabled() {
 		return true;
 	}
 
 	public void requestFocus() {
 		// not focusable
 	}
 
 	public void setFocusable(final boolean focusable) {
 		// not focusable
 	}
 
 	public void setToolTipText(final String toolTipText) {
 		// TODO render a tooltip
 
 	}
 
 	public void setVisible(final boolean visible) {
 		// always visible
 	}
 
 	public void setEnabled(final boolean enabled) {
 		// always enabled
 	}
 
 	public String getID() {
 		return null;
 	}
 
 	/**
 	 * {@link UIProcess} specific code
 	 */

 	public synchronized void updateProgress(final IProgressVisualizer visualizer, final int progress) {
 		// save progress
 		getProcessManager().saveProgress(visualizer, progress);
 		checkTrigger();
 		//update user interface
 		updateUserInterface();
 	}
 
 	private void checkTrigger() {
 		if (getProcessManager().getPending().size() == 0) {
 			TimerUtil.stop(timedTrigger);
 			// we need a new instance to be able to restart the task later 
 			buildTrigger();
 		}
 
 	}
 
 	/**
 	 * updates the user interface taking care of thread serialization
 	 */
	private synchronized void updateUserInterface() {
 		/*
 		 * Consider the client closing while executing this method. The display
 		 * may be disposed!
 		 */
 		if (display == null || display.isDisposed()) {
 			return;
 		}
 		if (display.getThread().equals(Thread.currentThread())) {
 			// update on the current thread = user interface thread
 			updateBaseAndList();
 			return;
 		}
 		// we are not on the user interface thread
 		display.asyncExec(new Runnable() {
 			public void run() {
 				updateBaseAndList();
 			}
 		});
 	}
 
 	/**
 	 * updates the controls for the statusline base and the list of
 	 * {@link UIProcess}es
 	 */
 	private void updateBaseAndList() {
 		triggerUIBaseUpdate();
 		triggerUIListUpdate();
 	}
 
 	/**
 	 * update the base progress in the {@link Statusline}
 	 */
 	private void triggerUIBaseUpdate() {
 		final ProcessDetail pDetailBase = getProcessManager().getStatuslineRelevant();
 		if (pDetailBase == null) {
 			return;
 		}
 		getUIControl().triggerBaseUpdate(pDetailBase.toPido());
 	}
 
 	/**
 	 * update the list (user interface) containing all {@link UIProcess}es
 	 * excluding base
 	 */
 	private void triggerUIListUpdate() {
 		// determine all infos
 		final List<ProgressInfoDataObject> pidos = getProcessManager().getPidos(new IProcessDetailFilter() {
 
 			public boolean accept(final ProcessDetail procDetail) {
 				// we do not want the list containing the base info
 				return /*
 						 * !getProcessManager().isRelevantForStatusline(procDetail
 						 * .getVisualizer());
 						 */true;
 			}
 
 		});
 
 		// tell the ui
 		getUIControl().triggerListUpdate(pidos, true);
 	}
 
 	public synchronized void finalUpdateUI(final IProgressVisualizer visualizer) {
 		final ProcessDetail detailForVisualizer = getProcessManager().detailForVisualizer(visualizer);
 		if (detailForVisualizer == null) {
 			return;
 		}
 		detailForVisualizer.setState(visualizer.getProcessInfo().isCanceled() ? ProcessState.CANCELED
 				: ProcessState.FINISHED);
 		updateUserInterface();
 		checkTrigger();
 		checkStillNeeded(visualizer);
 
 	}
 
 	private boolean checkStillNeeded(final IProgressVisualizer visualizer) {
 
 		// if context is disposed, unregister contextUpdateListener
 		final Object context = visualizer.getProcessInfo().getContext();
 
 		if (context instanceof IDisposable && ((IDisposable) context).isDisposed()) {
 			getProcessManager().unregister(getProcessManager().detailForVisualizer(visualizer));
 			unregisterContextUpdateListener(visualizer, false);
 			return false;
 		}
 
 		final List<Object> contexts = new ArrayList<Object>(1);
 		contexts.add(context);
 		final ProcessDetail detail = getProcessManager().detailForVisualizer(visualizer);
 		if (detail != null
 				&& (detail.getState() == ProcessState.FINISHED || detail.getState() == ProcessState.CANCELED)
 				&& contextLocator.getActiveContexts(contexts).size() == 1) {
 			getProcessManager().unregister(getProcessManager().detailForVisualizer(visualizer));
 			unregisterContextUpdateListener(visualizer, false);
 			return false;
 		}
 
 		return true;
 	}
 
 	private void unregisterContextUpdateListener(final IProgressVisualizer visualizer,
 			final boolean removeFromContextLocator) {
 		final IContextUpdateListener contextListener = visualizer2ContextListener.get(visualizer);
 		if (removeFromContextLocator) {
 			contextLocator.removeContextUpdateListener(contextListener, visualizer.getProcessInfo().getContext());
 		}
 		// clean memory
 		visualizer2ContextListener.remove(visualizer);
 	}
 
 	public void setContextLocator(final IVisualContextManager contextLocator) {
 		this.contextLocator = contextLocator;
 	}
 
 	public void addProgressVisualizer(final IProgressVisualizer visualizer) {
 
 	}
 
 	public synchronized void initialUpdateUI(final IProgressVisualizer visualizer, final int totalWork) {
 		createAndRegisterProcessDetail(visualizer);
 		// register timed updater if first time called
 		if (getProcessManager().getPending().size() == 1) {
 			TimerUtil.schedule(timedTrigger, 0, 200);
 		}
 	}
 
 	class TimedTrigger extends TimerTask {
 
 		@Override
 		public void run() {
 			triggerTimedUpdate();
 		}
 
 	}
 
 	private void createAndRegisterProcessDetail(final IProgressVisualizer visualizer) {
 		if (getProcessManager().detailForVisualizer(visualizer) != null) {
 			return;
 		}
 		getProcessManager().register(new ProcessDetail(timeStamp(), visualizer));
 		observeContext(visualizer);
 	}
 
 	private void observeContext(final IProgressVisualizer visualizer) {
 		final IContextUpdateListener listener = new IContextUpdateListener() {
 
 			public void beforeContextUpdate(final Object context) {
 			}
 
 			public boolean contextUpdated(final Object context) {
 				final boolean needed = checkStillNeeded(visualizer);
 				updateUserInterface();
 				return !needed;
 			}
 
 		};
 		final Object context = visualizer.getProcessInfo().getContext();
 		contextLocator.addContextUpdateListener(listener, context);
 		saveMapping(visualizer, listener);
 
 	}
 
 	private final Map<IProgressVisualizer, IContextUpdateListener> visualizer2ContextListener = new HashMap<IProgressVisualizer, IContextUpdateListener>();
 
 	private void saveMapping(final IProgressVisualizer visualizer, final IContextUpdateListener listener) {
 		visualizer2ContextListener.put(visualizer, listener);
 	}
 
 	/**
 	 * This method should be called by a timer thread
 	 */
 	public void triggerTimedUpdate() {
 		if (getProcessManager().getPending().size() > 0) {
 			getProcessManager().updatePending();
 			updateUserInterface();
 		}
 	}
 
 	private static long timeStamp() {
 		return System.currentTimeMillis();
 	}
 
 	public void removeProgressVisualizer(final IProgressVisualizer visualizer) {
 
 	}
 
 }
