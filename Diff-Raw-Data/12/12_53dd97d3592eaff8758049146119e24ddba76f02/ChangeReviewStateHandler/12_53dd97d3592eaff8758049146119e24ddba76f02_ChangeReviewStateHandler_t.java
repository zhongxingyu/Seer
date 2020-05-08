 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2011 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the context-sensitive command to set an element
  * as reviewed/not reviewed
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.ui.internal.commands;
 
 import java.util.Iterator;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.SendNotificationInputDialog;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.MailServicesProxy;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.progress.UIJob;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class ChangeReviewStateHandler extends AbstractHandler {
 
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
 
 		final UIJob job = new UIJob("Changing Reviewed State...") {
 			@Override
 			public IStatus runInUIThread(IProgressMonitor monitor) {
 				final ISelection selection = HandlerUtil.getCurrentSelection(event);
 				if (selection instanceof IStructuredSelection) {
 					if (!selection.isEmpty()) {
 						IR4EUIModelElement element = null;
 						for (final Iterator<?> iterator = ((IStructuredSelection) selection).iterator(); iterator.hasNext();) {
 							try {
 								element = (IR4EUIModelElement) iterator.next();
 								R4EUIPlugin.Ftracer.traceInfo("Changing review state for element " + element.getName());
 								element.setUserReviewed(!(element.isUserReviewed()));
 
 								//If we just completed the review, prompt user for mail sending
 								if (R4EUIModelController.getActiveReview().isUserReviewed()) {
 									R4EUIModelController.setJobInProgress(true);
 
 									final SendNotificationInputDialog dialog = new SendNotificationInputDialog(
 											R4EUIModelController.getNavigatorView()
 													.getSite()
 													.getWorkbenchWindow()
													.getShell(), R4EUIModelController.getActiveReview());
 									dialog.create();
 									final int result = dialog.open();
 									if (result == Window.OK) {
 										final int messageType = dialog.getMessageTypeValue();
 										try {
 											switch (messageType) {
 											case R4EUIConstants.MESSAGE_TYPE_COMPLETION:
 												//Send completion notification
 												MailServicesProxy.sendCompletionNotification();
 												break;
 											default:
 												//Do nothing, should never happen
 											}
 										} catch (CoreException e) {
 											UIUtils.displayCoreErrorDialog(e);
 										} catch (ResourceHandlingException e) {
 											UIUtils.displayResourceErrorDialog(e);
 										} finally {
 											R4EUIModelController.setJobInProgress(false);
 										}
 									} else if (result != Window.CANCEL) {
 										R4EUIModelController.setJobInProgress(false); //Enable commands in case of error
 									}
 								}
 								UIUtils.setNavigatorViewFocus(element, 0);
 							} catch (ResourceHandlingException e) {
 								UIUtils.displayResourceErrorDialog(e);
 							} catch (OutOfSyncException e) {
 								UIUtils.displaySyncErrorDialog(e);
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
