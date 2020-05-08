 /******************************************************************************
  * Copyright (c) 2009 Red Hat, IBM
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Rob Stryker - initial implementation and ongoing maintenance
  *    Chuck Bridgham - Ongoing improvements
  ******************************************************************************/
 package org.eclipse.jst.servlet.ui.internal;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.ui.internal.propertypage.ProjectReferenceWizardFragment;
 import org.eclipse.wst.common.componentcore.ui.internal.taskwizard.IWizardHandle;
 import org.eclipse.wst.common.componentcore.ui.propertypage.IReferenceWizardConstants;
 
 public class CustomWebProjectReferenceWizardFragment extends ProjectReferenceWizardFragment {
 	
 	public CustomWebProjectReferenceWizardFragment() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
	private boolean isWebLib;
 	private Button button;
 	
 	@Override
 	public Composite createComposite(Composite parent, IWizardHandle handle) {
 		
 		Composite c = new Composite(parent, SWT.NONE);
 		c.setLayout(new FormLayout());
 		
 		// make button
 		button = new Button(c, SWT.CHECK);
 		button.setText(Messages.getString("CustomWebProjectReferenceWizardFragment.0")); //$NON-NLS-1$
 		button.setToolTipText(Messages.getString("CustomWebProjectReferenceWizardFragment.1")); //$NON-NLS-1$
 		FormData buttonData = new FormData();
 		buttonData.bottom = new FormAttachment(100,-5);
 		buttonData.left = new FormAttachment(0,5);
 		buttonData.right = new FormAttachment(100,-5);
 		button.setLayoutData(buttonData);
 		button.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 			public void widgetSelected(SelectionEvent e) {
 				handleEdit();
 			}
 		});
 		button.setSelection(true);
 
 		Composite tableComp = super.createComposite(c, handle);
 		FormData tableData = new FormData();
 		tableData.top = new FormAttachment(0,5);
 		tableData.bottom = new FormAttachment(button,-5);
 		tableData.left = new FormAttachment(0,5);
 		tableData.right = new FormAttachment(100,-5);
 		tableComp.setLayoutData(tableData);
 		handle.setTitle(org.eclipse.wst.common.componentcore.ui.Messages.ProjectReferenceTitle);
 		handle.setDescription(Messages.getString("WebProjectReferenceDescription")); //$NON-NLS-1$
 		return c;
 	}
 	
 	public Composite createTableColumnComposite(Composite parent) {
 		Composite aButtonColumn = new Composite(parent, SWT.NONE);
 	
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		aButtonColumn.setLayout(layout);
 		GridData data = new GridData(GridData.FILL_BOTH);
 		//data.minimumHeight = 500;
 		//data.minimumWidth = 400;
 		aButtonColumn.setLayoutData(data);
 		return aButtonColumn;
 	}
 
 	protected void handleEdit() {
 		isWebLib = button.getSelection();
 	}
 
 	@Override
 	protected String getArchiveName(IProject proj, IVirtualComponent comp) {
 		return super.getArchiveName(proj, comp);
 	}
 
 	@Override
 	public void performFinish(IProgressMonitor monitor) throws CoreException {
 		for (int i = 0; i < selected.length; i++) {
 			IProject proj = selected[i];
 			if(JavaEEProjectUtilities.getJ2EEProjectType(proj).equals("")) //$NON-NLS-1$
 				J2EEProjectUtilities.createFlexJavaProjectForProjectOperation(proj).execute(monitor, null);
 		}
 		String location = "/"; //$NON-NLS-1$
 		if(isWebLib) location += J2EEConstants.WEB_INF_LIB;
 		getTaskModel().putObject(IReferenceWizardConstants.DEFAULT_LIBRARY_LOCATION, location);
 		super.performFinish(monitor);
 	}
 	
 }
