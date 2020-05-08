 package fiuba.mda.model;
 
 import com.google.inject.Singleton;
 
 import fiuba.mda.utilities.SimpleEvent;
 import fiuba.mda.utilities.SimpleEvent.Observer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Represents the document model of the application, managing the different
  * selections an states the application goes through when interacting with the
  * user.
  */
 @Singleton
 public class Application {
 	private final SimpleEvent<Application> projectOpenEvent = new SimpleEvent<>(
 			this);
 
 	private final SimpleEvent<Application> packageActivatedEvent = new SimpleEvent<>(
 			this);
 
 	private final SimpleEvent<Application> hierarchyChangedEvent = new SimpleEvent<>(
 			this);
 
 	private Observer<Project> onHierarchyChanged = new Observer<Project>() {
 		@Override
 		public void notify(Project observable) {
 			hierarchyChangedEvent.raise();
 		}
 	};
 
 	private Project currentProject;
 
 	private ProjectComponent selection;
 
 	/**
 	 * Checks if you have a current open project
 	 * 
 	 * @return true if the application has a current project, false otherwise
 	 */
 	public boolean hasCurrentProject() {
 		return currentProject != null;
 	}
 
 	/**
 	 * Obtains the current open project, if any.
 	 * 
 	 * @throws RuntimeException
 	 *             if there is no current project. Check with
 	 *             {@link Application#hasCurrentProject()} first to ensure this
 	 *             exception isn't raised.
 	 * @return the current open project
 	 */
 	public Project getCurrentProject() {
 		if (!hasCurrentProject()) {
 			throw new RuntimeException("There is no current project");
 		}
 		return currentProject;
 	}
 
 	/**
 	 * Obtains the current active package
 	 * 
 	 * @throws RuntimeException
 	 *             if there is no current project. Check
 	 *             {@link Application#hasCurrentProject()} to ensure the
 	 *             exception isn't raised.
 	 * @return the active package
 	 */
 	public ModelPackage getActivePackage() {
 		if (!hasCurrentProject()) {
 			throw new RuntimeException("There is no current project");
 		}
 
 		if (selection != null) {
 			return selection.locateOwningPackage();
 		} else {
 			return currentProject.getRootPackage();
 		}
 	}
 
     public List<String> getAllGIDiagramForActivePackage(){
         return getAllGIDiagramForPackage(getActivePackage());
     }
 
     public List<String> getAllGIDiagramForPackage(ModelPackage modelPackage){
         List<String> returnList = new ArrayList<>();
         List<ProjectComponent> childrens = modelPackage.getChildren();
         for (ProjectComponent children : childrens){
             if (children instanceof ModelPackage){
                 returnList.addAll(getAllGIDiagramForPackage((ModelPackage) children));
             }
 
            if (children instanceof ModelAspect && children.getName().equals("Interfaces Gráficas")){
                 List<ProjectComponent> children1 = children.getChildren();
                 for (ProjectComponent pc : children1){
                     returnList.add(pc.getQualifiedName());
                 }
             }
         }
         return returnList;
     }
 
 	public ProjectComponent getSelectedComponent() {
 		if (!hasCurrentProject()) {
 			throw new RuntimeException("There is no current project");
 		}
 
 		if (selection == null) {
 			return currentProject.getRootPackage();
 		} else {
 			return selection;
 		}
 	}
 
 	public void select(final ProjectComponent component) {
 		selection = component;
 	}
 
 	public void clearSelection() {
 		selection = null;
 	}
 
 	/**
 	 * Opens a new project, setting it as the current open one
 	 * 
 	 * @param project
 	 *            the project to set as the current open
 	 */
 	public void openProject(final Project project) {
 		if (currentProject != null) {
 			currentProject.hierarchyChangedEvent()
 					.unobserve(onHierarchyChanged);
 		}
 		currentProject = project;
 		currentProject.hierarchyChangedEvent().observe(onHierarchyChanged);
 		this.projectOpenEvent.raise();
 	}
 
 	/**
 	 * An event raised when a new project has been open
 	 * 
 	 * @return the event
 	 */
 	public SimpleEvent<Application> projectOpenEvent() {
 		return projectOpenEvent;
 	}
 
 	/**
 	 * An event raised when a package has been activated
 	 * 
 	 * @return the event
 	 */
 	public SimpleEvent<Application> packageActivatedEvent() {
 		return packageActivatedEvent;
 	}
 
 	/**
 	 * An event raised when anything in the component hierarchy of the project
 	 * has changed
 	 * 
 	 * @return the event
 	 */
 	public SimpleEvent<Application> hierarchyChangedEvent() {
 		return hierarchyChangedEvent;
 	}
 }
