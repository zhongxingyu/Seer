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
 
 package org.icefaces.samples.showcase.example.ace.chart;
 
 import org.icefaces.ace.component.chart.Axis;
 import org.icefaces.ace.component.chart.AxisType;
 import org.icefaces.ace.event.PointValueChangeEvent;
 import org.icefaces.ace.event.SelectEvent;
 import org.icefaces.ace.event.SeriesSelectionEvent;
 import org.icefaces.ace.event.UnselectEvent;
 import org.icefaces.ace.model.chart.CartesianSeries;
 import org.icefaces.ace.model.chart.DragConstraintAxis;
 import org.icefaces.ace.model.table.RowStateMap;
 import org.icefaces.util.JavaScriptRunner;
 import org.icefaces.samples.showcase.metadata.annotation.*;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.CustomScoped;
 import javax.faces.bean.ManagedBean;
 import javax.faces.context.FacesContext;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 @ComponentExample(
         parent = ChartBean.BEAN_NAME,
         title = "example.ace.chart.dynamic.title",
         description = "example.ace.chart.dynamic.description",
         example = "/resources/examples/ace/chart/chartDynamic.xhtml"
 )
 @ExampleResources(
         resources ={
             // xhtml
             @ExampleResource(type = ResourceType.xhtml,
                     title="ChartDynamic.xhtml",
                     resource = "/resources/examples/ace/chart/chartDynamic.xhtml"),
             // Java Source
             @ExampleResource(type = ResourceType.java,
                     title="ChartDynamicBean.java",
                     resource = "/WEB-INF/classes/org/icefaces/samples/showcase/example/ace/chart/ChartDynamicBean.java")
         }
 )
 @ManagedBean(name= ChartDynamicBean.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class ChartDynamicBean extends ComponentExampleImpl<ChartDynamicBean> implements Serializable
 {
     public static final String BEAN_NAME = "chartDynamicBean";
     
    private RowStateMap rowStateMap = new RowStateMap();
     private Integer[][] tableData = new Integer[][] {
             {2,3,1,4},
             {5,6,8,7},
             {10,9,12,11},
     };
     private List<CartesianSeries> lineData = new ArrayList<CartesianSeries>() {{
         add(new CartesianSeries() {{
             setType(CartesianType.LINE);
             setLabel("Plot Data");
             setDragable(true);
             setDragConstraintAxis(DragConstraintAxis.Y);
         }});
     }};
     private Axis tableDemoAxis = new Axis() {{
         setType(AxisType.CATEGORY);
         setLabel("Letter Axis");
         setTicks(new String[]{"A", "B", "C", "D"});
     }};
     
     public ChartDynamicBean() {
         super(ChartDynamicBean.class);
     }
     
     public Integer[][] getTableData() {
         return tableData;
     }
 
     public void setTableData(Integer[][] data) {
         this.tableData = data;
     }
     
     public RowStateMap getRowStateMap() {
         return rowStateMap;
     }
 
     public void setRowStateMap(RowStateMap rowStateMap) {
         this.rowStateMap = rowStateMap;
     }
     
     public List<CartesianSeries> getLineData() {
         return lineData;
     }
 
     public void setLineData(List<CartesianSeries> lineData) {
         this.lineData = lineData;
     }
     
     public Axis getTableDemoAxis() {
         return tableDemoAxis;
     }
 
     public void setTableDemoAxis(Axis tableDemoAxis) {
         this.tableDemoAxis = tableDemoAxis;
     }
     
     @PostConstruct
     public void initMetaData() {
         super.initMetaData();

        setupSelection();
    }

    public void setupSelection() {
        rowStateMap.get(tableData[0]).setSelected(true);

        CartesianSeries s = lineData.get(0);
        s.add("A", tableData[0][0]);
        s.add("B", tableData[0][1]);
        s.add("C", tableData[0][2]);
        s.add("D", tableData[0][3]);
     }
     
     public void pointChange(PointValueChangeEvent event) {
         JavaScriptRunner.runScript(FacesContext.getCurrentInstance(),
                 "ice.ace.jq('.ui-datatable-data tr.ui-selected td:eq("+event.getPointIndex()+")').effect('pulsate', {}, 500);");
 
         for (Object o : rowStateMap.getSelected()) {
             Integer[] values = (Integer[]) o;
             values[event.getPointIndex()] = ((Double)((Object[])event.getNewValue())[1]).intValue();
         }
     }
     
     public void redrawChartListener(SelectEvent e) {
         CartesianSeries s = lineData.get(0);
         s.clear();
         Integer[] indicies = (Integer[]) e.getObject();
         s.add("A", indicies[0]);
         s.add("B", indicies[1]);
         s.add("C", indicies[2]);
         s.add("D", indicies[3]);
     }
 
     public void clearChartListener(UnselectEvent e) {
         CartesianSeries s = lineData.get(0);
         s.clear();
     }
 }
