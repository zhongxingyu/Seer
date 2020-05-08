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
 package com.github.croesch.micro_debug.argument;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Enumeration of all possible command line arguments for the debugger.
  * 
  * @author croesch
  * @since Date: Aug 13, 2011
  */
 public abstract class AArgument {
 
   /** the different ways this argument can be called */
   private final String[] args = new String[2];
 
   /** the number of parameters that argument requires */
   private final int numOfParams;
 
   /** the list of arguments that are available */
   private static final List<AArgument> VALUES = new ArrayList<AArgument>();
 
  static {
    VALUES.add(Help.getInstance());
    VALUES.add(OutputFile.getInstance());
    VALUES.add(UnbufferedOutput.getInstance());
    VALUES.add(Version.getInstance());
  }

   /**
    * Constructs a new argument with its name as long argument and its first letter as short argument. For example
    * ARGUMENT will result in:
    * <ul>
    * <li><code>--argument</code></li>
    * <li><code>-a</code></li>
    * </ul>
    * <br>
    * <b>Note:</b> A <code>_</code> in the name will be translated to a <code>-</code>.<br>
    * This argument will require no parameters.
    * 
    * @since Date: Aug 13, 2011
    * @see #Argument(int)
    */
   protected AArgument() {
     this(0);
   }
 
   /**
    * Constructs a new argument with its name as long argument and its first letter as short argument. For example
    * ARGUMENT will result in:
    * <ul>
    * <li><code>--argument</code></li>
    * <li><code>-a</code></li>
    * </ul>
    * <br>
    * <b>Note:</b> A <code>_</code> in the name will be translated to a <code>-</code>. <br>
    * This argument will require the passed number of parameters.
    * 
    * @param nop the number of parameters this argument requires
    * @since Date: Aug 17, 2011
    * @see #Argument()
    */
   protected AArgument(final int nop) {
     this.args[0] = "--" + name();
     this.args[1] = "-" + name().substring(0, 1);
     this.numOfParams = nop;
   }
 
   /**
    * Returns whether this argument can be called with the given {@link String}. Will return <code>false</code>, if the
    * given {@link String} is <code>null</code> or if the {@link Argument} is a pseudo-argument that cannot be called.
    * 
    * @since Date: Aug 13, 2011
    * @param argStr the {@link String} to test if it's a possible call for this argument
    * @return <code>true</code>, if this argument can be called with the given {@link String}.<br>
    *         For example <code>--argument</code> will return <code>true</code> for the argument <code>ARGUMENT</code>.
    */
   private boolean matches(final String argStr) {
     if (this instanceof WrongParameterNumberArgument || this instanceof UnknownArgument) {
       // make sure we cannot call pseudo-arguments
       return false;
     }
     return argStr != null && (argStr.equals(this.args[0]) || argStr.equals(this.args[1]));
   }
 
   /**
    * Returns the number of parameters this argument requires.
    * 
    * @since Date: Aug 17, 2011
    * @return the number of parameters for that argument.
    */
   private int getNumberOfParameters() {
     return this.numOfParams;
   }
 
   /**
    * Returns the {@link Argument} that matches with the given {@link String}.
    * 
    * @since Date: Aug 13, 2011
    * @param s the {@link String} that is able to call the returned {@link Argument}.
    * @return the {@link Argument} that matches the given {@link String}, or <code>null</code> if no argument can be
    *         called with the given {@link String}.
    * @see Argument#matches(String)
    */
   public static AArgument of(final String s) {
     for (final AArgument a : values()) {
       if (a.matches(s)) {
         return a;
       }
     }
     return null;
   }
 
   /**
    * Converts a given array of {@link String}s into a {@link Map} that contains an entry for each valid {@link Argument}
    * and the possible parameters belonging to it.
    * 
    * @since Date: Aug 17, 2011
    * @param args the array of {@link String}s
    * @return the {@link Map} that contains pairs of {@link Argument}s and arrays of strings that contain all parameters
    *         for that argument.
    */
   public static Map<AArgument, String[]> createArgumentList(final String[] args) {
     // map that'll contain the parsed arguments
     final Map<AArgument, String[]> map = new HashMap<AArgument, String[]>();
 
     if (args != null) {
       // arguments are a valid array, so start iterating
       for (int i = 0; i < args.length; ++i) {
 
         // handle only non-null-VALUES
         if (args[i] != null) {
           // parse the current argument
           final AArgument arg = of(args[i]);
           if (arg == null) {
             // unknown argument, add it to the map as parameter to ERROR_UNKNOWN
             addErrorArgument(map, UnknownArgument.getInstance(), args[i]);
           } else {
             // known argument, check if the required parameters are given
             if (i + arg.getNumberOfParameters() < args.length) {
               // copy parameters into own array
               final String[] params = new String[arg.getNumberOfParameters()];
               System.arraycopy(args, i + 1, params, 0, arg.getNumberOfParameters());
               // skip parsing the parameters of current argument in further iterations
               i += arg.getNumberOfParameters();
               // put argument with parameters to the map
               map.put(arg, params);
             } else {
               // not enough parameters given, add argument as parameter to ERROR_PARAM_NUMBER
               addErrorArgument(map, WrongParameterNumberArgument.getInstance(), args[i]);
             }
           }
         }
       }
     }
     return map;
   }
 
   /**
    * Appends the given value to the array belonging to the given argument in the given map.
    * 
    * @since Date: Dec 2, 2011
    * @param map the map, that'll contain a key argument and an array that contains at least the given value
    * @param arg the argument that is the key in the given map
    * @param value the new value to append to the array, belonging to the key in the map.
    */
   private static void addErrorArgument(final Map<AArgument, String[]> map, final AArgument arg, final String value) {
     if (!map.containsKey(arg)) {
       // key not in map -> create it
       map.put(arg, new String[] { value });
     } else {
       // key already in map -> append value to the array
       final String[] newArray = appendValueToArray(map.get(arg), value);
       map.put(arg, newArray);
     }
   }
 
   /**
    * Appends a value to the given array and returns the new created array.
    * 
    * @since Date: Dec 2, 2011
    * @param old the array to append the value to
    * @param value the value to append
    * @return the new array, containing elements from the old array and the new value.
    */
   private static String[] appendValueToArray(final String[] old, final String value) {
     final String[] newArray = new String[old.length + 1];
     // copy array
     System.arraycopy(old, 0, newArray, 0, old.length);
     // set new value
     newArray[old.length] = value;
 
     return newArray;
   }
 
   /**
    * Executes commands that result in the specific argument.
    * 
    * @since Date: Dec 2, 2011
    * @param params the parameters of that argument, in case of the pseudo arguments (prefix ERROR_) this array contains
    *        the causes for the error.
    * @return <code>false</code>, if the argument enforces the application to stop
    */
   public abstract boolean execute(String ... params);
 
   /**
    * Returns the name of this argument containing only lower case characters and -.
    * 
    * @since Date: Feb 28, 2012
    * @return the name of this argument.
    */
   protected abstract String name();
 
   /**
    * Releases important references.
    * 
    * @since Date: Feb 15, 2012
    */
   public static void releaseAllResources() {
     OutputFile.getInstance().releaseResources();
   }
 
   /**
    * Returns the list of arguments that are available.
    * 
    * @since Date: Feb 28, 2012
    * @return the list of arguments that are available.
    */
   public static List<AArgument> values() {
     return VALUES;
   }
 }
