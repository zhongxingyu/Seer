 /*******************************************************************************
  * Copyright 2010 Nathan Kopp
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.d2;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.d2.annotations.D2Entity;
 import org.d2.annotations.D2Indexed;
 import org.d2.context.D2Context;
 import org.d2.index.DocBuilder;
 import org.d2.index.DocBuilderWrapper;
 import org.d2.pluggable.Indexer;
 import org.d2.pluggable.StorageSystem;
 import org.d2.serialize.D2XmlEntityConverter;
 import org.nkts.util.Util;
 import org.objenesis.Objenesis;
 import org.objenesis.ObjenesisStd;
 import org.objenesis.instantiator.ObjectInstantiator;
 
 
 public class Bucket
 {
     private Objenesis objenesis = new ObjenesisStd();
     
     private String name;
     private Class<? extends Object> clazz;
     private D2XmlEntityConverter converter;
 //    protected Map<String, Object> cache = new SoftHashMap<String, Object>();
     
     private Map<Class, ObjectInstantiator> instantiators = new HashMap<Class, ObjectInstantiator>();
 
     private StorageSystem storage;
     private Indexer indexer;
     private DocBuilder docBuilder;
     private DocBuilderWrapper docBuilderWrapper;
     
     private List<UniqueConstraint> constraints = new ArrayList<UniqueConstraint>();
 
     public Bucket(Class<? extends Object> clazz)
     {
         this(clazz, null, null);
     }
     public Bucket(Class<? extends Object> clazz, UniqueConstraint... constraints)
     {
         this(clazz, null, null);
         for(UniqueConstraint c : constraints)
         {
             this.constraints.add(c);
         }
     }
     public Bucket(Class<? extends Object> clazz, StorageSystem system, Indexer  indexer)
     {
         super();
         this.storage = system;
         this.indexer = indexer;
         D2Entity annotation = clazz.getAnnotation(D2Entity.class);
         this.clazz = clazz;
         this.name = annotation.bucketName();
         try
         {
             if(annotation.docConverter()!=D2Entity.class)
             {
                 this.docBuilderWrapper = (DocBuilderWrapper)annotation.docConverter().newInstance();
             }
         }
         catch (Exception e)
         {
             Util.wrap(e);
         }
     }
 
     public Bucket(String name, Class<? extends Object> clazz, DocBuilder docBuilder)
     {
         super();
         this.name = name;
         this.clazz = clazz;
         this.docBuilder = docBuilder;
     }
     
     public void addConstratint(UniqueConstraint constraint)
     {
         constraints.add(constraint);
     }
     
     public synchronized ObjectInstantiator getInstantiator(Class clazz)
     {
         ObjectInstantiator instantiator = instantiators.get(clazz);
         if(instantiator==null)
         {
             instantiator = objenesis.getInstantiatorOf(clazz);
             instantiators.put(clazz, instantiator);
         }
         return instantiator;
     }
     
     public void setD2(D2 d2)
     {
         try
         {
             if(storage==null) storage = d2.getDefaultStorageFactory().createStorage(this);
             if(indexer==null) indexer = d2.getDefaultIndexerFactory().createIndexer(this);
             
             if(docBuilderWrapper!=null)
             {
                 docBuilderWrapper.setBuilderImpl(indexer.getDocBuilder());
                 docBuilder = docBuilderWrapper;
             }
             else
             {
                 docBuilder = indexer.getDocBuilder();
             }
             
             docBuilder = indexer.getDocBuilder();
             
 //            if(converter==null)
 //                converter = new D2XmlEntityConverter(d2, clazz);
         }
         catch(Exception e)
         {
            throw Util.wrap(e);
         }
     }
     
     
     private boolean anyFieldHasIndexedAnnotation(Class<? extends Object> clazz)
     {
         if(Util.getFirstAnnotatedField(clazz, D2Indexed.class)!=null) return true;
         if(Util.getFirstAnnotatedMethod(clazz, D2Indexed.class)!=null) return true;
         return false;
     }
     
 
     public String getName()
     {
         return name;
     }
     public void setName(String name)
     {
         this.name = name;
     }
     public Class<? extends Object> getClazz()
     {
         return clazz;
     }
     public void setClazz(Class<?> clazz)
     {
         this.clazz = clazz;
     }
     public D2XmlEntityConverter getConverter()
     {
         return converter;
     }
     public void setConverter(D2XmlEntityConverter converter)
     {
         this.converter = converter;
     }
     public Indexer getIndexer()
     {
         return indexer;
     }
     public void setIndexer(Indexer indexer)
     {
         this.indexer = indexer;
     }
 
 //    public Map<String, Object> getCache()
 //    {
 //        return cache;
 //    }
 
 
     public StorageSystem getStorage()
     {
         return storage;
     }
     public List<UniqueConstraint> getConstraints()
     {
         return constraints;
     }
     public Object applyConstraints(Object obj, D2Context context)
     {
         for(UniqueConstraint c : constraints)
         {
             obj = c.applyConstraint(obj, context);
         }
         return obj;
     }
 }
