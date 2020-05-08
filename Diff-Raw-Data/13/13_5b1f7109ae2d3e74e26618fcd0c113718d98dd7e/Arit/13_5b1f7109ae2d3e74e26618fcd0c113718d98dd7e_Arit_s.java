 /**********************************************************************/
 /* Copyright 2013 KRV                                                 */
 /*                                                                    */
 /* Licensed under the Apache License, Version 2.0 (the "License");    */
 /* you may not use this file except in compliance with the License.   */
 /* You may obtain a copy of the License at                            */
 /*                                                                    */
 /*  http://www.apache.org/licenses/LICENSE-2.0                        */
 /*                                                                    */
 /* Unless required by applicable law or agreed to in writing,         */
 /* software distributed under the License is distributed on an        */
 /* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,       */
 /* either express or implied.                                         */
 /* See the License for the specific language governing permissions    */
 /* and limitations under the License.                                 */
 /**********************************************************************/
 package robot;
 
 // Libraries
 import stackable.*;
 import exception.*;
 
 /**
  * <b>Assembly functions - class Arit</b><br>
  * Provides the funcions for arithmetic
  * operations (sum, subtraction, multiplication,
  * division and modulus) in the data inside the 
  * stack of the virtual machine.
  * 
  * @author Renato Cordeiro Ferreira
  * @see Ctrl
  */
 final public class Arit
 {
     // No instances of this class allowed
     private Arit() {} 
     
     /** 
      * Interface for arithmetic operations,
      * Dummy interface with the aim of being a 
      * model for all the arithmetic operations.
      */
     private interface Operation 
     {
         double op(double a, double b);
     }
     
     /**
      * General-model funcion calculate. <br>
      * Takes out the two arguments in the top 
      * of the main stack and pushes the answer
      * of the operation made over them.
      * 
      * @param rvm Virtual Machine
      * @param op  Anonymous inner class of type operation
      */
     private static final void calculate(RVM rvm, Operation op) 
         throws WrongTypeException,
                StackUnderflowException
     {
         Stackable arg1 = rvm.DATA.pop();
         Stackable arg2 = rvm.DATA.pop();
         
         if(arg1 instanceof Num && arg2 instanceof Num)
         {
             Num a = (Num) arg1, b = (Num) arg2;
             double ans = op.op(b.getNumber(), a.getNumber());
             rvm.DATA.push(new Num(ans));
         }
         else if(arg1 instanceof Coordinate && arg2 instanceof Coordinate)
         {
             Coordinate a = (Coordinate) arg1, b = (Coordinate) arg2;
             
            double a1 = a.getCoordinate()[0];
            double a2 = a.getCoordinate()[1];
            double b1 = b.getCoordinate()[0];
            double b2 = b.getCoordinate()[1];
             
            double ans1 = op.op(b1, a1);
            double ans2 = op.op(b2, a2);
             rvm.DATA.push(new Coordinate(ans1, ans2));
         }
         else { throw new WrongTypeException("Num or Coordinate"); }
     }
 
     /**
      * Assembly function ADD.
      * @param rvm Virtual Machine
      */
     static void ADD(RVM rvm) 
         throws WrongTypeException, StackUnderflowException
     {
         calculate(rvm, new Operation() { 
             public double op(double a, double b) { return a+b; } }
         );
     }
     
     /**
      * Assembly function SUB.
      * @param rvm Virtual Machine
      */
     static void SUB(RVM rvm) 
         throws WrongTypeException, StackUnderflowException
     {
         calculate(rvm, new Operation() { 
             public double op(double a, double b) { return a-b; } }
         );
     }
     
     /**
      * Assembly function MUL.
      * @param rvm Virtual Machine
      */
     static void MUL(RVM rvm) 
         throws WrongTypeException, StackUnderflowException
     {
         calculate(rvm, new Operation() { 
             public double op(double a, double b) { return a*b; } }
         );
     }
     
     /**
      * Assembly function DIV.
      * @param rvm Virtual Machine
      */
     static void DIV(RVM rvm) 
         throws WrongTypeException, StackUnderflowException
     {
         calculate(rvm, new Operation() { 
             public double op(double a, double b) { return a/b; } }
         );
     }
     
     /**
      * Assembly function MOD.
      * @param rvm Virtual Machine
      */
     static void MOD(RVM rvm) 
         throws WrongTypeException, StackUnderflowException
     {
         calculate(rvm, new Operation() { 
             public double op(double a, double b) { return a%b; } }
         );
     }
 }
