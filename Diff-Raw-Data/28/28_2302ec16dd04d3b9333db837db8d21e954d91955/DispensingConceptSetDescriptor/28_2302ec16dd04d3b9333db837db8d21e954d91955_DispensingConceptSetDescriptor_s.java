 package org.openmrs.module.dispensing.descriptor;
 
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.Concept;
 import org.openmrs.Drug;
 import org.openmrs.Encounter;
 import org.openmrs.Obs;
 import org.openmrs.Location;
 import org.openmrs.api.ConceptService;
 import org.openmrs.module.dispensing.DispensedMedication;
 import org.openmrs.module.dispensing.DispensingApiConstants;
 import org.openmrs.module.dispensing.MedicationDose;
 import org.openmrs.module.dispensing.MedicationDuration;
 import org.openmrs.api.LocationService;
 import org.openmrs.module.emrapi.EmrApiConstants;
 import org.openmrs.module.emrapi.descriptor.ConceptSetDescriptor;
 
 public class DispensingConceptSetDescriptor extends ConceptSetDescriptor {
 
     private Concept dispensingSetConcept;
     private Concept medicationOrdersConcept;
     private Concept quantityOfMedicationDispensedConcept;
     private Concept generalDrugFrequencyConcept;
     private Concept quantityOfMedicationPrescribedPerDoseConcept;
     private Concept unitsOfMedicationPrescribedPerDoseConcept;
     private Concept medicationDurationConcept;
     private Concept timeUnitsConcept;
     private Concept timingOfHospitalPrescriptionConcept;
     private Concept dischargeLocationConcept;
     private LocationService locationService;
 
     public DispensingConceptSetDescriptor(ConceptService conceptService, LocationService locationService) {
 
         this.locationService = locationService;
 
         setup(conceptService,  EmrApiConstants.EMR_CONCEPT_SOURCE_NAME,
                 "dispensingSetConcept", DispensingApiConstants.CONCEPT_CODE_DISPENSING_MEDICATION_CONCEPT_SET,
                 "medicationOrdersConcept", DispensingApiConstants.CONCEPT_CODE_MEDICATION_ORDERS,
                 "quantityOfMedicationDispensedConcept", DispensingApiConstants.CONCEPT_CODE_QUANTITY_OF_MEDICATION_DISPENSED,
                 "generalDrugFrequencyConcept", DispensingApiConstants.CONCEPT_CODE_GENERAL_DRUG_FREQUENCY,
                 "quantityOfMedicationPrescribedPerDoseConcept", DispensingApiConstants.CONCEPT_CODE_QUANTITY_OF_MEDICATION_PRESCRIBED_PER_DOSE,
                 "unitsOfMedicationPrescribedPerDoseConcept", DispensingApiConstants.CONCEPT_CODE_UNITS_OF_MEDICATION_PRESCRIBED_PER_DOSE,
                 "medicationDurationConcept", DispensingApiConstants.CONCEPT_CODE_MEDICATION_DURATION,
                "timeUnitsConcept", DispensingApiConstants.CONCEPT_CODE_TIME_UNITS,
                // note that these last two concepts are technically part of the set, but included as top-level obs on the encounter (see toDispensedMedcation method)
                "timingOfHospitalPrescriptionConcept", DispensingApiConstants.CONCEPT_CODE_HOSPITAL_PRESCRIPTION_TIMING,
                "dischargeLocationConcept", DispensingApiConstants.CONCEPT_CODE_DISCHARGE_LOCATION);
     }
 
     public DispensingConceptSetDescriptor() {
     }
 
     public Concept getDispensingSetConcept() {
         return dispensingSetConcept;
     }
 
     public void setDispensingSetConcept(Concept dispensingSetConcept) {
         this.dispensingSetConcept = dispensingSetConcept;
     }
 
     public Concept getGeneralDrugFrequencyConcept() {
         return generalDrugFrequencyConcept;
     }
 
     public void setGeneralDrugFrequencyConcept(Concept generalDrugFrequencyConcept) {
         this.generalDrugFrequencyConcept = generalDrugFrequencyConcept;
     }
 
     public Concept getMedicationDurationConcept() {
         return medicationDurationConcept;
     }
 
     public void setMedicationDurationConcept(Concept medicationDurationConcept) {
         this.medicationDurationConcept = medicationDurationConcept;
     }
 
     public Concept getMedicationOrdersConcept() {
         return medicationOrdersConcept;
     }
 
     public void setMedicationOrdersConcept(Concept medicationOrdersConcept) {
         this.medicationOrdersConcept = medicationOrdersConcept;
     }
 
     public Concept getQuantityOfMedicationDispensedConcept() {
         return quantityOfMedicationDispensedConcept;
     }
 
     public void setQuantityOfMedicationDispensedConcept(Concept quantityOfMedicationDispensedConcept) {
         this.quantityOfMedicationDispensedConcept = quantityOfMedicationDispensedConcept;
     }
 
     public Concept getQuantityOfMedicationPrescribedPerDoseConcept() {
         return quantityOfMedicationPrescribedPerDoseConcept;
     }
 
     public void setQuantityOfMedicationPrescribedPerDoseConcept(Concept quantityOfMedicationPrescribedPerDoseConcept) {
         this.quantityOfMedicationPrescribedPerDoseConcept = quantityOfMedicationPrescribedPerDoseConcept;
     }
 
     public Concept getTimeUnitsConcept() {
         return timeUnitsConcept;
     }
 
     public void setTimeUnitsConcept(Concept timeUnitsConcept) {
         this.timeUnitsConcept = timeUnitsConcept;
     }
 
     public Concept getUnitsOfMedicationPrescribedPerDoseConcept() {
         return unitsOfMedicationPrescribedPerDoseConcept;
     }
 
     public void setUnitsOfMedicationPrescribedPerDoseConcept(Concept unitsOfMedicationPrescribedPerDoseConcept) {
         this.unitsOfMedicationPrescribedPerDoseConcept = unitsOfMedicationPrescribedPerDoseConcept;
     }
 
     public Concept getTimingOfHospitalPrescriptionConcept() {
         return timingOfHospitalPrescriptionConcept;
     }
 
     public void setTimingOfHospitalPrescriptionConcept(Concept timingOfHospitalPrescriptionConcept) {
         this.timingOfHospitalPrescriptionConcept = timingOfHospitalPrescriptionConcept;
     }
 
     public Concept getDischargeLocationConcept() {
         return dischargeLocationConcept;
     }
 
     public void setDischargeLocationConcept(Concept dischargeLocation) {
         this.dischargeLocationConcept = dischargeLocation;
     }
 
     public boolean isDispensedMedication(Obs obsGroup){
         return (obsGroup != null) ? obsGroup.getConcept().equals(dispensingSetConcept) : false;
     }
 
     public Obs findObsatTopLevel ( Obs obsGroup, Concept concept){
         Encounter encounter  = obsGroup.getEncounter();
         for (Obs observation : encounter.getObsAtTopLevel(true)) {
             if (observation.getConcept().getName() == concept.getName()){
                    return observation;
             }
         }
         return null;
     }
 
     public DispensedMedication toDispensedMedication(Obs obsGroup){
         if (!isDispensedMedication(obsGroup)) {
             throw new IllegalArgumentException("Not an obs group for a dispensed diagnosis" + obsGroup);
         }
         DispensedMedication dispensedMedication = new DispensedMedication();
         dispensedMedication.setDispensedDateTime(obsGroup.getEncounter().getEncounterDatetime());
 
         Drug drug = getDrugToDispensedMedication(obsGroup);
         dispensedMedication.setDrug(drug);
 
         MedicationDose medicationDose = GetMedicationDoseToDispensedMedication(obsGroup);
         dispensedMedication.setMedicationDose(medicationDose);
 
         String frequency = getGeneralDrugFrequencyToDispensedMedication(obsGroup, dispensedMedication);
         dispensedMedication.setPrescribedFrequency(frequency);
 
         MedicationDuration medicationDuration =  getMedicationDurationToDispensedMedication(obsGroup);
         dispensedMedication.setMedicationDuration(medicationDuration);
 
         int quantityOfMedicationDispensed = getQuantityOfMedicationDispensedConceptToDispensedMedication(obsGroup);
         dispensedMedication.setQuantityDispensed(quantityOfMedicationDispensed);
 
         String timingOfHospitalPrescription = getTimingOfHospitalPrescriptionToDispensedMedication(obsGroup);
         dispensedMedication.setTimingOfHospitalPrescription(timingOfHospitalPrescription);
 
         Location dischargeLocation = getDischargeLocationToDispensedMedication(obsGroup);
         dispensedMedication.setDischargeLocation(dischargeLocation);
 
         return dispensedMedication;
     }
 
     public Location getDischargeLocationToDispensedMedication(Obs obsGroup) {
         Obs obs;
         Location dischargeLocation = null;
         obs = findObsatTopLevel(obsGroup, dischargeLocationConcept);
         if (obs != null){
             int dischargeLocationId  = Integer.valueOf(obs.getValueText());
             dischargeLocation = locationService.getLocation(dischargeLocationId);
         }
         return dischargeLocation;
     }
 
     private String getTimingOfHospitalPrescriptionToDispensedMedication(Obs obsGroup) {
         String timingOfHospitalPrescription = null;
         Obs obs;
         obs =  findObsatTopLevel(obsGroup, timingOfHospitalPrescriptionConcept);
         if (obs !=null){
             timingOfHospitalPrescription = obs.getValueCoded().getName().getName();
         }
         return timingOfHospitalPrescription;
     }
 
     private int getQuantityOfMedicationDispensedConceptToDispensedMedication(Obs obsGroup) {
         Obs obs;
         int quantitydispensed = 0;
 
         obs = findMember(obsGroup, quantityOfMedicationDispensedConcept);
         if (obs != null) {
             Double valueNumeric = obs.getValueNumeric();
             if (valueNumeric != null) {
                 quantitydispensed = new Integer(valueNumeric.intValue());
             }
         }
         return  quantitydispensed;
     }
 
     private String getGeneralDrugFrequencyToDispensedMedication(Obs obsGroup, DispensedMedication dispensedMedication) {
         Obs obs;
         String frequency = "";
         obs = findMember(obsGroup, generalDrugFrequencyConcept);
         if (obs != null){
              frequency = obs.getValueText();
         }
         return frequency;
     }
 
     private MedicationDuration getMedicationDurationToDispensedMedication(Obs obsGroup) {
         Obs obs;
         obs = findMember(obsGroup, medicationDurationConcept);
         Integer duration = null;
         if (obs != null) {
             Double valueNumeric = obs.getValueNumeric();
             if (valueNumeric != null){
                 duration = new Integer(valueNumeric.intValue());
             }
         }
         obs = findMember(obsGroup, timeUnitsConcept);
         String units = null;
         if (obs != null) {
             units = obs.getValueCoded().getName().getName();
         }
         MedicationDuration medicationDuration = null;
         if (duration != null && StringUtils.isNotEmpty(units)){
             medicationDuration = new MedicationDuration(duration, units);
         }
 
         return medicationDuration;
     }
 
     public Drug getDrugToDispensedMedication(Obs obsGroup) {
         Obs obs = findMember(obsGroup, medicationOrdersConcept);
         if (obs == null){
             throw new IllegalArgumentException("Obs group does not contain a drug observation: " + obsGroup);
         }
         Drug drug = obs.getValueDrug();
         if (drug == null ){
             throw new IllegalArgumentException("Obs group does not contain a drug: " + obs);
         }
         return drug;
     }
 
     public MedicationDose GetMedicationDoseToDispensedMedication (Obs obsGroup) {
             Obs obs;
             obs = findMember(obsGroup, quantityOfMedicationPrescribedPerDoseConcept);
             Integer dose = null;
             if (obs != null) {
                 Double valueNumeric = obs.getValueNumeric();
                 if (valueNumeric != null) {
                     dose = new Integer(valueNumeric.intValue());
                 }
             }
 
             obs = findMember(obsGroup, unitsOfMedicationPrescribedPerDoseConcept);
             String units = null;
             if (obs != null){
                 units = obs.getValueText();
             }
 
             MedicationDose medicationDose = null;
 
             if (dose != null && StringUtils.isNotBlank(units)){
                  medicationDose = new MedicationDose(dose, units);
 
             }
             return medicationDose;
     }
 }
