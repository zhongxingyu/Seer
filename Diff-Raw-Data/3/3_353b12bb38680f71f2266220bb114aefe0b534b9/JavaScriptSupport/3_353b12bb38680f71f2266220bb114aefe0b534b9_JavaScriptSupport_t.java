 /*******************************************************************************
  * Copyright (c) 2011 BestSolution.at and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
  ******************************************************************************/
 package org.eclipse.e4.tools.emf.ui.script.js;
 
 import java.util.Map;
 
 import org.eclipse.e4.tools.emf.ui.common.IScriptingSupport;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 public class JavaScriptSupport implements IScriptingSupport {
 	public void openEditor(Shell shell, final Object mainElement, final Map<String, Object> additionalData) {
 		TitleAreaDialog dialog = new TitleAreaDialog(shell) {
 			private Text scriptField;
 
 			@Override
 			protected Control createDialogArea(Composite parent) {
 				Composite container = (Composite) super.createDialogArea(parent);
 				getShell().setText("Execute JavaScript");
 				setTitle("Execute JavaScript");
 				setMessage("Enter some JavaScript and execute it");
 				scriptField = new Text(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
 				scriptField.setLayoutData(new GridData(GridData.FILL_BOTH));
				scriptField.setFont(JFaceResources.getTextFont());
 				return container;
 			}
 			
 			@Override
 			protected void okPressed() {
 				execute(mainElement, additionalData, scriptField.getText());
 			}
 			
 			@Override
 			protected Button createButton(Composite parent, int id,
 					String label, boolean defaultButton) {
 				return super.createButton(parent, id, id == IDialogConstants.OK_ID ? "Execute" : label, defaultButton);
 			}
 		};
 		
 		dialog.open();
 	}
 
 	private void execute(Object mainElement, Map<String, Object> additionalData, String script) {
 		Context cx = Context.enter();
 		Scriptable sc = cx.initStandardObjects();
 		
 		ScriptableObject.putProperty(sc, "mainObject", mainElement);
 		ScriptableObject.putProperty(sc, "additionalData", additionalData);
 		cx.evaluateString(sc, script, "<cmd>", 1, null);
 	}
 }
