 package org.inftel.ssa.web;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.ejb.EJB;
 import javax.faces.application.Application;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.ViewScoped;
 import javax.faces.component.html.HtmlOutputText;
 import javax.faces.component.html.HtmlPanelGroup;
 import javax.faces.context.FacesContext;
 import org.inftel.ssa.domain.Task;
 import org.inftel.ssa.domain.TaskStatus;
 import org.inftel.ssa.domain.User;
 import org.inftel.ssa.services.ResourceService;
 import org.primefaces.component.dashboard.Dashboard;
 import org.primefaces.component.panel.Panel;
 import org.primefaces.event.DashboardReorderEvent;
 import org.primefaces.model.DashboardColumn;
 import org.primefaces.model.DashboardModel;
 import org.primefaces.model.DefaultDashboardColumn;
 import org.primefaces.model.DefaultDashboardModel;
 
 @ManagedBean
 @ViewScoped
 public class DashboardManager implements Serializable {
 	public static final String TASK_PREFIX = "task_id_";
 
 	private final static Logger logger = Logger.getLogger(TaskManager.class.getName());
 	private static final long serialVersionUID = 1L;
 	@ManagedProperty(value = "#{sprintManager}")
 	private SprintManager sprintManager;
 	@ManagedProperty(value = "#{projectManager}")
 	private ProjectManager projectManager;
 	@ManagedProperty(value = "#{userManager}")
 	private UserManager userManager;
 	private int columnCount;
 	private Dashboard dashboard;
 	private DashboardModel dashboardModel;
 	@EJB
 	private ResourceService resourecService;
 
 	public DashboardManager() {
 	}
 
 	public void init() {
 		User currentUser = userManager.getCurrentUser();
 		columnCount = 3;
 		FacesContext fc = FacesContext.getCurrentInstance();
 		Application application = fc.getApplication();
 
 		dashboard = (Dashboard) application.createComponent(fc, "org.primefaces.component.Dashboard", "org.primefaces.component.DashboardRenderer");
 		dashboard.setId("dashboard");
 
 		dashboardModel = new DefaultDashboardModel();
 
 		DashboardColumn toDo = new DefaultDashboardColumn();
 		DashboardColumn inProgress = new DefaultDashboardColumn();
 		DashboardColumn done = new DefaultDashboardColumn();
 
 
 		dashboardModel.addColumn(toDo);
 		dashboardModel.addColumn(inProgress);
 		dashboardModel.addColumn(done);
 
 		dashboard.setModel(dashboardModel);
 
		List<Task> tasks = projectManager.getCurrentProject(true).getTasks();
 		//Estas tareas tendrian que estar filtradas por el currenteSprint
 		for (Task task : tasks) {
 			Panel panel = (Panel) application.createComponent(fc, "org.primefaces.component.Panel", "org.primefaces.component.PanelRenderer");
 			//Al establecer el id me daba error sino ponia al menos una cadena de texto. Raro, la verdad
 			panel.setId(TASK_PREFIX + task.getId().toString());
 			panel.setHeader(task.getSummary());
 
 			dashboard.getChildren().add(panel);
 			
 			// Style by task owner
 			String styles = "well";
 			if (task.getUser() == null) {
 				styles = styles + " none-user";
 			} else if (task.getUser().equals(currentUser)) {
 				styles = styles + " current-user";
 			} else {
 				styles = styles + " team-user";
 			}
 			panel.setStyleClass(styles);
 			
 			DashboardColumn column = dashboardModel.getColumn(task.getStatus().ordinal());
 			column.addWidget(panel.getId());
 
 			HtmlOutputText priority = new HtmlOutputText();
 			priority.setStyleClass("priority");
 			priority.setValue("Priority: " + task.getPriority());
 
 			HtmlOutputText owner = new HtmlOutputText();
 			owner.setStyleClass("owner");
 			owner.setValue("Owner: " + (task.getUser() == null ? "nadie" : task.getUser().getEmail()));
 
 			HtmlOutputText description = new HtmlOutputText();
 			description.setStyleClass("description");
 			description.setValue(task.getDescription());
 
 			HtmlPanelGroup content = new HtmlPanelGroup();
 			content.setLayout("block");
 			content.setStyleClass("subtitle");
 			content.getChildren().add(priority);
 			content.getChildren().add(owner);
 			
 			panel.getChildren().add(content);
 			panel.getChildren().add(description);
 			
 		}
 	}
 
 	public Dashboard getDashboard() {
 		init();
 		return dashboard;
 	}
 
 	public void setDashboard(Dashboard dashboard) {
 		this.dashboard = dashboard;
 	}
 
 	public int getColumnCount() {
 		return columnCount;
 	}
 
 	public void setColumnCount(int columnCount) {
 		this.columnCount = columnCount;
 	}
 
 	public ProjectManager getProjectManager() {
 		return projectManager;
 	}
 
 	public void setProjectManager(ProjectManager projectManager) {
 		this.projectManager = projectManager;
 	}
 
 	public SprintManager getSprintManager() {
 		return sprintManager;
 	}
 
 	public void setSprintManager(SprintManager sprintManager) {
 		this.sprintManager = sprintManager;
 	}
 
 	public UserManager getUserManager() {
 		return userManager;
 	}
 
 	public void setUserManager(UserManager userManager) {
 		this.userManager = userManager;
 	}
 
 	public void handleReorder(DashboardReorderEvent event) {
 		String widgetId = event.getWidgetId();
 		int columnIndex = event.getColumnIndex();
 		//TODO actualizar las tareas
 		logger.info("Widget movido: " + widgetId);
 		Task task = resourecService.findTask(Long.parseLong(widgetId.substring(TASK_PREFIX.length())));
 		task.setStatus(TaskStatus.values()[columnIndex]);
 		resourecService.saveTask(task);
 		logger.info("Columna destino: " + columnIndex);
 
 	}
 }
