 package com.makingdevs;
 
 public class Calculadora {
 
   CacheDeCalculadora cacheDeCalculadora;
 
   public Integer suma(Integer a, Integer b){
    Integer resultado = 0;
    if(cacheDeCalculadora.existeElResultadoDeSumar(a,b)){
      resultado = cacheDeCalculadora.obtenerElResultadoDeSumar(a,b);
    } else {
      resultado = a + b; 
      cacheDeCalculadora.persistirSuma(a,b,resultado);
    }
    return resultado;
   }
 }
