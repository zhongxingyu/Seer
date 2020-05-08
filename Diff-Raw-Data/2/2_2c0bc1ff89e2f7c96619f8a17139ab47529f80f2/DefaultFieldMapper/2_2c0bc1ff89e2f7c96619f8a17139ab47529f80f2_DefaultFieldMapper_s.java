 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mycompany.logtransformer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author dchakr
  */
 public class DefaultFieldMapper extends FieldMapper {
 
   @Override
   char getDelimiter() {
     return new Character('|');
   }
 
   @Override
   List<Integer> getFieldPositions() {
     List<Integer> fieldPositions = new ArrayList<Integer>();
     fieldPositions.add(new Integer(0));
     fieldPositions.add(new Integer(1));
     fieldPositions.add(new Integer(2));
     fieldPositions.add(new Integer(3));
     fieldPositions.add(new Integer(4));
     fieldPositions.add(new Integer(5));
     fieldPositions.add(new Integer(8));
    fieldPositions.add(new Integer(8));
     fieldPositions.add(new Integer(10));
     fieldPositions.add(new Integer(11));
     fieldPositions.add(new Integer(15));
     fieldPositions.add(new Integer(18));
 
     return fieldPositions;
   }
 
   @Override
   Map<Integer, String> getFieldNames() {
     Map<Integer, String> fieldNames = new HashMap<Integer, String>();
     fieldNames.put(new Integer(0), "field0");
     fieldNames.put(new Integer(1), "field1");
     fieldNames.put(new Integer(2), "field2");
     fieldNames.put(new Integer(3), "field3");
     fieldNames.put(new Integer(4), "field4");
     fieldNames.put(new Integer(5), "field5");
     fieldNames.put(new Integer(8), "field8");
     fieldNames.put(new Integer(9), "field9");
     fieldNames.put(new Integer(10), "field10");
     fieldNames.put(new Integer(11), "field11");
     fieldNames.put(new Integer(15), "field15");
     fieldNames.put(new Integer(18), "field18");    
 
     return fieldNames;
   }
 
   @Override
   String getStartIdentifier() {
     return "SYS-LOG";
   }
 }
