 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.uiprocess;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.window.Window;
 import org.eclipse.riena.core.util.ListenerList;
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.ui.core.uiprocess.UIProcess;
 import org.eclipse.riena.ui.swt.utils.IPropertyNameProvider;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.ProgressBar;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * Control for showing a progress or process info window.
  */
 public class UIProcessControl implements IProgressControl, IPropertyNameProvider {
 
 	private static final int UPDATE_DELAY = 200;
 
 	private boolean processing;
 
 	// the jface window
 	private UIProcessWindow processWindow;
 
 	private ListenerList<ICancelListener> cancelListeners = new ListenerList<ICancelListener>(ICancelListener.class);
 
 	private ProcessUpdateThread processUpdateThread;
 
 	private String name;
 
 	public UIProcessControl(Shell parentShell) {
 		Assert.isNotNull(parentShell);
 		createProcessWindow(parentShell);
 	}
 
 	private void createProcessWindow(Shell parentShell) {
 		// create window
 		processWindow = new UIProcessWindow(parentShell, this);
 		// observe window
 		processWindow.addProcessWindowListener(new IProcessWindowListener() {
 
 			public void windowAboutToClose() {
 				stopProcessing();
 				fireCanceled(true);
 			}
 
 		});
 	}
 
 	/**
 	 * @return - the {@link Window}
 	 */
 	public Window getWindow() {
 		return processWindow;
 
 	}
 
 	/**
 	 * open the Jface Window
 	 */
 	private void showWindow() {
 		processWindow.openWindow();
 	}
 
 	/**
 	 * close the window
 	 */
 	public void stop() {
 		closeWindow();
 	}
 
 	private void closeWindow() {
 		// stop update thread
 		stopProcessing();
 		// close AppWindow
 		processWindow.closeWindow();
 
 	}
 
 	/**
 	 * 
 	 * @return true if the window is in processing state
 	 */
 	protected synchronized boolean isProcessing() {
 		return processing;
 	}
 
 	public synchronized void setProcessing(boolean processing) {
 		this.processing = processing;
 	}
 
 	/**
 	 * changes the window into processing state
 	 */
 	public void showProcessing() {
 		startProcessing();
 
 	}
 
 	/**
 	 * technical implementation of progressing in the window
 	 */
 	private void startProcessing() {
 		// only if not allready processing
 		if (!isProcessing()) {// use the synched way..
 			setProcessing(true);
 			getProgressBar().setMaximum(90);
 			getPercentLabel().setText(""); //$NON-NLS-1$
 			if (processUpdateThread == null || !processUpdateThread.isAlive()) {
 				startUpdateThread();
 			}
 		}
 	}
 
 	/*
 	 * starts the update thread needed in conjunction with the progressing state
 	 * to visualize some activity
 	 */
 	private void startUpdateThread() {
 		processUpdateThread = new ProcessUpdateThread();
 		processUpdateThread.start();
 	}
 
 	/**
 	 * This Thread is used to visualize "progressing"
 	 */
 	private final class ProcessUpdateThread extends Thread {
 
 		@Override
 		public void run() {
 			setPriority(Thread.MAX_PRIORITY);
 			setName("ProcessUpdateThread"); //$NON-NLS-1$
 			processUpdateLoop();
 		}
 	}
 
 	/*
 	 * called by a worker thread used to visualize progressing activity
 	 */
 	private void processUpdateLoop() {
 		final int[] selection = new int[] { 0 };
 
 		while (isProcessing()) {
 			if (selection[0] <= 100) {
 				selection[0] += 10;
 			} else {
 				selection[0] = 0;
 			}
 			if (!getProgressBar().isDisposed()) {
 				getProgressBar().getDisplay().asyncExec(new Runnable() {
 
 					public void run() {
 						if (!getProgressBar().isDisposed()) {
 							if (isProcessing()) {
 								getProgressBar().setSelection(selection[0]);
 							}
 						}
 					}
 				});
 			}
 
 			try {
 				Thread.sleep(UPDATE_DELAY);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/*
 	 * returns the progressbar of the window
 	 */
 	private ProgressBar getProgressBar() {
 		return processWindow.getProgressBar();
 	}
 
 	/**
 	 * visualizes a specific progress of the {@link UIProcess}
 	 */
 	public void showProgress(int value, int maxValue) {
 		stopProcessing();
 		int percentValue = calcSelection(value, maxValue);
 		if (getWindow().getShell() != null && !getWindow().getShell().isDisposed()) {
 			getPercentLabel().setText(String.valueOf(percentValue) + " %"); //$NON-NLS-1$
 			getProgressBar().setSelection(percentValue);
 		}
 	}
 
 	private Label getPercentLabel() {
 		return processWindow.getPercent();
 	}
 
 	private int calcSelection(int value, int maxValue) {
 		double v = (double) value / (double) maxValue * 100;
 		return (int) v;
 	}
 
 	private void stopProcessing() {
 		setProcessing(false);
 	}
 
 	public void start() {
 		// open the ui
 		showWindow();
 	}
 
 	public void setDescription(String text) {
		processWindow.setDescrition(text);
 	}
 
 	public void setTitle(String text) {
 		Shell shell = processWindow.getShell();
 		if (!StringUtils.equals(text, shell.getText())) {
 			shell.setText(text);
 			Rectangle bounds = shell.getBounds();
 			shell.redraw(0, 0, bounds.width, bounds.height, true);
 		}
 	}
 
 	/**
 	 * add an {@link ICancelListener} to be notified about cancel events.
 	 */
 	public void addCancelListener(ICancelListener listener) {
 		cancelListeners.add(listener);
 	}
 
 	public void removeCancelListener(ICancelListener listener) {
 		cancelListeners.remove(listener);
 	}
 
 	/**
 	 * notify listener
 	 * 
 	 * @param windowClosing
 	 */
 	protected void fireCanceled(boolean windowClosing) {
 		for (ICancelListener listener : cancelListeners.getListeners()) {
 			listener.canceled(windowClosing);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.riena.ui.swt.utils.IPropertyNameProvider#getPropertyName()
 	 */
 	public String getPropertyName() {
 		return name;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.riena.ui.swt.utils.IPropertyNameProvider#setPropertyName(
 	 * java.lang.String)
 	 */
 	public void setPropertyName(String propertyName) {
 		this.name = propertyName;
 	}
 }
