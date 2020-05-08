 /*
  * Created on Apr 26, 2006
  */
 package uk.org.ponder.rsf.templateresolver;
 
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.stringutil.StringList;
 import uk.org.ponder.webapputil.ConsumerInfo;
 
 /**
  * A simple TemplateResolverStrategy that looks in any ConsumerRequestInfo valid
  * for the current request for a prefix to be applied to the view id to form a
  * template name, or else falls back to just the view id.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 
 public class CRITemplateResolverStrategy implements
     SimpleTemplateResolverStrategy {
   private String basedir;
 
   private ConsumerInfo ciproxy;
   public static final String CONSUMERTYPE_SEPARATOR = "-";
 
   /**
    * Set the base directory in which template files will be sought. This must
    * contain both trailing slash and leading slash.
    */
   public void setBaseDirectory(String basedir) {
     this.basedir = basedir;
   }
 
   public void setConsumerInfo(ConsumerInfo ci) {
     this.ciproxy = ci;
   }
 
   public StringList resolveTemplatePath(ViewParameters viewparams) {
     StringList togo = new StringList();
     ConsumerInfo ci = ciproxy.get();
     if (ci.consumertype != null) {
       String consumerprefix = ci.consumertype + CONSUMERTYPE_SEPARATOR;
       togo.add(basedir + consumerprefix + viewparams.viewID);
     }
 
     togo.add(basedir + viewparams.viewID);
     return togo;
   }
 
 }
