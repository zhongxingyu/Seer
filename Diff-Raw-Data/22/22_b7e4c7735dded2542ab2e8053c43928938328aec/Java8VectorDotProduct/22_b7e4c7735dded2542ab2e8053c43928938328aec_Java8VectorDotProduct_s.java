 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.adoptajsr.parallel.java8;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.stream.DoubleStream;
 import java.util.stream.Stream;
 import static java.util.stream.StreamSupport.doubleStream;
 import java.util.stream.Streams;
 import org.adoptajsr.parallel.runners.VectorDotProduct;
 
 /**
  *
  * @author richard
  */
 public class Java8VectorDotProduct extends VectorDotProduct {
     
     private final boolean parallel;
     
     private List<Double> x;
     private List<Double> y;
     
     public Java8VectorDotProduct(boolean parallel) {
         this.parallel = parallel;
     }
 
     // Required to created a boxed list, sigh
     private List<Double> asList(double[] array) {
         List<Double> list = new ArrayList<>();
         for (double element : array) {
             list.add(element);
         }
         return list;
     }
 
     @Override
     public void prepare(double[] xArray, double[] yArray) {
         x = asList(xArray);
         y = asList(yArray);
     }
     
     private Stream<Double> stream(List<Double> list) {
         return parallel ? list.parallelStream() : list.stream();
     }
 
     @Override
     public double dotProduct() {
        return Streams.zip(stream(x), stream(y), (acc, value) -> acc * value)
                      .reduce(1.0, Double::sum);
     }
 
     @Override
     public String getName() {
         return "Java 8 " + (parallel ? "in parallel" : "sequentially");
     }
 
 }
