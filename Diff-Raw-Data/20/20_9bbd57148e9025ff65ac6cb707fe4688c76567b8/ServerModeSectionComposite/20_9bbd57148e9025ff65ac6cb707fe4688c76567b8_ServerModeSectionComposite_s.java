 /******************************************************************************* 
  * Copyright (c) 2010 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.ide.eclipse.as.ui.editor;
 
 import java.util.ArrayList;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledPageBook;
 import org.eclipse.wst.server.core.IServerWorkingCopy;
 import org.eclipse.wst.server.core.util.SocketUtil;
 import org.eclipse.wst.server.ui.internal.command.ServerCommand;
 import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
 import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
 import org.jboss.ide.eclipse.as.core.server.internal.BehaviourModel;
 import org.jboss.ide.eclipse.as.core.server.internal.BehaviourModel.Behaviour;
 import org.jboss.ide.eclipse.as.core.server.internal.BehaviourModel.BehaviourImpl;
 import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
 import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
 import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
 import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
 import org.jboss.ide.eclipse.as.core.util.ServerConverter;
 import org.jboss.ide.eclipse.as.ui.FormUtils;
 import org.jboss.ide.eclipse.as.ui.Messages;
 import org.jboss.ide.eclipse.as.ui.UIUtil;
 import org.jboss.ide.eclipse.as.ui.editor.IDeploymentTypeUI.IServerModeUICallback;
 
 public class ServerModeSectionComposite extends Composite {
 	private ArrayList<DeployUIAdditions> deployAdditions;
 	private Combo deployTypeCombo;
 	private ScrolledPageBook preferencePageBook;
 	private IServerModeUICallback callback;
 	private Button executeShellScripts; // may be null;
 
 	public ServerModeSectionComposite(Composite parent, int style, IServerModeUICallback callback) {
 		super(parent, style);
 		this.callback = callback;
 		loadDeployTypeData();
 		FormToolkit toolkit = new FormToolkit(getDisplay());
 		FormUtils.adaptFormCompositeRecursively(this, toolkit);	
 		setLayout(new FormLayout());
 		
 		Control top = null;
 		if( showExecuteShellCheckbox()) {
 			executeShellScripts = new Button(this, SWT.CHECK);
 			executeShellScripts.setText(Messages.EditorDoNotLaunch);
 			FormData fd = UIUtil.createFormData2(0, 5, null, 0, 0, 5, null, 0);
 			executeShellScripts.setLayoutData(fd);
 			top = executeShellScripts;
 			executeShellScripts.setSelection(LaunchCommandPreferences.isIgnoreLaunchCommand(callback.getServer()));
 			executeShellScripts.addSelectionListener(new SelectionListener(){
 				public void widgetSelected(SelectionEvent e) {
 					executeShellToggled();
 				}
 				public void widgetDefaultSelected(SelectionEvent e) {
 				}}
 			);
 		}
 		
 		deployTypeCombo = new Combo(this, SWT.READ_ONLY);
 		FormData fd = UIUtil.createFormData2(top, 5, null, 0, 0, 5, 50, -5);
 		deployTypeCombo.setLayoutData(fd);
 		
 
 	    preferencePageBook = toolkit.createPageBook(this, SWT.FLAT|SWT.TOP);
 	    preferencePageBook.setLayoutData(UIUtil.createFormData2(
 	    		deployTypeCombo, 5, 0, 200, 0, 5, 100, -5));
 
 	    // fill widgets
 	    String[] nameList = new String[deployAdditions.size()];
 	    for( int i = 0; i < nameList.length; i++ ) {
 	    	nameList[i] = deployAdditions.get(i).behaviourName;
 	    }
 	    deployTypeCombo.setItems(nameList);
 		DeployableServerBehavior ds = ServerConverter.getDeployableServerBehavior(callback.getServer().getOriginal());
 		String current = null;
 		if( ds != null ) {
 			Behaviour b = BehaviourModel.getModel().getBehaviour(callback.getServer().getOriginal().getServerType().getId());
			String behaviourType = DeploymentPreferenceLoader.getCurrentDeploymentMethodTypeId(callback.getServer().getOriginal());
 			current = b.getImpl(behaviourType).getName();
 		} else {
 			String host = callback.getServer().getHost();
 //			IJBossServerPublishMethodType behType = null;
 			BehaviourImpl impl = null;
 			String serverTypeId = callback.getServer().getServerType().getId();
 			if( SocketUtil.isLocalhost(host)) {
				impl = BehaviourModel.getModel().getBehaviour(serverTypeId).getImpl(LocalPublishMethod.LOCAL_PUBLISH_METHOD);
 			} else {
 				// socket is not localhost, hard code this for now
				impl = BehaviourModel.getModel().getBehaviour(serverTypeId).getImpl("rse");
 			}
 			current = impl.getName();
 			callback.execute(new ChangeServerPropertyCommand(
 					callback.getServer(), IDeployableServer.SERVER_MODE, 
 					impl.getId(), Messages.EditorChangeServerMode));
 		}
 		if( current != null ) {
 			int index = deployTypeCombo.indexOf(current);
 			if( index != -1 ) 
 				deployTypeCombo.select(index);
 		}
 	    deployTypeCombo.addModifyListener(new ModifyListener(){
 			public void modifyText(ModifyEvent e) {
 				deployTypeChanged(true);
 			}});
 	    deployTypeChanged(false);
 	}
 	
 	protected boolean showExecuteShellCheckbox() {
 		return true;
 	}
 
 	protected void executeShellToggled() {
 		callback.execute(new ChangeServerPropertyCommand(
 				callback.getServer(), IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS, 
 				new Boolean(executeShellScripts.getSelection()).toString(), Messages.EditorDoNotLaunchCommand));
 	}
 	
 	private class DeployUIAdditions {
 		private String behaviourName;
 		private String behaviourId;
 		
 		private IDeploymentTypeUI ui;
 		private boolean registered = false;
 		
 		public DeployUIAdditions(String name, String id,IDeploymentTypeUI ui) {
 			this.behaviourName = name;
 			this.behaviourId = id;
 			this.ui = ui;
 		}
 		
 		public boolean isRegistered() {
 			return registered;
 		}
 		public void createComposite(Composite parent) {
 			// UI can be null
 			if( ui != null ) {
 				ui.fillComposite(parent, callback);
 				registered = true;
 			} else {
 				parent.setLayout(new FillLayout());
 				Composite child = new Composite(parent, SWT.None);
 				child.setLayout(new FormLayout());
 			}
 		}
 	}
 
 	private void loadDeployTypeData() {
 		deployAdditions = new ArrayList<DeployUIAdditions>();
 		Behaviour b = BehaviourModel.getModel().getBehaviour(callback.getServer().getServerType().getId());
 		BehaviourImpl[] supportedBehaviours = b.getImplementations();
 		for( int i = 0; i < supportedBehaviours.length; i++) {
 			IDeploymentTypeUI ui = EditorExtensionManager.getDefault().getPublishPreferenceUI(supportedBehaviours[i].getId());
 			deployAdditions.add(new DeployUIAdditions(supportedBehaviours[i].getName(), 
 					supportedBehaviours[i].getId(), ui));
 		}
 	}
 
 	private void deployTypeChanged(boolean fireEvent) {
 		int index = deployTypeCombo.getSelectionIndex();
 		if( index != -1 ) {
 			DeployUIAdditions ui = deployAdditions.get(index);
 			if( !ui.isRegistered()) {
 				Composite newRoot = preferencePageBook.createPage(ui);
 				ui.createComposite(newRoot);
 			}
 			preferencePageBook.showPage(ui);
 			if( fireEvent ) {
 				callback.execute(new ChangeServerPropertyCommand(
 						callback.getServer(), IDeployableServer.SERVER_MODE, 
 						ui.behaviourId, "Change server mode"));
 				String deployType = null;
 				if( shouldChangeDefaultDeployType(callback.getServer())) {
 					if( ui.behaviourId.equals(LocalPublishMethod.LOCAL_PUBLISH_METHOD)) {
 						deployType = IDeployableServer.DEPLOY_METADATA;
 					} else {
 						deployType = IDeployableServer.DEPLOY_SERVER;
 					}
 					callback.execute(new ChangeServerPropertyCommand(
 							callback.getServer(), IDeployableServer.DEPLOY_DIRECTORY_TYPE, 
 							deployType, "Change server's deploy location"));
 				}
 			}
 		} else {
 			// null selection
 		}
 	}
 
 	private boolean shouldChangeDefaultDeployType(IServerWorkingCopy server) {
 		return !server.getServerType().getId().equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER) &&
 				!server.getServerType().getId().equals(IJBossToolingConstants.SERVER_AS_70);
 	}
 	
 	public static class ChangeServerPropertyCommand extends ServerCommand {
 		private IServerWorkingCopy server;
 		private String key;
 		private String oldVal;
 		private String newVal;
 		public ChangeServerPropertyCommand(IServerWorkingCopy server, String key, String val, String commandName) {
 			this(server, key, val, LocalPublishMethod.LOCAL_PUBLISH_METHOD, commandName);
 		}
 		
 		public ChangeServerPropertyCommand(IServerWorkingCopy server, String key, String val, String oldDefault, String commandName) {
 			super(server, commandName);
 			this.server = server;
 			this.key = key;
 			this.newVal = val;
 			this.oldVal = server.getAttribute(key, oldDefault);
 		}
 		
 		public void execute() {
 			server.setAttribute(key, newVal);
 		}
 		public void undo() {
 			server.setAttribute(key, oldVal);
 		}
 	}	
 }
