 /*---
    Copyright 2006-2007 Visual Systems Corporation.
    http://www.vscorp.com
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
         http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 ---*/
 package wicket.contrib.webbeans.containers;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import wicket.AttributeModifier;
 import wicket.Component;
 import wicket.contrib.webbeans.actions.BeanActionButton;
 import wicket.contrib.webbeans.fields.LabeledField;
 import wicket.contrib.webbeans.fields.UnlabeledField;
 import wicket.contrib.webbeans.model.BeanMetaData;
 import wicket.contrib.webbeans.model.ElementMetaData;
 import wicket.contrib.webbeans.model.TabMetaData;
 import wicket.markup.ComponentTag;
 import wicket.markup.html.form.Form;
 import wicket.markup.html.list.ListItem;
 import wicket.markup.html.list.ListView;
 import wicket.markup.html.panel.Panel;
 import wicket.model.IModel;
 import wicket.model.Model;
 
 /**
  * A panel for generically displaying Java Beans in a grid-style layout.
  * The Bean config may specify the number of columns as "cols". The default is 3.
  * Elements within the grid may specify a config parameter of "colspan" which indicates the
  * number of columns to span in the grid. 
  * These parameters, along with EMPTY fields, allow for flexible layout.
  * 
  * @author Dan Syrstad
  */
 public class BeanGridPanel extends Panel
 {
     public static final String PARAM_COLSPAN = "colspan";
     public static final String PARAM_COLS = "cols";
 
     private static final long serialVersionUID = -2149828837634944417L;
 
     private Object bean; 
     private BeanMetaData beanMetaData;
     private TabMetaData tabMetaData;
     private boolean showLabels;
     private int columns;
 
     /**
      * Construct a new BeanGridPanel.
      *
      * @param id the Wicket id for the panel.
      * @param bean the bean to be displayed. This may be an IModel or regular bean object.
      * @param beanMetaData the meta data for the bean
      */
     public BeanGridPanel(String id, final Object bean, final BeanMetaData beanMetaData)
     {
         this(id, bean, beanMetaData, null);
     }
 
     /**
      * Construct a new BeanGridPanel.
      *
      * @param id the Wicket id for the panel.
      * @param bean the bean to be displayed. This may be an IModel or regular bean object.
      * @param beanMetaData the meta data for the bean
      * @param groupMetaData the tab to be displayed. If this is null, all displayed properties 
      *  for the bean will be displayed.
      */
     public BeanGridPanel(String id, final Object bean, final BeanMetaData beanMetaData, TabMetaData groupMetaData)
     {
         this(id, bean, beanMetaData, groupMetaData, true);
     }
     
     /**
      * Construct a new BeanGridPanel.
      *
      * @param id the Wicket id for the panel.
      * @param bean the bean to be displayed. This may be an IModel or regular bean object.
      * @param beanMetaData the meta data for the bean
      * @param tabMetaData the tab to be displayed. If this is null, all displayed properties 
      *  for the bean will be displayed.
      * @param showLabels if true, property labels will be displayed, otherwise they won't. 
      */
     public BeanGridPanel(String id, final Object bean, final BeanMetaData beanMetaData, TabMetaData tabMetaData, 
                 final boolean showLabels)
     {
         super(id);
 
         this.bean = bean;
         this.beanMetaData = beanMetaData;
         this.tabMetaData = tabMetaData;
         this.showLabels = showLabels;
 
         List<ElementMetaData> displayedProperties;
         if (tabMetaData == null) {
             displayedProperties = beanMetaData.getDisplayedElements();
         }
         else {
             displayedProperties = beanMetaData.getTabElements(tabMetaData);
         }
         
         // Get Number of rows from config
         //Properties config = beanMetaData.getParameters();
         columns = beanMetaData.getIntParameter(PARAM_COLS, 3);
         if (columns < 1) {
             throw new RuntimeException("Invalid columns config value: " + columns);
         }
         
         // Break out the rows and columns ahead of time.
         List<List<ElementMetaData>> rowsAndCols = new ArrayList<List<ElementMetaData>>();
         int colPos = 0;
         List<ElementMetaData> currRow = null;
         for (ElementMetaData element : displayedProperties) {
             int colspan = element.getIntParameter(PARAM_COLSPAN, 1);
             if (colspan < 1 || colspan > columns) { 
                 throw new RuntimeException("Invalid colspan parameter value: " + colspan);
             }
 
             // If colspan > number of columns left, start a new row.
             if ((colPos + colspan) > columns) {
                 colPos = 0;
             }
             
             if (colPos == 0) {
                 currRow = new ArrayList<ElementMetaData>();
                 rowsAndCols.add(currRow);
             }
 
             currRow.add(element);
             colPos += colspan;
             if (colPos >= columns) {
                 colPos = 0;
             }
         }
         
         Model propModel = new Model((Serializable)rowsAndCols);
         add( new RowListView("r", propModel) );
     }
 
     @Override
     public void detachModels()
     {
         super.detachModels();
         if (bean instanceof IModel) {
             ((IModel)bean).detach();
         }
     }
 
     @Override
     protected void onComponentTag(ComponentTag tag)
     {
         super.onComponentTag(tag);
         beanMetaData.warnIfAnyParameterNotConsumed(tabMetaData);
     }
     
     private final class RowListView extends ListView
     {
         
         RowListView(String id, IModel model)
         {
             super(id, model);
         }
 
         protected void populateItem(ListItem item)
         {
             List<ElementMetaData> columns = (List<ElementMetaData>)item.getModelObject();
             
             item.add( new ColListView("c", new Model((Serializable)columns)));
         }
     }
 
     private final class ColListView extends ListView
     {
         ColListView(String id, IModel model)
         {
             super(id, model);
         }
 
         protected void populateItem(ListItem item)
         {
             ElementMetaData element = (ElementMetaData)item.getModelObject();
             int colspan = element.getIntParameter(PARAM_COLSPAN, 1);
             
             Component component;
             if (element.isAction()) {
                 Form form = (Form)findParent(Form.class);
                 component = new BeanActionButton("c", element, form, bean);
             }
             else {
                 component = beanMetaData.getComponentRegistry().getComponent(bean, "c", element);
                 if (!(component instanceof UnlabeledField) && showLabels) {
                     component = new LabeledField("c", element.getLabelComponent("l"), component);
                 }
             }
 
             item.add( new AttributeModifier(PARAM_COLSPAN, true, new Model(String.valueOf(colspan))) );
             int pct100 = (colspan * 10000) / columns;
             String width = "width: " + (pct100 / 100) + "." + (pct100 % 100) + "%;";
             item.add( new AttributeModifier("style", true, new Model(width)) );
             item.add(component);
         }
     }
 }
