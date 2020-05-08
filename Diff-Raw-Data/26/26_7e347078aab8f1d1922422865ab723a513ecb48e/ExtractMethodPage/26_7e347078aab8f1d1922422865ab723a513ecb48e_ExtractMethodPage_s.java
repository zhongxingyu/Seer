 /***** BEGIN LICENSE BLOCK *****
  * Version: CPL 1.0/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Common Public
  * License Version 1.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.eclipse.org/legal/cpl-v10.html
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * Copyright (C) 2006 Mirko Stocker <me@misto.ch>
  * 
  * Alternatively, the contents of this file may be used under the terms of
  * either of the GNU General Public License Version 2 or later (the "GPL"),
  * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the CPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the CPL, the GPL or the LGPL.
  ***** END LICENSE BLOCK *****/
 
 package org.rubypeople.rdt.refactoring.ui.pages;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Observable;
 import java.util.Observer;
 
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.rubypeople.rdt.refactoring.core.SelectionInformation;
 import org.rubypeople.rdt.refactoring.core.extractmethod.ExtractedMethodHelper;
 import org.rubypeople.rdt.refactoring.core.extractmethod.MethodExtractor;
 import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper;
 import org.rubypeople.rdt.refactoring.ui.pages.extractmethod.ButtonStateListener;
 import org.rubypeople.rdt.refactoring.ui.pages.extractmethod.ExtractMethodComposite;
 import org.rubypeople.rdt.refactoring.ui.pages.extractmethod.IValidationController;
 import org.rubypeople.rdt.refactoring.ui.pages.extractmethod.MethodArgumentTableItem;
 import org.rubypeople.rdt.refactoring.ui.pages.extractmethod.MethodNameListener;
 import org.rubypeople.rdt.refactoring.ui.pages.extractmethod.ParameterTextChanged;
 import org.rubypeople.rdt.refactoring.ui.pages.extractmethod.ParametersButtonDownListener;
 import org.rubypeople.rdt.refactoring.ui.pages.extractmethod.ParametersButtonUpListener;
 import org.rubypeople.rdt.refactoring.ui.pages.extractmethod.SignatureObserver;
 
 public class ExtractMethodPage extends UserInputWizardPage implements IValidationController {
 
 	private static final String title = "Convert Local Variable to Field...";
 
 	private MethodExtractor methodExtractor;
 
 	private ExtractMethodComposite extractComposite;
 
 	private final SelectionInformation selectionInformation;
 
 	private ExtractedMethodHelper extractedMethod;
 
 	public ExtractMethodPage(MethodExtractor methodExtractor, SelectionInformation selectionInformation) {
 		super(title);
 		this.methodExtractor = methodExtractor;
 		extractedMethod = methodExtractor.getExtractedMethod();
 		this.selectionInformation = selectionInformation;
 	}
 
 	private class ItemNameObserver implements Observer {
 		public void update(Observable o, Object arg) {
 			ParameterTextChanged change = (ParameterTextChanged) arg;
 			extractedMethod.changeParameter(change.getOriginalPosition(), change.getTo());
 		}
 	}
 
 	private class ItemOrderObserver implements Observer {
 		public void update(Observable o, Object arg) {
 			ParameterTextChanged change = (ParameterTextChanged) arg;
 			extractedMethod.changeParameter(change.getOriginalPosition(), change.getNewPosition());
 		}
 	}
 
 	public void createControl(Composite parent) {
 
 		extractComposite = new ExtractMethodComposite(parent, this, extractedMethod.hasArguments(), extractedMethod
 				.getVisibility() != VisibilityNodeWrapper.METHOD_VISIBILITY.NONE);
 
 		extractedMethod.addObserver(
 				new SignatureObserver(extractComposite.getMethodSignaturePreviewLabel(), extractedMethod));
 
 		if (extractedMethod.hasArguments()) {
 			setupArgumentsTable(extractComposite.getParametersTable(), extractComposite.getUpParametersButton(), extractComposite
 					.getDownParametersButton(), extractComposite.getEditParametersButton());
 		}
 
 		setControl(extractComposite);
 
 		addNewMethodNameListener();
 		if (extractedMethod.getVisibility() != VisibilityNodeWrapper.METHOD_VISIBILITY.NONE) {
 			setupVisibilityHandlers();
 		}
 
 		if (extractComposite.getCellEditorListener() != null) {
 			extractComposite.getCellEditorListener().addObserver(new ItemNameObserver());
 		}
 
 		setupSelectionPreview();
 	}
 
 	private void setupSelectionPreview() {
 		extractComposite.getCodeViewer().setPreviewText(selectionInformation.getSource());
 
		int start = extractedMethod.getSelectedNodes().getPosition().getStartOffset();
		int length = extractedMethod.getSelectedNodes().getPosition().getEndOffset() - start;
		extractComposite.getCodeViewer().setBackgroundColor(start, length, SWT.COLOR_GRAY);
 
		extractComposite.getCodeViewer().setBackgroundColor(selectionInformation.getStartOfSelection(),
				selectionInformation.getEndOfSelection() - selectionInformation.getStartOfSelection(), SWT.COLOR_DARK_GRAY);
 
 		scrollToSelection();
 	}
 
 	private void setupVisibilityHandlers() {
 		if (ExtractedMethodHelper.DEFAULT_VISIBILITY.equals(VisibilityNodeWrapper.METHOD_VISIBILITY.PRIVATE)) {
 			extractComposite.getPrivateAccessRadioButton().setSelection(true);
 		} else if (ExtractedMethodHelper.DEFAULT_VISIBILITY.equals(VisibilityNodeWrapper.METHOD_VISIBILITY.PROTECTED)) {
 			extractComposite.getProtectedAccessRadioButton().setSelection(true);
 		} else if (ExtractedMethodHelper.DEFAULT_VISIBILITY.equals(VisibilityNodeWrapper.METHOD_VISIBILITY.PUBLIC)) {
 			extractComposite.getPublicAccessRadioButton().setSelection(true);
 		}
 
 		extractComposite.getPrivateAccessRadioButton().addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				extractedMethod.setVisibility(VisibilityNodeWrapper.METHOD_VISIBILITY.PRIVATE);
 			}
 		});
 
 		extractComposite.getPublicAccessRadioButton().addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				extractedMethod.setVisibility(VisibilityNodeWrapper.METHOD_VISIBILITY.PUBLIC);
 			}
 		});
 
 		extractComposite.getProtectedAccessRadioButton().addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				extractedMethod.setVisibility(VisibilityNodeWrapper.METHOD_VISIBILITY.PROTECTED);
 			}
 		});
 
 		extractComposite.getNoneAccessRadioButton().addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				extractedMethod.setVisibility(VisibilityNodeWrapper.METHOD_VISIBILITY.NONE);
 			}
 		});
 	}
 
 	private void setupArgumentsTable(final Table table, Button upButton, Button downButton, Button editButton) {
 
 		insertParameterItems(table);
 
 		ParametersButtonUpListener upListener = new ParametersButtonUpListener(extractComposite.getParametersTable());
 		ParametersButtonDownListener downListener = new ParametersButtonDownListener(extractComposite.getParametersTable());
 		extractComposite.getUpParametersButton().addListener(SWT.Selection, upListener);
 		extractComposite.getDownParametersButton().addListener(SWT.Selection, downListener);
 
 		ButtonStateListener listener = new ButtonStateListener(table, upButton, downButton, editButton);
 		upListener.addObserver(listener);
 		downListener.addObserver(listener);
 		upListener.addObserver(new ItemOrderObserver());
 		downListener.addObserver(new ItemOrderObserver());
 
 		table.addListener(SWT.Selection, listener);
 	}
 
 	private void insertParameterItems(final Table table) {
 
 		String[] names = extractedMethod.getArguments().toArray(new String[extractedMethod.getArguments().size()]);
 		for (int i = 0; i < names.length; i++) {
 			new MethodArgumentTableItem(table, names[i], true, i, i);
 		}
 	}
 
 	private void addNewMethodNameListener() {
 		MethodNameListener methodNameListener = new MethodNameListener(methodExtractor, this);
 		extractComposite.getNewMethodNameText().getText().addModifyListener(methodNameListener);
 		setComplete(methodNameListener, false);
 	}
 
 	private HashMap<Object, Boolean> completedValidators = new HashMap<Object, Boolean>();
 
 	public void setError(String message) {
 		setMessage(message, IMessageProvider.ERROR);
 	}
 
 	public void setComplete(Object source, boolean complete) {
 		completedValidators.put(source, complete);
 
 		boolean allOk = true;
 		for (boolean ok : completedValidators.values()) {
 			if (!ok) {
 				allOk = false;
 			}
 		}
 		setPageComplete(allOk);
 	}
 
 	public void scrollToSelection() {
 		extractComposite.getCodeViewer().getTextWidget().setSelection(selectionInformation.getStartOfSelection());
 		extractComposite.getCodeViewer().getTextWidget().setSelection(selectionInformation.getEndOfSelection());
 		extractComposite.getCodeViewer().getTextWidget().showSelection();
 	}
 
 	public ArrayList<String> getInvalidNames() {
 		return extractedMethod.getLocalOnlyVariables();
 	}
 }
