 package org.amanzi.integrator.awe;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import net.refractions.udig.project.IRubyProject;
 import net.refractions.udig.project.internal.Project;
 import net.refractions.udig.project.internal.ProjectElement;
 import net.refractions.udig.project.internal.ProjectPlugin;
 import net.refractions.udig.project.internal.RubyFile;
 import net.refractions.udig.project.internal.RubyProjectElement;
 import net.refractions.udig.project.internal.RubyProject;
 import net.refractions.udig.project.internal.Spreadsheet;
 import net.refractions.udig.project.internal.impl.ProjectFactoryImpl;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.emf.ecore.resource.Resource;
 
 /**
  * Class for integration functionality of AWE project to RDT plugin
  * 
  * Note: 
  * It's only initial implementation of integration. 
  * 
  * @author Lagutko_N
  *
  */
 
 public class AWEProjectManager {
 	
 	/**
 	 * Constants for type of Object
 	 */
 	
 	/*
 	 * Obejct is null
 	 */
 	public static final int NO_OBJECT = -1;
 	
 	/*
 	 * Not supported type
 	 */
     public static final int UNKNOWN = 0;
     
     /*
      * AWE Project type
      */
     public static final int AWE_PROJECT = 1;
     
     /*
      * Ruby Project Type
      */
     public static final int RUBY_PROJECT = 2;    
 	
 	/*
 	 * Constant for Qualified name of PersistencePreferences that will contain
 	 * name of AWE Project
 	 */
 	
 	private static final String RUBY_PROJECT_DEFAULT_NAME = "AWEScript";
 	public static final QualifiedName AWE_PROJECT_NAME = new QualifiedName("awe_project", "name");
 	
 	/**
 	 * Returns all Ruby projects that are referenced by AWE projects
 	 * 
 	 * @return array of RubyProjects
 	 */
 	
 	public static IProject[] getAllRubyProjects() {
 		ArrayList<IProject> rubyProjects = new ArrayList<IProject>();
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		
 		for (Project aweProject : ProjectPlugin.getPlugin().getProjectRegistry().getProjects()) {
 			for (RubyProject rubyProject : aweProject.getElements(RubyProject.class)) {
 				IProject resourceProject = root.getProject(rubyProject.getName());
 				try {					
 					resourceProject.setPersistentProperty(AWE_PROJECT_NAME, aweProject.getName());
 				}
 				catch (CoreException e) {
 					//TODO: handle this exception
 				}
			
				rubyProjects.add(resourceProject);		
 			}
 		}
 		
 		return rubyProjects.toArray(new IProject[]{});
 	}
 	
 	/**
 	 * Returns all AWE projects
 	 * 
 	 * @return array of AWE Projects
 	 */
 	
 	public static IProject[] getAWEProjects() {
 		ArrayList<IProject> aweProjects = new ArrayList<IProject>();
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		
 		for (Project project : ProjectPlugin.getPlugin().getProjectRegistry().getProjects()) {
 			IProject aweProject = root.getProject(project.getName());
 			aweProjects.add(aweProject);
 		}
 		
 		return aweProjects.toArray(new IProject[]{});
 	}
 	
 	/**
 	 * Utility function that search for AWE Project by it's name
 	 * 
 	 * @param name name of AWE Project
 	 * @return AWE project
 	 */
 	
 	private static Project findProject(String name) {
 		for (Project project : ProjectPlugin.getPlugin().getProjectRegistry().getProjects()) {
 			if (project.getName().equals(name)) {
 				return project;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Creates RubyScript for RubyProject if RubyProject doesn't contain script with this name
 	 * 
 	 * This method calls not only when RubyScript creates from RDT functionality, but also 
 	 * when we create a project structure. We must check if project structure already contains
 	 * this Script because RDT plug-in creates project structure more than one time. 
 	 * 
 	 * @param rubyProject 
 	 * @param scriptName
 	 * @param scriptResource
 	 */
 	
 	private static void createScriptIfNotExist(RubyProject rubyProject, String scriptName, IResource scriptResource) {
 		boolean exist = false;
 		
 		RubyProjectElement element = findRubyScript(rubyProject, scriptName);
 		if (element != null) {
 			exist = true;
 		}
 		
 		if (!exist) {
 			RubyFile rubyFile = ProjectFactoryImpl.eINSTANCE.createRubyFile();
 			rubyFile.setName(scriptName);
 			rubyFile.setResource(scriptResource);
 			rubyProject.getRubyElementsInternal().add(rubyFile);
 			rubyFile.setRubyProjectInternal(rubyProject);
 		}
 	}
 	
 	/**
 	 * Creates RubyProject for AWEProject if AWEProject doesn't contain project with this name
 	 * 
 	 * This method calls not only when RubyProject creates from RDT functionality, but also 
 	 * when we create a project structure. We must check if project structure already contains
 	 * this Project because RDT plug-in creates project structure more than one time. 
 	 * 
 	 * @param aweProject 
 	 * @param rubyProjectName
 	 */
 	
 	private static void createProjectIfNotExist(Project aweProject, String rubyProjectName) {
 		boolean exist = false;
 		
 		for (RubyProject rubyProject : aweProject.getElements(RubyProject.class)) {
 			if (rubyProject.getName().equals(rubyProjectName)) {
 				exist = true;
 			}
 		}
 		
 		if (!exist) {
 			RubyProject ruby = ProjectFactoryImpl.eINSTANCE.createRubyProject();
 			ruby.setName(rubyProjectName);
 			ruby.setProjectInternal(aweProject);			
 		}
 	}
 	
 	/**
 	 * Utility function that get name of AWE Project from RubyProject 
 	 * 
 	 * @param rubyProject resource of RubyProject
 	 * @return name of AWE project
 	 */
 	
 	private static String getAWEprojectNameFromResource(IProject rubyProject) {
 		String name = null;
 		try {
 			name = rubyProject.getPersistentProperty(AWE_PROJECT_NAME);
 		}
 		catch (CoreException e) {
 			
 		}
 		return name;
 	}
 	
 	/**
 	 * Creates RubyProject
 	 * 
 	 * @param rubyProject RDT Ruby Project
 	 */
 	
 	public static void createRubyProject(IProject rubyProject) {
 		String name = getAWEprojectNameFromResource(rubyProject);
 		if (name != null) {
 			Project project = findProject(name);
 			if (project != null) {
 				createProjectIfNotExist(project, rubyProject.getName());
 			}
 		}
 	}
 	
 	/**
 	 * Utility function that deletes ProjectElement from AWE Project
 	 * 
 	 * This functionality copied from uDIG plugins.
 	 * 
 	 * @param element AWE ProjectElement to delete
 	 */
 	
 	private static void deleteElement(ProjectElement element) {
 		Project projectInternal = element.getProjectInternal();
         projectInternal.getElementsInternal().remove(element);
                     
         deleteResource(element);   
 	}
 	
 	/**
 	 * Utility function that deletes RubyProjectElement from AWE Project
 	 * 
 	 * This functionality copied from uDIG plugins.
 	 * 
 	 * @param element AWE RubyProjectElement to delete
 	 */
 	
 	private static void deleteElement(RubyProjectElement element) {
 		RubyProject projectInternal = element.getRubyProjectInternal();
 		projectInternal.getRubyElementsInternal().remove(element);
                     
         deleteResource(element);   
 	}
 	
 	/**
 	 * Utility function that delete EMF resource of AWE ProjectElement
 	 * 
 	 * @param element AWE ProjectElement to delete
 	 */
 	
 	private static void deleteResource(ProjectElement element) {
 		Resource resource = element.eResource();
 		if (resource != null) {
             resource.getContents().remove(element);
             resource.unload();
         }   
 	}
 	
 	/**
 	 * Delete RubyProject
 	 * 
 	 * @param rubyProject RDT RubyProject to delete
 	 */
 	
 	public static void deleteRubyProject(IProject rubyProject) {
 		String name = getAWEprojectNameFromResource(rubyProject);
 		
 		Project project = findProject(name);	
 		RubyProject ruby = findRubyProject(project, rubyProject.getName());
 		if (ruby != null) {
 			deleteElement(ruby);
 		}
 	}
 	
 	/**
 	 * Search for RubyProject inside an AWE Project by Name
 	 * 
 	 * @param aweProject AWE Project
 	 * @param rubyProjectName name of RubyProject
 	 * @return AWE RubyProject or null if there are no such projects
 	 */
 	
 	private static RubyProject findRubyProject(Project aweProject, String rubyProjectName) {
 		for (RubyProject ruby : aweProject.getElements(RubyProject.class)) {
 			if (ruby.getName().equals(rubyProjectName)) {
 				return ruby;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Search for RubyScript inside a AWE RubyProject by name
 	 * 
 	 * @param rubyProject AWE Ruby Project
 	 * @param rubyScriptName name of RubyScript
 	 * @return AWE RubyScript or null if there are no such scripts
 	 */
 	
 	private static RubyFile findRubyScript(RubyProject rubyProject, String rubyScriptName) {
 		for (RubyProjectElement rubyElement : rubyProject.getRubyElementsInternal()) {
 			if (rubyElement.getFileExtension().equals("urf")) {
 				if (rubyElement.getName().equals(rubyScriptName)) {
 					return (RubyFile)rubyElement;
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Search for Spreadsheet inside a AWE RubyProject by name
 	 * 
 	 * @param rubyProject AWE Ruby Project
 	 * @param sheetName name of Spreadsheet
 	 * @return AWE Spreadsheet or null if there are no such scripts
 	 */
 	
 	private static Spreadsheet findSpreadsheet(RubyProject rubyProject, String sheetName) {
 		for (RubyProjectElement rubyElement : rubyProject.getRubyElementsInternal()) {
 			if (rubyElement.getFileExtension().equals("uss")) {
 				if (rubyElement.getName().equals(sheetName)) {
 					return (Spreadsheet)rubyElement;
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Delete RubyScript from AWE Project
 	 * 
 	 * @param scriptName name of Script
 	 * @param rubyProject RDT Ruby Project
 	 */
 	
 	public static void deleteRubyScript(String scriptName, IProject rubyProject) {
 		String aweProjectName = getAWEprojectNameFromResource(rubyProject);
 		
 		Project project = findProject(aweProjectName);
 		if (project != null) {
 			RubyProject ruby = findRubyProject(project, rubyProject.getName());
 			if (ruby != null) {
 				RubyProjectElement element = findRubyScript(ruby, scriptName);
 				if (element != null) {
 					deleteElement(element);
 				}
 			}
 		}		
 	}
 	
 	/**
 	 * Creates RubyScript in AWE project structure
 	 * 
 	 * @param rubyProject parent RDT Ruby Project
 	 * @param scriptName name of Script
 	 * @param resource resource of Script
 	 */
 	
 	public static void createRubyScript(IProject rubyProject, String scriptName, IResource resource) {
 		String name = getAWEprojectNameFromResource(rubyProject);
 		Project project = findProject(name);
 		RubyProject ruby = findRubyProject(project, rubyProject.getName());
 		if (ruby != null) {
 			createScriptIfNotExist(ruby, scriptName, resource);
 		}		
 	}
 	
 	/**
 	 * Creates Spreadsheet
 	 * 
 	 * @param rubyProject RubyProject that contain's Spreadsheet
 	 * @param sheetName name of Spreadsheet
 	 * @param resource resource of Spreadsheet
 	 */
 	
 	public static void createSpreadsheet(IProject rubyProject, String sheetName, IResource resource) {
 		String aweProjectName = getAWEprojectNameFromResource(rubyProject);
 		Project project = findProject(aweProjectName);
 		
 		RubyProject ruby = findRubyProject(project, rubyProject.getName());
 		if (ruby != null) {
 			createSpreadsheetIfNotExist(ruby, sheetName, resource);
 		}
 	}
 	
 	/**
 	 * Creates spreadsheet if it not exist
 	 * 
 	 * @param rubyProject name of RubyProject that will contain Spreadsheet
 	 * @param sheetName name of Spreadsheet
 	 * @param sheetResource resource for Spreadsheet
 	 */
 	
 	private static void createSpreadsheetIfNotExist(RubyProject rubyProject, String sheetName, IResource sheetResource) {
 		boolean exist = false;
 		
 		RubyProjectElement element = findSpreadsheet(rubyProject, sheetName);
 		if (element != null) {
 			exist = true;
 		}
 		
 		if (!exist) {
 			Spreadsheet spreadsheet = ProjectFactoryImpl.eINSTANCE.createSpreadsheet();
 			spreadsheet.setName(sheetName);
 			spreadsheet.setResource(sheetResource);
 			spreadsheet.setRubyProjectInternal(rubyProject);
 		}
 	}
 	
 	/**
      * Obtains the current project.
      * 
      * @return The current active project
      */
     public static Object getActiveGISProject() {
         Project project = ProjectPlugin.getPlugin().getProjectRegistry().getCurrentProject();
 
         if (project != null)
             return project;
 
         return ProjectPlugin.getPlugin().getProjectRegistry().getDefaultProject();
     }
 
     /**
      * Return all Projects. The list is unmodifiable.
      * 
      * @return all Projects.
      */
     public static List getGISProjects() {
         return Collections.unmodifiableList(ProjectPlugin.getPlugin().getProjectRegistry()
                 .getProjects());
     }
     
     /**
      * Obtains the current project.
      * 
      * @return The current active project name
      */
     public static String getActiveProjectName() {
         Project project = ProjectPlugin.getPlugin().getProjectRegistry().getCurrentProject();
 
         if (project == null)
             project = ProjectPlugin.getPlugin().getProjectRegistry().getDefaultProject();
         
         return project.getName();
     }
     
     /**
      * Returns type of the Object
      * 
      * @param object object
      * @return object type
      */
     
     public static int getType(Object object) {
     	if (object == null) {
     		return NO_OBJECT;
     	}
     	if (object instanceof net.refractions.udig.project.IProject) {
     		return AWE_PROJECT;
     	}
     	else if (object instanceof IRubyProject) {
     		return RUBY_PROJECT; 
     	}
     	else {
     		return UNKNOWN;
     	}
     }
     
     /**
      * Searches for name of RubyProject inside given AWE Project
      * 
      * @param aweObject AWEProject
      * @return name of internal RubyProject or null if no RubyProject inside an AWE Project
      */
     
     public static String getDefaultRubyProjectName(Object aweObject) {
     	String rubyName = null;
     	
     	if (aweObject != null) {
    		Project aweProject = (Project)aweObject;
    		if (aweProject.getElements(RubyProject.class).size() > 0) {
    			rubyName = aweProject.getElements(RubyProject.class).get(0).getName();
    		}
     	}
     	
     	return rubyName;
     }
     
     /**
      * Search for name of Ruby Project
      * 
      * @param aweProjectName name of AWE project
      * @param rubyProjectName name of Ruby Project (if it's null than searches for default Ruby Project name)
      * @return name of Ruby Project to search if this project exist, null if project doesn't exist or default name if rubyProjectName is null
      */
     
     public static String findRubyProjectName(String aweProjectName, String rubyProjectName) {
     	Project aweProject = findProject(aweProjectName);
     	
     	if (rubyProjectName != null) {
     		RubyProject project = findRubyProject(aweProject, rubyProjectName);
     		if (project == null) {
     			return null;
     		}
     		else {
     			return project.getName();
     		}
     	}
     	else {
     		RubyProject project = findRubyProject(aweProject, RUBY_PROJECT_DEFAULT_NAME);
     		if (project == null) {
     			return getDefaultRubyProjectName(aweProject);
     		}
     		else {
     			return project.getName();
     		}    		
     	}
     }
     
     /**
      * Returns name of AWE Proejct
      * 
      * @param aweObject object of AWEProject
      * @return name of AWE Project
      */
     
     public static String getAWEProjectName(Object aweObject) {
     	if (aweObject != null) {
     		Project aweProject = (Project)aweObject;
     		
     		return aweProject.getName();
     	}
     	
     	return getActiveProjectName();
     }
     
     /**
      * Search is name of Awe Project exist. If doesn't exist return default project name
      * 
      * @param projectName name of AWE project to search
      * @return name of AWE project or default name
      */
     
     public static String findAWEProjectName(String projectName) {
     	if (projectName == null) {
     		return getActiveProjectName();
     	}
     	
     	Project project = findProject(projectName);
     	
     	if (project == null) {
     		return null;
     	}
     	else {
     		return project.getName();
     	}
     }
 }
