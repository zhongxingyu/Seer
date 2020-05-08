 package frontend.editorTable;
 
 import core.service.RessourceService;
 import fit.Parse;
 import fit.exception.FitParseException;
 import fitArchitectureAdapter.HtmlTableUtils;
 import org.apache.commons.io.FileUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 
 import javax.swing.table.AbstractTableModel;
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 public class FitTableModelTest {
 
   private HtmlTableUtils htmlTableUtils;
 
   @Mock
   private AbstractTableModel tableModel;
 
   @Before
   public void setUp() {
     htmlTableUtils = new HtmlTableUtils();
     MockitoAnnotations.initMocks(this);
     Mockito.when(tableModel.getColumnCount()).thenReturn(3);
   }
 
   @Test
   public void addEmptyRow() {
     Parse tableWithFixture = htmlTableUtils.createTableWithEmptyFixture(3);
     FitTableModel fitTableModel = new FitTableModel(tableWithFixture, tableModel);
     fitTableModel.addEmptyRow(3);
 
     assertTrue(isRowInTableEmpty(tableWithFixture, 1));
   }
 
   @Test
   public void deleteRow() {
     Parse tableWithFixture = htmlTableUtils.createTableWithEmptyFixture(3);
     FitTableModel fitTableModel = new FitTableModel(tableWithFixture, tableModel);
     fillWithRows(fitTableModel, 5);
     assertTrue(isRowFilledWith(tableWithFixture, 1, "1"));
     assertTrue(isRowFilledWith(tableWithFixture, 2, "2"));
     assertTrue(isRowFilledWith(tableWithFixture, 3, "3"));
     assertTrue(isRowFilledWith(tableWithFixture, 4, "4"));
     assertTrue(isRowFilledWith(tableWithFixture, 5, "5"));
 
     fitTableModel.deleteFirstRow();
     assertEquals(5, fitTableModel.getRowCountWithFixtureRow());
     assertTrue(isRowFilledWith(tableWithFixture, 1, "2"));
     assertTrue(isRowFilledWith(tableWithFixture, 2, "3"));
     assertTrue(isRowFilledWith(tableWithFixture, 3, "4"));
     assertTrue(isRowFilledWith(tableWithFixture, 4, "5"));
 
     fitTableModel.deleteLastRow();
     assertEquals(4, fitTableModel.getRowCountWithFixtureRow());
     assertTrue(isRowFilledWith(tableWithFixture, 1, "2"));
     assertTrue(isRowFilledWith(tableWithFixture, 2, "3"));
     assertTrue(isRowFilledWith(tableWithFixture, 3, "4"));
 
     fitTableModel.deleteRowByTableIndex(1);
     assertEquals(3, fitTableModel.getRowCountWithFixtureRow());
     assertTrue(isRowFilledWith(tableWithFixture, 1, "2"));
     assertTrue(isRowFilledWith(tableWithFixture, 2, "4"));
 
     fitTableModel.deleteRowByTableIndex(0);
     assertEquals(2, fitTableModel.getRowCountWithFixtureRow());
     assertTrue(isRowFilledWith(tableWithFixture, 1, "4"));
 
     fitTableModel.deleteRowByTableIndex(0);
     assertEquals(1, fitTableModel.getRowCountWithFixtureRow());
   }
 
   @Test
   public void deleteRowsWithRealTestFile() throws URISyntaxException, IOException, FitParseException {
    File testFile = new RessourceService().loadRessourceFile(FitTableModel.class,"test.html");
     String content = FileUtils.readFileToString(testFile);
     Parse table = new Parse(content);
 
     FitTableModel fitTableModel = new FitTableModel(table, tableModel);
     assertEquals(4, fitTableModel.getRowCountWithoutFixture());
 
     fitTableModel.deleteRowByTableIndex(2);
     assertEquals(3, fitTableModel.getRowCountWithoutFixture());
   }
 
   @Test
   public void addRowAt() {
     Parse tableWithFixture = htmlTableUtils.createTableWithEmptyFixture(3);
     FitTableModel fitTableModel = new FitTableModel(tableWithFixture, tableModel);
 
     List<String> firstRowContent = createRow(1);
     Parse firstRow = htmlTableUtils.createFilledRow(firstRowContent);
     fitTableModel.addRowAtByTableIndex(0, firstRow);
     assertTrue(isRowFilledWith(tableWithFixture, 1, "1"));
 
     List<String> secondRowContent = createRow(2);
     Parse secondRow = htmlTableUtils.createFilledRow(secondRowContent);
     fitTableModel.addRowAtByTableIndex(0, secondRow);
     assertTrue(isRowFilledWith(tableWithFixture, 1, "2"));
     assertTrue(isRowFilledWith(tableWithFixture, 2, "1"));
   }
 
   @Test
   public void setFixtureName() {
     Parse tableWithFixture = htmlTableUtils.createTableWithEmptyFixture(3);
     FitTableModel fitTableModel = new FitTableModel(tableWithFixture, tableModel);
 
     assertEquals("", fitTableModel.getFixtureName());
 
     fitTableModel.setFixtureName("newFixtureName");
     String fixtureNameInModel = fitTableModel.getFixtureName();
     assertEquals("newFixtureName", fixtureNameInModel);
 
     fitTableModel.setNewTable(null);
     fitTableModel.setFixtureName("abcd");
     String fixtureName = fitTableModel.getFixtureName();
     assertNull(fixtureName);
   }
 
   @Test
   public void getColumnCount() {
     Parse tableWithFixture = htmlTableUtils.createTableWithEmptyFixture(3);
     FitTableModel fitTableModel = new FitTableModel(tableWithFixture, tableModel);
     int columnCount = fitTableModel.getColumnCount();
     assertEquals(3, columnCount);
 
     fitTableModel.setNewTable(null);
     columnCount = fitTableModel.getColumnCount();
     assertEquals(0, columnCount);
   }
 
   @Test
   public void getRowCountWithFixture() {
     Parse tableWithFixture = htmlTableUtils.createTableWithEmptyFixture(3);
     FitTableModel fitTableModel = new FitTableModel(tableWithFixture, tableModel);
 
     fillWithRows(fitTableModel, 5);
     assertEquals(6, fitTableModel.getRowCountWithFixtureRow());
 
     fitTableModel.setNewTable(null);
     assertEquals(0, fitTableModel.getRowCountWithFixtureRow());
   }
 
   @Test
   public void getRowCountWithoutFixture() {
     Parse tableWithFixture = htmlTableUtils.createTableWithEmptyFixture(3);
     FitTableModel fitTableModel = new FitTableModel(tableWithFixture, tableModel);
 
     fillWithRows(fitTableModel, 5);
     assertEquals(5, fitTableModel.getRowCountWithoutFixture());
 
     fitTableModel.setNewTable(null);
     assertEquals(0, fitTableModel.getRowCountWithoutFixture());
   }
 
   private void fillWithRows(FitTableModel table, int rows) {
     for (int i = 1; i <= rows; i++) {
       List<String> secondRowContent = createRow(i);
       Parse secondRow = htmlTableUtils.createFilledRow(secondRowContent);
       table.addRowAtByTableIndex(i - 1, secondRow);
     }
   }
 
   private List<String> createRow(int rowNumber) {
     List<String> rowContent = new ArrayList<String>();
     for (int i = 0; i < 3; i++) {
       rowContent.add("" + rowNumber);
     }
     return rowContent;
   }
 
   private boolean isRowInTableEmpty(Parse table, int row) {
     return isRowFilledWith(table, row, "");
   }
 
   private boolean isRowFilledWith(Parse table, int row, String content) {
     Parse columns = table.at(0, row).parts;
     boolean isRowEmpty = true;
     while (columns != null) {
       if (!columns.text().equals(content)) {
         isRowEmpty = false;
       }
       columns = columns.more;
     }
     return isRowEmpty;
 
   }
 }
