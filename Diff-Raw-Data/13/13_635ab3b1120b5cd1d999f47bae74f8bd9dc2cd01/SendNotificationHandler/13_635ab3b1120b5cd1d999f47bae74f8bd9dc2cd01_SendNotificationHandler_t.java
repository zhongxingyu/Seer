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
  * This class implements the command used to send an email or a notification to
  * other participants
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.ui.internal.commands;
 
 import java.util.AbstractSet;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.expressions.EvaluationContext;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.SendNotificationInputDialog;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.MailServicesProxy;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.progress.UIJob;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class SendNotificationHandler extends AbstractHandler {
 
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
 
 		final UIJob job = new UIJob("Sending Notification...") {
 			@Override
 			public IStatus runInUIThread(IProgressMonitor monitor) {
 				final Object source = ((EvaluationContext) event.getApplicationContext()).getDefaultVariable();
 				Object obj = null;
 				if (source instanceof List) {
 					if (((List<?>) source).size() > 0) {
 						final Object sourceElement = ((List<?>) source).get(0); //If this is a list, get first element
 						if (sourceElement instanceof AbstractSet) {
 							final Iterator<?> iterator = ((AbstractSet<?>) sourceElement).iterator();
 							obj = iterator.next();
 						} else {
 							obj = source;
 						}
 					} else {
 						//empty selection, try to get active editor selection
 						final IEditorPart editorPart = PlatformUI.getWorkbench()
 								.getActiveWorkbenchWindow()
 								.getActivePage()
 								.getActiveEditor(); // $codepro.audit.disable methodChainLength
 
 						//Try to get the active editor highlighted range and set it as the editor's selection
 						if (null != editorPart) {
 							if (editorPart instanceof ITextEditor) {
 								obj = editorPart;
 							}
 						}
 					}
 				} else if (source instanceof AbstractSet) {
 					final Iterator<?> iterator = ((AbstractSet<?>) source).iterator();
 					obj = iterator.next();
 				}
 				R4EUIModelController.setJobInProgress(true);
 				//if the source is unique and is a Review element, all options are available.  Otherwise, only ask questions is supported
 				final SendNotificationInputDialog dialog = new SendNotificationInputDialog(
 						R4EUIModelController.getNavigatorView().getSite().getWorkbenchWindow().getShell(), obj);
 				dialog.create();
 				final int result = dialog.open();
 				if (result == Window.OK) {
 					final int messageType = dialog.getMessageTypeValue();
 
 					try {
 						switch (messageType) {
 						case R4EUIConstants.MESSAGE_TYPE_ITEMS_READY:
 							//Send review items ready notification
 							MailServicesProxy.sendItemsReadyNotification();
 							break;
 						case R4EUIConstants.MESSAGE_TYPE_PROGRESS:
 							//Send progress notification
 							MailServicesProxy.sendProgressNotification();
 							break;
 						case R4EUIConstants.MESSAGE_TYPE_COMPLETION:
 							//Send completion notification
 							MailServicesProxy.sendCompletionNotification();
 							break;
 						case R4EUIConstants.MESSAGE_TYPE_QUESTION:
 							//Send question
 							MailServicesProxy.sendQuestion(obj);
 							break;
 						case R4EUIConstants.MESSAGE_TYPE_MEETING:
 							//Send question
 							MailServicesProxy.sendMeetingRequest();
							UIUtils.setNavigatorViewFocus(R4EUIModelController.getActiveReview(), 0);
 							break;
 						default:
 							//Do nothing, should never happen
 						}
 					} catch (CoreException e) {
 						UIUtils.displayCoreErrorDialog(e);
 					} catch (ResourceHandlingException e) {
 						UIUtils.displayResourceErrorDialog(e);
 					} catch (OutOfSyncException e) {
 						UIUtils.displaySyncErrorDialog(e);
 					} finally {
 						R4EUIModelController.setJobInProgress(false);
 					}
 				} else if (result != Window.CANCEL) {
 					R4EUIModelController.setJobInProgress(false); //Enable commands in case of error
 				}
 				return Status.OK_STATUS;
 			}
 		};
 		job.setUser(true);
 		job.schedule();
 		return null;
 	}
 }
