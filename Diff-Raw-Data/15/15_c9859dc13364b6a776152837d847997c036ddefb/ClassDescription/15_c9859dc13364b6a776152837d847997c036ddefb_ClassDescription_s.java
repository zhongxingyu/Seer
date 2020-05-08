 package com.mutation.runner;
 
 import java.util.Set;
 
 import com.mutation.runner.utililties.ToStringUtils;
 
 public class ClassDescription {
 
 	private String className;
 
 	private String packageName;
 
 	private String qualifiedClassName;
 
 	private String classFile;
 
 	private String sourceFile;
 
 	private Set<String> associatedTestNames;
 
 	public String getClassFile() {
 		return classFile;
 	}
 
 	public void setClassFile(String classFile) {
 		this.classFile = classFile;
 	}
 
 	/**
 	 * 
 	 * @return local class name withou package name
 	 */
 	public String getClassName() {
 		return className;
 	}
 
 	public void setClassName(String className) {
 		this.className = className;
		if (getPackageName() == null)
			setQualifiedClassName(className);
 		else
			setQualifiedClassName(getPackageName() + "." + className);
 	}
 
 	@Override
 	public String toString() {
 		return ToStringUtils.toString(this);
 	}
 
 	public String getPackageName() {
 		return packageName;
 	}
 
 	public void setPackageName(String packageName) {
 		this.packageName = packageName;
 
		if (packageName == null)
			setQualifiedClassName(getClassName());
 		else
			setQualifiedClassName(packageName + "." + getClassName());
 	}
 
 	public Set<String> getAssociatedTestNames() {
 		return associatedTestNames;
 	}
 
 	public void setAssociatedTestNames(Set<String> associatedTestNames) {
 		this.associatedTestNames = associatedTestNames;
 	}
 
 	public String getSourceFile() {
 		return sourceFile;
 	}
 
 	public void setSourceFile(String sourceFile) {
 		this.sourceFile = sourceFile;
 	}
 
 	public String getQualifiedClassName() {
 		return qualifiedClassName;
 	}
 
 	public void setQualifiedClassName(String qualifiedName) {
 		this.qualifiedClassName = qualifiedName;
 	}
 }
