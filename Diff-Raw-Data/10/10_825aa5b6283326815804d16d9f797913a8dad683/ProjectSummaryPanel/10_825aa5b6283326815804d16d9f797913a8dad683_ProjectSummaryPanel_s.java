 package com.media2359.euphoria.view.client.project;
 
 import java.util.List;

import org.apache.log4j.Logger;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Composite;
 import com.media2359.euphoria.view.message.project.Project;
 import com.media2359.euphoria.view.message.project.ProjectListRequest;
 import com.media2359.euphoria.view.message.project.ProjectListResponse;
 import com.media2359.euphoria.view.server.project.ProjectService;
 import com.media2359.euphoria.view.server.project.ProjectServiceAsync;
 import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
 import com.sencha.gxt.widget.core.client.box.AutoProgressMessageBox;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
 
 public class ProjectSummaryPanel extends Composite {
 	interface ProjectSummaryUiBinder extends
 			UiBinder<VerticalLayoutContainer, ProjectSummaryPanel> {
 	}
 
 	ProjectSummaryUiBinder uiBinder = GWT.create(ProjectSummaryUiBinder.class);
 
 	private ProjectServiceAsync projectService = GWT
 			.create(ProjectService.class);
 
 	@UiField
 	BasicGrid basicGrid;
 	private Logger log = Logger.getLogger("EuphoriaLogger");
 
 	/**
 	 * This is the entry point method.
 	 */
 	public ProjectSummaryPanel() {
 		initWidget(uiBinder.createAndBindUi(this));
 
 		/**
 		 * Fetch the data when this panel is shown
 		 */
 		loadData();
 	}
 
 	private void loadData() {
 		final AutoProgressMessageBox messageBox = new AutoProgressMessageBox(
 				"Progress", "Loading data. Please wait...");
 
 		final AsyncCallback<ProjectListResponse> callback = new AsyncCallback<ProjectListResponse>() {
 
 			public void onFailure(Throwable caught) {
 				messageBox.hide();
 				AlertMessageBox alert = new AlertMessageBox("Error",
 						caught.getMessage());
 				alert.show();
 			}
 
 			public void onSuccess(ProjectListResponse result) {
 				messageBox.hide();
 				List<Project> projects = result.getProjects();
 
 				if ((projects != null) && (!projects.isEmpty())) {
 					// Now populate GXT Grid
 					basicGrid.populateData(projects);
 				} else {
 					// Remove all items
 					basicGrid.clear();
 				}
 			}
 		};
 		log.info("Getting all projects in ProjectSummaryPanel");
 		projectService.getAllProjects(new ProjectListRequest(), callback);
 		messageBox.auto();
 		messageBox.show();
 
 	}
 }
