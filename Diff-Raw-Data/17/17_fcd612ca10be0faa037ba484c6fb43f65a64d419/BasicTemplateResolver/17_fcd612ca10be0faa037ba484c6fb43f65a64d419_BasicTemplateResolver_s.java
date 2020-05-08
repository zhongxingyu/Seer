 /*
  * Created on Sep 19, 2005
  */
 package uk.org.ponder.rsf.templateresolver;
 
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 
 import uk.org.ponder.rsf.template.TPIAggregator;
 import uk.org.ponder.rsf.template.XMLCompositeViewTemplate;
 import uk.org.ponder.rsf.template.XMLViewTemplate;
 import uk.org.ponder.rsf.template.XMLViewTemplateParser;
 import uk.org.ponder.rsf.view.ViewTemplate;
 import uk.org.ponder.rsf.viewstate.BaseURLProvider;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.springutil.CachingInputStreamSource;
 import uk.org.ponder.stringutil.StringList;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * A basic template resolver accepting a TemplateExtensionInferrer and a
  * TemplateResolverStrategy to load a view template.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class BasicTemplateResolver implements TemplateResolver,
     ApplicationContextAware {
 
   private TemplateExtensionInferrer tei;
   private int cachesecs;
   private BaseURLProvider bup;
   private TPIAggregator aggregator;
   private List strategies;
 
   public void setBaseURLProvider(BaseURLProvider bup) {
     this.bup = bup;
   }
 
   /**
    * Set the lag in seconds at which the filesystem will be polled for changes
    * in the view template. If this value is 0 or the resource is not a
    * filesystem resource, it will always be reloaded.
    */
   public void setCacheSeconds(int cachesecs) {
     this.cachesecs = cachesecs;
   }
 
   public void setTemplateExtensionInferrer(TemplateExtensionInferrer tei) {
     this.tei = tei;
   }
 
   public void setTemplateResolverStrategies(List strategies) {
     this.strategies = strategies;
   }
 
   public void setTPIAggregator(TPIAggregator aggregator) {
     this.aggregator = aggregator;
   }
 
   // this is a map of viewID onto template file.
   private HashMap templates = new HashMap();
 
   private CachingInputStreamSource cachingiis;
 
   public ViewTemplate locateTemplate(ViewParameters viewparams) {
     TemplateResolverStrategy rootstrategy = null;
     int highestpriority = 0;
     XMLCompositeViewTemplate xcvt = strategies.size() == 1 ? null
         : new XMLCompositeViewTemplate();
 
     for (int i = 0; i < strategies.size(); ++i) {
       TemplateResolverStrategy trs = (TemplateResolverStrategy) strategies
           .get(i);
      int thispri = trs instanceof RootAwareTRS? 
          ((RootAwareTRS) trs).getRootResolverPriority(): 1;
      if (thispri > highestpriority) {
        rootstrategy = trs;
        highestpriority = thispri;
      }
       if (thispri == highestpriority && thispri != 0) {
         if (rootstrategy != null) {
           Logger.log.warn("Duplicate root TemplateResolverStrategy " + trs
               + " found, using first entry " + rootstrategy);
         }
       }
     }
     if (rootstrategy == null) {
       rootstrategy = (TemplateResolverStrategy) strategies.get(0);
       Logger.log
           .warn("No root TemplateResolverStrategy found, using first entry of "
               + rootstrategy);
     }
 
     for (int i = 0; i < strategies.size(); ++i) {
       TemplateResolverStrategy trs = (TemplateResolverStrategy) strategies
           .get(i);
       XMLViewTemplate template = locateTemplate(viewparams, trs);
     
       if (xcvt != null) {
         xcvt.globalmap.aggregate(template.globalmap);
         if (trs == rootstrategy) {
           xcvt.roottemplate = template;
         }
       }
       else {
         return template;
       }
     }
 
     return xcvt;
   }
 
   private XMLViewTemplate locateTemplate(ViewParameters viewparams,
       TemplateResolverStrategy strs) {
     String resourcebase = "/";
     if (strs instanceof BaseAwareTemplateResolverStrategy) {
       BaseAwareTemplateResolverStrategy batrs = (BaseAwareTemplateResolverStrategy) strs;
       resourcebase = batrs.getTemplateResourceBase();
     }
 
     StringList bases = strs.resolveTemplatePath(viewparams);
     String extension = tei.inferTemplateExtension(viewparams);
     InputStream is = null;
     String fullpath = null;
     for (int i = 0; i < bases.size(); ++i) {
       fullpath = resourcebase + bases.stringAt(i) + "." + extension;
       is = cachingiis.openStream(fullpath);
       if (is != null)
         break;
     }
     if (is == null) {
       throw UniversalRuntimeException.accumulate(new FileNotFoundException(),
           "Cannot load template file from path " + fullpath);
     }
 
     XMLViewTemplate template = null;
     if (is == CachingInputStreamSource.UP_TO_DATE) {
       template = (XMLViewTemplate) templates.get(fullpath);
     }
     if (template == null) {
       List tpis = aggregator.getFilteredTPIs();
       try {
         // possibly the reason is it had a parse error last time, which may have
         // been corrected
         if (is == CachingInputStreamSource.UP_TO_DATE) {
           is = cachingiis.getNonCachingResolver().openStream(fullpath);
         }
         XMLViewTemplateParser parser = new XMLViewTemplateParser();
         parser.setTemplateParseInterceptors(tpis);
         template = (XMLViewTemplate) parser.parse(is);
         // there WILL be one slash in the path.
         int lastslashpos = fullpath.lastIndexOf('/');
         String resourcebaseext = fullpath.substring(1, lastslashpos + 1);
         String extresourcebase = bup.getResourceBaseURL();
         if (strs instanceof BaseAwareTemplateResolverStrategy) {
           BaseAwareTemplateResolverStrategy batrs = (BaseAwareTemplateResolverStrategy) strs;
           extresourcebase = batrs.getExternalURLBase();
         }
         template.setResourceBase(extresourcebase + resourcebaseext);
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
