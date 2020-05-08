 package com.bobvash.resolutiontracker.client;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.tools.ant.taskdefs.Taskdef;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DecoratedPopupPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class ResolutionDayWidget extends VerticalPanel {
 	private Date date;
 	private final List<TaskListClientView> loadedTaskList = new ArrayList<TaskListClientView>();
 	private final VerticalPanel tasksHolderPanel = new VerticalPanel();
 
 	private final TaskManagementServiceAsync taskManagementService = GWT
 			.create(TaskManagementService.class);
 
 	public ResolutionDayWidget(final Date date) {
 		this.date = date;
 		this.setSize("100px", "100px");
 		this.setSpacing(5);
 
 		HorizontalPanel headerHolderPanel = new HorizontalPanel();
 		Label dayLabel = new Label("" + date.getDate());
 		headerHolderPanel.add(dayLabel);
 		dayLabel.setWidth("50px");
 
 		Button newTaskButton = new Button("+");
 		newTaskButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				final String newTaskDescription = Window.prompt(
 						"What will the new task be?", "");
 				if (newTaskDescription.length() > 0) //TODO NOT ASSUME FIRST
 				{
 					TaskListClientView taskListToAddTo;
 					if (loadedTaskList.size() > 0) {
 						taskManagementService.addTaskToList(loadedTaskList.get(0), new SingleTaskClientView(newTaskDescription, false), 
 							new AsyncCallback<Void>() {
 
 								@Override
 								public void onFailure(Throwable error) {
 									Window.alert("Creation of new task failed: " + error.getMessage());
 								}
 
 								@Override
 								public void onSuccess(Void ignore) {
 									loadTasksForDay();
 								}
 							});
 
 					}
 					else {
 						taskManagementService.saveTaskList( new TaskListClientView(date, new SingleTaskClientView[]{new SingleTaskClientView(newTaskDescription, false)}),  
 								new AsyncCallback<Void>() {
 
 									@Override
 									public void onFailure(Throwable error) {
 										Window.alert("Creation of new task failed: " + error.getMessage());
 									}
 
 									@Override
 									public void onSuccess(Void ignore) {
 										loadTasksForDay();
 									}
 								});
 						
 					}
 				}
 			}
 		});
 		newTaskButton.setHeight("20px");
 		newTaskButton.setStyleName("dayWidgetOutOfRange");
 		headerHolderPanel.add(newTaskButton);
 		
 		Button removeTaskButtion = new Button("-");
 		removeTaskButtion.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				final String taskDescription = Window.prompt(
 						"Which task to remove?", "");
 				if (taskDescription.length() > 0)  //TODO GET ACTUAL INSTANCE OF SINGLETAKSCLIENTVIEW OBJECT
 					if (loadedTaskList.size() > 0)
 						taskManagementService.removeFromTasklist(loadedTaskList.get(0), new SingleTaskClientView(taskDescription, false),
 							new AsyncCallback<Void>() {
 
 								@Override
 								public void onFailure(Throwable error) {
 									Window.alert("Removal of task failed: " + error.getMessage());
 								}
 
 								@Override
 								public void onSuccess(Void ignore) {
 									loadTasksForDay();
 								}
 							});
 			}
 		});
 		removeTaskButtion.setHeight("20px");
 		removeTaskButtion.setStyleName("dayWidgetOutOfRange");
 		headerHolderPanel.add(removeTaskButtion);
 
 		this.add(headerHolderPanel);
 		loadTasksForDay();
 		
 		this.add(tasksHolderPanel);
 	}
 
 	private void loadTasksForDay() {
 		taskManagementService.getTasksForDate(date,
 				new AsyncCallback<TaskListClientView>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						Window.alert("Loading of tasks failed: " + caught.getMessage());
 					}
 
 					@Override
 					public void onSuccess(TaskListClientView result) {
 
 						while(tasksHolderPanel.getWidgetCount() > 0)
 							tasksHolderPanel.remove(tasksHolderPanel.getWidget(0));
 						
 						loadedTaskList.clear();
						loadedTaskList.add(result);
						for (final TaskListClientView taskList: loadedTaskList)
							for (final SingleTaskClientView task : taskList.getTasks()) {
								createNewTaskWidget(taskList, task);
 						}
 					}
 				});
 	}
 	
 	private void createNewTaskWidget(final TaskListClientView currentTaskList, final SingleTaskClientView task) {
 		final int maxButtonTextLength = 8;
 		final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
 	    simplePopup.ensureDebugId("cwBasicPopup-simplePopup");
 	    simplePopup.setWidth("150px");
         simplePopup.setWidget(new Label(task.getDescription()));
 	    
 		HorizontalPanel taskRowPanel = new HorizontalPanel();
 
 		Button taskButton = new Button("SUP SUP", new ClickHandler() {
 		        public void onClick(ClickEvent event) {
 		          // Reposition the popup relative to the button
 		          Widget source = (Widget) event.getSource();
 		          int left = source.getAbsoluteLeft() + 10;
 		          int top = source.getAbsoluteTop() + 10;
 		          simplePopup.setPopupPosition(left, top);
 
 		          // Show the popup
 		          simplePopup.show();
 		        }
 		      });
 		if (task.getDescription().length() < maxButtonTextLength)
 			taskButton.setText(task.getDescription());
 		else
 			taskButton.setText(task.getDescription().substring(0, maxButtonTextLength));
 		taskButton.setWidth("80px");
 		taskRowPanel.add(taskButton);
 
 		final PushButton taskStatusCompleteButton = new PushButton(new Image(
 				"resources/complete.png"));
 		final PushButton taskStatusIncompleteButton = new PushButton(new Image(
 				"resources/incomplete.png"));
 
 		taskStatusCompleteButton.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 
 				taskManagementService.updateTaskStatus(currentTaskList, task, false,
 						
 						new AsyncCallback<Void>() {
 
 							@Override
 							public void onFailure(Throwable caught) {
 								Window.alert("Update of tasks failed: " + caught.getMessage());
 							}
 
 							@Override
 							public void onSuccess(Void ignore) {
 								loadTasksForDay();
 							}
 						});
 			}
 		});
 		taskStatusIncompleteButton.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 
 				taskManagementService.updateTaskStatus(currentTaskList, task, true,
 						new AsyncCallback<Void>() {
 
 							@Override
 							public void onFailure(Throwable caught) {
 								Window.alert("Update of tasks failed: " + caught.getMessage());
 							}
 
 							@Override
 							public void onSuccess(Void ignore) {
 								loadTasksForDay();
 							}
 						});
 			}
 		});
 		taskStatusCompleteButton.setVisible(task.isCompleted());
 		taskStatusIncompleteButton.setVisible(!task.isCompleted());
 
 		taskRowPanel.add(taskStatusCompleteButton);
 		taskRowPanel.add(taskStatusIncompleteButton);
 
 		tasksHolderPanel.add(taskRowPanel);
 		
 	}
 }
