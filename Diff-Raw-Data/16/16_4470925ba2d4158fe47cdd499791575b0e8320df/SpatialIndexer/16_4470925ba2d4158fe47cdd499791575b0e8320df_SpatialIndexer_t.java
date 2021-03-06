 package org.apache.lucene.spatial.search;
 
 import org.apache.lucene.document.Fieldable;
 import org.apache.lucene.spatial.base.shape.Shape;
 
 /**
  * must be thread safe
  */
 public interface SpatialIndexer<T extends SpatialFieldInfo> {
 
   boolean isPolyField();
  Fieldable createField(T fieldInfo, Shape shape, boolean index, boolean store);
  Fieldable[] createFields(T fieldInfo, Shape shape, boolean index, boolean store);
 }
