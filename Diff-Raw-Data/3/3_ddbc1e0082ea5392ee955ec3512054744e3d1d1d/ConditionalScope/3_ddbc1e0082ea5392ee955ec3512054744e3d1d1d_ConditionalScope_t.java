 /*
  * Copyright 2012 Robert Stoll <rstoll@tutteli.ch>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 package ch.tutteli.tsphp.typechecker.scopes;
 
 import ch.tutteli.tsphp.common.IScope;
 import ch.tutteli.tsphp.common.ISymbol;
 import ch.tutteli.tsphp.common.ITSPHPAst;
 import ch.tutteli.tsphp.typechecker.error.ErrorReporterRegistry;
 
 /**
  *
  * @author Robert Stoll <rstoll@tutteli.ch>
  */
 public class ConditionalScope extends AScope implements IConditionalScope
 {
 
     public ConditionalScope(IScope enclosingScope) {
         super("cScope", enclosingScope);
     }
 
     @Override
     public void define(ISymbol symbol) {
         enclosingScope.define(symbol);
         symbol.setDefinitionScope(this);
     }
 
     @Override
     public boolean doubleDefinitionCheck(ISymbol symbol) {
         IScope scope = getEnclosingNonConditionalScope(symbol);
        if(scope instanceof INamespaceScope){
            scope = scope.getEnclosingScope();
        }
         return ScopeHelperRegistry.get().doubleDefinitionCheck(scope.getSymbols(), symbol, new IAlreadyDefinedMethodCaller()
         {
             @Override
             public void callAccordingAlreadyDefinedMethod(ISymbol firstDefinition, ISymbol symbolToCheck) {
                 ErrorReporterRegistry.get().definedInOuterScope(firstDefinition, symbolToCheck);
             }
         });
     }
 
     @Override
     public ISymbol resolve(ITSPHPAst ast) {
         return enclosingScope.resolve(ast);
     }
 
     private IScope getEnclosingNonConditionalScope(ISymbol symbol) {
         IScope scope = symbol.getDefinitionAst().getScope();
         while (scope != null && scope instanceof IConditionalScope) {
             scope = scope.getEnclosingScope();
         }
         return scope;
     }
 }
