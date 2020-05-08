 package org.adoptajsr.parallel.runners;
 
 import java.io.IOException;
 import static java.time.Clock.system;
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 import org.adoptajsr.parallel.java8.Java8VectorDotProduct;
 import org.adoptajsr.parallel.manual.ImperativeVectorDotProduct;
 
 public abstract class VectorDotProduct {
     
     private static final int SIZE = 10_000_000;
 
     public static void main( String[] args ) {
         double[] x = randomArray(), y = randomArray();
         Arrays.asList(
                 new Java8VectorDotProduct(false),
//                new Java8VectorDotProduct(true),
                 new ImperativeVectorDotProduct()
                 ).forEach(impl -> {
 
             impl.prepare(x, y);
             impl.dotProduct();
             System.out.println(impl.getName() + "are you ready?");
//            awaitInput();
            List<Double> results = new ArrayList<>();
             for (int j = 0; j < 10; j++) {
                 long time = System.nanoTime();
                 for (int i = 0; i < 10; i++) {
                    results.add(impl.dotProduct());
                 }
                 System.out.println(System.nanoTime() - time);
             }
            System.out.println(results);
         });
     }
     
     private static double[] randomArray() {
         Random random = new Random();
         double[] array = new double[SIZE];
         for (int i = 0; i < SIZE; i++) {
             array[i] = random.nextDouble();
         }
         return array;
     }
 
     private static void awaitInput() {
         try {
             System.in.read();
         } catch (IOException iOException) {}
     }
     
     public abstract void prepare(double[] x, double[] y);
 
     public abstract double dotProduct();
 
     public abstract String getName();
 
 }
