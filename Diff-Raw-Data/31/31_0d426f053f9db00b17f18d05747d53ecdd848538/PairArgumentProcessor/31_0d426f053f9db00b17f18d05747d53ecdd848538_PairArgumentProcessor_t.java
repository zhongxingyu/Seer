 package uk.co.jacekk.bukkit.baseplugin.command.args;
 
 import java.util.LinkedHashMap;
 import java.util.Set;
 import java.util.Map.Entry;
 
 /**
  * Processes command arguments into key-value pairs.
  * 
  * The command
  * <pre>/example a letter b number c something</pre>
  * is an example of a situation where this could be useful.
  * 
  * The keys are not case sensitive, but the values are. Any
  * arguments that do not match the format are discarded.
  * 
  * @author Jacek Kuzemczak
  */
 public class PairArgumentProcessor extends ArgumentProcessor {
 	
 	private LinkedHashMap<String, String> values;
 	
 	/**
 	 * @param args	The command arguments.
 	 */
 	public PairArgumentProcessor(String[] args){
		this.args = args;
 		this.values = new LinkedHashMap<String, String>();
		
		this.process();
 	}
 	
 	public void process(){
 		int k = 0;
 		int v = 1;
 		
 		while (k < this.args.length && v < this.args.length){
 			this.values.put(this.args[k].toLowerCase(), this.args[v]);
 			
 			k += 2;
 			v += 2;
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
 	
 }
