 package app;
 
 import app.dataTracking.CWDataCollector;
 import agent.AgentGenerator;
 import agent.RVOAgent;
 import agent.RVOAgent.Act;
 import agent.RVOAgent.SenseThink;
 import agent.clustering.ClusteredSpace;
 import environment.Obstacle.RVOObstacle;
 import environment.RVOSpace;
 import environment.XMLScenarioManager;
 import environment.geography.Agent;
 import environment.geography.AgentLine;
 import environment.geography.Goals;
 import environment.geography.Obstacle;
 import environment.geography.SimulationScenario;
 import agent.latticegas.LatticeSpace;
 import app.PropertySet.Model;
 import app.dataTracking.DataTracker;
 import environment.geography.AgentGroup;
 import environment.geography.Position;
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.vecmath.Point2d;
 import javax.xml.bind.JAXBException;
 import motionPlanners.socialforce.SocialForce;
 import sim.engine.RandomSequence;
 import sim.engine.Schedule;
 import sim.engine.Sequence;
 import sim.engine.SimState;
 import sim.engine.Stoppable;
 
 /**
  * RVOModel
  *
  * @author michaellees
  * Created: Nov 24, 2010
  *
  * Copyright michaellees
  *
  * Description:
  *
  * The RVOModel is the core of the program. It contains the agentList,
  * the obstacleList and the rvoSpace and also the different parameters of the
  * MODEL itself. It also contains the main().
  *
  */
 public class RVOModel extends SimState {
     
     private int worldXSize = PropertySet.WORLDXSIZE;
     private int worldYSize = PropertySet.WORLDYSIZE;
     private double gridSize = PropertySet.GRIDSIZE;
     private RVOSpace rvoSpace;
     private LatticeSpace latticeSpace;
     /**
      * Actually initialised to ArrayList as can be seen in second constructor, 
      * But using List type here so that we can change the implementation later 
      * if needed
      */
     private List<RVOAgent> agentList;
  
     private List<AgentGenerator> agentLineList;
     private Stoppable generatorStopper;
     private DataTracker dataTracker = null;
 
 //    //the list to keep record of every agent's status in each timestep
 //    //each record contains a list of status for each agent
 //    //for each agent, there is a list to record all the necessary status (e.g., velocity, position etc)
 //    public ArrayList<ArrayList<ArrayList>> records;
     public RVOModel() {
         this(PropertySet.SEED);
         
     }
     
     public RVOModel(long seed) {
         super(seed);
         if (PropertySet.INITIALISEFROMXML) {
             try {
                 XMLScenarioManager settings = XMLScenarioManager.instance("environment.geography");
                 SimulationScenario scenario = (SimulationScenario) settings.unmarshal(PropertySet.FILEPATH);
                 RVOGui.scale = scenario.getScale();
                 worldXSize = SocialForce.worldXSize = RVOGui.checkSizeX = scenario.getXsize();
                 worldYSize = SocialForce.worldYSize = RVOGui.checkSizeY = scenario.getYsize();
             } catch (JAXBException ex) {
                 Logger.getLogger(RVOModel.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         agentList = new ArrayList<RVOAgent>();
         //obstacleList = new ArrayList<RVOObstacle>();
         agentLineList = new ArrayList<AgentGenerator>();
         
     }
     
     @Override
     public void start() {
         
         super.start();
         // This function is equivalent to a reset. 
         //Need to readup a bit more to see if it is even necessary...
         setup();        
         if (PropertySet.INITIALISEFROMXML) {
             initialiseFromXML();
         } else {
             buildSpace();
             createAgents();
         }
         if(PropertySet.TRACK_DATA) {
             dataTracker = new CWDataCollector(this, agentList);
             schedule.scheduleRepeating(dataTracker, 4, 1.0);
         }
        
     }
 
     /**
      * Creates the continuous 2D space that will be used for the simulation. 
      * Creates an appropriate space for Clustering
      */
     private void buildSpace() {
         if (!PropertySet.USECLUSTERING) {
             rvoSpace = new RVOSpace(worldXSize, worldYSize, gridSize, this);
         } else {
             rvoSpace = new ClusteredSpace(worldXSize, worldYSize, gridSize, this);
         }
     }
 
     /**
      * To use RVO without using an xml to initialise layout. Arranges agents in 
      * a pre decided order
      */
     private void createAgents() {
         int numAgentsPerSide = 5;
         double gap = 1.5f;
         for (int i = 1; i < numAgentsPerSide + 1; i++) {
             addNewAgent(
                     new RVOAgent(new Point2d(2, i * gap + 0.5),
                     new Point2d(8, i * gap + 0.5),
                     rvoSpace,
                     new Color(Color.HSBtoRGB((float) i / (float) numAgentsPerSide, 1.0f, 0.68f))));
             addNewAgent(
                     new RVOAgent(new Point2d(8, i * gap + 0.5),
                     new Point2d(2, i * gap + 0.5),
                     rvoSpace,
                     new Color(Color.HSBtoRGB(0.7f - (float) i / (float) numAgentsPerSide, 1.0f, 0.68f))));
         }
         this.scheduleAgents();
 //        this.addNewAgent(new RVOAgent( new Point2d(8,3.5), new Point2d(2,3.5)
 //        , this.rvoSpace, new Color(Color.HSBtoRGB(1.0f, 1.0f, 0.68f))));
 
     }
 
     /**
      * resets all the values to the initial values. this is just to be safe.
      */
     public void setup() {
        
         rvoSpace = null;
         agentList = new ArrayList<RVOAgent>();
         //obstacleList = new ArrayList<RVOObstacle>();
         agentLineList = new ArrayList<AgentGenerator>();
         RVOAgent.agentCount = 0;
         
     }
     
     public void scheduleAgents() {
         List<SenseThink> senseThinkAgents = new ArrayList<SenseThink>();
         
         List<Act> actAgents = new ArrayList<Act>();
         for (RVOAgent agent : agentList) {
             senseThinkAgents.add(agent.getSenseThink());
             actAgents.add(agent.getAct());
         }
 
 //        senseThinkStoppable = mySpace.getRvoModel().schedule.scheduleRepeating(senseThinkAgent, 2, 1.0);
 //        actStoppable = mySpace.getRvoModel().schedule.scheduleRepeating(actAgent, 3, 1.0);
 //        (new RVOAgent(this.rvoSpace)).scheduleAgent();
         schedule.scheduleRepeating(Schedule.EPOCH, 1, new RandomSequence(senseThinkAgents.toArray(new SenseThink[]{})), 1.0);
         schedule.scheduleRepeating(Schedule.EPOCH, 2, new Sequence(actAgents.toArray(new Act[]{})), 1.0);
     }
     
     public List<RVOAgent> getAgentList() {
         return agentList;
     }
     
     public int getWorldYSize() {
         return worldYSize;
     }
     
     public int getWorldXSize() {
         return worldXSize;
     }
     
     public double getGridSize() {
         return gridSize;
     }
     
     public RVOSpace getRvoSpace() {
         return rvoSpace;
     }
     
     public LatticeSpace getLatticeSpace() {
         return latticeSpace;
     }
     
     public Stoppable getGeneratorStoppable() {
         return generatorStopper;
     }
     
     private void addNewObstacle(RVOObstacle obstacle) {
         //obstacleList.add(obstacle);
         rvoSpace.addNewObstacle(obstacle);
     }
     
     public void addNewAgent(RVOAgent a) {
         a.createSteppables();
         agentList.add(a);
         rvoSpace.updatePositionOnMap(a, a.getX(), a.getY());
         if (PropertySet.LATTICEMODEL) {
             latticeSpace.addAgentAt(a.getX(), a.getY());
         }
     }
     
     private void addNewAgentLine(AgentGenerator tempAgentLine, int frequency) {
         agentLineList.add(tempAgentLine);
         generatorStopper = schedule.scheduleRepeating(tempAgentLine, 1, (double) frequency);
     }
     
 
 
     /**
      * Initialize data from the XML file specified
      */
     private void initialiseFromXML() {
         try {
             XMLScenarioManager settings = XMLScenarioManager.instance("environment.geography");
             SimulationScenario scenario = (SimulationScenario) settings.unmarshal(PropertySet.FILEPATH);
             
             if (!PropertySet.USECLUSTERING) {
                 rvoSpace = new RVOSpace(worldXSize, worldYSize, gridSize, this);
             } else {
                 rvoSpace = new ClusteredSpace(worldXSize, worldYSize, gridSize, this);
                 ((ClusteredSpace) rvoSpace).scheduleClustering();
             }
             
             if (PropertySet.LATTICEMODEL) {
                 latticeSpace = new LatticeSpace(worldXSize, worldYSize, this);
                 latticeSpace.scheduleLattice();
                 latticeSpace.setDirection(scenario.getDirection());
             }
             
             List<Agent> xmlAgentList = scenario.getCrowd();
             for (int i = 0; i < xmlAgentList.size(); i++) {
                 Agent tempAgent = xmlAgentList.get(i);
 
                 //@hunan: added in a new RVOAgent constructor to set the necessary parameters for PBM use only
                 if (PropertySet.MODEL == Model.PatternBasedMotion) {
                     RVOAgent tempRVOAgent = new RVOAgent(
                             new Point2d(tempAgent.getPosition().getX(), tempAgent.getPosition().getY()),
                             new Point2d(tempAgent.getGoal().getX(), tempAgent.getGoal().getY()),
                             rvoSpace, new Color(0.0f, 0.0f, 1.0f),
                             tempAgent.getPreferedSpeed(), tempAgent.getCommitmentLevel());
                     
                     addNewAgent(tempRVOAgent);
                 } else {
                     RVOAgent tempRVOAgent = new RVOAgent(
                             new Point2d(tempAgent.getPosition().getX(), tempAgent.getPosition().getY()),
                             new Point2d(tempAgent.getGoal().getX(), tempAgent.getGoal().getY()),
                             rvoSpace,
                             Color.red);
                     
                     tempRVOAgent.setPreferredSpeed(tempAgent.getPreferedSpeed());
                     tempRVOAgent.setMaximumSpeed(tempAgent.getPreferedSpeed() * 2.0);
                     
                     addNewAgent(tempRVOAgent);
                 }
             }
             
             List<Goals> xmlGoalList = scenario.getEnvironmentGoals();
             if (PropertySet.LATTICEMODEL) {
                 if (xmlGoalList.size() > 0) {
                     for (Goals tempGoal : xmlGoalList) {
                         latticeSpace.addGoal(tempGoal);
                     }
                 }
             }
             
             List<Obstacle> xmlObstacleList = scenario.getObstacles();
             for (int i = 0; i < xmlObstacleList.size(); i++) {
                 Obstacle tempObst = xmlObstacleList.get(i);
                 RVOObstacle tempRvoObst = new RVOObstacle(tempObst);
                 addNewObstacle(tempRvoObst);
                 if (PropertySet.LATTICEMODEL) {
                     latticeSpace.addObstacle(tempObst);
                 }
             }
             if (PropertySet.MODEL == Model.SocialForce) {
                 SocialForce.initializeObstacleSet(xmlObstacleList);
             }
             
             
             List<Position> xmlRoadMap = scenario.getRoadMap();
             if (!xmlRoadMap.isEmpty()) {
                 List<Point2d> actualRoadMap = new ArrayList<Point2d>();
                 for (Position point : xmlRoadMap) {
                     actualRoadMap.add(new Point2d(point.getX(), point.getY()));
                 }
                 rvoSpace.addRoadMap(actualRoadMap);
             }
             
             
             List<AgentLine> xmlAgentLineList = scenario.getGenerationLines();
             for (int i = 0; i < xmlAgentLineList.size(); i++) {
                 Point2d start = new Point2d(xmlAgentLineList.get(i).getStartPoint().getX(), xmlAgentLineList.get(i).getStartPoint().getY());
                 Point2d end = new Point2d(xmlAgentLineList.get(i).getEndPoint().getX(), xmlAgentLineList.get(i).getEndPoint().getY());
                 AgentGenerator tempAgentLine = new AgentGenerator(start, end, scenario.getGenerationLines().get(i).getNumber(), scenario.getGenerationLines().get(i).getDirection(), scenario.getEnvironmentGoals(), this);
                 addNewAgentLine(tempAgentLine, scenario.getGenerationLines().get(i).getFrequency());
             }
             
             List<AgentGroup> xmlAgentGroupList = scenario.getAgentGroups();
             for (AgentGroup tempAgentGroup : xmlAgentGroupList) {
                 
                 Point2d start = new Point2d(tempAgentGroup.getStartPoint().getX(), tempAgentGroup.getStartPoint().getY());
                 Point2d end = new Point2d(tempAgentGroup.getEndPoint().getX(), tempAgentGroup.getEndPoint().getY());
                 if (tempAgentGroup.getSize() == 1) {
                     double x = (start.getX() + end.getX()) / 2;
                     double y = (start.getY() + end.getY()) / 2;
                     RVOAgent agent = new RVOAgent(this.getRvoSpace());
                     agent.setCurrentPosition(x, y);
                     
                     this.addNewAgent(agent);
                     if (rvoSpace.hasRoadMap()) {
                         agent.setGoal(rvoSpace.getFinalGoal());
                     }
                     break;
                 }
                 int numberPossibleOnWidth = (int) Math.floor((end.getX() - start.getX()) / (2.0 * RVOAgent.RADIUS));
                 int numberPossibleOnHeight = (int) Math.floor((end.getY() - start.getY()) / (2.0 * RVOAgent.RADIUS));
                 int maxPossible = numberPossibleOnWidth * numberPossibleOnHeight;
                 int spacesBetween = (int) Math.floor((maxPossible - tempAgentGroup.getSize()) / (tempAgentGroup.getSize() - 1));
                 spacesBetween++;
                 
                 final double maxSpeed = tempAgentGroup.getMaxSpeed();
                 final double minSpeed = tempAgentGroup.getMinSpeed();
                 final double meanSpeed = tempAgentGroup.getMeanSpeed();
                 final double sdevSpeed = tempAgentGroup.getSDevSpeed();
                 
                 for (double position = 1;
                         position <= maxPossible; position += spacesBetween) {
                     
                     double distanceFromStart = (RVOAgent.RADIUS + 2 * (position - 1) * RVOAgent.RADIUS);
                     
                     double x = start.getX() + (distanceFromStart % (end.getX() - start.getX()));
                     double y = (start.getY() + (((int) (distanceFromStart / (end.getX() - start.getX()))) * RVOAgent.RADIUS * 2.0));
                     
                     
                     RVOAgent agent = new RVOAgent(this.getRvoSpace());
                     agent.setCurrentPosition(x, y);
                     
                     
                     double initialSpeed = random.nextGaussian() * sdevSpeed + meanSpeed;
                     if (initialSpeed < minSpeed) {
                         initialSpeed = minSpeed;
                     } else if (initialSpeed > maxSpeed) {
                         initialSpeed = maxSpeed;
                     }
                     
                     
                     agent.setPreferredSpeed(initialSpeed);
                     agent.setMaximumSpeed(maxSpeed);
                     
                     this.addNewAgent(agent);
                     if (rvoSpace.hasRoadMap()) {
                         agent.setGoal(rvoSpace.getFinalGoal());
                     }
                     //          agent.setVelocity(agent.findPrefVelocity());
                 }
             }
         } catch (JAXBException ex) {
             Logger.getLogger(RVOModel.class.getName()).log(Level.SEVERE, null, ex);
         }
         this.scheduleAgents();
     }
 
     DataTracker getDataTracker() {
         return this.dataTracker;
     }
     
     @Override
     public void finish(){
          if(dataTracker!=null){
             dataTracker.storeToFile();
             dataTracker =null;
         }
     }
     
         public static void main(String[] args) {
         // Read tutorial 2 of mason to see what this does.. or refer to documentation of this function
         
         PropertySet.initializeProperties();
         
         doLoop(RVOModel.class, args);
         
         System.exit(0);
     }
 }
