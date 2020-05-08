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
 package org.jboss.as.console.client.teiid;
 
 import org.jboss.as.console.client.shared.dispatch.DispatchAsync;
 import org.jboss.as.console.client.shared.viewframework.AbstractEntityView;
 import org.jboss.as.console.client.shared.viewframework.Columns;
 import org.jboss.as.console.client.shared.viewframework.EntityToDmrBridge;
 import org.jboss.as.console.client.shared.viewframework.EntityToDmrBridgeImpl;
 import org.jboss.as.console.client.shared.viewframework.FrameworkPresenter;
 import org.jboss.as.console.client.teiid.model.Transport;
 import org.jboss.as.console.client.widgets.forms.ApplicationMetaData;
 import org.jboss.ballroom.client.widgets.forms.Form;
 import org.jboss.ballroom.client.widgets.forms.FormAdapter;
 import org.jboss.ballroom.client.widgets.tables.DefaultCellTable;
 
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 
 public class TransportView extends AbstractEntityView<Transport> implements TransportPresenter.MyView, FrameworkPresenter {
     private final EntityToDmrBridgeImpl<Transport> bridge;
     
     @Inject
     public TransportView(ApplicationMetaData propertyMetaData, DispatchAsync dispatcher) {
         super(Transport.class, propertyMetaData);
         bridge = new EntityToDmrBridgeImpl<Transport>(propertyMetaData, Transport.class, this, dispatcher);
     }
 
     @Override
     public Widget createWidget() {
        return createEmbeddableWidget();
     }
 
     @Override
     public EntityToDmrBridge<Transport> getEntityBridge() {
         return bridge;
     }
 
     @Override
     protected DefaultCellTable<Transport> makeEntityTable() {
         DefaultCellTable<Transport> table = new DefaultCellTable<Transport>(5);
         table.addColumn(new Columns.NameColumn(), Columns.NameColumn.LABEL);
         table.setVisible(true);
         return table;
     }
     
     @Override
     protected FormAdapter<Transport> makeAddEntityForm() {
         Form<Transport> form = new Form<Transport>(Transport.class);
         form.setNumColumns(1);
 
         form.setFields(formMetaData.findAttribute("name").getFormItemForAdd(),
                        formMetaData.findAttribute("protocol").getFormItemForAdd(),
                        formMetaData.findAttribute("socketBinding").getFormItemForAdd(),
                        formMetaData.findAttribute("maxSocketThreads").getFormItemForAdd(),
                        formMetaData.findAttribute("authenticationDomain").getFormItemForAdd(),
                        formMetaData.findAttribute("maxSessionsAllowed").getFormItemForAdd(),
                        formMetaData.findAttribute("sessionExpirationTime").getFormItemForAdd(),
                        formMetaData.findAttribute("sSLEnabled").getFormItemForAdd(),
                        formMetaData.findAttribute("sSLMode").getFormItemForAdd(),
                        formMetaData.findAttribute("sSLAuthMode").getFormItemForAdd(),
                        formMetaData.findAttribute("sSLProtocol").getFormItemForAdd(),
                        formMetaData.findAttribute("keyManagementAlgorithm").getFormItemForAdd(),
                        formMetaData.findAttribute("enabledCipherSuites").getFormItemForAdd(),
                        formMetaData.findAttribute("keystoreName").getFormItemForAdd(),
                        formMetaData.findAttribute("keystorePassword").getFormItemForAdd(),
                        formMetaData.findAttribute("keystoreType").getFormItemForAdd(),
                        formMetaData.findAttribute("truststoreName").getFormItemForAdd(),
                        formMetaData.findAttribute("truststorePassword").getFormItemForAdd());
         return form;
     }  
     
     @Override
     protected String getEntityDisplayName() {
         return "Transports";
     }
 }
