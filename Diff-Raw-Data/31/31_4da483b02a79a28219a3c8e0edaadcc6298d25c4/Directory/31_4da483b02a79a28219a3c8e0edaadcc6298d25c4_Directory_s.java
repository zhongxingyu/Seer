/*
 * Created on Jun 4, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
 package nl.cwi.sen.metastudio.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
/**
 * @author kooiker
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
 public class Directory extends Model {
 	protected List directories;
 	protected List modules;
 
 	private static IModelVisitor adder = new Adder();
 	private static IModelVisitor remover = new Remover();
 
 	public Directory() {
 		directories = new ArrayList();
 		modules = new ArrayList();
 	}
 
 	private static class Adder implements IModelVisitor {
 		public void visitDirectory(Directory directory, Object argument) {
 			((Directory)argument).addDirectory(directory);
 		}
 		
 		public void visitModule(Module module, Object argument) {
 			((Directory)argument).addModule(module);
 		}
 	}
 
 	private static class Remover implements IModelVisitor {
 		public void visitDirectory(Directory directory, Object argument) {
 			((Directory)argument).removeDirectory(directory);
 			directory.addListener(NullDeltaListener.getSoleInstance());
 		}
 
 		public void visitModule(Module module, Object argument) {
 			((Directory)argument).removeModule(module);
 		}
		
 	}
 	
 	public Directory(String directory) {
 		this();
 		this.modulePath = directory;
 	}
 	
 	public Directory getChild(String childName) {
 		for (int i = 0; i < directories.size(); i++) {
 			Directory dir = (Directory)directories.get(i);
 			if (childName.equals(dir.getModulePath())) {
 				return dir;
 			}
 		}
 		return null;
 	}
 	
 	public Module getChildModule(String childName) {
 		for (int i = 0; i < modules.size(); i++) {
 			Module module = (Module)modules.get(i);
 			if (childName.equals(module.getModuleName())) {
 				return module;
 			}
 		}
 		return null;
 	}
 	
 	public List getDirectories() {
 		return directories;
 	}
 	
 	protected void addDirectory(Directory directory) {
 		directories.add(directory);
 		directory.parent = this;
 		fireAdd(directory);
 	}
 	
 	protected void addModule(Module module) {
 		modules.add(module);
 		module.parent = this;
 		fireAdd(module);
 	}
 	
 	public List getModules() {
 		return modules;
 	}
 	
 	public void remove(Model toRemove) {
 		toRemove.accept(remover, this);
 	}
 	
 	protected void removeModule(Module module) {
 		modules.remove(module);
 		module.addListener(NullDeltaListener.getSoleInstance());
 		fireRemove(module);
 	}
 	
 	protected void removeDirectory(Directory directory) {
 		directories.remove(directory);
 		directory.addListener(NullDeltaListener.getSoleInstance());
 		fireRemove(directory);	
 	}
 
 	public void add(Model toAdd) {
 		toAdd.accept(adder, this);
 	}
 	
 	public int size() {
 		return getDirectories().size() + getModules().size();
 	}
 	
 	public void accept(IModelVisitor visitor, Object argument) {
 		visitor.visitDirectory(this, argument);
 	}
 }
