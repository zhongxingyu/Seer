 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.ui.controller;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.emfstore.internal.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.internal.client.ui.views.historybrowserview.HistoryBrowserView;
 import org.eclipse.emf.emfstore.internal.server.model.impl.api.ESHistoryInfoImpl;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.HistoryInfo;
import org.eclipse.emf.emfstore.internal.server.model.versioning.TagVersionSpec;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.emf.emfstore.server.model.ESHistoryInfo;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ElementListSelectionDialog;
 
 /**
  * UI controller for removing version tags.
  * 
  * @author emueller
  * 
  */
 // TODO: re-package exception for more sensible error messages
 public class UIRemoveTagController extends AbstractEMFStoreUIController<Void> {
 
 	private final ESHistoryInfoImpl historyInfo;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell
 	 *            the parent {@link Shell}
 	 * @param historyInfo
 	 *            the {@link HistoryInfo} from which to remove the tag
 	 */
 	public UIRemoveTagController(Shell shell, ESHistoryInfo historyInfo) {
 		super(shell);
 		this.historyInfo = (ESHistoryInfoImpl) historyInfo;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.ui.common.MonitoredEMFStoreAction#doRun(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public Void doRun(IProgressMonitor monitor) throws ESException {
 
 		// TODO: controller currently does not work if the active workbench window is not
 		// the history view
 		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
 
 		if (activePage == null || !(activePage.getActivePart() instanceof HistoryBrowserView)) {
 			return null;
 		}
 
 		final HistoryBrowserView historyBrowserView = (HistoryBrowserView) activePage.getActivePart();
 
 		final LabelProvider tagLabelProvider = new LabelProvider() {
 			@Override
 			public String getText(Object element) {
				return ((TagVersionSpec) element).getName();
 			}
 		};
 
 		ElementListSelectionDialog dlg = new ElementListSelectionDialog(PlatformUI.getWorkbench()
 			.getActiveWorkbenchWindow().getShell(), tagLabelProvider);
 		dlg.setElements(historyInfo.getTagSpecs().toArray());
 		dlg.setTitle("Tag selection");
 		dlg.setBlockOnOpen(true);
 		dlg.setMultipleSelection(true);
 		int ret = dlg.open();
 
 		if (ret != Window.OK) {
 			return null;
 		}
 
 		ProjectSpace projectSpace = historyBrowserView.getProjectSpace();
 		Object[] result = dlg.getResult();
 
 		for (Object o : result) {
			if (o instanceof TagVersionSpec) {
				TagVersionSpec tag = (TagVersionSpec) o;
 				try {
 					// TODO: monitor
					projectSpace.removeTag(historyInfo.toInternalAPI().getPrimarySpec(), tag);
 				} catch (ESException e) {
 					MessageDialog.openError(getShell(), "Remove tag failed", "Remove tag failed: " + e.getMessage());
 				}
 			}
 		}
 
 		historyBrowserView.refresh();
 
 		return null;
 	}
 }
