 package org.theeuropeanlibrary.uim.gui.gwt.shared;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 /**
  * Workflow object
  * 
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since Apr 28, 2011
  */
 public class WorkflowDTO implements IsSerializable {
     private String identifier;
     private String name;
     private String description;
 
     /**
      * Creates a new instance of this class.
      * 
      * @param identifier
      * @param name
      * @param description
      */
     public WorkflowDTO(String identifier, String name, String description) {
         this.name = name;
         this.description = description;
     }
 
     /**
      * Creates a new instance of this class.
      */
     public WorkflowDTO() {
         super();
     }
 
     /**
      * @param name
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * @param description
      */
     public void setDescription(String description) {
         this.description = description;
     }
 
     /**
      * @return name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @return description
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * @return identifier
      */
     public String getIdentifier() {
         return identifier;
     }
 
     /**
      * @param identifier
      */
     public void setIdentifier(String identifier) {
         this.identifier = identifier;
     }
 }
