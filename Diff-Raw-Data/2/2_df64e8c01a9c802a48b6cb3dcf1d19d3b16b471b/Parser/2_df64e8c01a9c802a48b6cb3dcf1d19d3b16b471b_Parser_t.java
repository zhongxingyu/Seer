 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package de.weltraumschaf.neuron.shell;
 
 import com.google.common.collect.Lists;
 import de.weltraumschaf.neuron.shell.ShellCommand.MainType;
 import de.weltraumschaf.neuron.shell.ShellCommand.SubType;
 import java.util.List;
 
 /**
  * Parses input line from interactive shell.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 class Parser {
 
     /**
      * Tokenize the input line.
      */
     private final Scanner scanner;
 
     /**
      * Dedicated constructor.
      *
      * @param scanner to scann input line
      */
     public Parser(final Scanner scanner) {
         super();
         this.scanner = scanner;
     }
 
     /**
      * Parses given input line.
      *
      * @param input line to parse
      * @return recognized shell command
      * @throws SyntaxException if, the parsed line has syntax errors
      */
     ShellCommand parse(final String input) throws SyntaxException {
         final List<Token> tokens = scanner.scan(input);
         final Token commandtoken = tokens.get(0);
 
         if (TokenType.LITERAL != commandtoken.getType()) {
             throw new SyntaxException("Command expected as first word!");
         }
 
         if (! ShellCommand.isCommand(commandtoken)) {
            throw new SyntaxException(String.format("Unrecognized token '%s'!", commandtoken.getValue()));
         }
 
         final MainType command = ShellCommand.determineCommand(commandtoken);
         SubType subCommand = SubType.NONE;
         int argumentBegin = 1;
 
         if (tokens.size() > 1) {
             final Token secondToken = tokens.get(1);
 
             if (secondToken.getType() == TokenType.LITERAL && ShellCommand.isSubCommand(secondToken)) {
                 ++argumentBegin;
                 subCommand = ShellCommand.determineSubCommand(secondToken);
             }
         }
 
         List<Token> arguments;
 
         if (tokens.size() > argumentBegin) {
             arguments = tokens.subList(argumentBegin, tokens.size());
         } else {
             arguments = Lists.newArrayList();
         }
 
         final ShellCommand cmd = new ShellCommand(command, subCommand, arguments);
         verifyCommand(cmd);
         return cmd;
     }
 
     /**
      * Verifies parsed command of consistency.
      *
      * Consistency checks are:
      * - correct sub command type
      * - correct number of arguments
      *
      * @param cmd command to verify
      * @throws SyntaxException if, verification has failed
      */
     private void verifyCommand(final ShellCommand cmd) throws SyntaxException {
         switch (cmd.getCommand()) {
             case EXIT:
             case HELP:
             case RESET:
                 if (cmd.getSubCommand() != SubType.NONE) {
                     throw new SyntaxException(String.format("Command %s does not support subcommands!",
                                                             cmd.getCommand()));
                 }
                 if (! cmd.getArguments().isEmpty()) {
                     throw new SyntaxException(String.format("Command %s does not support arguments!",
                                                             cmd.getCommand()));
                 }
                 break;
             case NODE:
                 verifyNodeCommand(cmd);
                 break;
             default:
                 // Nothing to do here.
         }
     }
 
     /**
      * Verify commands of main command type {@link MainType#NODE}.
      *
      * Consistency checks are:
      * - correct number of arguments for sub command type
      *
      * @param cmd command to verify
      * @throws SyntaxException if, wrong number of arguments or unsupported subcommand was parsed
      */
     private void verifyNodeCommand(final ShellCommand cmd) throws SyntaxException {
         final int argumentCount = cmd.getArguments().size();
         switch (cmd.getSubCommand()) {
             case LIST:
                 if (argumentCount != 0) {
                     throw new SyntaxException(String.format("Command %s does support no arguments!", cmd.getCommand()));
                 }
                 break;
             case ADD:
                 if (argumentCount != 0 && argumentCount != 1) {
                     throw new SyntaxException(String.format("Command %s one or zero arguments!", cmd.getCommand()));
                 }
                 break;
             case DEL:
             case INFO:
                 if (argumentCount != 1) {
                     throw new SyntaxException(String.format("Command %s require one argument!", cmd.getCommand()));
                 }
                 break;
             case CONNECT:
                 if (argumentCount != 2) {
                     throw new SyntaxException(String.format("Command %s require two argument!", cmd.getCommand()));
                 }
                 break;
             default:
                 throw new SyntaxException(String.format("Command %s does not support subcommand %s!",
                                                         cmd.getCommand(),
                                                         cmd.getSubCommand()));
 
         }
 
     }
 
 }
