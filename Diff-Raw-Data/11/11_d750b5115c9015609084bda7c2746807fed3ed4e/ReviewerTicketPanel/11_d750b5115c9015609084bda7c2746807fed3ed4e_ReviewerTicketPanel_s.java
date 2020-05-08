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
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.CheckBox;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.FormComponent;
 import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.ResourceModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.validation.validator.StringValidator;
 import org.openengsb.core.common.taskbox.TaskboxService;
 import org.openengsb.core.common.taskbox.model.Task;
 import org.openengsb.core.common.workflow.WorkflowException;
 import org.openengsb.openticket.model.ReviewerTicket;
 
 public class ReviewerTicketPanel extends Panel {
 
     @SpringBean(name = "taskboxService")
     private TaskboxService service;
 
     private ReviewerTicket temp;
 
     public ReviewerTicketPanel(String id, Task task) {
         super(id);
 
         temp = new ReviewerTicket(task);
 
         final FeedbackPanel feedback = new FeedbackPanel("feedback");
         feedback.setOutputMarkupId(true);
         add(feedback);
 
         CompoundPropertyModel<ReviewerTicket> ticketModel = new CompoundPropertyModel<ReviewerTicket>(temp);
         Form<ReviewerTicket> form = new Form<ReviewerTicket>("editTicket", ticketModel);
         form.setOutputMarkupId(true);
         add(form);
 
         form.add(new Label("header-label-ticket", new ResourceModel("header.label.ticket")));
         form.add(new Label("header-label-reviewerticket", new ResourceModel("header.label.reviewerticket")));
 
         form = constituteReadOnlyFields(form);
         form = constituteEditableFields(form);
 
         AjaxButton saveButton = new AjaxButton("save", form) {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 form.remove("listContainer");
                 form.add(printTicketProperties());
 
                 form.setOutputMarkupId(true);
                 target.addComponent(form);
 
                 info(getLocalizer().getString("info.tempsaved", this));
                 target.addComponent(feedback);
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 target.addComponent(feedback);
             }
         };
         form.add(saveButton);
 
         form.add(new Button("reset"));
 
         form.add(new Label("finished-label", new ResourceModel("edit.label.finished.false")));
 
         AjaxButton closeButton = new AjaxButton("close", form) {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 try {
                     service.finishTask(temp);
 
                     info(getLocalizer().getString("info.finished", this));
                     target.addComponent(feedback);
 
                     this.setEnabled(false);
                     this.setVisible(false);
                     form.remove("close");
                     form.add(this);
 
                     form.setEnabled(false);
 
                     Label finished_label = new Label("finished-label", new ResourceModel("edit.label.finished.true"));
                     form.remove("finished-label");
                     form.add(finished_label);
 
                     form.remove("listContainer");
                     form.add(printTicketProperties());
 
                     form.setOutputMarkupId(true);
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
 
         form.add(closeButton);
         form.add(printTicketProperties());
     }
 
     @SuppressWarnings("unchecked")
     private WebMarkupContainer printTicketProperties() {
         ArrayList<String> ticket_properties = new ArrayList<String>();
         String propertyName;
         for (int i = 0; i < temp.propertyCount(); i++) {
             propertyName = new String(temp.propertyKeySet().toArray()[i].toString());
             ticket_properties.add(propertyName + " : " + temp.getProperty(propertyName));
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
 
     private Form<ReviewerTicket> constituteReadOnlyFields(Form<ReviewerTicket> form) {
 
         FormComponent<String> fcId = new TextField<String>("taskId");
         fcId.setEnabled(false);
         fcId.setLabel(new ResourceModel("edit.label.id"));
         form.add(fcId);
         form.add(new SimpleFormComponentLabel("edit-label-id", fcId));
 
         FormComponent<String> fcTime = new TextField<String>("taskCreationTimestamp");
         fcTime.setEnabled(false);
         fcTime.setLabel(new ResourceModel("edit.label.time"));
         form.add(fcTime);
         form.add(new SimpleFormComponentLabel("edit-label-time", fcTime));
 
         FormComponent<String> fcName = new TextField<String>("name");
         fcName.setEnabled(false);
         fcName.setLabel(new ResourceModel("edit.label.name"));
         form.add(fcName);
         form.add(new SimpleFormComponentLabel("edit-label-name", fcName));
 
         FormComponent<String> fcDesc = new TextField<String>("description");
         fcDesc.setEnabled(false);
         fcDesc.setLabel(new ResourceModel("edit.label.desc"));
         form.add(fcDesc);
         form.add(new SimpleFormComponentLabel("edit-label-desc", fcDesc));
 
         FormComponent<String> fcType = new TextField<String>("taskType");
         fcType.setEnabled(false);
         fcType.setLabel(new ResourceModel("edit.label.type"));
         form.add(fcType);
         form.add(new SimpleFormComponentLabel("edit-label-type", fcType));
 
         FormComponent<String> fcPriority = new TextField<String>("priority");
         fcPriority.setEnabled(false);
         fcPriority.setLabel(new ResourceModel("edit.label.priority"));
         form.add(fcPriority);
         form.add(new SimpleFormComponentLabel("edit-label-priority", fcPriority));
 
         FormComponent<String> fcCust = new TextField<String>("customer");
         fcCust.setEnabled(false);
         fcCust.setLabel(new ResourceModel("edit.label.customer"));
         form.add(fcCust);
         form.add(new SimpleFormComponentLabel("edit-label-customer", fcCust));
 
         FormComponent<String> fcEmail = new TextField<String>("contactEmailAddress");
         fcEmail.setEnabled(false);
         fcEmail.setLabel(new ResourceModel("edit.label.email"));
         form.add(fcEmail);
         form.add(new SimpleFormComponentLabel("edit-label-email", fcEmail));
 
         return form;
     }
 
     private Form<ReviewerTicket> constituteEditableFields(Form<ReviewerTicket> form) {
 
         CheckBox fcTR = new CheckBox("ticketResolved");
         fcTR.setLabel(new ResourceModel("edit.label.tr"));
         form.add(fcTR);
         form.add(new SimpleFormComponentLabel("edit-label-tr", fcTR));
 
         FormComponent<String> fcFeedback = new TextArea<String>("feedback");
         fcFeedback.setRequired(true);
         fcFeedback.add(StringValidator.maximumLength(1000));
         fcFeedback.setLabel(new ResourceModel("edit.label.feedback"));
         form.add(fcFeedback);
         form.add(new SimpleFormComponentLabel("edit-label-feedback", fcFeedback));
 
         return form;
     }
 }
