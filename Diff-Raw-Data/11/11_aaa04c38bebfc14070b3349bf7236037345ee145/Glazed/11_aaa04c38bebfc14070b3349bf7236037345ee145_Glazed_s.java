 
 package ray.material;
 
 import ray.IntersectionRecord;
 import ray.RayRecord;
 import ray.math.Color;
 import ray.math.Vector3;
 
 public class Glazed extends Material {
 
 	protected double refractiveIndex;
 	public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }
 
 	protected Material substrate;
 	public void setSubstrate(Material substrate) {
 		if (substrate.hasPerfectlySpecularComponent())
 			throw new RuntimeException("Substrate to a glazed material cannot have perfectly specular component!");
 		this.substrate = substrate; 
 	}
 
 	/** The exponent controlling the sharpness of the specular reflection. */
 	protected double exponent = 1.0;
 	public void setExponent(double exponent) { this.exponent = exponent; }
 
 	public Glazed() { }
 
 	/**
 	 * Whether the glazed material can interact with light or not depends
 	 * on whether the substrate can do so or not, so just return the substrate's
 	 * result of the same method.
 	 */
 	@Override
 	public boolean canInteractWithLight() {
 		return substrate.canInteractWithLight();
 	}
 	
 	@Override
 	public void evaluate(Color value, IntersectionRecord record, Vector3 incoming,
 			Vector3 outgoing) {
 		// TODO(B): fill in this function.
 		substrate.evaluate(value, record, incoming, outgoing);
 		
 		//Calculate Fresnel Coeff using Schlick's Approx
 		Vector3 outgoingNorm = new Vector3(outgoing);
 		outgoingNorm.normalize();
 		
 		Vector3 normalNorm = record.normal;
 		normalNorm.normalize();
 		
 		//Project outgoing ray to normal
 		double projection = outgoingNorm.dot(normalNorm);
 
 		double n_1, n_2;
 		if (projection > 0) { //outgoing is outside the material
 			n_1 = 1.0;
 			n_2 = refractiveIndex;
 		}
 		else if (projection < 0) { //outgoing is inside the material
			n_1 = refractiveIndex;
			n_2 = 1.0;
 		}
 		else { //if this happens, there's no scaling component?
 			return;
 		}
 		
 		double r_0 = Math.pow(((n_2 - n_1)/(n_2 + n_1)), 2.0);
 		//Fresnel Coefficient
 		//r_0 + (1 - r_0)(1 - cos(theta))^5
 		//cos(theta) = (normalNorm dot outgoingNorm) / (||normalNorm|| ||outgoingNorm||)
 		double fresnel = r_0 + (1.0 - r_0) * Math.pow(1.0 - projection, 5.0);
 		
 		
 		//SCALE COLOR BY 1.0 - FRESNEL
 		value.scale(1.0 - fresnel);
 	}
 
 	/**
 	 * The glazed material has a perfectly specular component, so return true.
 	 */
 	@Override
 	public boolean hasPerfectlySpecularComponent() {
 		return true;
 	}
 
 	/**
 	 * Generate one ray record, which should contains the ray pointing
 	 * in the perfect-mirror reflection direction, and the factor should
 	 * be the Fresnel coefficient value.
 	 */
 	@Override
 	public RayRecord[] getIncomingSpecularRays(IntersectionRecord record, Vector3 outgoing) {
 		// TODO(B): Fill in this method.		
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
 		else if (projection < 0) { //outgoing is inside the material
			n_1 = refractiveIndex;
			n_2 = 1.0;
 		}
 		else { //this should never happen
 			//if the outgoing and the normal are perp., then the
 			//outgoing is implied not to intersect the surface, contradiction!
 			System.out.println("This happened!");
 			return null;
 		}
 		
 		double r_0 = Math.pow(((n_2 - n_1)/(n_2 + n_1)), 2.0);
 		//Fresnel Coefficient
 		//r_0 + (1 - r_0)(1 - cos(theta))^5
 		//cos(theta) = (normalNorm dot outgoingNorm) / (||normalNorm|| ||outgoingNorm||)
 		double fresnel = r_0 + (1.0 - r_0) * Math.pow(1.0 - projection, 5.0);
 		
 		//Reflected Ray = 2 * normalNorm * (normalNorm dot outgoingNorm) - outgoingNorm
 		Vector3 reflectedRay = new Vector3(normalNorm);
 		reflectedRay.scale(2.0);
 		reflectedRay.scale(projection);
 		reflectedRay.sub(outgoingNorm);
 		
 		RayRecord[] toReturn = new RayRecord[1];
 		toReturn[0] = new RayRecord();
 		toReturn[0].ray.start = 0;
 		toReturn[0].ray.end = Double.POSITIVE_INFINITY;
 		//starts at the intersection point
 		toReturn[0].ray.set(record.location, reflectedRay);
 		
 		toReturn[0].factor.set(fresnel);
 		
 		return toReturn;
 	}
 }
