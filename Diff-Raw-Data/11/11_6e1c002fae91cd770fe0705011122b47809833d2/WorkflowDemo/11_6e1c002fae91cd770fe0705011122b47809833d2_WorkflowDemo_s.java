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
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.model.StringResourceModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.openengsb.core.taskbox.TaskboxService;
 import org.openengsb.core.taskbox.model.Ticket;
 
 @AuthorizeInstantiation("CASEWORKER")
 public class WorkflowDemo extends BasePage {
     @SpringBean
     private TaskboxService service;
     
     public WorkflowDemo() {
         try {
         	TicketService ticketService = new TicketServiceImpl();
         	Ticket ticket = ticketService.createEmptyTicket();
         	
         	ticket.setType("reviewer");
         	service.startWorkflow("ticket", ticket);
             add(new Label("testoutput", service.getWorkflowMessage()));
         	
             ticket = ticketService.createEmptyTicket();
             ticket.setType("developer");
         	service.startWorkflow("ticket", ticket);
             add(new Label("testoutput2", service.getWorkflowMessage()));
             
         } catch (Exception e) {
         	StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw);
             e.printStackTrace(pw);
         	
         	add(new Label("testoutput", new StringResourceModel("error", this, null).getString() + 
             		"\n" + e.getMessage() + "\n\nStacktrace:\n" + sw.toString()));
             add(new Label("testoutput2", ""));
         }
     }
 }
