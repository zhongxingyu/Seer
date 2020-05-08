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
 package de.weltraumschaf.neuron.cmd;
 
 import de.weltraumschaf.commons.IO;
 import de.weltraumschaf.neuron.node.Node;
 import de.weltraumschaf.neuron.shell.Environment;
 import de.weltraumschaf.neuron.shell.Token;
 import java.util.List;
 
 /**
  * Executes `node unlisten ID`.
  *
  * @author "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 class NodeListen extends BaseCommand {
 
     /**
      * Dedicated constructor.
      *
      * @param env shell environment
      * @param io shell I/O
      * @param arguments command arguments
      */
     public NodeListen(final Environment env, final IO io, final List<Token> arguments) {
         super(env, io, arguments);
     }
 
     @Override
     public void execute() {
         final Token<Integer> t = getArguments().get(0);
         final int id = t.getValue();
 
         if (getEnv().hasNode(id)) {
             final Node n = getEnv().getNode(id);
             n.addObserver(getEnv().getHandler());
            getIo().println(String.format("Listening for events emmitted by node %d.", id));
         } else {
            getIo().println(String.format("There is no node with id %d!", id));
         }
     }
 
 }
