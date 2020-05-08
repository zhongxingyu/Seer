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
 
 import com.google.common.base.Objects;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Describes a parsed command of the interactive shell.
  *
  * Commands consist always of a command and may have a subcommand.
  * This type is immutable.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class ShellCommand {
 
     /**
      * Map the literal command string to corresponding type enum.
      */
     private static final Map<String, MainType> COMMANDS = Maps.newHashMap();
 
     /**
      * Map the literal sub command string to corresponding type enum.
      */
     private static final Map<String, SubType> SUB_COMMANDS = Maps.newHashMap();
 
     static {
         for (final MainType t : MainType.values()) {
             COMMANDS.put(t.toString(), t);
         }
         for (final SubType t : SubType.values()) {
             if (t == SubType.NONE) {
                 continue; // Ignore to do not recognize empty strings in #isSubCommand().
             }
             SUB_COMMANDS.put(t.toString(), t);
         }
     }
 
     /**
      * Obligatory main command.
      */
     private final MainType command;
 
     /**
      * Optional sub command.
      *
      * If the main type does not support sub commands this will be {@link SubType#NONE}.
      */
     private final SubType subCommand;
 
     /**
      * Optional arguments.
      *
      * If the command has no arguments this will be an empty list.
      */
     private final List<Token> arguments;
 
     /**
      * Constructor for commands with no supported sub commands.
      *
      * @param command main command
      * @param arguments command arguments, may be an empty list
      */
     public ShellCommand(final MainType command, final List<Token> arguments) {
         this(command, SubType.NONE, arguments);
     }
 
     /**
      * Dedicated constructor.
      *
      * @param command main command
      * @param subCommand sub command
      * @param arguments command arguments, may be an empty list
      */
     public ShellCommand(final MainType command, final SubType subCommand, final List<Token> arguments) {
         super();
         this.command    = command;
         this.subCommand = subCommand;
         this.arguments  = Lists.newArrayList(arguments); // Defense copy
     }
 
     /**
      * Get command main type.
      *
      * @return main type of command
      */
     public MainType getCommand() {
         return command;
     }
 
     /**
      * Get optional command sub type.
      *
      * If the main type does not support any sub type {@link SubType#NONE} will be returned.
      *
      * @return sub type of command
      */
     public SubType getSubCommand() {
         return subCommand;
     }
 
     /**
      * Get arguments.
      *
      * This method will always return a list. If no arguments are present an empty list will be returned.
      *
      * @return Returns a defense copy.
      */
     public List<Token> getArguments() {
         return Lists.newArrayList(arguments); // Defense copy
     }
 
     /**
      * Determines if the string literal value of the token is a main command.
      *
      * TODO Maybe remove this.
      *
      * @param t token to check
      * @return true if the token is a command else false
      */
     static boolean isCommand(final Token<String> t) {
         return COMMANDS.containsKey(t.getValue());
     }
 
     /**
      * Determines the appropriate main command type for given string token.
      *
     * You should check with {@link #isCommand(de.weltraumschaf.neuron.shell.Token)} before invocation.
      * Determining a non command token string will result in an exception.
      *
      * @param t token to check
      * @return command main type
      * // CHECKSTYLE:OFF
      * @throws IllegalArgumentException if, token is not a main command
      * // CHECKSTYLE:ON
      */
     static MainType determineCommand(final Token<String> t) {
         if (! isCommand(t)) {
             throw new IllegalArgumentException(String.format("'%s' is not a command!", t.getValue()));
         }
         return COMMANDS.get(t.getValue());
     }
 
     /**
      * Determines if the string literal value of the token is a sub command.
      *
      * TODO Maybe remove this.
      *
      * @param t token to check
      * @return true if the token is a sub command else false
      */
     static boolean isSubCommand(final Token<String> t) {
         return SUB_COMMANDS.containsKey(t.getValue());
     }
 
     /**
      * Determines the appropriate sub command type for given string token.
      *
     * You should check with {@link #isSubCommand(de.weltraumschaf.neuron.shell.Token)} before invocation.
      * Determining a non sub command token string will result in an exception.
      *
      * @param t token to check
      * @return command sub type
      * // CHECKSTYLE:OFF
      * @throws IllegalArgumentException if, token is not a sub command
      * // CHECKSTYLE:ON
      */
     static SubType determineSubCommand(final Token<String> t) {
         if (! isSubCommand(t)) {
             throw new IllegalArgumentException(String.format("'%s' is not a sub command!", t.getValue()));
         }
         return SUB_COMMANDS.get(t.getValue());
     }
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this)
                       .add("mainCommand", command)
                       .add("subCommand", subCommand)
                       .add("arguments", arguments)
                       .toString();
     }
 
     /**
      * Marker interface for shell command types.
      */
     public interface Type {
 
     }
 
     /**
      * Enumerates the available commands.
      */
     public enum MainType implements Type {
 
         /** Help command. */
         HELP("help"),
         /** Reset command. */
         RESET("reset"),
         /** Exit command. */
         EXIT("exit"),
         /** Node command. */
         NODE("node"),
         /** Message command. */
         MESSAGE("message"),
         /** Dump command. */
         DUMP("dump"),
         /** Sample command. */
         SAMPLE("sample");
 
         /**
          * Literal command string used in shell.
          */
         private final String name;
 
         /**
          * Initialize name.
          *
          * @param name literal shell command string
          */
         private MainType(final String name) {
             this.name = name;
         }
 
         @Override
         public String toString() {
             return name;
         }
 
     }
 
     /**
      * Enumerates the optional subcommands.
      */
     public enum SubType implements Type {
         /** No sub command. */
         NONE(""),
         /** Add sub command for node command. */
         ADD("add"),
         /** Del sub command for node command. */
         DEL("del"),
         /** Connect sub command for node command. */
         CONNECT("connect"),
         /** Disconnect sub command for node command. */
         DISCONNECT("disconnect"),
         /** List sub command for node command. */
         LIST("list"),
         /** Info sub command for node command. */
         INFO("info"),
         /** Listen sub command for node command. */
         LISTEN("listen"),
         /** Unlisten sub command for node command. */
         UNLISTEN("unlisten"),
         /** Dot subcommand for dump command. */
         DOT("dot"),
         /** Tree subcommand for dump command. */
         TREE("tree"),
         /** Bidirectional tree subcommand for dump command. */
         BITREE("bitree");
 
         /**
          * Literal command string used in shell.
          */
         private final String name;
 
         /**
          * Initialize name.
          *
          * @param name literal shell command string
          */
         private SubType(final String name) {
             this.name = name;
         }
 
         @Override
         public String toString() {
             return name;
         }
 
     }
 
 }
