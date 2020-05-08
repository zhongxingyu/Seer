 package net.codjo.test.release.task.file;
 import net.codjo.test.common.LogString;
 import net.codjo.test.common.PathUtil;
 import net.codjo.test.common.excel.matchers.CellValueStringifier;
 import net.codjo.test.release.TestEnvironment;
 import net.codjo.test.release.TestEnvironmentMock;
 import static net.codjo.test.release.task.AgfTask.BROADCAST_LOCAL_DIR;
 import static net.codjo.test.release.task.AgfTask.TEST_DIRECTORY;
 import net.codjo.test.release.task.util.RemoteCommandMock;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFRichTextString;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.DefaultLogger;
 import org.apache.tools.ant.Project;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import org.junit.Before;
 import org.junit.Test;
 
 public class FileAssertTest {
     protected static final String EXPECTED_FILE_NAME = "fileExpected.txt";
     private FileAssert fileAssert;
     private Project project;
 
 
     @Before
     public void setUp() throws Exception {
         project = new Project();
         project.setProperty(TEST_DIRECTORY, PathUtil.findResourcesFileDirectory(getClass()).getPath());
         project.setProperty(BROADCAST_LOCAL_DIR, PathUtil.findResourcesFileDirectory(getClass()).getPath());
         project.addReference(TestEnvironment.class.getName(), new TestEnvironmentMock(project));
 
         activateAntLog();
 
         fileAssert = new FileAssert();
         fileAssert.setProject(project);
     }
 
 
     @Test
     public void test_plainFile() {
         fileAssert.setActual(EXPECTED_FILE_NAME);
         fileAssert.setExpected("fileActualExpected.txt");
         fileAssert.execute();
     }
 
 
     @Test
     public void test_plainFile_notOk() {
         fileAssert.setActual(EXPECTED_FILE_NAME);
         fileAssert.setExpected("fileActualNotExpected.txt");
 
         assertComparisonFailure("Fichier produit en erreur : " + toFilePath(EXPECTED_FILE_NAME));
     }
 
 
     @Test
     public void test_plainFile_remote() {
         project.setProperty("agf.test.remote", "YES");
 
         fileAssert.setActual(EXPECTED_FILE_NAME);
         fileAssert.setExpected("fileActualExpected.txt");
 
         assertComparisonFailure("manque argument broadcast.output.remote.dir dans le fichier de configuration");
     }
 
 
     @Test
     public void test_plainFile_remote_forceLocal() {
         project.setProperty("agf.test.remote", "YES");
 
         fileAssert.setRemote(false);
         fileAssert.setActual(EXPECTED_FILE_NAME);
         fileAssert.setExpected("fileActualExpected.txt");
 
         fileAssert.execute();
     }
 
 
     @Test
     public void test_excelFile() {
         fileAssert.setActual("FileAssertTest_sameAsexpected.xls");
         fileAssert.setExpected("FileAssertTest_expected.xls");
         fileAssert.execute();
     }
 
 
     @Test
     public void test_excelFile_WithMergeRegion() {
         fileAssert.setActual("FileAssertWithMergeRegion_expected.xls");
         fileAssert.setExpected("FileAssertWithMergeRegion_expected.xls");
         fileAssert.setCompareStyle(true);
         fileAssert.execute();
     }
 
 
     @Test
     public void test_excelFile_WithMergeRegionError() {
         fileAssert.setActual("FileAssertWithMergeRegion_expected.xls");
         fileAssert.setExpected("FileAssertWithMergeRegion_notSameAsExpected.xls");
         fileAssert.setCompareStyle(true);
         assertComparisonFailure("Fichier produit en erreur : "
                                 + toFilePath("FileAssertWithMergeRegion_expected.xls"));
     }
 
 
     @Test
     public void test_excelFile_withBoldStyle() {
         fileAssert.setActual("FileAssertWithBoldStyle_expected.xls");
         fileAssert.setExpected("FileAssertWithBoldStyle_expected.xls");
         fileAssert.setCompareStyle(true);
         fileAssert.execute();
     }
 
 
     @Test
     public void test_excelFile_withBoldStyleError() {
         fileAssert.setActual("FileAssertWithBoldStyle_expected.xls");
         fileAssert.setExpected("FileAssertWithBoldStyle_notSameAsExpected.xls");
         fileAssert.setCompareStyle(true);
         assertComparisonFailure("Fichier produit en erreur : "
                                 + toFilePath("FileAssertWithBoldStyle_expected.xls"));
     }
 
 
     @Test
     public void test_excelFile_withItalicStyle() {
         fileAssert.setActual("FileAssertWithItalicStyle_expected.xls");
         fileAssert.setExpected("FileAssertWithItalicStyle_expected.xls");
         fileAssert.setCompareStyle(true);
         fileAssert.execute();
     }
 
 
     @Test
     public void test_excelFile_withItalicStyleError() {
         fileAssert.setActual("FileAssertWithItalicStyle_expected.xls");
         fileAssert.setExpected("FileAssertWithItalicStyle_notSameAsExpected.xls");
         fileAssert.setCompareStyle(true);
         assertComparisonFailure("Fichier produit en erreur : "
                                 + toFilePath("FileAssertWithItalicStyle_expected.xls"));
     }
 
 
     @Test
     public void test_excelFile_withAlignmentStyle() {
         fileAssert.setActual("FileAssertWithAlignmentStyle_expected.xls");
         fileAssert.setExpected("FileAssertWithAlignmentStyle_expected.xls");
         fileAssert.setCompareStyle(true);
         fileAssert.execute();
     }
 
 
     @Test
     public void test_excelFile_withAlignmentStyleError() {
         fileAssert.setActual("FileAssertWithAlignmentStyle_expected.xls");
         fileAssert.setExpected("FileAssertWithAlignmentStyle_notSameAsExpected.xls");
         fileAssert.setCompareStyle(true);
         assertComparisonFailure("Fichier produit en erreur : "
                                 + toFilePath("FileAssertWithAlignmentStyle_expected.xls"));
     }
 
 
     @Test
     public void test_excelFile_withCellBorder() {
         fileAssert.setActual("FileAssertWithCellBorder_expected.xls");
         fileAssert.setExpected("FileAssertWithCellBorder_expected.xls");
         fileAssert.setCompareStyle(true);
         fileAssert.execute();
     }
 
 
     @Test
     public void test_excelFile_withCellBorderError() {
         fileAssert.setActual("FileAssertWithCellBorder_expected.xls");
         fileAssert.setExpected("FileAssertWithCellBorder_notSameAsExpected.xls");
         fileAssert.setCompareStyle(true);
         assertComparisonFailure(
               "Fichier produit en erreur : " + toFilePath("FileAssertWithCellBorder_expected.xls"));
     }
 
 
     @Test
     public void test_excelFile_withMargin() {
         fileAssert.setActual("FileAssertWithMargin_expected.xls");
         fileAssert.setExpected("FileAssertWithMargin_expected.xls");
         fileAssert.setCompareStyle(true);
         fileAssert.execute();
     }
 
 
     @Test
     public void test_excelFile_withMarginError() {
         fileAssert.setActual("FileAssertWithMargin_expected.xls");
         fileAssert.setExpected("FileAssertWithMargin_notSameAsExpected.xls");
         fileAssert.setCompareStyle(true);
         assertComparisonFailure(
               "Fichier produit en erreur : " + toFilePath("FileAssertWithMargin_expected.xls"));
     }
 
 
     @Test
     public void test_excelFile_withFontSize() {
         fileAssert.setActual("FileAssertWithFontSize_expected.xls");
         fileAssert.setExpected("FileAssertWithFontSize_expected.xls");
         fileAssert.setCompareStyle(true);
         fileAssert.execute();
     }
 
 
     @Test
     public void test_excelFile_withFontSizeError() {
         fileAssert.setActual("FileAssertWithFontSize_expected.xls");
         fileAssert.setExpected("FileAssertWithFontSize_notSameAsExpected.xls");
         fileAssert.setCompareStyle(true);
         assertComparisonFailure(
               "Fichier produit en erreur : " + toFilePath("FileAssertWithFontSize_expected.xls"));
     }
 
 
     @Test
     public void test_excelFile_WithDate() throws Exception {
         fileAssert.setActual("FileAssertWithDateTest_sameAsexpected.xls");
         fileAssert.setExpected("FileAssertWithDateTest_expected.xls");
 
         HSSFWorkbook workbook = new HSSFWorkbook();
         HSSFSheet sheet = workbook.createSheet("sheet1");
         HSSFRow row = sheet.createRow(0);
         HSSFCell cell = row.createCell(0);
         String date = new SimpleDateFormat(CellValueStringifier.DATE_FORMAT).format(new Date());
         cell.setCellValue(new HSSFRichTextString(date));
         //sheet.setColumnWidth((short)0, (short)10);
 
         FileOutputStream outputStream = new FileOutputStream(fileAssert.getActualFile());
         workbook.write(outputStream);
         outputStream.flush();
         outputStream.close();
 
         fileAssert.setCompareStyle(false);
         fileAssert.execute();
         fileAssert.getActualFile().delete();
     }
 
 
     @Test
     public void test_excelFile_contentNotOk() {
         fileAssert.setActual("FileAssertTest_badValue.xls");
         fileAssert.setExpected("FileAssertTest_expected.xls");
 
         assertComparisonFailure("Fichier produit en erreur : " + toFilePath("FileAssertTest_badValue.xls"));
     }
 
 
     @Test
     public void test_excelFile_badSheetCount() {
         fileAssert.setActual("FileAssertTest_badSheetCount.xls");
         fileAssert.setExpected("FileAssertTest_expected.xls");
         fileAssert.setCompareStyle(false);
 
         assertComparisonFailure("Fichier produit en erreur : " + toFilePath("FileAssertTest_badSheetCount.xls"));
     }
 
 
     @Test
     public void test_pdfFile() {
         fileAssert.setActual("FileAssertTest_expected.pdf");
         fileAssert.setExpected("FileAssertTest_expected.pdf");
         fileAssert.execute();
     }
 
 
     @Test
     public void test_pdfFileError() {
         fileAssert.setActual("FileAssertTest_expected.pdf");
         fileAssert.setExpected("FileAssertTest_notSameAsExpected.pdf");
         assertComparisonFailure("Fichier produit en erreur : "
                                 + toFilePath("FileAssertTest_expected.pdf"));
     }
 
 
     @Test
     public void test_compare_localMode() throws Exception {
         LogString log = new LogString();
 
         fileAssert.setActual(EXPECTED_FILE_NAME);
         fileAssert.setExpected("fileActualExpected.txt");
         fileAssert.setCopyFromRemoteCommand(new RemoteCommandMock(log));
         fileAssert.setRemote(false);
 
         fileAssert.execute();
 
         log.assertContent("");
     }
 
 
     @Test
     public void test_compare_remoteMode() throws Exception {
         LogString log = new LogString();
 
         fileAssert.setActual(EXPECTED_FILE_NAME);
         fileAssert.setExpected("fileActualExpected.txt");
         fileAssert.setCopyFromRemoteCommand(new RemoteCommandMock(log));
         project.setProperty("agf.test.remote", "YES");
         fileAssert.setRemote(true);
 
         fileAssert.execute();
 
         log.assertContent("execute()");
     }
 
 
     @Test
     public void test_xmlFile() {
         fileAssert.setActual("FileAssertXml.xml");
         fileAssert.setExpected("FileAssertXml_expected.xml");
 
         fileAssert.execute();
     }
 
 
     @Test
     public void test_xmlFile_notOk() {
         fileAssert.setActual("FileAssertXml.xml");
         fileAssert.setExpected("FileAssertXml_notSameAsExpected.xml");
 
         assertComparisonFailure("Fichier produit en erreur : " + toFilePath("FileAssertXml.xml"));
     }
 
 
     @Test
     public void test_comparisonType_default() {
         fileAssert.setActual("FileAssertComparisonTypeDefault.model");
         fileAssert.setExpected("FileAssertComparisonTypeDefault_expected.model");
 
         fileAssert.execute();
     }
 
 
     @Test
     public void test_comparisonType_xls() {
         fileAssert.setActual("FileAssertComparisonTypeXls.model");
         fileAssert.setExpected("FileAssertComparisonTypeXls_expected.model");
         fileAssert.setComparisonType("xls");
 
         fileAssert.execute();
     }
 
 
     @Test
     public void test_comparisonType_pdf() {
         fileAssert.setActual("FileAssertComparisonTypePdf.model");
         fileAssert.setExpected("FileAssertComparisonTypePdf_expected.model");
         fileAssert.setComparisonType("pdf");
 
         fileAssert.execute();
     }
 
 
     @Test
     public void test_comparisonType_xml() {
         fileAssert.setActual("FileAssertComparisonTypeXml.model");
         fileAssert.setExpected("FileAssertComparisonTypeXml_expected.model");
         fileAssert.setComparisonType("xml");
 
         fileAssert.execute();
     }
 
 
     @Test
     public void test_comparisonType_unknown() throws Exception {
         fileAssert.setActual("FileAssertComparisonTypeDefault.model");
         fileAssert.setExpected("FileAssertComparisonTypeDefault_expected.model");
         fileAssert.setComparisonType("unknown");
 
         assertComparisonFailure("Valeur incorrecte pour l'attribut comparisonType (unknown).");
     }
 
 
     private void assertComparisonFailure(String errorMessage) {
         try {
             fileAssert.execute();
             fail();
         }
         catch (BuildException e) {
             assertEquals(errorMessage, e.getMessage());
         }
     }
 
 
     private String toFilePath(String fileName) {
         String resourcePath = PathUtil.findResourcesFileDirectory(getClass()).getPath();
        return new File(resourcePath, fileName).getPath();
     }
 
 
     private void activateAntLog() {
         DefaultLogger listener = new DefaultLogger();
         listener.setMessageOutputLevel(Project.MSG_INFO);
         //noinspection UseOfSystemOutOrSystemErr
         listener.setOutputPrintStream(System.out);
         project.addBuildListener(listener);
     }
 }
