 package com.amee.domain.data;
 
 import com.amee.domain.item.BaseItemValue;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Transformer;
 import org.springframework.beans.factory.annotation.Autowire;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import javax.persistence.Transient;
 import java.util.Date;
import java.util.HashMap;
 import java.util.List;
 
 @Configurable(autowire = Autowire.BY_TYPE)
public class ItemValueMap extends HashMap {
 
     private LegacyItemValueMap legacyItemValueMap;
     private NuItemValueMap nuItemValueMap;
 
     @Transient
     private transient ItemValueMap adapter;
 
     public ItemValueMap(LegacyItemValueMap legacyItemValueMap) {
         super();
         setLegacyItemValueMap(legacyItemValueMap);
     }
 
     public ItemValueMap(NuItemValueMap nuItemValueMap) {
         super();
         setNuItemValueMap(nuItemValueMap);
     }
 
     private void setLegacyItemValueMap(LegacyItemValueMap itemValueMap) {
         itemValueMap.setAdapter(this);
         this.legacyItemValueMap = itemValueMap;
     }
 
     private void setNuItemValueMap(NuItemValueMap itemValueMap) {
         itemValueMap.setAdapter(this);
         this.nuItemValueMap = itemValueMap;
     }
 
     public static ItemValueMap getItemValueMap(LegacyItemValueMap itemValueMap) {
         if (itemValueMap != null) {
             if (itemValueMap.getAdapter() != null) {
                 return itemValueMap.getAdapter();
             } else {
                 return new ItemValueMap(itemValueMap);
             }
         } else {
             return null;
         }
     }
 
     public static ItemValueMap getItemValueMap(NuItemValueMap itemValueMap) {
         if (itemValueMap != null) {
             if (itemValueMap.getAdapter() != null) {
                 return itemValueMap.getAdapter();
             } else {
                 return new ItemValueMap(itemValueMap);
             }
         } else {
             return null;
         }
     }
 
     public boolean isLegacy() {
         if (getLegacyItemValueMap() != null) {
             return true;
         } else if (getNuItemValueMap() != null) {
             return false;
         } else {
             throw new IllegalStateException("Missing map.");
         }
     }
 
     public ItemValue get(String path) {
         if (isLegacy()) {
             return getLegacyItemValueMap().get(path).getAdapter();
         } else {
             return getNuItemValueMap().get(path).getAdapter();
         }
     }
 
     public List<ItemValue> getAll(Date startDate) {
         if (isLegacy()) {
             return legacyToAdapter(getLegacyItemValueMap().getAll(startDate));
         } else {
             return nuToAdapter(getNuItemValueMap().getAll(startDate));
         }
     }
 
     public List<ItemValue> getAll(String path) {
         if (isLegacy()) {
             return legacyToAdapter(getLegacyItemValueMap().getAll(path));
         } else {
             return nuToAdapter(getNuItemValueMap().getAll(path));
         }
     }
 
     public ItemValue get(String path, Date startDate) {
         if (isLegacy()) {
             return getLegacyItemValueMap().get(path, startDate).getAdapter();
         } else {
             return getNuItemValueMap().get(path, startDate).getAdapter();
         }
     }
 
     public ItemValue put(String path, ItemValue itemValue) {
         if (isLegacy()) {
             getLegacyItemValueMap().put(path, itemValue.getLegacyEntity());
             return itemValue;
         } else {
             getNuItemValueMap().put(path, itemValue.getNuEntity());
             return itemValue;
         }
     }
 
     private List<ItemValue> legacyToAdapter(List<LegacyItemValue> legacyItemValues) {
         return (List<ItemValue>) CollectionUtils.collect(
                 legacyItemValues, new Transformer() {
                     public Object transform(Object legacyItemValue) {
                         return ((LegacyItemValue) legacyItemValue).getAdapter();
                     }
                 });
     }
 
     private List<ItemValue> nuToAdapter(List<BaseItemValue> baseItemValues) {
         return (List<ItemValue>) CollectionUtils.collect(
                 baseItemValues, new Transformer() {
                     public Object transform(Object baseItemValue) {
                         return ((BaseItemValue) baseItemValue).getAdapter();
                     }
                 });
     }
 
     public LegacyItemValueMap getLegacyItemValueMap() {
         return legacyItemValueMap;
     }
 
     public NuItemValueMap getNuItemValueMap() {
         return nuItemValueMap;
     }
 
     public ItemValueMap getAdapter() {
         return adapter;
     }
 
 
 }
