 package ray.material;
 
 import ray.IntersectionRecord;
 import ray.RayRecord;
 import ray.math.Color;
 import ray.math.Vector3;
 
 /**
  * A Glass material. 
  *
  */
 public class Glass extends Material {
 
 	protected double refractiveIndex;
 	public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }
 
 
 	public Glass() { }
 
 	/**
 	 * The glass material cannot directly interact with light, so
 	 * return false.
 	 */
 	@Override
 	public boolean canInteractWithLight() {
 		return false;
 	}
 
 	/**
 	 * As the glass material cannot interact directly with light,
 	 * we set the value to zero.
 	 */
 	@Override
 	public void evaluate(Color value, IntersectionRecord record,
 			Vector3 incoming, Vector3 outgoing) {
 		value.set(0.0);
 	}
 
 
 	/**
 	 * The glass material has at least one perfectly specular component,
 	 * so return true.
 	 */
 	@Override
 	public boolean hasPerfectlySpecularComponent() {
 		return true;
 	}
 
 
 	@Override
 	public RayRecord[] getIncomingSpecularRays(IntersectionRecord record, Vector3 outgoing) {
 		// TODO(B): Fill in this method.
 
 		// --- copied from Glazed -- need all the variables!
 		Vector3 outgoingNorm = new Vector3(outgoing);
 		outgoingNorm.normalize();
 		
 		Vector3 normalNorm = record.normal;
 		normalNorm.normalize();
 		
 		//Project outgoing ray to normal
 		double projection = outgoingNorm.dot(normalNorm);
 		
 		//Fresnel Coefficient using Schlick's Approx
 		double n_1, n_2;
 		if (projection > 0) { //outgoing is outside the material
 			n_1 = 1.0;
 			n_2 = refractiveIndex;
 		}
 		else { //outgoing is inside the material
 			n_1 = refractiveIndex;
 			n_2 = 1.0;
 		}
 		/*
 		else { //this should never happen
 			//if the outgoing and the normal are perp., then the
 			//outgoing is implied not to intersect the surface, contradiction!
 			System.out.println("This happened!");
 			return null;
 		}*/
 		
 		double r_0 = Math.pow(((n_2 - n_1)/(n_2 + n_1)), 2.0);
 		//Fresnel Coefficient
 		//r_0 + (1 - r_0)(1 - cos(theta))^5
 		//cos(theta) = (normalNorm dot outgoingNorm) / (||normalNorm|| ||outgoingNorm||)
 		
 		double fresnel = r_0 + (1.0 - r_0) * Math.pow(1.0 - projection, 5.0);
 		
 		//Reflected Ray = 2 * normalNorm * (normalNorm dot outgoingNorm) - outgoingNorm
 		Vector3 reflectedRay = new Vector3(normalNorm);
 		reflectedRay.scale(2.0);
 		reflectedRay.scale(normalNorm.dot(outgoingNorm));
 		reflectedRay.sub(outgoingNorm);
 		
 		// --- end copy
 		
 		//Check for total internal reflection
 		//Taken from Lecture 35 Slide 27
 		//TODO outgoing may have to be reversed... this will be a bug if it arises.
 		Vector3 outgoingNormReversed = new Vector3(outgoing);
 		outgoingNormReversed.scale(-1.0);
 		outgoingNormReversed.normalize();
 		projection = outgoingNormReversed.dot(normalNorm);
 		
		double determinant = 1 - (Math.pow(n_1, 2.0) * (1 -Math.pow(projection, 2.0)))/(Math.pow(n_2, 2.0));
 		
 		RayRecord[] toReturn = null;
		if (determinant < 0) {
 			toReturn = new RayRecord[1];
 			
 		}
 		else {
 			toReturn = new RayRecord[2];
 		}
 		
 		//avoid c/ping this part of the code multiple times
 		toReturn[0] = new RayRecord();
 		toReturn[0].ray.start = 0;
 		toReturn[0].ray.end = Double.POSITIVE_INFINITY;
 		//starts at the intersection point
 		toReturn[0].ray.set(record.location, reflectedRay);
 		
		if (determinant < 0) {
 			//For total internal reflection, its factor is 1.0
 			toReturn[0].factor.set(1.0);
 		}
 		else {
 			toReturn[0].factor.set(fresnel);
 			
 			//Calculate transmitted vector
 			//Taken from Lecture 35 Slide 27
 			//TODO may need to reverse outgoingNorm!
 			Vector3 transmittedVector = new Vector3(outgoingNormReversed);
 			Vector3 subtractArgOne = new Vector3(normalNorm);
 			Vector3 subtractArgTwo = new Vector3(normalNorm);
 			
 			//TODO may need to reverse outgoingNorm!
 			subtractArgOne.scale(projection);
 			transmittedVector.sub(subtractArgOne);
 			transmittedVector.scale(n_1/n_2);
 			
 			subtractArgTwo.scale(Math.sqrt(determinant));
 			transmittedVector.sub(subtractArgTwo);
 			
 			toReturn[1] = new RayRecord();
 			toReturn[1].ray.start = 0;
 			toReturn[1].ray.end = Double.POSITIVE_INFINITY;
 			//starts at the intersection point as well
 			toReturn[1].ray.set(record.location, transmittedVector);
 			
 			toReturn[1].factor.set(1.0 - fresnel);
 		}
 		
 		return toReturn;
 	}
 }
