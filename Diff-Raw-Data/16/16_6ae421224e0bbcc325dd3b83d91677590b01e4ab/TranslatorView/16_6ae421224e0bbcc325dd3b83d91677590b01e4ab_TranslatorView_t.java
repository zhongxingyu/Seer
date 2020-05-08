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
 import org.jboss.as.console.client.shared.viewframework.Columns.NameColumn;
 import org.jboss.as.console.client.shared.viewframework.EntityToDmrBridge;
 import org.jboss.as.console.client.shared.viewframework.EntityToDmrBridgeImpl;
 import org.jboss.as.console.client.shared.viewframework.FrameworkPresenter;
 import org.jboss.as.console.client.teiid.model.Translator;
 import org.jboss.as.console.client.widgets.forms.ApplicationMetaData;
 import org.jboss.ballroom.client.widgets.forms.Form;
 import org.jboss.ballroom.client.widgets.forms.FormAdapter;
 import org.jboss.ballroom.client.widgets.tables.DefaultCellTable;
 
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 
 public class TranslatorView extends AbstractEntityView<Translator> implements TranslatorPresenter.MyView, FrameworkPresenter {
     private final EntityToDmrBridgeImpl<Translator> bridge;
     
     @Inject
     public TranslatorView(ApplicationMetaData propertyMetaData, DispatchAsync dispatcher) {
         super(Translator.class, propertyMetaData);
         bridge = new EntityToDmrBridgeImpl<Translator>(propertyMetaData, Translator.class, this, dispatcher);
     }
 
     @Override
     public Widget createWidget() {
        return super.createWidget();
     }
 
     @Override
     public EntityToDmrBridge<Translator> getEntityBridge() {
         return bridge;
     }
 
     @Override
     protected DefaultCellTable<Translator> makeEntityTable() {
         DefaultCellTable<Translator> table = new DefaultCellTable<Translator>(4);
 
         table.addColumn(new NameColumn(), NameColumn.LABEL);
 
         TextColumn<Translator> cacheContainerColumn = new TextColumn<Translator>() {
             @Override
             public String getValue(Translator record) {
                 return record.getModuleName();
             }
         };
         table.addColumn(cacheContainerColumn, "Module Name");
 
         return table;
     }
 
     @Override
     protected FormAdapter<Translator> makeAddEntityForm() {
         Form<Translator> form = new Form<Translator>(beanType);
         form.setNumColumns(1);
 
         form.setFields(formMetaData.findAttribute("name").getFormItemForAdd(),
                        formMetaData.findAttribute("moduleName").getFormItemForAdd());
         return form;
     }  
     
     @Override
     protected String getEntityDisplayName() {
         return "Translators";
     }
 }
