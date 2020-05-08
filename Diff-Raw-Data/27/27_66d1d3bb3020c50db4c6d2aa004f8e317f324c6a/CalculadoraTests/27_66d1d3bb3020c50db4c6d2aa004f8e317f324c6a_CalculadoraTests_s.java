 package com.makingdevs;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.runners.MockitoJUnit44Runner;
 import org.mockito.Mock;
 import org.mockito.InjectMocks;
 
 @RunWith(MockitoJUnit44Runner.class) // Nos ayudamos de este runner en JUnit para usar las anotaciones de Mockito
 public class CalculadoraTests {
 
   @Mock CacheDeCalculadora cacheDeCalculadora; // Este es mi colaborador
 
   @InjectMocks Calculadora calculadora = new Calculadora(); // Clase principal a probar
 
   @Test
   public void pruebaSumaDeDosNumeros(){
     Integer resultado = calculadora.suma(4,5);
     assertTrue(9 == resultado);
   }
   
   @Test
   public void pruebaSumaDeDosNumerosGuardandoEnCache(){
     Integer resultado = calculadora.suma(6,2);
     assertTrue(8 == resultado);
    verify(cacheDeCalculadora).persistirSuma(6,2); // ¿En verdad se llamo a este método? 
   }
   
   @Test
   public void pruebaSumaDeDosNumerosObteniendoResultadoDeCache(){
     when(cacheDeCalculadora.existeElResultadoDeSumar(10,4)).thenReturn(true); // Simulamos el comportamiento de un método
     when(cacheDeCalculadora.obtenerElResultadoDeSumar(10,4)).thenReturn(14); // Aislamos el componente 
     Integer resultadoEnSegundaEjecucion = calculadora.suma(10,4);
     assertTrue(14 == resultadoEnSegundaEjecucion);
    verify(cacheDeCalculadora,never()).persistirSuma(6,2); // Este método no debe ejecutarse
     verify(cacheDeCalculadora).existeElResultadoDeSumar(10,4);
     verify(cacheDeCalculadora).obtenerElResultadoDeSumar(10,4);
   }
 }
