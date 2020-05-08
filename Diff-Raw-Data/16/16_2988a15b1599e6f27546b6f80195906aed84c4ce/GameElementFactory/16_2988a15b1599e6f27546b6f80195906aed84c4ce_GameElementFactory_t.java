 package vooga.towerdefense.factories;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import vooga.towerdefense.action.Action;
import vooga.towerdefense.action.actionlist.MoveToTarget;
 import vooga.towerdefense.attributes.AttributeConstants;
 import vooga.towerdefense.factories.actionfactories.ActionFactory;
 import vooga.towerdefense.gameElements.GameElement;
 import vooga.towerdefense.model.GameMap;
 import vooga.towerdefense.util.Location;
 
 /**
  * A factory that creates game elements based on preset data
  * Reads from an xmlfile (eventually)
  * @author Matthew Roy
  * @author Xu Rui
  *
  */
 public class GameElementFactory {
     /**
      * Name of the element that is defined in this class. For convenience.
      */
     private String myName;
     protected GameElementDefinition myDef; 
     private List<ActionFactory> myActionsToMake;
     private GameMap myMap;
     
     public GameElementFactory() {
         myActionsToMake = new ArrayList<ActionFactory>();
     }
     
     public GameElementFactory(String name, GameElementDefinition definition) {
         this();
         myName = name;
         myDef = definition;
     }
     
     public GameElementFactory(GameElementDefinition definition) {
         this(definition.get(AttributeConstants.NAME), definition);
     }
     
     public void initialize(GameMap map) {
         myMap = map;
     }
     
     public GameMap getMap() {
         return myMap;
     }
     
     public String getName() {
         return myName;
     }
     
     public GameElementDefinition getDefinition() {
         return myDef;
     }
     
     /**
      * For testing only
      * @param def
      */
     @Deprecated
     public void setDefinition(GameElementDefinition def) {
         myDef = def;
     }
     
     public AttributeManagerFactory createAttributeFactory() {
         AttributeManagerFactory factory = new AttributeManagerFactory();
         return factory;
     }
     
     public void setActionFactories(List<ActionFactory> actionsToMake) {
         myActionsToMake = actionsToMake;
     }
     
     /**
      * Creates a list of actions for a specific element
      * @param e element to base actions around
      * @return list of the actions for that element
      */
     public List<Action> createActions(GameElement element) {
         List<Action> actions = new ArrayList<Action>();
         for (ActionFactory a : myActionsToMake) {
             actions.add(a.createAction(element));
         }
         return actions;
     }
     
     /**
      * Creates a new game element
      * @return
      */
     public GameElement createElement(){
         if (myDef == null) {
             return null;
         }
         GameElement element = new GameElement(myDef.getImage(), 
                                               myDef.getCenter(), 
                                               myDef.getSize(), 
                                               createAttributeFactory().makeAttributeManager(),
                                               myDef.getType());
         element.addActions(createActions(element));
         return element;
     }
 
     /**
      * @param spawnLocation
      * @return 
      */
     public GameElement createElement (Location spawnLocation) {
         GameElement element = new GameElement(myDef.getImage(), 
                                               spawnLocation, 
                                               myDef.getSize(), 
                                               createAttributeFactory().makeAttributeManager(),
                                               myDef.getType());
         element.addActions(createActions(element));
         return element;
     }
    
    public GameElement createElement (Location spawn, Location Target,
        Location targetLocation) {
            GameElement projectile = new GameElement(myDef.myImage,
                            spawn, myDef.getSize(), createAttributeFactory().makeAttributeManager(), getName());
            projectile.addActions(createActions(projectile));

            List<Action> actions = new ArrayList<Action>();
            actions.add(new MoveToTarget(projectile.getCenter(),
                            targetLocation, projectile.getAttributeManager().getAttribute(
                                            AttributeConstants.MOVE_SPEED)));
            projectile.addActions(actions);
            return projectile;
    }
 
 }
