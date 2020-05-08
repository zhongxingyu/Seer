 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 //
 // Basic compiler error.
 
 package me.pavlina.alco.compiler.errors;
 
 import java.io.PrintStream;
 import me.pavlina.alco.compiler.ErrorAnnotator;
 import me.pavlina.alco.lex.Token;
 
 /**
  * Any compiler error. This is the base class for Unexpected, UnexpectedChar,
  * and UnexpectedEOF, and can also be thrown by itself.
  */
 public class CError extends Exception
 {
 
     /**
      * Return a CError initialised after (marking the position directly
      * following) a token.
      * @param message Error message string
      * @param token Token with which to associate
      * @return CError object
      */
     public static CError after (String message, Token token)
     {
         // Create a CError after the token
         int line = token.line;
         int col = token.col + token.value.length ();
         return new CError (message, line, col, 0, 0, token.annotator);
     }
 
     /**
      * Return a CError initialised at (underlining) a token.
      * @param message Error message string
      * @param token Token with which to associate
      * @return CError object
      */
     public static CError at (String message, Token token)
     {
         // Create a CError at the token
         int line = token.line;
         int col = token.col;
         int start = token.col;
         int stop = token.col + token.value.length ();
         return new CError (message, line, col, start, stop, token.annotator);
     }
 
     protected String         message, note;
     protected int            line, col, start, stop;
     protected ErrorAnnotator annotator;
 
 
     /**
      * Do not initialise the CError. Some subclasses are limited by the fact
      * that super() must come first, so they can initialise CError themselves.
      */
     protected CError ()
     {
        note = "";
     }
 
     /**
      * Initialise a CError without a code location.
      * @param message Error message string
      */
     public CError (String message)
     {
         super ("uncaught compiler error");
         this.message = message;
         this.note = "";
         line = col = start = stop = 0;
         annotator = null;
     }
 
     /**
      * Initialise a CError.
      * @param message Error message string
      * @param line Line of code which triggered the error (zero-based)
      * @param col Column under which to show an arrow (zero-based)
      * @param start Column under which to start an underline
      * @param stop Column before which to stop the underline
      * @param annotator ErrorAnnotator object to format the code line
      */
     public CError (String message, int line, int col, int start, int stop,
             ErrorAnnotator annotator)
     {
         super ("uncaught compiler error");
         this.message = message;
         this.note = "";
         this.line = line;
         this.col = col;
         this.start = start;
         this.stop = stop;
         this.annotator = annotator;
     }
 
     /**
      * Set a note to be displayed after the error message. */
     public void setNote (String note) {
         this.note = note;
     }
 
     /**
      * Print out the error message, fully annotated.
      * @param out PrintStream to which to output
      */
     public void print (PrintStream out)
     {
         if (annotator == null) {
             out.print ("error: ");
             out.println (message);
         } else {
             out.printf ("%s:%d:%d: error: ", annotator.filename (), line + 1,
                         col + 1);
             out.println (message);
             annotator.annotate (line, col, start, stop, out);
         }
         out.print (note);
     }
 
 }
