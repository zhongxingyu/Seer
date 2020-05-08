 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 347421 - Make setDoneChanges accessible to sub classes
  *    Benjamin Schmeling - mwenz - Bug 367483 - Support composite connections
 *    mgorning - Bug 376572 - Generic context buttons name changeable via getName() method
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.features;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.graphiti.features.IDeleteFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.IMultiDeleteInfo;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.impl.RemoveContext;
 import org.eclipse.graphiti.features.impl.AbstractFeature;
 import org.eclipse.graphiti.mm.pictograms.CompositeConnection;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.ui.internal.Messages;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * The Graphiti default implementation of the {@link IDeleteFeature}. It cares
  * about removing the shape(s) representing the deleted domain object(s) from
  * the diagram (by delegating to the {@link IRemoveFeature} provided by the
  * {@link IFeatureProvider}. After that the EMF domain objects will be deleted
  * using standard EMF functionality.<br>
  * Before the deletion process starts the tool user will be asked if he really
  * wants to delete; the popup appears only once for multi object deletions.
  */
 public class DefaultDeleteFeature extends AbstractFeature implements IDeleteFeature {
 
 	private static final String NAME = Messages.DefaultDeleteFeature_1_xfld;
 	private static final String DESC = org.eclipse.graphiti.internal.Messages.ContextEntryHelper_5_xfld;
 
 	private boolean doneChanges = false;
 
 	/**
 	 * Creates a new {@link DefaultDeleteFeature}.
 	 * 
 	 * @param fp
 	 *            the feature provider
 	 */
 	public DefaultDeleteFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	/*
 	 * @see
 	 * org.eclipse.graphiti.func.IDelete#canDelete(org.eclipse.graphiti.features
 	 * .context.IDeleteContext)
 	 */
 	public boolean canDelete(IDeleteContext context) {
 		PictogramElement pictogramElement = context.getPictogramElement();
 		IRemoveContext rc = new RemoveContext(pictogramElement);
 		IRemoveFeature removeFeature = getFeatureProvider().getRemoveFeature(rc);
 		boolean ret = (removeFeature != null) && removeFeature.canRemove(rc);
 		return ret;
 	}
 
 	/*
 	 * @see
 	 * org.eclipse.graphiti.func.IDelete#delete(org.eclipse.graphiti.features
 	 * .context.IDeleteContext)
 	 */
 	public void delete(IDeleteContext context) {
 		// we need this reset, since the an instance of this feature can be
 		// used multiple times, e.g. as a part of a pattern
 		setDoneChanges(false);
 
 		IMultiDeleteInfo multiDeleteInfo = context.getMultiDeleteInfo();
 		if (multiDeleteInfo != null && multiDeleteInfo.isDeleteCanceled()) {
 			return;
 		}
 		PictogramElement pe = context.getPictogramElement();
 		Object[] businessObjectsForPictogramElement = getAllBusinessObjectsForPictogramElement(pe);
 		if (businessObjectsForPictogramElement != null && businessObjectsForPictogramElement.length > 0) {
 			if (multiDeleteInfo == null) {
 				if (!getUserDecision(context)) {
 					return;
 				}
 			} else {
 				if (multiDeleteInfo.isShowDialog()) {
 					boolean okPressed = getUserDecision(context);
 					if (okPressed) {
 						// don't show further dialogs
 						multiDeleteInfo.setShowDialog(false);
 					} else {
 						multiDeleteInfo.setDeleteCanceled(true);
 						return;
 					}
 				}
 			}
 		}
 
 		preDelete(context);
 		if (pe instanceof CompositeConnection) {
 			// Find all domain objects for the children connections of the
 			// composite connection...
 			List<Object> compositeChildConnectionsBOs = collectCompositeConnectionsBOs((CompositeConnection) pe);
 			// ... and add them to the list of BOs to delete (no duplicates)
 			for (Object object : businessObjectsForPictogramElement) {
 				if (!compositeChildConnectionsBOs.contains(object)) {
 					compositeChildConnectionsBOs.add(object);
 				}
 			}
 			// Update BOs to delete
 			businessObjectsForPictogramElement = compositeChildConnectionsBOs
 					.toArray(new Object[compositeChildConnectionsBOs.size()]);
 		}
 		IRemoveContext rc = new RemoveContext(pe);
 		IFeatureProvider featureProvider = getFeatureProvider();
 		IRemoveFeature removeFeature = featureProvider.getRemoveFeature(rc);
 		if (removeFeature != null) {
 			removeFeature.remove(rc);
 			// Bug 347421: Set hasDoneChanges flag only after first modification
 			setDoneChanges(true);
 		}
 
 		deleteBusinessObjects(businessObjectsForPictogramElement);
 
 		postDelete(context);
 	}
 
 	private List<Object> collectCompositeConnectionsBOs(CompositeConnection composite) {
 		List<Object> result = new ArrayList<Object>();
 		for (Connection childConnection : composite.getChildren()) {
 			Object[] allBusinessObjectsForChildConnection = getAllBusinessObjectsForPictogramElement(childConnection);
 			for (Object object : allBusinessObjectsForChildConnection) {
 				if (!result.contains(object)) {
 					result.add(object);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Standard functionality to delete all given domain objects.
 	 * 
 	 * @param businessObjects
 	 *            the domain objects to delete
 	 */
 	protected void deleteBusinessObjects(Object[] businessObjects) {
 		if (businessObjects != null) {
 			for (Object bo : businessObjects) {
 				deleteBusinessObject(bo);
 			}
 		}
 	}
 
 	/**
 	 * Standard functionality to delete one domain object. Will delegate for
 	 * {@link EObject}s to {@link EcoreUtil#delete(EObject, boolean)}; non-EMF
 	 * objects will be ignored.
 	 * 
 	 * @param bo
 	 *            the domain object to delete
 	 */
 	protected void deleteBusinessObject(Object bo) {
 		if (bo instanceof EObject) {
 			EcoreUtil.delete((EObject) bo, true);
 		}
 	}
 
 	/*
 	 * @see
 	 * org.eclipse.graphiti.func.IDelete#preDelete(org.eclipse.graphiti.features
 	 * .context.IDeleteContext)
 	 */
 	public void preDelete(IDeleteContext context) {
 	}
 
 	/*
 	 * @see
 	 * org.eclipse.graphiti.func.IDelete#postDelete(org.eclipse.graphiti.features
 	 * .context.IDeleteContext)
 	 */
 	public void postDelete(IDeleteContext context) {
 	}
 
 	/*
 	 * @see
 	 * org.eclipse.graphiti.features.IFeature#canExecute(org.eclipse.graphiti
 	 * .features.context.IContext)
 	 */
 	public boolean canExecute(IContext context) {
 		boolean ret = false;
 		if (context instanceof IDeleteContext) {
 			ret = canDelete((IDeleteContext) context);
 		}
 		return ret;
 	}
 
 	/*
 	 * @see
 	 * org.eclipse.graphiti.features.IFeature#execute(org.eclipse.graphiti.features
 	 * .context.IContext)
 	 */
 	public void execute(IContext context) {
 		if (context instanceof IDeleteContext) {
 			delete((IDeleteContext) context);
 		}
 	}
 
 	/**
 	 * Hook to return the name of the delete operation e.g for display in the
 	 * context menu. The standard implementation simply returns "Delete".
 	 * 
 	 * @return The diplay name of the operation
 	 */
 	@Override
 	public String getName() {
 		return NAME;
 	}
 
 	@Override
 	public String getDescription() {
 		return DESC;
 	}
 
 	/*
 	 * @see org.eclipse.graphiti.features.IFeature#hasDoneChanges()
 	 */
 	@Override
 	public boolean hasDoneChanges() {
 		return doneChanges;
 	}
 
 	/**
 	 * Can be called by users (in subclasses of {@link DefaultDeleteFeature}) to
 	 * tell the framework that changes have been done that should reflect on the
 	 * command stack. The method is called by the standard implementation in
 	 * {@link #delete(IDeleteContext)} right after the first shape has been
 	 * removed from the diagram.
 	 * 
 	 * @param doneChanges
 	 *            <code>true</code>in case modifications have been made,
 	 *            <code>false</code> otherwise
 	 * 
 	 * @since 0.9
 	 */
 	protected void setDoneChanges(boolean doneChanges) {
 		this.doneChanges = doneChanges;
 	}
 
 	/**
 	 * Shows a dialog which asks the user to confirm the deletion of one or more
 	 * elements.
 	 * 
 	 * @param context
 	 *            delete context
 	 * @return <code>true</code> to delete element(s); <code>false</code> to
 	 *         cancel delete
 	 */
 	protected boolean getUserDecision(IDeleteContext context) {
 		String msg;
 		IMultiDeleteInfo multiDeleteInfo = context.getMultiDeleteInfo();
 		if (multiDeleteInfo != null) {
 			msg = MessageFormat.format(Messages.DefaultDeleteFeature_2_xmsg, multiDeleteInfo.getNumber());
 		} else {
 			String deleteName = getDeleteName(context);
 			if (deleteName != null && deleteName.length() > 0) {
 				msg = MessageFormat.format(Messages.DefaultDeleteFeature_3_xmsg, deleteName);
 			} else {
 				msg = Messages.DefaultDeleteFeature_4_xmsg;
 			}
 		}
 		return MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
 				Messages.DefaultDeleteFeature_5_xfld, msg);
 	}
 
 	/**
 	 * Returns the delete name which will be used for the delete dialog. E.g.
 	 * "file test.java". The default implementation returns <code>null</code> to
 	 * indicate that the popup text refers to "this object".
 	 * 
 	 * @param context
 	 *            the delete context
 	 * @return the delete name, or <code>null</code> to indicate no special name
 	 *         shall be used
 	 * @since 0.8
 	 */
 	protected String getDeleteName(IDeleteContext context) {
 		return null;
 	}
 }
