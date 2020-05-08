 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.core.context;
 
 import java.util.AbstractCollection;
 import java.util.AbstractMap;
 import java.util.AbstractSet;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.seasar.framework.util.EnumerationIterator;
 
 /**
  * @author Shinpei Ohtani
  */
 public abstract class AbstractExternalContextMap extends AbstractMap {
 
     private Set entrySet_;
 
     private Set keySet_;
 
     private Collection values_;
 
     public AbstractExternalContextMap() {
     }
 
     public void clear() {
         //avoid ConcurrentModificationException
         List list = new ArrayList();
         for (Enumeration e = getAttributeNames(); e.hasMoreElements();) {
             String key = (String) e.nextElement();
             list.add(key);
         }
         clearReally(list);
     }
 
     private void clearReally(List keys) {
         for (Iterator itr = keys.iterator(); itr.hasNext();) {
             String key = (String) itr.next();
             removeAttribute(key);
         }
     }
 
     public boolean containsKey(Object key) {
        return (getAttribute(key.toString()) != null);
     }
 
     public boolean containsValue(Object value) {
         if (value != null) {
             for (Enumeration e = getAttributeNames(); e.hasMoreElements();) {
                 String key = (String) e.nextElement();
                 Object attributeValue = getAttribute(key);
                 if (value.equals(attributeValue)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public Set entrySet() {
         if (entrySet_ == null) {
             entrySet_ = new EntrySet(this);
         }
         return entrySet_;
     }
 
     public Object get(Object key) {
        return getAttribute(key.toString());
     }
 
     public Object put(Object key, Object value) {
        String keyStr = key.toString();
         Object o = getAttribute(keyStr);
         setAttribute(keyStr, value);
         return o;
     }
 
     public void putAll(Map map) {
         for (Iterator itr = map.entrySet().iterator(); itr.hasNext();) {
             Map.Entry entry = (Map.Entry) itr.next();
             String key = (String) entry.getKey();
             setAttribute(key, entry.getValue());
         }
     }
 
     public boolean isEmpty() {
         return !getAttributeNames().hasMoreElements();
     }
 
     public Set keySet() {
         if (keySet_ == null) {
             keySet_ = new KeySet(this);
         }
         return keySet_;
     }
 
     public Object remove(Object key) {
        String keyStr = key.toString();
         Object o = getAttribute(keyStr);
         removeAttribute(keyStr);
         return o;
     }
 
     public Collection values() {
         if (values_ == null) {
             values_ = new ValuesCollection(this);
         }
         return values_;
     }
 
     protected abstract Object getAttribute(String key);
 
     protected abstract void setAttribute(String key, Object value);
 
     protected abstract Enumeration getAttributeNames();
 
     protected abstract void removeAttribute(String key);
 
     abstract class AbstractExternalContextSet extends AbstractSet {
 
         /* (non-Javadoc)
          * @see java.util.AbstractCollection#size()
          */
         public int size() {
             int size = 0;
             for (Iterator itr = iterator(); itr.hasNext(); size++) {
                 itr.next();
             }
             return size;
         }
 
     }
 
     class EntrySet extends AbstractExternalContextSet {
 
         private AbstractExternalContextMap contextMap_;
 
         public EntrySet(AbstractExternalContextMap contextMap) {
             contextMap_ = contextMap;
         }
 
         /* (non-Javadoc)
          * @see java.util.AbstractCollection#iterator()
          */
         public Iterator iterator() {
             return new EntryIterator(contextMap_);
         }
 
         /* (non-Javadoc)
          * @see java.util.AbstractCollection#remove(java.lang.Object)
          */
         public boolean remove(Object o) {
             if (!(o instanceof Map.Entry)) {
                 return false;
             }
             Map.Entry entry = (Map.Entry) o;
             Object returnObj = contextMap_.remove(entry.getKey());
             return (returnObj != null);
         }
     }
 
     class KeySet extends AbstractExternalContextSet {
 
         private AbstractExternalContextMap contextMap_;
 
         public KeySet(AbstractExternalContextMap contextMap) {
             contextMap_ = contextMap;
         }
 
         /* (non-Javadoc)
          * @see java.util.AbstractCollection#iterator()
          */
         public Iterator iterator() {
             return new KeyIterator(contextMap_);
         }
 
         /* (non-Javadoc)
          * @see java.util.AbstractCollection#remove(java.lang.Object)
          */
         public boolean remove(Object o) {
             if (!(o instanceof String)) {
                 return false;
             }
             String s = (String) o;
             Object returnObj = contextMap_.remove(s);
             return (returnObj != null);
         }
     }
 
     class ValuesCollection extends AbstractCollection {
 
         private AbstractExternalContextMap contextMap_;
 
         public ValuesCollection(AbstractExternalContextMap contextMap) {
             contextMap_ = contextMap;
         }
 
         public int size() {
             int size = 0;
             for (Iterator itr = iterator(); itr.hasNext(); size++) {
                 itr.next();
             }
             return size;
         }
 
         public Iterator iterator() {
             return new ValuesIterator(contextMap_);
         }
 
     }
 
     abstract class AbstractExternalContextIterator extends EnumerationIterator {
 
         private final AbstractExternalContextMap contextMap_;
 
         private String currentKey_;
 
         private boolean removeCalled_ = false;
 
         public AbstractExternalContextIterator(
                 final AbstractExternalContextMap contextMap) {
             super(contextMap.getAttributeNames());
             contextMap_ = contextMap;
         }
 
         public Object next() {
             currentKey_ = (String) super.next();
             try {
                 return doNext();
             } finally {
                 removeCalled_ = false;
             }
         }
 
         public void remove() {
             if (currentKey_ != null && !removeCalled_) {
                 doRemove();
                 removeCalled_ = true;
             } else {
                 throw new IllegalStateException();
             }
         }
 
         protected String getCurrentKey() {
             return currentKey_;
         }
 
         protected Object getValueFromMap(String key) {
             return contextMap_.get(key);
         }
 
         protected void removeKeyFromMap(String key) {
             contextMap_.remove(key);
         }
 
         protected void removeValueFromMap(Object value) {
             if (containsValue(value)) {
                 for (Iterator itr = entrySet().iterator(); itr.hasNext();) {
                     Map.Entry e = (Map.Entry) itr.next();
                     if (value.equals(e.getValue())) {
                         contextMap_.remove(e.getKey());
                     }
                 }
             }
 
         }
 
         protected abstract Object doNext();
 
         protected abstract void doRemove();
     }
 
     class EntryIterator extends AbstractExternalContextIterator {
 
         public EntryIterator(AbstractExternalContextMap contextMap) {
             super(contextMap);
         }
 
         protected Object doNext() {
             String key = getCurrentKey();
             return new ImmutableEntry(key, getValueFromMap(key));
         }
 
         protected void doRemove() {
             String key = getCurrentKey();
             removeKeyFromMap(key);
         }
 
     }
 
     class KeyIterator extends AbstractExternalContextIterator {
 
         public KeyIterator(AbstractExternalContextMap contextMap) {
             super(contextMap);
         }
 
         protected Object doNext() {
             return getCurrentKey();
         }
 
         protected void doRemove() {
             removeKeyFromMap(getCurrentKey());
         }
     }
 
     class ValuesIterator extends AbstractExternalContextIterator {
 
         public ValuesIterator(AbstractExternalContextMap contextMap) {
             super(contextMap);
         }
 
         protected Object doNext() {
             String key = getCurrentKey();
             return getValueFromMap(key);
         }
 
         protected void doRemove() {
             String key = getCurrentKey();
             Object value = getValueFromMap(key);
             removeValueFromMap(value);
         }
 
     }
 
     protected static class ImmutableEntry implements Map.Entry {
 
         private final Object key_;
 
         private final Object value_;
 
         public ImmutableEntry(Object key, Object value) {
             key_ = key;
             value_ = value;
         }
 
         public Object getKey() {
             return key_;
         }
 
         public Object getValue() {
             return value_;
         }
 
         public Object setValue(Object arg0) {
             throw new UnsupportedOperationException("Immutable entry.");
         }
 
         public boolean equals(Object obj) {
             if (obj == null || !(obj instanceof ImmutableEntry)) {
                 return false;
             }
             ImmutableEntry entry = (ImmutableEntry) obj;
             Object key = entry.getKey();
             Object value = entry.getValue();
 
             return (key == key_ || (key != null && key.equals(key_)))
                     && (value == value_ || (value != null && value
                             .equals(value_)));
         }
 
         public int hashCode() {
             return ((key_ != null) ? key_.hashCode() : 0)
                     ^ ((value_ != null) ? value_.hashCode() : 0);
         }
     }
 
 }
