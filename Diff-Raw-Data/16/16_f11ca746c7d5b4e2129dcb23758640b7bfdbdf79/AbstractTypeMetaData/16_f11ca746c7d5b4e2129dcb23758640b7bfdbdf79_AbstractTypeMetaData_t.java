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
 
 import org.jboss.beans.metadata.spi.BeanMetaData;
 import org.jboss.beans.metadata.spi.MetaDataVisitor;
 import org.jboss.beans.metadata.spi.PropertyMetaData;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.joinpoint.spi.TargettedJoinpoint;
 import org.jboss.kernel.plugins.config.Configurator;
 import org.jboss.kernel.spi.config.KernelConfigurator;
 import org.jboss.kernel.spi.dependency.KernelController;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.reflect.spi.ClassInfo;
 import org.jboss.util.JBossStringBuilder;
 
 /**
  * A typed value.
  *
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public abstract class AbstractTypeMetaData extends AbstractValueMetaData
 {
    /**
     * The type
     */
    protected String type;
 
    /**
     * The configurator
     */
    protected KernelController controller;
 
    /**
     * The configurator
     */
    protected KernelConfigurator configurator;
 
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
       controller = (KernelController) visitor.getControllerContext().getController();
       configurator = visitor.getControllerContext().getKernel().getConfigurator();
       preparePreinstantiatedLookup(visitor);
       visitor.initialVisit(this);
    }
 
    private void preparePreinstantiatedLookup(MetaDataVisitor visitor)
    {
       Object parent = visitor.visitorNodeStack().pop();
       try
       {
          if (parent instanceof PropertyMetaData)
          {
             PropertyMetaData pmd = (PropertyMetaData) parent;
             propertyName = pmd.getName();
             Object gp = visitor.visitorNodeStack().peek();
             if (gp instanceof BeanMetaData)
             {
                BeanMetaData bmd = (BeanMetaData) gp;
                beanName = bmd.getName();
             }
          }
       }
       finally
       {
          visitor.visitorNodeStack().push(parent);
       }
    }
 
    protected Object preinstantiatedLookup(ClassLoader cl, Class expected)
    {
      Object result = null;
       if (propertyName != null && beanName != null)
       {
          try
          {
             KernelControllerContext context = (KernelControllerContext) controller.getContext(beanName, ControllerState.INSTANTIATED);
             TargettedJoinpoint joinpoint = configurator.getPropertyGetterJoinPoint(context.getBeanInfo(), propertyName);
             joinpoint.setTarget(context.getTarget());
            result = joinpoint.dispatch();
          }
          catch (Throwable t)
          {
            log.warn("Exception in preinstantiated lookup: " + t);
          }
         if (result != null && expected != null && expected.isAssignableFrom(result.getClass()) == false)
            throw new ClassCastException(result.getClass() + " is not a " + expected.getName());
       }
      return result;
    }
 
    /**
     * Set the configurator
     *
     * @param configurator the configurator
     */
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
 
    protected Class getClass(MetaDataVisitor visitor, String classType) throws Throwable
    {
       KernelControllerContext context = visitor.getControllerContext();
       ClassLoader cl = Configurator.getClassLoader(context.getBeanMetaData());
       return getClassInfo(classType, cl).getType();
    }
 
 }
