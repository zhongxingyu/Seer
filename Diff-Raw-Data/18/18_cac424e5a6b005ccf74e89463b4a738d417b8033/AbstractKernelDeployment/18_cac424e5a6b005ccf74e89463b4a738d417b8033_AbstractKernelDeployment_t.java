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
 package org.jboss.kernel.plugins.deployment;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.xml.bind.annotation.XmlAnyElement;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElements;
 import javax.xml.bind.annotation.XmlNsForm;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import org.jboss.beans.metadata.plugins.AbstractAnnotationMetaData;
 import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
 import org.jboss.beans.metadata.plugins.AbstractClassLoaderMetaData;
 import org.jboss.beans.metadata.plugins.AbstractLazyMetaData;
 import org.jboss.beans.metadata.plugins.AbstractLifecycleMetaData;
 import org.jboss.beans.metadata.plugins.AbstractNamedAliasMetaData;
 import org.jboss.beans.metadata.plugins.MutableLifecycleHolder;
 import org.jboss.beans.metadata.spi.AnnotationMetaData;
 import org.jboss.beans.metadata.spi.BeanMetaData;
 import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
 import org.jboss.beans.metadata.spi.ClassLoaderMetaData;
 import org.jboss.beans.metadata.spi.LifecycleMetaData;
 import org.jboss.beans.metadata.spi.NamedAliasMetaData;
 import org.jboss.beans.metadata.spi.factory.GenericBeanFactoryMetaData;
 import org.jboss.dependency.spi.ControllerMode;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.kernel.spi.deployment.KernelDeployment;
 import org.jboss.managed.api.annotation.ManagementObject;
 import org.jboss.managed.api.annotation.ManagementProperties;
 import org.jboss.managed.api.annotation.ManagementProperty;
 import org.jboss.util.JBossObject;
 import org.jboss.util.JBossStringBuilder;
 import org.jboss.xb.annotations.JBossXmlSchema;
 
 /**
  * An abstract kernel deployment.
  * 
  * @author <a href="ales.justin@jboss.com">Ales Justin</a>
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 @ManagementObject(properties = ManagementProperties.EXPLICIT) // TODO - explicitly add props we want to manage 
@JBossXmlSchema(namespace="urn:jboss:bean-deployer:2.0", elementFormDefault=XmlNsForm.QUALIFIED, replacePropertyRefs=false)
 @XmlRootElement(name="deployment")
 @XmlType(name="deploymentType", propOrder={"annotations", "classLoader", "beanFactories", "create", "start", "stop", "destroy", "aliases"})
 public class AbstractKernelDeployment extends JBossObject
    implements KernelDeployment, MutableLifecycleHolder, Serializable
 {
    private static final long serialVersionUID = 3l;
 
    /** The name of the deployment */
    protected String name;
 
    /** Whether it is installed */
    protected boolean installed;
 
    /** The installed contexts */
    protected transient List<KernelControllerContext> installedContexts = new CopyOnWriteArrayList<KernelControllerContext>();
 
    /** Is deployment scoped */
    protected Boolean scoped;
 
    /** The annotations */
    protected Set<AnnotationMetaData> annotations;
 
    /** The beans List<BeanMetaDataFactory> */
    protected List<BeanMetaDataFactory> beanFactories;
 
    /** The ClassLoader */
    protected ClassLoaderMetaData classLoader;
 
    /** default create lifecycle method */
    protected LifecycleMetaData create;
 
    /** default start lifecycle method */
    protected LifecycleMetaData start;
 
    /** default stop lifecycle method */
    protected LifecycleMetaData stop;
 
    /** default destroy lifecycle method */
    protected LifecycleMetaData destroy;
 
    /** The ControllerMode */
    protected ControllerMode mode;
 
    /** The aliases */
    protected Set<NamedAliasMetaData> aliases;
 
    /**
     * Create a new kernel deployment
     */
    public AbstractKernelDeployment()
    {
    }
 
    /**
     * Set the bean factories.
     *
     * @deprecated use setBeanFactories
     * @param beans a List<BeanMetaData>.
     */
    @SuppressWarnings("unchecked")
    public void setBeans(List beans)
    {
       this.beanFactories = beans;
       flushJBossObjectCache();
    }
 
    /**
     * Set the bean factories.
     * 
     * @param beanFactories a List<BeanMetaDataFactory>.
     */
    @ManagementProperty(managed=true)
    @XmlElements
    ({
       @XmlElement(name="bean", type=AbstractBeanMetaData.class),
       @XmlElement(name="beanfactory", type=GenericBeanFactoryMetaData.class),
       @XmlElement(name="lazy", type=AbstractLazyMetaData.class)
    })
    @XmlAnyElement
    public void setBeanFactories(List<BeanMetaDataFactory> beanFactories)
    {
       this.beanFactories = beanFactories;
       flushJBossObjectCache();
    }
 
    public String getName()
    {
       return name;
    }
 
    @XmlAttribute
    public void setName(String name)
    {
       this.name = name;
       flushJBossObjectCache();
    }
 
    public boolean isInstalled()
    {
       return installed;
    }
 
    public void setInstalled(boolean installed)
    {
       this.installed = installed;
       flushJBossObjectCache();
    }
 
    public void addInstalledContext(KernelControllerContext context)
    {
       installedContexts.add(context);
       flushJBossObjectCache();
    }
 
    public List<KernelControllerContext> getInstalledContexts()
    {
       return installedContexts;
    }
 
    public void removeInstalledContext(KernelControllerContext context)
    {
       installedContexts.remove(context);
       flushJBossObjectCache();
    }
 
    public List<BeanMetaData> getBeans()
    {
       List<BeanMetaDataFactory> factories = getBeanFactories();
       if (factories == null || factories.size() == 0)
          return null;
       List<BeanMetaData> result = new ArrayList<BeanMetaData>(factories.size());
       for (BeanMetaDataFactory factory : factories)
       {
          List<BeanMetaData> beans = factory.getBeans();
          for (BeanMetaData bmd : beans)
          {
             // check annotations
             if (annotations != null && annotations.isEmpty() == false)
             {
                Set<AnnotationMetaData> annotationsBMD = bmd.getAnnotations();
                if (annotationsBMD == null)
                {
                   annotationsBMD = new HashSet<AnnotationMetaData>();
                   bmd.setAnnotations(annotationsBMD);
                }
                annotationsBMD.addAll(annotations);
             }
             // impl specific
             if (bmd instanceof AbstractBeanMetaData)
             {
                AbstractBeanMetaData bean = (AbstractBeanMetaData)bmd;
                // set deployment defaults, if not already set per bean
                if (bean.getCreate() == null && getCreate() != null)
                {
                   bean.setCreate(getCreate());
                }
                if (bean.getStart() == null && getStart() != null)
                {
                   bean.setStart(getStart());
                }
                if (bean.getStop() == null && getStop() != null)
                {
                   bean.setStop(getStop());
                }
                if (bean.getDestroy() == null && getDestroy() != null)
                {
                   bean.setDestroy(getDestroy());
                }
 
                // controller mode
                if (bean.getMode() == null && getMode() != null)
                {
                   bean.setMode(getMode());
                }
             }
          }
          result.addAll(beans);
       }
       return result;
    }
 
    public Boolean getScoped()
    {
       return scoped;
    }
 
    @XmlAttribute
    public void setScoped(Boolean scoped)
    {
       this.scoped = scoped;
    }
 
    public Set<AnnotationMetaData> getAnnotations()
    {
       return annotations;
    }
 
    @XmlElement(name="annotation", type=AbstractAnnotationMetaData.class)
    public void setAnnotations(Set<AnnotationMetaData> annotations)
    {
       this.annotations = annotations;
    }
 
    public List<BeanMetaDataFactory> getBeanFactories()
    {
       return beanFactories;
    }
 
    public ClassLoaderMetaData getClassLoader()
    {
       return classLoader;
    }
 
    /**
     * Set the classloader
     * 
     * @param classLoader the classloader metadata
     */
    @XmlElement(name="classloader", type=AbstractClassLoaderMetaData.class)
    public void setClassLoader(ClassLoaderMetaData classLoader)
    {
       this.classLoader = classLoader;
    }
 
    public LifecycleMetaData getCreate()
    {
       return create;
    }
 
    @XmlElement(name="create", type=AbstractLifecycleMetaData.class)
    public void setCreate(LifecycleMetaData create)
    {
       create.setState(ControllerState.CREATE);
       this.create = create;
    }
 
    public LifecycleMetaData getStart()
    {
       return start;
    }
 
    @XmlElement(name="start", type=AbstractLifecycleMetaData.class)
    public void setStart(LifecycleMetaData start)
    {
       start.setState(ControllerState.START);
       this.start = start;
    }
 
    public LifecycleMetaData getStop()
    {
       return stop;
    }
 
    @XmlElement(name="stop", type=AbstractLifecycleMetaData.class)
    public void setStop(LifecycleMetaData stop)
    {
       stop.setState(ControllerState.START);
       this.stop = stop;
    }
 
    public LifecycleMetaData getDestroy()
    {
       return destroy;
    }
 
    @XmlElement(name="destroy", type=AbstractLifecycleMetaData.class)
    public void setDestroy(LifecycleMetaData destroy)
    {
       destroy.setState(ControllerState.CREATE);
       this.destroy = destroy;
    }
 
    public Set<NamedAliasMetaData> getAliases()
    {
       return aliases;
    }
 
    @XmlElement(name="alias", type=AbstractNamedAliasMetaData.class)
    public void setAliases(Set<NamedAliasMetaData> aliases)
    {
       this.aliases = aliases;
    }
 
    public ControllerMode getMode()
    {
       return mode;
    }
 
    @XmlAttribute
    public void setMode(ControllerMode mode)
    {
       this.mode = mode;
    }
 
    public void toString(JBossStringBuilder buffer)
    {
       buffer.append("name=").append(name);
       buffer.append(" installed=").append(installed);
       if (classLoader != null)
          buffer.append(" classLoader=").append(classLoader);
       if (beanFactories != null)
          buffer.append(" beanFactories=").append(beanFactories);
    }
 
    public void toShortString(JBossStringBuilder buffer)
    {
       buffer.append(name);
    }
 
    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
       in.defaultReadObject();
       installedContexts = new CopyOnWriteArrayList<KernelControllerContext>();
    }
 }
