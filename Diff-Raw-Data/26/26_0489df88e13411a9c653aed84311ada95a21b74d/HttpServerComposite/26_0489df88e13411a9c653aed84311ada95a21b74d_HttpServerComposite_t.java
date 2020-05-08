 /*******************************************************************************
  * Copyright (c) 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.server.http.ui.internal;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.wst.server.core.IServerWorkingCopy;
 import org.eclipse.wst.server.http.core.internal.HttpServer;
 import org.eclipse.wst.server.ui.wizard.IWizardHandle;
 /**
  * Wizard page to set the server properties.
  */
 public class HttpServerComposite extends Composite {
 	protected IServerWorkingCopy serverWC;
 	protected HttpServer server;
 	protected Text prefix;
 	protected Spinner port;
 	protected Button publishCheckBox;
 	protected IWizardHandle wizard;
 
 	/**
 	 * HttpServerComposite
 	 * 
 	 * @param parent the parent composite
 	 * @param wizard the wizard handle
 	 */
 	public HttpServerComposite(Composite parent, IWizardHandle wizard) {
 		super(parent, SWT.NONE);
 		this.wizard = wizard;
 		wizard.setTitle(Messages.wizardTitle);
 		wizard.setDescription(Messages.wizardDescription);
 		wizard.setImageDescriptor(HttpUIPlugin.getImageDescriptor(HttpUIPlugin.IMG_WIZ_SERVER));
 
 		createControl();
 	}
 
 	protected void setServer(IServerWorkingCopy newServer) {
 		if (newServer == null) {
 			serverWC = null;
 			server = null;
 		} else {
 			serverWC = newServer;
 			server = (HttpServer) newServer.loadAdapter(HttpServer.class, null);
 		}
 		
		if (!isDisposed())
			init();
 		validate();
 	}
 
 	/**
 	 * Provide a wizard page to change the Apache installation directory.
 	 */
 	protected void createControl() {
 		GridLayout layout = new GridLayout();
 		setLayout(layout);
 		setLayoutData(new GridData(GridData.FILL_BOTH));
 		
 		Composite comp = new Composite(this, SWT.NONE);
 		layout = new GridLayout();
 		layout.numColumns = 2;
 		comp.setLayout(layout);
 		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ContextIds.RUNTIME_COMPOSITE);
 		
 		createServerInfoGroup(comp);
 		
 		Font font = comp.getFont();
 		publishCheckBox = new Button(comp, SWT.CHECK);
 		publishCheckBox.setText(Messages.shouldPublish);
 		publishCheckBox.setFont(font);
 		GridData data = new GridData(GridData.FILL_HORIZONTAL);
 		data.horizontalSpan = 2;
 		publishCheckBox.setLayoutData(data);
 		
 		publishCheckBox.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent se) {
 				Button b = (Button) se.getSource();
 				server.setPublishing(b.getSelection());
 				validate();
 			}
 		});
 		
 		init();
 		validate();
 		
 		Dialog.applyDialogFont(this);
 		
 		port.forceFocus();
 	}
 
 	protected void init() {
 		if (port == null || serverWC == null || server == null)
 			return;
 		
 		port.setSelection(server.getPort());
 		prefix.setText(server.getURLPrefix());
 		
 		boolean canPublish = server.isPublishing();
 		publishCheckBox.setSelection(canPublish);
 	}
 
 	protected void validate() {
 		/*if (server == null) {
 			wizard.setMessage("", IMessageProvider.ERROR);
 			return;
 		}
 		
 		wizard.setMessage(null, IMessageProvider.NONE);*/
 	}
 
 	private void createServerInfoGroup(Composite parent) {
 		Font font = parent.getFont();
 		
 		// port label
 		Label portLabel = new Label(parent, SWT.NONE);
 		portLabel.setFont(font);
 		portLabel.setText(Messages.port);
 		
 		// port entry field
 		port = new Spinner(parent, SWT.BORDER);
 		port.setMinimum(0);
 		port.setMaximum(999999);
 		GridData data = new GridData(GridData.FILL_HORIZONTAL);
 		data.widthHint = 305;
 		port.setLayoutData(data);
 		port.setFont(font);
 		port.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				if (server != null)
 					try {
 						server.setPort(port.getSelection());
 					} catch (Exception ex) {
 						// ignore
 					}
 				validate();
 			}
 		});
 		
 		// prefix label
 		Label prefixLabel = new Label(parent, SWT.NONE);
 		prefixLabel.setFont(font);
 		prefixLabel.setText(Messages.URLPrefix);
 		
 		// prefix entry field
 		prefix = new Text(parent, SWT.BORDER);
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		data.widthHint = 305;
 		prefix.setLayoutData(data);
 		prefix.setFont(font);
 		prefix.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				server.setURLPrefix(prefix.getText());
 				validate();
 			}
 		});
 	}
 }
