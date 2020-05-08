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
 
 package org.eclipse.mylyn.reviews.r4e.ui.editors;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Iterator;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareEditorInput;
 import org.eclipse.compare.CompareUI;
 import org.eclipse.compare.ICompareNavigator;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.mylyn.reviews.r4e.ui.Activator;
 import org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIPosition;
 import org.eclipse.mylyn.reviews.r4e.ui.model.R4EUIAnomalyBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.model.R4EUIAnomalyContainer;
 import org.eclipse.mylyn.reviews.r4e.ui.model.R4EUIComment;
 import org.eclipse.mylyn.reviews.r4e.ui.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.model.R4EUISelection;
 import org.eclipse.mylyn.reviews.r4e.ui.model.R4EUISelectionContainer;
 import org.eclipse.mylyn.reviews.r4e.ui.model.R4EUITextPosition;
 import org.eclipse.team.ui.synchronize.SaveableCompareEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IReusableEditor;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.ide.IDE;
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
 	 * Field R4E_COMPARE_EDITOR_TITLE.
 	 * (value is ""R4E Compare"")
 	 */
 	private static final String R4E_COMPARE_EDITOR_TITLE = "R4E Compare";  //$NON-NLS-1$ // $codepro.audit.disable constantNamingConvention
 
 	
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Method openEditor.
 	 * 		Open the editor
 	 * @param aPage IWorkbenchPage - the current workbench page
 	 * @param aSelection ISelection - the currently selected model element
 	 * @param forceSingleEditor boolean - flag to force single editor
 	 */
 	public static void openEditor(IWorkbenchPage aPage, ISelection aSelection, boolean forceSingleEditor) {
 
 		if (aSelection.isEmpty() || !(aSelection instanceof IStructuredSelection)) return;
 
 		IR4EUIModelElement element = null;
 		boolean targetFileEditable = false;
 		IR4EUIPosition position = null;
 		R4EUISelectionContainer container = null;
		int selectionIndex = 0;
 		
 		for (final Iterator<?> iterator = ((IStructuredSelection)aSelection).iterator(); iterator.hasNext();) {
 			element = (IR4EUIModelElement) iterator.next();
 
 			try {
 				//Depending on which element was selected in the tree, we make the target file editable
 				//The file is editable if it was opened from the anomaly or comment level, otherwise it is not
 				//Also check to get the position we should put the cursor on and the highlight range in the editor
 				if (element instanceof R4EUIAnomalyContainer) {
 					targetFileEditable = true;
 				} else if (element instanceof R4EUIAnomalyBasic) {
 					targetFileEditable = true;
 					position = ((R4EUIAnomalyBasic)element).getPosition();
 				} else if (element instanceof R4EUIComment) {
 					targetFileEditable = true;
 					position = ((R4EUIAnomalyBasic)element.getParent()).getPosition();
 				} else if (element instanceof R4EUISelection) {
 					position = ((R4EUISelection)element).getPosition();
 					container = (R4EUISelectionContainer) ((R4EUISelection)element).getParent();
 					selectionIndex = container.getSelectionList().indexOf(element);
 				}
 
 				//Find the parent FileContextElement
 				while (!(element instanceof R4EUIFileContext)) {
 					element = element.getParent();
 					if (null == element) return;
 				}
 				R4EUIFileContext context = ((R4EUIFileContext) element);
 				
 				//Get file from FileContext
 				IFile baseFile = context.getBaseFile();
 				IFile targetFile = context.getTargetFile();
 				
 				//Check if the base file is set, if so, we will use the compare editor.  Otherwise we use the normal editor of the appropriate type
 				if (context.isFileVersionsComparable() && !forceSingleEditor) {
 					openCompareEditor(aPage, baseFile, targetFile, targetFileEditable, selectionIndex);
 					//openCompareEditor(context); //TODO this was used when using Egit compare engine
 				} else {
 					targetFile = context.getTargetFile();
 					if (null != targetFile) {
 						openSingleEditor(aPage, targetFile, position);
 					} /*else {
 						//TODO can this happen??? or this should be an error?
 					}*/
 				}
 			} catch (PartInitException e) {
 				traceException(e);
 			} /*catch (FileNotFoundException e) {
 				traceException(e);
 			} catch (ReviewVersionsException e) {
 				traceException(e);
 			}*/
 		}
 	}
 
 	/**
 	 * Method traceException.
 	 * @param e
 	 */
 	private static void traceException(Exception e) {
 		Activator.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 		Activator.getDefault().logError("Exception: " + e.toString(), e);
 	}
 
 	/**
 	 * Method openCompareEditor.
 	 * @param context
 	 * @throws FileNotFoundException
 	 * @throws ReviewVersionsException
 	 */
 	/*
 	//TODO this was used when using Egit compare engine
 	private static void openCompareEditor(R4EUIFileContext context) throws FileNotFoundException,
 			ReviewVersionsException {
 		final R4EUIReviewItem commitItem = (R4EUIReviewItem) (context.getParent());
 		final R4EItem item = commitItem.getItem();
 		// TODO: Only one project per Review Item supported at the moment
 		final String projectPlatStr = item.getProjectURIs().get(0);
 		final IProject project = ResourceUtils.toIProject(projectPlatStr);
 		final ReviewsVersionsIF versionsIf = ReviewsVersionsIFFactory.instance.getVersionsIF(project);
 		// TODO: Check the possibility to optimise the selections to take advantage of the session e.g. keep the
 		// sessions per review and clean them at closing of the review
 		final String sessionNum = versionsIf.openCompareSession(item);
 		versionsIf.openCompareEditor(sessionNum, context.getFileContext());
 		versionsIf.closeCompareSession(sessionNum);
 	}
 	*/
 	
 	/**
 	 * Method openSingleEditor.
 	 *  	Open the single-mode default editor for the file type
 	 * @param aPage IWorkbenchPage - the current workbench page
 	 * @param aPosition IR4EPosition - the position to go to in the file
 	 * @param aFile IFile
 	 * @throws PartInitException
 	 * @throws URISyntaxException
 	 */
 	private static void openSingleEditor(IWorkbenchPage aPage, IFile aFile, IR4EUIPosition aPosition) throws PartInitException {
 		
 		IEditorPart editor = null;
 
 		//Check if file exists in workspace
 		if (null != aFile) {
 			//Open the editor on the target file
 			Activator.Ftracer.traceInfo("Open workspace file " + aFile.getName() + " with single-mode editor");
 			editor = IDE.openEditor(aPage, aFile);
 		/*} else {
 			// TODO this is not supported for now
 			//File is not in workspace, try to open it as an external file
 			//Open the editor on the target file
 			Activator.Ftracer.traceInfo("Open external file " + aFile.toString() + " with single-mode editor");
 			editor = IDE.openEditor(aPage, aFile.getLocationURI(), 
 					PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(aFile.getName()).getId(), true); // $codepro.audit.disable methodChainLength
 		}
 		*/
 			//Set highlighted selection and reset cursor if possible
 			if (editor instanceof ITextEditor && aPosition instanceof R4EUITextPosition) {
 				((ITextEditor) editor).setHighlightRange(((R4EUITextPosition)aPosition).getOffset(), 
 						((R4EUITextPosition)aPosition).getLength(), true);
 				final TextSelection selectedText = new TextSelection(((R4EUITextPosition)aPosition).getOffset(), 
 						((R4EUITextPosition)aPosition).getLength());
 				((ITextEditor) editor).getSelectionProvider().setSelection(selectedText);
 			}
 		}
 	}
 	
 	/**
 	 * Method openCompareEditor.
 	 *  	Open the compare-mode default editor for the file types
 	 * @param aPage IWorkbenchPage - the current workbench page
 	 * @param aBaseFile IFile - the base (or reference) file 
 	 * @param aTargetFile IFile
 	 * @param aTargetFileEditable boolean - flag set whether the target file is editable or not
 	 * @param selectionIndex int - the index of the selection to go to in the target file
 	 */
 	private static void openCompareEditor(IWorkbenchPage aPage, IFile aBaseFile, IFile aTargetFile,  // $codepro.audit.disable unusedMethod
			boolean aTargetFileEditable, int selectionIndex) {
 		
 		//Reuse editor if it is already open on the same input
 		CompareEditorInput input = null;
 		final IEditorPart editor = findReusableCompareEditor(aPage, aBaseFile.getLocationURI(), aTargetFile.getLocationURI());
 		if (null != editor) {
 			//Simply provide focus to editor
 			aPage.activate(editor);
 			input = (R4ECompareEditorInput) editor.getEditorInput();
 		} else {
 			final CompareConfiguration config = new CompareConfiguration();
 			config.setLeftEditable(aTargetFileEditable);
 			config.setRightEditable(false);
 			config.setProperty(CompareConfiguration.IGNORE_WHITESPACE, Boolean.valueOf(true));
 
 			final ITypedElement ancestor = null;   //Might be improved later
 			final ITypedElement left = getCompareItem(aTargetFile.getLocationURI());
 			final ITypedElement right = getCompareItem(aBaseFile.getLocationURI());
 
 		    input = new R4ECompareEditorInput(config, ancestor, left, right);
 			input.setTitle(R4E_COMPARE_EDITOR_TITLE);   // Adjust the compare title
 
 			Activator.Ftracer.traceInfo("Open compare editor on files " + left.getName() + " (Target) and "
 					+ right.getName() + " (Base)");
 			CompareUI.openCompareEditor(input, true);
 		}
 		
		//Set the correct difference, based on the selection index, in the compare editor
		final ICompareNavigator navigator = input.getNavigator();
 		
		while (!(navigator.selectChange(false))) { // $codepro.audit.disable emptyWhileStatement, methodInvocationInLoopCondition
			//Reset position to the first difference
		}
 			
		for (int i = 0; i < selectionIndex; i++) {
			navigator.selectChange(true);   //get the difference that corresponds to the right selection
 		}
 	}
 	
 	
 	/** 
 	 * Method findReusableCompareEditor.
 	 * 		Find the appropriate compare editor based on the file types
 	 * @param aPage IWorkbenchPage - the current workbench page
 	 * @param aBaseFileURI URI - the base (or reference) file URI
 	 * @param aTargetFileURI URI - the target file URI
 	 * @return IEditorPart - the editor to use
 	 */
 	public static IEditorPart findReusableCompareEditor(IWorkbenchPage aPage, URI aBaseFileURI, 
 			URI aTargetFileURI) {
 		
 		final IEditorReference[] editorRefs = aPage.getEditorReferences();
 		IEditorPart part = null;
 		R4ECompareEditorInput input = null;
 		String nameLeft = null;
 		String nameRight = null;
 		// first loop looking for an editor with the same input
 		for (int i = 0; i < editorRefs.length; i++) {
 			part = editorRefs[i].getEditor(false);
 			if (null != part && part instanceof IReusableEditor) {
 					// check if the editor input type complies with the types given by the caller
 					if (R4ECompareEditorInput.class.isInstance(part.getEditorInput())) {
 						//Now check if the input files are the same as with the found editor
 						input = (R4ECompareEditorInput)part.getEditorInput();
 						nameLeft = input.getLeftElement().getName();
 						nameRight = input.getRightElement().getName();
 						if (nameLeft.equals(new File(aTargetFileURI).getName()) && nameRight.equals(new File(aBaseFileURI).getName())) {
 							return part;
 						}
 					}
 			}
 		}
 		// no re-usable editor found
 		return null;
 	}
 	
 	
 	/**
 	 * Method getCompareItem.
 	 *  	Get a reference to an item that will be compared in the compare editor
 	 * @param aFileURI URI - the file URI to use
 	 * @return the ITypeElement corresponding to the compare item
 	 */
 	private static ITypedElement getCompareItem(URI aFileURI) {
 
 		if (ResourcesPlugin.getWorkspace().getRoot().exists(new Path(aFileURI.getPath()))) {
 			final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(aFileURI.getPath()));
 			return SaveableCompareEditorInput.createFileElement(file);
 		}
 		// File do not exist in workspace, create a compare item (which will not be editable)
 		return new R4ECompareItem(aFileURI);
 	}
 }
