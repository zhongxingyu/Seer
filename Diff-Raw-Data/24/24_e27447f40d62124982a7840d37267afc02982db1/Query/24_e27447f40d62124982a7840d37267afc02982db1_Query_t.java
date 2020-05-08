 package org.motechproject.carereporting.xml.mapping;
 
 import javax.xml.bind.annotation.XmlElement;
 
 public class Query {
 
     private DwQuery dwQuery;
 
     private String indicatorName;
 
    public void setIndicatorName(String indicatorName) {
        this.indicatorName = indicatorName;
     }
 
     @XmlElement(name = "indicator-name")
     public String getIndicatorName() {
         return indicatorName;
     }
 
     @XmlElement(name = "dw-query")
     public DwQuery getDwQuery() {
         return dwQuery;
     }
 
     public void setDwQuery(DwQuery dwQuery) {
         this.dwQuery = dwQuery;
     }
 }
