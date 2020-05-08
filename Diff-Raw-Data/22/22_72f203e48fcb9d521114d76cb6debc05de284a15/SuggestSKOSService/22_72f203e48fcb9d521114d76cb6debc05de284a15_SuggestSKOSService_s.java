 package pt.unl.fct.di.suggestskos;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.lucene.util.Version;
 
 import pt.unl.fct.di.suggestskos.resources.ExpansionsResource;
 import pt.unl.fct.di.suggestskos.resources.SuggestResource;
 
 import at.ac.univie.mminf.luceneSKOS.search.SKOSAutocompleter;
 import at.ac.univie.mminf.luceneSKOS.skos.SKOSEngine;
 import at.ac.univie.mminf.luceneSKOS.skos.impl.SKOSEngineImpl;
 
 import com.google.common.cache.CacheBuilderSpec;
 import com.yammer.dropwizard.Service;
 import com.yammer.dropwizard.bundles.AssetsBundle;
 import com.yammer.dropwizard.config.Environment;
 
 public class SuggestSKOSService extends Service<SuggestSKOSConfiguration> {
   
   public static void main(String[] args) throws Exception {
     new SuggestSKOSService().run(args);
   }
   
   private SuggestSKOSService() {
     super("suggest-skos");
     
     // By default a restart will be required to pick up any changes to assets.
     // Use the following spec to disable that behaviour, useful when developing.
     // CacheBuilderSpec cacheSpec = CacheBuilderSpec.disableCaching();
     
     CacheBuilderSpec cacheSpec = AssetsBundle.DEFAULT_CACHE_SPEC;
     addBundle(new AssetsBundle("/public", cacheSpec, "/public"));
   }
   
   @Override
   protected void initialize(SuggestSKOSConfiguration configuration,
       Environment environment) throws IOException {
     final String fileName = configuration.getFileName();
     final String languages = configuration.getLanguages();
     
     File file = new File(fileName);
     if (!file.exists()) {
      throw new IOException("File " + fileName + "not found!");
     }
     
     final String[] langs = languages.split("\\s+");
     
     final SKOSEngine skosEngine = new SKOSEngineImpl(Version.LUCENE_40,
         fileName, langs);
     final SKOSAutocompleter skosAutocompleter = new SKOSAutocompleter(
         Version.LUCENE_40, fileName, langs);
     
     environment.addResource(new ExpansionsResource(skosEngine));
     environment.addResource(new SuggestResource(skosAutocompleter));
   }
   
 }
