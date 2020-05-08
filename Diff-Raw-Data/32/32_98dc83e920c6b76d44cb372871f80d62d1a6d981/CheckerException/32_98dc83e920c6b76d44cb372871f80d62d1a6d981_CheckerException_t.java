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
 
 import net.sf.clirr.core.internal.ExceptionUtil;
 
 /**
  * An exception class representing a failure during checking of the
  * specified jar files.
  * <p>
  * The Clirr coding conventions use checked exceptions (such as this one)
  * for errors whose cause is something external to the clirr library/app.
  * Unchecked exceptions are used for errors that are due to bugs within
  * clirr code (assertion-violation type problems).
  */
public class CheckerException extends Exception
 {
     public CheckerException(String msg)
     {
         super(msg);
     }
 
     public CheckerException(String msg, Throwable cause)
     {
         super(msg);
         ExceptionUtil.initCause(this, cause);
     }
 }
