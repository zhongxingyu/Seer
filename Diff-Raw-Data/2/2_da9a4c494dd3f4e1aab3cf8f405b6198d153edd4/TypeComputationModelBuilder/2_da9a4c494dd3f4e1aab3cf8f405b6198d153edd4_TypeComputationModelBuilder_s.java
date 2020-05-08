 /*******************************************************************************
  * Copyright (c) 2009-2012 CWI
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   * Anastasia Izmaylova - A.Izmaylova@cwi.nl (CWI)
 *******************************************************************************/
 package prototype.org.rascalmpl.eclipse.library.lang.java.jdt.refactorings.internal;
 
 import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.ADT_ENTITY;
 import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_ENTITY;
 import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.ADT_ID;
 
 import java.lang.reflect.Modifier;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.imp.pdb.facts.IList;
 import org.eclipse.imp.pdb.facts.IListWriter;
 import org.eclipse.imp.pdb.facts.IMapWriter;
 import org.eclipse.imp.pdb.facts.ISetWriter;
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.IValueFactory;
 import org.eclipse.imp.pdb.facts.type.TypeFactory;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.ClassInstanceCreation;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.FieldAccess;
 import org.eclipse.jdt.core.dom.IBinding;
 import org.eclipse.jdt.core.dom.IMethodBinding;
 import org.eclipse.jdt.core.dom.IPackageBinding;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.IVariableBinding;
 import org.eclipse.jdt.core.dom.Initializer;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.core.dom.Name;
 import org.eclipse.jdt.core.dom.QualifiedName;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.SuperFieldAccess;
 import org.eclipse.jdt.core.dom.SuperMethodInvocation;
 import org.rascalmpl.eclipse.library.lang.java.jdt.internal.BindingsResolver;
 
 public class TypeComputationModelBuilder {
 	
 	private static final TypeFactory ftypes = TypeFactory.getInstance();
 	/*
 	 *  Type computation model as a set of functions on types: 
 	 */
 	private static final String EVALUATION_FUNCTION = "evaluation_func";
 	private static final String SUBTYPES_FUNCTION = "supertypes_func";
 	private static final String DECLARES_FUNCTION = "declares_func";
 	private static final String OVERRIDES_FUNCTION = "overrides_func";
 	private static final String BOUNDS_FUNCTION = "bounds_func";
 	
 	private static final String IsSTATIC_DECL_FUNCTION = "isStaticDecl_func";
 	
 	private ISetWriter evaluation_func;
 	private ISetWriter subtypes_func;
 	private ISetWriter declares_func;  // performs lookup into supertypes
 	private ISetWriter overrides_func; // performs lookup into supertypes
 	private ISetWriter bounds_func; 
 	private ISetWriter isStaticDecl_func;
 
 	
 	private IMapWriter semantics_of_paramaterized_types_func;
 	
 	private IValue typeComputationModel;
 	private IValue semanticsOfParameterizedTypes;
 	
 	private final org.eclipse.imp.pdb.facts.type.Type functionTupleType = ftypes.tupleType(ADT_ENTITY, ADT_ENTITY);
 	private final org.eclipse.imp.pdb.facts.type.Type 
 		semantics_of_paramaterized_types = ftypes.tupleType( ftypes.tupleType( ftypes.listType(ADT_ENTITY), ftypes.listType(ADT_ENTITY) ), 
 														     ADT_ENTITY );
 	
 	private final IValueFactory values;
 	private final BindingConverter bindingConverter; 
 		
 	@SuppressWarnings("deprecation")
 	public TypeComputationModelBuilder(final IValueFactory values, final BindingConverter bindingConverter) {	
 		this.values = values;
 		this.bindingConverter = bindingConverter;
 		
 		evaluation_func = values.setWriter(functionTupleType);
 		subtypes_func = values.setWriter(functionTupleType);
 		declares_func = values.setWriter(functionTupleType);
 		overrides_func = values.setWriter(functionTupleType);
 		bounds_func = values.setWriter(functionTupleType);
 		isStaticDecl_func = values.setWriter(functionTupleType);
 		semantics_of_paramaterized_types_func = values.mapWriter(ADT_ENTITY, semantics_of_paramaterized_types);
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void extract(CompilationUnit cu) {
 		IMapWriter computations = values.mapWriter(ftypes.stringType(), ftypes.relTypeFromTuple(functionTupleType));
 		TypeValuesCollector collector = new TypeValuesCollector(this.bindingConverter);
 		cu.accept(collector);
 		
 		computations.put(values.string(EVALUATION_FUNCTION), evaluation_func.done());
 		computations.put(values.string(SUBTYPES_FUNCTION), subtypes_func.done());
 		computations.put(values.string(DECLARES_FUNCTION), declares_func.done());
 		computations.put(values.string(OVERRIDES_FUNCTION), overrides_func.done());
 		computations.put(values.string(BOUNDS_FUNCTION), bounds_func.done());
 		computations.put(values.string(IsSTATIC_DECL_FUNCTION), isStaticDecl_func.done());
		
 		typeComputationModel = computations.done();
 		semanticsOfParameterizedTypes = semantics_of_paramaterized_types_func.done();
 	}
 	
 	public IValue getTypeComputationModel() { 
 		return this.typeComputationModel; 
 	}
 	
 	public IValue getSemanticsOfParameterizedTypes() { 
 		return this.semanticsOfParameterizedTypes; 
 	}
 	
 	class TypeValuesCollector extends ASTVisitor {
 		
 		private final BindingsImporter bindingsImporter;
 		
 		public TypeValuesCollector(final BindingConverter bindingConverter) {
 			bindingsImporter = new BindingsImporter(bindingConverter);	
 		}
 		
 		public void preVisit(ASTNode node) {
 			bindingsImporter.resolveBindings(node);
 			bindingsImporter.manageStacks(node, true);
 		}
 		
 		public void postVisit(ASTNode node) {
 			bindingsImporter.manageStacks(node, false);
 		}
 		
 		class BindingsImporter extends BindingsResolver {	
 
 			private final Set<Object> bindings = new HashSet<Object>();
 			private final Map<Object, Object> declares = new HashMap<Object, Object>();
 			private final Map<Object, Object> declaresChecks = new HashMap<Object, Object>();
 			private final Map<Object, Object> overridesChecks = new HashMap<Object, Object>();
 			
 			public BindingsImporter(BindingConverter bindingConverter) {
 				super(bindingConverter);
 			}			
 			
 			/*
 			 * Takes care of the 'DECLARES' semantics 
 			 */
 			public void resolveBindings(ClassInstanceCreation node) {
 				super.resolveBindings(node);
 				Expression e = node.getExpression();
 				if(e != null) 
 					importDeclaresSemantics(e.resolveTypeBinding(), node.resolveConstructorBinding().getDeclaringClass());
 			}
 
 			public void resolveBindings(FieldAccess node) {
 				super.resolveBindings(node);
 				Expression e = node.getExpression();
 				importDeclaresSemantics(e.resolveTypeBinding(), node.resolveFieldBinding());
 			}
 
 			public void resolveBindings(SuperFieldAccess node) {
 				super.resolveBindings(node);
 				Name qualifier = node.getQualifier();
 				if(qualifier != null) 
 					importDeclaresSemantics(qualifier.resolveTypeBinding(), node.resolveFieldBinding());
 				else {
 					ITypeBinding scope = getEnclosingType();
 					if(scope != null) 
 						importDeclaresSemantics(scope, node.resolveFieldBinding());
 				}
 			}
 
 			public void resolveBindings(MethodInvocation node) {
 				super.resolveBindings(node);
 				Expression e = node.getExpression();
 				if(e !=null) 
 					importDeclaresSemantics(e.resolveTypeBinding(), node.resolveMethodBinding());
 				else {
 					ITypeBinding scope = getEnclosingType();
 					if(scope != null) 
 						importDeclaresSemantics(scope, node.resolveMethodBinding());
 				}
 			}
 
 			public void resolveBindings(SuperMethodInvocation node) {
 				super.resolveBindings(node);
 				Name qualifier = node.getQualifier();
 				if(qualifier != null) 
 					importDeclaresSemantics(qualifier.resolveTypeBinding(), node.resolveMethodBinding());
 				else {
 					ITypeBinding scope = getEnclosingType();
 					if(scope != null) 
 						importDeclaresSemantics(scope, node.resolveMethodBinding());
 				}
 			}
 			
 			public void resolveBindings(Name node) {
 				super.resolveBindings(node);
 				if(node instanceof QualifiedName) {
 					QualifiedName qn = (QualifiedName) node;
 					IBinding binding = qn.resolveBinding();
 					if(binding instanceof IVariableBinding) {
 						IVariableBinding vbinding = (IVariableBinding) binding;
 						if(vbinding.isField()) 
 							importDeclaresSemantics(qn.getQualifier().resolveTypeBinding(), vbinding);
 					}
 				}
 				if(node instanceof SimpleName && !(node.getParent() instanceof QualifiedName)) {
 					SimpleName sn = (SimpleName) node;
 					IBinding binding = sn.resolveBinding();
 					if(binding instanceof IVariableBinding) {
 						IVariableBinding vbinding = (IVariableBinding) binding;
 						if(vbinding.isField()) {
 							ITypeBinding scope = getEnclosingType();
 							if(scope != null) 
 								importDeclaresSemantics(scope, vbinding);
 						}
 					}
 				}	
 			}
 			
 			public void resolveBindings(MethodDeclaration node) {	
 				super.resolveBindings(node);
 				importDeclaresSemantics(node.resolveBinding().getDeclaringClass(), node.resolveBinding());
 				importOverrideSemantics(node.resolveBinding(), node.resolveBinding().getDeclaringClass());
 			}
 			
 			/*
 			 * Takes care of the 'PARAMETERIZED TYPES' and 'EVALUATION' semantics 
 			 */
 			public void importBinding(IMethodBinding binding) {
 				if(bindings.contains(binding.getKey())) return ; 
 				bindings.add(binding.getKey());
 				
 				importEvaluationSemantics(binding);	
 				importBinding(binding.getReturnType(), null);
 				for(ITypeBinding ptype : binding.getParameterTypes()) importBinding(ptype, null);
 				importBinding(binding.getDeclaringClass(), null);
 				importSemanticsOfParameterizedTypes(binding);
 				importBinding(binding.getMethodDeclaration());
 				importStaticSemantics(binding);
 				importStaticSemantics(binding.getMethodDeclaration());
 			}
 			
 			public void importBinding(ITypeBinding binding, Initializer initializer) {
 				if(bindings.contains(binding.getKey())) return ;
 				bindings.add(binding.getKey());
 				
 				if(binding.isTypeVariable() || binding.isCapture() || binding.isWildcardType()) {	
 					if(binding.getWildcard() != null)                   // Captures
 						importBinding(binding.getWildcard(), null);   
 					if(binding.getBound() != null)                      // WildCards
 						importBinding(binding.getBound(), null);       
 					for(ITypeBinding bound : binding.getTypeBounds()) { // Type variables or captures
 						importBinding(bound, null); 
 						bounds_func.insert(values.tuple(bindingConverter.getEntity(binding), bindingConverter.getEntity(bound)));
 					}
 					return ;
 				}
 				importSupertypesSemantics(binding, initializer);
 				importSemanticsOfParameterizedTypes(binding, initializer);
 				importBinding(binding.getTypeDeclaration(), null);
 			}
 			
 			public void importBinding(IVariableBinding binding, Initializer initializer) {
 				if(bindings.contains(binding.getKey())) return ;
 				bindings.add(binding.getKey());
 				
 				importEvaluationSemantics(binding, initializer);
 				importBinding(binding.getType(), null);
 				if(binding.getDeclaringClass() != null) importBinding(binding.getDeclaringClass(), null);
 				importSemanticsOfParameterizedTypes(binding, initializer);
 				importBinding(binding.getVariableDeclaration(), null);
 				importStaticSemantics(binding, initializer);
 				importStaticSemantics(binding.getVariableDeclaration(), null);
 			}
 
 			private void importEvaluationSemantics(IMethodBinding binding) {
 				evaluation_func.insert(values.tuple(bindingConverter.getEntity(binding), bindingConverter.getEntity(binding.getReturnType())));
 			}
 			
 			private void importEvaluationSemantics(IVariableBinding binding, Initializer initializer) {
 				evaluation_func.insert(values.tuple(bindingConverter.getEntity(binding, initializer), bindingConverter.getEntity(binding.getType())));
 			}
 
 			@SuppressWarnings("deprecation")
 			private final IValue zerobindings = values.tuple(values.listWriter(ADT_ENTITY).done(), values.listWriter(ADT_ENTITY).done());
 			
 			private void importSemanticsOfParameterizedTypes(IMethodBinding binding) {
 				IValue bindings = zerobindings;
 				if(!binding.getKey().equals(binding.getMethodDeclaration().getKey())) 
 					bindings = values.tuple(getTypeArguments(binding), getTypeParameters(binding.getMethodDeclaration()));
 				semantics_of_paramaterized_types_func
 						.put(bindingConverter.getEntity(binding), values.tuple(bindings, bindingConverter.getEntity(binding.getMethodDeclaration())));
 			}
 			
 			private void importSemanticsOfParameterizedTypes(ITypeBinding binding, Initializer initializer) {
 				IValue bindings = zerobindings;
 				if(!(binding.isTypeVariable() || binding.isWildcardType() || binding.isCapture())) 
 				   if(!binding.getKey().equals(binding.getTypeDeclaration().getKey())) 
 					   bindings = values.tuple(getTypeArguments(binding), getTypeParameters(binding.getTypeDeclaration()));
 				semantics_of_paramaterized_types_func
 						.put(bindingConverter.getEntity(binding, initializer), values.tuple(bindings, bindingConverter.getEntity(binding.getTypeDeclaration())));
 			}
 			
 			private void importSemanticsOfParameterizedTypes(IVariableBinding binding, Initializer initializer) {
 				IValue bindings = zerobindings;
 				if(!binding.getKey().equals(binding.getVariableDeclaration().getKey())) 
 					bindings = values.tuple(getTypeArguments(binding), getTypeParameters(binding.getVariableDeclaration()));
 				semantics_of_paramaterized_types_func
 						.put(bindingConverter.getEntity(binding, initializer), values.tuple(bindings, bindingConverter.getEntity(binding.getVariableDeclaration())));
 			}
 
 			@SuppressWarnings("deprecation")
 			private IList getTypeArguments(IMethodBinding binding) {
 				IListWriter args = values.listWriter(ADT_ENTITY);
 				if(binding.getTypeArguments().length != 0) 
 					for(ITypeBinding arg: binding.getTypeArguments()) {
 						args.append(bindingConverter.getEntity(arg));
 						importBinding(arg, null);
 					}
 				else 
 					for(@SuppressWarnings("unused") ITypeBinding param : binding.getMethodDeclaration().getTypeParameters()) 
 						args.append(createZeroEntity());
 				
 				if(binding.getDeclaringClass() != null) 
 					return getTypeArguments(binding.getDeclaringClass()).concat(args.done());
 				return args.done();
 			}
 			
 			@SuppressWarnings("deprecation")
 			private IList getTypeArguments(ITypeBinding binding) {
 				IListWriter args = values.listWriter(ADT_ENTITY);
 				if(binding.getTypeArguments().length != 0) 
 					for(ITypeBinding arg: binding.getTypeArguments()) {
 						args.append(bindingConverter.getEntity(arg));
 						importBinding(arg, null);
 					}
 				else 
 					for(@SuppressWarnings("unused") ITypeBinding param : binding.getTypeDeclaration().getTypeParameters()) 
 						args.append(createZeroEntity());
 				
 				if(binding.getTypeDeclaration().isLocal()) 
 					return args.done(); 
 				if(binding.getDeclaringClass() != null) 
 					return getTypeArguments(binding.getDeclaringClass()).concat(args.done()); 
 				return args.done();
 			}
 			
 			@SuppressWarnings("deprecation")
 			private IList getTypeArguments(IVariableBinding binding) {
 				IListWriter args = values.listWriter(ADT_ENTITY);
 				if(binding.getDeclaringMethod() != null) 
 					return args.done();
 				if(binding.getDeclaringClass() != null) 
 					return getTypeArguments(binding.getDeclaringClass()).concat(args.done());
 				return args.done();
 			}
 			
 			@SuppressWarnings("deprecation")
 			private IList getTypeParameters(IMethodBinding binding) {
 				IListWriter params = values.listWriter(ADT_ENTITY);
 				for(ITypeBinding param: binding.getTypeParameters()) {
 					params.append(bindingConverter.getEntity(param));
 					importBinding(param, null);
 				}
 				if(binding.getDeclaringClass() != null) 
 					return getTypeParameters(binding.getDeclaringClass()).concat(params.done());
 				return params.done();
 			}
 			
 			@SuppressWarnings("deprecation")
 			private IList getTypeParameters(ITypeBinding binding) {
 				IListWriter params = values.listWriter(ADT_ENTITY);
 				for(ITypeBinding param: binding.getTypeParameters()) {
 					params.append(bindingConverter.getEntity(param));
 					importBinding(param, null);
 				}
 				if(binding.isLocal()) 
 					return params.done(); 
 				if(binding.getDeclaringClass() != null) 
 					return getTypeParameters(binding.getDeclaringClass()).concat(params.done());
 				return params.done();
 			};
 			
 			@SuppressWarnings("deprecation")
 			private IList getTypeParameters(IVariableBinding binding) {
 				IListWriter params = values.listWriter(ADT_ENTITY);
 				if(binding.getDeclaringMethod() != null) 
 					return params.done(); 
 				if(binding.getDeclaringClass() != null) 
 					return getTypeParameters(binding.getDeclaringClass()).concat(params.done());
 				return params.done();
 			}
 
 			private void importSupertypesSemantics(ITypeBinding binding, Initializer initializer) {
 				ITypeBinding supType = binding.getSuperclass();
 				if(supType != null) {
 					subtypes_func.insert(values.tuple(bindingConverter.getEntity(binding, initializer), bindingConverter.getEntity(supType)));
 					importBinding(supType, null);
 				}
 				for(ITypeBinding sup : binding.getInterfaces()) {
 					subtypes_func.insert(values.tuple(bindingConverter.getEntity(binding, initializer), bindingConverter.getEntity(sup)));
 					importBinding(sup, null);
 				}
 			}	
 
 			private boolean importDeclaresSemantics(ITypeBinding decl, IMethodBinding binding) {
 				String keyofdecl = decl.getKey();
 				if(declaresChecks.containsKey(keyofdecl) && declaresChecks.get(keyofdecl).equals(binding.getKey())) {
 					if(declares.containsKey(keyofdecl) && declares.get(keyofdecl).equals(binding.getKey())) 
 						return true;
 					else return false;
 				}
 				boolean found = false;
 				for(IMethodBinding method : decl.getDeclaredMethods()) 
 					if(binding.getKey().equals(method.getKey())) { found = true; break; }	
 				if(!found) {
 					ITypeBinding supType = decl.getSuperclass();
 					if(supType != null && importDeclaresSemantics(supType, binding)) found = true;
 				}
 				if(!found)
 					for(ITypeBinding sup : decl.getInterfaces())
 						if(importDeclaresSemantics(sup, binding)) { found = true; break; }
 				if(found) {
 					declares_func.insert(values.tuple(bindingConverter.getEntity(decl), bindingConverter.getEntity(binding)));
 					declares.put(decl.getKey(), binding.getKey());
 				}
 				declaresChecks.put(keyofdecl, binding.getKey());
 				return found;
 			}
 			
 			private boolean importDeclaresSemantics(ITypeBinding decl, ITypeBinding binding) {
 				String keyofdecl = decl.getKey();
 				if(declaresChecks.containsKey(keyofdecl) && declaresChecks.get(keyofdecl).equals(binding.getKey())) {
 					if(declares.containsKey(keyofdecl) && declares.get(keyofdecl).equals(binding.getKey())) 
 						return true;
 					else return false;
 				}
 				boolean found = false;
 				for(ITypeBinding type : decl.getDeclaredTypes()) 
 					if(binding.getKey().equals(type.getKey())) { found = true; break; }
 				if(!found) {
 					ITypeBinding supType = decl.getSuperclass();
 					if(supType != null && importDeclaresSemantics(supType, binding)) found = true;
 				}
 				if(!found)
 					for(ITypeBinding sup : decl.getInterfaces())
 						if(importDeclaresSemantics(sup, binding)) { found = true; break; }	
 				if(found) {
 					declares_func.insert(values.tuple(bindingConverter.getEntity(decl), bindingConverter.getEntity(binding)));
 					declares.put(decl.getKey(), binding.getKey());
 				}
 				declaresChecks.put(keyofdecl, binding.getKey());
 				return found;
 			}
 			
 			private boolean importDeclaresSemantics(ITypeBinding decl, IVariableBinding binding) {
 				String keyofdecl = decl.getKey();
 				if(declaresChecks.containsKey(keyofdecl) && declaresChecks.get(keyofdecl).equals(binding.getKey())) {
 					if(declares.containsKey(keyofdecl) && declares.get(keyofdecl).equals(binding.getKey())) 
 						return true;
 					else return false;
 				}
 				boolean found = false;
 				for(IVariableBinding field : decl.getDeclaredFields()) 
 					if(binding.getKey().equals(field.getKey())) { found = true; break; }
 				if(!found) {
 					ITypeBinding supType = decl.getSuperclass();
 					if(supType != null && importDeclaresSemantics(supType, binding)) found = true;
 				}
 				if(!found)
 					for(ITypeBinding sup : decl.getInterfaces())
 						if(importDeclaresSemantics(sup, binding)) { found = true; break; }
 				if(found) { 
 					declares_func.insert(values.tuple(bindingConverter.getEntity(decl), bindingConverter.getEntity(binding)));
 					declares.put(decl.getKey(), binding.getKey());
 				}
 				declaresChecks.put(keyofdecl, binding.getKey());
 				return found;
 			}			
 
 			private void importOverrideSemantics(IMethodBinding binding, ITypeBinding type) {
 				String keyofbinding = binding.getKey();
 				if(overridesChecks.containsKey(keyofbinding) && overridesChecks.get(binding.getKey()).equals(type.getKey())) return ;
 				boolean found = false;
 				if(binding.getDeclaringClass().getKey().equals(type)) 
 					overrides_func.insert(values.tuple(bindingConverter.getEntity(binding), bindingConverter.getEntity(binding)));
 				else
 					for(IMethodBinding method : type.getDeclaredMethods()) 
 						if(binding.overrides(method)) {
 							overrides_func.insert(values.tuple(bindingConverter.getEntity(binding), bindingConverter.getEntity(method)));
 							importBinding(method);
 							found = true; break;
 						}
 				if(!found) {
 					ITypeBinding supType = type.getSuperclass();
 					if(supType != null) 
 						importOverrideSemantics(binding, supType);
 					for(ITypeBinding i : type.getInterfaces()) 
 						importOverrideSemantics(binding, i);
 				}
 				overridesChecks.put(binding.getKey(), type.getKey());
 			}
 			
 			private void importStaticSemantics(IMethodBinding binding) {
 				if(Modifier.isStatic(binding.getModifiers())) {
 					IValue mb = bindingConverter.getEntity(binding);
 					isStaticDecl_func.insert(values.tuple(mb, mb));
 				}
 			}
 			
 			private void importStaticSemantics(IVariableBinding binding, Initializer initializer) {
 				if(Modifier.isStatic(binding.getModifiers())) {
 					IValue vb = bindingConverter.getEntity(binding, initializer);
 					isStaticDecl_func.insert(values.tuple(vb, vb));
 				}
 			}
 			
 			@SuppressWarnings("deprecation")
 			private IValue createZeroEntity() {
 				return values.constructor(CONS_ENTITY, values.listWriter(ADT_ID).done());
 			}
 
 			public void importBinding(IPackageBinding binding) {}
 		}
 	}
 }
