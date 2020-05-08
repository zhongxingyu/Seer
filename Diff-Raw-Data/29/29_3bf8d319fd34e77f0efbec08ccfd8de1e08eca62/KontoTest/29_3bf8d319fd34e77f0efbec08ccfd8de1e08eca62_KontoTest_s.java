 package com.example.domain;
 
 import org.junit.Test;
 
 import java.io.Serializable;
 
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertTrue;
 
 public class KontoTest {
 
     @Test
     public void kontoShouldBeSerializable() {
         assertTrue(Serializable.class.isAssignableFrom(Konto.class));
     }
 
     @Test
     public void kontoShouldReturnValue() {
     	assertEquals(100, new Konto(100,"test").getWert());
     }
     @Test(expected = IllegalStateException.class)
     public void kontoShouldNotAllowNegativeValue() {
     	new Konto(-100,"test");
     }
 
     @Test(expected = IllegalStateException.class)
     public void kontoShouldNotAllowValueToBecomeNegative() {
    	new Konto(1,"test").buche(-2);
     }
     
     @Test(expected = IllegalStateException.class)
     public void kontoShouldNotAllowNullName() {
     	new Konto(1,null);
     }
     
     @Test
     public void kontoShouldBeRenderedWithNameAndValue() {
     	assertEquals("test: 1 EUR",new Konto(1,"test").toString());
     }
 }
