 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2007, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
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
 package org.jboss.deployers.plugins.structure;
 
 import java.io.Externalizable;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.jboss.deployers.spi.structure.ContextInfo;
 import org.jboss.deployers.spi.structure.StructureMetaData;
 
 /**
  * StructureMetaDataImpl.
  * 
  * @author <a href="adrian@jboss.org">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class StructureMetaDataImpl implements StructureMetaData, Externalizable
 {
    /** The serialVersionUID */
    private static final long serialVersionUID = 2341637762171510800L;
    
    /** The contexts */
    private List<ContextInfo> contexts = new CopyOnWriteArrayList<ContextInfo>();
 
    public void addContext(ContextInfo context)
    {
       if (context == null)
          throw new IllegalArgumentException("Null context");
       String path = context.getPath();
       if (path == null)
          throw new IllegalArgumentException("Context has no path");
 
       for (ContextInfo other : contexts)
       {
          if (path.equals(other.getPath()))
            throw new IllegalStateException("Context already exists with path '" + path + "' contexts=" + getContexts());
       }
       contexts.add(context);
    }
 
    public ContextInfo getContext(String path)
    {
       if (path == null)
          throw new IllegalArgumentException("Null path");
       for (ContextInfo context : contexts)
       {
          if (path.equals(context.getPath()))
             return context;
       }
       return null;
    }
 
    public void removeContext(ContextInfo context)
    {
       if (context == null)
          throw new IllegalArgumentException("Null context");
       contexts.remove(context);
    }
 
    public void removeContext(String path)
    {
       if (path == null)
          throw new IllegalArgumentException("Null path");
       
       for (ContextInfo context : contexts)
       {
          if (path.equals(context.getPath()))
             contexts.remove(context);
       }
    }
 
    public List<ContextInfo> getContexts()
    {
       return contexts;
    }
    
    @Override
    public String toString()
    {
       StringBuilder builder = new StringBuilder();
       builder.append(getClass().getSimpleName());
       builder.append("{");
       toString(builder);
       builder.append("}");
       return builder.toString();
    }
    
    /**
     * For subclasses to override toString()
     * 
     * @param builder the builder
     */
    protected void toString(StringBuilder builder)
    {
       builder.append("contexts=").append(contexts);
    }
    
    @Override
    public boolean equals(Object obj)
    {
       if (obj == this)
          return true;
       if (obj == null || obj instanceof StructureMetaData == false)
          return false;
       
       StructureMetaData other = (StructureMetaData) obj;
 
       List<ContextInfo> thisContexts = getContexts();
       List<ContextInfo> otherContexts = other.getContexts();
       if (thisContexts == null)
          return otherContexts == null;
       return thisContexts.equals(otherContexts);
    }
    
    @Override
    public int hashCode()
    {
       List<ContextInfo> contexts = getContexts();
       if (contexts == null)
          return 0;
       return contexts.hashCode();
    }
 
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
       contexts = (List) in.readObject();
    }
 
    /**
     * @serialData context from {@link #getContexts()}
     * @param out the output
     * @throws IOException for any error
     */
    public void writeExternal(ObjectOutput out) throws IOException
    {
       out.writeObject(getContexts());
    }
 }
