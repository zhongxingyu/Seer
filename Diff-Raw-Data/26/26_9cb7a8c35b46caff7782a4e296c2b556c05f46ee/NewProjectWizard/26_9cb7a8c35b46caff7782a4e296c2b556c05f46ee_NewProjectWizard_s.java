 // Copyright (c) 2011, David J. Pearce (djp@ecs.vuw.ac.nz)
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //    * Redistributions of source code must retain the above copyright
 //      notice, this list of conditions and the following disclaimer.
 //    * Redistributions in binary form must reproduce the above copyright
 //      notice, this list of conditions and the following disclaimer in the
 //      documentation and/or other materials provided with the distribution.
 //    * Neither the name of the <organization> nor the
 //      names of its contributors may be used to endorse or promote products
 //      derived from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 // DISCLAIMED. IN NO EVENT SHALL DAVID J. PEARCE BE LIABLE FOR ANY
 // DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 // ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 // SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package wyclipse.wizards;
 
 import java.util.Arrays;
 
 import org.eclipse.core.runtime.*;
 
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.CoreException;
 
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 // Should not extend this, but seems that I can!
 import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
 import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
 import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
 import org.eclipse.ui.IPerspectiveRegistry;
 import org.eclipse.ui.activities.WorkbenchActivityHelper;
 import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
 
 import wyclipse.Activator;
 import wyclipse.WhileyCore;
 
 /**
  * This Wizard is responsible for constructing a new Whiley project. In fact, it
  * basically reuses everything from the existing JDT new project wizard, with
  * the minor addition of appending the Whiley Nature and Builder onto the
  * project.
  * 
  * @author David J. Pearce
  * 
  */
 public class NewProjectWizard extends NewElementWizard implements IExecutableExtension {
 	private NewJavaProjectWizardPageOne page1;
 	private NewJavaProjectWizardPageTwo page2;
 	private IConfigurationElement configElement;	
 	
 	/**
 	 * Constructor for WhileyModuleNewWizard.
 	 */
 	public NewProjectWizard() {
 		super();
 		setWindowTitle("New Whiley Project");
 	}
 	
 	/**
 	 * Add pages to the wizard.
 	 */
 	public void addPages() {
 		page1 = new NewJavaProjectWizardPageOne();
 		page2 = new NewJavaProjectWizardPageTwo(page1);
 		page1.setTitle("Create a Whiley Project");
 		page2.setTitle("Settings");
 		page2.setDescription("Define the build settings");
 		addPage(page1);
 		addPage(page2);
 	}
 	
 	public IJavaElement getCreatedElement() {
 		return page2.getJavaProject();
 	}
 
 	public void finishPage(IProgressMonitor monitor)
 			throws InterruptedException, CoreException {
 		page2.performFinish(monitor);
 	}
 	
 	public void setInitializationData(IConfigurationElement config,
 			String property, Object data) {
 		configElement = config;
 	}
 	
 	/**
 	 * This method is called when 'Finish' button is pressed in the wizard.
 	 */
 	@Override
 	public boolean performFinish() {
 		boolean result = super.performFinish();
 		if(result) {
 			// Project construction succeeded. Now, we need to add the Whiley
 			// nature to the new project, in order that we can build Whiley
 			// files!
 			try {
 				IJavaProject javaProject = page2.getJavaProject(); 
 				IProject project = javaProject.getProject();
 				IProjectDescription desc = project.getDescription();
 				
 				// First, append Whiley Nature onto list of natures
 				String[] oldNatures = desc.getNatureIds();
 				String[] newNatures = Arrays.copyOf(oldNatures, oldNatures.length+1);
 				newNatures[oldNatures.length] = Activator.WYCLIPSE_NATURE_ID;							
 				desc.setNatureIds(newNatures);
 
 				// Second, prepend Whiley Builder onto list of builders
 				ICommand buildCommand = desc.newCommand();				
 				buildCommand.setBuilderName(Activator.WYCLIPSE_BUILDER_ID);
 				
 //				ICommand[] oldBuilders = desc.getBuildSpec();
//				ICommand[] newBuilders = new ICommand[oldBuilders.length+1];
//				System.arraycopy(oldBuilders, 0, newBuilders, 1, oldBuilders.length);				
//				newBuilders[0] = buildCommand;
 //				ICommand[] newBuilders = Arrays.copyOf(oldBuilders, oldBuilders.length+1);	
 //				newBuilders[oldBuilders.length] = buildCommand;
 				ICommand[] newBuilders = new ICommand[1];
 				newBuilders[0] = buildCommand;
 				desc.setBuildSpec(newBuilders);		
 				
 				// Third, add the Whiley class path container
 				WhileyCore.addWhileyClasspathContainer(javaProject);
 				
 				// done
 				project.setDescription(desc,null);
 				BasicNewProjectResourceWizard.updatePerspective(configElement);
 				
 			} catch(CoreException e) {
 				return false; // I guess??
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * This method is called when 'Cancel' button is pressed in the wizard. In
 	 * this case, we need to undo what has been done.
 	 */	
 	@Override
     public boolean performCancel() {
         page2.performCancel();
         return super.performCancel();
     }
 
 	
 }
