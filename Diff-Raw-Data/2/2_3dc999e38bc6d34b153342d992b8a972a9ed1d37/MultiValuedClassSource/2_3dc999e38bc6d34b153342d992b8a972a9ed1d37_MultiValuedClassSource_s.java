 package com.iotake.solr.client.binder.source;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.apache.solr.client.solrj.beans.BindingException;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrInputDocument;
 
 import com.iotake.solr.client.binder.instantiator.BeanInstantiator;
 
 public abstract class MultiValuedClassSource extends ClassSource implements
     MultiValuableDocumentSource {
 
   private final String path;
 
   public MultiValuedClassSource(Class<?> documentClass,
       BeanInstantiator instantiator, FieldSource[] fieldSources, String path) {
     super(documentClass, instantiator, fieldSources);
     this.path = path;
   }
 
   public void transferEmpty(SolrInputDocument document) {
     for (FieldSource fieldSource : getFieldSources()) {
      ((MultiValuedSource) fieldSource).transferEmpty(document);
     }
   }
 
   public Object doExtract(SolrDocument document) {
     FieldSource[] fieldSources = getFieldSources();
     @SuppressWarnings("unchecked")
     Iterator<Object>[] iterators = new Iterator[fieldSources.length];
     int items = 0;
     for (int i = 0; i < fieldSources.length; i++) {
       FieldSource fieldSource = fieldSources[i];
       @SuppressWarnings("unchecked")
       Collection<Object> collection = (Collection<Object>) fieldSource
           .extract(document);
       if (i == 0) {
         items = collection == null ? 0 : collection.size();
       }
       iterators[i] = collection == null ? null : collection.iterator();
     }
     Collection<Object> beans = new ArrayList<Object>(items);
     for (int j = 0; j < items; j++) {
       Object bean = instantiate();
       for (int i = 0; i < fieldSources.length; i++) {
         FieldSource fieldSource = fieldSources[i];
         Iterator<Object> iterator = iterators[i];
         if (iterator == null || !iterator.hasNext()) {
           throw new BindingException(
               "Incorrect number of collection values for "
                   + getDocumentClass().getName() + "."
                   + fieldSource.getField().getName() + " using path " + path);
         }
         Object fieldValue = iterator.next();
         fieldSource.setFieldValue(bean, fieldValue);
 
       }
       beans.add(bean);
     }
     return beans;
   }
 }
