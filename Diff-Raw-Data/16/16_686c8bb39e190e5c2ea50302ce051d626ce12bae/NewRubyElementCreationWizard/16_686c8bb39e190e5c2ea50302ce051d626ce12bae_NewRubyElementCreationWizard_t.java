 package org.rubypeople.rdt.internal.ui.wizards;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.amanzi.integrator.awe.AWEProjectManager;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.ui.IWorkbench;
 import org.rubypeople.rdt.core.ILoadpathEntry;
 import org.rubypeople.rdt.core.IRubyProject;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.internal.ui.wizards.buildpaths.BuildPathsBlock;
 import org.rubypeople.rdt.internal.ui.wizards.buildpaths.CPListElement;
 import org.rubypeople.rdt.launching.RubyRuntime;
 import org.rubypeople.rdt.ui.PreferenceConstants;
 
 public abstract class NewRubyElementCreationWizard extends NewElementWizard {
 	
 	private final String DEFAULT_RUBY_PROJECT_NAME = "AWEScript";
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
 	 */
 	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
 		Object selectedObject = null;
 		IStructuredSelection newSelection = currentSelection;
 		
 		
		selectedObject = currentSelection.getFirstElement();
 		
		if (AWEProjectManager.getType(selectedObject) != AWEProjectManager.RUBY_PROJECT) {
		    newSelection = createRubyProject(selectedObject);
		}
			
 		super.init(workbench, newSelection);
 	}
 	
 	/**
 	 * Creates RubyProject from selection
 	 * 
 	 * @param selectedObject selected object
 	 * @return changed selection
 	 */
 	
 	private IStructuredSelection createRubyProject(Object selectedObject) {
 		StructuredSelection newSelection = null;	
 		
 		String rubyProjectName = AWEProjectManager.getDefaultRubyProjectName(selectedObject);
 		String aweProjectName = AWEProjectManager.getAWEProjectName(selectedObject);
 		
 		try {
 			IRubyProject rubyProject = configureRubyProject(rubyProjectName, aweProjectName);
 			newSelection = new StructuredSelection(rubyProject);
 		}
 		catch (CoreException e) {
 			//TODO: handle this exception
 			e.printStackTrace();
 		}
 		
 		return newSelection;
 	}
 	
 	/**
 	 * Add configuration to create RubyProject
 	 * 
 	 * @param rubyProjectName name of Ruby Project
 	 * @param aweProjectName name of Awe Project
 	 * @return configured RubyProject
 	 * @throws CoreException
 	 */
 	
 	private IRubyProject configureRubyProject(String rubyProjectName, String aweProjectName) throws CoreException {
 		if (rubyProjectName == null) {
 			rubyProjectName = DEFAULT_RUBY_PROJECT_NAME;
 		}
 		
 		rubyProjectName = aweProjectName + "." + rubyProjectName;
 		
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(rubyProjectName);		
 		
 		if (project.exists()) {
 			return RubyCore.create(project, aweProjectName);
 		}
 		URI rootLocation = null;
 		try {
 			rootLocation= ResourcesPlugin.getWorkspace().getRoot().getLocationURI();
 			URI realLocation= new URI(rootLocation.getScheme(), null,
 			Path.fromPortableString(rootLocation.getPath()).append(rubyProjectName).toString(), null);
 		} catch (URISyntaxException e) {
 			Assert.isTrue(false, "Can't happen"); //$NON-NLS-1$		
 		}
 		
 		BuildPathsBlock.createProject(project, null, null);
 		IRubyProject rubyProject = RubyCore.create(project, aweProjectName);
 		List<ILoadpathEntry> cpEntries= new ArrayList<ILoadpathEntry>();
 		IPath projectPath= project.getFullPath();
 		cpEntries.add(RubyCore.newSourceEntry(projectPath));
 		cpEntries.addAll(Arrays.asList(getDefaultLoadpathEntry()));
 		
 		List<CPListElement> newClassPath= new ArrayList<CPListElement>();
 		for (ILoadpathEntry entry : cpEntries) {
 			newClassPath.add(CPListElement.createFromExisting(entry, rubyProject));
 		}			
 		
 		BuildPathsBlock.addRubyNature(project, null);
 		BuildPathsBlock.flush(newClassPath, rubyProject, null);
 		
 		return rubyProject;
 	}
 	
 	/**
 	 * Returns loadpath entries
 	 * 
 	 * @return
 	 */
 	
 	private ILoadpathEntry[] getDefaultLoadpathEntry() {
 		ILoadpathEntry[] defaultJRELibrary = PreferenceConstants.getDefaultRubyVMLibrary();
 		String compliance = null;
 		IPath jreContainerPath = new Path(RubyRuntime.RUBY_CONTAINER);
 		if (compliance == null || defaultJRELibrary.length > 1 || !jreContainerPath.isPrefixOf(defaultJRELibrary[0].getPath())) {
 			// use default
 			return defaultJRELibrary;
 		}		
 		return defaultJRELibrary;
 	}
 }
