 /*******************************************************************************
  * Copyright (c) 2003, 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.server.ui.internal.wizard.page;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.eclipse.ui.help.IWorkbenchHelpSystem;
 import org.eclipse.wst.server.core.*;
 import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
 import org.eclipse.wst.server.core.util.SocketUtil;
 import org.eclipse.wst.server.ui.internal.*;
 import org.eclipse.wst.server.ui.internal.viewers.ServerTypeComposite;
 import org.eclipse.wst.server.ui.internal.wizard.TaskWizard;
 import org.eclipse.wst.server.ui.internal.wizard.WizardTaskUtil;
 import org.eclipse.wst.server.ui.wizard.WizardFragment;
 /**
  * Wizard page used to create a server and configuration at the same time.
  */
 public class NewManualServerComposite extends Composite {
 	public interface ServerSelectionListener {
 		public void serverSelected(IServerAttributes server);
 		public void runtimeSelected(IRuntime runtime);
 	}
 
 	public interface IWizardHandle2 {
 		public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InterruptedException, InvocationTargetException;
 		public void update();
 		public void setMessage(String newMessage, int newType);
 	}
 	protected IWizardHandle2 wizard;
 
 	protected ServerTypeComposite serverTypeComposite;
 
 	protected Label runtimeLabel;
 	protected Combo runtimeCombo;
 	protected Link configureRuntimes;
 	protected Link addRuntime;
 	protected IRuntime[] runtimes;
 	protected IRuntime newRuntime;
 
 	protected Text serverName;
 	protected String defaultServerName;
 	protected boolean serverNameModified;
 	protected boolean updatingServerName;
 	protected ToolBarManager serverNameToolBar;
 
 	protected IRuntime runtime;
 	protected IServerWorkingCopy server;
 	protected ServerSelectionListener listener;
 
 	protected String host;
 
 	protected IModuleType moduleType;
 	protected IModule module;
 	protected String serverTypeId;
 	protected boolean includeIncompatible;
 
 	protected ServerCreationCache cache = new ServerCreationCache();
 
 	/**
 	 * Creates a new server and server configuration.  If the initial
 	 * resource selection contains exactly one container resource then it will be
 	 * used as the default container resource.
 	 *
 	 * @param parent a parent composite
 	 * @param wizard a wizard handle
 	 * @param moduleType a module type
 	 * @param module an optional module
 	 * @param serverTypeId a server type id, or null
 	 * @param includeIncompatible true to include incompatible servers that support similar module types
 	 * @param listener a server selection listener
 	 */
 	public NewManualServerComposite(Composite parent, IWizardHandle2 wizard, IModuleType moduleType, IModule module, String serverTypeId, boolean includeIncompatible, ServerSelectionListener listener) {
 		super(parent, SWT.NONE);
 		this.wizard = wizard;
 		this.listener = listener;
 		
 		this.moduleType = moduleType;
 		this.module = module;
 		this.serverTypeId = serverTypeId;
 		this.includeIncompatible = includeIncompatible;
 		
 		createControl();
 		wizard.setMessage("", IMessageProvider.ERROR); //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns this page's initial visual components.
 	 */
 	protected void createControl() {
 		// top level group
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 3;
 		layout.horizontalSpacing = SWTUtil.convertHorizontalDLUsToPixels(this, 4);
 		layout.verticalSpacing = SWTUtil.convertVerticalDLUsToPixels(this, 4);
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		setLayout(layout);
 
 		this.setFont(getParent().getFont());
 		IWorkbenchHelpSystem whs = PlatformUI.getWorkbench().getHelpSystem();
 		whs.setHelp(this, ContextIds.NEW_SERVER_WIZARD);
 		
 		serverTypeComposite = new ServerTypeComposite(this, moduleType, serverTypeId, new ServerTypeComposite.ServerTypeSelectionListener() {
 			public void serverTypeSelected(IServerType type2) {
 				handleTypeSelection(type2);
 				//WizardUtil.defaultSelect(parent, CreateServerWizardPage.this);
 			}
 		});
 		serverTypeComposite.setIncludeIncompatibleVersions(includeIncompatible);
 		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
 		data.horizontalSpan = 3;
 		serverTypeComposite.setLayoutData(data);
 		whs.setHelp(serverTypeComposite, ContextIds.NEW_SERVER_TYPE);
 		
 		Label serverNameLabel = new Label(this, SWT.NONE);
 		serverNameLabel.setText(Messages.serverName);
 		
 		serverName = new Text(this, SWT.SINGLE | SWT.BORDER | SWT.CANCEL);
 		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		if ((serverName.getStyle() & SWT.CANCEL) != 0)
 			data.horizontalSpan = 2;
 		serverName.setLayoutData(data);
 		
 		if (server != null)
 			serverName.setText(server.getName());
 		
 		serverName.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				if (updatingServerName)
 					return;
 				
 				String name = serverName.getText();
 				if (server != null) {
 					server.setName(name);
 					IRuntime runtime2 = server.getRuntime();
 					if (runtime2 != null && runtime2 instanceof IRuntimeWorkingCopy) {
 						IRuntimeWorkingCopy rwc = (IRuntimeWorkingCopy) runtime2;
 						rwc.setName(name);
 					}
 				}
 				
 				if (serverNameModified)
 					return;
 				
 				serverNameModified = true;
 				if (serverNameToolBar != null)
 					serverNameToolBar.getControl().setVisible(true);
 			}
 		});
 		
 		if ((serverName.getStyle() & SWT.CANCEL) == 0) {
 			serverNameToolBar = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL);
 			serverNameToolBar.createControl(this);
 			
 			IAction resetDefaultAction = new Action("", IAction.AS_PUSH_BUTTON) {//$NON-NLS-1$
 				public void run() {
 					ServerUtil.setServerDefaultName(server);
 					serverName.setText(server.getName());
 					serverNameModified = false;
 					if (serverNameToolBar != null)
 						serverNameToolBar.getControl().setVisible(false);
 				}
 			};
 			
 			resetDefaultAction.setToolTipText(Messages.serverNameDefault);
 			resetDefaultAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ETOOL_RESET_DEFAULT));
 			resetDefaultAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DTOOL_RESET_DEFAULT));
 			
 			serverNameToolBar.add(resetDefaultAction);
 			serverNameToolBar.update(false);
 			serverNameToolBar.getControl().setVisible(false);
 		}
 		
 		runtimeLabel = new Label(this, SWT.NONE);
 		runtimeLabel.setText(Messages.wizNewServerRuntime);
 		
 		runtimeCombo = new Combo(this, SWT.READ_ONLY);
 		runtimeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		runtimeCombo.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				try {
 					setRuntime(runtimes[runtimeCombo.getSelectionIndex()]);
 				} catch (Exception ex) {
 					// ignore
 				}
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 		
 		addRuntime = new Link(this, SWT.NONE);
 		addRuntime.setText("<a>" + Messages.addRuntime + "</a>");
 		addRuntime.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
 		addRuntime.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				IServerType serverType = serverTypeComposite.getSelectedServerType();
 				if (showRuntimeWizard(serverType) != Window.CANCEL)
 					updateRuntimeCombo(serverType);
 			}
 		});
 		
 		configureRuntimes = new Link(this, SWT.NONE);
 		configureRuntimes.setText("<a>" + Messages.configureRuntimes + "</a>");
 		data = new GridData(GridData.HORIZONTAL_ALIGN_END);
 		data.horizontalSpan = 3;
 		configureRuntimes.setLayoutData(data);
 		configureRuntimes.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (showPreferencePage()) {
 					runtime = null;
 					IServerType serverType = serverTypeComposite.getSelectedServerType();
 					updateRuntimeCombo(serverType);
 				}
 			}
 		});
 		Dialog.applyDialogFont(this);
 	}
 
 	protected boolean showPreferencePage() {
 		String id = "org.eclipse.wst.server.ui.preferencePage";
 		String id2 = "org.eclipse.wst.server.ui.runtime.preferencePage";
 		final PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), id2, new String[] { id, id2 }, null);
 		return (dialog.open() == Window.OK);
 	}
 
 	protected int showRuntimeWizard(IServerType serverType) {
 		WizardFragment fragment = null;
 		TaskModel taskModel = new TaskModel();
 		IRuntimeType runtimeType = serverType.getRuntimeType();
 		final WizardFragment fragment2 = ServerUIPlugin.getWizardFragment(runtimeType.getId());
 		if (fragment2 == null)
 			return Window.CANCEL;
 		
 		try {
 			IRuntimeWorkingCopy runtimeWorkingCopy = runtimeType.createRuntime(null, null);
 			taskModel.putObject(TaskModel.TASK_RUNTIME, runtimeWorkingCopy);
 		} catch (CoreException ce) {
 			Trace.trace(Trace.SEVERE, "Error creating runtime", ce);
 			return Window.CANCEL;
 		}
 		fragment = new WizardFragment() {
 			protected void createChildFragments(List<WizardFragment> list) {
 				list.add(fragment2);
 				list.add(WizardTaskUtil.SaveRuntimeFragment);
 			}
 		};
 		TaskWizard wizard2 = new TaskWizard(Messages.wizNewRuntimeWizardTitle, fragment, taskModel);
 		wizard2.setForcePreviousAndNextButtons(true);
 		WizardDialog dialog = new WizardDialog(getShell(), wizard2);
 		return dialog.open();
 	}
 
 	public void setHost(String host) {
 		this.host = host;
 		if (serverTypeComposite == null)
 			return;
 		
 		boolean changed = false;
 		if (host == null) {
 			changed = serverTypeComposite.setHost(true);
 		} else if (SocketUtil.isLocalhost(host))
 			changed = serverTypeComposite.setHost(true);
 		else
 			changed = serverTypeComposite.setHost(false);
 		
 		if (changed)
 			handleTypeSelection(serverTypeComposite.getSelectedServerType());
 		else if (server != null) {
 			server.setHost(host);
 			if (!serverNameModified) {
 				updatingServerName = true;
 				ServerUtil.setServerDefaultName(server);
 				serverName.setText(server.getName());
 				updatingServerName = false;
 			}
 		}
 	}
 
 	/**
 	 * Load a server of the given type.
 	 */
 	protected void loadServerImpl(IServerType serverType) {
 		server = null;
 		
 		if (serverType == null)
 			return;
 		
 		boolean isLocalhost = SocketUtil.isLocalhost(host);
 		
 		server = cache.getCachedServer(serverType, isLocalhost);
 		if (server != null) {
 			server.setHost(host);
 			ServerUtil.setServerDefaultName(server);
 			runtime = server.getRuntime();
 			listener.runtimeSelected(runtime);
 			return;
 		}
 		
 		try {
 			// try to create runtime first
 			IRuntime run = null;
 			if (serverType.hasRuntime()) {
 				runtime = null;
 				updateRuntimes(serverType, isLocalhost);
 				run = getDefaultRuntime();
 			}
 			server = cache.createServer(serverType, run, isLocalhost, null);
 			if (server != null) {
 				server.setHost(host);
 				ServerUtil.setServerDefaultName(server);
 				
 				if (serverType.hasRuntime() && server.getRuntime() == null) {
 					runtime = null;
 					updateRuntimes(serverType, isLocalhost);
 					setRuntime(getDefaultRuntime());
 					
 					if (server.getServerType() != null && server.getServerType().hasServerConfiguration() && !runtime.getLocation().isEmpty())
 						((ServerWorkingCopy)server).importRuntimeConfiguration(runtime, null);
 				}
 				((ServerWorkingCopy)server).setDefaults(null);
 			}
 		} catch (CoreException ce) {
 			Trace.trace(Trace.SEVERE, "Error creating server", ce);
 			server = null;
 			runtime = null;
 			wizard.setMessage(ce.getLocalizedMessage(), IMessageProvider.ERROR);
 		}
 		
 		if (server == null)
 			wizard.setMessage(Messages.wizErrorServerCreationError, IMessageProvider.ERROR);
 	}
 
 	/**
 	 * Pick the first non-stub runtime first. Otherwise, just pick the first runtime.
 	 * 
 	 * @return the default runtime
 	 */
 	protected IRuntime getDefaultRuntime() {
 		if (runtimes == null || runtimes.length == 0)
 			return null;
 		
 		if (runtimes != null) {
 			int size = runtimes.length;
 			for (int i = 0; i < size; i++) {
 				if (!runtimes[i].isStub())
 					return runtimes[i];
 			}
 		}
 		return runtimes[0];
 	}
 
 	protected void updateRuntimes(IServerType serverType, boolean isLocalhost) {
 		if (serverType == null)
 			return;
 		
 		IRuntimeType runtimeType = serverType.getRuntimeType();
 		runtimes = ServerUIPlugin.getRuntimes(runtimeType);
 		newRuntime = null;
 		
 		if (runtimes != null) {
 			List<IRuntime> runtimes2 = new ArrayList<IRuntime>();
 			int size = runtimes.length;
 			for (int i = 0; i < size; i++) {
 				IRuntime runtime2 = runtimes[i];
 				if (isLocalhost || !runtime2.isStub())
 					runtimes2.add(runtime2);
 			}
 			runtimes = new IRuntime[runtimes2.size()];
 			runtimes2.toArray(runtimes);
 			if (runtimes.length > 0)
 				return;
 		}
 		
 		// create a new runtime
 		try {
 			IRuntimeWorkingCopy runtimeWC = runtimeType.createRuntime(null, null);
 			ServerUtil.setRuntimeDefaultName(runtimeWC);
 			runtimes = new IRuntime[1];
 			runtimes[0] = runtimeWC;
 			newRuntime = runtimeWC;
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Couldn't create runtime", e); //$NON-NLS-1$
 		}
 	}
 
 	protected void updateRuntimeCombo(IServerType serverType) {
 		if (serverType == null || !serverType.hasRuntime() || server == null) {
 			if (runtimeLabel != null) {
 				runtimeLabel.setEnabled(false);
 				runtimeCombo.setItems(new String[0]);
 				runtimeCombo.setEnabled(false);
 				runtimeLabel.setVisible(false);
 				runtimeCombo.setVisible(false);
 				configureRuntimes.setEnabled(false);
 				configureRuntimes.setVisible(false);
 				addRuntime.setEnabled(false);
 				addRuntime.setVisible(false);
 			}
 			runtimes = new IRuntime[0];
 			setRuntime(null);
 			return;
 		}
 		
 		updateRuntimes(serverType, !SocketUtil.isLocalhost(server.getHost()));
 		
 		int size = runtimes.length;
 		String[] items = new String[size];
 		for (int i = 0; i < size; i++) {
 			if (runtimes[i].equals(newRuntime))
 				items[i] = Messages.wizNewServerRuntimeCreate;
 			else
 				items[i] = runtimes[i].getName();
 		}
 		
 		if (runtime == null)
 			setRuntime(getDefaultRuntime());
 		
 		if (runtimeCombo != null) {
 			runtimeCombo.setItems(items);
 			if (runtimes.length > 0) {
 				int sel = -1;
 				for (int i = 0; i < size; i++) {
 					if (runtimes[i].equals(runtime))
 						sel = i;
 				}
 				if (sel < 0) {
 					sel = 0;
 				}
 				
 				runtimeCombo.select(sel);
				setRuntime(runtimes[0]);
 			}
 			
 			IRuntimeType runtimeType = serverType.getRuntimeType();
 			boolean showRuntime = ServerUIPlugin.getRuntimes(runtimeType).length >=1;
 			runtimeCombo.setEnabled(showRuntime);
 			runtimeLabel.setEnabled(showRuntime);
 			configureRuntimes.setEnabled(showRuntime);
 			addRuntime.setEnabled(showRuntime);
 			runtimeLabel.setVisible(showRuntime);
 			runtimeCombo.setVisible(showRuntime);
 			configureRuntimes.setVisible(showRuntime);
 			addRuntime.setVisible(showRuntime);
 		}
 	}
 
 	protected void setRuntime(IRuntime runtime2) {
 		runtime = runtime2;
 		if (server != null) {
 			server.setRuntime(runtime);
 			ServerUtil.setServerDefaultName(server);
 		}
 		listener.runtimeSelected(runtime);
 	}
 
 	/**
 	 * Handle the server type selection.
 	 */
 	protected void handleTypeSelection(IServerType serverType) {
 		boolean wrong = false;
 		if (serverType != null && moduleType != null) {
 			IRuntimeType runtimeType = serverType.getRuntimeType();
 			if (!ServerUtil.isSupportedModule(runtimeType.getModuleTypes(), moduleType)) {
 				serverType = null;
 				wrong = true;
 				//wizard.setMessage("Not the right spec level2", IMessageProvider.ERROR);
 			}
 		}
 		
 		if (wrong) {
 			server = null;
 			runtime = null;
 			wizard.setMessage(NLS.bind(Messages.errorVersionLevel, new Object[] { moduleType.getName(), moduleType.getVersion() }), IMessageProvider.ERROR);
 		} else if (serverType == null) {
 			server = null;
 			runtime = null;
 			wizard.setMessage("", IMessageProvider.ERROR); //$NON-NLS-1$
 		} else {
 			wizard.setMessage(null, IMessageProvider.NONE);
 			loadServerImpl(serverType);
 			if (server != null && module != null) {
 				IStatus status = NewServerComposite.isSupportedModule(server, module);
 				if (status != null) {
 					if (status.getSeverity() == IStatus.ERROR)
 						wizard.setMessage(status.getMessage(), IMessageProvider.ERROR);
 					else if (status.getSeverity() == IStatus.WARNING)
 						wizard.setMessage(status.getMessage(), IMessageProvider.WARNING);
 					else if (status.getSeverity() == IStatus.INFO)
 						wizard.setMessage(status.getMessage(), IMessageProvider.INFORMATION);
 					server = null;
 				}
 			}
 		}
 		
 		if (serverName != null && !serverNameModified) {
 			updatingServerName = true;
 			if (server == null)
 				serverName.setText("");
 			else
 				serverName.setText(server.getName());
 			updatingServerName = false;
 		}
 		
 		updateRuntimeCombo(serverType);
 		if (serverName != null) {
 			if (server == null) {
 				serverName.setEditable(false);
 				serverNameToolBar.getControl().setVisible(false);
 			} else {
 				serverName.setEditable(true);
 				serverNameToolBar.getControl().setVisible(serverNameModified);
 			}
 		}
 		listener.serverSelected(server);
 		wizard.update();
 	}
 
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
 	
 		if (visible) {
 			/*if (defaultServerFactory != null) {
 				tree.setSelection(new TreeItem[] { defaultServerFactory });
 				tree.showItem(tree.getItems()[0]);
 			}*/
 			// force the focus to initially validate the fields
 			handleTypeSelection(null);
 		}
 		
 		Control[] c = getChildren();
 		if (c != null) {
 			int size = c.length;
 			for (int i = 0; i < size; i++)
 				if (c[i] != null && c[i] instanceof ServerTypeComposite)
 					c[i].setVisible(visible);
 		}
 		if (visible)
 			handleTypeSelection(serverTypeComposite.getSelectedServerType());
 	}
 
 	public void refresh() {
 		serverTypeComposite.refresh();
 	}
 
 	public IRuntime getRuntime() {
 		return runtime;
 	}
 
 	public IServerWorkingCopy getServer() {
 		return server;
 	}
 }
