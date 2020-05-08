 //////////////////////////////////////////////////////////////////////////////
 // Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003 - 2005  Lars Khne
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //////////////////////////////////////////////////////////////////////////////
 package net.sf.clirr.core;
 
 import net.sf.clirr.core.spi.Scope;
 import net.sf.clirr.core.spi.Scoped;
 
 /**
  * Selects zero or more java scope values (public, protected, package,
  * private). An instance of this class is used when comparing two versions
  * of an application to indicate what items are of interest. When the target
  * audience is "normal" users of the applications, only changes to items
  * which have public or protected scope are relevant. When the audience is
  * developers of the applications, then package-scope and private-scope
  * changes are also of interest.
  *
  * @author Simon Kitching
  */
 public final class ScopeSelector
 {
     private Scope scope = Scope.PROTECTED;
 
     /**
      * Construct an instance which selects public and protected objects and
      * ignores package and private objects. The selectXXX methods can later
      * be used to adjust this default behaviour.
      */
     public ScopeSelector()
     {
     }
 
     /**
      * Construct an instance which selects public and protected objects and
      * ignores package and private objects. The selectXXX methods can later
      * be used to adjust this default behaviour.
      */
     public ScopeSelector(Scope scope)
     {
         this.scope = scope;
     }
 
     /** Specify which scope objects are of interest. */
     public void setScope(Scope scope)
     {
         this.scope = scope;
     }
 
     /**
      * Get the scope that this object is configured with.
      */
     public Scope getScope()
     {
         return scope;
     }
 
     /**
      * Return a string which indicates what scopes this object will consider
      * to be selected (ie relevant).
      */
     public String toString()
     {
         return scope.getDesc();
     }
 
     /**
     * Given a BCEL object, return true if this object's scope is one of the
      * values this object is configured to match.
     * <p>
     * Note that BCEL classes Field and Method inherit from the AccessFlags
     * base class and so are valid parameters to this method.
     * <p>
     * Note that despite JavaClass objects extending AccessFlags, the
     * methods which determine the accessibility of a JavaClass fail
     * miserably (bad bcel design) for nested classes. Therefore this
     * method <i>must not</i> be passed a JavaClass object as a parameter.
     * If this is done, a RuntimeException will be thrown to indicate a
     * programmer error.
      *
     * @param object is the object whose scope is to be checked.
      * @return true if the object is selected.
      */
     public boolean isSelected(Scoped scoped)
     {
         return !scoped.getEffectiveScope().isLessVisibleThan(scope);
     }
 
     /**
      * Return true if objects of the specified scope, or more visible,
      * are selected by this selector.
      *
      * @param scope is the scope being checked
      * @return true if objects of the specified scope are selected.
      */
     public boolean isSelected(Scope scope)
     {
         return !scope.isLessVisibleThan(this.scope);
     }
 
 }
 
