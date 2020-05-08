 /*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
 package org.jboss.tools.windup.ui.internal.commands;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.jboss.tools.windup.core.WindupService;
 import org.jboss.tools.windup.ui.internal.Messages;
 import org.jboss.tools.windup.ui.internal.Utils;
 
 /**
  * <p>
  * Handles generating the windup report for a project.
  * </p>
  */
 public class GenerateWindupReportHandler extends AbstractHandler {
 
 	@Override
 	public Object execute(final ExecutionEvent event) throws ExecutionException {
 		//create a job to generate the report in
 		Job job = new Job(Messages.generate_windup_report) {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
				ISelection selection = HandlerUtil.getCurrentSelection(event);
 				IResource selectedResource = Utils.getSelectedResource(selection);
 				
				return WindupService.getDefault().generateReport(selectedResource, monitor);
 			}
 		};
 		job.setUser(true);
 		job.schedule();
 		
 		return null;
 	}
 }
