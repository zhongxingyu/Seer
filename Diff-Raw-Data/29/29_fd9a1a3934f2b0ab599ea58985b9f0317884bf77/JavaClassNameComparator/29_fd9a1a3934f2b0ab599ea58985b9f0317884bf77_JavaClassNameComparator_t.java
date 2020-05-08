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
 
 package net.sf.clirr.framework;
 
 import java.util.Comparator;
 import org.apache.bcel.classfile.JavaClass;
 
 /**
  * A class which specifies that JavaClass instances are ordered
  * by string comparisons of their names.
 *
 * @author Simon Kitching
  */
 
public final class JavaClassNameComparator implements Comparator
 {
    /**
     * A JavaClassNameComparator does not carry any state,
     * so only this singleton instance should be used.
     */
    public static final JavaClassNameComparator COMPARATOR =
         new JavaClassNameComparator();

    /**
     * Prevents external instantiation.
     * Clients should use {@link #COMPARATOR} exclusively.
     */
    private JavaClassNameComparator()
    {
    }

    /** @see Comparator#compare */
     public int compare(Object o1, Object o2)
     {
        String n1 = ((JavaClass) o1).getClassName();
        String n2 = ((JavaClass) o2).getClassName();
         return n1.compareTo(n2);
     }
 }
