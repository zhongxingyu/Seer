 package org.antz29.jsbuilder;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Scanner;
 import java.util.Vector;
 import java.util.regex.MatchResult;
 import java.util.regex.Pattern;
 
 import org.antz29.jsbuilder.utils.StringUtils;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.DirectoryScanner;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.types.FileSet;
 
 public class Builder extends Task {
 
 	public static final int MODE_DEV = 1;
 	public static final int MODE_PROD = 2;
 	
 	private Vector<FileSet> filesets = new Vector<FileSet>();
 	private Vector<Package> packages = new Vector<Package>();
 	private Vector<StaticModule> static_modules = new Vector<StaticModule>();
 
 	private boolean compile = true;
 	private int mode = MODE_PROD;
 	private File output;
 	
 	private RendererLoader renderer_loader;
 
 	public Project getAntProject() {
 		return getProject();
 	}
 
 	public void setCompile(boolean compile) {
 		this.compile = compile;
 	}	
 	
 	public boolean getCompile() {
 		return compile;
 	}	
 	
 	public int getMode() {
 		return mode;
 	}
 	
 	public File getOutput() {
 		return output;
 	}
 	
 	public void setMode(String mode) throws BuildException {
 		if (mode.equals("prod")) {
 			this.mode = MODE_PROD;
 		}
 		else if (mode.equals("dev")) {
 			this.mode = MODE_DEV;
 		}
 		else {
 			throw new BuildException("Invalid mode: " + mode + " - User either 'dev' or 'prod'");
 		}
 	}
 
 	public void setOutput(File output) {
 		this.output = output;
 	}
 	
 	public void addRenderer(RendererLoader loader) {
 		renderer_loader = loader;
 	}
 	
 	public void addModule(StaticModule module) {
 		static_modules.add(module);
 	}
 	
 	public void addFileSet(FileSet fileset) {
 		if (!filesets.contains(fileset))
 			filesets.add(fileset);
 	}
 
 	public Package findPackage(String search) {
 		Package found = null;
 		for (Package pkg : packages) {
 			if (pkg.getName().equals(search)) {
 				found = pkg;
 			}
 		}
 		return found;
 	}
 
 	public Module findModule(String search) {
 		String[] mod = search.split(":");
 
 		if (mod.length != 2)
 			return null;
 
 		Package pkg;
 		pkg = findPackage(mod[0]);
 
 		if (pkg == null)
 			return null;
 
 		Vector<Module> mods = pkg.getModules();
 
 		Module found = null;
 		for (Module module : mods) {
 			if (module.getName().equals(mod[1])) {
 				found = module;
 			}
 		}
 		return found;
 	}
 
 	private Vector<File> getFiles() {
 		Vector<File> out = new Vector<File>();
 
 		for (FileSet fileset : filesets) {
 			DirectoryScanner ds = fileset.getDirectoryScanner(getProject());
 			File dir = ds.getBasedir();
 			String[] filesInSet = ds.getIncludedFiles();
 
 			for (String filename : filesInSet) {
 				File file = new File(dir, filename);
 				if (!out.contains(file))
 					out.add(file);
 			}
 		}
 
 		return out;
 	}
 
 	private Hashtable<String, String[]> parseFile(File file) {
 
 		Hashtable<String, String[]> tokens = new Hashtable<String, String[]>();
 		Scanner scanner;
 
 		Pattern pattern = Pattern
				.compile("#(PACKAGE|MODULE|DEPENDS):([0-9a-z.,:!\040]*)");
 
 		try {
 			scanner = new Scanner(new FileInputStream(file), "UTF-8");
 		} catch (FileNotFoundException e) {
 			return null;
 		}
 
 		try {
 			String next_token = scanner.findWithinHorizon(pattern, 1000);
 			while (next_token != null) {
 				MatchResult match = scanner.match();
 
 				String[] value = StringUtils.trimArray(match.group(2).split(","));
 				String token = match.group(1).trim();
 				tokens.put(token, value);
 
 				next_token = scanner.findWithinHorizon(pattern, 1000);
 			}
 			return tokens;
 		} finally {
 			scanner.close();
 		}
 	}
 
 	public Package addPackage(String name) {
 		Package new_package = new Package().setName(name).setBuilder(this);
 
 		int find_package = packages.indexOf(new_package);
 
 		if (find_package == -1) {
 			packages.add(new_package);
 			return new_package;
 		}
 
 		return packages.get(find_package);
 	}
 
 	private void parsePackages(Vector<File> files) {
 		for (File file : files) {
 			Hashtable<String, String[]> tokens = parseFile(file);
 
 			if (tokens.get("MODULE") == null) {
 				getProject().log("WARNING: " + file.getName() + " has no MODULE token. Ignoring file.");
 				continue;
 			}
 
 			String module_name = tokens.get("MODULE")[0];
 			String package_name = (tokens.get("PACKAGE") != null) ? tokens
 					.get("PACKAGE")[0] : getProject().getName();
 
 			Package pkg = addPackage(package_name);
 			Module mod = pkg.addModule(module_name, file);
 
 			String[] deps = tokens.get("DEPENDS");
 			if (deps != null) {
 				mod.setUnresolvedDeps(deps);
 			}
 		}
 	}
 
 	private Vector<Module> getLoadOrder() {
 		Vector<Module> order = new Vector<Module>();
 
 		for (Package pkg : packages) {
 			for (Module mod : pkg.getModules()) {
 				mod.resolveDeps();
 				order.add(mod);
 			}
 		}
 
 		Collections.sort(order);
 
 		return order;
 	}
 
 	private Vector<Module> verifyLoadOrder(Vector<Module> order) {
 
 		Vector<Module> bad_order = testLoadOrder(order);
 
 		int sanity = 0;
 
 		while (bad_order.size() > 0 && sanity < 10) {
 			sanity++;
 			order = fixLoadOrder(order, bad_order);
 			bad_order = testLoadOrder(order);
 		}
 
 		if (bad_order.size() > 0) {
 			getProject()
 					.log("WARNING: Failed to resolve dependencies for all modules (possible circular dependency?\n" +
 							"These modules have problems: " + bad_order);
 		}
 
 		return order;
 	}
 
 	private Vector<Module> testLoadOrder(Vector<Module> order) {
 		Vector<Module> order_test = new Vector<Module>();
 		Vector<Module> bad_order = new Vector<Module>();
 
 		for (Module mod : order) {
 			order_test.add(mod);
 			for (Module dep : mod.getDeps()) {
 				if (!order_test.contains(dep)) {
 					bad_order.add(mod);
 				}
 			}
 		}
 
 		return bad_order;
 	}
 
 	private Integer getCorrectPosition(Vector<Module> order, Module mod) {
 		Vector<Integer> dep_locs = new Vector<Integer>();
 		Vector<Module> deps = mod.getDeps();
 
 		for (Module dep : deps) {
 			int loc = order.indexOf(dep);
 			if (loc == -1) {
 				getProject().log("WARNING: " + mod + " has unresolvable dependency: " + dep);
 				return 0;
 			}
 			dep_locs.add(loc);
 		}
 
 		Collections.sort(dep_locs);
 
 		return dep_locs.lastElement();
 	}
 
 	private Vector<Module> fixLoadOrder(Vector<Module> order,
 			Vector<Module> bad_order) {
 
 		for (Module mod : bad_order) {
 			int location = order.indexOf(mod);
 			order.remove(location);
 			int new_location = getCorrectPosition(order, mod);
 			order.add((new_location + 1), mod);
 		}
 
 		return order;
 	}
 
 	public void addStaticModule(StaticModule sm) {
 		Package pkg = this.addPackage(sm.getPackage());
 		Module mod = pkg.addModule(sm.getName(), sm.getFile());
 		mod.setUnresolvedDeps(sm.getDependencies());
 		
 		this.log("Added static module " + sm.getPackage() + ":" + sm.getName());
 	}
 	
 	private Renderer getRenderer() {
 		return renderer_loader.loadRenderer();
 	}
 		
 	public void execute() throws BuildException {
 		Vector<File> files = getFiles();			
 		
 		getProject().log("Mode: " + mode);
 		getProject().log("Output: " + output.getAbsolutePath());
 		getProject().log("Processing " + files.size() + " file(s)");
 		if (static_modules.size() > 0) {
 			getProject().log("Defined " + static_modules.size() + " static module(s)");
 		}
 		getProject().log("\n");
 		
 		for (StaticModule sm : static_modules)
 		{
 			this.addStaticModule(sm);
 		}
 		
 		parsePackages(files);
 		
 		Vector<Module> order = getLoadOrder();
 		order = verifyLoadOrder(order);
 		
 		Renderer renderer;
 		if (renderer_loader == null)
 		{
 			renderer = new DefaultRenderer();
 		}
 		else {
 			renderer = getRenderer();
 		}
 				
 		renderer.setBuilder(this);		
 		renderer.renderPackages(packages, order);
 		renderer.renderRules(packages, order);
 	}
 }
