 /*******************************************************************************
  * Copyright (c) 2012 eclipse-ccase.sourceforge.net.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  *
  * Contributors:
  *     eraonel - inital API and implementation
  *     IBM Corporation - concepts and ideas from Eclipse
  *******************************************************************************/
 package net.sourceforge.eclipseccase.ui.preferences;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 
 import net.sourceforge.eclipseccase.diff.PreferenceHelper;
 
 import java.text.Collator;
 import java.util.*;
 import net.sourceforge.eclipseccase.ClearCasePlugin;
 import net.sourceforge.eclipseccase.IClearCasePreferenceConstants;
 import org.eclipse.jface.preference.*;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 /**
  * @author eraonel
  *
  */
 /**
  * The main preference page for the Eclipse ClearCase integration.
  */
 public class DiffMergePreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IClearCasePreferenceConstants {
 
 	private String selectedTool = "";// Initial value.
 
 	private static Map<String, String> toolPathMap = new LinkedHashMap<String, String>();
 
 	private BooleanFieldEditor useExternal;
 
 	private ComboViewer comboViewer;
 
 	private Text execPath;
 
 	private static final int SPAN = 1;
 
 	public static final String TOOL_IBM = "ibm";
 
 	public static final String TOOL_KDIFF = "kdiff3";
 
 	/**
 	 * Creates a new instance.
 	 */
 	public DiffMergePreferencePage() {
 		setDescription(PreferenceMessages.getString("DiffMergePreferencePage.Description")); //$NON-NLS-1$
 		// Set the preference store for the preference page.
 		setPreferenceStore(new ClearCasePreferenceStore());
 	}
 
 	@Override
 	protected Control createContents(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		useExternal = new BooleanFieldEditor(COMPARE_EXTERNAL, PreferenceMessages.getString("Preferences.General.CompareWithExternalTool"), //$NON-NLS-1$
 				composite);
 		addFieldEditor(useExternal);
 		// TODO Auto-generated method stub
 		Group groupDiff = new Group(composite, SWT.NULL);
 		GridLayout layoutDiff = new GridLayout();
 		layoutDiff.numColumns = 1;
 		groupDiff.setLayout(layoutDiff);
 		GridData dataDiff = new GridData();
 		dataDiff.horizontalAlignment = GridData.FILL;
 		groupDiff.setLayoutData(dataDiff);
 		groupDiff.setText("External Diff tool settings:");
 		comboViewer = new ComboViewer(groupDiff);
 		Combo combo = comboViewer.getCombo();
 		combo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
 		comboViewer.setContentProvider(new IStructuredContentProvider() {
 			String[] vals;
 
 			public void dispose() {
 			}
 
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 				vals = (String[]) newInput;
 			}
 
 			public Object[] getElements(Object inputElement) {
 				return vals;
 			}
 		});
 		// This is called when I select the page.
 		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent evt) {
 
 				ISelection selection = evt.getSelection();
 				if (selection instanceof StructuredSelection) {
 					StructuredSelection sel = (StructuredSelection) selection;
 					if (!selection.isEmpty()) {
 
 						selectedTool = sel.getFirstElement().toString();
 						if (selectedTool.equals(TOOL_IBM)) {
 							// Sine we already have cleartool path no need to
 							// input.
 							execPath.setEnabled(false);
 						} else {
 							execPath.setEnabled(true);
 						}
 					}
 
 					if (selectedTool.equals(TOOL_IBM)) {
 						// Sine we already have cleartool path no need to input.
 						execPath.setEnabled(false);
 					}
 
 					// set matching execPath
 					if (selectedTool != null & execPath != null) {
 						toolPathMap = PreferenceHelper.strToMap(getPreferenceStore().getString(IClearCasePreferenceConstants.EXTERNAL_DIFF_TOOL_EXEC_PATH));
 						execPath.setText(PreferenceHelper.getExecPath(selectedTool, toolPathMap));
 					}
 
 				}
 			}
 		});
 		createLabel(groupDiff, PreferenceMessages.getString("DiffMergePreferencePage.External.Diff.Tool.ExecPath"), SPAN); //$NON-NLS-1$
 		execPath = new Text(groupDiff, SWT.BORDER);
 		execPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		initializeValues();
 		//TODO: Not ready since no merge support.
 //		Group groupMerge = new Group(composite, SWT.NULL);
 //		GridLayout layoutMerge = new GridLayout();
 //		layoutMerge.numColumns = 3;
 //		groupMerge.setLayout(layoutMerge);
 //		GridData dataMerge = new GridData();
 //		dataMerge.horizontalAlignment = GridData.FILL;
 //		groupMerge.setLayoutData(dataMerge);
 //		groupMerge.setText("External Merge tool settings:");
 		return parent;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
 	 */
 	public void init(IWorkbench workbench) {
 		// ignore
 	}
 
 	/**
 	 * Initializes values for non-FieldEditors.
 	 */
 	private void initializeValues() {
 		final IPreferenceStore store = getPreferenceStore();
 		selectedTool = store.getString(IClearCasePreferenceConstants.EXTERNAL_DIFF_TOOL);
 		ArrayList<String> tools = new ArrayList<String>();
 		tools.add(TOOL_IBM);
 		tools.add(TOOL_KDIFF);
 		Collator collator = Collator.getInstance();
 		collator.setStrength(Collator.PRIMARY);
 		Collections.sort(tools, collator);
 		comboViewer.setInput(tools.toArray(new String[tools.size()]));
 		comboViewer.reveal(selectedTool);
 		comboViewer.setSelection(new StructuredSelection(selectedTool), true);
 	}
 
 	// Needs to be done for each fieldeditor.
 	private void addFieldEditor(FieldEditor fieldEditor) {
 		fieldEditor.setPreferencePage(this);
 		fieldEditor.setPreferenceStore(getPreferenceStore());
 		fieldEditor.load();
 	}
 
 	/**
 	 * creates a label
 	 */
 	private Label createLabel(Composite parent, String text, int span) {
 		Label label = new Label(parent, SWT.LEFT);
 		label.setText(text);
 		GridData data = new GridData();
 		data.horizontalSpan = span;
 		data.horizontalAlignment = GridData.FILL;
 		label.setLayoutData(data);
 		return label;
 	}
 
 	// This should be called when we have chnaged selectedTool.
 	private void updateTextValue() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
 	 */
 	@Override
 	public boolean performOk() {
 		useExternal.store();
 		if (!selectedTool.equals("")) {
 			getPreferenceStore().setValue(IClearCasePreferenceConstants.EXTERNAL_DIFF_TOOL, selectedTool);
 		//check if
		if(execPath.getText().equals("")){
 			MessageDialog.openError(getShell(), PreferenceMessages.getString("DiffMergePreferencePage.error.title"), PreferenceMessages.getString(("DiffMergePreferencePage.error.noPath"))); //$NON-NLS-1$ //$NON-NLS-2$
 			execPath.setFocus();
 			return false;
 		}
 		
 		}
 		// put in map
 		toolPathMap.put(selectedTool, execPath.getText());
 		// now store it.
 		getPreferenceStore().setValue(IClearCasePreferenceConstants.EXTERNAL_DIFF_TOOL_EXEC_PATH, PreferenceHelper.mapToStr(toolPathMap));
 		
 		if (super.performOk()) {
 			ClearCasePlugin.getDefault().resetClearCase();
 			return true;
 		}
 		return false;
 	}
 }
