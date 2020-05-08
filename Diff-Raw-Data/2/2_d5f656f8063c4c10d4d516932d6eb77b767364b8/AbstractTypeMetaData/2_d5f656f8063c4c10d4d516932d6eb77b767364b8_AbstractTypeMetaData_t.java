 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
 import java.util.Stack;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlTransient;
 
 import org.jboss.beans.info.spi.BeanInfo;
 import org.jboss.beans.info.spi.PropertyInfo;
 import org.jboss.beans.metadata.spi.BeanMetaData;
 import org.jboss.beans.metadata.spi.MetaDataVisitor;
 import org.jboss.beans.metadata.spi.MetaDataVisitorNode;
 import org.jboss.beans.metadata.spi.PropertyMetaData;
 import org.jboss.dependency.spi.Controller;
 import org.jboss.dependency.spi.ControllerContext;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.dependency.spi.dispatch.AttributeDispatchContext;
 import org.jboss.kernel.plugins.config.Configurator;
 import org.jboss.kernel.spi.config.KernelConfigurator;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.reflect.spi.ClassInfo;
 import org.jboss.reflect.spi.TypeInfo;
 import org.jboss.util.JBossStringBuilder;
 import org.jboss.joinpoint.spi.Joinpoint;
 
 /**
  * A typed value.
  *
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @author <a href="ales.justin@jboss.com">Ales Justin</a>
  * @version $Revision$
  */
 public abstract class AbstractTypeMetaData extends AbstractValueMetaData
    implements Serializable
 {
    private static final long serialVersionUID = 3L;
 
    /**
     * The type
     */
    protected String type;
 
    /**
     * The context
     */
    protected transient ControllerContext context;
 
    /**
     * The configurator
     */
    protected transient KernelConfigurator configurator;
 
    /**
     * The property name
     */
    protected String propertyName;
 
    /**
     * The bean name
     */
    protected String beanName;
 
    /**
     * Create a new typed value
     */
    public AbstractTypeMetaData()
    {
    }
 
    /**
     * Create a new typed value
     *
     * @param value the value
     */
    public AbstractTypeMetaData(String value)
    {
       super(value);
    }
 
    /**
     * Set the type
     *
     * @param type the type
     */
    @XmlAttribute(name="class")
    public void setType(String type)
    {
       this.type = type;
    }
 
    public String getType()
    {
       return type;
    }
 
    public void initialVisit(MetaDataVisitor visitor)
    {
       context = visitor.getControllerContext();
       configurator = visitor.getControllerContext().getKernel().getConfigurator();
       preparePreinstantiatedLookup(visitor);
       visitor.initialVisit(this);
    }
 
    /**
     * Check if we can extract the information about
     * existing instance - only on property metadata.
     *
     * @param visitor underlying visitor
     */
    protected void preparePreinstantiatedLookup(MetaDataVisitor visitor)
    {
       Stack<MetaDataVisitorNode> visitorNodes = visitor.visitorNodeStack();
       // pop it so that we can get to grand parent for more info
       MetaDataVisitorNode parent = visitorNodes.pop();
       try
       {
          if (parent instanceof PropertyMetaData)
          {
             PropertyMetaData pmd = (PropertyMetaData)parent;
             if (pmd.isPreInstantiate())
             {
                propertyName = pmd.getName();
                Object gp = visitorNodes.peek();
                if (gp instanceof BeanMetaData)
                {
                   BeanMetaData bmd = (BeanMetaData)gp;
                   beanName = bmd.getName();
                }
             }
          }
       }
       finally
       {
          visitorNodes.push(parent);
       }
    }
 
    /**
     * Check for already existing instances.
     *
     * @param <T> expected type
     * @param cl the classloader
     * @param expected the expected class
     * @return existing instance or null otherwise
     */
    protected <T> T preinstantiatedLookup(ClassLoader cl, Class<T> expected)
    {
       if (propertyName != null && beanName != null)
       {
          Object result = null;
          try
          {
             Controller controller = context.getController();
             ControllerContext context = controller.getContext(beanName, ControllerState.INSTANTIATED);
             if (context != null && context instanceof AttributeDispatchContext)
             {
                Object target = context.getTarget();
                if (target != null)
                {
                   BeanInfo beanInfo = configurator.getBeanInfo(target.getClass());
                   PropertyInfo pi = Configurator.resolveProperty(log.isTraceEnabled(), beanInfo, cl, propertyName, null);
                  if (pi.isReadable())
                   {
                      result = ((AttributeDispatchContext)context).get(propertyName);
                   }
                }
             }
          }
          catch (Throwable t)
          {
             log.warn("Exception in preinstantiated lookup for: " + beanName + "." + propertyName + ", " + t);
          }
          return checkResult(result, expected);
       }
       return null;
    }
 
    /**
     * Check result for class compatibility.
     *
     * @param <T> the expected type
     * @param result the result
     * @param expected expected class
     * @return casted result
     * @throws ClassCastException if result cannot be casted into expected parameter
     */
    protected <T> T checkResult(Object result, Class<T> expected)
    {
       if (result != null && expected.isAssignableFrom(result.getClass()) == false)
          throw new ClassCastException(result.getClass() + " is not a " + expected.getName());
       return expected.cast(result);
    }
 
    /**
     * Create new instance from type field.
     * Fall back to info parameter if no type field is set.
     *
     * @param <T> expected type
     * @param info the type of reference we are about to set
     * @param cl the classloader to use
     * @param expected the expected class
     * @param explicit is type explicit
     * @return class instance or null if type is too broad
     * @throws Throwable on any error
     */
    protected <T> T createInstance(TypeInfo info, ClassLoader cl, Class<T> expected, boolean explicit) throws Throwable
    {
       if (info == null || Object.class.getName().equals(info.getName()))
          return null;
 
       if (info instanceof ClassInfo == false)
       {
          if (explicit)
             throw new IllegalArgumentException(info.getName() + " is not a class");
          else
             return null;
       }
 
       if (((ClassInfo) info).isInterface())
       {
          if (explicit)
             throw new IllegalArgumentException(info.getName() + " is an interface");
          else
             return null;
       }
 
       BeanInfo beanInfo = configurator.getBeanInfo(info);
       Joinpoint constructor = configurator.getConstructorJoinPoint(beanInfo);
       Object result = constructor.dispatch();
 
       if (expected.isAssignableFrom(result.getClass()) == false)
          throw new ClassCastException(result.getClass() + " is not a " + expected.getName());
 
       return expected.cast(result);
    }
 
    /**
     * Create the class instance
     *
     * @param <T> expected type
     * @param info the request type
     * @param cl the classloader
     * @param expected the expected class
     * @return the class instance
     * @throws Throwable for any error
     */
    protected <T> T getTypeInstance(TypeInfo info, ClassLoader cl, Class<T> expected) throws Throwable
    {
       return getTypeInstance(info, cl, expected, true);
    }
 
    /**
     * Create the class instance
     *
     * @param <T> expected type
     * @param info the request type
     * @param cl the classloader
     * @param expected the expected class
     * @param preInstantiatedLookup  whether to do the preinstantiated lookup
     * @return the class instance
     * @throws Throwable for any error
     */
    protected <T> T getTypeInstance(TypeInfo info, ClassLoader cl, Class<T> expected, boolean preInstantiatedLookup) throws Throwable
    {
       T result = null;
 
       TypeInfo typeInfo = getClassInfo(cl);
       // we have explicitly set type
       if (typeInfo != null)
          result = createInstance(typeInfo, cl, expected, true);
 
       if (result == null)
       {
          if (preInstantiatedLookup)
             result = preinstantiatedLookup(cl, expected);
          // try info param
          if (result == null)
          {
             result = createInstance(info, cl, expected, false);
             // get default
             if (result == null)
                result = expected.cast(getDefaultInstance());
          }
       }
 
       return result;
    }
 
    /**
     * Get the default instance.
     *
     * @return the default instance
     */
    protected abstract Object getDefaultInstance();
 
    /**
     * Set the configurator
     *
     * @param configurator the configurator
     */
    @XmlTransient
    public void setConfigurator(KernelConfigurator configurator)
    {
       this.configurator = configurator;
    }
 
    public void toString(JBossStringBuilder buffer)
    {
       super.toString(buffer);
       if (type != null)
          buffer.append(" type=").append(type);
    }
 
    /**
     * Get the class info for this type
     *
     * @param cl classloader
     * @return the class info
     * @throws Throwable for any error
     */
    protected ClassInfo getClassInfo(ClassLoader cl) throws Throwable
    {
       return getClassInfo(type, cl);
    }
 
    protected ClassInfo getClassInfo(String classType, ClassLoader cl) throws Throwable
    {
       if (classType == null)
          return null;
 
       return configurator.getClassInfo(classType, cl);
    }
 
    /**
     * Get the type info for this type
     *
     * @param cl classloader
     * @return the type info
     * @throws Throwable for any error
     */
    protected TypeInfo getTypeInfo(ClassLoader cl) throws Throwable
    {
       return getTypeInfo(type, cl);
    }
 
    protected TypeInfo getTypeInfo(String classType, ClassLoader cl) throws Throwable
    {
       if (classType == null)
          return null;
 
       return configurator.getTypeInfo(classType, cl);
    }
 
    protected ClassInfo getClass(MetaDataVisitor visitor, String classType) throws Throwable
    {
       KernelControllerContext context = visitor.getControllerContext();
       ClassLoader cl = Configurator.getClassLoader(context.getBeanMetaData());
       return getClassInfo(classType, cl);
    }
 
 }
