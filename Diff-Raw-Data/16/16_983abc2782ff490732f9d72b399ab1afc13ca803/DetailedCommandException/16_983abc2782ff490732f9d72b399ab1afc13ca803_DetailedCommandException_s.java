 /*
  * DetailedCommandException.java
  * Copyright (C) 2011,2012 Wannes De Smet
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.xenmaster.web;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.wgr.lang.I18N;
 import net.wgr.wcp.command.CommandException;
 
 import org.xenmaster.api.entity.NamedEntity;
 import org.xenmaster.controller.BadAPICallException;
 
 /**
  * 
  * @created Jan 19, 2012
  * @author double-u
  */
 public class DetailedCommandException extends CommandException {
 
     protected String title;
     protected String name;
     // Uppercase alfanumeric + underscores, must contain at least one underscore
     protected final static Pattern ERROR_NAME = Pattern.compile("[A-Z0-9_]+_[A-Z0-9_]+");
 
     public DetailedCommandException(String commandName, BadAPICallException ex) {
         super(ex.getErrorName(), commandName);
 
         this.name = ex.getErrorName();
         String betterName = getNestedErrorName(ex.getErrorDescription());
         if (betterName == null) {
             betterName = ex.getErrorName();
         }
        this.message = getMessage(betterName);
         this.title = getTitle(betterName);
         
         if (ex.getOrigin() instanceof NamedEntity) {
             NamedEntity ne = (NamedEntity) ex.getOrigin();
             this.title = String.format(this.title, ne.getName(), ne.getDescription());
             this.message = String.format(this.message, ne.getName(), ne.getDescription());
         }
     }
 
     protected final String getMessage(String errorName) {
         return (I18N.hasText(errorName + "_MESSAGE") ? I18N.getText(errorName + "_MESSAGE") : errorName);
     }
     
     protected final String getTitle(String errorName) {
         return (I18N.hasText(errorName) ? I18N.getText(errorName) : errorName);
     }
 
     /**
      * XAPI error messages are user nor machine friendly
      * @param name original name
      * @return deepest error name
      */
     protected final String getNestedErrorName(String name) {
         Matcher matcher = ERROR_NAME.matcher(name);
 
         String lastMatch = null;
         while (matcher.find()) {
             String match = matcher.group();
             if (match.length() > 2) {
                 lastMatch = match;
             }
         }
 
         return lastMatch;
     }
 }
