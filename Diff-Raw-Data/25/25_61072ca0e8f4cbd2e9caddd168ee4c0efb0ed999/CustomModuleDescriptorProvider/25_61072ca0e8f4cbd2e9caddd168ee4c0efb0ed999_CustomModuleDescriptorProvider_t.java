 package org.amplafi.hivemind.util;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 
 import java.util.regex.Pattern;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hivemind.ApplicationRuntimeException;
 import org.apache.hivemind.ClassResolver;
 import org.apache.hivemind.ErrorHandler;
 import org.apache.hivemind.HiveMind;
 import org.apache.hivemind.ModuleDescriptorProvider;
 import org.apache.hivemind.Resource;
 import org.apache.hivemind.parse.ModuleDescriptor;
 import org.apache.hivemind.parse.SubModuleDescriptor;
 import org.apache.hivemind.parse.XmlResourceProcessor;
 import org.apache.hivemind.util.URLResource;
 
 /**
  * Implementation of the {@link ModuleDescriptorProvider} interface which uses the
  * {@link org.apache.hivemind.parse.DescriptorParser} to provide module descriptors defined in XML.
  * The module descriptors are loaded from files or resources on the classpath.
  *
  * Allows defining rules that will skip hivemind files (by name pattern or file location [jar, file]).
  * Also allows including submodules form classpath locations by using the classpath:// prefix.
  *
  * @author Knut Wannheden
  * @author Andreas Androeu
  */
 public class CustomModuleDescriptorProvider implements ModuleDescriptorProvider
 {
     /**
      * magic prefix to see files in the class path.
      */
     private static final String CLASSPATH = "classpath://";
 
     private static final Log LOG = LogFactory.getLog(CustomModuleDescriptorProvider.class);
 
     /**
      * The default path, within a JAR or the classpath, to the XML HiveMind module deployment
      * descriptor: <code>META-INF/hivemodule.xml</code>. Use this constant with the
      * {@link #CustomModuleDescriptorProvider(ClassResolver, String)} constructor.
      */
     public static final String HIVE_MODULE_XML = "META-INF/hivemodule.xml";
 
     /**
      * Set of all specified resources processed by this ModuleDescriptorProvider. Descriptors of
      * sub-modules are not included.
      */
     private List<Resource> _resources = new ArrayList<Resource>();
 
     /**
      * List of parsed {@link ModuleDescriptor} instances. Also includes referenced sub-modules.
      */
     private List _moduleDescriptors = new ArrayList();
 
     private ClassResolver _resolver;
 
     private ErrorHandler _errorHandler;
 
     /**
      * Parser instance used by all parsing of module descriptors.
      */
     private XmlResourceProcessor _processor;
 
     private boolean excludeFiles;
 
     private boolean excludeJars;
 
     private String excludePattern;
 
     /**
      * Convenience constructor. Equivalent to using
      * {@link #CustomModuleDescriptorProvider(ClassResolver, String)}with {@link #HIVE_MODULE_XML} as
      * the second argument.
      */
     public CustomModuleDescriptorProvider(ClassResolver resolver) {
         this(resolver, HIVE_MODULE_XML);
     }
 
     public CustomModuleDescriptorProvider(ClassResolver resolver, String excludePattern,
             boolean excludeFiles, boolean excludeJars) {
         this(resolver, HIVE_MODULE_XML, excludePattern, excludeFiles, excludeJars);
     }
 
     /**
      * Loads all XML module descriptors found on the classpath (using the given
      * {@link org.apache.hivemind.ClassResolver}. Only module descriptors matching the specified
      * path are loaded. Use the {@link CustomModuleDescriptorProvider#HIVE_MODULE_XML} constant to load
      * all descriptors in the default location.
      */
     public CustomModuleDescriptorProvider(ClassResolver resolver, String resourcePath) {
         this(resolver, resourcePath, null, false, false);
     }
 
     public CustomModuleDescriptorProvider(ClassResolver resolver, String resourcePath,
             String excludePattern, boolean excludeFiles, boolean excludeJars) {
         _resolver = resolver;
         this.excludePattern = excludePattern;
         this.excludeFiles = excludeFiles;
         this.excludeJars = excludeJars;
 
         _resources.addAll(getDescriptorResources(resourcePath, _resolver));
     }
 
     /**
      * Constructs an XmlModuleDescriptorProvider only loading the ModuleDescriptor identified by the
      * given {@link org.apache.hivemind.Resource}.
      */
     public CustomModuleDescriptorProvider(ClassResolver resolver, Resource resource)
     {
         _resolver = resolver;
         _resources.add(resource);
     }
 
     /**
      * Constructs an XmlModuleDescriptorProvider loading all ModuleDescriptor identified by the
      * given List of {@link org.apache.hivemind.Resource} objects.
      */
     public CustomModuleDescriptorProvider(ClassResolver resolver, List resources)
     {
         _resolver = resolver;
         _resources.addAll(resources);
     }
 
     private List<Resource> getDescriptorResources(String resourcePath, ClassResolver resolver)
     {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Processing modules visible to " + resolver);
         }
 
         List<Resource> descriptors = new ArrayList<Resource>();
 
         ClassLoader loader = resolver.getClassLoader();
         Enumeration<URL> urls;
 
         try
         {
             urls = loader.getResources(resourcePath);
         }
         catch (IOException ex)
         {
             throw new ApplicationRuntimeException("UnableToFindModules(" + resolver + ", " + ex + ")",
                     ex);
         }
 
         Pattern pattern = excludePattern==null ? null : Pattern.compile(excludePattern);
 
         while (urls.hasMoreElements())
         {
             URL descriptorURL = urls.nextElement();
 
             String protocol = descriptorURL.getProtocol();
 
             if (excludeFiles && protocol.equals("file")) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("ExcludeFiles therefore excluding " + descriptorURL);
                 }
                 continue;
             }
 
             if (excludeJars && protocol.equals("jar")) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("ExcludeJars therefore excluding " + descriptorURL);
                 }
                 continue;
             }
 
             if (pattern!=null && pattern.matcher(descriptorURL.toString()).matches()) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("ExcludePattern therefore excluding " + descriptorURL);
                 }
                 continue;
             }
 
            LOG.debug("Will process hivemind main file: " + descriptorURL);
             descriptors.add(new URLResource(descriptorURL));
         }
 
         return descriptors;
     }
 
     public List getModuleDescriptors(ErrorHandler handler)
     {
         _errorHandler = handler;
 
         _processor = getResourceProcessor(_resolver, handler);
 
         for (Object element : _resources) {
             Resource resource = (Resource) element;
 
             processResource(resource);
         }
 
         _processor = null;
 
         _errorHandler = null;
 
         return _moduleDescriptors;
     }
 
     private void processResource(Resource resource)
     {
         try
         {
             ModuleDescriptor md = _processor.processResource(resource);
 
             _moduleDescriptors.add(md);
 
             // After parsing a module, parse any additional modules identified
             // within the module (using the <sub-module> element) recursively.
             processSubModules(md);
         }
         catch (RuntimeException ex)
         {
             _errorHandler.error(LOG, ex.getMessage(), HiveMind.getLocation(ex), ex);
         }
     }
 
     private void processSubModules(ModuleDescriptor moduleDescriptor)
     {
         List subModules = moduleDescriptor.getSubModules();
 
         if (subModules == null) {
            LOG.debug("No submodules in "+moduleDescriptor);
             return;
         }
        LOG.debug("Beginning processing submodules in "+moduleDescriptor);
 
         for (Iterator i = subModules.iterator(); i.hasNext();)
         {
             SubModuleDescriptor smd = (SubModuleDescriptor) i.next();
 
             Resource descriptorResource = smd.getDescriptor();
 
             String path = descriptorResource.getPath();
 
             int classpathPos = path.indexOf(CLASSPATH);
             if (classpathPos>=0) {
                 // java's current directory is prepended to "classpath://" so that needs to be stripped off as well
                 // i.e. Users/patmoore/projects/amplafi-foundation/target/test-classes/classpath://amplafi-mock.tapestry4.xml
                 path = path.substring(classpathPos + CLASSPATH.length());
                 List<Resource> descriptorResources = getDescriptorResources(path, _resolver);
                 if ( descriptorResources== null || descriptorResources.isEmpty()) {
                     _errorHandler.error(
                         LOG, "classpathCannotFindsubModule:"+ path,
                         smd.getLocation(),
                         null);
                     continue;
                 } else if ( descriptorResources.size() > 1) {
                     _errorHandler.error(LOG, "WARNING:"+path+" multiple locations found "+descriptorResources, smd.getLocation(), null);
                 }
                 descriptorResource = descriptorResources.get(0);
             }
 
             if (descriptorResource.getResourceURL() == null)
             {
                 _errorHandler.error(
                         LOG,
                         "subModuleDoesNotExist:" + descriptorResource,
                         smd.getLocation(),
                         null);
                 continue;
             }
            LOG.debug("processing submodule "+descriptorResource);
             processResource(descriptorResource);
         }
        LOG.debug("Completed processing submodules in "+moduleDescriptor);
     }
 
     protected XmlResourceProcessor getResourceProcessor(ClassResolver resolver, ErrorHandler handler)
     {
         return new XmlResourceProcessor(resolver, handler);
     }
 
     @Override
     public String toString() {
         return super.toString() + "[excludeJars="+ excludeJars+ "; excludeFiles="+excludeFiles+"; excludePattern="+excludePattern+"]";
     }
 }
