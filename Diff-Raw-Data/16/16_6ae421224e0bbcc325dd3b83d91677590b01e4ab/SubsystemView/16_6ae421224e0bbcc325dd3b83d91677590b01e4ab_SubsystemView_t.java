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
 
 import java.util.EnumSet;
 
 import org.jboss.as.console.client.shared.dispatch.DispatchAsync;
 import org.jboss.as.console.client.shared.viewframework.AbstractEntityView;
 import org.jboss.as.console.client.shared.viewframework.EntityDetails;
 import org.jboss.as.console.client.shared.viewframework.EntityEditor;
 import org.jboss.as.console.client.shared.viewframework.EntityToDmrBridge;
 import org.jboss.as.console.client.shared.viewframework.FrameworkButton;
 import org.jboss.as.console.client.shared.viewframework.FrameworkPresenter;
 import org.jboss.as.console.client.shared.viewframework.SingleEntityToDmrBridgeImpl;
 import org.jboss.as.console.client.teiid.model.SubsystemConfiguration;
 import org.jboss.as.console.client.widgets.forms.ApplicationMetaData;
 import org.jboss.ballroom.client.widgets.forms.FormAdapter;
 import org.jboss.ballroom.client.widgets.tables.DefaultCellTable;
 
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 
 public class SubsystemView extends AbstractEntityView<SubsystemConfiguration> implements SubsystemPresenter.MyView, FrameworkPresenter {
     private final EntityToDmrBridge<SubsystemConfiguration> bridge;
 
     @Inject
     public SubsystemView(ApplicationMetaData propertyMetaData, DispatchAsync dispatcher) {
         super(SubsystemConfiguration.class, propertyMetaData, EnumSet.of(FrameworkButton.ADD, FrameworkButton.REMOVE));
         bridge = new SingleEntityToDmrBridgeImpl<SubsystemConfiguration>(propertyMetaData, SubsystemConfiguration.class, this, dispatcher);
     }
 
     @Override
     public Widget createWidget() {
        return super.createWidget();
     }
 
     @Override
     protected EntityEditor<SubsystemConfiguration> makeEntityEditor() {
         EntityDetails<SubsystemConfiguration> details = new EntityDetails<SubsystemConfiguration>(
                 this,
                 getEntityDisplayName(),
                 makeEditEntityDetailsForm(),
                 getAddress(),
                 hideButtons);
         return new EntityEditor<SubsystemConfiguration>(this, getEntityDisplayName(), null, makeEntityTable(), details, hideButtons);
     }
 
     @Override
     public EntityToDmrBridge<SubsystemConfiguration> getEntityBridge() {
         return bridge;
     }
 
     @Override
     protected DefaultCellTable<SubsystemConfiguration> makeEntityTable() {
         DefaultCellTable<SubsystemConfiguration> table = new DefaultCellTable<SubsystemConfiguration>(5);
         table.setVisible(false);
         return table;
     }
 
     @Override
     protected FormAdapter<SubsystemConfiguration> makeAddEntityForm() {
         return null;
     }
 
     @Override
     protected String getEntityDisplayName() {
         return "Query Engine";
     }
 }
