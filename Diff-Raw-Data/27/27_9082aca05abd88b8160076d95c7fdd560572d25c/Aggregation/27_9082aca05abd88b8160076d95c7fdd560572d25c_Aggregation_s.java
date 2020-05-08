 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mase.app.aggregation;
 
 import java.util.ArrayList;
 import java.util.List;
 import mase.AgentController;
 import mase.GroupController;
 import mase.mason.MaseSimState;
 import mase.mason.SmartAgent;
 import sim.field.continuous.Continuous2D;
 import sim.util.Double2D;
 
 /**
  *
  * @author jorge
  */
 public class Aggregation extends MaseSimState {
 
     protected AggregationParams par;
     protected List<AggregationAgent> agents;
     protected Continuous2D field;
     protected GroupController gc;
 
     public Aggregation(long seed, AggregationParams par, GroupController gc) {
         super(seed);
         this.par = par;
         this.gc = gc;
     }
 
     @Override
     public void start() {
         super.start();
         this.field = new Continuous2D(par.discretization, par.size, par.size);
         placeAgents();
     }
 
     @Override
     public Object getField() {
         return field;
     }
 
     @Override
     public List<? extends SmartAgent> getSmartAgents() {
         return agents;
     }
 
     protected void placeAgents() {
         agents = new ArrayList<AggregationAgent>(par.numAgents);
         AgentController[] acs = gc.getAgentControllers(par.numAgents);
         for (int i = 0; i < par.numAgents; i++) {
            acs[i].reset();
            AggregationAgent ag = new AggregationAgent(this, field, acs[i]);
             Double2D p;
             double margin = par.wallRadius + AggregationAgent.RADIUS + 1;
             do {
                 p = new Double2D(margin + random.nextDouble() * (par.size - margin * 2), 
                         margin + random.nextDouble() * (par.size - margin * 2));
             } while (!field.getNeighborsExactlyWithinDistance(p, par.agentRadius + AggregationAgent.RADIUS * 2 + 1).isEmpty());
             ag.setLocation(p);
             ag.setOrientation(random.nextDouble() * Math.PI * 2);
             schedule.scheduleRepeating(ag);
             agents.add(ag);
         }
     }
 }
