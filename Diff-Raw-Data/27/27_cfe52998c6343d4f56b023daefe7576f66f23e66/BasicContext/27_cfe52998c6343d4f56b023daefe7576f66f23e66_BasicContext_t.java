 package org.eclipse.dltk.ddp;
 
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.core.ISourceModule;
 
public class BasicContext implements IContext, ISourceModuleContext {
 
 	private final ISourceModule sourceModule;
 	private final ModuleDeclaration rootNode;
 
 	public BasicContext(ISourceModule sourceModule, ModuleDeclaration rootNode) {
 		this.sourceModule = sourceModule;
 		this.rootNode = rootNode;
 	}
 
	public BasicContext(ISourceModuleContext parent) {
		sourceModule = parent.getSourceModule();
		rootNode = parent.getRootNode();
 	}
 
 	public ModuleDeclaration getRootNode() {
 		return rootNode;
 	}
 
 	public ISourceModule getSourceModule() {
 		return sourceModule;
 	}
 	
 	
 	
 }
