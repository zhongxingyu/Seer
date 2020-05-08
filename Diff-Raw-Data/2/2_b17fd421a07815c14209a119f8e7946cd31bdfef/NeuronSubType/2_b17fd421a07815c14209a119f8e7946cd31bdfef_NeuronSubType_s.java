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
 package de.weltraumschaf.neuron.shell;
 
 import de.weltraumschaf.commons.shell.SubCommandType;
 
 /**
  * Enumerates the optional subcommands.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public enum NeuronSubType implements SubCommandType {
 
     /**
      * No sub command.
      */
     NONE(""),
     /**
      * Add sub command for node command.
      */
     ADD("add"),
     /**
      * Del sub command for node command.
      */
     DEL("del"),
     /**
      * Connect sub command for node command.
      */
     CONNECT("connect"),
     /**
      * Disconnect sub command for node command.
      */
     DISCONNECT("disconnect"),
     /**
      * List sub command for node command.
      */
     LIST("list"),
     /**
      * Info sub command for node command.
      */
     INFO("info"),
     /**
      * Listen sub command for node command.
      */
     LISTEN("listen"),
     /**
      * Unlisten sub command for node command.
      */
     UNLISTEN("unlisten"),
     /**
      * Dot subcommand for dump command.
      */
     DOT("dot"),
     /**
      * Tree subcommand for dump command.
      */
     TREE("tree"),
     /**
      * Bidirectional tree subcommand for dump command.
      */
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
     private NeuronSubType(final String name) {
         this.name = name;
     }
 
     @Override
     public String toString() {
         return name;
     }
 
 }
