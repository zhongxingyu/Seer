 package org.inftel.ssa.web;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import org.inftel.ssa.domain.Project;
 import org.inftel.ssa.domain.Sprint;
 import org.inftel.ssa.domain.Task;
 import org.inftel.ssa.services.ResourceService;
 import org.primefaces.model.DualListModel;
 import org.primefaces.model.LazyDataModel;
 import org.primefaces.model.SortOrder;
 
 /**
  *
  * @author Jesus Ruiz Oliva
  */
 @ManagedBean
 @SessionScoped
 public class SprintManager implements Serializable {
 
 	private final static Logger logger = Logger.getLogger(SprintManager.class.getName());
 	@EJB
 	private ResourceService resources;
 	private static final long serialVersionUID = 1L;
 	private LazyDataModel<Sprint> sprints;
 	private Sprint currentSprint;
 	@ManagedProperty("#{projectManager}")
 	private ProjectManager projectManager;
 	private DualListModel<Task> tasks;
 
 	@PostConstruct
 	public void init() {
 		sprints = new LazyDataModel() {
 
 			@Override
 			public List load(int first, int pageSize, String sortField, org.primefaces.model.SortOrder sortOrder, Map filters) {
 				logger.log(Level.INFO, "lazy data model [first={0}, pageSize={1}, sortField={2}, sortOrder={3}, filters={4}]", new Object[]{first, pageSize, sortField, sortOrder, filters});
 				setRowCount(resources.countSprintsByProject(projectManager.getCurrentProject(), sortField, sortOrder == SortOrder.ASCENDING, filters));
 				return resources.findSprintsByProject(projectManager.getCurrentProject(), first, pageSize, sortField, sortOrder == SortOrder.ASCENDING, filters);
 			}
 		};
 	}
 
 	public String remove() {
 		Sprint currentSprint = sprints.getRowData();
 		//Remover currentSprint;        
 		return "/sprint/index.xhtml";
 	}
 
 	public String create() {
 		Sprint sprint = new Sprint();
 		setCurrentSprint(sprint);
 		return "/sprint/create?faces-redirect=true";
 	}
 
 	public String save() {
 		if (currentSprint != null) {
 			currentSprint.setProject(projectManager.getCurrentProject());
 			resources.saveSprint(currentSprint);
 		}
 		return "/sprint/index?faces-redirect=true";
 	}
 
 	public String saveEdit() {
 		logger.info("guardando sprint");
 		if (currentSprint.getProject() == null) {
 			logger.info("creando nuevo sprint");
 			Project current = projectManager.getCurrentProject();
 			currentSprint.setProject(current);
 			// debe guardarse primero para obter id valido
 			currentSprint = resources.saveSprint(currentSprint);
 		}
 //		for (Task task : tasks.getSource()) {
 //			Task modified = resources.findTask(task.getId());
 //			modified.setSprint(currentSprint);
 //			resources.saveTask(modified);
 //		}
 		List<Task> associatedTasks = new ArrayList<Task>();
 		for (Task task : tasks.getTarget()) {
 			Task associated = resources.findTask(task.getId());
 			associated.setSprint(currentSprint);
 			// Si es nueva se configura la tarea
 			if (!currentSprint.getTasks().contains(associated)) {
 				associated.setSprint(currentSprint);
 				associated = resources.saveTask(associated);
 				logger.info("añadiendo tarea " + associated);
 			}
 			// Se crea la nueva lista
 			associatedTasks.add(associated);
 		}
 		// si una tarea que antes tenia ahora no, se elimina
 		for (Task task : currentSprint.getTasks()) {
 			if (!associatedTasks.contains(task)) {
 				task.setSprint(null);
 				resources.saveTask(task);
 				logger.info("eliminando tarea " + task);
 			} 
 		}
 		currentSprint.setTasks(associatedTasks);
 		resources.saveSprint(currentSprint);
 		return "/sprint/index?faces-redirect=true";
 	}
 
 	public String edit() {
 		setCurrentSprint(resources.findSprint(sprints.getRowData().getId()));
 		populatePickListTasks();
 		return "/sprint/edit?faces-redirect=true";
 	}
 
 	public void populatePickListTasks() {
 		List<Task> tasksSource = new ArrayList<Task>();
 		List<Task> tasksTarget = new ArrayList<Task>();
 		List<Task> tasksCurrentProject = projectManager.getCurrentProject(true).getTasks();
 		for (Task task : tasksCurrentProject) {
 
 			if (task.getSprint() == null) {
 				tasksSource.add(task);
			} else if (task.getSprint().getId()==currentSprint.getId()){ //No se añaden las tareas asignadas a otros sprints
 				tasksTarget.add(task);
 			}
 		}
 		tasks = new DualListModel<Task>(tasksSource, tasksTarget);
 	}
 
 	// --------------------------------------------------------------------------- Getters & Setters
 	public LazyDataModel<Sprint> getSprints() {
 		return sprints;
 	}
 
 	public void setSprints(LazyDataModel<Sprint> sprints) {
 		this.sprints = sprints;
 	}
 
 	public Sprint getCurrentSprint() {
 		return currentSprint;
 	}
 
 	public void setCurrentSprint(Sprint currentSprint) {
 		this.currentSprint = currentSprint;
 	}
 
 	public ProjectManager getProjectManager() {
 		return projectManager;
 	}
 
 	public void setProjectManager(ProjectManager projectManager) {
 		this.projectManager = projectManager;
 	}
 
 	public DualListModel<Task> getTasks() {
 		return tasks;
 	}
 
 	public void setTasks(DualListModel<Task> tasks) {
 		this.tasks = tasks;
 	}
 }
