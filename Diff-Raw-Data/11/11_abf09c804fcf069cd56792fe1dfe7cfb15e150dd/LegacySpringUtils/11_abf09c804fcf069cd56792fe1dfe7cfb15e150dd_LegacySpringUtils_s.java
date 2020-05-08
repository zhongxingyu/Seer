 package com.worthsoln.utils;
 
 import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;
 import com.worthsoln.service.AboutmeManager;
 import com.worthsoln.service.CommentManager;
 import com.worthsoln.service.DiagnosisManager;
 import com.worthsoln.service.EdtaCodeManager;
 import com.worthsoln.service.EmailVerificationManager;
 import com.worthsoln.service.FeedbackManager;
 import com.worthsoln.service.LetterManager;
 import com.worthsoln.service.LogEntryManager;
 import com.worthsoln.service.MedicineManager;
 import com.worthsoln.service.NewsManager;
 import com.worthsoln.service.PatientManager;
 import com.worthsoln.service.ResultHeadingManager;
 import com.worthsoln.service.SecurityUserManager;
 import com.worthsoln.service.SplashPageManager;
 import com.worthsoln.service.TestResultManager;
 import com.worthsoln.service.UKTransplantManager;
 import com.worthsoln.service.UnitManager;
 import com.worthsoln.service.UserManager;
 import com.worthsoln.service.ibd.IbdManager;
 import com.worthsoln.service.impl.SpringApplicationContextBean;
 
 /**
  *  Used to connect up the methods in the old model classes that do service layer operations to the newer service
  * managers. We can move stuff out of this when the service/DAO code gets moved out from the model classes.
  * When/if...
  */
 public class LegacySpringUtils {
 
     private static AboutmeManager aboutmeManager;
 
     private static CommentManager commentManager;
 
     private static DiagnosisManager diagnosisManager;
 
     private static EdtaCodeManager edtaCodeManager;
 
     private static EmailVerificationManager emailVerificationManager;
 
     private static FeedbackManager feedbackManager;
 
     private static LetterManager letterManager;
 
     private static LogEntryManager logEntryManager;
 
     private static MedicineManager medicineManager;
 
     private static NewsManager newsManager;
 
     private static PatientManager patientManager;
 
     private static ResultHeadingManager resultHeadingManager;
 
     private static SecurityUserManager securityUserManager;
 
     private static SplashPageManager splashPageManager;
 
     private static SpringApplicationContextBean springApplicationContextBean;
 
     private static TestResultManager testResultManager;
 
     private static UKTransplantManager ukTransplantManager;
 
     private static UnitManager unitManager;
 
     private static UserManager userManager;
 
     public static AboutmeManager getAboutmeManager() {
         return aboutmeManager;
     }
 
     public static void setAboutmeManager(AboutmeManager aboutmeManager) {
         LegacySpringUtils.aboutmeManager = aboutmeManager;
     }
 
     public static CommentManager getCommentManager() {
         return commentManager;
     }
 
     public static void setCommentManager(CommentManager commentManager) {
         LegacySpringUtils.commentManager = commentManager;
     }
 
     public static DiagnosisManager getDiagnosisManager() {
         return diagnosisManager;
     }
 
     public static void setDiagnosisManager(DiagnosisManager diagnosisManager) {
         LegacySpringUtils.diagnosisManager = diagnosisManager;
     }
 
     public static EdtaCodeManager getEdtaCodeManager() {
         return edtaCodeManager;
     }
 
     public static void setEdtaCodeManager(EdtaCodeManager edtaCodeManager) {
         LegacySpringUtils.edtaCodeManager = edtaCodeManager;
     }
 
     public static EmailVerificationManager getEmailVerificationManager() {
         return emailVerificationManager;
     }
 
     public static void setEmailVerificationManager(EmailVerificationManager emailVerificationManager) {
         LegacySpringUtils.emailVerificationManager = emailVerificationManager;
     }
 
     public static FeedbackManager getFeedbackManager() {
         return feedbackManager;
     }
 
     public static void setFeedbackManager(FeedbackManager feedbackManager) {
         LegacySpringUtils.feedbackManager = feedbackManager;
     }
 
     public static LetterManager getLetterManager() {
         return letterManager;
     }
 
     public static void setLetterManager(LetterManager letterManager) {
         LegacySpringUtils.letterManager = letterManager;
     }
 
     public static LogEntryManager getLogEntryManager() {
         return logEntryManager;
     }
 
     public static void setLogEntryManager(LogEntryManager logEntryManager) {
         LegacySpringUtils.logEntryManager = logEntryManager;
     }
 
     public static MedicineManager getMedicineManager() {
         return medicineManager;
     }
 
     public static void setMedicineManager(MedicineManager medicineManager) {
         LegacySpringUtils.medicineManager = medicineManager;
     }
 
     public static NewsManager getNewsManager() {
         return newsManager;
     }
 
     public static void setNewsManager(NewsManager newsManager) {
         LegacySpringUtils.newsManager = newsManager;
     }
 
     public static PatientManager getPatientManager() {
         return patientManager;
     }
 
     public static void setPatientManager(PatientManager patientManager) {
         LegacySpringUtils.patientManager = patientManager;
     }
 
     public static ResultHeadingManager getResultHeadingManager() {
         return resultHeadingManager;
     }
 
     public static void setResultHeadingManager(ResultHeadingManager resultHeadingManager) {
         LegacySpringUtils.resultHeadingManager = resultHeadingManager;
     }
 
     public static void setSecurityUserManager(SecurityUserManager securityUserManager) {
         LegacySpringUtils.securityUserManager = securityUserManager;
     }
 
     public static SecurityUserManager getSecurityUserManager() {
         return securityUserManager;
     }
 
     public static SplashPageManager getSplashPageManager() {
         return splashPageManager;
     }
 
     public static void setSplashPageManager(SplashPageManager splashPageManager) {
         LegacySpringUtils.splashPageManager = splashPageManager;
     }
 
     public static UKTransplantManager getUkTransplantManager() {
         return ukTransplantManager;
     }
 
     public static SpringApplicationContextBean getSpringApplicationContextBean() {
         return springApplicationContextBean;
     }
 
     public static void setSpringApplicationContextBean(SpringApplicationContextBean springApplicationContextBean) {
         LegacySpringUtils.springApplicationContextBean = springApplicationContextBean;
     }
 
     public static TestResultManager getTestResultManager() {
         return testResultManager;
     }
 
     public static void setTestResultManager(TestResultManager testResultManager) {
         LegacySpringUtils.testResultManager = testResultManager;
     }
 
     public static void setUkTransplantManager(UKTransplantManager ukTransplantManager) {
         LegacySpringUtils.ukTransplantManager = ukTransplantManager;
     }
 
     public static UserManager getUserManager() {
         return userManager;
     }
 
     public static void setUserManager(UserManager userManager) {
         LegacySpringUtils.userManager = userManager;
     }
 
     public static UnitManager getUnitManager() {
         return unitManager;
     }
 
     public static void setUnitManager(UnitManager unitManager) {
         LegacySpringUtils.unitManager = unitManager;
     }
 }
