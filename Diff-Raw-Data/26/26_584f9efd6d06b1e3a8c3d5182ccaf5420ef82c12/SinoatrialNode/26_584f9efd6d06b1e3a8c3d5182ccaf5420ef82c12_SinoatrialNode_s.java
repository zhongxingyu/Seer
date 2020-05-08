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
 public class SinoatrialNode extends Profile
 {
     public SinoatrialNode()
     {
         super("Sinoatrial node");
     }
 
     @Override
     public CellularAutomataModel loadParameters(CellularAutomataModel model)
     {
         if(model instanceof Nishiyama)
         {
            model.getParameter("delta1").setValue("4");
         }
 
         return model;
     }
 }
