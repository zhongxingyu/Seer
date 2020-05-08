 package net.histos.java2as.as3.transfer;
 
 import net.histos.java2as.as3.As3CustomType;
 import net.histos.java2as.as3.As3SimpleType;
 import net.histos.java2as.as3.DependencyResolver;
 import net.histos.java2as.core.conf.PackageMapper;
 import net.histos.java2as.core.meta.DependencyKind;
 import net.histos.java2as.core.meta.JavaTransferObject;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Represents an ActionScript transfer object class.
  */
 public class As3TransferObject {
 
 	//
 	// Fields
 	//
 
 	protected JavaTransferObject transferObject;
 	protected List<As3Property> properties;
 	protected List<As3Dependency> dependencies;
 
 	protected String qualifiedName = "";
 	protected String packageName = "";
 	protected String simpleName = "";
 	protected String importsFragment = "";
 	protected String polymorphicsFragment = "";
 
 	//
 	// Constructors
 	//
 
 	/**
 	 * Constructs an ActionScript transfer object based on the Java transfer object.
 	 *
 	 * @param transferObject Java transfer object.
 	 */
 	public As3TransferObject(JavaTransferObject transferObject) {
 		this.transferObject = transferObject;
 		this.properties = new ArrayList<As3Property>();
 		this.dependencies = new ArrayList<As3Dependency>();
 	}
 
 	//
 	// Public Methods
 	//
 
 	/**
 	 * Adds a property to this transfer object.
 	 *
 	 * @param property Property.
 	 */
 	public void addProperty(As3Property property) {
 		if (property != null) {
 			this.properties.add(property);
 			addDependency(property.getDependency());
 		}
 	}
 
 	/**
 	 * Adds a dependency to this transfer object
 	 *
 	 * @param dependency Dependency.
 	 */
 	public void addDependency(As3Dependency dependency) {
 		// ensure there is only ever one superclass type registered
 		if (dependency.getDependencyKind() == DependencyKind.SUPERCLASS) {
 			As3Dependency existingSuperclass = null;
 			// a new superclass type will replace the old one
 			for (As3Dependency dep : dependencies) {
 				if (dep.getDependencyKind() == DependencyKind.SUPERCLASS) {
 					existingSuperclass = dep;
 					break;
 				}
 			}
 			if (existingSuperclass == null) {
 				dependencies.add(dependency);
 			} else if (dependency.getDependencyType() != As3SimpleType.Object) {
 				dependencies.remove(existingSuperclass);
 				dependencies.add(dependency);
 			}
 		}
		this.dependencies.add(dependency);
 	}
 
 	/**
 	 * Updates metadata based on the supplied DependencyResolver.
 	 *
 	 * @param packageMapper
 	 * @param dependencyResolver DependencyResolver to use for deciding if/how an item gets imported.
 	 */
 	public void buildMetadata(PackageMapper packageMapper, DependencyResolver dependencyResolver) {
 
 		simpleName = transferObject.getSimpleName();
 
 		// modify the package name and qualified name if package mapper is applicable
 		if (packageMapper.canMap(transferObject.getPackageName())) {
 			packageName = packageMapper.performMap(transferObject.getPackageName());
 			qualifiedName = packageName + "." + simpleName;
 		} else {
 			packageName = transferObject.getPackageName();
 			qualifiedName = transferObject.getName();
 		}
 
 		buildImportsFragment(dependencyResolver);
 		buildPolymorphicsFragment(dependencyResolver);
 
 	}
 
 	/**
 	 * Specify configuration parameters to customize the output.
 	 *
 	 * @param superclassName Name of the superclass to use if no other is specified.
 	 */
 	public void applyConfiguration(String superclassName) {
 		if (superclassName != null && superclassName.length() > 0) {
 			// add a new default dependency on the specified superclass name
 			addDependency(new As3Dependency(DependencyKind.SUPERCLASS, new As3CustomType(superclassName)));
 		}
 	}
 
 	//
 	// Protected Methods
 	//
 
 	/**
 	 * Builds multi-line string fragment declaring all imported types.
 	 *
 	 * @param dependencyResolver DependencyResolver to use for deciding if/how an item gets imported.
 	 */
 	protected void buildImportsFragment(DependencyResolver dependencyResolver) {
 
 		List<String> imports = new ArrayList<String>();
 
 		// loop over all deps and determine which to add
 		for (As3Dependency dependency : dependencies) {
 			if (dependencyResolver.shouldResolve(packageName, dependency)) {
 				String resolvedName = dependencyResolver.resolveQualifiedName(dependency);
 				// check for dups
 				if (!imports.contains(resolvedName))
 					imports.add(resolvedName);
 			}
 		}
 
 		Collections.sort(imports);
 
 		StringBuilder fragment = new StringBuilder();
 
 		// build a nicely formatted string with empty lines between new top-level packages
 		// this matches the Flex Builder "organize imports" feature
 		String prevTopLevelPackage = "";
 		for (String importLine : imports) {
 			String nextTopLevelPackage = importLine.substring(0, importLine.indexOf("."));
 			if (!prevTopLevelPackage.equals(nextTopLevelPackage)) {
 				fragment.append("\n");
 				prevTopLevelPackage = nextTopLevelPackage;
 			}
 			fragment.append("\t");
 			fragment.append(importLine);
 			fragment.append(";\n");
 		}
 
 		this.importsFragment = fragment.toString().trim();
 	}
 
 	/**
 	 * Builds the string used to declare superclass and implemented interfaces.
 	 *
 	 * @param dependencyResolver DependencyResolver to use for deciding if/how an item gets imported.
 	 */
 	protected void buildPolymorphicsFragment(DependencyResolver dependencyResolver) {
 
 		String fragment = "";
 
 		for (As3Dependency dependency : dependencies)
 			if (dependency.getDependencyKind() == DependencyKind.SUPERCLASS && dependencyResolver.shouldResolve(packageName, dependency)) {
 				fragment += " extends " + dependency.getSimpleName() + "";
 				break;
 			}
 
 		for (As3Dependency dependency : dependencies) {
 			int count = 0;
 			if (dependency.getDependencyKind() == DependencyKind.INTERFACE && dependencyResolver.shouldResolve(packageName, dependency)) {
 				count++;
 				if (count == 1)
 					fragment += " implements ";
 				if (count > 1)
 					fragment += ", ";
 				fragment += dependency.getSimpleName();
 			}
 		}
 
 		if (fragment != null && fragment.length() > 0) {
 			fragment = fragment.trim();
 			fragment = fragment.replaceAll("  ", " ");
 		}
 
 		this.polymorphicsFragment = fragment;
 
 	}
 
 	//
 	// Getters and Setters
 	//
 
 	public String getPackageName() {
 		return packageName;
 	}
 
 	public String getQualifiedName() {
 		return qualifiedName;
 	}
 
 	public String getSimpleName() {
 		return simpleName;
 	}
 
 	public String getImportsFragment() {
 		return importsFragment;
 	}
 
 	public String getPolymorphicsFragment() {
 		return polymorphicsFragment;
 	}
 
 	public List<As3Property> getProperties() {
 		return this.properties;
 	}
 
 }
