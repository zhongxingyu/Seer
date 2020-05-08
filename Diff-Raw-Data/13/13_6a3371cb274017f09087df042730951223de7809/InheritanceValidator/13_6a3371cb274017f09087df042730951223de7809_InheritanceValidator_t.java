 package gov.nih.nci.ncicb.cadsr.loader.validator;
 
 import java.util.*;
 
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.dao.ValueDomainDAO;
 
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 import gov.nih.nci.ncicb.cadsr.loader.event.ProgressListener;
 import gov.nih.nci.ncicb.cadsr.loader.event.ProgressEvent;
 
 import gov.nih.nci.ncicb.cadsr.loader.ext.CadsrModule;
 import gov.nih.nci.ncicb.cadsr.loader.ext.CadsrModuleListener;
 import gov.nih.nci.ncicb.cadsr.loader.util.*;
 
 import org.apache.log4j.Logger;
 
 /**
  * Validate issues that come from mapping inherited attributes <ul>
  * <li>Inherited attributes CDE mapping must have matching properties
  * all the way down the inheritance path.
  * <ul>
  */
 public class InheritanceValidator implements Validator, CadsrModuleListener {
   private ElementsLists elements = ElementsLists.getInstance();
 
   private ValidationItems items = ValidationItems.getInstance();
 
   private ProgressListener progressListener;
 
   private InheritedAttributeList inheritedList = InheritedAttributeList.getInstance();
 
   private CadsrModule cadsrModule = null;
 
   private Logger logger = Logger.getLogger(InheritanceValidator.class.getName());
 
   public InheritanceValidator() {
   }
 
   public void addProgressListener(ProgressListener l) {
     progressListener = l;
   }
 
   /**
    * returns a list of Validation errors.
    */
   public ValidationItems validate() {
     
     Collection<DataElement> inheritedDEs = inheritedList.getAllInherited();
 
     for(DataElement inheritedDE : inheritedDEs) {
       if(inheritedDE.getPublicId() != null) {
         DataElement parentDE = inheritedList.getParent(inheritedDE);
         while(parentDE != null) {
          if(parentDE.getPublicId() != null && 
             parentDE.getDataElementConcept() != null &&
             parentDE.getDataElementConcept().getProperty() != null && 
             inheritedDE.getDataElementConcept() != null &&
             inheritedDE.getDataElementConcept().getProperty() != null 
             ) {
            Property inheritedProp = inheritedDE.getDataElementConcept().getProperty();
            Property parentProp = parentDE.getDataElementConcept().getProperty();
            if(!inheritedProp.getPublicId().equals(parentProp.getPublicId())
               || !inheritedProp.getVersion().equals(parentProp.getVersion())) {
               items.addItem(new ValidationError
                             (PropertyAccessor.getProperty
                              ("inherited.and.parent.property.mismatch", LookupUtil.lookupFullName(inheritedDE), LookupUtil.lookupFullName(parentDE)),inheritedDE));
             }
           } else { // parentDE mapped to concept. Verify that concepts match
             String conceptConcat = parentDE.getDataElementConcept().getProperty().getPreferredName();
             String[] revCodes = conceptConcat.split(":");
             String[] conceptCodes = new String[revCodes.length];
             for(int i=0; i<revCodes.length; i++)
               conceptCodes[i] = revCodes[revCodes.length - 1 - i];
 
             if(conceptCodes.length > 0) {
               try {
                 if(!cadsrModule.matchDEToPropertyConcepts(inheritedDE, conceptCodes)) {
                   items.addItem(new ValidationError
                                 (PropertyAccessor.getProperty
                                  ("inherited.concept.mismatch", LookupUtil.lookupFullName(inheritedDE)), inheritedDE));
                 }
               } catch (Exception e) {
                 logger.error(e);
               } // end of try-catch
             }
 //             if(!inheritedList.isInherited(parentDE)) {
 //               items.addItem(new ValidationError
 //                             (PropertyAccessor.getProperty
 //                              ("parent.attribute.not.mapped", LookupUtil.lookupFullName(inheritedDE)), inheritedDE));
               
 //             }
           }
           parentDE = inheritedList.getParent(parentDE);
         }
       }
     }
     return items;
   }
 
   public void setCadsrModule(CadsrModule module) {
     this.cadsrModule = module;
   }
 
 }
