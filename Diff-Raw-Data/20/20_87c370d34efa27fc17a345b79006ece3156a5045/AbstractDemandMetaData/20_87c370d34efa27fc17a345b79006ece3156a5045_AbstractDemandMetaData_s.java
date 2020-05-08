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
 import java.util.Iterator;
 
 import javax.xml.bind.annotation.XmlTransient;
 import javax.xml.bind.annotation.XmlType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlValue;
 
 import org.jboss.beans.metadata.spi.DemandMetaData;
 import org.jboss.beans.metadata.spi.MetaDataVisitor;
 import org.jboss.beans.metadata.spi.MetaDataVisitorNode;
 import org.jboss.dependency.plugins.AbstractDependencyItem;
 import org.jboss.dependency.spi.Controller;
 import org.jboss.dependency.spi.ControllerContext;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.dependency.spi.DependencyItem;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.kernel.api.dependency.MatcherFactory;
 import org.jboss.kernel.api.dependency.Matcher;
 import org.jboss.util.JBossObject;
 import org.jboss.util.JBossStringBuilder;
 import org.jboss.util.HashCode;
 
 /**
  * A demand.
  * 
  * @author <a href="ales.justin@jboss.com">Ales Justin</a>
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 @XmlType
 public class AbstractDemandMetaData extends JBossObject
    implements DemandMetaData, Serializable
 {
    private static final long serialVersionUID = 3L;
 
    /** The demand */
    protected Object demand;
    
    /** When the dependency is required */
    protected ControllerState whenRequired = ControllerState.DESCRIBED;
 
    /** The transformer */
    protected String transformer;
 
    /**
     * Create a new demand
     */
    public AbstractDemandMetaData()
    {
    }
 
    /**
     * Create a new demand
     * 
     * @param demand the demand
     */
    public AbstractDemandMetaData(Object demand)
    {
       this.demand = demand;
    }
    
    /**
     * Set the required state of the dependency
     * 
     * @param whenRequired when the dependecy is required
     */
    @XmlAttribute(name="state")
    public void setWhenRequired(ControllerState whenRequired)
    {
       this.whenRequired = whenRequired;
       flushJBossObjectCache();
    }
    
    /**
     * Set the demand
     * 
     * @param demand the demand
     */
    @XmlValue
    public void setDemand(Object demand)
    {
       this.demand = demand;
       flushJBossObjectCache();
    }
 
    public Object getDemand()
    {
       return demand;
    }
 
    public ControllerState getWhenRequired()
    {
       return whenRequired;
    }
 
    /**
     * Get the transformer class name.
     *
     * @return the transformer class name
     */
    public String getTransformer()
    {
       return transformer;
    }
 
    /**
     * Set the transformer class name.
     *
     * @param transformer the transformer class name
     */
    @XmlAttribute
    public void setTransformer(String transformer)
    {
       this.transformer = transformer;
    }
 
    public void initialVisit(MetaDataVisitor visitor)
    {
       KernelControllerContext context = visitor.getControllerContext();
       DependencyItem item = new DemandDependencyItem(context.getName());
       visitor.addDependency(item);
       visitor.initialVisit(this);
    }
 
    public void describeVisit(MetaDataVisitor vistor)
    {
       vistor.describeVisit(this);
    }
 
    @XmlTransient
    public Iterator<? extends MetaDataVisitorNode> getChildren()
    {
       return null;
    }
    
    public void toString(JBossStringBuilder buffer)
    {
       buffer.append("demand=").append(demand);
       if (whenRequired != null)
          buffer.append(" whenRequired=").append(whenRequired.getStateString());
    }
    
    public void toShortString(JBossStringBuilder buffer)
    {
       buffer.append(demand);
    }
 
    public boolean equals(Object obj)
    {
       if (obj instanceof AbstractDemandMetaData == false)
          return false;
       return equals(demand, ((AbstractDemandMetaData)obj).demand);
    }
 
    protected int getHashCode()
    {
       return HashCode.generate(demand);
    }
 
    /**
     * Information about a demand dependency.
     */
    public class DemandDependencyItem extends AbstractDependencyItem 
    {
       /** The matcher */
       private Matcher matcher;
 
       /**
        * Create a new demand dependecy
        * 
        * @param name my name
        */
       public DemandDependencyItem(Object name)
       {
          super(name, null, whenRequired, null);
          if (getTransformer() != null)
             matcher = MatcherFactory.getInstance().createMatcher(getTransformer(), getDemand());
       }
       
       public boolean resolve(Controller controller)
       {
          Object name = (matcher != null) ? matcher : getDemand();
          ControllerContext context = controller.getInstalledContext(name);
          if (context != null)
          {
             setIDependOn(context.getName());
             addDependsOnMe(controller, context);
             setResolved(true);
          }
          else
          {
             setResolved(false);
          }
          return isResolved();
       }
 
       public void unresolved()
       {
          setIDependOn(null);
          setResolved(false);
       }
       
       public void toString(JBossStringBuilder buffer)
       {
          super.toString(buffer);
          buffer.append(" demand=").append(demand);
       }
       
       public void toShortString(JBossStringBuilder buffer)
       {
          buffer.append(getName()).append(" demands ").append(demand);
       }
 
       @Override
       public String toHumanReadableString()
       {
          StringBuilder builder = new StringBuilder();
          builder.append("Demands '").append(getDemand());
          return builder.toString();
       }
    }
 }
