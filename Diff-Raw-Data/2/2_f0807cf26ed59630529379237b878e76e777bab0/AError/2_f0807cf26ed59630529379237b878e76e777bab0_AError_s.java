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
 
 import java.util.logging.Logger;
 
 import com.github.croesch.micro_debug.commons.Printer;
 import com.github.croesch.micro_debug.i18n.Text;
 
 /**
  * Represents an argument to represent an invalid argument.
  * 
  * @author croesch
  * @since Date: Feb 28, 2012
  */
 public abstract class AError extends AArgument {
 
   /**
    * Prints the given array of arguments as an error. Returns whether the application can continue.
    * 
    * @since Date: Jan 14, 2012
    * @param params the arguments to be printed as an error
    * @param errorText the {@link Text} that is used to print each argument
    * @param errorArgument the type of error that the arguments are
    * @return <code>true</code>, if the application can start,<br>
    *         <code>false</code> otherwise
    */
   final boolean printError(final String[] params, final Text errorText, final AArgument errorArgument) {
     if (params == null || params.length == 0) {
       // if there are no arguments then calling this method might be a mistake
       Logger.getLogger(getClass().getName()).warning("No parameters passed to execution of '" + errorArgument.name()
                                                              + "'");
       return true;
     }
 
     // flag, because we still might have a corrupt array
     boolean argumentFound = false;
     for (final String param : params) {
       if (param != null) {
         // we have the argument, so print it
         Printer.printErrorln(errorText.text(param));
         argumentFound = true;
       }
     }
     if (argumentFound) {
      Help.getInstance().execute();
     }
     return !argumentFound;
   }
 
   @Override
   public final boolean execute(final String ... params) {
     return printError(params, getErrorText(), this);
   }
 
   @Override
   protected final String name() {
     return "ERROR";
   }
 
   /**
    * Returns the {@link Text} to visualise the problem of the invalid argument.
    * 
    * @since Date: Feb 28, 2012
    * @return the {@link Text} that describes why the argument is invalid.
    */
   protected abstract Text getErrorText();
 }
