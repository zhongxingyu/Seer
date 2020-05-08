 package net.sf.jlayercheck.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 
 import javax.swing.tree.TreeNode;
 
 import net.sf.jlayercheck.util.exceptions.OrphanedSearchException;
 import net.sf.jlayercheck.util.exceptions.OverlappingModulesDefinitionException;
 import net.sf.jlayercheck.util.model.ClassDependency;
 import net.sf.jlayercheck.util.model.ClassSource;
 import net.sf.jlayercheck.util.modeltree.ClassNode;
 import net.sf.jlayercheck.util.modeltree.DefaultModelTree;
 import net.sf.jlayercheck.util.modeltree.DefaultPackageNode;
 import net.sf.jlayercheck.util.modeltree.DependenciesTreeModel;
 import net.sf.jlayercheck.util.modeltree.DependentClassNode;
 import net.sf.jlayercheck.util.modeltree.DependentModelTree;
 import net.sf.jlayercheck.util.modeltree.DependentModuleNode;
 import net.sf.jlayercheck.util.modeltree.DependentPackageNode;
 import net.sf.jlayercheck.util.modeltree.ModelTree;
 import net.sf.jlayercheck.util.modeltree.ModuleNode;
 import net.sf.jlayercheck.util.modeltree.PackageNode;
 import net.sf.jlayercheck.util.modeltree.UnallowedOrAllowedDependency;
 
 import org.objectweb.asm.ClassReader;
 
 /**
  * <p>Contains a configuration that describes the architecture of the project.
  * It consists of modules that contain packages. Modules can use other modules,
  * but only in a strict directional order, no dependency loops.
  * 
  * <p>An important function is {@link #getModelTree(DependencyVisitor)} that
  * creates a tree of all modules, packages and classes and their dependencies.
  * 
  * @author webmaster@earth3d.org
  */
 public class XMLConfiguration {
 	protected static Logger logger = Logger.getLogger("JLayerCheck"); 
 	
 	/**
 	 * Contains the packages that belong to one module.
 	 */
 	protected Map<String, Set<String>> modulePackages = new TreeMap<String, Set<String>>();
 
 	/**
 	 * Contains the dependencies that belong to one module.
 	 */
 	protected Map<String, Set<String>> moduleDependencies = new TreeMap<String, Set<String>>();
 
 	/**
 	 * Contains the sources defined in the configuration file.
 	 */
 	protected List<ClassSource> classSources = new ArrayList<ClassSource>();
 	
 	/**
 	 * Contains the packages that are excluded from the analysis (like java.**).
 	 */
 	protected Set<String> excludeList = new TreeSet<String>();
 	
     /**
      * Contains classnames of classes that are used as program entries for the
      * orphaned classes search.
      */
     protected Set<String> entryClasses = new TreeSet<String>();
 
 	public XMLConfiguration() {
 	}
 
 	protected void addPackageToExcludeList(String packageName) {
 		excludeList.add(packageName);
 	}
 
 	protected void addDependencyToModule(String moduleName, String dependencyName) {
 		logger.finer("Add dependency "+dependencyName+" to module "+moduleName);
 		
 		Set<String> deps = moduleDependencies.get(moduleName);
 		
 		if (deps == null) {
 			deps = new TreeSet<String>();
 			moduleDependencies.put(moduleName, deps);
 		}
 		
 		deps.add(dependencyName);
 	}
 
 	protected void addPackageToModule(String moduleName, String packageName) {
 		logger.finer("Add package "+packageName+" to module "+moduleName);
 		packageName = packageName.replaceAll("\\.", "/");
 
 		Set<String> packs = modulePackages.get(moduleName);
 		
 		if (packs == null) {
 			packs = new TreeSet<String>();
 			modulePackages.put(moduleName, packs);
 		}
 		
 		packs.add(packageName);
 	}
 	
 	/**
 	 * Returns the dependencies of all modules. The key is the element
 	 * that depends from all values in the given set. E.g. a dataset
 	 * a -> (b,c,d) means, that a depends on the modules b, c and d. This
 	 * normally means that b, c and d are on a lower layer in the
 	 * architecture.
 	 * 
 	 * @return Map with all module dependencies
 	 */
 	public Map<String, Set<String>> getModuleDependencies() {
 		return moduleDependencies;
 	}
 
 	/**
 	 * Returns the packages contained in every module. The key of the
 	 * Map is the name of the module and the Set contains all package
 	 * names.
 	 * 
 	 * @return Map with all modules and their packages
 	 */
 	public Map<String, Set<String>> getModulePackages() {
 		return modulePackages;
 	}
 
 	/**
 	 * <p>Returns the matching module for the given classname or null. If more than
 	 * one module matches, an exception is thrown, because it is a configuration
 	 * error.
 	 * <p>The input must be a classname, not a package name. The last part is
 	 * expected to be the name of the class. 
 	 * 
 	 * @return the matching module or null if none was found
 	 * @throws OverlappingModulesDefinitionException 
 	 */
 	public String getMatchingModule(String classname) throws OverlappingModulesDefinitionException {
 		String result = null;
 		
 		for(String modulename : getModulePackages().keySet()) {
 			for(String packagename : getModulePackages().get(modulename)) {
 				packagename = convertToRegularExpression(packagename);
 				packagename = packagename + "/[^/]*"; // allow an appended classname 
 
 				if (classname.matches(packagename)) {
 					if (result == null) {
 						result = modulename;
 					} else {
 						throw new OverlappingModulesDefinitionException("Class "+classname+" is matched by more than one module definition.");
 					}
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Returns a map that contains the mapping from packages
 	 * to modules.
 	 * 
 	 * @return
 	 */
 	public Map<String, String> getPackageModules() {
 		Map<String, String> result = new TreeMap<String, String>();
 		
 		for(String modulename : getModulePackages().keySet()) {
 			for(String packagename : getModulePackages().get(modulename)) {
 				result.put(packagename, modulename);
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Returns the list of sources for the class and java files.
 	 * 
 	 * @return list of sources
 	 */
 	public List<ClassSource> getClassSources() {
 		return classSources;
 	}
 
     /**
      * Returns a map containing all java class names and an URL
      * that points to the source file.
      * 
      * @return
      */
     public Map<String, URL> getAllClassSources() {
         Map<String, URL> result = new TreeMap<String, URL>();
         for(ClassSource source : getClassSources()) {
             result.putAll(source.getSourceFiles());
         }
         return result;
     }
 
     /**
      * Returns all package entries that are specified to exclude
      * in the configuration file.
      * 
      * @return
      */
 	public Set<String> getExcludeList() {
 		return excludeList;
 	}
 
 	/**
 	 * Returns true if the given class is excluded from the analysis.
 	 * 
 	 * @param dependency
 	 * @return
 	 */
 	public boolean isExcluded(String classname) {
 		for(String exclude : getExcludeList()) {
 			exclude = convertToRegularExpression(exclude);
 			
 			if (classname.matches(exclude)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	/**
 	 * Used internally to convert a string from the wildcard format used
 	 * in the configuration file into a regular expression.
 	 * 
 	 * @param wildcardstring
 	 * @return wildcardstring converted to regular expression
 	 */
 	protected String convertToRegularExpression(String wildcardstring) {
 		wildcardstring = wildcardstring.replaceAll("\\.", "/");
 //		wildcardstring = wildcardstring.replaceAll("/\\*[^\\*]", "/[^\\.]*");
 		wildcardstring = wildcardstring.replaceAll("\\*", ".*");
 		return wildcardstring;
 	}
 
     /**
      * Returns a Set of classes that are named as entry classes in the
      * configuration file. These classes are points where the execution
      * of the system can start. All classes that are directly or indirectly
      * referenced from these named entry classes are marked as used, all
      * others are marked as unused/orphaned. 
      * 
      * @return the entryClasses
      */
     public Set<String> getEntryClasses() {
         return entryClasses;
     }
 
     /**
      * Calculates the orphaned classes based on the entry points of the configuration
      * file and the dependency data from the DependencyVisitor.
      * 
      * @param dv the dependency data to use
      * @throws OrphanedSearchException 
      */
     public Set<String> getOrphanedClasses(Map<String, Map<String, Set<Integer>>> dependencies) throws OrphanedSearchException {
         Set<String> visitedClasses = new HashSet<String>();
         
         // add the entry points
         visitedClasses.addAll(entryClasses);
         
         // add all dependend classes
         Set<String> unvisited = null;
         Map<String, URL> allClassSources = getAllClassSources();
         do {
             unvisited = getUnvisitedDependendClasses(visitedClasses, dependencies, allClassSources);
             visitedClasses.addAll(unvisited);
         } while(unvisited.size()>0);
         
         // find the missing classes and return them as orphaned
         Set<String> orphanedClasses = new TreeSet<String>();
         
         for(String classname : dependencies.keySet()) {
             if (!visitedClasses.contains(classname)) {
                 orphanedClasses.add(classname);
             }
         }
         
         return orphanedClasses;
     }
 
     /**
      * Returns all classnames that are directly referenced by the visited classes
      * but not yet contained.
      * 
      * @param visitedClasses
      * @param dv the dependencies to use
      * @param allClassSources a map containing all classnames for which java source files are available
      * @return Set containing class names
      * @throws OrphanedSearchException 
      */
     protected Set<String> getUnvisitedDependendClasses(Set<String> visitedClasses, Map<String, Map<String, Set<Integer>>> dependencies, Map<String, URL> allClassSources) throws OrphanedSearchException {
         Set<String> result = new HashSet<String>();
         
         for(String visitedClass : visitedClasses) {
             if (dependencies.get(visitedClass) != null) {
                 for(String classname : dependencies.get(visitedClass).keySet()) {
                     if (!visitedClasses.contains(classname)) {
                         result.add(classname);
                     }
                 }
             } else {
                 if (allClassSources.containsKey(visitedClass)) {
                     // a class file for a source file is missing!
                     throw new OrphanedSearchException("Class file for "+visitedClass+" is not available! (Source file is "+allClassSources.get(visitedClass).toExternalForm());
                 } else {
                     logger.fine("Dependencies for "+visitedClass+" not found!");
                 }
             }
         }
         
         return result;
     }
     
     /**
      * Returns a map containing the dependencies (from class, to class) that
      * are not allowed by the rules.
      * 
      * @param xcp the configuration to use
      * @return
      * @throws OverlappingModulesDefinitionException 
      */
     public Map<String, Map<String, ClassDependency>> getUnallowedDependencies(Map<String, Map<String, Set<Integer>>> dependencies) throws OverlappingModulesDefinitionException {
 		Map<String, Map<String, ClassDependency>> unallowedDependencies = new TreeMap<String, Map<String, ClassDependency>>();
 		
 		for(String classname : dependencies.keySet()) {
 			for(String dependency : dependencies.get(classname).keySet()) {
 				// check if packagename is an allowed dependency for classname
 				if (isUnallowedDependency(classname, dependency)) {
 					Map<String, ClassDependency> depList = unallowedDependencies.get(classname);
 					if (depList == null) {
 						depList = new TreeMap<String, ClassDependency>();
 						unallowedDependencies.put(classname, depList);
 					}
 					ClassDependency cd = depList.get(dependency);
 					if (cd == null) {
 						cd = new ClassDependency(dependency);
 						depList.put(dependency, cd);
 					}
 					for(Integer lineNumber : dependencies.get(classname).get(dependency)) {
 						cd.addLineNumber(lineNumber);
 					}
 				}
 			}
 		}
 		
 		return unallowedDependencies;
     }
 
     /**
      * Returns true if the dependency from fromClass to toClass is not allowed,
      * otherwise false.
      * 
      * @param fromClass
      * @param toClass
      * @return
      * @throws OverlappingModulesDefinitionException
      */
     public boolean isUnallowedDependency(String fromClass, String toClass) throws OverlappingModulesDefinitionException {
 		String classmodule = getMatchingModule(fromClass);
 		String dependencymodule = getMatchingModule(toClass);
 		
 		if (classmodule == null) {
 			// unspecified package
 			return false;
 		} else {
 			if (!classmodule.equals(dependencymodule)) {
 				if (!(isExcluded(fromClass) || isExcluded(toClass))) {
 					if (isUnallowedModuleDependency(classmodule, dependencymodule)) {
 						return true;
 					}
 				}
 			}
 		}
 	
 		return false;
     }
 
     /**
      * Returns true if the dependency from fromModule to toModule is not allowed,
      * otherwise false.
      * 
      * @param fromModule
      * @param toModule
      * @return
      */
     public boolean isUnallowedModuleDependency(String fromModule, String toModule) {
     	if (fromModule.equals(toModule)) return false;
     	
 		if (getModuleDependencies().get(fromModule) == null || toModule == null || 
 				!getModuleDependencies().get(fromModule).contains(toModule)) {
 			return true;
 		}
 		
 		return false;
     }
     
     /**
      * Returns a list of packages that are not assigned to
      * a module in the given configuration.
      * 
      * @param xcp
      * @return
      * @throws OverlappingModulesDefinitionException 
      */
     public Set<String> getUnspecifiedPackages(Map<String, Map<String, Set<Integer>>> dependencies) throws OverlappingModulesDefinitionException {
 		Set<String> unspecifiedPackages = new TreeSet<String>();
 		
 		for(String classname : dependencies.keySet()) {
 			String classPackageName = StringUtils.getPackageName(classname);
 			
 			// check if packagename is an allowed dependency for classname
 			String classmodule = getMatchingModule(classPackageName+"/Dummy");
 
 			if (classmodule == null) {
 				unspecifiedPackages.add(classPackageName);
 			}
 		}
 		
 		return unspecifiedPackages;
     }
     
     /**
      * Builds a model that contains all dependency information that was retrieved
      * from the class files. It can be used for displaying the dependencies in a 
      * tree view.
      * 
      * @param dv
      * @return
      * @throws OverlappingModulesDefinitionException
      */
     public ModelTree getModelTree(DependencyVisitor dv) throws OverlappingModulesDefinitionException {
     	DefaultModelTree result = new DefaultModelTree();
     	
     	// build ModelTree
     	for(String packagename : dv.getPackages().keySet()) {
 
     		// get the module for this package
 			String packagemodule = getMatchingModule(packagename+"/Dummy");
 			
 			boolean packageUnassigned = false;
 			if (packagemodule == null) {
 				packagemodule = "unassigned";
 				packageUnassigned = true;
 			}
 			
 			ModuleNode module = result.getModule(packagemodule);
 			
 			if (module == null) {
 				module = new DependentModuleNode(packagemodule, packageUnassigned);
 				result.add(module);
 			}
 			
 			// add the new package node
     		DefaultPackageNode packagenode = new DependentPackageNode(packagename);
 			module.add(packagenode);
 
     		for(String classname : dv.getPackages().get(packagename)) {
     			// add class nodes for all packages
     			packagenode.add(new DependentClassNode(new ClassDependency(classname)));
     		}
     	}
 
     	// add dependencies
     	for(String classname : dv.getDependencies().keySet()) {
     		for(String dep : dv.getDependencies().get(classname).keySet()) {
     			// create ClassDependency
     			ClassDependency cd = new ClassDependency(dep);
     			
     			for(Integer line : dv.getDependencies().get(classname).get(dep)) {
     				cd.addLineNumber(line);
     			}
 
     			cd.setUnallowedDependency(isUnallowedDependency(classname, dep));
     			
     			// and add it to the ClassNode
 				ClassNode cn = result.getClassNode(classname);
 				if (cn != null) {
 					cn.addClassDependency(cd);
 				} else {
 					System.out.println("Class "+classname+" not found (to "+dep+")!");
 				}
     		}
     	}
 
     	cumulateDependencyViolations(result);
     	
     	return result;
     }
 
     /**
      * Updates the given modeltree by replacing all information of the given classFile with
      * new information.
      * 
      * @param mt
      * @param dv
      * @param classFile
      * @throws OverlappingModulesDefinitionException 
      * @throws IOException 
      * @throws FileNotFoundException 
      */
     public void updateModelTree(ModelTree mt, File classFile) throws OverlappingModulesDefinitionException, FileNotFoundException, IOException {
     	DependencyVisitor dv = new DependencyVisitor();
     	
     	// let the DependencyVisitor retrieve the information from the classFile
 		new ClassReader(new FileInputStream(classFile)).accept(dv, 0);
 		
 		// replace the information in the modeltree
 		for(Set<String> classes : dv.getPackages().values()) {
 			for(String clazz : classes) {
 				ClassNode cn = mt.getClassNode(clazz);
				if (cn != null) {
					cn.removeFromParent();
				}
 			}
 		}
 		
 		ModelTree additionalModelTree = getModelTree(dv);
 		mt.merge(additionalModelTree);
     }
     
     /**
      * Returns true if the module sourceModule may access destModule.
      * 
      * @param sourceModule
      * @param destModule
      * @return
      */
 	public boolean isUnallowedDependency(ModuleNode sourceModule, ModuleNode destModule) {
 		return isUnallowedModuleDependency(sourceModule.getModuleName(), destModule.getModuleName());
 	}
 	
 	/**
 	 * Recalculates the violations state of all nodes of the tree that contain children
 	 * (e.g. packages and modules).
 	 * @param ModelTree the tree that should be recalculated
 	 */
 	public void cumulateDependencyViolations(ModelTree mt) {
     	// calculate state unallowed/allowed dependencies for the tree
 		// compute unallowed dependency marks
 		for(ModuleNode mn : mt.getModules()) {
 			boolean mnUnallowed = false;
 			for(PackageNode pn : mn.getPackages()) {
 				boolean pnUnallowed = false;
 				for (ClassNode cn : pn.getClasses()) {
 					if (cn instanceof DependentClassNode) {
 						DependentClassNode dcn = (DependentClassNode) cn;
 						
 						DependenciesTreeModel dtm = new DependenciesTreeModel();
 						dtm.setRoot(createModel(dcn, mt, dtm));
 						dcn.getClassDependency().setUnallowedDependency(((UnallowedOrAllowedDependency) dtm.getRoot()).isUnallowedDependency()); 
 						dcn.setDependenciesTreeModel(dtm);
 						
 						if (dcn.getClassDependency().isUnallowedDependency()) {
 							pnUnallowed = true;
 							mnUnallowed = true;
 						}
 						
 						dtm = new DependenciesTreeModel();
 						dtm.setRoot(createIncomingModel(dcn, mt, dtm));
 						dcn.setIncomingDependenciesTreeModel(dtm);
 					}
 				}
 				
 				if (pn instanceof DependentPackageNode) {
 					((DependentPackageNode) pn).setUnallowedDependency(pnUnallowed);
 				}
 			}
 
 			if (mn instanceof DependentModuleNode) {
 				((DependentModuleNode) mn).setUnallowedDependency(mnUnallowed);
 			}
 		}
 	}
 
 	/**
 	 * Creates a dependency tree model for the given node of the given ModelTree.
 	 * 
 	 * @param node
 	 * @param treemodel
 	 * @param xmlconf the configuration used to determine which dependencies are allowed
 	 * @return
 	 */
 	public TreeNode createModel(ClassNode node, ModelTree treemodel, DependenciesTreeModel modelToUpdate) {
 		DependentModelTree depTree = new DependentModelTree();
 		
 		// build tree from dependencies
 		for(ClassDependency cd : node.getClassDependencies()) {
 			
 			ClassNode depClass = treemodel.getClassNode(cd.getDependency());
 			
 			if (!cd.getDependency().equals(node.getName())) {
 				modelToUpdate.addClassNodeToDependentModelTree(depTree, cd, depClass);
 			}
 		}
 		
 		// compute unallowed dependency marks
 		boolean treeUnallowed = false;
 		for(ModuleNode mn : depTree.getModules()) {
 			boolean mnUnallowed = false;
 			for(PackageNode pn : mn.getPackages()) {
 				boolean pnUnallowed = false;
 				for (ClassNode cn : pn.getClasses()) {
 					if (cn instanceof DependentClassNode) {
 						DependentClassNode dcn = (DependentClassNode) cn;
 						
 						// recompute if dependency is allowed
 						boolean unallowedDependency = false;
 						if (!pn.isUnassignedPackage()) {
 							ModuleNode dModule = mn;
 							ClassNode sourceClass = node;
 	
 							if (sourceClass !=null) {
 								PackageNode sourcePackage = (PackageNode) sourceClass.getParent();
 	
 								if (!sourcePackage.isUnassignedPackage()) {
 									ModuleNode sourceModule = (ModuleNode) sourcePackage.getParent();
 									
 									if (!dModule.isUnassignedModule() && !sourceModule.isUnassignedModule()) {
 										unallowedDependency = isUnallowedDependency(sourceModule, dModule);
 									}
 								}
 							} else {
 								unallowedDependency = true;
 							}
 						}
 						dcn.getClassDependency().setUnallowedDependency(unallowedDependency);
 						
 						if (dcn.getClassDependency().isUnallowedDependency()) {
 							pnUnallowed = true;
 							mnUnallowed = true;
 							treeUnallowed = true;
 						}
 					}
 				}
 				
 				if (pn instanceof DependentPackageNode) {
 					((DependentPackageNode) pn).setUnallowedDependency(pnUnallowed);
 				}
 			}
 	
 			if (mn instanceof DependentModuleNode) {
 				((DependentModuleNode) mn).setUnallowedDependency(mnUnallowed);
 			}
 		}
 		depTree.setUnallowedDependency(treeUnallowed);
 		
 		// sort nodes and sort the "unassigned" node to the end
 		depTree.sortNodes();
 		
 		return depTree;
 	}
 
 	/**
 	 * Creates a dependency tree model for the given node of the given ModelTree.
 	 * 
 	 * @param node
 	 * @param treemodel
 	 * @param xmlconf the configuration used to determine which dependencies are allowed
 	 * @return
 	 */
 	public TreeNode createIncomingModel(ClassNode node, ModelTree treemodel, DependenciesTreeModel modelToUpdate) {
 		DependentModelTree depTree = new DependentModelTree();
 		
 		// build tree from incoming references
 		for(ModuleNode mn : treemodel.getModules()) {
 			for(PackageNode pn : mn.getPackages()) {
 				for (ClassNode cn : pn.getClasses()) {
 					for(ClassDependency cd : cn.getClassDependencies()) {
 						if (cd.getDependency().equals(node.getName())) {
 							// Class cn has a dependency to this class
 							
 							// only add it if dependency and origin are not the same
 							if (!cn.getName().equals(node.getName())) {
 								ClassNode depClass = treemodel.getClassNode(cn.getName());
 								ClassDependency ncd = new ClassDependency(cn.getName());
 								modelToUpdate.addClassNodeToDependentModelTree(depTree, ncd, depClass);
 							}
 						}
 					}
 				}
 			}
 			
 		}
 		
 		// compute unallowed dependency marks
 		boolean treeUnallowed = false;
 		for(ModuleNode mn : depTree.getModules()) {
 			boolean mnUnallowed = false;
 			for(PackageNode pn : mn.getPackages()) {
 				boolean pnUnallowed = false;
 				for (ClassNode cn : pn.getClasses()) {
 					if (cn instanceof DependentClassNode) {
 						DependentClassNode dcn = (DependentClassNode) cn;
 						
 						// recompute if dependency is allowed
 						boolean unallowedDependency = false;
 						if (!pn.isUnassignedPackage()) {
 							ModuleNode dModule = mn;
 							ClassNode sourceClass = node;
 
 							if (sourceClass !=null) {
 								PackageNode sourcePackage = (PackageNode) sourceClass.getParent();
 
 								if (!sourcePackage.isUnassignedPackage()) {
 									ModuleNode sourceModule = (ModuleNode) sourcePackage.getParent();
 									
 									if (!dModule.isUnassignedModule() && !sourceModule.isUnassignedModule()) {
 										unallowedDependency = isUnallowedDependency(dModule, sourceModule);
 									}
 								}
 							} else {
 								unallowedDependency = true;
 							}
 						}
 						dcn.getClassDependency().setUnallowedDependency(unallowedDependency);
 						
 						if (dcn.getClassDependency().isUnallowedDependency()) {
 							pnUnallowed = true;
 							mnUnallowed = true;
 							treeUnallowed = true;
 						}
 					}
 				}
 				
 				if (pn instanceof DependentPackageNode) {
 					((DependentPackageNode) pn).setUnallowedDependency(pnUnallowed);
 				}
 			}
 
 			if (mn instanceof DependentModuleNode) {
 				((DependentModuleNode) mn).setUnallowedDependency(mnUnallowed);
 			}
 		}
 		depTree.setUnallowedDependency(treeUnallowed);
 		
 		// sort nodes and sort the "unassigned" node to the end
 		depTree.sortNodes();
 		
 		return depTree;
 	}
 
 	/**
 	 * Adds a new entry class. An entry class is a class where the program
 	 * can start (e.g. a class that contains a main method, an Applet class
 	 * or a servlet). It is used to detect orphaned classes.
 	 * 
 	 * @param entryClass
 	 */
 	public void addEntryClass(String entryClass) {
 		getEntryClasses().add(entryClass);
 	}
 
 	/**
 	 * Adds dependencies from a module to other modules.
 	 *  
 	 * @param moduleName
 	 * @param name
 	 */
 	public void addModuleDependency(String moduleName, TreeSet<String> name) {
 		moduleDependencies.put(moduleName, name);
 	}
 
 	/**
 	 * Adds a new ClassSource.
 	 * @param source
 	 */
 	public void addClassSource(ClassSource source) {
 		classSources.add(source);
 	}
 }
