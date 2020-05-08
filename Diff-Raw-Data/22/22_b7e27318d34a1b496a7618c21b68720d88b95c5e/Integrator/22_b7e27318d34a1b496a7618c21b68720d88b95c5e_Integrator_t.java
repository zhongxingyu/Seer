 package feynstein.properties.integrators;
 
 import feynstein.*;
 import feynstein.properties.*;
 
public abstract class Integrator extends Property<Integrator> {
 	final double h;
 	
 	public Integrator(double stepSize, Scene scene) {
 		super(scene);
		objectType = "Integrator";
 		h = stepSize;
 	}
 }
