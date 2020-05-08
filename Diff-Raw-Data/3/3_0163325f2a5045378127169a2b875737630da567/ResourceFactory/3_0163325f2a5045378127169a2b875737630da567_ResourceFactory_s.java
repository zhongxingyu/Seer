 /**
  * 
  */
 package de.saumya.gwt.datamapper.client;
 
 import java.util.HashMap;
 import java.util.Map;
 
import com.google.gwt.core.client.GWT;
 import com.google.gwt.xml.client.Element;
 import com.google.gwt.xml.client.Node;
 import com.google.gwt.xml.client.NodeList;
 
 import de.saumya.gwt.datamapper.client.Resource.State;
 
 public abstract class ResourceFactory<E extends Resource<E>> {
 
     private final Map<String, E> cache = new HashMap<String, E>();
 
     protected final Repository   repository;
 
     public ResourceFactory(final Repository repository) {
         this.repository = repository;
     }
 
     abstract public String storageName();
 
     public String storagePluralName() {
         return storageName() + "s";
     }
 
     abstract public E newResource();
 
     String getString(final Element root, final String name) {
         if (root == null) {
             return null;
         }
         final NodeList list = root.getElementsByTagName(name);
         for (int i = 0; i < list.getLength(); i++) {
             final Node node = list.item(i);
             if (node.getParentNode().equals(root)) {
                 return node == null || node.getFirstChild() == null
                         ? null
                         : node.getFirstChild().getNodeValue();
             }
         }
         return null;
     }
 
     void putIntoCache(final E resource) {
         if (resource.key() != null) {
             if (this.cache.containsKey(resource.key())) {
                 throw new IllegalStateException("just created resource already in cache: "
                         + resource);
             }
             this.cache.put(resource.key(), resource);
         }
         else {
             this.singleton = resource;
         }
     }
 
     void removeFromCache(final E resource) {
         if (resource.key() != null) {
             this.cache.remove(resource.key());
         }
     }
 
     void clearCache() {
         this.singleton = null;
         this.cache.clear();
     }
 
     E getResource(final Element root) {
         return getResource(keyFromXml(root));
     }
 
     private E singleton;
 
     E getResource() {
         if (this.singleton == null) {
             this.singleton = newResource();
         }
         return this.singleton;
     }
 
     E getResource(final String key) {
        GWT.log(key + " = " + this.cache.toString(), null);
         if (key == null) {
             return newResource();
         }
         else {
             E result = this.cache.get(key);
             if (result == null) {
                 result = newResource();
                 this.cache.put(key, result);
             }
             return result;
         }
     }
 
     public E getChildResource(final Element root, final String name) {
         final Element element = (Element) root.getElementsByTagName(name)
                 .item(0);
         if (element == null) {
             return null;
         }
         final E resource = getResource(keyFromXml(element));
         resource.fromXml(element);
         return resource;
     }
 
     public abstract String keyName();
 
     private String keyFromXml(final Element root) {
         return keyName() == null ? null : getString(root, keyName());
     }
 
     public Resources<E> getChildResources(final Element root, final String name) {
         final Element element = (Element) root.getElementsByTagName(name)
                 .item(0);
         final Resources<E> resources = newResources();
         if (element != null) {
             resources.fromXml(element);
         }
         return resources;
     }
 
     public Resources<E> newResources() {
         return new Resources<E>(this);
     }
 
     public E get(final int key, final ResourceChangeListener<E> listener) {
         return get("" + key, listener);
     }
 
     public E get(final ResourceChangeListener<E> listener) {
         final E resource = getResource();
         resource.state = State.TO_BE_LOADED;
         resource.addResourceChangeListener(listener);
         this.repository.get(storageName(),
                             new ResourceRequestCallback<E>(resource, this));
         return resource;
     }
 
     public E get(final String key, final ResourceChangeListener<E> listener) {
         final E resource = getResource(key);
         resource.state = State.TO_BE_LOADED;
         resource.addResourceChangeListener(listener);
         this.repository.get(storageName(),
                             key,
                             new ResourceRequestCallback<E>(resource, this));
         return resource;
     }
 
     public Resources<E> all(final ResourcesChangeListener<E> listener) {
         final Resources<E> list = new Resources<E>(this);
         list.addResourcesChangeListener(listener);
         this.repository.all(storageName(),
                             new ResourceListRequestCallback(list));
         return list;
     }
 }
