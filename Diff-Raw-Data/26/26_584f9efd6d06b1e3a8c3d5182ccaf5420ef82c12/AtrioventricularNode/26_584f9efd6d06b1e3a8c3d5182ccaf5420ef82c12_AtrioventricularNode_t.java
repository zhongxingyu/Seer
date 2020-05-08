 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package heartsim.cam.profile;
 
 import heartsim.cam.CellularAutomataModel;
 import heartsim.cam.Nishiyama;
 
 /**
  *
  * @author Lee Boynton
  */
 public class AtrioventricularNode extends Profile
 {
     public AtrioventricularNode()
     {
         super("Atrioventricular node");
     }
 
     @Override
     public CellularAutomataModel loadParameters(CellularAutomataModel model)
     {
         if(model instanceof Nishiyama)
         {
            model.getParameter("Delta 2").setValue("8");
         }
 
         return model;
     }
 }
