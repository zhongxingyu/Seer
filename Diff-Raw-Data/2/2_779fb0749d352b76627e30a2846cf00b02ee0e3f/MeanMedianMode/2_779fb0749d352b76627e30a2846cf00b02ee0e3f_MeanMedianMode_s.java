 package com.aestrea.bootcamp;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 public class MeanMedianMode {
 
     public void findMeanMedianMode(String[] args) {
         if (args.length > 0) {
             ArrayList<Integer> userInput = new ArrayList<Integer>();
             userInput = convertStringArgsToInteger(args);
             Collections.sort(userInput);
             System.out.println("Mean: " + findMean(userInput));
             System.out.println("Median: " + findMedian(userInput));
             System.out.println("Mode: " + findMode(userInput));
         } else System.out.println("No Arguments Found!");
     }
 
     private static ArrayList findMode(ArrayList<Integer> userInput) {
         HashMap<Integer, Integer> seen = new HashMap();
         ArrayList<Integer> modeElements = new ArrayList<Integer>();
         int max = 0;
 
         for (int i = 0; i < userInput.size(); i++) {
             if (seen.containsKey(userInput.get(i))) {
                 seen.put(userInput.get(i), seen.get(userInput.get(i)) + 1);
             } else {
                 seen.put(userInput.get(i), 1);
             }
             if (seen.get(userInput.get(i)) > max) {
                 max = seen.get(userInput.get(i));
                 modeElements.clear();
                 modeElements.add(userInput.get(i));
             } else if (seen.get(userInput.get(i)) == max) {
                 modeElements.add(userInput.get(i));
             }
         }
         return modeElements;
     }
 
    private int findMedian(ArrayList<Integer> userInput) {
         boolean isEven = (userInput.size() % 2) == 0;
 
         if (isEven) {
             int upper = userInput.get(userInput.size() / 2 - 1);
             int lower = userInput.get(userInput.size() / 2);
             return (upper + lower) / 2;
         } else {
             return userInput.get((userInput.size() + 1) / 2 - 1);
         }
     }
 
     private int findMean(ArrayList<Integer> userInput) {
         int sum = 0;
 
         for (Integer i : userInput) {
             sum += i;
         }
         return sum / userInput.size();
     }
 
     private ArrayList<Integer> convertStringArgsToInteger(String[] args) {
         ArrayList<Integer> temp = new ArrayList<Integer>();
         if (args.length > 0) {
             for (String s : args) {
                 temp.add(Integer.parseInt(s));
             }
         }
         return temp;
     }
 }
