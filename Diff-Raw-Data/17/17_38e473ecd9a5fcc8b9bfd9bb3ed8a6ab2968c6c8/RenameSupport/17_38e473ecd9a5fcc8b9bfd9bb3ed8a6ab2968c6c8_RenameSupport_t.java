 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui.refactoring;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.manipulation.IRefactoringEngine;
 import org.eclipse.dltk.internal.corext.refactoring.rename.RenameScriptFolderProcessor;
import org.eclipse.dltk.internal.corext.refactoring.rename.RenameScriptProjectProcessor;
 import org.eclipse.dltk.internal.corext.refactoring.rename.RenameSourceFolderProcessor;
 import org.eclipse.dltk.internal.corext.refactoring.rename.RenameSourceModuleProcessor;
 import org.eclipse.dltk.internal.corext.refactoring.rename.ScriptRenameProcessor;
 import org.eclipse.dltk.internal.corext.refactoring.rename.ScriptRenameRefactoring;
 import org.eclipse.dltk.internal.corext.refactoring.tagging.INameUpdating;
 import org.eclipse.dltk.internal.corext.refactoring.tagging.IReferenceUpdating;
 import org.eclipse.dltk.internal.corext.refactoring.tagging.ITextUpdating;
 import org.eclipse.dltk.internal.ui.DLTKUIMessages;
 import org.eclipse.dltk.internal.ui.refactoring.UserInterfaceStarter;
 import org.eclipse.dltk.internal.ui.refactoring.reorg.RenameUserInterfaceManager;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableContext;
 import org.eclipse.ltk.core.refactoring.RefactoringStatus;
 import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
 import org.eclipse.swt.widgets.Shell;
 
 
 public class RenameSupport {
 	
 	private RenameRefactoring fRefactoring;
 	private RefactoringStatus fPreCheckStatus;
 	
 	/** Flag indication that no additional update is to be performed. */
 	public static final int NONE= 0;
 	
 	/** Flag indicating that references are to be updated as well. */
 	public static final int UPDATE_REFERENCES= 1 << 0;
 	
 	public static final int UPDATE_TEXTUAL_MATCHES= 1 << 6;
 
 	/**
 	 * Creates a new rename support for the given {@link IScriptProject}.
 	 * 
 	 * @param project the {@link IScriptProject} to be renamed.
 	 * @param newName the project's new name. <code>null</code> is a valid
 	 * value indicating that no new name is provided.
 	 * @param flags flags controlling additional parameters. Valid flags are
 	 * <code>UPDATE_REFERENCES</code> or <code>NONE</code>.
 	 * @return the {@link RenameSupport}.
 	 * @throws CoreException if an unexpected error occurred while creating
 	 * the {@link RenameSupport}.
 	 */
 	public static RenameSupport create(IScriptProject project, String newName, int flags) throws CoreException {
 		ScriptRenameProcessor processor= new RenameScriptProjectProcessor(project);
 		return new RenameSupport(processor, newName, flags);
 	}
 	
 	private RenameSupport(ScriptRenameProcessor processor, String newName, int flags) throws CoreException {
 		fRefactoring= new ScriptRenameRefactoring(processor);
 		initialize(fRefactoring, newName, flags);
 	}
 	
 	/**
 	 * Creates a new rename support for the given {@link IProjectFragment}.
 	 * 
 	 * @param root the {@link IProjectFragment} to be renamed.
 	 * @param newName the package fragment root's new name. <code>null</code> is
 	 * a valid value indicating that no new name is provided.
 	 * @return the {@link RenameSupport}.
 	 * @throws CoreException if an unexpected error occurred while creating
 	 * the {@link RenameSupport}.
 	 */
 	public static RenameSupport create(IProjectFragment root, String newName) throws CoreException {
 		ScriptRenameProcessor processor= new RenameSourceFolderProcessor(root);
 		return new RenameSupport(processor, newName, 0);
 	}
 	
 	/**
 	 * Creates a new rename support for the given {@link IScriptFolder}.
 	 * 
 	 * @param fragment the {@link IScriptFolder} to be renamed.
 	 * @param newName the package fragment's new name. <code>null</code> is a
 	 * valid value indicating that no new name is provided.
 	 * @param flags flags controlling additional parameters. Valid flags are
 	 * <code>UPDATE_REFERENCES</code>, and <code>UPDATE_TEXTUAL_MATCHES</code>,
 	 * or their bitwise OR, or <code>NONE</code>.
 	 * @return the {@link RenameSupport}.
 	 * @throws CoreException if an unexpected error occurred while creating
 	 * the {@link RenameSupport}.
 	 */
 	public static RenameSupport create(IScriptFolder fragment, String newName, int flags) throws CoreException {
 		ScriptRenameProcessor processor= new RenameScriptFolderProcessor(fragment);
 		return new RenameSupport(processor, newName, flags);
 	}
 	
 	/**
 	 * Creates a new rename support for the given {@link ISourceModule}.
 	 * 
 	 * @param unit the {@link ISourceModule} to be renamed.
 	 * @param newName the compilation unit's new name. <code>null</code> is a
 	 * valid value indicating that no new name is provided.
 	 * @param flags flags controlling additional parameters. Valid flags are
 	 * <code>UPDATE_REFERENCES</code>, and <code>UPDATE_TEXTUAL_MATCHES</code>,
 	 * or their bitwise OR, or <code>NONE</code>.
 	 * @return the {@link RenameSupport}.
 	 * @throws CoreException if an unexpected error occurred while creating
 	 * the {@link RenameSupport}.
 	 */
 	public static RenameSupport create(ISourceModule unit, String newName, int flags) throws CoreException {
 		ScriptRenameProcessor processor= new RenameSourceModuleProcessor(unit);
 		return new RenameSupport(processor, newName, flags);
 	}	

	public static RenameSupport create(IRefactoringEngine engine,
			IModelElement element, String newName, int flags)
			throws CoreException {
		ScriptRenameProcessor processor = engine.createRenameProcessor(element);
		return new RenameSupport(processor, newName, flags);
	}
 	/**
 	 * Executes some light weight precondition checking. If the returned status
 	 * is an error then the refactoring can't be executed at all. However,
 	 * returning an OK status doesn't guarantee that the refactoring can be
 	 * executed. It may still fail while performing the exhaustive precondition
 	 * checking done inside the methods <code>openDialog</code> or
 	 * <code>perform</code>.
 	 * 
 	 * The method is mainly used to determine enable/disablement of actions.
 	 * 
 	 * @return the result of the light weight precondition checking.
 	 * 
 	 * @throws CoreException if an unexpected exception occurs while performing the checking.
 	 * 
 	 * @see #openDialog(Shell)
 	 * @see #perform(Shell, IRunnableContext)
 	 */
 	public IStatus preCheck() throws CoreException {
 		ensureChecked();
 		if (fPreCheckStatus.hasFatalError())
 			return fPreCheckStatus.getEntryMatchingSeverity(RefactoringStatus.FATAL).toStatus();
 		else
 			return new Status(IStatus.OK, DLTKUIPlugin.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
 	}
 	private void ensureChecked() throws CoreException {
 		if (fPreCheckStatus == null) {
 			if (!fRefactoring.isApplicable()) {
 				fPreCheckStatus= RefactoringStatus.createFatalErrorStatus(DLTKUIMessages.RenameSupport_not_available); 
 			} else {
 				fPreCheckStatus= new RefactoringStatus();
 			}
 		}
 	}
 	public void openDialog(Shell parent) throws CoreException {
 		ensureChecked();
 		if (fPreCheckStatus.hasFatalError()) {
 			showInformation(parent, fPreCheckStatus);
 			return; 
 		}
 		UserInterfaceStarter starter= RenameUserInterfaceManager.getDefault().getStarter(fRefactoring);
 		starter.activate(fRefactoring, parent, getScriptRenameProcessor().needsSavedEditors());
 	}
 	private void showInformation(Shell parent, RefactoringStatus status) {
 		String message= status.getMessageMatchingSeverity(RefactoringStatus.FATAL);
 		MessageDialog.openInformation(parent, DLTKUIMessages.RenameSupport_dialog_title, message); 
 	}
 	private ScriptRenameProcessor getScriptRenameProcessor() {
 		return (ScriptRenameProcessor) fRefactoring.getProcessor();
 	}
 	private static void initialize(RenameRefactoring refactoring, String newName, int flags) {
 		if (refactoring.getProcessor() == null)
 			return;
 		setNewName((INameUpdating)refactoring.getAdapter(INameUpdating.class), newName);
 		IReferenceUpdating reference= (IReferenceUpdating)refactoring.getAdapter(IReferenceUpdating.class);
 		if (reference != null) {
 			reference.setUpdateReferences(updateReferences(flags));
 		}
 		ITextUpdating text= (ITextUpdating)refactoring.getAdapter(ITextUpdating.class);
 		if (text != null) {
 			text.setUpdateTextualMatches(updateTextualMatches(flags));
 		}
 	}
 	private static void setNewName(INameUpdating refactoring, String newName) {
 		if (newName != null)
 			refactoring.setNewElementName(newName);
 	}
 	private static boolean updateReferences(int flags) {
 		return (flags & UPDATE_REFERENCES) != 0;
 	}
 	
 	private static boolean updateTextualMatches(int flags) {
 		int TEXT_UPDATES= UPDATE_TEXTUAL_MATCHES;
 		return (flags & TEXT_UPDATES) != 0;
 	}
 }
