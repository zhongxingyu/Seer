 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.neo.wizards;
 
 import java.util.ArrayList;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.loader.dialogs.DriveDialog;
 import org.eclipse.jface.preference.DirectoryFieldEditor;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.Traverser;
 
 /**
  * <p>
  * Main page if ETSIImportWizard
  * </p>
  * 
  * @author Lagutko_n
  * @since 1.0.0
  */
 public class ETSIImportWizardPage extends WizardPage {
 	
 	private class DirectoryEditor extends DirectoryFieldEditor {
 		
 		/**
 	     * Creates a directory field editor.
 	     * 
 	     * @param name the name of the preference this field editor works on
 	     * @param labelText the label text of the field editor
 	     * @param parent the parent of the field editor's control
 	     */
 	    public DirectoryEditor(String name, String labelText, Composite parent) {
 	        super(name, labelText, parent);
 	    }
 		
 		/* (non-Javadoc)
 	     * Method declared on StringButtonFieldEditor.
 	     * Opens the directory chooser dialog and returns the selected directory.
 	     */
 	    protected String changePressed() {
 	    	getTextControl().setText(DriveDialog.getDefaultDirectory());
 	    	
 	    	return super.changePressed();
 	    }
 		
 	}
 
 
     private String fileName;
     private Composite main;
     private Combo dataset;
     private DirectoryFieldEditor editor;
     private ArrayList<String> members;
     private String datasetName;
 
     /**
      * Constructor
      * 
      * @param pageName page name
      * @param description page description
      */
     public ETSIImportWizardPage(String pageName, String description) {
         super(pageName);
         setTitle(pageName);
         setDescription(description);
         setPageComplete(isValidPage());
     }
 
     /**
      *check page
      * 
      * @return true if page valid
      */
     protected boolean isValidPage() {
         return fileName != null;
     }
 
     @Override
     public void createControl(Composite parent) {
         main = new Group(parent, SWT.NULL);
         main.setLayout(new GridLayout(3, false));
         Label label = new Label(main, SWT.LEFT);
         label.setText("Dataset:");
         label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        dataset = new Combo(main, SWT.DROP_DOWN);
         dataset.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
         dataset.setItems(getAllDatasets());
         dataset.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
             	datasetName = dataset.getSelectionIndex() < 0 ? null : members.get(dataset.getSelectionIndex());
                 setPageComplete(isValidPage());
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });        
         editor = new DirectoryEditor("fileSelectESTI", "Directory: ", main); // NON-NLS-1
         editor.getTextControl(main).addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 setFileName(editor.getStringValue());
             }
         });
         setControl(main);
     }
 
     /**
      * Sets file name
      * 
      * @param fileName file name
      */
     protected void setFileName(String fileName) {
         this.fileName = fileName;
         setPageComplete(isValidPage());
         DriveDialog.setDefaultDirectory(fileName);
     }
 
     /**
      * Forms list of Datasets
      * 
      * @return array of Datasets nodes
      */
     private String[] getAllDatasets() {
         Transaction tx = NeoUtils.beginTransaction();
         try {        	
         	members = new ArrayList<String>();
         	Traverser allDatasetTraverser = NeoCorePlugin.getDefault().getProjectService().getAllDatasetTraverser(
                     NeoServiceProvider.getProvider().getService().getReferenceNode());
             for (Node node : allDatasetTraverser) {
                 members.add((String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME));
             }
             
             return members.toArray(new String[] {});
         } finally {
             tx.finish();
         }
     }
 
     /**
      * @return Returns the fileName.
      */
     public String getFileName() {
         return fileName;
     }
 
     /**
      * @return Returns the selected Dataset name.
      */
     public String getDatasetName() {
         return datasetName;
     }
 }
