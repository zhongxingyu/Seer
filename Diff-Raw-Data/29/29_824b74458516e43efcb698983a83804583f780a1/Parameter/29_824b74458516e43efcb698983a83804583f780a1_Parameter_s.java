 /*
  * Copyright (C) 2011-2012  Christian Roesch
  * 
  * This file is part of micro-debug.
  * 
  * micro-debug is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * micro-debug is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with micro-debug.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.croesch.micro_debug.commons;
 
 import com.github.croesch.micro_debug.i18n.Text;
 import com.github.croesch.micro_debug.parser.IParser;
 import com.github.croesch.micro_debug.parser.IntegerParser;
 import com.github.croesch.micro_debug.parser.RegisterParser;
 
 /**
  * Represents the different types of parameter that are possible.
  * 
  * @author croesch
  * @since Date: Dec 3, 2011
  */
 public enum Parameter {
 
   /** the numerical argument, can be decimal or every other basis */
   NUMBER (new IntegerParser(), Text.INVALID_NUMBER),
 
   /** a {@link com.github.croesch.micro_debug.mic1.register.Register} as argument */
   REGISTER (new RegisterParser(), Text.INVALID_REGISTER);
 
   /** the parser that is able to parse a given string and return the parsed object */
   private final IParser parser;
 
   /** the text to visualise that the given string cannot be parsed - is not valid */
   private final Text errorText;
 
   /**
    * Create a parameter with the given parser to parse {@link String}s and the given {@link Text} to be able to
    * visualise invalid input to the user.
    * 
    * @since Date: Feb 22, 2012
    * @param p the {@link IParser} that parses the {@link String} and returns an {@link Object}
    * @param eText the {@link Text} to visualise that the input is invalid
    */
   private Parameter(final IParser p, final Text eText) {
     this.parser = p;
     this.errorText = eText;
   }
 
   /**
    * Converts the given {@link String} to an object that has the expected type. Returns an object with the logical type
    * of the enumeration that has the given value. Returns <code>null</code>, if the given string is not a valid value.
    * 
    * @since Date: Dec 3, 2011
    * @param str the value to convert into the correct data type, can be <code>null</code>
    * @return an {@link Object} with the logical type of the enumeration that has the value given by the given string,<br>
    *         or <code>null</code> if the given {@link String} is no valid representation for any value of the data type
    */
   public final Object getValue(final String str) {
    if (str == null) {
      // null is never valid
      return null;
    }

     final Object ret = this.parser.parse(str);
     if (ret == null) {
       Printer.printErrorln(this.errorText.text(str));
     }
     return ret;
   }
 }
