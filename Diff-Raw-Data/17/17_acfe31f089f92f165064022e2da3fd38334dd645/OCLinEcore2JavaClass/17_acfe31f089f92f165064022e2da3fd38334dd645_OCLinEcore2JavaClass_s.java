 /**
  * <copyright>
  *
  * Copyright (c) 2011,2012 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  **/
 package org.eclipse.ocl.examples.codegen.expression;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.codegen.ecore.genmodel.GenClassifier;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jdt.annotation.Nullable;
 import org.eclipse.ocl.examples.codegen.analyzer.CodeGenAnalysis;
 import org.eclipse.ocl.examples.codegen.analyzer.CodeGenAnalyzer;
 import org.eclipse.ocl.examples.codegen.common.PivotQueries;
 import org.eclipse.ocl.examples.codegen.generator.CodeGenSnippet;
 import org.eclipse.ocl.examples.codegen.generator.CodeGenText;
 import org.eclipse.ocl.examples.codegen.generator.java.JavaCodeGenerator;
 import org.eclipse.ocl.examples.domain.utilities.DomainUtil;
 import org.eclipse.ocl.examples.domain.values.util.ValuesUtil;
 import org.eclipse.ocl.examples.pivot.Constraint;
 import org.eclipse.ocl.examples.pivot.Element;
 import org.eclipse.ocl.examples.pivot.ExpressionInOCL;
 import org.eclipse.ocl.examples.pivot.Feature;
 import org.eclipse.ocl.examples.pivot.NamedElement;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.Property;
 import org.eclipse.ocl.examples.pivot.ValueSpecification;
 import org.eclipse.ocl.examples.pivot.Variable;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 
 /**
  * OCL2JavaClass supports generation of the content of a JavaClassFile to provide the polymorphic implementation
  * of an ExpressionInOCL.
  */
 public class OCLinEcore2JavaClass extends JavaCodeGenerator
 {
 	public static final @NonNull String INSTANCE_NAME = "INSTANCE";
 
 	protected final @NonNull CodeGenAnalyzer cgAnalyzer;
 	protected final @NonNull EClassifier eClassifier;
 	protected final @NonNull CodeGenSnippet fileSnippet = createCodeGenSnippet("", CodeGenSnippet.LIVE);
 
 	private static @NonNull Comparator<? super NamedElement> nameComparator = new Comparator<NamedElement>(){
 
 		public int compare(NamedElement o1, NamedElement o2) {
 			String n1 = String.valueOf(o1.getName());
 			String n2 = String.valueOf(o2.getName());
 			return n1.compareTo(n2);
 		}
 	};
 
 	public OCLinEcore2JavaClass(@NonNull MetaModelManager metaModelManager, @NonNull EClassifier eClassifier) {
 		super(metaModelManager);
 		cgAnalyzer = new CodeGenAnalyzer(this);
 		this.eClassifier = eClassifier;
 		nameManager.reserveName(INSTANCE_NAME, null);
 	}
 
 	private void activateGuards(@NonNull CodeGenAnalysis analysis) {
 		Set<CodeGenAnalysis> invalidGuards = analysis.getInvalidGuards();
 		if (invalidGuards != null) {
 			for (CodeGenAnalysis invalidGuard : invalidGuards) {
 				getSnippet(invalidGuard.getExpression()).getName();
 			}
 		}
 		Set<CodeGenAnalysis> nullGuards = analysis.getNullGuards();
 		if (nullGuards != null) {
 			for (CodeGenAnalysis nullGuard : nullGuards) {
 				getSnippet(nullGuard.getExpression()).getName();
 			}
 		}
 		CodeGenAnalysis[] children = analysis.getChildren();
 		if (children != null) {
 			for (CodeGenAnalysis child : children) {
 				assert child != null;
 				activateGuards(child);
 			}
 		}
 	}
 
 	public @Nullable CodeGenAnalysis findAnalysis(@NonNull Element element) {
 		return cgAnalyzer.findAnalysis(element);
 	}
 
 	protected void generateInnerClass(@NonNull CodeGenSnippet innerClassSnippet, @NonNull String className, @NonNull String title, @NonNull ExpressionInOCL expression, @Nullable Feature feature) {
 //		System.out.println("innerClass " + className);
 		boolean isRequired = (feature == null) || feature.isRequired();
 		Class<?> returnClass = feature != null ? getBoxedClass(feature.getTypeId()) : Boolean.class;
 		push();
 		CodeGenAnalysis rootAnalysis = cgAnalyzer.analyze(expression, isRequired);
 		cgAnalyzer.optimize(rootAnalysis);
 		List<Variable> parameterVariable = DomainUtil.nonNullEMF(expression.getParameterVariable());
 		Class<?> baseClass = genModelHelper.getAbstractOperationClass(parameterVariable);
 		CodeGenText classDefinition = innerClassSnippet.appendIndentedText("");
 		classDefinition.appendCommentWithOCL(title, expression);
 		classDefinition.append("public static class " + className + " extends ");
 		classDefinition.appendClassReference(baseClass);
 		classDefinition.append("\n");
 		classDefinition.append("{\n");
 		//
 		//	Inner Class statics
 		//
 		CodeGenSnippet innerStatics = innerClassSnippet.appendIndentedNodes(null, CodeGenSnippet.LIVE);
 		innerStatics.append("public static final " + innerClassSnippet.atNonNull() + " " + className + " " + INSTANCE_NAME + " = new " + className + "();\n");
 //		getSnippetLabel(GLOBAL_ROOT).push(globalRoot);
 		innerClassSnippet.append("\n");
 		generateEvaluateFunction(innerClassSnippet, returnClass, isRequired, expression);
 		innerClassSnippet.append("}\n");
 		activateGuards(rootAnalysis);
 		pop();
 	}
 
 	protected void generateOuterClassDefinition(@NonNull GenClassifier genClassifier, @NonNull String className, @Nullable Class<?> baseClass, @NonNull org.eclipse.ocl.examples.pivot.Class pivotClass) {
 //		@NonNull EClassifier eClassifier = genClassifier.getEcoreClassifier();
 //		Element element = expInOcl;
 //		String outerTitle = className + " provides the Java implementation for\n";
 		CodeGenText classDefinition = fileSnippet.appendIndentedText("");
 //		classDefinition.appendCommentWithOCL(title, element);
 		classDefinition.append("@SuppressWarnings(\"nls\")\n");
 		classDefinition.append("public class " + className);
 		if (baseClass != null) {
 			classDefinition.append(" extends ");
 			classDefinition.appendClassReference(baseClass);
 		}
 		classDefinition.append("\n");
 		classDefinition.append("{\n");
 		//
 		//	Outer class statics
 		//
 		CodeGenSnippet globalRoot = fileSnippet.appendIndentedNodes(null, CodeGenSnippet.LIVE);
 //		globalRoot.append("public static " + fileSnippet.atNonNull() + " " + className + " " + instanceName + " = new " + className + "();\n");
 		getSnippetLabel(GLOBAL_ROOT).push(globalRoot);
 		//
 		//	Class invariants
 		//
 		List<Constraint> ownedRules = new ArrayList<Constraint>(pivotClass.getOwnedRule());
 		Collections.sort(ownedRules, nameComparator);
 		for (Constraint constraint : ownedRules) {
 			ValueSpecification specification = DomainUtil.nonNullModel(constraint.getSpecification());
 			ExpressionInOCL expression = PivotQueries.getExpressionInOCL(pivotClass, specification);
 			if ((expression != null) && (expression.getContextVariable() != null)) {
 				fileSnippet.append("\n");
 				CodeGenSnippet innerClassSnippet = fileSnippet.appendIndentedNodes(null, 0);
				String innerClassName = "_" + constraint.getStereotype() +  "_" + constraint.getName();
				String innerTitle = "Implementation of the " + pivotClass.getName() + " '" + constraint.getName() + "' <" + constraint.getStereotype() + ">\n";
 				generateInnerClass(innerClassSnippet, innerClassName, innerTitle, expression, null);
 			}
 		}
 		//
 		//	Operation bodies
 		//
 		List<Operation> ownedOperations = new ArrayList<Operation>(PivotQueries.getOperations(pivotClass));
 		Collections.sort(ownedOperations, nameComparator);
 		for (Operation anOperation : ownedOperations) {
 			assert anOperation != null;
 			for (Constraint constraint : anOperation.getOwnedRule()) {
 				ValueSpecification specification = DomainUtil.nonNullModel(constraint.getSpecification());
 				ExpressionInOCL expression = PivotQueries.getExpressionInOCL(anOperation, specification);
 				if ((expression != null) && (expression.getContextVariable() != null)) {
 					fileSnippet.append("\n");
 					CodeGenSnippet innerClassSnippet = fileSnippet.appendIndentedNodes(null, 0);
 					String constraintName = constraint.getName();
 					if (constraintName == null) {
 						constraintName = "";
 					}
 					String innerClassName = "_" + anOperation.getName() +  "_" + constraint.getStereotype() +  "_" + constraintName;
 					String innerTitle = "Implementation of the " + pivotClass.getName() + "::" + anOperation.getName() + " '" + constraintName + "' <" + constraint.getStereotype() + ">\n";
 					generateInnerClass(innerClassSnippet, innerClassName, innerTitle, expression, anOperation);
 				}
 			}
 		}
 		//
 		//	Property bodies
 		//
 		List<Property> ownedProperties = new ArrayList<Property>(PivotQueries.getProperties(pivotClass));
 		Collections.sort(ownedProperties, nameComparator);
 		for (Property aProperty : ownedProperties) {
 			assert aProperty != null;
 			for (Constraint constraint : aProperty.getOwnedRule()) {
 				ValueSpecification specification = DomainUtil.nonNullModel(constraint.getSpecification());
 				ExpressionInOCL expression = PivotQueries.getExpressionInOCL(aProperty, specification);
 				if ((expression != null) && (expression.getContextVariable() != null)) {
 					fileSnippet.append("\n");
 					CodeGenSnippet innerClassSnippet = fileSnippet.appendIndentedNodes(null, 0);
					String innerClassName = "_" + aProperty.getName() +  "_" + constraint.getStereotype() +  "_" + constraint.getName();
					String innerTitle = "Implementation of the " + pivotClass.getName() + "::" + aProperty.getName() + " '" + constraint.getName() + "' <" + constraint.getStereotype() + ">\n";
 					generateInnerClass(innerClassSnippet, innerClassName, innerTitle, expression, aProperty);
 				}
 			}
 		}
 		
 		fileSnippet.append("}\n");
 	}
 
 	public @NonNull CodeGenSnippet generateClassFile(@NonNull GenClassifier genClassifier, @NonNull String packageName, @NonNull String className, @NonNull org.eclipse.ocl.examples.pivot.Class pivotClass) {
 		@SuppressWarnings("null")@NonNull GenPackage genPackage = genClassifier.getGenPackage();
 		String copyright = genPackage.getCopyright(" ");
 		fileSnippet.append("/**\n");
 		CodeGenText commentBody = fileSnippet.appendIndentedText(" *");
 		if (copyright != null) {
 			commentBody.append(copyright);
 			commentBody.append("\n");
 		}
 		commentBody.append("************************************************************************\n");
 		commentBody.append(" This code is 100% auto-generated\n");
 		commentBody.append(" using: " + getClass().getName() + "\n");
 		commentBody.append("\n");
 		commentBody.append(" Do not edit it.\n");
 		commentBody.append("/\n");
 		fileSnippet.append("package " + packageName + ";\n");
 		fileSnippet.append("\n");
 		//
 		CodeGenText importsBlock = fileSnippet.appendIndentedText("");
 		//
 		generateOuterClassDefinition(genClassifier, className, ValuesUtil.class, pivotClass);
 		//
 		resolveLiveImports(importsBlock);
 		importsBlock.append("\n");
 		return fileSnippet;
 	}
 
 	public @NonNull CodeGenAnalysis getAnalysis(@NonNull Element element) {
 		return cgAnalyzer.getAnalysis(element);
 	}
 
 	protected void resolveLiveImports(@NonNull CodeGenText importsBlock) {
 		Set<String> referencedClasses = new HashSet<String>();
 		fileSnippet.gatherLiveSnippets(new HashSet<CodeGenSnippet>(), referencedClasses);
 		List<String> allImports = new ArrayList<String>(referencedClasses);
 		Collections.sort(allImports);
 		for (String anImport : allImports) {
 			importsBlock.append("import " + anImport + ";\n");
 		}
 	}
 }
