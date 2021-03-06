 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
  * as indicated by the @author tags. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 
 package org.jboss.as.console.client.shared.subsys.messaging;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import org.jboss.as.console.client.shared.subsys.messaging.model.JMSEndpoint;
 import org.jboss.as.console.client.widgets.forms.Form;
 import org.jboss.as.console.client.widgets.forms.TextItem;
 import org.jboss.as.console.client.widgets.tools.ToolButton;
 import org.jboss.as.console.client.widgets.tools.ToolStrip;
 
 import java.util.List;
 
 /**
  * @author Heiko Braun
  * @date 5/10/11
  */
 public class TopicList {
 
     private EndpointTable table;
     private JMSPresenter presenter;
     private ToolButton edit;
 
     private Form<JMSEndpoint> form;
 
     public TopicList(JMSPresenter presenter) {
         this.presenter = presenter;
     }
 
     Widget asWidget() {
         VerticalPanel layout = new VerticalPanel();
 
         ToolStrip toolStrip = new ToolStrip();
         toolStrip.getElement().setAttribute("style", "margin-bottom:10px;");
 
         edit = new ToolButton("Edit", new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
 
                 if(edit.getText().equals("Edit"))
                     presenter.onEditTopic();
                 else
                     presenter.onSaveTopic(form.getEditedEntity().getName(), form.getChangedValues());
             }
         });
 
         //toolStrip.addToolButton(edit);
 
         toolStrip.addToolButton(new ToolButton("Delete", new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 presenter.onDeleteTopic(form.getEditedEntity());
             }
         }));
 
 
         toolStrip.addToolButtonRight(new ToolButton("Add", new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 presenter.launchNewTopicDialogue();
             }
         }));
 
 
         layout.add(toolStrip);
 
         // -----
         table = new EndpointTable();
 
         layout.add(table);
 
 
         // -----
 
         form = new Form(JMSEndpoint.class);
         form.setNumColumns(2);
 
         TextItem name = new TextItem("name", "Name");
         TextItem jndi = new TextItem("jndiName", "JNDI");
 
         form.setFields(name, jndi);
 
         layout.add(form.asWidget());
 
         form.bind(table);
 
         return layout;
     }
 
     public void setTopics(List<JMSEndpoint> topics) {
         table.setRowData(0, topics);
 
         if(!topics.isEmpty())
             table.getSelectionModel().setSelected(topics.get(0), true);
     }
 }
