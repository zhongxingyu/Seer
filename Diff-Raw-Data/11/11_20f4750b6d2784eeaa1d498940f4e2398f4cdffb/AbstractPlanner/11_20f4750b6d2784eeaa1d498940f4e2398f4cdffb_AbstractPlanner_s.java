 package balle.strategy.planner;
 
 import java.util.ArrayList;
 
 import balle.controller.Controller;
 import balle.main.drawable.Drawable;
 import balle.strategy.Strategy;
 import balle.world.Snapshot;
 
 public abstract class AbstractPlanner implements Strategy {
     private final ArrayList<Drawable> drawables = new ArrayList<Drawable>();
 
     /**
      * Notify the executor of a change in the current state
      * 
      * @Override
      * @param snapshot
      */
     @Override
 	public final void step(Controller controller, Snapshot snapshot) {
         clearDrawables();
         onStep(controller, snapshot);
     }
 
     @Override
     public void stop(Controller controller) {
         controller.stop();
     }
 
     @Override
    public ArrayList<Drawable> getDrawables() {
         return drawables;
     }
 
    protected void clearDrawables() {
         drawables.clear();
     }
 
     /**
      * Add a new drawable to the list
      * 
      * @param drawable
      */
    protected void addDrawable(Drawable drawable) {
         drawables.add(drawable);
     }
 
     /**
      * Adds a list of drawables to the current set
      * 
      * @param drawables
      */
    protected void addDrawables(ArrayList<Drawable> drawables) {
         this.drawables.addAll(drawables);
     }
 
 	public boolean shouldStealStep(Snapshot snapshot) {
 		return false;
 	}
 
 	protected abstract void onStep(Controller controller, Snapshot snapshot);
 
 }
