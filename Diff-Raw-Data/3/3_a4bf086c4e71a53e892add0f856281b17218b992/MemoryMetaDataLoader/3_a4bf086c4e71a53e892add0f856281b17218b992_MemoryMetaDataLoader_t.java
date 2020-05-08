 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
 package org.jboss.metadata.plugins.loader.memory;
 
 import java.lang.annotation.Annotation;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.jboss.metadata.plugins.loader.AbstractMutableMetaDataLoader;
 import org.jboss.metadata.spi.ComponentMutableMetaData;
 import org.jboss.metadata.spi.retrieval.AnnotationItem;
 import org.jboss.metadata.spi.retrieval.AnnotationsItem;
 import org.jboss.metadata.spi.retrieval.Item;
 import org.jboss.metadata.spi.retrieval.MetaDataItem;
 import org.jboss.metadata.spi.retrieval.MetaDataRetrieval;
 import org.jboss.metadata.spi.retrieval.MetaDatasItem;
 import org.jboss.metadata.spi.retrieval.basic.BasicAnnotationItem;
 import org.jboss.metadata.spi.retrieval.basic.BasicAnnotationsItem;
 import org.jboss.metadata.spi.retrieval.basic.BasicMetaDataItem;
 import org.jboss.metadata.spi.retrieval.basic.BasicMetaDatasItem;
 import org.jboss.metadata.spi.scope.ScopeKey;
 import org.jboss.metadata.spi.signature.Signature;
 
 /**
  * MemoryMetaDataLoader.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public class MemoryMetaDataLoader extends AbstractMutableMetaDataLoader implements ComponentMutableMetaData
 {
    /** The annotations */
    private volatile Map<String, BasicAnnotationItem> annotations;
 
    /** MetaData by name */
    private volatile Map<String, BasicMetaDataItem> metaDataByName;
 
    /** The component metadata */
    private volatile Map<Signature, MetaDataRetrieval> components; 
 
    /** All annotations */
    private volatile BasicAnnotationsItem cachedAnnotationsItem;
 
    /** All meta data */
    private volatile BasicMetaDatasItem cachedMetaDatasItem;
    
    /** Whether we should cache items */
    private final boolean cachable;
    
    /**
     * Create a new MemoryMetaDataLoader.
     */
    public MemoryMetaDataLoader()
    {
       this(true, false);
    }
    
    /**
     * Create a new MemoryMetaDataLoader.
     * 
     * @param cachable whether items produced should be cachable
     * @param restricted whether restricted items are allowed
     */
    public MemoryMetaDataLoader(boolean cachable, boolean restricted)
    {
       super(restricted);
       this.cachable = cachable;
    }
    
    /**
     * Create a new MemoryMetaDataLoader.
     * 
     * @param scope the scope key
     */
    public MemoryMetaDataLoader(ScopeKey scope)
    {
       this(scope, true, false);
    }
    
    /**
     * Create a new MemoryMetaDataLoader.
     * 
     * @param scope the scope key
     * @param cachable whether items produced should be cachable
     * @param restricted whether restricted items are allowed
     */
    public MemoryMetaDataLoader(ScopeKey scope, boolean cachable, boolean restricted)
    {
       super(scope, restricted);
       this.cachable = cachable;
    }
    
    public <T> boolean isCachable(Item<T> item)
    {
       return cachable;
    }
 
    public AnnotationsItem retrieveAnnotations()
    {
       BasicAnnotationsItem result = cachedAnnotationsItem;
       if (result != null && result.isValid())
          return result;
       
       Map<String, BasicAnnotationItem> temp = annotations;
       if (temp == null)
          return noAnnotations();
       
       Collection<BasicAnnotationItem> values = temp.values();
       if (values.isEmpty())
          return noAnnotations();
       
       AnnotationItem[] items = values.toArray(new AnnotationItem[values.size()]);
       result = new BasicAnnotationsItem(this, items);
       cachedAnnotationsItem = result;
       return result;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Annotation> AnnotationItem<T> retrieveAnnotation(Class<T> annotationType)
    {
       Map<String, BasicAnnotationItem> temp = annotations; 
       if (temp == null)
          return null;
       return temp.get(annotationType.getName());
    }
 
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T addAnnotation(T annotation)
    {
       if (annotation == null)
          throw new IllegalArgumentException("Null annotation");
       checkRestricted(annotation);
       
       synchronized (this)
       {
          if (annotations == null)
             annotations = new ConcurrentHashMap<String, BasicAnnotationItem>();
       }
 
       T result = null;
 
       Class<? extends Annotation> annotationType = annotation.annotationType();
       BasicAnnotationItem<T> old = annotations.get(annotationType.getName());
       if (old != null)
       {
          result = old.getAnnotation();
          if (result == annotation)
             return result;
          old.invalidate();
       }
 
       BasicAnnotationItem<T> item = new BasicAnnotationItem<T>(this, annotation);
       annotations.put(annotationType.getName(), item);
       invalidateAnnotationsItem();
       invalidateMetaDatasItem();
       invalidate();
       return result;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T removeAnnotation(Class<T> annotationType)
    {
       if (annotations == null)
          return null;
       BasicAnnotationItem<T> annotation = annotations.remove(annotationType.getName());
       if (annotation == null)
          return null;
       annotation.invalidate();
       invalidateAnnotationsItem();
       invalidateMetaDatasItem();
       return annotation.getAnnotation();
    }
 
    public MetaDatasItem retrieveMetaData()
    {
       BasicMetaDatasItem result = cachedMetaDatasItem;
       if (result != null && result.isValid())
          return result;
       
       Collection<BasicMetaDataItem> all = null;
       Map<String, BasicAnnotationItem> temp1 = annotations;
       if (temp1 != null && temp1.size() > 0)
       {
         all = new ArrayList<BasicMetaDataItem>();
          Collection<BasicAnnotationItem> values = temp1.values();
          all.addAll(values);
       }
       Map<String, BasicMetaDataItem> temp2 = metaDataByName;
       if (temp2 != null && temp2.size() > 0)
       {
          if (all == null)
             all = new ArrayList<BasicMetaDataItem>();
          Collection<BasicMetaDataItem> values = temp2.values();
          all.addAll(values);
       }
 
       if (all == null)
          return noMetaDatas();
       
       MetaDataItem[] metaDataItems = all.toArray(new MetaDataItem[all.size()]);
       result = new BasicMetaDatasItem(this, metaDataItems);
       cachedMetaDatasItem = result;
       return result;
    }
 
    @SuppressWarnings("unchecked")
    public <T> MetaDataItem<T> retrieveMetaData(Class<T> type)
    {
       MetaDataItem<T> result = super.retrieveMetaData(type);
       if (result != null)
          return result;
       
       Map temp = metaDataByName;
       if (temp == null)
          return null;
       return (MetaDataItem<T>) temp.get(type.getName());
    }
 
    public MetaDataItem retrieveMetaData(String name)
    {
       Map<String, BasicMetaDataItem> temp = metaDataByName; 
       if (temp != null)
       {
          MetaDataItem result = temp.get(name);
          if (result != null)
             return result;
       }
 
       Map<String, BasicAnnotationItem> temp2 = annotations;
       if (temp2 != null)
          return temp2.get(name);
       
       return null;
    }
 
    @SuppressWarnings("unchecked")
    public <T> T addMetaData(T metaData, Class<T> type)
    {
       if (metaData == null)
          throw new IllegalArgumentException("Null metaData");
       if (type == null)
          throw new IllegalArgumentException("Null type");
       
       if (metaData instanceof Annotation)
          return (T) addAnnotation((Annotation) metaData);
 
       checkRestricted(type);
 
       synchronized (this)
       {
          if (metaDataByName == null)
             metaDataByName = new ConcurrentHashMap<String, BasicMetaDataItem>();
       }
 
       T result = null;
       
       BasicMetaDataItem<T> old = metaDataByName.get(type.getName());
       if (old != null)
       {
          result = old.getValue();
          if (result == metaData)
             return result;
          old.invalidate();
       }
       BasicMetaDataItem<T> item = new BasicMetaDataItem<T>(this, type.getName(), metaData);
       metaDataByName.put(type.getName(), item);
       invalidateMetaDatasItem();
       invalidate();
       return result;
    }
 
    @SuppressWarnings("unchecked")
    public <T> T removeMetaData(Class<T> type)
    {
       if (type == null)
          throw new IllegalArgumentException("Null type");
 
       if (type.isAnnotation())
          return (T) removeAnnotation((Class<Annotation>) type);
 
       if (metaDataByName == null)
          return null;
 
       BasicMetaDataItem<T> result = metaDataByName.remove(type.getName());
       if (result == null)
          return null;
       result.invalidate();
       invalidateMetaDatasItem();
       return result.getValue();
    }
 
    @SuppressWarnings("unchecked")
    public <T> T addMetaData(String name, T metaData, Class<T> type)
    {
       if (name == null)
          throw new IllegalArgumentException("Null name");
       if (metaData == null)
          throw new IllegalArgumentException("Null metaData");
       if (type == null)
          throw new IllegalArgumentException("Null type");
       
       checkRestricted(type);
 
       synchronized (this)
       {
          if (metaDataByName == null)
             metaDataByName = new ConcurrentHashMap<String, BasicMetaDataItem>();
       }
 
       T result = null;
       
       BasicMetaDataItem<T> old = metaDataByName.get(name);
       if (old != null)
       {
          result = old.getValue();
          if (result == metaData)
             return result;
          old.invalidate();
       }
       BasicMetaDataItem<T> item = new BasicMetaDataItem<T>(this, name, metaData);
       metaDataByName.put(name, item);
       invalidateMetaDatasItem();
       invalidate();
       return result;
    }
 
    @SuppressWarnings("unchecked")
    public <T> T removeMetaData(String name, Class<T> type)
    {
       if (name == null)
          throw new IllegalArgumentException("Null name");
 
       Map<String, BasicMetaDataItem> temp = metaDataByName;
       if (temp == null)
          return null;
       
       BasicMetaDataItem<T> result = temp.remove(name);
       if (result == null)
          return null;
       result.invalidate();
       invalidateMetaDatasItem();
       return result.getValue();
    }
 
    public MetaDataRetrieval addComponentMetaDataRetrieval(Signature signature, MetaDataRetrieval component)
    {
       if (signature == null)
          throw new IllegalArgumentException("Null signature");
       
       if (components == null)
          components = new ConcurrentHashMap<Signature, MetaDataRetrieval>();
       
       return components.put(signature, component);
    }
 
    public MetaDataRetrieval removeComponentMetaDataRetrieval(Signature signature)
    {
       if (signature == null)
          throw new IllegalArgumentException("Null signature");
       
       if (components == null)
          return null;
       
       return components.remove(signature);
    }
 
    public MetaDataRetrieval getComponentMetaDataRetrieval(Signature signature)
    {
       if (components == null)
          return null;
       
       return components.get(signature);
    }
 
    /**
     * Invalidate the annotations item
     */
    protected void invalidateAnnotationsItem()
    {
       BasicAnnotationsItem temp = cachedAnnotationsItem;
       if (temp != null)
       {
          temp.invalidate();
          cachedAnnotationsItem = null;
       }
    }
 
    /**
     * Set no annotations
     * 
     * @return no annotations
     */
    protected BasicAnnotationsItem noAnnotations()
    {
       BasicAnnotationsItem result = new BasicAnnotationsItem(this, BasicAnnotationsItem.NO_ANNOTATION_ITEMS);
       cachedAnnotationsItem = result;
       return result;
    }
 
    /**
     * Invalidate the metaDatas item
     */
    protected void invalidateMetaDatasItem()
    {
       BasicMetaDatasItem temp = cachedMetaDatasItem;
       if (temp != null)
       {
          temp.invalidate();
          cachedMetaDatasItem = null;
       }
    }
 
    /**
     * Set no meta data
     * 
     * @return no meta data
     */
    protected BasicMetaDatasItem noMetaDatas()
    {
       BasicMetaDatasItem result = new BasicMetaDatasItem(this, BasicMetaDatasItem.NO_META_DATA_ITEMS);
       cachedMetaDatasItem = result;
       return result;
    }
 }
