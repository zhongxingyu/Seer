 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
 package org.jboss.managed.plugins;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.jboss.managed.api.DeploymentTemplateInfo;
 import org.jboss.managed.api.ManagedComponent;
 import org.jboss.managed.api.ManagedDeployment;
 import org.jboss.managed.api.ManagedObject;
 import org.jboss.managed.api.ManagedProperty;
 
 /**
  * A simple ManagedDeployment bean implementation
  * 
  * @author Scott.Stark@jboss.org
  * @version $Revision$
  */
 public class ManagedDeploymentImpl
    implements ManagedDeployment, Serializable
 {
    private static final long serialVersionUID = 1;
    private String name;
    private String simpleName;
    private Set<String> types;
    private DeploymentPhase phase;
    private ManagedDeployment parent;
    private Map<String, ManagedObject> unitMOs;
    private Map<String, ManagedProperty> properties;
    private Map<String, ManagedComponent> components = new HashMap<String, ManagedComponent>();
    private ArrayList<ManagedDeployment> children = new ArrayList<ManagedDeployment>();
    
    public ManagedDeploymentImpl(String name, String simpleName, DeploymentPhase phase,
          ManagedDeployment parent, Map<String, ManagedObject> unitMOs)
    {
       // TODO: simple vs full deployment name
       this.name = name;
       this.phase = phase;
       this.parent = parent;
       this.unitMOs = unitMOs;
       properties = new HashMap<String, ManagedProperty>();
       for(ManagedObject mo : unitMOs.values())
       {
          properties.putAll(mo.getProperties());
       }
    }
 
    public String getName()
    {
       return name;
    }
    public String getSimpleName()
    {
       return simpleName;
    }
 
    public boolean addType(String type)
    {
       return types.add(type);
    }
    public Set<String> getTypes()
    {
       return types;
    }
    public void setTypes(Set<String> types)
    {
       this.types = types;
    }
 
    public DeploymentPhase getDeploymentPhase()
    {
       return phase;
    }
    public ManagedDeployment getParent()
    {
       return parent;
    }
 
    public Set<String> getComponentTemplateNames()
    {
       // TODO Auto-generated method stub
       return null;
    }
    public void addComponent(String name, ManagedComponent comp)
    {
      components.put(name, comp);
    }
    public ManagedComponent getComponent(String name)
    {
       ManagedComponent mc = components.get(name);
       return mc;
    }
 
 
    public Map<String, ManagedComponent> getComponents()
    {
       return components;
    }
 
    public boolean removeComponent(String name)
    {
       ManagedComponent mc = components.remove(name);
       return mc != null;
    }
 
    public Set<String> getDeploymentTemplateNames()
    {
       // TODO Auto-generated method stub
       return null;
    }
    public List<ManagedDeployment> getChildren()
    {
       return children;
    }
 
    public ManagedDeployment addModule(String deplymentBaseName, DeploymentTemplateInfo info)
    {
       // TODO Auto-generated method stub
       return null;
    }
 
    public Map<String, ManagedProperty> getProperties()
    {
       return properties;
    }
 
    public ManagedProperty getProperty(String name)
    {
       return properties.get(name);
    }
 
    public Set<String> getPropertyNames()
    {
       return properties.keySet();
    }
 
    public Set<String> getManagedObjectNames()
    {
       return unitMOs.keySet();
    }
    public Map<String, ManagedObject> getManagedObjects()
    {
       return unitMOs;
    }
    public ManagedObject getManagedObject(String name)
    {
       return unitMOs.get(name);
    }
 
    public String toString()
    {
       StringBuilder tmp = new StringBuilder(super.toString());
       tmp.append('{');
       tmp.append("name=");
       tmp.append(getName());
       tmp.append(", types=");
       tmp.append(types);
       tmp.append(", phase=");
       tmp.append(phase);
       tmp.append(", parent=");
       if( parent != null )
       {
          tmp.append("ManagedDeployment@");
          tmp.append(System.identityHashCode(parent));
       }
       else
       {
          tmp.append("null");
       }
       tmp.append(", components=");
       tmp.append(components);
       tmp.append(", children=");
       tmp.append(children);
       tmp.append('}');
       return tmp.toString();
    }
 
 }
