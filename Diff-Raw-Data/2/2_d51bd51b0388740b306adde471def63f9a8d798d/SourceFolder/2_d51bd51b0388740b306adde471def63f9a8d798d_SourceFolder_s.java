 package ch.hsr.ifs.pystructure.typeinference.visitors;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import ch.hsr.ifs.pystructure.typeinference.model.base.NamePath;
 import ch.hsr.ifs.pystructure.typeinference.model.definitions.Module;
 import ch.hsr.ifs.pystructure.typeinference.model.definitions.Package;
 import ch.hsr.ifs.pystructure.typeinference.model.definitions.PathElement;
 import ch.hsr.ifs.pystructure.typeinference.model.definitions.PathElementContainer;
 
 public class SourceFolder implements PathElementContainer {
 	
 	private static final String PYTHON_FILES = ".*\\.py";
 	private final File sourceDirectory;
 	private final Map<String, PathElement> children;
 	private final Map<File, Module> filesToModules;
 	
 	public SourceFolder(File sourceDirectory) {
 		this.sourceDirectory = sourceDirectory;
 		this.children = new HashMap<String, PathElement>();
 		this.filesToModules = new HashMap<File, Module>();
 	}
 
 	public static void main(String[] args) {
 		File path = new File("s101g/examples/pydoku/");
 		SourceFolder sourceDir = new SourceFolder(path);
 		sourceDir.traverse();
 	}
 	
 	public void addChild(PathElement child) {
 		children.put(child.getName(), child);
 	}
 	
 	public PathElement getChild(String name) {
 		return children.get(name);
 	}
 	
 	public PathElementContainer getParent() {
 		return null;
 	}
 	
 	public NamePath getNamePath() {
 		return null;
 	}
 	
 	public Module getModule(File file) {
 		return filesToModules.get(file);
 	}
 	
 	public Collection<Module> getModules() {
 		return filesToModules.values();
 	}
 	
 	public void traverse() {
 		if (sourceDirectory.isDirectory()) {
 			for (File file : sourceDirectory.listFiles()) {
 				traverse(file, this);
 			}
 		}
 	}
 	
 	private void traverse(File file, PathElementContainer parent) {
 		String fileName = file.getName();
 		
 		if (file.isFile()) {
 			if (fileName.matches(PYTHON_FILES)) {
 				String name = fileName.replaceAll("\\.py$", "");
 				addModule(name, parent, file);
 			}
 			
 		} else if (file.isDirectory()) {
 			File initFile = new File(file, "__init__.py");
 			if (initFile.exists()) {
 				Package pkg = addPackage(fileName, parent, file, initFile);
 				
 				for (File child : file.listFiles()) {
 					if (child.equals(initFile)) {
 						// Don't register __init__.py files as modules
 						continue;
 					}
 					traverse(child, pkg);
 				}
 			}
 		}
 	}
 	
 	private void addModule(String name, PathElementContainer parent, File file) {
 		Module module = new Module(name, parent, file);
 		
 		StructureDefinitionVisitor structureDefinitionVisitor = new StructureDefinitionVisitor();
 		structureDefinitionVisitor.run(module);
 		DefinitionVisitor definitionVisitor = new DefinitionVisitor(module);
 		definitionVisitor.run();
 		
 		parent.addChild(module);
 		filesToModules.put(file, module);
 	}
 
 	private Package addPackage(String name, PathElementContainer parent, File dir, File initFile) {
 		Package pkg = new Package(name, parent, dir, initFile);
 		
 		parent.addChild(pkg);
 		return pkg;
 	}
 
 }
