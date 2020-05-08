 //////////////////////////////////////////////////////////////////////////////
 // Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003 - 2004  Lars Khne
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
 
 package net.sf.clirr.core.internal.checks;
 
 import net.sf.clirr.core.Severity;
 import net.sf.clirr.core.ScopeSelector;
 import net.sf.clirr.core.Message;
 import net.sf.clirr.core.internal.AbstractDiffReporter;
 import net.sf.clirr.core.internal.ApiDiffDispatcher;
 import net.sf.clirr.core.internal.ClassChangeCheck;
 import net.sf.clirr.core.CheckerException;
 
 import org.apache.bcel.classfile.JavaClass;
 
 /**
  * Detects changes in class access declaration, for both "top-level" classes,
  * and nested classes.
  * <p>
  * Java class files only ever contain scope specifiers of "public" or "package".
  * For top-level classes, this is expected: it is not possible to have a
  * top-level protected or private class.
  * <p>
  * However nested classes <i>can</i> be declared as protected or private. The
  * way to tell the real scope of a nested class is to ignore the scope in
  * the actual class file itself, and instead look in the "InnerClasses"
  * attribute stored on the enclosing class. This is exactly what the java
  * compiler does when compiling, and what the jvm does when verifying class
  * linkage at runtime.
  *
  * @author Simon Kitching
  */
public final class ClassScopeCheck
        extends AbstractDiffReporter
        implements ClassChangeCheck
 {
     private static final Message MSG_SCOPE_INCREASED = new Message(1000);
     private static final Message MSG_SCOPE_DECREASED = new Message(1001);
     private static final Message MSG_ERROR_DETERMINING_SCOPE_OLD = new Message(1002);
     private static final Message MSG_ERROR_DETERMINING_SCOPE_NEW = new Message(1003);
 
     private ScopeSelector scopeSelector;
 
     /**
      * Create a new instance of this check.
      * @param dispatcher the diff dispatcher that distributes the detected changes to the listeners.
      */
     public ClassScopeCheck(ApiDiffDispatcher dispatcher, ScopeSelector scopeSelector)
     {
         super(dispatcher);
         this.scopeSelector = scopeSelector;
     }
 
     /** {@inheritDoc} */
     public boolean check(JavaClass compatBaseline, JavaClass currentVersion)
     {
         ScopeSelector.Scope bScope;
         try
         {
             bScope = ScopeSelector.getClassScope(compatBaseline);
         }
         catch (CheckerException ex)
         {
            log(MSG_ERROR_DETERMINING_SCOPE_OLD,
                Severity.ERROR, compatBaseline.getClassName(), null, null,
                new String[] {ex.getMessage()});
             return false;
         }
 
         ScopeSelector.Scope cScope;
         try
         {
             cScope = ScopeSelector.getClassScope(currentVersion);
         }
         catch (CheckerException ex)
         {
            log(MSG_ERROR_DETERMINING_SCOPE_NEW,
                Severity.ERROR, compatBaseline.getClassName(), null, null,
                new String[] {ex.getMessage()});
             return false;
         }
 
         if (!scopeSelector.isSelected(bScope) && !scopeSelector.isSelected(cScope))
         {
             // neither the old nor the new class are "visible" at the scope
             // the user of this class cares about, so just skip this test
             // and all following tests for this pair of classes.
             return false;
         }
 
         if (cScope.isMoreVisibleThan(bScope))
         {
             String[] args = {bScope.getDesc(), cScope.getDesc()};
 
            log(MSG_SCOPE_INCREASED,
                Severity.INFO, compatBaseline.getClassName(), null, null, args);
         }
         else if (cScope.isLessVisibleThan(bScope))
         {
             String[] args = {bScope.getDesc(), cScope.getDesc()};
 
            log(MSG_SCOPE_DECREASED,
                getSeverity(compatBaseline, Severity.ERROR),
                compatBaseline.getClassName(), null, null, args);
         }
 
         // Apply further checks only if both versions of the class have scopes
         // of interest. For example, when the user is only interested in
         // public & protected classes, then for classes which have just become
         // public/protected we just want to report that it is now "visible";
         // because the class was not visible before the differences since its
         // last version are not relevant. And for classes which are no longer
         // public/protected, we just want to report that the whole class is no
         // longer "visible"; as it is not visible to users any changes to it
         // are irrelevant.
         return scopeSelector.isSelected(bScope) && scopeSelector.isSelected(cScope);
     }
 
 }
