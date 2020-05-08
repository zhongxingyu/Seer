 package com.technozor.ouertanitee.samples;
 
 import java.util.function.Function;
 import com.technozor.ouertanitee.Input;
 import com.technozor.ouertanitee.Iteratee;
 import java.util.concurrent.CompletableFuture;
 
 /**
  * @author slim ouertani
  */
 public class MaxIteratee implements Iteratee<Integer, Integer> {
 
     private final Function<Input<Integer>, Iteratee<Integer, Integer>> handler;
     private final Integer max;
 
     public MaxIteratee(Integer max) {
         this.max = max;
         this.handler = handler(max);
     }
 
     public MaxIteratee() {
         this(Integer.MIN_VALUE);
     }
 
     @Override
     public <B> CompletableFuture<B> handle(Function<Iteratee<Integer, Integer>, CompletableFuture<B>> folder) {
         return folder.apply(Iteratee.Cont(handler));
     }
 
     private static Integer max(Integer a, Integer b) {
         return (a > b) ? a : b;
     }
 
 
     @Override
     public Function<Input<Integer>, Iteratee<Integer, Integer>> handler() {
         return handler;
     }
 
     private Function<Input<Integer>, Iteratee<Integer, Integer>> handler(int max) {
         return (Input<Integer> e) -> {
             switch (e.onState()) {
                 case EL:
                     Input.El<Integer> el = (Input.El) e;
                     Integer elem = el.getE();
                     return new MaxIteratee(max(elem, max));
                 case EOF:
                     return  Iteratee.Done(max);
                 case Empty:
                 default:
                     return new MaxIteratee(max);
 
             }
         };
     }
     
     
     @Override
     public String toString() {
         return "MaxIteratee{" + "max=" + max + '}';
     }
 }
