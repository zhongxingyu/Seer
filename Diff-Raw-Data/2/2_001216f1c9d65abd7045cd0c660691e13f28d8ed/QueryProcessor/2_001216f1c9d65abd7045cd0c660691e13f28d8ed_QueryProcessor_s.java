 package com.develogical;
 
 import java.util.*;
 
 public class QueryProcessor {
 
     public String process(String query) {
         if (query.contains("programming")) {
             return "Computer programming is the comprehensive process that leads from an original " 
                    + "formulation of a computing problem to executable programs.";
         }
 
         if (query.contains("name")){
             return "Damp Peak";
         }
 
         if( query.contains("1 plus 1"))
             return "2";
 
        if (query.contains("largestNumber")) {
             return processLargestNumberRequest(query);
         }
 
         if (query.contains("github"))
             return "GitHub is the best place to share code with friends, co-workers, classmates, " +
                     "and complete strangers. " +
                     "Over three million people use GitHub to build amazing things together.";
 
         return "";
     }
 
     private String processLargestNumberRequest(String query) {
         Scanner s = new Scanner(query);
 
         ArrayList<Integer> numbersList = new ArrayList<Integer>();
 
         while (s.hasNextInt()) {
             numbersList.add(s.nextInt());
         }
 
         int largest = largestNumber(numbersList);
         return String.valueOf(largest);
     }
 
     public int largestNumber(ArrayList<Integer> numbers) {
         Collections.sort(numbers);
         return numbers.get(numbers.size() - 1);
     }
 }
