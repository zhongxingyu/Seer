 package uk.co.jacekk.bukkit.baseplugin.command.args;
 
 /**
  * Used to process command arguments into an easy to work with format.
  * 
  * @author Jacek Kuzemczak
  */
 public abstract class ArgumentProcessor {
 	
 	public String[] args;
 	
 	/**
 	 * Process the raw array of aruments, this is called automatically.
 	 */
 	public abstract void process();
 	
 }
