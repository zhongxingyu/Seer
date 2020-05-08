 package fr.noogotte.useful_commands.exception;
 
 import fr.aumgn.bukkitutils.command.exception.CommandError;
 
 public class PlayerNotInServer extends CommandError {
 
     private static final long serialVersionUID = -3139364881212075182L;
 
     public PlayerNotInServer() {
        super("Ce joueur n'est pas sur le serveur.");
     }
 
 }
