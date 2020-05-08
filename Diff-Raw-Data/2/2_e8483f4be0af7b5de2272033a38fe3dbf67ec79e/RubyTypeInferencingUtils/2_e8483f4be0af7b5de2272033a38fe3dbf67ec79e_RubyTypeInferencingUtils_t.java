 package org.eclipse.dltk.ruby.typeinference;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.Modifiers;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.ast.expressions.Assignment;
 import org.eclipse.dltk.ast.expressions.Expression;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.ast.references.VariableReference;
 import org.eclipse.dltk.ast.statements.Block;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.core.DLTKModelUtil;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ITypeHierarchy;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.search.IDLTKSearchConstants;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.SearchParticipant;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.ddp.IContext;
 import org.eclipse.dltk.ddp.ISourceModuleContext;
 import org.eclipse.dltk.evaluation.types.AmbiguousType;
 import org.eclipse.dltk.evaluation.types.IClassType;
 import org.eclipse.dltk.evaluation.types.IEvaluatedType;
 import org.eclipse.dltk.evaluation.types.RecursionTypeCall;
 import org.eclipse.dltk.evaluation.types.UnknownType;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.ruby.ast.RubySingletonMethodDeclaration;
 import org.eclipse.dltk.ruby.ast.SelfReference;
 import org.eclipse.dltk.ruby.core.RubyPlugin;
 import org.eclipse.dltk.ruby.core.model.FakeMethod;
 import org.eclipse.dltk.ruby.internal.parser.JRubySourceParser;
 
 public class RubyTypeInferencingUtils {
 
 	public static ASTNode[] getAllStaticScopes(ModuleDeclaration rootNode, final int requestedOffset) {
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
 			
 
 		};
 		try {
 			rootNode.traverse(visitor);
 		} catch (Exception e) {
 			RubyPlugin.log(e);
 		}
 		return (ASTNode[]) scopes.toArray(new ASTNode[scopes.size()]);
 	}
 
 	public static LocalVariableInfo findLocalVariable(ModuleDeclaration rootNode,
 			final int requestedOffset, String varName) {
 		ASTNode[] staticScopes = getAllStaticScopes(rootNode, requestedOffset);
 		int end = staticScopes.length;
 		for (int start = end - 1; start >= 0; start--) {
 			ASTNode currentScope = staticScopes[start];
 			if (!isRootLocalScope(currentScope))
 				continue;
 			ASTNode nextScope = (end < staticScopes.length ? staticScopes[end] : null);
 			Assignment[] assignments = findLocalVariableAssignments(currentScope, nextScope,
 					varName);
 			if (assignments.length > 0) {
 				return new LocalVariableInfo(currentScope, assignments);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Determines the type of a Ruby constant. Constant declarations are
 	 * searched inside the scopes corresponding to the given offset in the given
 	 * module.
 	 * 
 	 * Note that the given module/offset are used to determine the correct
 	 * search scopes only. They don't have to actually contain a reference to
 	 * the given constant.
 	 * 
 	 * The returned type contains a list of class fragments, but does not
 	 * contain a list of methods. Note that for a constant denoting a class, the
 	 * returned type is the metatype of the class.
 	 * 
 	 * The information is searched inside the project contaning the given
 	 * module.
 	 * 
 	 * XXX currently the only constants handled are class/module declarations
 	 * 
 	 * @param sourceModule
 	 *            the module containing the given offset
 	 * @param rootNode
 	 *            the root of AST corresponding to the given source module
 	 * @param requestedOffset
 	 *            the offset inside the given module denoting the correct set of
 	 *            scopes
 	 * @param constName
 	 *            A simple (non-qualified) name of the constant to search for
 	 * @return A type of the found constant
 	 */
 	public static IEvaluatedType evaluateConstantType(ISourceModule sourceModule,
 			ModuleDeclaration rootNode, final int requestedOffset, String constName) {
 		ConstantInfo[] declarations = findConstantDeclarations(sourceModule, rootNode,
 				requestedOffset, constName);
 
 		if (declarations == null)
 			return null;
 
 		boolean isType = false;
 		for (int i = 0; i < declarations.length; i++) {
 			ConstantInfo info = declarations[i];
 			if (info.getDeclaration() instanceof TypeDeclaration) {
 				isType = true;
 				break;
 			}
 		}
 
 		if (!isType) {
 			// TODO support non-type constants
 			System.out
 					.println("RubyTypeInferencingUtils.findConstant(): non-type constants are not supported yet.");
 			return null;
 		}
 
 		Collection fragments = new ArrayList();
 		ClassInfo container = null;
 		for (int i = 0; i < declarations.length; i++) {
 			ConstantInfo info = declarations[i];
 			if (info.getDeclaration() instanceof TypeDeclaration) {
 				TypeDeclaration typeDeclaration = (TypeDeclaration) info.getDeclaration();
 				container = info.getContainer();
 				try {
 					IType modelElement = RubyModelUtils.getModelTypeByAST(typeDeclaration, info
 							.getContainer().getSourceModule());
 					if (modelElement != null)
 						fragments.add(modelElement);
 				} catch (ModelException e) {
 					RubyPlugin.log(e);
 				}
 			}
 		}
 
 		Assert.isTrue(container != null);
 
 		String[] containerFQN = container.getFullyQualifiedName();
 		String[] fqn = new String[containerFQN.length + 1];
 		System.arraycopy(containerFQN, 0, fqn, 0, containerFQN.length);
 		fqn[containerFQN.length] = constName;
 		RubyClassType instanceType = new RubyClassType(fqn, (IType[]) fragments
 				.toArray(new IType[fragments.size()]), null);
 		RubyMetaClassType metaType = new RubyMetaClassType(instanceType, null);
 
 		return metaType;
 	}
 
 	/**
 	 * Finds all assignments/declarations of a Ruby constant. Constant
 	 * declarations are searched inside the scopes corresponding to the given
 	 * offset in the given module.
 	 * 
 	 * Note that the given module/offset are used to determine the correct
 	 * search scopes only. They don't have to actually contain a reference to
 	 * the given constant.
 	 * 
 	 * The information is searched inside the project contaning the given
 	 * module.
 	 * 
 	 * XXX currently the only constants handled are class/module declarations
 	 * 
 	 * @param sourceModule
 	 *            the module containing the given offset
 	 * @param rootNode
 	 *            the root of AST corresponding to the given source module
 	 * @param offset
 	 *            the offset inside the given module denoting the correct set of
 	 *            scopes
 	 * @param constName
 	 *            A simple (non-qualified) name of the constant to search for
 	 * @return An array listing all the declarations of the given constant
 	 *         (there might be multiple if the source code is incorrect, or if
 	 *         the constant denotes a class which is opened in several places).
 	 */
 	public static ConstantInfo[] findConstantDeclarations(ISourceModule sourceModule,
 			ModuleDeclaration rootNode, int offset, final String constName) {
 		Collection[] foundOccurances = findAllConstantDeclarations(sourceModule, rootNode, offset, constName);
 		if (foundOccurances == null)
 			return null;
 		for (int i = foundOccurances.length - 1; i >= 0; i--) {
 			Collection collection = foundOccurances[i];
 			if (!collection.isEmpty()) {
 				return (ConstantInfo[]) collection.toArray(new ConstantInfo[collection.size()]);
 			}
 		}
 
 		return null;
 	}
 
 	private static Collection[] findAllConstantDeclarations(ISourceModule sourceModule, ModuleDeclaration rootNode, int offset, final String constName) {
 		IDLTKProject project = sourceModule.getScriptProject();
 		String patternString = constName;
 		IDLTKSearchScope scope = SearchEngine.createSearchScope(new IModelElement[] { project });
 
 		SearchPattern pattern = SearchPattern.createPattern(patternString,
 				IDLTKSearchConstants.TYPE, IDLTKSearchConstants.DECLARATIONS,
 				SearchPattern.R_EXACT_MATCH);
 		List sourceModules;
 		try {
 			sourceModules = new SearchEngine().searchSourceOnly(pattern,
 					new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
 					new NullProgressMonitor());
 		} catch (CoreException e) {
 			RubyPlugin.log(e);
 			return null;
 		}
 
 		if (sourceModules.isEmpty())
 			return null;
 
 		ClassInfo[] requestedInfos = resolveClassScopes(sourceModule, rootNode,
 				new int[] { offset });
 		System.out.println("RubyTypeInferencingUtils.findConstantDeclarations()");
 		Collection[] foundOccurances = new ArrayList[requestedInfos.length + 1];
 		for (int i = 0; i < foundOccurances.length; i++)
 			foundOccurances[i] = new ArrayList();
 
 		for (Iterator iter = sourceModules.iterator(); iter.hasNext();) {
 			ISourceModule occuranceModule = (ISourceModule) iter.next();
 			ModuleDeclaration occuranceRoot = RubyTypeInferencingUtils.parseSource(occuranceModule);
 			ConstantInfo[] occuranceInfos = findConstantDeclarations(occuranceModule,
 					occuranceRoot, constName);
 			for (int i = 0; i < occuranceInfos.length; i++) {
 				ConstantInfo occurance = occuranceInfos[i];
 				if (occurance.getContainer().getFullyQualifiedName().length == 0)
 					// global constant
 					foundOccurances[0].add(occurance);
 				else
 					for (int j = 0; j < requestedInfos.length; j++) {
 						ClassInfo requestedInfo = requestedInfos[j];
 						if (occurance.getContainer().getEvaluatedType().equals(
 								requestedInfo.getEvaluatedType())) {
 							foundOccurances[1 + j].add(occurance);
 							break;
 						}
 					}
 			}
 		}
 		return foundOccurances;
 			}
 
 	public static IEvaluatedType determineSelfClass(IContext context, int keyOffset) {
 		if (context instanceof IInstanceContext) {
 			IInstanceContext instanceContext = (IInstanceContext) context;
 			return instanceContext.getInstanceType();
 		} else {
 			ISourceModuleContext basicContext = (ISourceModuleContext) context;
 			return determineSelfClass(basicContext.getSourceModule(), basicContext.getRootNode(),
 					keyOffset);
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
 	public static IClassType determineSelfClass(final ISourceModule sourceModule,
 			ModuleDeclaration rootNode, final int keyOffset) {
 		ClassInfo[] infos = resolveClassScopes(sourceModule, rootNode, new int[] { keyOffset });
 		RubyClassType result;
 		if (infos.length == 0)
 			result = RubyClassType.OBJECT_CLASS;
 		else
 			result = (RubyClassType) infos[infos.length - 1].getEvaluatedType();
 		ASTNode[] staticScopes = getAllStaticScopes(rootNode, keyOffset);
 		MethodDeclaration method = null;
 		for (int i = staticScopes.length - 1; i >= 0; i--)
 			if (staticScopes[i] instanceof MethodDeclaration) {
 				method = (MethodDeclaration) staticScopes[i];
 				break;
 			} else if (staticScopes[i] instanceof TypeDeclaration)
 				break;
 		RubyMetaClassType metaType = getMetaType(result);
 		if (method != null) {
 			if (method instanceof RubySingletonMethodDeclaration) {
 				RubySingletonMethodDeclaration declaration = (RubySingletonMethodDeclaration) method;
 				Expression receiver = declaration.getReceiver();
 				if (receiver instanceof SelfReference)
 					return metaType; // static method
 				if (receiver instanceof SimpleReference) 
 					if (((SimpleReference) receiver).getName().equals(result.getUnqualifiedName()))
 						return metaType; // singleton method of our metaclass
 				// TODO: handle singleton method of another class
 				return RubyMetaClassType.OBJECT_METATYPE;
 			}
 			return result;
 		}
 		return metaType;
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
 	 * @return The information about the enclosing class, or null if there is no
 	 *         enclosing class.
 	 */
 	public static ClassInfo resolveEnclosingClass(final ISourceModule sourceModule,
 			ModuleDeclaration rootNode, final int keyOffset) {
 		ClassInfo[] infos = resolveClassScopes(sourceModule, rootNode, new int[] { keyOffset });
 		if (infos.length == 0)
 			return null;
 		return infos[infos.length - 1];
 	}
 
 	/**
 	 * Determines fully-qualified names of all the class scopes that the given
 	 * offset is statically enclosed in.
 	 * 
 	 * @param sourceModule
 	 *            a module containing the given offsets
 	 * @param rootNode
 	 *            the root of AST corresponding to the given source module
 	 * @param keyOffset
 	 *            the offset
 	 * @return
 	 */
 	public static ClassInfo[] resolveClassScopes(final ISourceModule sourceModule,
 			ModuleDeclaration rootNode, final int keyOffset) {
 		return resolveClassScopes(sourceModule, rootNode, new int[] { keyOffset });
 	}
 
 	/**
 	 * Determines fully-qualified names of all the class scopes that the given
 	 * set of offsets is statically enclosed in.
 	 * 
 	 * @param sourceModule
 	 *            a module containing the given offsets
 	 * @param rootNode
 	 *            the root of AST corresponding to the given source module
 	 * @param sortedKeyOffsets
 	 *            the offsets in ascending order
 	 * @return
 	 */
 	public static ClassInfo[] resolveClassScopes(final ISourceModule sourceModule,
 			ModuleDeclaration rootNode, final int[] sortedKeyOffsets) {
 		final List scopes = new ArrayList();
 		final Collection classInfos = new ArrayList();
 		ASTVisitor visitor = new ASTVisitor() {
 
 			int index = 0;
 
 			private boolean interesting(ASTNode s) {
 				if (s.sourceStart() < 0 || s.sourceEnd() < s.sourceStart())
 					return true;
 				while (index < sortedKeyOffsets.length && sortedKeyOffsets[index] < s.sourceStart()) {
 					index++;
 				}
 				if (index >= sortedKeyOffsets.length)
 					return false;
 				if (sortedKeyOffsets[index] >= s.sourceEnd())
 					return false;
 				return true;
 			}
 
 			public boolean visit(Expression s) throws Exception {
 				if (!interesting(s))
 					return false;
 				return true;
 			}
 
 			public boolean visit(MethodDeclaration s) throws Exception {
 				if (!interesting(s))
 					return false;
 				return true;
 			}
 
 			public boolean visit(ModuleDeclaration s) throws Exception {
 				if (!interesting(s))
 					return false;
 				return true;
 			}
 
 			public boolean visit(Statement s) throws Exception {
 				// XXX workaround for a bug in block offset calculation
 				if (s instanceof Block)
 					return true;
 				if (!interesting(s))
 					return false;
 				return true;
 			}
 
 			public boolean visit(TypeDeclaration s) throws Exception {
 				if (!interesting(s))
 					return false;
 				String name = s.getName();
 				scopes.add(name);
 				String[] fqn = (String[]) scopes.toArray(new String[scopes.size()]);
 				ClassInfo type = new ClassInfo(s, sourceModule, new RubyClassType(fqn, null, null));
 				classInfos.add(type);
 				return true;
 			}
 
 			public boolean endvisit(TypeDeclaration s) throws Exception {
 				if (!interesting(s))
 					return false;
 				Assert.isTrue(scopes.size() >= 0);
 				scopes.remove(scopes.size() - 1);
 				return false /* dummy */;
 			}
 
 			public boolean visitGeneral(ASTNode s) throws Exception {
 				if (!interesting(s))
 					return false;
 				return true;
 			}
 
 		};
 		try {
 			rootNode.traverse(visitor);
 		} catch (Exception e) {
 			RubyPlugin.log(e);
 		}
 		return (ClassInfo[]) classInfos.toArray(new ClassInfo[classInfos.size()]);
 	}
 
 	/**
 	 * Searches the given module for all declaration of all constants with the
 	 * given name.
 	 * 
 	 * @param sourceModule
 	 *            the module containing the given offset
 	 * @param rootNode
 	 *            the root of AST corresponding to the given source module
 	 * @param constName
 	 *            a simple (non-qualified) name of the constant to search for
 	 * @return An array giving informations about every such constant
 	 *         declaration
 	 */
 	private static ConstantInfo[] findConstantDeclarations(final ISourceModule sourceModule,
 			ModuleDeclaration rootNode, final String constName) {
 		final List scopes = new ArrayList();
 		final Collection constInfos = new ArrayList();
 		ASTVisitor visitor = new ASTVisitor() {
 
 			public boolean visit(TypeDeclaration s) throws Exception {
 				String name = s.getName();
 				if (constName.equals(name)) {
 					String[] fqn = (String[]) scopes.toArray(new String[scopes.size()]);
 					ClassInfo type = new ClassInfo(s, sourceModule, new RubyClassType(fqn, null,
 							null));
 					ConstantInfo info = new ConstantInfo(type, s, name);
 					constInfos.add(info);
 				}
 				scopes.add(name);
 				return true;
 			}
 
 			public boolean endvisit(TypeDeclaration s) throws Exception {
 				scopes.remove(scopes.size() - 1);
 				return false /* dummy */;
 			}
 
 		};
 		try {
 			rootNode.traverse(visitor);
 		} catch (Exception e) {
 			RubyPlugin.log(e);
 		}
 		return (ConstantInfo[]) constInfos.toArray(new ConstantInfo[constInfos.size()]);
 	}
 
 	public static Assignment[] findLocalVariableAssignments(final ASTNode scope,
 			final ASTNode nextScope, final String varName) {
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
 		return (Assignment[]) assignments.toArray(new Assignment[assignments.size()]);
 	}
 
 	public static boolean isRootLocalScope(ASTNode node) {
 		return node instanceof ModuleDeclaration || node instanceof TypeDeclaration
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
 		JRubySourceParser parser = new JRubySourceParser(null);
 		try {
 			return parser.parse(module.getSource());
 		} catch (ModelException e) {
 			RubyPlugin.log(e);
 			return null;
 		}
 	}
 	
 	public static RubyMetaClassType getMetaType(RubyClassType type) {
 		return new RubyMetaClassType(type, null);
 	}
 
 	public static IClassType getMetaType(IClassType type) {
 		if (type instanceof RubyMetaClassType)
 			return new RubyClassType(new String[] {"Class"}, null, null);
 		return new RubyMetaClassType(type, null);
 	}
 	
 	public static IEvaluatedType getAmbiguousMetaType(IEvaluatedType receiver) {
 		if (receiver instanceof IClassType) 
 			return getMetaType((IClassType) receiver);
 		else if (receiver instanceof AmbiguousType) {
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
 	
 	private static class UniqueNamesList extends ArrayList {
 
 		HashSet names = new HashSet ();
 		
 		public boolean add(Object elem) {
 			if (elem instanceof IModelElement) {
 				IModelElement modelElement = (IModelElement) elem;
 				if (names.contains(modelElement.getElementName()))
 						return false;
 				names.add(modelElement.getElementName());
 			}
 			return super.add(elem);
 		}
 
 		public void clear() {			
 			super.clear();
 			names.clear();
 		}
 
 		public boolean contains(Object elem) {
 			if (elem instanceof IModelElement) {
 				IModelElement modelElement = (IModelElement) elem;
 				return names.contains(modelElement.getElementName());
 			}
 			return super.contains(elem);
 		}
 
 	}
 	
 	public static RubyMetaClassType resolveMethods(ISourceModule module, RubyMetaClassType type) {
 		if (type.getMethods() == null) {
 			List result = new UniqueNamesList();	
 			if (type.getInstanceType() != null) {
 				RubyClassType instanceType = (RubyClassType) type.getInstanceType();
 				
 				if (instanceType.getFQN()[0].equals("Object")) {					
 					IMethod[] topLevelMethods = RubyModelUtils.findTopLevelMethods(module, "");
 					for (int i = 0; i < topLevelMethods.length; i++) {
 						result.add(topLevelMethods[i]);
 					}					
 				}
 				
 				IType[] types = resolveTypeDeclarations(module.getScriptProject(), instanceType);
 				for (int i = 0; i < types.length; i++) {
 					try {
 						IMethod[] methods = types[i].getMethods();
 						IType[] subtypes = types[i].getTypes();
 						
 						for (int j = 0; j < methods.length; j++) {
 							if (methods[j].getElementName().startsWith("self.")) {
 								result.add(methods[j]);
 							}
 						}
 						
 						for (int j = 0; j < subtypes.length; j++) {
 							if (!subtypes[j].getElementName().equals("<< self"))
 								continue;
 							IMethod[] methods2 = subtypes[j].getMethods();
 							for (int k = 0; k < methods2.length; k++) {
 								int flags = methods2[k].getFlags();
 								if ((flags & Modifiers.AccStatic) == 0) {
 									result.add(methods2[k]);
 								}
 							}
 						}
 						
 					} catch (ModelException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			
 			FakeMethod[] metaMethods = RubyModelUtils.getFakeMetaMethods((ModelElement) module.getScriptProject(), "Object");
 			for (int j = 0; j < metaMethods.length; j++) {
 				result.add(metaMethods[j]);
 			}
 			if (metaMethods != null) {
 				return new RubyMetaClassType(type.getInstanceType(), (IMethod[])result.toArray(new IMethod[result.size()]));
 			}
 		}
 		return type;
 	}
 	
 	public static IType[] resolveTypeDeclarations (IDLTKProject project, RubyClassType type) {
 		String[] fqn = type.getFQN();
 		IType[] allTypes = type.getTypeDeclarations();
 		if (allTypes == null) {
 			StringBuffer strFqn = new StringBuffer();
 			for (int i = 0; i < fqn.length; i++) {
 				strFqn.append("::");
 				strFqn.append(fqn[i]);
 			}
 			allTypes = DLTKModelUtil.getAllTypes(project, strFqn.toString(), "::");
 		}
 		return allTypes;
 	}
 	
 	// FIXME should be in RubyClassType itself!
 	public static RubyClassType resolveMethods(IDLTKProject project, RubyClassType type) {
 		if (type.getAllMethods() != null)
 			return type;
 		String[] fqn = type.getFQN();
 		IType[] allTypes = resolveTypeDeclarations(project, type);
 		List methods = new UniqueNamesList ();		
 		for (int i = 0; i < allTypes.length; i++) {
 			try {				
 			
 				IMethod[] methods2 = allTypes[i].getMethods();
 				for (int j = 0; j < methods2.length; j++) {
 					if (!((methods2[j].getFlags() & Modifiers.AccStatic) > 0)) {
 						methods.add(methods2[j]);
 					}
 				}
 				RubyClassType superType = RubyModelUtils.getSuperType(allTypes[i]);
 				if (superType != null) {
 					superType = resolveMethods(project, superType);
 					IMethod[] allMethods = superType.getAllMethods();
 					for (int j = 0; j < allMethods.length; j++) {
 						methods.add(allMethods[j]);
 					}
 				}
 			} catch (ModelException e) {
 			}			
 		}		
 		return new RubyClassType(fqn, allTypes, (IMethod[]) methods.toArray(new IMethod[methods.size()]));
 	}
 	
 	public static String compileFQN(String[] fqn, boolean fully) {
 		StringBuffer res = new StringBuffer();
 		for (int i = 0; i < fqn.length; i++) {			
 			if (fully || i > 0)
 				res.append("::");
 			res.append(fqn[i]);
 		}
 		return res.toString();
 	}
 	
 	public static IType[] findSubtypes (ISourceModule module, RubyClassType type, String namePrefix) {
		List result = new UniqueNamesList ();
 		type = resolveMethods(module.getScriptProject(), type);		
 		IType[] declarations = type.getTypeDeclarations();
 		for (int i = 0; i < declarations.length; i++) {
 			IType[] subtypes;
 			try {
 				subtypes = declarations[i].getTypes();
 			} catch (ModelException e) {
 				e.printStackTrace();
 				continue;
 			}
 			for (int j = 0; j < subtypes.length; j++) {
 				String elementName = subtypes[j].getElementName();
 				if (elementName.startsWith("<<")) //skip singletons
 					continue;				
 				result.add(subtypes[j]);
 			}
 		}
 		if (type.getFQN()[0].equals("Object")) {
 			//get all top level types too
 			IType[] top = RubyModelUtils.findTopLevelTypes(module, namePrefix);
 			for (int j = 0; j < top.length; j++) {
 				result.add(top[j]);
 			}
 		}
 		return (IType[]) result.toArray(new IType[result.size()]);
 	}
 
 }
