 package simulation;
 
 import java.util.List;
 import util.Location;
 import util.Vector;
 
 /**
  * XXX.
  * 
  * @author Jack Matteucci
  */
 public class CenterOfMass extends EnvironmentalForce {
 
 	private double myMagnitude;
 	private double myExponent;
 	private Location myCenterOfMass;
	
	
     /**
      * Create a CenterOfMass object with a magnitude and an exponent, both of which
      * will ultimately determine the strength of this force.  Negative magnitudes give
      * a repelling force.
      */	
 	public CenterOfMass(double magnitude, double exponent) {
 		myMagnitude = magnitude;
 		myExponent = exponent;
 	}
 
 	/**
 	 * Cycles through a list of Masses, updating where the Center of Mass is,
 	 * and applying the specified force to each mass.
 	 */
 	@Override
 	public void Apply(List<Mass> Masses) {
 		updateCenterOfMass(Masses);
 		for(Mass m : Masses){
 			m.applyForce(new Vector(getForceAngle(m),getForceMagnitude(m)));
 		}
 	}
 	//returns the magnitude of the force that should be applied to a 
 	//given mass, given the inputs to the CenterOfMass class
 	private double getForceMagnitude(Mass mass){
 		return Math.pow(distance(mass), -myExponent)*Math.abs(myMagnitude);
 	}
	
 	//returns the direction of the force that should be applied to a 
 	//given mass, given the inputs to the CenterOfMass class
 	private double getForceAngle(Mass mass){
 		Double angle = Vector.angleBetween(myCenterOfMass.getX() - mass.getX(), myCenterOfMass.getY() - mass.getY());
 		if(myMagnitude<0){
 			return angle + 180;
 		}
 		return angle;
 	}
	
 	//returns the distance from a given mass to the center of mass.  This 
 	//helps the getForceMagnitude function
 	private double distance(Mass mass){
 		return new Location(mass.getX(), mass.getY()).distance(myCenterOfMass);
 	}
	
 	//Updates the location of the center of mass
 	private void updateCenterOfMass(List<Mass> Masses) {
 		double XCenterOfMass = 0;
 		double YCenterOfMass = 0;
 		double TotalMass = 0;
 		for(Mass m : Masses){
 			TotalMass += m.getMass();
 			XCenterOfMass += m.getMass()*m.getX();
 			YCenterOfMass += m.getMass()*m.getY();
 		}
 		XCenterOfMass /= TotalMass;
 		YCenterOfMass /= TotalMass;
 		myCenterOfMass = new Location(XCenterOfMass,YCenterOfMass);
 	}
 
}
