 /*******************************************************************************
  * Copyright (c) 2011 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.generator.launch.java;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.variables.VariablesPlugin;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.ContainerSelectionDialog;
 
 /**
  * @author Henrik Rentz-Reichert
  *
  */
 public class JavaGeneratorConfigTab extends AbstractLaunchConfigurationTab {
 
 	public static final String GEN_INSTANCE_DIAGRAM = "GenInstanceDiagram";
 	public static final String GEN_MODEL_PATH = "GenModelPath";
 	public static final String SAVE_GEN_MODEL = "SaveGenModel";
 	public static final String LIB = "Lib";
 	
 	private Button libButton;
 	private Button instanceDiagramButton;
 	private Button saveGenModel;
 	private Text genModelPath;
 	private Button browsePath;
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
 	 */
 	@Override
 	public String getName() {
 		return "Java Generator";
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	public void createControl(Composite parent) {
 		// Create main composite
 		Composite mainComposite = new Composite(parent,SWT.NONE);
 		setControl(mainComposite);
 		
 		GridLayout layout= new GridLayout();
 		layout.numColumns = 2;
 		layout.marginHeight = 10;
 		layout.marginWidth = 10;
 		mainComposite.setLayout(layout);
 		
 		libButton = createCheckButton(mainComposite, "generate all classes as library");
 		libButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
 
 		saveGenModel = createCheckButton(mainComposite, "save generator model");
 		saveGenModel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
 		saveGenModel.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleSaveGenModelSelected();
 			}
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				handleSaveGenModelSelected();
 			}
 			
 		});
 		genModelPath = new Text(mainComposite, SWT.SINGLE | SWT.BORDER);
     	genModelPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		genModelPath.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent e) {
 				validate();
 				setDirty(true);
 				updateLaunchConfigurationDialog();
 			}
 		});
 		browsePath = createPushButton(mainComposite, "Browse...", null);
 		//browsePath.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
 		browsePath.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handlePathButtonSelected();
 			}
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				handlePathButtonSelected();
 			}
 		});
 		instanceDiagramButton = createCheckButton(mainComposite, "generate instance diagram");
 		instanceDiagramButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
 	}
 
 	/**
 	 * 
 	 */
 	protected void handleSaveGenModelSelected() {
 		boolean save = saveGenModel.getSelection();
 		genModelPath.setEnabled(save);
 		browsePath.setEnabled(save);
 		validate();
 		setDirty(true);
 		updateLaunchConfigurationDialog();
 	}
 
 	/**
 	 * 
 	 */
 	private void validate() {
 		if (saveGenModel.getSelection())
 			if (genModelPath.getText().trim().isEmpty()) {
 				setErrorMessage("generator model path must not be empty");
 				return;
 			}
 		setErrorMessage(null);
 	}
 
 	/**
 	 * 
 	 */
 	protected void handlePathButtonSelected() {
 		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(),
 				   ResourcesPlugin.getWorkspace().getRoot(),
 				   false,
 				   "select a container for the generator model");
 		dialog.showClosedProjects(false);
 		dialog.open();
 		Object[] results = dialog.getResult();	
 		if ((results != null) && (results.length > 0) && (results[0] instanceof IPath)) {
 			IPath path = (IPath)results[0];
 			//path = path.append("genmodel.rim");
 			String fname = path.toString();
 			fname = VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression("workspace_loc", fname);
 			genModelPath.setText(fname);
 			setErrorMessage(null);
 			setDirty(true);
 			updateLaunchConfigurationDialog();
 		}		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
 	 */
 	@Override
 	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
 	 */
 	@Override
 	public void initializeFrom(ILaunchConfiguration configuration) {
 		try {
 			libButton.setSelection(configuration.getAttribute(LIB, false));
 			boolean save = configuration.getAttribute(SAVE_GEN_MODEL, false);
 			saveGenModel.setSelection(save);
 			genModelPath.setEnabled(save);
 			browsePath.setEnabled(save);
 			genModelPath.setText(configuration.getAttribute(GEN_MODEL_PATH, ""));
 			instanceDiagramButton.setSelection(configuration.getAttribute(GEN_INSTANCE_DIAGRAM, false));
 		}
 		catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
 	 */
 	@Override
 	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
 		configuration.setAttribute(LIB, libButton.getSelection());
 		configuration.setAttribute(SAVE_GEN_MODEL, saveGenModel.getSelection());
 		configuration.setAttribute(GEN_MODEL_PATH, genModelPath.getText());
 		configuration.setAttribute(GEN_INSTANCE_DIAGRAM, instanceDiagramButton.getSelection());
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
 	 */
 	@Override
 	public boolean canSave() {
 		if (saveGenModel.getSelection())
 			return !genModelPath.getText().trim().isEmpty();
 		
 		return true;
 	}
 }
