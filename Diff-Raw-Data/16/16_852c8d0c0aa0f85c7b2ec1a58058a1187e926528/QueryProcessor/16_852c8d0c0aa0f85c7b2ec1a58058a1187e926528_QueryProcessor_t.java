 package com.develogical;
 
 public class QueryProcessor {
 
     public String process(String query) {
         System.out.println(query);
         if (query.contains("name")) {
             return "Falling Sunset";
         }
        return null;
     }
 }
