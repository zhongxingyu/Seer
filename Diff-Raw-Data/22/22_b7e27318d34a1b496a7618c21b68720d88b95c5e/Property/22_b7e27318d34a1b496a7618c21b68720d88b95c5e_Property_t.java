 package feynstein.properties;
 
 import feynstein.*;
 
public abstract class Property<E extends Property> extends Built<E> {
 	final Scene scene;
 	
 	public Property(Scene scene) {
 		this.scene = scene;
		objectType = "Property";
 	}
 	
 	public Scene getScene() {
 		return scene;
 	}
 	
 	public abstract void update();
 }
