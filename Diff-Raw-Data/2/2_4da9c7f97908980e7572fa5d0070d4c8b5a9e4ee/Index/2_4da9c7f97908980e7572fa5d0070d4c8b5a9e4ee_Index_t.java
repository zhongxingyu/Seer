 /**
  * Copyright 2010 OpenEngSB Division, Vienna University of Technology
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openengsb.opencit.ui.web;
 
 import java.util.List;
 
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.StringResourceModel;
 import org.apache.wicket.resource.ContextRelativeResource;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.openengsb.opencit.core.projectmanager.ProjectManager;
 import org.openengsb.opencit.core.projectmanager.model.Project;
 
 public class Index extends BasePage {
 
     @SpringBean
     private ProjectManager projectManager;
 
     public Index() {
         WebMarkupContainer connectingServicePanel = new WebMarkupContainer("projectlistPanel");
         connectingServicePanel.setOutputMarkupId(true);
 
         @SuppressWarnings("serial")
         IModel<List<Project>> projectsModel = new LoadableDetachableModel<List<Project>>() {
             @Override
             protected List<Project> load() {
                 List<Project> projects = projectManager.getAllProjects();
                 return projects;
             }
         };
 
         Label noConServices =
            new Label("noProjects", new StringResourceModel("noProjectsAvailable", this, null));
         noConServices.setVisible(false);
         noConServices.setOutputMarkupId(true);
 
         connectingServicePanel.add(createServiceListView(projectsModel, "projectlist"));
         connectingServicePanel.add(noConServices);
 
         add(connectingServicePanel);
         if (projectsModel.getObject().isEmpty()) {
             noConServices.setVisible(true);
         }
     }
 
     @SuppressWarnings("serial")
     private ListView<Project> createServiceListView(IModel<List<Project>> projectsModel,
             String id) {
         return new ListView<Project>(id, projectsModel) {
 
             @Override
             protected void populateItem(ListItem<Project> item) {
                 Project project = item.getModelObject();
                 item.add(new Label("project.name", project.getId()));
                 String imageName = getImage(project);
                 item.add(new Image("project.state", new ContextRelativeResource(imageName)));
             }
 
             private String getImage(Project project) {
                 if (project.getState() == null) {
                     return "images/traffic_light_green.png";
                 }
                 switch (project.getState()) {
                     case OK:
                         return "images/traffic_light_green.png";
                     case IN_PROGRESS:
                         return "images/traffic_light_yellow.png";
                     case FAILURE:
                         return "images/traffic_light_red.png";
                     default:
                         return "images/traffic_light_green.png";
                 }
             }
         };
     }
 }
