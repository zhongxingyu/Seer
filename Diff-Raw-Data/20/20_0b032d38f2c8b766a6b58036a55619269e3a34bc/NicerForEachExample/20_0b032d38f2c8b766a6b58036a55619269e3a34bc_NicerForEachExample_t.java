 package com.nearinfinity.java8.samples.iteration;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class NicerForEachExample {
     public static void main(String[] args) {
         List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        //noinspection Convert2MethodRef
         numbers.forEach((final Integer value) -> System.out.println(value));
     }
 }
