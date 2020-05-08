 // Copyright (C) 2000 Paco Gomez
 // Copyright (C) 2000, 2001, 2002, 2003 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.common;
 
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 
 /**
  * Base exception class for The Grinder. Includes support for chained
  * (or "nested") exceptions.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class GrinderException extends Exception {
 
   private final Throwable m_nestedThrowable;
 
   /**
    * Creates a new <code>GrinderException</code> instance with no
    * nested <code>Throwable</code>.
    *
    * @param message Helpfull message.
    */
   public GrinderException(String message) {
     this(message, null);
   }
 
   /**
    * Creates a new <code>GrinderException</code> instance.
    *
    * @param message Helpfull message.
    * @param nestedThrowable A nested <code>Throwable</code>
    */
   public GrinderException(String message, Throwable nestedThrowable) {
     super(message);
     m_nestedThrowable = nestedThrowable;
   }
 
   /**
    * Print a stack trace to the given <code>PrintWriter</code>.
    *
    * @param s a <code>PrintWriter</code> value
    */
   public void printStackTrace(PrintWriter s) {
     printNestedTrace(s);
     printImmediateTrace(s);
     s.println();
     s.flush();
   }
 
   /**
    * Print the stack trace excluding information about nested
    * exceptions.
    *
    * @param s a <code>PrintWriter</code> value
    */
   private void printImmediateTrace(PrintWriter s) {
     super.printStackTrace(s);
   }
 
   /**
    * Recursively print the nested exception stack trace. Where
    * possible, nested exception stack traces are shortened to remove
    * common stack frames that they share with their parent.
    *
    * @param s a <code>PrintWriter</code> value
    */
   private void printNestedTrace(PrintWriter s) {
     if (m_nestedThrowable != null) {
 
       final StringWriter stringWriter = new StringWriter(256);
       final PrintWriter printWriter = new PrintWriter(stringWriter);
       printImmediateTrace(printWriter);
       printWriter.flush();
       final StringBuffer immediateTrace = stringWriter.getBuffer();
 
       final StringWriter stringWriter2 = new StringWriter(256);
       final PrintWriter printWriter2 = new PrintWriter(stringWriter2);
 
       if (m_nestedThrowable instanceof GrinderException) {
         final GrinderException nestedGrinderException =
           (GrinderException)m_nestedThrowable;
 
         nestedGrinderException.printNestedTrace(s);
         nestedGrinderException.printImmediateTrace(printWriter2);
       }
       else {
         m_nestedThrowable.printStackTrace(printWriter2);
       }
 
       printWriter2.flush();
       final StringBuffer nestedTrace = stringWriter2.getBuffer();
       final boolean truncatedNestedTrace =
         removeCommonSuffix(nestedTrace, immediateTrace);
 
       s.print("(Nested exception) ");
       s.println(nestedTrace);
       if (truncatedNestedTrace) {
         s.println("\t<truncated>");
       }
       s.println();
     }
   }
 
   /**
    * If <code>original</code> shares a common suffix that begins
    * with a line feed with <code>other</code>, truncate
    * <code>original</code> to remove the suffix. Otherwise return
    * <code>original</code> unchanged.
    *
    * Package scope so the unit tests can access it.
    *
    * @param original The original string. Changed in-place.
    * @param other String to compare suffixes with. Unchanged.
    * @return Whether original was truncated or not.
    */
   static boolean removeCommonSuffix(StringBuffer original,
                                     StringBuffer other) {
     int p = original.length();
     int otherP = other.length();
 
     // Search backwards for first difference, ignoring white
     // space.
     OUTER:
     do {
       do {
         if (--p < 0) {
           // original is contained at end of other.
           break OUTER;
         }
       }
       while (Character.isWhitespace(original.charAt(p)));
 
       do {
         if (--otherP < 0) {
           break OUTER;
         }
       }
       while (Character.isWhitespace(other.charAt(otherP)));
     }
     while (original.charAt(p) == other.charAt(otherP));
 
     // p is now the index of the last character that differs.
 
     // Now wind forward to first new line.
     do {
       ++p;
 
       if (p == original.length()) {
         return false;
       }
     }
    while (original.charAt(p) != '\n');
 
     original.setLength(p);
     return true;
   }
 
   /**
    * Print a stack trace to the standard error stream.
    */
   public void printStackTrace() {
     printStackTrace(System.err);
   }
 
   /**
    * Print a stack trace to the given <code>PrintStream</code>.
    *
    * @param s a <code>PrintStream</code> value
    */
   public void printStackTrace(PrintStream s) {
     printStackTrace(new PrintWriter(s));
   }
 
   /**
    * Return any nested <code>Throwable</code>.
    *
    * @return A <code>Throwable</code> value, or <code>null</code> if
    * none.
    */
   public Throwable getNestedThrowable() {
     return m_nestedThrowable;
   }
 }
