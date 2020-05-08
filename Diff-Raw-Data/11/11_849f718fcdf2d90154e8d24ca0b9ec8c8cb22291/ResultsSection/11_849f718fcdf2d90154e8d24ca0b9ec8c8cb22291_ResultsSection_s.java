 package org.coode.dlquery;
 
 /**
  * Author: Matthew Horridge<br>
  * Stanford University<br>
  * Bio-Medical Informatics Research Group<br>
  * Date: 19/09/2013
  */
 public enum ResultsSection {
 
    DIRECT_SUPER_CLASSES("Direct super classes"),
 
    DIRECT_SUB_CLASSES("Direct sub classes"),
 
    SUPER_CLASSES("Super classes"),
 
    SUB_CLASSES("Sub classes"),
 
     EQUIVALENT_CLASSES("Equivalent classes"),
 
     INSTANCES("Instances");
 
 
     private String displayName;
 
     private ResultsSection(String displayName) {
         this.displayName = displayName;
     }
 
     public String getDisplayName() {
         return displayName;
     }
 }
