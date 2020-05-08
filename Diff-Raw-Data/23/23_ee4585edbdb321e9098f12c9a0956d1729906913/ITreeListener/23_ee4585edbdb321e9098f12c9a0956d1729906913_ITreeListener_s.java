 package pl.psnc.dl.wf4ever.portal.pages.ro.folderviewer.components.behaviours;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 
 /**
  * Tree events listener.
  * 
  * @author pejot
  * 
  */
 public interface ITreeListener {
 
     /**
      * Click event listener.
      * 
      * @param target
      *            ajax target
      */
    void onNodeLinkClicked(AjaxRequestTarget target);
 }
