 package vooga.towerdefense.factories.elementfactories;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.List;
 import util.Location;
 import util.Pixmap;
 import vooga.towerdefense.action.Action;
 import vooga.towerdefense.action.actionlist.MoveToTarget;
 import vooga.towerdefense.attributes.AttributeConstantsEnum;
 import vooga.towerdefense.attributes.AttributeManager;
 import vooga.towerdefense.attributes.DefaultAttributeManager;
 import vooga.towerdefense.factories.actionfactories.ActionFactory;
 import vooga.towerdefense.factories.attributefactories.AttributeFactory;
 import vooga.towerdefense.factories.attributefactories.AttributeManagerFactory;
 import vooga.towerdefense.factories.definitions.GameElementDefinition;
 import vooga.towerdefense.gameelements.GameElement;
 import vooga.towerdefense.model.GameMap;
 
 
 /**
  * A factory that creates game elements based on preset data
  * Reads from an xmlfile (eventually)
  * 
  * @author Matthew Roy
  * @author Xu Rui
  * @author Zhen Gou
  */
 public class GameElementFactory {
 
     // private static DefaultAttributeManager DEFAULT_ATTRIBUTE_MANAGER =
     // new DefaultAttributeManager();
 
     /**
      * Name of the element that is defined in this class. For convenience.
      */
     private String myName;
     private GameElementDefinition myDef;
     private List<ActionFactory> myActionsToMake;
     private GameMap myMap;
 
     private Pixmap myImage;
     private String myType;
     private Dimension mySize;
     private AttributeManagerFactory myAttributeManagerFactory;
 
     /**
      * complete constructor
      * 
      * @param name
      * @param image
      * @param location
      * @param size
      * @param attrManager
      */
 
     public GameElementFactory (String name, String type,
                                Pixmap image, Dimension size,
                                AttributeManagerFactory attrManager, List<ActionFactory> myActions) {
         myName = name;
         myType = type;
         myImage = image;
         mySize = size;
         myAttributeManagerFactory = attrManager;
         myActionsToMake = myActions;
         myAttributeManagerFactory.addAttributeFactory(makeAffiliation());
     }
 
     /**
      * returns the type of this factory.
      * 
      * @return a string representing the type
      */
     public String getType () {
         return myType;
     }
 
     private AttributeFactory makeAffiliation () {
         AttributeConstantsEnum affliationValue = AttributeConstantsEnum.valueOf(myType);
         AttributeFactory affiliation =
                 new AttributeFactory(
                                      AttributeConstantsEnum.AFFILIATION.getStatusCode(),
                                      affliationValue.getValue());
         return affiliation;
     }
 
     @Deprecated
     public GameElementFactory (String name, Pixmap image) {
         myName = name;
         myImage = image;
         myAttributeManagerFactory = new AttributeManagerFactory();
         mySize = new Dimension(50, 50);// TODO: just for testing before having the xml loader
                                        // working
 
     }
 
     @Deprecated
     public GameElementFactory (String name, String type, GameElementDefinition definition) {
         this(name, type, definition.getImage(), definition.getSize(), definition
                 .getAttributeManagerFactory(), definition.getActions());
         myName = name;
         myDef = definition;
     }
 
     /**
      * must be called before create element
      * 
      * @param map
      */
     public void initialize (GameMap map) {
         myMap = map;
     }
 
     public GameMap getMap () {
         return myMap;
     }
 
     public String getName () {
         return myName;
     }
 
     @Deprecated
     public GameElementDefinition getDefinition () {
         return myDef;
     }
 
     /**
      * For testing only
      * 
      * @param def
      */
     @Deprecated
     public void setDefinition (GameElementDefinition def) {
         myDef = def;
     }
 
     public AttributeManager createAttributeFactory () {
         return myAttributeManagerFactory.makeAttributeManager();
     }
 
     public void setActionFactories (List<ActionFactory> actionsToMake) {
         myActionsToMake = actionsToMake;
     }
 
     /**
      * Creates a list of actions for a specific element
      * 
      * @param e element to base actions around
      * @return list of the actions for that element
      */
     public List<Action> createActions (GameElement element) {
         List<Action> actions = new ArrayList<Action>();
         for (ActionFactory a : myActionsToMake) {
             System.out.println(a);
                actions.add(a.createAction(element));
         }
         return actions;
     }
 
     /**
      * Creates a game element at a location
      * 
      * @param spawnLocation
      * @return
      */
     public GameElement createElement (Location spawnLocation) {
         GameElement element = new GameElement(myImage,
                                               spawnLocation,
                                               mySize,
                                               createAttributeFactory());
         element.addActions(createActions(element));
         return element;
     }
 
     public GameElement createElement (Location spawn, GameElement target) {
         GameElement projectile =
                 new GameElement(myImage,
                                 spawn, mySize, myAttributeManagerFactory.makeAttributeManager());
         projectile.addActions(createActions(projectile));
 
         List<Action> actions = new ArrayList<Action>();
         actions.add(new MoveToTarget(projectile.getCenter(),
                                      target.getCenter(), projectile.getAttributeManager()
                                              .getAttribute(AttributeConstantsEnum.MOVE_SPEED
                                                            .getStatusCode())));
         projectile.addActions(actions);
         return projectile;
     }
 }
