 package controllers;
 
 import models.project.Requirement;
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
                requirement = Requirement.findById(requirementId);
                 checkInAccount(requirement);
         }
         return requirement;
     }
 
 }
