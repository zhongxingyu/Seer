 package Learning.Towers;
 
 import Learning.Towers.Behaviours.Constructive.Construction;
 import Learning.Towers.Entities.Entity;
 import Learning.Towers.Entities.EntityFactory;
 import Learning.Towers.Entities.Meta.Group;
 
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Piers
  * Date: 19/10/12
  * Time: 16:42
  */
 public class Faction {
     private final static Random random = new Random();
 
     private ArrayList<Group> groups;
     private Dictionary<Construction, Group> baseGroup;
 
     private float r, g, b;
 
     protected Faction(float r, float g, float b) {
         this.r = r;
         this.g = g;
         this.b = b;
 
         groups = new ArrayList<>();
         baseGroup = new Hashtable<>();
 
         Entity base = EntityFactory.getBase(this, Utilities.randomLocation(200), 100);
         Group newGroup = new Group(r, g, b, base.getConstructionBehaviour().getSpawnPoint(), 5, this);
         baseGroup.put(base.getConstructionBehaviour(), newGroup);
 
         Main.GAME_LOOP.addEntity(base);
         Main.GAME_LOOP.addEntity(newGroup);
     }
 
     public void makeEntity(Vector2D location, Construction base){
         makeEntity(location.x, location.y, base);
     }
 
     public void makeEntity(double x, double y, Construction base) {
         // Create new entity
         Entity entity = EntityFactory.getGroupedEntity(this, baseGroup.get(base), new Vector2D(x, y), 2);
 
         // Add entity to group
         baseGroup.get(base).addEntity(entity);
         // register entity with game
         Main.GAME_LOOP.addEntity(entity);
 
         // House keeping on the groups
         if (baseGroup.get(base).isFull()) { ;
             Group newGroup = new Group(r, g, b, base.getSpawnPoint(), 5, this);
             groups.add(baseGroup.get(base));
             baseGroup.get(base).switchToWander();
             baseGroup.put(base, newGroup);
             Main.GAME_LOOP.addEntity(newGroup);
         }
     }
 
     public void addConstruction(Construction construction, Vector2D spawnPoint){
        if(baseGroup.get(construction) == null){
             Group group = new Group(r, g, b, spawnPoint, 20, this);
             Main.GAME_LOOP.addEntity(group);
             baseGroup.put(construction, group);
         }
     }
 
     public void update() {
 
     }
 
     public float getR() {
         return r;
     }
 
     public float getG() {
         return g;
     }
 
     public float getB() {
         return b;
     }
 }
 
 
 
