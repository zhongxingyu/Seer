 package dk.itu.ecdar.text.generator.framework;
 
 /**
  * Base class for uncontrollable edge implementations.
  * 
  * Uncontrollable edges are defined as producing input
  * for the environment.
  */
 public abstract class IEdgeUncontrollable extends IEdge {
 
 	public IEdgeUncontrollable(ILocation from, ILocation to, String signal) {
 		super(from, to, signal);
 	}
 
 	@Override
 	public boolean acceptInput(String input) {
 		return false;
 	}
 
 }
