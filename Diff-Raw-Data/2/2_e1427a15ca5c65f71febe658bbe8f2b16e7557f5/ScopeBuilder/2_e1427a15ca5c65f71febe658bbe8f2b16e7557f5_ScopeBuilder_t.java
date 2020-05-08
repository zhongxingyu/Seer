 package org.clafer.scope;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.clafer.ast.AstClafer;
 
 /**
  * Builder pattern for scopes. Use {@link Scope#builder()},
 * {@link Scope#setScope(org.clafer.ast.AstClafer, int)}, {@link Scope#defaultScope(int)},
  * {@link Scope#intLow}, or {@link Scope#intHigh} to construct the builder. The
  * default scope and lowest and highest integers will be given a default if not
  * set explicitly.
  *
  * @author jimmy
  * @see Scope
  */
 public class ScopeBuilder implements Scopable {
 
     private final Map<AstClafer, Integer> scope;
     private int defaultScope = 1;
     private int intLow = -16;
     private int intHigh = 16;
 
     ScopeBuilder(Map<AstClafer, Integer> scope, int defaultScope, int intLow, int intHigh) {
         this.scope = new HashMap<>(scope);
         this.defaultScope = defaultScope;
         this.intLow = intLow;
         this.intHigh = intHigh;
     }
 
     ScopeBuilder() {
         this.scope = new HashMap<>();
     }
 
     /**
      * Set the scope of the Clafer. If the scope is already set for the Clafer,
      * then the new scope overrides the previous one.
      *
      * @param clafer the Clafer
      * @param scope the scope of the clafer
      * @return this builder
      */
     public ScopeBuilder setScope(AstClafer clafer, int scope) {
         this.scope.put(clafer, scope);
         return this;
     }
 
     /**
      * Adjust the scope of the Clafer. If the Clafer already has a scope then
      * {@code newScope = oldScope + adjust}. Otherwise
      * {@code newScope = defaultScope + adjust}.
      *
      * @param clafer the Clafer
      * @param adjust increment the scope by this amount
      * @return this builder
      */
     public ScopeBuilder adjustScope(AstClafer clafer, int adjust) {
         Integer oldScope = scope.get(clafer);
         scope.put(clafer, adjust + (oldScope == null ? defaultScope : oldScope.intValue()));
         return this;
     }
 
     /**
      * Set the scope for unspecified Clafers. If the default scope is already
      * set, then the new default scope overrides the previous one.
      *
      * @param defaultScope the default scope
      * @return this builder
      */
     public ScopeBuilder defaultScope(int defaultScope) {
         this.defaultScope = defaultScope;
         return this;
     }
 
     /**
      * Adjust the scope of unspecified Clafers.
      *
      * @param adjust increment the default scope by this amount
      * @return this builder
      */
     public ScopeBuilder adjustDefaultScope(int adjust) {
         defaultScope += adjust;
         return this;
     }
 
     /**
      * Set the lowest (inclusive) integer used for solving. If the lowest
      * integer is already set, then the new lowest integer overrides the
      * previous one.
      *
      * @param intLow the lowest integer
      * @return this builder
      */
     public ScopeBuilder intLow(int intLow) {
         this.intLow = intLow;
         return this;
     }
 
     /**
      * Adjust the lowest integer used for solving.
      *
      * @param adjust increment the lowest integer by this amount
      * @return this builder
      */
     public ScopeBuilder adjustIntLow(int adjust) {
         intLow += adjust;
         return this;
     }
 
     /**
      * Set the highest (inclusive) integer used for solving. If the highest
      * integer is already set, then the new highest integer overrides the
      * previous one.
      *
      * @param intHigh the highest integer
      * @return this builder
      */
     public ScopeBuilder intHigh(int intHigh) {
         this.intHigh = intHigh;
         return this;
     }
 
     /**
      * Adjust the highest integer used for solving.
      *
      * @param adjust increment the highest integer by this amount
      * @return this builder
      */
     public ScopeBuilder adjustIntHigh(int adjust) {
         intHigh += adjust;
         return this;
     }
 
     /**
      * Finalizes all the decisions made in the builder. Further changes to this
      * builder is permitted for building more scopes, but the returned scope
      * will not be affected.
      *
      * @return the built scope
      */
     @Override
     public Scope toScope() {
         return new Scope(scope, defaultScope, intLow, intHigh);
     }
 }
