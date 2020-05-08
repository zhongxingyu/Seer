 /*
  * Copyright (C) 2011 by Sergey Arkhipov <serge@aerialsounds.org>
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
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 
 
 package org.aerialsounds.ccli;
 
 
 
 import static java.util.Arrays.sort;
 
 import java.util.Collection;
 import java.util.Set;
 
 import org.aerialsounds.ccli.options.AbstractOption;
 
 
 
 public class CliHelpGenerator {
 
 
 
 // ===============================================================================================================
 // F I E L D S
 // ===============================================================================================================
 
 
 
     protected StringBuilder builder;
     protected String[]      names;
 
 
 
 // ===============================================================================================================
 // P U B L I C   M E T H O D S
 // ===============================================================================================================
 
 
 
     final public String
     generate (final Collection<Set<AbstractOption>> values) {
         builder = new StringBuilder();
         names   = null;
 
         for ( Set<AbstractOption> set : values )
             generateForSet(set);
 
         String ret     = builder.toString();
                builder = null;
                names   = null;
 
         return ret;
     } /* generate */
 
 
 
 // ===============================================================================================================
 // P R O T E C T E D   M E T H O D S
 // ===============================================================================================================
 
 
 
     protected void
     generateOptionList () {
         for ( int i = 0; i < names.length; ++i ) {
             if ( i > 0 )
                 builder.append(", ");
             builder.append(names[i]);
         }
     } /* generateOptionList */
 
 
     protected void
     generateHelp (final Set<AbstractOption> set) {
         if ( !set.isEmpty() ) {
             builder.append("\n    ");
            builder.append(set.iterator().next().getFullName());
             builder.append("\n");
         }
     } /* generateHelp */
 
 
     protected void
     after () {
 
     } /* after */
 
 
     protected void
     before () {
 
     } /* before */
 
 
 
 // ===============================================================================================================
 // P R I V A T E   M E T H O D S
 // ===============================================================================================================
 
 
 
     private void
     generateForSet (final Set<AbstractOption> set) {
         before();
         retrieveOptionNames(set);
         generateOptionList();
         generateHelp(set);
         after();
     } /* generateForSet */
 
 
     private void
     retrieveOptionNames (final Set<AbstractOption> set) {
         names = new String[set.size()];
 
         int i = 0;
         for ( AbstractOption o : set )
             names[i++] = o.getFullName();
 
         sort(names);
     } /* retrieveOptionNames */
 
 
 } /* class CliHelpGenerator */
 
