 package org.openmrs.module.emr.fragment.controller;
 
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.api.PatientService;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
 import org.openmrs.ui.framework.fragment.action.SuccessResult;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
 * Created with IntelliJ IDEA.
 * User: cospih
 * Date: 2/15/13
 * Time: 4:13 PM
 * To change this template use File | Settings | File Templates.
  */
 public class EditPatientIdentifierFragmentController {
 
     public FragmentActionResult editPatientIdentifier (UiUtils ui,
                                                @RequestParam("patientId") Patient patient,
                                                @RequestParam("identifierTypeId") PatientIdentifierType identifierType,
                                                @RequestParam(value="identifierValue", required = false) String identifierValue,
                                                @RequestParam(value="locationId", required = false) Location location,
                                                @SpringBean("patientService")PatientService service) {
 
         if(patient!=null && identifierType!=null){
             PatientIdentifier patientIdentifier = patient.getPatientIdentifier(identifierType);
             if(patientIdentifier==null && StringUtils.isNotBlank(identifierValue)){
                 patientIdentifier = new PatientIdentifier(identifierValue, identifierType, location);
             }else{
                 if(StringUtils.isNotBlank(identifierValue)){
                     patientIdentifier.setIdentifier(identifierValue);
                 }else{
                     patientIdentifier.setVoided(true);
                 }
             }
             patient.addIdentifier(patientIdentifier);
            service.savePatient(patient);
         }
         return new SuccessResult( ui.message("emr.patientDashBoard.editPatientIdentifier.successMessage"));
     }
 }
