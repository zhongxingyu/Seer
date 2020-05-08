 package de.oose.taskboard.client;
 
 import javax.inject.Inject;
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.web.bindery.event.shared.EventBus;
 
 import de.oose.taskboard.client.event.DeleteTaskEvent;
 import de.oose.taskboard.client.event.EditTaskCancelledEvent;
 import de.oose.taskboard.client.event.EditTaskEvent;
 import de.oose.taskboard.client.event.LoginEvent;
 import de.oose.taskboard.client.event.UpdateTaskEvent;
 import de.oose.taskboard.client.place.LoggedUser;
 import de.oose.taskboard.client.presenter.EditTaskPresenter;
 import de.oose.taskboard.client.presenter.LoginPresenter;
 import de.oose.taskboard.client.presenter.Presenter;
 import de.oose.taskboard.client.presenter.TaskListPresenter;
 import de.oose.taskboard.client.service.TaskServiceAsync;
 import de.oose.taskboard.shared.bo.TaskBO;
 import de.oose.taskboard.shared.bo.UserBO;
 
 /**
  * The main class of the application, responsible for reacting to events and
  * dispatching to the correct presenters, which coordinate the view.
  * 
  * @author markusklink
  * 
  */
 public class AppController implements Presenter, ValueChangeHandler<String> {
 
 	private static final String HISTORY_EDIT = "edit";
 	private static final String HISTORY_TASKLIST = "taskList";
 	private static final String HISTORY_LOGIN = "login";
 
 	private final EventBus eventBus;
 
 	private final TaskServiceAsync taskService;
 
 	private HasWidgets container;
 
	private final EditTaskPresenter editTaskPresenter;
 
 	private final LoginPresenter loginPresenter;

 	private final TaskListPresenter tasklistPresenter;
 
 	private UserBO loggedUser;
 
 	@Inject
 	public AppController(TaskServiceAsync taskService,
			LoginPresenter loginPresenter, TaskListPresenter tasklistPresenter,
			EditTaskPresenter editTaskPresenter, @LoggedUser UserBO loggedUser,
			EventBus eventBus) {
 		this.taskService = taskService;
 		this.eventBus = eventBus;
 		this.loginPresenter = loginPresenter;
 		this.tasklistPresenter = tasklistPresenter;
		this.editTaskPresenter = editTaskPresenter;
 		this.loggedUser = loggedUser;
 		bind();
 	}
 
 	public void init() {
 
 	}
 
 	private void bind() {
 		History.addValueChangeHandler(this);
 
 		eventBus.addHandler(EditTaskEvent.TYPE,
 				new EditTaskEvent.EditTaskHandler() {
 
 					@Override
 					public void onEditTask(EditTaskEvent event) {
 						History.newItem(HISTORY_EDIT, false);
 						TaskBO task = event.getTaskBO();
 						editTaskPresenter.setUserBO(loggedUser);
 						editTaskPresenter.setTask(task);
 						editTaskPresenter.go(container);
 					}
 				});
 
 		eventBus.addHandler(EditTaskCancelledEvent.TYPE,
 				new EditTaskCancelledEvent.EditTaskCancelledHandler() {
 
 					@Override
 					public void onEditTaskCancelled(EditTaskCancelledEvent event) {
 						doEditTaskCancelled();
 					}
 				});
 
 		eventBus.addHandler(UpdateTaskEvent.TYPE,
 				new UpdateTaskEvent.UpdateTaskHandler() {
 
 					@Override
 					public void onUpdateTask(UpdateTaskEvent event) {
 						History.newItem(HISTORY_TASKLIST);
 					}
 				});
 
 		eventBus.addHandler(DeleteTaskEvent.TYPE,
 				new DeleteTaskEvent.DeleteTaskHandler() {
 
 					@Override
 					public void onDeleteTask(DeleteTaskEvent event) {
 						History.newItem(HISTORY_TASKLIST);
 					}
 				});
 
 		eventBus.addHandler(LoginEvent.TYPE, new LoginEvent.LoginHandler() {
 
 			@Override
 			public void onLogin(LoginEvent event) {
 				loggedUser = event.getAccount();
 				if (loggedUser == null) {
 					History.newItem(HISTORY_LOGIN);
 				} else {
 					History.newItem(HISTORY_TASKLIST);
 				}
 			}
 		});
 	}
 
 	private void doEditTaskCancelled() {
 		History.newItem(HISTORY_TASKLIST);
 	}
 
 	// Methode die aufgerufen wird, wenn im Browser "Back", "Forward" geklickt
 	// werden
 	@Override
 	public void onValueChange(ValueChangeEvent<String> event) {
 		String token = event.getValue();
 
 		if (token != null) {
 			if (token.equals(HISTORY_LOGIN)) {
 				loginPresenter.go(container);
 			}
 
 			if (token.equals(HISTORY_TASKLIST)) {
 				tasklistPresenter.setLoggedUser(loggedUser);
 				tasklistPresenter.go(container);
 			}
 		}
 
 	}
 
 	// setzen des Initialzustandes für die History. Damit wird die Startview
 	// gesetzt
 	// oder, falls ein Historyeintrag gesetzt ist, entsprechend an die richtige
 	// View
 	// weitergeleitet
 	@Override
 	public void go(HasWidgets container) {
 		this.container = container;
 
 		if ("".equals(History.getToken())) {
 			History.newItem(HISTORY_LOGIN);
 		} else {
 			History.fireCurrentHistoryState();
 		}
 
 	}
 
 }
