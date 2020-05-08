 package com.mns.mojoinvest.server.mustache;
 
 import com.github.mustachejava.Mustache;
 import com.github.mustachejava.MustacheFactory;
 import com.google.inject.Inject;
 import com.sun.jersey.api.view.Viewable;
 import com.sun.jersey.spi.resource.Singleton;
 import com.sun.jersey.spi.template.ViewProcessor;
 
 import javax.ws.rs.ext.Provider;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 //TODO: Why is there a problem with sharing the mustacheFactory
 //TODO: How to set base path for mustacheFactory?
 
 @Singleton
 @Provider
 public class MustacheViewProcessor implements ViewProcessor<Mustache> {
 
    private final Logger log = Logger.getLogger(this.getClass().toString());
 
     private final Map<String, Mustache> compiledTemplates;
     private final String basePath;
     private final boolean live;
 
     @Inject
     public MustacheViewProcessor(String path, boolean live) {
 
         compiledTemplates = new HashMap<String, Mustache>();
         basePath = path;
         this.live = live;
 
         precompileTemplates(new File(basePath));
     }
 
     protected String getDefaultExtension() {
         return ".mustache";
     }
 
     private void precompileTemplates(File dir) {
         for (File f : dir.listFiles()) {
             precompileTemplatesRecursively(f, "");
         }
     }
 
     private void precompileTemplatesRecursively(File dir, String namespace) {
         namespace += '/';
 
         if (dir.isDirectory()) {
             namespace += dir.getName();
             for (File f : dir.listFiles()) {
                 precompileTemplatesRecursively(f, namespace);
             }
         } else if (dir.exists()) {
             String key = namespace + dir.getName();
             MustacheFactory mustacheFactory = new NonEscapingMustacheFactory();
             Mustache m = mustacheFactory.compile(dir.getAbsolutePath());
             compiledTemplates.put(key, m);
         }
     }
 
 
     @Override
     public Mustache resolve(final String path) {
         final String defaultExtension = getDefaultExtension();
         final String filePath = path.endsWith(defaultExtension) ? path : path + defaultExtension;
         if (compiledTemplates.containsKey(filePath)) {
             return compiledTemplates.get(filePath);
         }
         return null;
     }
 
     @Override
     public void writeTo(Mustache mustache, Viewable viewable, OutputStream out) throws IOException {
 
         if (live) {
             precompileTemplates(new File(basePath));
         }
         mustache.execute(new PrintWriter(out), viewable.getModel()).flush();
     }
 }
