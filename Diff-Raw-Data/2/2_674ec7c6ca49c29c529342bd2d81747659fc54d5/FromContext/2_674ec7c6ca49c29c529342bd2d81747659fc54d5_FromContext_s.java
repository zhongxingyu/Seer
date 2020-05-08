 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.beans.metadata.plugins;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Proxy;
 import java.util.Set;
 import java.util.Collections;
 import java.util.HashSet;
 
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.metadata.spi.MetaData;
 import org.jboss.metadata.spi.scope.ScopeKey;
 import org.jboss.metadata.spi.helpers.UnmodifiableMetaData;
 import org.jboss.util.JBossObject;
 import org.jboss.util.JBossStringBuilder;
 import org.jboss.reflect.plugins.introspection.ReflectionUtils;
 import org.jboss.dependency.spi.ControllerContext;
 import org.jboss.beans.info.spi.BeanInfo;
 import org.jboss.beans.info.spi.helpers.UnmodifiableBeanInfo;
 
 /**
  * Inject from controller context:
  *  * name - controller context name
  *  * aliases - aliases
  *  * metadata - inject MetaData
  *  * beaninfo - BeanInfo
  *  * scope - ScopeKey
  *  * id - identifier
  *  * dynamic - method specific
  *  * ...
  *
  * @param <T> exact controller context type
  * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
  */
 public abstract class FromContext<T extends ControllerContext> extends JBossObject
       implements Serializable
 {
    private static final long serialVersionUID = 1L;
 
    /** name */
    public static final FromContext NAME = new NameFromContext("name");
 
    /** alias */
    public static final FromContext ALIASES = new AliasesFromContext("aliases");
 
    /** metadata */
    public static final FromContext METADATA = new MetaDataFromContext("metadata");
 
    /** beaninfo */
    public static final FromContext BEANINFO = new BeanInfoFromContext("beaninfo");
 
    /** scope */
    public static final FromContext SCOPE = new ScopeFromContext("scope");
 
    /** id */
    public static final FromContext ID = new IdFromContext("id");
 
    /** The type string */
    protected final String fromString;
 
    /**
     * Create a new state
     *
     * @param fromString the string representation
     */
    protected FromContext(String fromString)
    {
       if (fromString == null)
          throw new IllegalArgumentException("Null from string");
       this.fromString = fromString;
    }
 
    /**
     * Return from type.
     *
     * @param fromString type
     * @return FromContext instance
     */
    public static FromContext getInstance(String fromString)
    {
       if (NAME.getFromString().equalsIgnoreCase(fromString))
          return NAME;
       else if (ALIASES.getFromString().equalsIgnoreCase(fromString))
          return ALIASES;
       else if (METADATA.getFromString().equalsIgnoreCase(fromString))
          return METADATA;
       else if (BEANINFO.getFromString().equalsIgnoreCase(fromString))
          return BEANINFO;
       else if (SCOPE.getFromString().equalsIgnoreCase(fromString))
          return SCOPE;
       else if (ID.getFromString().equalsIgnoreCase(fromString))
          return ID;
       else
          return new DynamicFromContext(fromString);
    }
 
    /**
     * Validate context before execution.
     * After validation we must be able to cast context to T instance.
     *
     * @param context the context
     */
    protected void validate(ControllerContext context)
    {
    }
 
    /**
     * Execute injection on context.
     *
     * @param context the target context
     * @return lookup value
     * @throws Throwable for any error
     */
    @SuppressWarnings("unchecked")
    public Object executeLookup(ControllerContext context) throws Throwable
    {
       validate(context);
       return internalExecute((T)context);
    }
 
    /**
     * Execute internal lookup on context.
     *
     * @param context the target context
     * @return lookup value
     * @throws Throwable for any error
     */
    public abstract Object internalExecute(T context) throws Throwable;
 
    /**
     * Get the from string
     *
     * @return the state string
     */
    public String getFromString()
    {
       return fromString;
    }
 
    public boolean equals(Object object)
    {
       if (object == null || object instanceof FromContext == false)
          return false;
       FromContext other = (FromContext) object;
       return fromString.equals(other.getFromString());
    }
 
    public void toString(JBossStringBuilder buffer)
    {
       buffer.append(fromString);
    }
 
    protected int getHashCode()
    {
       return fromString.hashCode();
    }
 
    private static abstract class KernelFromContext extends FromContext<KernelControllerContext>
    {
       private static final long serialVersionUID = 1L;
 
       protected KernelFromContext(String fromString)
       {
          super(fromString);
       }
 
       protected void validate(ControllerContext context)
       {
          if (context instanceof KernelControllerContext == false)
             throw new UnsupportedOperationException("Cannot execute " + getFromString() + " on underlying context: " + context);
       }
    }
 
    private static class NameFromContext extends FromContext
    {
       private static final long serialVersionUID = 1L;
 
       public NameFromContext(String fromString)
       {
          super(fromString);
       }
 
       public Object internalExecute(ControllerContext context)
       {
          return context.getName();
       }
    }
 
    private static class AliasesFromContext extends FromContext
    {
       private static final long serialVersionUID = 1L;
 
       public AliasesFromContext(String fromString)
       {
          super(fromString);
       }
 
       public Set<Object> internalExecute(ControllerContext context)
       {
          Set<Object> aliases = context.getAliases();
          return aliases != null ? Collections.unmodifiableSet(aliases) : null;
       }
    }
 
    private static class MetaDataFromContext extends KernelFromContext
    {
       private static final long serialVersionUID = 1L;
 
       public MetaDataFromContext(String fromString)
       {
          super(fromString);
       }
 
       public MetaData internalExecute(KernelControllerContext context)
       {
          MetaData metaData = context.getMetaData();
          return metaData != null ? new UnmodifiableMetaData(metaData) : null;
       }
    }
 
    private static class BeanInfoFromContext extends KernelFromContext
    {
       private static final long serialVersionUID = 1L;
 
       public BeanInfoFromContext(String fromString)
       {
          super(fromString);
       }
 
       public BeanInfo internalExecute(KernelControllerContext context)
       {
          BeanInfo info = context.getBeanInfo();
          return info != null ? new UnmodifiableBeanInfo(info) : null;
       }
    }
 
    private static class ScopeFromContext extends KernelFromContext
    {
       private static final long serialVersionUID = 1L;
 
       public ScopeFromContext(String fromString)
       {
          super(fromString);
       }
 
       public ScopeKey internalExecute(KernelControllerContext context)
       {
          ScopeKey key = context.getScope();
          return key != null ? key.clone() : null;
       }
    }
 
    private static class IdFromContext extends FromContext
    {
       private static final long serialVersionUID = 1L;
 
       public IdFromContext(String fromString)
       {
          super(fromString);
       }
 
       public Object internalExecute(ControllerContext context)
       {
          // todo - change to actual id when impl
          return context.getName();
       }
    }
 
    private static class DynamicFromContext extends FromContext
    {
       private static final long serialVersionUID = 1L;
 
       public DynamicFromContext(String fromString)
       {
          super(fromString);
       }
 
       protected Method findMethod(Class clazz)
       {
          if (clazz == null || clazz == Object.class)
             return null;
 
          Method[] methods = clazz.getDeclaredMethods();
          for(Method m : methods)
          {
             if (m.getName().equals(getFromString()) && m.getParameterTypes().length == 0)
             {
                return m;
             }
          }
 
          Method method = findMethod(clazz.getSuperclass());
          if (method != null)
             return method;
 
          for(Class infc : clazz.getInterfaces())
          {
             Method m = findMethod(infc);
             if (m != null)
                return m;
          }
          return null;
       }
 
       protected void getInterfaces(Class clazz, Set<Class> interfaces)
       {
          if (clazz == Object.class || clazz == null)
             return;
          for (Class iface : clazz.getInterfaces())
             interfaces.add(iface);
          getInterfaces(clazz.getSuperclass(), interfaces);         
       }
 
       public Object internalExecute(ControllerContext context) throws Throwable
       {
          Method method = findMethod(context.getClass());
          if (method == null)
            throw new IllegalArgumentException("No such getter on context class: " + getFromString());
          Object result = ReflectionUtils.invoke(method, context, new Object[]{});
          if (result != null)
          {
             Set<Class> interfaces = new HashSet<Class>();
             getInterfaces(result.getClass(), interfaces);
             return Proxy.newProxyInstance(
                      ControllerContext.class.getClassLoader(),
                      interfaces.toArray(new Class[interfaces.size()]),
                      new DynamicWrapper(result));
          }
          return null;
       }
 
       /**
        * This warpper throws error on methods that start with set or add.
        */
       private class DynamicWrapper implements InvocationHandler
       {
          private Object target;
 
          public DynamicWrapper(Object target)
          {
             this.target = target;
          }
 
          /**
           * Check if the method is unsupported.
           *
           * @param method the executed method.
           * @return true if unsupported, false otherwise
           */
          protected boolean isUnsupported(Method method)
          {
             String name = method.getName();
             return (name.startsWith("set") || name.startsWith("add"));
          }
 
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
          {
             if (isUnsupported(method))
                throw new UnsupportedOperationException();
             return ReflectionUtils.invoke(method, target, args);
          }
       }
    }
 
 }
