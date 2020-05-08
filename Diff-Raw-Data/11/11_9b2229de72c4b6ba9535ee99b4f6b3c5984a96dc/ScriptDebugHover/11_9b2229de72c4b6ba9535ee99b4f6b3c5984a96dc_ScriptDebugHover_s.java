 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.debug.ui;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.model.IVariable;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.dltk.core.ICodeAssist;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.debug.core.eval.IScriptEvaluationEngine;
 import org.eclipse.dltk.debug.core.eval.IScriptEvaluationResult;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;
 import org.eclipse.dltk.debug.core.model.IScriptStackFrame;
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 import org.eclipse.dltk.debug.core.model.IScriptVariable;
 import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
 import org.eclipse.dltk.debug.ui.ScriptDebugModelPresentation;
 import org.eclipse.dltk.internal.ui.text.HTMLTextPresenter;
 import org.eclipse.dltk.internal.ui.text.ScriptWordFinder;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.text.hover.IScriptEditorTextHover;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.DefaultInformationControl;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IInformationControl;
 import org.eclipse.jface.text.IInformationControlCreator;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextHoverExtension;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.editors.text.EditorsUI;
 
 public abstract class ScriptDebugHover implements IScriptEditorTextHover,
 		ITextHoverExtension {
 
 	private IEditorPart editor;
 
 	public void setEditor(IEditorPart editor) {
 		this.editor = editor;
 	}
 
 	public IRegion getHoverRegion(final ITextViewer textViewer, int offset) {
 		return ScriptWordFinder.findWord(textViewer.getDocument(), offset);
 	}
 
 	/**
 	 * Returns the stack frame in which to search for variables, or
 	 * <code>null</code> if none.
 	 * 
 	 * @return the stack frame in which to search for variables, or
 	 *         <code>null</code> if none
 	 */
 	protected IScriptStackFrame getFrame() {
 		IAdaptable adaptable = DebugUITools.getDebugContext();
 		if (adaptable != null) {
 			return (IScriptStackFrame) adaptable
 					.getAdapter(IScriptStackFrame.class);
 		}
 		return null;
 	}
 
 	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
 		IScriptStackFrame frame = getFrame();
 		if (frame != null) {
 			IDocument document = textViewer.getDocument();
 
 			if (document != null) {
 				try {
 					String snippet = document.get(hoverRegion.getOffset(),
 							hoverRegion.getLength());
 
 					/*
 					 * if (hoverRegion.getOffset() > 0) { IRegion hoverRegion2 =
 					 * getHoverRegion(textViewer, hoverRegion.getOffset());
 					 * hoverRegion = hoverRegion2; variableName =
 					 * document.get(hoverRegion2.getOffset(),
 					 * hoverRegion2.getLength()); }
 					 */
 
 					try {
 						// Try to find variable
 						IScriptVariable variable = frame.findVariable(snippet
 								.trim());
 						if (variable != null) {
 							return getVariableText(variable);
 						}
 
 						// Try to evaluate
 						IScriptEvaluationEngine engine = ((IScriptThread) frame
 								.getThread()).getEvaluationEngine();
 						IScriptEvaluationResult result = engine.syncEvaluate(
 								snippet, null);
 
 						if (result != null) {
 							return getResultText(result);
 						}
 					} catch (DebugException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 				} catch (BadLocationException e) {
 					return null;
 				}
 			}
 
 			ICodeAssist codeAssist = null;
 			if (editor != null) {
 				IEditorInput input = editor.getEditorInput();
 				Object element = DLTKUIPlugin.getDefault()
 						.getWorkingCopyManager().getWorkingCopy(input);
 				if (element instanceof ICodeAssist) {
 					codeAssist = ((ICodeAssist) element);
 				}
 			}
 
 			/*
 			 * if (codeAssist != null) { return getRemoteHoverInfo(frame,
 			 * textViewer, hoverRegion); }
 			 */
 
 			IModelElement[] resolve = null;
 			try {
 				resolve = codeAssist.codeSelect(hoverRegion.getOffset(),
 						hoverRegion.getLength());
 			} catch (ModelException e1) {
 				resolve = new IModelElement[0];
 			}
 
 			for (int i = 0; i < resolve.length; i++) {
 				IModelElement scriptElement = resolve[i];
 				System.out.println("Element: " + scriptElement.getClass());
 				if (scriptElement instanceof IField) {
 					IField field = (IField) scriptElement;
 					IScriptVariable variable = null;
 					IScriptDebugTarget debugTarget = (IScriptDebugTarget) frame
 							.getDebugTarget();
 				}
 			}
 		}
 
 		return null;
 	}
 
 	/*
 	 * private String getRemoteHoverInfo(IScriptStackFrame frame, ITextViewer
 	 * textViewer, IRegion hoverRegion) { if (frame != null) { try { IDocument
 	 * document = textViewer.getDocument(); if (document != null) { String
 	 * variableName = document.get(hoverRegion.getOffset(),
 	 * hoverRegion.getLength()); String generateHoverForLocal =
 	 * generateHoverForLocal(frame, variableName); if (generateHoverForLocal ==
 	 * null) { if (!variableName.startsWith("this")) { variableName = "this." +
 	 * variableName; generateHoverForLocal = generateHoverForLocal( frame,
 	 * variableName); } } return generateHoverForLocal; } } catch
 	 * (BadLocationException x) { } } return null; }
 	 */
 
 	/*
 	 * private String generateHoverForLocal(IScriptStackFrame frame, String
 	 * varName) { String variableText = null; IScriptVariable variable = null;
 	 * int iip = varName.indexOf('.'); if (iip != -1) try { String vn =
 	 * varName.substring(0, iip); IScriptVariable findVariable =
 	 * frame.findVariable(vn); while (iip != -1) {
 	 * 
 	 * String name = varName.substring(iip + 1); int pos = name.indexOf('.'); if
 	 * (pos != -1) { varName = name; name = name.substring(0, pos); } iip = pos;
 	 * if (findVariable == null) return null; IScriptVariable[] children =
 	 * findVariable.getChildren(); for (int a = 0; a < children.length; a++) {
 	 * if (children[a].getName().equals(name)) { findVariable = children[a]; } } }
 	 * return getVariableText(findVariable) + "=" +
 	 * findVariable.getValueString(); } catch (DebugException e) {
 	 * DLTKDebugPlugin.log(e); return null; } try { variable =
 	 * frame.findVariable(varName); } catch (DebugException e) {
 	 * DLTKDebugPlugin.log(e); } if (variable != null) { variableText =
 	 * getVariableText(variable) + "=" + variable.getValueString(); } return
 	 * variableText; }
 	 */
 
 	protected String getResultText(IScriptEvaluationResult result)
 			throws DebugException {
		return prepareHtml(result.getSnippet() + " = "
				+ result.getValue().getValueString());
 	}
 
 	protected String getVariableText(IVariable variable) throws DebugException {
 		return prepareHtml(variable.getName() + " = "
 				+ variable.getValue().getValueString());
 	}
 
 	protected String prepareHtml(String text) {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("<p><pre>"); //$NON-NLS-1$
 		buffer.append(replaceHTMLChars(text));
 		buffer.append("</pre></p>"); //$NON-NLS-1$
 		if (buffer.length() > 0) {
 			return buffer.toString();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Replaces reserved HTML characters in the given string with their escaped
 	 * equivalents. This is to ensure that variable values containing reserved
 	 * characters are correctly displayed.
 	 */
 	private static String replaceHTMLChars(String variableText) {
 		StringBuffer buffer = new StringBuffer(variableText.length());
 		char[] characters = variableText.toCharArray();
 		for (int i = 0; i < characters.length; i++) {
 			char character = characters[i];
 			switch (character) {
 			case '<':
 				buffer.append("&lt;"); //$NON-NLS-1$
 				break;
 			case '>':
 				buffer.append("&gt;"); //$NON-NLS-1$
 				break;
 			case '&':
 				buffer.append("&amp;"); //$NON-NLS-1$
 				break;
 			case '"':
 				buffer.append("&quot;"); //$NON-NLS-1$
 				break;
 			default:
 				buffer.append(character);
 			}
 		}
 		return buffer.toString();
 	}
 
 	/**
 	 * Returns the value of this filters preference (on/off) for the given view.
 	 * 
 	 * @param part
 	 * @return boolean
 	 */
 	public static boolean getBooleanPreferenceValue(String id, String preference) {
 		String compositeKey = id + "." + preference; //$NON-NLS-1$
 		IPreferenceStore store = DLTKDebugUIPlugin.getDefault()
 				.getPreferenceStore();
 		boolean value = false;
 		if (store.contains(compositeKey)) {
 			value = store.getBoolean(compositeKey);
 		} else {
 			value = store.getBoolean(preference);
 		}
 		return value;
 	}
 
 	public IInformationControlCreator getHoverControlCreator() {
 		return new IInformationControlCreator() {
 			public IInformationControl createInformationControl(Shell parent) {
 				return new DefaultInformationControl(parent, SWT.NONE,
 						new HTMLTextPresenter(true), EditorsUI
 								.getTooltipAffordanceString());
 			}
 		};
 	}
 
 	/**
 	 * Returns a configured model presentation for use displaying variables.
 	 */
 	protected abstract ScriptDebugModelPresentation getModelPresentation();
 }
