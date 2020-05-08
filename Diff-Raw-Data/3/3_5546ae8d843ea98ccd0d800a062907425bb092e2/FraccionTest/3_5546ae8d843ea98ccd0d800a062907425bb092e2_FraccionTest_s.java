 package spai.egit2.test;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import spai.egit2.Fraccion;
 
 public class FraccionTest {
     private Fraccion fraccion;
 
     @Before
     public void initObjects() {
         this.fraccion = new Fraccion(2, 3);
     }
 
     @Test
     public void testcontructorIntInt() {
         assertEquals(2, this.fraccion.getNumerador());
         assertEquals(3, this.fraccion.getDenominador());
     }
 
     @Test
     public void testConstructorSinParametros() {
         Fraccion fraccion = new Fraccion();
         assertEquals(1, fraccion.getNumerador());
         assertEquals(1, fraccion.getDenominador());
     }
 
     @Test
     public void testSetNumerador() {
         this.fraccion.setNumerador(4);
         assertEquals(4, fraccion.getNumerador());
     }
 
     @Test
     public void testSetDenominador() {
         this.fraccion.setDenominador(4);
         assertEquals(4, fraccion.getDenominador());
     }
 
     @Test
     public void testDecimal() {
         assertEquals(2.0/3.0, fraccion.decimal(), 1e-10);
     }
     
     @Test
     public void testIsPropia() {
     	assertTrue(this.fraccion.isPropia(this.fraccion));
     }
     
     @Test
     public void testIsImpropia() {
    	assertTrue(this.fraccion.isImpropia(this.fraccion));
     }
     
     @Test
     public void testIsEquivalente() {
     	assertTrue(this.fraccion.isEquivalente(this.fraccion));
     }
     
     @Test
     public void testMenor() {
     	Fraccion smallerFraccion = new Fraccion(1, 3);
     	
     	Fraccion comparisonResult = this.fraccion.menor(smallerFraccion, this.fraccion);
     	assertEquals(smallerFraccion, comparisonResult);
     }
     
     @Test
     public void testMayor() {
     	Fraccion biggerFraccion = new Fraccion(5, 6);
     	
     	Fraccion comparisonResult = this.fraccion.mayor(biggerFraccion, this.fraccion);
     	assertEquals(biggerFraccion, comparisonResult);
     }
     
     @Test
     public void testMultiplicar() {
     	Fraccion multiplicationResult = this.fraccion.multiplicar(this.fraccion, this.fraccion);
     	Fraccion assumedResult = new Fraccion(4, 9);
     	assertTrue(assumedResult.isEquivalente(multiplicationResult));
     }
     
     
     
     
 
 }
