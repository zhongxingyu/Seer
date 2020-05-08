 /*******************************************************************************
  * Copyright (c) 2007, 2013 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt.uiprocess;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 
 import org.eclipse.riena.ui.core.uiprocess.IProgressVisualizer;
 import org.eclipse.riena.ui.core.uiprocess.ProcessInfo;
 import org.eclipse.riena.ui.ridgets.AbstractRidget;
 import org.eclipse.riena.ui.ridgets.IContextUpdateListener;
 import org.eclipse.riena.ui.ridgets.IUIProcessRidget;
 import org.eclipse.riena.ui.ridgets.IVisualContextManager;
 import org.eclipse.riena.ui.ridgets.swt.AbstractSWTRidget;
 import org.eclipse.riena.ui.ridgets.uibinding.IBindingPropertyLocator;
 import org.eclipse.riena.ui.swt.uiprocess.ICancelListener;
 import org.eclipse.riena.ui.swt.uiprocess.UIProcessControl;
 import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;
 
 /**
  * The {@link UIProcessRidget} is not a standard {@link AbstractSWTRidget} as it
  * does not bind a {@link Control} but a {@link UIProcessControl}. Another
  * difference is that it does not hold any detail state of the uiProcessControl.
  */
 public class UIProcessRidget extends AbstractRidget implements IUIProcessRidget {
 
 	private UIProcessControl uiProcessControl;
 	private final CancelListener cancelListener;
 	private final Map<IProgressVisualizer, Progress> visualizerProgress;
 	private final Map<Object, VisualizerContainer> contexts;
 	private IVisualContextManager contextLocator;
 	private boolean focusAble;
 
 	public UIProcessRidget() {
 		cancelListener = new CancelListener();
 		visualizerProgress = new HashMap<IProgressVisualizer, Progress>();
 		contexts = new HashMap<Object, VisualizerContainer>();
 	}
 
 	static class ContextDataComparator implements Comparator<VisualizerContainer> {
 
 		public int compare(final VisualizerContainer o1, final VisualizerContainer o2) {
 			final int time1 = getVisualizerTime(o1);
 			final int time2 = getVisualizerTime(o2);
 			if (time1 > time2) {
 				return -1;
 			}
 			if (time1 == time2) {
 				return 0;
 			}
 			return 1;
 		}
 
 		private int getVisualizerTime(final VisualizerContainer data) {
 			return data.get(data.getCurrentVisualizer());
 		}
 
 	}
 
 	//get the container of the active context if exists
 	private VisualizerContainer getActiveContextContainer() {
 		final List<VisualizerContainer> data = getActiveContextContainerList();
 		Collections.sort(data, new ContextDataComparator());
 		if (data.size() > 0) {
 			return data.get(0);
 		}
 		return null;
 	}
 
 	// get the list of active contexts
 	private List<VisualizerContainer> getActiveContextContainerList() {
 		final List<Object> activeContexts = getActiveContexts();
 		final List<VisualizerContainer> data = new ArrayList<VisualizerContainer>();
 		for (final Object object : activeContexts) {
 			data.add(contexts.get(object));
 		}
 		return data;
 	}
 
 	private List<Object> getActiveContexts() {
 		return getContextLocator().getActiveContexts(new LinkedList<Object>(contexts.keySet()));
 	}
 
 	/*
 	 * holds the progress of a visualized UiProcess
 	 */
 	private final static class Progress {
 		private int totalWork = -1;
 		private int completed = 0;
 
 		private Progress() {
 			super();
 		}
 	}
 
 	private void showProcessing() {
 		getUIControl().showProcessing();
 	}
 
 	/**
 	 * open the window controlled by the ridget
 	 */
 	public void open() {
 		getUIControl().start();
 		updateUi();
 	}
 
 	/**
 	 * close the window controlled by the ridget
 	 */
 	public void close() {
 		getUIControl().stop();
 	}
 
 	private class CancelListener implements ICancelListener {
 
 		public void canceled(final boolean windowClosed) {
 			if (getCurrentVisualizer() != null) {
 				cancelCurrentVisualizer(windowClosed);
 			}
 		}
 
 		private void cancelCurrentVisualizer(final boolean windowClosed) {
 			if (windowClosed) {
 				cancelAllVisualizersInContext();
 			} else {
 				getCurrentProcessInfo().cancel();
 			}
 			if (isLonelyVisualizer(getCurrentVisualizer()) && !windowClosed) {
 				close();
 			}
 			removeProgressVisualizer(getCurrentVisualizer());
 		}
 
 		private void cancelAllVisualizersInContext() {
 			// clean up
 			final List<VisualizerContainer> activeContextDataList = getActiveContextContainerList();
 			for (final VisualizerContainer visualizerContextData : activeContextDataList) {
 				for (final IProgressVisualizer visualizer : visualizerContextData.keySet()) {
 					visualizer.getProcessInfo().cancel();
 				}
 			}
 		}
 
 	}
 
 	protected void bindUIControl() {
 		if (getUIControl() != null) {
 			uiProcessControl.addCancelListener(cancelListener);
 		}
 	}
 
 	public void setUIControl(final Object uiControl) {
 		checkUIControl(uiControl);
 		unbindUIControl();
 		uiProcessControl = (UIProcessControl) uiControl;
 		bindUIControl();
 	}
 
 	protected void checkUIControl(final Object uiControl) {
 		checkType(uiControl, UIProcessControl.class);
 
 	}
 
 	protected void unbindUIControl() {
 		if (getUIControl() != null) {
 			getUIControl().removeCancelListener(cancelListener);
 		}
 	}
 
 	public void addProgressVisualizer(final IProgressVisualizer visualizer) {
 		final Object context = visualizer.getProcessInfo().getContext();
 		// is there any contextData for this context? (any other visualizers for
 		// the same context?)
 		VisualizerContainer contextData = contexts.get(context);
 		if (contextData == null) {
 			// create Container for context to hold all visualizers for the
 			// context
 			contextData = new VisualizerContainer();
 			contexts.put(context, contextData);
 			contextLocator.addContextUpdateListener(contextChangeHandler, context);
 		}
 		// save when the visualizers was started
 		saveVisualizerStartupTime(visualizer, contextData);
 		// create the Progress Object to save progress and total work for the
 		// visualizer
 		createVisualizerProgress(visualizer);
 		// observe processInfo changes(description, title, ..)
 		observeProcessInfo(visualizer.getProcessInfo());
 	}
 
 	private final ContextChangeHandler contextChangeHandler = new ContextChangeHandler();
 
 	// saves the bounds of the window whenever the execution context changes
 	private class ContextChangeHandler implements IContextUpdateListener {
 
 		public boolean contextUpdated(final Object context) {
 			checkContexts();
 			return false;
 
 		}
 
 		public void beforeContextUpdate(final Object context) {
 			// save the bounds in all context parts
 			final List<Object> activeContexts = getActiveContexts();
 			for (final Object subContext : activeContexts) {
 				saveBounds(subContext);
 			}
 
 		}
 
 	}
 
 	private void checkContexts() {
 		if (getActiveContexts().size() == 0) {
 			close();
 		} else {
 
 			final IProgressVisualizer currentVisualizer = getCurrentVisualizer();
 
 			// if this is a user-job, show the ProgressWindow 
 			if (currentVisualizer != null && currentVisualizer.getProcessInfo().isDialogVisible()) {
 				open();
 				Object currentContext = null;
 				for (final Entry<Object, VisualizerContainer> entry : contexts.entrySet()) {
 					final VisualizerContainer container = entry.getValue();
 					if (container != null && container.getCurrentVisualizer() == currentVisualizer) {
 						currentContext = entry.getKey();
 						break;
 					}
 				}
 				if (currentContext != null) {
 					getUIControl().getWindow().getShell().setBounds(contexts.get(currentContext).getBounds());
 				}
 				final int progress = getProgress(currentVisualizer).completed;
 				if (progress <= 0) {
 					showProcessing();
 				}
 			} else {
 				showProcessing();
 			}
 		}
 	}
 
 	/**
 	 * @see IProgressVisualizer#finalUpdateUI()
 	 */
 	public void finalUpdateUI(final IProgressVisualizer visualizer) {
 		if (!visualizer.getProcessInfo().isDialogVisible()) {
 			return;
 		}
 		// if its the only visualizer for the current context: close window
 		if (isActive(visualizer) && isLonelyVisualizer(visualizer)) {
 			visualizer.getProcessInfo().setIgnoreCancel(true);
 			close();
 		}
 
 	}
 
 	// is the visualizer the only one?
 	private boolean isLonelyVisualizer(final IProgressVisualizer visualizer) {
 		final List<VisualizerContainer> activeContextContainerList = getActiveContextContainerList();
 		if (activeContextContainerList.size() == 0) {
 			return true;
 		}
 		int count = 0;
 		for (final VisualizerContainer visualizerContainer : activeContextContainerList) {
 			count += visualizerContainer.size();
 			if (count > 1) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * @see IProgressVisualizer#initialUpdateUI(int)
 	 */
 	public void initialUpdateUI(final IProgressVisualizer visualizer, final int totalWork) {
 		if (!visualizer.getProcessInfo().isDialogVisible()) {
 			return;
 		}
 		if (isActive(visualizer)) {
 			// all this makes sense if the visualizers is part of one of the
 			// active contexts
 			open();
 			showProcessing();
 			saveTotalWork(visualizer, totalWork);
 			getProgress(visualizer).completed = 0;
 			updateUi();
 		}
 	}
 
 	private void createVisualizerProgress(final IProgressVisualizer visualizer) {
 		// for progress data (total & completed work)
 		visualizerProgress.put(visualizer, new Progress());
 	}
 
 	// save the time when tihe visualizer has been first seen 
 	private void saveVisualizerStartupTime(final IProgressVisualizer visualizer, final VisualizerContainer contextData) {
 		final long time = System.currentTimeMillis();
 		contextData.put(visualizer, Integer.valueOf((int) time));
 	}
 
 	private void observeProcessInfo(final ProcessInfo processInfo) {
 		processInfo.addPropertyChangeListener(getProcessInfoListener());
 	}
 
 	private PropertyChangeListener processInfoListener;
 
 	private PropertyChangeListener getProcessInfoListener() {
 		if (processInfoListener == null) {
 			processInfoListener = new ProcessInfoListener();
 		}
 		return processInfoListener;
 	}
 
 	/*
 	 * observes the processInfo of a visualizer
 	 */
 	private class ProcessInfoListener implements PropertyChangeListener {
 
 		public void propertyChange(final PropertyChangeEvent evt) {
 			final ProcessInfo pInfo = ProcessInfo.class.cast(evt.getSource());
 			if (isActive(pInfo) && !ProcessInfo.PROPERTY_CANCELED.equals(evt.getPropertyName())) {
 				updateUi();
 			}
 		}
 
 		private boolean isActive(final ProcessInfo info) {
 			return getCurrentVisualizer() != null && getCurrentVisualizer().getProcessInfo().equals(info);
 		}
 
 	}
 
 	/*
 	 * the current visualizer is part of the list of active contexts at the
 	 * specific point in time. the current visualizer is the last one added to
 	 * the merged list of visualizers in all contexts.
 	 */
 	protected IProgressVisualizer getCurrentVisualizer() {
 		final VisualizerContainer activeContextContainer = getActiveContextContainer();
 		if (activeContextContainer != null) {
 			return activeContextContainer.getCurrentVisualizer();
 		}
 		return null;
 	}
 
 	/*
 	 * update ui but take care of disposed widgets!
 	 */
 	private void updateUi() {
 		final Shell windowShell = getWindowShell();
 		if (windowShell != null) {
 			windowShell.getDisplay().syncExec(new Runnable() {
 
 				public void run() {
 					final Shell shell = getWindowShell();
 					if (shell != null && !shell.isDisposed()) {
 						getUIControl().setDescription(getCurrentProcessInfo().getNote());
 						getUIControl().setTitle(getCurrentProcessInfo().getTitle());
 						getUIControl().setCancelVisible(getCurrentProcessInfo().isCancelVisible());
 						getUIControl().setCancelEnabled(getCurrentProcessInfo().isCancelEnabled());
 						getUIControl().pack();
 						// show the progress
 						reinitializeProgress();
 
 					}
 				}
 
 				private void reinitializeProgress() {
 					final int progress = visualizerProgress.get(getCurrentVisualizer()).completed;
 					if (progress <= -1) {
 						showProcessing();
 					}
 				}
 			});
 		}
 	}
 
 	private Shell getWindowShell() {
 		final Shell shell = getUIControl().getWindow().getShell();
 		return shell;
 	}
 
 	// get the info for the current visualizer
 	private ProcessInfo getCurrentProcessInfo() {
 		return getCurrentVisualizer().getProcessInfo();
 	}
 
 	private void saveTotalWork(final IProgressVisualizer visualizer, final int totalWork) {
 		this.visualizerProgress.get(visualizer).totalWork = totalWork;
 	}
 
 	/**
 	 * cleanly remove the visualizer from the its container and update user
 	 * interface
 	 */
 	public void removeProgressVisualizer(final IProgressVisualizer visualizer) {
 		removeVisualizerFromContextData(visualizer);
 		removeVisualizerProgress(visualizer);
 		cleanContext();
 		if (getCurrentVisualizer() != null) {
 			updateUi();
 		}
 		//remove context listener to avoid memory leaks 
 		final Object context = visualizer.getProcessInfo().getContext();
 		contextLocator.removeContextUpdateListener(contextChangeHandler, context);
 	}
 
 	private void cleanContext() {
 		final Iterator<VisualizerContainer> contextIter = contexts.values().iterator();
 		while (contextIter.hasNext()) {
 			final VisualizerContainer container = contextIter.next();
 			if (container != null && container.size() == 0) {
 				contextIter.remove();
 			}
 		}
 	}
 
 	private void removeVisualizerProgress(final IProgressVisualizer visualizer) {
 		visualizerProgress.remove(visualizer);
 	}
 
 	private void removeVisualizerFromContextData(final IProgressVisualizer visualizer) {
 		final Collection<VisualizerContainer> contextDataKeys = contexts.values();
 		for (final VisualizerContainer contextData : contextDataKeys) {
 			contextData.remove(visualizer);
 		}
 	}
 
 	protected boolean isActive(final IProgressVisualizer visualizer) {
 		return visualizer != null && visualizer == getCurrentVisualizer();
 	}
 
 	/**
 	 * @see IProgressVisualizer#updateProgress(int)
 	 */
 	public void updateProgress(final IProgressVisualizer visualizer, final int progress) {
 		if (!visualizer.getProcessInfo().isDialogVisible()) {
 			return;
 		}
 		saveProgress(visualizer, progress);
 		if (isActive(visualizer)) {
 			getUIControl().showProgress(getProgress(visualizer).completed, getTotalWork(visualizer));
 		}
 	}
 
 	// cache the progress of a visualizer
 	private void saveProgress(final IProgressVisualizer visualizer, final int progressValue) {
 		final Progress progress = getProgress(visualizer);
 		if (progress != null) {
 
 			if (ProcessInfo.ProgresStrategy.UNIT.equals(visualizer.getProcessInfo().getProgresStartegy())) {
 				progress.completed += progressValue;
 			} else {
 				progress.completed = progressValue;
 			}
 		}
 	}
 
 	private Progress getProgress(final IProgressVisualizer visualizer) {
 		return visualizerProgress.get(visualizer);
 	}
 
 	private Integer getTotalWork(final IProgressVisualizer visualizer) {
 		return getProgress(visualizer).totalWork;
 	}
 
 	private void saveBounds(final Object visualContext) {
 		if (visualContext != null) {
 			final Shell shell = getUIControl().getWindow().getShell();
 			if (shell != null) {
 				contexts.get(visualContext).setBounds(shell.getBounds());
 			}
 		}
 	}
 
 	public void setContextLocator(final IVisualContextManager contextLocator) {
 		this.contextLocator = contextLocator;
 	}
 
 	public IVisualContextManager getContextLocator() {
 		return contextLocator;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.ui.ridgets.IRidget#getToolTipText()
 	 */
 	public String getToolTipText() {
 		if (getWindowShell() != null && !getWindowShell().isDisposed() && isFocusable()) {
 			return getWindowShell().getToolTipText();
 		}
 		return ""; //$NON-NLS-1$
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.ui.ridgets.IRidget#getUIControl()
 	 */
 	public UIProcessControl getUIControl() {
 		return uiProcessControl;
 	}
 
 	/**
 	 * answers true if the dialog window has the focus. otherwise false
 	 */
 
 	public boolean hasFocus() {
 		if (getWindowShell() != null && !getWindowShell().isDisposed() && isFocusable()) {
 			return getWindowShell().isFocusControl();
 		}
 		return false;
 	}
 
 	public boolean isFocusable() {
 		return focusAble;
 	}
 
 	public void setFocusable(final boolean focusable) {
 		this.focusAble = focusable;
 	}
 
 	/**
 	 * answers true if the dialog window is visible. otherwise false
 	 */
 
 	public boolean isVisible() {
 		if (getWindowShell() != null && !getWindowShell().isDisposed()) {
 			return getWindowShell().isVisible();
 		}
 		return false;
 	}
 
 	/**
 	 * answers true if the dialog window is enabled. otherwise false
 	 */
 	public boolean isEnabled() {
 		if (getWindowShell() != null && !getWindowShell().isDisposed()) {
 			return getWindowShell().isEnabled();
 		}
 		return false;
 	}
 
 	/**
 	 * request focus of window
 	 */
 	public void requestFocus() {
 		if (getWindowShell() != null && !getWindowShell().isDisposed() && isFocusable()) {
 			getWindowShell().forceFocus();
 		}
 	}
 
 	public void setToolTipText(final String toolTipText) {
 		if (getWindowShell() != null && !getWindowShell().isDisposed()) {
 			getWindowShell().setToolTipText(toolTipText);
 		}
 	}
 
 	/**
 	 * controls the visibility of the dialog
 	 */
 	public void setVisible(final boolean visible) {
 		if (visible) {
 			open();
 		} else {
 			close();
 		}
 	}
 
 	/**
 	 * controls the enabled state of the managed dialog window
 	 */
 	public void setEnabled(final boolean enabled) {
 		if (getWindowShell() != null && !getWindowShell().isDisposed()) {
 			getWindowShell().setEnabled(enabled);
 		}
 	}
 
 	public String getID() {
 		final IBindingPropertyLocator locator = SWTBindingPropertyLocator.getInstance();
 		return locator.locateBindingProperty(getUIControl());
 	}
 
 }
