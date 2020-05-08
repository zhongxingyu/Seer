 package subobjectjava.translate;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import jnome.core.expression.invocation.ConstructorInvocation;
 import jnome.core.language.Java;
 import jnome.core.type.BasicJavaTypeReference;
 import jnome.core.type.JavaTypeReference;
 
 import org.rejuse.association.SingleAssociation;
 import org.rejuse.logic.ternary.Ternary;
 import org.rejuse.predicate.UnsafePredicate;
 
 import subobjectjava.model.component.AbstractClause;
 import subobjectjava.model.component.ComponentRelation;
 import subobjectjava.model.component.ConfigurationBlock;
 import subobjectjava.model.component.ConfigurationClause;
 import subobjectjava.model.component.RenamingClause;
 import subobjectjava.model.expression.SubobjectConstructorCall;
 import subobjectjava.model.language.SubobjectJavaOverridesRelation;
 import chameleon.core.declaration.CompositeQualifiedName;
 import chameleon.core.declaration.Declaration;
 import chameleon.core.declaration.QualifiedName;
 import chameleon.core.declaration.Signature;
 import chameleon.core.declaration.SimpleNameSignature;
 import chameleon.core.declaration.TargetDeclaration;
 import chameleon.core.element.Element;
 import chameleon.core.expression.ActualArgument;
 import chameleon.core.expression.Expression;
 import chameleon.core.expression.Invocation;
 import chameleon.core.expression.NamedTargetExpression;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.SelectorWithoutOrder;
 import chameleon.core.member.Member;
 import chameleon.core.method.Method;
 import chameleon.core.method.RegularImplementation;
 import chameleon.core.method.RegularMethod;
 import chameleon.core.method.exception.ExceptionClause;
 import chameleon.core.modifier.Modifier;
 import chameleon.core.namespacepart.Import;
 import chameleon.core.namespacepart.NamespacePart;
 import chameleon.core.reference.SimpleReference;
 import chameleon.core.statement.Block;
 import chameleon.core.variable.FormalParameter;
 import chameleon.core.variable.VariableDeclaration;
 import chameleon.exception.ChameleonProgrammerException;
 import chameleon.oo.language.ObjectOrientedLanguage;
 import chameleon.oo.type.BasicTypeReference;
 import chameleon.oo.type.RegularType;
 import chameleon.oo.type.Type;
 import chameleon.oo.type.TypeReference;
 import chameleon.oo.type.generics.InstantiatedTypeParameter;
 import chameleon.oo.type.generics.TypeParameter;
 import chameleon.oo.type.generics.TypeParameterSubstitution;
 import chameleon.oo.type.inheritance.SubtypeRelation;
 import chameleon.support.expression.AssignmentExpression;
 import chameleon.support.expression.SuperConstructorDelegation;
 import chameleon.support.expression.SuperTarget;
 import chameleon.support.expression.ThisLiteral;
 import chameleon.support.member.simplename.SimpleNameMethodHeader;
 import chameleon.support.member.simplename.SimpleNameMethodSignature;
 import chameleon.support.member.simplename.method.NormalMethod;
 import chameleon.support.member.simplename.method.RegularMethodInvocation;
 import chameleon.support.member.simplename.variable.MemberVariableDeclarator;
 import chameleon.support.modifier.Protected;
 import chameleon.support.modifier.Public;
 import chameleon.support.statement.ReturnStatement;
 import chameleon.support.statement.StatementExpression;
 import chameleon.util.Util;
 
 public class JavaTranslator {
 
 //	public JavaTranslator(SubobjectJava language, ElementProvider<Namespace> namespaceProvider) throws ParseException, IOException {
 //		_sourceLanguage = language;
 //		_typeProvider = new BasicDescendantProvider<Type>(namespaceProvider, Type.class);
 //	}
 	
 	
 	/**
 	 * Return a type that represents the translation of the given JLow class to a Java class.
 	 */
 	public Type translation(Type original) throws ChameleonProgrammerException, LookupException {
 		Type type = original.clone();
 		type.setUniParent(original.parent());
 		List<ComponentRelation> relations = original.directlyDeclaredMembers(ComponentRelation.class);
 		for(ComponentRelation relation : relations) {
       //ensureTranslation(relation.componentType());
 			// Add a field subobject
 //			MemberVariableDeclarator fieldForComponent = fieldForComponent(relation,type);
 //			if(fieldForComponent != null) {
 //			  type.add(fieldForComponent);
 //			}
 			
 			// Add a getter for subobject
 			Method getterForComponent = getterForComponent(relation,type);
 			if(getterForComponent != null) {
 				type.add(getterForComponent);
 			}
 
 			// Add a setter for subobject
 			Method setterForComponent = setterForComponent(relation,type);
 			if(setterForComponent != null) {
 				type.add(setterForComponent);
 			}
 			
 			//type.addAll(aliasMethods(relation));
 			
 			// Create the inner classes for the components
 			inner(type, relation, type);
       type.flushCache();
   		addOutwardDelegations(relation, type);
   		type.flushCache();
   		// Replace constructor calls
   		
   		//translate inner classes
 
 		}
 		
 		replaceSuperCalls(type);
 		for(ComponentRelation relation: type.directlyDeclaredMembers(ComponentRelation.class)) {
 //			replaceSuperCalls(relation, type);
 			replaceConstructorCalls(relation);
 
 			MemberVariableDeclarator fieldForComponent = fieldForComponent(relation,type);
 			if(fieldForComponent != null) {
 			  type.add(fieldForComponent);
 			}
 			
 			relation.disconnect();
 		}
 		type.setUniParent(null);
 		return type;
 	}
 	
 	public void replaceConstructorCalls(final ComponentRelation relation) throws LookupException {
 		Type type = relation.nearestAncestor(Type.class);
 		List<SubobjectConstructorCall> constructorCalls = type.descendants(SubobjectConstructorCall.class, new UnsafePredicate<SubobjectConstructorCall,LookupException>() {
 			@Override
 			public boolean eval(SubobjectConstructorCall constructorCall) throws LookupException {
 				return constructorCall.getTarget().getElement().equals(relation);
 			}
 		}
 		);
 		for(SubobjectConstructorCall call: constructorCalls) {
 			Invocation inv = new ConstructorInvocation((BasicJavaTypeReference) innerClassTypeReference(relation, type), null);
 			// move actual arguments from subobject constructor call to new constructor call. 
 			inv.addAllArguments(call.actualArgumentList().getActualParameters());
 			Invocation setterCall = new RegularMethodInvocation(setterName(relation), null);
 			setterCall.addArgument(new ActualArgument(inv));
 			SingleAssociation<SubobjectConstructorCall, Element> parentLink = call.parentLink();
 			parentLink.getOtherRelation().replace(parentLink, setterCall.parentLink());
 		}
 	}
 
 	public void inner(Type type, ComponentRelation relation, Type outer) throws LookupException {
 		Type innerClass = createInnerClassFor(relation,type);
 		//_innerClassMap.put(relation, type);
 		type.add(innerClass);
 		Type componentType = relation.componentType();
 		for(ComponentRelation nestedRelation: componentType.members(ComponentRelation.class)) {
 			// subst parameters
 			ComponentRelation clonedNestedRelation = nestedRelation.clone();
 			clonedNestedRelation.setUniParent(nestedRelation.parent());
 			substituteTypeParameters(clonedNestedRelation, componentType);
 			inner(innerClass, clonedNestedRelation, outer);
 		}
 	}
 	
 	private Map<ComponentRelation, Type> _innerClassMap = new HashMap<ComponentRelation, Type>();
 	
 	public void addOutwardDelegations(ComponentRelation relation, Type outer) throws LookupException {
 		ConfigurationBlock block = relation.configurationBlock();
 		for(ConfigurationClause clause: block.clauses()) {
 			if(clause instanceof AbstractClause) {
 				
 				AbstractClause ov = (AbstractClause)clause;
 				QualifiedName qn = ov.oldFqn();
 				final QualifiedName poppedName = qn.popped();
 				int size = poppedName.length();
 				TargetDeclaration container = relation.componentType();
 				for(int i = 1; i<= size; i++) {
 					final int x = i;
 					SelectorWithoutOrder<Declaration> selector = 
 						new SelectorWithoutOrder<Declaration>(new SelectorWithoutOrder.SignatureSelector() {
 							public Signature signature() {
 								return poppedName.elementAt(x);
 							}}, Declaration.class);
 					
 //SimpleReference<Declaration> ref = new SimpleReference<Declaration>(poppedName, Declaration.class);
 //					ref.setUniParent(relation.parent());
 				    container = (TargetDeclaration) container.targetContext().lookUp(selector);//x ref.getElement();
 				}
 				final Signature lastSignature = qn.lastSignature();
 				SelectorWithoutOrder<Declaration> selector = 
 					new SelectorWithoutOrder<Declaration>(new SelectorWithoutOrder.SignatureSelector() {
 						public Signature signature() {
 							return lastSignature;
 						}}, Declaration.class);
 				
 //				SimpleReference<Declaration> ref = new SimpleReference<Declaration>(null, lastSignature.clone(), Declaration.class);
 //				ref.setUniParent(relation.parent());
 				Type targetInnerClass = targetInnerClass(outer, relation, poppedName);
 				Declaration decl = container.targetContext().lookUp(selector);
 				if(decl instanceof Method) {
 					Method<?,?,?,?> method = (Method<?, ?, ?, ?>) decl;
 				  Method original = createOriginal(method, original(method.name()));
 				  if(original != null) {
 				  	targetInnerClass.add(original);
 				  }
 				  Method outward = createOutward(method,((SimpleNameMethodSignature)ov.newSignature()).name(),relation);
 				  if(outward != null) {
 				  	targetInnerClass.add(outward);
 				  }
 				  if(ov instanceof RenamingClause) {
 				  	outer.add(createAlias(relation, method, ((SimpleNameMethodSignature)ov.newSignature()).name()));
 				  }
 				}
 			}
 		}
 	}
 	
 	public Method createAlias(ComponentRelation relation, Method<?,?,?,?> method, String newName) throws LookupException {
 		NormalMethod<?,?,?> result;
 		result = innerMethod(method, newName);
 		Block body = new Block();
 		result.setImplementation(new RegularImplementation(body));
 		Invocation invocation = invocation(result, original(method.name()));
 		TypeReference ref = getRelativeClassName(relation);
 		Expression target = new RegularMethodInvocation(getterName(relation), null);
 		invocation.setTarget(target);
 		substituteTypeParameters(method, result);
 		addImplementation(method, body, invocation);
 		return result;
 	}
 
 	
 	public Type targetInnerClass(Type outer, ComponentRelation relation, QualifiedName poppedName) throws LookupException {
 		List<Signature> sigs = new ArrayList<Signature>();
 		sigs.add(relation.signature());
 		sigs.addAll(poppedName.signatures());
 		CompositeQualifiedName innerName = new CompositeQualifiedName();
 		CompositeQualifiedName acc = new CompositeQualifiedName();
 //		innerName.append(outer.signature().clone());
 		for(Signature signature: sigs) {
 			acc.append(signature.clone());
 		  innerName.append(new SimpleNameSignature(innerClassName(outer, acc)));
 		}
 		SimpleReference<Type> tref = new SimpleReference<Type>(innerName, Type.class);
 		tref.setUniParent(outer);
 //		outer.setUniParent(relation.nearestAncestor(Type.class).parent());
 		Type result = tref.getElement();
 //		outer.setUniParent(null);
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param relation A component relation from either the original class, or one of its nested components.
 	 * @param outer The outer class being generated.
 	 */
 	public Type createInnerClassFor(ComponentRelation relation, Type outer) throws ChameleonProgrammerException, LookupException {
 		NamespacePart nsp = relation.farthestAncestor(NamespacePart.class);
 //		Type parentType = relation.nearestAncestor(Type.class);
 		Type componentType = relation.componentType();
 		NamespacePart originalNsp = componentType.farthestAncestor(NamespacePart.class);
 		for(Import imp: originalNsp.imports()) {
 			nsp.addImport(imp.clone());
 		}
 		Type stub = new RegularType(innerClassName(relation, outer));
 		
 		TypeReference superReference;
 		if(relation.nearestAncestor(Type.class).signature().equals(outer.signature()) && (outer.nearestAncestor(Type.class) == null)) {
 		  superReference = relation.componentTypeReference().clone();
 		} else {
 		   String innerClassName = innerClassName(relation, relation.nearestAncestor(Type.class));
 		  superReference = relation.language(Java.class).createTypeReference(innerClassName);
 		 }
 		stub.addInheritanceRelation(new SubtypeRelation(superReference));
 		List<Method> localMethods = componentType.directlyDeclaredMembers(Method.class);
 		for(Method<?,?,?,?> method: localMethods) {
 			if(method.is(method.language(ObjectOrientedLanguage.class).CONSTRUCTOR) == Ternary.TRUE) {
 				NormalMethod<?,?,?> clone = (NormalMethod) method.clone();
 				clone.setUniParent(method.parent());
 				for(BasicTypeReference<?> tref: clone.descendants(BasicTypeReference.class)) {
 					if(tref.getTarget() == null) {
 					  Type t = tref.getElement().baseType();
 					  if(t instanceof RegularType) {
 					  	String fqn = t.getFullyQualifiedName();
 					  	String qn = Util.getAllButLastPart(fqn);
 					  	if(qn != null && (! qn.isEmpty())) {
 					  		tref.setTarget(new SimpleReference<TargetDeclaration>(qn, TargetDeclaration.class));
 					  	}
 					  }
 					}
 				}
 				clone.setUniParent(null);
 				String name = stub.signature().name();
 				RegularImplementation impl = (RegularImplementation) clone.implementation();
 				Block block = new Block();
 				impl.setBody(block);
 				// substitute parameters before replace the return type, method name, and the body.
 				// the types are not known in the component type, and the super class of the component type
 				// may not have a constructor with the same signature as the current constructor.
 				substituteTypeParameters(method, clone);
 				Invocation inv = new SuperConstructorDelegation();
 				useParametersInInvocation(clone, inv);
 				block.addStatement(new StatementExpression(inv));
 				clone.setReturnTypeReference(relation.language(Java.class).createTypeReference(name));
 				((SimpleNameMethodHeader)clone.header()).setName(name);
 				stub.add(clone);
 			}
 		}
 		return stub;
 	}
 	public final static String SHADOW = "_subobject_";
 	
 	public Method createOutward(Method<?,?,?,?> method, String newName, ComponentRelation relation) throws LookupException {
 		NormalMethod<?,?,?> result;
 		if((method.is(method.language(ObjectOrientedLanguage.class).DEFINED) == Ternary.TRUE) && 
 				(method.is(method.language(ObjectOrientedLanguage.class).OVERRIDABLE) == Ternary.TRUE)) {
 			result = innerMethod(method, method.name());
 			Block body = new Block();
 			result.setImplementation(new RegularImplementation(body));
 			Invocation invocation = invocation(result, newName);
 			TypeReference ref = getRelativeClassName(relation);
 			ThisLiteral target = new ThisLiteral(ref);
 			invocation.setTarget(target);
 			substituteTypeParameters(method, result);
 			addImplementation(method, body, invocation);
 		} else {
 			result = null;
 		}
 		return result;
 	}
 	
 	public TypeReference getRelativeClassName(ComponentRelation relation) {
 		return relation.language(Java.class).createTypeReference(relation.nearestAncestor(Type.class).signature().name());
 	}
 	
 	public Method createOriginal(Method<?,?,?,?> method, String original) throws LookupException {
 		NormalMethod<?,?,?> result;
 		if((method.is(method.language(ObjectOrientedLanguage.class).DEFINED) == Ternary.TRUE) && 
 				(method.is(method.language(ObjectOrientedLanguage.class).OVERRIDABLE) == Ternary.TRUE)) {
 			result = innerMethod(method, original);
 			substituteTypeParameters(method, result);
 			Block body = new Block();
 			result.setImplementation(new RegularImplementation(body));
 			Invocation invocation = invocation(result, method.name());
 			invocation.setTarget(new SuperTarget());
 			addImplementation(method, body, invocation);
 		}
 		else {
 			result = null;
 		}
 		return result;
 	}
 
 	private void substituteTypeParameters(Method<?, ?, ?, ?> methodInTypeWhoseParametersMustBeSubstituted, NormalMethod<?, ?, ?> methodWhereActualTypeParametersMustBeFilledIn) throws LookupException {
 		methodWhereActualTypeParametersMustBeFilledIn.setUniParent(methodInTypeWhoseParametersMustBeSubstituted);
 		Type type = methodInTypeWhoseParametersMustBeSubstituted.nearestAncestor(Type.class);
 		substituteTypeParameters(methodWhereActualTypeParametersMustBeFilledIn, type);
 		methodWhereActualTypeParametersMustBeFilledIn.setUniParent(null);
 	}
 
 	private void addImplementation(Method<?, ?, ?, ?> method, Block body, Invocation invocation) throws LookupException {
 		if(method.returnType().equals(method.language(Java.class).voidType())) {
 			body.addStatement(new StatementExpression(invocation));
 		} else {
 			body.addStatement(new ReturnStatement(invocation));
 		}
 	}
 
 	private NormalMethod<?, ?, ?> innerMethod(Method<?, ?, ?, ?> method, String original) {
 		NormalMethod<?, ?, ?> result;
 		result = new NormalMethod(method.header().clone(), method.returnTypeReference().clone());
 		((SimpleNameMethodHeader)result.header()).setName(original);
 		ExceptionClause exceptionClause = method.getExceptionClause();
 		ExceptionClause clone = (exceptionClause != null ? exceptionClause.clone(): null);
 		result.setExceptionClause(clone);
 		result.addModifier(new Public());
 		return result;
 	}
 
 	public void substituteTypeParameters(Element<?, ?> result, Type type) throws LookupException {
 		List<TypeParameter> typeParameters = type.parameters();
 		List<TypeParameterSubstitution> substitutions = new ArrayList<TypeParameterSubstitution>();
 		for(TypeParameter par: typeParameters) {
 			if(par instanceof InstantiatedTypeParameter) {
 				substitutions.add(((InstantiatedTypeParameter)par).substitution(result));
 			}
 		}
 		for(TypeParameterSubstitution substitution: substitutions){
 			substitution.apply();
 		}
 	}
 	
 	public String innerClassName(Type outer, QualifiedName qn) {
 		StringBuffer result = new StringBuffer();
 		result.append(outer.signature().name());
 		result.append(SHADOW);
 		List<Signature> sigs = qn.signatures();
 		int size = sigs.size();
 		for(int i = 0; i < size; i++) {
 			result.append(((SimpleNameSignature)sigs.get(i)).name());
 			if(i < size - 1) {
 				result.append(SHADOW);
 			}
 		}
 		return result.toString();
 	}
 	
 	public String innerClassName(ComponentRelation relation, Type outer) throws LookupException {
 		return innerClassName(outer, relation.signature()); 
 	}
 	
 	public void replaceSuperCalls(Type type) throws LookupException {
 		List<SuperTarget> superTargets = type.descendants(SuperTarget.class, new UnsafePredicate<SuperTarget,LookupException>() {
 
 			@Override
 			public boolean eval(SuperTarget superTarget) throws LookupException {
 				return superTarget.getTargetDeclaration() instanceof ComponentRelation;
 			}
 			
 		}
 		);
 		for(SuperTarget superTarget: superTargets) {
 			Element<?,?> inv = superTarget.parent();
 			if(inv instanceof RegularMethodInvocation) {
 				RegularMethodInvocation call = (RegularMethodInvocation) inv;
 			  Invocation subObjectSelection = new RegularMethodInvocation(getterName((ComponentRelation) superTarget.getTargetDeclaration()), null);
 			  call.setTarget(subObjectSelection);
 			  call.setName(original(call.name()));
 			}
       
 		}
 	}
 	
 /*	public void replaceSuperCalls(final ComponentRelation relation, Type parent) throws LookupException {
 		List<SuperTarget> superTargets = parent.descendants(SuperTarget.class, new UnsafePredicate<SuperTarget,LookupException>() {
 
 			@Override
 			public boolean eval(SuperTarget superTarget) throws LookupException {
 				return superTarget.getTargetDeclaration().equals(relation);
 			}
 			
 		}
 		);
 		for(SuperTarget superTarget: superTargets) {
 			Element<?,?> inv = superTarget.parent();
 			if(inv instanceof RegularMethodInvocation) {
 				RegularMethodInvocation call = (RegularMethodInvocation) inv;
 			  Invocation subObjectSelection = new RegularMethodInvocation(getterName(relation), null);
 			  call.setTarget(subObjectSelection);
 			  call.setName(original(call.name()));
 			}
       
 		}
 	}*/
 	
 	public String original(String name) {
 		return "original__"+name;
 	}
 	
 	public MemberVariableDeclarator fieldForComponent(ComponentRelation relation, Type outer) throws LookupException {
 		if(! overrides(relation)) {
 			MemberVariableDeclarator result = new MemberVariableDeclarator(innerClassTypeReference(relation, outer));
 		  result.add(new VariableDeclaration(fieldName(relation)));
 		  return result;
 		} else {
 			return null;
 		}
 	}
 
 	private JavaTypeReference innerClassTypeReference(ComponentRelation relation, Type outer) throws LookupException {
 		return relation.language(Java.class).createTypeReference(innerClassName(relation, outer));
 	}
 	
 	public String getterName(ComponentRelation relation) {
 		return relation.signature().name()+COMPONENT;
 	}
 	
 	public final static String COMPONENT = "__component__lkjkberfuncye__";
 	
 	public Method getterForComponent(ComponentRelation relation, Type outer) throws LookupException {
 		if(! overrides(relation)) {
 			RegularMethod result = new NormalMethod(new SimpleNameMethodHeader(getterName(relation)), innerClassTypeReference(relation, outer));
 			result.addModifier(new Public());
 			Block body = new Block();
 			result.setImplementation(new RegularImplementation(body));
 			body.addStatement(new ReturnStatement(new NamedTargetExpression(fieldName(relation), null)));
 			return result;
 		} else {
 			return null;
 		}
 	}
 	
 	public String setterName(ComponentRelation relation) {
 		return "set"+COMPONENT+"__"+relation.signature().name();
 	}
 	
 	public Method setterForComponent(ComponentRelation relation, Type outer) throws LookupException {
 		if(! overrides(relation)) {
 		String name = relation.signature().name();
 		RegularMethod result = new NormalMethod(new SimpleNameMethodHeader(setterName(relation)), relation.language(Java.class).createTypeReference("void"));
 		result.header().addFormalParameter(new FormalParameter(name, innerClassTypeReference(relation,outer)));
 		result.addModifier(new Protected());
 		Block body = new Block();
 		result.setImplementation(new RegularImplementation(body));
 		NamedTargetExpression componentFieldRef = new NamedTargetExpression(fieldName(relation), null);
 		componentFieldRef.setTarget(new ThisLiteral());
 		body.addStatement(new StatementExpression(new AssignmentExpression(componentFieldRef, new NamedTargetExpression(name, null))));
 		return result;
 		} else {
 			return null;
 		}
 	}
 	
 	private boolean overrides(ComponentRelation relation) throws LookupException {
 		Type type = relation.nearestAncestor(Type.class);
 		for(Type superType: type.getDirectSuperTypes()) {
 			List<ComponentRelation> superComponents = superType.members(ComponentRelation.class);
 			for(ComponentRelation superComponent: superComponents) {
 				if(new SubobjectJavaOverridesRelation().contains(relation, superComponent)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public List<Method> aliasMethods(ComponentRelation relation) throws LookupException {
 		List<Method> result = new ArrayList<Method>();
 		List<? extends Member> members = relation.getIntroducedMembers();
 		members.remove(relation);
 		for(Member member: members) {
 			result.add(aliasFor(member, relation));
 		}
 		return result;
 	
 	}
 	public Method aliasFor(Member<?,?,?,?> member, ComponentRelation relation) throws LookupException{
 		Java lang = member.language(Java.class);
 		if(member instanceof Method) {
 			Method<?,?,?,?> method = (Method) member;
 			Method<?,?,?,?> origin = (Method) method.origin();
 			String methodName = fieldName(relation);
 			Method result = new NormalMethod(method.header().clone(), lang.createTypeReference(method.returnType().getFullyQualifiedName()));
 			Block body = new Block();
 			result.setImplementation(new RegularImplementation(body));
 			Invocation invocation = invocation(method, origin.name());
 			invocation.setTarget(new NamedTargetExpression(methodName, null));
 			if(origin.returnType().equals(origin.language(ObjectOrientedLanguage.class).voidType())) {
 				body.addStatement(new StatementExpression(invocation));
 			} else {
 				body.addStatement(new ReturnStatement(invocation));
 			}
 			for(Modifier mod: origin.modifiers()) {
 				result.addModifier(mod.clone());
 			}
 			return result;
 		} else {
 			throw new ChameleonProgrammerException("Translation of member of type "+member.getClass().getName()+" not supported.");
 		}
 	}
 
 	private Invocation invocation(Method<?, ?, ?, ?> method, String origin) {
 		Invocation invocation = new RegularMethodInvocation(origin, null);
 		// pass parameters.
 		useParametersInInvocation(method, invocation);
 		return invocation;
 	}
 
 	private void useParametersInInvocation(Method<?, ?, ?, ?> method, Invocation invocation) {
 		for(FormalParameter param: method.formalParameters()) {
 			invocation.addArgument(new ActualArgument(new NamedTargetExpression(param.signature().name(), null)));
 		}
 	}
 	
 	public String fieldName(ComponentRelation relation) {
 		return relation.signature().name();
 	}
 	
 	
 }
