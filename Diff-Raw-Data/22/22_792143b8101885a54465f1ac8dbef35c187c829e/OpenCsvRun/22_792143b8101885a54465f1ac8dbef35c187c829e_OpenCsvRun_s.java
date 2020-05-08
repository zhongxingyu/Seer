 package org.sample.main;
 
 import au.com.bytecode.opencsv.CSVReader;
 import au.com.bytecode.opencsv.bean.CsvToBean;
 import au.com.bytecode.opencsv.bean.OrderLessHeaderColumnNameMappingStrategy;
 import org.sample.domain.Person;
 
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * @author Jatinder Singh on 2012-11-21 at 10:06 PM
  */
 public class OpenCsvRun {
     public static void main(String[] args) {
         OrderLessHeaderColumnNameMappingStrategy orderLessHeaderImpl = new OrderLessHeaderColumnNameMappingStrategy();
 
         // Map rows in CSV file to Person bean
         orderLessHeaderImpl.setType(Person.class);
 
        // Create a HashMap of columns to be read
        HashMap<String, Integer> columnsToMap = new HashMap<String, Integer>();

        // Put the value as null. The values will be later filled
        // when the original header from CSV file gets pulled.
        columnsToMap.put("fname", null);
        columnsToMap.put("telephone", null);
        columnsToMap.put("id", null);
 
         // Set the columns to be mapped
         orderLessHeaderImpl.setHeaderToMap(columnsToMap);
 
         CsvToBean csvToBean = new CsvToBean();
         List<Person> list = null;
 
         CSVReader reader = new CSVReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("test.csv")));
         list = csvToBean.parse(orderLessHeaderImpl, reader);
 
         for(Person person : list) {
             System.out.println(person);
         }
     }
 }
