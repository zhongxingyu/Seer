 package org.icefaces.impl.event;
 
 import org.icefaces.util.EnvUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.event.*;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.*;
 import java.util.logging.Logger;
 
 public class ResourceOrdering implements SystemEventListener {
     private final static Logger Log = Logger.getLogger(ResourceOrdering.class.getName());
     private HashMap<String, ResourceEntry> resourceMap = new HashMap<String, ResourceEntry>();
     private ArrayList<ResourceEntry> nonRootDependencies = new ArrayList<ResourceEntry>();
     private ArrayList<ResourceEntry> masterDependencyList = new ArrayList<ResourceEntry>();
 
     public ResourceOrdering() {
         try {
             //read all resource dependencies manifests
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = dbf.newDocumentBuilder();
 
             Enumeration<URL> urls = this.getClass().getClassLoader().getResources("META-INF/resource-dependency.xml");
             while (urls.hasMoreElements()) {
                 URL url = urls.nextElement();
                 try {
                    InputStream stream = (InputStream) url.getContent();
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
                             }
                         }
                     }
                 } catch (Exception e) {
                     Log.warning("Failed to process resource dependency metadata at " + url);
                 }
             }
 
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
         } catch (Exception e) {
             throw new RuntimeException(e);
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
             orderResources(context, root, "head");
             orderResources(context, root, "body");
         }
     }
 
     private void orderResources(FacesContext context, UIViewRoot root, String target) {
         String facetName = EnvUtils.isMojarra() ? "javax_faces_location_" + target.toUpperCase() : target;
         UIComponent resourceContainer = root.getFacets().get(facetName);
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
         orderedChildren.addAll(resourceContainer.getChildren());
 
         for (UIComponent componentResource : orderedChildren) {
             root.addComponentResource(context, componentResource, target);
         }
 
         //restore reource container to non transient state
         resourceContainer.setInView(true);
     }
 
     public boolean isListenerForSource(final Object source) {
         return EnvUtils.isICEfacesView(FacesContext.getCurrentInstance()) && (source instanceof UIViewRoot);
     }
 
     private static class ResourceEntry {
         private String name;
         private String library;
         private String target;
         private List<ResourceEntry> dependants = new ArrayList<ResourceEntry>();
 
 
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
