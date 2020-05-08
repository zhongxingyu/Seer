 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 William Chesters
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc At paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 
 package org.melati.poem.prepro;
 
 import java.io.StreamTokenizer;
 
 
 /**
  * Thrown when an unexpected token is encountered during parsing.
  * Accommodates following  types of error. 
  * Syntactic assertion failures where the tokens found do not match 
  * those expected.
  * Vocabulary failures where values are outside the token set.
  * Model violations where legal expressions cannot actually be 
  * implemented, such as String troid columns.
  * Limitations on tables which must be either abstract or populated 
  * and must not hide a table in this or an imported DSD.     
  */
 class ParsingDSDException extends RuntimeException {
   private static final long serialVersionUID = 1L;
 
   String expected, got;
   int lineNumber;
   String message;
   FieldDef field = null;
 
   /**
    * Constructor.
    */
   ParsingDSDException() {
     super();
   }
 
   ParsingDSDException(String expected, String got) {
    this.expected = expected;
    this.got = "\"" + got + "\"";
   }
 
   ParsingDSDException(String expected, StreamTokenizer got) {
     this.expected = expected;
     this.got = "\"" + got.toString() + "\"";
     this.lineNumber = got.lineno();
   }
 
   ParsingDSDException(String expected, 
                       String got, StreamTokenizer context) {
     this.expected = expected;
     this.got = "\"" + got + "\" near " + context.toString();
     this.lineNumber = context.lineno();
   }
 
   ParsingDSDException(int lineNo, String error) {
     this.message = error;
     this.lineNumber = lineNo;
   }
 
   /** @return the message */
   public String getMessage() {
     String returnString = "";
     if (lineNumber != 0)
       returnString = "Definition ending on line " + lineNumber + ": ";
     if (expected != null)
       returnString = returnString + "Expected \"" + expected + 
          "\" but got " + got + "\n";
     if (message != null)
       returnString = returnString + message;
     return returnString;
   }
 }
