 package feynstein.properties.integrators;
 
 import feynstein.*;
 import feynstein.properties.*;
 
public abstract class Integrator extends Property {
 	final double h;
 	
 	public Integrator(double stepSize, Scene scene) {
 		super(scene);
 		h = stepSize;
 	}
 }
