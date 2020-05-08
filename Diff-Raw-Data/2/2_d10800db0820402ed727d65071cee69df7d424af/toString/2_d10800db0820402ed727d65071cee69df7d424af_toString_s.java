 // Tags: JDK1.1
 
 // Copyright (C) 1999, 2000 Red Hat, Inc.
 
 // This file is part of Mauve.
 
 // Mauve is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2, or (at your option)
 // any later version.
 
 // Mauve is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with Mauve; see the file COPYING.  If not, write to
 // the Free Software Foundation, 59 Temple Place - Suite 330,
 // Boston, MA 02111-1307, USA.  */
 
 package gnu.testlet.java.lang.reflect.Method;
 import gnu.testlet.Testlet;
 import gnu.testlet.TestHarness;
 import java.lang.reflect.Method;
 import java.lang.reflect.InvocationTargetException;
 
 public class toString implements Testlet
 {
   public Method getMethod (Class ic, String name, Class[] list)
   {
     Method m = null;
     try
       {
 	m = ic.getMethod(name, list);
       }
     catch (Throwable _)
       {
 	// Nothing.
       }
     return m;
   }
 
   public String no_args ()
   {
     return "zardoz";
   }
 
   public void test (TestHarness harness)
   {
     Class ic = null;
     try
       {
 	ic = Class.forName ("gnu.testlet.java.lang.reflect.Method.toString");
       }
     catch (Throwable _)
       {
 	// Lose.
       }
 
     Class[] na_list = new Class[0];
     Method na_meth = getMethod (ic, "no_args", na_list);
    harness.check (na_meth.toString (), "public void gnu.testlet.java.lang.reflect.Method.toString.no_args(java.lang.String)");
   }
 }
