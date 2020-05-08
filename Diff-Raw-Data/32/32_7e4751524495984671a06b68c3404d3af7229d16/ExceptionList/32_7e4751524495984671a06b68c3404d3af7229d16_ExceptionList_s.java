 /*******************************************************************************
  * $Id$
  * $Author$
  * $Date$
  *
  * Copyright 2002-2003  YAJUL Developers, Joshua Davis, Kent Vogel.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  ******************************************************************************/
 package org.yajul.util;
 
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  * A checked exception that contains a list of other exceptions.
  * User: josh
  * Date: Oct 24, 2003
  * Time: 7:51:20 AM
  */
 public class ExceptionList extends Exception
 {
     /** The list of throwables. **/
     private ArrayList list = new ArrayList();
     /** True if a message was explicitly specified. **/
     private boolean messageSpecified = false;
 
     /**
      * Constructs a new exception with <code>null</code> as its detail message.
      * The cause is not initialized, and may subsequently be initialized by a
      * call to initCause.
      */
     public ExceptionList()
     {
     }
 
     /**
      * Constructs a new exception with the specified detail message.  The
      * cause is not initialized, and may subsequently be initialized by
      * a call to initCause.
      *
      * @param   message   the detail message. The detail message is saved for
      *          later retrieval by the {@link #getMessage()} method.
      */
     public ExceptionList(String message)
     {
         super(message);
         messageSpecified = true;
     }
 
     /**
      * Constructs a new exception with the specified detail message and
      * cause.  <p>Note that the detail message associated with
      * <code>cause</code> is <i>not</i> automatically incorporated in
      * this exception's detail message.
      *
      * @param  message the detail message (which is saved for later retrieval
      *         by the {@link #getMessage()} method).
      * @param  cause the cause (which is saved for later retrieval by the
      *         {@link #getCause()} method).  (A <tt>null</tt> value is
      *         permitted, and indicates that the cause is nonexistent or
      *         unknown.)
      */
     public ExceptionList(String message, Throwable cause)
     {
         super(message);
         messageSpecified = true;
         add(cause);
     }
 
     /**
      * Constructs a new exception with the specified cause and a detail
      * message of <tt>(cause==null ? null : cause.toString())</tt> (which
      * typically contains the class and detail message of <tt>cause</tt>).
      * This constructor is useful for exceptions that are little more than
      * wrappers for other throwables (for example, {@link
      * java.security.PrivilegedActionException}).
      *
      * @param  cause the cause (which is saved for later retrieval by the
      *         {@link #getCause()} method).  (A <tt>null</tt> value is
      *         permitted, and indicates that the cause is nonexistent or
      *         unknown.)
      */
     public ExceptionList(Throwable cause)
     {
         add(cause);
     }
 
     /**
      * Returns the cause of this throwable or <code>null</code> if the
      * cause is nonexistent or unknown.  (The cause is the throwable that
      * caused this throwable to get thrown.)
      *
      * <p>This implementation returns the cause that was supplied via one of
      * the constructors requiring a <tt>Throwable</tt>, or that was set after
      * creation with the initCause(Throwable) method.  While it is
      * typically unnecessary to override this method, a subclass can override
      * it to return a cause set by some other means.  This is appropriate for
      * a "legacy chained throwable" that predates the addition of chained
      * exceptions to <tt>Throwable</tt>.  Note that it is <i>not</i>
      * necessary to override any of the <tt>PrintStackTrace</tt> methods,
      * all of which invoke the <tt>getCause</tt> method to determine the
      * cause of a throwable.
      *
      * @return  the cause of this throwable or <code>null</code> if the
      *          cause is nonexistent or unknown.
      */
     public Throwable getCause()
     {
         if (list.size() > 0)
             return (Throwable)list.get(0);
         else
             return null;
     }
 
     /**
      * Converts everything to a string.
      * @return a string.
      */
     public String toString()
     {
         if (list.size() == 0)
             return super.toString();
         else
         {
             StringBuffer buf = new StringBuffer();
             buf.append(super.toString());
             int i = 0;
             for (Iterator iterator = list.iterator(); iterator.hasNext();i++)
             {
                 Throwable throwable = (Throwable) iterator.next();
                 buf.append("\nDetail [");
                 buf.append(Integer.toString(i));
                 buf.append("] = ");
                 buf.append(throwable.toString());
             }
             return buf.toString();
         }
     }
 
     /**
      * Returns the detail message string of this throwable.
      *
      * @return  the detail message string of this <tt>Throwable</tt> instance
      *          (which may be <tt>null</tt>).
      */
     public String getMessage()
     {
         if ((!messageSpecified) && (list.size() > 0))
             return getFirst().getMessage();
         else
             return super.getMessage();
     }
 
     /**
      * Creates a localized description of this throwable.
      * Subclasses may override this method in order to produce a
      * locale-specific message.  For subclasses that do not override this
      * method, the default implementation returns the same result as
      * <code>getMessage()</code>.
      *
      * @return  The localized description of this throwable.
      * @since   JDK1.1
      */
     public String getLocalizedMessage()
     {
         if ((!messageSpecified) && (list.size() > 0))
             return getFirst().getLocalizedMessage();
         else
             return super.getLocalizedMessage();
     }
 
     /**
      * Prints a stack trace.
      */
     public void printStackTrace()
     {
         super.printStackTrace();
         for (Iterator iterator = list.iterator(); iterator.hasNext();)
         {
             Throwable throwable = (Throwable) iterator.next();
             throwable.printStackTrace();
         }
     }
 
     /**
      * Prints a stack trace to the specified print stream.
      * @param s the print stream.
      */
     public void printStackTrace(PrintStream s)
     {
         super.printStackTrace(s);
         int i = 0;
         for (Iterator iterator = list.iterator(); iterator.hasNext();i++)
         {
             Throwable throwable = (Throwable) iterator.next();
             s.println("Detail [" + i + "] :");
             throwable.printStackTrace(s);
         }
     }
 
     /**
      * Prints this throwable and its backtrace to the specified print writer.
      * @param w the print writer.s
      */
      public void printStackTrace(PrintWriter w)
      {
         super.printStackTrace(w);
          int i = 0;
          for (Iterator iterator = list.iterator(); iterator.hasNext();i++)
          {
              Throwable throwable = (Throwable) iterator.next();
              w.println("Detail [" + i + "] :");
              throwable.printStackTrace(w);
          }
     }
 
     /**
      * Adds a new exception (Throwable) to the list.
      * @param t The new exception (Throwable).
      */
     public void add(Throwable t)
     {
         list.add(t);
     }
 
     /**
      * Returns the number of throwables in the list.
      * @return int - The number of throwables in the list.
      */
     public int size()
     {
         return list.size();
     }
 
     /**
      * Returns an iterator which will provide all of the Throwables.
      * @return Iterator - An iterator of all the Throwables.
      */
     public Iterator iterator()
     {
         return list.iterator();
     }
 
     /**
      * Throws this ExceptionList object if there are more than one
      * throwables in the list.  If there is only one throwable in the
      * list, the throwable element will be thrown.
      * @throws Throwable if the size of the list is not zero.
      */
     public void throwIfThrowable() throws Throwable
     {
         // If there is only one throwable in the list, just
         // throw it.
         if (list.size() == 1)
             throw getFirst();
         // Otherwise, throw the whole list.
        else
            throw this;
     }
 
     /**
      * Throws this ExceptionList object if there are more than one
      * throwables in the list.  If there is only one throwable in the
      * list, *and* that throwable is acutally a checked exception, then
      * the throwable element will be thrown.
      * @throws Exception if the size of the list is not zero.
      */
     public void throwIfException() throws Exception
     {
         if (list.size() == 1)
         {
             Throwable t = getFirst();
             if (t instanceof Exception)
             {
                 Exception exception = (Exception) t;
                 throw exception;
             }
             throw this;
         }
        else
         {
             throw this;
         }
     }
 
     private Throwable getFirst()
     {
         Throwable t = (Throwable) list.get(0);
         return t;
     }
 }
