 package dungeon.game;
 
 import dungeon.models.World;
 import dungeon.models.messages.Transform;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Allows you to commit a bunch of transforms to a world.
  *
  * When any one of those transforms fails, the transaction will be rolled back to the last time you committed.
  *
  * Example
  *
  * push(x)
  * push(y)
  * commit() # <-- Committed State 1
  * push(a)
  * push(b)
  * commit()
  *
  * When applying the b transform in the second commit call, an exception is thrown and the transaction will be rolled
  * back to state 1. E.g. after commit #2 {@link #getWorld()} will return the same object it did after commit #1.
  */
 public class Transaction {
   private World world;
 
   /**
    * The committed transforms.
    */
   private final List<Transform> transforms = new ArrayList<>();
 
   /**
    * The transforms currently in transaction.
    */
   private final List<Transform> pendingTransforms = new ArrayList<>();
 
   public Transaction (World world) {
     this.world = world;
   }
 
   /**
    * Pushes a transform into the current transaction.
    */
   public void push (Transform transform) {
     this.pendingTransforms.add(transform);
   }
 
   /**
    * Commits the currently pending transforms.
    *
    * The world object is only updated, when all pending transforms succeed.
    */
   public void commit () {
     World world = this.world;
 
     try {
       for (Transform transform : this.pendingTransforms) {
        world = world.apply(transform);
       }
 
       this.transforms.addAll(this.pendingTransforms);
 
       this.world = world;
     } finally {
       this.pendingTransforms.clear();
     }
   }
 
   /**
    * A shorthand for committing a single transform.
    */
   public void pushAndCommit (Transform transform) {
     this.push(transform);
 
     this.commit();
   }
 
   /**
    * Rollback to the last commit, e.g. dismiss all pending transforms.
    */
   public void rollback () {
     this.pendingTransforms.clear();
   }
 
   /**
    * Returns all committed transforms.
    */
   public List<Transform> getTransforms () {
     return Collections.unmodifiableList(this.transforms);
   }
 
   /**
    * Returns the state of the world after applying all committed transforms.
    */
   public World getWorld () {
     return this.world;
   }
 }
