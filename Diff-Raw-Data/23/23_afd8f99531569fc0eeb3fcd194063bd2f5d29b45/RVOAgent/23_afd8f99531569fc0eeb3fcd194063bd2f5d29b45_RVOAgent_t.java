 package agent;
 
 /**
  * 
  *
  * @author michaellees
  * Created: Nov 29, 2010
  *
  * 
  *
  * Description:This class describes the agents themselves, except for the 
  * portrayal components all the internal characteristics of the agents are 
  * stored here.
  *
  */
 import utility.PrecisePoint;
 import app.PropertySet;
 import environment.RVOSpace;
 import javax.vecmath.Point2d;
 import javax.vecmath.Vector2d;
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 import motionPlanners.socialforce.SocialForce;
 import motionPlanners.VelocityCalculator;
 import motionPlanners.pbm.WorkingMemory;
 import motionPlanners.pbm.WorkingMemory.strategymatchingCommitment;
 import motionPlanners.rvo1.RuleBasedNew;
 import motionPlanners.rvo1.RVO_1_Standard;
 import motionPlanners.rvo1.RVO_1_WithAccel;
 import motionPlanners.rvo2.RVO_2_1;
 import sim.engine.SimState;
 import sim.engine.Steppable;
 import sim.engine.Stoppable;
 import sim.portrayal.LocationWrapper;
 import sim.util.Bag;
 import sim.util.Proxiable;
 
 public class RVOAgent extends AgentPortrayal implements Proxiable {
 
     //static constants
     // public static final double MAXACCEL = 260.0f; //m/s*s
     public static double INFO_LIMIT; // Chunks!
 //    public static final double INFO_LIMIT = Double.MAX_VALUE; // Chunks!
     public static double RADIUS;
     public static double DEFAULT_PREFERRED_SPEED;
     public static int SENSOR_RANGE; //sensor range in proportion to agent radius
     public static int agentCount = 0; // number of agents
     
  
 
     protected int id;
     int currentGoal = 0;
     /**
      * This is used in the time to collision calculation to scale the radius of self
      * This factor is multiplied by the radius of the agent and added to the
      * original radius when calculating time to collision.
      *
      * i.e., an agent will consider itself to have a radius (r + personalSpaceFactor*r)
      *
      * This means agents will estimate collisions (i.e., non infinite time to collision)
      * for a circle bigger than their actual size.
      *
      * If this is zero then the standard RVO calculation for time to collision is performed
      */
     private double personalSpaceFactor = 0.5;
     /**
      * Current position of the agent from javax.vecmath
      */
     protected PrecisePoint currentPosition;
     protected double mass = 70; // in KG
     
     /**
      * Current velocity of the agent
      */
     protected PrecisePoint velocity;
     private PrecisePoint chosenVelocity;
     /**
      * Agents preferred velocity, calculated each timestep according to goal
      * and current location
      */
     protected Vector2d prefVelocity;
     /**
      * Sets whether to display the agent's velocity on the map or not
      */
     protected double preferredSpeed;
     protected double maxSpeed;
     /**
      * Intermediate goal destination of agent
      */
     protected Point2d goal;
     /**
      * Environmental space of the agents, contains multiple MASON fields
      */
     protected RVOSpace mySpace;
 
     //@hunan: added for PBM only
     public boolean violateExpectancy;
     
     /**
      * The motion planning system used by the agent, this can used any method
      * for motion planning that implements the VelocityCalculator interface
      */
     protected VelocityCalculator velocityCalc;
     protected strategymatchingCommitment commitmentLevel;
     protected Stoppable senseThinkStoppable;
     protected Stoppable actStoppable;
     private SenseThink senseThinkAgent;
     private Act actAgent;
     private Point2d currentGoalPoint;
     private boolean dead = false;
 
     public RVOAgent(RVOSpace mySpace) {
         super(); //for portraying the trails on the agentportrayal layer
         this.mySpace = mySpace;
         currentPosition = new PrecisePoint();
         goal = new Point2d();
  
         //@hunan: added for PBM only
         violateExpectancy = false;
         
         //DEFAULT_PREFERRED_SPEED is the default value specified in 1.xml
         //the parameter value of preferredSpeed should be only set to this default value when it is not set with the value from the xml initialization file
         if(preferredSpeed==0) preferredSpeed = RVOAgent.DEFAULT_PREFERRED_SPEED;
 
         if (PropertySet.MODEL == PropertySet.Model.RVO2) {
             velocityCalc = new RVO_2_1();
         } else if (PropertySet.MODEL == PropertySet.Model.PatternBasedMotion) {
             velocityCalc = new WorkingMemory(this);
         } else if (PropertySet.MODEL == PropertySet.Model.RVO1Standard) {
             velocityCalc = new RVO_1_Standard();
         } else if (PropertySet.MODEL == PropertySet.Model.RVO1Acceleration) {
             velocityCalc = new RVO_1_WithAccel();
         } else if (PropertySet.MODEL == PropertySet.Model.RuleBasedNew) {
             velocityCalc = new RuleBasedNew();
         } else if (PropertySet.MODEL == PropertySet.Model.SocialForce) {
             velocityCalc = new SocialForce();
         }
         id = agentCount++;
     }
     
     /*
      * Default constructor to create agents from XML file
      */
     public RVOAgent(Point2d startPosition, Point2d goal, RVOSpace mySpace, Color col) {
         this(mySpace);
         setColor(col);
         currentPosition = new PrecisePoint(startPosition.getX(), startPosition.getY());
         this.goal = goal;
         findPrefVelocity();
     }
     
      /*
      * The constructor to create agents from XML file for PBM Only!
      */
     public RVOAgent(Point2d startPosition, Point2d goal, RVOSpace mySpace, Color col, double prefSpd, int commitLevel) {
         this(mySpace);
         setColor(col);
         setCommitmentLevel(commitLevel);
         currentPosition = new PrecisePoint(startPosition.getX(), startPosition.getY());
         preferredSpeed = prefSpd;
         maxSpeed = preferredSpeed * 1.5;
         this.goal = goal;
         findPrefVelocity();
         //set the initial velocity of each agent to its initial preferred velocity towards its goal
         velocity = new PrecisePoint(prefVelocity.getX(),prefVelocity.getY());
     }
     
     
     
         //@Should indicate, only called in VT's clusteredAgent
     public RVOAgent(RVOAgent otherAgent) {
         this(otherAgent.getMySpace());
         preferredSpeed = otherAgent.getPreferredSpeed();
         maxSpeed = preferredSpeed * 2.0;
         currentPosition = new PrecisePoint();
         this.setCurrentPosition(otherAgent.getCurrentPosition().getX(), otherAgent.getCurrentPosition().getY());
         goal = new Point2d(otherAgent.getGoal().getX(), otherAgent.getGoal().getY());
         mySpace = otherAgent.mySpace;
         id = otherAgent.getId();
         agentCount--;
     }
     
     public Point2d getGoal() {
         return goal;
     }
 
     public void setGoal(Point2d goal) {
         this.goal = goal;
         //  findPrefVelocity();
         //  velocity = new Vector2d(this.prefVelocity);
     }
 
     private boolean reachedGoal() {
         return (currentPosition.toPoint().distance(goal) < RADIUS);
     }
 
     public Point2d getCurrentPosition() {
         return currentPosition.toPoint();
     }
 
     public double getX() {
         return currentPosition.getX();
     }
 
     public double getY() {
         return currentPosition.getY();
     }
 
     final public void setCurrentPosition(double x, double y) {
         currentPosition = new PrecisePoint(x, y);
     }
 
     public Vector2d getVelocity() {
         if (velocity == null) {
             velocity = new PrecisePoint();
         }
         return velocity.toVector();
     }
 
     public void setVelocity(Vector2d prefVel) {
         velocity = new PrecisePoint(prefVel.getX(), prefVel.getY());
     }
 
     public double getSpeed() {
         return getVelocity().length();
     }
 
     public double getMass() {
         return mass;
     }
 
     public void setMass(double mass) {
         this.mass = mass;
     }
 //    public RVOAgent getAgent(int id){
 //        if(this.id==id) return this;
 //        return null;
 //    }
 //    
 
     /**
      * Sets and returns the prefered velocity. Generally this is just the velocity towards goal.
      * But in the evacTest scenario, this is set to the checkpoint nearby
      * @return new Preferred Velocity
      */
     public final Vector2d findPrefVelocity() {
         if (mySpace.hasRoadMap()) {
             prefVelocity = mySpace.determinePrefVelocity(this);
             prefVelocity.scale(preferredSpeed);
         } else {
             prefVelocity = new Vector2d(goal);
             prefVelocity.sub(currentPosition.toPoint());
             prefVelocity.normalize();
             prefVelocity.scale(preferredSpeed); //@hunan:added the scale for perferredSpeed
         }
         return prefVelocity;
     }
 
     public double getMaxSpeed() {
         return maxSpeed;
     }
 
     public double getPersonalSpaceFactor() {
         return personalSpaceFactor;
     }
 
     public RVOSpace getMySpace() {
         return mySpace;
     }
 
     public void setMySpace(RVOSpace space) {
         this.mySpace = space;
     }
 
     /**
      * Returns the predicted position i simulation steps in the future based on linear dead reckoning.
      *
      * @last modified by: hunan　at Dec 20th, 2011
      */
     public Point2d getNextPosition(int i) {
         PrecisePoint predictPos = new PrecisePoint(this.getVelocity().getX(),this.getVelocity().getY());
 //        Vector2d predictPos =new Vector2d(this.getVelocity().getX(),this.getVelocity().getY());
         predictPos.scale(i * PropertySet.TIMESTEP);
         predictPos.add(this.getCurrentPosition());
         return predictPos.toPoint();
     }
 
     public int getId() {
         return id;
     }
 
     public void setPreferredSpeed(double preferredSpeed) {
         this.preferredSpeed = preferredSpeed;
     }
 
     @Override
     public String getName(LocationWrapper wrapper) {
         return "Agent " + id;
     }
     
     @Override
     public String toString(){
         return "Agent" +id;
     }
 
     public VelocityCalculator getRvoCalc() {
         return velocityCalc;
     }
 
     public Vector2d getPrefVelocity() {
         return prefVelocity;
     }
 
     public strategymatchingCommitment getCommitementLevel() {
         return commitmentLevel;
     }
 
     public final void setCommitmentLevel(final int number) {
         switch (number) {
             case 1:
                 commitmentLevel = strategymatchingCommitment.LOWCOMMITMENT;
                 break;
             case 2:
                 commitmentLevel = strategymatchingCommitment.MIDCOMMITMENT;
                 break;
             case 3:
                 commitmentLevel = strategymatchingCommitment.HIGHCOMMITMENT;
                 break;
         }
     }
 
     public double getPreferredSpeed() {
         return preferredSpeed;
     }
 
     @Override
     public Object propertiesProxy() {
         return new MyProxy();
     }
 
     public SenseThink getSenseThink() {
         return this.senseThinkAgent;
     }
 
     public Act getAct() {
         return this.actAgent;
     }
 
     public void createSteppables() {
         this.senseThinkAgent = new SenseThink();
         this.actAgent = new Act();
 
     }
 
     public void setCurrentGoal(Point2d currentGoal) {
         this.currentGoalPoint = currentGoal;
     }
 
     public void setMaximumSpeed(double maxSpeed) {
         this.maxSpeed = maxSpeed;
     }
     
     
  
     
 
     public class SenseThink implements Steppable {
 
         @Override
         public void step(SimState ss) {
             if (!dead) {
                 if (reachedGoal()) {
                     RVOAgent.this.dead = true;
                     return;
                 }
                 findPrefVelocity(); //update the preferredVelocity according to the current position and the goal
 
 //                chosenVelocity = new PrecisePoint(prefVelocity.getX(), prefVelocity.getY());
 
                 Bag sensedNeighbours = mySpace.senseNeighbours(RVOAgent.this);
 
                 if (PropertySet.INFOPROCESSING) {
                     /**
                      * Here we process the neighbour list that was passed to it to determine collisions
                      */
                     List<RVOAgent> sortedList = new ArrayList<RVOAgent>();
                     determineInitialLists(sortedList, sensedNeighbours);
                     sensedNeighbours.clear();
                     sensedNeighbours.addAll(sortedList);
                
                 }
             
                if(PropertySet.MODEL == PropertySet.Model.PatternBasedMotion){
                    //use PBM to get the refined prefVel, which is expected to minimize collision
                    prefVelocity = velocityCalc.calculateVelocity(RVOAgent.this, sensedNeighbours, mySpace.senseObstacles(RVOAgent.this),
                             prefVelocity, PropertySet.TIMESTEP);
 
 //                    use RVO2 as the motion adjustment mechanism to ensure collision free.
                    VelocityCalculator velocityCalc2 = new RVO_2_1();
 
                    Vector2d tempVelocity = velocityCalc2.calculateVelocity(RVOAgent.this, sensedNeighbours, mySpace.senseObstacles(RVOAgent.this),
                             prefVelocity, PropertySet.TIMESTEP);
 
                     //Check expectancies according to the difference b/t prefVel and the actualVel (chosenVel)
                    //the comparison of two vectors (velocities) depends both on direction and speed as follows
                    Vector2d diff_V = new Vector2d(tempVelocity);
                    diff_V.sub(prefVelocity);
                    double diff_Speed = diff_V.length();
                    double diff_Direction_cosine = Math.cos(tempVelocity.angle(prefVelocity));
 
                    // To check whether the speed within variance of 0.2 and direction within angle of 10 degree
                    if (diff_Speed <= 0.2 && diff_Direction_cosine >= Math.cos(10*Math.PI/180)) {
                         //TODO: add in count to record the number of steps PBM gets good results for evaluation purpose later
                        violateExpectancy = false;
                     }
                    else{
                        //the actual vel violate the prefVel given by the Steering strategy
                        violateExpectancy = true;
                    }                
                    chosenVelocity = new PrecisePoint(tempVelocity.getX(), tempVelocity.getY());
 //                   chosenVelocity = new PrecisePoint(prefVelocity.getX(), prefVelocity.getY());
                 }
                 else{
                     //default as towards the goal
                    
                    /**
                     * Very slight perturbation of velocity to remove deadlock problems of perfect symmetry
                     */
                     prefVelocity.x+= Math.random()*0.000000001;
                     prefVelocity.y+= Math.random()*0.000000001;
 //                
 // if(id==0){
 //     System.out.println();
 // }
                     
                     Vector2d tempVelocity = velocityCalc.calculateVelocity(RVOAgent.this, sensedNeighbours, mySpace.senseObstacles(RVOAgent.this),
                             prefVelocity, PropertySet.TIMESTEP);
                     chosenVelocity = new PrecisePoint(tempVelocity.getX(), tempVelocity.getY());
 
                }
             }//end of if(!dead)
         }//end of step(ss)
 
         private void determineInitialLists(List<RVOAgent> sortedList, Bag sensedNeighbours) {
             List<Double> distanceScoreList = new ArrayList<Double>();
             for (Object temp: sensedNeighbours) {
                RVOAgent tempAgent = (RVOAgent) temp;
                 Vector2d distanceVector = new Vector2d(tempAgent.getCurrentPosition());
                 distanceVector.sub(RVOAgent.this.getCurrentPosition());
 
 //                        double distance = distanceVector.length();
                 double distanceScore = assignDistanceScore(distanceVector, tempAgent);
                 boolean added = false;
                 int j = 0;
 //                        if (tempAgent.getCurrentPosition().getX() < 2.5) {
 //                            System.out.println("test");
 //                        }
                 for (; j < distanceScoreList.size(); j++) {
 
                     if (tempAgent.equals( // don't sense the same element
                             RVOAgent.this)
                             || (tempAgent.getCurrentPosition().equals(
                             sortedList.get(j).getCurrentPosition())
                             && tempAgent.getRadius() == sortedList.get(j).getRadius())) {
                         added = true;
                         break;
                     }
 
 
                     if (distanceScoreList.get(j).compareTo(new Double(distanceScore)) < 0) { // this item is closer than exisiting
                         distanceScoreList.add(j, new Double(distanceScore));      // insert the element into the list
                         sortedList.add(j, tempAgent);
                         added = true;
                         break;
                     } else if (distanceScoreList.get(j).equals(distanceScore)) { // this item is closer than exisiting
                         if (tempAgent.getVelocity().dot(sortedList.get(j).getVelocity()) < 0) {
                             if (tempAgent.getVelocity().dot(RVOAgent.this.getVelocity()) < 0) {
                                 // the currently sensed agent is moving in the opposite direction of my agent
 
                                 distanceScoreList.add(j, new Double(distanceScore));      // insert the element into the list
                                 sortedList.add(j, tempAgent);
                                 added = true;
                                 break;
                             } else {
                                 continue;
                             }
                         }
 
                         Vector2d distanceCheck = new Vector2d(sortedList.get(j).getCurrentPosition());
                         distanceCheck.sub(RVOAgent.this.getCurrentPosition());
                         double sortedDistance = distanceCheck.length() - sortedList.get(j).getRadius();
                         double tempDistance = distanceVector.length() - tempAgent.getRadius();
 
                         if (tempDistance < sortedDistance) {
 
                             distanceScoreList.add(j, new Double(distanceScore));      // insert the element into the list
                             sortedList.add(j, tempAgent);
                             added = true;
                             break;
                         }
                     }
                 }
                 if (!added) {
                     distanceScoreList.add(j, new Double(distanceScore));      // insert the element into the list
                     sortedList.add(j, tempAgent);
                 }
             }
             for (int j = 0; j < sortedList.size(); j++) {
                 if (sortedList.get(j).getRadius() > 0.16) {
                     RVOAgent clusteredAgent = (RVOAgent) sortedList.get(j);
                     for (int k = 0; k < sortedList.size(); k++) {
                         if (j != k) {
                             RVOAgent tempAgent = (RVOAgent) sortedList.get(k);
                             Vector2d distanceCheck = new Vector2d(clusteredAgent.getCurrentPosition());
                             distanceCheck.sub(tempAgent.getCurrentPosition());
                             if (distanceCheck.length() + tempAgent.getRadius() <= clusteredAgent.getRadius()) {
                                 sortedList.remove(k);
                                 distanceScoreList.remove(k);
                                 k--;
                             }
                         }
                     }
                 }
             }
             double accumulatedScore = 0.0;
             int indexToBeDeleted = 0;
             for (; indexToBeDeleted < distanceScoreList.size(); indexToBeDeleted++) {
                 accumulatedScore += distanceScoreList.get(indexToBeDeleted);
                 if (accumulatedScore >= RVOAgent.INFO_LIMIT) {
                     break;
                 }
             }
             for (int j = distanceScoreList.size() - 1; j > indexToBeDeleted; j--) {
                 distanceScoreList.remove(indexToBeDeleted);
                 sortedList.remove(j);
             }
         }
 
         private double assignDistanceScore(Vector2d distanceVector, RVOAgent tempAgent) {
 
             double distance = (distanceVector.length() - tempAgent.getRadius() - RVOAgent.RADIUS);
             if (distance < 0) {
                 return 1.5; //1.5? interesting effect
             }
 //            double distanceScore = RVOAgent.INFO_LIMIT- (RVOAgent.INFO_LIMIT/10.0)*Math.log((RVOAgent.INFO_LIMIT/10.0)*distanceVector.length());
             double distanceScore = Math.max(Math.min(1.0f, (Math.expm1(5.0f / distance) - 0.11)), 0.1);
 
             Vector2d angleFormedVector = new Vector2d(RVOAgent.this.getGoal());
             angleFormedVector.sub(RVOAgent.this.currentPosition.toPoint());
             double angleFormed = angleFormedVector.dot(distanceVector);
             double angleScore = 0.0f;
             if (angleFormed < 0) {
                 angleScore = 0.1f;
             } else {
                 angleFormed = angleFormed / (distanceVector.length() * angleFormedVector.length());
                 angleFormed = Math.acos(angleFormed);
 
 
                 if (angleFormed < Math.PI / 3.0f) {
                     angleScore = 1.0f;
 
                 } else if (angleFormed < 4.0f * Math.PI / 9.0f) {
                     angleScore = (8.1f * (3 * angleFormed - Math.PI) / (Math.PI)) + 0.1f;
                 } else {
                     angleScore = 0.1f;
                 }
             }
 //            if (RVOAgent.this.getId() == 0) {
 //                System.out.println(tempAgent.getCurrentPosition().getX() + "," + angleFormed * 180.0f / Math.PI + " as= " + angleScore);
 //                //    System.out.println(distanceScore+" ds= "+distanceScore);
 //            }
 
 //            return (distanceScore + angleScore) / 2.0f;
             return (distanceScore > 1.0 ? distanceScore : distanceScore * angleScore);
         }
     }
 
     
     /**
      * updates the actual position after calculation. The division of steps is to 
      * ensure that all agents update their positions and move simultaneously. 
      * Implementation of Removable step is to make sure agents die after exiting 
      * the simulation area
      */
     public class Act implements Steppable {
 
         @Override
         public void step(SimState ss) {
             if (!dead) {
 
 //            if (reachedGoal()) {
 //                currentPosition = new Point2d(-4000, 4000);
 //                goal = new Point2d(-4000, 4000);
 //                //             currentGoal++;
 //                actStoppable.stop();
 //                return;
 //            }
                 velocity = chosenVelocity;
                 
                 double currentPosition_x = (currentPosition.getX()
                         + velocity.getX() * PropertySet.TIMESTEP);
                 double currentPosition_y = (currentPosition.getY()
                         + velocity.getY() * PropertySet.TIMESTEP);
                 setCurrentPosition(currentPosition_x, currentPosition_y);
                 getMySpace().updatePositionOnMap(RVOAgent.this, currentPosition_x,
                         currentPosition_y);
             }
         }
     }
 
     public class MyProxy {
 
         public Vector2d getVelocity() {
            if(velocity!=null){
                return velocity.toVector();
            }else {
                return new Vector2d();
            }
            
         }
 
         public Vector2d getPrefVelocity() {
             return prefVelocity;
         }
 
         public Point2d getPosition() {
             return currentPosition.toPoint();
         }
 
         public Point2d getGoal() {
             return goal;
         }
 
         public Point2d getCurrentGoal() {
             return currentGoalPoint;
         }
         
         public double getPreferredSpeed() {
             return RVOAgent.this.preferredSpeed;
         }
         
         public String getName() {
             return "Agent "+RVOAgent.this.getId();
         }
         
       
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final RVOAgent other = (RVOAgent) obj;
         if (this.id != other.id) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 67 * hash + this.id;
         return hash;
     }
     
     
 }
