 package core;
 
 import com.sun.xml.internal.ws.util.StringUtils;
 import core.scopes.Prototype;
 
 import javax.inject.Inject;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 public class BeanDefinition {
     private Class clazz;
     private IocContainer container;
     private Object instance = null;
     private boolean prototypeScope;
     private List<Field> injectedFields = new ArrayList<Field>();
     private List<Method> injectedSetter = new ArrayList<Method>();
 
     public BeanDefinition(Class clazz, IocContainer container) {
         this.clazz = clazz;
         this.container = container;
     }
 
     public void init() throws Exception {
         if(isInitialized()) return;
         extractMetaData();
         initBean();
     }
 
     public boolean assignableTo(Class type) {
         return type.isAssignableFrom(clazz);
     }
 
     public Object getBean() throws Exception {
         if(!isInitialized())
            initBean();
         return prototypeScope ? newInstance() : instance;
     }
 
     private boolean isInitialized() {
         return instance != null;
     }
 
     private void extractMetaData() throws Exception {
         prototypeScope = clazz.isAnnotationPresent(Prototype.class);
 
         for (Field declaredField : clazz.getDeclaredFields()) {
             if(declaredField.isAnnotationPresent(Inject.class)) {
                 injectedFields.add(declaredField);
             }
         }
 
         for (Method declaredMethod : clazz.getDeclaredMethods()) {
             if(declaredMethod.isAnnotationPresent(Inject.class)) {
                 if(declaredMethod.getName().matches("set[A-Z].*]")) {
                     throw new Exception(declaredMethod.getName() + " doesn't look like a setter");
                 }
                 injectedSetter.add(declaredMethod);
             }
         }
     }
 
     private void initBean() throws Exception {
         instance = newInstance();
     }
 
     private Object newInstance() throws Exception {
         Object instance = initConstructorInjection();
         initSetterInjectionOnField(instance);
         initSetterInjectionOnSetter(instance);
         return instance;
     }
 
     private void initSetterInjectionOnSetter(Object instance) throws Exception {
         for (Method setter : injectedSetter) {
             Class<?>[] parameterTypes = setter.getParameterTypes();
             if(parameterTypes.length != 1) {
                 throw new Exception(setter.getName() + " should have and only have one parameter");
             }
             setter.invoke(instance, container.getBeanByCompatibleType(parameterTypes[0]));
         }
     }
 
     private void initSetterInjectionOnField(Object instance) throws Exception {
         for (Field injectedField : injectedFields) {
             injectField(instance, injectedField);
         }
     }
 
     private void injectField(Object instance, Field declaredField) throws Exception {
         String name = declaredField.getName();
         Method method = clazz.getMethod("set" + StringUtils.capitalize(name), declaredField.getType());
         method.invoke(instance, container.getBeanByCompatibleType(declaredField.getType()));
     }
 
     private Object initConstructorInjection() throws Exception {
         Constructor[] constructors = filterWithInjectAnnotation(clazz.getConstructors());
         if(constructors.length == 0) {
             return clazz.newInstance();
         } else if(constructors.length == 1) {
             return initInstanceWithConstructor(constructors[0]);
         } else {
             throw new Exception("classes registered in IOC container must have one or zero constructor, but "
                     + clazz.getName() +
                     " have " + constructors.length);
         }
     }
 
     private Constructor[] filterWithInjectAnnotation(Constructor[] constructors) {
         List<Constructor> filtered = new ArrayList<Constructor>();
         for (Constructor constructor : constructors) {
             if(constructor.isAnnotationPresent(Inject.class)) {
                 filtered.add(constructor);
             }
         }
         return filtered.toArray(new Constructor[0]);
     }
 
     private Object initInstanceWithConstructor(Constructor constructor) throws Exception {
         Class[] parameterTypes = constructor.getParameterTypes();
         List<Object> params = new ArrayList<Object>();
         for (Class type : parameterTypes) {
             params.add(container.getBeanByCompatibleType(type));
         }
         return initInstanceWithParams(params, constructor);
     }
 
     private Object initInstanceWithParams(List<Object> params, Constructor constructor) throws Exception, IllegalAccessException, InstantiationException {
         switch (params.size()) {
             case 0 : return constructor.newInstance();
             case 1 : return constructor.newInstance(params.get(0));
             case 2 : return constructor.newInstance(params.get(0), params.get(1));
             case 3 : return constructor.newInstance(params.get(0), params.get(1), params.get(2));
             case 4 : return constructor.newInstance(params.get(0), params.get(1), params.get(2), params.get(3));
             case 5 : return constructor.newInstance(params.get(0), params.get(1), params.get(2), params.get(3), params.get(4));
             case 6 : return constructor.newInstance(params.get(0), params.get(1), params.get(2), params.get(3), params.get(4), params.get(5));
             case 7 : return constructor.newInstance(params.get(0), params.get(1), params.get(2), params.get(3), params.get(4), params.get(5), params.get(6));
             case 8 : return constructor.newInstance(params.get(0), params.get(1), params.get(2), params.get(3), params.get(4), params.get(5), params.get(6), params.get(7));
             case 9 : return constructor.newInstance(params.get(0), params.get(1), params.get(2), params.get(3), params.get(4), params.get(5), params.get(6), params.get(7), params.get(8));
             case 10 : return constructor.newInstance(params.get(0), params.get(1), params.get(2), params.get(3), params.get(4), params.get(5), params.get(6), params.get(7), params.get(8), params.get(9));
             default: throw new Exception("too many constructor parameters");
         }
     }
 }
