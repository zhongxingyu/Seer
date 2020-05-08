 package com.technozor.ouertanitee;
 
 
 import java.util.concurrent.CompletableFuture;
 
 
 /**
  * @author slim ouertani
  */
 public interface FutureUtils {
 
     static<T> CompletableFuture<T> flatMap(CompletableFuture<CompletableFuture<T>> f){
         return f.thenApplyAsync(t -> t.join());
     }
 }
