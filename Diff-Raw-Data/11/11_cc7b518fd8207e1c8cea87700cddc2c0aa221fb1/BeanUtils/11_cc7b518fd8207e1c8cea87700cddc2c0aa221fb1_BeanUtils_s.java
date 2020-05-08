 package org.springframework.beans;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 import org.springframework.util.Assert;
 import org.springframework.util.ReflectionUtils;
 
 import com.h2.util.lang.reflect.ReflectUtils;
 
 
 
 public class BeanUtils {
    
    @SuppressWarnings("unchecked")
    public static <T> T convertToBean(String value, String clazz) {
       return (T) convertToBean(value, getClass(clazz));
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T convertToBean(String value, Class<T> clazz) {
       
       if(String.class.isAssignableFrom(clazz)) {
          return (T) new String(value);
       }
       else if(Integer.class.isAssignableFrom(clazz)
             || Integer.TYPE.isAssignableFrom(clazz)) {
          return (T) new Integer(value);
       }
       else if(Long.class.isAssignableFrom(clazz)
             || Long.TYPE.isAssignableFrom(clazz)) {
          return (T) new Long(value);
       }
       else if(Float.class.isAssignableFrom(clazz)
             || Float.TYPE.isAssignableFrom(clazz)) {
          return (T) new Float(value);
       }
       else if(Double.class.isAssignableFrom(clazz)
             || Double.TYPE.isAssignableFrom(clazz)) {
          return (T) new Double(value);
       }
       else if(Boolean.class.isAssignableFrom(clazz)
             || Boolean.TYPE.isAssignableFrom(clazz)) {
          return (T) new Boolean(value);
       }
       
       return null;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(String clazzName)
          throws BeanInstantiationException {
       try {
          return (Class<T>) Class.forName(clazzName);
       } catch (ClassNotFoundException exp) {
          throw new FatalBeanException(
                "Could not find class: " + clazzName, exp);
       }
    }
 
    /**
     * Convenience method to instantiate a class using its no-arg constructor.
     * As this method doesn't try to load classes by name, it should avoid
     * class-loading issues.
     * @param clazz class to instantiate
     * @return the new instance
     * @throws BeanInstantiationException if the bean cannot be instantiated
     */
    public static <T> T instantiate(Class<T> clazz) throws BeanInstantiationException {
       if (clazz.isInterface()) {
          throw new BeanInstantiationException(clazz, "Specified class is an interface");
       }
       try {
          return clazz.newInstance();
       }
       catch (InstantiationException ex) {
          throw new BeanInstantiationException(clazz, "Is it an abstract class?", ex);
       }
       catch (IllegalAccessException ex) {
          throw new BeanInstantiationException(clazz, "Is the constructor accessible?", ex);
       }
    }
    
    /**
     * Convenience method to instantiate a class using the given constructor.
     * As this method doesn't try to load classes by name, it should avoid
     * class-loading issues.
     * <p>Note that this method tries to set the constructor accessible
     * if given a non-accessible (that is, non-public) constructor.
     * @param ctor the constructor to instantiate
     * @param args the constructor arguments to apply
     * @return the new instance
     * @throws BeanInstantiationException if the bean cannot be instantiated
     */
    public static <T> T instantiateClass(Class<T> clazz,
          Object... args) throws BeanInstantiationException {
       Assert.notNull(clazz, "Class must not be null");
       Constructor<T> ctor = null;
       try {
          Class<?>[] constructorArgClasses = new Class<?>[args.length];
          ctor = ReflectUtils.getConstructor(clazz, constructorArgClasses);
          return ctor.newInstance(args);
       }
       catch (InstantiationException exp) {
          throw new BeanInstantiationException(ctor.getDeclaringClass(),
                "Is it an abstract class?", exp);
       }
       catch (IllegalAccessException exp) {
          throw new BeanInstantiationException(ctor.getDeclaringClass(),
                "Is the constructor accessible?", exp);
       }
       catch (IllegalArgumentException exp) {
          throw new BeanInstantiationException(ctor.getDeclaringClass(),
                "Illegal arguments for constructor", exp);
       }
       catch (InvocationTargetException exp) {
          throw new BeanInstantiationException(ctor.getDeclaringClass(),
                "Constructor threw exception", exp.getTargetException());
       }
    }
    
    /**
     * Convenience method to instantiate a class using the given constructor.
     * As this method doesn't try to load classes by name, it should avoid
     * class-loading issues.
     * <p>Note that this method tries to set the constructor accessible
     * if given a non-accessible (that is, non-public) constructor.
     * @param ctor the constructor to instantiate
     * @param args the constructor arguments to apply
     * @return the new instance
     * @throws BeanInstantiationException if the bean cannot be instantiated
     */
    public static <T> T instantiateClass(Constructor<T> ctor, Object... args)
          throws BeanInstantiationException {
       Assert.notNull(ctor, "Constructor must not be null");
       try {
          ReflectionUtils.makeAccessible(ctor);
          return ctor.newInstance(args);
       }
       catch (InstantiationException exp) {
          throw new BeanInstantiationException(ctor.getDeclaringClass(),
                "Is it an abstract class?", exp);
       }
       catch (IllegalAccessException exp) {
          throw new BeanInstantiationException(ctor.getDeclaringClass(),
                "Is the constructor accessible?", exp);
       }
       catch (IllegalArgumentException exp) {
          throw new BeanInstantiationException(ctor.getDeclaringClass(),
                "Illegal arguments for constructor", exp);
       }
       catch (InvocationTargetException exp) {
          throw new BeanInstantiationException(ctor.getDeclaringClass(),
                "Constructor threw exception", exp.getTargetException());
       }
    }
 }
