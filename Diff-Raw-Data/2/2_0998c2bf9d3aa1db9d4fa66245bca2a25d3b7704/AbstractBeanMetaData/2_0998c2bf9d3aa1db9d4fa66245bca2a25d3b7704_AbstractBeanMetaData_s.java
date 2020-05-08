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
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import org.jboss.beans.metadata.spi.AutowireType;
 import org.jboss.beans.metadata.spi.BeanMetaData;
 import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
 import org.jboss.beans.metadata.spi.CallbackMetaData;
 import org.jboss.beans.metadata.spi.ClassLoaderMetaData;
 import org.jboss.beans.metadata.spi.ConstructorMetaData;
 import org.jboss.beans.metadata.spi.DemandMetaData;
 import org.jboss.beans.metadata.spi.DependencyMetaData;
 import org.jboss.beans.metadata.spi.InstallMetaData;
 import org.jboss.beans.metadata.spi.LifecycleMetaData;
 import org.jboss.beans.metadata.spi.MetaDataVisitor;
 import org.jboss.beans.metadata.spi.MetaDataVisitorNode;
 import org.jboss.beans.metadata.spi.PropertyMetaData;
 import org.jboss.beans.metadata.spi.SupplyMetaData;
 import org.jboss.dependency.plugins.AbstractDependencyItem;
 import org.jboss.dependency.spi.Controller;
 import org.jboss.dependency.spi.ControllerContext;
 import org.jboss.dependency.spi.ControllerMode;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.dependency.spi.DependencyItem;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.managed.api.annotation.ManagementObject;
 import org.jboss.managed.api.annotation.ManagementProperties;
 import org.jboss.managed.api.annotation.ManagementProperty;
 import org.jboss.reflect.spi.TypeInfo;
 import org.jboss.util.JBossObject;
 import org.jboss.util.JBossStringBuilder;
 
 /**
  * Metadata for a bean.
  *
  * @author <a href="ales.justin@jboss.com">Ales Justin</a>
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 @ManagementObject(properties = ManagementProperties.EXPLICIT) // TODO - explicitly add props we want to manage
 @XmlRootElement(name="bean")
 @XmlType(name="beanType", propOrder={"aliases", "annotations", "classLoader", "constructor", "properties", "create", "start", "stop", "destroy", "depends", "demands", "supplies", "installs", "uninstalls", "installCallbacks", "uninstallCallbacks"})
 public class AbstractBeanMetaData extends AbstractFeatureMetaData
    implements BeanMetaData, BeanMetaDataFactory, MutableLifecycleHolder, Serializable
 {
    private static final long serialVersionUID = 3L;
 
    /** The bean fully qualified class name */
    protected String bean;
 
    /** The name of this instance */
    protected String name;
 
    /** The aliases */
    protected Set<Object> aliases;
 
    /** The parent */
    protected String parent;
 
    /** Is abstract */
    protected boolean isAbstract; 
 
    /** Autowire type */
    protected AutowireType autowireType;
 
    /** The mode */
    protected ControllerMode mode;
 
    /** Is contextual injection candidate */
    protected boolean autowireCandidate = true;
 
    /** The properties configuration Set<PropertyMetaData> */
    private Set<PropertyMetaData> properties;
 
    /** The bean ClassLoader */
    protected ClassLoaderMetaData classLoader;
 
    /** The constructor */
    protected ConstructorMetaData constructor;
 
    /** The create lifecycle */
    protected LifecycleMetaData create;
 
    /** The start lifecycle */
    protected LifecycleMetaData start;
 
    /** The stop lifecycle */
    protected LifecycleMetaData stop;
 
    /** The destroy lifecycle */
    protected LifecycleMetaData destroy;
 
    /** What the bean demands Set<DemandMetaData> */
    protected Set<DemandMetaData> demands;
 
    /** What the bean supplies Set<SupplyMetaData> */
    protected Set<SupplyMetaData> supplies;
 
    /** What the bean dependencies Set<DependencyMetaData> */
    protected Set<DependencyMetaData> depends;
 
    /** The install operations List<InstallMetaData> */
    protected List<InstallMetaData> installs;
 
    /** The uninstall operations List<InstallMetaData> */
    protected List<InstallMetaData> uninstalls;
 
    /** The install callback List<InstallMetaData> */
    protected List<CallbackMetaData> installCallbacks;
 
    /** The uninstall callback List<InstallMetaData> */
    protected List<CallbackMetaData> uninstallCallbacks;
 
    /** The context */
    protected transient ControllerContext context;
 
    /**
     * Create a new bean meta data
     */
    public AbstractBeanMetaData()
    {
       super();
    }
 
    /**
     * Create a new bean meta data
     *
     * @param bean the bean class name
     */
    public AbstractBeanMetaData(String bean)
    {
       this.bean = bean;
    }
    /**
     * Create a new bean meta data
     *
     * @param name the name
     * @param bean the bean class name
     */
    public AbstractBeanMetaData(String name, String bean)
    {
       this.name = name;
       this.bean = bean;
    }
 
    public List<BeanMetaData> getBeans()
    {
       List<BeanMetaData> nestedBeans = findNestedBeans();
       if (nestedBeans.isEmpty())
       {
          return Collections.singletonList((BeanMetaData)this);
       }
       else
       {
          nestedBeans.add(this);
          return nestedBeans;
       }
    }
 
    protected List<BeanMetaData> findNestedBeans()
    {
       List<BeanMetaData> allBeans = new ArrayList<BeanMetaData>();
       addBeans(this, allBeans);
       return allBeans;
    }
 
    protected void addBeans(MetaDataVisitorNode current, List<BeanMetaData> list)
    {
       for(Iterator<? extends MetaDataVisitorNode> children = current.getChildren(); children != null && children.hasNext();)
       {
          MetaDataVisitorNode next = children.next();
          if (next instanceof BeanMetaDataFactory)
          {
             list.addAll(((BeanMetaDataFactory) next).getBeans());
          }
          else
          {
             addBeans(next, list);
             if (next instanceof BeanMetaData)
             {
                list.add((BeanMetaData) current);
             }
          }
       }
    }
 
    /**
     * Get the bean class name.
     * @return the fully qualified bean class name.
     */
    public String getBean()
    {
       return bean;
    }
 
    /**
     * Set the bean class name and flush the object cache.
     *
     * @param bean The bean class name to set.
     */
    @XmlAttribute(name="class")
    public void setBean(String bean)
    {
       this.bean = bean;
       flushJBossObjectCache();
    }
 
    /**
     * Get a property
     *
     * @param name the name
     * @return the property name
     */
    public PropertyMetaData getProperty(String name)
    {
       if (name == null)
          throw new IllegalArgumentException("Null name");
       if (properties != null && properties.size() > 0)
       {
          for (PropertyMetaData prop : properties)
          {
             if (name.equals(prop.getName()))
                return prop;
          }
       }
       return null;
    }
 
    /**
     * Add a property
     *
     * @param property the property
     */
    public void addProperty(PropertyMetaData property)
    {
       if (property == null)
          throw new IllegalArgumentException("Null property");
       if (properties == null)
          properties = new HashSet<PropertyMetaData>();
       properties.add(property);
       flushJBossObjectCache();
    }
 
    /**
     * Set the propertiess.
     *
     * @param properties Set<PropertyMetaData>
     */
    @ManagementProperty(managed=true) // TODO - this ok? 
    @XmlElement(name="property", type=AbstractPropertyMetaData.class)
    public void setProperties(Set<PropertyMetaData> properties)
    {
       this.properties = properties;
       flushJBossObjectCache();
    }
 
    public ClassLoaderMetaData getClassLoader()
    {
       return classLoader;
    }
 
    @XmlElement(name="classloader", type=AbstractClassLoaderMetaData.class)
    public void setClassLoader(ClassLoaderMetaData classLoader)
    {
       this.classLoader = classLoader;
    }
 
    /**
     * Set the constructor
     *
     * @param constructor the constructor metadata
     */
    @XmlElement(name="constructor", type=AbstractConstructorMetaData.class)
    public void setConstructor(ConstructorMetaData constructor)
    {
       this.constructor = constructor;
    }
 
    /**
     * Set what the bean demands.
     *
     * @param demands Set<DemandMetaData>
     */
    @XmlElement(name="demand", type=AbstractDemandMetaData.class)
    public void setDemands(Set<DemandMetaData> demands)
    {
       this.demands = demands;
       flushJBossObjectCache();
    }
 
    /**
     * Set what the bean supplies.
     *
     * @param supplies Set<SupplyMetaData>
     */
    @XmlElement(name="supply", type=AbstractSupplyMetaData.class)
    public void setSupplies(Set<SupplyMetaData> supplies)
    {
       this.supplies = supplies;
       flushJBossObjectCache();
    }
 
    /**
     * Set what the bean depends.
     *
     * @param depends Set<DependencyMetaData>
     */
    @XmlElement(name="depends", type=AbstractDependencyMetaData.class)
    public void setDepends(Set<DependencyMetaData> depends)
    {
       this.depends = depends;
       flushJBossObjectCache();
    }
 
    public String getName()
    {
       return name;
    }
 
    /**
     * Set the name.
     *
     * @param name The name to set.
     */
    @XmlAttribute
    public void setName(String name)
    {
       this.name = name;
       flushJBossObjectCache();
    }
 
    public Set<Object> getAliases()
    {
       return aliases;
    }
 
    @XmlElement(name="alias", type=String.class)
    public void setAliases(Set<Object> aliases)
    {
       this.aliases = aliases;
    }
 
    public String getParent()
    {
       return parent;
    }
 
    /**
     * Set the parent.
     *
     * @param parent the parent name
     */
    @XmlAttribute
    public void setParent(String parent)
    {
       this.parent = parent;
    }
 
    public boolean isAbstract()
    {
       return isAbstract;
    }
 
    /**
     * Set abstract.
     *
     * @param anAbstract is abstract
     */
    @XmlAttribute
    public void setAbstract(boolean anAbstract)
    {
       isAbstract = anAbstract;
    }
 
    public AutowireType getAutowireType()
    {
       return autowireType;
    }
 
    /**
     * Set autowire type.
     *
     * @param autowireType the type
     */
    @XmlAttribute(name="autowire-type")
    public void setAutowireType(AutowireType autowireType)
    {
       this.autowireType = autowireType;
    }
 
    public ControllerMode getMode()
    {
       return mode;
    }
 
    @XmlAttribute
    public void setMode(ControllerMode mode)
    {
       this.mode = mode;
       flushJBossObjectCache();
    }
 
    public boolean isAutowireCandidate()
    {
       return autowireCandidate;
    }
 
    @XmlAttribute(name="autowire-candidate")
    public void setAutowireCandidate(boolean autowireCandidate)
    {
       this.autowireCandidate = autowireCandidate;
    }
 
    public Set<PropertyMetaData> getProperties()
    {
       return properties;
    }
 
    public ConstructorMetaData getConstructor()
    {
       return constructor;
    }
 
    public LifecycleMetaData getCreate()
    {
       return create;
    }
 
    /**
     * Set the lifecycle metadata
     *
     * @param lifecycle the lifecycle metadata
     */
    @XmlElement(name="create", type=AbstractLifecycleMetaData.class)
    public void setCreate(LifecycleMetaData lifecycle)
    {
       lifecycle.setState(ControllerState.CREATE);
       this.create = lifecycle;
    }
 
    public LifecycleMetaData getStart()
    {
       return start;
    }
 
    /**
     * Set the start metadata
     *
     * @param lifecycle the lifecycle metadata
     */
    @XmlElement(name="start", type=AbstractLifecycleMetaData.class)
    public void setStart(LifecycleMetaData lifecycle)
    {
       lifecycle.setState(ControllerState.START);
       this.start = lifecycle;
    }
 
    public LifecycleMetaData getStop()
    {
       return stop;
    }
 
    /**
     * Set the stop metadata
     *
     * @param lifecycle the lifecycle metadata
     */
    @XmlElement(name="stop", type=AbstractLifecycleMetaData.class)
    public void setStop(LifecycleMetaData lifecycle)
    {
       lifecycle.setState(ControllerState.START);
       this.stop = lifecycle;
    }
 
    public LifecycleMetaData getDestroy()
    {
       return destroy;
    }
 
    /**
     * Set the destroy metadata
     *
     * @param lifecycle the lifecycle metadata
     */
    @XmlElement(name="destroy", type=AbstractLifecycleMetaData.class)
    public void setDestroy(LifecycleMetaData lifecycle)
    {
       lifecycle.setState(ControllerState.CREATE);
       this.destroy = lifecycle;
    }
 
    public Set<DemandMetaData> getDemands()
    {
       return demands;
    }
 
    public Set<SupplyMetaData> getSupplies()
    {
       return supplies;
    }
 
    public Set<DependencyMetaData> getDepends()
    {
       return depends;
    }
 
    public List<InstallMetaData> getInstalls()
    {
       return installs;
    }
 
    /**
     * Set the installs
     *
     * @param installs List<InstallMetaData>
     */
    @XmlElement(name="install", type=AbstractInstallMetaData.class)
    public void setInstalls(List<InstallMetaData> installs)
    {
       this.installs = installs;
       flushJBossObjectCache();
    }
 
    public List<InstallMetaData> getUninstalls()
    {
       return uninstalls;
    }
 
    /**
     * Set the uninstalls
     *
     * @param uninstalls List<InstallMetaData>
     */
    @XmlElement(name="uninstall", type=AbstractInstallMetaData.class)
    public void setUninstalls(List<InstallMetaData> uninstalls)
    {
       this.uninstalls = uninstalls;
       flushJBossObjectCache();
    }
 
    public List<CallbackMetaData> getInstallCallbacks()
    {
       return installCallbacks;
    }
 
    @XmlElement(name="incallback", type=InstallCallbackMetaData.class)
    public void setInstallCallbacks(List<CallbackMetaData> installCallbacks)
    {
       this.installCallbacks = installCallbacks;
       flushJBossObjectCache();
    }
 
    public List<CallbackMetaData> getUninstallCallbacks()
    {
       return uninstallCallbacks;
    }
 
    @XmlElement(name="uncallback", type=UninstallCallbackMetaData.class)
    public void setUninstallCallbacks(List<CallbackMetaData> uninstallCallbacks)
    {
       this.uninstallCallbacks = uninstallCallbacks;
       flushJBossObjectCache();
    }
 
    public void initialVisit(MetaDataVisitor visitor)
    {
       KernelControllerContext ctx = visitor.getControllerContext();
       if (ctx.getBeanMetaData() == this)
          context = ctx;
       boolean isInnerBean = visitor.visitorNodeStack().isEmpty() == false;
       if (isInnerBean)
       {
          Object name = ctx.getName();
          Object iDependOn = getUnderlyingValue();
          ControllerState whenRequired = visitor.getContextState();
          DependencyItem di = new AbstractDependencyItem(name, iDependOn, whenRequired, ControllerState.INSTALLED);
          visitor.addDependency(di);
       }
       super.initialVisit(visitor);
    }
 
    protected void addChildren(Set<MetaDataVisitorNode> children)
    {
       super.addChildren(children);
       if (classLoader != null && classLoader.getClassLoader() != this)
          children.add(classLoader);
       if (constructor != null)
          children.add(constructor);
       if (properties != null)
          children.addAll(properties);
       if (create != null)
          children.add(create);
       if (start != null)
          children.add(start);
       if (stop != null)
          children.add(stop);
       if (destroy != null)
          children.add(destroy);
       if (demands != null)
          children.addAll(demands);
       if (supplies != null)
          children.addAll(supplies);
       if (depends != null)
          children.addAll(depends);
       if (installs != null)
          children.addAll(installs);
       if (uninstalls != null)
          children.addAll(uninstalls);
       if (installCallbacks != null)
          children.addAll(installCallbacks);
       if (uninstallCallbacks != null)
          children.addAll(uninstallCallbacks);
    }
 
    public TypeInfo getType(MetaDataVisitor visitor, MetaDataVisitorNode previous) throws Throwable
    {
       throw new IllegalArgumentException("Cannot determine inject class type: " + this);
    }
 
    public Object getUnderlyingValue()
    {
       return name;
    }
 
    @SuppressWarnings("unchecked")
    public Object getValue(TypeInfo info, ClassLoader cl) throws Throwable
    {
       Controller controller = context.getController();
       ControllerContext lookup = controller.getInstalledContext(name);
       if (lookup == null || lookup.getTarget() == null)
       {
          // possible call for classloader
          if (info == null && classLoader != null && classLoader.getClassLoader() == this)
          {
             return cl;
          }
          throw new IllegalArgumentException("Bean not yet installed: " + name);
       }
       Object target = lookup.getTarget();
       // TODO - add progression here as well?
       if (info != null && info.getType().isAssignableFrom(target.getClass()) == false)
       {
          throw new ClassCastException(target + " is not a " + info);
       }
       return target;
    }
 
    public void toString(JBossStringBuilder buffer)
    {
       buffer.append("name=").append(name);
       if (aliases != null)
          buffer.append(" aliases=").append(aliases);
       buffer.append(" bean=").append(bean);
       buffer.append(" properties=");
       JBossObject.list(buffer, properties);
       if (classLoader != null && classLoader.getClassLoader() != this)
          buffer.append(" classLoader=").append(classLoader);
       buffer.append(" constructor=").append(constructor);
       buffer.append(" autowireCandidate=").append(autowireCandidate);
       if (create != null)
          buffer.append(" create=").append(create);
       if (start != null)
          buffer.append(" start=").append(start);
       if (stop != null)
          buffer.append(" stop=").append(stop);
       if (destroy != null)
          buffer.append(" destroy=").append(destroy);
       if (demands != null)
       {
          buffer.append(" demands=");
          JBossObject.list(buffer, demands);
       }
       super.toString(buffer);
       if (supplies != null)
       {
          buffer.append(" supplies=");
          JBossObject.list(buffer, supplies);
       }
       if (depends != null)
       {
          buffer.append(" depends=");
          JBossObject.list(buffer, depends);
       }
       if (installs != null)
       {
          buffer.append(" installs=");
          JBossObject.list(buffer, installs);
       }
       if (uninstalls != null)
       {
          buffer.append(" uninstalls=");
          JBossObject.list(buffer, uninstalls);
       }
       if (installCallbacks != null)
       {
          buffer.append(" installCallbacks=");
          JBossObject.list(buffer, installCallbacks);
       }
       if (uninstallCallbacks != null)
       {
          buffer.append(" uninstallCallbacks=");
          JBossObject.list(buffer, uninstallCallbacks);
       }
    }
 
    public void toShortString(JBossStringBuilder buffer)
    {
       buffer.append(bean);
       buffer.append('/');
       buffer.append(name);
    }
 }
