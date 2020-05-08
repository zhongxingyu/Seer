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
 package org.jboss.classloading.spi.helpers;
 
 import java.io.Serializable;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlTransient;
 
 import org.jboss.classloading.spi.version.Version;
 import org.jboss.classloading.spi.version.VersionComparatorRegistry;
 
 /**
  * NameAndVersionSupport.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class NameAndVersionSupport implements Serializable, Cloneable
 {
    /** The serialVersionUID */
    private static final long serialVersionUID = 6943685422194480909L;
 
    /** The name  */
   private String name = "<unknwown>";
    
    /** The version */
    private Object version = Version.DEFAULT_VERSION;
 
    /**
     * Create a new NameAndVersionSupport with the default version
     */
    public NameAndVersionSupport()
    {
    }
 
    /**
     * Create a new NameAndVersionSupport with the default version
     * 
     * @param name the name
     * @throws IllegalArgumentException for a null name
     */
    public NameAndVersionSupport(String name)
    {
       this(name, null);
    }
    
    /**
     * Create a new NameAndVersionSupport.
     * 
     * @param name the name
     * @param version the version - pass null for default version
     * @throws IllegalArgumentException for a null name
     */
    public NameAndVersionSupport(String name, Object version)
    {
       if (name == null)
          throw new IllegalArgumentException("Null name");
       if (version == null)
          version = Version.DEFAULT_VERSION;
       this.name = name;
       this.version = version;
    }
    
    /**
     * Get the name.
     * 
     * @return the name.
     */
    public String getName()
    {
       return name;
    }
 
    /**
     * Set the name.
     * 
     * @param name the name.
     */
    @XmlAttribute
    public void setName(String name)
    {
       if (name == null)
          throw new IllegalArgumentException("Null name");
       this.name = name;
    }
 
    /**
     * Get the version.
     * 
     * @return the version.
     */
    public Object getVersion()
    {
       return version;
    }
 
    /**
     * Set the version.
     * 
     * @param version the version.
     */
    @XmlTransient
    public void setVersion(Object version)
    {
       if (version == null)
          version = Version.DEFAULT_VERSION;
       this.version = version;
    }
 
    /**
     * Get the version.
     * 
     * @return the version.
     */
    public Version getTheVersion()
    {
       Object version = getVersion();
       if (version instanceof Version)
          return (Version) version;
       if (version instanceof String)
          return Version.parseVersion((String) version);
       throw new IllegalStateException(version + " is not an instanceof version");
    }
 
    /**
     * Set the version.
     * 
     * @param version the version.
     */
    @XmlAttribute(name="version")
    public void setTheVersion(Version version)
    {
       setVersion(version);
    }
 
    @Override
    public boolean equals(Object obj)
    {
       if (obj == this)
          return true;
       if (obj == null || obj instanceof NameAndVersionSupport == false)
          return false;
       NameAndVersionSupport other = (NameAndVersionSupport) obj;
       if (getName().equals(other.getName()) == false)
          return false;
       return VersionComparatorRegistry.getInstance().same(getVersion(), other.getVersion());
    }
    
    @Override
    public int hashCode()
    {
       return getName().hashCode();
    }
    
    @Override
    public String toString()
    {
       return getClass().getSimpleName() + " " + getName() + ":" + getVersion();
    }
 
    @Override
    public NameAndVersionSupport clone()
    {
       try
       {
          NameAndVersionSupport clone = (NameAndVersionSupport) super.clone();
          return clone;
       }
       catch (CloneNotSupportedException e)
       {
          throw new RuntimeException("Unexpected", e);
       }
    }
 }
