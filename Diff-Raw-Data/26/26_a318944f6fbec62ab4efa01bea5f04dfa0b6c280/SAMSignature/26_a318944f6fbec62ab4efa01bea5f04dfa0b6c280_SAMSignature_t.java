 package org.wixpress.hoopoe.lambda;
 
 import java.lang.reflect.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Yoav
  * @since 10/5/11
  */
 public class SAMSignature<SAM> {
 
     private Class<SAM> samType;
     private LambdaSignature<?> lambdaSignature;
     private Class<?>[] lambdaApplyParameters;
     private Method samMethod;
     private List<MaterializedTypeVariable> materializedTypeVariableses = new ArrayList<MaterializedTypeVariable>();
 
     public SAMSignature(Class<SAM> samType) {
         this(samType, new Class<?>[0]);
     }
 
     public SAMSignature(Class<SAM> samType, Class<?>[] genericTypes) {
         this.samType = samType;
         if (!samType.isInterface()) {
             throw new LambdaException("SAM class [%s] is required to be an interface", samType.getName());
         }
 
         buildGenericTypeVariables(genericTypes);
         findSamMethod();
         BuildSignature();
     }
 
     private void buildGenericTypeVariables(Class<?>[] genericTypes) {
         if (samType.getTypeParameters().length != genericTypes.length)
             throw new LambdaException("SAM class [%s] requires [%d] generic type variables, but only [%d] where provided", samType.getName(),
                     samType.getTypeParameters().length, genericTypes.length);
 
         for (int i=0; i < samType.getTypeParameters().length; i++) {
             TypeVariable typeVariable = samType.getTypeParameters()[i];
             for (Type bound: typeVariable.getBounds())
                 if (bound instanceof Class<?> && !((Class)bound).isAssignableFrom(genericTypes[i]))
                     throw new LambdaException("SAM class [%s] generic type variable [%d, %s] is not compatible with generic parameter [%s]", samType.getName(),
                             i, samType.getTypeParameters()[i].toString(), genericTypes[i]);
             materializedTypeVariableses.add(new MaterializedTypeVariable(typeVariable.getName(), genericTypes[i]));
         }
     }
 
     private void findSamMethod() {
         for (Method method: samType.getMethods()) {
             if (isCandidateMethod(method)) {
                 if (samMethod != null)
                     throw new LambdaException("SAM interface [%s] is required to have just one method, but at least 2 found - [%s], [%s]", samType.getName(), samMethod, method);
                 else
                     samMethod = method;
             }
         }
     }
 
     private boolean isCandidateMethod(Method method) {
         for (Method objectMethod: Object.class.getMethods()) {
             if (sameMethodSignatureAndName(method, objectMethod))
                 return false;
         }
         return true;
     }
 
     private boolean sameMethodSignatureAndName(Method m1, Method m2) {
         if (m1.getName().equals(m2.getName()) &&
                 m1.getReturnType().equals(m2.getReturnType())) {
             /* Avoid unnecessary cloning */
             Class[] params1 = m1.getParameterTypes();
             Class[] params2 = m2.getParameterTypes();
             if (params1.length == params2.length) {
                 for (int i = 0; i < params1.length; i++) {
                     if (params1[i] != params2[i])
                         return false;
                 }
                 return true;
             }
         }
         return false;
     }
 
 
     private void BuildSignature() {
         Var[] vars = new Var[samMethod.getParameterTypes().length];
         lambdaApplyParameters = new Class<?>[samMethod.getParameterTypes().length];
         for (int i=0; i < vars.length; i++) {
             //noinspection unchecked
             Type paramType = samMethod.getGenericParameterTypes()[i];
             if (paramType instanceof Class)
                 vars[i] = new Var((Class<?>)paramType);
             else if (paramType instanceof TypeVariable)
                 vars[i] = new Var(findMaterializedType(((TypeVariable)paramType).getName()));
             else
                 throw new LambdaException("unexpected param generic type [%s] for SAM [%s]", paramType, samType.getName());
 
             lambdaApplyParameters[i] = Object.class;
         }
 
        Class<?> materializedRetType;
        Type genericRetType = samMethod.getGenericReturnType();
        if (genericRetType instanceof Class<?>)
            materializedRetType = samMethod.getReturnType();
        else if (genericRetType instanceof TypeVariable)
            materializedRetType = findMaterializedType(((TypeVariable) genericRetType).getName());
        else
            throw new LambdaException("unexpected generic return type [%s] for SAM [%s]", samMethod.getReturnType(), samType.getName());

        this.lambdaSignature = new LambdaSignature<Object>(materializedRetType, vars);
     }
 
     private Class findMaterializedType(String name) {
         for (MaterializedTypeVariable materializedTypeVariable: materializedTypeVariableses) {
             if (materializedTypeVariable.name.equals(name))
                 return materializedTypeVariable.actualClass;
         }
         throw new LambdaException("materialized type not found for name [%s] for SAM [%s]", name, samType.getName());
     }
 
     public SAM build(String code, Val ... vals) {
         final Object theLambda = lambdaSignature.build(code, vals);
         //noinspection unchecked
         return (SAM)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] {samType}, new InvocationHandler() {
             @Override
             public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                 return theLambda.getClass().getMethod("apply", lambdaApplyParameters).invoke(theLambda, args);
             }
         });
     }
 
     private class MaterializedTypeVariable {
         private String name;
         private Class<?> actualClass;
 
         private MaterializedTypeVariable(String name, Class<?> actualClass) {
             this.name = name;
             this.actualClass = actualClass;
         }
     }
 }
