 /*******************************************************************************
  * Copyright (c) 2011, 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.views.editor.pages;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.tcf.te.runtime.nls.Messages;
 import org.eclipse.tcf.te.ui.jface.interfaces.IValidatingContainer;
 import org.eclipse.tcf.te.ui.views.activator.UIPlugin;
 import org.eclipse.tcf.te.ui.views.editor.Editor;
 import org.eclipse.tcf.te.ui.views.interfaces.IEditorPage;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.editor.FormPage;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.part.MultiPageSelectionProvider;
 
 /**
  * Abstract details editor page implementation.
  */
 public abstract class AbstractEditorPage extends FormPage implements IEditorPage, IValidatingContainer {
 	// The unique page id
 	private String id;
 	private Composite messageComp = null;
 	private Label message = null;
 	private Label messageType = null;
 	/**
 	 * Constructor.
 	 */
 	public AbstractEditorPage() {
 		super("", ""); // //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.part.EditorPart#setInitializationData(org.eclipse.core.runtime.
 	 * IConfigurationElement, java.lang.String, java.lang.Object)
 	 */
 	@Override
 	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
 		super.setInitializationData(config, propertyName, data);
 
 		if (config != null) {
 			// Initialize the id field by reading the <id> extension attribute.
 			// Throws an exception if the id is empty or null.
 			id = config.getAttribute("id"); //$NON-NLS-1$
 			if (id == null || id.trim().length() == 0) {
 				IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), NLS
 				                .bind(Messages.Extension_error_missingRequiredAttribute, "id", config.getContributor().getName())); //$NON-NLS-1$
 				UIPlugin.getDefault().getLog().log(status);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.forms.editor.FormPage#getId()
 	 */
 	@Override
 	public String getId() {
 		return id;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see
 	 * org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
 	 */
 	@Override
 	protected void createFormContent(IManagedForm managedForm) {
 		super.createFormContent(managedForm);
 		Assert.isNotNull(managedForm);
 		managedForm.setInput(getEditorInputNode());
 
 		messageComp = new Composite(managedForm.getForm().getForm().getHead(), SWT.NONE);
 		GridLayout gl = new GridLayout(2, false);
 		gl.marginHeight = 0;
 		gl.marginWidth = 0;
 		gl.marginLeft = 0;
 		gl.marginRight = 0;
 		messageComp.setLayout(gl);
 
 		messageType = new Label(messageComp, SWT.NONE);
 		GridData gd = new GridData(20, 20);
 		messageType.setLayoutData(gd);
 		message = new Label(messageComp, SWT.NONE);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
 	 */
 	@Override
 	protected void setInput(IEditorInput input) {
 		super.setInput(input);
 		// Update the managed form too
 		if (getManagedForm() != null) {
 			getManagedForm().setInput(getEditorInputNode());
 		}
 		getSite().getSelectionProvider()
 		                .setSelection(input != null ? new StructuredSelection(getEditorInputNode()) : null);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.part.EditorPart#setInputWithNotify(org.eclipse.ui.IEditorInput)
 	 */
 	@Override
 	protected void setInputWithNotify(IEditorInput input) {
 		super.setInputWithNotify(input);
 		// Update the managed form too
 		if (getManagedForm() != null) {
 			getManagedForm().setInput(getEditorInputNode());
 		}
 		getSite().getSelectionProvider()
 		                .setSelection(input != null ? new StructuredSelection(getEditorInputNode()) : null);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.forms.editor.FormPage#setActive(boolean)
 	 */
 	@Override
 	public void setActive(boolean active) {
 		super.setActive(active);
 		if (!active) {
 			ISelection selection = getEditorInput() != null ? new StructuredSelection(getEditorInputNode()) : null;
 			getSite().getSelectionProvider().setSelection(selection);
 			if (getSite().getSelectionProvider() instanceof MultiPageSelectionProvider) {
 				SelectionChangedEvent changedEvent = new SelectionChangedEvent(getSite()
 				                .getSelectionProvider(), selection);
 				((MultiPageSelectionProvider) getSite().getSelectionProvider())
 				                .firePostSelectionChanged(changedEvent);
 			}
 		}
 	}
 
 	/**
 	 * Returns the node associated with the current editor input.
 	 *
 	 * @return The node or <code>null</code>.
 	 */
 	public Object getEditorInputNode() {
 		IEditorInput input = getEditorInput();
 		return input != null ? input.getAdapter(Object.class) : null;
 	}
 
 	/**
 	 * Called from the parent properties editor <code>doSave(IProgressMonitor)</code> method.
 	 *
 	 * @param monitor The progress monitor or <code>null</code>
 	 * @see Editor#doSave(IProgressMonitor)
 	 */
 	public void preDoSave(IProgressMonitor monitor) {
 		// do nothing
 	}
 
 	/**
 	 * Called from the parent properties editor <code>doSave(IProgressMonitor)</code> method.
 	 *
 	 * @param monitor The progress monitor or <code>null</code>
 	 * @see Editor#doSave(IProgressMonitor)
 	 */
 	public void postDoSave(IProgressMonitor monitor) {
 		// do nothing
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.jface.interfaces.IValidatingContainer#validate()
 	 */
 	@Override
 	public final void validate() {
 		ValidationResult result = doValidate();
 		if (result != null)
 			setMessage(result.getMessage(), result.getMessageType());
 		else
 			setMessage(null, IMessageProvider.NONE);
 	}
 
 	/**
 	 * Get the image for the given message type.
 	 * @param messageType The message type.
 	 * @return The image.
 	 */
 	protected Image getMessageImage(int messageType) {
 		switch (messageType) {
 		case IMessageProvider.INFORMATION:
 			return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
 		case IMessageProvider.WARNING:
 			return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
 		case IMessageProvider.ERROR:
 			return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
 		default:
 			return null;
 		}
 	}
 
 	/**
 	 * Set a message to this editor page.
 	 * @param message The message.
 	 * @param messageType The message type.
 	 */
 	@Override
     public void setMessage(String text, int type) {
 		if (getManagedForm() != null && messageComp != null && message != null && messageType != null) {
 			ScrolledForm form = getManagedForm().getForm();
 
 			if (text != null) {
 				messageType.setImage(getMessageImage(type));
 				message.setText(text);
				messageComp.pack();
 				form.setHeadClient(messageComp);
 			}
 			else {
 				form.setHeadClient(null);
 			}
			form.reflow(true);
 		}
 	}
 
 	/**
 	 * Do the validation.
 	 *
 	 * @return The validation result or <code>null</code>.
 	 */
 	protected ValidationResult doValidate() {
 		return new ValidationResult();
 	}
 }
