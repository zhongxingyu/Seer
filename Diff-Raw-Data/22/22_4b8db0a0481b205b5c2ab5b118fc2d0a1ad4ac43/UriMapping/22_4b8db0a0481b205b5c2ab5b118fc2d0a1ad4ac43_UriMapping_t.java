 package net.contextfw.web.application.internal.servlet;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import net.contextfw.web.application.PropertyProvider;
 import net.contextfw.web.application.WebApplicationException;
 import net.contextfw.web.application.component.Component;
 import net.contextfw.web.application.internal.initializer.InitializerProvider;
 import net.contextfw.web.application.internal.service.InitHandler;
 import net.contextfw.web.application.lifecycle.View;
 
 
 public class UriMapping implements Comparable<UriMapping> {
 
     private static final String REGEX = "regex:";
     
     private final String path;
     
     private final UriPatternMatcher matcher;
     
     private final Class<? extends Component> viewClass;
     
     private final InitServlet initServlet;
     
     public UriMapping(Class<? extends Component> viewClass, String path, InitServlet initServlet) {
         this.initServlet = initServlet;
         this.viewClass = viewClass;
         if (path.startsWith(REGEX)) {
             this.path = path.substring(REGEX.length());
             matcher = UriPatternType.get(UriPatternType.REGEX, this.path);
         } else {
             this.path = path;
             matcher = UriPatternType.get(UriPatternType.SERVLET, path);
         }
     }
     
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((matcher == null) ? 0 : matcher.hashCode());
         result = prime * result + ((path == null) ? 0 : path.hashCode());
         result = prime * result + ((viewClass == null) ? 0 : viewClass.hashCode());
         return result;
     }
     
     @Override
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         } else if (o instanceof UriMapping) {
             UriMapping other = (UriMapping) o;
             if (this.getMatcher().getPatternType() == other.getMatcher().getPatternType()) {
                 return this.getPath().equals(other.path);
             }
         }
         return false;
     }
     
     @Override
     public int compareTo(UriMapping other) {
         if (this.getMatcher().getPatternType() == UriPatternType.SERVLET 
                 && other.getMatcher().getPatternType() == UriPatternType.REGEX) {
             return -1;
         } else if (this.getMatcher().getPatternType() == UriPatternType.REGEX 
                 && other.getMatcher().getPatternType() == UriPatternType.SERVLET) {
             return 1;
         } else {
            String myUri =this.getMatcherUri();
            String otherUri = other.getMatcherUri();
            if (myUri == null && otherUri == null) {
                return 0;
            } else if (myUri == null && otherUri != null) {
                return 1;
            } else if (myUri != null && otherUri == null) {
                return -1;
            } else {
                return otherUri.compareTo(myUri);
            }
         }
     }
     
     private String getMatcherUri() {
        return matcher.extractPath(path);
     }
 
     public String getPath() {
         return path;
     }
 
     public UriPatternMatcher getMatcher() {
         return matcher;
     }
     
     @SuppressWarnings("unchecked")
     public static SortedSet<UriMapping> createMappings(
                 Collection<Class<?>> origClasses,
                 ClassLoader classLoader,
                 InitializerProvider initializerProvider,
                 InitHandler initHandler, 
                 PropertyProvider properties) {
         
         // Note: This process creates some phantom chains from 
         // views that do not have any url. Those chains are
         // however ingnored and are not such problem.
         
         SortedSet<UriMapping> mappings = new TreeSet<UriMapping>();
         
         try {
             for (Class<?> origClass : origClasses) {
                 Class<?> cl = classLoader.loadClass(origClass.getCanonicalName());
                 View annotation = cl.getAnnotation(View.class);
                 if (annotation != null) {
                     
                     if (!Component.class.isAssignableFrom(cl)) {
                         throw new WebApplicationException("Class " + cl.getName()
                                 + " annotated with @View does " +
                                 "not extend Component");
                     }
                     
                     List<Class<? extends Component>> chain = 
                         initializerProvider.getInitializerChain(cl.asSubclass(Component.class));
                     
                     InitServlet servlet = new InitServlet(initHandler, chain);
                     
                     for (String url : annotation.url()) {
                         if (!"".equals(url)) {
                             mappings.add(new UriMapping((Class<? extends Component>) cl, url, servlet));
                         }
                     }
                     
                     for (String property : annotation.property()) {
                         if (!"".equals(property)) {
                             if (!properties.get().containsKey(property)) {
                                 throw new WebApplicationException("No url bound to property: "
                                         + property);
                             }
                             
                             String url = properties.get().getProperty(property);
 
                             if (url != null && !"".equals(url)) {
                                 mappings.add(new UriMapping((Class<? extends Component>) cl, url, servlet));
                             } else {
                                 throw new WebApplicationException(
                                         "No url bound to view component. (class="
                                                 + cl.getSimpleName() + ", property=" + property
                                                 + ")");
                             }
                         }
                     }
                 }
             }
         } catch (ClassNotFoundException e) {
             throw new WebApplicationException(e);
         }
         
         return mappings;
     }
 
     public Class<? extends Component> getViewClass() {
         return viewClass;
     }
 
     public InitServlet getInitServlet() {
         return initServlet;
     }
     
     public UriPatternType getPatternType() {
         return matcher.getPatternType();
     }
 }
