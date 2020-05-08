 /*
  * Created on Sep 19, 2005
  */
 package uk.org.ponder.rsf.templateresolver;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 
 import org.springframework.core.io.ResourceLoader;
 
 import uk.org.ponder.rsf.flow.errors.SilentRedirectException;
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
 public class BasicTemplateResolver implements TemplateResolver {
 
   private TemplateExtensionInferrer tei;
   private int cachesecs;
   private BaseURLProvider bup;
   private TPIAggregator aggregator;
   private List strategies;
 
   public void setBaseURLProvider(BaseURLProvider bup) {
     this.bup = bup;
   }
 
   public void setResourceLoader(ResourceLoader resourceLoader) {
     cachingiis = new CachingInputStreamSource(resourceLoader, cachesecs);
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
 
     // NB if we really want this optimisation, it must be based on #
     // of RETURNED templates, not the number of strategies!
     XMLCompositeViewTemplate xcvt = strategies.size() == 1 ? null
         : new XMLCompositeViewTemplate();
     int highestpriority = 0;
     StringList tried = new StringList();
     
     for (int i = 0; i < strategies.size(); ++i) {
       TemplateResolverStrategy trs = (TemplateResolverStrategy) strategies
           .get(i);
       int thispri = trs instanceof RootAwareTRS ? ((RootAwareTRS) trs)
           .getRootResolverPriority()
           : 1;
       
       boolean isexpected = trs instanceof ExpectedTRS ? ((ExpectedTRS) trs)
           .isExpected()
           : true;
       boolean ismultiple = trs instanceof MultipleTemplateResolverStrategy ? ((MultipleTemplateResolverStrategy) trs)
           .isMultiple()
           : false;
 
       StringList bases = trs.resolveTemplatePath(viewparams);
 
       StringList[] usebases;
       if (ismultiple) {
         usebases = new StringList[bases.size()];
         for (int j = 0; j < usebases.length; ++j) {
           usebases[j] = new StringList(bases.stringAt(j));
         }
       }
       else {
         usebases = new StringList[] { bases };
       }
       
       for (int j = 0; j < usebases.length; ++j) {
         XMLViewTemplate template = locateTemplate(viewparams, trs, usebases[j],
             isexpected ? tried : null, ismultiple && isexpected);
         if (template != null) {
           if (xcvt != null) {
             xcvt.globalmap.aggregate(template.globalmap);
             if (template.mustcollectmap != null) {
               xcvt.mustcollectmap.aggregate(template.mustcollectmap);
             }
             
             if (thispri == highestpriority && thispri != 0) {
               if (xcvt.roottemplate != null) {
                 Logger.log.warn("Duplicate root TemplateResolverStrategy " + trs
                     + " found at priority " + thispri +", using first entry");
               }
             }
             if (thispri > highestpriority) {
               xcvt.roottemplate = template;
               highestpriority = thispri;
             }
 
           }
           else {
             return template;
           }
         } // end if template returned
       }
     }
     
     if (xcvt != null && xcvt.roottemplate == null) {
       Exception eclass = viewparams.viewID.trim().length() == 0? 
           (Exception)new SilentRedirectException() : new IllegalArgumentException();
       throw UniversalRuntimeException.accumulate(eclass,
           "No TemplateResolverStrategy which was marked as a root resolver (rootPriority > 0) " +
           "returned a template: tried paths (expected) " + tried.toString());
     }
     return xcvt;
   }
 
   public XMLViewTemplate locateTemplate(ViewParameters viewparams,
       TemplateResolverStrategy strs, StringList bases, StringList tried, boolean logfailure) {
     String resourcebase = "/";
     if (strs instanceof BaseAwareTemplateResolverStrategy) {
       BaseAwareTemplateResolverStrategy batrs = (BaseAwareTemplateResolverStrategy) strs;
       resourcebase = batrs.getTemplateResourceBase();
     }
 
     String extension = tei.inferTemplateExtension(viewparams);
     InputStream is = null;
     String fullpath = null;
     for (int i = 0; i < bases.size(); ++i) {
       fullpath = resourcebase + bases.stringAt(i) + "." + extension;
       if (tried != null) {
         tried.add(fullpath);
       }
       is = cachingiis.openStream(fullpath);
       if (is != null)
         break;
       if (is == null && logfailure) {
        Logger.log.warn("Failed to load template from " + fullpath);
       }
     }
     if (is == null) {
       return null;
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
         if (strs instanceof ForceContributingTRS) {
           ForceContributingTRS fctrs = (ForceContributingTRS) strs;
           if (fctrs.getMustContribute()) {
             template.mustcollectmap = template.collectmap;
           }
         }
         template.setResourceBase(extresourcebase + resourcebaseext);
         template.fullpath = fullpath;
         templates.put(fullpath, template);
       }
       catch (Exception e) {
         throw UniversalRuntimeException.accumulate(e,
             "Error parsing view template file " + fullpath);
       }
     }
     return template;
   }
 
 }
