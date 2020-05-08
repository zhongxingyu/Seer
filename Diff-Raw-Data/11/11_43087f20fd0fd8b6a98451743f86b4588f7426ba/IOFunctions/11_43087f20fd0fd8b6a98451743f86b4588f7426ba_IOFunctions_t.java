 /**
  *  Copyright (c) 2011, The Staccato-Commons Team
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation; version 3 of the License.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  */
 
 package net.sf.staccatocommons.io;
 
 import java.io.File;
 
 import net.sf.staccatocommons.defs.function.Function;
 import net.sf.staccatocommons.lang.function.AbstractFunction;
 import net.sf.staccatocommons.restrictions.Constant;
 
 /**
  * @author flbulgarelli
  * 
  */
 public class IOFunctions {
 
   /**
    * Answers a function that returns the name of a file
    * 
    * @return a {@link Function} that returns <code>arg.getName()</code>
    */
   @Constant
   public static Function<File, String> fileName() {
     return new AbstractFunction<File, String>() {
       public String apply(File arg) {
         return arg.getName();
       }
     };
   }
 
   /**
    * Answers a function that returns the path of a file
    * 
    * @return a {@link Function} that returns <code>arg.getPath()</code>
    */
   @Constant
   public static Function<File, String> filePath() {
     return new AbstractFunction<File, String>() {
       public String apply(File arg) {
         return arg.getPath();
       }
     };
   }
 
 }
