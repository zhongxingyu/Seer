 package com.github.wolfie.kuramud.client.commands.combat;
 
 import org.vaadin.console.Console;
 import org.vaadin.console.Console.Command;
 
 import com.github.wolfie.kuramud.Util;
 import com.github.wolfie.kuramud.server.PlayerCharacter;
 
 public class Attack implements Command {
 
   private static final long serialVersionUID = 4292250342764055479L;
 
   private final PlayerCharacter player;
 
   public Attack(final PlayerCharacter player) {
     this.player = player;
   }
 
   @Override
   public Object execute(final Console console, final String[] argv)
       throws Exception {
 
    if (argv.length > 1) {
       final String arguments = Util.argvToString(argv);
       player.attack(arguments);
     } else {
       player.output("Who do you want to attack?");
     }
 
     return null;
   }
 
   @Override
   public String getUsage(final Console console, final String[] argv) {
     return "Attack players or mobs in the same room";
   }
 
 }
