 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.impl.event;
 
 import org.icefaces.util.EnvUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.faces.application.ResourceHandler;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIOutput;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.*;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class ResourceOrdering implements SystemEventListener {
     private final static Logger Log = Logger.getLogger(ResourceOrdering.class.getName());
     private HashMap<String, ResourceEntry> resourceMap = new HashMap<String, ResourceEntry>();
     private ArrayList<ResourceEntry> nonRootDependencies = new ArrayList<ResourceEntry>();
     private ArrayList<ResourceEntry> masterDependencyList = new ArrayList<ResourceEntry>();
 
     public ResourceOrdering() {
         try {
             //Read all resource dependencies manifests that appear on the classpath.  This
             //typically means all jars containing a META-INF/resource-dependency.xml file.
             Enumeration<URL> urls = this.getClass().getClassLoader().getResources("META-INF/resource-dependency.xml");
             List<URL> urlList = Collections.list(urls);
 
             //For the application .war itself, any META-INF/resource-dependency.xml file is
             //not actually on the classpath so won't be picked up using the above method.  For these
             //files, we need the ServletContext.getResource() method.
             FacesContext fc = FacesContext.getCurrentInstance();
             if (fc != null) {
                 ExternalContext ec = fc.getExternalContext();
                 URL warResourceDependencyURL = ec.getResource("/META-INF/resource-dependency.xml");
                 if (warResourceDependencyURL != null) {
                     urlList.add(warResourceDependencyURL);
                 }
             }
 
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = dbf.newDocumentBuilder();
 
             processResourceDependencies(db, urlList);
 
             //traverse dependency tree
             List<ResourceEntry> roots = new ArrayList<ResourceEntry>(resourceMap.values());
             roots.removeAll(nonRootDependencies);
             LinkedList<ResourceEntry> queue = new LinkedList<ResourceEntry>();
             queue.addAll(roots);
 
             while (!queue.isEmpty()) {
                 ResourceEntry entry = queue.removeFirst();
                 queue.addAll(entry.getDependants());
                 if (masterDependencyList.contains(entry)) {
                     masterDependencyList.remove(entry);
                 }
                 masterDependencyList.add(entry);
             }
 
             fc.getExternalContext().getApplicationMap().put(ResourceOrdering.class.getName(), this);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     public interface ResourceIterator {
         void resource(String name, String library, String target);
     }
 
     public void traverseOrderedResources(ResourceIterator iterator) {
         Iterator i = masterDependencyList.iterator();
         while (i.hasNext()) {
             ResourceEntry next = (ResourceEntry) i.next();
             iterator.resource(next.name, next.library, next.target);
         }
     }
 
     private void processResourceDependencies(DocumentBuilder db, List<URL> urls) {
         Log.log(Level.FINE, "resource-dependency.xml URLs: " + urls);
         for (int index = 0; index < urls.size(); index++) {
             URL url = urls.get(index);
             try {
                 InputStream stream = url.openStream();
                 Document doc = db.parse(stream);
                 doc.normalizeDocument();
 
                 //read jar's dependency declarations
                 NodeList resourceElements = doc.getDocumentElement().getChildNodes();
                 for (int i = 0, l = resourceElements.getLength(); i < l; i++) {
                     Node node = resourceElements.item(i);
                     if (node instanceof Element) {
                         Element resourceElement = (Element) node;
                         String name = resourceElement.getAttribute("name");
                         String library = normalizeLibraryName(resourceElement.getAttribute("library"));
                         String target = normalizeTargetName(resourceElement.getAttribute("target"));
                         ResourceEntry sourceResourceEntry = lookupOrCreateResource(name, library, target);
 
                         nonRootDependencies.add(sourceResourceEntry);
 
                         NodeList dependencies = resourceElement.getElementsByTagName("resource");
                         for (int j = 0, ll = dependencies.getLength(); j < ll; j++) {
                             Element dependOnResourceElement = (Element) dependencies.item(j);
                             String dependencyName = dependOnResourceElement.getAttribute("name");
                             String dependencyLibrary = normalizeLibraryName(dependOnResourceElement.getAttribute("library"));
                             String dependencyTarget = normalizeTargetName(dependOnResourceElement.getAttribute("target"));
 
                             ResourceEntry targetResourceEntry = lookupOrCreateResource(dependencyName, dependencyLibrary, dependencyTarget);
                             targetResourceEntry.addDependant(sourceResourceEntry);
                             sourceResourceEntry.addDependency(targetResourceEntry);
                         }
                     }
                 }
             } catch (Exception e) {
                 Log.warning("Failed to process resource dependency metadata at " + url);
             }
         }
     }
 
     private ResourceEntry lookupOrCreateResource(String name, String library, String target) {
         String key = ResourceEntry.key(name, library, target);
         ResourceEntry entry = resourceMap.get(key);
         if (entry == null) {
             entry = new ResourceEntry(name, library, target);
             resourceMap.put(key, entry);
             return entry;
         } else {
             return entry;
         }
     }
 
     public void processEvent(SystemEvent event) throws AbortProcessingException {
         if (event.getSource() instanceof UIViewRoot) {
             FacesContext context = FacesContext.getCurrentInstance();
             UIViewRoot root = (UIViewRoot) event.getSource();
             collectTransitiveDependencies(context, root, "head");
             collectTransitiveDependencies(context, root, "body");
             orderResources(context, root, "head");
             orderResources(context, root, "body");
         }
     }
 
     private void collectTransitiveDependencies(FacesContext context, UIViewRoot root, String target) {
         UIComponent resourceContainer = getResourceContainer(root, target);
         HashSet<ResourceEntry> collectedResourceEntries = new HashSet<ResourceEntry>();
         HashSet<ResourceEntry> currentResourceEntries = new HashSet<ResourceEntry>();
         List children = resourceContainer.getChildren();
 
         //iterate over the added resources (through annotation) and collect the transitive dependencies for each one
         for (UIComponent next : new ArrayList<UIComponent>(children)) {
             Map attributes = next.getAttributes();
             String name = (String) attributes.get("name");
             String library = normalizeLibraryName((String) attributes.get("library"));
            ResourceEntry entry = resourceMap.get(ResourceEntry.key(name, library, target));
             currentResourceEntries.add(entry);
             LinkedList<ResourceEntry> queue = new LinkedList();
             queue.add(entry);
             //traverse the transitive dependencies
             while (!queue.isEmpty()) {
                 ResourceEntry e = queue.removeFirst();
                 if (e != null) {
                     List<ResourceEntry> dependencies = e.getDependencies();
                     queue.addAll(dependencies);
                     //add the found resources to the set
                     collectedResourceEntries.addAll(dependencies);
                 }
             }
         }
         //avoid adding entries that are already present in the resource container
         collectedResourceEntries.removeAll(currentResourceEntries);
         //make resource containers transient so that the removal and addition of resource is not track by the JSF state saving
         resourceContainer.setInView(false);
         //add corresponding UIOutput components for the collected resources
         for (ResourceEntry next: collectedResourceEntries) {
             if (target.equals(next.target)) {
                 UIOutput c = new UIOutput();
                 String rendererType = context.getApplication().getResourceHandler().getRendererTypeForResourceName(next.name);
                 c.setTransient(true);
                 c.setRendererType(rendererType);
                 Map attributes = c.getAttributes();
                 attributes.put("name", next.name);
                 attributes.put("library", next.library);
                 attributes.put("version", "fubar");
 
                 root.addComponentResource(context, c, next.target);
             }
         }
         //restore resource container to non transient state
         resourceContainer.setInView(true);
     }
 
     private void orderResources(FacesContext context, UIViewRoot root, String target) {
         UIComponent resourceContainer = getResourceContainer(root, target);
         //make resource containers transient so that the removal and addition of resource is not track by the JSF state saving
         resourceContainer.setInView(false);
 
         ArrayList<UIComponent> orderedChildren = new ArrayList();
 
         for (ResourceEntry resourceEntry : masterDependencyList) {
             List children = resourceContainer.getChildren();
             for (UIComponent next : new ArrayList<UIComponent>(children)) {
                 Map attributes = next.getAttributes();
                 String name = (String) attributes.get("name");
                 String library = normalizeLibraryName((String) attributes.get("library"));
 
                 if (resourceEntry.name.equals(name) && resourceEntry.library.equals(library)) {
                     root.removeComponentResource(context, next, target);
                     orderedChildren.add(next);
                 }
             }
         }
         //append the rest of the components that do not have dependency data
         List<UIComponent> remainingChildren = new ArrayList<UIComponent>(resourceContainer.getChildren());
         for (UIComponent next: remainingChildren) {
             root.removeComponentResource(context, next, target);
             orderedChildren.add(next);
         }
 
         for (UIComponent componentResource : orderedChildren) {
             root.addComponentResource(context, componentResource, target);
         }
 
         //restore resource container to non transient state
         resourceContainer.setInView(true);
     }
 
     private static UIComponent getResourceContainer(UIViewRoot root, String target) {
         String facetName = EnvUtils.isMojarra() ? "javax_faces_location_" + target.toUpperCase() : target;
         return root.getFacets().get(facetName);
     }
 
     public boolean isListenerForSource(final Object source) {
         return EnvUtils.isICEfacesView(FacesContext.getCurrentInstance()) && (source instanceof UIViewRoot);
     }
 
     private static class ResourceEntry {
         private String name;
         private String library;
         private String target;
         private List<ResourceEntry> dependants = new ArrayList<ResourceEntry>();
         private List<ResourceEntry> dependencies = new ArrayList<ResourceEntry>();
 
         private ResourceEntry(String name, String library, String target) {
             this.name = name;
             this.library = library;
             this.target = target;
         }
 
         public void addDependant(ResourceEntry entry) {
             dependants.add(entry);
         }
 
         public List<ResourceEntry> getDependants() {
             return dependants;
         }
 
         public void addDependency(ResourceEntry entry) {
             dependencies.add(entry);
         }
 
         public List<ResourceEntry> getDependencies() {
             return dependencies;
         }
 
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             ResourceEntry that = (ResourceEntry) o;
 
             if (library != null ? !library.equals(that.library) : that.library != null) return false;
             if (name != null ? !name.equals(that.name) : that.name != null) return false;
             if (target != null ? !target.equals(that.target) : that.target != null) return false;
 
             return true;
         }
 
         public int hashCode() {
             int result = name != null ? name.hashCode() : 0;
             result = 31 * result + (library != null ? library.hashCode() : 0);
             result = 31 * result + (target != null ? target.hashCode() : 0);
             return result;
         }
 
         public static String key(String name, String library, String target) {
             return name + library + target;
         }
 
         public String toString() {
             return "Resource{name: " + name + ", library: " + library + ", target: " + target + "}";
         }
     }
 
     //register ResourceOrdering dynamically to make sure it is invoked last when PreRenderComponentEvent is fired
     public static class RegisterListener implements SystemEventListener {
         public void processEvent(SystemEvent event) throws AbortProcessingException {
             FacesContext context = FacesContext.getCurrentInstance();
             context.getApplication().subscribeToEvent(PreRenderComponentEvent.class, new ResourceOrdering());
         }
 
         public boolean isListenerForSource(Object source) {
             return true;
         }
     }
 
     private static String normalizeLibraryName(String name) {
         return name == null ? "" : name;
     }
 
     private static String normalizeTargetName(String name) {
         return name == null || "".equals(name) ? "head" : name;
     }
 }
