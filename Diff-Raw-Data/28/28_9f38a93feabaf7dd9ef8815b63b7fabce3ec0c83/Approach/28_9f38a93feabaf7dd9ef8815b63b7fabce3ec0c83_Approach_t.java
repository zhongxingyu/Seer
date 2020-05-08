 package controllers;
 
 import controllers.deadbolt.Restrict;
 import models.general.UnitRole;
 import models.project.ApproachRelease;
 import models.project.ApproachTestCycle;
import models.tree.jpa.TreeNode;
 import play.data.validation.Valid;
 import play.data.validation.Validation;
 
 public class Approach extends TMController {
 
     @Restrict(UnitRole.USER)
     public static void index() {
         render();
     }
 
     @Restrict(UnitRole.USER)
     public static void cycleDetails(Long cycleId) {
         ApproachTestCycle cycle = getCycle(cycleId);
         render(cycle);
     }
 
     @Restrict(UnitRole.USER)
     public static void releaseDetails(Long releaseId) {
         ApproachRelease release = getRelease(releaseId);
         render(release);
     }
 
 
     @Restrict(UnitRole.USER)
     public static void editCycle(@Valid ApproachTestCycle cycle) {
         checkInAccount(cycle);
         if (Validation.hasErrors()) {
             // TODO return validation errors, somehow
             error();
         }
         cycle.save();
         ok();
     }
 
     @Restrict(UnitRole.USER)
     public static void editRelease(@Valid ApproachRelease release) {
         checkInAccount(release);
         if (Validation.hasErrors()) {
             // TODO return validation errors, somehow
             error();
         }
         release.save();
         ok();
     }
 
 
     private static ApproachTestCycle getCycle(Long cycleId) {
         ApproachTestCycle cycle = null;
         if (cycleId != null) {
            TreeNode node = TreeNode.find(cycleId, ApproachTree.APPROACH_TREE);
            if(node == null) {
                return null;
            }
            cycle = ApproachTestCycle.findById(node.nodeId);
             checkInAccount(cycle);
         }
         return cycle;
     }
 
     private static ApproachRelease getRelease(Long releaseId) {
         ApproachRelease release = null;
         if (releaseId != null) {
            TreeNode node = TreeNode.find(releaseId, ApproachTree.APPROACH_TREE);
            if (node == null) {
                return null;
            }
            release = ApproachRelease.findById(node.nodeId);
             checkInAccount(release);
         }
         return release;
     }
 
 
 }
