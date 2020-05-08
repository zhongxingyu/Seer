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
 
 import java.util.Arrays;
 
 import net.sf.clirr.event.Severity;
 import net.sf.clirr.framework.AbstractDiffReporter;
 import net.sf.clirr.framework.ApiDiffDispatcher;
 
 public abstract class AbstractClassSetChangeCheck
         extends AbstractDiffReporter
 {
     protected AbstractClassSetChangeCheck(ApiDiffDispatcher dispatcher)
     {
         super(dispatcher);
     }
 
    protected final void compareClassNameSets(
            String[] setA, String[] setB, String diffPrefix, Severity severity)
     {
         Arrays.sort(setA);
         Arrays.sort(setB);
 
         for (int i = 0; i < setA.length; i++)
         {
             String oldClassName = setA[i];
             if (Arrays.binarySearch(setB, oldClassName) < 0)
             {
                 log(diffPrefix + oldClassName, severity);
             }
         }
     }
 
 }
