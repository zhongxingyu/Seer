 package lab4;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Set;
 
 /**
  *
  * @author Mark Van Weelden <mvanweelden1@my.wctc.edu>
  */
 public class CsvCommaFormat implements FormatStrategy<List<LinkedHashMap<String, String>>,List<String>>{
     
     private static final String CRLF = "\n";
     private static final String COMMA = ",";
     private boolean hasHeader;
 
     public CsvCommaFormat(boolean hasHeader) {
         this.hasHeader = hasHeader;
     }
     
     
 
     @Override
     public List<LinkedHashMap<String, String>> decode(List<String> dataFromSrc) {
        List<LinkedHashMap<String, String>> decodedData =
                 new ArrayList<LinkedHashMap<String, String>>();
 
         String[] headerFields = null;
         for (int recordNo = 0; recordNo < dataFromSrc.size(); recordNo++) {
             String firstRow = dataFromSrc.get(recordNo);
             
             String[] fields = dataFromSrc.get(recordNo).split(COMMA);
             if (hasHeader && (recordNo == 0)) { // first record may be header
                 headerFields = fields;
 //                continue;
             }
 
             LinkedHashMap<String, String> record =
                     new LinkedHashMap<String, String>();
             for (int i = 0; i < fields.length; i++) {
                 if (hasHeader && (recordNo == 0)) { // zero is first record, could be header
                     break; // not a record so skip following code
                     // because it's a header but has no data values
 
                     // if header included, we store header info as key and data value
                 } else if (hasHeader) {
                     record.put(headerFields[i], fields[i]);
 
                     // if no header we create an artificial key from a counter and add value
                 } else {
                     record.put("" + i, fields[i]);
                 }
             }
 
             // Only add the record if it's not the first row (header)
            if (recordNo != 0) {
                 decodedData.add(record);
             }
         }
 
         return decodedData;
     }
 
     @Override
     public String encode(List<LinkedHashMap<String, String>> dataFromSrc) {
         StringBuilder formattedData = new StringBuilder();
 
         boolean headerNotSet = true;
         Set<String> fieldNames = null;
         if (hasHeader) {
             fieldNames = dataFromSrc.get(0).keySet();
         }
 
         for (int recordNo = 0; recordNo < dataFromSrc.size(); recordNo++) {
             if (fieldNames != null && headerNotSet) {
                 for (String fieldName : fieldNames) {
                     // using quoted values to eliminate problems with
                     // embedded commas in a data value
                     formattedData.append(fieldName);
                     formattedData.append(COMMA);
                 }
                 // remove trailing comma
                 formattedData.deleteCharAt(formattedData.length() - 1);
                 formattedData.append(CRLF);
                 addDataValues(dataFromSrc, recordNo, formattedData);
                 headerNotSet = false;
 
             } else {
                 addDataValues(dataFromSrc, recordNo, formattedData);
             }
         }
 
         // Here's the CSV formatted data as a single String that can be
         // saved to a file.
         return formattedData.toString();
     }
     
     private void addDataValues(List<LinkedHashMap<String, String>> dataFromFile, int recordNo, StringBuilder formattedData) {
         // write a normal data row
         Collection<String> valueCol = dataFromFile.get(recordNo).values();
         for (String value : valueCol) {
             // using quoted values to eliminate problems with
             // embedded commas in a data value
             formattedData.append(value);
             formattedData.append(COMMA);
         }
         // remove trailing comma
         formattedData.deleteCharAt(formattedData.length() - 1);
         formattedData.append(CRLF);
     }
 
 }
