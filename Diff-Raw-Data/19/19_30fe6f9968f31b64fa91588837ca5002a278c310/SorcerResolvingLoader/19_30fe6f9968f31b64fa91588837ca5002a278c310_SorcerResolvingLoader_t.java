 package sorcer.rio.rmi;
 
 import org.rioproject.resolver.RemoteRepository;
 import org.rioproject.resolver.Resolver;
 import org.rioproject.resolver.ResolverException;
 import org.rioproject.resolver.ResolverHelper;
 import org.rioproject.rmi.ResolvingLoader;
 import org.rioproject.url.artifact.ArtifactURLConfiguration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.lang.reflect.Field;
 import java.net.MalformedURLException;
 import java.rmi.server.RMIClassLoader;
 import java.rmi.server.RMIClassLoaderSpi;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * SORCER class
  * User: prubach
  * Date: 13.03.14
  */
 public class SorcerResolvingLoader extends ResolvingLoader {
     /**
      * A table of artifacts to derived codebases. This improves performance by resolving the classpath once per
      * artifact.
      */
     private final Map<String, String> artifactToCodebase = new ConcurrentHashMap<String, String>();
     /**
      * A table of classes to artifact: codebase. This will ensure that if the annotation is requested for a class that
      * has it's classpath resolved from an artifact, that the artifact URL is passed back instead of the resolved
      * (local) classpath.
      */
     private final Map<String, String> classAnnotationMap = new ConcurrentHashMap<String, String>();
     private static final Resolver resolver;
     private static final Logger logger = LoggerFactory.getLogger(ResolvingLoader.class.getName());
     static {
         try {
             resolver = ResolverHelper.getResolver();
         } catch (ResolverException e) {
             throw new RuntimeException(e);
         }
     }
     private static final RMIClassLoaderSpi loader = RMIClassLoader.getDefaultProviderInstance();
 
     @Override
     public Class<?> loadClass(final String codebase,
                               final String name,
                               final ClassLoader defaultLoader) throws MalformedURLException, ClassNotFoundException {
         if(logger.isTraceEnabled()) {
             logger.trace("codebase: {}, name: {}, defaultLoader: {}",
                     codebase, name, defaultLoader==null?"NULL":defaultLoader.getClass().getName());
         }
         String resolvedCodebase = resolveCodebase(codebase);
         if(codebase!=null && codebase.startsWith("artifact:") && classAnnotationMap.get(name)==null) {
             classAnnotationMap.put(name, codebase);
             logger.trace("class: {}, codebase: {}, size now {}", name, codebase, classAnnotationMap.size());
         }
         logger.trace("Load class {} using codebase {}, resolved to {}", name, codebase, resolvedCodebase);
         return loader.loadClass(resolvedCodebase, name, defaultLoader);
     }
 
     @Override
     public Class<?> loadProxyClass(final String codebase,
                                    final String[] interfaces,
                                    final ClassLoader defaultLoader) throws MalformedURLException, ClassNotFoundException {
         if(logger.isTraceEnabled()) {
             logger.trace("codebase: {}, interfaces: {}, defaultLoader: {}",
                     codebase, Arrays.toString(interfaces), defaultLoader==null?"NULL":defaultLoader.getClass().getName());
         }
         String resolvedCodebase = resolveCodebase(codebase);
         if(logger.isTraceEnabled()) {
             StringBuilder builder = new StringBuilder();
             for(String s : interfaces) {
                 if(builder.length()>0) {
                     builder.append(" ");
                 }
                 builder.append(s);
             }
             logger.trace("Load proxy classes {} using codebase {}, resolved to {}, defaultLoader: {}",
                     builder.toString(), codebase, resolvedCodebase, defaultLoader);
         }
         return loader.loadProxyClass(resolvedCodebase, interfaces, defaultLoader);
     }
 
     @Override
     public ClassLoader getClassLoader(String codebase) throws MalformedURLException {
         if(logger.isTraceEnabled()) {
             logger.trace("codebase: {}", codebase);
         }
         String resolvedCodebase = resolveCodebase(codebase);
         return loader.getClassLoader(resolvedCodebase);
     }
 
     @Override
     public String getClassAnnotation(final Class<?> aClass) {
         String annotation = classAnnotationMap.get(aClass.getName());
         if(annotation == null)
             annotation = loader.getClassAnnotation(aClass);
         return annotation;
     }
 
     private String resolveCodebase(final String codebase) {
         String adaptedCodebase;
         StringBuilder resolvedCodebaseBuilder = new StringBuilder();
         if(codebase!=null && codebase.startsWith("artifact:")) {
             String[] artifacts = codebase.split(" ");
             Set<String> jarsSet = new HashSet<String>();
             for (String artf: artifacts) {
                 adaptedCodebase = artifactToCodebase.get(artf);
                 if(adaptedCodebase==null) {
 
                     try {
                         logger.debug("Resolve {} ", artf);
                         StringBuilder builder = new StringBuilder();
                         String path =  artf.substring(artf.indexOf(":")+1);
                         ArtifactURLConfiguration artifactURLConfiguration = new ArtifactURLConfiguration(path);
                         String[] cp = null;
                        for (RemoteRepository rr : artifactURLConfiguration.getRepositories()) {
                            rr.setSnapshotChecksumPolicy(RemoteRepository.CHECKSUM_POLICY_IGNORE);
                            rr.setReleaseChecksumPolicy(RemoteRepository.CHECKSUM_POLICY_IGNORE);
                         }
                        cp = resolver.getClassPathFor(artifactURLConfiguration.getArtifact(),
                            artifactURLConfiguration.getRepositories());
                         for(String s : cp) {
                             if(builder.length()>0)
                                 builder.append(" ");
                             builder.append(new File(s).toURI().toURL().toExternalForm());
                         }
                         adaptedCodebase = builder.toString();
                         artifactToCodebase.put(artf, adaptedCodebase);
                     } catch (ResolverException e) {
                         logger.warn("Unable to resolve {}", artf);
                     } catch (MalformedURLException e) {
                         logger.warn("The codebase {} is malformed", artf, e);
                     }
                 }
                 if (adaptedCodebase!=null)
                     for (String jar : adaptedCodebase.split(" "))
                         jarsSet.add(jar);
             }
             for (String jar: jarsSet) {
                 if (resolvedCodebaseBuilder.length()>0)
                     resolvedCodebaseBuilder.append(" ");
                 resolvedCodebaseBuilder.append(jar);
             }
         } else {
             return codebase;
         }
         return resolvedCodebaseBuilder.toString();
     }
 }
