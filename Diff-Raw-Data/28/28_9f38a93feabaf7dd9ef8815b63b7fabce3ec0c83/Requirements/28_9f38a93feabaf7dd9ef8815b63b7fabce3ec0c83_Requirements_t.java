 package controllers;
 
 import models.project.Requirement;
import models.tree.jpa.TreeNode;
 import play.data.validation.Valid;
 import play.data.validation.Validation;
 
 /**
  * TODO security
  *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 public class Requirements extends TMController {
 
     public static void index() {
         render();
     }
 
     public static void requirementDetails(Long requirementId) {
         Requirement requirement = getRequirement(requirementId);
         render(requirement);
     }
 
     public static void linked(Long requirementId) {
         Requirement requirement = getRequirement(requirementId);
         render(requirement);
     }
 
     public static void edit(@Valid Requirement requirement) {
         checkInAccount(requirement);
         if(Validation.hasErrors()) {
             // TODO handle validation errors in view somehow
             error();
         }
         requirement.save();
         ok();
     }
 
     /**
      * Resolves a Requirement given a TreeNode id
      */
     private static Requirement getRequirement(Long requirementId) {
         Requirement requirement = null;
         if(requirementId != null) {
                TreeNode node = TreeNode.find(requirementId, RequirementTree.REQUIREMENT_TREE);
                if(node == null) {
                    return null;
                }
                requirement = Requirement.findById(node.nodeId);
                 checkInAccount(requirement);
         }
         return requirement;
     }
 
 }
