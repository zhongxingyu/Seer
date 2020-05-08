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
  * This class implements the review navigator view toolbar command used 
  * to refresh (i.e. sychronize with serialization model) the review/review group data 
  * 
  * Contributors:
  *   Jacques Bouthillier - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.ui.internal.commands;
 
 import java.io.File;
 import java.util.Iterator;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewGroup;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.progress.UIJob;
 
 /**
  * @author lmcbout
  * @version $Revision: 1.0 $
  */
 public class ReportElementHandler extends AbstractHandler {
 
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
 
 		if (R4EUIPlugin.isUserReportAvailable()) {
 			final UIJob job = new UIJob("Reporting Element...") { //$NON-NLS-1$
 				@Override
 				public IStatus runInUIThread(IProgressMonitor monitor) {
 					final ISelection selection = HandlerUtil.getCurrentSelection(event);
 					if (selection instanceof IStructuredSelection) {
 						if (!selection.isEmpty()) {
 							IR4EUIModelElement element = null;
 							R4EUIReviewGroup group = null;
 							final IStructuredSelection structSelection = (IStructuredSelection) selection;
							final int size = structSelection.size();
							final File[] listSelectReviews = new File[size];
 							final Iterator<?> iter = structSelection.iterator();
 
 							int i = 0;
 							while (iter.hasNext()) {
 								element = (IR4EUIModelElement) iter.next();
 								if (element instanceof R4EUIReviewBasic) {
 									R4EUIReviewBasic extentElement = (R4EUIReviewBasic) element;
 									R4EUIPlugin.Ftracer.traceInfo("Review name element " //$NON-NLS-1$
 											+ extentElement.getReview().getName());
									listSelectReviews[i] = new File(extentElement.getReview().getName());
									i++;
 
 								} else if (element instanceof R4EUIReviewGroup) {
 									group = (R4EUIReviewGroup) element;
 									R4EUIPlugin.Ftracer.traceInfo("Group file: " + group.getReviewGroup().getFolder()); //$NON-NLS-1$
 								}
 							}
 
 							final org.eclipse.mylyn.reviews.r4e.report.impl.IR4EReport reportGen = org.eclipse.mylyn.reviews.r4e.report.impl.R4EReportFactory.getInstance();
 
							reportGen.setReviewListSelection(listSelectReviews);
 //							reportGen.setReportType(reportGen.SINGLE_REPORT_TYPE);
 
 							final String groupFile = groupReview((IR4EUIModelElement) structSelection.getFirstElement());
 							R4EUIPlugin.Ftracer.traceInfo("Info: " + "Group file: " + groupFile); //$NON-NLS-1$//$NON-NLS-2$
 							reportGen.handleReportGeneration(groupFile);
 
 						}
 					}
 					return Status.OK_STATUS;
 				}
 			};
 			job.setUser(true);
 			job.schedule();
 		}
 		return null;
 	}
 
 	/**
 	 * Method refreshReview.
 	 * 
 	 * @param element
 	 *            IR4EUIModelElement
 	 * @return String
 	 */
 	private String groupReview(IR4EUIModelElement element) {
 		IR4EUIModelElement searchElement = element;
 		R4EUIReviewGroup group = null;
 		if (null != element) {
 			searchElement = element;
 			while (null != searchElement) {
 				if (searchElement instanceof R4EUIReviewBasic) {
 					searchElement = searchElement.getParent();
 					R4EUIPlugin.Ftracer.traceInfo("Info: " + "Review element: " + searchElement.getName()); //$NON-NLS-1$ //$NON-NLS-2$
 				} else if (searchElement instanceof R4EUIReviewGroup) {
 					group = (R4EUIReviewGroup) searchElement;
 					R4EUIPlugin.Ftracer.traceInfo("Info: " + "Group file: " + group.getGroupFile()); //$NON-NLS-1$//$NON-NLS-2$
 					break;
 				} else {
 					//Should not reach this point unless we did not find the group
 					R4EUIPlugin.Ftracer.traceInfo("Info: " + "Group search file: " + searchElement.getName()); //$NON-NLS-1$ //$NON-NLS-2$
 					searchElement = searchElement.getParent();
 				}
 			}
 		}
 
 		if (null != group) {
 			return group.getGroupFile();
 		}
 		return ""; //$NON-NLS-1$
 
 	}
 }
