 package jDistsim.utils.pattern.mvc;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 26.10.12
  * Time: 17:32
  */
 public class AbstractController<TModel extends AbstractModel> {
 
     private final AbstractFrame mainFrame;
     private final TModel model;
 
     public AbstractController(AbstractFrame mainFrame, TModel model) {
         this.mainFrame = mainFrame;
         this.model = model;
     }
 
    protected AbstractFrame getMainFrame() {
         return mainFrame;
     }
 
     public TModel getModel() {
         return model;
     }
 }
