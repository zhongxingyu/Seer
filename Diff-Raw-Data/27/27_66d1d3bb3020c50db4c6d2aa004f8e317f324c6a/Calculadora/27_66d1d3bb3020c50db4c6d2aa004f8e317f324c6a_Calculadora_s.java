 package com.makingdevs;
 
 public class Calculadora {
 
   CacheDeCalculadora cacheDeCalculadora;
 
   public Integer suma(Integer a, Integer b){
    cacheDeCalculadora.persistirSuma(a,b);
    return a+b;
   }
 }
