 /*
  * Created on Apr 26, 2006
  */
 package uk.org.ponder.rsf.templateresolver;
 
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.stringutil.StringList;
 
 /** Resolves an incoming set of ViewParameters onto a list of template path
  * names (minus extension) to be searched for an appropriate template file.
 * <p>The view is assumed to be rendered from a single template.
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  *
  */
 
 public interface SimpleTemplateResolverStrategy {
   /** @param viewparams The view parameters for the view being rendered.
    * @return A list of resource names (as supplied to some kind of 
    * InputStreamSource) MINUS extension, that will be searched, IN ORDER,
   * for a view template to be used for the current view. These are full
   * paths relative to the current context root, beginning with a leading slash.
    */
   public StringList resolveTemplatePath(ViewParameters viewparams);
 }
