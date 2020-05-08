 package org.openmrs.module.dispensing;
 
 import org.openmrs.Concept;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.utils.ModuleProperties;
 import org.springframework.stereotype.Component;
 
 @Component("dispensingProperties")
public class DispensingProperties extends ModuleProperties {
 
     public Concept getDispensingConstructConcept() {
         return getEmrApiConceptByMapping(DispensingApiConstants.CONCEPT_CODE_DISPENSING_MEDICATION_CONCEPT_SET);
     }
 
     public Concept getMedicationConcept() {
         return getEmrApiConceptByMapping(DispensingApiConstants.CONCEPT_CODE_MEDICATION_ORDERS);
     }
 
     public Concept getDosageConcept() {
         return getEmrApiConceptByMapping(DispensingApiConstants.CONCEPT_CODE_QUANTITY_OF_MEDICATION_PRESCRIBED_PER_DOSE);
     }
 
     public Concept getDosageUnitsConcept() {
         return getEmrApiConceptByMapping(DispensingApiConstants.CONCEPT_CODE_UNITS_OF_MEDICATION_PRESCRIBED_PER_DOSE);
     }
 
     public Concept getMedicationFrequencyConcept() {
         return getEmrApiConceptByMapping(DispensingApiConstants.CONCEPT_CODE_DRUG_FREQUENCY);
     }
 
     public Concept getMedicationDurationConcept() {
         return getEmrApiConceptByMapping(DispensingApiConstants.CONCEPT_CODE_MEDICATION_DURATION);
     }
 
     public Concept getMedicationDurationUnitsConcept() {
         return getEmrApiConceptByMapping(DispensingApiConstants.CONCEPT_CODE_TIME_UNITS);
     }
 
     public Concept getDispensedAmountConcept() {
         return getEmrApiConceptByMapping(DispensingApiConstants.CONCEPT_CODE_QUANTITY_OF_MEDICATION_DISPENSED);
     }
 
     public Concept getAdministrationInstructions() {
         return getEmrApiConceptByMapping(DispensingApiConstants.CONCEPT_CODE_ADMINISTRATION_INSTRUCTIONS);
     }
 
    protected Concept getEmrApiConceptByMapping(String code) {
        return getSingleConceptByMapping(conceptService.getConceptSourceByName(EmrApiConstants.EMR_CONCEPT_SOURCE_NAME), code);
    }
 }
