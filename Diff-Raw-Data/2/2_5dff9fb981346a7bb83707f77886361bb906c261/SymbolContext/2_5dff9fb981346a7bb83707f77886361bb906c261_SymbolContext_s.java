 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.kkirch.symbols;
 
 import com.kkirch.ir.Id;
 import com.kkirch.lexer.Token;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author kkirch
  */
 public class SymbolContext {
 
     private Map<Token, Id> symbolTable = new HashMap<Token, Id>();
     protected SymbolContext parentContext;
 
     public SymbolContext(SymbolContext parentContext) {
         this.parentContext = parentContext;
     }
 
     public void put(Token t, Id i) {
         symbolTable.put(t, i);
     }
 
     public Id get(Token t) {
         Id foundId = null;
         SymbolContext currentContext = this;
         while (currentContext != null) {
            foundId = symbolTable.get(t);
             if (foundId == null) {
                 currentContext = currentContext.parentContext;
             } else {
                 break;
             }
         }
         return foundId;
     }
 }
