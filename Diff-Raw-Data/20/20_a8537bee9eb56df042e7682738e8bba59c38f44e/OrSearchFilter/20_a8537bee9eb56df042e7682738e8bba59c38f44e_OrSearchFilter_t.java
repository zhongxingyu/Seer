 package com.clouway.cuse.spi;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author Ivan Lazov <ivan.lazov@clouway.com>
  */
 public class OrSearchFilter implements SearchFilter {
 
   private List<String> listOfValues = new ArrayList<String>();
 
   public OrSearchFilter(String... values) {
     Collections.addAll(listOfValues, values);
   }
 
   public OrSearchFilter(List<Long> values) {
 
     for (Long value : values) {
       listOfValues.add(String.valueOf(value));
     }
   }
 
   public String getValue() {
 
     StringBuilder builder = new StringBuilder();

    if (listOfValues.size() > 0) {
      builder.append("(");
    }
 
     for (int i = 0; i < listOfValues.size(); i++) {
 
       String value = listOfValues.get(i);
 
       if (i < listOfValues.size() - 1) {
         builder.append(value).append(" OR ");
       } else {
         builder.append(value).append(")");
       }
     }
 
     return builder.toString();
   }
 }
