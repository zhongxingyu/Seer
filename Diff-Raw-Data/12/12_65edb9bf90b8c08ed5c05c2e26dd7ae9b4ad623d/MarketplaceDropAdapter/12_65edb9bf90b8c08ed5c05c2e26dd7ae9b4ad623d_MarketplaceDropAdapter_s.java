 /*******************************************************************************
  * Copyright (c) 2011 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui.wizards;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.epp.internal.mpc.ui.wizards.MarketplaceUrlHandler.SolutionInstallationInfo;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.dnd.URLTransfer;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IStartup;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.progress.UIJob;
 
 /**
  * @author Benjamin Muskalla
  */
 public class MarketplaceDropAdapter implements IStartup {
 
 	public void earlyStartup() {
 		UIJob registerJob = new UIJob(Display.getDefault(), Messages.MarketplaceDropAdapter_0) {
 			{
 				setPriority(Job.DECORATE);
 			}
 
 			@Override
 			public IStatus runInUIThread(IProgressMonitor monitor) {
 				IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench()
				.getWorkbenchWindows();
 				for (IWorkbenchWindow window : workbenchWindows) {
 					Shell shell = window.getShell();
 					installDropTarget(shell);
 				}
 				return Status.OK_STATUS;
 			}
 
 		};
 		registerJob.schedule();
 	}
 
 	public void installDropTarget(final Shell shell) {
 		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
 		DropTarget target = new DropTarget(shell, operations);
 		target.setTransfer(new Transfer[] { URLTransfer.getInstance() });
 		target.addDropListener(new DropTargetAdapter() {
 			@Override
 			public void dragEnter(DropTargetEvent e) {
 				if (e.detail == DND.DROP_NONE) {
 					e.detail = DND.DROP_LINK;
 				}
 			}
 
 			@Override
 			public void dragOperationChanged(DropTargetEvent e) {
 				if (e.detail == DND.DROP_NONE) {
 					e.detail = DND.DROP_LINK;
 				}
 			}
 
 			@Override
 			public void drop(DropTargetEvent event) {
 				if (event.data == null) {
 					event.detail = DND.DROP_NONE;
 					return;
 				}
				String url = getUrlFromEvent(event);
 				if (MarketplaceUrlHandler.isPotentialSolution(url)) {
					proceedInstallation(url);
 				}
 			}
 
 			// Depending on the form the link and browser/os,
 			// we get the url twice in the data separated by new lines
 			private String getUrlFromEvent(DropTargetEvent event) {
 				String eventData = (String) event.data;
 				String[] dataLines = eventData.split(System.getProperty("line.separator")); //$NON-NLS-1$
 				String url = dataLines[0];
 				return url;
 			}
 		});
 	}
 
 	protected void proceedInstallation(String url) {
 		SolutionInstallationInfo info = MarketplaceUrlHandler.createSolutionInstallInfo(url);
 		if (info != null) {
 			MarketplaceUrlHandler.triggerInstall(info);
 		}
 	}
 
 }
