 package com.technosophos.rhizome.command;
 
 import java.util.List;
 import java.util.Map;
 
 import com.technosophos.rhizome.controller.CommandConfiguration;
 import com.technosophos.rhizome.controller.CommandInitializationException;
 import com.technosophos.rhizome.controller.CommandResult;
 import com.technosophos.rhizome.controller.RhizomeCommand;
 import com.technosophos.rhizome.controller.ReRouteRequest;
 import com.technosophos.rhizome.repository.RepositoryManager;
 
 /**
  * Basic command functionality.
  * This abstract class implements some basic functionality of the {@link RhizomeCommand}
  * interface, and also provides some basic utility methods that will expedite development
  * of commands.
  * @author mbutcher
  *
  */
 public abstract class AbstractCommand implements RhizomeCommand {
 
 	/**
 	 * The repository name parameter: "repository_name"
 	 */
 	public static final String PARAM_REPO_NAME = "repository_name";
 	
 	/**
 	 * The repository name directive: "repository_name"
 	 */
 	public static final String DIRECTIVE_REPO_NAME = "repository_name";
 	
 	/** the CommandConfiguration */
 	protected CommandConfiguration comConf = null;
 	/** the RepositoryManager instance */
 	protected RepositoryManager repoman = null;
 	
 	protected Map<String, Object> params = null;
 	protected List<CommandResult> results = null;
 	
 	//inherit javadoc
 	public void doCommand(Map<String, Object> params, List<CommandResult> results) throws ReRouteRequest {
 		this.params = params;
 		this.results = results;
 		
 		this.execute();
 	}
 	
 	/**
 	 * Execute the command.
 	 * This is called by {@link doCommand(Map<String, Object>, List<CommandResult>)}. All 
 	 * command processing should be done here.
 	 * @throws ReRouteRequest
 	 */
 	protected abstract void execute() throws ReRouteRequest;
 
 	/**
 	 * This simply stores the {@link CommandConfiguration} and {@link RepositoryManager} locally.
 	 * Override this if you need any special initialization done.
 	 * @param CommandConfiguration command configuration
 	 * @param RepositoryManager initialized repository manager
 	 */
 	public void init(CommandConfiguration comConf, RepositoryManager rm) 
 			throws CommandInitializationException {
 		assert comConf != null;
 		if(comConf == null) 
 			throw new CommandInitializationException("Command configuration cannot be null");
 		this.comConf = comConf;
 		this.repoman = rm;
 	}
 	
 	/**
 	 * Get the correctly-prefixed parameter name.
 	 * <p>You <i>ought</i> to use this method for getting a paramter name.</p>
 	 * <p>
 	 * If this command should use a prefix, this will return the prefixed parameter name.
 	 * A command may or may not need to use a prefixed name. This is determined primarily
 	 * by the {@link CommandConfiguration} object.
 	 * </p>
 	 * @param param
 	 * @return
 	 */
 	protected String getPrefixedParamName(String param) {
 		if(this.comConf.hasPrefix()) return this.comConf.getPrefix() + param;
 		return param;
 	}
 	
 	/**
 	 * Get a parameter. Automatically use the prefix if present.
 	 * @param params the parameters hash.
 	 * @param name
 	 * @return the object value in the map, or null if the value isn't found.
 	 * @deprecated
 	 */
 	protected Object getParam(Map<String, Object> params, String name) {
 		String pname = this.getPrefixedParamName(name);
 		if(!params.containsKey(pname)) return null;
 		return params.get(pname);
 	}
 	/**
 	 * Get a parameter by name. This automatically uses prefixing if necessary.
 	 * @param name Name of the parameter to get.
 	 * @param defaultValue Value to return if param is not found.
 	 * @return The parameter value, or default value if not found.
 	 */
 	protected Object getParam(String name, Object defaultValue) {
 		String pname = this.getPrefixedParamName(name);
 		if(!this.params.containsKey(pname)) return defaultValue;
 		Object o = params.get(pname);
 		return o == null ? defaultValue : o;
 	}
 	/**
 	 * This is equivalent to doing a params.get(name).
 	 * @param params
 	 * @param name
 	 * @return
 	 * @deprecated
 	 */
 	protected Object getParamNoPrefix(Map<String, Object> params, String name) {
 		return params.get(name);
 	}
 	
 	/**
 	 * Get a param by name, but skip prefix mapping.
 	 * @param name Name of param to get
 	 * @param defaultValue Value to return if param isn't found, or value is null.
 	 * @return An object: the value of the param, if found, or defaultValue if not found.
 	 */
 	protected Object getParamNoPrefix(String name, Object defaultValue) {
 		if( !this.params.containsKey(name))  return defaultValue ;
 		Object o = this.params.get(name);
 		return o == null ? defaultValue : o;
 	}
 	
 	/**
 	 * Get a parameter value.
 	 * Some params (notably those that come from servlets) store objects in arrays,
 	 * even though most of the time the array only contains one value. This method 
 	 * checks to see if the item is an array. If it is, then it returns the FIRST ITEM,
 	 * If it is not, then the entire object is returned. Null is returned if no value is
 	 * found, or if the array is empty.
 	 * @param params The parameters hash
 	 * @param name The name to hunt for
 	 * @return The object (if not an array), the first element of an array (if obj is array), or null.
 	 * @deprecated
 	 */
 	protected Object getFirstParam(Map<String, Object> params, String name) {
 		String pname = this.getPrefixedParamName(name);
 		if(!params.containsKey(pname)) return null;
 		
 		Object o = params.get(pname);
 		if( o instanceof Object[] ) {
 			Object[] oa = (String[])o;
 			if(oa.length == 0) return null;
 			else return oa[0];
 		}
 		return o;
 	}
 	
 	/**
 	 * Get a parameter value.
 	 * Some params (notably those that come from servlets) store objects in arrays,
 	 * even though most of the time the array only contains one value. This method 
 	 * checks to see if the item is an array. If it is, then it returns the FIRST ITEM,
 	 * If it is not, then the entire object is returned. defaultValue is returned if no value is
 	 * found, or if the array is empty.
 	 * @param name The name to hunt for
 	 * @param defaultValue Object to return if no such value is found in params
 	 * @return The object (if not an array), the first element of an array (if obj is array), or null.
 	 */
 	protected Object getFirstParam(String name, Object defaultValue) {
 		String pname = this.getPrefixedParamName(name);
 		if(!this.params.containsKey(pname)) return defaultValue;
 		
 		Object o = params.get(pname);
 		if( o instanceof Object[] ) {
 			Object[] oa = (String[])o;
 			if(oa.length == 0) return defaultValue;
 			else {
 				return oa[0] == null ? defaultValue : oa[0];
 			}
 		}
 		return o == null ? defaultValue : o;
 	}
 	
 	/**
 	 * @deprecated
 	 * @param params
 	 * @param name
 	 * @return
 	 */
 	protected boolean hasParam(Map<String, Object> params, String name) {
 		String pname = this.getPrefixedParamName(name);
 		return params.containsKey(pname);
 	}
 	
 	/**
 	 * Returns true if there is a parameter with the given name. 
 	 * Currently, this also checks the value, so if the key exists, but the value is null, 
 	 * this will return false.
 	 * @param name Name of param to look up
 	 * @return True if the param exists, though it may have a null value.
 	 */
 	protected boolean hasParam(String name) {
 		String pname = this.getPrefixedParamName(name);
		return this.params.containsKey(pname) && this.params.get(name) != null;
 	}
 	
 	/**
 	 * Get the repo name for this request. 
 	 * <p>First, it attempts to get the repo name
 	 * from params, and if that fails, it attempts to get one from the 
 	 * {@link CommandConfiguration} object from 
 	 * {@link #init(CommandConfiguration, RepositoryManager)}.</p>
 	 * <p>This may return null if no repo name can be found.</p>
 	 * <p>If you do not want the "failover" to the CommandConfiguration that this
 	 * method offers, you can just use the {@link #getParam(Map, String)} method
 	 * using the {@link #PARAM_REPO_NAME} constant.</p>
 	 * @param params Map of params.
 	 * @return Repository name.
 	 * @deprecated
 	 */
 	protected String getCurrentRepositoryName(Map<String, Object> params) {
 		String repoName = this.getParam(params, PARAM_REPO_NAME).toString();
 		
 		if(repoName == null || "".equals(repoName)) {
 			if( !this.comConf.hasDirective(DIRECTIVE_REPO_NAME)) return null;
 			
 			String [] repoNames = this.comConf.getDirective(DIRECTIVE_REPO_NAME);
 			if(repoNames == null 
 					|| repoNames.length == 0 
 					|| repoNames[0] == null 
 					|| "".equals(repoNames[0])) {
 				return null;
 			}
 			return repoNames[0];
 		}
 		return repoName;
 	}
 	
 	/**
 	 * Create and initialize a new {@link CommandResult}.
 	 * This is a convenience method for creating a new command result.
 	 * @return initialized CommandResult
 	 */
 	protected CommandResult createCommandResult() {
 		return new CommandResult(this.comConf);
 	}
 	
 	/**
 	 * Create and initialize a new {@link CommandResult}.
 	 * This is a convenience method for creating a new command result.
 	 * @param o The object that the command result wraps.
 	 * @return initialized CommandResult (ready to add to the results list)
 	 */
 	protected CommandResult createCommandResult(Object o) {
 		return new CommandResult(this.comConf, o);
 	}
 	
 	/**
 	 * Create a new error message {@link CommandResult}.
 	 * @param errMsg technical error message
 	 * @param friendlyErrMsg user-friendly error message
 	 * @return initialized CommandResult with error information
 	 */
 	protected CommandResult createErrorCommandResult(String errMsg, String friendlyErrMsg) {
 		CommandResult r = new CommandResult(this.comConf);
 		r.setError(errMsg, friendlyErrMsg);
 		return r;
 	}
 	
 	/**
 	 * Create a new error message {@link CommandResult}.
 	 * @param errMsg technical error message
 	 * @param friendlyErrMsg user-friendly error message
 	 * @param e Exception that the controller should examine or print
 	 * @return initialized CommandResult with error information
 	 */
 	protected CommandResult createErrorCommandResult(String errMsg, String friendlyErrMsg, Exception e) {
 		CommandResult r = new CommandResult(this.comConf);
 		r.setError(errMsg, friendlyErrMsg, e);
 		return r;
 	}
 
 }
