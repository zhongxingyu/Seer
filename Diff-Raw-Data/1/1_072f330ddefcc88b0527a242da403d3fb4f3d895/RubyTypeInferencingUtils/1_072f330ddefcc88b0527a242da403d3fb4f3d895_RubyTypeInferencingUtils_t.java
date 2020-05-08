 package org.eclipse.dltk.ruby.typeinference;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.ast.expressions.Assignment;
 import org.eclipse.dltk.ast.expressions.Expression;
 import org.eclipse.dltk.ast.references.ConstantReference;
 import org.eclipse.dltk.ast.references.VariableReference;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.core.DLTKModelUtil;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.mixin.IMixinElement;
 import org.eclipse.dltk.core.mixin.IMixinRequestor;
 import org.eclipse.dltk.core.mixin.MixinModel;
 import org.eclipse.dltk.core.search.TypeNameMatch;
 import org.eclipse.dltk.core.search.TypeNameMatchRequestor;
 import org.eclipse.dltk.evaluation.types.AmbiguousType;
 import org.eclipse.dltk.evaluation.types.UnknownType;
 import org.eclipse.dltk.ruby.ast.ColonExpression;
 import org.eclipse.dltk.ruby.core.RubyPlugin;
 import org.eclipse.dltk.ruby.internal.parser.RubySourceElementParser;
 import org.eclipse.dltk.ruby.internal.parser.mixin.IRubyMixinElement;
 import org.eclipse.dltk.ruby.internal.parser.mixin.RubyMixinBuildVisitor;
 import org.eclipse.dltk.ruby.internal.parser.mixin.RubyMixinClass;
 import org.eclipse.dltk.ruby.internal.parser.mixin.RubyMixinMethod;
 import org.eclipse.dltk.ruby.internal.parser.mixin.RubyMixinModel;
 import org.eclipse.dltk.ti.IContext;
 import org.eclipse.dltk.ti.IInstanceContext;
 import org.eclipse.dltk.ti.ISourceModuleContext;
 import org.eclipse.dltk.ti.types.IEvaluatedType;
 import org.eclipse.dltk.ti.types.RecursionTypeCall;
 
 public class RubyTypeInferencingUtils {
 
 	/**
 	 * Searches all top level types, which starts with prefix
 	 */
 	public static IType[] getAllTypes(
 			org.eclipse.dltk.core.ISourceModule module, String prefix) {
 		final List types = new ArrayList();
 
 		TypeNameMatchRequestor requestor = new TypeNameMatchRequestor() {
 
 			public void acceptTypeNameMatch(TypeNameMatch match) {
 				IType type = (IType) match.getType();
 				if (type.getParent() instanceof ISourceModule) {
 					types.add(type);
 				}
 			}
 
 		};
 
 		DLTKModelUtil.searchTypeDeclarations(module.getScriptProject(), prefix
 				+ "*", requestor);
 
 		return (IType[]) types.toArray(new IType[types.size()]);
 	}
 
 	public static ASTNode[] getAllStaticScopes(ModuleDeclaration rootNode,
 			final int requestedOffset) {
 		final Collection scopes = new ArrayList();
 		ASTVisitor visitor = new OffsetTargetedASTVisitor(requestedOffset) {
 			public boolean visitInteresting(MethodDeclaration s) {
 				scopes.add(s);
 				return true;
 			}
 
 			public boolean visitInteresting(ModuleDeclaration s) {
 				scopes.add(s);
 				return true;
 			}
 
 			public boolean visitInteresting(TypeDeclaration s) {
 				scopes.add(s);
 				return true;
 			}
 			// TODO: handle Ruby blocks here
 			// XXX: what for?
 		};
 		try {
 			rootNode.traverse(visitor);
 		} catch (Exception e) {
 			RubyPlugin.log(e);
 		}
 		if (scopes.size() == 0)
 			scopes.add(rootNode);
 		return (ASTNode[]) scopes.toArray(new ASTNode[scopes.size()]);
 	}
 
 	public static IMixinElement[] getModelStaticScopes(MixinModel model,
 			ModuleDeclaration rootNode, final int requestedOffset) {
 		String[] modelStaticScopesKeys = getModelStaticScopesKeys(model,
 				rootNode, requestedOffset);
 		IMixinElement[] result = new IMixinElement[modelStaticScopesKeys.length];
 		for (int i = 0; i < modelStaticScopesKeys.length; i++) {
 			result[i] = model.get(modelStaticScopesKeys[i]);
 			if (result[i] == null)
 				throw new RuntimeException("getModelStaticScopes(): Failed to get element from mixin-model: " + modelStaticScopesKeys[i]);
 		}
 		return result;
 	}
 
 	public static String[] getModelStaticScopesKeys(MixinModel model,
 			ModuleDeclaration rootNode, final int requestedOffset) {
 		ASTNode[] allStaticScopes = RubyTypeInferencingUtils
 				.getAllStaticScopes(rootNode, requestedOffset);		
 		return RubyMixinBuildVisitor.restoreScopesByNodes(allStaticScopes);
 	}
 
 	private static String evaluateClassKey(Statement expr, List topScopes,
 			MixinModel model) {
 		if (expr instanceof ColonExpression) {
 			ColonExpression colonExpression = (ColonExpression) expr;
 			if (colonExpression.isFull()) {
 				return colonExpression.getName();
 			} else {
 				String key = evaluateClassKey(colonExpression.getLeft(),
 						topScopes, model);
 				if (key != null) {
 					return key + MixinModel.SEPARATOR
 							+ colonExpression.getName();
 				}
 			}
 		} else if (expr instanceof ConstantReference) {
 			ConstantReference constantReference = (ConstantReference) expr;
 			String name = constantReference.getName();
 			String[] top = (String[]) topScopes.toArray(new String[topScopes
 					.size()]);
 			for (int i = top.length - 1; i >= 0; i--) {
 				String pk = top[i] + MixinModel.SEPARATOR + name;
 				String[] findKeys = model.findKeys(pk);
 				if (findKeys != null && findKeys.length > 0)
 					return pk;
 			}
 			String[] findKeys = model.findKeys(name);
 			if (findKeys != null && findKeys.length > 0)
 				return name;
 		}
 		return null;
 	}
 
 	public static LocalVariableInfo findLocalVariable(
 			ModuleDeclaration rootNode, final int requestedOffset,
 			String varName) {
 		ASTNode[] staticScopes = getAllStaticScopes(rootNode, requestedOffset);
 		int end = staticScopes.length;
 		for (int start = end - 1; start >= 0; start--) {
 			ASTNode currentScope = staticScopes[start];
 			if (!isRootLocalScope(currentScope))
 				continue;
 			ASTNode nextScope = (end < staticScopes.length ? staticScopes[end]
 					: null);
 			Assignment[] assignments = findLocalVariableAssignments(
 					currentScope, nextScope, varName);
 			if (assignments.length > 0) {
 				return new LocalVariableInfo(currentScope, assignments);
 			}
 		}
 		return null;
 	}
 
 	public static IEvaluatedType determineSelfClass(IContext context,
 			int keyOffset) {
 		if (context instanceof IInstanceContext) {
 			IInstanceContext instanceContext = (IInstanceContext) context;
 			return instanceContext.getInstanceType();
 		} else {
 			ISourceModuleContext basicContext = (ISourceModuleContext) context;
 			return determineSelfClass(basicContext.getSourceModule(),
 					basicContext.getRootNode(), keyOffset);
 		}
 	}
 
 	/**
 	 * Determines a fully-qualified names of the class scope that the given
 	 * offset is statically enclosed in.
 	 * 
 	 * @param sourceModule
 	 *            a module containing the given offsets
 	 * @param rootNode
 	 *            the root of AST corresponding to the given source module
 	 * @param keyOffset
 	 *            the offset
 	 * @return The type of <code>self</code> at the given offset (never null)
 	 */
 	public static RubyClassType determineSelfClass(
 			final ISourceModule sourceModule, ModuleDeclaration rootNode,
 			final int keyOffset) {
 		RubyMixinModel rubyModel = RubyMixinModel.getInstance();
 		String[] keys = getModelStaticScopesKeys(rubyModel.getRawModel(),
 				rootNode, keyOffset);
 		if (keys != null && keys.length > 0) {
 			String inner = keys[keys.length - 1];
 			IRubyMixinElement rubyElement = rubyModel.createRubyElement(inner);
 			if (rubyElement instanceof RubyMixinMethod) {
 				RubyMixinMethod method = (RubyMixinMethod) rubyElement;
 				return new RubyClassType(method.getSelfType().getKey());
 			} else if (rubyElement instanceof RubyMixinClass) {
 				RubyMixinClass rubyMixinClass = (RubyMixinClass) rubyElement;
 				return new RubyClassType(rubyMixinClass.getKey());
 			}
 		}
 		return null;
 		// ClassInfo[] infos = resolveClassScopes(sourceModule, rootNode, new
 		// int[] { keyOffset });
 		// RubyClassType result;
 		// if (infos.length == 0)
 		// result = RubyClassType.OBJECT_CLASS;
 		// else
 		// result = (RubyClassType) infos[infos.length - 1].getEvaluatedType();
 		// ASTNode[] staticScopes = getAllStaticScopes(rootNode, keyOffset);
 		// MethodDeclaration method = null;
 		// for (int i = staticScopes.length - 1; i >= 0; i--)
 		// if (staticScopes[i] instanceof MethodDeclaration) {
 		// method = (MethodDeclaration) staticScopes[i];
 		// break;
 		// } else if (staticScopes[i] instanceof TypeDeclaration)
 		// break;
 		// RubyClassType metaType = getMetaType(result);
 		// if (method != null) {
 		// if (method instanceof RubySingletonMethodDeclaration) {
 		// RubySingletonMethodDeclaration declaration =
 		// (RubySingletonMethodDeclaration) method;
 		// Expression receiver = declaration.getReceiver();
 		// if (receiver instanceof SelfReference)
 		// return metaType; // static method
 		// if (receiver instanceof SimpleReference)
 		// if (((SimpleReference)
 		// receiver).getName().equals(result.getUnqualifiedName()))
 		// return metaType; // singleton method of our metaclass
 		// // TODO: handle singleton method of another class
 		// //return RubyMetaClassType.OBJECT_METATYPE;
 		// return new RubyClassType("Object%");
 		// }
 		// return result;
 		// }
 		// return metaType;
 	}
 
 	public static Assignment[] findLocalVariableAssignments(
 			final ASTNode scope, final ASTNode nextScope, final String varName) {
 		final Collection assignments = new ArrayList();
 		ASTVisitor visitor = new ASTVisitor() {
 
 			public boolean visit(Expression s) throws Exception {
 				if (s instanceof Assignment) {
 					Assignment assignment = (Assignment) s;
 					Expression lhs = assignment.getLeft();
 					if (lhs instanceof VariableReference) {
 						VariableReference varRef = (VariableReference) lhs;
 						if (varName.equals(varRef.getName())) {
 							assignments.add(assignment);
 						}
 					}
 				}
 				return true;
 			}
 
 			public boolean visit(MethodDeclaration s) throws Exception {
 				if (s == scope)
 					return true;
 				return false;
 			}
 
 			public boolean visit(TypeDeclaration s) throws Exception {
 				if (s == scope)
 					return true;
 				return false;
 			}
 
 			public boolean visitGeneral(ASTNode node) throws Exception {
 				if (node == nextScope)
 					return false;
 				return true;
 			}
 
 		};
 		try {
 			scope.traverse(visitor);
 		} catch (Exception e) {
 			RubyPlugin.log(e);
 		}
 		return (Assignment[]) assignments.toArray(new Assignment[assignments
 				.size()]);
 	}
 
 	public static boolean isRootLocalScope(ASTNode node) {
 		return node instanceof ModuleDeclaration
 				|| node instanceof TypeDeclaration
 				|| node instanceof MethodDeclaration;
 	}
 
 	public static IEvaluatedType combineTypes(Collection evaluaedTypes) {
 		Set types = new HashSet(evaluaedTypes);
 		types.remove(null);
 		if (types.size() > 1 && types.contains(RecursionTypeCall.INSTANCE))
 			types.remove(RecursionTypeCall.INSTANCE);
 		return combineUniqueTypes((IEvaluatedType[]) types
 				.toArray(new IEvaluatedType[types.size()]));
 	}
 
 	private static IEvaluatedType combineUniqueTypes(IEvaluatedType[] types) {
 		if (types.length == 0)
 			return UnknownType.INSTANCE;
 		if (types.length == 1)
 			return types[0];
 		return new AmbiguousType(types);
 	}
 
 	public static IEvaluatedType combineTypes(IEvaluatedType[] evaluaedTypes) {
 		return combineTypes(Arrays.asList(evaluaedTypes));
 	}
 
 	public static ModuleDeclaration parseSource(ISourceModule module) {
 		// JRubySourceParser parser = new JRubySourceParser(null);
 		// try {
 		// return parser.parse(module.getSource());
 		// } catch (ModelException e) {
 		// RubyPlugin.log(e);
 		// return null;
 		// }
 		return RubySourceElementParser.parseModule(module);
 	}
 
 	public static RubyClassType getMetaType(RubyClassType type) {
 		// TODO
 		return type;
 	}
 
 	public static IEvaluatedType getAmbiguousMetaType(IEvaluatedType receiver) {
 		if (receiver instanceof AmbiguousType) {
 			Set possibleReturns = new HashSet();
 			AmbiguousType ambiguousType = (AmbiguousType) receiver;
 			IEvaluatedType[] possibleTypes = ambiguousType.getPossibleTypes();
 			for (int i = 0; i < possibleTypes.length; i++) {
 				IEvaluatedType type = possibleTypes[i];
 				IEvaluatedType possibleReturn = getAmbiguousMetaType(type);
 				possibleReturns.add(possibleReturn);
 			}
 			return RubyTypeInferencingUtils.combineTypes(possibleReturns);
 		}
 		return null;
 	}
 
 	// public static RubyMetaClassType resolveMethods(ISourceModule module,
 	// RubyMetaClassType type) {
 	// if (type.getMethods() == null) {
 	// List result = new UniqueNamesList();
 	// if (type.getInstanceType() != null) {
 	// RubyClassType instanceType = (RubyClassType) type
 	// .getInstanceType();
 	//
 	// if (instanceType.getFQN()[0].equals("Object")) {
 	// IMethod[] topLevelMethods = RubyModelUtils
 	// .findTopLevelMethods(module, "");
 	// for (int i = 0; i < topLevelMethods.length; i++) {
 	// result.add(topLevelMethods[i]);
 	// }
 	// }
 	//
 	// IType[] types = resolveTypeDeclarations(module
 	// .getScriptProject(), instanceType);
 	// for (int i = 0; i < types.length; i++) {
 	// try {
 	// IMethod[] methods = types[i].getMethods();
 	// IType[] subtypes = types[i].getTypes();
 	//
 	// for (int j = 0; j < methods.length; j++) {
 	// if (methods[j].getElementName().startsWith("self.")) {
 	// result.add(methods[j]);
 	// }
 	// }
 	//
 	// for (int j = 0; j < subtypes.length; j++) {
 	// if (!subtypes[j].getElementName().equals("<< self"))
 	// continue;
 	// IMethod[] methods2 = subtypes[j].getMethods();
 	// for (int k = 0; k < methods2.length; k++) {
 	// int flags = methods2[k].getFlags();
 	// if ((flags & Modifiers.AccStatic) == 0) {
 	// result.add(methods2[k]);
 	// }
 	// }
 	// }
 	//
 	// if (!type.getInstanceType().getTypeName().equals(
 	// "Object")) {
 	// RubyMetaClassType superType = null;//
 	// /getMetaType(RubyModelUtils.getSuperType(types[i]));
 	// if (superType != null) {
 	// superType = resolveMethods(module, superType);
 	// IMethod[] allMethods = superType.getMethods();
 	// for (int j = 0; j < allMethods.length; j++) {
 	// result.add(allMethods[j]);
 	// }
 	// }
 	// }
 	// } catch (ModelException e) {
 	// e.printStackTrace();
 	// }
 	// }
 	// }
 	//
 	// FakeMethod[] metaMethods = RubyModelUtils.getFakeMetaMethods(
 	// (ModelElement) module.getScriptProject(), "Object");
 	// for (int j = 0; j < metaMethods.length; j++) {
 	// result.add(metaMethods[j]);
 	// }
 	// if (metaMethods != null) {
 	// return new RubyMetaClassType(type.getInstanceType(),
 	// (IMethod[]) result.toArray(new IMethod[result.size()]));
 	// }
 	// }
 	// return type;
 	// }
 
 	// public static IType[] resolveTypeDeclarations(IDLTKProject project,
 	// RubyClassType type) {
 	// String modelKey = type.getModelKey();
 	// IMixinElement mixinElement = RubyTypeInferencer.getModel()
 	// .get(modelKey);
 	// if (mixinElement == null)
 	// return new IType[0];
 	// Object[] allObjects2 = mixinElement.getAllObjects();
 	// IType[] allObjects = new IType[allObjects2.length];
 	// for (int i = 0; i < allObjects2.length; i++) {
 	// allObjects[i] = (IType) allObjects2[i];
 	// }
 	// return allObjects;
 	// }
 	//
 	// // FIXME should be in RubyClassType itself!
 	// public static RubyClassType resolveMethods(IDLTKProject project,
 	// RubyClassType type) {
 	// if (type.getAllMethods() != null)
 	// return type;
 	// String[] fqn = type.getFQN();
 	// IType[] allTypes = resolveTypeDeclarations(project, type);
 	// List methods = new UniqueNamesList();
 	// for (int i = 0; i < allTypes.length; i++) {
 	// try {
 	//
 	// IMethod[] methods2 = allTypes[i].getMethods();
 	// for (int j = 0; j < methods2.length; j++) {
 	// if (!((methods2[j].getFlags() & Modifiers.AccStatic) > 0)) {
 	// methods.add(methods2[j]);
 	// }
 	// }
 	// RubyClassType superType = RubyModelUtils
 	// .getSuperType(allTypes[i]);
 	// if (superType != null) {
 	// superType = resolveMethods(project, superType);
 	// IMethod[] allMethods = superType.getAllMethods();
 	// for (int j = 0; j < allMethods.length; j++) {
 	// methods.add(allMethods[j]);
 	// }
 	// }
 	// } catch (ModelException e) {
 	// }
 	// }
 	// if (allTypes.length == 0) {
 	// FakeMethod[] fakeMethods = RubyModelUtils.getFakeMethods(
 	// (ModelElement) project, "Object");
 	// for (int j = 0; j < fakeMethods.length; j++) {
 	// methods.add(fakeMethods[j]);
 	// }
 	// }
 	// return new RubyClassType(fqn, allTypes, (IMethod[]) methods
 	// .toArray(new IMethod[methods.size()]));
 	// }
 
 	// public static IType[] findSubtypes(ISourceModule module,
 	// RubyClassType type, String namePrefix) {
 	// List result = new UniqueNamesList();
 	// type = resolveMethods(module.getScriptProject(), type);
 	// IType[] declarations = type.getTypeDeclarations();
 	// for (int i = 0; i < declarations.length; i++) {
 	// IType[] subtypes;
 	// try {
 	// subtypes = declarations[i].getTypes();
 	// } catch (ModelException e) {
 	// e.printStackTrace();
 	// continue;
 	// }
 	// for (int j = 0; j < subtypes.length; j++) {
 	// String elementName = subtypes[j].getElementName();
 	// if (elementName.startsWith("<<")) // skip singletons
 	// continue;
 	// result.add(subtypes[j]);
 	// }
 	// }
 	// if (type.getFQN()[0].equals("Object")) {
 	// // get all top level types too
 	// IType[] top = RubyModelUtils.findTopLevelTypes(module, namePrefix);
 	// for (int j = 0; j < top.length; j++) {
 	// result.add(top[j]);
 	// }
 	// }
 	// return (IType[]) result.toArray(new IType[result.size()]);
 	// }
 
 	public static String searchConstantElement(ModuleDeclaration module,
 			int calculationOffset, String constantName) {
 		MixinModel model = RubyMixinModel.getRawInstance();
 		String[] modelStaticScopes = getModelStaticScopesKeys(model, module,
 				calculationOffset);
 
 		String resultKey = null;
 
 		for (int i = modelStaticScopes.length - 1; i >= 0; i--) {
 			String possibleKey = modelStaticScopes[i]
 					+ IMixinRequestor.MIXIN_NAME_SEPARATOR + constantName;
 			String[] keys = model.findKeys(possibleKey);
 			if (keys != null && keys.length > 0) {
 				resultKey = possibleKey;
 				break;
 			}
 		}
 
 		// check top-most scope
 		if (resultKey == null) {
 			// constantElement = model.get(constantName);
 			String[] keys = model.findKeys(constantName);
 			if (keys != null && keys.length > 0) {
 				resultKey = constantName;
 			}
 		}
 
 		return resultKey;
 	}
 
 }
