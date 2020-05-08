 package org.i4qwee.chgk.trainer.controller.brain.manager;
 
 import org.i4qwee.chgk.trainer.controller.brain.listener.NamesListener;
 
 /**
  * User: 4qwee
  * Date: 06.01.12
  * Time: 13:23
  */
 public class NamesManager extends Manager<NamesListener>
 {
     private static final NamesManager instance = new NamesManager();
 
     private String leftName;
     private String rightName;
 
     private NamesManager()
     {
     }
 
     public static NamesManager getInstance()
     {
         return instance;
     }
 
     public String getLeftName()
     {
        return leftName;
     }
 
     public void setLeftName(String leftName)
     {
         this.leftName = leftName;
         notifyListeners();
     }
 
     public String getRightName()
     {
        return rightName;
     }
 
     public void setRightName(String rightName)
     {
         this.rightName = rightName;
         notifyListeners();
     }
 
     public void setNames(String leftName, String rightName)
     {
         this.leftName = leftName;
         this.rightName = rightName;
         notifyListeners();
     }
 
     @Override
     protected void notifyListeners()
     {
         for (NamesListener listener : listeners)
             listener.onNamesChanged(leftName, rightName);
     }
 }
