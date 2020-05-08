 
 public enum CommandWord {
	PLAY("play"), QUIT("quit"), UNKNOWN("unknown");
 	
 
     // Le String de commande.
     private String commandString;
 
     CommandWord(String commandString) {
         this.commandString = commandString;
     }
 
     public String toString() {
         return commandString;
     }
 }
