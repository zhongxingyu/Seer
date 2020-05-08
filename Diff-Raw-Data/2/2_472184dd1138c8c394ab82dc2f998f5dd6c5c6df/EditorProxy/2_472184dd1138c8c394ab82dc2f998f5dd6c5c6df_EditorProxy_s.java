 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, useForLoop, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class is used to access various eclipse editors from R4E model elements
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.editors;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 
 import org.eclipse.compare.CompareEditorInput;
 import org.eclipse.compare.CompareUI;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.content.IContentType;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.util.OpenStrategy;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileVersion;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIPosition;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIComment;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIContent;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUITextPosition;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.CommandUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IEditorRegistry;
 import org.eclipse.ui.IReusableEditor;
 import org.eclipse.ui.IStorageEditorInput;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class EditorProxy {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field R4E_COMPARE_EDITOR_TITLE. (value is ""R4E Compare"")
 	 */
 	private static final String R4E_COMPARE_EDITOR_TITLE = "R4E Compare";
 
 	/**
 	 * Field DEFAULT_EDITOR_NAME. (value is ""org.eclipse.ui.DefaultTextEditor"")
 	 */
 	private static final String DEFAULT_EDITOR_NAME = "org.eclipse.ui.DefaultTextEditor";
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method openEditor. Open the editor
 	 * 
 	 * @param aPage
 	 *            IWorkbenchPage - the current workbench page
 	 * @param aSelection
 	 *            ISelection - the currently selected model element
 	 * @param forceSingleEditor
 	 *            boolean - flag to force single editor
 	 * @throws CoreException
 	 */
 	public static void openEditor(IWorkbenchPage aPage, ISelection aSelection, boolean forceSingleEditor) {
 
 		if (aSelection.isEmpty() || !(aSelection instanceof IStructuredSelection)) {
 			return;
 		}
 
 		Object element = null;
 		IR4EUIPosition position = null;
 
 		R4EUIFileContext context = null;
 		R4EFileVersion baseFileVersion = null;
 		R4EFileVersion targetFileVersion = null;
 
 		for (final Iterator<?> iterator = ((IStructuredSelection) aSelection).iterator(); iterator.hasNext();) {
 
 			element = iterator.next();
 			if (!(element instanceof IR4EUIModelElement)) {
 				continue;
 			}
 
 			//Depending on which element was selected in the tree, we make the target file editable
 			//The file is editable if it was opened from the anomaly or comment level, otherwise it is not
 			//Check to get the position we should put the cursor on and the highlight range in the editor
 			if (element instanceof R4EUIAnomalyBasic) {
 				position = ((R4EUIAnomalyBasic) element).getPosition();
 			} else if (element instanceof R4EUIComment) {
				position = ((R4EUIAnomalyBasic) ((R4EUIAnomalyBasic) element).getParent()).getPosition();
 			} else if (element instanceof R4EUIContent) {
 				position = ((R4EUIContent) element).getPosition();
 			}
 
 			//Find the parent FileContextElement
 			while (!(element instanceof R4EUIFileContext)) {
 				element = ((IR4EUIModelElement) element).getParent();
 				if (null == element) {
 					return;
 				}
 			}
 			context = ((R4EUIFileContext) element);
 
 			//Get files from FileContext
 			baseFileVersion = context.getBaseFileVersion();
 			targetFileVersion = context.getTargetFileVersion();
 
 			//If the files are the same (or do not exist), open the single editor
 			if (null == baseFileVersion || null == targetFileVersion
 					|| baseFileVersion.getVersionID().equals(targetFileVersion.getVersionID())) {
 				forceSingleEditor = true;
 			}
 
 			//Check if the base file is set, if so, we will use the compare editor.  Otherwise we use the normal editor of the appropriate type
 			if (context.isFileVersionsComparable() && !forceSingleEditor) {
 				openCompareEditor(aPage, baseFileVersion, targetFileVersion);
 			} else {
 				if (null != targetFileVersion) {
 					openSingleEditor(aPage, targetFileVersion, position);
 				} else {
 					//File was removed, open the base then
 					openSingleEditor(aPage, baseFileVersion, position);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Method openSingleEditor. Open the single-mode default editor for the file type
 	 * 
 	 * @param aPage
 	 *            IWorkbenchPage - the current workbench page
 	 * @param aFileVersion
 	 *            R4EFileVersion - the file version
 	 * @param aPosition
 	 *            IR4EUIPosition - the position to go to in the file
 	 */
 	private static void openSingleEditor(IWorkbenchPage aPage, R4EFileVersion aFileVersion, IR4EUIPosition aPosition) {
 
 		try {
 			IStorageEditorInput editorInput = null;
 
 			//NOTE:  We use the workspace file as input if it is in sync with the file to review,
 			//		 otherwise we use the file to review that is included in the review repository
 			if (CommandUtils.useWorkspaceResource(aFileVersion)) {
 				editorInput = new R4EFileEditorInput(aFileVersion);
 			} else {
 				editorInput = new R4EFileRevisionEditorInput(aFileVersion);
 			}
 			final String id = getEditorId(editorInput.getName(),
 					getContentType(editorInput.getName(), editorInput.getStorage().getContents()));
 			final IEditorPart editor = aPage.openEditor(editorInput, id, OpenStrategy.activateOnOpen());
 
 			//Set highlighted selection and reset cursor if possible
 			if (editor instanceof ITextEditor && aPosition instanceof R4EUITextPosition) {
 				((ITextEditor) editor).setHighlightRange(((R4EUITextPosition) aPosition).getOffset(),
 						((R4EUITextPosition) aPosition).getLength(), true);
 				final TextSelection selectedText = new TextSelection(((R4EUITextPosition) aPosition).getOffset(),
 						((R4EUITextPosition) aPosition).getLength());
 				((ITextEditor) editor).getSelectionProvider().setSelection(selectedText);
 			}
 		} catch (CoreException e) {
 			UIUtils.displayCoreErrorDialog(e);
 		}
 	}
 
 	/**
 	 * Method openCompareEditor. Open the compare-mode default editor for the file types
 	 * 
 	 * @param aPage
 	 *            IWorkbenchPage - the current workbench page
 	 * @param aBaseFileVersion
 	 *            R4EFileVersion - the base (or reference) file version
 	 * @param aTargetFileVersion
 	 *            R4EFileVersion - the target (or current) file version
 	 * @param aPosition
 	 *            IR4EUIPosition
 	 */
 	private static void openCompareEditor(IWorkbenchPage aPage, R4EFileVersion aBaseFileVersion,
 			R4EFileVersion aTargetFileVersion) {
 
 		//Reuse editor if it is already open on the same input
 		CompareEditorInput input = null;
 		final IEditorPart editor = findReusableCompareEditor(aPage, aBaseFileVersion, aTargetFileVersion);
 
 		if (null != editor) {
 			aPage.activate(editor); //Simply provide focus to editor
 
 			//Go to the correct element in the compare editor
 			UIUtils.selectElementInEditor((R4ECompareEditorInput) editor.getEditorInput());
 		} else {
 			input = CommandUtils.createCompareEditorInput(aBaseFileVersion, aTargetFileVersion);
 			input.setTitle(R4E_COMPARE_EDITOR_TITLE); // Adjust the compare title
 
 			R4EUIPlugin.Ftracer.traceInfo("Open compare editor on files "
 					+ ((null != aTargetFileVersion) ? aTargetFileVersion.getName() : "") + " (Target) and "
 					+ ((null != aBaseFileVersion) ? aBaseFileVersion.getName() : "") + " (Base)");
 			CompareUI.openCompareEditor(input, true);
 			//NOTE:  The position is set in editor in R4ECompareEditorInput#createContents
 		}
 	}
 
 	/**
 	 * Method findReusableCompareEditor. Find the appropriate compare editor based on the file types
 	 * 
 	 * @param aPage
 	 *            IWorkbenchPage - the current workbench page
 	 * @param aBaseFile
 	 *            R4EFileVersion - the base (or reference) file version
 	 * @param aTargetFile
 	 *            R4EFileVersion - the target file version
 	 * @return IEditorPart - the editor to use
 	 */
 	public static IEditorPart findReusableCompareEditor(IWorkbenchPage aPage, R4EFileVersion aBaseFile,
 			R4EFileVersion aTargetFile) {
 
 		final IEditorReference[] editorRefs = aPage.getEditorReferences();
 		IEditorPart part = null;
 		R4ECompareEditorInput input = null;
 		ITypedElement left = null;
 		ITypedElement right = null;
 		// first loop looking for an editor with the same input
 		for (IEditorReference editorRef : editorRefs) {
 			part = editorRef.getEditor(false);
 			if (null != part && part instanceof IReusableEditor) {
 				// check if the editor input type complies with the types given by the caller
 				if (R4ECompareEditorInput.class.isInstance(part.getEditorInput())) {
 					//Now check if the input files are the same as with the found editor
 					input = (R4ECompareEditorInput) part.getEditorInput();
 					left = input.getLeftElement();
 					right = input.getRightElement();
 
 					//Case:  No input in editor, that should never happen but guard here just in case
 					if (null == left && null == right) {
 						return null;
 					}
 
 					//Get the file versions
 					R4EFileVersion leftVersion = null;
 					R4EFileVersion rightVersion = null;
 					if (left instanceof R4EFileRevisionTypedElement) {
 						leftVersion = ((R4EFileRevisionTypedElement) left).getFileVersion();
 					} else if (left instanceof R4EFileTypedElement) {
 						leftVersion = ((R4EFileTypedElement) left).getFileVersion();
 					}
 					if (right instanceof R4EFileRevisionTypedElement) {
 						rightVersion = ((R4EFileRevisionTypedElement) right).getFileVersion();
 					} else if (right instanceof R4EFileTypedElement) {
 						rightVersion = ((R4EFileTypedElement) right).getFileVersion();
 					}
 
 					//Case:  No target file and base is the same
 					if (null == leftVersion && null == aTargetFile && null != rightVersion && null != aBaseFile
 							&& rightVersion.equals(aBaseFile)) {
 						return part;
 					}
 
 					//Case:  No base file and target is the same
 					if (null == rightVersion && null == aBaseFile && null != leftVersion && null != aTargetFile
 							&& leftVersion.equals(aTargetFile)) {
 						return part;
 					}
 
 					//Case: Base and target are the same
 					if (null != leftVersion && null != rightVersion && null != aBaseFile && null != aTargetFile) {
 						if (leftVersion.equals(aTargetFile) && rightVersion.equals(aBaseFile)) {
 							return part;
 						}
 					}
 				}
 			}
 		}
 		// no re-usable editor found
 		return null;
 	}
 
 	/**
 	 * Method getEditorId.
 	 * 
 	 * @param aFileName
 	 *            String
 	 * @param aType
 	 *            IContentType
 	 * @return String
 	 */
 	private static String getEditorId(String aFileName, IContentType aType) {
 		final IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
 		final IEditorDescriptor descriptor = registry.getDefaultEditor(aFileName, aType);
 		String id = null;
 		if (null == descriptor || descriptor.isOpenExternal()) {
 			id = DEFAULT_EDITOR_NAME;
 		} else {
 			id = descriptor.getId();
 		}
 		return id;
 	}
 
 	/**
 	 * Method getContentType.
 	 * 
 	 * @param aFileName
 	 *            String
 	 * @param aContents
 	 *            InputStream
 	 * @return IContentType
 	 */
 	private static IContentType getContentType(String aFileName, InputStream aContents) {
 		IContentType type = null;
 		if (null != aContents) {
 			try {
 				type = Platform.getContentTypeManager().findContentTypeFor(aContents, aFileName);
 			} catch (IOException e) {
 				R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e);
 			}
 		}
 		if (null == type) {
 			type = Platform.getContentTypeManager().findContentTypeFor(aFileName);
 		}
 		return type;
 	}
 }
