 /*
  * Created on Apr 26, 2006
  */
 package uk.org.ponder.rsf.templateresolver;
 
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.stringutil.StringList;
 
 /** Resolves an incoming set of ViewParameters onto a list of template path
  * names (minus extension) to be searched for an appropriate template file.
 * <p>The view is assumed to be rendered from a single template from a selection
 * all held within the same base directory.
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  *
  */
 
 public interface SimpleTemplateResolverStrategy {
   /** @param viewparams The view parameters for the view being rendered.
    * @return A list of resource names (as supplied to some kind of 
    * InputStreamSource) MINUS extension, that will be searched, IN ORDER,
   * for a view template to be used for the current view. 
    */
   public StringList resolveTemplatePath(ViewParameters viewparams);
  
  /** @return The common base directory that is assumed to hold all templates */
  public String getBaseDirectory();
 }
