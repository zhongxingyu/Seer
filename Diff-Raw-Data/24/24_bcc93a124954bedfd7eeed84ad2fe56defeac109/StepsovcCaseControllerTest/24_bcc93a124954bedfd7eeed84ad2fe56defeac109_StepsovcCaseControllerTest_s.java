 package org.wv.stepsovc.web.controllers;
 
 import org.apache.velocity.app.VelocityEngine;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.wv.stepsovc.web.request.BeneficiaryCase;
 import org.wv.stepsovc.web.request.CaseUpdateType;
 import org.wv.stepsovc.web.services.BeneficiaryService;
 import org.wv.stepsovc.web.services.ReferralService;
 
 import static org.mockito.Mockito.verify;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 
 public class StepsovcCaseControllerTest {
 
     @Mock
     BeneficiaryService mockBeneficiaryService;
     @Mock
     VelocityEngine velocityEngine;
     @Mock
     ReferralService mockReferralService;
 
 
     StepsovcCaseController stepsovcCaseController;
     private BeneficiaryCase beneficiaryCase;
 
 
     @Before
     public void setup() {
         initMocks(this);
        stepsovcCaseController = new StepsovcCaseController(mockBeneficiaryService, mockReferralService, null);
         beneficiaryCase = new BeneficiaryCase();
     }
 
 
     @Test
     public void ShouldCallBeneficiaryRegistrationHandler() throws Exception {
         beneficiaryCase.setForm_type(CaseUpdateType.BENEFICIARY_REGISTRATION.getType());
         stepsovcCaseController.createCase(beneficiaryCase);
         verify(mockBeneficiaryService).createBeneficiary(beneficiaryCase);
     }
 
     @Test
     public void ShouldCallNewReferralHandler() throws Exception {
         beneficiaryCase.setForm_type(CaseUpdateType.NEW_REFERRAL.getType());
         stepsovcCaseController.createCase(beneficiaryCase);
         verify(mockReferralService).addNewReferral(beneficiaryCase);
     }
 
     @Test
     public void ShouldCallUpdateReferralHandler() throws Exception {
         beneficiaryCase.setForm_type(CaseUpdateType.UPDATE_REFERRAL.getType());
         stepsovcCaseController.createCase(beneficiaryCase);
         verify(mockReferralService).updateNotAvailedReasons(beneficiaryCase);
     }
 
     @Test
     public void ShouldCallUpdateServiceHandler() throws Exception {
         beneficiaryCase.setForm_type(CaseUpdateType.UPDATE_SERVICE.getType());
         stepsovcCaseController.createCase(beneficiaryCase);
         verify(mockReferralService).updateAvailedServices(beneficiaryCase);
     }
 }
