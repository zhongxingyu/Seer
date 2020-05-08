 package com.feedly.cassandra.dao;
 
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.Collection;
 import java.util.List;
 
 import com.feedly.cassandra.IKeyspaceFactory;
 import com.feedly.cassandra.entity.EIndexType;
 import com.feedly.cassandra.entity.EntityMetadata;
 import com.feedly.cassandra.entity.IndexMetadata;
 import com.feedly.cassandra.entity.PropertyMetadata;
 
 /*
  * helper class to do finds (reads by index).
  */
 @SuppressWarnings("unchecked")
 public class FindHelper<K, V> extends LoadHelper<K, V>
 {
     private final HashIndexFindHelper<K, V> _hashIndexFinder;
     private final RangeIndexFindHelper<K, V> _rangeIndexFinder;
     
     FindHelper(EntityMetadata<V> meta, IKeyspaceFactory factory, IStaleIndexValueStrategy staleValueStrategy)
     {
         super(meta, factory);
         _hashIndexFinder = new HashIndexFindHelper<K, V>(meta, factory);
         _rangeIndexFinder = new RangeIndexFindHelper<K, V>(meta, factory, staleValueStrategy);
     }
     
     private IndexMetadata chooseIndex(boolean rangeOnly, V... templates) 
     {
         List<PropertyMetadata> props = new ArrayList<PropertyMetadata>();
         BitSet dirty = asEntity(templates[0]).getModifiedFields();
         
         if(templates.length > 1)
         {
             dirty = (BitSet) dirty.clone();
             for(int i = templates.length-1; i > 0; i--)
                 dirty.and(asEntity(templates[i]).getModifiedFields());
         }
             
         for(int i = dirty.nextSetBit(0); i >= 0; i = dirty.nextSetBit(i + 1))
         {
             props.add(_entityMeta.getProperties().get(i));
         }
         
         if(props.isEmpty())
             throw new IllegalArgumentException("no properties set");
         
         IndexMetadata matching = null;
         int matchCnt = 0;
         for(IndexMetadata im : _entityMeta.getIndexes())
         {
             if(rangeOnly && im.getType() != EIndexType.RANGE)
                 continue;
             
             if(props.equals(im.getIndexedProperties()))
             {
                 matching = im;
                 matchCnt = im.getIndexedProperties().size();
                 break;
             }
 
             int cnt = 0;
             for(PropertyMetadata indexedProp : im.getIndexedProperties())
             {
                 if(props.contains(indexedProp))
                     cnt++;
                 else
                     break;
             }
 
             if(cnt > matchCnt)
             {
                 matchCnt = cnt;
                 matching = im;
             }
             else if(cnt > 0 && cnt == matchCnt && im.getIndexedProperties().size() < matching.getIndexedProperties().size())
             {
                 //smaller number of columns
                 matching = im;
             }
         }
         
         if(matching != null)
         {
            _logger.info("selected index {} [{} of {} col(s)]",  matching, matchCnt);
             return matching;
         }
         
         throw new IllegalStateException("no applicable index for properties " + props);
     }
     
     
     public V find(V template, FindOptions options)
     {
         IndexMetadata index = chooseIndex(false, template);
         if(index.getType() == EIndexType.HASH)
         {
             return _hashIndexFinder.find(template, options, index);
         }
         else 
         {
             return _rangeIndexFinder.find(template, options, index);
         }
     }
     
     public Collection<V> mfind(V template, FindOptions options)
     {
         IndexMetadata index = chooseIndex(false, template);
         if(index.getType() == EIndexType.HASH)
         {
             return _hashIndexFinder.mfind(template, options, index);
         }
         else 
         {
             return _rangeIndexFinder.mfind(template, options, index);
         }
     }
 
     public Collection<V> mfindBetween(V startTemplate, V endTemplate)
     {
         return mfindBetween(startTemplate, endTemplate, null);
     }
 
     public Collection<V> mfindBetween(V startTemplate, V endTemplate, FindBetweenOptions options)
     {
         if(options == null)
             options = new FindBetweenOptions();
         
         IndexMetadata index = chooseIndex(true, startTemplate, endTemplate);
 
         return _rangeIndexFinder.mfindBetween(startTemplate, endTemplate, options, index);
     }
 }
