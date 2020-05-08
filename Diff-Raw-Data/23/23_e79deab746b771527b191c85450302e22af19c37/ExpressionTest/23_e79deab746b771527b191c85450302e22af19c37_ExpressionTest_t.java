 package br.com.igorribeirolima.fx.expression;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import br.com.igorribeirolima.fx.api.Fx;
 
 public class ExpressionTest {
   
   @Test
   public void testeExpressaoSimples() {
     Fx fx = new SimpleExpression();
     Assert.assertEquals( -2.0, fx.calc("-1-1"), 0.0001 );
     Assert.assertEquals( 2.0,  fx.calc("+1+1"), 0.0001 );
     Assert.assertEquals( 0.0,  fx.calc("-1+1"), 0.0001 );
     Assert.assertEquals( 0.0,  fx.calc("+1-1"), 0.0001 );
     Assert.assertEquals( -1.0, fx.calc("-1*1"), 0.0001 );
     Assert.assertEquals( -1.0, fx.calc("-1/1"), 0.0001 );
     Assert.assertEquals( 2.0,  fx.calc("1+1"), 0.0001 );
     Assert.assertEquals( 2.0,  fx.calc("4/2"), 0.0001 );
     Assert.assertEquals( 4.0,  fx.calc("2*2"), 0.0001 );
     Assert.assertEquals( 4.0,  fx.calc("2^2"), 0.0001 );
     Assert.assertEquals( 4.0,  fx.calc("6-2"), 0.0001 );
   }
   
   @Test
   public void testeExpressao() {
     Fx fx = new ManyExpression(); 
     Assert.assertEquals( 4.0,  fx.calc("1+1+1+1"), 0.0001 );
     Assert.assertEquals( 0.0,  fx.calc("1+1-1-1"), 0.0001 );
     Assert.assertEquals( 4.0,  fx.calc("2*2*2/2"), 0.0001 );
     Assert.assertEquals( 16.0, fx.calc("2^2^2"), 0.0001 );
   }
   
   @Test
   public void testeParaVerificarALimpezaDeEspacosEmBranco() {
 	String strCleaning = UtilExpression.clearBlanckSpace("\"Rio Grande\"  ==  \"Jucuruçu\",    \"Rio Grande\"==\"Rio Alcobaça ou Itanhém\"");
 	Assert.assertEquals("\"Rio Grande\"==\"Jucuruçu\",\"Rio Grande\"==\"Rio Alcobaça ou Itanhém\"", strCleaning);
   }
 
   @Test
   public void testeExpressaoComParenteses() {
     Fx fx = new GenericExpression();
 	Assert.assertEquals( -6.0, fx.calc("((1+1)-4)*(9/3)"), 0.0001 );
 	Assert.assertEquals( 0.0,  fx.calc("(1+1)-(1+1)"), 0.0001 );
 	Assert.assertEquals( 3.0,  fx.calc("(2*2)-(2/2)"), 0.0001 );
 	Assert.assertEquals( 6.0,  fx.calc("(2^2)+2"), 0.0001 );
   }
 
   @Test
   public void testeParaVerificarOrdemDePrecedencia(){
     Fx fx = new GenericExpression();
 	Assert.assertEquals( 7.0,  fx.calc("2+2*2+1"), 0.0001 );
 	Assert.assertEquals( 8.0,  fx.calc("2+2*2+1*(2*1^3)"), 0.0001 );
 	Assert.assertEquals( 12.0, fx.calc("2+2^2+2*(2*1^3+1)"), 0.0001 );
 	Assert.assertEquals( 8.0,  fx.calc("2+2^2+2*(2*1^3>1)"), 0.0001 );
   }
 
   @Test
   public void testeParaVerificarCalculoDeFuncaoDentroDeFuncao() {
     Fx fx = new MathExpression();
 	Assert.assertEquals( 1.0, fx.calc("IF(AND(1,1,1,1),1,0)"), 0.0001 );
 	Assert.assertEquals( 1.0, fx.calc("IF(AND((1+1^2(1))<>0,1,1,1),1,0)"), 0.0001 );
 	Assert.assertEquals( 1.0, fx.calc("AND(\"GD2\"==\"GD2\")"), 0.0001 );
 	Assert.assertEquals( 1.0, fx.calc("IF( AND(\"GD2\"==\"GD2\"), 5 * 2 < 10.5, 5 * 2 < 5.5 )"), 0.0001 );
   }
 
   @Test
   public void testeParaVerificarUmaExpressaoDeRegraDeModoDeUso() {
     Fx fx = new MathExpression();
 	Double value = fx.calc("IF(OR(\"GD2\"==\"SF6\",\"GD2\"==\"SF7\",\"GD2\"==\"SF8\",\"GD2\"==\"SF9\",\"GD2\"==\"SF10\", \"GD2\"==\"JQ1\", \"GD2\"==\"JQ2\", \"GD2\"==\"JQ3\", \"GD2\"==\"PA1\", \"GD2\"==\"MU1\", \"Rio Grande\"==\"Jucuruçu\", \"Rio Grande\"==\"Rio Alcobaça ou Itanhém\"), 1 <= 0.5, 1 <= 1 )");
 	Assert.assertEquals(1.0, value, 0.0001);
   }
  
  @Test
  public void testeParaVerificarUmaExpressaoComHoraDeRegraDeModoDeUso() {
	  Fx fx = new MathExpression();
	  Assert.assertEquals(6.0, fx.calc("IF( HourToNumber(\"10:10\")<20, 2*3, 4*5 )"), 0.0001);
	  Assert.assertEquals(28.0, fx.calc("IF( HourToNumber(\"10:10\")>20, 2*3, 4*7 )"), 0.0001);
  }
  
 }
