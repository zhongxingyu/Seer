 package galapagos;
 
 import java.util.*;
 import java.lang.*;
 
 public class Biotope extends Observable {
     public final int width, height;
     private final double breedingProbability;
     private final int maxHitpoints, initialHitpoints, hitpointsPerRound;
     private final int minMaxAge, maxMaxAge;
     private final int finchesPerBehavior;
     private int round;
     public final World<GalapagosFinch> world;
     private final TreeMap<String,Statistics> statisticsTree;
     private final List<Behavior> finchBehaviors;
 
     private final static int HelpedGotHelpValue = 3;
     private final static int HelpedDidntGetHelpValue = 0;
     private final static int DidntHelpGotHelpValue = 5;
     private final static int DidntHelpDidntGetHelpValue = 1;
     private ArrayList<Boolean> engagedFinches;
     
     /**
      * Create a Biotope object with some sensible default environment
      * values.
      *
      * @param behaviors A list of behavior objects that will be cloned
      * to create the actual behaviors for the finches.
      */
     public Biotope (List<Behavior> behaviors) {
         this(100, 100, 1.00/3.00, 12, 7, 3, 10, 13, 40, behaviors);
     }
     
     /**
      * Create a Biotope object with the environment values specified
      * by the arguments.
      *
      * @param width The width of the simulated game world in
      * cells. Each cell can hold a single finch.
      * @param height The height of the simulated game world in
      * cells. Each cell can hold a single finch.
      * @param breedingProbability The chance each finch has to create
      * offspring each round.
      * @param maxHitpoints The maximum number of hit points finches
      * can attain through being claned.
      * @param initialHitpoints The number of hit points a new finch
      * will have.
      * @param hitpointsPerRound The number of hit points a finch will
      * loose each round.
      * @param minMaxAge The lower bound on the randomly determined
      * maximum age of finches in rounds.
      * @param maxMaxAge The upper bound on the randomly determined
      * maximum age of finches in rounds.
      * @param finchesPerBehavior The number of finches that will
      * initially be created for each behavior.
      * @param behaviors A list of behavior objects that will be cloned
      * to create the actual behaviors for the finches.
      * 
      * @require 0 < width
      * @require 0 < height
      * @require 0.0 <= breedingProbability <= 1.0
      * @require 0 < initialHitpoints <= maxHitpoints
      * @require 0 <= hitPointsPerRound
      * @require 1 < minMaxAge <= maxMaxAge
      * @require 0 <= finchesPerBehavior
      */
     public Biotope (int width, int height, double breedingProbability, int maxHitpoints, 
             int initialHitpoints, int hitpointsPerRound, int minMaxAge, int maxMaxAge,
             int finchesPerBehavior, List<Behavior> behaviors) {
         assert (0.0 <= breedingProbability && breedingProbability <= 1.0) 
             : "breedingProbability must be between 0 and 1, inclusive.";
         assert (0 < initialHitpoints)
             : "initialHitpoints must be greater than zero.";
        assert (initialHitpoints < maxHitpoints)
             : "maxHitpoints must be greater than initialHitpoints.";
         assert (0 <= hitpointsPerRound)
             : "hitPointsPerRound must be greater than zero.";
         assert (0 < minMaxAge)
             : "The lower bound on the maximum age must be greater than zero";
         assert (minMaxAge <= maxMaxAge)
             : "The upper bound on the maximum age must be greater than the lower bound.";
         assert (0 <= finchesPerBehavior)
             : "At least one finch per behavior must be specified.";
         this.width = width;
         this.height = height;
         this.breedingProbability = breedingProbability;
         this.maxHitpoints = maxHitpoints;
         this.initialHitpoints = initialHitpoints;
         this.hitpointsPerRound = hitpointsPerRound;
         this.minMaxAge = minMaxAge;
         this.maxMaxAge = maxMaxAge;
         this.finchesPerBehavior = finchesPerBehavior;
         this.finchBehaviors = behaviors;
         world = new World<GalapagosFinch>(width, height);
         statisticsTree = new TreeMap<String,Statistics>();
         engagedFinches = new ArrayList<Boolean>(width * height);
         
         for (int i = 0; i < width * height; i++)
             engagedFinches.add(false);
         
         addStartFinches();
     }
 
     /**
      * Add the initial finches using the values for finchesPerBehavior
      * and the list of behaviors.
      */
     private void addStartFinches()
     {
         Iterator<World<GalapagosFinch>.Place> worldIterator = world.randomIterator();
         for (Iterator<Behavior> bIterator = finchBehaviors.iterator();
              bIterator.hasNext();)
         {
             Behavior b = bIterator.next();
             statisticsTree.put(b.toString(),new Statistics());
             
             
             for (int i = 0;i < finchesPerBehavior && worldIterator.hasNext();i++)
                 {   
                     World<GalapagosFinch>.Place p = worldIterator.next();
                     placeFinch(p,b,false);
                 }
         }
     }
     
     /**
      * Place a new finch of the provided behavior at the given place.
      *
      * @param p Where the new finch will be put in the world.
      * @param b A behavior object that will be cloned to get the
      * behavior for the new finch.
      * @param born If true, the finch is considered to be new-born,
      * affecting statistics, and making sure that it will not breed
      * until the next round.
      *
      * @ensure p.getElement() == null
      */
     private void placeFinch (World<GalapagosFinch>.Place p, Behavior b, Boolean born) {
         Statistics stat = statisticsTree.get(b.toString());
         stat.incPopulation();
         GalapagosFinch finch = new GalapagosFinch(initialHitpoints,
                                                   maxHitpoints,
                                                   randomMaxAge(),
                                                   b.clone());
         if (born) 
             stat.incBorn();
         else
             finch.makeOlder();
         p.setElement(finch);
     }
     
     /**
      * Put a new finch in the world at position x, y. The finch will
      * have the provided behavior, and it will overwrite any already
      * existing finch at the position.
      */
     public void putFinch (int x, int y, Behavior b) {
         World.Place p = world.getAt(x, y);
         // If we have a finch here already, subtract it from the total
         // population of that type.
         if (p.getElement() != null) {
             Statistics s = statisticsTree.get(((GalapagosFinch)p.getElement()).behavior().toString());
             s.decPopulation();
         }
         placeFinch(p, b, false);
         setChanged();
         notifyObservers();
     }
 
     /**
      * Return a random age for a new finch, based on maxMaxAge and
      * minMaxAge.
      *
      * @ensure minMaxAge < randomMaxAge() < maxMaxAge;
      */
     private int randomMaxAge () {
         return minMaxAge + (int)(Math.random() * (maxMaxAge - minMaxAge));
     }
 
     /**
      * Run a single round in the simulation, consisting of updating
      * statistics, randomly creating offspring, arrange meetings
      * between finches, age and possibly kill finches and notify any
      * observers.
      */
     public void runRound () {
         for (Statistics stat : statisticsTree.values())
             stat.newRound();
         breed();
         makeMeetings();
         grimReaper();
         round++;
         setChanged();
         notifyObservers();
     }
     
     /**
      * Let some of the finches breed, if possible (a finch needs an
      * empty neighbour place to breed).
      */
     private void breed () {
         for (Iterator<World<GalapagosFinch>.Place> i = world.randomIterator(); i.hasNext();) {
             World<GalapagosFinch>.Place place = i.next();
             GalapagosFinch finch = place.getElement();
             if (finch != null && finch.age() > 0 && Math.random() <= breedingProbability) {
                 List<World<GalapagosFinch>.Place> neighbours = place.emptyNeighbours(); 
                 if (!neighbours.isEmpty())
                     placeFinch(neighbours.get(0), finch.behavior().clone(), true);
             }
         }
     }
     
     /**
      * Sets up random meetings between the finches of the world.
      */
     private void makeMeetings() {
         clearEngagementKnowledge();
         for (Iterator i = world.randomIterator(); i.hasNext(); ) {
             World<GalapagosFinch>.Place place = (World.Place) i.next();
             if(place.getElement() != null && !isEngaged(place))
                 makeMeeting(place);
         }
     }
 
     /**
      * Engages the finch at the specified place with one of its neighbours (if any). 
      * And makes them meet eachother.
      * @require place.getElement() != null && !isEngaged(place)
      * @ensure isEngaged(place)
      * @param place the place holding the unengaged finch
      */
     private void makeMeeting(World<GalapagosFinch>.Place place) {
         assert (place.getElement() != null) : "Can't engage a null-finch";
         assert (!isEngaged(place)) : "The finch is already engaged";
 
         List<World<GalapagosFinch>.Place> filledNeighbours = place.filledNeighbours(); 
 
         for (World<GalapagosFinch>.Place p : filledNeighbours)
             if (!isEngaged(p)) {
                 engage(p);
                 engage(place);
                 meet(place.getElement(), p.getElement());
                 return;
             }
     }
 
     /**
      * Clear the information about which finches have already met this
      * round.
      */
     private void clearEngagementKnowledge() {
         for (int i = 0; i < engagedFinches.size(); i++)
             engagedFinches.set(i, false);
     }
 
     /**
      * Register the fact that the finch at place has been involved in
      * a meeting this round.
      *
      * @require place.getElement() != null
      */
     private void engage(World.Place place) {
         assert place.getElement() != null
             : "Cannot register an empty place as having participated in a meeting.";
         engagedFinches.set(place.xPosition() * height + place.yPosition(), true);
     }
 
     /**
      * Return true if place has already been engaged in a meeting.
      */
     private boolean isEngaged(World.Place place) {
         return engagedFinches.get(place.xPosition() * height + place.yPosition());
     }
     
     /**
      * Perform a meeting between two finches, letting them act on each
      * other and inform them of the other finch's action. The actions
      * will be performed in parallel, not sequentially, so there is no
      * bias towards providing one finch or the other with information
      * about what the other finch has done.
      */
     private void meet(GalapagosFinch finch1, GalapagosFinch finch2) {
         //let both finches decide if they won't to help the other finch
         Action finch1Action = finch1.decide(finch2);
         Action finch2Action = finch2.decide(finch1);
         
         finch1.changeHitpoints(getMeetingResult(finch1Action, finch2Action));
         finch2.changeHitpoints(getMeetingResult(finch2Action, finch1Action));
         
         //tell the finches what was done to them (so they eventually can learn)
         finch1.response(finch2, finch2Action);
         finch2.response(finch1, finch1Action);
     }
 
     /**
      * Get the hit point increase value for performing finch1Action
      * while having finch2Action performed on you.
      */
     private int getMeetingResult(Action finch1Action, Action finch2Action) {
         if (finch1Action == Action.CLEANING) {
             if (finch2Action == Action.CLEANING) {
                 return HelpedGotHelpValue;
             }
             else if (finch2Action == Action.IGNORING) {
                 return HelpedDidntGetHelpValue;
             }
         } else if (finch1Action == Action.IGNORING) {
             if (finch2Action == Action.CLEANING) {
                 return DidntHelpGotHelpValue;
             }
             else if (finch2Action == Action.IGNORING) {
                 return DidntHelpDidntGetHelpValue;
             }
         }
         throw new Error("Unhandled Action combination");
     }
     
     /**
      * Decrease the hitpoints of all finches by hitpointsPerRound, and
      * find dead finches and remove them from world, and store the
      * changes in statisticsTree.
      */
     private void grimReaper () {
         for (World<GalapagosFinch>.Place p : world) if (p.getElement() != null) {
             GalapagosFinch f = p.getElement();
             f.changeHitpoints(-hitpointsPerRound);
             f.makeOlder();
             FinchStatus newStatus = f.status();
             if (newStatus != FinchStatus.ALIVE) {
                 Statistics s = statisticsTree.get(f.behavior().toString());
                 if (newStatus == FinchStatus.DEAD_AGE)
                     s.incDeadByAge();
                     else s.incDeadByTicks();
                 s.decPopulation();
                 p.setElement(null);
             }
         }
     }
     
     /**
      * Return the statistics for the behavior with the specified name.
      *
      * @require behavior must be the name of a known Behavior.
      */
     public Statistics statistics(String behavior) {
         assert(statisticsTree.containsKey(behavior))
             : "Asked to retrieve statistics for unknown behavior.";
         return statisticsTree.get(behavior.toString());
     }
     
     /**
      * Get the current round number.
      *
      * @ensure 0 < round()
      */
     public int round () {
         return round;
     }
     
     /**
      * Return an iterator for walking through the places of the world
      * of this Biotope object.
      */
     public Iterator<World<GalapagosFinch>.Place> worldIterator () {
         return world.iterator();
     }
     
     /**
      * Get a list of all the behaviors in the Biotope.
      */
     public List<Behavior> behaviors () {
         return finchBehaviors;
     }
     
     /**
      * Notify the observers of the Biotope.
      */
     public void doNotifyObservers () {
         setChanged();
         notifyObservers();
     }
 }
