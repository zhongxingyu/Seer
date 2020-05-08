 /*
  * ClassModel.java (JMetricVis)
  * Copyright 2011 Luke Plaster. All rights reserved.
  */
 package org.lukep.javavis.program.generic.models;
 
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Vector;
 
 import org.lukep.javavis.metrics.IMeasurableVisitor;
 import org.lukep.javavis.metrics.MetricAttribute;
 import org.lukep.javavis.metrics.MetricMeasurement;
 import org.lukep.javavis.metrics.MetricRegistry;
 import org.lukep.javavis.program.generic.models.Relationship.RelationshipType;
 import org.lukep.javavis.util.JavaVisConstants;
 
 public class ClassModel extends AbstractModel {
 	
 	protected String superClassName;
 	protected int methodCount = 0;
 	protected int variableCount = 0;
 	protected MethodModel constructorMethod;
 	protected LinkedList<ClassAncestor> ancestors = new LinkedList<ClassAncestor>();
 
 	public ClassModel(AbstractModelSourceLang lang, String simpleName, String qualifiedName) {
 		super(lang, JavaVisConstants.METRIC_APPLIES_TO_CLASS);
 		this.simpleName = simpleName;
 		this.qualifiedName = qualifiedName;
 	}
 	
 	///////////////////////////////////////////////////////
 	
 	@Override
 	public MetricMeasurement accept(MetricAttribute metric, IMeasurableVisitor visitor) {
 		return visitor.visit(metric, this);
 	}
 	
 	///////////////////////////////////////////////////////
 	
 	public int getTotalNumberOfStatements() {
 		int totalStatements = 0;
 		MetricMeasurement result;
 		
 		Vector<MethodModel> methods = getMethods();
 		if (methods != null) {
 			for (MethodModel method : methods) {
 				result = method.getMetricMeasurement(
 							MetricRegistry.getInstance().getMetricAttribute(
 								JavaVisConstants.METRIC_NUM_OF_STATEMENTS));
 				assert(result != null);
 				totalStatements += result.getResult();
 			}
 		}
 		
 		return totalStatements;
 	}
 	
 	public float getAvgCyclomaticComplexity() {
 		int count = 0;
 		float avgComplexity = 0;
 		MetricMeasurement result;
 		
 		Vector<MethodModel> methods = getMethods();
 		if (methods != null) {
 			for (MethodModel method : methods) {
 					result = method.getMetricMeasurement(
 								MetricRegistry.getInstance().getMetricAttribute(
 									JavaVisConstants.METRIC_CYCLO_COMPLEX));
 					assert(result != null);
 					avgComplexity += result.getResult();
 					count++;
 			}
 			if (count > 0)
 				avgComplexity /= count;
 		}
 		
 		return avgComplexity;
 	}
 	
 	public double getMaxCyclomaticComplexity() {
 		double maxComplexity = 0;
 		MetricMeasurement result;
 		
 		Vector<MethodModel> methods = getMethods();
 		if (methods != null) {
 			for (MethodModel method : methods) {
 					result = method.getMetricMeasurement(
 								MetricRegistry.getInstance().getMetricAttribute(
 									JavaVisConstants.METRIC_CYCLO_COMPLEX));
 					assert(result != null);
 					if (result.getResult() > maxComplexity)
 						maxComplexity = result.getResult();
 			}
 		}
 		
 		return maxComplexity;
 	}
 	
 	///////////////////////////////////////////////////////
 
 	@Override
 	public String getContainerName() {
 		int lastDotIndex = qualifiedName.lastIndexOf('.');
 		if (qualifiedName.length() > 0 && lastDotIndex != -1) // class is member of a named package (non-default)
 			return qualifiedName.substring(0, lastDotIndex);
 		else
 			return JavaVisConstants.DEFAULT_PACKAGE_NAME;
 	}
 	
 	///////////////////////////////////////////////////////
 
 	public String getSuperClassName() {
 		return superClassName;
 	}
 
 	public void setSuperClassName(String parentName) {
 		this.superClassName = parentName;
 	}
 	
 	public MethodModel getConstructorMethod() {
 		return constructorMethod;
 	}
 
 	public void setConstructorMethod(MethodModel constructorMethod) {
 		this.constructorMethod = constructorMethod;
 	}
 
 	public void addAncestor(ClassAncestor ancestor) {
 		this.ancestors.addFirst(ancestor);
 	}
 	
 	public Queue<ClassAncestor> getAncestors() {
 		return ancestors;
 	}
 	
 	public int getAncestorCount() {
 		return ancestors.size();
 	}
 	
 	public int getInheritedFieldCount() {
 		int inheritedFields = 0;
 		for (ClassAncestor ancestor : ancestors)
 			inheritedFields += ancestor.getDeclaredFields();
 		return inheritedFields;
 	}
 	
 	public int getInheritedMethodCount() {
 		int inheritedMethods = 0;
 		for (ClassAncestor ancestor : ancestors)
 			inheritedMethods += ancestor.getDeclaredMethods();
 		return inheritedMethods;
 	}
 
 	public void addMethod(MethodModel method) {
 		addChild(method, RelationshipType.ENCLOSED_IN);
 		methodCount++;
 	}
 	
 	public Vector<MethodModel> getMethods() {
 		if (children != null) {
 			Vector<MethodModel> methods = new Vector<MethodModel>(children.size());
 			for (Relationship m : children)
 				if (m.target instanceof MethodModel)
 					methods.add((MethodModel) m.target);
 			return methods;
 		}
		return null;
 	}
 	
 	public int getMethodCount() {
 		return methodCount;
 	}
 	
 	public void addVariable(VariableModel variable) {
 		addChild(variable, RelationshipType.MEMBER_OF);
 		variableCount++;
 	}
 	
 	public Vector<VariableModel> getVariables() {
 		if (children != null) {
 			Vector<VariableModel> variables = new Vector<VariableModel>(children.size());
 			for (Relationship m : children)
 				if (m.target instanceof VariableModel)
 					variables.add((VariableModel) m.target);
 			return variables;
 		}
		return null;
 	}
 	
 	public int getVariableCount() {
 		return variableCount;
 	}
 
 	@Override
 	public String toString() {
 		return simpleName;
 	}
 	
 }
