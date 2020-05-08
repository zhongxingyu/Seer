package com.coldhardcode.scatter;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import javax.servlet.ServletContext;
 import com.impetus.annovention.Discoverer;
 import com.impetus.annovention.Filter;
 import com.impetus.annovention.FilterImpl;
 
 /**
  * The Class ClasspathReader.
  * 
  * @author animesh.kumar
  */
 public class WebClasspathDiscoverer extends Discoverer {
 
         /** The filter. */
         private Filter filter;
 
         private ServletContext context;
 
         /**
          * Instantiates a new classpath reader.
          */
         public WebClasspathDiscoverer(ServletContext context) {
                 filter = new FilterImpl();
                 this.context = context;
         }
 
         /**
          * Uses java.class.path system-property to fetch URLs
          * 
          * @return the URL[]
          */
         @SuppressWarnings("deprecation")
         @Override
         public final URL[] findResources() {
                 List<URL> list = new ArrayList<URL>();
                 Set resources = this.context.getResourcePaths("/");
                 Iterator<String> resIter = resources.iterator();
                 while(resIter.hasNext()) {
                         String path = resIter.next();
 
                         try {
                                 System.out.println("Adding " + path);
                                 list.add(this.context.getResource(path));
                         } catch (MalformedURLException e) {
                                 throw new RuntimeException(e);
                         }
                 }
                 return list.toArray(new URL[list.size()]);
         }
 
         /* @see com.impetus.annovention.Discoverer#getFilter() */
         public final Filter getFilter() {
                 return filter;
         }
 
         /**
          * @param filter
          */
         public final void setFilter(Filter filter) {
                 this.filter = filter;
         }
 }
