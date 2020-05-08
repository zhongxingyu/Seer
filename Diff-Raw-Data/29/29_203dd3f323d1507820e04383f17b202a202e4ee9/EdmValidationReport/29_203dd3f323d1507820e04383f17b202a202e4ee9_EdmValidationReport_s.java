 /* CollectionValidation.java - created on 20 de Ago de 2012, Copyright (c) 2011 The European Library, all rights reserved */
 package org.theeuropeanlibrary.uim.check.edm;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * The outcome of a schema validation of a collection
  * 
  * @author Nuno Freire (nfreire@gmail.com)
  * @since 20 de Ago de 2012
  */
 public class EdmValidationReport {
     private int                      recordCount;
     private int                      invalidRecords;
 
     private HashMap<String, Integer> errorMessagesCounts = new HashMap<String, Integer>();
 
     /**
      * A record was validated without errors
      */
     public void addValidRecord() {
         recordCount++;
     }
 
     /**
      * A record was validated with errors
      * 
      * @param errors
      */
     public void addInvalidRecord(List<String> errors) {
         recordCount++;
         invalidRecords++;
         for (String error : new HashSet<String>(errors)) {
            Integer msgCount = errorMessagesCounts.get(error);
            if (msgCount == null)
                errorMessagesCounts.put(error, 1);
            else
                errorMessagesCounts.put(error, msgCount + 1);
         }
     }
 
     /**
      * @return total records reported
      */
     public int getRecordCount() {
         return recordCount;
     }
 
     /**
      * @return total records with errors
      */
     public int getInvalidRecords() {
         return invalidRecords;
     }
 
     /**
      * @return overall error counts by message
      */
     public Map<String, Integer> getErrorMessagesCounts() {
         return errorMessagesCounts;
     }
 
     /**
      * @return overall error counts by message sorted by occurence
      */
     public List<Entry<String, Integer>> getErrorMessagesCountsSorted() {
         ArrayList<Entry<String, Integer>> entries = new ArrayList<Entry<String, Integer>>(
                 errorMessagesCounts.entrySet());
         Collections.sort(entries, new Comparator<Entry<String, Integer>>() {
             @Override
             public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                 return o2.getValue() - o1.getValue();
             }
         });
         return entries;
     }
 
     /**
      * @return total records without errors
      */
     public int getValidRecords() {
         return recordCount - invalidRecords;
     }
 
     /**
      * @return Percentage of valid records
      */
     public float getValidRecordsPercent() {
         return (float)getValidRecords() * 100 / recordCount;
     }
 }
