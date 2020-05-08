 package com.fing.pis.bizativiti.common.metamodel;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public abstract class MetamodelConnector extends MetamodelElement {
 
     private MetamodelFlowElement from;
     private MetamodelFlowElement to;
     private List<MetamodelCoordinate> coordinates;
     private String name;
 
     public MetamodelConnector(String id, String description, MetamodelFlowElement to, MetamodelFlowElement from,
             String name, List<MetamodelCoordinate> coordinates) {
         super(id, description);
         this.to = to;
        //        if (this.to == null) {
        //            throw new IllegalArgumentException("To Element of connector " + id + " is null");
        //        }
        //        this.from = from;
        //        if (this.from == null) {
        //            throw new IllegalArgumentException("From Element of connector " + id + " is null");
        //        }
 
         this.coordinates = new ArrayList<MetamodelCoordinate>();
         setCoordinates(coordinates);
         this.name = name;
     }
 
     public List<MetamodelCoordinate> getCoordinates() {
         return coordinates;
     }
 
     public void setCoordinates(List<MetamodelCoordinate> coordinates) {
         this.coordinates = coordinates;
     }
 
     public void addCoordinate(MetamodelCoordinate coordinate) {
         this.coordinates.add(coordinate);
     }
 
     public MetamodelFlowElement getFrom() {
         return from;
     }
 
     public void setFrom(MetamodelFlowElement from) {
         this.from = from;
     }
 
     public MetamodelFlowElement getTo() {
         return to;
     }
 
     public void setTo(MetamodelFlowElement to) {
         this.to = to;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
 }
