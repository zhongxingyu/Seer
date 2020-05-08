 package org.lawrencebower.docgen.web.controller.data_entry;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.lawrencebower.docgen.core.generator.utils.ChecksumUtils;
 import org.lawrencebower.docgen.core.generator.utils.DocGenFileUtils;
 import org.lawrencebower.docgen.web.model.SessionData;
 import org.lawrencebower.docgen.web.test_examples.factory.DocumentFactoryTestImpl;
 import org.lawrencebower.docgen.web_logic.business.controler_business.data_entry.DataEntryCB;
 import org.lawrencebower.docgen.web_model.view.constants.ViewConstants;
 import org.lawrencebower.docgen.web_model.view.document.binding.DataEntryBindBean;
 import org.lawrencebower.docgen.web_model.view.view_factory.ViewFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.web.context.request.WebRequest;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import static junit.framework.Assert.assertTrue;
 import static junit.framework.TestCase.assertEquals;
 import static org.lawrencebower.docgen.web.test_examples.factory.DocumentFactoryTestImpl.*;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:META-INF/web-application-test-context.xml")
 public class SetFieldsControllerTest {
 
     @Autowired
     DataEntryCB business;
     @Autowired
     ViewFactory viewFactory;
     @Autowired
     DocGenFileUtils fileUtils;
     @Autowired
     SessionSetupUtils sessionSetupUtils;
     @Autowired
     SessionData sessionData;
     SetFieldsController controller;
 
     @Autowired
     @Qualifier("webTestInputRoot")
     String testInputRoot;
 
     @Autowired
     @Qualifier("pdfOutputRoot")
     String testOutputRoot;
 
     String testInputPath;
 
     @Autowired
     ChecksumUtils checksumUtils;
 
     @Before
     public void setUp() throws Exception {
 
         setupSessionData();
         prepareDocumentSetOnSession();
 
         controller = new SetFieldsController();
         controller.setSessionData(sessionData);
         controller.setBusiness(business);
 
         setupInputPath();
 
         createOutputPath();
     }
 
     /**
      * sets the directory where the expected file templates are written
      */
     private void setupInputPath() {
         String packageName = getClass().getPackage().getName();
         packageName = packageName.replaceAll("\\.", "\\\\");
 
         testInputPath = testInputRoot + packageName + File.separator;
     }
 
     private void createOutputPath() {
         fileUtils.makeDirIfNotExists(new File(testOutputRoot));
     }
 
     private void setupSessionData() {
         sessionSetupUtils.setupSessionData(sessionData);
     }
 
     @Test
     public void testSubmitFields_validData_validOutput() throws Exception {
 
         ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 
         controller.submitFields(new DataEntryBindBean(), outStream);
 
         verifyExpectedPDFFilesWritten();
 
         verifyBytesWrittenToStreamCorrect(outStream);
     }
 
     private void prepareDocumentSetOnSession() {
 
         PrepareFieldsController prepareFields = new PrepareFieldsController();
 
         prepareFields.setSessionData(sessionData);
 
         prepareFields.setBusiness(business);
 
         prepareFields.prepareFields();
     }
 
     /**
      * Checks that the bytes written to the output stream are those of the concatenated file written in
      * the output directory
      */
     private void verifyBytesWrittenToStreamCorrect(ByteArrayOutputStream outStream) {
 
         File expectedConcatenatedFile = new File(testOutputRoot + ViewConstants.CONCATENATED_FILE_NAME);
         byte[] expectedFileBytes = fileUtils.readFileToByteArray(expectedConcatenatedFile);
         String expectedChecksum = checksumUtils.getChecksumFromBytes(expectedFileBytes);
 
         String httpResponseChecksum = checksumUtils.getChecksumFromBytes(outStream.toByteArray());
 
         assertEquals(expectedChecksum, httpResponseChecksum);
     }
 
     private void verifyExpectedPDFFilesWritten() {
         checkDoc1();
         checkDoc2();
        checkDoc3();
         checkConcatenatedFiles();
     }
 
     private void checkConcatenatedFiles() {
         File expectedConcatenatedFile = new File(testInputPath + "EXPECTED_CONCATENATED_FILE.pdf");
         File createdConcatenatedFile = new File(testOutputRoot + ViewConstants.CONCATENATED_FILE_NAME);
         checkFilesAreSame(expectedConcatenatedFile, createdConcatenatedFile);
     }
 
     private void checkDoc1() {
         File expectedConcatenatedFile = new File(testInputPath + "EXPECTED_DOC1.pdf");
         File createdConcatenatedFile = new File(testOutputRoot + DOC_1_NAME + ".pdf");
         checkFilesAreSame(expectedConcatenatedFile, createdConcatenatedFile);
     }
 
     private void checkDoc2() {
         File expectedConcatenatedFile = new File(testInputPath + "EXPECTED_DOC2.pdf");
         File createdConcatenatedFile = new File(testOutputRoot + DOC_2_NAME + "_" + PRODUCT_MODEL_1 + ".pdf");
         checkFilesAreSame(expectedConcatenatedFile, createdConcatenatedFile);
     }
 
    private void checkDoc3() {
        File expectedConcatenatedFile = new File(testInputPath + "EXPECTED_DOC3.pdf");
        File createdConcatenatedFile = new File(testOutputRoot + DOC_2_NAME + "_" + PRODUCT_MODEL_2 + ".pdf");
        checkFilesAreSame(expectedConcatenatedFile, createdConcatenatedFile);
    }

     private void checkFilesAreSame(File expectedConcatenatedFile, File createdConcatenatedFile) {
         boolean filesAreSame =
                 checksumUtils.filteredFileChecksumsAreSame(expectedConcatenatedFile, createdConcatenatedFile);
 
         String createdName = createdConcatenatedFile.getName();
         String expectedName = expectedConcatenatedFile.getName();
         String message = String.format("File '%s' is not the same as expected file '%s'", createdName, expectedName);
 
         assertTrue(message, filesAreSame);
     }
 
     private WebRequest makeMockRequestWithParamMap() {
         WebRequest mockRequest = mock(WebRequest.class);
         Map<String, String[]> paramMap = new HashMap<>();
         paramMap.put(DocumentFactoryTestImpl.EXAMPLE_FIELD, new String[]{"hello"});
         when(mockRequest.getParameterMap()).thenReturn(paramMap);
         return mockRequest;
     }
 
 }
