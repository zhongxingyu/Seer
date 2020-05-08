 package au.com.bytecode.opencsv.bean;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import java.io.IOException;
 import java.util.HashMap;
 import static java.util.Map.Entry;
 
 /**
  * @author Jatinder Singh on 2012-11-21 at 9:56 PM
  */
 public class OrderLessHeaderColumnNameMappingStrategy extends HeaderColumnNameMappingStrategy {
     /**
      * Header specified by user to be mapped to a bean
      */
     protected HashMap<String, Integer> columnsToMap;
 
     /**
      * Captures the file header and maps the position of the
      * requested header to the original in the CSV file
      *
      * @param reader
      * @throws IOException
      */
     @Override
     public void captureHeader(CSVReader reader) throws IOException {
         header = reader.readNext();
 
         for (int i = 0; i < header.length; i++) {
             String headerName = header[i].trim();
             if (columnsToMap.containsKey(headerName)) {
                 columnsToMap.remove(headerName);
                 columnsToMap.put(headerName, i);
             }
         }
     }
 
     /**
      * Overriden to consult headerToMap data member to return only the
      * asked column name
      *
      * @param col
      * @return
      */
     @Override
     protected String getColumnName(int col) {
         if (columnsToMap != null) {
             for (Entry<String, Integer> entry : columnsToMap.entrySet()) {
                 if (entry.getValue().equals(col)) {
                     return entry.getKey();
                 }
             }
         }
 
         return null;
     }
 
     /**
      * Sets the headers to map in a CSV file
      *
      * @param headerToMap
      */
    public void setHeaderToMap(String[] headerToMap) {
        columnsToMap = new HashMap<String, Integer>();
        for(String column : headerToMap) {
            columnsToMap.put(column, null);
        }
     }
 }
