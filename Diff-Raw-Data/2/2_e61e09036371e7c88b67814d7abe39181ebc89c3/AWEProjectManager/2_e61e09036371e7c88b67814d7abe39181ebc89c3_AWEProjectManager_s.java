 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.integrator.awe;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import net.refractions.udig.project.IProjectElement;
 import net.refractions.udig.project.IRubyProject;
 import net.refractions.udig.project.IRubyProjectElement;
 import net.refractions.udig.project.ISpreadsheet;
 import net.refractions.udig.project.internal.Map;
 import net.refractions.udig.project.internal.Project;
 import net.refractions.udig.project.internal.ProjectElement;
 import net.refractions.udig.project.internal.ProjectPlugin;
 import net.refractions.udig.project.internal.RubyFile;
 import net.refractions.udig.project.internal.RubyProject;
 import net.refractions.udig.project.internal.RubyProjectElement;
 import net.refractions.udig.project.internal.Spreadsheet;
 import net.refractions.udig.project.internal.SpreadsheetType;
 import net.refractions.udig.project.internal.impl.ProjectFactoryImpl;
 
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.database.nodes.AweProjectNode;
 import org.amanzi.neo.core.database.nodes.RubyProjectNode;
 import org.amanzi.neo.core.database.nodes.SpreadsheetNode;
 import org.amanzi.neo.core.database.services.AweProjectService;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.ui.IActionDelegate;
 import org.neo4j.api.core.Transaction;
 
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
 	
 	private static final String DELETE_ACTION_CLASS_NAME = "net.refractions.udig.project.ui.internal.actions.Delete";
 
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
 					ProjectPlugin.log(null, e);
 				}
 				finally {			
 					rubyProjects.add(resourceProject);
 				}
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
 	
 	public static String getAWEprojectNameFromResource(IProject rubyProject) {
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
         
         projectInternal.eResource().setModified(true);
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
 	 * Search for child spreadsheet inside parent spreadsheet
 	 *
 	 * @param parentSpreadsheet parent Spreadsheet
 	 * @param sheetName name of child Spreadsheet
 	 * @return child Spreadsheet or null if not found
 	 */
 	private static Spreadsheet findChildSpreadsheet(Spreadsheet parentSpreadsheet, String sheetName) {
 	    for (Spreadsheet child : parentSpreadsheet.getChildSpreadsheets()) {
 	        if (child.getName().equals(sheetName)) {
 	            return child;
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
 	 * Creates Neo4J-based Spreadsheet
 	 *
 	 * @param rubyProject RubyProject that contain's Spreadsheet
      * @param sheetName name of Spreadsheet
      * @param resourcePath URL of Spreadsheet resource
 	 */
 	
 	public static void createNeoSpreadsheet(IProject rubyProject, String sheetName, URL resourcePath) {
 	    String aweProjectName = getAWEprojectNameFromResource(rubyProject);
         Project project = findProject(aweProjectName);
         
         RubyProject ruby = findRubyProject(project, rubyProject.getName());
         if (ruby != null) {
             if (createSpreadsheetIfNotExist(ruby, sheetName, resourcePath, SpreadsheetType.NEO4J_SPREADSHEET)){
             	NeoCorePlugin.getDefault().getProjectService().findOrCreateSpreadsheet(aweProjectName, rubyProject.getName(), sheetName);
             }
         }        
 	}
 	
 	/**
 	 * Creates Child Spreadsheet
 	 *
 	 * @param rubyProject RubyProject that contains Spreadsheet
 	 * @param parentSpreadsheetName name of parent Spreadsheet
 	 * @param sheetName name of child Spreadsheet
 	 * @param resourcePath resource URL for Spreadsheet
 	 */
 	public static void createChildNeoSpreadsheet(IProject rubyProject, String parentSpreadsheetName, String sheetName, URL resourcePath) {
 	    String aweProjectName = getAWEprojectNameFromResource(rubyProject);
         Project project = findProject(aweProjectName);
         
         RubyProject ruby = findRubyProject(project, rubyProject.getName());
         Spreadsheet parentSpreadsheet = findSpreadsheet(ruby, parentSpreadsheetName);
         if (ruby != null) {
             createChildSpreadsheetIfNotExist(parentSpreadsheet, sheetName, resourcePath, SpreadsheetType.NEO4J_SPREADSHEET);
         }
 	}
 	
 	/**
 	 * Creates child Spreadsheet if it not exist
 	 *
 	 * @param parentSpreadsheet parent Spreadsheet
 	 * @param sheetName name of child Spreadsheet
 	 * @param resourceURL URL for Spreadsheet
 	 * @param type type of Spreadsheet
 	 * @return was spreadsheet created, or it exists
 	 */
 	private static boolean createChildSpreadsheetIfNotExist(Spreadsheet parentSpreadsheet, String sheetName, URL resourceURL, SpreadsheetType type) {
 	    Spreadsheet childSpreadsheet = findChildSpreadsheet(parentSpreadsheet, sheetName);
 	    if (childSpreadsheet == null) {
 	        Spreadsheet spreadsheet = ProjectFactoryImpl.eINSTANCE.createSpreadsheet();
 	        spreadsheet.setName(sheetName);
             spreadsheet.setSpreadsheetPath(resourceURL);    
             spreadsheet.setSpreadsheetType(type);
             parentSpreadsheet.getChildSpreadsheets().add(spreadsheet);
             spreadsheet.setParentSpreadsheet(parentSpreadsheet);
 	    }
 	    
 	    return childSpreadsheet == null;
 	}
 	
 	/**
 	 * Creates spreadsheet if it not exist
 	 * 
 	 * @param rubyProject name of RubyProject that will contain Spreadsheet
 	 * @param sheetName name of Spreadsheet
 	 * @param sheetResource resource for Spreadsheet
 	 * @return true if Spreadsheet was created
 	 */
 	
 	private static boolean createSpreadsheetIfNotExist(RubyProject rubyProject, String sheetName, URL resourceURL, SpreadsheetType type) {
 		RubyProjectElement element = findSpreadsheet(rubyProject, sheetName);
 		if (element == null) {
 			Spreadsheet spreadsheet = ProjectFactoryImpl.eINSTANCE.createSpreadsheet();
 			spreadsheet.setName(sheetName);
 			spreadsheet.setSpreadsheetPath(resourceURL);
 			spreadsheet.setRubyProjectInternal(rubyProject);	
 			spreadsheet.setSpreadsheetType(type);
 			rubyProject.getRubyElementsInternal().add(spreadsheet);
 			if (type==SpreadsheetType.NEO4J_SPREADSHEET){
 				 NeoCorePlugin.getDefault().getProjectService();
 			}
 		}
 		return element == null;
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
     public static List<Project> getGISProjects() {
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
     	//Lagutko 16.07.2009, object can be RubyProject also in the case if it's IProject (from org.eclipse.core.resources)
     	else if ((object instanceof IRubyProject) || (object instanceof IProject)) {
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
     	    //Lagutko 16.07.2009, aweObject can als be a Map, handle it
     	    if (aweObject instanceof Project) {
     	        Project aweProject = (Project)aweObject;
     	        if (aweProject.getElements(RubyProject.class).size() > 0) {
     	    	rubyName = aweProject.getElements(RubyProject.class).get(0).getName();
     	        }
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
 	//Lagutko 16.07.2009, aweObject can als be a Map, handle it
     	if (aweObject != null) {
     	    if (aweObject instanceof Project) {
     		Project aweProject = (Project)aweObject;
     		
     		return aweProject.getName();
     	    }
     	    else if (aweObject instanceof Map) {
     		Map map = (Map)aweObject;
     		
     		return map.getProjectInternal().getName();
     	    }
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
     
     /**
      * Method that computes Spreadsheets of RubyProject
      *
      * @param rubyProjectResource Resource of Ruby Project
      * @return List of Spreadsheets for given Ruby Project
      */
     public static List<String> getSpreadsheetsOfRubyProject(IProject rubyProjectResource) {
         String rubyProjectName = rubyProjectResource.getName();
         String aweProjectName = getAWEprojectNameFromResource(rubyProjectResource);
         
         AweProjectService projectService = NeoCorePlugin.getDefault().getProjectService();
         
         AweProjectNode aweProject = projectService.findOrCreateAweProject(aweProjectName);
         RubyProjectNode rubyProject = projectService.findOrCreateRubyProject(aweProject, rubyProjectName);
         Transaction tx = NeoServiceProvider.getProvider().getService().beginTx();
         try {
             Iterator<SpreadsheetNode> spreadsheetIterator = rubyProject.getSpreadsheets();
 
             ArrayList<String> spreadsheets = new ArrayList<String>(0);
             while (spreadsheetIterator.hasNext()) {
                 spreadsheets.add(spreadsheetIterator.next().getSpreadsheetName());
             }
 
             return spreadsheets;
         } finally {
             tx.finish();
         }
     }
     
     /**
      * Deletes a Spreadsheets
      *
      * @param spreadsheetsToDelete map of Spreadsheets to Delete
      */
     public static void deleteSpreadsheets(HashMap<IProject, List<String>> spreadsheetsToDelete) {
         ArrayList<Spreadsheet> spreadsheets = new ArrayList<Spreadsheet>(0);
         
         for (IProject rubyProjectResource : spreadsheetsToDelete.keySet()) {
             String rubyProjectName = rubyProjectResource.getName();
             String aweProjectName = getAWEprojectNameFromResource(rubyProjectResource);
         
             Project aweProject = findProject(aweProjectName); 
             RubyProject rubyProject = findRubyProject(aweProject, rubyProjectName);
             
             for (IRubyProjectElement element : rubyProject.getElements(ISpreadsheet.class)) {
                 if (spreadsheetsToDelete.get(rubyProjectResource).contains(element.getName())) {
                     spreadsheets.add((Spreadsheet)element);
                 }
             }
         }
         
         try {
             IActionDelegate deleteAction = (IActionDelegate)Class.forName(DELETE_ACTION_CLASS_NAME).newInstance();
             deleteAction.selectionChanged(null, new StructuredSelection(spreadsheets));
             deleteAction.run(null);            
         }
         catch (IllegalAccessException e) {
             ProjectPlugin.log(null, e);
             return;
         }
         catch (ClassNotFoundException e) {
             ProjectPlugin.log(null, e);
             return;
         }
         catch (InstantiationException e) {
             ProjectPlugin.log(null, e);
             return;
         }
     }
     
     /**
      * Renames RubyProject in AWE Project Structure and Database
      *
      * @param rubyProjectResource Resource of Ruby Project to Rename
      * @param newName new Name of Ruby Project
      * @author Lagutko_N
      */
     public static void renameRubyProject(IProject rubyProjectResource, String newName) {
         //computes names of AWE and Ruby Project
         String rubyProjectName = rubyProjectResource.getName();
         String aweProjectName = getAWEprojectNameFromResource(rubyProjectResource);
     
         //get AWE and Ruby Elements from EMF structure
         Project aweProject = findProject(aweProjectName);
         RubyProject rubyProject = findRubyProject(aweProject, rubyProjectName);
         
         //create a new Ruby Project with a newName
         createProjectIfNotExist(aweProject, newName);
         RubyProject newProject = findRubyProject(aweProject, newName);
         //add elements to new Ruby Project
         newProject.getRubyElementsInternal().addAll(rubyProject.getRubyElementsInternal());
         
         AweProjectService service = NeoCorePlugin.getDefault().getProjectService();
         service.renameRubyProject(aweProjectName, rubyProjectName, newName);
     }
 
     /**
      * Renames Spreadsheet in AWE Project Structure
      *
      * @param rubyProjectResource resource of parent Ruby Project
      * @param oldName old Name of Spreadsheet
      * @param newName new Name of Spreadsheet
      */
     public static void renameSpreadsheet(IProject rubyProjectResource, String oldName, String newName) {
         String rubyProjectName = rubyProjectResource.getName();
         String aweProjectName = getAWEprojectNameFromResource(rubyProjectResource);
         
         Project aweProject = findProject(aweProjectName);
         RubyProject rubyProject = findRubyProject(aweProject, rubyProjectName);
         Spreadsheet spreadsheet = findSpreadsheet(rubyProject, oldName);
         
         spreadsheet.setName(newName);
         spreadsheet.eResource().setModified(true);
     }
 
     /**
      * Compute name of AWE project
      * 
      * @param nameObject - selection
      * @return name
      */
     public static String computeAWEProjectName(Object nameObject) {
         if (nameObject instanceof net.refractions.udig.project.IProject){
             return ((net.refractions.udig.project.IProject)nameObject).getName();
         }if (nameObject instanceof IProjectElement){
             return ((IProjectElement)nameObject).getProject().getName();
         }
         return null;
     }
 }
