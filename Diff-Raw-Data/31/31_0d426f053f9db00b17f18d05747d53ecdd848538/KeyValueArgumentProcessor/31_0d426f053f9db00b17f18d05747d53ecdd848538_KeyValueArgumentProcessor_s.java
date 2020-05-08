 package uk.co.jacekk.bukkit.baseplugin.command.args;
 
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.Map.Entry;
 import java.util.Set;
 
 /**
  * Processes command arguments into key-value pairs.
  * 
  * The command
  * <pre>/example a:letter b:number c:something</pre>
  * is an example of a situation where this could be useful.
  * 
  * The keys are not case sensitive, but the values are.
  * 
  * @author Jacek Kuzemczak
  */
 public class KeyValueArgumentProcessor extends ArgumentProcessor {
 	
 	private String separator;
 	private LinkedHashMap<String, String> values;
 	private LinkedList<String> leftover;
 	
 	/**
 	 * @param args			The command arguments.
 	 * @param seperator		The string used to separate the keys and values.
 	 */
 	public KeyValueArgumentProcessor(String[] args, String separator){
		super(args);
		
 		this.separator = separator;
 		this.values = new LinkedHashMap<String, String>();
 		this.leftover = new LinkedList<String>();
 	}
 	
 	public void process(){
 		for (String argument : this.args){
 			String[] parts = argument.split(this.separator, 2);
 			
 			if (parts.length == 2){
 				this.values.put(parts[0].toLowerCase(), parts[1]);
 			}else{
 				this.leftover.add(argument);
 			}
 		}
 	}
 	
 	/**
 	 * Check to see if a value exists.
 	 * 
 	 * @param key	The key of the entry.
 	 * @return		True if the key was found, false if not.
 	 */
 	public boolean contains(String key){
 		return this.values.containsKey(key.toLowerCase());
 	}
 	
 	/**
 	 * Gets the value associated with a key.
 	 * 
 	 * @param key	The key.
 	 * @return		the value.
 	 */
 	public String get(String key){
 		return this.values.get(key.toLowerCase());
 	}
 	
 	/**
 	 * Gets all of the key-value argument values.
 	 * 
 	 * @return	The values.
 	 */
 	public Set<Entry<String, String>> getAll(){
 		return this.values.entrySet();
 	}
 	
 	/**
 	 * Gets all of the arguments that did not match the key-value format.
 	 * 
 	 * @return	The arguments.
 	 */
 	public LinkedList<String> getLeftOver(){
 		return this.leftover;
 	}
 	
 }
