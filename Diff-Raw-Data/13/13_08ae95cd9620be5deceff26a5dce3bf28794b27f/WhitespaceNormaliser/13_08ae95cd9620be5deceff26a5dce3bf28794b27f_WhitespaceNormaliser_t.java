 /*
  * Copyright (C) 2013 University of Edinburgh.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.org.ukfederation.mdnorm;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 
 /**
  * Normalises white space according to some basic rules provided as constructor
  * arguments.
  */
 public class WhitespaceNormaliser {
 
     /**
      * Indicates whether to retain odd spaces (ones which do not
      * align exactly with a tab stop).
      */
     private final boolean retainingOddSpaces;
 
     /**
      * Indicates whether to retain blank lines.
      */
     private final boolean retainingBlankLines;
 
     /**
      * Number of spaces corresponding to a tab character.
      */
     private final int tabSize;
 
     /**
      * Constructor.
      *
      * @param retainOddSpaces whether to retain odd spaces (ones which do not
      *      align exactly with a tab stop)
      * @param retainBlankLines whether to retain blank lines
      * @param spacesPerTab number of spaces corresponding to a tab character
      */
     public WhitespaceNormaliser(final boolean retainOddSpaces,
             final boolean retainBlankLines, final int spacesPerTab) {
         retainingOddSpaces = retainOddSpaces;
         retainingBlankLines = retainBlankLines;
         tabSize = spacesPerTab;
     }
 
     /**
      * Processes a stream of characters, normalising the white space on the way by.
      * 
      * @param in source of character stream
      * @param out destination of character stream
      * 
      * @throws IOException if thrown by the reader or writer
      */
     // CheckStyle: CyclomaticComplexity|MethodLength OFF -- more readable not split up
     public void process(final Reader in, final Writer out) throws IOException {
         // CheckStyle:CyclomaticComplexity|MethodLength ON
        
        // amount of leading white space
        int lead = 0;
        
        // processing the start of the line
        boolean start = true;
        
         for (;;) {
             final int c = in.read();
 
             if (start) {
                 switch (c) {
 
                     case -1:
                         // end of input at line start
                         return;
 
                     case '\r':
                         // drop carriage return if we see one
                         break;
 
                     case ' ':
                         lead++;
                         break;
 
                     case '\t':
                         do {
                             lead++;
                         } while ((lead % tabSize) != 0);
                         break;
 
                     case '\n':
                         // line contains only whitespace
                        // throw away white space entirely
                        lead = 0;
                         if (retainingBlankLines) {
                             out.append('\n');
                         }
                         break;
 
                     default:
                         // flush leading space as tabs
                         while (lead >= tabSize) {
                             lead -= tabSize;
                             out.append('\t');
                         }
 
                         /*
                          * If the tabs don't make up the required space exactly, optionally make up the difference with
                          * spaces.
                          */
                         if (retainingOddSpaces) {
                             while (lead > 0) {
                                 lead--;
                                 out.append(' ');
                             }
                         }
 
                         // no longer processing leading space
                         start = false;
 
                         // retain this character
                         out.append((char) c);
                         break;
 
                 }
 
             } else {
 
                 switch (c) {
 
                     case -1:
                         // end of file in middle of line
                         out.append('\n');
                         return;
 
                     case '\n':
                         start = true;
                         lead = 0;
                         out.append((char) c);
                         break;
 
                     default:
                         out.append((char) c);
                         break;
 
                 }
             }
         }
     }
 }
