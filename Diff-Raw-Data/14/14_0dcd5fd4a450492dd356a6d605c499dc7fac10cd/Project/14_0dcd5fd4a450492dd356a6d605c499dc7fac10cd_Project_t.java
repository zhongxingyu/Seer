 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package nextgen.model;
 
 import java.util.HashSet;
 
 /**
  *
  * @author Rodrigo
  */
 public class Project extends Entity {
 
     private HashSet<Element> elements;
 
     public Project(String name, String description) {
         super(name, description);
         elements = new HashSet<>();
     }
 
     public Project(int id, String name, String description, HashSet<Element> elements) {
         super(name, description);
         this.elements = elements;
     }
 
     // <editor-fold defaultstate="collapsed" desc="Get and Set">
    public HashSet<Element> getElements() {
         return elements;
     }
 
    public void setElements(HashSet<Element> elements) {
         this.elements = elements;
     }
     // </editor-fold>
 }
