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
 package org.jboss.dependency.plugins;
 
 import org.jboss.dependency.spi.Controller;
 import org.jboss.dependency.spi.ControllerContext;
 import org.jboss.dependency.spi.ControllerMode;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.dependency.spi.DependencyInfo;
 import org.jboss.dependency.spi.DependencyItem;
 import org.jboss.logging.Logger;
 import org.jboss.util.JBossObject;
 import org.jboss.util.JBossStringBuilder;
 
 /**
  * A DependencyItem.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public class AbstractDependencyItem extends JBossObject implements DependencyItem
 {
    /** The log */
    private static final Logger log = Logger.getLogger(AbstractDependencyItem.class);
    
    /** What I depend on */
    private Object iDependOn;
 
    /** My name */
    private Object name;
 
    /** When the dependency is required */
    private ControllerState whenRequired = ControllerState.DESCRIBED;
 
    /** The state of the dependency */
    private ControllerState dependentState;
    
    /** Whether we are resolved */
    private boolean resolved;
 
    /**
     * Create a new dependency item
     */
    public AbstractDependencyItem()
    {
    }
 
    /**
     * Create a new dependency item
     *
     * @param name my name
     * @param iDependOn what I depend on
     * @param whenRequired when the dependency is required 
     * @param dependentState the required state of the dependent 
     */
    public AbstractDependencyItem(Object name, Object iDependOn, ControllerState whenRequired, ControllerState dependentState)
    {
       this.name = name;
       setIDependOn(iDependOn);
       this.whenRequired = whenRequired;
       this.dependentState = dependentState;
    }
 
    public Object getName()
    {
       return name;
    }
 
    public Object getIDependOn()
    {
       return iDependOn;
    }
 
    public ControllerState getWhenRequired()
    {
       return whenRequired;
    }
 
    public ControllerState getDependentState()
    {
       return dependentState;
    }
 
    public boolean isResolved()
    {
       return resolved;
    }
 
    public boolean resolve(Controller controller)
    {
       boolean previous = resolved;
       ControllerContext context;
 
       if (dependentState == null)
          context = controller.getInstalledContext(iDependOn);
       else
       {
          context = controller.getContext(iDependOn, dependentState);
          if (context == null)
          {
             if (ControllerState.INSTALLED.equals(dependentState))
                context = controller.getInstalledContext(iDependOn);
          }
       }
 
       if (context == null)
       {
          resolved = false;
          ControllerContext unresolvedContext = controller.getContext(iDependOn, null);
          if (unresolvedContext != null && ControllerMode.ON_DEMAND.equals(unresolvedContext.getMode()))
          {
             try
             {
                controller.enableOnDemand(unresolvedContext);
             }
             catch (Throwable ignored)
             {
                if (log.isTraceEnabled())
                   log.trace("Unexpected error", ignored);
             }
          }
       }
       else
       {
          addDependsOnMe(controller, context);
          resolved = true;
       }
 
       if (previous != resolved)
       {
          flushJBossObjectCache();
          if (log.isTraceEnabled())
          {
             if (resolved)
                log.trace("Resolved " + this);
             else
                log.trace("Unresolved " + this);
          }
       }
       return resolved;
    }
 
   public boolean unresolved(Controller controller)
    {
       if (resolved)
       {
          resolved = false;
          flushJBossObjectCache();
          log.trace("Forced unresolved " + this);
       }
       return true;
    }
    
    public void toString(JBossStringBuilder buffer)
    {
       buffer.append("name=").append(name);
       buffer.append(" dependsOn=").append(iDependOn);
       if (whenRequired != null)
          buffer.append(" whenRequired=").append(whenRequired.getStateString());
       if (dependentState != null)
          buffer.append(" dependentState=").append(dependentState.getStateString());
       buffer.append(" resolved=").append(resolved);
    }
    
    public void toShortString(JBossStringBuilder buffer)
    {
       buffer.append(name).append(" dependsOn ").append(iDependOn);
    }
 
    /**
     * Register a dependency with another context
     * 
     * @param controller the controller
     * @param context the other context
     */
    protected void addDependsOnMe(Controller controller, ControllerContext context)
    {
       DependencyInfo info = context.getDependencyInfo();
       if (info != null)
          info.addDependsOnMe(this);
    }
 
    /**
     * Set what I depend upon 
     * 
     * @param iDependOn what I depend upon
     */
    protected void setIDependOn(Object iDependOn)
    {
       this.iDependOn = iDependOn;
       
       // HACK: Try to fixup JMX like ObjectNames to their canonical name
       Object fixup = JMXObjectNameFix.needsAnAlias(iDependOn);
       if (fixup != null)
          this.iDependOn = fixup;
 
       flushJBossObjectCache();
    }
 
    /**
     * Set the resolved state 
     * 
     * @param resolved the new resolved state
     */
    protected void setResolved(boolean resolved)
    {
       this.resolved = resolved;
       flushJBossObjectCache();
    }
 
    public String toHumanReadableString()
    {
       StringBuilder builder = new StringBuilder();
       builder.append("Depends on '").append(getIDependOn());
       return builder.toString();
    }
 }
