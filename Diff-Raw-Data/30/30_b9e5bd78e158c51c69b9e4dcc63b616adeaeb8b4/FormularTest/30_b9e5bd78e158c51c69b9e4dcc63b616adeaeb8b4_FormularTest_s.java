 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.nitrox.formular;
 
 import com.google.common.base.Splitter;
 import java.math.BigDecimal;
 import java.util.HashSet;
 import static org.junit.Assert.*;
 import org.junit.Test;
 import org.nitrox.formular.exception.NotValidExpressionException;
 
 /**
  *
  * @author Alessandro Lima (alessandrolima@gmail.com)
  */
 public class FormularTest {
 
     @Test
     public void shouldAddVariablesAndEvalAreaFormula() {
         VariableBuild varBuild = new VariableBuild();
         
         Variable height = varBuild.withNameAndWithValue("heigth", new BigDecimal("9.04")).build();
         Variable width = varBuild.withNameAndWithValue("width", new BigDecimal("7.07")).build();
         
         Formula formula = new Formula();
         formula.putVar(height, width);        
         formula.setExpression("heigth * width");
         
         BigDecimal result = formula.eval();
         
         assertEquals(new BigDecimal("63.9128"), result);        
     }
     
     @Test
     public void shouldEvalAreaFormulaWithIntegerNumbers() {
         VariableBuild varBuild = new VariableBuild();
         
         Variable height = varBuild.withNameAndWithValue("heigth", new BigDecimal("9.04")).build();
         Variable width = varBuild.withNameAndWithValue("width", new BigDecimal("7.07")).build();
         
         Formula formula = new Formula();
         formula.putVar(height, width);        
         formula.setExpression("heigth * width + 2");
         
         BigDecimal result = formula.eval();
         
         assertEquals(new BigDecimal("65.9128"), result);        
     }
     
     @Test
     public void shouldEvalAreaFormulaWithFloatNumbers() {
         VariableBuild varBuild = new VariableBuild();
         
         Variable height = varBuild.withNameAndWithValue("heigth", new BigDecimal("9.04")).build();
         Variable width = varBuild.withNameAndWithValue("width", new BigDecimal("7.07")).build();
         
         Formula formula = new Formula();
         formula.putVar(height, width);        
         formula.setExpression("heigth * width + 2.01");
         
         BigDecimal result = formula.eval();
         
         assertEquals(new BigDecimal("65.9228"), result);        
     }
     
     @Test
     public void shouldEvalDivision() {
         VariableBuild varBuild = new VariableBuild();
         
         Variable height = varBuild.withNameAndWithValue("heigth", new BigDecimal("9.04")).build();
         Variable width = varBuild.withNameAndWithValue("width", new BigDecimal("7.07")).build();
         
         Formula formula = new Formula();
         formula.putVar(height, width);        
         formula.setExpression("heigth / width");
         
         BigDecimal result = formula.eval();
         
         assertEquals(new BigDecimal("1.28"), result);        
     }
     
     @Test
     public void shouldEvalParantesis() {
         VariableBuild varBuild = new VariableBuild();
         
         Variable height = varBuild.withNameAndWithValue("heigth", new BigDecimal("8.0")).build();
         Variable width = varBuild.withNameAndWithValue("width", new BigDecimal("5.0")).build();
         
         Formula formula = new Formula();
         formula.putVar(height, width);        
         formula.setExpression("((heigth + 2) / width)*3");
         
         BigDecimal result = formula.eval();
         
         assertEquals(new BigDecimal("6.0"), result);        
     }
     
     @Test
     public void shouldCheckIfIsValidExpression() {
        
        Formula formula = new Formula(); 
        assertTrue(formula.isValid("variable2 + variable3"));
        assertTrue(formula.isValid("variable2 + variable3 - variable4 * variable5 / variable6"));
        assertTrue(formula.isValid("variable"));
        
        System.out.println("EXPRESSAO : variable2 + variable3 +");
        assertFalse(formula.isValid("variable2 + variable3 + "));
        
        System.out.println("EXPRESSAO : variable2 variable3 +");
        assertFalse(formula.isValid("variable2 variable3 + "));
        
        System.out.println("EXPRESSAO :variable2 @ variable3");
        assertFalse(formula.isValid("variable2 @ variable3"));
         
  
     }
     
     @Test
     public void shouldCheckIfIsValidExpressionForPattern() {
        
        Formula formula = new Formula(); 
        String pattern = "\\^|log";
        //assertTrue(formula.isValidForPattern("variable2 + variable3 - variable4 ^ variable5 log variable6", pattern));
        
        System.out.println("EXPRESSAO : variable2 + variable3 +");
        assertFalse(formula.isValidForPattern("variable2 + variable3 + ", pattern));
        
        System.out.println("EXPRESSAO : variable2 + variable3 +");
        assertFalse(formula.isValidForPattern("variable2 + variable3 log variable4 ln variable6 ", pattern));
         
     }
     
     @Test
     public void shouldReturnTheVariablesOfFormulaExpression() {
        VariableBuild varBuild = new VariableBuild();
        Variable var1 =  varBuild.withNameAndWithValue("var1", BigDecimal.ZERO).build();
        Variable var2 =  varBuild.withNameAndWithValue("var2", BigDecimal.ZERO).build();
        Variable var3 =  varBuild.withNameAndWithValue("var3", BigDecimal.ZERO).build();
        Variable var4 =  varBuild.withNameAndWithValue("var4", BigDecimal.ZERO).build();
        Variable var5 =  varBuild.withNameAndWithValue("var5", BigDecimal.ZERO).build();
        
        HashSet<Variable> variables = new HashSet<Variable>();
        variables.add(var1);
        variables.add(var2);
        variables.add(var3);
        variables.add(var4);
        variables.add(var5);
        
        String expression = "var1 + var2 - var3 * var4 / var5";
        
        Formula formula = new Formula(); 
        HashSet<Variable> result = formula.getExpressionVariables(expression);
        
        assertEquals(variables.size(), result.size());
        
         for (Variable variable : result) {
             assertTrue(variables.contains(variable));
         }        
     }
     
     @Test (expected = NotValidExpressionException.class)
     public void shouldThowExceptionWhenTryToGetVariablesOfFormulaExpression() {
             
        String expression = "var1 + var2 var3 * var4 / var5";
        
        Formula formula = new Formula(); 
        HashSet<Variable> result = formula.getExpressionVariables(expression);
               
     }
     
 }
