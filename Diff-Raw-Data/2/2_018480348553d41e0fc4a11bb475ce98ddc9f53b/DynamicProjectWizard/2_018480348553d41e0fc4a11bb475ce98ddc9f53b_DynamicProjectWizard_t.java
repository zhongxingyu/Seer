 /*
   GRANITE DATA SERVICES
   Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.
 
   This file is part of Granite Data Services.
 
   Granite Data Services is free software; you can redistribute it and/or modify
   it under the terms of the GNU Library General Public License as published by
   the Free Software Foundation; either version 2 of the License, or (at your
   option) any later version.
 
   Granite Data Services is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
   for more details.
 
   You should have received a copy of the GNU Library General Public License
   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.granite.wizard;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 
 /**
  * @author Franck WOLFF
  */
 public class DynamicProjectWizard extends Wizard implements IExecutableExtension, INewWizard {
 
 	private IWorkbench workbench;
 	private IStructuredSelection selection;
 	private IConfigurationElement configurationElement;
 	private DynamicProjectWizardPageOne pageOne;
 
 	public DynamicProjectWizard() {
		setWindowTitle("New GraniteDS Project");
 	}
 
 	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
 		configurationElement = cfig;
 	}
 	
 	public IConfigurationElement getConfigurationElement() {
 		return configurationElement;
 	}
 	
 	public IWorkbench getWorkbench() {
 		return workbench;
 	}
 
 	public IStructuredSelection getSelection() {
 		return selection;
 	}
 
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.workbench = workbench;
 		this.selection = selection;
 	}
 
 	@Override
 	public void addPages() {
 		super.addPages();
 		
 		pageOne = new DynamicProjectWizardPageOne(configurationElement);
 		addPage(pageOne);
 	}
 
 	@Override
 	public void createPageControls(Composite pageContainer) {
 		super.createPageControls(pageContainer);
 		
 		try {
 			setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(
 				new Image(pageContainer.getDisplay(), getClass().getClassLoader().getResourceAsStream("icons/gdswiz.gif"))
 			));
 		}
 		catch (Exception e) {
 			// ignore...
 		}
 	}
 
 	@Override
 	public boolean canFinish() {
 		return pageOne != null && pageOne.canFinish();
 	}
 
 	public boolean performFinish() {
 		return pageOne.performFinish();
 	}
 
 	public boolean performCancel() {
 		return pageOne.performCancel();
 	}
 }
