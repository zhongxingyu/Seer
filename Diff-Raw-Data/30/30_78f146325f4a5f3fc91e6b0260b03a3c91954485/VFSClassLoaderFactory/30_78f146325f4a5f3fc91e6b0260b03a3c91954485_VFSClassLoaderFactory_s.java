 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, JBoss Inc., and individual contributors as indicated
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
 package org.jboss.classloading.spi.vfs.metadata;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 
 import org.jboss.beans.metadata.spi.BeanMetaData;
 import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
 import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
 import org.jboss.classloader.spi.ClassLoaderSystem;
 import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
 import org.jboss.classloading.spi.version.Version;
 import org.jboss.classloading.spi.vfs.dependency.VFSClassLoaderPolicyModule;
 
 /**
  * VFSClassLoaderFactory.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class VFSClassLoaderFactory extends ClassLoadingMetaData implements BeanMetaDataFactory
 {
    /** The serialVersionUID */
    private static final long serialVersionUID = -4446195023830263521L;
 
    /** The default name for the classloading system */
    public static final String DEFAULT_CLASSLOADER_SYSTEM_NAME = "ClassLoaderSystem";
    
    /** The name of the classloader system */
    private String classLoaderSystemName = DEFAULT_CLASSLOADER_SYSTEM_NAME;
    
    /** The context name */
    private String contextName = null;
    
    /** The roots */
    private List<String> roots = new CopyOnWriteArrayList<String>();
 
    /**
     * Create a new VFSClassLoaderFactory.
     */
    public VFSClassLoaderFactory()
    {
    }
 
    /**
     * Create a new VFSClassLoaderFactory.
     * 
     * @param name the name
     */
    public VFSClassLoaderFactory(String name)
    {
       this(name, Version.DEFAULT_VERSION);
    }
 
    /**
     * Create a new VFSClassLoaderFactory.
     * 
     * @param name the name
     * @param version the version
     */
    public VFSClassLoaderFactory(String name, String version)
    {
       this(name, Version.parseVersion(version));
    }
 
    /**
     * Create a new VFSClassLoaderFactory.
     * 
     * @param name the name
     * @param version the version
     */
    public VFSClassLoaderFactory(String name, Version version)
    {
       setName(name);
       setVersion(version);
    }
    
    /**
     * Get the classLoaderSystemName.
     * 
     * @return the classLoaderSystemName.
     */
    public String getClassLoaderSystemName()
    {
       return classLoaderSystemName;
    }
 
    /**
     * Set the classLoaderSystemName.
     * 
     * @param classLoaderSystemName the classLoaderSystemName.
     */
    @XmlAttribute(name="system")
    public void setClassLoaderSystemName(String classLoaderSystemName)
    {
       if (classLoaderSystemName == null)
          classLoaderSystemName = DEFAULT_CLASSLOADER_SYSTEM_NAME;
       this.classLoaderSystemName = classLoaderSystemName;
    }
 
    /**
     * Get the contextName.
     * 
     * @return the contextName.
     */
    public String getContextName()
    {
       return contextName;
    }
 
    /**
     * Set the contextName.
     * 
     * @param contextName the contextName.
     */
    @XmlAttribute(name="context")
    public void setContextName(String contextName)
    {
       this.contextName = contextName;
    }
 
    /**
     * Get the roots.
     * 
     * @return the roots.
     */
    public List<String> getRoots()
    {
       return roots;
    }
 
    /**
     * Set the roots.
     * 
     * @param roots the roots.
     */
    @XmlElement(name="root")
    public void setRoots(List<String> roots)
    {
       if (roots == null)
          roots = new CopyOnWriteArrayList<String>();
       this.roots = roots;
    }
 
    public List<BeanMetaData> getBeans()
    {
       // Determine some properties
       String contextName = getContextName();
       if (contextName == null)
          contextName = getName() + ":" + getVersion().toString(); 
       String moduleName = contextName + "$MODULE";
       
       // Create the module
       BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(moduleName, VFSClassLoaderPolicyModule.class.getName());
       builder.addConstructorParameter(VFSClassLoaderFactory.class.getName(), this);
       builder.addConstructorParameter(String.class.getName(), moduleName);
       builder.addPropertyMetaData("roots", roots);
       builder.setNoClassLoader();
       builder.addUninstall("removeClassLoader");
       BeanMetaData module = builder.getBeanMetaData();
       
       // Create the classloader
       builder = BeanMetaDataBuilder.createBuilder(contextName, ClassLoader.class.getName());
       builder.setNoClassLoader();
       builder.setFactory(moduleName);
       builder.setFactoryMethod("registerClassLoaderPolicy");
       builder.addConstructorParameter(ClassLoaderSystem.class.getName(), builder.createInject(classLoaderSystemName));
       BeanMetaData classLoader = builder.getBeanMetaData();
       
       return Arrays.asList(classLoader, module);
    }
 }
