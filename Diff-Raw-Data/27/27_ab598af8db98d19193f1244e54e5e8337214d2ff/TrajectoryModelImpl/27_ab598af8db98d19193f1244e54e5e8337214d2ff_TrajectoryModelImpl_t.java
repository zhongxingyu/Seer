 package arida.ufc.br.moap.datamodelapi.imp;
 
 import arida.ufc.br.moap.core.algorithm.spi.ITrajectoryFactory;
 import arida.ufc.br.moap.core.beans.MovingObject;
 import arida.ufc.br.moap.core.beans.Trajectory;
 import arida.ufc.br.moap.core.beans.TrajectoryFactoryImp;
 import arida.ufc.br.moap.core.beans.iterators.api.IMovingObjectIterable;
 import arida.ufc.br.moap.core.beans.iterators.api.ITrajectoryIterable;
 import arida.ufc.br.moap.core.beans.iterators.imp.MovingObjectIterableImp;
 import arida.ufc.br.moap.core.beans.iterators.imp.TrajectoryIterableImp;
 import arida.ufc.br.moap.datamodelapi.spi.AbstractTrajectoryModel;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * ITrajectoryModel implementation.
  *
  * @author franzejr
  * @author rafaelelias
  */
 public class TrajectoryModelImpl<S, T> extends AbstractTrajectoryModel<S, T> {
 
     public TrajectoryModelImpl() {
         super();
     }
     /*
      * List of all trajectories. Remembering that a trajectory
      * is a set of points which each point has a Latitude,Longitude
      * and one Timestamp.
      */
     /*
      * Trajectory List
      */
     private List<Trajectory<S, T>> trajectories = new ArrayList<Trajectory<S, T>>();
     /*
      *Identify a trajectory 
      */
     private HashMap<String, Trajectory<S, T>> trajectoryIndices = new HashMap<String, Trajectory<S, T>>();
     /*
      * Identify Moving Objects
      */
     private HashMap<String, MovingObject> movingObjectIndices = new HashMap<String, MovingObject>();
     /*
      * Moving Object List
      */
     private List<MovingObject> movingObjects = new ArrayList<MovingObject>();
     /*
      * Build a trajectory for a model
      */
     private ITrajectoryFactory<S, T> factory = new TrajectoryFactoryImp<S, T>();
 
     @Override
     public Trajectory<S, T> getTrajectory(int id) {
 
         if (Integer.valueOf(id) == null) {
             throw new NullPointerException("Trajectory idx cannot be NULL");
         }
 
         return trajectories.get(id);
     }
 
     @Override
     public ITrajectoryIterable getTrajectories() {
 
         ITrajectoryIterable iterable = new TrajectoryIterableImp(trajectories, readWriteLock.readLock());
         return iterable;
     }
 
     private void addMovingObject(MovingObject mo) {
 
         if (mo == null) {
             throw new NullPointerException("Moving object cannot be NULL");
         }
 
         if (!this.movingObjectIndices.containsKey(mo.getId())) {
             this.movingObjects.add(mo);
             this.movingObjectIndices.put(mo.getId(), mo);
         }
     }
 
     @Override
     public MovingObject getMovingObject(String id) {
         return this.movingObjectIndices.get(id);
     }
 
     @Override
     public int getTrajectoryCount() {
         return this.trajectories.size();
     }
 
     @Override
     public int getMovingObjectCount() {
         return this.movingObjects.size();
     }
 
     @Override
     public void addTrajectory(Trajectory trajectory) {
         if (trajectory == null) {
             throw new IllegalArgumentException("trajectory cannot be NULL");
         }
         if (trajectory.getMovingObject() == null) {
             throw new NullPointerException("Moving object for the trajectory cannot be NULL");
         }
 
         if (!this.trajectoryIndices.containsKey(trajectory.getId())) {
             this.trajectories.add(trajectory);
             this.trajectoryIndices.put(trajectory.getId(), trajectory);
             addMovingObject(trajectory.getMovingObject());
         }
     }
 
     @Override
     public String toString() {
         return "";
     }
 
     @Override
     public ITrajectoryFactory factory() {
         return this.factory;
     }
 
     @Override
     public MovingObject getMovingObject(int idx) {
         if (Integer.valueOf(idx) == null) {
             throw new IllegalArgumentException("Moving object idx cannot be NULL");
         }
         return this.movingObjects.get(idx);
     }
 
     @Override
     public IMovingObjectIterable getMovingObjects() {
         readLock();
         return new MovingObjectIterableImp(movingObjects, readWriteLock.readLock());
     }
 
     @Override
     public Collection<Trajectory<S, T>> getTrajectories(MovingObject mo) {
         if (mo == null) {
             throw new IllegalArgumentException("mo id cannot be NULL");
         }
         List<Trajectory<S, T>> list = new ArrayList<Trajectory<S, T>>();
         for (Trajectory traj : this.trajectoryIndices.values()) {
             if (traj.getMovingObject().equals(mo)) {
                 list.add(traj);
             }
         }
         return list;
     }
 
     @Override
     public Collection<Trajectory<S, T>> getTrajectories(String moId) {
         if (moId == null) {
             throw new IllegalArgumentException("mo id cannot be NULL");
         }
         MovingObject mo = this.movingObjectIndices.get(moId);
         return getTrajectories(mo);
     }
 
     @Override
     public Trajectory getTrajectory(String id) {
         if (id == null) {
             throw new IllegalArgumentException("trajectory id cannot be NULL");
         }
         return this.trajectoryIndices.get(id);
     }
 
     @Override
     public Trajectory removeTrajectory(int id) {
         if (Integer.valueOf(id) == null) {
             throw new IllegalArgumentException("trajectory id cannot be NULL");
         }
         Trajectory t = this.trajectories.remove(id);
         if (t != null) {
             this.trajectoryIndices.remove(t.getId());
         }
         return t;
     }
 
     @Override
     public Trajectory removeTrajectory(String id) {
         if (id == null) {
             throw new IllegalArgumentException("trajectory id cannot be NULL");
         }
         Trajectory t = this.trajectoryIndices.remove(id);
         if (t != null) {
             this.trajectories.remove(t);
         }
 
         return t;
     }
 
     @Override
     public MovingObject removeMovingObject(int idx) {
         if (Integer.valueOf(idx) == null) {
             throw new IllegalArgumentException("trajectory id cannot be NULL");
         }
         MovingObject mo = this.movingObjects.remove(idx);
         if (mo != null) {
             this.movingObjectIndices.remove(mo.getId());
 
             /*
              * Remove all the Moving Object's trajectories
              */
             removeTrajectories(mo);
 
         }
 
         return mo;
     }
 
     @Override
     public MovingObject removeMovingObject(String id) {
         if (id == null) {
             throw new IllegalArgumentException("trajectory id cannot be NULL");
         }
         MovingObject mo = this.movingObjectIndices.remove(id);
         if (mo != null) {
             this.movingObjects.remove(mo);
             /*
              * Remove all the Moving Object's trajectories
              */
             removeTrajectories(mo);
         }
 
         return mo;
     }
 
    /* 
     * @return Remove all trajectories of a given {@link Moving Object}
     */
     private void removeTrajectories(MovingObject mo) {
        Collection<Trajectory<S,T>> c = getTrajectories(mo);
        Iterator iter = c.iterator();
        while(iter.hasNext()){
            this.trajectories.remove((Trajectory<S,T>)iter.next());
         }
     }
 
     public ReentrantReadWriteLock getReadWriteLock() {
         return readWriteLock;
     }
    
    @Override
    protected void finalize() throws Throwable{
        super.finalize();
        
        this.factory = null;
        this.movingObjectIndices = null;
        this.movingObjects = null;
        this.trajectories = null;
        this.trajectoryIndices = null;
        
    }
 }
