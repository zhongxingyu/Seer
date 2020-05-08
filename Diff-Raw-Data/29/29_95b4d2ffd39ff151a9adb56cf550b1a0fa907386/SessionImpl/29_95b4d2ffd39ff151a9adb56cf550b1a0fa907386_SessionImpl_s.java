 /*
  * Swordess-ldap, an Object-Directory Mapping tool. 
  * 
  * Copyright (c) 2013, 2013 Liu Xingyu.
  * 
  * Swordess-ldap is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  * 
  * Swordess-ldap is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with Swordess-ldap. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.swordess.ldap.odm.core;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.naming.NameNotFoundException;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.BasicAttributes;
 import javax.naming.directory.ModificationItem;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 import javax.naming.ldap.InitialLdapContext;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.swordess.ldap.Session;
 import org.swordess.ldap.SessionException;
 import org.swordess.ldap.odm.Distinguishable;
 import org.swordess.ldap.odm.core.ProxyFactory.SetterInterceptor;
 import org.swordess.ldap.odm.metadata.EntityMetaData;
 import org.swordess.ldap.odm.metadata.PropertyMetaData;
 import org.swordess.ldap.util.AttrUtils;
 import org.swordess.ldap.util.CollectionUtils;
 import org.swordess.ldap.util.Evaluator;
 import org.swordess.ldap.util.LogUtils;
 import org.swordess.ldap.util.ModUtils;
 
 
 
 public class SessionImpl implements Session {
 
     private static final Log LOG = LogFactory.getLog(SessionImpl.class);
     
     private Map<String, Object> sessionCache = new HashMap<String, Object>();
     
     private final InitialLdapContext ctx;
     private final boolean bindToThreadLocal;
     
     SessionImpl(InitialLdapContext ctx, boolean bindToThreadLocal) {
         this.ctx = ctx;
         this.bindToThreadLocal = bindToThreadLocal;
     }
     
     @Override
     public void create(Object obj) {
         if (null == obj) {
             return;
         }
         if (obj instanceof Persistent) {
             update(obj);
             return;
         }
         
         Object idValue = EntityMetaData.get(obj.getClass()).getIdProperty().getter().get(obj);
         if (null == idValue) {
             throw new SessionException("Unable to persist an object which has no id: " + obj);
         }
         
         String dn = DnHelper.build(idValue.toString(), obj.getClass());
         LogUtils.debug(LOG, "create dn=" + dn);
         
         try {
             ctx.bind(dn, null, fromTransientToAttributes(obj));
             
             /*
              * We didn't do an extra lookup invocation to put it into the cache,
              * as we have no idea whether the client code need the persisted
              * entity or not. Besides, if the answer is no, we slow down the
              * speed. If yes, the client code still hold the original reference
              * of the transient object and we would rather the client code to do
              * the extra lookup by itself.
              */
             
         } catch (NamingException e) {
             throw new SessionException(e.getMessage(), e);
         }
     }
     
     @Override
     public void update(Object entity) {
         if (null == entity) {
             return;
         }
         
         if (!(entity instanceof Persistent)) {
             create(entity);
             return;
         }
         
         String dn = DnHelper.build(entity);
         List<ModificationItem> mods = fromEntityToModificationItems(entity);
         if (mods.isEmpty()) {
             LogUtils.debug(LOG, "no changes found when updating dn=" + dn + ", do nothing");
             return;
         }
         
         LogUtils.debug(LOG, "update dn=" + dn);
         
         try {
             ctx.modifyAttributes(dn, mods.toArray(new ModificationItem[0]));
             
             /*
              * All current modifications are cleared, so could we continue
              * to use this entity? The answer is no!
              * 
              * For simple string properties, this works fine. But for
              * multiple values we need to do following things before reusing
              * this entity:
              * 1. clear changes of all the modified MonitoredList
              * 2. turn normal List into MonitoredList
              */
             ProxyFactory.getModifiedPropNames(entity).clear();
             for (PropertyMetaData propMetaData : EntityMetaData.get(ClassHelper.actualClass(entity.getClass()))) {
                 if (propMetaData.isReadonly() || !propMetaData.isMultiple()) {
                     continue;
                 }
                 
                 Object propValue = propMetaData.getter().get(entity);
                 if (null == propValue) {
                     continue;
                 }
                 
                 List propValues = (List) propValue;
                 if (propValues instanceof MoniteredList) {
                     // clear changes of all the modifed MoniteredList
                     ((MoniteredList)propValues).clearChanges();
                 } else {
                     // turn normal List into MoniteredList
                     propMetaData.setter().set(entity, new MoniteredList(propValues));
                 }
             }
             
             /*
              * Now we have no need to remove the entity from the cache. And its
              * possible to continuous use of the entity.
              */
             
         } catch (NamingException e) {
             throw new SessionException(e.getMessage(), e);
         }
     }
     
     @Override
     public void delete(Object entity) {
         if (null == entity) {
             return;
         }
         
         if (!(entity instanceof Persistent)) {
             LogUtils.debug(LOG, entity + " is not persistent, do nothing");
             return;
         }
         
         String dn = DnHelper.build(entity);
         LogUtils.debug(LOG, "delete dn=" + dn);
         
         try {
             ctx.unbind(dn);
             sessionCache.remove(dn);
         } catch (NamingException e) {
             throw new SessionException(e.getMessage(), e);
         }
     }
     
     @Override
     public void delete(String dn) {
         if (StringUtils.isEmpty(dn)) {
             return;
         }
         
         LogUtils.debug(LOG, "delete dn=" + dn);
         
         try {
             ctx.unbind(dn);
             sessionCache.remove(dn);
         } catch (NameNotFoundException ignore) {
             LogUtils.debug(LOG, "Name not found: " + dn);
         } catch (NamingException e) {
             throw new SessionException(e.getMessage(), e);
         }
     }
     
     @SuppressWarnings("unchecked")
     @Override
     public <T> T read(Class<T> clazz, String dn) {
         if (null == dn) {
             return null;
         }
         
         LogUtils.debug(LOG, "read " + clazz.getName() + " with dn=" + dn);
         
         if (sessionCache.containsKey(dn)) {
             LogUtils.info(LOG, "cache hit " + dn);
             return (T) sessionCache.get(dn);
         }
         
         try {
             Attributes allDefinedAttrs = ctx.getAttributes(dn, EntityMetaData.getDefinedAttrNames(clazz));
             T entity = fromAttributesToEntity(clazz, allDefinedAttrs);
             sessionCache.put(dn, entity);
             return entity;
             
         } catch (NameNotFoundException e) {
             return null;
             
         } catch (NamingException e) {
             throw new SessionException(e.getMessage(), e);
         }
     }
 
     @Override
     public Map<String, Object> read(Class<?> clazz, String dn, String[] returningAttrs) {
         if (null == dn) {
             return null;
         }
         
         LogUtils.debug(LOG, String.format("read %s with dn=%s, returningAttrs=%s",
                 clazz.getName(), dn, Arrays.toString(returningAttrs)));
         
         // fetch the information from session cache first to decrease one possible lookup operation
         if (sessionCache.containsKey(dn)) {
             Object entity = sessionCache.get(dn);
             Map<String, Object> retVal = new HashMap<String, Object>();
             
             EntityMetaData metaData = EntityMetaData.get(clazz);
             for (String returningAttr : returningAttrs) {
                 PropertyMetaData propMetaData = metaData.getProperty(returningAttr);
                 if (null == propMetaData) {
                     continue;
                 }
                 
                 Object propValue = propMetaData.getter().get(entity);
                 if (null == propValue) {
                     continue;
                 }
                 
                 if (!propMetaData.isMultiple()) {
                     retVal.put(returningAttr, propValue);
                 } else {
                     List propValues = (List) propValue;
                     if (!propValues.isEmpty()) {
                        retVal.put(returningAttr, propValues);
                     }
                 }
             }
             return retVal;
             
         } else {
             try {
                 return fromAttributesToMap(clazz, ctx.getAttributes(dn, returningAttrs));
             } catch (NamingException e) {
                 throw new SessionException(e.getMessage(), e);
             }
         }
     }
 
     @Override
     public <T> List<T> search(Class<T> clazz, String filter) {
         if (null == filter) {
             return null;
         }
         
         LogUtils.debug(LOG, "search " + clazz.getName() + " with filter=" + filter);
         
         SearchControls ctrl = new SearchControls();
         ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
         ctrl.setReturningAttributes(EntityMetaData.getDefinedAttrNames(clazz));
         
         List<T> retVal = new ArrayList<T>();
         try {
             NamingEnumeration<SearchResult> results = ctx.search(EntityMetaData.get(clazz).context(), filter, ctrl);
             while (results.hasMore()) {
                 try {
                     SearchResult result = results.next();
                     T entity = null;
                     if (sessionCache.containsKey(result.getNameInNamespace())) {
                         // guarantee the reference integrity for one search result
                         entity = (T) sessionCache.get(result.getNameInNamespace());
                     } else {
                         entity = fromAttributesToEntity(clazz, result.getAttributes());
                         sessionCache.put(result.getNameInNamespace(), entity);
                     }
                     retVal.add(entity);
                 } catch (NamingException e) {
                     LogUtils.error(LOG, "Unable to construct the entity", e);
                 }
             }
         } catch (NamingException e) {
             throw new SessionException(e.getMessage(), e);
         }
         return retVal;
     }
 
     @Override
     public List<Map<String, Object>> search(Class<?> clazz, String filter, String[] returningAttrs) {
         if (null == filter) {
             return null;
         }
         
         LogUtils.debug(LOG, String.format("search %s with filter=%s, returningAttrs=%s",
                 clazz.getName(), filter, Arrays.toString(returningAttrs)));
         
         SearchControls ctrl = new SearchControls();
         ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
         ctrl.setReturningAttributes(returningAttrs);
         
         try {
             List<Map<String, Object>> retVal = new ArrayList<Map<String,Object>>();
             NamingEnumeration<SearchResult> results = ctx.search(EntityMetaData.get(clazz).context(), filter, ctrl);
             while (results.hasMore()) {
                 try {
                     SearchResult result = results.next();
                     retVal.add(fromAttributesToMap(clazz, result.getAttributes()));
                 } catch (NamingException e) {
                     LogUtils.error(LOG, "Unable to construct the map", e);
                 }
             }
             return retVal;
         } catch (NamingException e) {
             throw new SessionException(e.getMessage(), e);
         }
     }
 
     @Override
     public <T> T uniqueSearch(Class<T> clazz, String filter) {
         return unique(search(clazz, filter));
     }
 
     @Override
     public Map<String, Object> uniqueSearch(Class<?> clazz, String filter, String[] returningAttrs) {
         return unique(search(clazz, filter, returningAttrs));
     }
     
     private static <T> T unique(List<T> list) {
         if (CollectionUtils.isEmpty(list)) {
             return null;
         }
         return list.get(0);
     }
     
     @Override
     public void close() {
         sessionCache.clear();
         
         // release the internal JNDI connection
         if (bindToThreadLocal) {
             DefaultSessionFactory.getDefaultFactory().closeCurrentSession();
         } else {
             DefaultSessionFactory.getDefaultFactory().closeLdapContext(ctx);
         }
     }
 
     private static Attributes fromTransientToAttributes(Object obj) {
         EntityMetaData metaData = EntityMetaData.get(ClassHelper.actualClass(obj.getClass()));
         Attributes toSaves = new BasicAttributes();
         for (PropertyMetaData propMetaData : metaData) {
             Object propValue = propMetaData.getter().get(obj);
             if (propValue == null) {
                 continue;
             }
             
             // ignore empty list when create
             if (propMetaData.isMultiple() && ((List) propValue).isEmpty()) {
                 continue;
             }
             
             Evaluator<String> evaluator = null;
             if (propMetaData.isReference()) {
                 // reference property use dn as attribute value
                 final EntityMetaData metaDataOfReferenceProp = EntityMetaData.get(propMetaData.getValueClass());
                 evaluator = new Evaluator<String>() {
                     public String eval(Object obj) {
                         Object idValue = metaDataOfReferenceProp.getIdProperty().getter().get(obj);
                         if (null == idValue) {
                             return null;
                         }
                         return DnHelper.build((String)idValue, metaDataOfReferenceProp.getManagedClass());
                     }
                 };
             }
             
             if (!propMetaData.isMultiple()) {
                 AttrUtils.putIfNotNull(toSaves, AttrUtils.create(propMetaData.getLdapPropName(), propValue, evaluator));
             } else {
                 AttrUtils.putIfNotNull(toSaves, AttrUtils.create(propMetaData.getLdapPropName(), (List)propValue, evaluator));
             }
         }
         
         toSaves.put(AttrUtils.create("objectclass", metaData.objectClasses()));
         return toSaves;
     }
     
     private static List<ModificationItem> fromEntityToModificationItems(Object entity) {
         List<ModificationItem> mods = new ArrayList<ModificationItem>();
         
         EntityMetaData metaData = EntityMetaData.get(ClassHelper.actualClass(entity.getClass()));
         Set<String> modifiedJavaBeanPropNames = ProxyFactory.getModifiedPropNames(entity);
         if (!CollectionUtils.isEmpty(modifiedJavaBeanPropNames)) {
             LogUtils.debug(LOG, "found modified properties for " + DnHelper.build(entity) + ": " + modifiedJavaBeanPropNames);
             
             // single valued properties
             for (String javaBeanPropName : modifiedJavaBeanPropNames) {
                 PropertyMetaData propMetaData = metaData.getPropertyByJavaBeanPropName(javaBeanPropName);
                 if (propMetaData.isReadonly() || propMetaData.isMultiple()) {
                     continue;
                 }
                 
                 Object propValue = propMetaData.getter().get(entity);
                 if (null == propValue) {
                     mods.add(ModUtils.remove(propMetaData.getLdapPropName()));
                 } else {
                     Evaluator<String> evaluator = propMetaData.isReference() ? DN_EVALUATOR : null;
                     CollectionUtils.addIfNotNull(mods, ModUtils.replace(propMetaData.getLdapPropName(), propValue, evaluator));
                 }
             }
         }
         
         // multiple valued properties
         for (PropertyMetaData propMetaData : metaData) {
             if (propMetaData.isReadonly() || !propMetaData.isMultiple()) {
                 continue;
             }
             
             List propValues = (List) propMetaData.getter().get(entity);
             if (null == propValues) {
                 mods.add(ModUtils.remove(propMetaData.getLdapPropName()));
             } else {
                 Evaluator<String> evaluator = propMetaData.isReference() ? DN_EVALUATOR : null;
                 if (propValues instanceof MoniteredList) {
                     MoniteredList moniteredList = (MoniteredList) propValues;
                     CollectionUtils.addIfNotNull(mods, ModUtils.add(propMetaData.getLdapPropName(), moniteredList.getAddedElements(), evaluator));
                     CollectionUtils.addIfNotNull(mods, ModUtils.remove(propMetaData.getLdapPropName(), moniteredList.getRemovedElements(), evaluator));
                 } else {
                     CollectionUtils.addIfNotNull(mods, ModUtils.replace(propMetaData.getLdapPropName(), propValues, evaluator));
                 }
             }
         }
         
         return mods;
     }
     
     private <T> T fromAttributesToEntity(Class<T> clazz, Attributes attributes) throws NamingException {
         try {
             Map.Entry<Object, SetterInterceptor> pair = ProxyFactory.getProxiedEntity(clazz);
             T entity = (T) pair.getKey();
             
             EntityMetaData metaData = EntityMetaData.get(clazz);
             Set<String> multipleLdapAttrNames = new HashSet<String>();
             for (PropertyMetaData propMetaData : metaData) {
                 if (propMetaData.isMultiple()) {
                     multipleLdapAttrNames.add(propMetaData.getLdapPropName());
                 }
             }
             
             for (NamingEnumeration<? extends Attribute> attrs = attributes.getAll(); attrs.hasMore();) {
                 Attribute attr = attrs.next();
     
                 PropertyMetaData propMetaData = metaData.getProperty(attr.getID());
                 if (null == propMetaData) {
                     // current attribute exist in LDAP but not defined in our
                     // POJO.
                     continue;
                 }
     
                 if (propMetaData.isId()) {
                     propMetaData.setter().set(entity, attr.get());
                     if (entity instanceof Distinguishable) {
                         ((Distinguishable)entity).setDN(attr.getID() + "=" + attr.get().toString() + "," + metaData.context());
                     }
                     
                 } else {
                     List<String> attrValues = new ArrayList<String>();
                     for (NamingEnumeration<?> all = attr.getAll(); all.hasMore();) {
                         /*
                          * TODO Currently, we treat all attribute values as
                          * string. But once there are any binary data, we need
                          * to fulfill that situation.
                          */
                         attrValues.add(all.next().toString());
                     }
     
                     if (!propMetaData.isReference()) {
                         if (!propMetaData.isMultiple()) {
                             propMetaData.setter().set(entity, attrValues.get(0));
                         } else {
                             propMetaData.setter().set(entity, new MoniteredList<String>(attrValues));
                             multipleLdapAttrNames.remove(propMetaData.getLdapPropName());
                         }
     
                     } else {
                         final Class<?> referenceType = propMetaData.getValueClass();
                         if (!propMetaData.isMultiple()) {
                             propMetaData.setter().set(entity, ProxyFactory.getLazyLoadingProxiedEntity(referenceType, attrValues.get(0)));
     
                         } else {
                             List references = new ArrayList();
                             for (String dn : attrValues) {
                                 references.add(ProxyFactory.getLazyLoadingProxiedEntity(referenceType, dn));
                             }
                             propMetaData.setter().set(entity, new MoniteredList(references));
                             multipleLdapAttrNames.remove(propMetaData.getLdapPropName());
                         }
                     }
                 }
                 
                 /*
                  * The rest attribute names in multipleLdapAttrNames are those
                  * not presented in LDAP side. In order to track what changes
                  * occurred to these attributes, we need to use MoniteredList.
                  */
                 for (String notPresentedMultipleLdapAttrName : multipleLdapAttrNames) {
                     metaData.getProperty(notPresentedMultipleLdapAttrName).setter().set(entity, new MoniteredList());
                 }
             }
             
             /*
              * Once all the properties have been initialized, we should turn on
              * the switch of SetterInterceptor to monitor changes.
              */
             pair.getValue().turnOn();
             return entity;
         
         } catch (NamingException e) {
             LogUtils.debug(LOG, "failed to go through attributes");
             throw e;
         }
     }
     
     private Map<String, Object> fromAttributesToMap(Class<?> clazz, Attributes attributes) throws NamingException {
         try {
             Map<String, Object> map = new HashMap<String, Object>();
             EntityMetaData metaData = EntityMetaData.get(clazz);
             for (NamingEnumeration<? extends Attribute> attrs = attributes.getAll(); attrs.hasMore();) {
                 Attribute attr = attrs.next();
                 
                 PropertyMetaData propMetaData = metaData.getProperty(attr.getID());
                 if (null == propMetaData) {
                     continue;
                 }
                 
                 List<String> attrValues = new ArrayList<String>();
                 for (NamingEnumeration<?> all = attr.getAll(); all.hasMore();) {
                     attrValues.add(all.next().toString());
                 }
                 
                 if (!propMetaData.isMultiple()) {
                     map.put(attr.getID(), attrValues.get(0));
                 } else {
                     map.put(attr.getID(), attrValues);
                 }
             }
             
             return map;
             
         } catch (NamingException e) {
             LogUtils.debug(LOG, "failed to go through attributes");
             throw e;
         }
     }
     
     private static final Evaluator<String> DN_EVALUATOR = new Evaluator<String>() {
 
         @Override
         public String eval(Object obj) {
             return DnHelper.build(obj);
         }
 
     };
     
     /**
      * An interface which marks an object as persistent. Client code should not
      * modeling any class via this interface as it will be the process of
      * {@link Session}.
      */
     public interface Persistent {
     }
     
     /**
      * A list which provides additional features for monitoring element
      * additions and removals.
      */
     @SuppressWarnings("serial")
     private static class MoniteredList<E> extends ArrayList<E> {
         
         private List<E> added = new ArrayList<E>();
         private List<E> removed = new ArrayList<E>();
         
         public MoniteredList() {
             super();
         }
         
         public MoniteredList(Collection<? extends E> c) {
             super(c);
         }
         
         @Override
         public boolean add(E e) {
             boolean retVal = super.add(e);
             added.add(e);
             return retVal;
         }
         
         @Override
         public void add(int index, E element) {
             super.add(index, element);
             added.add(element);
         }
         
         @Override
         public boolean addAll(Collection<? extends E> c) {
             boolean retVal = super.addAll(c);
             added.addAll(c);
             return retVal;
         }
         
         @Override
         public boolean addAll(int index, Collection<? extends E> c) {
             boolean retVal = super.addAll(index, c);
             added.addAll(c);
             return retVal;
         }
         
         @Override
         public E remove(int index) {
             E retVal = super.remove(index);
             removed.add(retVal);
             return retVal;
         }
         
         @SuppressWarnings("unchecked")
         @Override
         public boolean remove(Object o) {
             boolean retVal = super.remove(o);
             removed.add((E) o);
             return retVal;
         }
         
         @SuppressWarnings("unchecked")
         @Override
         public boolean removeAll(Collection<?> c) {
             boolean retVal = super.removeAll(c);
             removed.addAll((Collection<? extends E>) c);
             return retVal;
         }
         
         public List<E> getAddedElements() {
             return new ArrayList<E>(added);
         }
         
         public List<E> getRemovedElements() {
             return new ArrayList<E>(removed);
         }
         
         /**
          * This method will be called once an entity has been updated
          * successfully. The changes must be cleared because we want to reuse
          * the entity this list belongs to. The reason is, if not cleared, the
          * changes will be gathered the next time when updating.
          */
         public void clearChanges() {
             added.clear();
             removed.clear();
         }
         
     }
 
     
 }
