 package feynstein.properties;
 
 import feynstein.*;
 
public abstract class Property {
 	final Scene scene;
 	
 	public Property(Scene scene) {
 		this.scene = scene;
 	}
 	
 	public Scene getScene() {
 		return scene;
 	}
 	
 	public abstract void update();
 }
