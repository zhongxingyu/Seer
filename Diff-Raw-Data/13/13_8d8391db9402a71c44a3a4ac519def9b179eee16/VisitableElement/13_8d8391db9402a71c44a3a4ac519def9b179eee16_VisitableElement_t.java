 package com.rockwellautomation.verification.element;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.rockwellautomation.verification.operation.OperationVisitor;
 
 /**
  * Base implementation for all VisitableElements.
  * 
  * @author JCase
  */
 public abstract class VisitableElement {
 
   /**
    * Call the appropriate visit() method on the OperationVisitor based on the runtime type of this AbstractElement.
    * 
    * @param v
    */
   protected abstract void doVisit(OperationVisitor<?> v);
 
   /** The Children of this element */
   private List<VisitableElement> children = new ArrayList<VisitableElement>();
 
   /**
    * Required for Visitor pattern
    * 
    * @param v
    *          The OperationVisitor that is visiting this VisitableElement
    */
   public final void accept(OperationVisitor<?> v) {
     doVisit(v);
 
     // Pass the visitor to all children so they can visit it
     for (VisitableElement e : this.children) {
       
       // Must re-check the short cutting flag inside the loop because it may be set by the last iteration
      if ( (v.isShortCuttingConditionMet()) ) {
        break;
       }
      else {
        e.accept(v);
       }
     }
   }
 
   /**
    * Adds the VisitableElement to the List of children
    * 
    * @param child
    */
   public final void addChild(VisitableElement child) {
     this.children.add(child);
   }
 }
