 package net.sf.clirr.core.spi;
 
 /**
  * A Java source code entity like a type or a method that has the 
  * concept of a visibility scope.
  * 
  * Each entity has two scopes: One that is declared and the effective scope.
  * For example a public method can have an effective scope of package if it
  * appears in a class that is package visible.
  *  
  * @author lk
  *
  */
 public interface Scoped
 {
     /**
      * The declared scope of this entity.
      * @return the scope that appears in the modifiers of this entity.
      */
     Scope getDeclaredScope();
     
     /**
      * The effective Scope of this entity.
      * 
      * @return the minimum scope of the modifiers of this entity and
     * all of it's containers.
      */
     Scope getEffectiveScope();
 }
