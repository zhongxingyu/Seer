 /*******************************************************************************
  * Copyright (c) 2012 Jens von Pilgrim
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Jens von Pilgrim - initial API and implementation
  **************************************************************************** */
 package de.jevopi.mitra2.validation;
 
 import static de.jevopi.mitra2.context.ProjectContext.hasContext;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.xtext.validation.Check;
 
 import de.jevopi.mitra2.MitraError;
 import de.jevopi.mitra2.MitraUtils;
 import de.jevopi.mitra2.context.ProjectContext;
 import de.jevopi.mitra2.metamodel.IMetamodel;
 import de.jevopi.mitra2.metamodel.MetamodelManager;
 import de.jevopi.mitra2.mitra.CollectionType;
 import de.jevopi.mitra2.mitra.Expression;
 import de.jevopi.mitra2.mitra.FormalParameter;
 import de.jevopi.mitra2.mitra.LocalVariableDeclaration;
 import de.jevopi.mitra2.mitra.MetamodelDeclaration;
 import de.jevopi.mitra2.mitra.MitraPackage;
 import de.jevopi.mitra2.mitra.Module;
 import de.jevopi.mitra2.mitra.Parameter;
 import de.jevopi.mitra2.mitra.RuleDeclaration;
 import de.jevopi.mitra2.mitra.StatementExpression;
 import de.jevopi.mitra2.mitra.VarDeclaration;
 
 public class MitraJavaValidator extends AbstractMitraJavaValidator {
 
 	@Check
 	public void checkModule(Module module) {
 		if (!hasContext(module)) return;
 		Resource resource = module.eResource();
 		if (resource != null) {
			String resourceName = resource.getURI().lastSegment();
			String moduleNameWithExt = module.getName() + ".mitra";
			if (!resourceName.equals(moduleNameWithExt)) {
 				error("Module and resource name must be equal", module, module
 					.eClass().getEStructuralFeature(MitraPackage.MODULE__NAME),
 						-1);
 			}
 		}
 		if (!Character.isUpperCase(module.getName().charAt(0))) {
 			warning("Module name should start with a capital", module, module
 				.eClass().getEStructuralFeature(MitraPackage.MODULE__NAME), -1);
 		}
 	}
 
 	@Check
 	public void checkMetamodelDeclaration(
 			MetamodelDeclaration metamodelDeclaration) {
 		if (!hasContext(metamodelDeclaration)) return;
 
 		MetamodelManager metamodelManager =
 			getMetamodelManager(metamodelDeclaration);
 		try {
 			IMetamodel metamodel =
 				metamodelManager.getMetamodel(metamodelDeclaration);
 			if (metamodel == null) {
 				error("Cannot load metamodel", metamodelDeclaration,
 						metamodelDeclaration.eContainingFeature(), -1);
 			}
 		} catch (Exception ex) {
 			error(ex.getMessage(), metamodelDeclaration,
 					metamodelDeclaration.eContainingFeature(), -1);
 		}
 	}
 
 	/**
 	 * @param eobj
 	 * @return
 	 */
 	protected MetamodelManager getMetamodelManager(EObject eobj) {
 		ProjectContext context = ProjectContext.getProjectContext(eobj);
 		MetamodelManager metamodelManager = context.getMetamodelManager();
 		return metamodelManager;
 	}
 
 	@Check
 	public void check(Parameter parameter) {
 		if (parameter.getVardecl().getType() instanceof CollectionType
 			&& MitraUtils.retrieveContainingRule(parameter).isTraced()) {
 
 			error("Cannot trace rules with collection type parameters",
 					parameter, parameter.eContainingFeature(), -1);
 		}
 	}
 
 	/**
 	 * Checks sub classes Assignment, ClassInstaneCreationExpression,
 	 * RuleInvocation, RuleInvocationSuper, StaticAcces, and VariableAccess
 	 * 
 	 * @param statementExpression
 	 */
 	@Check
 	public void check(StatementExpression statementExpression) {
 		doValidate(statementExpression);
 	}
 
 	@Check
 	public void check(Expression expression) {
 		doValidate(expression);
 	}
 
 	@Check
 	public void check(LocalVariableDeclaration localVariableDeclaration) {
 		if (!hasContext(localVariableDeclaration)) return;
 
 		doValidate(localVariableDeclaration);
 		FlowValidator flowValidator =
 			new FlowValidator(
 				ProjectContext.getProjectContext(localVariableDeclaration),
 				getMessageAcceptor());
 		flowValidator.validateLocalVariable(localVariableDeclaration);
 	}
 
 	@Check
 	public void check(VarDeclaration varDeclaration) {
 		doValidate(varDeclaration);
 	}
 
 	/**
 	 * @param assignment
 	 */
 	protected void doValidate(EObject eobject) {
 		if (!hasContext(eobject)) return;
 
 		TypeValidator validator =
 			new TypeValidator(ProjectContext.getProjectContext(eobject),
 				getMessageAcceptor());
 		try {
 			validator.visit(eobject);
 		} catch (Throwable ex) {
 
 			if (!(ex instanceof MitraError)) {
 				ex.printStackTrace();
 				error("Validation error: " + ex, eobject, eobject
 					.eContainingFeature(), -1);
 			}
 		}
 	}
 }
