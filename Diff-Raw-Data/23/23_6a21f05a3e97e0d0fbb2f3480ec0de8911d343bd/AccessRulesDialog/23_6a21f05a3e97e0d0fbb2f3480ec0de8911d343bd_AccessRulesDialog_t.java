 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.ui.wizards.buildpath;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IAccessRule;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.corext.util.Messages;
 import org.eclipse.dltk.internal.ui.wizards.NewWizardMessages;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.DialogField;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.IDialogFieldListener;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.IListAdapter;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.LayoutUtil;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.ListDialogField;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.ScriptElementLabels;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.StatusDialog;
 import org.eclipse.jface.viewers.ColumnLayoutData;
 import org.eclipse.jface.viewers.ColumnPixelData;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.forms.events.HyperlinkAdapter;
 import org.eclipse.ui.forms.events.HyperlinkEvent;
 import org.eclipse.ui.forms.widgets.FormText;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
 
 
 public class AccessRulesDialog extends StatusDialog {
 	
 	public static final int SWITCH_PAGE= 10;
 	
 	private final ListDialogField fAccessRulesList;
 	private final SelectionButtonDialogField fCombineRulesCheckbox;
 	private final BPListElement fCurrElement;
 	
 	private final IDLTKProject fProject;
 	private final boolean fParentCanSwitchPage;
 	
 	private static final int IDX_ADD= 0;
 	private static final int IDX_EDIT= 1;
 	private static final int IDX_UP= 3;
 	private static final int IDX_DOWN= 4;
 	private static final int IDX_REMOVE= 6;
 
 	
 	public AccessRulesDialog(Shell parent, BPListElement entryToEdit, IDLTKProject project, boolean parentCanSwitchPage) {
 		super(parent);
 		setShellStyle(getShellStyle() | SWT.RESIZE);
 		
 		fCurrElement= entryToEdit;
 		fProject= project; // can be null
 
 		setTitle(NewWizardMessages.AccessRulesDialog_title); 
 		
 		fAccessRulesList= createListContents(entryToEdit);
 		
 		fCombineRulesCheckbox= new SelectionButtonDialogField(SWT.CHECK);
 		fCombineRulesCheckbox.setLabelText(NewWizardMessages.AccessRulesDialog_combine_label);
 		fCombineRulesCheckbox.setSelection(Boolean.TRUE.equals(entryToEdit.getAttribute(BPListElement.COMBINE_ACCESSRULES)));
 		
 		fParentCanSwitchPage= parentCanSwitchPage;
 	}
 	
 	
 	private ListDialogField createListContents(BPListElement entryToEdit) {
 		String label= NewWizardMessages.AccessRulesDialog_rules_label; 
 		String[] buttonLabels= new String[] {
 				NewWizardMessages.AccessRulesDialog_rules_add, 
 				NewWizardMessages.AccessRulesDialog_rules_edit, 
 				null,
 				NewWizardMessages.AccessRulesDialog_rules_up, 
 				NewWizardMessages.AccessRulesDialog_rules_down, 
 				null,
 				NewWizardMessages.AccessRulesDialog_rules_remove
 		};
 		
 		TypeRestrictionAdapter adapter= new TypeRestrictionAdapter();
 		AccessRulesLabelProvider labelProvider= new AccessRulesLabelProvider();
 		
 		ListDialogField patternList= new ListDialogField(adapter, buttonLabels, labelProvider);
 		patternList.setDialogFieldListener(adapter);
 
 		patternList.setLabelText(label);
 		patternList.setRemoveButtonIndex(IDX_REMOVE);
 		patternList.setUpButtonIndex(IDX_UP);
 		patternList.setDownButtonIndex(IDX_DOWN);
 		patternList.enableButton(IDX_EDIT, false);
 	
 		IAccessRule[] rules= (IAccessRule[]) entryToEdit.getAttribute(BPListElement.ACCESSRULES);
 		ArrayList elements= new ArrayList(rules.length);
 		for (int i= 0; i < rules.length; i++) {
 			elements.add(rules[i]);
 		}
 		patternList.setElements(elements);
 		patternList.selectFirstElement();
 		return patternList;
 	}
 
 
 	protected Control createDialogArea(Composite parent) {
 		Composite composite= (Composite) super.createDialogArea(parent);
 				
 		int maxLabelSize= 0;
 		GC gc= new GC(composite);
 		try {
 			maxLabelSize= gc.textExtent(AccessRulesLabelProvider.getResolutionLabel(IAccessRule.K_ACCESSIBLE)).x;
 			int len2= gc.textExtent(AccessRulesLabelProvider.getResolutionLabel(IAccessRule.K_DISCOURAGED)).x;
 			if (len2 > maxLabelSize) {
 				maxLabelSize= len2;
 			}
 			int len3= gc.textExtent(AccessRulesLabelProvider.getResolutionLabel(IAccessRule.K_NON_ACCESSIBLE)).x;
 			if (len3 > maxLabelSize) {
 				maxLabelSize= len3;
 			}
 		} finally {
 			gc.dispose();
 		}
 		
 		ColumnLayoutData[] columnDta= new ColumnLayoutData[] {
 				new ColumnPixelData(maxLabelSize + 40),
 				new ColumnWeightData(1),
 		};
 		fAccessRulesList.setTableColumns(new ListDialogField.ColumnsDescription(columnDta, null, false));
 		
 
 		Composite inner= new Composite(composite, SWT.NONE);
 		inner.setFont(composite.getFont());
 		
 		GridLayout layout= new GridLayout();
 		layout.marginHeight= 0;
 		layout.marginWidth= 0;
 		layout.numColumns= 2;
 		inner.setLayout(layout);
 		inner.setLayoutData(new GridData(GridData.FILL_BOTH));
 		
 		Label description= new Label(inner, SWT.WRAP);
 
 		description.setText(getDescriptionString()); 
 		
 		GridData data= new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
 		data.widthHint= convertWidthInCharsToPixels(70);
 		description.setLayoutData(data);
 		
 		fAccessRulesList.doFillIntoGrid(inner, 3);
 				
 		LayoutUtil.setHorizontalSpan(fAccessRulesList.getLabelControl(null), 2);
 		
 		data= (GridData) fAccessRulesList.getListControl(null).getLayoutData();
 		data.grabExcessHorizontalSpace= true;
 		data.heightHint= SWT.DEFAULT;
 		
 		if (fCurrElement.getEntryKind() == IBuildpathEntry.BPE_PROJECT) {
 			fCombineRulesCheckbox.doFillIntoGrid(inner, 2);
 		}
 		
 		if (fProject != null) {
 			String forbiddenSeverity=  fProject.getOption(DLTKCore.COMPILER_PB_FORBIDDEN_REFERENCE, true);
 			String discouragedSeverity= fProject.getOption(DLTKCore.COMPILER_PB_DISCOURAGED_REFERENCE, true);
 			String[] args= { getLocalizedString(discouragedSeverity), getLocalizedString(forbiddenSeverity) };
 			
 			FormToolkit toolkit= new FormToolkit(parent.getDisplay());
 			toolkit.setBackground(null);
 			try {
 				FormText text = toolkit.createFormText(composite, true);
 				text.setFont(inner.getFont());
 				if (fParentCanSwitchPage) {
 					// with link
 					text.setText(Messages.format(NewWizardMessages.AccessRulesDialog_severity_info_with_link, args), true, false);
 					text.addHyperlinkListener(new HyperlinkAdapter() {
 						public void linkActivated(HyperlinkEvent e) {
 							doErrorWarningLinkPressed();
 						}
 					});
 				} else {
 					// no link
 					text.setText(Messages.format(NewWizardMessages.AccessRulesDialog_severity_info_no_link, args), true, false);
 				}
 				data= new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
 				data.widthHint= convertWidthInCharsToPixels(70);
 				text.setLayoutData(data);				
 			} catch (IllegalArgumentException e) {
 				DLTKUIPlugin.log(e); // invalid string
 			} finally {
 				toolkit.dispose();
 			}
 		}
 		applyDialogFont(composite);		
 		return composite;
 	}
 
 	final void doErrorWarningLinkPressed() {
 		if (fParentCanSwitchPage && MessageDialog.openQuestion(getShell(), NewWizardMessages.AccessRulesDialog_switch_dialog_title, NewWizardMessages.AccessRulesDialog_switch_dialog_message)) {
 	        setReturnCode(SWITCH_PAGE);
 			close();
 		}
 	}
 
 	private String getLocalizedString(String severity) {
 		if (DLTKCore.ERROR.equals(severity)) {
 			return NewWizardMessages.AccessRulesDialog_severity_error;
 		} else if (DLTKCore.WARNING.equals(severity)) {
 			return NewWizardMessages.AccessRulesDialog_severity_warning;
 		} else {
 			return NewWizardMessages.AccessRulesDialog_severity_ignore;
 		}
 	}
 	
 	private String getDescriptionString() {
 		String desc;
 		String name= fCurrElement.getPath().lastSegment();
		if( name.equals(IBuildpathEntry.BUILTIN_EXTERNAL_ENTRY.toString())) {
 			name = ScriptElementLabels.BUILTINS_FRAGMENT;
 		}
 		switch (fCurrElement.getEntryKind()) {
 			case IBuildpathEntry.BPE_CONTAINER:
 				try {
 					name= ScriptElementLabels.getDefault().getContainerEntryLabel(fCurrElement.getPath(), fCurrElement.getDLTKProject());
 				} catch (ModelException e) {
 				}
 				desc= NewWizardMessages.AccessRulesDialog_container_description;
 				break;
 			case IBuildpathEntry.BPE_PROJECT:
 				desc=  NewWizardMessages.AccessRulesDialog_project_description;
 				break;
 			default:
 				desc=  NewWizardMessages.AccessRulesDialog_description;
 		}
 		
 		return Messages.format(desc, name);
 	}
 
 
 	protected void doCustomButtonPressed(ListDialogField field, int index) {
 		if (index == IDX_ADD) {
 			addEntry(field);
 		} else if (index == IDX_EDIT) {
 			editEntry(field);
 		}
 	}
 	
 	protected void doDoubleClicked(ListDialogField field) {
 		editEntry(field);
 	}
 	
 	protected void doSelectionChanged(ListDialogField field) {
 		List selected= field.getSelectedElements();
 		field.enableButton(IDX_EDIT, canEdit(selected));
 	}
 	
 	private boolean canEdit(List selected) {
 		return selected.size() == 1;
 	}
 	
 	private void editEntry(ListDialogField field) {
 		
 		List selElements= field.getSelectedElements();
 		if (selElements.size() != 1) {
 			return;
 		}
 		IAccessRule rule= (IAccessRule) selElements.get(0);
 		AccessRuleEntryDialog dialog= new AccessRuleEntryDialog(getShell(), rule, fCurrElement);
 		if (dialog.open() == Window.OK) {
 			field.replaceElement(rule, dialog.getRule());
 		}
 	}
 
 	private void addEntry(ListDialogField field) {
 		AccessRuleEntryDialog dialog= new AccessRuleEntryDialog(getShell(), null, fCurrElement);
 		if (dialog.open() == Window.OK) {
 			field.addElement(dialog.getRule());
 		}
 	}	
 	
 	
 		
 	// -------- TypeRestrictionAdapter --------
 
 	private class TypeRestrictionAdapter implements IListAdapter, IDialogFieldListener {
 		
 		public void customButtonPressed(ListDialogField field, int index) {
 			doCustomButtonPressed(field, index);
 		}
 		
 		public void selectionChanged(ListDialogField field) {
 			doSelectionChanged(field);
 		}
 		
 		public void doubleClicked(ListDialogField field) {
 			doDoubleClicked(field);
 		}
 		
 		public void dialogFieldChanged(DialogField field) {
 		}
 		
 	}
 	
 	protected void doStatusLineUpdate() {
 	}		
 	
 	protected void checkIfPatternValid() {
 	}
 	
 	public IAccessRule[] getAccessRules() {
 		List elements= fAccessRulesList.getElements();
 		return (IAccessRule[]) elements.toArray(new IAccessRule[elements.size()]);
 	}
 	
 	public boolean doCombineAccessRules() {
 		return fCombineRulesCheckbox.isSelected();
 	}
 	
 	/*
 	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
 	 */
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 		if(DLTKCore.DEBUG) {
 			System.err.println("TODO: AccessRulesDialog add help context");
 		}
 		//PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IDLTKHelpContextIds.ACCESS_RULES_DIALOG);
 	}
 
 
 	public void performPageSwitch(IWorkbenchPreferenceContainer pageContainer) {
 		//HashMap data= new HashMap();
 		if(DLTKCore.DEBUG) {
 			System.err.println("TODO:AccessRulesDialog:");
 		}
 		//data.put(ProblemSeveritiesPreferencePage.DATA_SELECT_OPTION_KEY, DLTKCore.COMPILER_PB_FORBIDDEN_REFERENCE);
 		//data.put(ProblemSeveritiesPreferencePage.DATA_SELECT_OPTION_QUALIFIER, DLTKCore.PLUGIN_ID);
 		//pageContainer.openPage(ProblemSeveritiesPreferencePage.PROP_ID, data);
 	}
 }
