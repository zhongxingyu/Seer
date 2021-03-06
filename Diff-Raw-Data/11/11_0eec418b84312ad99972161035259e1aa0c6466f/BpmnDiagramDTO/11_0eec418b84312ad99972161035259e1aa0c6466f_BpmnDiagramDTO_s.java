 package com.camunda.fox.cycle.web.dto;
 
 import com.camunda.fox.cycle.entity.BpmnDiagram;
 import com.camunda.fox.cycle.entity.BpmnDiagram.Status;
 
 /**
  * This is a data object which exposes a {@link BpmnDiagram} to the client via rest.
  * 
  * @author nico.rehwaldt
  */
 public class BpmnDiagramDTO {
   
   private Long id;
   private String modeller;
   private String diagramPath;
 
   private Status status;
   
  private BpmnDiagramDTO(BpmnDiagram diagram) {
     this.id = diagram.getId();
     this.modeller = diagram.getModeller();
     this.diagramPath = diagram.getDiagramPath();
     
     this.status = diagram.getStatus();
   }
 
   public Long getId() {
     return id;
   }
 
   public void setId(Long id) {
     this.id = id;
   }
   
   public String getModeller() {
     return modeller;
   }
 
   public void setModeller(String modeller) {
     this.modeller = modeller;
   }
 
   public Status getStatus() {
     return status;
   }
   
   public String getDiagramPath() {
     return diagramPath;
   }
 
   public void setDiagramPath(String diagramPath) {
     this.diagramPath = diagramPath;
   }
   
   /**
    * Wraps a bpmn diagram as a data object
    * @param diagram
    * @return 
    */
   public static BpmnDiagramDTO wrap(BpmnDiagram diagram) {
     return new BpmnDiagramDTO(diagram);
   }
 }
