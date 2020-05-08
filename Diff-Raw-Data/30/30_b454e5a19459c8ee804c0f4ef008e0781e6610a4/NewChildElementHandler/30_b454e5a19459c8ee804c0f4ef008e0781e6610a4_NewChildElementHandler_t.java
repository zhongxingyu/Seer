 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2011, 2013 Ericsson AB and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the context-sensitive command to add a child element
  * to the navigator view/model
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.ui.internal.commands.handlers;
 
 import java.util.List;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.viewers.AbstractTreeViewer;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.mylyn.reviews.frame.core.model.ReviewComponent;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.CompatibilityException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.swt.widgets.Widget;
 
 /**
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class NewChildElementHandler extends AbstractHandler {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field COMMAND_MESSAGE. (value is ""Adding New Child Element..."")
 	 */
 	private static final String COMMAND_MESSAGE = "Adding New Child Element...";
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method execute.
 	 * 
 	 * @param aEvent
 	 *            ExecutionEvent
 	 * @return Object
 	 * @see org.eclipse.core.commands.IHandler#execute(ExecutionEvent)
 	 */
 	public Object execute(final ExecutionEvent aEvent) {
 
 		final IR4EUIModelElement element = getParentElement(aEvent);
 
 		//Get data from user
 		if (null != element) {
 			try {
 				final List<ReviewComponent> tempModelComponents = element.createChildModelDataElement();
 
 				//Create actual model element
 				final Job job = new Job(COMMAND_MESSAGE) {
 					public String familyName = R4EUIConstants.R4E_UI_JOB_FAMILY;
 
 					@Override
 					public boolean belongsTo(Object family) {
 						return familyName.equals(family);
 					}
 
 					@Override
 					public IStatus run(IProgressMonitor monitor) {
 						R4EUIModelController.setJobInProgress(true);
 						monitor.beginTask(COMMAND_MESSAGE, IProgressMonitor.UNKNOWN);
 
 						if (tempModelComponents != null) {
 							for (final ReviewComponent tempModelComponent : tempModelComponents) {
 								R4EUIPlugin.Ftracer.traceInfo("Adding child to element " + element.getName());
 								IR4EUIModelElement newElement = null;
 								try {
 									newElement = element.createChildren(tempModelComponent);
 									R4EUIModelController.setJobInProgress(false);
 									UIUtils.setNavigatorViewFocus(newElement, AbstractTreeViewer.ALL_LEVELS);
 								} catch (ResourceHandlingException e) {
 									UIUtils.displayResourceErrorDialog(e);
 
 									//Remove object if partially created
 									try {
 										element.removeChildren(newElement, true);
 									} catch (ResourceHandlingException e1) {
 										UIUtils.displayResourceErrorDialog(e1);
 									} catch (OutOfSyncException e1) {
 										UIUtils.displaySyncErrorDialog(e1);
 									} catch (CompatibilityException e1) {
 										UIUtils.displayCompatibilityErrorDialog(e1);
 									}
 
 								} catch (CompatibilityException e) {
 									UIUtils.displayCompatibilityErrorDialog(e);
 
 									//Remove object if partially created
 									try {
 										element.removeChildren(newElement, true);
 									} catch (ResourceHandlingException e1) {
 										UIUtils.displayResourceErrorDialog(e1);
 									} catch (OutOfSyncException e1) {
 										UIUtils.displaySyncErrorDialog(e1);
 									} catch (CompatibilityException e1) {
 										UIUtils.displayCompatibilityErrorDialog(e1);
 									}
 								} catch (OutOfSyncException e) {
 									UIUtils.displaySyncErrorDialog(e);
 
 									//Remove object if partially created
 									try {
 										element.removeChildren(newElement, true);
 									} catch (ResourceHandlingException e1) {
 										UIUtils.displayResourceErrorDialog(e1);
 									} catch (OutOfSyncException e1) {
 										UIUtils.displaySyncErrorDialog(e1);
 									} catch (CompatibilityException e1) {
 										UIUtils.displayCompatibilityErrorDialog(e1);
 									}
 								}
 							}
 						} else {
 							R4EUIPlugin.Ftracer.traceInfo("Adding child to element NULL");
 						}
 
 						R4EUIModelController.setJobInProgress(false);
 						monitor.done();
 						return Status.OK_STATUS;
 					}
 				};
 				job.setUser(true);
 				job.schedule();
 			} catch (ResourceHandlingException e) {
 				UIUtils.displayResourceErrorDialog(e);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Method getParentElement.
 	 * 
 	 * @param event
 	 *            ExecutionEvent
 	 * @return IR4EUIModelElement
 	 */
 	private IR4EUIModelElement getParentElement(ExecutionEvent event) {
 		final Widget triggerObject = ((Event) event.getTrigger()).widget;
 		IR4EUIModelElement element = null;
 
 		if (triggerObject instanceof ToolItem) {
 			Object data = ((ToolItem) triggerObject).getParent().getData(R4EUIConstants.ANNOTATION_TOOLBAR);
 			if (null == data || !(Boolean) data) {
 				//Add element to the root of the tree
 				return R4EUIModelController.getRootElement();
 			}
 		}
 
 		if (triggerObject instanceof MenuItem) {
 			Object data = ((MenuItem) triggerObject).getData();
			if (data != null && data instanceof ContributionItem) {
				ContributionItem contribItem = (ContributionItem) data;
				R4EUIPlugin.Ftracer.traceInfo("Menu command selected: " + contribItem.getId());
				IContributionItem[] items = contribItem.getParent().getItems();
				if (contribItem.getId().endsWith(R4EUIConstants.NEW_CHILD_ELEMENT_COMMAND)) {
 					R4EUIPlugin.Ftracer.traceInfo("Menu command selected  Group, return the root");
 					//Add element to the root of the tree
 					return R4EUIModelController.getRootElement();
 				}
 
 			}
 		}
		R4EUIPlugin.Ftracer.traceInfo("getParentElement(): look for selection");
 		final IStructuredSelection selection = (IStructuredSelection) R4EUIModelController.getNavigatorView()
 				.getTreeViewer()
 				.getSelection();
 		if (!selection.isEmpty()) {
 			element = (IR4EUIModelElement) selection.getFirstElement();
 		} else {
 			//Add element to the root of the tree
 			element = R4EUIModelController.getRootElement();
 		}
		R4EUIPlugin.Ftracer.traceInfo("getParentElement(): return element: " + element.getName());

 		return element;
 	}
 }
