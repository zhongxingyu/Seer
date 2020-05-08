 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
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
  * This class implements the context-sensitive command used 
  * to remove the currently selected element form the model
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.ui.internal.commands;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.MessageDialogWithToggle;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFormalReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewComponent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIContent;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewItem;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.MailServicesProxy;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.progress.UIJob;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class RemoveElementHandler extends AbstractHandler {
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method execute.
 	 * 
 	 * @param event
 	 *            ExecutionEvent
 	 * @return Object
 	 * @throws ExecutionException
 	 * @see org.eclipse.core.commands.IHandler#execute(ExecutionEvent)
 	 */
 	public Object execute(final ExecutionEvent event) {
 
 		final UIJob job = new UIJob("Removing Element...") {
 			@Override
 			public IStatus runInUIThread(IProgressMonitor monitor) {
 				final ISelection selection = HandlerUtil.getCurrentSelection(event);
 				if (selection instanceof IStructuredSelection) {
 					if (!selection.isEmpty()) {
 						Object element = null;
 						MessageDialogWithToggle dialog = null;
 						R4EReview review = null;
 						if (null != R4EUIModelController.getActiveReview()) {
 							review = R4EUIModelController.getActiveReview().getReview();
 						}
 						final List<R4EReviewComponent> removedItems = new ArrayList<R4EReviewComponent>();
 						for (final Iterator<?> iterator = ((IStructuredSelection) selection).iterator(); iterator.hasNext();) {
 							element = iterator.next();
 							if (!(element instanceof IR4EUIModelElement)) {
 								continue;
 							}
 							R4EUIPlugin.Ftracer.traceInfo("Disable element " + ((IR4EUIModelElement) element).getName());
 							dialog = MessageDialogWithToggle.openOkCancelConfirm(null, "Disable element",
 									"Do you really want to disable this element?",
 									"Also delete from file (not supported yet)", false, null, null);
 							if (dialog.getReturnCode() == Window.OK) {
 								try {
 									if (element instanceof R4EUIReviewItem) {
 										removedItems.add(((R4EUIReviewItem) element).getItem());
 									} else if (element instanceof R4EUIContent) {
 										removedItems.add(((R4EUIContent) element).getContent());
 									}
 
 									if (((IR4EUIModelElement) element).isOpen()) {
 										((IR4EUIModelElement) element).close();
 										for (IR4EUIModelElement childElement : ((IR4EUIModelElement) element).getChildren()) {
 											if (null != childElement && childElement.isOpen()) {
 												childElement.close();
 												break;
 											}
 										}
 									}
									UIUtils.setNavigatorViewFocus(((IR4EUIModelElement) element).getParent(), 0);
 									((IR4EUIModelElement) element).getParent().removeChildren(
 											(IR4EUIModelElement) element, dialog.getToggleState());
 								} catch (ResourceHandlingException e) {
 									UIUtils.displayResourceErrorDialog(e);
 								} catch (OutOfSyncException e) {
 									UIUtils.displaySyncErrorDialog(e);
 								}
 							}
 						}
 
 						//Send email notification if needed
 						if (null != review) {
 							if (0 < removedItems.size()
 									&& review.getType().equals(R4EReviewType.R4E_REVIEW_TYPE_FORMAL)) {
 								if (((R4EFormalReview) review).getCurrent()
 										.getType()
 										.equals(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION)) {
 									try {
 										MailServicesProxy.sendItemsRemovedNotification(removedItems);
 									} catch (CoreException e) {
 										UIUtils.displayCoreErrorDialog(e);
 									} catch (ResourceHandlingException e) {
 										UIUtils.displayResourceErrorDialog(e);
 									}
 								}
 							}
 						}
 					}
 				}
 				return Status.OK_STATUS;
 			}
 		};
 		job.setUser(true);
 		job.schedule();
 		return null;
 	}
 }
