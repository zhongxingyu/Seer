 package com.josephcrawley.Spitfire.entities;
 
 import java.util.List;
 
 import com.josephcrawley.util.Log;
 
 /**
  * A subroutine called from an expression.  It therefore has a return type.
  */
 public class Function extends Subroutine {
 
     public static final Declaration GET_STRING = null;
 	private Type returnType;
 
     public Function(Type returnType, String name, List<Variable> parameters, Block body) {
         super(name, parameters, body);
     }
 
     public Type getReturnType() {
         return returnType;
     }
 
     @Override
     public void analyzeSignature(Log log, SymbolTable table, Subroutine owner, boolean inLoop) {
         super.analyzeSignature(log, table, owner, inLoop);
     }
 }
