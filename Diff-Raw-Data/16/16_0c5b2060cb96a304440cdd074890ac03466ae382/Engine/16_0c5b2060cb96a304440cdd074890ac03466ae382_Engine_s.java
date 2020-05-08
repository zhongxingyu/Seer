 /**
  * Engine.java
  * 
  * Created on 14.05.2013
  */
 
 package at.pria.koza.harmonic;
 
 
 import static java.lang.String.*;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 
 /**
  * <p>
  * The class {@code Engine} represents a graph of objects that describes the application. The Engine's State is
  * modified by Actions.
  * </p>
  * 
  * @version V1.0 26.07.2013
  * @author SillyFreak
  */
 public class Engine {
     private static Random random;
     
     private static Random getRandom() {
         if(random == null) random = new Random();
         return random;
     }
     
     private static int nextNonZeroInt() {
         Random r = getRandom();
         int result;
         for(;;)
             if((result = r.nextInt()) != 0) return result;
     }
     
     private int                  id;
     private long                 nextStateId;
     
     private int                  nextEntityId = 0;
     private Map<Integer, Entity> entities     = new HashMap<>();
     
     private final State          root;
     private Branch               head;
     
     /**
      * <p>
      * Creates a non-spectating engine.
      * </p>
      */
     public Engine() {
         this(false);
     }
     
     /**
      * <p>
      * Creates an engine.
      * </p>
      * 
      * @param spectating whether this engine will only spectate or also execute actions
      */
     public Engine(boolean spectating) {
         this(spectating? 0:nextNonZeroInt());
     }
     
     /**
      * <p>
      * Creates an engine with the given ID. Spectating engines have an ID equal to zero.
      * </p>
      * 
      * @param id the engine's ID.
      */
     public Engine(int id) {
         this.id = id;
         nextStateId = (id & 0xFFFFFFFFl) << 32;
         root = new State(this, null, 0l, null);
         setHead(root);
     }
     
     long nextStateId() {
         return nextStateId++;
     }
     
     /**
      * <p>
      * Returns this engine's ID. An engine that is only spectating (i.e. receiving actions, but not sending any)
      * may have an ID of 0. Other engines have a non-zero random 32 bit ID.
      * </p>
      * <p>
      * This ID is used to prevent conflicts in IDs of states created by this engine: Instead of assigning random
      * IDs to states and hoping that no state IDs in an engine's execution ever clash, random IDs are only assigned
      * to engines, and states get IDs based on these. As the set of engines is relatively stable during the
      * execution of an application, as opposed to the set of states, this scheme is safer.
      * </p>
      * 
      * @return this engine's ID
      */
     public int getId() {
         return id;
     }
     
     public void setHead(State head) {
         setHead(new Branch(head));
     }
     
     public void setHead(Branch head) {
         if(head == null) throw new IllegalArgumentException();
         this.head = head;
     }
     
     public Branch getHead() {
         return head;
     }
     
     /**
      * <p>
      * Adds an entity to this engine, assigning it a unique id.
      * </p>
      * 
      * @param entity the entity to register in this engine
      */
     public void put(Entity entity) {
         new RegisterEntity(this, entity).apply();
     }
     
     /**
      * <p>
      * Returns the entity associated with the given ID.
      * </p>
      * 
      * @param id the ID to resolve
      * @return the entity that is associated with the ID, or {@code null}
      */
     public Entity get(int id) {
         return entities.get(id);
     }
     
     @Override
     public String toString() {
         return format("%s@%08X", getClass().getSimpleName(), id);
     }
     
     private static class RegisterEntity extends Modification {
         private final Engine engine;
         private final Entity entity;
         
         public RegisterEntity(Engine engine, Entity entity) {
             this.engine = engine;
             this.entity = entity;
         }
         
         @Override
         protected void apply0() {
             int id = engine.nextEntityId++;
             entity.setEngine(engine, id);
             engine.entities.put(id, entity);
         }
         
         @Override
         public void revert() {
             engine.entities.remove(entity.getId());
             entity.setEngine(null, -1);
             engine.nextEntityId--;
         }
     }
 }
