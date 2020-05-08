 package edu.rivfader.commands;
 
 import edu.rivfader.data.Database;
 import edu.rivfader.data.Row;
 import edu.rivfader.relalg.ITable;
 import edu.rivfader.relalg.IQualifiedNameRow;
 import edu.rivfader.relalg.QualifiedNameRow;
 import edu.rivfader.relalg.IQualifiedColumnName;
 import edu.rivfader.errors.NoColumnValueMappingPossible;
 import java.io.Writer;
 import java.io.IOException;
 
 import java.util.List;
 
 /**
  * This command implements inserting without specifying the columns.
  * @author harald
  */
 public class InsertCommandWithoutValues implements ICommand {
     /**
      * contains the table to insert into.
      */
     private ITable table;
     /**
      * contains the values to set.
      */
     private List<String> values;
 
     /**
      * constructs a new insertion node.
      * @param pTableName the name of the table to modify.
      * @param pValues the values to insert into the table
      */
     public InsertCommandWithoutValues(final ITable pTable,
             final List<String> pValues) {
         table = pTable;
         values = pValues;
     }
 
     @Override
     public void execute(final Database context, final Writer output)
         throws IOException {
         List<IQualifiedColumnName> cns; // column names
         IQualifiedNameRow vr; // value row
 
         table.setDatabase(context);
         cns = table.getColumnNames();
         if(cns.size() != values.size()) {
             throw new NoColumnValueMappingPossible(
                    "Incorrect amount of values for table");
         }
 
         vr = new QualifiedNameRow(cns);
         for(int i = 0; i < values.size(); i++) {
             vr.setData(cns.get(i), values.get(i));
         }
         table.appendRow(vr);
     }
 }
