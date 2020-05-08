 package models;
 
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 import javax.persistence.*;
 import java.util.*;
 
 /**
  * User: soyoung
  * Date: Feb 28, 2011
  */
 @Entity
 public class Requirement extends Model implements Comparable {
 
     @OneToMany(mappedBy = "requirement")
     public List<Rule> rules;
 
     @Required
     public String label;
 
     public String details;
 
     public Date dateCreated;
     public Date lastUpdated;
 
     // steps within this requirement
     @OneToMany(mappedBy = "parent")
     @OrderBy("sortOrder ASC")
     public Set<Step> steps;
 
     Boolean allSteps = false;
 
     // extensions within this requirement
     @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
     @OrderBy("sortOrder ASC") 
     public List<Requirement> extensions;
 
     @ManyToOne(optional = true)
     public Requirement parent;
 
     @ManyToMany(mappedBy = "extensions")
     public Set<Step> extendedSteps = null;
 
     public int sortOrder;
 
     public String toString() {
         return label;
     }
 
     public int compareTo(Object obj) {
 		return sortOrder - ((Requirement)obj).sortOrder;
 	}
     
     public int primaryPathStepsSize(){
         return steps.size();
     }
 
     public boolean hasPrimarySteps() {
         return !steps.isEmpty();
     }
 
     public Set<Step> getExtendedSteps() {
         if (extendedSteps == null) {
             extendedSteps = new TreeSet<Step>();
         }
         return extendedSteps;
     }
 
     public void normalizeSteps() {
         int sortOrder = 0;
         for (Iterator<Step> iterator = steps.iterator(); iterator.hasNext();) {
             Step step = iterator.next();
             step.sortOrder = sortOrder++;
             step.save();
         }        
     }
 
     public Requirement addExtension(String extensionLabel, boolean allPrimaryPathSteps) {
         return this.addExtension(extensionLabel, this.steps, true);
     }
 
     public Requirement addExtension(String extensionLabel, Set<Step> extendedSteps, boolean allPrimaryPathSteps) {
         int nextNum = extensions.size();
 
         Requirement extension = new Requirement();
         extension.label = extensionLabel;
         extension.sortOrder = nextNum;
         extension.allSteps = allPrimaryPathSteps;
         extension.parent = this;
         extension.save();
 
         extensions.add(extension);
 
         for (Iterator<Step> iterator = extendedSteps.iterator(); iterator.hasNext();) {
             Step step = iterator.next();
             step.getExtensions().add(extension);
             step.save();
         }
 
         return extension;
     }
 
     public void removeExtension(Requirement extension){
         extensions.remove(extension);
         this.save();
     }
 
 
     public Step addStep(String newRequirementLabel) {
         Requirement newRequirement = new Requirement();
         newRequirement.label = newRequirementLabel;
         newRequirement.save();
 
         return addStep(newRequirement);
     }
 
     public Step addStep(Requirement requirement) {
         normalizeSteps();
 
         Step step = new Step();
         step.sortOrder = this.steps.size();
         step.requirement = requirement;
         step.parent = this;
         step.dateCreated = new Date();
 
         // add step to any (all step) extensions
         for (Iterator<Requirement> iterator = extensions.iterator(); iterator.hasNext();) {
             Requirement extension = iterator.next();
             if(extension.allSteps){
                 step.getExtensions().add(extension);
             }
         }
 
         step.save();
         
         return step;
     }
 
     public void removeStep(Step step, boolean removeRequirement) {
         if (removeRequirement) {
             step.requirement.deepDelete();
         }
         else {
             step.delete();
 
             // todo: what to do about orphaned requirements that this step was extended by
         }
         normalizeSteps();
     }
 
 
     /**
      * delete the requirement and all steps pointing to it
      */
     public void deepDelete() {
        List<Step> steps = Step.find("byRequirement", this).fetch();
        for (Iterator<Step> iterator = steps.iterator(); iterator.hasNext();) {
             Step step = iterator.next();
             step.delete();
             // todo: what to do about orphaned requirements that this step was extended by
         }
 
         this.delete();
     }
 
 
 //
 //    private void removeStep(Step step) {
 //        for (Iterator<Requirement> iterator = step.extensions.iterator(); iterator.hasNext();) {
 //            Requirement extension = iterator.next();
 //            extension.removeStep(step);
 //            extension.save();
 //        }
 //
 //
 //    }
 
 //
 //    private void removeOrphanedExtensions(extensions){
 //
 //        Set extensionsToDelete = extensions.findAll{e ->
 //            (!e.hasPrimaryPathSteps())
 //        }
 //
 //        println "extensionsToDelete: " + extensionsToDelete
 //
 //        extensionsToDelete.each{extension ->
 //            ExtensionPath currentExtensionPath = extension.extensionPath
 //            currentExtensionPath.removeExtension(extension)
 //            currentExtensionPath.save()
 //        }
 //    }
 //
 //    PrimaryPathStep addPrimaryPathStep(String newRequirementLabel) {
 //        def newRequirement = new Requirement(project:this.project,label: newRequirementLabel, topLevel: false).save();
 //        return addPrimaryPathStep(newRequirement)
 //    }
 //
 //    PrimaryPathStep addPrimaryPathStep(Requirement newRequirement) {
 //        initPrimaryPath()
 //        def newRank = primaryPath.size();
 //        PrimaryPathStep primaryPathStep = new PrimaryPathStep(primaryPath: primaryPath, requirement: newRequirement, rank: newRank)
 //        primaryPath.addPrimaryPathStep(primaryPathStep)
 //        normalizePrimaryPathSteps();
 //        primaryPath.save()
 //
 //        extensionPath?.updateExtensions(primaryPathStep)
 //
 //        return primaryPathStep;
 //    }
 //
     public Set allOtherParentRequirements(Requirement parentRequirement){
 
         List<Step> steps = Step.find("byRequirement", this).fetch();
 
         Set requirements = new HashSet();
 
         for (Iterator<Step> i = steps.iterator(); i.hasNext();) {
             Step step = i.next();
             if (!parentRequirement.id.equals(step.requirement.id)) {
                 requirements.add(step.requirement);
             }
         }
 
         return requirements;
     }
 //
 //
 }
