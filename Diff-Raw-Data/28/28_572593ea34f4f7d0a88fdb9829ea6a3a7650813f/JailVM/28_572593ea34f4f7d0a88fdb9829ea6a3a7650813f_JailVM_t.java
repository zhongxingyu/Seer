 package be.uclouvain.jail.vm;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Map;
 import java.util.Stack;
 
 import net.chefbe.autogram.ast2.IASTNode;
 import net.chefbe.autogram.ast2.ILocation;
 import net.chefbe.autogram.ast2.parsing.ParseException;
 import net.chefbe.autogram.ast2.parsing.active.ASTLoader;
 import net.chefbe.autogram.ast2.parsing.active.ActiveParser;
 import net.chefbe.autogram.ast2.parsing.active.ASTLoader.EnumTypeResolver;
 import net.chefbe.autogram.ast2.parsing.peg.Input;
 import net.chefbe.autogram.ast2.parsing.peg.Pos;
 import net.chefbe.autogram.ast2.utils.BaseLocation;
 import net.chefbe.javautils.adapt.AdaptUtils;
 import net.chefbe.javautils.adapt.IAdapter;
 import net.chefbe.javautils.collections.map.ListOrderedMap;
 import net.chefbe.javautils.robust.exceptions.CoreException;
import be.uclouvain.isis.jail.IsisToolkit;
 import be.uclouvain.jail.algo.graph.copy.match.GMatchNodes;
 import be.uclouvain.jail.dialect.IGraphDialect;
 import be.uclouvain.jail.uinfo.IUserInfo;
 import be.uclouvain.jail.vm.IJailVMEnvironment.LEVEL;
 import be.uclouvain.jail.vm.JailVMException.ERROR_TYPE;
 import be.uclouvain.jail.vm.autogram.JailNodes;
 import be.uclouvain.jail.vm.autogram.JailParser;
 import be.uclouvain.jail.vm.toolkits.AutomatonToolkit;
 import be.uclouvain.jail.vm.toolkits.GraphToolkit;
 
 /**
  * Provides JAIL virtual machine, which interprets .jail files.
  * 
  * @author blambeau
  */
 public class JailVM implements IJailVMScope {
 
 	/** Core toolkit. */
 	private JailCoreToolkit core;
 	
 	/** VM memory. */
 	private IJailVMScope memory;
 	
 	/** Installed plugins. */
 	private Map<String,IJailVMToolkit> toolkits;
 	
 	/** External environnement. */
 	private IJailVMEnvironment env;
 	
 	/** Executed sources stack. */
 	private Stack<Object> sources;
 	
 	/** Creates a VM instance. */
 	public JailVM(IJailVMEnvironment env) {
 		this.env = env;
 		memory = new JailVMMapScope();
 		sources = new Stack<Object>();
 		toolkits = new ListOrderedMap<String,IJailVMToolkit>();
 		core = new JailCoreToolkit();
 		registerToolkit("jail",core);
 		registerToolkit("fa",new AutomatonToolkit());
 		registerToolkit("graph",new GraphToolkit());
		registerToolkit("isis",new IsisToolkit());
 	}
 	
 	/** Creates a virtual machine with default environment. */
 	public JailVM() {
 		this(new DefaultJailVMEnvironment());
 	}
 
 	/** Returns the core toolkit. */
 	public JailCoreToolkit getCoreToolkit() {
 		return core;
 	}
 
 	/** Returns the environment. */
 	public IJailVMEnvironment getEnvironment() {
 		return env;
 	}
 
 	/** Resolves a relative path, through currently executed source. */
 	public String resolvePath(String path) throws JailVMException {
 		if (sources.isEmpty()) {
 			throw new JailVMException(JailVMException.ERROR_TYPE.INTERNAL_ERROR,
 					null,"Unexpected empty execution stack.");
 		}
 		Object current = sources.peek();
 		if (current instanceof File) {
 			File f = (File) current;
 			return new File(f.getParentFile(),path).getAbsolutePath();
 		} else if (current instanceof URL) {
 			URL url = (URL) current;
 			try {
 				return url.toURI().resolve(path).toURL().getFile();
 			} catch (URISyntaxException e) {
 				throw new JailVMException(JailVMException.ERROR_TYPE.INTERNAL_ERROR,null,e);
 			} catch (MalformedURLException e) {
 				throw new JailVMException(JailVMException.ERROR_TYPE.INTERNAL_ERROR,null,e);
 			}
 		}
 		return null;
 	}
 	
 	/** Executes some commands taken on some source. */
 	public void execute(Object source) throws JailVMException {
 		try {
 			sources.push(source);
 			
 			// create location and pos
 			ILocation loc = new BaseLocation(source);
 			Pos pos = new Pos(Input.input(loc),0);
 			
 			// create parser and parse
 			JailParser parser = new JailParser();
 			((ActiveParser)parser.getParser("gm")).setActiveLoader(
 				new ASTLoader(new EnumTypeResolver<GMatchNodes>(GMatchNodes.class))
 			);
 			parser.setActiveLoader(
 				new ASTLoader(new EnumTypeResolver<JailNodes>(JailNodes.class))
 			);
 			IASTNode root = (IASTNode) parser.pUnit(pos);
 			
 			// execute on callback
 			try {
 				root.accept(new JailVMCallback(this));
 			} catch (CoreException e) {
 				handleError(e.getCause());
 			} catch (Exception e) {
 				handleError(e);
 			}
 			
 			sources.pop();
 		} catch (IOException ex) {
 			handleError(new JailVMException(ERROR_TYPE.INTERNAL_ERROR,null,"Unable to read input jail commands.",ex));
 		} catch (ParseException ex) {
 			handleError(new JailVMException(ERROR_TYPE.PARSE_ERROR,null,null,ex));
 		}
 	}
 	
 	/** Handles an error. */
 	private void handleError(Throwable t) {
 		if (t instanceof JailVMException) {
 			env.handleError((JailVMException)t);
 		} else if (t.getCause() instanceof JailVMException) {
 			env.handleError((JailVMException)t.getCause());
 		} else {
 			env.handleError(t);
 		}
 	}
 	
 	/** Registers some plugin. */
 	public void registerToolkit(String namespace, IJailVMToolkit toolkit) {
 		toolkits.put(namespace, toolkit);
 		toolkit.install(this);
 	}
 	
 	/** Registers a loader. */
 	public void registerDialectLoader(String extension, IGraphDialect loader) {
 		core.registerDialectLoader(extension, loader);
 	}
 	
 	/** Returns a plugin mapped to some namespace. */
 	public IJailVMToolkit getToolkit(String namespace) {
 		return toolkits.get(namespace);
 	}
 	
 	/** Affects some value to a variable. */
 	public void affect(String varName, Object value) {
 		if (varName == null || value == null) {
 			throw new IllegalArgumentException("varName as well as value are mandatory.");
 		}
 		memory.affect(varName, value);
 
 		// sets name if possible
 		IUserInfo info = (IUserInfo) AdaptUtils.adapt(value, IUserInfo.class);
 		if (info != null) {
 			info.setAttribute("JailVM.VarName", varName);
 		}
 	}
 
 	/** Checks if a variable is known. */
 	public boolean knows(String var) {
 		return memory.knows(var);
 	}
 
 	/** Returns the value of a variable. */
 	public Object valueOf(String var) {
 		return memory.valueOf(var);
 	}
 
 	/** Executes a system command. */
 	public void executeSystemCommand(String name, Object[] args) throws JailVMException {
 		if ("help".equals(name)) {
 			help(name,args);
 		} else {
 			throw JailVMException.unknownCommand(name);
 		}
 	}
 	
 	/** Helps a command. */
 	public void help(String command, Object[] args) throws JailVMException {
 		if (args.length<1) {
 			StringBuffer sb = new StringBuffer();
 			for (IJailVMToolkit toolkit: toolkits.values()) {
 				sb.append("\nCommands contributed by ")
 				  .append(toolkit.getClass().getSimpleName())
 				  .append(":\n");
 				for (IJailVMCommand c: toolkit) {
 					String signature = c.getSignature();
 					if (signature == null) {
 						signature = c.getName() + " [no signature available]";
 					}
 					sb.append("  + ").append(signature).append("\n");
 				}
 			}
 			env.printConsole(sb.toString(), LEVEL.USER);
 		} else {
 			for (Object arg: args) {
 				IJailVMCommand c = findCommand(arg.toString());
 				if (c == null) {
 					throw JailVMException.unknownCommand(arg.toString());
 				} else {
 					env.printConsole("\n" + c.getHelp() + "\n" + c.getSignature(), LEVEL.USER);
 				}
 			}
 		}
 	}
 	
 	/** Finds a command by name. */
 	private IJailVMCommand findCommand(String name) {
 		for (IJailVMToolkit toolkit: toolkits.values()) {
 			if (toolkit.hasCommand(name)) {
 				return toolkit.getCommand(name);
 			}
 		}
 		return null;
 	}
 	
 	/** Executes a command on a specific toolkit. */
 	public Object executeCommand(
 			IJailVMToolkit toolkit, String command, 
 			JailVMStack stack, JailVMOptions options) throws JailVMException {
 		
 		// create empty options if null
 		if (options == null) {
 			options = new JailVMOptions();
 		}
 		
 		// invoke command
 		if (!toolkit.hasCommand(command)) {
 			throw JailVMException.unknownCommand(command);
 		} else {
 			return toolkit.getCommand(command).execute(this, stack, options);
 		}		
 	}
 
 	/** Executes a named command. */
 	public Object executeCommand(
 			String namespace, String command, 
 			JailVMStack stack, JailVMOptions options) throws JailVMException {
 		
 		if (namespace != null) {
 			// find toolkit by namespace
 			IJailVMToolkit toolkit = getToolkit(namespace);
 			return executeCommand(toolkit,command,stack,options);
 		} else {
 			// find the correct toolkit
 			IJailVMToolkit found = null;
 			for (IJailVMToolkit toolkit: toolkits.values()) {
 				if (toolkit.hasCommand(command)) {
 					found = toolkit;
 				}
 			}
 			
 			// execute command on found toolkit
 			if (found == null) {
 				throw JailVMException.unknownCommand(command);
 			} else {
 				return executeCommand(found,command,stack,options);
 			}
 		}
 	}
 
 	/** Registers an adaptation. */
 	public void registerAdaptation(Class src, Class target, IAdapter adapter) {
 		AdaptUtils.register(src, target, adapter);
 	}
 
 	/** Creates a user command. */
 	public void addCommand(IJailVMToolkit toolkit, IJailVMCommand command) {
 		toolkit.addCommand(command);
 	}
 	
 	/** Adds a command to a namespace resolved toolkit. */
 	public void addCommand(String namespace, IJailVMCommand command) throws JailVMException {
 		IJailVMToolkit toolkit = getToolkit(namespace);
 		if (toolkit == null) {
 			throw new JailVMException(ERROR_TYPE.INTERNAL_ERROR,null,"Unknwon toolkit " + namespace);
 		}
 	}
 	
 	/** Adds a command in the user toolkit. */
 	public void addCommand(IJailVMCommand command) {
 		addCommand(core,command);
 	}
 
 }
