 package reflekto.impl;
 
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import reflekto.AccessFilter;
 import reflekto.AccessModifier;
 import reflekto.ClassType;
 import reflekto.Method;
 import reflekto.Parameterizable;
 import reflekto.Type;
 import reflekto.TypeVariable;
 
 public class DefaultMethod extends AbstractCallableMember implements Method {
 
 	private final java.lang.reflect.Method method;
 	private ReadOnlyReference<List<Type>> declaredParameterTypes = new LazyInit<List<Type>>(){
 		@Override
 		protected List<Type> init() {
 			return initDeclaredParameterTypes();
 		}
 	};
 	private ReadOnlyReference<List<Type>> actualParameterTypes;
 	private ReadOnlyReference<Type> declaredReturnType = new LazyInit<Type>(){
 		@Override
 		protected Type init() {
 			return initDeclaredReturnType();
 		}
 	};
 	private ReadOnlyReference<Type> actualReturnType;
 	private ReadOnlyReference<List<TypeVariable>> declaredTypeParameters = new LazyInit<List<TypeVariable>>(){
 		@Override
 		protected List<TypeVariable> init() {
 			return initDeclaredTypeParameters();
 		}
 	};
 	private ReadOnlyReference<List<Type>> actualTypeParameters;
 
 	@SuppressWarnings("unchecked")
 	public DefaultMethod(java.lang.reflect.Method m, ClassType declaringClass, FullReflector reflector) {
 		super(declaringClass, reflector);
 		this.method = m;
 		if(declaringClass.isGenericInvocation()){
 			this.actualParameterTypes = new LazyInit<List<Type>>(){
 				@Override
 				protected List<Type> init() {
 					return initActualParameterTypes();
 				}
 			};
 			this.actualReturnType = new LazyInit<Type>(){
 				@Override
 				protected Type init() {
 					return initActualReturnType();
 				}
 			};
 			this.actualTypeParameters = new LazyInit<List<Type>>(){
 				@Override
 				protected List<Type> init() {
 					return initActualTypeParameters();
 				}
 			};
 		} else {
 			this.actualParameterTypes = this.declaredParameterTypes;
 			this.actualReturnType = this.declaredReturnType;
 			// Yes, I know this is ugly, but since all of the involved objects are effectively final,
 			// it is effectively safe, and makes my life easier than carrying around two levels of wildcard types.
 			this.actualTypeParameters = (ReadOnlyReference<List<Type>>)(Object)this.declaredTypeParameters;
 		}
 	}
 
 	/**
 	 * Builds an erasure version of the specified original method
 	 */
 	protected DefaultMethod(final DefaultMethod original){
 		super(original.getDeclaringClass(), original.reflector);
 		this.method = original.method;
 		this.actualTypeParameters = new FinalReference<List<Type>>(Collections.<Type>emptyList());
 		this.actualReturnType = new LazyInit<Type>(){
 			@Override
 			protected Type init() {
 				return original.getActualReturnType().withErasure();
 			}
 		};
 		this.actualParameterTypes = new LazyInit<List<Type>>(){
 			@Override
 			protected List<Type> init() {
 				List<Type> erased = new ArrayList<Type>(original.getActualParameterTypes().size());
 				for(Type t : original.getActualParameterTypes()){
 					Type te = t.withErasure();
 					erased.add(te);
 				}
 				return Collections.unmodifiableList(erased);
 			}
 		};
 	}
 
 
 	private List<Type> initDeclaredParameterTypes() {
 		java.lang.reflect.Type[] jParamTypes = method.getGenericParameterTypes();
 		List<Type> paramTypes = new ArrayList<Type>(jParamTypes.length);
 		for(java.lang.reflect.Type jParamType : jParamTypes){
 			paramTypes.add(reflector.reflect(jParamType));
 		}
 		return Collections.unmodifiableList(paramTypes);
 	}
 
 	private Type initDeclaredReturnType() {
 		return reflector.reflect(method.getGenericReturnType());
 	}
 
 	private List<TypeVariable> initDeclaredTypeParameters() {
 		java.lang.reflect.TypeVariable<?>[] jTypeParams = method.getTypeParameters();
 		List<TypeVariable> typeParams = new ArrayList<TypeVariable>(jTypeParams.length);
 		for(java.lang.reflect.TypeVariable<?> jTypeParam : jTypeParams){
 			typeParams.add(reflector.reflect(jTypeParam));
 		}
 		return Collections.unmodifiableList(typeParams);
 	}
 
 	private List<Type> initActualParameterTypes() {
 		List<Type> declaredTypes = getDeclaredParameterTypes();
 		List<TypeVariable> declaredVars = this.getDeclaringClass().getDeclaredTypeParameters();
 		List<Type> actualVars = this.getDeclaringClass().getActualTypeParameters();
 		List<Type> actualTypes = new ArrayList<Type>(declaredTypes.size());
 		for(Type declaredType : declaredTypes){
 			Type actualType = declaredType.assignVariables(declaredVars, actualVars);
 			actualTypes.add(actualType);
 		}
 		return Collections.unmodifiableList(actualTypes);
 	}
 
 	private Type initActualReturnType() {
 		Type declaredReturnType = getDeclaredReturnType();
 		List<TypeVariable> declaredVars = this.getDeclaringClass().getDeclaredTypeParameters();
 		List<Type> actualVars = this.getDeclaringClass().getActualTypeParameters();
 		return declaredReturnType.assignVariables(declaredVars, actualVars);
 	}
 
 	private List<Type> initActualTypeParameters() {
 		List<TypeVariable> declared = getDeclaredTypeParameters();
 		List<Type> actual = new ArrayList<Type>(declared.size());
 		List<TypeVariable> classDeclared = getDeclaringClass().getDeclaredTypeParameters();
 		List<Type> classActual = getDeclaringClass().getActualTypeParameters();
 		for(TypeVariable var : declared){
 			actual.add(var.assignVariables(classDeclared, classActual));
 		}
 		return  Collections.unmodifiableList(actual);
 	}
 
 	public String getName() {
 		return method.getName();
 	}
 
 
 	public String declarationString() {
 		StringBuilder builder = new StringBuilder();
 		if(this.isPublic()){
 			builder.append("public ");
 		}
 		if(this.isPrivate()){
 			builder.append("private ");
 		}
 		if(this.isProtected()){
 			builder.append("protected ");
 		}
 		if(this.isAbstract()){
 			builder.append("abstract ");
 		}
 		if(this.isFinal()){
 			builder.append("final ");
 		}
 		if(this.isStatic()) {
			builder.append("static");
 		}
 
 		// type parameters
 		List<Type> typeParameters = getActualTypeParameters();
 		if(typeParameters.size() > 0){
 			builder.append('<');
 			for(int i = 0; i < typeParameters.size(); i ++){
 				if(i > 0){
 					builder.append(", ");
 				}
 				builder.append(typeParameters.get(i).getGenericName().full());
 			}
 			builder.append("> ");
 		}
 
 		Type returnType = getActualReturnType();
 		if(returnType instanceof TypeVariable){
 			builder.append(returnType.getGenericName().simple());
 		} else {
 			builder.append(returnType.getGenericName().full());
 		}
 		builder.append(" ");
 
 		builder.append(getName());
 		builder.append('(');
 		List<Type> parameterTypes = getActualParameterTypes();
 		for(int i = 0; i < parameterTypes.size(); i ++){
 			if(i > 0){
 				builder.append(", ");
 			}
 			if(returnType instanceof TypeVariable){
 				builder.append(parameterTypes.get(i).getGenericName().simple());
 			} else {
 				builder.append(parameterTypes.get(i).getGenericName().full());
 			}
 		}
 		builder.append(')');
 
 		return builder.toString();
 	}
 
 	public boolean isErasure() {
 		if(!this.getActualTypeParameters().isEmpty()){
 			return false;
 		}
 		if(!this.getActualReturnType().isErasure()){
 			return false;
 		}
 		for(Type t : this.getActualParameterTypes()){
 			if(!t.isErasure()){
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public Parameterizable assignVariables(List<TypeVariable> variable,
 			List<Type> value) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Method withErasure() {
 		// JLS §4.6:
 		// Type erasure also maps the signature of a method to a signature that has no
 		// parameterized types or type variables. The erasure of a constructor or method
 		// signature s is a signature consisting of the same name as s and the erasures
 		// of all the formal parameter types given in s.
 		//
 		// The type parameters and the return type of a method also undergo erasure if
 		// the method's signature is erased.
 		//
 		// The erasure of the signature of a generic method has no type parameters.
 
 		if(this.isErasure()){
 			return this;
 		}
 
 		return reflector.reflectErasure(this);
 	}
 
 	public List<Type> getDeclaredParameterTypes() {
 		return declaredParameterTypes.get();
 	}
 
 	public List<Type> getActualParameterTypes() {
 		return actualParameterTypes.get();
 	}
 
 	public List<TypeVariable> getDeclaredTypeParameters() {
 		return declaredTypeParameters.get();
 	}
 
 	public List<Type> getActualTypeParameters() {
 		return actualTypeParameters.get();
 	}
 
 	public Type getDeclaredReturnType() {
 		return declaredReturnType.get();
 	}
 
 
 	public Type getActualReturnType() {
 		return actualReturnType.get();
 	}
 
 
 	public Type getInferredReturnType(List<Type> argumentTypes) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	public boolean isPublic() {
 		return Modifier.isPublic(method.getModifiers());
 	}
 
 
 	public boolean isPackageProtected() {
 		return !isPublic() && !isProtected() && !isPrivate();
 	}
 
 
 	public boolean isProtected() {
 		return Modifier.isProtected(method.getModifiers());
 	}
 
 
 	public boolean isPrivate() {
 		return Modifier.isPrivate(method.getModifiers());
 	}
 
 
 	public boolean isAbstract() {
 		return Modifier.isAbstract(method.getModifiers());
 	}
 
 
 	public boolean isFinal() {
 		return Modifier.isFinal(method.getModifiers());
 	}
 
 	public boolean isStatic(){
 		return Modifier.isStatic(method.getModifiers());
 	}
 
 	public boolean isAccessibleFrom(ClassType caller) {
 		// caller is the same class as declaringClass
 		// PRIVATE is visible
 		if(this.declaringClass.getDeclaredClass().equals(caller.getDeclaredClass())){
 			return AccessFilter.PRIVATE.andMoreLenient().accepts(getAccessModifier());
 		}
 
 		// caller is an inner class (at any depth) of declaringClass, or vice-versa
 		// PRIVATE is visible
 		if(declaringClass.isInnerClassOf(caller) || caller.isInnerClassOf(declaringClass)){
 			return AccessFilter.PRIVATE.andMoreLenient().accepts(getAccessModifier());
 		}
 
 		// caller is in the same package as declaringClass
 		// PACKAGE is visible
 		if(this.declaringClass.getPackage().equals(caller.getPackage())){
 			return AccessFilter.PACKAGE.andMoreLenient().accepts(getAccessModifier());
 		}
 
 		// caller is a subType of declaringClass
 		// PROTECTED is visible
 		if(declaringClass.isSuperClassOf(caller)){
 			return AccessFilter.PROTECTED.andMoreLenient().accepts(getAccessModifier());
 		}
 
 		// caller is a superType of declaringClass
 		// AND and this method overrides a method in caller
 		// PROTECTED is visible
 		if(caller.isSuperClassOf(declaringClass) && //
 				caller.methods().getOverridden(this) != null) {
 			return AccessFilter.PROTECTED.andMoreLenient().accepts(getAccessModifier());
 		}
 
 		// caller has no relation to declaringClass
 		// PUBLIC is visible
 		return AccessFilter.PUBLIC.accepts(getAccessModifier());
 	}
 
 	public AccessModifier getAccessModifier() {
 		return AccessModifier.fromModifier(method.getModifiers());
 	}
 
 	public boolean overrides(Method m2) {
 		// from JLS §8.4.8.1 : Overriding (by instance methods)
 		//
 		//		An instance method m1, declared in class C, overrides another instance method m2,
 		//		declared in class A iff all of the following are true:
 		//		 - C is a subclass of A.
 		//		 - The signature of m1 is a subsignature (§8.4.2) of the signature of m2.
 		//		 - Either:
 		//		    - m2 is public, protected, or declared with default access in the same package
 		//		      as C, or
 		//          - m1 overrides a method m3 (m3 distinct from m1, m3 distinct from m2), such that
 		//		      m3 overrides m2.
 		//
 		//      The signature of an overriding method may differ from the overridden one if a formal
 		//		parameter in one of the methods has a raw type, while the corresponding parameter in the
 		//		other has a parameterized type.
 		//
 		//		The rules allow the signature of the overriding method to differ from the overridden one,
 		//		to accommodate migration of pre-existing code to take advantage of generics. See §8.4.2
 		//		for further analysis.
 
 		// lets keep the same method names as in the JLS, to make it easier to relate the spec text and the
 		// implementation
 		Method m1 = this;
 		ClassType c = m1.getDeclaringClass();
 		ClassType a = m2.getDeclaringClass();
 
 		if(m2.isPrivate() || ! a.isSuperClassOf(c) || ! m1.isSubsignature(m2)){
 			return false;
 		}
 		if(m2.isPublic() || m2.isProtected() || m2.isPackageProtected() && c.getPackage().equals(a.getPackage())){
 			return true;
 		}
 
 		assert m2.isPackageProtected();
 		assert !c.getPackage().equals(a.getPackage());
 		assert !a.isInterface(); // because interface only contain public methods, and m2 is package-protected
 		assert !c.isInterface(); // because C is a subclass of A
 
 		// given that both A and C are classes, we do not need to search for m3 in interfaces. We will only look
 		// for it in the class hierarchy
 		ClassType b = c.getSuperClass();
 		Method m3 = b.methods().getOverriding(m2);
 		if(m3 == null){
 			// there is no m3 that overrides m2
 			return false;
 		}
 
 		return this.overrides(m3); // recursive, might go for another cycle of looking for an m3
 	}
 
 
 	public boolean isSubsignature(Method that) {
 		// from JLS §8.4.2 : Method signature
 		//
 		//		Two methods have the same signature if they have the same name and argument
 		//		types.
 		//
 		//		Two method or constructor declarations M and N have the same argument types if
 		//		all of the following conditions hold:
 		//		 - They have the same number of formal parameters (possibly zero)
 		//		 - They have the same number of type parameters (possibly zero)
 		//		 - Let A1, ..., An be the type parameters of M and let B1, ..., Bn be the type parameters
 		//		   of N. After renaming each occurrence of a Bi in N's type to Ai, the bounds of
 		//		   corresponding type variables are the same, and the formal parameter types of M
 		//		   and N are the same.
 		//
 		//		The signature of a method m1 is a subsignature of the signature of a method m2 if
 		//		either:
 		//		 - m2 has the same signature as m1, or
 		//		 - the signature of m1 is the same as the erasure (§4.6) of the signature of m2.
 		if(!this.getName().equals(that.getName())){
 			return false;
 		}
 		if(this.getActualParameterTypes().size() != that.getActualParameterTypes().size()){
 			return false;
 		}
 		if(this.getActualTypeParameters().size() != that.getActualTypeParameters().size()){
 			return false;
 		}
 
 		boolean sameSignature = //
 				signatureEquals(this.getActualParameterTypes(), that.getActualParameterTypes()) && //
 				signatureEquals(this.getActualTypeParameters(), that.getActualTypeParameters());
 
 		return sameSignature || signatureEquals( //
 				this.withErasure().getActualParameterTypes(), //
 				that.withErasure().getActualParameterTypes());
 	}
 
 	private static boolean signatureEquals(List<? extends Type> a, List<? extends Type> b){
 		for(int i = 0; i < a.size(); i ++){
 			if(!a.get(i).isSameType(b.get(i))){
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public Method assignTypeVariables(List<TypeVariable> vars, List<Type> values) {
 		// TODO: code it
 		return null;
 	}
 }
