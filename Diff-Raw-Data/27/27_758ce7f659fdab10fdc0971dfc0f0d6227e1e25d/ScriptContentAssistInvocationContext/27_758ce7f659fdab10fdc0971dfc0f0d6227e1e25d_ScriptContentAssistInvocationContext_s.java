 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui.text.completion;
 
 import org.eclipse.dltk.core.CompletionContext;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.internal.ui.editor.EditorUtility;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.text.completion.ContentAssistHistory.RHSHistory;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.ui.IEditorPart;
 
 /**
  * Describes the context of a content assist invocation in a Script editor.
  */
 public abstract class ScriptContentAssistInvocationContext extends
 		ContentAssistInvocationContext {
 	private final IEditorPart fEditor;
 
 	private ISourceModule fSourceModule = null;
 	private boolean fSourceModuleComputed = false;
 
 	private CompletionProposalLabelProvider fLabelProvider;
 	private ScriptCompletionProposalCollector fCollector;
 	private RHSHistory fRHSHistory;
 	private IType fType;
 
	private String fLangaugeNatureID;
 	private boolean isContextInformationMode; 
 
 	/**
 	 * Creates a new context.
 	 * 
 	 * @param viewer
 	 *            the viewer used by the editor
 	 * @param offset
 	 *            the invocation offset
 	 * @param editor
 	 *            the editor that content assist is invoked in
 	 */
 	public ScriptContentAssistInvocationContext(ITextViewer viewer, int offset,
 			IEditorPart editor, String natureId) {
 		super(viewer, offset);
 		
 		fEditor = editor;
		fLangaugeNatureID = natureId;
 	}
 
 	/**
 	 * Creates a new context.
 	 * 
 	 * @param unit
 	 *            the compilation unit in <code>document</code>
 	 */
 	public ScriptContentAssistInvocationContext(ISourceModule unit) {
 		super();
 		fSourceModule = unit;
 		fSourceModuleComputed = true;
 		fEditor = null;
 	}
 
 	public IEditorPart getEditor() {
 	  return fEditor;
 	}
 
	public String getLangaugeNatureID() {
		return fLangaugeNatureID;
 	}
 
 	/**
 	 * Returns the compilation unit that content assist is invoked in,
 	 * <code>null</code> if there is none.
 	 * 
 	 * @return the compilation unit that content assist is invoked in, possibly
 	 *         <code>null</code>
 	 */
 	public ISourceModule getSourceModule() {
 		if (!fSourceModuleComputed) {
 			fSourceModuleComputed = true;
 			if (fCollector != null)
 				fSourceModule = fCollector.getSourceModule();
 			else {
 				IModelElement je = EditorUtility.getEditorInputModelElement(
 						fEditor, false);
 				if (je instanceof ISourceModule)
 					fSourceModule = (ISourceModule) je;
 			}
 		}
 		return fSourceModule;
 	}
 
 	/**
 	 * Returns the project of the compilation unit that content assist is
 	 * invoked in, <code>null</code> if none.
 	 * 
 	 * @return the currentscriptproject, possibly <code>null</code>
 	 */
 	public IScriptProject getProject() {
 		ISourceModule unit = getSourceModule();
 		return unit == null ? null : unit.getScriptProject();
 	}
 
 	/**
 	 * Returns the keyword proposals that are available in this context,
 	 * possibly none.
 	 * 
 	 * @return the available keyword proposals.
 	 */
 	public IScriptCompletionProposal[] getKeywordProposals() {
 		if (fCollector != null)
 			return fCollector.getKeywordCompletionProposals();
 		return new IScriptCompletionProposal[0];
 	}
 
 	/**
 	 * Sets the collector, which is used to access the compilation unit, the
 	 * core context and the label provider.
 	 * 
 	 * @param collector
 	 *            the collector
 	 */
 	public void setCollector(ScriptCompletionProposalCollector collector) {
 		fCollector = collector;
 	}
 
 	/**
 	 * Returns the {@link CompletionContext core completion context} if
 	 * available, <code>null</code> otherwise.
 	 * 
 	 * @return the core completion context if available, <code>null</code>
 	 *         otherwise
 	 */
 	public CompletionContext getCoreContext() {
 		if (fCollector != null)
 			return fCollector.getContext();
 		return null;
 	}
 
 	/**
 	 * Returns an float in [0.0,&nbsp;1.0] based on whether the type has been
 	 * recently used as a right hand side for the type expected in the current
 	 * context. 0 signals that the <code>qualifiedTypeName</code> does not
 	 * match the expected type, while 1.0 signals that
 	 * <code>qualifiedTypeName</code> has most recently been used in a similar
 	 * context.
 	 * 
 	 * @param qualifiedTypeName
 	 *            the type name of the type of interest
 	 * @return a relevance in [0.0,&nbsp;1.0] based on previous content assist
 	 *         invocations
 	 */
 	public float getHistoryRelevance(String qualifiedTypeName) {
 		return getRHSHistory().getRank(qualifiedTypeName);
 	}
 
 	/**
 	 * Returns the content assist type history for the expected type.
 	 * 
 	 * @return the content assist type history for the expected type
 	 */
 	private RHSHistory getRHSHistory() {
 		if (fRHSHistory == null) {
 			CompletionContext context = getCoreContext();
 			if (context != null) {
 //				char[][] expectedTypes = context.getExpectedTypesSignatures();
 //				if (expectedTypes != null && expectedTypes.length > 0) {
 //					String expected = SignatureUtil.stripSignatureToFQN(String
 //							.valueOf(expectedTypes[0]));
 //					fRHSHistory = DLTKUIPlugin.getDefault()
 //							.getContentAssistHistory().getHistory(expected);
 //				}
 			}
 			if (fRHSHistory == null)
 				fRHSHistory = DLTKUIPlugin.getDefault()
 						.getContentAssistHistory().getHistory(null);
 		}
 		return fRHSHistory;
 	}
 
 	/**
 	 * Returns the expected type if any, <code>null</code> otherwise.
 	 * 
 	 * @return the expected type if any, <code>null</code> otherwise
 	 */
 	public IType getExpectedType() {
 //		if (fType == null && getSourceModule() != null) {
 //			CompletionContext context = getCoreContext();
 //			if (context != null) {
 //				char[][] expectedTypes = context.getExpectedTypesSignatures();
 //				if (expectedTypes != null && expectedTypes.length > 0) {
 //					IScriptProject project = getSourceModule().getScriptProject();
 //					if (project != null) {
 //						try {
 //							fType = project.findType(SignatureUtil
 //									.stripSignatureToFQN(String
 //											.valueOf(expectedTypes[0])));
 //						} catch (ModelException x) {
 //							DLTKUIPlugin.log(x);
 //						}
 //					}
 //				}
 //			}
 //		}
 		return fType;
 	}
 
 	/**
 	 * Returns a label provider that can be used to compute proposal labels.
 	 * 
 	 * @return a label provider that can be used to compute proposal labels
 	 */
 	public CompletionProposalLabelProvider getLabelProvider() {
 		if (fLabelProvider == null) {
 			if (fCollector != null)
 				fLabelProvider = fCollector.getLabelProvider();
 			else
 				fLabelProvider = createLabelProvider();
 		}
 
 		return fLabelProvider;
 	}
 
 	protected abstract CompletionProposalLabelProvider createLabelProvider();
 
 	/**
 	 * @param value
 	 */
 	public void setContextInformationMode(boolean value) {
 		isContextInformationMode = value;		
 	}
 	
 	public boolean isContextInformationMode() {
 		return isContextInformationMode;
 	}
 }
