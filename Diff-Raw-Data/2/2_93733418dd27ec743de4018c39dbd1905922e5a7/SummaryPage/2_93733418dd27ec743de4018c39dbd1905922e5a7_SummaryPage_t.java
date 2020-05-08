 /**
  * Copyright (c) 2008 The RCER Development Team.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
  * this entire header must remain intact.
  *
  * $Id$
  */
 package net.sf.rcer.jcoimport;
 
 import java.text.MessageFormat;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * A wizard page that presents a summary before the actual actions are performed.
  * @author vwegert
  *
  */
 public class SummaryPage extends WizardPage {
 
 	private ProjectGeneratorSettings generatorSettings;
 
 	private IWorkspaceRoot workspaceRoot;
 
 	private Button checkboxPlugin;
 	private Button checkboxFragmentWin32;
 	private Button checkboxFragmentWin64IA;
 	private Button checkboxFragmentWin64x86;
 	private Button checkboxFragmentLinux32;
 	private Button checkboxFragmentLinux64IA;
 	private Button checkboxFragmentLinux64x86;
 	private Button checkboxFragmentDarwin32;
 	private Button checkboxFragmentDarwin64;
 
 	private DataBindingContext context;
 
 	private Button checkboxExportBundles;
 
 
 	/**
 	 * Default constructor.
 	 * @param context
 	 * @param generatorSettings 
 	 */
 	SummaryPage(DataBindingContext context, ProjectGeneratorSettings generatorSettings) {
 		super(Messages.SummaryPage_PageName);
 		this.generatorSettings = generatorSettings;
 		this.context = context;
 		setTitle(Messages.SummaryPage_PageTitle);
 		setPageComplete(true);
 		workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
 	 */
 	public void createControl(Composite parent) {
 		Composite top = new Composite(parent, SWT.NONE);
 		GridLayoutFactory.swtDefaults().applyTo(top);
 		
 		Label info = new Label(top, SWT.NONE);
 		info.setText(Messages.SummaryPage_InfoLabel);
 		
 		checkboxPlugin = new Button(top, SWT.CHECK);
 		context.bindValue(SWTObservables.observeSelection(checkboxPlugin), 
 				BeansObservables.observeValue(generatorSettings, "pluginProjectSelected"), //$NON-NLS-1$
 				new UpdateValueStrategy(), new UpdateValueStrategy());
 		
 		checkboxFragmentWin32 = new Button(top, SWT.CHECK);
 		context.bindValue(SWTObservables.observeSelection(checkboxFragmentWin32), 
 				BeansObservables.observeValue(generatorSettings, "win32FragmentSelected"), //$NON-NLS-1$
 				new UpdateValueStrategy(), new UpdateValueStrategy());
 		
 		checkboxFragmentWin64IA = new Button(top, SWT.CHECK);
 		context.bindValue(SWTObservables.observeSelection(checkboxFragmentWin64IA), 
 				BeansObservables.observeValue(generatorSettings, "win64IAFragmentSelected"), //$NON-NLS-1$
 				new UpdateValueStrategy(), new UpdateValueStrategy());
 		
 		checkboxFragmentWin64x86 = new Button(top, SWT.CHECK);
 		context.bindValue(SWTObservables.observeSelection(checkboxFragmentWin64x86), 
 				BeansObservables.observeValue(generatorSettings, "win64x86FragmentSelected"), //$NON-NLS-1$
 				new UpdateValueStrategy(), new UpdateValueStrategy());
 		
 		checkboxFragmentLinux32 = new Button(top, SWT.CHECK);
		context.bindValue(SWTObservables.observeSelection(checkboxFragmentLinux32), 
 				BeansObservables.observeValue(generatorSettings, "linux32FragmentSelected"), //$NON-NLS-1$
 				new UpdateValueStrategy(), new UpdateValueStrategy());
 		
 		checkboxFragmentLinux64IA = new Button(top, SWT.CHECK);
 		context.bindValue(SWTObservables.observeSelection(checkboxFragmentLinux64IA), 
 				BeansObservables.observeValue(generatorSettings, "linux64IAFragmentSelected"), //$NON-NLS-1$
 				new UpdateValueStrategy(), new UpdateValueStrategy());
 		
 		checkboxFragmentLinux64x86 = new Button(top, SWT.CHECK);
 		context.bindValue(SWTObservables.observeSelection(checkboxFragmentLinux64x86), 
 				BeansObservables.observeValue(generatorSettings, "linux64x86FragmentSelected"), //$NON-NLS-1$
 				new UpdateValueStrategy(), new UpdateValueStrategy());
 		
 		checkboxFragmentDarwin32 = new Button(top, SWT.CHECK);
 		context.bindValue(SWTObservables.observeSelection(checkboxFragmentDarwin32), 
 				BeansObservables.observeValue(generatorSettings, "darwin32FragmentSelected"), //$NON-NLS-1$
 				new UpdateValueStrategy(), new UpdateValueStrategy());
 		
 		checkboxFragmentDarwin64 = new Button(top, SWT.CHECK);
 		context.bindValue(SWTObservables.observeSelection(checkboxFragmentDarwin64), 
 				BeansObservables.observeValue(generatorSettings, "darwin64FragmentSelected"), //$NON-NLS-1$
 				new UpdateValueStrategy(), new UpdateValueStrategy());
 
 		@SuppressWarnings("unused")
 		Label spacer = new Label(top, SWT.NONE);
 		
 		Composite export = new Composite(top, SWT.NONE);
 		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(export);
 		
 		checkboxExportBundles = new Button(export, SWT.CHECK);
 		GridDataFactory.swtDefaults().span(2, 1).applyTo(checkboxExportBundles);
 		checkboxExportBundles.setText(Messages.SummaryPage_ExportToDropinsLabel);
 		context.bindValue(SWTObservables.observeSelection(checkboxExportBundles), 
 				BeansObservables.observeValue(generatorSettings, "bundleExportSelected"), //$NON-NLS-1$
 				new UpdateValueStrategy(), new UpdateValueStrategy());
 		
 		Label exportLabel = new Label(export, SWT.NONE);
 		exportLabel.setText(Messages.SummaryPage_TargetPathLabel);
 		
 		Text exportPathText = new Text(export, SWT.SINGLE | SWT.BORDER);
 		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(exportPathText);
 		context.bindValue(SWTObservables.observeText(exportPathText, SWT.Modify), 
 				BeansObservables.observeValue(generatorSettings, "exportPath"), //$NON-NLS-1$
 				new UpdateValueStrategy(), new UpdateValueStrategy());
 		// TODO validate the target path
 		
 		updateCheckboxes();
 		setControl(top);
 		Dialog.applyDialogFont(top);
 	}
 
 	/**
 	 * Updates the state of the checkboxes so that the text and the state correspond to the selected files.
 	 */
 	private void updateCheckboxes() {
 		// TODO use databinding mechanisms to update the checkboxes
 		updateCheckbox(checkboxPlugin, "foo", IProjectNames.PLUGIN_JCO); //$NON-NLS-1$
 		updateCheckbox(checkboxFragmentWin32, 			
 				generatorSettings.getWin32FileName(), 
 				IProjectNames.FRAGMENT_WINDOWS_32);
 		updateCheckbox(checkboxFragmentWin64IA,		
 				generatorSettings.getWin64IAFileName(), 
 				IProjectNames.FRAGMENT_WINDOWS_64IA);
 		updateCheckbox(checkboxFragmentWin64x86,     
 				generatorSettings.getWin64x86FileName(), 
 				IProjectNames.FRAGMENT_WINDOWS_64X86);
 		updateCheckbox(checkboxFragmentLinux32,      
 				generatorSettings.getLinux32FileName(), 
 				IProjectNames.FRAGMENT_LINUX_32);
 		updateCheckbox(checkboxFragmentLinux64IA,    
 				generatorSettings.getLinux64IAFileName(), 
 				IProjectNames.FRAGMENT_LINUX_64IA);
 		updateCheckbox(checkboxFragmentLinux64x86,   
 				generatorSettings.getLinux64x86FileName(), 
 				IProjectNames.FRAGMENT_LINUX_64X86);
 		updateCheckbox(checkboxFragmentDarwin32, 
 				generatorSettings.getDarwin32FileName(), 
 				IProjectNames.FRAGMENT_DARWIN_32);
 		updateCheckbox(checkboxFragmentDarwin64,  
 				generatorSettings.getDarwin64FileName(), 
 				IProjectNames.FRAGMENT_DARWIN_64);
 	}
 
 	/**
 	 * Updates the state of a single checkboxes so that the text and the state correspond to the selected file. 
 	 * @param checkbox the checkbox to update
 	 * @param fileName the name of the archive file
 	 * @param projectName the name of the target project
 	 */
 	private void updateCheckbox(Button checkbox, String fileName, String projectName) {
 		if (fileName.length() == 0) {
 			checkbox.setText(MessageFormat.format(Messages.SummaryPage_NoSourceLabel, projectName));
 			checkbox.setSelection(false);
 			checkbox.setEnabled(false);
 		} else {
 			checkbox.setEnabled(true);
 			if (workspaceRoot.getProject(projectName).exists()) {
 				checkbox.setText(MessageFormat.format(Messages.SummaryPage_ReplaceProjectLabel, projectName));
 				checkbox.setSelection(false);
 			} else {
 				checkbox.setText(MessageFormat.format(Messages.SummaryPage_CreateProjectLabel, projectName));
 				checkbox.setSelection(true);
 			}
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
 	 */
 	@Override
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
 		if (visible) {
 			updateCheckboxes();
 		}
 	}
 	
 }
