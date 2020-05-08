 package com.technozor.ouertanitee;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.CompletableFuture;
 import java.util.function.BiFunction;
 import java.util.function.Function;
 import java.util.stream.Stream;
 
 
 /**
  * @author slim ouertani
  */
 @FunctionalInterface
 public interface Enumerator<E> {
 
     <A> CompletableFuture<Iteratee<E, A>> apply(Iteratee<E, A> it);
 
     static <E> Enumerator<E> empty() {
         return new Enumerator<E>() {
             @Override
             public <A> CompletableFuture<Iteratee<E, A>> apply(Iteratee<E, A> it) {
                 return CompletableFuture.completedFuture(it);
             }
         };
     }
     
     default <B> Input<B> run(Iteratee<E, B> it) {
       
         switch (it.onState()) {
             case CONT:
                 return run(apply(it));
             case ERROR:
                 Iteratee.Error e = (Iteratee.Error) it;
                 throw new RuntimeException(e.getMsg());
             case Done:
                 Iteratee.Done<E, B> d = (Iteratee.Done) it;
                 return Input.el(d.getA());
             default:
                 throw new IllegalStateException();
         }
     }
 
     default <B> Input<B> run(CompletableFuture<Iteratee<E, B>> fit) {
         return run(fit.join());
     }
 
     static <B> Enumerator<B> enumInput(Input<B> input) {
         return new Enumerator<B>() {
 
             @Override
             public <A> CompletableFuture<Iteratee<B, A>> apply(Iteratee<B, A> it) {
                 Function<Iteratee<B, A>, CompletableFuture<Iteratee<B, A>>> step = (Iteratee<B, A> t) -> {
                     switch (t.onState()) {
                         case CONT:
                             
                             return CompletableFuture.supplyAsync(() -> {
                                 Iteratee.Cont<B, A> c = (Iteratee.Cont) t;
                                 return c.getK().apply(input);
                             });
                             
                         default:
                             return CompletableFuture.completedFuture(t);
                     }
                 };
                 return it.handle(step);
             }
         };
     }
 
     /**
      *
      * @param <B>
      * @param input
      * @return
      */
     static <B> Enumerator<B> enumInput(final Input<B>... input) {
 
         switch (input.length) {
             case 0:
                 return Enumerator.empty();
             case 1:
                 return new Enumerator<B>() {
                 @Override
                 public <A> CompletableFuture<Iteratee<B, A>> apply(Iteratee<B, A> i) {
 
                     return i.handle((Iteratee<B, A> t) -> {
                         switch (t.onState()) {
                             case CONT:
 
                                 return CompletableFuture.supplyAsync(() -> {
                                 Iteratee.Cont<B, A> c = (Iteratee.Cont) t;
                                 return c.getK().apply(input[0]);
                             });
                             default:
                                 return CompletableFuture.completedFuture(t);
                         }
                     });
                 }
             };
 
             default:
 
                 List<Input<B>> of = Arrays.asList(input);
                 return new Enumerator<B>() {
                     @Override
                     public <A> CompletableFuture<Iteratee<B, A>> apply(Iteratee<B, A> it) {
                         return enumSeq(of, it);
                     }
                 };
         }
 
     }
     
     static <B> Enumerator<B> enumInput(final B... input) {
         Input<B>[] toArray = (Input<B>[]) Stream.of(input).map(t -> Input.el(t)).toArray();
         
         return enumInput(Arrays.asList(toArray).add(Input.EOF));
     }
     /**
      * Need to add Eof at the end
      * @param <B>
      * @param input
      * @return 
      */
      static <B> Enumerator<B> enumInput(Stream<B> input) {
         Stream<Input<B>> map = input.map(t -> Input.el(t));
         Enumerator<B> empty = Enumerator.empty();
         throw new RuntimeException("Not implemented yet");
        
     }
      
       static <E, A> CompletableFuture<Iteratee<E, A>> enumStream(Stream<Input<E>>  l, Iteratee<E, A> i) {
           BiFunction<Iteratee<E, A>,Input<E>,  CompletableFuture<Iteratee<E, A>>> f = ( Iteratee<E, A> u,Input<E> t) -> {
               switch(u.onState()) {
                   case CONT :
                       return CompletableFuture.supplyAsync(() -> u.handler().apply(t));
                    default:
                     return CompletableFuture.completedFuture(u);   
               }
           };
           return CompletableFuture.supplyAsync(() -> CollectionUtils.leftFoldM(i,l, f));
       }
 
     static <E, A> CompletableFuture<Iteratee<E, A>> enumSeq(List<Input<E>> l, Iteratee<E, A> i) {
         BiFunction<Iteratee<E, A>,Input<E> , CompletableFuture<Iteratee<E, A>>> f = ( Iteratee<E, A> it,Input<E> a) -> {
          
             switch (it.onState()) {
                 case CONT:
                     return CompletableFuture.supplyAsync(() -> it.handler().apply(a));
 
                 default:
                     return CompletableFuture.completedFuture(it);
             }
         };
 
         return CompletableFuture.supplyAsync(() -> CollectionUtils.leftFoldM( i, l,f));
     }
 
 }
