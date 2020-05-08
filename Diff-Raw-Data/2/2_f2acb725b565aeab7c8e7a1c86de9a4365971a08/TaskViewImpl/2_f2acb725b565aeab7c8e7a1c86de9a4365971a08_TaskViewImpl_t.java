 /*
  * Copyright 2011 Google Inc. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.oose.taskboard.client.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import de.oose.taskboard.client.presenter.TaskPresenter.Display;
 import de.oose.taskboard.shared.bo.TaskBO;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.Label;
 
 /**
  * Sample implementation of {@link TaskView}.
  */
 public class TaskViewImpl extends VerticalPanel implements Display {
 	private CellList<TaskBO> clPlanung;
 	private CellList<TaskBO> clArbeit;
 	private CellList<TaskBO> clAbnahme;
 	private CellList<TaskBO> clFertig;
 	private DecoratorPanel decoratorPanel;
 	private DecoratorPanel decoratorPanel_1;
 	private DecoratorPanel decoratorPanel_2;
 	private DecoratorPanel decoratorPanel_3;
 	private VerticalPanel verticalPanel;
 	private Label lblNewLabel;
 	private VerticalPanel vPArbeit;
 	private VerticalPanel vPDone;
 	private VerticalPanel vpReview;
 	private Label lblNewLabel_1;
 	private Label lblNewLabel_2;
 	private Label lblNewLabel_3;
 
 	public TaskViewImpl() {
 		setSize("800", "600");
 
 		
 
 		HorizontalPanel horizontalPanel = new HorizontalPanel();
 		add(horizontalPanel);
 		horizontalPanel.setSize("100%", "100%");
 										
 										verticalPanel = new VerticalPanel();
 										horizontalPanel.add(verticalPanel);
 										
										lblNewLabel = new Label("Planning");
 										lblNewLabel.setStyleName("bigFont");
 										verticalPanel.add(lblNewLabel);
 										lblNewLabel.setSize("210", "18");
 										
 										decoratorPanel = new DecoratorPanel();
 										verticalPanel.add(decoratorPanel);
 										
 												clPlanung = new CellList<TaskBO>(
 														new AbstractCell<TaskBO>() {
 															@Override
 															public void render(Context context, TaskBO value,
 																	SafeHtmlBuilder sb) {
 																sb.append(SafeHtmlUtils.fromString(value.getTitel()));
 															}
 														});
 												decoratorPanel.setWidget(clPlanung);
 												clPlanung.setSize("200px", "300px");
 												
 												vPArbeit = new VerticalPanel();
 												horizontalPanel.add(vPArbeit);
 												
 												lblNewLabel_1 = new Label("Work");
 												lblNewLabel_1.setStyleName("bigFont");
 												vPArbeit.add(lblNewLabel_1);
 												
 												decoratorPanel_1 = new DecoratorPanel();
 												vPArbeit.add(decoratorPanel_1);
 												
 														clArbeit = new CellList<TaskBO>(
 																new AbstractCell<TaskBO>() {
 																	@Override
 																	public void render(Context context, TaskBO value,
 																			SafeHtmlBuilder sb) {
 																		sb.append(SafeHtmlUtils.fromString(value.getTitel()));
 																	}
 																});
 														decoratorPanel_1.setWidget(clArbeit);
 														clArbeit.setSize("200px", "300px");
 												
 												vpReview = new VerticalPanel();
 												horizontalPanel.add(vpReview);
 												
 												lblNewLabel_3 = new Label("Review");
 												lblNewLabel_3.setStyleName("bigFont");
 												vpReview.add(lblNewLabel_3);
 												
 												decoratorPanel_3 = new DecoratorPanel();
 												vpReview.add(decoratorPanel_3);
 												
 														clAbnahme = new CellList<TaskBO>(
 																new AbstractCell<TaskBO>() {
 																	@Override
 																	public void render(Context context, TaskBO value,
 																			SafeHtmlBuilder sb) {
 																		sb.append(SafeHtmlUtils.fromString(value.getTitel()));
 																	}
 																});
 														decoratorPanel_3.setWidget(clAbnahme);
 														clAbnahme.setSize("200px", "300px");
 												
 												vPDone = new VerticalPanel();
 												horizontalPanel.add(vPDone);
 												
 												lblNewLabel_2 = new Label("Done");
 												lblNewLabel_2.setStyleName("bigFont");
 												vPDone.add(lblNewLabel_2);
 												
 												decoratorPanel_2 = new DecoratorPanel();
 												vPDone.add(decoratorPanel_2);
 												
 														clFertig = new CellList<TaskBO>(
 																new AbstractCell<TaskBO>() {
 																	@Override
 																	public void render(Context context, TaskBO value,
 																			SafeHtmlBuilder sb) {
 																		sb.append(SafeHtmlUtils.fromString(value.getTitel()));
 																	}
 																});
 														decoratorPanel_2.setWidget(clFertig);
 														clFertig.setSize("200px", "300px");
 		
 		Button btnNewTask = new Button("New button");
 		btnNewTask.setText("New Task");
 		add(btnNewTask);
 
 	}
 
 	@Override
 	public void setTaskList(List<TaskBO> tasks) {
 		clPlanung.setRowData(filter(tasks,"planung"));
 		clArbeit.setRowData(filter(tasks, "arbeit"));
 		clAbnahme.setRowData(filter(tasks, "abnahme"));
 		clFertig.setRowData(filter(tasks, "fertig"));
 	}
 	
 	private List<TaskBO> filter(List<TaskBO> original, String status) {
 		List<TaskBO> result = new ArrayList<TaskBO>();
 		for (TaskBO task : original) {
 			if (status.equalsIgnoreCase(task.getStatus())) {
 				result.add(task);
 			}
 		}
 		return result;
 	}
 
 }
