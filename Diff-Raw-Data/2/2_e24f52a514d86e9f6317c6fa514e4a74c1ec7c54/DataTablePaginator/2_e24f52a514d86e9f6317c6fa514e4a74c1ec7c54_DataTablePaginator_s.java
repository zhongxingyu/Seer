 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
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
 
 import org.icefaces.samples.showcase.metadata.annotation.*;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 import java.util.ArrayList;
 import java.util.List;
 import org.icefaces.samples.showcase.dataGenerators.utilityClasses.DataTableData;
 import org.icefaces.samples.showcase.example.compat.dataTable.Car;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.CustomScoped;
 import javax.faces.bean.ManagedBean;
 import java.io.Serializable;
 import javax.faces.model.SelectItem;
 
 @ComponentExample(
         parent = DataTableBean.BEAN_NAME,
         title = "example.ace.dataTable.paginator.title",
         description = "example.ace.dataTable.paginator.description",
         example = "/resources/examples/ace/dataTable/dataTablePaginator.xhtml"
 )
 @ExampleResources(
         resources ={
             // xhtml
             @ExampleResource(type = ResourceType.xhtml,
                     title="dataTablePaginator.xhtml",
                     resource = "/resources/examples/ace/dataTable/dataTablePaginator.xhtml"),
             // Java Source
             @ExampleResource(type = ResourceType.java,
                     title="DataTablePaginator.java",
                     resource = "/WEB-INF/classes/org/icefaces/samples/showcase"+
                     "/example/ace/dataTable/DataTablePaginator.java")
         }
 )
 @ManagedBean(name= DataTablePaginator.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class DataTablePaginator extends ComponentExampleImpl<DataTablePaginator> implements Serializable {
     public static final String BEAN_NAME = "dataTablePaginator";
     
     private static final SelectItem[] POSITION_AVAILABLE = { new SelectItem("bottom", "Bottom"),
                                                            new SelectItem("top", "Top"),
                                                            new SelectItem("both", "Both") };
     
     private boolean paginator = true;
     private String position = POSITION_AVAILABLE[0].getValue().toString();
     private List<Car> carsData;
     private int rows = 10;
     private int startPage = 1;
     
     /////////////---- CONSTRUCTOR BEGIN
     public DataTablePaginator() {
         super(DataTablePaginator.class);
         carsData = new ArrayList<Car>(DataTableData.getDefaultData());
     }
 
     @PostConstruct
     public void initMetaData() {
         super.initMetaData();
     }
 
     /////////////---- GETTERS & SETTERS BEGIN
     public boolean getPaginator() { return paginator; }
     public String getPosition() { return position; }
     public int getRows() { return rows; }
     public int getStartPage() { return startPage; }
     public SelectItem[] getPositionAvailable() { return POSITION_AVAILABLE; }
     public List<Car> getCarsData() { return carsData; }
     public int getStartPageMaximum() {
        return (int)Math.ceil(30.0/(double)rows);
     }
     
     public void setPaginator(boolean paginator) { this.paginator = paginator; }
     public void setPosition(String position) { this.position = position; }
     public void setRows(int rows) {
         this.rows = rows;
         setStartPage(getStartPage());
     }
     public void setStartPage(int startPage) {
         this.startPage = startPage;
         int maxPages = getStartPageMaximum();
         if( this.startPage < 1 ){
             this.startPage = 1;
         } else if( startPage > maxPages ){
             this.startPage = maxPages;
         }
     }
     public void setCarsData(List<Car> carsData) { this.carsData = carsData; }
 }
