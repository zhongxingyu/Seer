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
 
 package org.openengsb.openticket.ui.web;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.openengsb.core.common.taskbox.TaskboxService;
 import org.openengsb.core.common.workflow.WorkflowException;
 import org.openengsb.core.common.workflow.WorkflowService;
 
 @AuthorizeInstantiation("CASEWORKER")
 public class TaskboxDemo extends BasePage {
    @SpringBean(name = "taskboxService")
     private TaskboxService taskboxService;
     @SpringBean
     private WorkflowService workflowService;
 
     public int getAmount() {
         return taskboxService.getOpenTasks().size();
     }
 
     public TaskboxDemo() {
         final Label amount = new Label("amount", new PropertyModel<Integer>(this, "amount"));
         amount.setOutputMarkupId(true);
         add(amount);
 
         final FeedbackPanel feedback = new FeedbackPanel("feedback");
         feedback.setOutputMarkupId(true);
         add(feedback);
 
         Form<Object> form = new Form<Object>("form");
         form.setOutputMarkupId(true);
         add(form);
 
         form.add(new AjaxButton("startFlow", form)
         {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 try {
                     workflowService.startFlow("TaskDemoWorkflow");
                     info("Workflow started!");
                 } catch (WorkflowException e) {
                     info(e.getMessage());
                 }
                 target.addComponent(feedback);
                 target.addComponent(amount);
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 target.addComponent(feedback);
             }
         });
 
         form.add(new AjaxButton("finishFlow", form)
         {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 try {
                     taskboxService.finishTask(taskboxService.getOpenTasks().get(0));
                     info("Workflow finished!");
                 } catch (WorkflowException e) {
                     info(e.getMessage());
                 } catch (IndexOutOfBoundsException e) {
                     info("No workflow available");
                 }
                 target.addComponent(feedback);
                 target.addComponent(amount);
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 target.addComponent(feedback);
             }
         });
     }
 }
