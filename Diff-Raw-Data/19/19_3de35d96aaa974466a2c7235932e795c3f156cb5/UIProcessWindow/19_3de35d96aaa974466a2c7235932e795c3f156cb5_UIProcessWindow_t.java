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
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.jface.window.ApplicationWindow;
 import org.eclipse.riena.ui.core.uiprocess.UIProcess;
 import org.eclipse.riena.ui.swt.IRienaDialog;
 import org.eclipse.riena.ui.swt.RienaDialogDelegate;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.ProgressBar;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * The window visualizing the progress of an {@link UIProcess}. Have a look at
  * {@link ApplicationWindow} to get more detailed information about window
  * handling.
  */
 public class UIProcessWindow extends ApplicationWindow implements IUIProcessWindow, IRienaDialog {
 
 	private static final int CANCEL_BUTTON_WIDTH = 70;
 	private static final int PROGRESS_BAR_WIDTH = 210;
 
 	private Set<IProcessWindowListener> windowListeners;
 	private ProgressBar progressBar;
 	private Label description;
 	private Label percent;
 	private UIProcessControl progressControl;
 	private RienaDialogDelegate dlgDelegate;
 
 	public UIProcessWindow(Shell parentShell, UIProcessControl progressControl) {
 		super(parentShell);
 		this.progressControl = progressControl;
 		windowListeners = new HashSet<IProcessWindowListener>();
 		dlgDelegate = new RienaDialogDelegate(this);
 	}
 
 	@Override
 	public void create() {
 		dlgDelegate.initDialog();
 		super.create();
 	}
 
 	/**
 	 * do the layouting for {@link FormLayout} for the parent here
 	 */
 	private void createWindowLayout(Composite parent) {
 		FormLayout layout = new FormLayout();
 		layout.marginTop = 10;
 		layout.marginBottom = 10;
 		layout.marginLeft = 20;
 		layout.marginRight = 20;
 		parent.setLayout(layout);
 	}
 
 	/**
 	 * On this place the {@link IUIProcessCanvas} gets layouted.
 	 * 
 	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	protected Control createContents(Composite parent) {
 
 		Control contentsComposite = dlgDelegate.createContents(parent);
 		Composite centerComposite = dlgDelegate.getCenterComposite();
 
 		createWindowLayout(centerComposite);
 
 		FormData formDate = new FormData();
 
 		// description
 		formDate.width = 210;
 		formDate.height = 35;
 
 		description = new Label(centerComposite, SWT.NONE);
 		description.setBackground(centerComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
 		description.setLayoutData(formDate);
 
 		// percent
 		formDate = new FormData();
 
 		formDate.width = 30;
 		formDate.height = 13;
 		formDate.top = new FormAttachment(description, 5);
 		percent = new Label(centerComposite, SWT.NONE);
 		percent.setBackground(centerComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
 		percent.setLayoutData(formDate);
 		// progressBar
 		formDate = new FormData();
 		formDate.top = new FormAttachment(percent, 10);
 		formDate.width = PROGRESS_BAR_WIDTH;
 		formDate.height = 15;
 
 		progressBar = new ProgressBar(centerComposite, SWT.HORIZONTAL);
 		progressBar.setMinimum(0);
 		progressBar.setMaximum(100);
 		progressBar.setLayoutData(formDate);
 		centerComposite.setBackground(centerComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
 
 		// cancel
 		Button cancel = new Button(centerComposite, SWT.NONE);
 		cancel.addSelectionListener(new SelectionListener() {
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				progressControl.fireCanceled(false);
 
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				progressControl.fireCanceled(false);
 			}
 
 		});
 		cancel.setText("cancel"); //$NON-NLS-1$
 		formDate = new FormData();
 		formDate.top = new FormAttachment(progressBar, 10);
 		formDate.width = CANCEL_BUTTON_WIDTH;
 		formDate.left = new FormAttachment(0,
 				(int) ((double) PROGRESS_BAR_WIDTH / 2 - (double) CANCEL_BUTTON_WIDTH / 2));
 		cancel.setLayoutData(formDate);
 
 		return contentsComposite;
 	}
 
 	public Label getPercent() {
 		return percent;
 	}
 
 	public Label getDescription() {
 		return description;
 	}
 
	public void setDescription(String description) {
 		getDescription().setText(description);
 	}
 
 	public void closeWindow() {
 		close();
 	}
 
 	@Override
 	public boolean close() {
 		fireWindowAboutToClose();
 		dlgDelegate.removeDialogTitleBarMouseListener();
 		return super.close();
 	}
 
 	public void openWindow() {
 		open();
 	}
 
 	@Override
 	public int getShellStyle() {
 		return super.getShellStyle();
 	}
 
 	@Override
 	public void setShellStyle(int newShellStyle) {
 		super.setShellStyle(newShellStyle);
 	}
 
 	public void addProcessWindowListener(IProcessWindowListener listener) {
 		windowListeners.add(listener);
 	}
 
 	protected void fireWindowAboutToClose() {
 		for (IProcessWindowListener listener : windowListeners) {
 			listener.windowAboutToClose();
 		}
 	}
 
 	public ProgressBar getProgressBar() {
 		return progressBar;
 	}
 
 	/**
 	 * This method does nothing, because this window has no menu, not cool or
 	 * tool bar and no status line.
 	 * 
 	 * @see org.eclipse.jface.window.ApplicationWindow#createTrimWidgets(org.eclipse.swt.widgets.Shell)
 	 */
 	@Override
 	protected void createTrimWidgets(Shell shell) {
 		// do nothing
 	}
 
 	public void setHideOsBorder(boolean hideOsBorder) {
 		dlgDelegate.setHideOsBorder(hideOsBorder);
 	}
 
 	public boolean isHideOsBorder() {
 		return dlgDelegate.isHideOsBorder();
 	}
 
 	public boolean isCloseable() {
 		return true;
 	}
 
 	public boolean isMaximizeable() {
 		return false;
 	}
 
 	public boolean isMinimizeable() {
 		return false;
 	}
 
 	public boolean isResizeable() {
 		return false;
 	}
 
 	public boolean isApplicationModal() {
 		return true;
 	}
 
 }
