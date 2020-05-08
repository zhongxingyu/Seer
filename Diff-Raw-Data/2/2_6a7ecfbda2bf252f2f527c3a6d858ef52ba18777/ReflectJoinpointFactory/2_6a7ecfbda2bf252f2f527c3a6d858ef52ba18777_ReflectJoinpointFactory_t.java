 /*
  * JBoss, the OpenSource J2EE webOS
  *
  * Distributable under LGPL license.
  * See terms of license at gnu.org.
  */
 package org.jboss.joinpoint.plugins.reflect;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.jboss.joinpoint.spi.ConstructorJoinpoint;
 import org.jboss.joinpoint.spi.FieldGetJoinpoint;
 import org.jboss.joinpoint.spi.FieldSetJoinpoint;
 import org.jboss.joinpoint.spi.JoinpointException;
 import org.jboss.joinpoint.spi.JoinpointFactory;
 import org.jboss.joinpoint.spi.MethodJoinpoint;
 import org.jboss.reflect.spi.ClassInfo;
 import org.jboss.reflect.spi.ConstructorInfo;
 import org.jboss.reflect.spi.FieldInfo;
 import org.jboss.reflect.spi.MethodInfo;
 
 /**
  * A join point factory based on reflection
  *
  * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
  */
 public class ReflectJoinpointFactory implements JoinpointFactory
 {
    /** The class info */
    protected ClassInfo classInfo;
 
    public static void handleErrors(String context, Class[] parameters, Object[] arguments, Throwable t) throws Throwable
    {
       if (t instanceof IllegalArgumentException)
       {
          ArrayList expected = new ArrayList();
          Class[] parameterTypes = parameters;
          for (int i = 0; i < parameterTypes.length; ++i)
             expected.add(parameterTypes[i].getName());
          ArrayList actual = new ArrayList();
          if (arguments != null)
          {
             for (int i = 0; i < arguments.length; ++i)
             {
                if (arguments[i] == null)
                   actual.add(null);
                else
                  actual.add(arguments[i].getClass().getName());
             }
          }
          throw new IllegalArgumentException("Wrong arguments. " + context + " expected=" + expected + " actual=" + actual);
       }
       else if (t instanceof InvocationTargetException)
       {
          throw ((InvocationTargetException) t).getTargetException();
       }
       throw t;
    }
    
    public ReflectJoinpointFactory(ClassInfo classInfo)
    {
       this.classInfo = classInfo;
    }
    
    public ClassInfo getClassInfo()
    {
       return classInfo;
    }
 
    public ConstructorJoinpoint getConstructorJoinpoint(ConstructorInfo constructorInfo, Map metadata) throws JoinpointException
    {
       return new ReflectConstructorJoinPoint(constructorInfo);
    }
 
    public FieldGetJoinpoint getFieldGetJoinpoint(FieldInfo fieldInfo) throws JoinpointException
    {
       return new ReflectFieldGetJoinPoint(fieldInfo);
    }
 
    public FieldSetJoinpoint getFieldSetJoinpoint(FieldInfo fieldInfo) throws JoinpointException
    {
       return new ReflectFieldSetJoinPoint(fieldInfo);
    }
 
    public MethodJoinpoint getMethodJoinpoint(MethodInfo methodInfo) throws JoinpointException
    {
       return new ReflectMethodJoinPoint(methodInfo);
    }
 }
