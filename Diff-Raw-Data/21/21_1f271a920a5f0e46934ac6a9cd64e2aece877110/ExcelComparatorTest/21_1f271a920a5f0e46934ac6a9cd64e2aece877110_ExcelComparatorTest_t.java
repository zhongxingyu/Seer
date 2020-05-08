 package net.codjo.test.common.excel;
 import net.codjo.test.common.excel.matchers.BorderSheetStyleMatcher;
 import net.codjo.test.common.excel.matchers.FontSizeSheetStyleMatcher;
 import net.codjo.test.common.excel.matchers.SheetMatcher;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.junit.Assert;
 import org.junit.Test;
 /**
  *
  */
 public class ExcelComparatorTest {
 
     private static final List<String> SHEETS_TO_ASSERT_EMPTY_LIST = Collections.emptyList();
     private static final List<SheetMatcher> SHEET_MATCHER_EMPTY_LIST = Collections.emptyList();
 
 
     @Test
     public void test_assertContent() throws Exception {
         assertExcelFileOk("actual.xls",
                           "expected_ok.xls",
                           SHEETS_TO_ASSERT_EMPTY_LIST,
                           SHEET_MATCHER_EMPTY_LIST);
     }
 
 
     @Test
     public void test_blankLines() throws Exception {
         assertExcelFileOk("actual_blanklines.xls",
                           "expected_blanklines.xls",
                           SHEETS_TO_ASSERT_EMPTY_LIST,
                           SHEET_MATCHER_EMPTY_LIST);
     }
 
 
     @Test
     public void test_blankLinesKo() throws Exception {
         assertExcelFileKo("actual_blanklines_ko.xls",
                           "expected_blanklines.xls",
                           "Contenu de la feuille 'Feuil1' en erreur.\n"
                           + "Excel line 3\n"
                           + "\tExpected > 2 | 13/05/2008 | toto |  |  |  |  |  |\n"
                           + "\tActual   >  |  |  |  |  |  |  |  |\n"
                           + "\tGap      > *\n",
                           SHEETS_TO_ASSERT_EMPTY_LIST,
                           SHEET_MATCHER_EMPTY_LIST);
     }
 
 
     @Test
     public void test_assertContentBadContent() throws Exception {
         final String expectedErrorMessage = "Contenu de la feuille 'Feuil1' en erreur.\n"
                                             + "Excel line 1\n"
                                             + "\tExpected > 1 | 18/02/1978 |  | pipo | titi |\n"
                                             + "\tActual   > 1 | 18/02/1978 |  | pipo |\n"
                                             + "\tGap      > __________________________*\n";
         assertExcelFileKo("actual.xls",
                           "expected_bad_column_count.xls",
                           expectedErrorMessage,
                           SHEETS_TO_ASSERT_EMPTY_LIST,
                           SHEET_MATCHER_EMPTY_LIST);
     }
 
 
     @Test
     public void test_assertContentBadSheetWrongOrder() throws Exception {
         final String expectedErrorMessage =
               "Les deux classeurs Excel ne contiennent pas les mmes feuilles\n"
               + "expected = Feuil1, Feuil2\n"
               + "actual   = Feuil2, Feuil1\n";
         assertExcelFileKo("actual.xls", "expected_bad_sheets_wrong_order.xls",
                           expectedErrorMessage,
                           SHEETS_TO_ASSERT_EMPTY_LIST, SHEET_MATCHER_EMPTY_LIST);
     }
 
 
     @Test
    public void test_assertContentAndStyleOk() throws Exception {
         assertExcelFileOk("actual.xls", "expected_ok.xls",
                           SHEETS_TO_ASSERT_EMPTY_LIST,
                           Arrays.<SheetMatcher>asList(new BorderSheetStyleMatcher(),
                                                       new FontSizeSheetStyleMatcher()));
     }
 
 
     @Test
    public void test_assertContentAndStyleKo() throws Exception {
         final String expectedErrorMessage =
               "Style bordure des cellules de la feuille 'Feuil1' en erreur.\n"
               + "Excel line 1\n"
               + "\tExpected > T=BORDER_NONE/B=BORDER_THICK/L=BORDER_NONE/R=BORDER_THICK | T=BORDER_NONE/B=BORDER_THICK/L=BORDER_THICK/R=BORDER_NONE | T=BORDER_NONE/B=BORDER_NONE/L=BORDER_NONE/R=BORDER_NONE | T=BORDER_NONE/B=BORDER_NONE/L=BORDER_NONE/R=BORDER_NONE |\n"
               + "\tActual   > T=BORDER_THIN/B=BORDER_THIN/L=BORDER_THIN/R=BORDER_THIN | T=BORDER_THIN/B=BORDER_THIN/L=BORDER_THIN/R=BORDER_THIN | T=BORDER_NONE/B=BORDER_NONE/L=BORDER_NONE/R=BORDER_NONE | T=BORDER_NONE/B=BORDER_NONE/L=BORDER_NONE/R=BORDER_NONE |\n"
               + "\tGap      > _________*\n";
 
         assertExcelFileKo("actual.xls", "expected_style_ko.xls",
                           expectedErrorMessage,
                           SHEETS_TO_ASSERT_EMPTY_LIST,
                           Arrays.<SheetMatcher>asList(new BorderSheetStyleMatcher(),
                                                       new FontSizeSheetStyleMatcher()));
     }
 
 
     @Test
     public void test_assertContentOnlyTwoSheets() throws Exception {
         assertExcelFileOk("actual.xls", "expected_ok_three_sheets.xls",
                           Arrays.<String>asList("Feuil1", "Feuil2"),
                           SHEET_MATCHER_EMPTY_LIST);
     }
 
 
     @Test
     public void test_assertContentOneSheet() throws Exception {
         assertExcelFileOk("actual.xls", "expected_ok_one_sheet.xls",
                           Arrays.<String>asList("Feuil1"),
                           SHEET_MATCHER_EMPTY_LIST);
     }
 
 
     @Test
     public void test_assertContentBadSheet() throws Exception {
         final String expectedErrorMessage =
               "Les deux classeurs Excel ne contiennent pas les mmes feuilles\n"
               + "expected = boum\n"
               + "actual   = Feuil2, Feuil1\n";
         assertExcelFileKo("actual.xls", "expected_bad_sheets.xls",
                           expectedErrorMessage,
                           SHEETS_TO_ASSERT_EMPTY_LIST, SHEET_MATCHER_EMPTY_LIST);
     }
 
 
     private void assertExcelFileOk(String actualFilename,
                                    String expectedFilename,
                                   List<String> sheetsToAssert,
                                   List<SheetMatcher> sheetMatcherList) throws IOException {
         final HSSFWorkbook actualWorkbook = ExcelUtil.loadWorkbook(new File(getClass().getResource(
               actualFilename).getPath()));
         final HSSFWorkbook expectedWorkbook = ExcelUtil.loadWorkbook(new File(getClass().getResource(
               expectedFilename).getPath()));
 
         Assert.assertTrue(ExcelComparator.execute(expectedWorkbook, actualWorkbook,
                                                  sheetsToAssert, sheetMatcherList));
     }
 
 
     private void assertExcelFileKo(String actualFilename, String expectedFilename,
                                    String expectedErrorMessage,
                                   List<String> sheetsToAssert,
                                   List<SheetMatcher> sheetMatcherList)
           throws IOException {
         final HSSFWorkbook actualWorkbook = ExcelUtil.loadWorkbook(new File(getClass().getResource(
               actualFilename).getPath()));
         final HSSFWorkbook expectedWorkBook = ExcelUtil.loadWorkbook(new File(getClass().getResource(
               expectedFilename).getPath()));
         try {
             ExcelComparator.execute(expectedWorkBook, actualWorkbook,
                                    sheetsToAssert, sheetMatcherList);
             Assert.fail();
         }
         catch (ExcelMatchingException ex) {
             Assert.assertEquals(expectedErrorMessage,
                                 ex.getMessage());
         }
     }
 }
