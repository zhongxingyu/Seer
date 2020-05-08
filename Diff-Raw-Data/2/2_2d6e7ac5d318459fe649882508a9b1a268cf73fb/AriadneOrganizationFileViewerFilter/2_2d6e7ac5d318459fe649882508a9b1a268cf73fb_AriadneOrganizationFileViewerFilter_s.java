 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package fr.obeo.ariadne.ide.connector.ui.internal.actions.explore;
 
 import fr.obeo.ariadne.ide.connector.ui.internal.AriadneUIPlugin;
 import fr.obeo.ariadne.model.organization.presentation.OrganizationModelWizard;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 
 /**
  * This viewer filter will be used to make sure that only IFile useable by the Organization model editor or
  * container that possess one of those organization file can be displayed in a viewer.
  * 
  * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
  * @since 1.0
  */
 public class AriadneOrganizationFileViewerFilter extends ViewerFilter {
 
 	/**
 	 * Indicates if the analyzed IContainer has an organization file within.
 	 */
 	private boolean isContainerWithOrganizationFile;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object,
 	 *      java.lang.Object)
 	 */
 	@Override
 	public boolean select(Viewer viewer, Object parentElement, Object element) {
 		boolean isOrganizationFile = element instanceof IFile
 				&& OrganizationModelWizard.FILE_EXTENSIONS.contains(((IFile)element).getFileExtension());
		if (element instanceof IContainer) {
 			// Reset the variable
 			this.isContainerWithOrganizationFile = false;
 
 			IContainer iContainer = (IContainer)element;
 			this.navigateElement(iContainer);
 		}
 
 		return isOrganizationFile || isContainerWithOrganizationFile;
 	}
 
 	/**
 	 * Sets the variable isContainerViewOrganizationFile to <code>true</code> if the iContainer with an
 	 * organization file within, <code>false</code> otherwise.
 	 * 
 	 * @param iContainer
 	 *            The iContainer
 	 */
 	private void navigateElement(IContainer iContainer) {
 		try {
 			iContainer.accept(new IResourceVisitor() {
 				@Override
 				public boolean visit(IResource resource) throws CoreException {
 					if (resource instanceof IFile
 							&& OrganizationModelWizard.FILE_EXTENSIONS.contains(((IFile)resource)
 									.getFileExtension())) {
 						isContainerWithOrganizationFile = true;
 					}
 					return true;
 				}
 			});
 		} catch (CoreException e) {
 			AriadneUIPlugin.log(e, true);
 		}
 	}
 }
