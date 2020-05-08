 package com.worthsoln.test.importer;
 
 import com.worthsoln.ibd.model.Allergy;
 import com.worthsoln.ibd.model.MyIbd;
 import com.worthsoln.ibd.model.Procedure;
 import com.worthsoln.patientview.XmlImportUtils;
 import com.worthsoln.patientview.logging.AddLog;
 import com.worthsoln.patientview.model.Centre;
 import com.worthsoln.patientview.model.Diagnostic;
 import com.worthsoln.patientview.model.Letter;
 import com.worthsoln.patientview.model.Medicine;
 import com.worthsoln.patientview.model.Patient;
 import com.worthsoln.patientview.model.Specialty;
 import com.worthsoln.patientview.model.TestResult;
 import com.worthsoln.patientview.model.Unit;
 import com.worthsoln.service.CentreManager;
 import com.worthsoln.service.DiagnosticManager;
 import com.worthsoln.service.LetterManager;
 import com.worthsoln.service.LogEntryManager;
 import com.worthsoln.service.MedicineManager;
 import com.worthsoln.service.PatientManager;
 import com.worthsoln.service.TestResultManager;
 import com.worthsoln.service.UnitManager;
 import com.worthsoln.service.ibd.IbdManager;
 import com.worthsoln.service.impl.SpringApplicationContextBean;
 import com.worthsoln.test.helpers.RepositoryHelpers;
 import com.worthsoln.test.helpers.impl.TestableResultsUpdater;
 import com.worthsoln.test.service.BaseServiceTest;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.core.io.Resource;
 import org.springframework.mock.web.MockHttpSession;
 
 import javax.inject.Inject;
 import java.io.IOException;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
  * The importer is kicked off from ParserMonitorServlet.
  *
  * There are 3 threads - XmlParserThread, UktParserThread & UktExportThread.
  *
  * There are 2 versions of the patient view xml schema in the examples directory.
  *
  * - pv_schema_1.0.xml - used by rpv
  * - pv_schema_2.0.xml - used by ibd
  */
 public class ImporterTest extends BaseServiceTest {
 
     @Inject
     private SpringApplicationContextBean springApplicationContextBean;
 
     @Inject
     private CentreManager centreManager;
 
     @Inject
     private LetterManager letterManager;
 
     @Inject
     private DiagnosticManager diagnosticManager;
 
     @Inject
     private MedicineManager medicineManager;
 
     @Inject
     private PatientManager patientManager;
 
     @Inject
     private TestResultManager testResultManager;
 
     @Inject
     private IbdManager ibdManager;
 
     @Inject
     private UnitManager unitManager;
 
     @Inject
     private RepositoryHelpers repositoryHelpers;
 
     @Inject
     private LogEntryManager logEntryManager;
 
     @Before
     public void setupSystem() {
         Unit mockUnit = new Unit();
         mockUnit.setUnitcode("RM301");
         mockUnit.setName("RM301: RUNNING MAN TEST UNIT");
         mockUnit.setShortname("RM301");
         mockUnit.setRenaladminemail("renaladmin@mailinator.com");
 
         Specialty mockSpecialty = new Specialty();
         mockSpecialty.setName("Renal Patient View");
         mockSpecialty.setContext("renal");
         mockSpecialty.setDescription("Renal Patient View");
 
         mockSpecialty = repositoryHelpers.createSpecialty("", "", "");
 
         mockUnit.setSpecialty(mockSpecialty);
 
         unitManager.save(mockUnit);
     }
 
     @Test
     /**
      *  Calls XmlParserUtils.updateXmlData with files and a dao ref
      *
      *      - ResultParser
      *          - parseResults
      *          - removePatientFromSystem
      *          - updatePatientData
      *              - updatePatientDetails
      *              - updateCentreDetails
      *              - deleteDateRanges
      *              - insertResults
      *              - deleteLetters
      *              - insertLetters
      *              - deleteOtherDiagnoses
      *              - insertOtherDiagnoses
      *              - deleteMedicines
      *              - insertMedicines
      */
     public void testXmlParserUsingRenalFile() throws IOException {
 
         Resource xmlFileResource = springApplicationContextBean.getApplicationContext()
                 .getResource("classpath:A_00794_1234567890.gpg.xml");
         Resource xsdFileResource = springApplicationContextBean.getApplicationContext()
                 .getResource("classpath:importer/pv_schema_2.0.xsd");
 
         TestableResultsUpdater testableResultsUpdater = new TestableResultsUpdater();
         MockHttpSession mockHttpSession = new MockHttpSession();
 
         testableResultsUpdater.update(mockHttpSession.getServletContext(), xmlFileResource.getFile(),
                 xsdFileResource.getFile());
 
         List<Centre> centres = centreManager.getAll();
 
         assertEquals("Incorrect number of centres", 1, centres.size());
         assertEquals("Incorrect centre", "A", centres.get(0).getCentreCode());
 
         List<Patient> patients = patientManager.get("A");
 
         assertEquals("Incorrect number of patients", 1, patients.size());
         assertEquals("Incorrect patient", "1234567890", patients.get(0).getNhsno());
 
         List<TestResult> results = testResultManager.get("1234567890", "A");
 
         assertEquals("Incorrect number of results", 316, results.size());
 
         List<Medicine> medicines = medicineManager.getAll();
 
         assertEquals("Incorrect number of medicines", 8, medicines.size());
 
         List<Letter> letters = letterManager.getAll();
 
         assertEquals("Incorrect number of letters", 2, letters.size());
     }
 
     /**
      * Test if importer handles empty test file. This probably means that the encryption did not work.
      *
      * An email should be sent to RPV admin email address and an entry should be created in log table
      *
      * @throws IOException
      */
     @Test
     public void testXmlParserUsingEmptyIBDFile() throws IOException {
         Resource xmlFileResource = springApplicationContextBean.getApplicationContext()
                .getResource("classpath:rm301_emptyfile_9876543210.xml");
         Resource xsdFileResource = springApplicationContextBean.getApplicationContext()
                 .getResource("classpath:importer/pv_schema_2.0.xsd");
 
         TestableResultsUpdater testableResultsUpdater = new TestableResultsUpdater();
         MockHttpSession mockHttpSession = new MockHttpSession();
 
         testableResultsUpdater.update(mockHttpSession.getServletContext(), xmlFileResource.getFile(),
                 xsdFileResource.getFile());
 
         checkNoDataHasBeenImportedFromIBDImportFile();
 
         checkLogEntry(XmlImportUtils.extractFromXMLFileNameNhsno(xmlFileResource.getFile().getName()),
                 AddLog.PATIENT_DATA_FAIL);
     }
 
     /**
      * Check if no data was imported
      */
     private void checkNoDataHasBeenImportedFromIBDImportFile() {
         List<Centre> centres = centreManager.getAll();
         assertEquals("Centres were imported although data file was supposed to be empty", 0, centres.size());
 
         List<Unit> units = unitManager.getAll(false);
         /**
          * {@link #setupSystem()} creates one unit so its ok if we have one unit now
          */
         assertEquals("Units were imported although data file was supposed to be empty", 1, units.size());
     }
 
     /**
      * Check if log entry was created
      *
      * @param nhsNo  nhsNo of patient
      * @param action log type
      */
     private void checkLogEntry(String nhsNo, String action) {
         assertNotNull("Log entry was not created", logEntryManager.getLatestLogEntry(nhsNo, action));
     }
 
     /**
      * Test if importer handles test results with future date
      *
      * The whole file should be rejected, an email should be sent to RPV admin email, and a "patient data fail"
      *      entry should be added to the log table
      *
      * @throws IOException
      */
     @Test
     public void testXmlParserCheckFutureTestResultDateInIBDFile() throws IOException {
         Resource xmlFileResource = springApplicationContextBean.getApplicationContext()
                 .getResource("classpath:rm301_resultWithFutureDate_9876543210.xml");
         Resource xsdFileResource = springApplicationContextBean.getApplicationContext()
                 .getResource("classpath:importer/pv_schema_2.0.xsd");
 
         TestableResultsUpdater testableResultsUpdater = new TestableResultsUpdater();
         MockHttpSession mockHttpSession = new MockHttpSession();
 
         testableResultsUpdater.update(mockHttpSession.getServletContext(), xmlFileResource.getFile(),
                 xsdFileResource.getFile());
 
         checkNoDataHasBeenImportedFromIBDImportFile();
 
         checkLogEntry(XmlImportUtils.extractFromXMLFileNameNhsno(xmlFileResource.getFile().getName()),
                         AddLog.PATIENT_DATA_FAIL);
     }
 
     /**
      * Test if importer handles test results outside date ranges specified
      *
      * Whole file needs to be rejected, and an email needs to be sent to RPV admin email
      *
      * @throws IOException
      */
     @Test
     public void testXmlParserCheckTestResultOutsideDataRangeInIBDFile() throws IOException {
         Resource xmlFileResource = springApplicationContextBean.getApplicationContext()
                 .getResource("classpath:rm301_resultWithOutsideDaterange_9876543210.xml");
         Resource xsdFileResource = springApplicationContextBean.getApplicationContext()
                 .getResource("classpath:importer/pv_schema_2.0.xsd");
 
         TestableResultsUpdater testableResultsUpdater = new TestableResultsUpdater();
         MockHttpSession mockHttpSession = new MockHttpSession();
 
         testableResultsUpdater.update(mockHttpSession.getServletContext(), xmlFileResource.getFile(),
                 xsdFileResource.getFile());
 
         checkNoDataHasBeenImportedFromIBDImportFile();
 
         checkLogEntry(XmlImportUtils.extractFromXMLFileNameNhsno(xmlFileResource.getFile().getName()),
                 AddLog.PATIENT_DATA_FAIL);
     }
 
     /**
      * Test if importer handles test results with empty values
      *
      * Whole file needs to be rejected, n email should be sent to RPV admin and the error should be logged.
      *
      * @throws IOException
      */
     @Test
     public void testXmlParserCheckTestResultWithEmptyValueInIBDFile() throws IOException {
         Resource xmlFileResource = springApplicationContextBean.getApplicationContext()
                 .getResource("classpath:rm301_resultWithEmptyValue_9876543210.xml");
         Resource xsdFileResource = springApplicationContextBean.getApplicationContext()
                 .getResource("classpath:importer/pv_schema_2.0.xsd");
 
         TestableResultsUpdater testableResultsUpdater = new TestableResultsUpdater();
         MockHttpSession mockHttpSession = new MockHttpSession();
 
         testableResultsUpdater.update(mockHttpSession.getServletContext(), xmlFileResource.getFile(),
                 xsdFileResource.getFile());
 
         checkNoDataHasBeenImportedFromIBDImportFile();
 
         checkLogEntry(XmlImportUtils.extractFromXMLFileNameNhsno(xmlFileResource.getFile().getName()),
                 AddLog.PATIENT_DATA_FAIL);
     }
 
     /**
      * Test if the importer imports data
      *
      * @throws IOException
      */
     @Test
     public void testXmlParserUsingIBDFile() throws IOException {
         Resource xmlFileResource = springApplicationContextBean.getApplicationContext()
                 .getResource("classpath:rm301_1244_9876543210.xml");
         Resource xsdFileResource = springApplicationContextBean.getApplicationContext()
                 .getResource("classpath:importer/pv_schema_2.0.xsd");
 
         TestableResultsUpdater testableResultsUpdater = new TestableResultsUpdater();
         MockHttpSession mockHttpSession = new MockHttpSession();
 
         testableResultsUpdater.update(mockHttpSession.getServletContext(), xmlFileResource.getFile(), xsdFileResource.getFile());
 
         checkIbdImportedData();
 
         List<TestResult> results = testResultManager.get("9876543210", "RM301");
 
         assertEquals("Incorrect number of results", 3, results.size());
     }
 
     /**
      * If you run the import twice for the same file we still have the same data set
      */
     @Test
     public void testXmlParserUsingIBDFileMultipleRuns() throws IOException {
         Resource xmlFileResource = springApplicationContextBean.getApplicationContext()
                 .getResource("classpath:rm301_1244_9876543210.xml");
         Resource xsdFileResource = springApplicationContextBean.getApplicationContext()
                 .getResource("classpath:importer/pv_schema_2.0.xsd");
 
         TestableResultsUpdater testableResultsUpdater = new TestableResultsUpdater();
         MockHttpSession mockHttpSession = new MockHttpSession();
 
         // run twice
         testableResultsUpdater.update(mockHttpSession.getServletContext(), xmlFileResource.getFile(), xsdFileResource.getFile());
         testableResultsUpdater.update(mockHttpSession.getServletContext(), xmlFileResource.getFile(), xsdFileResource.getFile());
 
         checkIbdImportedData();
 
         // Note the results get deleted each run on date range
     }
 
     /**
      * check the data importer should have imported.
      */
     private void checkIbdImportedData() {
         // test the stuff that should be the same regardless of how many imports of the file are done
 
         List<Centre> centres = centreManager.getAll();
 
         assertEquals("Incorrect number of centres", 1, centres.size());
         assertEquals("Incorrect centre", "RM301", centres.get(0).getCentreCode());
 
         List<Patient> patients = patientManager.get("RM301");
 
         assertEquals("Incorrect number of patients", 1, patients.size());
         assertEquals("Incorrect patient", "9876543210", patients.get(0).getNhsno());
 
         List<Letter> letters = letterManager.getAll();
 
         assertEquals("Incorrect number of letters", 2, letters.size());
 
         MyIbd myIbd = ibdManager.getMyIbd("9876543210");
         assertNotNull("No MyIbd information was parsed", myIbd);
 
         Diagnostic diagnostic = diagnosticManager.get("9876543210");
         assertNotNull("No diagnostic information was parsed", diagnostic);
 
         Procedure procedure = ibdManager.getProcedure("9876543210");
         assertNotNull("No procedure information was parsed", procedure);
 
         Allergy allergy = ibdManager.getAllergy("9876543210");
         assertNotNull("No allergy information was parsed", allergy);
     }
 }
