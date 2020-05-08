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
 package org.openengsb.openticket.ui.web.panel;
 
 import java.util.ArrayList;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.openengsb.core.common.taskbox.TaskboxService;
 import org.openengsb.core.common.taskbox.model.Task;
 import org.openengsb.core.common.workflow.WorkflowException;
 
 public class TaskFinalViewPanel extends Panel {
 
     @SpringBean(name = "taskboxService")
     private TaskboxService service;
 
     private Task task;
 
     public TaskFinalViewPanel(String id, Task t) {
         super(id);
 
         this.task = t;
 
         final FeedbackPanel feedback = new FeedbackPanel("feedback");
         feedback.setOutputMarkupId(true);
         add(feedback);
 
         add(printTicketProperties(task));
 
         CompoundPropertyModel<Task> tm = new CompoundPropertyModel<Task>(task);
         Form<Task> form = new Form<Task>("taskForm", tm);
         form.setOutputMarkupId(true);
         add(form);
 
         AjaxButton okButton = new AjaxButton("ok", form) {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 try {
                     service.finishTask(task);
 
                     this.setEnabled(false);
                     form.remove("ok");
                     form.add(this);
                     target.addComponent(form);
                 } catch (WorkflowException e) {
                     e.printStackTrace();
                     error("Error: " + e.toString());
                 }
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 target.addComponent(feedback);
             }
         };
         form.add(okButton);
     }
 
     @SuppressWarnings("unchecked")
     private WebMarkupContainer printTicketProperties(Task task) {
         ArrayList<String> ticket_properties = new ArrayList<String>();
         String propertyName;
         for (int i = 0; i < task.propertyCount(); i++) {
             propertyName = new String(task.propertyKeySet().toArray()[i].toString());
             ticket_properties.add(propertyName + " : " + task.getProperty(propertyName));
         }
 
         ListView lv = new ListView("propertiesList", new ArrayList<String>(ticket_properties)) {
             @Override
             protected void populateItem(ListItem item) {
                 item.add(new Label("propertiesLabel", item.getModel()));
             }
         };
 
         WebMarkupContainer listContainer = new WebMarkupContainer("listContainer");
         listContainer.setOutputMarkupId(true);
         listContainer.add(lv);
 
         return listContainer;
     }
 }
