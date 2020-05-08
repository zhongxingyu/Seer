 /*******************************************************************************
  * Copyright (c) 2012, 5AM Solutions, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  * disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  * disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
  * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.fiveamsolutions.plc.web.struts2.wizard;
 
 import java.util.regex.Pattern;
 
 import javax.validation.Valid;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.fiveamsolutions.plc.data.ChallengeQuestion;
 import com.fiveamsolutions.plc.data.PLCUser;
 import com.fiveamsolutions.plc.data.PatientAccount;
 import com.fiveamsolutions.plc.service.PatientInformationService;
 import com.fiveamsolutions.plc.web.struts2.util.PLCSessionHelper;
 import com.google.inject.Inject;
 
 /**
  * @author Abraham J. Evans-EL <aevansel@5amsolutions.com>
  *
  */
 public class RegisterPatientAction extends ConsentWizardAction {
     private static final long serialVersionUID = 1L;
     private static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[A-Z])(?=.*[!@#$%&*]).{8,20})";
     private final PatientInformationService patientService;
     @Valid
     private PLCUser user = new PLCUser();
     private String repeatPassword;
     private String repeatEmail;
     private String challengeQuestion;
     private String challengeAnswer;
 
     /**
      * Class constructor.
      *
      * @param patientService the patient service
      */
     @Inject
     public RegisterPatientAction(PatientInformationService patientService) {
         this.patientService = patientService;
     }
 
     /**
      * Registers the given patient account.
      * @return the struts forwarding result
      */
     public String register() {
         PatientAccount account = new PatientAccount();
         account.setPlcUser(user);
         account.setPatientDemographics(PLCSessionHelper.getPatientDemographics(getSession()));
 
         ChallengeQuestion qa = new ChallengeQuestion();
         qa.setQuestion(challengeQuestion);
         qa.setAnswer(challengeAnswer);
         account.getChallengeQuestions().add(qa);
 
         patientService.registerPatient(account);
         return SUCCESS;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate() {
         super.validate();
         if (!Pattern.matches(PASSWORD_PATTERN, user.getPassword())) {
            addFieldError("user.password", getText("errors.password.invalid"));
         }
         if (!StringUtils.equals(user.getPassword(), getRepeatPassword())) {
            addFieldError("user.password", "Passwords must match.");
            addFieldError("repeatPassword", "Passwords must match.");
         }
         if (!StringUtils.equals(user.getEmail(), getRepeatEmail())) {
            addFieldError("user.email", "Email Addresses must match.");
            addFieldError("repeatEmail", "Email Addresses must match.");
         }
         if (StringUtils.isEmpty(challengeQuestion)) {
             addFieldError("challengeQuestion", getText(FIELD_REQUIRED_KEY));
         }
         if (StringUtils.isEmpty(challengeAnswer)) {
             addFieldError("challengeAnswer", getText(FIELD_REQUIRED_KEY));
         }
     }
 
     /**
      * @return the user
      */
     public PLCUser getUser() {
         return user;
     }
 
     /**
      * @param user the user to set
      */
     public void setUser(PLCUser user) {
         this.user = user;
     }
 
     /**
      * @return the repeatPassword
      */
     public String getRepeatPassword() {
         return repeatPassword;
     }
 
     /**
      * @param repeatPassword the repeatPassword to set
      */
     public void setRepeatPassword(String repeatPassword) {
         this.repeatPassword = repeatPassword;
     }
 
     /**
      * @return the repeatEmail
      */
     public String getRepeatEmail() {
         return repeatEmail;
     }
 
     /**
      * @param repeatEmail the repeatEmail to set
      */
     public void setRepeatEmail(String repeatEmail) {
         this.repeatEmail = repeatEmail;
     }
 
     /**
      * @return the challegeQuestion
      */
     public String getChallengeQuestion() {
         return challengeQuestion;
     }
 
     /**
      * @param challengeQuestion the challengeQuestion to set
      */
     public void setChallengeQuestion(String challengeQuestion) {
         this.challengeQuestion = challengeQuestion;
     }
 
     /**
      * @return the challengeAnswer
      */
     public String getChallengeAnswer() {
         return challengeAnswer;
     }
 
     /**
      * @param challengeAnswer the challengeAnswer to set
      */
     public void setChallengeAnswer(String challengeAnswer) {
         this.challengeAnswer = challengeAnswer;
     }
 
 }
