 /*
  * Created on 13 Aug 2006
  */
 package uk.org.ponder.rsf.templateresolver;
 
 import uk.org.ponder.rsf.view.ViewTemplate;
 
 /** A TemplateResolver which is aware of a particular association between
  * internal resource paths (as provided to a ResourceLoader) and external
  * URLs which may be resolved by the client.
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  *
  */
 public interface BaseAwareTemplateResolverStrategy {
   /** The resource base from which access to the physical template file 
    * by the templating engine is to be based. The default path is "/"
    * corresponding to the root of servlet context resources. Should begin
    * with a /.
    * see {@link ViewTemplate} for further comments.
    * 
    */ 
   public String getTemplateResourceBase();
   /** Returns the externally usable URL corresponding to the base path
   * given from getTemplateResourceBase(). Will return <code>""</code> in 
   * the default case that this is a "local" resolver, that is, served from
   * the current Servlet Container. This is to handle the case where 
   * the resource base should be determined from the incoming request. 
    */
   public String getExternalURLBase();
 }
