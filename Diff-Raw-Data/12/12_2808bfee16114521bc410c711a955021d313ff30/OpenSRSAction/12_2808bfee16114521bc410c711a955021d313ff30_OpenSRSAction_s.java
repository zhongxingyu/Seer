 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.ac.diamond.sda.navigator.actions;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.ide.IDE;
 
 import uk.ac.diamond.scisoft.analysis.rcp.navigator.srs.SRSTreeData;
 import uk.ac.diamond.sda.intro.navigator.NavigatorRCPActivator;
 
 public class OpenSRSAction extends Action {
 
 	private IWorkbenchPage page;
 	private SRSTreeData data;
 	private ISelectionProvider provider;
 
 	/**
 	 * Construct the OpenSRSAction with the given page.
 	 * 
 	 * @param p
 	 *            The page to use as context to open the editor.
 	 * @param selectionProvider
 	 *            The selection provider
 	 */
 	public OpenSRSAction(IWorkbenchPage p, ISelectionProvider selectionProvider) {
 		setText("Open SRS Editor"); //$NON-NLS-1$
 		page = p;
 		provider = selectionProvider;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.action.Action#isEnabled()
 	 */
 	@Override
 	public boolean isEnabled() {
 		ISelection selection = provider.getSelection();
 		if (!selection.isEmpty()) {
 			IStructuredSelection sSelection = (IStructuredSelection) selection;
 			if (sSelection.size() == 1 && sSelection.getFirstElement() instanceof SRSTreeData) {
 				data = ((SRSTreeData) sSelection.getFirstElement());
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.action.Action#run()
 	 */
 	@Override
 	public void run() {
 		try {
 			if (isEnabled()) {
 				IFile srsFile = data.getFile();
 				IDE.openEditor(page, srsFile);

 			}
 		} catch (PartInitException e) {
 			NavigatorRCPActivator.logError(0, "Could not open SRS Editor!", e); //$NON-NLS-1$
 			MessageDialog.openError(Display.getDefault().getActiveShell(), "Error Opening SRS Editor", //$NON-NLS-1$
 					"Could not open SRS Editor!"); //$NON-NLS-1$
 		}
 	}
 }
