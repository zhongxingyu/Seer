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
 package org.jboss.aop.advice;
 
 import java.util.Iterator;
 import java.util.Map;
 
 import org.jboss.aop.Advisor;
 
 import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;
 
 /**
  * Contains definition of aspect or interceptor
  * Scope defaults to PER_VM if it is null.
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @version $Revision$
  */
 public class AspectDefinition
 {
    protected String name;
    protected Scope scope = Scope.PER_VM;
    protected AspectFactory factory;
    protected boolean deployed = true;
 
   /**
    * @deprecated should not call this directly
    */
   public Map advisors = new ConcurrentReaderHashMap();
 
    /**
     * @param name
     * @param scope   defaults to PER_VM if null
     * @param factory
     */
    public AspectDefinition(String name, Scope scope, AspectFactory factory)
    {
 
       this.name = name;
       this.scope = scope;
       this.factory = factory;
       if (this.scope == null) this.scope = Scope.PER_VM;
       if (this.name == null) this.name = this.factory.getName();
    }
 
    public AspectDefinition() {}
 
    public synchronized void undeploy()
    {
       if (advisors.size() > 0)
       {
          for (Iterator it = advisors.keySet().iterator() ; it.hasNext() ; )
          {
             Advisor advisor = (Advisor)it.next();
             if (advisors.remove(advisor) !=  null)
             {
                if (scope == Scope.PER_INSTANCE)
                {
                   advisor.removePerInstanceAspect(this);
                }
                else if (scope == Scope.PER_JOINPOINT)
                {
                   advisor.removePerInstanceJoinpointAspect(this);
                }
                else if (scope == Scope.PER_CLASS)
                {
                   advisor.removePerClassAspect(this);
                }
             }
          }
          advisors.clear();
       }
       this.deployed = false;
    }
    public boolean isDeployed()
    {
       return deployed;
    }
 
    public void setName(String name)
    {
       this.name = name;
    }
 
    public void setScope(Scope scope)
    {
       this.scope = scope;
    }
 
    public void setFactory(AspectFactory factory)
    {
       this.factory = factory;
    }
 
    public AspectFactory getFactory()
    {
       return factory;
    }
 
    public String getName()
    {
       return name;
    }
 
    public Scope getScope()
    {
       return scope;
    }
 
    public int hashCode()
    {
       return name.hashCode();
    }
 
    public boolean equals(Object obj)
    {
       if (obj == this) return true;
       if (!(obj instanceof AspectDefinition)) return false;
       return name.equals(((AspectDefinition) obj).name);
    }
 
    public synchronized void registerAdvisor(Advisor advisor)
    {
       advisors.put(advisor, Boolean.TRUE);
    }
 
    public synchronized void unregisterAdvisor(Advisor advisor)
    {
       advisors.remove(advisor);
    }
 }
