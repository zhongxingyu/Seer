 package org.openmrs.module.mirebalais.smoke;
 
 import junit.framework.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.openmrs.module.mirebalais.smoke.dataModel.Patient;
 import org.openmrs.module.mirebalais.smoke.helper.PatientDatabaseHandler;
import org.openmrs.module.mirebalais.smoke.helper.UserDatabaseHandler;
import org.openmrs.module.mirebalais.smoke.pageobjects.LoginPage;
 import org.openmrs.module.mirebalais.smoke.pageobjects.NonCodedDiagnosesList;
 import org.openmrs.module.mirebalais.smoke.pageobjects.PatientDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.ReportsHomePage;
 import org.openqa.selenium.By;
 
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
 
 public class NonCodedDiagnosesTest extends DbTest {
 
     private static final String NON_CODED_DIAGNOSIS = "nonCodedDiagnosis";
     private static final String CODED_DIAGNOSIS = "IGU";
     private static final String CODE_DIAGNOSIS_DIALOG = "code-diagnosis-dialog";
 
 
     private By cancelCodeDiagnosis = By.cssSelector("#" + CODE_DIAGNOSIS_DIALOG + " .cancel");
     private By confirmCodeDiagnosis = By.cssSelector("#" + CODE_DIAGNOSIS_DIALOG + " .confirm");
 
     @BeforeClass
     public static void prepare() throws Exception {
         logInAsClinicalUser();
     }
 
     @Before
     public void setUp() throws Exception {
         Patient testPatient = PatientDatabaseHandler.insertNewTestPatient();
         initBasicPageObjects();
 
         appDashboard.goToPatientPage(testPatient.getId());
         patientDashboard.startVisit();
     }
 
 
     @Test
     public void shouldShowNonCodedDiagnosesPage() throws Exception {
         ReportsHomePage reportsHomePage = new ReportsHomePage(driver);
         // enter a non-coded diagnosis
         patientDashboard.addConsultNoteWithDischarge(NON_CODED_DIAGNOSIS);
 
         // make sure the consult note has finished submitting before opening reports page
         assertThat(patientDashboard.countEncountersOfType(PatientDashboard.CONSULTATION), is(1));
 
         // verify the non-coded diagnosis is displayed in the reports page
         appDashboard.openReportApp();
         reportsHomePage.openNonCodedDiagnosesReport();
         NonCodedDiagnosesList nonCodedDiagnosesList = new NonCodedDiagnosesList(driver);
         List<String> nonCodedDiagnoses = nonCodedDiagnosesList.getNonCodedDiagnoses();
         Assert.assertTrue(nonCodedDiagnoses.contains(NON_CODED_DIAGNOSIS));
 
         //open the Code a Diagnosis dialog
         nonCodedDiagnosesList.openCodeDiagnosisDialog(NON_CODED_DIAGNOSIS);
         // enter a coded diagnosis
         nonCodedDiagnosesList.setClearTextToField("diagnosis-search", CODED_DIAGNOSIS);
         driver.findElement(By.cssSelector("strong.matched-name")).click();
 
         nonCodedDiagnosesList.clickOn(confirmCodeDiagnosis);
         nonCodedDiagnosesList.wait5seconds.until(invisibilityOfElementLocated(By.id(CODE_DIAGNOSIS_DIALOG)));
 
         // verify the NON-CODED diagnosis is no longer present
         nonCodedDiagnoses = nonCodedDiagnosesList.getNonCodedDiagnoses();
         Assert.assertFalse(nonCodedDiagnoses.contains(NON_CODED_DIAGNOSIS));
 
     }
 
 }
