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
 package org.jboss.classloading.spi.metadata;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.xml.bind.annotation.XmlAnyElement;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElements;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import org.jboss.classloading.plugins.metadata.ModuleCapability;
 import org.jboss.classloading.plugins.metadata.PackageCapability;
 
 /**
  * CapabilitiesMetaData.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
@XmlType(name="capabilties", propOrder= {"capabilities"})
 @XmlRootElement(name="capabilities", namespace="urn:jboss:classloading:1.0")
 public class CapabilitiesMetaData implements Serializable, Cloneable
 {
    /** The serialVersionUID */
    private static final long serialVersionUID = -7910704924025591308L;
    
    /** The capabilities */
    private List<Capability> capabilities;
 
    /**
     * Get the capabilities.
     * 
     * @return the capabilities.
     */
    public List<Capability> getCapabilities()
    {
       return capabilities;
    }
 
    /**
     * Set the capabilities.
     * 
     * @param capabilities the capabilities.
     */
    @XmlElements
    ({
       @XmlElement(name="module", type=ModuleCapability.class),
       @XmlElement(name="package", type=PackageCapability.class)
    })
    @XmlAnyElement
    public void setCapabilities(List<Capability> capabilities)
    {
       this.capabilities = capabilities;
    }
 
    /**
     * Add a capability
     * 
     * @param capability the capability
     * @throws IllegalArgumentException for a null capability
     */
    public void addCapability(Capability capability)
    {
       if (capability == null)
          throw new IllegalArgumentException("Null capability");
       if (capabilities == null)
          capabilities = new CopyOnWriteArrayList<Capability>();
       capabilities.add(capability);
    }
 
    /**
     * Remove a capability
     * 
     * @param capability the capability
     * @throws IllegalArgumentException for a null capability
     */
    public void removeCapability(Capability capability)
    {
       if (capability == null)
          throw new IllegalArgumentException("Null capability");
       if (capabilities == null)
          return;
       capabilities.remove(capability);
    }
 
    @Override
    public CapabilitiesMetaData clone()
    {
       try
       {
          CapabilitiesMetaData clone = (CapabilitiesMetaData) super.clone();
          if (capabilities != null)
          {
             List<Capability> clonedCapabilities = new CopyOnWriteArrayList<Capability>(capabilities);
             clone.setCapabilities(clonedCapabilities);
          }
          return clone;
       }
       catch (CloneNotSupportedException e)
       {
          throw new RuntimeException("Unexpected", e);
       }
    }
 }
