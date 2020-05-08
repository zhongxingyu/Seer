 package jsclassloader.cli;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import jsclassloader.Bundler;
 import jsclassloader.Config;
 import jsclassloader.dependency.ClassNode;
 
 
 public class CommandLineRunner {
 
 	static boolean usePropertiesFile;
 	static String propFileName;
 	static List<String> seedClassesFromCmdLine;
 	static String [] sourceFolders;
 	private final static Logger LOG = Logger.getLogger("JS-Class-Loader");
 	
 	public static void main(String[] args) throws Exception {
 		
 		LOG.setLevel(Level.WARNING);
 		
 		ArgumentParser parser = new ArgumentParser();
 		Config config = parser.parseArgs(args);
 		
 		Bundler bundler = new Bundler(config);
 		
 		PrintStream out;
 		
 		if (parser.isListMode()) {
 			for (ClassNode item : bundler.getClassList()) {
 				System.out.println(item.getValue());
 			}
 		}
 		else {
 			String scriptTagFileConfig = config.getProperty(Config.PROP_SCRIPT_TAGS);
 			if (scriptTagFileConfig != null) {
 				File scriptTagFile = prepFile(scriptTagFileConfig);
 				out = new PrintStream(new FileOutputStream(scriptTagFile));
 				bundler.writeScriptTags(out, config);
 				out.close();
 			}
 			
 			String graphFileConfig = config.getProperty(Config.PROP_GRAPH_FILE);
 			if (graphFileConfig != null) {
 				File graphFile = prepFile(graphFileConfig);
 				out = new PrintStream(new FileOutputStream(graphFile));
 				out.print(bundler.getDependencyGraph().renderDotFile(bundler.getSeedClassNameList()));
 				out.close();
 			}
 			
 			String bundleFileConfig = config.getProperty(Config.PROP_BUNDLE_FILE);
 			if (bundleFileConfig != null) {
 				File bundleFile = prepFile(bundleFileConfig);
 				out = new PrintStream(new FileOutputStream(bundleFile));
 				bundler.write(out);
 				out.close();	
 			}
 			else {
 				bundler.write(System.out);
 			}
 		}
 	}
 	
 	public static File prepFile(String path) {
 		File file = new File(path);
		if (file.getParentFile() != null && !file.getParentFile().exists()) {
 			file.getParentFile().mkdirs();
 		}
 		return file;
 	}
 }
