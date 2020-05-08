 package descent.core.dom;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import descent.core.IJavaElement;
 import descent.core.IJavaProject;
 import descent.core.JavaModelException;
 import descent.core.Signature;
 import descent.core.WorkingCopyOwner;
 import descent.core.compiler.CharOperation;
 import descent.internal.compiler.parser.ASTDmdNode;
 import descent.internal.compiler.parser.AliasDeclaration;
 import descent.internal.compiler.parser.CallExp;
 import descent.internal.compiler.parser.Condition;
 import descent.internal.compiler.parser.ConditionalDeclaration;
 import descent.internal.compiler.parser.ConditionalStatement;
 import descent.internal.compiler.parser.CtorDeclaration;
 import descent.internal.compiler.parser.DotVarExp;
 import descent.internal.compiler.parser.Dsymbol;
 import descent.internal.compiler.parser.EnumMember;
 import descent.internal.compiler.parser.Expression;
 import descent.internal.compiler.parser.FuncDeclaration;
 import descent.internal.compiler.parser.ISignatureOptions;
 import descent.internal.compiler.parser.Id;
 import descent.internal.compiler.parser.IdentifierExp;
 import descent.internal.compiler.parser.Module;
 import descent.internal.compiler.parser.NewExp;
 import descent.internal.compiler.parser.SemanticContext;
 import descent.internal.compiler.parser.TemplateDeclaration;
 import descent.internal.compiler.parser.Type;
 import descent.internal.compiler.parser.TypeAArray;
 import descent.internal.compiler.parser.TypeBasic;
 import descent.internal.compiler.parser.TypeClass;
 import descent.internal.compiler.parser.TypeDArray;
 import descent.internal.compiler.parser.TypeDelegate;
 import descent.internal.compiler.parser.TypeEnum;
 import descent.internal.compiler.parser.TypeExp;
 import descent.internal.compiler.parser.TypeFunction;
 import descent.internal.compiler.parser.TypeIdentifier;
 import descent.internal.compiler.parser.TypePointer;
 import descent.internal.compiler.parser.TypeSArray;
 import descent.internal.compiler.parser.TypeSlice;
 import descent.internal.compiler.parser.TypeStruct;
 import descent.internal.compiler.parser.TypeTuple;
 import descent.internal.compiler.parser.TypedefDeclaration;
 import descent.internal.compiler.parser.VarDeclaration;
 import descent.internal.compiler.parser.VarExp;
 import descent.internal.core.InternalSignature;
 import descent.internal.core.util.Util;
 
 class DefaultBindingResolver extends BindingResolver {
 
 	/*
 	 * Holds on binding tables that can be shared by several ASTs.
 	 */
 	static class BindingTables {
 
 		/**
 		 * This map is used to get a binding from its binding key.
 		 */
 		Map<String, IBinding> bindingKeysToBindings;
 
 		/**
 		 * This map is used to get a binding from an old node.
 		 */
 		Map<Dsymbol, IBinding> symbolToBindings;
 
 		BindingTables() {
 			this.bindingKeysToBindings = new HashMap<String, IBinding>();
 			this.symbolToBindings = new HashMap<Dsymbol, IBinding>();
 		}
 
 	}
 
 	/**
 	 * This map is used to get an ast node from its binding (new binding) or DOM
 	 */
 	final Map<IBinding, ASTNode> bindingsToAstNodes;
 
 	/*
 	 * The shared binding tables accros ASTs.
 	 */
 	final BindingTables bindingTables;
 
 	/**
 	 * This map is used to retrieve an old ast node using the new ast node. This
 	 * is not an identity map.
 	 */
 	final Map<ASTNode, ASTDmdNode> newAstToOldAst;
 
 	/**
 	 * The working copy owner that defines the context in which this resolver is
 	 * creating the bindings.
 	 */
 	final WorkingCopyOwner workingCopyOwner;
 
 	/**
 	 * The project to lookup types.
 	 */
 	final IJavaProject javaProject;
 
 	/**
 	 * The name environment to search for compilation units.
 	 */
 //	final INameEnvironment environment;
 
 	final InternalSignature internalSignature;
 
 	final SemanticContext context;
 	
 	final int apiLevel;
 
 	/**
 	 * Constructor for DefaultBindingResolver.
 	 */
 	DefaultBindingResolver(IJavaProject project, SemanticContext context,
 			WorkingCopyOwner workingCopyOwner, BindingTables bindingTables) {
 		this.javaProject = project;
 		this.context = context;
 		this.bindingsToAstNodes = new HashMap();
 		this.bindingTables = bindingTables;
 		this.workingCopyOwner = workingCopyOwner;
 		this.newAstToOldAst = new HashMap();
 		this.internalSignature = new InternalSignature(javaProject);
 		this.apiLevel = context == null ? (javaProject == null ? AST.D2 : javaProject.getApiLevel()) : context.apiLevel;
 		
 //		try {
 //			this.environment = new SearchableEnvironment(
 //					(JavaProject) javaProject, workingCopyOwner);
 //		} catch (JavaModelException e) {
 //			Util.log(e);
 //			throw new RuntimeException(e);
 //		}
 	}
 
 	@Override
 	ASTNode findDeclaringNode(IBinding binding) {
 		return bindingsToAstNodes.get(binding);
 	}
 
 	@Override
 	ASTNode findDeclaringNode(String bindingKey) {
 		IBinding binding = bindingTables.bindingKeysToBindings.get(bindingKey);
		if (binding == null) {
			return findDeclaringNodeBruteForce(bindingKey);
		}
 		if (binding != null) {
			ASTNode node = findDeclaringNode(binding);
			if (node == null) {
				return findDeclaringNodeBruteForce(bindingKey);
			}
 		}
		return null;
 	}
 
 	private ASTNode findDeclaringNodeBruteForce(String bindingKey) {
 		for(Map.Entry<ASTNode, ASTDmdNode> entry : this.newAstToOldAst.entrySet()) {
 			ASTDmdNode value = entry.getValue();
 			if (value instanceof Dsymbol) {
 				Dsymbol sym = (Dsymbol) value;
 				String signature = sym.getSignature(ISignatureOptions.None);
 				if (bindingKey.equals(signature)) {
 					resolveDsymbol(sym, entry.getKey());
 					return entry.getKey();
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean resolveConditionalDeclaration(
 			descent.core.dom.ConditionalDeclaration declaration) {
 		if (parentIsTemplate(declaration)) {
 			return null;
 		}
 		
 		ASTDmdNode old = newAstToOldAst.get(declaration);
 		if (!(old instanceof ConditionalDeclaration)) {
 			return false;
 		}
 
 		Condition condition = ((ConditionalDeclaration) old).condition;
 		return resolveCondition(condition);
 	}
 
 	@Override
 	public Boolean resolveConditionalStatement(
 			descent.core.dom.ConditionalStatement statement) {
 		if (parentIsTemplate(statement)) {
 			return null;
 		}
 		
 		ASTDmdNode old = newAstToOldAst.get(statement);
 		if (!(old instanceof ConditionalStatement)) {
 			return false;
 		}
 
 		Condition condition = ((ConditionalStatement) old).condition;
 		return resolveCondition(condition);
 	}
 
 	private boolean parentIsTemplate(descent.core.dom.ASTNode node) {
 		ASTNode parent = node.getParent();
 		while(parent != null) {
 			switch(parent.getNodeType()) {
 			case ASTNode.TEMPLATE_DECLARATION:
 				return true;
 			case ASTNode.FUNCTION_DECLARATION:
 				FunctionDeclaration func = (FunctionDeclaration) parent;
 				if (func.templateParameters().size() > 0) {
 					return true;
 				}
 				break;
 			case ASTNode.AGGREGATE_DECLARATION:
 				AggregateDeclaration agg = (AggregateDeclaration) parent;
 				if (agg.templateParameters().size() > 0) {
 					return true;
 				}
 				break;
 			}
 			parent = parent.getParent();
 		}
 		return false;
 	}
 
 	private Boolean resolveCondition(Condition condition) {
 		int c = condition.inc;
 		switch (c) {
 		case 0:
 			return null;
 		case 1:
 			return Boolean.TRUE;
 		case 2:
 			return Boolean.FALSE;
 		}
 
 		return null;
 	}
 
 	@Override
 	ITypeBinding resolveAggregate(descent.core.dom.AggregateDeclaration type) {
 		ASTDmdNode old = newAstToOldAst.get(type);
 		if (!(old instanceof descent.internal.compiler.parser.AggregateDeclaration)) {
 			return null;
 		}
 
 		descent.internal.compiler.parser.AggregateDeclaration agg = (descent.internal.compiler.parser.AggregateDeclaration) old;
 		return resolveAggregateOrEnum(agg, type);
 	}
 
 	@Override
 	ITypeBinding resolveTemplate(descent.core.dom.TemplateDeclaration type) {
 		ASTDmdNode old = newAstToOldAst.get(type);
 		if (!(old instanceof TemplateDeclaration)) {
 			return null;
 		}
 
 		TemplateDeclaration temp = (TemplateDeclaration) old;
 		return resolveAggregateOrEnum(temp, type);
 	}
 
 	@Override
 	ITypeBinding resolveEnum(descent.core.dom.EnumDeclaration type) {
 		ASTDmdNode old = newAstToOldAst.get(type);
 		if (!(old instanceof descent.internal.compiler.parser.EnumDeclaration)) {
 			return null;
 		}
 
 		descent.internal.compiler.parser.EnumDeclaration e = (descent.internal.compiler.parser.EnumDeclaration) old;
 		return resolveAggregateOrEnum(e, type);
 	}
 
 	ITypeBinding resolveAggregateOrEnum(Dsymbol dsymbol, ASTNode original) {
 		if (dsymbol.ident == null || dsymbol.ident.ident == null) {
 			return null;
 		}
 		
 		return resolveAggregateOrEnum(dsymbol, getSignature(dsymbol), original);
 	}
 
 	ITypeBinding resolveAggregateOrEnum(Dsymbol dsymbol, String key, ASTNode original) {
 		IBinding binding = bindingTables.bindingKeysToBindings.get(key);
 		if (binding == null) {
 			binding = new TypeBinding(this, dsymbol, key);
 			glue(key, binding, original);
 		}
 
 		if (binding != null && !(binding instanceof ITypeBinding)) {
 			throw new IllegalStateException();
 		}
 
 		return (ITypeBinding) binding;
 	}
 
 	@Override
 	ITypeBinding resolveVariable(VariableDeclaration variable) {
 		if (variable.fragments().size() == 0) {
 			return null;
 		}
 
 		ASTDmdNode old = newAstToOldAst.get(variable.fragments().get(0));
 		if (!(old instanceof VarDeclaration)) {
 			return null;
 		}
 
 		VarDeclaration v = (VarDeclaration) old;
 		return resolveType(v.type, variable);
 	}
 
 	@Override
 	IVariableBinding resolveVariableFragment(VariableDeclarationFragment fragment) {
 		ASTDmdNode old = newAstToOldAst.get(fragment);
 		if (!(old instanceof VarDeclaration)) {
 			return null;
 		}
 
 		VarDeclaration decl = (VarDeclaration) old;
 		return resolveVarDeclaration(decl, fragment);
 	}
 
 	@Override
 	IBinding resolveAlias(descent.core.dom.AliasDeclaration alias) {
 		if (alias.fragments().size() == 0) {
 			return null;
 		}
 
 		ASTDmdNode old = newAstToOldAst.get(alias.fragments().get(0));
 		if (!(old instanceof descent.internal.compiler.parser.AliasDeclaration)) {
 			return null;
 		}
 
 		AliasDeclaration v = (AliasDeclaration) old;
 		if (v.type != null) {
 			return resolveType(v.type, alias);
 		} else if (v.aliassym != null) {
 			return resolveDsymbol(v.aliassym, alias);
 		}
 
 		return null;
 	}
 
 	@Override
 	ITypeBinding resolveAliasFragment(AliasDeclarationFragment fragment) {
 		ASTDmdNode old = newAstToOldAst.get(fragment);
 		if (!(old instanceof AliasDeclaration)) {
 			return null;
 		}
 
 		AliasDeclaration decl = (AliasDeclaration) old;
 		return resolveAliasOrTypedefDeclaration(decl, fragment);
 	}
 
 	@Override
 	ITypeBinding resolveTypedef(descent.core.dom.TypedefDeclaration typedef) {
 		if (typedef.fragments().size() == 0) {
 			return null;
 		}
 
 		ASTDmdNode old = newAstToOldAst.get(typedef.fragments().get(0));
 		if (!(old instanceof TypedefDeclaration)) {
 			return null;
 		}
 
 		TypedefDeclaration decl = (TypedefDeclaration) old;
 		return resolveType(decl.basetype, typedef);
 	}
 
 	@Override
 	ITypeBinding resolveTypedefFragment(TypedefDeclarationFragment fragment) {
 		ASTDmdNode old = newAstToOldAst.get(fragment);
 		if (!(old instanceof TypedefDeclaration)) {
 			return null;
 		}
 
 		TypedefDeclaration decl = (TypedefDeclaration) old;
 		return resolveAliasOrTypedefDeclaration(decl, fragment);
 	}
 	
 	private ITypeBinding resolveAliasOrTypedefDeclaration(Dsymbol sym, ASTNode original) {
 		if (sym.ident == null || sym.ident.ident == null) {
 			return null;
 		}
 		
 		String key = getSignature(sym);
 		if (key == null) {
 			return null;
 		}
 		
 		IBinding binding = bindingTables.bindingKeysToBindings.get(key);
 		if (binding == null) {
 			binding = new TypeBinding(this, sym, key);
 			glue(key, binding, original);
 		}
 		
 		if (binding != null && !(binding instanceof ITypeBinding)) {
 			throw new IllegalStateException();
 		}
 
 		return (ITypeBinding) binding;
 	}
 
 	private IVariableBinding resolveVarDeclaration(Dsymbol sym, ASTNode original) {
 		if (sym.ident == null || sym.ident.ident == null) {
 			return null;
 		}
 
 		String key = getSignature(sym);
 		if (key == null) {
 			return null;
 		}
 
 		IBinding binding = bindingTables.bindingKeysToBindings.get(key);
 		if (binding == null) {
 			binding = new VariableBinding(this, sym, key);
 			glue(key, binding, original);
 		}
 
 		if (binding != null && !(binding instanceof IVariableBinding)) {
 			throw new IllegalStateException();
 		}
 
 		return (IVariableBinding) binding;
 	}
 
 	@Override
 	IBinding resolveName(Name name) {
 		ASTNode parent = name.getParent();
 		if (parent != null) {
 			switch (parent.getNodeType()) {
 			case ASTNode.AGGREGATE_DECLARATION:
 				return resolveAggregate((descent.core.dom.AggregateDeclaration) parent);
 			case ASTNode.ENUM_DECLARATION:
 				return resolveEnum((descent.core.dom.EnumDeclaration) parent);
 			case ASTNode.TEMPLATE_DECLARATION:
 				return resolveTemplate((descent.core.dom.TemplateDeclaration) parent);
 			case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
 				return resolveVariableFragment((VariableDeclarationFragment) parent);
 			case ASTNode.ALIAS_DECLARATION_FRAGMENT:
 				return resolveAliasFragment((AliasDeclarationFragment) parent);
 			case ASTNode.TYPEDEF_DECLARATION_FRAGMENT:
 				return resolveTypedefFragment((TypedefDeclarationFragment) parent);
 			case ASTNode.ENUM_MEMBER:
 				return resolveEnumMember((descent.core.dom.EnumMember) parent);
 			case ASTNode.SIMPLE_TYPE:
 				return resolveType((descent.core.dom.Type) parent);
 			case ASTNode.IMPORT:
 				return resolveImport((descent.core.dom.Import) parent);
 			case ASTNode.QUALIFIED_NAME:
 				QualifiedName qName = (QualifiedName) parent;
 				if (qName.getName() == name) {
 					return resolveName(qName);
 				}
 				break;
 			case ASTNode.FUNCTION_DECLARATION:
 				if (name.getLocationInParent() == FunctionDeclaration.NAME_PROPERTY) {
 					return resolveMethod((FunctionDeclaration) parent);
 				}
 				break;
 			case ASTNode.ARGUMENT:
 				return resolveArgument((descent.core.dom.Argument) parent);
 			default:
 				if (parent instanceof descent.core.dom.Type
 						&& !(parent instanceof TemplateType)) {
 					return resolveType((descent.core.dom.Type) parent);
 				}
 
 				// See if it's a built-in property
 				if (name.isSimpleName()) {
 					if (parent instanceof DotIdentifierExpression
 							&& name.getLocationInParent().equals(
 									DotIdentifierExpression.NAME_PROPERTY)) {
 						DotIdentifierExpression dotid = (DotIdentifierExpression) parent;
 						descent.core.dom.Expression exp = dotid.getExpression();
 						if (exp != null) {
 							ASTDmdNode old = newAstToOldAst.get(exp);
 							if (!(old instanceof descent.internal.compiler.parser.Expression)) {
 								return null;
 							}
 
 							Expression oldExp = (Expression) old;
 							if (oldExp != null) {
 								Type type = oldExp.type;
 								if (type == null) {
 									Expression resolvedExp;
 									Dsymbol resolvedSymbol;
 									if ((resolvedExp = context.getResolvedExp(oldExp)) != null) {
 										if ((resolvedSymbol = context.getResolvedSymbol(oldExp)) != null && resolvedSymbol.type() != null
 												&& resolvedSymbol.type() instanceof TypeDelegate) {
 											type = resolvedSymbol.type();
 										} else {
 											type = resolvedExp.type;
 										}
 									}
 									if (type == null) {
 //										if (oldExp.resolvedSymbol != null) {
 //											// TODO use resolvedSymbol
 //										}
 									}
 								}
 								if (type != null) {
 									IBinding binding = resolveBuiltinProperty(
 											exp, type, (SimpleName) name);
 									if (binding != null) {
 										return binding;
 									}
 								}
 							}
 						}
 					}
 				}
 
 				if (name.isSimpleName()) {
 					ASTDmdNode old = newAstToOldAst.get(name);
 					if (!(old instanceof IdentifierExp)) {
 						return null;
 					}
 
 					IdentifierExp node = (IdentifierExp) old;
 					return resolveIdentifierExp(name, node);
 				}
 			}
 		}
 		return null;
 	}
 
 	private IBinding resolveBuiltinProperty(descent.core.dom.Expression exp, Type type, SimpleName name) {
 		String identifier = name.getIdentifier();
 		String signature = getSignature(type) + Signature.C_VARIABLE
 				+ identifier.length() + identifier;
 
 		IBinding binding = bindingTables.bindingKeysToBindings.get(signature);
 		if (binding == null) {
 			char[] prop = identifier.toCharArray();
 
 			if (CharOperation.equals(prop, Id.init)) {
 				binding = new BuiltinPropertyBinding(this, type, type,
 						identifier, signature);
 			} else if (CharOperation.equals(prop, Id.__sizeof)
 					|| CharOperation.equals(prop, Id.alignof)) {
 				binding = new BuiltinPropertyBinding(this, type, Type.tint32,
 						identifier, signature);
 			} else if (CharOperation.equals(prop, Id.mangleof)
 					|| CharOperation.equals(prop, Id.stringof)) {
 				// TODO it's probably a static array, fix this
 				binding = new BuiltinPropertyBinding(this, type,
 						new TypeDArray(type), identifier, signature);
 			} else if (CharOperation.equals(prop, Id.offsetof)) {
 				if (exp instanceof DotIdentifierExpression) {
 					DotIdentifierExpression die = (DotIdentifierExpression) exp;
 					if (die.getExpression() != null) {
 						IBinding typeBinding = die.getExpression().resolveTypeBinding();
 						if (typeBinding != null && typeBinding.getKind() == IBinding.TYPE) {
 							ITypeBinding tb = (ITypeBinding) typeBinding;
 							if (tb.isClass() || tb.isStruct()) {
 								binding = new BuiltinPropertyBinding(this, Type.tuns32, type,
 										identifier, signature);
 							}
 						}
 					}
 				}
 			} else if ((type instanceof TypeBasic || type instanceof TypeEnum)
 					&& (CharOperation.equals(prop, Id.min) || CharOperation
 							.equals(prop, Id.max))) {
 				if (type instanceof TypeBasic) {
 					if (type.isintegral() || type.isfloating()) {
 						binding = new BuiltinPropertyBinding(this, type, type,
 								identifier, signature);
 					}
 				} else {
 					binding = new BuiltinPropertyBinding(this, type, type,
 							identifier, signature);
 				}
 			} else if (type instanceof TypeBasic
 					&& type.isfloating()
 					&& (CharOperation.equals(prop, Id.infinity)
 							|| CharOperation.equals(prop, Id.nan) || CharOperation
 							.equals(prop, Id.epsilon))) {
 				binding = new BuiltinPropertyBinding(this, type, type,
 						identifier, signature);
 			} else if (type instanceof TypeBasic
 					&& type.isfloating()
 					&& (CharOperation.equals(prop, Id.dig)
 							|| CharOperation.equals(prop, Id.mant_dig)
 							|| CharOperation.equals(prop, Id.max_10_exp)
 							|| CharOperation.equals(prop, Id.max_exp)
 							|| CharOperation.equals(prop, Id.min_10_exp) || CharOperation
 							.equals(prop, Id.min_exp))) {
 				binding = new BuiltinPropertyBinding(this, type, Type.tint32,
 						identifier, signature);
 			} else if (type instanceof TypeBasic
 					&& type.iscomplex()
 					&& (CharOperation.equals(prop, Id.re)
 							|| CharOperation.equals(prop, Id.im))) {
 				Type component;
 				switch(type.ty) {
 				case Tcomplex32: component = Type.tfloat32; break;
 				case Tcomplex64: component = Type.tfloat64; break;
 				case Tcomplex80: component = Type.tfloat80; break;
 				default: throw new IllegalStateException("Should not happen");
 				}
 				
 				binding = new BuiltinPropertyBinding(this, type, component,
 						identifier, signature);
 			} else if (type instanceof TypeSArray || type instanceof TypeDArray) {
 				if (CharOperation.equals(prop, Id.dup)
 						|| CharOperation.equals(prop, Id.sort)
 						|| CharOperation.equals(prop, Id.reverse)) {
 					binding = new BuiltinPropertyBinding(this, type, type,
 							identifier, signature);
 				} else if (CharOperation.equals(prop, Id.length)
 						|| CharOperation.equals(prop, Id.ptr)) {
 					binding = new BuiltinPropertyBinding(this, type,
 							Type.tuns32, identifier, signature);
 				} else if (apiLevel >= AST.D2) {
 					if (CharOperation.equals(prop, Id.idup)) {
 						String otherSignature = signature;
 						if (signature.length() == 0
 								|| signature.charAt(0) != Signature.C_IMMUTABLE) {
 							otherSignature = String
 									.valueOf(Signature.C_IMMUTABLE)
 									+ signature;
 						}
 						binding = new BuiltinPropertyBinding(this, type
 								.invariantOf(context), type, identifier,
 								otherSignature);
 					}
 				}
 			} else if (type instanceof TypeAArray) {
 				if (CharOperation.equals(prop, Id.length)) {
 					binding = new BuiltinPropertyBinding(this, type,
 							Type.tuns32, identifier, signature);
 				} else if (CharOperation.equals(prop, Id.rehash)) {
 					binding = new BuiltinPropertyBinding(this, type, type,
 							identifier, signature);
 				} else if (CharOperation.equals(prop, Id.keys)) {
 					String otherSignature = "A"
 							+ ((TypeAArray) type).index.toString();
 					binding = new BuiltinPropertyBinding(this, new TypeDArray(
 							((TypeAArray) type).index), type, identifier, otherSignature);
 				} else if (CharOperation.equals(prop, Id.values)) {
 					String otherSignature = "A"
 							+ ((TypeAArray) type).next.toString();
 					binding = new BuiltinPropertyBinding(this, new TypeDArray(
 							((TypeAArray) type).next), type, identifier, otherSignature);
 				}
 			} else if (type instanceof TypeClass) {
 				if (CharOperation.equals(prop, Id.classinfo)) {
 					binding = new BuiltinPropertyBinding(this, type,
 							context.ClassDeclaration_classinfo.type, identifier, signature);
 				} else if (CharOperation.equals(prop, Id.tupleof)) {
 					// TODO: use correct type for tupleof
 					binding = new BuiltinPropertyBinding(this, type,
 							context.ClassDeclaration_classinfo.type, identifier, signature);
 				}
 			} else if (type instanceof TypeStruct) {
 				if (CharOperation.equals(prop, Id.tupleof)) {
 					// TODO: use correct type for tupleof
 					binding = new BuiltinPropertyBinding(this, type,
 							context.ClassDeclaration_classinfo.type, identifier, signature);
 				}
 			} else if (type instanceof TypeDelegate) {
 				if (CharOperation.equals(prop, Id.ptr)) {
 					binding = new BuiltinPropertyBinding(this, type,
 							context.Type_tvoidptr, identifier, signature);
 				} else if (CharOperation.equals(prop, Id.funcptr)) {
 					TypePointer tp = new TypePointer(type.next);
 					String newSignature = "P" + signature.substring(1);
 					
 					binding = new BuiltinPropertyBinding(this, type,
 							tp, identifier, newSignature);
 				}
 			} else if (type instanceof TypeTuple) {
 				if (CharOperation.equals(prop, Id.length)) {
 					binding = new BuiltinPropertyBinding(this, type,
 							Type.tuns32, identifier, signature);
 				}
 			}
 		}
 
 		return binding;
 	}
 
 	@Override
 	public IMethodBinding resolveNewExpression(NewExpression expression) {
 		ASTDmdNode old = newAstToOldAst.get(expression);
 		if (!(old instanceof descent.internal.compiler.parser.NewExp)) {
 			return null;
 		}
 
 		NewExp exp = (NewExp) old;
 		CtorDeclaration ctor = exp.member;
 		if (ctor == null) {
 			return null;
 		}
 
 		return resolveFuncDeclaration(ctor, expression);
 	}
 
 	@Override
 	public IMethodBinding resolveCallExpression(CallExpression expression) {
 		ASTDmdNode old = newAstToOldAst.get(expression);
 		if (!(old instanceof descent.internal.compiler.parser.CallExp)) {
 			return null;
 		}
 
 		FuncDeclaration func;
 
 		CallExp exp = (CallExp) old;
 		Expression resolvedExp;
 		Dsymbol resolvedSymbol;
 		if ((resolvedSymbol = context.getResolvedSymbol(exp.sourceE1)) == null
 				|| !(resolvedSymbol instanceof FuncDeclaration)) {
 			if ((resolvedExp = context.getResolvedExp(exp.sourceE1)) == null
 					|| !(resolvedExp instanceof VarExp)) {
 				if ((resolvedExp = exp.e1) == null || !(resolvedExp instanceof VarExp)) {
 					return null;
 				}
 			}
 
 			VarExp varExp = (VarExp) resolvedExp;
 			if (!(varExp.var instanceof FuncDeclaration)) {
 				return null;
 			}
 			func = (FuncDeclaration) varExp.var;
 		} else {
 			func = (FuncDeclaration) resolvedSymbol;
 		}
 
 		return resolveFuncDeclaration(func, expression);
 	}
 
 	private IBinding resolveIdentifierExp(ASTNode node, IdentifierExp id) {
 		Dsymbol resolvedSymbol;
 		if ((resolvedSymbol = context.getResolvedSymbol(id)) != null) {
 			if (resolvedSymbol instanceof AliasDeclaration) {
 				AliasDeclaration alias = (AliasDeclaration) resolvedSymbol;
 				if (alias.isImportAlias) {
 					resolvedSymbol = alias.aliassym;
 				}
 			}
 
 			IBinding binding = resolveDsymbol(resolvedSymbol, node);
 			if (binding != null) {
 				return binding;
 			}
 		}
 
 		Expression resolved = context.getResolvedExp(id);
 		if (resolved == null) {
 			return null;
 		}
 
 		switch (resolved.getNodeType()) {
 		case ASTDmdNode.VAR_EXP:
 			return resolveVarExp((VarExp) resolved);
 		case ASTDmdNode.DOT_VAR_EXP:
 			return resolveDotVarExp((DotVarExp) resolved);
 		case ASTDmdNode.TYPE_EXP:
 			return resolveType(((TypeExp) resolved).type, node);
 		}
 
 		return null;
 	}
 
 	IBinding resolveDsymbol(Dsymbol sym, ASTNode original) {
 		if (sym == null) {
 			return null;
 		}
 
 		sym = eraseTemplate(sym);
 
 		// If it is an opCall, use the parent
 		if (sym.isFuncDeclaration() != null && sym.ident != null
 				&& sym.ident.ident != null
 				&& CharOperation.equals(sym.ident.ident, Id.call)) {
 			sym = sym.effectiveParent();
 		}
 
 		switch (sym.getNodeType()) {
 		case ASTDmdNode.MODULE:
 			return resolveModule((Module) sym, original);
 		case ASTDmdNode.CLASS_DECLARATION:
 		case ASTDmdNode.STRUCT_DECLARATION:
 		case ASTDmdNode.UNION_DECLARATION:
 		case ASTDmdNode.INTERFACE_DECLARATION:
 		case ASTDmdNode.ENUM_DECLARATION:
 		case ASTDmdNode.TEMPLATE_DECLARATION:
 			return resolveAggregateOrEnum(sym, original);
 		case ASTDmdNode.VAR_DECLARATION:
 			return resolveVarDeclaration(sym, original);
 		case ASTDmdNode.ALIAS_DECLARATION:
 		case ASTDmdNode.TYPEDEF_DECLARATION:
 			return resolveAliasOrTypedefDeclaration(sym, original);
 		case ASTDmdNode.FUNC_DECLARATION:
 		case ASTDmdNode.CTOR_DECLARATION:
 			return resolveFuncDeclaration((FuncDeclaration) sym, original);
 		case ASTDmdNode.ENUM_MEMBER:
 			return resolveEnumMember((EnumMember) sym, original);
 		}
 		return null;
 	}
 
 	private Dsymbol eraseTemplate(Dsymbol sym) {
 		if (sym instanceof TemplateDeclaration) {
 			TemplateDeclaration temp = (TemplateDeclaration) sym;
 			if (temp.wrapper) {
 				return temp.members.get(0);
 			}
 		}
 		return sym;
 	}
 
 	private IBinding resolveVarExp(VarExp varExp) {
 		return null;
 	}
 
 	private IBinding resolveDotVarExp(DotVarExp dotVarExp) {
 		return null;
 	}
 
 	@Override
 	IBinding resolveType(descent.core.dom.Type type) {
 		ASTDmdNode old = newAstToOldAst.get(type);
 		if (old instanceof descent.internal.compiler.parser.Type) {
 			return resolveType((Type) old, type);
 		} else if (old instanceof IdentifierExp) {
 			return resolveIdentifierExp(type, (IdentifierExp) old);
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	IVariableBinding resolveEnumMember(descent.core.dom.EnumMember member) {
 		ASTDmdNode old = newAstToOldAst.get(member);
 		if (!(old instanceof descent.internal.compiler.parser.EnumMember)) {
 			return null;
 		}
 
 		descent.internal.compiler.parser.EnumMember em = (descent.internal.compiler.parser.EnumMember) old;
 		return resolveEnumMember(em, member);
 	}
 	
 	@Override
 	ICompilationUnitBinding resolveCompilationUnit(CompilationUnit unit) {
 		ASTDmdNode old = newAstToOldAst.get(unit);
 		if (!(old instanceof descent.internal.compiler.parser.Module)) {
 			return null;
 		}
 		
 		return resolveModule((Module) old, unit);
 	}
 
 	@Override
 	ICompilationUnitBinding resolveImport(descent.core.dom.Import imp) {
 		ASTDmdNode old = newAstToOldAst.get(imp);
 		if (!(old instanceof descent.internal.compiler.parser.Import)) {
 			return null;
 		}
 
 		descent.internal.compiler.parser.Import i = (descent.internal.compiler.parser.Import) old;
 		return resolveModule(i.mod, imp);
 	}
 
 	@Override
 	IMethodBinding resolveMethod(FunctionDeclaration method) {
 		ASTDmdNode old = newAstToOldAst.get(method);
 		if (!(old instanceof FuncDeclaration)) {
 			return null;
 		}
 
 		FuncDeclaration func = (FuncDeclaration) old;
 		return resolveFuncDeclaration(func, method);
 	}
 
 	@Override
 	public IVariableBinding resolveArgument(descent.core.dom.Argument argument) {
 		ASTDmdNode old = newAstToOldAst.get(argument);
 		if (!(old instanceof descent.internal.compiler.parser.Argument)) {
 			return null;
 		}
 
 		descent.internal.compiler.parser.Argument arg = (descent.internal.compiler.parser.Argument) old;
 		if (arg.var != null) {
 			if (arg.var instanceof VarDeclaration) {
 				return resolveVarDeclaration((VarDeclaration) arg.var, argument);
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	ITypeBinding resolveExpressionType(descent.core.dom.Expression expression) {
 		ASTDmdNode old = newAstToOldAst.get(expression);
 		if (!(old instanceof descent.internal.compiler.parser.Expression)) {
 			return null;
 		}
 
 		descent.internal.compiler.parser.Expression exp = (Expression) old;
 
 		if (expression instanceof Name) {
 			IBinding binding = resolveName((Name) expression);
 			if (binding != null) {
 				if (binding instanceof ITypeBinding) {
 					return (ITypeBinding) binding;
 				} else if (binding instanceof IVariableBinding) {
 					return ((IVariableBinding) binding).getType();
 				}
 			}
 			return null;
 		} else {
 			return resolveType(exp.type, expression);
 		}
 	}
 
 	/*
 	 * Method declared on BindingResolver.
 	 */
 	@Override
 	synchronized void store(ASTNode node, ASTDmdNode oldASTNode) {
 		this.newAstToOldAst.put(node, oldASTNode);
 	}
 
 	ITypeBinding resolveType(Type type, ASTNode original) {
 		if (type instanceof TypeIdentifier) {
 			return new TemplateParameterTypeBinding((TypeIdentifier) type);
 		}
 		
 		String key = getSignature(type);
 		if (key == null) {
 			return null;
 		}
 
 		IBinding binding = bindingTables.bindingKeysToBindings.get(key);
 		if (binding instanceof ITypeBinding) {
 			return (ITypeBinding) binding;
 		}
 
 		switch (type.getElementType()) {
 		case ASTDmdNode.TYPE_A_ARRAY:
 			binding = new TypeAArrayBinding(this, (TypeAArray) type, key);
 			break;
 		case ASTDmdNode.TYPE_BASIC:
 			binding = new TypeBasicBinding(this, (TypeBasic) type);
 			break;
 		case ASTDmdNode.TYPE_CLASS:
 			binding = resolveAggregateOrEnum(((TypeClass) type).sym, original);
 			break;
 		case ASTDmdNode.TYPE_D_ARRAY:
 			binding = new TypeDArrayBinding(this, (TypeDArray) type, key);
 			break;
 		case ASTDmdNode.TYPE_DELEGATE:
 			binding = new TypeFunctionOrDelegateBinding(this,
 					(TypeFunction) ((TypeDelegate) type).next,
 					false /* is not function */, key);
 			break;
 		case ASTDmdNode.TYPE_ENUM:
 			binding = resolveAggregateOrEnum(((TypeEnum) type).sym, key, original);
 			break;
 		case ASTDmdNode.TYPE_FUNCTION:
 			binding = new TypeFunctionOrDelegateBinding(this,
 					(TypeFunction) type, true /* is function */, key);
 			break;
 		// TODO
 		case ASTDmdNode.TYPE_INSTANCE:
 			break;
 		case ASTDmdNode.TYPE_POINTER:
 			binding = new TypePointerBinding(this, (TypePointer) type, key);
 			break;
 		case ASTDmdNode.TYPE_S_ARRAY:
 			binding = new TypeSArrayBinding(this, (TypeSArray) type, key);
 			break;
 		case ASTDmdNode.TYPE_SLICE:
 			binding = new TypeSliceBinding(this, (TypeSlice) type, key);
 			break;
 		case ASTDmdNode.TYPE_STRUCT:
 			binding = resolveAggregateOrEnum(((TypeStruct) type).sym, key, original);
 			break;
 		// TODO
 		case ASTDmdNode.TYPE_TUPLE:
 			break;
 		// TODO
 		case ASTDmdNode.TYPE_TYPEDEF:
 			break;
 		}
 
 		if (binding != null) {
 			glue(key, binding, original);
 		}
 
 		return (ITypeBinding) binding;
 	}
 
 	private IMethodBinding resolveFuncDeclaration(FuncDeclaration func, ASTNode original) {
 		if (func.ident == null || func.ident.ident == null) {
 			return null;
 		}
 
 		String key = getSignature(func);
 		if (key == null) {
 			return null;
 		}
 
 		IBinding binding = bindingTables.bindingKeysToBindings.get(key);
 		if (binding == null) {
 			binding = new MethodBinding(this, func, key);
 			glue(key, binding, original);
 		}
 
 		if (binding != null && !(binding instanceof IMethodBinding)) {
 			throw new IllegalStateException();
 		}
 
 		return (IMethodBinding) binding;
 	}
 
 	private IVariableBinding resolveEnumMember(EnumMember em, ASTNode original) {
 		if (em.ident == null || em.ident.ident == null) {
 			return null;
 		}
 
 		String key = getSignature(em);
 		if (key == null) {
 			return null;
 		}
 
 		IBinding binding = bindingTables.bindingKeysToBindings.get(key);
 		if (binding == null) {
 			binding = new VariableBinding(this, em, key);
 			glue(key, binding, original);
 		}
 
 		if (binding != null && !(binding instanceof IVariableBinding)) {
 			throw new IllegalStateException();
 		}
 
 		return (IVariableBinding) binding;
 	}
 
 	ICompilationUnitBinding resolveModule(Module mod, ASTNode original) {
 		if (mod == null || mod.ident == null || mod.ident.ident == null) {
 			return null;
 		}
 
 		String key = getSignature(mod);
 		if (key == null) {
 			return null;
 		}
 
 		IBinding binding = bindingTables.bindingKeysToBindings.get(key);
 		if (binding == null) {
 			binding = new CompilationUnitBinding(this, mod, key);
 			glue(key, binding, original);
 		}
 
 		if (binding != null && !(binding instanceof ICompilationUnitBinding)) {
 			throw new IllegalStateException();
 		}
 
 		return (ICompilationUnitBinding) binding;
 	}
 	
 	private void glue(String key, IBinding binding, ASTNode original) {
 		bindingTables.bindingKeysToBindings.put(key, binding);
 		if (original != null) {
 			bindingsToAstNodesPut(binding, original);
 		}
 	}
 
 	private void bindingsToAstNodesPut(IBinding binding, ASTNode original) {
 		if (original instanceof Name) {
 			return;
 		}
 		bindingsToAstNodes.put(binding, original);
 	}
 
 	public IJavaElement resolveBinarySearch(Dsymbol node) {
 		try {
 			descent.core.ICompilationUnit unit = getCompilationUnit(node);
 			if (unit != null) {
 				return internalSignature.binarySearch(unit, node.start,
 						node.start + node.length);
 			}
 		} catch (JavaModelException e) {
 			Util.log(e);
 		}
 		return null;
 	}
 	
 	private String getSignature(Dsymbol sym) {
 		return sym.getSignature(ISignatureOptions.None);
 	}
 	
 	private String getSignature(Type type) {
 		return type.getSignature(ISignatureOptions.None);
 	}
 
 	descent.core.ICompilationUnit getCompilationUnit(Dsymbol node) {
 		Module module = node.getModule();
 		if (module == null) {
 			return null;
 		}
 		String fqn = module.getFullyQualifiedName();
 		return internalSignature.getCompilationUnit(fqn);
 	}
 
 }
