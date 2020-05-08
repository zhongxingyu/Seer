 package test.java.com.mycompany.app;
 import main.java.com.mycompany.app.CalculadoraLib;
 import junit.framework.TestCase;
 
 
 public class CalculadoraTest extends TestCase {
 
 	private CalculadoraLib calculadora =  new CalculadoraLib();
 
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 	
 	public void testSoma() { 
		assertEquals(15, calculadora.soma(5, 10));
 	}
 	
 	public void testSubtracao() {
 		assertEquals(15, calculadora.subtracao(30, 15));
 	}
 	
 	public void testMultiplicacao() {
 		assertEquals(15, calculadora.multiplicacao(3, 5));
 	}
 	
 	public void testDiv() {
 		assertEquals(3, calculadora.divisao(15, 5));
 	}
 	
 }
