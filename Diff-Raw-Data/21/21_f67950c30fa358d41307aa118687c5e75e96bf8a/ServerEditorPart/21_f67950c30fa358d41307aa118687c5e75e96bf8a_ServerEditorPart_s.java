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
 
 import java.util.*;
 
 import org.eclipse.core.commands.operations.IUndoableOperation;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.wst.server.core.IServerWorkingCopy;
 import org.eclipse.wst.server.ui.internal.editor.*;
 /**
  * An abstract server editor which implements the most common methods
  * from IEditorPart.
  * 
  * This class also provides each editor page with an error message which
  * will be displayed on the status bar of the editor.
  * 
  * @since 1.0
  */
 public abstract class ServerEditorPart extends EditorPart {
 	/**
 	 * Property change id for the error message.
 	 */
 	public static final int PROP_ERROR = 5;
 
 	private String errorMessage = null;
 	private Map<String, List<ServerEditorSection>> sectionToInsertionId = null;
 	private List<ServerEditorSection> sections = null;
 	private ServerResourceCommandManager commandManager;
 	private FormToolkit toolkit;
 	private IManagedForm managedForm;
 
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
 	 * Create a new server editor part.
 	 */
 	public ServerEditorPart() {
 		super();
 	}
 
 	/**
 	 * @see org.eclipse.ui.IEditorPart#doSave(IProgressMonitor)
 	 */
 	public void doSave(IProgressMonitor monitor) {
 		// do nothing
 	}
 
 	/**
 	 * @see org.eclipse.ui.IEditorPart#doSaveAs()
 	 */
 	public void doSaveAs() {
 		// do nothing
 	}
 
 	/**
 	 * @see org.eclipse.ui.IEditorPart#isDirty()
 	 */
 	public boolean isDirty() {
 		return false;
 	}
 
 	/**
 	 * @see org.eclipse.ui.IEditorPart#isSaveAsAllowed()
 	 */
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
 	/**
 	 * Set the managed form that this part is using.
 	 * 
 	 * @param managedForm a managed form
 	 */
 	protected void setManagedForm(IManagedForm managedForm) {
 		this.managedForm = managedForm;
 	}
 
 	/**
 	 * Returns the managed form that this part is using, or <code>null</code> if no
 	 * managed form has been set.
 	 * 
 	 * @return managedForm the managed form that this part is using, or <code>null</code>
 	 *    if no managed form has been set
 	 */
 	protected IManagedForm getManagedForm() {
 		return managedForm;
 	}
 
 	/**
 	 * Set an error message for this page.
 	 * 
 	 * @param error the error message
 	 * @see #getManagedForm() Use forms UI based for errors via {@link org.eclipse.ui.forms.IMessageManager}
 	 *    on the message form instead of this method
 	 */
 	public void setErrorMessage(String error) {
 		if (error == null && errorMessage == null)
 			return;
 		
 		if (error != null && error.equals(errorMessage))
 			return;
 		
 		errorMessage = error;
 		super.firePropertyChange(PROP_ERROR);
 	}
 
 	/**
 	 * Updates the error message shown in the editor.
 	 * 
 	 * @see #getManagedForm() Use forms UI based for errors via {@link org.eclipse.ui.forms.IMessageManager}
 	 *    on the message form instead of this method
 	 */
 	public void updateErrorMessage() {
 		super.firePropertyChange(PROP_ERROR);
 	}
 
 	/**
 	 * Return the error message for this page.
 	 * 
 	 * @return the error message
 	 * @see #getManagedForm() Use forms UI based for errors via {@link org.eclipse.ui.forms.IMessageManager}
 	 *    on the message form instead of this method
 	 */
 	public String getErrorMessage() {
 		if (errorMessage == null) {
 			Iterator iterator = getSections().iterator();
 			while (iterator.hasNext()) {
 				ServerEditorSection section = (ServerEditorSection) iterator.next();
 				String error = section.getErrorMessage();
 				if (error != null)
 					return error;
 			}
 		}
 		return errorMessage;
 	}
 
 	/**
 	 * Returns error or status messages that will be displayed when the
 	 * server resource is saved. If there are any error messages, the
 	 * user will be unable to save the editor.
 	 * 
 	 * @return a set of status
 	 * @see #getManagedForm() Use forms UI based for errors via {@link org.eclipse.ui.forms.IMessageManager}
 	 *    on the message form instead of this method
 	 */
 	public IStatus[] getSaveStatus() {
 		Iterator iterator = getSections().iterator();
 		List<IStatus> list = new ArrayList<IStatus>();
 		while (iterator.hasNext()) {
 			ServerEditorSection section = (ServerEditorSection) iterator.next();
 			IStatus[] status = section.getSaveStatus();
 			if (status != null) {
 				int size = status.length;
				for (int i = 0; i < size; i++)
					list.add(status[i]);
 			}
 		}
 		
 		int size = list.size();
 		IStatus[] status = new IStatus[size];
 		list.toArray(status);
 		return status;
 	}
 
 	private List getSections() {
 		if (sections == null) {
 			sections = new ArrayList<ServerEditorSection>();
 			sectionToInsertionId = new HashMap<String, List<ServerEditorSection>>();
 			ServerEditor serverEditor = commandManager.getServerEditor();
 			Iterator iterator = ServerEditorCore.getServerEditorPageSectionFactories().iterator();
 			while (iterator.hasNext()) {
 				IServerEditorPageSectionFactory factory = (IServerEditorPageSectionFactory) iterator.next();
 				String insertionId = factory.getInsertionId();
 				
 				IServerEditorPartFactory pageFactory = serverEditor.getPageFactory(this);
 				if (pageFactory.supportsInsertionId(insertionId)) {
 					String serverTypeId = null;
 					if (server != null && server.getServerType() != null)
 						serverTypeId = server.getServerType().getId();
 					if (serverTypeId != null && factory.supportsType(serverTypeId)
 							&& factory.shouldCreateSection(server)) {
 						ServerEditorSection section = factory.createSection();
 						if (section != null) {
 							section.setServerEditorPart(this);
 							sections.add(section);
 							List<ServerEditorSection> list = null;
 							try {
 								list = sectionToInsertionId.get(insertionId);
 							} catch (Exception e) {
 								// ignore
 							}
 							if (list == null)
 								list = new ArrayList<ServerEditorSection>();
 							list.add(section);
 							sectionToInsertionId.put(insertionId, list);
 						}
 					}
 				}
 			}
 		}
 		return sections;
 	}
 
 	private List getSections(String insertionId) {
 		if (insertionId == null)
 			return null;
 		
 		getSections();
 		List<ServerEditorSection> list = new ArrayList<ServerEditorSection>();
 		try {
 			List<ServerEditorSection> sections2 = sectionToInsertionId.get(insertionId);
 			if (sections2 != null) {
 				Iterator<ServerEditorSection> iterator = sections2.iterator();
 				while (iterator.hasNext()) {
 					list.add(iterator.next());
 				}
 			}
 		} catch (Exception e) {
 			// ignore
 		}
 		return list;
 	}
 
 	/**
 	 * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
 	 */
 	public void init(IEditorSite site, IEditorInput input) {
 		setSite(site);
 		setInput(input);
 		if (input instanceof IServerEditorPartInput) {
 			IServerEditorPartInput sepi = (IServerEditorPartInput) input;
 			server = sepi.getServer();
 			commandManager = ((ServerEditorPartInput) sepi).getServerCommandManager();
 			readOnly = sepi.isServerReadOnly();
 		}
 		
 		Iterator iterator = getSections().iterator();
 		while (iterator.hasNext()) {
 			ServerEditorSection section = (ServerEditorSection) iterator.next();
 			section.init(site, input);
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
 	 * Return the server that is being edited.
 	 * 
 	 * @return a server working copy
 	 */
 	public IServerWorkingCopy getServer() {
 		return server;
 	}
 
 	/**
 	 * Inserts editor sections into the given composite.
 	 * 
 	 * @param parent the composite to add the section(s) to
 	 * @param id the section insertion id
 	 */
 	public void insertSections(Composite parent, String id) {
 		if (id == null)
 			return;
 		
 		Iterator iterator = getSections(id).iterator();
 		while (iterator.hasNext()) {
 			ServerEditorSection section = (ServerEditorSection) iterator.next();
 			section.createSection(parent);
 		}
 	}
 
 	/**
 	 * Dispose of the editor.
 	 */
 	public void dispose() {
 		super.dispose();
 		
 		Iterator iterator = getSections().iterator();
 		while (iterator.hasNext()) {
 			ServerEditorSection section = (ServerEditorSection) iterator.next();
 			section.dispose();
 		}
 		
 		if (toolkit != null) {
 			toolkit.dispose();
 			toolkit = null;
 		}
 		
 		commandManager = null;
 		sectionToInsertionId = null;
 		sections = null;
 	}
 
 	/**
 	 * Get a form toolkit to create widgets. It will be disposed automatically
 	 * when the editor is closed.
 	 * 
 	 * @param display the display
 	 * @return FormToolkit
 	 */
 	protected FormToolkit getFormToolkit(Display display) {
 		if (managedForm != null)
 			return managedForm.getToolkit();
 		
 		if (toolkit == null)
 			toolkit = new FormToolkit(display);
 		return toolkit;
 	}
 }
