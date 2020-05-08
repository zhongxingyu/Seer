 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mase.app.keepaway;
 
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
  * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
  */
 public class Keepaway extends MaseSimState {
 
     protected GroupController gc;
     protected KeepawayParams par;
     protected Continuous2D field;
     protected List<Keeper> keepers;
     protected List<Taker> takers;
     protected Ball ball;
     protected boolean caught;
     protected boolean outOfLimits;
     protected Double2D center;
 
     public Keepaway(long seed, KeepawayParams par, GroupController gc) {
         super(seed);
         this.gc = gc;
         this.par = par;
     }
 
     @Override
     public void start() {
         super.start();
         this.field = new Continuous2D(par.discretization, par.size, par.size);
         this.center = new Double2D(par.size / 2, par.size / 2);
         this.caught = false;
         this.outOfLimits = false;
         placeKeepers();
         placeTakers();
         placeBall();
     }
 
     @Override
     public Object getField() {
         return field;
     }
 
     protected void placeKeepers() {
         keepers = new ArrayList<Keeper>(par.numKeepers);
         AgentController[] acs = gc.getAgentControllers(par.numKeepers);
         // place keepers evenly distributed around the circle
         Double2D up = new Double2D(0, par.ringSize / 2);
         double rot = Math.PI * 2 / par.numKeepers;
         for(int i = 0 ; i < par.numKeepers ; i++) {
            //acs[i].reset();
             Keeper k = new Keeper(this, field, acs[i].clone(), par.passSpeed[i], par.moveSpeed[i], par.color[i]);
             Double2D v = up.rotate(rot * i);
             k.setLocation(v.add(center));
             k.setDirection(v.negate());
             k.setStopper(schedule.scheduleRepeating(k));
             k.enableCollisionDetection(par.collisions);
             keepers.add(k);
         }
     }
 
     protected void placeTakers() {
         takers = new ArrayList<Taker>(1);
         Taker t = new Taker(this, field);
         if(par.takersPlacement == KeepawayParams.V_CENTER) {
             t.setLocation(center);
         } else if(par.takersPlacement == KeepawayParams.V_RANDOM) {
             double q = random.nextDouble() * Math.PI *  2;
             double r = Math.sqrt(random.nextDouble());
             double x = (par.placeRadius * r) * Math.cos(q) + center.getX();
             double y = (par.placeRadius * r) * Math.sin(q) + center.getY();
             t.setLocation(new Double2D(x,y));
         }
         t.enableCollisionDetection(par.collisions);
         t.setStopper(schedule.scheduleRepeating(t));
         takers.add(t);
     }
 
     protected void placeBall() {
         ball = new Ball(this, field);
         int ag = par.ballPlacement;
         if(ag == -1) {
             ag = random.nextInt(keepers.size());
         }
         Keeper k = keepers.get(ag);
         Double2D loc = k.getLocation().add(k.getDirection().resize(5));
         ball.setLocation(loc);
         ball.setStopper(schedule.scheduleRepeating(ball));
     }
     
     protected void terminate() {
         for (Keeper k : keepers) {
             k.stop();
         }
         for (Taker t : takers) {
             t.stop();
         }
         ball.stop();
     }
 
     @Override
     public List<? extends SmartAgent> getSmartAgents() {
         return keepers;
     }
 }
