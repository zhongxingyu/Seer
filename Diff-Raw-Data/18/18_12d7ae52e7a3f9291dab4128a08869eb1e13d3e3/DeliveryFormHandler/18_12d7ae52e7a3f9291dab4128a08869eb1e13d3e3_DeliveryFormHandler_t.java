 package org.motechproject.ghana.national.handlers;
 
 import org.motechproject.ghana.national.bean.DeliveryForm;
 import org.motechproject.ghana.national.domain.ChildDeliveryOutcome;
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.ghana.national.exception.XFormHandlerException;
 import org.motechproject.ghana.national.service.*;
 import org.motechproject.ghana.national.service.request.DeliveredChildRequest;
 import org.motechproject.ghana.national.service.request.PregnancyDeliveryRequest;
 import org.motechproject.mrs.model.MRSUser;
 import org.motechproject.openmrs.advice.ApiSession;
 import org.motechproject.openmrs.advice.LoginAsAdmin;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class DeliveryFormHandler{
 
     Logger log = LoggerFactory.getLogger(DeliveryFormHandler.class);
 
     @Autowired
     PregnancyService pregnancyService;
 
     @Autowired
     MobileMidwifeService mobileMidwifeService;
 
     @Autowired
     PatientService patientService;
 
     @Autowired
     FacilityService facilityService;
 
     @Autowired
     StaffService staffService;
     @Autowired
     private CareService careService;
 
     @LoginAsAdmin
     @ApiSession
     public void handleFormEvent(DeliveryForm deliveryForm) {
         try {
             PregnancyDeliveryRequest deliveryRequest = createDeliveryRequest(deliveryForm);
            if(pregnancyService.isDeliverySuccessful(deliveryRequest) && !deliveryRequest.getMaternalDeath())
                 mobileMidwifeService.rollover(deliveryForm.getMotechId(),deliveryForm.getDate());
             else
                 mobileMidwifeService.unRegister(deliveryForm.getMotechId());
             pregnancyService.handleDelivery(deliveryRequest);
 
             if (!deliveryForm.getMaternalDeath()) {
                 careService.enrollMotherForPNC(deliveryRequest.getPatient(), deliveryRequest.getDeliveryDateTime());
             } else {
                 patientService.deceasePatient(deliveryForm.getDate().toDate(), deliveryForm.getMotechId(), "OTHER", "Delivery");
             }
 
         } catch (Exception e) {
             log.error("Encountered error while processing delivery form", e);
             throw new XFormHandlerException("Encountered error while processing delivery form", e);
         }
     }
 
 
     private PregnancyDeliveryRequest createDeliveryRequest(DeliveryForm deliveryForm) {
         Facility facility = facilityService.getFacilityByMotechId(deliveryForm.getFacilityId());
         MRSUser staff = staffService.getUserByEmailIdOrMotechId(deliveryForm.getStaffId());
         Patient patient = patientService.getPatientByMotechId(deliveryForm.getMotechId());
 
         PregnancyDeliveryRequest request = new PregnancyDeliveryRequest()
                 .staff(staff)
                 .patient(patient)
                 .facility(facility)
                 .deliveryDateTime(deliveryForm.getDate())
                 .childDeliveryMode(deliveryForm.getMode())
                 .childDeliveryOutcome(deliveryForm.getOutcome())
                 .maleInvolved(deliveryForm.getMaleInvolved())
                 .childDeliveryLocation(deliveryForm.getDeliveryLocation())
                 .childDeliveredBy(deliveryForm.getDeliveredBy())
                 .deliveryComplications(deliveryForm.getDeliveryComplications())
                 .vvf(deliveryForm.getVvf())
                 .maternalDeath(deliveryForm.getMaternalDeath())
                 .comments(deliveryForm.getComments())
                 .sender(deliveryForm.getSender());
 
         if (deliveryForm.getOutcome().equals(ChildDeliveryOutcome.SINGLETON) ||
                 deliveryForm.getOutcome().equals(ChildDeliveryOutcome.TWINS) ||
                 deliveryForm.getOutcome().equals(ChildDeliveryOutcome.TRIPLETS))
             request.addDeliveredChildRequest(new DeliveredChildRequest()
                     .childBirthOutcome(deliveryForm.getChild1Outcome())
                     .childRegistrationType(deliveryForm.getChild1RegistrationType())
                     .childMotechId(deliveryForm.getChild1MotechId())
                     .childSex(deliveryForm.getChild1Sex())
                     .childFirstName(deliveryForm.getChild1FirstName())
                     .childWeight(deliveryForm.getChild1Weight()));
 
         if (deliveryForm.getOutcome().equals(ChildDeliveryOutcome.TWINS) ||
                 deliveryForm.getOutcome().equals(ChildDeliveryOutcome.TRIPLETS))
             request.addDeliveredChildRequest(new DeliveredChildRequest()
                     .childBirthOutcome(deliveryForm.getChild2Outcome())
                     .childRegistrationType(deliveryForm.getChild2RegistrationType())
                     .childMotechId(deliveryForm.getChild2MotechId())
                     .childSex(deliveryForm.getChild2Sex())
                     .childFirstName(deliveryForm.getChild2FirstName())
                     .childWeight(deliveryForm.getChild2Weight()));
 
         if (deliveryForm.getOutcome().equals(ChildDeliveryOutcome.TRIPLETS))
             request.addDeliveredChildRequest(new DeliveredChildRequest()
                     .childBirthOutcome(deliveryForm.getChild3Outcome())
                     .childRegistrationType(deliveryForm.getChild3RegistrationType())
                     .childMotechId(deliveryForm.getChild3MotechId())
                     .childSex(deliveryForm.getChild3Sex())
                     .childFirstName(deliveryForm.getChild3FirstName())
                     .childWeight(deliveryForm.getChild3Weight()));
         return request;
     }
 }
