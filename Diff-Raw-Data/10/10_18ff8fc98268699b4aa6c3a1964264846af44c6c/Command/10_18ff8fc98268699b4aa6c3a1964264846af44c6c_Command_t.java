 package darep;
 
 import java.util.Arrays;
 import java.util.Map;
 
 /**
  * This class contains all options, flags and parameters given
  * by a Commandline call.
  * 
  * It is created by the parser and passed to the actual action that
  * will be performed.
  * 
  * Nomenclature:
  * 
  * - action:      the name of the command
  * - option:      options passed with a "-" that can have a value
  * - flag:        like option but doesn't have a value
  * - param(eter): arguments that don't start with a "-"
  * 
  * Example:
  * 
  *     add -n uberfile -m path/to/a/file
  *     
  *     add is the action
  *     "-n uberfile" is an option
  *     "-m" is a flag
  *     "path/to/a/file" is a parameter
  * 
  * 
  *
  * @author kevin
  */
 public class Command {
 
 	public enum ActionType {
 		add,
 		replace,
 		delete,
 		export,
 		list,
 		help
 	}
 
 	private ActionType action;
 
 	private Map<String, String> options;
 
 	private String[] flags;
 
 	private String[] parameters;
 
 	public Command(ActionType action, String[] arguments, 
 			Map<String, String> options, String[] flags) {
 
 		this.action = action;
 		this.options = options;
 		this.flags = flags;
 		this.parameters = arguments;
 	}
 
 	public ActionType getAction() {
 		return action;
 	}
 
 	/**
 	 * Returns a {@link String} {@link Array} with the given parameters.
 	 * @return
 	 */
 	public String[] getParams() {
 		return parameters;
 	}
 
 	/**
 	 * Returns true, if a flag is given and false otherwise.
 	 * @param name
 	 * @return
 	 */
 	public boolean hasFlag(String name) {
 		return Helper.arrayContains(name, flags);
 	}
 	
 	/**
 	 * Returns true, if a flag or option with the name "key" is given,
 	 * false otherwise.
 	 * @param key
 	 * @return
 	 */
 	public boolean isSet(String key) {
 		return (hasFlag(key)
 				|| options.containsKey(key));
 	}
 
 	/**
 	 * Returns the value (param) of an option or null if not set.
 	 * @param key
 	 * @return
 	 */
 	public String getOptionParam(String key) {
 		return options.get(key);
 	}
 	
	public void setOptionParam(String key, String value) {
		options.put(key, value);
	}
	
 	/**
 	 * Returns the options, null if not set.
 	 * @return
 	 */
 	public Map<String, String> getOptions() {
 		return this.options;
 	}
 	
 	/**
 	 * Returns the flags, null if not set.
 	 * @return
 	 */
 	public String[] getFlags() {
 		return this.flags;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("Action:\t\t");
 		sb.append(action.toString());
 
 		sb.append("\n");
 		
 		sb.append("Arguments:\t");
 		sb.append(Arrays.toString(parameters));
 		
 		sb.append("\n");
 
 		sb.append("flags:\t\t");
 		sb.append(Arrays.toString(flags));
 
 		sb.append("\n");
 		
 		sb.append("options:\t");
 		sb.append(options.toString());
 
 		return sb.toString();
 	}
 	@Override
 	public boolean equals(Object obj) {
 		if((obj instanceof Command) == false)
 			return false;
 		
 		Command other = (Command)obj;
 
 		if(this.action != other.action)
 			return false;
 		if(Helper.arrayIsPermutation(this.flags,other.flags) == false)
 			return false;
 		if(Helper.arrayIsPermutation(this.parameters, other.parameters) == false)
 			return false;
 		if(this.options.equals(other.options) == false) 
 			return false;
 		
 		return true;
 	}
 
 	
 }
