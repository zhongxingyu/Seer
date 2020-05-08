 /*
 * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
 * "Sven Strittmatter" <weltraumschaf(at)googlemail(dot)com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
 * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf(at)googlemail(dot)com>
  */
 package de.weltraumschaf.commons.shell;
 
 import com.google.common.collect.Maps;
 import java.util.Map;
 
 /**
  * Maps the literal string of an command to its enum type.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public abstract class LiteralCommandMap {
 
     /**
      * Map the literal command string to corresponding type enum.
      */
     private final Map<String, MainCommandType> commands = Maps.newHashMap();
 
     /**
      * Map the literal sub command string to corresponding type enum.
      */
     private final Map<String, SubCommandType> subCommands = Maps.newHashMap();
 
     /**
      * Calls the template methods {@link #initCommandMap(java.util.Map)}
      * and {@link #initSubCommandMap(java.util.Map)}.
      */
     public LiteralCommandMap() {
         super();
         initCommandMap(commands);
         initSubCommandMap(subCommands);
     }
 
     /**
      * Determines if the string literal value of the token is a main command.
      *
      * TODO Maybe remove this.
      *
      * @param t token to check
      * @return true if the token is a command else false
      */
     public final boolean isCommand(final Token<String> t) {
         return commands.containsKey(t.getValue());
     }
 
     /**
      * Determines the appropriate main command type for given string token.
      *
      * You should check with {@link #isCommand(de.weltraumschaf.commons.shell.Token)} before invocation.
      * Determining a non command token string will result in an exception.
      *
      * @param t token to check
      * @return command main type
      * // CHECKSTYLE:OFF
      * @throws IllegalArgumentException if, token is not a main command
      * // CHECKSTYLE:ON
      */
     public final MainCommandType determineCommand(final Token<String> t) {
         if (! isCommand(t)) {
             throw new IllegalArgumentException(String.format("'%s' is not a command!", t.getValue()));
         }
         return commands.get(t.getValue());
     }
 
     /**
      * Determines if the string literal value of the token is a sub command.
      *
      * TODO Maybe remove this.
      *
      * @param t token to check
      * @return true if the token is a sub command else false
      */
     public final boolean isSubCommand(final Token<String> t) {
         return subCommands.containsKey(t.getValue());
     }
 
     /**
      * Determines the appropriate sub command type for given string token.
      *
      * You should check with {@link #isSubCommand(de.weltraumschaf.commons.shell.Token)} before invocation.
      * Determining a non sub command token string will result in an exception.
      *
      * @param t token to check
      * @return command sub type
      * // CHECKSTYLE:OFF
      * @throws IllegalArgumentException if, token is not a sub command
      * // CHECKSTYLE:ON
      */
     public final SubCommandType determineSubCommand(final Token<String> t) {
         if (! isSubCommand(t)) {
             throw new IllegalArgumentException(String.format("'%s' is not a sub command!", t.getValue()));
         }
 
         return subCommands.get(t.getValue());
     }
 
     /**
      * Initializes the command map.
      *
      * @param map map to initialize
      */
     protected abstract void initCommandMap(final Map<String, MainCommandType> map);
 
     /**
      * Initializes the sub command map.
      *
      * @param map map to initialize
      */
     protected abstract void initSubCommandMap(final Map<String, SubCommandType> map);
 
 }
