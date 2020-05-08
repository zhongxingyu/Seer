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
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.jboss.dependency.spi.Controller;
 import org.jboss.dependency.spi.ControllerContext;
 import org.jboss.dependency.spi.ControllerContextActions;
 import org.jboss.dependency.spi.ControllerMode;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.dependency.spi.DependencyInfo;
 import org.jboss.dependency.spi.ScopeInfo;
 import org.jboss.util.JBossObject;
 import org.jboss.util.JBossStringBuilder;
 
 /**
  * A ControllerContext.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public class AbstractControllerContext extends JBossObject implements ControllerContext
 {
    /** The name */
    private Object name;
 
    /** The aliases */
    private Set<Object> aliases;
    
    /** The target */
    private Object target;
 
    /** The controller */
    private Controller controller;
 
    /** The state */
    private ControllerState state = ControllerState.ERROR;
 
    /** The required state */
    private ControllerState requiredState = ControllerState.NOT_INSTALLED;
 
    /** The mdoe */
    private ControllerMode mode = ControllerMode.AUTOMATIC;
    
    /** The actions */
    private ControllerContextActions actions;
    
    /** The dependencies */
    private DependencyInfo dependencies;
 
    /** The scope info */
    private ScopeInfo scopeInfo;
    
    /** Any error */
    private Throwable error;
 
    /**
     * Create a new AbstractControllerContext.
     * 
     * @param name the name
     * @param actions the actions
     */
    public AbstractControllerContext(Object name, ControllerContextActions actions)
    {
       this(name, null, actions, null, null);
    }
 
    /**
     * Create a new AbstractControllerContext.
     * 
     * @param name the name
     * @param actions the actions
     * @param dependencies the dependencies
     */
    public AbstractControllerContext(Object name, ControllerContextActions actions, DependencyInfo dependencies)
    {
       this(name, null, actions, dependencies, null);
    }
 
    /**
     * Create a new AbstractControllerContext.
     * 
     * @param name the name
     * @param actions the actions
     * @param dependencies the dependencies
     * @param target the target
     */
    public AbstractControllerContext(Object name, ControllerContextActions actions, DependencyInfo dependencies, Object target)
    {
       this(name, null, actions, dependencies, target);
    }
 
    /**
     * Create a new AbstractControllerContext.
     * 
     * @param name the name
     * @param aliases the aliases
     * @param actions the actions
     * @param dependencies the dependencies
     * @param target the target
     */
    public AbstractControllerContext(Object name, Set<Object> aliases, ControllerContextActions actions, DependencyInfo dependencies, Object target)
    {
       if (name == null)
          throw new IllegalArgumentException("Null name");
       if (actions == null)
          throw new IllegalArgumentException("Null actions");
 
       this.name = name;
       this.actions = actions;
       if (dependencies == null)
          this.dependencies = new AbstractDependencyInfo();
       else
         this.dependencies = dependencies;
       this.target = target;
       setAliases(aliases);
       initScopeInfo();
    }
 
    /**
     * Create a new AbstractControllerContext.
     * 
     * @param name the name
     * @param target the target
     */
    public AbstractControllerContext(Object name, Object target)
    {
       if (name == null)
          throw new IllegalArgumentException("Null name");
 
       this.name = name;
       this.target = target;
       initScopeInfo();
    }
    
    public Object getName()
    {
       return name;
    }
 
    /**
     * Set the name
     * 
     * @param name the name
     */
    public void setName(Object name)
    {
       this.name = name;
    }
    
    public Set<Object> getAliases()
    {
       return aliases;
    }
    
    /**
     * Set the aliases<p>
     * 
     * Aliases in this list only take effect if they are set before installation on the controller
     * 
     * @param aliases the aliases
     */
    public void setAliases(Set<Object> aliases)
    {
       // WARNING: This code is hack for backwards compatiblity
       
       // Here we fixup the aliases to map JMX like ObjectNames to their canonical form
       // I've made it a protected method needsAnAlias so others can subclass and
       // change the rules (including not doing this at all)
       // NOTE: This method should be invoked from all constructors
       if (aliases == null)
       {
          // There are no explicit aliases so just see whether the name is an ObjectName.
          Object alias = needsAnAlias(name);
          // No alias required
          if (alias == null)
             this.aliases = null;
          else
             // Add a single alias with canonical name
             this.aliases = Collections.singleton(alias);
       }
       else
       {
          // Always clone the aliases passed it
          this.aliases = new HashSet<Object>();
          // Check the main name
          Object alias = needsAnAlias(name);
          if (alias != null)
             this.aliases.add(alias);
          // Check the aliases
          for (Object passedAlias : aliases)
          {
             this.aliases.add(passedAlias);
             alias = needsAnAlias(passedAlias);
             if (alias != null)
                this.aliases.add(alias);
          }
       }
    }
 
    public ControllerState getState()
    {
       return state;
    }
    
    public ControllerState getRequiredState()
    {
       return requiredState;
    }
    
    public void setRequiredState(ControllerState state)
    {
       this.requiredState = state;
    }
    
    public ControllerMode getMode()
    {
       return mode;
    }
    
    public void setMode(ControllerMode mode)
    {
       this.mode = mode;
       flushJBossObjectCache();
    }
 
    /**
     * Get the controller
     * 
     * @return the controller
     */
    public Controller getController()
    {
       return controller;
    }
    
    public void setController(Controller controller)
    {
       this.controller = controller;
       flushJBossObjectCache();
    }
 
    public DependencyInfo getDependencyInfo()
    {
       return dependencies;
    }
 
    public ScopeInfo getScopeInfo()
    {
       return scopeInfo;
    }
 
    /**
     * Set the scopeInfo.
     * 
     * @param scopeInfo the scopeInfo.
     */
    public void setScopeInfo(ScopeInfo scopeInfo)
    {
       if (scopeInfo == null)
          throw new IllegalArgumentException("Null scope info");
       this.scopeInfo = scopeInfo;
    }
 
    public Object getTarget()
    {
       return target;
    }
 
    /**
     * Set the target
     *
     * @param target the target
     */
    public void setTarget(Object target)
    {
       this.target = target;
       flushJBossObjectCache();
    }
    
    public Throwable getError()
    {
       return error;
    }
 
    public void setError(Throwable error)
    {
       this.error = error;
       state = ControllerState.ERROR;
       flushJBossObjectCache();
    }
 
    public void setState(ControllerState state)
    {
       this.state = state;
       flushJBossObjectCache();
    }
 
    public void install(ControllerState fromState, ControllerState toState) throws Throwable
    {
       this.error = null;
       flushJBossObjectCache();
       actions.install(this, fromState, toState);
    }
 
    public void uninstall(ControllerState fromState, ControllerState toState)
    {
      this.error = null;
       flushJBossObjectCache();
       actions.uninstall(this, fromState, toState);
    }
 
    public void toString(JBossStringBuilder buffer)
    {
       buffer.append("name=").append(name);
       if (aliases != null)
          buffer.append(" aliases=").append(aliases);
       buffer.append(" target=").append(target);
       if (error != null || state.equals(ControllerState.ERROR) == false)
          buffer.append(" state=").append(state.getStateString());
       if (ControllerMode.AUTOMATIC.equals(mode) == false)
       {
          buffer.append(" mode=").append(mode.getModeString());
          buffer.append(" requiredState=").append(requiredState.getStateString());
       }
       if (dependencies != null)
          buffer.append(" depends=").append(dependencies);
       if (error != null)
       {
          StringWriter stringWriter = new StringWriter();
          PrintWriter writer = new PrintWriter(stringWriter);
          error.printStackTrace(writer);
          writer.flush();
          buffer.append(" error=").append(stringWriter.getBuffer());
       }
    }
 
    public void toShortString(JBossStringBuilder buffer)
    {
       buffer.append("name=").append(name);
       if (aliases != null)
          buffer.append(" aliases=").append(aliases);
       if (error != null || state.equals(ControllerState.ERROR) == false)
          buffer.append(" state=").append(state.getStateString());
       if (ControllerMode.AUTOMATIC.equals(mode) == false)
       {
          buffer.append(" mode=").append(mode.getModeString());
          buffer.append(" requiredState=").append(requiredState.getStateString());
       }
       if (error != null)
          buffer.append(" error=").append(error.getClass().getName()).append(": ").append(error.getMessage());
    }
    
    /**
     * Initialise the scope info
     */
    protected void initScopeInfo()
    {
       String className = null;
       Object target = getTarget();
       if (target != null)
          className = target.getClass().getName();
       setScopeInfo(new AbstractScopeInfo(getName(), className));
    }
    
    /**
     * Whether the given name needs an alias<p>
     * 
     * By default we just add aliases for JMX like ObjectNames to have a canonical name alias
     * 
     * @param original the original name
     * @return the alias if required or null if no alias required
     */
    protected Object needsAnAlias(Object original)
    {
       return JMXObjectNameFix.needsAnAlias(original);
    }
 }
