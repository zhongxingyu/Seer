 package rs2.environment.script;
 
 import groovy.util.GroovyScriptEngine;
 import groovy.util.ResourceException;
 import groovy.util.ScriptException;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.codehaus.groovy.control.customizers.ImportCustomizer;
 
 /**
  * @author Lazaro Brito
  *
  */
 public class ScriptBase {
 	private static GroovyScriptEngine engine;
 	
 	public static void generateImports(File mainDir, File dir, BufferedWriter writer) throws IOException {
 		for(File file : dir.listFiles()) {
 			if(file.isDirectory()) {
 				generateImports(mainDir, file, writer);
 				continue;
 			}
 			
 			if(file.getName().endsWith(".class") && !file.getName().contains("$")) {
 				String name = file.getPath().replace(File.separatorChar, '/').replace(mainDir.getPath() + "/", "").replace(".class", "").replace('/', '.');
 				
 				writer.write(name);
 				writer.newLine();
 			}
 		}
 	}
 	
 	private final Map<String, Class<?>> scripts = new HashMap<String, Class<?>>();
 	
	public ScriptBase(String[] classPath) {
 		try {
 			engine = new GroovyScriptEngine(classPath);
 			
 			ImportCustomizer imports = new ImportCustomizer();
 			imports.addImport("RS2Environment", "rs2.enrionment.RS2Environment");
 			imports.addImport("Constants", "alterrs.Constants");
			File importsFile = new File("data/script_api.txt");
 			loadImports(importsFile, imports);
 			
 			engine.getConfig().addCompilationCustomizers(imports);
 		} catch (IOException e) {
 			System.err.println("Failed to load script base!");
 			e.printStackTrace();
 		}
 	}
 	
 	private void loadImports(File file, ImportCustomizer imports) throws IOException {
 		BufferedReader reader = new BufferedReader(new FileReader(file));
 		String name;
 		while((name = reader.readLine()) != null) {
 			imports.addImport(name.substring(name.lastIndexOf('.')+1, name.length()), name);
 		}
 		reader.close();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <S> Class<S> loadScript(String scriptName) {
 		Class<S> clazz = (Class<S>) scripts.get(scriptName);
 		if(clazz == null) {
 			try {
 				clazz = engine.loadScriptByName(scriptName.replace('.', '/') + ".groovy");
 			} catch (ResourceException | ScriptException e) {
 				System.err.println("Failed to load script: " + scriptName);
 				e.printStackTrace();
 				return null;
 			}
 			scripts.put(scriptName, clazz);
 		}
 		return clazz;
 	}
 
 	public <S> S initScript(String scriptName, Object... args) {
 		try {
 			Class<S> clazz = loadScript(scriptName);
 			
 			Class<?> types[] = new Class[args.length];
 			for(int i=0; i<args.length; i++) {
 				types[i] = args[i].getClass();
 			}
 			
 			return (S) clazz.getConstructor(types).newInstance(args);
 		} catch (IllegalArgumentException | InvocationTargetException
 				| NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException e) {
 			System.err.println("Failed to initiate object: " + scriptName);
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public void clearCache() {
 		engine.getGroovyClassLoader().clearCache();
 		scripts.clear();
 	}
 }
