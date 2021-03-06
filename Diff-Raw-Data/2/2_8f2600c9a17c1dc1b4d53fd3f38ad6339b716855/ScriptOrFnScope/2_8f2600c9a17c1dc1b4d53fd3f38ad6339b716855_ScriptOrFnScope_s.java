 /*
  * YUI Compressor
  * Author: Julien Lecomte <jlecomte@yahoo-inc.com>
  * Copyright (c) 2007, Yahoo! Inc. All rights reserved.
  * Code licensed under the BSD License:
  *     http://developer.yahoo.net/yui/license.txt
  */
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 class ScriptOrFnScope {
 
     private int braceNesting;
     private ScriptOrFnScope parentScope;
     private ArrayList subScopes;
     private Hashtable identifiers = new Hashtable();
     private boolean markedForMunging = true;
 
     ScriptOrFnScope(int braceNesting, ScriptOrFnScope parentScope) {
         this.braceNesting = braceNesting;
         this.parentScope = parentScope;
         this.subScopes = new ArrayList();
         if (parentScope != null) {
             parentScope.subScopes.add(this);
         }
     }
 
     int getBraceNesting() {
         return braceNesting;
     }
 
     ScriptOrFnScope getParentScope() {
         return parentScope;
     }
 
     Identifier declareIdentifier(String symbol) {
         Identifier identifier = (Identifier) identifiers.get(symbol);
         if (identifier == null) {
             identifier = new Identifier(symbol, this);
             identifiers.put(symbol, identifier);
         }
         return identifier;
     }
 
     Identifier getIdentifier(String symbol) {
         return (Identifier) identifiers.get(symbol);
     }
 
     void markForMunging(boolean value) {
         markedForMunging = value;
     }
 
     private ArrayList getUsedSymbols() {
         ArrayList result = new ArrayList();
         Enumeration elements = identifiers.elements();
         while (elements.hasMoreElements()) {
             Identifier identifier = (Identifier) elements.nextElement();
             String mungedValue = identifier.getMungedValue();
             if (mungedValue == null) {
                 mungedValue = identifier.getValue();
             }
             result.add(mungedValue);
         }
         return result;
     }
 
     private ArrayList getAllUsedSymbols() {
         ArrayList result = new ArrayList();
         ScriptOrFnScope scope = this;
         while (scope != null) {
             result.addAll(scope.getUsedSymbols());
             scope = scope.parentScope;
         }
         return result;
     }
 
     void munge() {
 
        if (!markedForMunging) {
             return;
         }
 
         int pickFromSet = 1;
 
         // Do not munge symbols in the global scope!
         if (parentScope != null) {
 
             ArrayList freeSymbols = new ArrayList();
 
             freeSymbols.addAll(YUICompressor.ones);
             freeSymbols.removeAll(getAllUsedSymbols());
             if (freeSymbols.size() == 0) {
                 pickFromSet = 2;
                 freeSymbols.addAll(YUICompressor.twos);
                 freeSymbols.removeAll(getAllUsedSymbols());
             }
             if (freeSymbols.size() == 0) {
                 pickFromSet = 3;
                 freeSymbols.addAll(YUICompressor.threes);
                 freeSymbols.removeAll(getAllUsedSymbols());
             }
             if (freeSymbols.size() == 0) {
                 System.err.println("The YUI Compressor ran out of symbols. Aborting...");
                 System.exit(1);
             }
 
             Enumeration elements = identifiers.elements();
             while (elements.hasMoreElements()) {
                 if (freeSymbols.size() == 0) {
                     pickFromSet++;
                     if (pickFromSet == 2) {
                         freeSymbols.addAll(YUICompressor.twos);
                     } else if (pickFromSet == 3) {
                         freeSymbols.addAll(YUICompressor.threes);
                     } else {
                         System.err.println("The YUI Compressor ran out of symbols. Aborting...");
                         System.exit(1);
                     }
                     // It is essential to remove the symbols already used in
                     // the containing scopes, or some of the variables declared
                     // in the containing scopes will be redeclared, which can
                     // lead to errors.
                     freeSymbols.removeAll(getAllUsedSymbols());
                 }
                 Identifier identifier = (Identifier) elements.nextElement();
                 String mungedValue = (String) freeSymbols.remove(0);
                 identifier.setMungedValue(mungedValue);
             }
         }
 
         for (int i = 0; i < subScopes.size(); i++) {
             ScriptOrFnScope scope = (ScriptOrFnScope) subScopes.get(i);
             scope.munge();
         }
     }
 }
