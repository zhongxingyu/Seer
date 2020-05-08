 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.ui.wizards.appregister;
 
 import org.jboss.tools.common.model.ui.attribute.XAttributeSupport;
 import org.jboss.tools.common.model.ui.util.ModelUtilities;
 import org.eclipse.jface.dialogs.DialogPage;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Layout;
 import org.jboss.tools.common.meta.action.XEntityData;
 import org.jboss.tools.common.meta.action.impl.XEntityDataImpl;
 import org.jboss.tools.common.model.options.Preference;
 import org.jboss.tools.jst.web.project.helpers.NewWebProjectContext;
 import org.jboss.tools.jst.web.ui.Messages;
 
 public class NewProjectRegisterPage extends WizardPage {
	private final static String SERVLET_VERSION = "servletVersion"; //$NON-NLS-1$
 	private NewWebProjectContext context;
 	private XAttributeSupport support;
 //	private PropertyChangeListener updateDataListener;
 	AppRegisterComponent appRegister = new AppRegisterComponent();
 	Preference servletPreference;
 
 	public NewProjectRegisterPage(NewWebProjectContext context, Preference servletPreference) {
 		super(Messages.NewProjectRegisterPage_Register);
 		this.context = context;
 		this.servletPreference = servletPreference;
 		initServletSupport();
 		appRegister.setContext(context.getRegisterServerContext());
 
 		//For new WTP
 		appRegister.setEnabling(false);
 
 		appRegister.init();
 		initListeners();
 	}
 	
 	public AppRegisterComponent getAppRegisterComponent() {
 		return appRegister;
 	}
 	
 	public void dispose() {
 		super.dispose();
 		if (support!=null) support.dispose();
 		support = null;
 		if (appRegister!=null) appRegister.dispose();
 		appRegister = null;
 //		updateDataListener = null;
 	}
 
 	private void initServletSupport() {
 		String defaultServletVersion = servletPreference.getValue();
 		
 		XEntityData entityData = XEntityDataImpl.create(
			new String[][]{{"WebPrjCreateStepDirs", ""}, {SERVLET_VERSION, ""}}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		entityData.setValue(SERVLET_VERSION, defaultServletVersion);
 		context.setServletVersion(defaultServletVersion);
 		support = new XAttributeSupport(ModelUtilities.getPreferenceModel().getRoot(), entityData);
 		support.setLayout(getLayoutForSupport());
 	}
 
 	private Layout getLayoutForSupport() {
 		GridLayout gridLayout = new GridLayout(2, false);
 		gridLayout.marginHeight = 4;
 		gridLayout.marginWidth = 4;
 		gridLayout.horizontalSpacing = 10;
 		gridLayout.verticalSpacing = 10;
 		return gridLayout;
 	}
 
 	public void createControl(Composite parent)	{
 		initializeDialogUnits(parent);
 		Composite c = new Composite(parent, SWT.NONE);
 		c.setLayout(new GridLayout());
 		Control ch = support.createControl(c);
 		ch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		Label label = new Label(c, SWT.SEPARATOR | SWT.HORIZONTAL);
 		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		ch = appRegister.createControl(c);
 		ch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
 		setControl(c);
 	}
 
 	public void setVisible(boolean visible)	{
 		if (visible) {
 			lock = true;
 			loadServletVersion();
 			appRegister.loadApplicationName();
 			lock = false;
 		}
 		setPageComplete(validatePage());
 		super.setVisible(visible);
 	}
 	
 	public void loadServletVersion() {
		String currentServletVersion = support.getPropertyEditorAdapterByName(SERVLET_VERSION).getStringValue(true);
 		
 		String vs = context.getServletVersion();
 		if(vs != null && vs.length() > 0 && !vs.equals(currentServletVersion)) {
 			context.setServletVersion(vs);
			support.getPropertyEditorAdapterByName(SERVLET_VERSION).setValue(vs);
 		}
 		
 		if(context != null && context.getProjectTemplate() != null) {
 			String prefServletVersion = context.getProjectTemplate().getProjectVersion().getPreferredServletVersion();
 			if(prefServletVersion != null) {
 				int i = context.compareServletVersions(prefServletVersion, currentServletVersion);
 				if(i > 0) {
 					context.setServletVersion(prefServletVersion);
					support.getPropertyEditorAdapterByName(SERVLET_VERSION).setValue(prefServletVersion);
 				}
 			}
 		}
 	}
 	
 	private boolean lock = false;
 	
 	private boolean validatePage() {
 		lock = true;
 		try {
 			appRegister.commit();
 			String msg = appRegister.getErrorMessage();
 			String wrn = null;
 			if(msg == null) {
 				wrn = context.validateServletVersion();
 			}
 			if(msg != null) {
 				setErrorMessage(msg);
 				return false;
 			} else if(wrn != null) {
 				setErrorMessage(null);
 				setMessage(wrn, DialogPage.WARNING);
 			} else {
 				setErrorMessage(null);
 				setMessage(null);
 			}
 			return true;
 		} finally {
 			lock = false;
 		}
 	}
 	
 	private void initListeners() {
		support.getPropertyEditorAdapterByName(SERVLET_VERSION).addValueChangeListener(new InputChangeListener());
 		appRegister.addPropertyChangeListener(new InputChangeListener());
 	}
 
 	class InputChangeListener implements java.beans.PropertyChangeListener {
 		public void propertyChange(java.beans.PropertyChangeEvent evt) {
 			if(lock) return;
			context.setServletVersion(support.getPropertyEditorAdapterByName(SERVLET_VERSION).getStringValue(true));
 			setPageComplete(validatePage());
 		}
 	}
 	
 }
