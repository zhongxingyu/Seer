 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
 
 import java.util.Map;
 
 /**
  * @author Rhett Sutphin
  */
 public class DeleteCommand extends EditTemplateCommand {
 
     ////// MODES
 
     @Override
     protected Mode epochMode() {
         return new DeleteEpoch();
     }
 
     @Override
     protected Mode armMode() {
         return new DeleteArm();
     }
 
     private abstract class DeleteMode<T extends PlanTreeNode<? extends PlanTreeInnerNode>> implements Mode {
         public final void performEdit() {
             if (getObjectParent().getChildren().size() < 2) return;
             updateRevision(getObjectParent(), Remove.create(getObject()));
         }
 
         public Map<String, Object> getModel() {
            return null;
         }
 
         protected abstract T getObject();
         protected abstract PlanTreeInnerNode getObjectParent();
     }
 
     private class DeleteEpoch extends DeleteMode<Epoch> {
         public String getRelativeViewName() {
             return "deleteEpoch";
         }
 
         @Override
         protected Epoch getObject() {
             return getEpoch();
         }
 
         @Override
         protected PlanTreeInnerNode getObjectParent() {
             return getSafeEpochParent();
         }
     }
 
     private class DeleteArm extends DeleteMode<Arm> {
         public String getRelativeViewName() {
             return "deleteArm";
         }
 
         @Override
         protected Arm getObject() {
             return getArm();
         }
 
         @Override
         protected PlanTreeInnerNode getObjectParent() {
             return getSafeArmParent();
         }
     }
 }
