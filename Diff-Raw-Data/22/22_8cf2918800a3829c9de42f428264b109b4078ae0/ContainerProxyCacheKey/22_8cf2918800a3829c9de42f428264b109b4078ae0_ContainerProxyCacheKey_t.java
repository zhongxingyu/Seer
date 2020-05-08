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
 package org.jboss.aop.proxy.container;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.lang.ref.WeakReference;
 import java.util.Arrays;
 import java.util.Comparator;
 
 import org.jboss.metadata.spi.MetaData;
 import org.jboss.util.id.GUID;
 
 /**
  * 
  * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
  * @version $Revision$
  */
 public class ContainerProxyCacheKey implements Serializable
 {
    private static final long serialVersionUID = 8758283842273747310L;
    private static final WeakReference<Class>[] EMTPY_WR_ARRAY = new WeakReference[0];
    private static final AOPProxyFactoryMixin[] EMPTY_MIXIN_ARRAY = new AOPProxyFactoryMixin[0];
    
    private String managerFqn;
    private WeakReference<Class> clazzRef;
    private WeakReference<Class>[] addedInterfaces = EMTPY_WR_ARRAY;
    
    private MetaData metaData;
    
    private AOPProxyFactoryMixin[] addedMixins = EMPTY_MIXIN_ARRAY;
    private int hashcode = 0;
    private GUID guid = MarshalledContainerProxy.GUID;
    
    public ContainerProxyCacheKey(String managerFqn, Class clazz)
    {
       this.clazzRef = new WeakReference<Class>(clazz);
       this.managerFqn = managerFqn;
    }
    
    public ContainerProxyCacheKey(Class clazz)
    {
       this("/", clazz);
    }
    
    public ContainerProxyCacheKey(String managerFqn, Class clazz, Class[] addedInterfaces, MetaData metaData)
    {
       this(managerFqn, clazz); 
       this.addedInterfaces = ContainerCacheUtil.getSortedWeakReferenceForInterfaces(addedInterfaces);
       this.metaData = metaData; 
    }
 
    public ContainerProxyCacheKey(String managerFqn, Class clazz, Class[] addedInterfaces, AOPProxyFactoryMixin[] addedMixins, MetaData metaData)
    {
       this(managerFqn, clazz, addedInterfaces, metaData);
       
       if (addedMixins != null)
       {
          this.addedMixins = addedMixins;
          Arrays.sort(this.addedMixins, MixinAlphabetical.singleton);
       }
    }
 
    public Class getClazz()
    {
       return clazzRef.get();
    }
    
    public String getManagerFQN()
    {
       return managerFqn;
    }
  
    protected GUID getGuid()
    {
       return guid;
    }
 
    public boolean equals(Object obj)
    {
       if (this == obj)
       {
          return true;
       }
  
       if (obj.getClass() != ContainerProxyCacheKey.class)
       {
          return false;
       }
       
       ContainerProxyCacheKey other = (ContainerProxyCacheKey)obj;
 
       if (!managerFqn.equals(other.managerFqn))
       {
          return false;
       }
       if (!compareMetadataContext(other))
       {
          return false;
       }
       if (!compareClass(other))
       {
          return false;
       }
       if (!compareAddedInterfaces(other))
       {
          return false;
       }
       if (!compareAddedMixins(other))
       {
          return false;
       }
       if(!guid.equals(other.guid))
       {
          return false;
       }
       
       return true;
    }
 
    public int hashCode()
    {
       if (hashcode == 0)
       {
          
          Class clazz = (Class)clazzRef.get();
          StringBuffer sb = new StringBuffer();
          sb.append(managerFqn);
          if (clazz != null)
          {
             sb.append(clazz.getName());
          }
          
          if (addedInterfaces != null)
          {
             for (int i = 0 ; i < addedInterfaces.length ; i++)
             {
                sb.append(";");
                sb.append(((Class)addedInterfaces[i].get()).getName());
             }
          }
          
          hashcode = sb.toString().hashCode(); 
          
          if (metaData != null)
          {
             hashcode += metaData.hashCode();
          }
       }
       
       return hashcode;
    }
    
    public String toString()
    {
       StringBuffer buf = new StringBuffer("ContainerProxyCache");
       buf.append(((Class)clazzRef.get()).getName());
       buf.append(";interfaces=");
       if (addedInterfaces == null)
       {
          buf.append("null");
       }
       else
       {
          buf.append(Arrays.asList(addedInterfaces));
       }
       buf.append(";mixins=");
       if (addedMixins == null)
       {
          buf.append("null");
       }
       else
       {
          buf.append(Arrays.asList(addedMixins));
       }
       return buf.toString();
    }
    
    private boolean compareMetadataContext(ContainerProxyCacheKey other)
    {
       if (this.metaData == null && other.metaData == null)
       {
       }
       else if ((this.metaData != null && other.metaData != null))
       {
          if (!this.metaData.equals(other.metaData))
          {
             return false;
          }
       }
       else
       {
          return false;
       }
       return true;
    }
    
    private boolean compareClass(ContainerProxyCacheKey other)
    {
       return ContainerCacheUtil.compareClassRefs(this.clazzRef, other.clazzRef);
    }
    
    private boolean compareAddedInterfaces(ContainerProxyCacheKey other)
    {
       return ContainerCacheUtil.compareInterfaceRefs(this.addedInterfaces, other.addedInterfaces);
    }
 
    private boolean compareAddedMixins(ContainerProxyCacheKey other)
    {
       if ((this.addedMixins == null && other.addedMixins != null) ||
             (this.addedMixins == null && other.addedMixins != null))
       {
          return false;
       }
       
       if (this.addedMixins != null && other.addedMixins != null)
       {
          if (this.addedMixins.length != other.addedMixins.length)
          {
             return false;
          }
          
          for (int i = 0 ; i < this.addedMixins.length ; i++)
          {
             if (!this.addedMixins[i].equals(other.addedMixins[i]))
             {
                return false;
             }
          }
       }
       
       return true;
    }
    
     private void writeObject(java.io.ObjectOutputStream out) throws IOException
     {
        out.writeUTF(managerFqn);
        out.writeObject(guid);
        out.writeObject(clazzRef.get());
       Class[] ifs = null;
       if (addedInterfaces != null)
        {
          ifs = new Class[addedInterfaces.length];
          for (int i = 0 ; i < addedInterfaces.length ; i++)
          {
             ifs[i] = addedInterfaces[i].get();
          }
        }
        out.writeObject(ifs);
        out.writeObject(metaData);
        out.writeObject(addedMixins);
        out.writeInt(hashCode());
     }
     
     private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
     {
        managerFqn = in.readUTF();
        guid = (GUID)in.readObject();
        clazzRef = new WeakReference<Class>((Class)in.readObject());
        Class[] ifs = (Class[])in.readObject();
       if (ifs != null)
        {
          addedInterfaces = new WeakReference[ifs.length];
          for (int i = 0 ; i < ifs.length ; i++)
          {
             addedInterfaces[i] = new WeakReference<Class>(ifs[i]);
          }
        }
        metaData = (MetaData)in.readObject();
        addedMixins = (AOPProxyFactoryMixin[])in.readObject();
        hashcode = in.readInt();
     }
    
    static class MixinAlphabetical implements Comparator
    {
       static MixinAlphabetical singleton = new MixinAlphabetical();
       
       public int compare(Object o1, Object o2)
       {
          String name1 = ((AOPProxyFactoryMixin)o1).getMixin().getName();
          String name2 = ((AOPProxyFactoryMixin)o2).getMixin().getName();
          return (name1).compareTo(name2);
       }
    }
 }
