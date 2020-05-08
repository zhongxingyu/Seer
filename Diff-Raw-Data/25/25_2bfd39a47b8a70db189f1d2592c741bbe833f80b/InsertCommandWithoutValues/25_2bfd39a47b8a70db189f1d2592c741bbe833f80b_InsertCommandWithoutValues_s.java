 package edu.rivfader.commands;
 
 import edu.rivfader.data.Database;
 import edu.rivfader.data.Row;
 
 import java.io.Writer;
 import java.io.IOException;
 
 import java.util.List;
 
 public class InsertCommandWithoutValues implements ICommand {
     private String tableName;
     private List<String> values;
 
     public InsertCommandWithoutValues(final String pTableName,
             final List<String> pValues) {
         tableName = pTableName;
         values = pValues;
     }
 
     @Override
     public void execute(final Database context, final Writer output)
         throws IOException {
         context.openTableForWriting(tableName);
         List<String> columnNames = context.getColumnNames(tableName);
         Row valueRow = new Row(columnNames);
         for(int i = 0; i < values.size(); i++) {
             valueRow.setData(columnNames.get(i), values.get(i));
         }
         context.storeRow(tableName, valueRow);
         context.closeTable(tableName);
     }
 }
