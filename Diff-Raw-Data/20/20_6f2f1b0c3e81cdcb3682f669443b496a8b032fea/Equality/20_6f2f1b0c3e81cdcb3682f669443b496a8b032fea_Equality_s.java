 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.co.mtford.abduction.logic;
 
 import java.util.Map;
 
 /**
  *
  * @author mtford
  */
 public class Equality extends AbstractPredicate {
     
     public Equality(IUnifiable param1, IUnifiable param2) {
         this.parameters=new IUnifiable[2];
         parameters[0]=param1;
         parameters[1]=param2;
         name="equals";
     }
 
     public String getName() {
        return name;
     }
 
     public IUnifiable[] getParameters() {
         return parameters;
     }
     
     public void setParameters(IUnifiable param1, IUnifiable param2) {
         parameters[0]=param1;
         parameters[1]=param2;
     }
     
     
     
     
 }
