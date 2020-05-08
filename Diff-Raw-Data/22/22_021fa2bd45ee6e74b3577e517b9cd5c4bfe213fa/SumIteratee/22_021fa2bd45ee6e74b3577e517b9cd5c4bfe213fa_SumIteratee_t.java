 package com.technozor.ouertanitee.samples;
 
 import java.util.function.Function;
 import com.technozor.ouertanitee.Input;
 import com.technozor.ouertanitee.Iteratee;
 import java.util.concurrent.CompletableFuture;
 
 /**
  * @author slim ouertani
  */
 public class SumIteratee implements Iteratee<Integer, Integer> {
 
     private final Integer sum;
 
     private final Function<Input<Integer>, Iteratee<Integer, Integer>> handler;
 
     public SumIteratee(Integer sum, Function<Input<Integer>, Iteratee<Integer, Integer>> handler) {
         this.sum = sum;
         this.handler = handler;
     }
 
     public SumIteratee(Integer sum) {
         this(0, handler(sum));
     }
 
     public SumIteratee() {
         this(0);
     }
 
     public Integer getSum() {
         return sum;
     }
 
     @Override
     public <B> CompletableFuture<B> handle(Function<Iteratee<Integer, Integer>, CompletableFuture<B>> step) {
         return step.apply(Iteratee.Cont(handler));
     }
 
     @Override
     public Function<Input<Integer>, Iteratee<Integer, Integer>> handler() {
         return handler;
     }
 
     private static Function<Input<Integer>, Iteratee<Integer, Integer>> handler(int initValue) {
         return (Input<Integer> e) -> {
             switch (e.onState()) {
                 case EL:
                     Input.El<Integer> el = (Input.El) e;
                     Integer elem = el.getE();
                     return new SumIteratee(initValue + elem);
                 case EOF:
                     return Iteratee.Done(initValue);
                 case Empty:
                 default:
                     return new SumIteratee(initValue);
             }
         };
     }
 
     @Override
     public String toString() {
         return "SumIteratee{" + "sum=" + sum + '}';
     }
 
 }
