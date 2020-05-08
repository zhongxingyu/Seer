 package jDistsim.application.designer.model;
 
 import jDistsim.utils.pattern.mvc.AbstractFrame;
 import jDistsim.utils.pattern.mvc.AbstractModel;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 28.10.12
  * Time: 0:10
  */
 public class StatusBarModel extends AbstractModel {
 
     private String leftContentText;
     private String rightContentText;
 
     public StatusBarModel(AbstractFrame mainFrame) {
         this(mainFrame, new String(), new String());
     }
 
     @Override
     public void initialize() {
     }
 
     public StatusBarModel(AbstractFrame mainFrame, String leftContentText, String rightContentText) {
         super(mainFrame);
         this.leftContentText = leftContentText;
         this.rightContentText = rightContentText;
     }
 
     public String getLeftContentText() {
         return leftContentText;
     }
 
    //TODO FIXNOUT A OPRAVIT!
     public void setLeftContentText(String leftContentText) {
         this.leftContentText = leftContentText;
        //notifyObservers();
     }
 
     public String getRightContentText() {
         return rightContentText;
     }
 
     public void setRightContentText(String rightContentText) {
         this.rightContentText = rightContentText;
        //notifyObservers();
     }
 }
