 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.creation.ui.widgets;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.wst.command.internal.env.ui.widgets.SimpleWidgetDataContributor;
 import org.eclipse.wst.command.internal.env.ui.widgets.WidgetDataEvents;
 import org.jboss.tools.ws.core.utils.StatusUtils;
 import org.jboss.tools.ws.creation.core.data.ServiceModel;
 import org.jboss.tools.ws.creation.core.messages.JBossWSCreationCoreMessages;
 import org.jboss.tools.ws.creation.ui.utils.JBossCreationUIUtils;
 
 /**
  * @author Grid Qian
  */
 @SuppressWarnings("restriction")
 public class Java2WSDLCodeGenConfigWidget extends
 		SimpleWidgetDataContributor {
 
 	private ServiceModel model;
 	private Button btnUpdateWebxml;
 	private Combo  sourceCombo;
 	private boolean isOK;
 	private IStatus status = null;
 
 	public Java2WSDLCodeGenConfigWidget(ServiceModel model) {
 		this.model = model;
 		model.setGenWSDL(false);
 	}
 
 	public WidgetDataEvents addControls(Composite parent,
 			Listener statusListener) {
 
 		Composite configCom = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout(2, false);
 		configCom.setLayout(layout);
 		configCom.setLayoutData(new GridData(GridData.FILL_BOTH));
 		
 		//choose source folder
 		sourceCombo = JBossCreationUIUtils.createComboItem(configCom, model,JBossWSCreationCoreMessages.Label_SourceFolder_Name ,JBossWSCreationCoreMessages.Tooltip_SourceFolder);
 		sourceCombo.addListener(SWT.Modify, new Listener(){
 			public void handleEvent(Event arg0) {
                 String javaSourceFolder = sourceCombo.getText();
                 model.setJavaSourceFolder(javaSourceFolder);	
 			}	
         });
 		isOK = JBossCreationUIUtils.populateSourceFolderCombo(sourceCombo, model.getSrcList());
 		if(!isOK) {
 			status = StatusUtils.errorStatus(JBossWSCreationCoreMessages.Error_Message_No_SourceFolder);
 		}
 
 		final Button wsdlGen = new Button(configCom, SWT.CHECK | SWT.NONE);
 		GridData wsdlGenData = new GridData();
 		wsdlGenData.horizontalSpan = 2;
 		wsdlGen.setLayoutData(wsdlGenData);
 		wsdlGen.setText(JBossWSCreationCoreMessages.Label_Generate_WSDL);
 		wsdlGen.setSelection(false);
 		wsdlGen.addSelectionListener(new SelectionAdapter() {
 			
 			public void widgetSelected(SelectionEvent e) {
 				model.setGenWSDL(wsdlGen.getSelection());
 
 			}
 
 		});
 		
 		btnUpdateWebxml = new Button(configCom, SWT.CHECK);
 		btnUpdateWebxml.setText(JBossWSCreationCoreMessages.Label_Update_Webxml);
 		btnUpdateWebxml.setSelection(true);
 		btnUpdateWebxml.addSelectionListener(new SelectionAdapter(){
 			public void widgetSelected(SelectionEvent e) {
 				model.setUpdateWebxml(btnUpdateWebxml.getSelection());
 			}
 		});
 		return this;
 	}
 
 	public IStatus getStatus() {
 		return status;
 	}
 }
