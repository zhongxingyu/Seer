 package org.tmcdb.heapfile.cursor;
 
 import org.jetbrains.annotations.NotNull;
 import org.tmcdb.engine.data.Row;
 import org.tmcdb.engine.schema.Column;
 
 import java.io.IOException;
 import java.io.PrintStream;
 
 /**
  * @author Pavel Talanov
  */
 public final class CursorUtils {
 
     private CursorUtils() {
     }
 
     public static void toCSV(@NotNull Cursor cursor, @NotNull PrintStream output) throws IOException {
         Row row = cursor.next();
         if (row != null) {
             output.print(rowSchemaToCSV(row));
         }
         while (row != null) {
             output.println(rowToCSV(row));
             row = cursor.next();
         }
     }
 
     @NotNull
     private static String rowToCSV(@NotNull Row row) {
         StringBuilder sb = new StringBuilder();
         boolean first = true;
         for (Column column : row.getColumns()) {
             if (!first) {
                 sb.append(",");
             }
             first = false;
            sb.append(row.getValueForColumn(column));
         }
         return sb.toString();
     }
 
     @NotNull
     private static String rowSchemaToCSV(@NotNull Row row) {
         StringBuilder sb = new StringBuilder();
         boolean first = true;
         for (Column column : row.getColumns()) {
             if (!first) {
                 sb.append(",");
             }
             first = false;
             sb.append(column.getName()).append("(").append(column.getType().getPresentableName()).append(")");
         }
         sb.append(System.getProperty("line.separator"));
         return sb.toString();
     }
 }
