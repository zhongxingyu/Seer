 package org.rubypeople.rdt.internal.core.hierarchy;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.rubypeople.rdt.core.IType;
 import org.rubypeople.rdt.core.RubyModelException;
 import org.rubypeople.rdt.internal.codeassist.RubyElementRequestor;
 import org.rubypeople.rdt.internal.core.LogicalType;
 import org.rubypeople.rdt.internal.core.Openable;
 
 public class HierarchyResolver {
 
 	private boolean superTypesOnly;
 	private HierarchyBuilder builder;
 
 	public HierarchyResolver(Map options, HierarchyBuilder builder) {
 		this.builder = builder;
 	}
 
 	public void resolve(Openable[] openables, HashSet localTypes, IProgressMonitor monitor) {
 		try {
 			int openablesLength = openables.length;
 			boolean[] hasLocalType = new boolean[openablesLength];
 			org.rubypeople.rdt.core.IRubyScript[] cus = new org.rubypeople.rdt.core.IRubyScript[openablesLength];
 			int unitsIndex = 0;
 			
 			IType focus = this.builder.getType();
 			Openable focusOpenable = null;
 			if (focus != null) {
 				focusOpenable = (Openable)focus.getRubyScript();
 			}
 			
 			for (int i = 0; i < openablesLength; i++) {
 				Openable openable = openables[i];
 				if (openable instanceof org.rubypeople.rdt.core.IRubyScript) {
 					org.rubypeople.rdt.core.IRubyScript cu = (org.rubypeople.rdt.core.IRubyScript)openable;
 
 					// contains a potential subtype as a local or anonymous type?
 					boolean containsLocalType = false;
 					if (localTypes == null) { // case of hierarchy on region
 						containsLocalType = true;
 					} else {
 						IPath path = cu.getPath();
 						containsLocalType = localTypes.contains(path.toString());
 					}
 					
 					// Grab the types from the script and then connect them up!
 					IType[] types = cu.getAllTypes();
 					for (int j = 0; j < types.length; j++) {
 						try {
							reportHierarchy(types[i]);
 						} catch (RubyModelException e) {
 							// ignore
 						}
 					}
 				}
 			}			
 		} catch (ClassCastException e){ // work-around for 1GF5W1S - can happen in case duplicates are fed to the hierarchy with binaries hiding sources
 		} catch (RubyModelException e){ 
 		} finally {
 			reset();
 		}		
 	}
 
 	private void reportHierarchy(IType type) throws RubyModelException {
 		IType superclass;
		if (type.isModule()){ // do not connect interfaces to Object
 			superclass = null;
 		} else {
 			superclass = findSuperClass(type);
 		}
 		IType[] superinterfaces = findSuperInterfaces(type);
 		
 		this.builder.connect(type, superclass, superinterfaces);
 	}
 
 	private IType[] findSuperInterfaces(IType type) throws RubyModelException {
 		String[] names = type.getIncludedModuleNames();
 		List<IType> types = new ArrayList<IType>();
 		for (int i = 0; i < names.length; i++) {
 			types.add(getLogicalType(type, names[i]));
 		}
 		return (IType[]) types.toArray(new IType[types.size()]);
 	}
 
 	private IType findSuperClass(IType type) throws RubyModelException {
 		String name = type.getSuperclassName();
 		return getLogicalType(type, name);
 	}
 
 	private IType getLogicalType(IType type, String name) {
 		RubyElementRequestor requestor = new RubyElementRequestor(type.getRubyScript());
 		return new LogicalType(requestor.findType(name));
 	}
 
 	private void reset() {
 //		this.focusType = null;
 		this.superTypesOnly = false;		
 	}
 
 	public void resolve(IType type) {
 		org.rubypeople.rdt.core.IRubyScript cu = type.getRubyScript();
 		HashSet localTypes = new HashSet();
 		localTypes.add(cu.getPath().toString());
 		this.superTypesOnly = true;
 		resolve(new Openable[] {(Openable)cu}, localTypes, null);		
 	}
 
 }
