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
 package org.jboss.aop;
 
 import java.io.Serializable;
 import java.lang.ref.WeakReference;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.WeakHashMap;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.jboss.aop.advice.AspectDefinition;
 import org.jboss.aop.joinpoint.Joinpoint;
 import org.jboss.aop.metadata.SimpleMetaData;
 
 /**
  * Initialisation and getting of instance and joinpoint aspects needed by the various kinds of
  * InstanceAdvisor implementations
  *  
  * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
  * @version $Revision$
  */
 public class InstanceAdvisorDelegate implements Serializable
 {
    private static final long serialVersionUID = -5421366346785427537L;
    
    protected transient WeakReference classAdvisor;
    InstanceAdvisor instanceAdvisor;
    protected transient WeakHashMap aspects;
    protected transient WeakHashMap joinpointAspects;
    protected SimpleMetaData metadata;
 
 
    public InstanceAdvisorDelegate(Advisor classAdvisor, InstanceAdvisor instanceAdvisor)
    {
       this.instanceAdvisor = instanceAdvisor;
       this.classAdvisor = new WeakReference(classAdvisor);
    }
 
    public Advisor getAdvisor()
    {
       if (classAdvisor != null)
       {
          return (Advisor)classAdvisor.get();
       }
       return null;
    }
    
    public void initialize()
    {
       initializeAspects();
       initializeJoinpointAspects();
    }
    
    private Advisor getClassAdvisor()
    {
       return getAdvisor();
    }
    
    private synchronized void initializeAspects()
    {
       if (getClassAdvisor() == null) return;
       if (aspects != null) return; // doublecheck I know, but I don't want to do synchronization if not needed
       //ClassAdvisor cadvisor = (ClassAdvisor) classAdvisor;
       if (instanceAdvisor instanceof Advisor)
       {
          Advisor ia = (Advisor)instanceAdvisor;
          Set instanceDefs = ia.getPerInstanceAspectDefinitions();
          if (instanceDefs.size() > 0)
          {
             aspects = new WeakHashMap();
             for (Iterator it = instanceDefs.iterator() ; it.hasNext() ; )
             {
                AspectDefinition def = (AspectDefinition) it.next();
                ia.addPerInstanceAspect(def);
                Object aspect = def.getFactory().createPerInstance(getClassAdvisor(), instanceAdvisor);
                aspects.put(def, aspect);
             }
          }
       }
       Set defs = getClassAdvisor().getPerInstanceAspectDefinitions();
       if (defs.size() > 0)
       {
         if (aspects == null)
         {
            aspects = new WeakHashMap();
         }
          Iterator it = defs.iterator();
          while (it.hasNext())
          {
             AspectDefinition def = (AspectDefinition) it.next();
             Object aspect = def.getFactory().createPerInstance(getClassAdvisor(), instanceAdvisor);
             aspects.put(def, aspect);
          }
       }
    }
 
    private synchronized void initializeJoinpointAspects()
    {
       if (getClassAdvisor() == null) return;
       if (joinpointAspects != null) return; // doublecheck I know, but I don't want to do synchronization if not needed
       if (instanceAdvisor instanceof Advisor)
       {
          Advisor ia = (Advisor)instanceAdvisor;
          Map instanceJpAspects = ia.getPerInstanceJoinpointAspectDefinitions();
          
          if (instanceJpAspects.size() > 0)
          {
             joinpointAspects = new WeakHashMap();
             for (Iterator it = instanceJpAspects.keySet().iterator() ; it.hasNext() ; )
             {
                AspectDefinition def = (AspectDefinition) it.next();
                initJoinpointAspect(def, instanceJpAspects);
                Set joinpoints = (Set)instanceJpAspects.get(def);
                ia.addPerInstanceJoinpointAspect(joinpoints, def);
             }
          }
       }
       
       Map jpAspects = getClassAdvisor().getPerInstanceJoinpointAspectDefinitions();
       if (jpAspects.size() > 0)
       {
          joinpointAspects = new WeakHashMap();
          Iterator it = jpAspects.keySet().iterator();
          while (it.hasNext())
          {
             AspectDefinition def = (AspectDefinition) it.next();
             initJoinpointAspect(def, jpAspects);
          }
       }
    }
    
    private void initJoinpointAspect(AspectDefinition def, Map jpAspects)
    {
       ConcurrentHashMap joins = new ConcurrentHashMap();
       joinpointAspects.put(def, joins);
       Set joinpoints = (Set) jpAspects.get(def);
       Iterator jps = joinpoints.iterator();
       while (jps.hasNext())
       {
          Object joinpoint = jps.next();
          joins.put(joinpoint, def.getFactory().createPerJoinpoint(getClassAdvisor(), instanceAdvisor, (Joinpoint) joinpoint));
       }
    }
    
    public Object getPerInstanceAspect(String def)
    {
       Iterator it = aspects.keySet().iterator();
       while (it.hasNext())
       {
          AspectDefinition d = (AspectDefinition) it.next();
          if (d.getName().equals(def)) return aspects.get(d);
       }
       return null;
    }
    public Object getPerInstanceAspect(AspectDefinition def)
    {
       // aspects is a weak hash map of AspectDefinitions so that perinstance advices can be undeployed/redeployed
       if (aspects == null)
       {
          initializeAspects();
          return aspects.get(def);
       }
       Object aspect = aspects.get(def);
       if (aspect == null)
       {
          synchronized (this) // doublecheck, but I don't want to synchronize everywhere and dynamic aspects are rare
          {
             aspect = aspects.get(def);
             if (aspect != null) return aspect;
             if (classAdvisor != null && getClassAdvisor() instanceof ClassAdvisor)
             {
                ClassAdvisor cadvisor = (ClassAdvisor) getClassAdvisor();
                cadvisor.getPerInstanceAspectDefinitions().add(def);
                aspect = def.getFactory().createPerInstance(null, null);
                WeakHashMap copy = new WeakHashMap(aspects);
                copy.put(def, aspect);
                aspects = copy;
             }
          }
       }
       return aspect;
    }
 
    public Object getPerInstanceJoinpointAspect(Joinpoint joinpoint, AspectDefinition def)
    {
       // aspects is a weak hash map of AspectDefinitions so that perinstance advices can be undeployed/redeployed
       if (joinpointAspects == null)
       {
          initializeJoinpointAspects();
          return getJoinpointAspect(def, joinpoint);
       }
       Object aspect = getJoinpointAspect(def, joinpoint);
       if (aspect == null)
       {
          synchronized (this) // doublecheck, but I don't want to synchronize everywhere and dynamic aspects are rare
          {
             aspect = getJoinpointAspect(def, joinpoint);
             if (aspect != null) return aspect;
             if (classAdvisor != null && getClassAdvisor() instanceof ClassAdvisor)
             {
                ClassAdvisor cadvisor = (ClassAdvisor) getClassAdvisor();
                cadvisor.addPerInstanceJoinpointAspect(joinpoint, def);
                aspect = def.getFactory().createPerJoinpoint(getClassAdvisor(), instanceAdvisor, joinpoint);
                WeakHashMap copy = new WeakHashMap(joinpointAspects);
                Map map = (Map) copy.get(def);
                if (map == null)
                {
                   map = new ConcurrentHashMap();
                }
                map.put(joinpoint, aspect);
                joinpointAspects = copy;
             }
          }
       }
       return aspect;
    }
 
    private Object getJoinpointAspect(AspectDefinition def, Joinpoint joinpoint)
    {
       if (joinpointAspects == null) return null;
       Map map = (Map) joinpointAspects.get(def);
       Object aspect = map.get(joinpoint);
       return aspect;
    }
    
    public SimpleMetaData getMetaData()
    {
       if (metadata == null)
       {
          synchronized (this) // doublecheck :(
          {
             if (metadata == null)
             {
                metadata = new SimpleMetaData();
             }
          }
       }
       return metadata;
    }
 
 }
