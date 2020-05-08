 /*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.server.ui.editor;
 
 import org.eclipse.core.commands.operations.IUndoableOperation;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.wst.server.core.IServerWorkingCopy;
 import org.eclipse.wst.server.ui.internal.editor.ServerEditorPartInput;
 import org.eclipse.wst.server.ui.internal.editor.ServerResourceCommandManager;
 /**
  * An abstract server editor section.
  * 
  * @since 1.0
  */
 public abstract class ServerEditorSection {
 	private String errorMessage = null;
 
 	private ServerResourceCommandManager commandManager;
 
 	private Composite parentComp;
 	private ServerEditorPart editor;
 
 	/**
 	 * The server currently being edited.
 	 */
 	protected IServerWorkingCopy server;
 
 	/**
 	 * <code>true</code> if the server is read-only, and <code>false</code>
 	 * otherwise.
 	 */
 	protected boolean readOnly;
 
 	/**
 	 * Initialize the section.
 	 * 
 	 * @param site the editor site
 	 * @param input the editor input
 	 */
 	public void init(IEditorSite site, IEditorInput input) {
 		if (input instanceof IServerEditorPartInput) {
 			IServerEditorPartInput sepi = (IServerEditorPartInput) input;
 			server = sepi.getServer();
 			commandManager = ((ServerEditorPartInput) sepi).getServerCommandManager();
 			readOnly = sepi.isServerReadOnly();
 		}
 	}
 
 	/**
 	 * Executes the given operation and adds it to the operation history
 	 * with the correct context.
 	 * 
 	 * @param operation an operation ready to be executed
 	 */
 	public void execute(IUndoableOperation operation) {
 		commandManager.execute(operation);
 	}
 
 	/**
 	 * Create the section.
 	 * 
 	 * @param parent the parent composite
 	 */
 	public void createSection(Composite parent) {
 		this.parentComp = parent;
 	}
 
 	/**
 	 * Return the shell of the section.
 	 * 
 	 * @return the shell
 	 */
 	public Shell getShell() {
 		return parentComp.getShell();
 	}
 
 	/**
 	 * Return the error message for this section.
 	 * 
 	 * @return the error message
 	 * @see #getManagedForm() Use forms UI based for errors via {@link org.eclipse.ui.forms.IMessageManager}
 	 *    on the message form instead of this method
 	 */
 	public String getErrorMessage() {
 		return errorMessage;
 	}
 
 	/**
 	 * Returns error or status messages that will be displayed when the
 	 * server resource is saved. If there are any error messages, the
 	 * user will be unable to save the editor.
 	 * 
 	 * @return a status object with code <code>IStatus.OK</code> if this
 	 *   server can be saved, otherwise a status object indicating why
 	 *   it can't be
 	 * @see #getManagedForm() Use forms UI based for errors via {@link org.eclipse.ui.forms.IMessageManager}
 	 *    on the message form instead of this method
 	 */
 	public IStatus[] getSaveStatus() {
		return null;
 	}
 
 	/**
 	 * Set the editor part that this section belongs to.
 	 * 
 	 * @param editor the editor
 	 */
 	public void setServerEditorPart(ServerEditorPart editor) {
 		this.editor = editor;
 	}
 
 	/**
 	 * Set an error message for this section.
 	 * 
 	 * @param error an error message
 	 * @see #getManagedForm() Use forms UI based for errors via {@link org.eclipse.ui.forms.IMessageManager}
 	 *    on the message form instead of this method
 	 */
 	public void setErrorMessage(String error) {
 		if (error == null && errorMessage == null)
 			return;
 		
 		if (error != null && error.equals(errorMessage))
 			return;
 		
 		errorMessage = error;
 		if (editor != null)
 			editor.updateErrorMessage();
 	}
 
 	/**
 	 * Get a form toolkit to create widgets. It will be disposed automatically
 	 * when the editor is closed.
 	 * 
 	 * @param display the display
 	 * @return a FormToolkit
 	 */
 	protected FormToolkit getFormToolkit(Display display) {
 		return editor.getFormToolkit(display);
 	}
 
 	/**
 	 * Returns the managed form that the editor is using, or <code>null</code> if no
 	 * managed form has been set.
 	 * 
 	 * @return the managed form that the editor is using, or <code>null</code> if no
 	 *    managed form has been set.
 	 */
 	protected IManagedForm getManagedForm() {
 		return editor.getManagedForm();
 	}
 
 	/**
 	 * Disposes of the section.
 	 */
 	public void dispose() {
 		// ignore
 	}
 }
