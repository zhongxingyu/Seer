 /*******************************************************************************
  * Copyright (c) 2003, 2011 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.server.ui.internal.wizard.page;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.fieldassist.*;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.eclipse.ui.help.IWorkbenchHelpSystem;
 import org.eclipse.wst.server.core.*;
 import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
 import org.eclipse.wst.server.core.util.SocketUtil;
 import org.eclipse.wst.server.ui.AbstractUIControl;
 import org.eclipse.wst.server.ui.AbstractUIControl.IUIControlListener;
 import org.eclipse.wst.server.ui.AbstractUIControl.UIControlEntry;
 import org.eclipse.wst.server.ui.internal.*;
 import org.eclipse.wst.server.ui.internal.viewers.ServerTypeComposite;
 import org.eclipse.wst.server.ui.internal.wizard.TaskWizard;
 import org.eclipse.wst.server.ui.internal.wizard.WizardTaskUtil;
 import org.eclipse.wst.server.ui.internal.wizard.page.HostnameComposite.IHostnameSelectionListener;
 import org.eclipse.wst.server.ui.wizard.ServerCreationWizardPageExtension;
 import org.eclipse.wst.server.ui.wizard.WizardFragment;
 
 /**
  * Wizard page used to create a server and configuration at the same time.
  */
 public class NewManualServerComposite extends Composite implements IUIControlListener {
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
 	
 	protected HostnameComposite manualHostComp;
 	IHostnameSelectionListener hostnameListener;
 	protected Label hostnameLabel;
 	protected Text hostname;
 	protected ControlDecoration hostnameDecoration;
 
 	protected ServerCreationCache cache = new ServerCreationCache();
 	
 	private IServerType oldServerType;
 	
 	HostnameChangedAction hostnameChangeAction;
 	Timer timer = null;
 	
 	private boolean canSupportModule=true;
 
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
 		
 		List<ServerCreationWizardPageExtension> pageExtensionLst = ServerUIPlugin.getServerCreationWizardPageExtensions();
 		// Add the page modifier top section UI. 
 		for (ServerCreationWizardPageExtension curPageExtension : pageExtensionLst) {
 			curPageExtension.createControl(ServerCreationWizardPageExtension.UI_POSITION.TOP, this);
 			curPageExtension.setUIControlListener(this);
 		}
 		
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
 		
 		// Add the page modifier middle section UI. 
 		for (ServerCreationWizardPageExtension curPageExtension : pageExtensionLst) {
 			curPageExtension.createControl(ServerCreationWizardPageExtension.UI_POSITION.MIDDLE, this);
 		}
 		
 		hostnameListener = 	new IHostnameSelectionListener() {
 			public void hostnameSelected(String selectedHostname) {
 				setHost(selectedHostname);
 			}
 	    };		
 		hostnameLabel = new Label(this, SWT.NONE);
 		hostnameLabel.setText(Messages.hostname);
 		hostname = new Text(this, SWT.SINGLE | SWT.BORDER | SWT.CANCEL);
 		hostname.setText(HostnameComposite.LOCALHOST);
 		hostnameDecoration = new ControlDecoration(hostname, SWT.TOP | SWT.LEAD);
 		
 		GridData data2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL );
 		hostname.setLayoutData(data2);
 		new Label(this, SWT.NONE);
 		
 		hostname.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				setHostnameChangeTimer(hostname.getText());
 			}
 		});
 		
 		FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
 		FieldDecoration fd = registry.getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
 		hostnameDecoration.setImage(fd.getImage());
 		hostnameDecoration.setDescriptionText(fd.getDescription());
 		
 		hostname.addFocusListener(new FocusListener() {
 			public void focusGained(FocusEvent e) {
 				hostnameDecoration.show();
 			}
 
 			public void focusLost(FocusEvent e) {
 				hostnameDecoration.hide();
 			}
 		});
 		
 		List<String> hosts = ServerUIPlugin.getPreferences().getHostnames();
 		String[] hosts2 = hosts.toArray(new String[hosts.size()]);
 		new AutoCompleteField(hostname, new TextContentAdapter(), hosts2);
 		
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
 					((ServerWorkingCopy)server).setDefaults(null);
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
 				showRuntimeWizard(serverType);
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
 		
 		// Add the page modifier bottom section UI. 
 		for (ServerCreationWizardPageExtension curPageExtension : pageExtensionLst) {
 			curPageExtension.createControl(ServerCreationWizardPageExtension.UI_POSITION.BOTTOM, this);
 		}
 		
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
 			if (Trace.SEVERE) {
 				Trace.trace(Trace.STRING_SEVERE, "Error creating runtime", ce);
 			}
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
 		int returnValue = dialog.open();
 		if (returnValue != Window.CANCEL) {
 			updateRuntimeCombo(serverType);
 			IRuntime rt = (IRuntime)taskModel.getObject(TaskModel.TASK_RUNTIME);
 			if (rt != null && rt.getName() != null && runtimeCombo.indexOf(rt.getName()) != -1) {
 				setRuntime(rt);
 				runtimeCombo.select(runtimeCombo.indexOf(rt.getName()));
 			}
 		}
 		return returnValue;
 	}
 
 	public String getControlStringValue(String controlId) {
 		if (controlId != null && AbstractUIControl.PROP_HOSTNAME.equals(controlId)) {
 			return host;
 		}	
 		return null;
 	}
 	
 	public void setHost(String host) {
 		this.host = host;
 		if (serverTypeComposite == null)
 			return;
 		
 		IServerType selectedServerType = serverTypeComposite.getSelectedServerType();
 		handleHostnameChange(selectedServerType);
 	}
 	
 	protected void handleHostnameChange(IServerType serverType) {
 
 		wizard.setMessage(null, IMessageProvider.NONE);
 		if (!checkHostAndServerType(serverType)) {
 			return;// Host name validation failed, so there is no need to continue handling hostname change event			
 		}
 		loadServerImpl(serverType);
 		
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
 	}
 
 	/**
 	 * Validates the server's host name<br/>
 	 * @param selectedServerType
 	 * @return The results of validation: <br/>
 	 * <b>false</b> in case when the selected server type does not support remote host and the field "hostname" is not recognized as localhost one.  This method will also return false on an invalid server type.<br/> 
 	 * <b>true</b> in any other case   
 	 */
 	protected boolean checkHostAndServerType(IServerType selectedServerType){
 		if(selectedServerType == null){
 			return false;
 		}
 		boolean supportsRemote = selectedServerType.supportsRemoteHosts();
		if (hostname.getText().trim().length() == 0){
			wizard.setMessage(NLS.bind(Messages.wizEmptyHostName, new Object[0]), IMessageProvider.ERROR);
			return false;
		}
 		if(!supportsRemote && !SocketUtil.isLocalhost(hostname.getText())) {
 			wizard.setMessage(NLS.bind(Messages.wizCheckRemoteSupport, new Object[0]), IMessageProvider.ERROR);
 			return false;
 		}
 		
 		return true;
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
 			((ServerWorkingCopy)server).setDefaults(null);
 			runtime = server.getRuntime();
 			listener.runtimeSelected(runtime);
 			fireServerWorkingCopyChanged();
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
 				
 				if (serverType.hasRuntime() && server.getRuntime() == null) {
 					runtime = null;
 					updateRuntimes(serverType, isLocalhost);
 					setRuntime(getDefaultRuntime());
 					
 					if (server.getServerType() != null && server.getServerType().hasServerConfiguration() && !runtime.getLocation().isEmpty())
 						((ServerWorkingCopy)server).importRuntimeConfiguration(runtime, null);
 				}
 				
 				((ServerWorkingCopy)server).setDefaults(null);
 				fireServerWorkingCopyChanged();
 			}
 		} catch (CoreException ce) {
 			if (Trace.SEVERE) {
 				Trace.trace(Trace.STRING_SEVERE, "Error creating server", ce);
 			}
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
 			runtimes = new IRuntime[1];
 			runtimes[0] = runtimeWC;
 			newRuntime = runtimeWC;
 		} catch (Exception e) {
 			if (Trace.SEVERE) {
 				Trace.trace(Trace.STRING_SEVERE, "Couldn't create runtime", e);
 			}
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
 				setRuntime(runtimes[sel]);
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
 			((ServerWorkingCopy)server).setDefaults(null);
 			if (!serverNameModified) {
 				updatingServerName = true;
 				serverName.setText(server.getName());
 				updatingServerName = false;
 			}
 			else {
 				server.setName(serverName.getText());
 			}
 			// Validate if selected module is supported with the selected runtime
 			wizard.setMessage(null, IMessageProvider.NONE);
 			if( module!=null ){
 				canSupportModule=true;
 				IStatus status = NewServerComposite.isSupportedModule(server, module);
 				if (status != null) {
 					if (status.getSeverity() == IStatus.ERROR){
 						wizard.setMessage(status.getMessage(), IMessageProvider.ERROR);
 						canSupportModule=false;
 					}
 					else if (status.getSeverity() == IStatus.WARNING)
 						wizard.setMessage(status.getMessage(), IMessageProvider.WARNING);
 					else if (status.getSeverity() == IStatus.INFO)
 						wizard.setMessage(status.getMessage(), IMessageProvider.INFORMATION);
 				}
 			}
 		}
 		listener.runtimeSelected(runtime);
 
 	}
 	
 	protected void fireServerWorkingCopyChanged() {
 		List<ServerCreationWizardPageExtension> pageExtensionLst = ServerUIPlugin.getServerCreationWizardPageExtensions();
 		// Add the page modifier top section UI. 
 		for (ServerCreationWizardPageExtension curPageExtension : pageExtensionLst) {
 			curPageExtension.setServerWorkingCopy(getServer());
 		}
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
 		// Fire the property change event. 
 		List<ServerCreationWizardPageExtension> pageExtensionLst = ServerUIPlugin.getServerCreationWizardPageExtensions();
 		for (ServerCreationWizardPageExtension curPageExtension : pageExtensionLst) {
 			curPageExtension.handlePropertyChanged(new PropertyChangeEvent(this, AbstractUIControl.PROP_SERVER_TYPE, oldServerType, serverType));
 		}
 		// Update the old server type value.
 		oldServerType = serverType;
 
 		checkHostAndServerType(serverType);
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
 
 	protected void hostnameChanged(String newHost) {
 		if (newHost == null)
 			return;
 		/*
 		 * Bug 349434, with the fix in Timer.runTimer, the chance that a new 
 		 * host name is the same as the host name will be very rare. In some  
 		 * cases, it still needs to go through processes such as loadServerImpl. 
 		 * It doesn't worth to handle it differently. Therefore, we are not checking 
 		 * for the same host name in here.
 		 */
 
 		host = newHost;
 		hostnameListener.hostnameSelected(host);
 	}
 
 	public void handleUIControlMapChanged(Map<String, UIControlEntry> controlMap) {
 		if (controlMap == null) {
 			return;
 		}
 		
 		for (String curControlId : controlMap.keySet()) {
 			if (AbstractUIControl.PROP_HOSTNAME.equals(curControlId)) {
 				UIControlEntry curControlEntry = controlMap.get(curControlId);
 				if (hostnameLabel != null)
 					hostnameLabel.setEnabled(curControlEntry.isEnabled());
 				
 				if (hostname != null){
 					if (curControlEntry.getNewTextValue() != null)
 						hostname.setText(curControlEntry.getNewTextValue());
 					
 					hostname.setEnabled(curControlEntry.isEnabled());
 				}
 				
 				if (hostnameDecoration != null){
 					if(curControlEntry.isEnabled())
 						hostnameDecoration.show();
 					else
 						hostnameDecoration.hide();
 				}
 			}
 		}
 	}
 
 	public String getCurrentHostname() {
 		if (hostname != null)
 			return hostname.getText();
 		return null;
 	}
 
 	public boolean canSupportModule() {
 		return canSupportModule;
 	}
 
 	void setHostnameChangeTimer(String hostName) {
 		if (hostnameChangeAction == null) {
 			hostnameChangeAction = new HostnameChangedAction(hostName);
 		} else {
 			hostnameChangeAction.setHostName(hostName);
 		}
 
 		if (timer == null) {
 			timer = new Timer(300, hostnameChangeAction);
 		}
 		/*
 		 * Kick off the timer and then call setMessage if the Timer wasn't
 		 * previously running because we want to trigger the isComplete on the page
 		 * so that it stops the user from proceeding to the next page while the
 		 * timer is running.
 		 */
 		if (!timer.isRunning()) {
 			timer.runTimer();
 			wizard.setMessage(null, IMessageProvider.NONE);
 		} else {
 			timer.runTimer();
 		}
 	}
 
 	public boolean isTimerRunning() {
 		if (timer == null) {
 			return false;
 		}
 		return timer.isRunning();
 	}
 
 	public boolean isTimerScheduled() {
 		if (timer == null) {
 			return false;
 		}
 		return timer.isScheduled();
 	}
 
 	/**
 	 * Disposes the timer when the wizard is disposed.
 	 */
 	public void dispose() {
 		if (timer != null) {
 			timer.dispose();
 		}
 	}
 
 	private class HostnameChangedAction implements ActionListener {
 
 		String hostName;
 
 		public HostnameChangedAction(String name) {
 			hostName = name;
 		}
 
 		public void actionPerformed(ActionEvent a) {
 			Display.getDefault().asyncExec(new Runnable() {
 				public void run() {
 						hostnameChanged(hostName);
 				}
 			});
 		}
 
 		void setHostName(String host) {
 			hostName = host;
 		}
 	}
 }
