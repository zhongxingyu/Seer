 /*
  * $Id$
  *
  * Copyright (c) 2004-2005 by the TeXlapse Team.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package net.sourceforge.texlipse.wizards;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import net.sourceforge.texlipse.TexlipsePlugin;
 import net.sourceforge.texlipse.builder.BuilderChooser;
 import net.sourceforge.texlipse.properties.TexlipseProperties;
 import net.sourceforge.texlipse.templates.ProjectTemplateManager;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Text;
 
 
 /**
  * Prefs-page on the project creation wizard.
  * 
  * @author kpkarlss
  */
 public class TexlipseProjectCreationWizardPage extends TexlipseWizardPage {
 
     // textfield for project name
     private Text projectNameField;
     
     // project location, if not in workspace
     private Text projectLocationField;
     
     // textfield for template preview
     private Text descriptionField;
     
     // template chooser
 	private List templateList;
 
 	// template type (system/user)
 	private Label typeLabel;
 
     // output format / build order chooser
     private BuilderChooser outputChooser;
 
     private String workspacePath;
 
     /**
      * 
      * @param attributes Project attributes
      */
     public TexlipseProjectCreationWizardPage(TexlipseProjectAttributes attributes) {
         super(0, attributes);
     }
 
     /**
      * Create the layout of the page.
      * @param parent parent component in the UI
      * @return number of components using a status message
      */
     public void createComponents(Composite parent) {
         
         // path to the workspace root directory
         workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().addTrailingSeparator().toOSString();
         
         createProjectNameControl(parent);
         addSpacer(parent, 2);
         createProjectLocationControl(parent);
         addSpacer(parent, 2);
         createOutputFormatControl(parent);
         addSeparator(parent);
         addSpacer(parent, 2);
         createLabels(parent);
         createTemplateControl(parent);
         
         //this updates the template description area and the template type label
     	updateEntries();
     }
 
     /**
      * @param parent parent component
      */
     private void createOutputFormatControl(Composite parent) {
         outputChooser = new BuilderChooser(parent);
         GridData ngd = new GridData(GridData.FILL_HORIZONTAL);
         ngd.horizontalSpan = 2;
         outputChooser.setLayoutData(ngd);
         outputChooser.setSelectedBuilder(TexlipsePlugin.getDefault().getPreferenceStore().getInt(TexlipseProperties.BUILDER_NUMBER));
         String o = attributes.getOutputFile();
         attributes.setOutputFile(o.substring(0, o.lastIndexOf('.')+1) + outputChooser.getSelectedFormat());
         attributes.setOutputFormat(outputChooser.getSelectedFormat());
         outputChooser.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent event) {
                 String o = attributes.getOutputFile();
                 attributes.setOutputFile(o.substring(0, o.lastIndexOf('.')+1) + outputChooser.getSelectedFormat());
                 attributes.setOutputFormat(outputChooser.getSelectedFormat());
                 attributes.setBuilder(outputChooser.getSelectedBuilder());
             }});
     }
 
     /**
      * Create project name settings box.
      * @param composite the parent container
      */
     private void createProjectNameControl(Composite composite) {
         
         // add label
         Label label = new Label(composite, SWT.LEFT);
         label.setText(TexlipsePlugin.getResourceString("projectWizardNameLabel"));
         label.setToolTipText(TexlipsePlugin.getResourceString("projectWizardNameTooltip"));
         label.setLayoutData(new GridData());
 
         // add text field
         projectNameField = new Text(composite, SWT.SINGLE | SWT.BORDER);
         projectNameField.setText(attributes.getProjectName());
         projectNameField.setToolTipText(TexlipsePlugin.getResourceString("projectWizardNameTooltip"));
         projectNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         projectNameField.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 if (!projectNameField.isDisposed()) {
                     validateProjectName(projectNameField.getText());
                     if (!projectLocationField.isEnabled()) {
                         projectLocationField.setText(workspacePath + projectNameField.getText());
                     }
                 }
             }
         });
     }
 
     /**
      * Create project name settings box.
      * @param composite the parent container
      */
     private void createProjectLocationControl(Composite parent) {
 
         // create borders
         Group group = new Group(parent, SWT.NULL);
         group.setText(TexlipsePlugin.getResourceString("projectWizardLocationTitle"));
         group.setLayout(new GridLayout());
         GridData lgd = new GridData(GridData.FILL_HORIZONTAL);
         lgd.horizontalSpan = 2;
         group.setLayoutData(lgd);
         
         // add radioButtons
        Button createLocalProjectButton = new Button(group, SWT.RADIO | SWT.LEFT);
         createLocalProjectButton.setLayoutData(new GridData());
         createLocalProjectButton.setText(TexlipsePlugin.getResourceString("projectWizardLocationLocal"));
         createLocalProjectButton.setSelection(true);
         createLocalProjectButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent event) {
                 projectLocationField.setText(workspacePath + projectNameField.getText());
                 Control[] c = projectLocationField.getParent().getChildren();
                 for (int i = 0; i < c.length; i++) {
                     c[i].setEnabled(false);
                 }
                 attributes.setProjectLocation(null);
             }});
        Button createExternalProjectButton = new Button(group, SWT.RADIO | SWT.LEFT);
         createExternalProjectButton.setLayoutData(new GridData());
         createExternalProjectButton.setText(TexlipsePlugin.getResourceString("projectWizardLocationExternal"));
         createExternalProjectButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent event) {
                 Control[] c = projectLocationField.getParent().getChildren();
                 for (int i = 0; i < c.length; i++) {
                     c[i].setEnabled(true);
                 }
                 projectLocationField.setText("");
             }});
         
         Composite composite = new Composite(group, SWT.NULL);
         composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         GridLayout cgl = new GridLayout();
         cgl.numColumns = 3;
         composite.setLayout(cgl);
         
         // add location label
         Label label = new Label(composite, SWT.LEFT);
         label.setText(TexlipsePlugin.getResourceString("projectWizardLocationLabel"));
         label.setLayoutData(new GridData());
         label.setEnabled(false);
         
         // add location field
         projectLocationField = new Text(composite, SWT.SINGLE | SWT.BORDER);
         projectLocationField.setText(workspacePath);
         projectLocationField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         projectLocationField.setEnabled(false);
         projectLocationField.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 if (!projectLocationField.isDisposed() && projectLocationField.isEnabled()) {
                     validateProjectLocation(projectLocationField.getText());
                 }
             }});
         
         Button browseLocationButton = new Button(composite, SWT.PUSH);
         browseLocationButton.setText(TexlipsePlugin.getResourceString("openBrowse"));
         browseLocationButton.setLayoutData(new GridData());
         browseLocationButton.setEnabled(false);
         browseLocationButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent event) {
                 DirectoryDialog dialog = new DirectoryDialog(projectLocationField.getShell());
                 dialog.setMessage(TexlipsePlugin.getResourceString("projectWizardLocationSelect"));
                 dialog.setText(TexlipsePlugin.getResourceString("projectWizardLocationSelect"));
                 String current = projectLocationField.getText();
                 if (current == null || current.length() == 0) {
                     current = workspacePath;
                 }
                 dialog.setFilterPath(current);
                 String dirStr = dialog.open();
                 if (dirStr != null) {
                     File dir = new File(dirStr);
                     if (dir.exists() && dir.isDirectory()) {
                         projectLocationField.setText(dir.getAbsolutePath());
                     }
                 }
             }});
     }
 
     /**
      * Check if the external project location is valid.
      * This method updates the status message for the project location.
      * @param text the path to project location
      */
     private void validateProjectLocation(String text) {
         IWorkspace workspace = ResourcesPlugin.getWorkspace();
         IProject p = workspace.getRoot().getProject(projectNameField.getText());
         IStatus status = workspace.validateProjectLocation(p, new Path(text));
         if (status.getSeverity() == IStatus.OK) {
             attributes.setProjectLocation(text);
         }
         updateStatus(status, projectLocationField);
     }
 
     /**
      * Creates labels for the template list and the dexription text area 
      * to the given Composite object
      * 
      * @param composite the parent container
      */
     private void createLabels(Composite composite) {
     	
         // add label for the list of templates
         Label label = new Label(composite, SWT.LEFT);
         label.setText(TexlipsePlugin.getResourceString("projectWizardTemplateListLabel"));
         label.setToolTipText(TexlipsePlugin.getResourceString("projectWizardTemplateListTooltip"));
         label.setLayoutData(new GridData());
     	
         // add composite containing a label for the description area and templatetype (system/user) label
         Composite c = new Composite(composite, SWT.NONE);
         c.setLayout(new GridLayout(2,false));
         c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         
         // add label for the description area
         Label l = new Label(c, SWT.LEFT);
         l.setText(TexlipsePlugin.getResourceString("projectWizardTemplateDescriptionLabel"));
         l.setToolTipText(TexlipsePlugin.getResourceString("projectWizardTemplateDescriptionTooltip"));        
     	l.setLayoutData(new GridData());
 
         // add label for presenting the type of the selected template
     	typeLabel = new Label(c, SWT.RIGHT);
     	//Note that thetext of this label is set by updateEntries()
     	typeLabel.setToolTipText(TexlipsePlugin.getResourceString("projectWizardTemplateTypeTooltip"));
         typeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
     }
     
     
     /**
      * Creates a list element containing available templates (system and user)
      * and a text area next to it for showing description about the selected template
      * 
      * @param composite the parent container
      */
     private void createTemplateControl(Composite composite) {
         
         // add list for templates
         templateList = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
 		templateList.setItems(ProjectTemplateManager.loadTemplateNames());
         templateList.setLayoutData(new GridData(GridData.FILL_VERTICAL));
         templateList.setToolTipText(TexlipsePlugin.getResourceString("projectWizardTemplateTooltip"));
         templateList.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
             	attributes.setTemplate(templateList.getSelection()[0]);
             	updateEntries();
             }});
         
         templateList.setSelection(0);
         // this has to be done, because setSelection() doesn't generate an event
         attributes.setTemplate(templateList.getItem(0));
 
         
         // add TextField for the selected template's description
         descriptionField = new Text(composite, SWT.MULTI | SWT.BORDER);
         descriptionField.setToolTipText(TexlipsePlugin.getResourceString("projectWizardTemplateDescriptionTooltip"));
         descriptionField.setLayoutData(new GridData(GridData.FILL_BOTH));
         descriptionField.setEditable(false);
     }
 
     /**
      * Updates the description text area and template type label 
      * 
      */
     private void updateEntries(){
     	typeLabel.setText(templateType(attributes.getTemplate()));
     	readProjectTemplateDescription(attributes.getTemplate());
     }
 
     /**
      * Returns the type of the given template.
      * 
      * @param template name of a template (should be the selected item from 
      *   the list element containing all available templates).
      * @return Either "User defined template" or "System defined template" depending
      *   wether the given template is found in plugins templates directory (former if not,
      *   latter if yes).
      */
     private String templateType(String template){
     	URL templateUrl = TexlipsePlugin.getDefault().getBundle().getEntry("templates" + File.separator + template + ".tex");
     	if(templateUrl==null) return "User defined template";
     	else return "System template";
     }
 
     /**
      * Starts to read the given template (assuming it is found
      * from either plugins template directory or user's template directory...
      * if not, the description is set to "") and puts all lines
      * starting with "%%" (excluding "%%") to the description text area.
      * Reading is ended, when a line not starting wiht "%%" is encountered
      * or the end of file is reached.
      *  
      * @param template name of a template
      */
     private void readProjectTemplateDescription(String template) {
     	//if system template, then it is found here
         URL templateUrl = TexlipsePlugin.getDefault().getBundle().getEntry("templates" + File.separator + template + ".tex");
         String userTemplate = null;
     	if (templateUrl==null){
     		//if not, then the template is user defined 
     		File userTemplateFolder = ProjectTemplateManager.getUserTemplateFolder();
     		userTemplate = userTemplateFolder.getAbsolutePath()+File.separator+template+".tex";
     	}
         
     	try {
     		String line = null;
     		
 			BufferedReader r;
 			if(templateUrl!=null) r = new BufferedReader(new InputStreamReader(templateUrl.openStream()));
 			else r = new BufferedReader(new FileReader(userTemplate));
 			StringBuffer sb = new StringBuffer();
 			
 			while ((line = r.readLine()) != null) {
 				if(!line.startsWith("%%")) break;
 				if(line.length()>2) sb.append(line.substring(2));
 				sb.append('\n');
 			}
 			
 			r.close();
 			sb.toString();       
 	        descriptionField.setText(sb.toString());
     	} catch (IOException e) {
     		TexlipsePlugin.log("Reading a description of template file:", e);
     		descriptionField.setText("");
 		}
     }
 
     /**
      * Update the status line when the page becomes visible.
      */
     public void setVisible(boolean visible) {
         super.setVisible(visible);
         if (visible) {
             validateProjectName(projectNameField.getText());
         }
     }
 
     /**
      * Check if there already is a project under the given name.
      * @param text
      */
     private void validateProjectName(String text) {
         
         IWorkspace workspace = ResourcesPlugin.getWorkspace();
         IStatus status = workspace.validateName(text, IResource.PROJECT);
         
         if (status.isOK()) {
             if (workspace.getRoot().getProject(text).exists()) {
                 status = createStatus(IStatus.ERROR,
                         TexlipsePlugin.getResourceString("projectWizardNameError"));
             }
             attributes.setProjectName(text);
         }
 
         updateStatus(status, projectNameField);
     }
 }
