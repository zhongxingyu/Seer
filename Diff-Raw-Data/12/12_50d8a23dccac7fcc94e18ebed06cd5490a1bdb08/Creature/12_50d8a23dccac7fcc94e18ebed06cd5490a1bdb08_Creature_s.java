 package hu.miracle.workers;
 
 public abstract class Creature extends BaseObject {
 
 	// Members
 	protected Scene scene;
 
 	// Public interface
 	public void setScene(Scene scene) {
 		System.out.println(getClass().getCanonicalName() + ".setScene()");
 	}
 
 	public void terminate() {
 		System.out.println(getClass().getCanonicalName() + ".terminate()");
 	}
 
 }
