 /**
  * Copyright (C) 2013 Contributors.
  *
  * This file is part of FormulaJ.
  *
  * FormulaJ is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * FormulaJ is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see [http://www.gnu.org/licenses/].
  *
  * Contact: formulaj-user-list@googlegroups.com.
  */
 package formulaj.expression;
 
 import formulaj.MathExpression;
 import formulaj.expression.evaluator.impl.ImplictVariableExpressionEvaluator;
 
 public final class ExpressionBuilder
 {
     /**
      * Private constructor.
      */
     private ExpressionBuilder()
     {
         throw new UnsupportedOperationException();
     }
 
     /**
      * @param expression
      *            The expression to be evaluate and executed.
      * @param variableValues
      *            The values of the variables that are used in the expression. The variables are resolved according with their position and their
      *            name. If a variable is used more than one time in the expression, we don't need to repeat its value. For instance, consider the
      *            expression: <code><pre>{@code}ExpressionBuilder.evaluate("x + y * z ^ x", 1,2,3)</pre></code>. In this expression we have three
      *            variables (x, y, z) and the variable x appears two times in the expression. As we can see, the method was called with just three
      *            values (1,2,3). In this case the analyzer knows that the value of x is 1, y is 2 and finally the of z is 3.
      * @param <T>
      *            The type of the value returned by the expression.
      * @return The result after analyze and execute the given expression.
      * @throws EvaluationException
      *             If the given expression is invalid.
      */
     public static <T> T evaluate(String expression, Object... variableValues) throws EvaluationException
     {
         MathExpression<T> newMathExpression = ExpressionBuilder.<T> newMathExpression(expression);
         Value<T> value = newMathExpression.evaluate(new ImplictVariableExpressionEvaluator<T>(variableValues));
 
         return value.getValue();
     }
 
     /**
      * Returns an instance of one {@link MathExpression}.
      * 
      * @param <T>
      *            The type of the value returned by the expression.
      * @return An instance of a {@link MathExpression}.
      */
     public static <T> MathExpression<T> newMathExpression()
     {
         return new MathExpressionImpl<T>();
     }
 
     /**
      * Returns a {@link MathExpression} of a given expression.
      * 
      * @param expression
      *            The expression that represents a math expression.
      * @param <T>
      *            The type of the value returned by the expression.
      * @return A {@link MathExpression} instance of the given expression.
      */
     public static <T> MathExpression<T> newMathExpression(String expression)
     {
         return new MathExpressionImpl<T>(expression);
     }
 }
