 /* ProjectWizardPage.java
  *
  * Copyright (C) 2007-2008  Johann Prieur <johann.prieur@gmail.com>
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package valable.wizard.project;
 
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 
 import valable.ValaPlugin;
 
 public class ValaProjectWizardPage extends WizardPage {
 	
 	private Text projectNameText;
 	private Text locationText;
 	private Text authorNameText;
 	private Text authorEmailText;
 	
 	private Button licenseCheckBox;
 	
 	/**
 	 * Convenient method to easily get localized strings.
 	 * @param namespace
 	 * @return the corresponding string
 	 */
 	private static String _(String key) {
 		return ValaPlugin.getResourceBundle().getString(key);
 	}
 	
 	protected ValaProjectWizardPage() {
 		super(_("wizard.project.name"));
 		this.setTitle(_("wizard.project.title"));
 		this.setDescription(_("wizard.project.description"));
 	}
 	
 	/**
 	 * Determines if the "Finish" button can be enabled
 	 * @return true if there's enough information in the page to create the project
 	 */
 	private boolean validatePage() {
 		return !projectNameText.getText().equals(""); 
 	}
 
 	private String getWorkspacePath() {
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		return workspace.getRoot().getLocation().toString();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	public void createControl(Composite parent) {
 		Composite control = new Composite(parent, SWT.NULL);
 		control.setLayout(new GridLayout(1, false));
 		control.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		// Project name
 		Composite project = new Composite(control, SWT.NULL);
 		project.setLayout(new GridLayout(2, false));
 		project.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		Label projectNameLabel = new Label(project, SWT.NULL);
 		projectNameLabel.setText(_("wizard.project.name.label"));
 		projectNameText = new Text(project, SWT.BORDER | SWT.SINGLE);
 		projectNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		projectNameText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				locationText.setText(String.format("%s%s%s" , getWorkspacePath(),
 						IPath.SEPARATOR, projectNameText.getText()));
 				setPageComplete(validatePage());
 			}
 		});
 		
 		// Contents group
 		Group contentsGroup = new Group(control, SWT.SHADOW_ETCHED_IN);
 		contentsGroup.setText(_("wizard.project.contents.label"));
 		contentsGroup.setLayout(new GridLayout(1, false));
 		contentsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		Button newRadio = new Button(contentsGroup, SWT.RADIO);
 		newRadio.setText(_("wizard.project.contents.new"));
 		newRadio.setSelection(true);
 		Button existingRadio = new Button(contentsGroup, SWT.RADIO);
 		existingRadio.setText(_("wizard.project.contents.existing"));
 		existingRadio.setEnabled(false); // TODO : make this possible
 		Composite location = new Composite(contentsGroup,SWT.NULL);
 		location.setLayout(new GridLayout(3, false));
 		location.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		
 		Label directoryLabel = new Label(location, SWT.NULL);
 		directoryLabel.setText(_("wizard.project.contents.directory"));
 		locationText = new Text(location, SWT.BORDER | SWT.SINGLE);
 		locationText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		locationText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 			}
 		});
 		Button browseButton = new Button(location, SWT.PUSH);
 		browseButton.setText(_("wizard.project.contents.browse"));
 		
 		directoryLabel.setEnabled(false);
 		locationText.setEnabled(false);
 		browseButton.setEnabled(false);
 		locationText.setText(getWorkspacePath());
 		
 		// Info group
 		Group infoGroup = new Group(control, SWT.SHADOW_ETCHED_IN);
 		infoGroup.setText(_("wizard.project.info.label"));
 		infoGroup.setLayout(new GridLayout(2, false));
 		infoGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		Label authorNameLabel = new Label(infoGroup, SWT.NULL);
 		authorNameLabel.setText(_("wizard.project.info.author"));
 		authorNameText = new Text(infoGroup, SWT.BORDER | SWT.SINGLE);
 		authorNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		Label authorEmailLabel = new Label(infoGroup, SWT.NULL);
 		authorEmailLabel.setText(_("wizard.project.info.email"));
 		authorEmailText = new Text(infoGroup, SWT.BORDER | SWT.SINGLE);
 		authorEmailText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		authorEmailText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				String content = authorEmailText.getText();
 				if(content.matches("^(^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$)?$")) {
 					setMessage(_("wizard.project.description"), NONE);
 				}else{
 					setMessage(_("wizard.project.info.email.malformed"), WARNING);
 				}
 			}
 		});
 		Label licenseLabel = new Label(infoGroup, SWT.NULL);
 		licenseLabel.setText(_("wizard.project.info.license"));
 		final Combo licenseCombo = new Combo(infoGroup, SWT.READ_ONLY);
 		licenseCombo.addSelectionListener(new SelectionListener(){
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				licenseCheckBox.setEnabled(licenseCombo.getSelectionIndex() != 0);
 			}
 			
 		});
 		licenseCombo.add(_("wizard.project.info.license.unspecified"));
 		licenseCombo.add(_("wizard.project.info.license.gpl"));
 		licenseCombo.add(_("wizard.project.info.license.lgpl"));
 		
 		// Generation group
 		Group generationGroup = new Group(control, SWT.SHADOW_ETCHED_IN);
 		generationGroup.setText(_("wizard.project.generation.label"));
 		generationGroup.setLayout(new GridLayout(1, false));
 		generationGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		Button mainCheckBox = new Button(generationGroup, SWT.CHECK);
 		mainCheckBox.setText(_("wizard.project.generation.main"));
 		mainCheckBox.setSelection(true);
 		Button authorsCheckBox = new Button(generationGroup, SWT.CHECK);
 		authorsCheckBox.setText(_("wizard.project.generation.authors"));
 		authorsCheckBox.setSelection(true);
 		licenseCheckBox = new Button(generationGroup, SWT.CHECK);
 		licenseCheckBox.setText(_("wizard.project.generation.license"));
 		licenseCheckBox.setSelection(false);
 		licenseCheckBox.setEnabled(false);
 		Button translationCheckBox = new Button(generationGroup, SWT.CHECK);
 		translationCheckBox.setText(_("wizard.project.generation.translation"));
 		translationCheckBox.setSelection(false);
 		
 		setPageComplete(validatePage());
 		setControl(control);
 	}
 
 	IPath getProjectPath() {
        return (new Path(locationText.getText().trim())).removeLastSegments(1);
     }
 
 	IProject getProjectHandle() {
         return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
     }
 
 	String getProjectName() {
 		return projectNameText.getText().trim();
     }
 
 }
