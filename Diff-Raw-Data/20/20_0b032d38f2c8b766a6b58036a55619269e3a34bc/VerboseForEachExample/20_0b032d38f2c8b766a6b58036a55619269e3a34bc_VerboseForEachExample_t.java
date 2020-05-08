 package com.nearinfinity.java8.samples.iteration;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.function.Consumer;
 
 public class VerboseForEachExample {
     public static void main(String[] args) {
         List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        //noinspection Convert2Lambda,Anonymous2MethodRef
         numbers.forEach(new Consumer<Integer>() {
             @Override
             public void accept(Integer integer) {
                 System.out.println(integer);
             }
         });
     }
 }
