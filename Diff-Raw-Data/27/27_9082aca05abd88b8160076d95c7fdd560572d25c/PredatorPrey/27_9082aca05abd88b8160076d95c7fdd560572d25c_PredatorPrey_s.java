 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mase.app.pred;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import mase.AgentController;
 import mase.GroupController;
 import mase.mason.MaseSimState;
 import mase.mason.SmartAgent;
 import sim.field.continuous.Continuous2D;
 import sim.util.Bag;
 import sim.util.Double2D;
 
 /**
  *
  * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
  */
 public class PredatorPrey extends MaseSimState  {
 
     protected PredParams par;
     protected Continuous2D field;
     protected GroupController gc;
     protected List<Predator> predators;
     protected List<Prey> preys;
     protected List<Prey> activePreys;
     protected int captureCount;
 
     public PredatorPrey(long seed, PredParams params, GroupController gc) {
         super(seed);
         this.gc = gc;
         this.par = params;
     }
 
     @Override
     public void start() {
         super.start();
         this.field = new Continuous2D(par.discretization, par.size, par.size);
         this.predators = null;
         this.preys = null;
         this.activePreys = null;
         this.captureCount = 0;
         placePredators();
         placePreys();
     }
 
     protected void placePreys() {
         // randomly place preys, in the opposite half of the field
         preys = new ArrayList<Prey>();
         while (preys.size() < par.nPreys) {
             double x = 0;
             double y = 0;
             if (par.preyPlacement.equalsIgnoreCase(PredParams.V_CENTER)) {
                 x = par.size / 2;
                 y = par.size / 2;
             } else if (par.preyPlacement.equalsIgnoreCase(PredParams.V_RANDOM)) {
                 x = par.preyMargin + random.nextDouble() * (par.size - par.preyMargin * 2);
                 y = par.preyMargin + random.nextDouble() * (par.size - par.preyMargin * 2);
                 Bag close = field.getNeighborsExactlyWithinDistance(new Double2D(x, y), par.preySeparation + Prey.RADIUS * 2);
                 if (!close.isEmpty()) {
                     continue;
                 }
             }
             Prey newPrey = newPrey();
             newPrey.setLocation(new Double2D(x, y));
             newPrey.setStopper(schedule.scheduleRepeating(newPrey));
             preys.add(newPrey);
         }
         activePreys = new LinkedList<Prey>(preys);
     }
 
     protected void placePredators() {
         // align predators in a row in the bottom
         double w = par.predatorSeparation * (par.nPredators - 1);
         double y = 0;
         double startX = (field.width - w) / 2;
         predators = new ArrayList<Predator>(par.nPredators);
         AgentController[] controllers = gc.getAgentControllers(par.nPredators);
         for (int i = 0; i < par.nPredators; i++) {
             double x = startX + i * par.predatorSeparation;
            controllers[i].reset();
            Predator newPred = newPredator(controllers[i]);
             newPred.setLocation(new Double2D(x, y));
             newPred.setStopper(schedule.scheduleRepeating(newPred));
             predators.add(newPred);
         }
     }
 
     protected Predator newPredator(AgentController ac) {
         if (par.sensorMode == PredParams.V_ARCS) {
             return new MultiPredator(this, field, ac);
         } else {
             return new Predator(this, field, ac);
         }
     }
 
     protected Prey newPrey() {
         return new Prey(this, field);
     }
 
     protected void terminate() {
         for (Prey p : preys) {
             p.stop();
         }
         for (Predator p : predators) {
             p.stop();
         }
     }
 
     public int getCaptureCount() {
         return captureCount;
     }
 
     @Override
     public Object getField() {
         return field;
     }
 
     @Override
     public List<? extends SmartAgent> getSmartAgents() {
         return predators;
     }
 }
