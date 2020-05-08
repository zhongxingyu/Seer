 package feynstein.shapes;
 
 import feynstein.geometry.*;
 import feynstein.utilities.Vector3d;
 
 import java.util.ArrayList;
 
 public class SpringChain extends ParticleSet<SpringChain> {
 	/* the number of particles to interpolate with spring
 	 a value of one indicates to connect springs to the
 	 next particle only, a value of n indicates connect n
 	 springs between each particle and the next n particles
 	 */
 	int connectivity;
 	
 	public SpringChain() {
 		objectType = "SpringChain";
 		particleRadius = 0.2f;
 		connectivity = 1;
 	}
 
 	public SpringChain set_connectivity(int connectivity) {
 		this.connectivity = connectivity;
 		return this;
 	}
 	
     public SpringChain compileShape() {
 		//super.compileShape();
 		for (int i = 0; i < localMesh.size(); i++) {
			for(int j = 1; j < connectivity+1; j++) {
 				if(i < localMesh.size() - j)
 					localMesh.getEdges().add(new Edge(i, i+j));
 			}
 		}
 		return super.compileShape();
     }
 }
