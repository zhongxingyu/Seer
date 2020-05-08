 package org.nextflection.impl;
 
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.nextflection.ClassType;
 import org.nextflection.Method;
 import org.nextflection.Parameterizable;
 import org.nextflection.Type;
 import org.nextflection.TypeVariable;
 
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
 		} else {
 			this.actualParameterTypes = this.declaredParameterTypes;
 			this.actualReturnType = this.declaredReturnType;
 		}
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
 
 	private List<Type> initActualParameterTypes() {
 		List<Type> declaredTypes = getDeclaredParameterTypes();
 		List<TypeVariable> declaredVars = this.getDeclaringClass().getDeclaredTypeParameters();
 		List<Type> actualVars = this.getDeclaringClass().getActualTypeParameters();
 		List<Type> actualTypes = new ArrayList<Type>(declaredTypes.size());
 		for(Type declaredType : declaredTypes){
 			actualTypes.add(declaredType.withTypeArguments(declaredVars, actualVars));
 		}
 		return Collections.unmodifiableList(actualTypes);
 	}
 
 	private Type initActualReturnType() {
 		Type declaredReturnType = getDeclaredReturnType();
 		List<TypeVariable> declaredVars = this.getDeclaringClass().getDeclaredTypeParameters();
 		List<Type> actualVars = this.getDeclaringClass().getActualTypeParameters();
 		return declaredReturnType.withTypeArguments(declaredVars, actualVars);
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
 		List<TypeVariable> typeParameters = getActualTypeParameters();
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
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	public Parameterizable withTypeArguments(List<TypeVariable> variable,
 			List<Type> value) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	public List<TypeVariable> getTypeParameters() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	public Method withErasure() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	public List<Type> getDeclaredParameterTypes() {
 		return declaredParameterTypes.get();
 	}
 
 
 	public List<Type> getActualParameterTypes() {
 		return actualParameterTypes.get();
 	}
 
 
 	public List<TypeVariable> getDeclaredTypeParameters() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	public List<TypeVariable> getActualTypeParameters() {
 		// TODO Auto-generated method stub
 		return Collections.emptyList();
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
 }
