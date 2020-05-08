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
 import javax.xml.bind.annotation.XmlType;
 
 import org.jboss.classloading.plugins.metadata.ModuleRequirement;
 import org.jboss.classloading.plugins.metadata.PackageRequirement;
 import org.jboss.classloading.plugins.metadata.UsesPackageRequirement;
 
 /**
  * RequirementsMetaData.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
@XmlType(name="requirements", propOrder={"requirements"})
 public class RequirementsMetaData implements Serializable, Cloneable
 {
    /** The serialVersionUID */
    private static final long serialVersionUID = 3282035888494128833L;
    
    /** The requirements */
    private List<Requirement> requirements;
 
    /**
     * Get the requirements.
     * 
     * @return the requirements.
     */
    public List<Requirement> getRequirements()
    {
       return requirements;
    }
 
    /**
     * Set the requirements.
     * 
     * @param requirements the requirements.
     */
    @XmlElements
    ({
       @XmlElement(name="module", type=ModuleRequirement.class),
       @XmlElement(name="package", type=PackageRequirement.class),
       @XmlElement(name="uses", type=UsesPackageRequirement.class)
    })
    @XmlAnyElement
    public void setRequirements(List<Requirement> requirements)
    {
       this.requirements = requirements;
    }
 
    /**
     * Add a requirement
     * 
     * @param requirement the requirement
     * @throws IllegalArgumentException for a null requirement
     */
    public void addRequirement(Requirement requirement)
    {
       if (requirement == null)
          throw new IllegalArgumentException("Null requirement");
       if (requirements == null)
          requirements = new CopyOnWriteArrayList<Requirement>();
       requirements.add(requirement);
    }
 
    /**
     * Remove a requirement
     * 
     * @param requirement the requirement
     * @throws IllegalArgumentException for a null requirement
     */
    public void removeRequirement(Requirement requirement)
    {
       if (requirement == null)
          throw new IllegalArgumentException("Null requirement");
       if (requirements == null)
          return;
       requirements.remove(requirement);
    }
 
    @Override
    public RequirementsMetaData clone()
    {
       try
       {
          RequirementsMetaData clone = (RequirementsMetaData) super.clone();
          if (requirements != null)
          {
             List<Requirement> clonedRequirements = new CopyOnWriteArrayList<Requirement>(requirements);
             clone.setRequirements(clonedRequirements);
          }
          return clone;
       }
       catch (CloneNotSupportedException e)
       {
          throw new RuntimeException("Unexpected", e);
       }
    }
 }
