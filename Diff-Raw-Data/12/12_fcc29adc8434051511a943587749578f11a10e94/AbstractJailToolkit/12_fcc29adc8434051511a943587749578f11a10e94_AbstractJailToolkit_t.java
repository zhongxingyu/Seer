 package be.uclouvain.jail.vm;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 
 import net.chefbe.autogram.ast2.IASTNode;
 import net.chefbe.autogram.ast2.ILocation;
 import net.chefbe.autogram.ast2.parsing.ParseException;
 import net.chefbe.autogram.ast2.parsing.active.ASTLoader;
 import net.chefbe.autogram.ast2.parsing.active.ActiveParser;
 import net.chefbe.autogram.ast2.parsing.active.ASTLoader.EnumTypeResolver;
 import net.chefbe.autogram.ast2.parsing.peg.Input;
 import net.chefbe.autogram.ast2.parsing.peg.Pos;
 import net.chefbe.autogram.ast2.utils.BaseLocation;
 import net.chefbe.javautils.adapt.IAdapter;
 import be.uclouvain.jail.algo.graph.copy.match.GMatchNodes;
 import be.uclouvain.jail.vm.autogram.JailNodes;
 import be.uclouvain.jail.vm.autogram.JailParser;
 
 /**
  * Provides an implementation of IJailVMToolkit that allows user-defined operators 
  * to be attached at runtime.
  * 
  * <p>This class may be extended to implement your own toolkit. However, in many 
  * situations it will be simpler to extend {@link JailReflectionToolkit} instead.
  * However, if you extend this class directly, make sure that you always check 
  * super methods (hasCommand, executeCommand) in order to avoid bypassing user-defined 
  * commands (which have higher priority moreover).</p>
  * 
  * @author blambeau
  */
 public abstract class AbstractJailToolkit implements IJailVMToolkit, IAdapter {
 
 	/** Attached commands. */
 	private Map<String,IJailVMCommand> commands;
 	
 	/** Creates a toolkit instance. */
 	public AbstractJailToolkit() {
 		commands = new TreeMap<String,IJailVMCommand>(); 
 	}
 
 	/** Iterable implementation. */
 	public final Iterator<IJailVMCommand> iterator() {
 		return commands.values().iterator();
 	}
 
 	/** Installs the toolkiy on a virtual machine. */
 	public void install(JailVM vm) {
 	}
 	
 	/** Returns true if a command is recognized. */
 	public final boolean hasCommand(String command) {
 		return commands.containsKey(command);
 	}
 	
 	/** Adds a user command. */
 	public final void addCommand(IJailVMCommand command) {
 		commands.put(command.getName(), command);
 	}
 	
 	/** Returns the command mapped to a specific name. */
 	public final IJailVMCommand getCommand(String command) {
 		if (!hasCommand(command)) {
 			throw new IllegalStateException("hasCommand() should be checked first.");
 		}
 		return commands.get(command);
 	}
 
 	/** Ensures that the documentation has been loaded. */
 	protected void ensureDocumentation() {
 		Class c = getClass();
 		String name = c.getSimpleName();
		URL url = ClassLoader.getSystemClassLoader().getResource(name + ".jail");
 		if (url == null) {
			System.err.println("Warning: no documentation found for " + name + ".jail");
 		} else {
 			try {
 				// create parser and parse
 				JailParser parser = new JailParser();
 				((ActiveParser)parser.getParser("gm")).setActiveLoader(
 					new ASTLoader(new EnumTypeResolver<GMatchNodes>(GMatchNodes.class))
 				);
 				parser.setActiveLoader(
 					new ASTLoader(new EnumTypeResolver<JailNodes>(JailNodes.class))
 				);
 				
 				// create pos
 				ILocation loc = new BaseLocation(url);
 				Pos pos = new Pos(Input.input(loc),0);
 				
 				// parse
 				IASTNode node = (IASTNode) parser.pNativedoc(pos);
 				
 				// compile
 				node.accept(new JailVMDocumentationCallback(this));
 				
 			} catch (IOException ex) {
 				throw new IllegalStateException("Unable to load documentation of " + name,ex);
 			} catch (ParseException ex) {
 				throw new IllegalStateException("Documentation of " + name + " corrupted.",ex);
 			} catch (Exception ex) {
 				throw new IllegalStateException("Documentation of " + name + " corrupted.",ex);
 			}
 		}
 	}
 
 	/** Adaptation method. */
 	public Object adapt(Object who, Class type) {
 		return null;
 	}
 	
 }
