 package org.eclipse.jdt.internal.debug.ui.launcher;
 
 /*
  * (c) Copyright IBM Corp. 2000, 2001.
  * All Rights Reserved.
  */
  
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.ILaunchConfigurationTab;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 /**
  * Launch configuration tab for local java launches that presents the various paths (bootpath,
  * classpath and extension dirs path) and the environment variables to the user.
  */
 public class JavaEnvironmentTab extends JavaLaunchConfigurationTab {
 	
 	// Paths UI widgets
 	private TabFolder fPathTabFolder;
 	private TabItem fBootPathTabItem;
 	private TabItem fClassPathTabItem;
 	private TabItem fExtensionPathTabItem;
 	private List fBootPathList;
 	private List fClassPathList;
 	private List fExtensionPathList;
 	private Button fClassPathDefaultButton;
 	private Button fPathAddArchiveButton;
 	private Button fPathAddDirectoryButton;
 	private Button fPathRemoveButton;
 	private Button fPathMoveUpButton;
 	private Button fPathMoveDownButton;
 	
 	// Environment variables UI widgets
 	private Label fEnvLabel;
 	private Table fEnvTable;
 	private Button fEnvAddButton;
 	private Button fEnvEditButton;
 	private Button fEnvRemoveButton;
 	
 	// Remember last directory when browsing for archives or directories
 	private String fLastBrowsedDirectory;
 	
 	// Listener for list selection events
 	private SelectionAdapter fListSelectionAdapter;
 	
 	private static final String EMPTY_STRING = "";
 
 	// Constants used in reading & persisting XML documents containing path entries
 	private static final String PATH_XML_ENTRIES = "pathEntries";
 	private static final String PATH_XML_ENTRY = "pathEntry";
 	private static final String PATH_XML_PATH = "path";
 	
 	// Constants used in reading & persisting XML documents containing env. vars.
 	private static final String ENV_XML_ENTRIES = "envVarEntries";
 	private static final String ENV_XML_ENTRY = "envVarEntry";
 	private static final String ENV_XML_NAME = "envVarName";
 	private static final String ENV_XML_VALUE = "envVarValue";
 		
 	/**
 	 * @see ILaunchConfigurationTab#createTabControl(ILaunchConfigurationDialog, TabItem)
 	 */
 	public void createControl(Composite parent) {
 		
 		Composite comp = new Composite(parent, SWT.NONE);
 		setControl(comp);
 		GridLayout topLayout = new GridLayout();
 		topLayout.numColumns = 2;
 		comp.setLayout(topLayout);		
 		GridData gd;
 		
 		createVerticalSpacer(comp, 2);
 		
 		fPathTabFolder = new TabFolder(comp, SWT.NONE);
 		gd = new GridData(GridData.FILL_BOTH);
 		fPathTabFolder.setLayoutData(gd);
 		
 		Composite classPathComp = new Composite(fPathTabFolder, SWT.NONE);
 		GridLayout classPathLayout = new GridLayout();
 		classPathComp.setLayout(classPathLayout);
 		
 		fClassPathList = new List(classPathComp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
 		gd = new GridData(GridData.FILL_BOTH);
 		fClassPathList.setLayoutData(gd);
 		fClassPathList.setData(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH);
 		fClassPathList.addSelectionListener(getListSelectionAdapter());
 		fClassPathTabItem = new TabItem(fPathTabFolder, SWT.NONE, 0);
 		fClassPathTabItem.setText("Class&path");
 		fClassPathTabItem.setControl(classPathComp);
 		fClassPathTabItem.setData(fClassPathList);
 		
 		fClassPathDefaultButton = new Button(classPathComp, SWT.CHECK);
 		fClassPathDefaultButton.setText("&Use default classpath");
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
 		fClassPathDefaultButton.setLayoutData(gd);
 		fClassPathDefaultButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent evt) {
 				handleClassPathDefaultButtonSelected();
 			}
 		});
 		
 		fBootPathList = new List(fPathTabFolder, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
 		gd = new GridData(GridData.FILL_BOTH);
 		fBootPathList.setLayoutData(gd);
 		fBootPathList.setData(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH);
 		fBootPathList.addSelectionListener(getListSelectionAdapter());
 		fBootPathTabItem = new TabItem(fPathTabFolder, SWT.NONE, 1);
 		fBootPathTabItem.setText("&Bootpath");
 		fBootPathTabItem.setControl(fBootPathList);
 		fBootPathTabItem.setData(fBootPathList);
 		
 		fExtensionPathList = new List(fPathTabFolder, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
 		gd = new GridData(GridData.FILL_BOTH);
 		fExtensionPathList.setLayoutData(gd);
 		fExtensionPathList.setData(IJavaLaunchConfigurationConstants.ATTR_EXTPATH);
 		fExtensionPathList.addSelectionListener(getListSelectionAdapter());
 		fExtensionPathTabItem = new TabItem(fPathTabFolder, SWT.NONE, 2);
 		fExtensionPathTabItem.setText("E&xtension path");
 		fExtensionPathTabItem.setControl(fExtensionPathList);
 		fExtensionPathTabItem.setData(fExtensionPathList);
 		
 		Composite pathButtonComp = new Composite(comp, SWT.NONE);
 		GridLayout pathButtonLayout = new GridLayout();
 		pathButtonLayout.marginHeight = 0;
 		pathButtonLayout.marginWidth = 0;
 		pathButtonComp.setLayout(pathButtonLayout);
 		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
 		pathButtonComp.setLayoutData(gd);
 		
 		createVerticalSpacer(pathButtonComp, 1);
 		
 		fPathAddArchiveButton = new Button(pathButtonComp, SWT.PUSH);
 		fPathAddArchiveButton.setText("Add arc&hive...");
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fPathAddArchiveButton.setLayoutData(gd);
 		fPathAddArchiveButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent evt) {
 				handlePathAddArchiveButtonSelected();
 			}
 		});
 		
 		fPathAddDirectoryButton = new Button(pathButtonComp, SWT.PUSH);
 		fPathAddDirectoryButton.setText("Add &folder...");
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fPathAddDirectoryButton.setLayoutData(gd);
 		fPathAddDirectoryButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent evt) {
 				handlePathAddDirectoryButtonSelected();
 			}
 		});
 		
 		fPathRemoveButton = new Button(pathButtonComp, SWT.PUSH);
 		fPathRemoveButton.setText("&Remove");
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fPathRemoveButton.setLayoutData(gd);
 		fPathRemoveButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent evt) {
 				handlePathRemoveButtonSelected();
 			}
 		});
 		
 		Composite moveButtonComp = new Composite(pathButtonComp, SWT.NONE);
 		GridLayout moveButtonLayout = new GridLayout();
 		moveButtonLayout.marginHeight = 0;
 		moveButtonLayout.marginWidth = 0;
 		moveButtonLayout.numColumns = 2;
 		moveButtonComp.setLayout(moveButtonLayout);
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		moveButtonComp.setLayoutData(gd);
 		
 		fPathMoveUpButton = new Button(moveButtonComp, SWT.PUSH);
 		fPathMoveUpButton.setImage(JavaDebugImages.get(JavaDebugImages.IMG_OBJS_UP_NAV));
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fPathMoveUpButton.setLayoutData(gd);
 		fPathMoveUpButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent evt) {
 				handlePathMoveButtonSelected(true);
 			}
 		});
 		
 		fPathMoveDownButton = new Button(moveButtonComp, SWT.PUSH);
 		fPathMoveDownButton.setImage(JavaDebugImages.get(JavaDebugImages.IMG_OBJS_DOWN_NAV));
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fPathMoveDownButton.setLayoutData(gd);
 		fPathMoveDownButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent evt) {
 				handlePathMoveButtonSelected(false);
 			}
 		});
 		
 		setPathButtonsEnableState();
 		
 		createVerticalSpacer(comp, 2);
 		
 		fEnvLabel = new Label(comp, SWT.NONE);
 		fEnvLabel.setText("Environment variables:");
 		gd = new GridData();
 		gd.horizontalSpan = 2;
 		fEnvLabel.setLayoutData(gd);
 		
 		fEnvTable = new Table(comp, SWT.BORDER | SWT.MULTI);
 		fEnvTable.setData(IJavaLaunchConfigurationConstants.ATTR_ENVIRONMENT_VARIABLES);
 		TableLayout tableLayout = new TableLayout();
 		fEnvTable.setLayout(tableLayout);
 		gd = new GridData(GridData.FILL_BOTH);
 		fEnvTable.setLayoutData(gd);
 		TableColumn column1 = new TableColumn(fEnvTable, SWT.NONE);
 		column1.setText("Name");
 		TableColumn column2 = new TableColumn(fEnvTable, SWT.NONE);
 		column2.setText("Value");		
 		tableLayout.addColumnData(new ColumnWeightData(100));
 		tableLayout.addColumnData(new ColumnWeightData(100));
 		fEnvTable.setHeaderVisible(true);
 		fEnvTable.setLinesVisible(true);
 		fEnvTable.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent evt) {
 				setEnvButtonsEnableState();
 			}
 		});
 	
 		Composite envButtonComp = new Composite(comp, SWT.NONE);
 		GridLayout envButtonLayout = new GridLayout();
 		envButtonLayout.marginHeight = 0;
 		envButtonLayout.marginWidth = 0;
 		envButtonComp.setLayout(envButtonLayout);
 		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
 		envButtonComp.setLayoutData(gd);
 		
 		fEnvAddButton = new Button(envButtonComp, SWT.PUSH);
 		fEnvAddButton.setText("A&dd...");
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fEnvAddButton.setLayoutData(gd);
 		fEnvAddButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent evt) {
 				handleEnvAddButtonSelected();
 			}
 		});
 		
 		fEnvEditButton = new Button(envButtonComp, SWT.PUSH);
 		fEnvEditButton.setText("Ed&it...");
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fEnvEditButton.setLayoutData(gd);
 		fEnvEditButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent evt) {
 				handleEnvEditButtonSelected();
 			}
 		});
 		
 		fEnvRemoveButton = new Button(envButtonComp, SWT.PUSH);
 		fEnvRemoveButton.setText("Rem&ove");
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fEnvRemoveButton.setLayoutData(gd);
 		fEnvRemoveButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent evt) {
 				handleEnvRemoveButtonSelected();
 			}
 		});
 		
 		setEnvButtonsEnableState();
 		
 	}
 	
 	/**
 	 * @see ILaunchConfigurationTab#dispose()
 	 */
 	public void dispose() {
 	}
 	
 	protected void updateConfigFromPathList(List listWidget, ILaunchConfigurationWorkingCopy config) {
 		String[] items = listWidget.getItems();
 		java.util.List listDataStructure = createListFromArray(items);
 		String attributeID = (String) listWidget.getData();
 		if (listDataStructure.isEmpty()) {
 			config.setAttribute(attributeID, (java.util.List )null);			
 		} else {
 			config.setAttribute(attributeID, listDataStructure);
 		}
 	}
 	
 	protected java.util.List createListFromArray(Object[] array) {
 		java.util.List list = new ArrayList(array.length);
 		for (int i = 0; i < array.length; i++) {
 			list.add(array[i]);
 		}
 		return list;
 	}
 	
 	protected void updateConfigFromEnvTable(Table tableWidget, ILaunchConfigurationWorkingCopy config) {
 		TableItem[] items = tableWidget.getItems();
 		Map map = createMapFromTableItems(items);
 		String attributeID = (String) tableWidget.getData();
 		config.setAttribute(attributeID, map);			
 	}
 	
 	protected Map createMapFromTableItems(TableItem[] items) {
 		Map map = new HashMap(items.length);
 		for (int i = 0; i < items.length; i++) {
 			TableItem item = items[i];
 			String key = item.getText(0);
 			String value = item.getText(1);
 			map.put(key, value);
 		}		
 		return map;
 	}
 	
 	/**
 	 * @see JavaLaunchConfigurationTab#updateWidgetsFromConfig(ILaunchConfiguration)
 	 */
 	protected void updateWidgetsFromConfig(ILaunchConfiguration config) {
 		updateBootPathFromConfig(config);
 		updateClassPathFromConfig(config);
 		updateExtensionPathFromConfig(config);
 		updateEnvVarsFromConfig(config);
 	}
 	
 	protected void updateBootPathFromConfig(ILaunchConfiguration config) {
 		try {
 			java.util.List bootpath = null;
 			if (config != null) {
 				bootpath = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH, (java.util.List)null);
 			}
 			updatePathList(bootpath, fBootPathList);
 		} catch (CoreException ce) {			
 		}
 	}
 	
 	protected void updateClassPathFromConfig(ILaunchConfiguration config) {		
 		try {
 			java.util.List classpath = null;
 			if (config != null) {
 				classpath = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, (java.util.List)null);
 			}
 			if (classpath == null) {
 				fClassPathDefaultButton.setSelection(true);
 				handleClassPathDefaultButtonSelected();
 			} else {
 				updatePathList(classpath, fClassPathList);
 			}
 		} catch (CoreException ce) {			
 		}
 	}
 	
 	protected void updateExtensionPathFromConfig(ILaunchConfiguration config) {		
 		try {
 			java.util.List extpath = null;
 			if (config != null) {
 				extpath = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_EXTPATH, (java.util.List)null);
 			}
 			updatePathList(extpath, fExtensionPathList);
 		} catch (CoreException ce) {			
 		}
 	}
 	
 	protected void updateEnvVarsFromConfig(ILaunchConfiguration config) {
 		Map envVars = null;
 		try {
 			if (config != null) {
 				envVars = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_ENVIRONMENT_VARIABLES, (Map)null);
 			}
 			updateTable(envVars, fEnvTable);
 		} catch (CoreException ce) {
 		}
 	}
 	
 	protected void updatePathList(java.util.List listStructure, List listWidget) {
 		if (listStructure == null) {
 			return;
 		}
 		String[] stringArray = new String[listStructure.size()];
 		listStructure.toArray(stringArray);
 		listWidget.setItems(stringArray);
 	}
 	
 	protected void updateTable(Map map, Table tableWidget) {
		tableWidget.removeAll();
 		if (map == null) {
 			return;
 		}
 		Iterator iterator = map.keySet().iterator();
 		while (iterator.hasNext()) {
 			String key = (String) iterator.next();
 			String value = (String) map.get(key);
 			TableItem tableItem = new TableItem(tableWidget, SWT.NONE);
 			tableItem.setText(new String[] {key, value});			
 		}
 	}
 	
 	/**
 	 * Create some empty space 
 	 */
 	protected void createVerticalSpacer(Composite comp, int colSpan) {
 		Label label = new Label(comp, SWT.NONE);
 		GridData gd = new GridData();
 		gd.horizontalSpan = colSpan;
 		label.setLayoutData(gd);
 	}
 	
 	/**
 	 * Handle list selection events (for the active list only).
 	 */
 	protected void handleListSelectionChanged(SelectionEvent evt) {
 		List listWidget = (List) evt.widget;
 		if (getActiveListWidget() != listWidget) {
 			return;
 		}		
 		setPathButtonsEnableState();
 	}
 
 	/**
 	 * Show the user a file dialog, and let them choose one or more archive files.
 	 * All selected files are added to the end of the active list.
 	 */
 	protected void handlePathAddArchiveButtonSelected() {
 		// Get current list entries, and use first selection as insertion point
 		List listWidget = getActiveListWidget();
 		java.util.List previousItems = Arrays.asList(listWidget.getItems());
 		int[] selectedIndices = listWidget.getSelectionIndices();
 		int insertAtIndex = listWidget.getItemCount();
 		if (selectedIndices.length > 0) {
 			insertAtIndex = selectedIndices[0];
 		}
 		
 		// Show the dialog and get the results
 		FileDialog dialog= new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
 		dialog.setFilterPath(fLastBrowsedDirectory);
 		dialog.setText("Select archive file");
 		dialog.setFilterExtensions(new String[] { "*.jar;*.zip"});   //$NON-NLS-1$
 		dialog.open();
 		String[] results = dialog.getFileNames();
 		int resultCount = results.length;
 		if ((results == null) || (resultCount < 1)) {
 			return;
 		}
 		
 		// Insert the results at the insertion point
 		fLastBrowsedDirectory = dialog.getFilterPath();
 		int nextInsertionPoint = insertAtIndex;
 		for (int i = 0; i < resultCount; i++) {
 			String result = results[i];
 			if (!previousItems.contains(result)) {
 				File file = new File(fLastBrowsedDirectory, result);
 				listWidget.add(file.getAbsolutePath(), nextInsertionPoint++);
 			}
 		}
 		if (insertAtIndex != nextInsertionPoint) {
 			listWidget.setSelection(insertAtIndex, nextInsertionPoint - 1);
 			setPathButtonsEnableState();
 		}
 		updateLaunchConfigurationDialog();
 	}
 	
 	/**
 	 * Show the user a directory dialog, and let them choose a directory.
 	 * The selected directory is added to the end of the active list.
 	 */
 	protected void handlePathAddDirectoryButtonSelected() {
 		// Get current list entries, and use first selection as insertion point
 		List listWidget = getActiveListWidget();
 		java.util.List previousItems = Arrays.asList(listWidget.getItems());
 		int[] selectedIndices = listWidget.getSelectionIndices();
 		int insertAtIndex = listWidget.getItemCount();
 		if (selectedIndices.length > 0) {
 			insertAtIndex = selectedIndices[0];
 		}
 		
 		// Show the dialog and get the result
 		DirectoryDialog dialog= new DirectoryDialog(getShell(), SWT.OPEN);
 		dialog.setFilterPath(fLastBrowsedDirectory);
 		dialog.setMessage("Select directory to add to path");
 		String result = dialog.open();
 		if (result == null) {
 			return;
 		}
 		
 		// Insert the result at the insertion point
 		if (!previousItems.contains(result)) {
 			listWidget.add(result, insertAtIndex);
 			listWidget.setSelection(insertAtIndex);
 			setPathButtonsEnableState();
 			fLastBrowsedDirectory = result;
 		}
 		
 	}
 
 	/**
 	 * Remove all selected list items.
 	 */
 	protected void handlePathRemoveButtonSelected() {
 		List listWidget = getActiveListWidget();
 		int[] selectedIndices = listWidget.getSelectionIndices();
 		listWidget.remove(selectedIndices);
 		setPathButtonsEnableState();
 		updateLaunchConfigurationDialog();
 	}
 	
 	/**
 	 * Move the (single) selected list item up or down one position.
 	 */
 	protected void handlePathMoveButtonSelected(boolean moveUp) {
 		List listWidget = getActiveListWidget();
 		int selectedIndex = listWidget.getSelectionIndex();
 		int targetIndex = moveUp ? selectedIndex -1 : selectedIndex + 1;
 		if ((targetIndex < 0) || (targetIndex >= listWidget.getItemCount())) {
 			return;
 		}
 		String targetText = listWidget.getItem(targetIndex);
 		String selectedText = listWidget.getItem(selectedIndex);
 		listWidget.setItem(targetIndex, selectedText);
 		listWidget.setItem(selectedIndex, targetText);
 		listWidget.setSelection(targetIndex);
 		setPathButtonsEnableState();
 		updateLaunchConfigurationDialog();
 	}
 	
 	/**
 	 * Toggle the state of the class path default checkbox.
 	 */
 	protected void handleClassPathDefaultButtonSelected() {
 		boolean selected = fClassPathDefaultButton.getSelection();
 		if (selected) {
 			fClassPathList.setEnabled(false);
 		} else {
 			fClassPathList.setEnabled(true);	
 		}
 
 		if (selected) {
 			try {
 				IJavaProject javaProject = getJavaProject();
 				String[] defaultClassPath = new String[0];
 				if (javaProject != null) {
 					defaultClassPath = JavaRuntime.computeDefaultRuntimeClassPath(javaProject);
 				}
 				fClassPathList.setItems(defaultClassPath);
 			} catch (CoreException ce) {			
 			}
 		}
 		setPathButtonsEnableState();
 		updateLaunchConfigurationDialog();
 	}
 	
 	/**
 	 * Returns the Java project for this tab - retrieves
 	 * it from the main tab.
 	 */
 	protected IJavaProject getJavaProject() {
 		ILaunchConfigurationTab[] tabs = getLaunchConfigurationDialog().getTabs();
 		for (int i = 0; i < tabs.length; i++) {
 			if (tabs[i] instanceof JavaMainTab) {
 				return ((JavaMainTab)tabs[i]).getJavaProject();
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Set the enabled state of the four path-related buttons based on the
 	 * selection in the active List widget.
 	 */
 	protected void setPathButtonsEnableState() {
 		List listWidget = getActiveListWidget();
 		boolean useDefault = isUseDefaultClasspath();
 		
 		int selectCount = listWidget.getSelectionIndices().length;
 		boolean selection = selectCount > 0;
 		boolean singleSelection = selectCount == 1;
 		
 		int selectedIndex = listWidget.getSelectionIndex();
 		boolean firstSelected = selectedIndex == 0;
 		boolean lastSelcted = selectedIndex == (listWidget.getItemCount() - 1);
 		
 		fPathRemoveButton.setEnabled(!useDefault && selection);
 		fPathMoveUpButton.setEnabled(!useDefault && singleSelection && !firstSelected);
 		fPathMoveDownButton.setEnabled(!useDefault && singleSelection && !lastSelcted);
 		fPathAddArchiveButton.setEnabled(!useDefault);
 		fPathAddDirectoryButton.setEnabled(!useDefault);
 	
 	}
 	
 	protected void handleEnvAddButtonSelected() {
 		NameValuePairDialog dialog = new NameValuePairDialog(getShell(), 
 												"Add new environment variable", 
 												new String[] {"Name", "Value"}, 
 												new String[] {"", ""});
 		doEnvVarDialog(dialog);
 		setEnvButtonsEnableState();
 	}
 	
 	protected void handleEnvEditButtonSelected() {
 		TableItem selectedItem = fEnvTable.getSelection()[0];
 		String name = selectedItem.getText(0);
 		String value = selectedItem.getText(1);
 		NameValuePairDialog dialog = new NameValuePairDialog(getShell(), 
 												"Edit environment variable", 
 												new String[] {"Name", "Value"}, 
 												new String[] {name, value});
 		doEnvVarDialog(dialog);		
 	}
 	
 	/**
 	 * Show the specified dialog and update the env var table based on its results.
 	 */
 	protected void doEnvVarDialog(NameValuePairDialog dialog) {
 		if (dialog.open() != Window.OK) {
 			return;
 		}
 		String[] nameValuePair = dialog.getNameValuePair();
 		TableItem tableItem = getTableItemForName(nameValuePair[0]);
 		if (tableItem != null) {
 			tableItem.setText(1, nameValuePair[1]);
 		} else {
 			tableItem = new TableItem(fEnvTable, SWT.NONE);
 			tableItem.setText(nameValuePair);
 		}
 		fEnvTable.setSelection(new TableItem[] {tableItem});
 		updateLaunchConfigurationDialog();	
 	}
 
 	protected void handleEnvRemoveButtonSelected() {
 		int[] selectedIndices = fEnvTable.getSelectionIndices();
 		fEnvTable.remove(selectedIndices);
 		setEnvButtonsEnableState();
 	}
 	
 	/**
 	 * Set the enabled state of the three environment variable-related buttons based on the
 	 * selection in the Table widget.
 	 */
 	protected void setEnvButtonsEnableState() {
 		int selectCount = fEnvTable.getSelectionIndices().length;
 		if (selectCount < 1) {
 			fEnvEditButton.setEnabled(false);
 			fEnvRemoveButton.setEnabled(false);
 		} else {
 			fEnvRemoveButton.setEnabled(true);
 			if (selectCount == 1) {
 				fEnvEditButton.setEnabled(true);
 			} else {
 				fEnvEditButton.setEnabled(false);
 			}
 		}		
 		fEnvAddButton.setEnabled(true);
 	}
 	
 	/**
 	 * Helper method that indicates whether the specified env var name is already present 
 	 * in the env var table.
 	 */
 	protected TableItem getTableItemForName(String candidateName) {
 		TableItem[] items = fEnvTable.getItems();
 		for (int i = 0; i < items.length; i++) {
 			String name = items[i].getText(0);
 			if (name.equals(candidateName)) {
 				return items[i];
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Return the List widget that is currently visible in the paths tab folder.
 	 */
 	protected List getActiveListWidget() {
 		int selectedTabIndex = fPathTabFolder.getSelectionIndex();
 		if (selectedTabIndex < 0) {
 			return null;
 		}
 		TabItem tabItem = fPathTabFolder.getItem(selectedTabIndex);
 		List listWidget = (List)tabItem.getData();
 		return listWidget;
 	}
 	
 	/**
 	 * Practice lazy creation the singleton selection adapter for all path lists.
 	 */
 	protected SelectionAdapter getListSelectionAdapter() {
 		if (fListSelectionAdapter == null) {
 			fListSelectionAdapter = new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent evt) {
 					handleListSelectionChanged(evt);
 				}
 			};
 		}
 		return fListSelectionAdapter;
 	}
 		
 	/**
 	 * Initialize defaults based on the given java element.
 	 */
 	protected void initializeDefaults(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {		
 		initializeHardCodedDefaults(config);
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
 	 */
 	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
 		IJavaElement javaElement = getContext();
 		if (javaElement != null) {
 			initializeDefaults(javaElement, config);
 		} else {
 			initializeHardCodedDefaults(config);	
 		}
 	}
 	
 	/**
 	 * Initialize those attributes whose default values are independent of any context.
 	 */
 	protected void initializeHardCodedDefaults(ILaunchConfigurationWorkingCopy config) {					
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#isPageComplete()
 	 */
 	public boolean isValid() {
 		
 		setErrorMessage(null);
 		setMessage(null);
 		
 		return true;
 	}
 	
 	/**
 	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
 	 */
 	public void initializeFrom(ILaunchConfiguration configuration) {
 		updateBootPathFromConfig(configuration);
 		updateClassPathFromConfig(configuration);
 		updateEnvVarsFromConfig(configuration);
 		updateExtensionPathFromConfig(configuration);
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
 	 */
 	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
 		updateConfigFromEnvTable(fEnvTable, configuration);
 		if (isUseDefaultClasspath()) {
 			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, (String)null);
 		} else {
 			updateConfigFromPathList(fClassPathList, configuration);
 		}
 		updateConfigFromPathList(fBootPathList, configuration);
 		updateConfigFromPathList(fExtensionPathList, configuration);
 	}
 	
 	/**
 	 * Returns whether the 'use default classpath' button is checked
 	 */
 	protected boolean isUseDefaultClasspath() {
 		return fClassPathDefaultButton.getSelection();
 	}
 
 }
