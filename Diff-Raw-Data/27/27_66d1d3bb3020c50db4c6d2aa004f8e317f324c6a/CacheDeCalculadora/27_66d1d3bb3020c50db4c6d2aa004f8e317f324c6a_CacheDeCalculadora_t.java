 package com.makingdevs;
 
 public interface CacheDeCalculadora {
  void persistirSuma(Integer a, Integer b, Integer r);
   boolean existeElResultadoDeSumar(Integer a, Integer b);
   Integer obtenerElResultadoDeSumar(Integer a, Integer b);
 }
