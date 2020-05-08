 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.nitrox.formular;
 
 import com.google.common.base.Splitter;
 import java.io.NotActiveException;
 import java.math.BigDecimal;
 import java.util.HashSet;
 import org.nitrox.formular.exception.NotValidExpressionException;
 
 /**
  *
  * @author Alessandro Lima (alessandrolima@gmail.com)
  */
 public class Formula {
 
     private HashSet<Variable> formulaVariables = new HashSet<Variable>();
     private String expression;
     private ScriptRunner scriptRunner;
     private String defaultPatternOperators = "\\+|-|\\*|/";
 
     public Formula() {
         this.scriptRunner = new JRubyScriptRunnerManager();
     }
     
     /**
      * Put variables in a formula. 
      * This method is interesting when you know all 
      * variables, and they are few variables.
      * Ex: formula.putVar(area, height, width)
      * @param variables 
      */
     public void putVar(Variable... variables) {
         for (int i = 0; i < variables.length; i++) {
             formulaVariables.add(variables[i]);
         }
     }
     
     /**
      * Put variables in a formula. 
      * This method is interesting if you have many variables or when you have 
      * custom formula excutions, for example you have different formulas and 
      * variables on database, or web environment.
      * 
      * @param variables 
      */
     public void putVar(HashSet<Variable> variables) {
         this.formulaVariables = variables;
     }
 
     public String getExpression() {
         return expression;
     }
     
     public void setExpression(String expression) {
         this.expression = expression;
     }    
     
     /**
      * This method evaluate the formula expression and return your result
      * @return 
      */
     public BigDecimal eval() {        
         return scriptRunner.eval(this.getExpression(), this.formulaVariables);
     }
     
     /**
      * Checks if the expression is valid for evaluation
      * @param expression
      * @return 
      */
     public boolean isValid(String expression) {
         HashSet<Variable> variables = loadExpressionVariables(expression);
         return isScriptRunnable(expression, variables);        
     }
     
     
     private HashSet<Variable> loadExpressionVariables(String expression) {
         HashSet<Variable> variables = new HashSet<Variable>();
        String expressionWithNoParentesis = expression.replaceAll("[{|(|)|}]", "");
        
        for (String variablesName : Splitter.onPattern(defaultPatternOperators).omitEmptyStrings().split(expressionWithNoParentesis)) {
             Variable variable = new Variable();
             variable.setName(variablesName.trim());
             variable.setValue(new BigDecimal("1.00"));
             variables.add(variable);
         }
         return variables;
     }
     
     private boolean isScriptRunnable(String expression, HashSet<Variable> variables) {
        try {            
             scriptRunner.eval(expression, variables);
             return true;
        } catch (Exception e) {            
            e.printStackTrace();
             //
         }
         return false;
     }
     
     public boolean isValidForPattern(String expression, String pattern) {
         defaultPatternOperators = defaultPatternOperators + "|" + pattern;
         return this.isValid(expression);
     }
     
     /**
      * Get all variables of a expression as Variable objects.
      * This method is for help to get the variables when one expression is informed by one output.
      * All variables have as default the 1.00 value. To evaluate the formula with this 
      * variables you have to change the values of them and put in formula using putVar method.
      * @param expression
      * @return
      * @throws NotValidExpressionException if the expression is not valid, throws a NotValidExpression
      */
     public HashSet<Variable> getExpressionVariables(String expression) throws NotValidExpressionException {
         HashSet<Variable> variables = loadExpressionVariables(expression);
         if(!isScriptRunnable(expression, variables)) {
             throw new NotValidExpressionException("The expression: " + expression + " is not a valid expression");
         }
         return variables;
     }
     
 }
