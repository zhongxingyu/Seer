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
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.DriveTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.ActionUtil;
 import org.amanzi.neo.core.utils.CSVParser;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.amanzi.neo.core.utils.ActionUtil.RunnableWithResult;
 import org.amanzi.neo.loader.AbstractLoader;
 import org.amanzi.neo.loader.DriveLoader;
 import org.amanzi.neo.loader.GPSLoader;
 import org.amanzi.neo.loader.LoaderUtils;
 import org.amanzi.neo.loader.NemoLoader;
 import org.amanzi.neo.loader.OldNemoVersionLoader;
 import org.amanzi.neo.loader.RomesLoader;
 import org.amanzi.neo.loader.TEMSLoader;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.amanzi.neo.loader.internal.NeoLoaderPluginMessages;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialogWithToggle;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Traverser;
 
 /**
  * Dialog for Loading drive test data
  * 
  * @author Lagutko_N
  */
 public class DriveDialog {
     /** String ASC_PAT_FILE field */
     private static final String ASC_PAT_FILE = ".*_(\\d{6})_.*";
     private static final String FMT_PAT_FILE = ".*(\\d{4}-\\d{2}-\\d{2}).*";
     /*
      * Names of supported files for Drive data
      */
     public static final String[] Drive_FILE_NAMES = {
         "Drive Test Data (*.FMT; *.ASC; *.nmf; *.dt1)",
         "TEMS Drive Test Export (*.FMT)",
         "Romes drive test export (*.ASC)",
         "GSM drive test export (*.GSM)",
         "Nemo drive test export (*.nmf)",
         "Nemo drive test export (*.dt1)",
         "All Files (*.*)"};
 
     /*
      * Extensions of supported files for Drive data
      */
     public static final String[] Drive_FILE_EXTENSIONS = {
         "*.FMT;*.fmt;*.ASC;*.asc;*.dt1;*.DT1;*.NMF;*.nmf;*.GSM;*.gsm",
         "*.FMT;*.fmt",
         "*.ASC;*.asc",
         "*.GSM;*.gsm",
         "*.NMF;*.nmf",
         "*.dt1;*.DT1",
         "*.*"};
 
     /*
      * Minimum height of Shell
      */
     private static final int MINIMUM_HEIGHT = 400;
     
     /*
      * Minimum width of Shell
      */
     private static final int MINIMUM_WIDTH = 600;
 
     /*
      * Layout for One column and Fixed Width
      */
     //private final static GridLayout layoutOneColumnNotFixedWidth = new GridLayout(1, false);
 
     private static final int MAX_NEMO_LINE_READ = 50;
 
    private static final int FILE_GROUP_WIDTH = 410;
 	
 	/*
 	 * Shell of this Dialog
 	 */
 	private Shell dialogShell;
 	
 	/*
 	 * Button for FileDialog
 	 */
 	private Button browseDialogButton;
 	
 	/*
 	 * Button for adding to load files
 	 */
 	private Button addFilesToLoaded;
 	
 	/*
 	 * Button for removing from load files
 	 */
 	private Button removeFilesFromLoaded;
 	
 	/*
 	 * List for files to choose
 	 */
 	private List folderFilesList;
 	
 	/*
 	 * List for files to load
 	 */
 	private List filesToLoadList;
 	
 	/*
 	 * Cancel button
 	 */
 	private Button cancelButton;
 	
 	/*
 	 * Load button
 	 */
 	private Button loadButton;
     /**
      * file data
      */
     private Calendar workData = null;
     private boolean applyToAll = false;
 	/*
 	 * Maps for storing name of file and path to file
 	 */
 	private final HashMap<String, String> folderFiles = new HashMap<String, String>();
     private final Map<String, String> loadedFiles = new LinkedHashMap<String, String>();
 
     private Button addAllFilesToLoaded;
 
     private Button removeAllFilesFromLoaded;
 
 	private Combo cDataset;
 	private String datasetName;
 	/* 
 	 * Default directory for file dialogs 
 	 */
 	private static String defaultDirectory = null;
     /**
      * wizard page if tems dialog was created from import wizard page
      */
     private WizardPage wizardPage = null;
 
     private final LinkedHashMap<String, Node> dataset = new LinkedHashMap<String, Node>();
 
     private Label ldataset;
     private boolean addToSelect=false;
 
     // private String extension = null;
 
 	/**
 	 * Creates a Shell and add GUI elements
 	 * 
 	 * @param shell shell
 	 * @param createNewShell is true than create a child shell of given shell for dialog
 	 */
 	
 	protected DriveDialog(Shell shell, boolean createNewShell) {
 		if (createNewShell) {
 			dialogShell = new Shell(shell);
 		}
 		else {
 			dialogShell = shell;
 		}
 		
 		//TODO move to constants
 		dialogShell.setMinimumSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
 		
 		dialogShell.setText(NeoLoaderPluginMessages.DriveDialog_DialogTitle);
 		
 		createControl(dialogShell);
 		createActions(dialogShell);
 	}
 	
 	/**
 	 * Creates a Dialog in parentShell
 	 * 
 	 * @param parentShell
 	 */
 	
 	public DriveDialog(Shell parentShell) {
 		this(parentShell, true);
 	}
 	
 	/**
 	 * Create a Dialog in new Shell
 	 * @param display
 	 */
 	
 	public DriveDialog(Display display) {
 		this(new Shell(display), false);
 	}
 
     /**
      * Constructor for launch from import wizards
      * 
      * @param parent - Composite
      * @param wizardPage wizards page
      */
     public DriveDialog(Composite parent, WizardPage wizardPage) {
         this.wizardPage = wizardPage;
         dialogShell = parent.getShell();
         dialogShell.setText(NeoLoaderPluginMessages.DriveDialog_DialogTitle);
         createControlForDialog(parent);
         createActions(dialogShell);
     }
 	/**
 	 * Opens a Dialog
 	 * 
 	 */
 	
 	public void open() {		
 		dialogShell.pack();
 		dialogShell.open();
 	}
 	
 	/**
 	 * Creates controls in parent Composite
 	 * 
 	 * @param parent parent Composite
 	 */
 	
 	private void createControl(Composite parent) {
 		parent.setLayout(new FormLayout());
 		parent.setLayoutData(new GridData(SWT.FILL));
 		
 		createSelectFileGroup(parent);
 		createFinishButtons(parent);
 	}
 
     /**
      * Creates controls in parent Composite
      * 
      * @param parent parent Composite
      */
 
     public void createControlForDialog(Composite parent) {
         parent.setLayout(new FormLayout());
         parent.setLayoutData(new GridData(SWT.FILL));
         loadButton = new Button(parent, SWT.NONE);
         cancelButton = loadButton;
         createSelectFileGroup(parent);
         cancelButton.moveBelow(null);
         cancelButton.setVisible(false);
     }
 
 	/**
 	 * Creates group for selecting files to load
 	 * 
 	 * @param parent
 	 */
 	
 	private void createSelectFileGroup(Composite parent) {
 		Composite prev = createDatasetRow(parent);
 		Group group = new Group(parent, SWT.NONE);		
 		group.setLayout(new FormLayout());
 		FormData data = new FormData(); 
         data.left = new FormAttachment(0, 2);
         data.top = new FormAttachment(prev, 2);
         data.width = FILE_GROUP_WIDTH;
         data.bottom = new FormAttachment(100,-2);
 		group.setLayoutData(data);
 		prev = createFolderSelectionComposite(group);
 		prev = createManipulationComposite(group,prev);
 		createFileToLoadComposite(group,prev);
 	}
 /**
  * Creates group for selecting
  * @param parent
  */
 	private Composite createDatasetRow(Composite parent) {
 		Composite panel = new Composite(parent, SWT.NONE);
 		panel.setLayout(new FormLayout());
 		FormData data = new FormData(); 
         data.left = new FormAttachment(0, 2);
         data.top = new FormAttachment(0, 2);
         data.right = new FormAttachment(100,-2);
 		panel.setLayoutData(data);
 
         ldataset = new Label(panel, SWT.NONE);
 		ldataset.setText(NeoLoaderPluginMessages.DriveDialog_DatasetLabel);
 		cDataset = new Combo(panel, SWT.NONE);
         FormData dLabel = new FormData(); 
         dLabel.left = new FormAttachment(0, 5);
         dLabel.top = new FormAttachment(cDataset, 5, SWT.CENTER);
         ldataset.setLayoutData(dLabel);
 
         FormData dCombo = new FormData(); 
         dCombo.left = new FormAttachment(ldataset, 5);
         dCombo.top = new FormAttachment(0, 2);
         dCombo.right = new FormAttachment(40,3);
         cDataset.setLayoutData(dCombo);
         
         //TODO: Check if the following line is needed
         //Transaction tx = NeoServiceProvider.getProvider().getService().beginTx();
         Traverser allDatasetTraverser = NeoCorePlugin.getDefault().getProjectService().getAllDatasetTraverser(
                 NeoServiceProvider.getProvider().getService().getReferenceNode());
         for (Node node : allDatasetTraverser) {
             dataset.put((String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME), node);
         }
         String[] items = dataset.keySet().toArray(new String[0]);
         Arrays.sort(items);
         cDataset.setItems(items);
         cDataset.addModifyListener(new ModifyListener() {
 
             @Override
             public void modifyText(ModifyEvent e) {
                 changeDatasetSelection();
             }
         });
         cDataset.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 changeDatasetSelection();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         return panel;
 	}
 
     /**
      * change dataset selection
      */
     protected void changeDatasetSelection() {
         try {
             Node datasetNode = dataset.get(cDataset.getText());
             if (datasetNode != null) {
                 ArrayList<String> filrsToRemove = new ArrayList<String>();
                 for (Map.Entry<String, String> entry : loadedFiles.entrySet()) {
                     if (!checkExtension(getFileExt(entry.getValue()))) {
                         filrsToRemove.add(entry.getKey());
                     }
                 }
                 for (String fileName : filrsToRemove) {
                     removeFileToLoad(fileName);
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Creates List for choosing Files
      * 
      * @param parent
      */
 	
 	private Composite createFolderSelectionComposite(Composite parent) {
 		Composite panel = new Composite(parent, SWT.NONE);
 		panel.setLayout(new FormLayout());
 		FormData data = new FormData(); 
        data.left = new FormAttachment(0, 2);
         data.top = new FormAttachment(0, 2);
         data.bottom = new FormAttachment(100,-2);
         data.width = 2*FILE_GROUP_WIDTH/5;
         panel.setLayoutData(data);
 		
 		folderFilesList = createSelectionList(panel, NeoLoaderPluginMessages.DriveDialog_FilesToChooseListLabel);
 		return panel;
 	}
 	
 	/**
 	 * Creates Buttons for manipulations
 	 * 
 	 * @param parent
	 */
	
 	private Composite createManipulationComposite(Composite parent, Composite prev) {
 		Composite panel = new Composite(parent, SWT.NONE);
 		panel.setLayout(new FormLayout());
 		FormData data = new FormData(); 
         data.left = new FormAttachment(prev, 2);
         data.top = new FormAttachment(0, 2);
         data.bottom = new FormAttachment(100,-2);
 		panel.setLayoutData(data);
 		
 		Composite choosePanel = new Composite(panel, SWT.NONE);
 		choosePanel.setLayout(new FormLayout());
 		data = new FormData(); 
         data.left = new FormAttachment(0, 2);
         data.top = new FormAttachment(0, 2);
         data.right = new FormAttachment(100,-2);
         choosePanel.setLayoutData(data);		
 		browseDialogButton = createChooseButton(choosePanel,null, NeoLoaderPluginMessages.DriveDialog_BrowseButtonText, SWT.TOP);
 		
 		Composite actionPanel = new Composite(panel, SWT.NONE);
 		actionPanel.setLayout(new FormLayout());
 		data = new FormData(); 
         data.left = new FormAttachment(0, 2);
         data.top = new FormAttachment(choosePanel, 20);
         data.right = new FormAttachment(100,-2);
        data.bottom = new FormAttachment(100,-2);
 		actionPanel.setLayoutData(data);
 		
		addFilesToLoaded = createChooseButton(actionPanel,null, NeoLoaderPluginMessages.DriveDialog_AddButtonText, SWT.CENTER);
        addAllFilesToLoaded = createChooseButton(actionPanel,addFilesToLoaded, NeoLoaderPluginMessages.DriveDialog_AddAllButtonText, SWT.CENTER);
		removeFilesFromLoaded = createChooseButton(actionPanel,addAllFilesToLoaded, NeoLoaderPluginMessages.DriveDialog_RemoveButtonText, SWT.CENTER);
        removeAllFilesFromLoaded = createChooseButton(actionPanel,removeAllFilesFromLoaded, NeoLoaderPluginMessages.DriveDialog_RemoveAllButtonText, SWT.CENTER);
         return panel;
 	}
 	
 	/**
 	 * Creates List for files to load
 	 * 
 	 * @param parent
 	 */
 	
 	private void createFileToLoadComposite(Composite parent, Composite prev) {
 		Composite panel = new Composite(parent, SWT.NONE);
 		panel.setLayout(new FormLayout());
 		FormData data = new FormData(); 
         data.left = new FormAttachment(prev, 2);
         data.top = new FormAttachment(0, 2);
         data.bottom = new FormAttachment(100,-2);
         data.width = 2*FILE_GROUP_WIDTH/5;
 		panel.setLayoutData(data);
 		
 		filesToLoadList = createSelectionList(panel, NeoLoaderPluginMessages.DriveDialog_FilesToLoadListLabel);
 	}
 	
 	/**
 	 * Creates 'Cancel' and 'Load' buttons
 	 * 
 	 * @param parent
 	 */
 	
 	private void createFinishButtons(Composite parent) {
 		Composite panel = new Composite(parent, SWT.NONE);
 		panel.setLayout(new FormLayout());
 		FormData data = new FormData();
         data.right = new FormAttachment(100, -2);
         data.bottom = new FormAttachment(100, -2);	
 		panel.setLayoutData(data);
 		
 		cancelButton = new Button(panel, SWT.CENTER);
 		cancelButton.setText(NeoLoaderPluginMessages.DriveDialog_CancelButtonText);
 		FormData cancelButtonFormData = new FormData();
 		cancelButtonFormData.right = new FormAttachment(100, -10);
 		cancelButtonFormData.bottom = new FormAttachment(100, -10);
 		cancelButtonFormData.top = new FormAttachment(0, 10);
 		cancelButtonFormData.width = 100;
 		cancelButton.setLayoutData(cancelButtonFormData);
 		
 		loadButton = new Button(panel, SWT.CENTER);
 		loadButton.setText(NeoLoaderPluginMessages.DriveDialog_LoadButtonText);
 		loadButton.setEnabled(false);
 		FormData loadButtonFormData = new FormData();
 		loadButtonFormData.right = new FormAttachment(cancelButton, -10);
 		loadButtonFormData.bottom = new FormAttachment(100, -10);
 		loadButtonFormData.top = new FormAttachment(0, 10);
 		loadButtonFormData.width = 100;
 		loadButton.setLayoutData(loadButtonFormData);
 	}
 	
 	/**
 	 * Create button for manipulation
 	 * 
 	 * @param parent parent Composite
 	 * @param label label of Button
 	 * @param position position of Button
 	 * @return created Button
 	 */
 	
 	private Button createChooseButton(Composite parent,Button prev, String label, int position) {
 		Button button = new Button(parent, SWT.NONE);
 		button.setText(label);
 		FormData data = new FormData();
         data.left = new FormAttachment(0, 2);
         data.right = new FormAttachment(100, -2);
         if (prev==null) {
            data.top = new FormAttachment(0, 2);
         }else{
            data.top = new FormAttachment(prev, 2);
         }
         button.setLayoutData(data);
 		
 		return button;
 	}
 	
 	/**
 	 * Creates a List with Label
 	 * 
 	 * @param parent parent Composite
 	 * @param label test of Label
 	 * @return created List
 	 */
 	
 	private List createSelectionList(Composite parent, String label) {
 		Label listLabel = new Label(parent, SWT.NONE);
 		listLabel.setText(label);
 		FormData data = new FormData(); 
         data.left = new FormAttachment(0, 2);
         data.top = new FormAttachment(0, 2);
         listLabel.setLayoutData(data);
 		
 		List list = new List(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
 		data = new FormData(); 
         data.left = new FormAttachment(0, 2);
         data.top = new FormAttachment(listLabel, 2);
         data.right = new FormAttachment(100,-2);
         data.bottom = new FormAttachment(100,-2);
 		list.setLayoutData(data);		
 		
 		return list;
 	}
 	
 	/**
 	 * Creates actions for buttons
 	 * 
 	 * @param parentShell Dialog Shell
 	 */
 	
 	private void createActions(final Shell parentShell) {
 		
 		//opens Dialog for choosing files
 		browseDialogButton.addSelectionListener(new SelectionAdapter() {
 			
 			@Override
             public void widgetSelected(SelectionEvent e) {
 				// User has selected to open a single file
 		        FileDialog dlg = new FileDialog(parentShell, SWT.OPEN | SWT.MULTI);
 				dlg.setText(NeoLoaderPluginMessages.DriveDialog_FileDialogTitle);
                 dlg.setFilterNames(getFilerNames());
                 dlg.setFilterExtensions(getFilterExtensions());
 		        dlg.setFilterPath(getDefaultDirectory());
 				
 		        String fn = dlg.open();
 		      
 		        if (fn != null) {
 		        	setDefaultDirectory(dlg.getFilterPath());
                     Pattern extRegex = Pattern.compile(".*\\.(\\w+)$");
 		        	FileFilter fileFilter = null;
 		        	for (String name : dlg.getFileNames()) {
 		        	        Matcher m = extRegex.matcher(name);
 		        	        String extension="";
 		        	        if(m.matches()) {
 		        	            extension = m.group(1);
 		        	        }
                         if (checkExtension(extension)) {
     		        		addFileToLoad(name, dlg.getFilterPath(), true);
     			        	if (cDataset.getText().isEmpty()){
     			        		cDataset.setText(name);
     			        	}
 		        	    }
 		        	}
 
 		        	File[] listFiles = new File(getDefaultDirectory()).listFiles(fileFilter);
                     for (File file : listFiles) {
 		        		addFileToChoose(file.getName(), getDefaultDirectory(), true);
 		        	}
 
 		        }
 			}
 
 		});
 		
 		//adds selected files to files to load
 		addFilesToLoaded.addSelectionListener(new SelectionAdapter() {
 			
 			@Override
             public void widgetSelected(SelectionEvent e) {
 				for (String fileName : folderFilesList.getSelection()) {
 					addFileToLoad(fileName);
 				}
 			}
 			
 		});
 		
         addAllFilesToLoaded.addSelectionListener(new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 for (String fileName : folderFilesList.getItems()) {
                     addFileToLoad(fileName);
                 }
             }
 
         });
 
 		//removes selected files from files to load
 		removeFilesFromLoaded.addSelectionListener(new SelectionAdapter() {
 			
 			@Override
             public void widgetSelected(SelectionEvent e) {
 				for (String fileName : filesToLoadList.getSelection()) {
 					removeFileToLoad(fileName);
 				}
 			}
 			
 		});
         removeAllFilesFromLoaded.addSelectionListener(new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 for (String fileName : filesToLoadList.getItems()) {
                     removeFileToLoad(fileName);
                 }
             }
 
         });
 		
 		//closes dialog
 		cancelButton.addSelectionListener(new SelectionAdapter() {
 			
 			@Override
             public void widgetSelected(SelectionEvent e) {
 				dialogShell.close();
 			}
 			
 		});
 		
 		//loads Drive data from chosen files		
 		loadButton.addSelectionListener(new SelectionAdapter() {
 			
 			@Override
             public void widgetSelected(SelectionEvent e) {
 				runLoadingJob();				
 			}
 
 
 			
 		});
 		cDataset.addModifyListener(new ModifyListener() {
 			
 			@Override
 			public void modifyText(ModifyEvent e) {
 				checkLoadButton();
 			}
 		});
 		
 	}
 
     /**
      * @param extension
      * @return
      */
     protected boolean checkExtension(String extension) {
         Node datasetNode = dataset.get(cDataset.getText());
         if (datasetNode == null) {
             if (loadedFiles.isEmpty()) {
                 return true;
             }
             String file = loadedFiles.values().iterator().next();
             return extension.equals(getFileExt(file));
         } else {
             return NeoUtils.getDatasetType(datasetNode, null).getExtension().equalsIgnoreCase(extension);
         }
     }
 
     /**
      * get file descriptions
      * 
      * @return
      */
     protected String[] getFilterExtensions() {
         Node datasetNode = dataset.get(cDataset.getText());
         if (datasetNode == null) {
             String[] fileExtensions = DriveTypes.getFileExtensions(DriveTypes.TEMS, DriveTypes.ROMES,DriveTypes.GPS, DriveTypes.NEMO1, DriveTypes.NEMO2);
             ArrayList<String> fel = new ArrayList<String>(fileExtensions.length + 1);
             fel.add(Drive_FILE_EXTENSIONS[6]);
             fel.addAll(Arrays.asList(fileExtensions));
             String[] result = fel.toArray(new String[0]);
             return result;
         } else {
             return DriveTypes.getFileExtensions(NeoUtils.getDatasetType(datasetNode, null));
         }
     }
 
     /**
      * get file extensions
      * 
      * @return
      */
     protected String[] getFilerNames() {
         Node datasetNode = dataset.get(cDataset.getText());
         if (datasetNode == null) {
             String[] fileDescriptions = DriveTypes.getFileDescriptions(DriveTypes.TEMS, DriveTypes.ROMES, DriveTypes.GPS, DriveTypes.NEMO1, DriveTypes.NEMO2);
             ArrayList<String> fdl = new ArrayList<String>(fileDescriptions.length + 1);
             fdl.add(Drive_FILE_NAMES[6]);
             fdl.addAll(Arrays.asList(fileDescriptions));
             String[] result = fdl.toArray(new String[0]);
             return result;
         } else {
             return DriveTypes.getFileDescriptions(NeoUtils.getDatasetType(datasetNode, null));
         }
     }
     
     public void runLoadingJob() {
 		datasetName=cDataset.getText();
 		LoadDriveJob job = new LoadDriveJob(dialogShell.getDisplay());
 		job.schedule(50);
 	}	
 	
 	/**
 	 * Loads Drive data from files
 	 * @param extension2 
 	 * 
 	 */
 	
     private void loadDriveData(Display display, IProgressMonitor monitor) {
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 		monitor.beginTask("Importing " + loadedFiles.size() + " drive test files", loadedFiles.size() * DriveLoader.WORKED_PER_FILE);
         DriveLoader driveLoader = null;
         long memBefore = calculateMemoryUsage();
         ArrayList<Long> memoryConsumption = new ArrayList<Long>();
         for (String fileName : loadedFiles.keySet()) {
             String filePath = loadedFiles.get(fileName);
 			try {
 			    String extension = getFileExt(filePath);
 			    
 			    Calendar time = null;
 			    if (!extension.equals(DriveTypes.GPS.getExtension())) {
 			        time = getDate(filePath);
 			        if (time == null) {
 			            continue;
 			        }
 			    }
                 if (extension.toLowerCase().equals("fmt")) {
                     driveLoader = new TEMSLoader(time, filePath, display, datasetName);
 			    } else if(extension.toLowerCase().equals("asc")) {
                     driveLoader = new RomesLoader(time, filePath, display, datasetName);
                 } else if(extension.toLowerCase().equals("gps")) {
                     driveLoader = new GPSLoader(time, filePath, display, datasetName);
                 }else if (extension.toLowerCase().equals("nmf")) {
                     driveLoader = new NemoLoader(time, filePath, display, datasetName);
                 } else if (extension.toLowerCase().equals("dt1")) {
                     driveLoader = new OldNemoVersionLoader(time, filePath, display, datasetName);
                 } else {
 			        NeoLoaderPlugin.error("Unsupported file extension: "+extension);
 			    }			
                 driveLoader.setTaskSetted(true);
 				driveLoader.run(monitor);
 				driveLoader.printStats(false);	// stats for this load
 		        long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
 		        driveLoader.clearCaches();
 		        memoryConsumption.add(memAfter);
                 NeoLoaderPlugin.debug("Memory usage was "+memAfter+" after loading "+filePath);
 				if(monitor.isCanceled()) break;
 			}
 			catch (IOException e) {
 				NeoLoaderPlugin.exception(e);
 			}
 		}
         handleSelect(monitor, driveLoader.getRootNodes());
         if (driveLoader != null) {
         	try {
         		DriveLoader.finishUpGis();
         	}
         	catch (MalformedURLException e) {
         		NeoLoaderPlugin.error(e.getMessage());
         	}
         }
 		DriveLoader.printTimesStats();
         long memAfter = calculateMemoryUsage();
         NeoLoaderPlugin.info("Memory profile for drive load went from "+memBefore+" to "+memAfter);
         long maxMem = 0;
 		for(long mem: memoryConsumption){
 		    NeoLoaderPlugin.info("\t"+(mem - memBefore)+" ("+(int)((mem - memBefore)/(1024*1024))+"MB)");
 		    if(mem>maxMem){
 		        maxMem = mem;
 		    }
 		}
         NeoLoaderPlugin.info("\t"+memText(memAfter, memBefore));
         NeoLoaderPlugin.info("Transient  memory change: "+memText(maxMem, memBefore));
         NeoLoaderPlugin.info("Persistent memory change: "+memText(memAfter, memBefore));
 
         if(driveLoader!=null) {
             driveLoader.addLayersToMap();
         }
 
         monitor.done();
     }
     /**
      * Handle select.
      *
      * @param monitor the monitor
      * @param rootNodes the root nodes
      */
     protected void handleSelect(IProgressMonitor monitor, Node[] rootNodes) {
         if (!addToSelect||monitor.isCanceled()){
             return;
         }
         LinkedHashSet<Node> sets = LoaderUtils.getSelectedNodes(NeoServiceProvider.getProvider().getService());
         for (Node node : rootNodes) {
             sets.add(node);
         }
         LoaderUtils.storeSelectedNodes(sets);
     }
     /**
      * Gets Data of TEMS file
      * 
      * @param filePath file path
      * @return data
      */
     private Calendar getDate(final String filePath) {
         final Pair<Boolean, Calendar> timePair = getTimeOfFile(filePath);
         if (!timePair.getLeft()) {
             if (workData != null && applyToAll) {
                 return workData;
             }
             Calendar result = ActionUtil.getInstance().runTaskWithResult(new RunnableWithResult<Calendar>() {
                 Calendar result;
                 private DateTimeDialogWithToggle dialog;
 
                 @Override
                 public void run() {
                     Calendar prefDate = timePair.getRight();
                     dialog = new DateTimeDialogWithToggle(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Date of file", null, String.format(
                             "File '%s' has no date information.", new File(filePath).getName()), "Please specify the date on which this data was collected:",
                             MessageDialogWithToggle.QUESTION, new String[] {IDialogConstants.CANCEL_LABEL, IDialogConstants.OK_LABEL}, 0,
                             "apply this date to all files in this load ", applyToAll, prefDate.get(Calendar.YEAR), prefDate.get(Calendar.MONTH), prefDate
                                     .get(Calendar.DAY_OF_MONTH));
                     dialog.open();
                     if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
                         result = dialog.getCallendar();
                         applyToAll = dialog.getToggleState();
                     } else {
                         result = null;
                     }
                 }
 
                 @Override
                 public Calendar getValue() {
                     return result;
                 }
             });
             if (result == null) {
                 return null;
             } else {
                 workData = result;
                 return result;
             }
 
         } else {
             return timePair.getRight();
         }
     }
 
     /**
      * Get time of drive file
      * 
      * @param filePath - full file name
      * @return Pair<is time correct?, time of drive file>
      */
     private Pair<Boolean, Calendar> getTimeOfFile(String filePath) {
         String extension = getFileExt(filePath).toLowerCase();
         File file = new File(filePath);
         boolean correctTime = false;
         Calendar calendar = new GregorianCalendar();
         // roms data
         if (extension.equals("asc")) {
             CharSequence filename = file.getName();
             Pattern p = Pattern.compile(ASC_PAT_FILE);
             Matcher m = p.matcher(filename);
             if (m.matches()) {
                 String dateText = m.group(1);
                 try {
                     calendar.setTimeInMillis(new SimpleDateFormat("yyMMdd").parse(dateText).getTime());
                     correctTime = true;
                 } catch (ParseException e) {
                     NeoLoaderPlugin.error("Wrong filename format: " + filename);
                     correctTime = false;
                     calendar.setTimeInMillis(file.lastModified());
                 }
             } else {
                 NeoLoaderPlugin.error("Wrong filename format: " + filename);
                 calendar.setTimeInMillis(file.lastModified());
                 correctTime = false;
             }
 
         }// TEMS
         else if (extension.equals("fmt")) {
             CharSequence filename = file.getName();
             Pattern p = Pattern.compile(FMT_PAT_FILE);
             Matcher m = p.matcher(filename);
             if (m.matches()) {
                 String dateText = m.group(1);
                 try {
                     calendar.setTimeInMillis(new SimpleDateFormat("yyyy-MM-dd").parse(dateText).getTime());
                     correctTime = true;
                 } catch (ParseException e) {
                     NeoLoaderPlugin.error("Wrong filename format: " + filename);
                     correctTime = false;
                     calendar.setTimeInMillis(file.lastModified());
                 }
             } else {
                 NeoLoaderPlugin.error("Wrong filename format: " + filename);
                 calendar.setTimeInMillis(file.lastModified());
                 correctTime = false;
             }
         }// NEMO 1.86
         else if (extension.equals("dt1")) {
             try {
                 BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                 String line = reader.readLine();
                 reader.close();
                 calendar.setTimeInMillis(new SimpleDateFormat("dd.MM.yyyy").parse(line.split("     ")[2]).getTime());
                 correctTime=true;
             } catch (Exception e) {
                 NeoLoaderPlugin.exception(e);
                 correctTime=false;
                 calendar.setTimeInMillis(file.lastModified());
             } 
         }//NEMO2.1
         else if (extension.equals("nmf")) {
             try {
                 BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                 String line;
                 boolean found=false;
                 int c=0;
                 while ((line = reader.readLine()) != null&&++c<MAX_NEMO_LINE_READ) {
                     if (line.startsWith("#START")){
                         found = true;
                         break;
                     }
                 }
                 reader.close();
                 if (found) {
                     CSVParser parser = new CSVParser(',');
                     String data = parser.parse(line).get(3);
                     calendar.setTimeInMillis(new SimpleDateFormat("dd.MM.yyyy").parse(data).getTime());
                     correctTime = true;
                 } else {
                     correctTime = false;
                     calendar.setTimeInMillis(file.lastModified());
                 }
             } catch (Exception e) {
                 NeoLoaderPlugin.exception(e);
                 correctTime=false;
                 calendar.setTimeInMillis(file.lastModified());
             } 
         }       
         roundTime(calendar);
         return new Pair<Boolean, Calendar>(correctTime, calendar);
     }
 
     /**
      * Round time in calendar to start of day
      * 
      * @param calendar - calendar
      */
     private void roundTime(Calendar calendar) {
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
     }
 
     /**
      * gets file extension
      * 
      * @param filePath file path
      * @return file ext
      */
     public String getFileExt(String filePath) {
         int del = 1 + filePath.lastIndexOf(".");
         String extension = filePath.substring(del);
         return extension;
     }
 	
 	private static String memText(long memAfter, long memBefore){
         return ""+(memAfter - memBefore)+" ("+(int)((memAfter - memBefore)/(1024*1024))+"MB)";
 	}
 
 	public static long calculateMemoryUsage() {
         System.gc(); System.gc(); System.gc(); System.gc();
         try {Thread.sleep(50);} catch (InterruptedException e) {}
         System.gc(); System.gc(); System.gc(); System.gc();
         try {Thread.sleep(50);} catch (InterruptedException e) {}
         return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
 	}
 	
     /**
      * Add file to 'Choose File' list
      * 
      * @param name name of file
      * @param path path to file
      * @param isNeedToConvert if true than convert name to 'fileName (filePath)'
      */
 	
 	private void addFileToChoose(String name, String path, boolean isNeedToConvert) {		
 		if (isNeedToConvert) {
 			path = path + File.separator + name;
 			
 			name = name + " (" + path + ")";
 		}
 		
 		//if list doesn't contain this item than add it
 		if (!folderFiles.containsKey(name) && !loadedFiles.containsKey(name)) {
 			folderFiles.put(name, path);
 			
 			folderFilesList.add(name);
 		}
 	}
 	
 	/**
 	 * Add file to 'Files To Load' list
 	 * 
 	 * @param name name of file
 	 * @param path path to file
 	 * @param isNeedToConvert if true than convert name to 'fileName (filePath)'
 	 */
 	
 	private void addFileToLoad(String name, String path, boolean isNeedToConvert) {
 		if (isNeedToConvert) {
 			path = path + File.separator + name;
 			name = name + " (" + path + ")";
 		}
 		
 		//if list doesn't contain this item than add it
 		if (!loadedFiles.containsKey(name)) {
 			loadedFiles.put(name, path);
 			
 			filesToLoadList.add(name);
 		}
 		
 		//if added file already contains in FolderFilesList than remove it from FolderFilesList
 		if (folderFiles.containsKey(name)) {
 			folderFiles.remove(name);
 			folderFilesList.remove(name);
 		}
 		checkLoadButton();
 	}
 	
 	/**
 	 * Replaces file from 'Files to Choose' list to 'Files to Load' list
 	 * 
 	 * @param name name of file
 	 */
 	
 	private void addFileToLoad(String name) {
 		String path = folderFiles.get(name);
         if (!checkExtension(getFileExt(path))) {
             return;
         }
 		folderFiles.remove(name);
 		folderFilesList.remove(name);
 		
 		addFileToLoad(name, path, false);
 	}
 	
 	/**
 	 * Replaces file from 'File to Load' list to 'Files to Choose'
 	 * 
 	 * @param name name of file
 	 */
 	
 	private void removeFileToLoad(String name) {
 		String path = loadedFiles.get(name);
 		loadedFiles.remove(name);
 		filesToLoadList.remove(name);
 		
 		addFileToChoose(name, path, false);
 		
 		checkLoadButton();
 	}
 	
 	/**
 	 * Checks is it possible to load files. If possible (e.g. 'File to Load' list is not empty)
 	 * than enable 'Load' button.
 	 * 
 	 */
 	
 	private void checkLoadButton() {
         boolean enabled = filesToLoadList.getItemCount() > 0 && !cDataset.getText().trim().isEmpty();
         loadButton.setEnabled(enabled/*&&combo.getText().length()>0*/);
         if (wizardPage!=null){
             wizardPage.setPageComplete(enabled);
         }
 	}
 	
 	/**
 	 * Returns Default Directory path for file dialogs in DriveLoad and NetworkLoad
 	 * 
 	 * @return default directory
 	 */
 	
 	public static String getDefaultDirectory() {
         return NeoLoaderPlugin.getDefault().getPluginPreferences().getString(AbstractLoader.DEFAULT_DIRRECTORY_LOADER);
 	}
 	
 	/**
 	 * Sets Default Directory path for file dialogs in DriveLoad and NetworkLoad
 	 * 
 	 * @param newDirectory new default directory
 	 */
 	
 	public static void setDefaultDirectory(String newDirectory) {
         NeoLoaderPlugin.getDefault().getPluginPreferences().setValue(AbstractLoader.DEFAULT_DIRRECTORY_LOADER, newDirectory);
 	}
 	
 	/**
 	 * Is DefaultDirectory set?
 	 *
 	 * @return is default directory set?
 	 */
 	public static boolean hasDefaultDirectory() {
 		return defaultDirectory != null;
 	}
 	
 	/**
 	 * Job for loading Drive data
 	 * 
 	 * @author Lagutko_N
 	 *
 	 */
 
 	private class LoadDriveJob extends Job {
 		
 		private final Display jobDisplay;
 
 		public LoadDriveJob(Display jobDisplay) {
 			super(NeoLoaderPluginMessages.DriveDialog_MonitorName);
 			this.jobDisplay = jobDisplay;					
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			try {
                 loadDriveData(jobDisplay, monitor);
                 
                 return Status.OK_STATUS;
             } catch (Exception e) {
                 e.printStackTrace();
                 // TODO Handle Exception
                 throw (RuntimeException) new RuntimeException( ).initCause( e );
             }
 		}
 
 				
 	}
 
     /**
      *
      * @param addToSelect
      */
     public void setAddToSelect(boolean addToSelect) {
         this.addToSelect = addToSelect;
         
     }
 }
