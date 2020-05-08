 package com.my.plugin.app;
 
 import com.my.plugin.Plugin;
 import org.eclipse.aether.resolution.ArtifactResult;
 
 import java.io.File;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.List;
 
 import static me.smecsia.common.utils.ReflectUtil.classLoaderProxy;
 
 public class App {
 
     public static void main(String[] args) throws Exception {
         DependencyResolver resolver = new DependencyResolver(new File(System.getProperty("user.home") + "/.m2/repository"));
        DependencyResolver.ResolveResult result = resolver.resolve("com.my.plugin:plugin-impl:jar:0.1-SNAPSHOT");
 
         List<URL> artifactUrls = new ArrayList<URL>();
         for (ArtifactResult artRes : result.artifactResults) {
             artifactUrls.add(artRes.getArtifact().getFile().toURI().toURL());
         }
         final URLClassLoader urlClassLoader = new URLClassLoader(artifactUrls.toArray(new URL[artifactUrls.size()]));
 
         Class<?> clazz = urlClassLoader.loadClass("com.my.plugin.impl.PluginAdapter");
         final Plugin adapterInstance = classLoaderProxy(urlClassLoader, clazz.newInstance(), Plugin.class);
         System.out.println("Result: " + adapterInstance.perform(2, 3));
     }
 }
