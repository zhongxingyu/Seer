 import java.awt.Color;
 import java.util.ArrayList;
 
 import processing.core.PApplet;
 import processing.core.PVector;
 
 public class Sphere extends Primitive {
 	
 	public float radius;
 	public Sphere( Vertex vertex_, float radius_, PApplet parent_ ) {
 		super( vertex_, parent_ );
 		// If the radius is less than zero it was not set
 		if ( radius_ < 0 ) 
 			this.radius = PApplet.mag( vertex.dx, vertex.dy, vertex.dz );
 		else
 			this.radius = radius_;
 	}
 
 	@Override
 	public Color getColor(PVector intersectionPoint, ArrayList<Light> lights) {
		Color ambientComponent = new Color(this.ambientLight.getRed()
				* this.ambientMaterial.getRed() / (255.0f * 255.0f),
				this.ambientLight.getGreen() * this.ambientMaterial.getGreen()
						/ (255.0f * 255.0f), this.ambientLight.getBlue()
						* this.ambientMaterial.getBlue() / (255.0f * 255.0f));
 
 		PVector normal = PVector.sub(intersectionPoint, this.pos);
 		normal.normalize();
 
 		// Calculate the diffuse component
 		float diffuseR = 0f;
 		float diffuseG = 0f;
 		float diffuseB = 0f;
 		for (Light light : lights) {
 			// TODO: only valid for directional lights.
 			PVector lightDir = new PVector(light.vertex.dx, light.vertex.dy,
 					light.vertex.dz);
 			lightDir.normalize();
 		
 			float dot = normal.dot(lightDir);
 		
 			diffuseR += light.getIntensity(this.pos).getRed() * this.diffuseMaterial.getRed() * dot / (255.0f * 255.0f);
 			diffuseG += light.getIntensity(this.pos).getGreen() * this.diffuseMaterial.getGreen() * dot / (255.0f * 255.0f);
 			diffuseB += light.getIntensity(this.pos).getBlue() * this.diffuseMaterial.getBlue() * dot / (255.0f * 255.0f);
 		}
 		Color diffuseComponent = new Color(diffuseR, diffuseG, diffuseB);
 		
		return new Color(ambientComponent.getRed() + diffuseComponent.getRed(),
 				ambientComponent.getGreen() + diffuseComponent.getGreen(),
 				ambientComponent.getBlue() + diffuseComponent.getBlue());
 	}
 		
 	@Override
 	public void debug() {
 		parent.noStroke();
 		parent.translate( this.pos.x, this.pos.y, this.pos.z );
 		parent.sphere( this.radius );
 	}
 		
 	@Override
 	public PVector intersectionPoint(Ray r) throws IllegalArgumentException {
 		// Transform the ray into object space
 		Ray transformedRay = new Ray(PVector.sub(r.o, this.pos), r.d);
 		
 		float a = PVector.dot(transformedRay.d, transformedRay.d);
 		float b = 2 * PVector.dot(transformedRay.d, transformedRay.o);
 		float c = PVector.dot(transformedRay.o, transformedRay.o) - this.radius*this.radius;
 		
 		float d = b * b - 4 * a * c;
 		// d is negative so there is no root
 		if (d < 0) {
 			throw new IllegalArgumentException("No intersection point.");
 		}
 		
 		float dSqrt = PApplet.sqrt(d);
 		float s;
 		if ( b < 0 )
 			s = ( -b - dSqrt ) / 2;
 		else
 			s = ( -b + dSqrt ) / 2;
 		
 		float t0 = Math.min(s / a, c / s);
 		float t1 = Math.max(s / a, c / s);
 
 		// If t1 is less than zero the ray misses the sphere
 		if (t1 < 0) {
 			throw new IllegalArgumentException("No intersection point.");
 		}
 
 		@SuppressWarnings("unused")
 		float dist;
 		if ( t0 < 0 )
 			dist = t1;
 		else
 			dist = t0;
 
 		float xPoint = r.o.x + dist * (r.d.x);
 		float yPoint = r.o.y + dist * (r.d.y);
 		float zPoint = r.o.z + dist * (r.d.z);
 
 		return new PVector(xPoint, yPoint, zPoint);
 	}
 }
