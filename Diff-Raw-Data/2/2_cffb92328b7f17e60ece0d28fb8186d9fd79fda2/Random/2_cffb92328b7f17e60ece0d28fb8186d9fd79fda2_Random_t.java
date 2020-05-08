 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package RSLBench.Algorithms.Random;
 
 import RSLBench.Assignment.DCOP.DCOPAgent;
 import RSLBench.Assignment.DCOP.DCOPSolver;
 import rescuecore2.standard.entities.StandardEntityURN;
 import static rescuecore2.standard.entities.StandardEntityURN.FIRE_BRIGADE;
 
 /**
  * Pure greedy solver.
  *
  * @author Marc Pujol <mpujol@iiia.csic.es>
  */
 public class Random extends DCOPSolver {
 
     @Override
     protected DCOPAgent buildAgent(StandardEntityURN type) {
         switch(type) {
             case FIRE_BRIGADE:
                 return new RandomFireAgent();
            case POLICE_FORCE:
                return new RandomPoliceAgent();
             default:
                 throw new UnsupportedOperationException("The Random solver does not support agents of type " + type);
         }
     }
 
     @Override
     public String getIdentifier() {
         return "Random";
     }
 
 }
