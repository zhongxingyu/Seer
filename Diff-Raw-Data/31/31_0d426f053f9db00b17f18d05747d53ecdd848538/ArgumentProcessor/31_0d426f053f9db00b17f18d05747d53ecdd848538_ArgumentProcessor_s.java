 package uk.co.jacekk.bukkit.baseplugin.command.args;
 
 /**
  * Used to process command arguments into an easy to work with format.
  * 
  * @author Jacek Kuzemczak
  */
 public abstract class ArgumentProcessor {
 	
 	public String[] args;
 	
 	/**
	 * @param args	The command arguments.
	 */
	public ArgumentProcessor(String[] args){
		this.args = args;
		
		this.process();
	}
	
	/**
 	 * Process the raw array of aruments, this is called automatically.
 	 */
 	public abstract void process();
 	
 }
