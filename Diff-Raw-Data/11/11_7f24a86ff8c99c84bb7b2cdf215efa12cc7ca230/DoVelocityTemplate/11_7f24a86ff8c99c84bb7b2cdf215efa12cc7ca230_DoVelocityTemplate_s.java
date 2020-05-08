 package com.technosophos.rhizome.command.template;
 
 import java.util.List;
 import java.util.Map;
 import java.io.StringWriter;
 import com.technosophos.rhizome.command.AbstractCommand;
 import com.technosophos.rhizome.controller.CommandConfiguration;
 import com.technosophos.rhizome.controller.CommandInitializationException;
 import com.technosophos.rhizome.controller.CommandResult;
 import com.technosophos.rhizome.repository.RepositoryManager;
 
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 
 /**
 * Abstract class that performs velocity template rendering.
  * <p>This class handles velocity template manipulation. It will format the data contained
  * in all of the previous {@link CommandResult} items in the {@link List}, and it
  * will then remove those items from the list. (To not do so would be to give the 
  * controller the imperative to handle presenting them itself.)</p>
  * <p>A velocity command is assumed to be the <i>LAST FORMATTING COMMAND</i> in a chain of
  * commands. Since it formats ALL of the data in the {@link List} of command results,
  * it may remove all of the previous results from the list, leaving only its own 
  * CommandResult object in the list.</p>
  * <p>Commands that appear after this one in a Request Queue should perform their own 
  * formatting.</p>
  * @author mbutcher
  * @see #doCommand(Map, List)
  */
 public class DoVelocityTemplate extends AbstractCommand {
 
 	/** 
 	 * The string "template_name". 
 	 * This parameter should be present in the command configuration (e.g. in the 
 	 * command.xml file), and should indicate which velocity template will be used with
 	 * this command.  
 	 * @see CommandConfiguration
 	 * 
 	 */
 	public static final String TEMPLATE_NAME_CONF = "template_name";
 	
 	/** 
 	 * The string "template_path".
 	 * The template path configuration param (in commands.xml or the equiv.) should
 	 * point to the directory where the velocity templates are stored.
 	 * @see CommandConfiguration
 	 */
 	public static final String TEMPLATE_PATH_CONF = "template_path";
 	
 	/**
 	 * The string "template_macro".
 	 * Velocity allows you to load template macros during initialization. To perform
 	 * macro loading, include a list of macros in the template_macro directive of the 
 	 * {@see CommandConfiguration}.
 	 * @see CommandConfiguration
 	 * @see VelocityEngine.VM_MACRO
 	 */
 	public static final String TEMPLATE_MACRO_CONF = "template_macro";
 	
 	protected VelocityEngine velen = null;
 	protected String template_name = null;
 	protected Map<String, Object> params = null;
 	protected List<CommandResult> results = null;
 	
 	public void init(CommandConfiguration comConf, RepositoryManager rm)
 			throws CommandInitializationException {
 		super.init(comConf, rm);
 		try {
 			this.initVelocityEngine();
 		} catch (Exception e) {
 			throw new CommandInitializationException("Failed to initialize Velocity.", e);
 		}
 	}
 	
 	/**
 	 * Format the results of the list of command results.
 	 * <p>This uses the Velocity template engine to take a template and render the objects
 	 * in the List of {@link CommandResult}s into some unified textual format.</p>
 	 * <p>When this method completes, there will be only one CommandResult in the List:
 	 * the one containing the results of this method. All others will be deleted.</p>
 	 * <h3>What is in the context?</h3>
 	 * <p>Velocity templates have access to a Velocity context. What is in this context?
 	 * Here is a list.</p>
 	 * <ul>
 	 *  <li>The params passed into doCommand().</li>
 	 *  <li>The configuration directives from the {@see CommandConfiguration}.</li>
 	 *  <li>An entry for every object in the list of {@see CommandResult}s.</li>
 	 * </ul>
 	 * <p>There are a few important things to note about this. First, if a parameter and
 	 * a configuration directive have identical names, the directive is the one stored in
 	 * the map.</p>
 	 * <p>Second, the CommandResults are keyed using the command prefix and the command
 	 * name, while the object is stored as the value. For example a command with prefix
 	 * "test-" and command "GetDocument" can be retrieved with get(test-GetDocument).</p>
 	 * <h3>Order of Execution</h3>
 	 * <p>You might want to override some or all of the protected methods here (for 
 	 * instance, 
 	 * if you want to use the Singleton velocity model). Here is the sequence of method
 	 * calls for this method:</p>
 	 * <ol>
 	 * <li>Store a local copy of the params and results list so that other methods can access them.</li>
 	 * <li>Create the VelocityContext with this.createContext()</li>
 	 * <li>Render the contents of the template with this.processTemplate(). Note that it is up 
 	 * to the processTemplate() method to retrieve the template.</li>
 	 * <li>Create the command result, clear teh command list, and then add the command result
 	 * to the list.</li>
 	 * </ol>
 	 * <p>Note that in DoVelocityTemplate, all templates are loaded from the file system,
 	 * and the path used is specified by the values of the {@link #TEMPLATE_PATH_CONF}
 	 * in the {@link CommandConfiguration}.</p> 
 	 */
 	public void doCommand(Map<String, Object> params,
 			List<CommandResult> results) {
 		
 		this.params = params;
 		this.results = results;
 		
 		VelocityContext cxt = this.createContext();
 		
 		CommandResult cr = null;
 		try {
 			String tout = this.processTemplate(cxt);
 		
 			cr = this.createCommandResult(tout);
 		} catch (Exception e) {
 			String err = "Failed to get Velocity template: " + e.getMessage();
 			String fri = "The serve could not properly format your information.";
 			cr = this.createErrorCommandResult(err, fri, e);
 		} finally {
 			results.clear();
 			results.add(cr);
 		}
 	}
 	
 	/**
 	 * Create a VelocityContext.
 	 * <p>This method prepares a VelocityContext with all of the information appropriate for
 	 * the velocity templates, including all parameters passed to {@link #doCommand(Map, List)}
 	 * and all of the objects in the list from doCommand.</p>
 	 * <p>The parameters are put in the top-level of the Context.</p>
 	 * <p>The {@link CommandResult}s are stored with the key name retrieved from 
 	 * {@link CommandResult.getName()}.</p>
 	 * <h3>What is in the context?</h3>
 	 * <ul>
 	 *  <li>The params passed into doCommand().</li>
 	 *  <li>The configuration directives from the {@see CommandConfiguration}.</li>
 	 *  <li>An entry for every object in the list of {@see CommandResult}s.</li>
 	 * </ul>
 	 * <p>There are a few important things to note about this. First, if a parameter and
 	 * a configuration directive have identical names, the directive is the one stored in
 	 * the map.</p>
 	 * <p>Second, the CommandResults are keyed using the command prefix and the command
 	 * name, while the object is stored as the value. For example a command with prefix
 	 * "test-" and command "GetDocument" can be retrieved with get(test-GetDocument).</p>
 	 * @return An initialized VelocityContext.
 	 */
 	protected VelocityContext createContext() {
 		VelocityContext basic = new VelocityContext(this.params);
 		VelocityContext c = new VelocityContext(this.comConf.getDirectives(), basic);
 		Object o;
 		for(CommandResult cr: this.results) {
 			
 			if(cr.hasError()) {
 				c.put(cr.getName(), cr);
 			} else {
 				o = cr.getResult();
 				/*
 				//Chain, effectively flattening the map.
 				if(o instanceof Map) {
 					c = new VelocityContext((Map)o, c);
 				// Put it in the map.
 				} else {
 					c.put(cr.getCommandName(), o);
 				}
 				*/
 				c.put(cr.getName(), o);
 			}
 		}
 		return c;
 	}
 	
 	/**
 	 * Apply a template.
 	 * <p>This processes the velocity template, using the given context. It returns the results
 	 * in the form of a string.</p>
 	 * <p>This version uses the {@link VelocityEngine.mergeTemplate(String, VelocityContext, Writer} 
 	 * method to process the template. THis method is dependent on reading templates from
 	 * the file system. Override this if you want to read templates from another location,
 	 * such as the Rhizome Repository.</p>
 	 * @param c An initialized VelocityContext.
 	 * @return The results of template processing.
 	 * @throws Exception Any exception that Velocity throws is thrown here.
 	 */
 	protected String processTemplate(VelocityContext c) throws Exception {
 		StringWriter w = new StringWriter();
 		this.velen.mergeTemplate(this.template_name, c, w);
 		return w.toString();
 	}
 	
 	/**
 	 * Initialize Velocity Engine.
 	 * <p>This method initializes the velocity engine as a standard class (not a 
 	 * singleton). It is called during the initialization of this command class.</p>
 	 * <p>It is assumed that this initialization will be able to make use of the local
 	 * instances of the {@link CommandConfiguration} and the {@RepositoryManager}.</p>
 	 *
 	 */
 	protected void initVelocityEngine() throws Exception, CommandInitializationException {
 		
 		// Make sure that both configuration parameters are set.
 		if(!this.comConf.hasDirective(TEMPLATE_NAME_CONF)) {
 			String err = String.format(
 					"Configuration parameters %s must be set in the command configuration.",
 					TEMPLATE_NAME_CONF);
 			throw new CommandInitializationException(err);
 		}
 		this.template_name = this.comConf.getDirective(TEMPLATE_NAME_CONF)[0];
 		
 		this.velen = new VelocityEngine();
 		this.velen.init();
 		
 		if(this.comConf.hasDirective(TEMPLATE_PATH_CONF)) {
 			String [] pp = this.comConf.getDirective(TEMPLATE_PATH_CONF);
 			if( pp.length == 1) {
 				this.velen.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, pp[0]);	
 			} else if(pp.length > 1) {
 				StringBuffer sb = new StringBuffer();
 				for(String str: pp) {
 					sb.append(str);
 					sb.append(',');
 				}
 				this.velen.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, 
 						sb.toString());
 			}
 		} // else... who knows where the templates come from?
 		
 		if( this.comConf.hasDirective(TEMPLATE_MACRO_CONF)) {
 			String [] mac_names = this.comConf.getDirective(TEMPLATE_MACRO_CONF);
 			StringBuffer sb = new StringBuffer();
 			for(String name: mac_names) {
 				sb.append(name);
 				sb.append(',');	
 			}
 			//FIXME: Should this be VM_LIBRARY or VM_LIBRARY_DEFAULT?
 			this.velen.setProperty(VelocityEngine.VM_LIBRARY, sb.toString());
 		}
 	}
 
 }
