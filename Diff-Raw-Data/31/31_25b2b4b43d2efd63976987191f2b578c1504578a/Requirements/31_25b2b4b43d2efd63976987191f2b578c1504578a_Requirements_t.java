 package controllers;
 
 import models.Requirement;
 import models.Step;
 import org.apache.commons.lang.StringUtils;
 import play.mvc.Controller;
 import play.templates.Template;
 import play.templates.TemplateLoader;
 import utils.ExtraJavaExtensions;
 
 import java.util.*;
 
 /**
  * User: soyoung
  * Date: Feb 28, 2011
  */
 public class Requirements extends Controller {
     public static void show(Long id) throws Exception {
         Requirement requirement = Requirement.findById(id);
         notFoundIfNull(requirement);
         render(requirement);
     }
 
     public static void edit(Long id) throws Exception {
 
     }
 
     public static void ajaxAddPrimaryStep(Long requirementId, Long linkedRequirementId, String stepLabel) throws Exception {
         Requirement requirement = Requirement.findById(requirementId);
 
         // link to an existing requirement
         if (linkedRequirementId != null) {
             Requirement linkedRequirement = Requirement.findById(linkedRequirementId);
             requirement.addStep(linkedRequirement);
             requirement.save();
 
             renderJSON("{ \"result\":{\"okay\": true} }");
         } else {
             if (stepLabel != null) {
 //                        stepLabel = stepLabel.capitalizeFirstLetter()
                 Requirement newRequirement = new Requirement();
                 newRequirement.label = stepLabel;
 
                 newRequirement.save();
 
                 requirement.addStep(newRequirement);
                 requirement.save();
 
                 renderJSON("{ \"result\":{\"okay\": true} }");
             } else {
                 renderJSON("{ \"result\": {\"okay\": false, \"message\": \"Label must not be empty.\"} }");
             }
         }
     }
 
     public static void ajaxFetchPrimaryPath(Long requirementId) throws Exception {
         Requirement requirement = Requirement.findById(requirementId);
         renderTemplate("Requirements/_primaryPath.html", requirement);
     }
 
     public static void ajaxUpdateLabel(String label, Long id) throws Exception {
         if (!StringUtils.isEmpty(label)) {
             Requirement requirement = Requirement.findById(id);
             requirement.label = label;
             requirement.save();
             renderJSON("{ \"result\":{\"okay\": true} }");
         }
     }
 
     public static void ajaxRemovePrimaryStep(Long id) throws Exception {
         Step stepToDelete = Step.findById(id);
 
        Requirement parent = stepToDelete.parent;
        Requirement requirement = stepToDelete.requirement;
 
         if (stepToDelete != null && requirement != null) {
 
             // check if a requirement has any dependencies
             try {
                 boolean hasChildren = !requirement.steps.isEmpty();
                 boolean hasReferences = stepToDelete.requirementReferencesCheck();
 
                 if (!hasChildren && !hasReferences) {

                    parent.removeStep(stepToDelete, true);
                    parent.save();
                     renderJSON("{ \"result\":{\"okay\": true} }");
                 } else {
 
                     String message = "";
                     String frag = "";
                     boolean showUnlinkBtn = true;
                     if (hasChildren && hasReferences) {
                         message = "WARNING: This requirement has both child requirements and is referenced by other parent requirements.";
 
                         Template template = TemplateLoader.load(template("Requirements/_requirementsList.html"));
                        Set<Requirement> requirements = stepToDelete.requirement.allOtherParentRequirements(parent);
 
                         Map params = new HashMap();
                         params.put("requirements", requirements);
 
                         frag = template.render(params);
                         frag = ExtraJavaExtensions.escapeJSON(frag);
                                 
                     } else if (hasChildren && !hasReferences) {
                         showUnlinkBtn = false;
                         message = "WARNING: This requirement has child requirements.";
                     } else if (!hasChildren && hasReferences) {
                         message = "WARNING: This requirement is referenced by other parent requirements.";
 
                         Template template = TemplateLoader.load(template("Requirements/_requirementsList.html"));
                         Set<Requirement> requirements = stepToDelete.requirement.allOtherParentRequirements(requirement);
 
                         Map params = new HashMap();
                         params.put("requirements", requirements);
 
                         frag = template.render(params);
                         frag = ExtraJavaExtensions.escapeJSON(frag);
                     }
 
                     renderJSON("{ \"result\": {\"okay\": false, \"message\": \"" + message + "\", \"fragment\": \"" + frag + "\", \"showUnlinkBtn\": " + showUnlinkBtn + "} }");
                 }
             } catch (Exception e) {
                 e.printStackTrace();
                 renderJSON("{ \"result\": {\"okay\": false, \"message\": \"" + e.getMessage() + "\"} }");
             }
         } else {
             renderJSON("{ \"result\": {\"okay\": false, \"message\": \"Unable to remove the Step due to a data inconsistency.\"}}");
         }
     }
 
     public static void ajaxFetchExtensions(Long requirementId) throws Exception {
         Requirement requirement = Requirement.findById(requirementId);
         List<Requirement> extensions = requirement.extensions;
 
         renderTemplate("Requirements/_extensions.html", extensions, requirementId);
     }
 
     public static void ajaxUnlinkPrimaryStep(Long id) throws Exception {
         Step stepToDelete = Step.findById(id);
         Requirement requirement = stepToDelete.parent;
 
         if (stepToDelete != null && requirement != null) {
             requirement.removeStep(stepToDelete, false);
             requirement.save();
             renderJSON("{ \"result\": {\"okay\": true}}");
         } else {
             renderJSON("{ \"result\": {\"okay\": false, \"message\": \"Unable to remove the Step due to a data inconsistency.\"}}");
         }
     }
 
     public static void ajaxDeepDeletePrimaryStep(Long id) throws Exception {
         Step stepToDelete = Step.findById(id);
         Requirement requirement = stepToDelete.parent;
 
         if (stepToDelete != null && requirement != null) {
             requirement.removeStep(stepToDelete, true);
             requirement.save();
             renderJSON("{ \"result\" : {\"okay\": true} }");
         } else {
             renderJSON("{ \"result\" : {\"okay\": false, \"message\": \"Unable to remove the Step due to a data inconsistency.\"}}");
         }
     }
 
     public static void ajaxCombine() throws Exception {
 
     }
 
     public static void ajaxAddExtension(String extensionLabel, Long requirementId, Long[] pps) throws Exception {
 
         if (extensionLabel != null) {
 
 //            extensionLabel = extensionLabel.capitalizeFirstLetter()
 
             Requirement requirement = Requirement.findById(requirementId);
 
             //Link to all primaryPathSteps.
             if (pps == null) {
                 requirement.addExtension(extensionLabel, true);
             } else {
                 Set primaryPathSteps = new TreeSet();
 
                 for (Iterator<Step> i = requirement.steps.iterator(); i.hasNext();) {
                     Step step = i.next();
                     for (int j = 0; j < pps.length; j++) {
                         Long stepId = pps[j];
                         if (stepId.equals(step.id)) {
                             primaryPathSteps.add(step);
                             break;
                         }
                     }
                 }
 
                 requirement.addExtension(extensionLabel, primaryPathSteps, false);
 
             }
 
             requirement.save();
             renderJSON("{\"result\": {\"okay\": true}}");
         } else {
             renderJSON("{\"result\": {\"okay\": false, \"message\": \"Label must not be empty.\"}}");
         }
 
     }
 
     public static void ajaxSearchJSON() throws Exception {
 
     }
 
     public static void ajaxUpdateExtensionPathDisplayOrder(Long requirementId, Long[] ext) throws Exception {
         Requirement requirement = Requirement.findById(requirementId);
         int index = 0;
 
         if (ext != null) {
 
             for (int i = 0; i < ext.length; i++) {
                 Long id = ext[i];
 
                 Requirement extension = Requirement.findById(id);
                 extension.sortOrder = index++;
                 extension.save();
             }
 
             Template template = TemplateLoader.load(template("Requirements/_extensions.html"));
 
             Map params = new HashMap();
             params.put("extensions", requirement.extensions);
             params.put("requirementId", requirement.id);
 
             String frag = template.render(params);
 
             frag = ExtraJavaExtensions.escapeJSON(frag);
 
             renderJSON("{ \"result\" : {\"okay\": true, \"fragment\": \"" + frag + "\"}}");
 
         } else {
             renderJSON("{\"result\": {\"okay\": false}}");
         }
     }
 
     public static void ajaxUpdatePrimaryPathOrder(Long requirementId, Long[] linkId) throws Exception {
         Requirement requirement = Requirement.findById(requirementId);
         int index = 0;
 
         if (linkId != null) {
 
             for (int i = 0; i < linkId.length; i++) {
                 Long id = linkId[i];
 
                 Step step = Step.findById(id);
                 step.sortOrder = index++;
                 step.save();
             }
 
             Template template = TemplateLoader.load(template("Requirements/_primaryPath.html"));
 
             Map params = new HashMap();
             params.put("requirement", requirement);
 
             String frag = template.render(params);
 
             frag = ExtraJavaExtensions.escapeJSON(frag);
 
             renderJSON("{ \"result\" : {\"okay\": true, \"fragment\": \"" + frag + "\"}}");
         } else {
             renderJSON("{\"result\": {\"okay\": false}}");
         }
     }
 
     public static void ajaxAddExtensionStep() throws Exception {
 
     }
 
     public static void ajaxUpdateExtensionLabel(String label, Long id) throws Exception {
         if (!StringUtils.isEmpty(label)) {
             Requirement extension = Requirement.findById(id);
             extension.label = label;
             extension.save();
             renderJSON("{\"result\": {\"okay\": true}}");
         }
     }
 
     public static void ajaxUpdateExtensionStepLabel() throws Exception {
 
     }
 
     public static void ajaxDeleteExtensionStep() throws Exception {
     }
 
     public static void ajaxDeleteExtension(Long id) throws Exception {
         Requirement extension = Requirement.findById(id);
 
         for (Iterator<Step> i = extension.extendedSteps.iterator(); i.hasNext();) {
             Step step = i.next();
             step.extensions.remove(extension);
             step.save();
         }
 
         extension.delete();
         renderJSON("{ \"result\": {\"okay\": true}}");               
     }
 
     public static void ajaxAddLinkedStepToExtension() throws Exception {
 
     }
 }
