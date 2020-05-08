 package org.motechproject.carereporting.domain.types;
 
 public enum ReportType {
    BarChart(1), LineChart(2), PieChart(3);
 
     private Integer value;
 
     ReportType(Integer value) {
         this.value = value;
     }
 
     public static ReportType fromString(String value) {
         switch(value) {
             case "bar chart":
                 return BarChart;
             case "pie chart":
                 return PieChart;
             case "line chart":
             default:
                 return LineChart;
         }
     }
 
     public Integer getValue() {
         return value;
     }
 }
