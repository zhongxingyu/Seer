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
 package formulaj.expression.function.math;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Objects;
 
 import formulaj.Expression;
 import formulaj.expression.Computable;
 import formulaj.expression.Decimal;
 import formulaj.expression.EvaluationException;
 import formulaj.expression.Value;
 import formulaj.expression.evaluator.Evaluator;
 import formulaj.expression.evaluator.impl.Evaluators;
 import formulaj.expression.function.Function;
 
 
 public abstract class FunctionSupport<E> implements Function<Value<Decimal>>
 {
     /**
      * The number of arguments required by the function.
      */
     private final int numberOfArguments;
 
     /**
      * The name of this function.
      */
     private final String name;
 
     /**
      * Creates an instance of {@link FunctionSupport} with the given number of arguments to be checked before call this function.
      * 
      * @param numberOfArgs
      *            The number of arguments that is required to evaluate this function.
      */
     public FunctionSupport(int numberOfArgs)
     {
         this.numberOfArguments = numberOfArgs;
         this.name = this.getClass().getSimpleName().toLowerCase();
     }
 
     /**
      * Creates an instance of {@link FunctionSupport} with the given number of arguments to be checked before call this function.
      * 
      * @param numberOfArgs
      *            The number of arguments that is required to evaluate this function.
      * @param functionName
      *            The function's name. Might not be <code>null</code>.
      */
     public FunctionSupport(int numberOfArgs, String functionName)
     {
         this.numberOfArguments = numberOfArgs;
         this.name = functionName;
     }
 
     @Override
     public String name()
     {
         return name;
     }
 
     @Override
     public <T> Value<Decimal> evaluate(Expression<T> expression) throws EvaluationException
     {
         return this.evaluate(expression, Evaluators.<Expression<T>, Value<Decimal>> get(expression.getClass()));
     }
 
     @Override
     public <T> Value<Decimal> evaluate(Expression<T> expression, Evaluator<Expression<T>, Value<Decimal>> evaluator) throws EvaluationException
     {
         return Objects.requireNonNull(evaluator).eval(Objects.requireNonNull(expression));
     }
 
     @Override
     public <R, T extends Computable<R>> Value<Decimal> evaluate(T[] arguments)
     {
         return this.evaluate(Arrays.asList(arguments));
     }
 
     @Override
     public <R, T extends Computable<R>> Value<Decimal> evaluate(List<T> arguments)
     {
         checkArguments(arguments);
         Decimal result = this.eval(transform(arguments));
         return new Value<Decimal>(result);
     }
 
     /**
      * This method throws the exception {@link IllegalArgumentException} with the instructions to call this {@link Function} if the number of
      * function's arguments is incorrect.
      * 
      * @param args
      *            {@link List} with the arguments passed to the function.
      * @param <R>
      *            The {@link Computable} value type.
      * @param <T>
      *            A {@link Computable} argument.
      * @throws IllegalArgumentException
      *             Exception with the instruction to call this {@link Function}.
      */
     protected <R, T extends Computable<R>> void checkArguments(List<T> args)
     {
         StringBuilder message = new StringBuilder();
 
         if (args == null)
         {
             message.append(String.format("The function %s requires %s argument%s. But there wasn't any argument.", this.name(),
                     this.numberOfArguments, this.numberOfArguments > 1 ? "s" : ""));
 
             throw new IllegalArgumentException(message.toString());
         }
         else if (args.size() != this.numberOfArguments)
         {
             message.append(String.format("The function %s requires %s argument%s.", this.name(), this.numberOfArguments,
                     this.numberOfArguments > 1 ? "s" : ""));
 
             throw new IllegalArgumentException(message.toString());
         }
     }
 
     /**
     * Transforms a {@link List} of T in an array of E. The default implementation assumes that E is a {@link Decimal}.
      * 
      * @param arguments
     *            The {@link List} of computable arguments that must be translated to an array of {@link Decimal}.
      * @param <R>
      *            The {@link Computable} value type.
      * @param <T>
      *            A {@link Computable} argument.
     * @return An array with the arguments transformed in {@link Decimal}.
      */
     @SuppressWarnings("unchecked")
     protected <R, T extends Computable<R>> E [] transform(List<T> arguments)
     {
         List<R> args = new ArrayList<>();
         
         for (Object computable : arguments)
         {
            // Oh my Gosh, what is this? ;)
            args.add((R) Decimal.from(((Computable<R>) computable).getValue().toString()));
         }
 
         return (E[]) args.toArray(new Decimal[args.size()]);
     }
 
     /**
      * Executes the function.
      * 
      * @param args
      *            The arguments of the function. It's never <code>null</code> and the its length is always equals to {@link #numberOfArguments}.
      * @return The value after evaluation of this function.
      */
     protected abstract Decimal eval(E [] args);
 
 }
