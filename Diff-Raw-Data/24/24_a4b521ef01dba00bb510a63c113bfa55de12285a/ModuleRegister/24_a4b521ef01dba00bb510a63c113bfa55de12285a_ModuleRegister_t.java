 package org.cwi.waebric;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.cwi.waebric.parser.SyntaxException;
 import org.cwi.waebric.parser.WaebricParser;
 import org.cwi.waebric.parser.ast.AbstractSyntaxTree;
 import org.cwi.waebric.parser.ast.module.Import;
 import org.cwi.waebric.parser.ast.module.Module;
 import org.cwi.waebric.parser.ast.module.ModuleId;
 import org.cwi.waebric.parser.ast.module.Modules;
 import org.cwi.waebric.scanner.LexicalException;
 import org.cwi.waebric.scanner.WaebricScanner;
 
 /**
  * Module register allows users a clean interface to load the AST of modules based on
  * their identifier. Additionally, loaded modules will be cached to improve performance.
  * @author Jeroen van Schagen
  * @date 10-06-2009
  */
 public class ModuleRegister {
 	
 	/**
 	 * Instance
 	 */
 	private static ModuleRegister instance;
 
 	/**
 	 * Cached modules
 	 */
 	private final Map<ModuleId, Modules> cache;
 	
 	/**
 	 * Construct cache.
 	 */
 	private ModuleRegister() {
 		cache = new HashMap<ModuleId, Modules>();
 	}
 	
 	/**
 	 * Attempt to load a module from the file system based on an identifier.
 	 * @param identifier Module identifier, maps on relative file position.
 	 * @throws IOException 
 	 */
 	public Modules requestModules(ModuleId identifier) throws IOException, ModuleLoadException {
 		if(hasCached(identifier)) { return cache.get(identifier); } // Already checked module.
 		if(identifier.size() == 0) { return null; } // Invalid identifier, quit cache for performance
 
 		// Attempt to process file
 		String path = getPath(identifier);
 		FileReader reader = new FileReader(path);
 		WaebricScanner scanner = new WaebricScanner(reader);
 		List<LexicalException> le = scanner.tokenizeStream(); // Scan module
 		WaebricParser parser = new WaebricParser(scanner.iterator());
 		List<SyntaxException> se = parser.parseTokens(); // Parse module
 		
 		// Requested module contains lexical, s
 		if(le.size() + se.size() > 0) {
 			throw new ModuleLoadException(path, le, se);
 		}
 		
 		// Retrieve modules
 		Modules data = parser.getAbstractSyntaxTree().getRoot();
 		if(data != null) { cacheModules(identifier, data); } // Cache dependent modules
 		return data;
 	}
 	
 	/**
 	 * Store a module and its AST in cache. By caching parsed modules they only
 	 * have to be parsed once, which increases efficiency. It does however
 	 * require a bit more memory to store all loaded modules, when the memory
 	 * consumption gets too high use the clearCache function.
 	 * @param identifier Module identifier
 	 * @param modules Abstract syntax tree
 	 */
 	public void cacheModules(ModuleId identifier, Modules modules) {
 		cache.put(identifier, modules);
 	}
 	
 	/**
 	 * Verify that a module is stored in the cache.
 	 * @param identifier Module identifier
 	 * @return
 	 */
 	public boolean hasCached(ModuleId identifier) {
 		return cache.containsKey(identifier);
 	}
 	
 	/**
 	 * Retrieve all transitive dependent modules.
 	 * @param modules Modules from which dependent modules are retrieved.
 	 * @return 
 	 * @throws  
 	 */
 	public Modules loadDependencies(Modules modules) {
		Modules result = new Modules(modules); // Store base content
 
 		for(Module module: modules) {
 			for(Import imprt: module.getImports()) {
 				if(! result.contains(imprt.getIdentifier())) {
 					try {
 						// Retrieve dependent modules
						Modules dependencies = requestModules(imprt.getIdentifier()); 
						
						// Recursively check for additional dependencies
						for(Module dependency: loadDependencies(dependencies)) {
							if(! result.contains(dependency.getIdentifier())) {
								result.add(dependency); // Store dependency
							}
						}
 					} catch (Exception e) {
 						// Skip invalid import directives
 					}
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Retrieve all transitive dependent modules.
 	 * @param module Module from which dependent modules are retrieved.
 	 * @return
 	 */
 	public Modules loadDependencies(Module module) {
 		Modules modules = new Modules();
 		modules.add(module);
 		return loadDependencies(modules);
 	}
 	
 	/**
 	 * Retrieve all transitive dependent modules.
 	 * @param ast Abstract syntax tree from which dependent modules are retrieved.
 	 * @return
 	 */
 	public Modules loadDependencies(AbstractSyntaxTree ast) {
 		return loadDependencies(ast.getRoot());
 	}
 	
 	/**
 	 * Clear module cache.
 	 */
 	public void clearCache() {
 		cache.clear();
 	}
 	
 	/**
 	 * Construct path, based on module identifier.
 	 * @param identifier Module identifier
 	 * @return Path
 	 */
 	public static String getPath(ModuleId identifier) {
 		String path = "";
 		for(int i = 0; i < identifier.size(); i++) {
 			if(i > 0) { path += "/"; }
 			path += identifier.get(i).getToken().getLexeme().toString();
 		}
 		path += ".wae";
 		return path;
 	}
 	
 	/**
 	 * Retrieve default instance of module cache.
 	 * @see <a href="http://en.wikipedia.org/wiki/Singleton_pattern">Singleton pattern</a>
 	 * @return Default instance
 	 */
 	public static ModuleRegister getInstance() {
 		if(instance == null) { instance = new ModuleRegister(); }
 		return instance;
 	}
 	
 	/**
 	 * Retrieve new instance of module cache.
 	 * @return New instance
 	 */
 	public static ModuleRegister newInstance() {
 		return new ModuleRegister();
 	}
 
 	/**
 	 * Thrown when lexical or syntactical exceptions occured during
 	 * the loading of a module.
 	 * @author Jeroen van Schagen
 	 */
 	public class ModuleLoadException extends Exception {
 
 		/**
 		 * Generated ID
 		 */
 		private static final long serialVersionUID = -1908201843040907562L;
 		
 		public ModuleLoadException(String path, List<LexicalException> le, List<SyntaxException> se) {
 			super("Module \"" + path + "\" could not be loaded, because several errors occured: " + le.toString() + se.toString());
 		}
 		
 	}
 	
 }
