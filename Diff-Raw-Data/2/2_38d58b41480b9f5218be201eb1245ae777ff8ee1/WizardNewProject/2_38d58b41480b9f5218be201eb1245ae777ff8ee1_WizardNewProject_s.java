 /*
  Kick Assembler plugin - An Eclipse plugin for convenient Kick Assembling
  Copyright (c) 2012 - P-a Backstrom <pa.backstrom@gmail.com>
 
  Based on ASMPlugin - http://sourceforge.net/projects/asmplugin/
  Copyright (c) 2006 - Andy Reek, D. Mitte
 
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */ 
 package org.lyllo.kickassplugin.ui;
 
 import java.net.URI;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.filesystem.URIUtil;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
 import org.lyllo.kickassplugin.Activator;
 import org.lyllo.kickassplugin.Constants;
 import org.lyllo.kickassplugin.Messages;
 
 
 /**
  * The new ASM project wizard.
  * 
  * @author Andy Reek
  * @since 25.11.2005
  */
 public class WizardNewProject extends Wizard implements INewWizard {
 
 	/**
 	 * The first page of the wizard.
 	 */
 	private WizardNewProjectCreationPage page1;
 
 	/**
 	 * The constructor.
 	 */
 	public WizardNewProject() {
 		super();
 		setWindowTitle(Messages.WIZARD_NEW_PROJECT_TITLE);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void addPages() {
 		super.addPages();
 
 		page1 = new WizardNewProjectCreationPage(Messages.WIZARD_NEW_PROJECT_PAGE1_TITLE);
 		page1.setTitle(Messages.WIZARD_NEW_PROJECT_PAGE1_TITLE);
 		page1.setImageDescriptor(Constants.WIZARD_NEW);
 		page1.setDescription(Messages.WIZARD_NEW_PROJECT_DESCRIPTION);
 		addPage(page1);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean performFinish() {
 		try {
 			IPath locationPath = page1.getLocationPath();
 			IProject project = page1.getProjectHandle();
 
 			if (!project.exists()) {
 				IProjectDescription desc= project.getWorkspace().newProjectDescription(project.getName());
 				URI locationURI =  locationPath != null ? URIUtil.toURI(locationPath) : null;
 				if (locationURI != null && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(locationURI)) {
 					locationURI= null;
 				}
 				desc.setLocationURI(locationURI);
 				project.create(desc, new NullProgressMonitor());
 			}
 			if (!project.isOpen()) {
 				project.open(null);
 			}
 			
 			IProjectDescription description = project.getDescription();
			description.setLocation(locationPath);

 			String[] natures = description.getNatureIds();
 			String[] newNatures = new String[natures.length + 1];
 			System.arraycopy(natures, 0, newNatures, 0, natures.length);
 			newNatures[natures.length] = Constants.NATURE_ID;
 			description.setNatureIds(newNatures);
 
 			project.setDescription(description, null);
 		
 			createOrRefreshFolder(project, "src");
 			createOrRefreshFolder(project, Constants.DEFAULT_BUILD_DIRECTORY);
 
 		} catch (CoreException e) {
 			Activator.getDefault().getLog().log(e.getStatus());
 		}
 		return true;
 	}
 
 	private void createOrRefreshFolder(IProject project, String dir) throws CoreException {
 		IFolder folder = project.getFolder(dir);
 		if (!folder.exists()){
 			folder.create(IResource.NONE, true, null);
 		} else {
 			folder.refreshLocal(IResource.DEPTH_INFINITE, null);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 	}
 }
