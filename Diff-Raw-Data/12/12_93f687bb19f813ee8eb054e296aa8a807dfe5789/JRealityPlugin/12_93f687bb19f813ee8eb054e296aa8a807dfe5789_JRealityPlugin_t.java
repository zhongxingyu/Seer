 package de.tum.in.jrealityplugin;
 
import de.cinderella.api.cs.CindyScript;
 import de.cinderella.api.cs.CindyScriptPlugin;
 
 /**
  * Implementation of the plugin interface
  */
 public class JRealityPlugin extends CindyScriptPlugin {
 
 	/* (non-Javadoc)
 	 * @see de.cinderella.api.cs.CindyScriptPlugin#getAuthor()
 	 */
 	@Override
 	public String getAuthor() {		
 		return "Jan Sommer und Matthias Reitinger";
 	}
 
 	/* (non-Javadoc)
 	 * @see de.cinderella.api.cs.CindyScriptPlugin#getName()
 	 */
 	@Override
 	public String getName() {
 		return "jReality for Cinderella";
 	}
 
	/**
	 * Squares the given number
	 * @param x
	 * @return The square of x
	 */
	@CindyScript("square")
	public double square(double x) {
		return x*x;
	}

 }
