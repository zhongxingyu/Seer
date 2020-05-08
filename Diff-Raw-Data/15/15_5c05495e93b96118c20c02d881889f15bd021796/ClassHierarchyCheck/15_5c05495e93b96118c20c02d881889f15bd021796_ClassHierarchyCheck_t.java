 //////////////////////////////////////////////////////////////////////////////
 // Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003  Lars Kühne
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
 
 package net.sf.clirr.checks;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.clirr.event.Severity;
 import net.sf.clirr.framework.AbstractDiffReporter;
 import net.sf.clirr.framework.ApiDiffDispatcher;
 import net.sf.clirr.framework.ClassChangeCheck;
 import org.apache.bcel.classfile.JavaClass;
 
 public final class ClassHierarchyCheck
         extends AbstractDiffReporter
         implements ClassChangeCheck
 {
     public ClassHierarchyCheck(ApiDiffDispatcher dispatcher)
     {
         super(dispatcher);
     }
 
     private List getSetDifference(JavaClass[] orig, JavaClass[] subtracted)
     {
         List list1 = getClassNames(orig);
         List list2 = getClassNames(subtracted);
 
         List retval = new ArrayList(list1);
         retval.removeAll(list2);
 
         /*
         System.out.println("list1 = " + list1);
         System.out.println("list2 = " + list2);
         System.out.println("retval = " + retval);
         */
         return retval;
     }
 
     private List getClassNames(JavaClass[] orig)
     {
         List list = new ArrayList(orig.length);
         for (int i = 0; i < orig.length; i++)
         {
             JavaClass javaClass = orig[i];
             list.add(javaClass.getClassName());
         }
         return list;
     }
 
     public void check(JavaClass compatBaseline, JavaClass currentVersion)
     {
         JavaClass[] compatSuper = compatBaseline.getSuperClasses();
         JavaClass[] currentSuper = currentVersion.getSuperClasses();
 
         List added = getSetDifference(currentSuper, compatSuper);
         List removed = getSetDifference(compatSuper, currentSuper);
 
         for (int i = 0; i < added.size(); i++)
         {
             String s = (String) added.get(i);
            log("Added " + s + " to the list of superclasses of "
                    + compatBaseline.getClassName(), Severity.INFO);
         }
 
         for (int i = 0; i < removed.size(); i++)
         {
             String s = (String) removed.get(i);
            log("Removed " + s + " from the list of superclasses of "
                    + compatBaseline.getClassName(), Severity.ERROR);
         }
     }
 }
