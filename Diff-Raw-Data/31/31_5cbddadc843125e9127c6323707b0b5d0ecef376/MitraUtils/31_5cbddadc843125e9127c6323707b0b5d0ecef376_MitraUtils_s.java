 /*******************************************************************************
  * Copyright (c) 2012 Jens von Pilgrim
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Jens von Pilgrim - initial API and implementation
  ******************************************************************************/
 
 package de.jevopi.mitra2;
 
 import static java.util.Collections.emptyList;
 import static java.util.Collections.emptyMap;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.xtext.EcoreUtil2;
 import org.eclipse.xtext.naming.QualifiedName;
 
 import com.google.common.base.Function;
 
 import de.jevopi.mitra2.context.PathHandler.ResourceType;
 import de.jevopi.mitra2.context.ProjectContext;
 import de.jevopi.mitra2.metamodel.MObject;
 import de.jevopi.mitra2.mitra.CollectionType;
 import de.jevopi.mitra2.mitra.Module;
 import de.jevopi.mitra2.mitra.ModuleReference;
 import de.jevopi.mitra2.mitra.Parameter;
 import de.jevopi.mitra2.mitra.ParameterModifier;
 import de.jevopi.mitra2.mitra.PrimitiveType;
 import de.jevopi.mitra2.mitra.QualifiedParameterReference;
 import de.jevopi.mitra2.mitra.QualifiedRuleReference;
 import de.jevopi.mitra2.mitra.RealLiteral;
 import de.jevopi.mitra2.mitra.ReferenceType;
 import de.jevopi.mitra2.mitra.RuleDeclaration;
 import de.jevopi.mitra2.mitra.RuleReference;
 import de.jevopi.mitra2.mitra.SimpleParameterReference;
 import de.jevopi.mitra2.mitra.SimpleRuleReference;
 import de.jevopi.mitra2.mitra.Type;
 import de.jevopi.mitra2.mitra.VisibilityModifier;
 import de.jevopi.utils.ComposedIterable;
 import de.jevopi.xtext.utils.XtextUtils;
 
 /**
  * General purpose Mitra model utility methods.
  * 
  * @author Jens von Pilgrim (developer@jevopi.de)
  * @since Jan 16, 2012
  */
 public class MitraUtils {
 
 	public static final String FQN_SEPARATOR = ":";
 
 	/**
 	 * Retrieves the Module in which the given object is (transitively)
 	 * contained.
 	 * 
 	 * @param eobj
 	 * @return the module or null, if eobj is not contained in any module.
 	 */
 	public static Module retrieveModule(EObject eobj) {
 		if (eobj == null) return null;
 		if (eobj instanceof Module) return (Module) eobj;
 		return retrieveModule(eobj.eContainer());
 	}
 
 	/**
 	 * Finds rule declaration in which the given element was defined.
 	 * 
 	 * @param
 	 * @return the rule declaration of null, if argument is not contained in any
 	 *         rule
 	 */
 	public static RuleDeclaration retrieveContainingRule(EObject eobj) {
 		if (eobj == null) return null;
 		if (eobj instanceof RuleDeclaration) return (RuleDeclaration) eobj;
 		return retrieveContainingRule(eobj.eContainer());
 	}
 
 	/**
 	 * Returns the name of a type. Robust method, i.e., it tries to avoid
 	 * throwing an exception.
 	 * 
 	 * @param type
 	 * @return name or "null" or "*error*";
 	 */
 	public static String getTypeName(Type type) {
 		if (type == null) return "null";
 		if (type instanceof PrimitiveType) {
 			return ((PrimitiveType) type).getPrimitiveType().getName();
 		}
 		if (type instanceof ReferenceType) {
 			if (((ReferenceType) type).getEClassifier() == null) {
 				return "null";
 			}
 			return ((ReferenceType) type).getEClassifier().getName();
 		}
 		if (type instanceof CollectionType) {
 			return ((CollectionType) type).getCollectionType().getName();
 		}
 		return "*error*";
 	}
 
 	/**
 	 * Returns a composed list of types of formal and return parameters.
 	 * 
 	 * @param ruleReference
 	 * @return
 	 */
 	public static List<Type> retrieveRuleRefParameterTypes(
 			RuleReference ruleReference) {
 		List<Type> types = new ArrayList<Type>();
 		if (ruleReference instanceof SimpleRuleReference) {
 			SimpleRuleReference simpleRuleReference =
 				(SimpleRuleReference) ruleReference;
 			for (SimpleParameterReference spr : simpleRuleReference
 				.getParameterReferences()) {
 				types.add(spr.getType());
 			}
 			for (SimpleParameterReference spr : simpleRuleReference
 				.getReturnReferences()) {
 				types.add(spr.getType());
 			}
 		} else {
 			QualifiedRuleReference qualifiedRuleReference =
 				(QualifiedRuleReference) ruleReference;
 			for (QualifiedParameterReference qpr : qualifiedRuleReference
 				.getParameterReferences()) {
 				types.add(qpr.getVardecl().getType());
 			}
 			for (QualifiedParameterReference qpr : qualifiedRuleReference
 				.getReturnReferences()) {
 				types.add(qpr.getVardecl().getType());
 			}
 		}
 		return types;
 	}
 
 	/**
 	 * Returns a composed list of names of formal and return parameters. In case
 	 * of simple rule references, the parameter references may not specify a
 	 * name themselves. In these cases, the name of the parameter as defined in
 	 * the referenced rule declaration is used, if
 	 * <code>fallBackOnDeclaredNames</code> is true.
 	 * 
 	 * @param ruleReference
 	 * @param fallBackOnDeclaredNames
 	 * @return
 	 */
 	public static List<String> retrieveRuleRefParameterNames(
 			RuleReference ruleReference, boolean fallBackOnDeclaredNames) {
 		List<String> names = new ArrayList<String>();
 		if (ruleReference instanceof SimpleRuleReference) {
 			RuleDeclaration ruleDeclaration =
 				ruleReference.getRuleDeclaration();
 			SimpleRuleReference simpleRuleReference =
 				(SimpleRuleReference) ruleReference;
 			String name;
 			int i = 0;
 			for (SimpleParameterReference spr : simpleRuleReference
 				.getParameterReferences()) {
 				name = spr.getName();
 				if (name == null && fallBackOnDeclaredNames) {
 					name =
 						ruleDeclaration.getFormalParameters().get(i)
 							.getVardecl().getName();
 				}
 				names.add(name);
 				i++;
 			}
 			i = 0;
 			for (SimpleParameterReference spr : simpleRuleReference
 				.getReturnReferences()) {
 				name = spr.getName();
 				if (name == null && fallBackOnDeclaredNames) {
 					name =
 						ruleDeclaration.getReturnParameters().get(i)
 							.getVardecl().getName();
 				}
 				names.add(name);
 				i++;
 			}
 		} else {
 			QualifiedRuleReference qualifiedRuleReference =
 				(QualifiedRuleReference) ruleReference;
 			for (QualifiedParameterReference qpr : qualifiedRuleReference
 				.getParameterReferences()) {
 				names.add(qpr.getVardecl().getName());
 			}
 			for (QualifiedParameterReference qpr : qualifiedRuleReference
 				.getReturnReferences()) {
 				names.add(qpr.getVardecl().getName());
 			}
 		}
 		return names;
 	}
 
 	/**
 	 * @param i_referer
 	 * @return
 	 */
 	public static List<RuleDeclaration> retrieveVisibleRules(EObject i_referer) {
 		Module module = retrieveModule(i_referer);
 		List<RuleDeclaration> rules = new ArrayList<RuleDeclaration>();
 		rules.addAll(module.getRuleDeclarations());
 		for (ModuleReference moduleReference : module.getImports()) {
 			for (RuleDeclaration ruleInImport : moduleReference.getModule()
 				.getRuleDeclarations()) {
 				if (ruleInImport.getVisibility() == VisibilityModifier.PUBLIC) {
 					rules.add(ruleInImport);
 				}
 			}
 		}
 		return rules;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static Iterable<? extends Parameter> collectRuleParameters(
 			RuleDeclaration ruleDeclaration) {
 		if (ruleDeclaration==null) {
 			return emptyList();
 		}
 		return ComposedIterable.compose(ruleDeclaration.getFormalParameters(),
 				ruleDeclaration.getReturnParameters());
 	}
 
 	public static List<SimpleRuleReference> collectOverriddenRules(
 			RuleDeclaration ruleDeclaration) {
 		if (ruleDeclaration.getOverriddenRule() == null) {
 			return Collections.emptyList();
 		}
 		
 		SimpleRuleReference overridden = ruleDeclaration.getOverriddenRule();
 		List<SimpleRuleReference> overriddenRules =
 			new ArrayList<SimpleRuleReference>(2);
 		overriddenRules.add(overridden);
 		overriddenRules.addAll(collectOverriddenRules(overridden
 			.getRuleDeclaration()));
 		return overriddenRules;
 
 	}
 
 	public static List<SimpleRuleReference> collectImplementedRules(
 			RuleDeclaration ruleDeclaration) {
 		if (ruleDeclaration.getImplementedRules() == null
 			|| ruleDeclaration.getImplementedRules().isEmpty()) {
 			return Collections.emptyList();
 		} else {
 			List<SimpleRuleReference> implementedRules =
 				new ArrayList<SimpleRuleReference>(2);
 			implementedRules.addAll(ruleDeclaration.getImplementedRules());
 			for (RuleReference implementedRule : implementedRules) {
 				implementedRules.addAll(collectImplementedRules(implementedRule
 					.getRuleDeclaration()));
 			}
 			return implementedRules;
 		}
 	}
 
 	public static float toFloat(RealLiteral realLiteral) {
 		String s = realLiteral.getFloatValue();
 		return Float.parseFloat(s);
 	}
 
 	/**
 	 * @param i_type
 	 * @return
 	 * @since Feb 3, 2012
 	 */
 	public static String getSimpleTypeName(Type i_type) {
 		if (i_type instanceof PrimitiveType) {
 			return ((PrimitiveType) i_type).getPrimitiveType().getLiteral();
 		} else if (i_type instanceof CollectionType) {
 			return getSimpleTypeName(((CollectionType) i_type).getTypePar())
 				+ "s";
 		} else if (i_type instanceof ReferenceType) {
 			return ((ReferenceType) i_type).getEClassifier().getName();
 		}
 		throw new IllegalArgumentException("Type " + i_type + " unknown");
 	}
 
 	/**
 	 * @param i_arguments
 	 * @return
 	 */
 	public static List<Type> getTypes(List<MObject> i_arguments) {
 		ArrayList<Type> argTypes = new ArrayList<Type>(i_arguments.size());
 		for (MObject arg : i_arguments) {
 			argTypes.add(arg.getActualType());
 		}
 		return argTypes;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T> T
 			loadModel(ProjectContext projectContext, String fileName)
 					throws Exception {
 		URI uri =
 			projectContext.getPathHandler().resolve(ResourceType.MODEL,
 					fileName);
 		Resource resource = projectContext.getResourceSet().createResource(uri);
 		resource.load(emptyMap());
 		return (T) resource.getContents().get(0);
 	}
 
 	public static Module loadModule(ProjectContext projectContext,
 			String fqnModuleName) throws Exception {
 		String modulePathName = convertFQNToPathName(fqnModuleName);
 		URI uri =
 			projectContext.getPathHandler().resolve(ResourceType.MODULE,
 					modulePathName);
 		Resource resource = projectContext.getResourceSet().createResource(uri);
 		resource.load(emptyMap());
 		Module module = (Module) resource.getContents().get(0);
 		EcoreUtil.resolveAll(module);
 		Diagnostic diagnostic = Diagnostician.INSTANCE.validate(module);
 		if (diagnostic.getSeverity() == Diagnostic.ERROR) {
 			throw new Exception(diagnostic.getMessage());
 		}
 		return module;
 
 	}
 
 	public static String convertFQNToPathName(String fqnModuleName) {
		return fqnModuleName.replaceAll(FQN_SEPARATOR, File.separator)
 			+ ".mitra";
 	}
 
 	public static String getFQModuleName(String fqnRule) {
		int pos = fqnRule.lastIndexOf(FQN_SEPARATOR);
 		if (pos <= 0)
 			throw new IllegalArgumentException(
 				"Qualified name does not contains a module name: " + fqnRule);
 		return fqnRule.substring(0, pos);
 	}
 
 	public static String getSimpleRuleName(String fqnRule) {
		int pos = fqnRule.lastIndexOf(FQN_SEPARATOR);
 		if (pos <= 0) return fqnRule; // name is already simple
 		return fqnRule.substring(pos + 1);
 	}
 
 	public static String fqn(RuleDeclaration ruleDeclaration) {
 		return MitraNameProvider.INSTANCE
 			.getFullyQualifiedName(ruleDeclaration).toString();
 	}
 
 	public static String fqn(RuleReference ruleReference) {
 		return MitraNameProvider.INSTANCE.getFullyQualifiedName(ruleReference)
 			.toString();
 	}
 
 	public static String fqn(Module module) {
 		return fullyQualifiedName(module).toString();
 	}
 
 	public static QualifiedName fullyQualifiedName(Module module) {
 		return MitraNameProvider.INSTANCE.getFullyQualifiedName(module);
 	}
 
 	public static boolean isFqnRulename(String name) {
 		return name != null && name.contains(FQN_SEPARATOR);
 	}
 
 	// Xtext 1.0
 	/*
 	public static class ModuleToFQNFunction implements Function<Module, String> {
 
 		@Override
 		public String apply(Module from) {
 			return fqn(from);
 		}
 
 	}
 	*/
 
 	public static class ModuleToFQNFunction implements
 			Function<Module, QualifiedName> {
 
 		@Override
 		public QualifiedName apply(Module from) {
 			return fullyQualifiedName(from);
 		}
 
 	}
 
 	/**
 	 * Returns true if rule, a super rule or an implemented rule is traced.
 	 * 
 	 * @param ruleDeclaration
 	 * @return
 	 */
 	public static boolean isTraced(RuleDeclaration ruleDeclaration) {
 		if (ruleDeclaration == null) return false;
 		if (ruleDeclaration.isTraced()) return true;
 		if (ruleDeclaration.getOverriddenRule() != null) {
 			// transitive:
 			if (isTraced(ruleDeclaration.getOverriddenRule()
 				.getRuleDeclaration())) return true;
 		}
 		for (SimpleRuleReference ruleRef : ruleDeclaration
 			.getImplementedRules()) {
 			if (isTraced(ruleRef.getRuleDeclaration())) {
 				return true;
 			}
 		}
 		return false;
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <PT extends Parameter> PT getParameterByName(
 			RuleDeclaration ruleDeclaration, String parName) {
 		for (Parameter p : collectRuleParameters(ruleDeclaration)) {
 			if (p.getVardecl().getName().equals(parName)) return (PT) p;
 		}
 		return null;
 	}
 
 	public static boolean hasParameterOfKind(RuleDeclaration ruleDeclaration,
 			ParameterModifier parameterKind) {
 		for (Parameter par : collectRuleParameters(ruleDeclaration)) {
 			if (par.getModifier() == parameterKind) return true;
 
 		}
 		return false;
 	}
 
 	public static String[] guessFileExtensions(Type type) {
 		String[] extensions;
 		int index = 0;
 		if (type instanceof ReferenceType) {
 			ReferenceType referenceType = (ReferenceType) type;
 
 			// Xtext 1.x:
 			// EcoreUtil2.resolveAll(referenceType, new CancelIndicator.NullImpl());
 
 			// Xtext 2.3:
 			EcoreUtil2.resolveAll(referenceType,
 					XtextUtils.getCancelIndicator());
 
 			EClassifier eClassifier = referenceType.getEClassifier();
 			if (eClassifier != null && eClassifier.getEPackage() != null) {
 				EPackage ePackage = eClassifier.getEPackage();
 				extensions = new String[3];
 				extensions[index++] = "*." + ePackage.getName().toLowerCase();
 			} else {
 				extensions = new String[2];
 			}
 			extensions[index++] = "*.xmi;*.xml";
 
 		} else {
 			extensions = new String[1];
 		}
 		extensions[index] = "*.*";
 		return extensions;
 	}
 }
