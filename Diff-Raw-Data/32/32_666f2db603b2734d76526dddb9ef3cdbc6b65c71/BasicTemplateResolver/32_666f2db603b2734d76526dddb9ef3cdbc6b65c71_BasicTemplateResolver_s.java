 /*
  * Created on Sep 19, 2005
  */
 package uk.org.ponder.rsf.templateresolver;
 
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.HashMap;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 
 import uk.org.ponder.rsf.template.XMLViewTemplate;
 import uk.org.ponder.rsf.view.ViewTemplate;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.springutil.CachingInputStreamSource;
 import uk.org.ponder.stringutil.StringList;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * A basic template resolver accepting a TemplateExtensionInferrer and
  * a TemplateResolverStrategy to load a view template.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class BasicTemplateResolver implements TemplateResolver,
     ApplicationContextAware {
 
   private TemplateExtensionInferrer tei;
   private int cachesecs;
   private SimpleTemplateResolverStrategy strs;
 
   /** Set the lag in seconds at which the filesystem will be polled for changes
    * in the view template. If this value is 0 or the resource is not a 
    * filesystem resource, it will always be reloaded.
    */
   public void setCacheSeconds(int cachesecs) {
     this.cachesecs = cachesecs;
   }
   
   public void setTemplateExtensionInferrer(TemplateExtensionInferrer tei) {
     this.tei = tei;
   }
   
   public void setTemplateResolverStrategy(SimpleTemplateResolverStrategy strs) {
     this.strs = strs;
   }
 
   // this is a map of viewID onto template file.
   private HashMap templates = new HashMap();
  
   private CachingInputStreamSource cachingiis;
 
   public ViewTemplate locateTemplate(ViewParameters viewparams) {
     StringList bases = strs.resolveTemplatePath(viewparams);
     String extension = tei.inferTemplateExtension(viewparams);
     InputStream is = null;
     String fullpath = null;
     for (int i = 0; i < bases.size(); ++ i) {
       fullpath = bases.stringAt(i) + "." + extension;
       is = cachingiis.openStream(fullpath);
       if (is != null) break;
     }
     if (is == null) {
       throw UniversalRuntimeException.accumulate(new FileNotFoundException(),
           "Cannot load template file from path " + fullpath);
     }
     
    String basedir = strs.getBaseDirectory(); 
    
     ViewTemplate template = null;
     if (is == CachingInputStreamSource.UP_TO_DATE) {
       template = (ViewTemplate) templates.get(fullpath);
     }
     if (template == null) { 
       try {
      // possibly the reason is it had a parse error last time, which may have been corrected
         if (is == CachingInputStreamSource.UP_TO_DATE) {
           is = cachingiis.getNonCachingResolver().openStream(fullpath);
         }
         template = new XMLViewTemplate();
         template.parse(is);
        template.setRelativePath(basedir.substring(1));
         templates.put(fullpath, template);
       }
       catch (Exception e) {
         throw UniversalRuntimeException.accumulate(e,
             "Error parsing view template file " + fullpath);
       }
     }
     return template;
   }
 
   public void setApplicationContext(ApplicationContext applicationContext) {
     cachingiis = new CachingInputStreamSource(applicationContext, cachesecs);
   }
 
 }
