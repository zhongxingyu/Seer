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
 package com.github.croesch.micro_debug.parser;
 
 /**
  * Parses {@link Integer}s from {@link String}s.
  * 
  * @author croesch
  * @since Date: Feb 22, 2012
  */
 public final class IntegerParser implements IParser {
 
   /**
    * Parses the given {@link String} and returns an {@link Object} of the specific type. If the input cannot be parsed
    * into a valid object, than it will return <code>null</code>.
    * 
    * @since Date: Feb 22, 2012
    * @param toParse the {@link String} to convert to an {@link Object}.
    * @return the {@link Object} read from the {@link String},<br>
    *         or <code>null</code> if the input was invalid
    */
   public Integer parse(final String toParse) {
    if (toParse == null) {
      return null;
    }
 
     try {
       // try parsing and if this works, it was a valid number
       return Integer.valueOf(toParse);
     } catch (final NumberFormatException nfe) {
       // number might be 0x.. or .._.., so split the number in radix and the number
       // convert aliases (0x,0b,..) to the correct notation
       final String[] num = convertAliases(toParse).split("_");
       if (num.length != 2) {
         // not a valid number - no idea what to do
         return null;
       }
 
       // number is a special number
       try {
         // try to parse the number with the specified radix
         return Integer.valueOf(num[0], Integer.parseInt(num[1]));
       } catch (final NumberFormatException nfe2) {
         // didn't work -> invalid number
         return null;
       }
     }
   }
 
   /**
    * Converts valid aliases like <code>0x</code> to their representation in the notation.
    * 
    * @since Date: Jan 28, 2012
    * @param num the number that might contain aliases
    * @return the number with aliases converted to notation
    */
   private String convertAliases(final String num) {
     if (num.length() > 1 && num.charAt(0) == '0') {
       switch (Character.toLowerCase(num.charAt(1))) {
         case 'b':
           return num.substring(2) + "_2";
         case 'o':
           return num.substring(2) + "_8";
         case 'x':
           return num.substring(2) + "_16";
         default:
           return num;
       }
     }
     return num;
   }
 }
