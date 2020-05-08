 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.samples.showcase.example.ace.dataTable;
 
 import org.icefaces.ace.component.datatable.DataTable;
 import org.icefaces.samples.showcase.dataGenerators.utilityClasses.DataTableData;
 import org.icefaces.samples.showcase.example.compat.dataTable.Car;
 import org.icefaces.samples.showcase.metadata.annotation.ComponentExample;
 import org.icefaces.samples.showcase.metadata.annotation.ExampleResource;
 import org.icefaces.samples.showcase.metadata.annotation.ExampleResources;
 import org.icefaces.samples.showcase.metadata.annotation.ResourceType;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 
import javax.faces.application.Application;
 import javax.faces.bean.CustomScoped;
 import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 @ComponentExample(
         parent = DataTableBean.BEAN_NAME,
         title = "example.ace.dataTable.grouping.title",
         description = "example.ace.dataTable.grouping.description",
         example = "/resources/examples/ace/dataTable/dataTableGrouping.xhtml"
 )
 @ExampleResources(
         resources ={
             // xhtml
             @ExampleResource(type = ResourceType.xhtml,
                     title="dataTableGrouping.xhtml",
                     resource = "/resources/examples/ace/dataTable/dataTableGrouping.xhtml"),
             // Java Source
             @ExampleResource(type = ResourceType.java,
                     title="DataTableGrouping.java",
                     resource = "/WEB-INF/classes/org/icefaces/samples/showcase"+
                     "/example/ace/dataTable/DataTableGrouping.java")
         }
 )
 @ManagedBean(name= DataTableGrouping.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class DataTableGrouping extends ComponentExampleImpl<DataTableGrouping> implements Serializable {
     public static final String BEAN_NAME = "dataTableGrouping";
     private DataTable table;
     private List<Car> carsData;
     /////////////---- CONSTRUCTOR BEGIN
     public DataTableGrouping() {
         super(DataTableGrouping.class);
         carsData = new ArrayList<Car>(DataTableData.getDefaultData());
     }
     
     /////////////---- GETTERS & SETTERS BEGIN
     public List<Car> getCarsData() { return carsData; }
     public void setCarsData(List<Car> carsData) { this.carsData = carsData; }
     public DataTable getTable() { return table; }
     public void setTable(DataTable table) { this.table = table; }
     
     /////////////---- METHOD INVOCATION VIA VIEW EL
     public double groupTotal(String groupProperty, String valueProperty, Object i) {
         // Fix for bugged method invocation in early TC7 releases
         int index = (Integer) i;
 
         double total = 0;
         boolean nextRowInGroup = false;
         FacesContext context = FacesContext.getCurrentInstance();
         Application application = context.getApplication();
 
         int currentIndex = table.getRowIndex();
         table.setRowIndex(index);
 
         Object groupValue = application.evaluateExpressionGet(context, "#{"+groupProperty+"}", Object.class);
 
         do {
             total += application.evaluateExpressionGet(context, "#{"+valueProperty+"}", Double.class);
 
             table.setRowIndex(--index);
 
             Object obj = application.evaluateExpressionGet(context, "#{"+groupProperty+"}", Object.class);
             if (table.isRowAvailable() && groupValue.equals(obj))
                 nextRowInGroup = true;
             else
                 nextRowInGroup = false;
         } while (nextRowInGroup);
 
         table.setRowIndex(currentIndex);
         return 0.0;
     }
 }
