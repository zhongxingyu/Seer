 package com.pjbollinger.guilds;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Guilds extends JavaPlugin {
 
     @Override
     public void onEnable(){
         getLogger().info("Guilds has started.");
     }
 
     @Override
     public void onDisable(){
         getLogger().info("Guilds has stopped.");
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
         if(cmd.getName().equalsIgnoreCase("Judy")){
             getLogger().info("Judy says, 'Hi!'");
             return true;
         }
         else if(cmd.getName().equalsIgnoreCase("g")){
             if(args[0].equalsIgnoreCase("create <name of Guild>")){
                 //Stuff for creating a faction will go here
             }
             else if(args[0].equalsIgnoreCase("invite")){
                 //Stuff for inviting a player will go here
                 //Need to specify <name>
             }
             else if(args[0].equalsIgnoreCase("invitations")){
                 //Stuff for viewing which Guild(s) have invited a player to join will go here
             }
             else if(args[0].equalsIgnoreCase("join")){
                 //Stuff for if invited OR if desire to join an OPEN Guild
                 //Need to specify <name of Guild>
             }
             else if(args[0].equalsIgnoreCase("leave")){
                 //Stuff for leaving the Guild
             }
             else if(args[0].equalsIgnoreCase("kick")){
                 //Stuff for getting rid of a troublemaker
                 //Need to specify <name>
             }
             else if(args[0].equalsIgnoreCase("moderator")){
                 //Stuff for designating a Guild member as moderator of the Guild
                 //Need to specify <name>
             }
             else if(args[0].equalsIgnoreCase("member")){
                 //Stuff for removing Guild mod status, returning to member only status
                 //Need to specify <name>
             }
             else if(args[0].equalsIgnoreCase("leader")){
                 //Stuff for reassigning role of Guild leader
                 // MUST be approved by SO or SAdmin before going into effect
                 //Need to specify <name>
             }
             else if(args[0].equalsIgnoreCase("list")){
                 //Stuff for creating/showing list of Guilds
                 //page # is optional
             }
             else if(args[0].equalsIgnoreCase("show")){
                 //Stuff for showing info about a Guild
             }
             else if(args[0].equalsIgnoreCase("player")){
                 //Stuff for showing info about a single person
             }
             else if(args[0].equalsIgnoreCase("home")){
                 //Stuff for going to home set point
             }
             else if(args[0].equalsIgnoreCase("set")){
                 //Stuff for setting the value, property, etc.
                 //examples: property = home, value = open (is Guild open or closed)
                 //examples: name = change name of Guild, description = motto for Guild
                 //Need to specify <name, property> and [value(s), description]
             }
             else if(args[0].equalsIgnoreCase("ally")){
                 //Stuff for inviting another Guild to be allied with yours
                 //Need to specify <name>
             }
            else if(args[0].eqaulsIgnoreCase("neutral")){
                 //Stuff for returning to neutral with a Guild
                 //Need to specify <name>
             }
 
         }
         return false;
     }
 }
