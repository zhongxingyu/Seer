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
 
 package org.amanzi.neo.loader.dialogs;
 
 import java.util.ArrayList;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.DriveTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.loader.correlate.ETSICorrellator;
 import org.amanzi.neo.loader.internal.NeoLoaderPluginMessages;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.Traverser;
 
 /**
  * Dialog for Correlation 
  * 
  * @author Lagutko_N
  * @since 1.0.0
  */
 public class CorrelateDialog {
 	
 	/*
      * Minimum height of Shell
      */
     private static final int MINIMUM_HEIGHT = 180;
     
     /*
      * Minimum width of Shell
      */
     private static final int MINIMUM_WIDTH = 400;
 	
 	/*
 	 * Shell of this Dialog
 	 */
 	private Shell dialogShell;
 	
 	/*
 	 * Combos to choose datasets
 	 */
 	private Combo firstCombo, secondCombo;
 	
 	/*
 	 * Buttons
 	 */
 	private Button correlate, cancel;
 	
 	public CorrelateDialog(Shell parentShell) {
 		this(parentShell, true);
 	}
 	
 	/**
 	 * Creates a Shell and add GUI elements
 	 * 
 	 * @param shell shell
 	 * @param createNewShell is true than create a child shell of given shell for dialog
 	 */
 	
 	protected CorrelateDialog(Shell shell, boolean createNewShell) {
 		if (createNewShell) {
 			dialogShell = new Shell(shell);
 		}
 		else {
 			dialogShell = shell;
 		}
 		
 		dialogShell.setMinimumSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
 		
		dialogShell.setText(NeoLoaderPluginMessages.CorrelateDialog_DialogTitle);
 		
 		createControl(dialogShell);
 		createActions(dialogShell);
 	}
 	
 	/**
 	 * Opens a Dialog
 	 */
 	public void open() {
 		dialogShell.pack();
 		dialogShell.open();
 	}
 	
 	/**
 	 * Creates actions for buttons
 	 * 
 	 * @param parentShell Dialog Shell
 	 */
 	
 	private void createActions(final Shell parentShell) {
 		firstCombo.addSelectionListener(new SelectionListener() {
 			
 			private String previous;
 			
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				String item = firstCombo.getText();
 				if (previous != null) {
 					secondCombo.add(previous);
 				}
 				previous = item;
 				
 				if ((firstCombo.getText() != null) && (secondCombo.getText() != null) &&
 					(firstCombo.getText().length() > 0) && (secondCombo.getText().length() > 0)) {
 					correlate.setEnabled(true);
 				}
 				else {
 					correlate.setEnabled(false);
 				}
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 		
 		secondCombo.addSelectionListener(new SelectionListener() {
 			
 			private String previous;
 			
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				String item = secondCombo.getText();
 				if (previous != null) {
 					firstCombo.add(previous);
 				}
 				previous = item;
 				
 				if ((firstCombo.getText() != null) && (secondCombo.getText() != null) &&
 						(firstCombo.getText().length() > 0) && (secondCombo.getText().length() > 0)) {
 					correlate.setEnabled(true);
 				}
 				else {
 					correlate.setEnabled(false);
 				}
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 		
 		correlate.addSelectionListener(new SelectionListener() {
 			
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				runCorrelation(firstCombo.getText(), secondCombo.getText());
 				dialogShell.close();				
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 		
 		cancel.addSelectionListener(new SelectionListener() {
 			
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				dialogShell.close();
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 					
 			}
 		});
 	}
 	
 	private void runCorrelation(final String firstDataset, final String secondDataset) {
 		Job correlateJob = new Job("Correlation Job") {
 			
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				ETSICorrellator correlator = new ETSICorrellator();
 				correlator.correlate(firstDataset, secondDataset);
 				return Status.OK_STATUS;
 			}
 		};
 		
 		correlateJob.schedule(50);
 	}
 
 	/**
 	 * Creates controls in parent Composite
 	 * 
 	 * @param parent parent Composite
 	 */
 	
 	private void createControl(Composite parent) {
 		parent.setLayout(new GridLayout(1, true));		
 		parent.setLayoutData(new GridData(SWT.FILL));
 		
 		Label firstComboLabel = new Label(parent, SWT.NONE);
		firstComboLabel.setText(NeoLoaderPluginMessages.CorrelateDialog_select_probe_data);
 		
 		firstCombo = new Combo(parent, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
 		firstCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		firstCombo.setItems(getAMSDatasets());
 		
 		Label secondComboLabel = new Label(parent, SWT.NONE);
		secondComboLabel.setText(NeoLoaderPluginMessages.CorrelateDialog_select_drive_data);
 		
 		secondCombo = new Combo(parent, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
 		secondCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		secondCombo.setItems(getNonAMSDatasets());
 		
 		createFinishButtons(parent);
 	}
 	
 	/**
 	 * Creates 'Cancel' and 'Load' buttons
 	 * 
 	 * @param parent
 	 */
 	
 	private void createFinishButtons(Composite parent) {
 		Composite panel = new Composite(parent, SWT.NONE);
 		panel.setLayout(new FormLayout());
 		GridData data = new GridData(SWT.FILL, SWT.BOTTOM, true, false);	
 		panel.setLayoutData(data);
 		
 		cancel = new Button(panel, SWT.CENTER);
 		cancel.setText(NeoLoaderPluginMessages.DriveDialog_CancelButtonText);
 		FormData cancelButtonFormData = new FormData();
 		cancelButtonFormData.right = new FormAttachment(100, -10);
 		cancelButtonFormData.bottom = new FormAttachment(100, -10);
 		cancelButtonFormData.top = new FormAttachment(0, 10);
 		cancelButtonFormData.width = 100;
 		cancel.setLayoutData(cancelButtonFormData);
 		
 		correlate = new Button(panel, SWT.CENTER);
 		correlate.setText(NeoLoaderPluginMessages.CorrelateDialog_CorrelateButtonText);
 		correlate.setEnabled(false);
 		FormData loadButtonFormData = new FormData();
 		loadButtonFormData.right = new FormAttachment(cancel, -10);
 		loadButtonFormData.bottom = new FormAttachment(100, -10);
 		loadButtonFormData.top = new FormAttachment(0, 10);
 		loadButtonFormData.width = 100;
 		correlate.setLayoutData(loadButtonFormData);
 	}
 	
 	/**
 	 * Returns array of names of all Non-AMS datasets
 	 *
 	 * @return names of all Non-AMS datasets
 	 */
 	private String[] getNonAMSDatasets() {
     	ArrayList<String> result = new ArrayList<String>();
     	Transaction tx = NeoUtils.beginTransaction();
         try {        	
         	Traverser allDatasetTraverser = NeoCorePlugin.getDefault().getProjectService().getAllDatasetTraverser(
                     NeoServiceProvider.getProvider().getService().getReferenceNode());        	
             for (Node node : allDatasetTraverser) {
             	DriveTypes type = NeoUtils.getDatasetType(node, NeoServiceProvider.getProvider().getService());
             	if ((type != DriveTypes.AMS) && (type != DriveTypes.AMS_CALLS)) {
             		result.add((String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME));
             	}
             }
             return result.toArray(new String[] {});
         } finally {
             tx.finish();
         }
     }
     
 	/**
 	 * Returns array of names of all AMS datasets
 	 *
 	 * @return name of all AMS datasets
 	 */
     private String[] getAMSDatasets() {
     	ArrayList<String> result = new ArrayList<String>();
     	Transaction tx = NeoUtils.beginTransaction();
         try {        	
         	Traverser allDatasetTraverser = NeoCorePlugin.getDefault().getProjectService().getAllDatasetTraverser(
                     NeoServiceProvider.getProvider().getService().getReferenceNode());        	
             for (Node node : allDatasetTraverser) {
             	DriveTypes type = NeoUtils.getDatasetType(node, NeoServiceProvider.getProvider().getService());
             	if (type == DriveTypes.AMS) {
             		result.add((String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME));
             	}
             }
             return result.toArray(new String[] {});
         } finally {
             tx.finish();
         }
     }
 }
