 package org.eclipse.dltk.ddp;
 
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.core.ISourceModule;
 
public class BasicContext implements IContext {
 
 	private final ISourceModule sourceModule;
 	private final ModuleDeclaration rootNode;
 
 	public BasicContext(ISourceModule sourceModule, ModuleDeclaration rootNode) {
 		this.sourceModule = sourceModule;
 		this.rootNode = rootNode;
 	}
 
	public BasicContext(BasicContext parent) {
		sourceModule = parent.sourceModule;
		rootNode = parent.rootNode;
 	}
 
 	public ModuleDeclaration getRootNode() {
 		return rootNode;
 	}
 
 	public ISourceModule getSourceModule() {
 		return sourceModule;
 	}
 	
 	
 	
 }
