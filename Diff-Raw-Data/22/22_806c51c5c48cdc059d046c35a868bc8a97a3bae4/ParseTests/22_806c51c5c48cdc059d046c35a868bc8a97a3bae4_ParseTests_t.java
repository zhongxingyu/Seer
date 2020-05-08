 package sqlParser;
 
 import dbCommands.*;
 import dbEnvironment.DbContext;
 import junit.framework.Assert;
 import org.junit.Test;
 import queryParser.SQLParser;
 import queryParser.SingleCondition;
 import tableTypes.ColumnTuple;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: maratx
  * Date: 1/21/14
  * Time: 9:12 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ParseTests {
 
     private String CreateTestTable(DbContext context) {
 
         ColumnTuple columnTuple1 = new ColumnTuple("column1", 4, "int");
         ColumnTuple columnTuple2 = new ColumnTuple("column2", 16, "char");
         ColumnTuple columnTuple3 = new ColumnTuple("col3", 2, "char");
         ArrayList<ColumnTuple> tuples = new ArrayList<ColumnTuple>();
         tuples.add(columnTuple1);
         tuples.add(columnTuple2);
         tuples.add(columnTuple3);
         String tableName = "SelectAllTestTable";
         CreateTableCommand createTableCommand = new CreateTableCommand(tableName, tuples);
 
         createTableCommand.executeCommand(context);
 
         ArrayList<String> args = new ArrayList<String>();
         ArrayList<TableRow> rows = new ArrayList<TableRow>();
         int numberOfRows = 10;
 
         for (int i = 0; i < numberOfRows; ++i)
         {
             args.add(Integer.toString(i));
             args.add("abc" + Integer.toString(i));
             args.add("ccc" + Integer.toString(i));
             TableRow tableRow = new TableRow(args);
             rows.add(tableRow);
             args.clear();
         }
 
         return tableName;
     }
 
     @Test
     public void SelectAllTest() {
         DbContext context = new DbContext(RESOURCE_PATH);
         String tableName = CreateTestTable(context);
         String query = "SELECT * FROM " + tableName;
 
        SQLParser parser = new SQLParser(context);
        SelectAllRowsCommand command = (SelectAllRowsCommand) parser.parse(query);
         Assert.assertEquals(tableName, command.tableName());
 
         context.close();
     }
 
     @Test
     public void SelectWhereTest() {
         DbContext context = new DbContext(RESOURCE_PATH);
         String tableName = CreateTestTable(context);
         String query = "SELECT * FROM " + tableName + " WHERE column1 > 3 AND col3 <> \"ccc5\"";
 
        SQLParser parser = new SQLParser(context);
        SelectCommand command = (SelectCommand) parser.parse(query);
         RowPredicate predicate = command.predicate();
         List<SingleCondition> conditions = predicate.conditions();
 
         Assert.assertEquals("{0}", conditions.get(0)._val1);
         Assert.assertEquals(">", conditions.get(0)._operator);
         Assert.assertEquals("3", conditions.get(0)._val2);
 
         Assert.assertEquals("{2}", conditions.get(1)._val1);
         Assert.assertEquals("<>", conditions.get(1)._operator);
         Assert.assertEquals("\"ccc5\"", conditions.get(1)._val2);
 
         List<Object> test_row1 = new ArrayList<Object>();
         test_row1.add(4);
         test_row1.add("\"abc4\"");
         test_row1.add("\"ccc4\"");
 
         List<Object> test_row2 = new ArrayList<Object>();
         test_row2.add(5);
         test_row2.add("\"abc5\"");
         test_row2.add("\"ccc5\"");
 
         Assert.assertEquals(predicate.evaluate(test_row1), true);
         Assert.assertEquals(predicate.evaluate(test_row2), false);
     }
 
     private static final String RESOURCE_PATH = "/home/maratx/GitRepos/JavaDBMS/src/test/resources/";
 }
