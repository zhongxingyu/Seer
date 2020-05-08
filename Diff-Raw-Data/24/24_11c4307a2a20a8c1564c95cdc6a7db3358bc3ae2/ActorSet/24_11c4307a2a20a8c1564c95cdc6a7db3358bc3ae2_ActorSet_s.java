 package actor;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 
 import actor.ship.Bandit;
 
 /**
  * ActorSet provides a thread safe backing store for our actors
  * @author Dustin Lundquist <dustin@null-ptr.net>
  */
 public class ActorSet implements Set<Actor> {
 
     private Map<ActorId, Actor> actors;
     public final int playerId;
     private List<Queue<Actor>> addNotifyees;
     private int asteroidCount, banditCount, banditBaseCount;
 
     public ActorSet(int playerId) {
         this.playerId = playerId;
         actors = new java.util.concurrent.ConcurrentHashMap<ActorId, Actor>();
         addNotifyees = new java.util.concurrent.CopyOnWriteArrayList<Queue<Actor>>();
         asteroidCount = 0;
         banditCount = 0;
         banditBaseCount = 0;
     }
     
     public ActorSet() {
         this(0);
     }
     
     private void adjustStat(Actor a, int increment){
         if(a instanceof Asteroid){
             asteroidCount += increment;
         } else if(a instanceof Bandit){
             banditCount += increment;
         } else if(a instanceof BanditBase){
             banditBaseCount += increment;
         }
     }
 
     @Override
     /**
      * Add an actor to the ActorSet, assigning it an ActorId if one isn't already set
      */
     public boolean add(Actor a) {
         if (playerId != 0) { // Restrictive actor creation on network client
             if (!(a.getParentId() != null && actors.get(a.getParentId()) instanceof actor.ship.PlayerShip))
                 return false;
         }
         
         if (a.id == null)
             a.id = new ActorId(playerId);
 
         if (actors.containsKey(a.id))
             return false;
         
         // Set a back reference to this ActorSet for actor.add() and actor.delete()
         a.actors = this;
         
         actors.put(a.id, a);
         adjustStat(a,1);
        
         for(Queue<Actor> q: addNotifyees)
             q.offer(a);
 
         return true;
     }
     
 
     public int getAsteroidCount() {
         return asteroidCount;
     }
 
     public int getBanditCount() {
         return banditCount;
     }
     
     public int getBanditBaseCount(){
         return banditBaseCount;
     }
 
     /**
      * Adds an actor to the ActorSet, replacing the an actor with the same id if it
      * already exists. Used for network updates
      * This bypasses the normal add and any newActorConsumers (so the network clients don't send updates about the new actors they just received from the server)
      * @param a the actor to add
      */
     public void addOrReplace(Actor a) {
         if (a.id == null)
             return;
         
         a.actors = this;
         actors.put(a.id, a);        
     }
 
     @Override
     public boolean addAll(Collection<? extends Actor> list) {
         boolean changed = false;
         
         for (Actor a: list)
             if (add(a))
                 changed = true;
 
         return changed;
     }
 
     @Override
     public void clear() {
         actors.clear();
         asteroidCount = 0;
         banditCount = 0;
     }
 
     @Override
    public boolean contains(Object arg0) {
        return false;
     }
 
     public boolean contains(Actor a) {
         if (a.id == null)
             return false;
         return actors.containsKey(a.id);
     }
 
     @Override
     public boolean containsAll(Collection<?> list) {
         for (Object o: list)
             if (contains(o) == false)
                 return false;
 
         return true;
     }
 
     @Override
     public boolean isEmpty() {
         return actors.isEmpty();
     }
 
 
     @Override
     public Iterator<Actor> iterator() {
        return actors.values().iterator();
     }
 
 
     @Override
    public boolean remove(Object arg0) {
        return false;
     }
 
     public boolean remove(Actor a) {
         if (a.id == null)
             return false;
         adjustStat(a,-1);
         return actors.remove(a.id) != null;
     }
 
     @Override
     public boolean removeAll(Collection<?> list) {
         boolean changed = false;
         
         for (Object o: list)
             if (remove(o))
                 changed = true;
 
         return changed;
     }
 
 
     @Override
     public boolean retainAll(Collection<?> arg0) {
         throw new UnsupportedOperationException();
     }
 
 
     @Override
     public int size() {
         return actors.size();
     }
 
 
     @Override
     public Object[] toArray() {
         return actors.values().toArray();
     }
 
 
     @Override
     public <T> T[] toArray(T[] a) {
         return actors.values().toArray(a);
     }
 
     /**
      * Returns an Actor from the ActorSet with the provided ActorId if it exists
      * @param id
      * @return
      */
     public Actor findById(ActorId id) {
         return actors.get(id);
     }
 
     /**
      * Update all the actors in the ActorSet
      * @param frames the number of frame elapsed since the last update
      */
     public void update(int frames) {
         for (Actor a: this) {
             a.update();
         }
     }
     
     /**
      * Returns a new instance of a List containing all
      * the actors currently in the ActorSet
      * @return
      */
     public List<Actor> getCopyList() {
        return new java.util.ArrayList<Actor>(actors.values());    
     }
 
     /**
      * Adds a queue to that should be offered each new actor added to this ActorSet
      * Basically this is subscribing to a copy of each new actor.
      * Used for networking
      * @param queue
      */
     public void addNewActorConsumer(Queue<Actor> queue) {
         addNotifyees.add(queue);
     }
 }
