 package feynstein;
 
 import feynstein.forces.*;
 import feynstein.geometry.*;
 import feynstein.properties.*;
 import feynstein.properties.integrators.*;
 import feynstein.shapes.*;
 import feynstein.utilities.*;
 
 import java.awt.Frame;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.media.opengl.awt.*;
 import com.jogamp.opengl.util.*;
 
 public abstract class Scene {
     public static GLCanvas canvas = new GLCanvas();
     public static Frame frame = new Frame("Feynstein");
     public static Animator animator = new Animator(canvas);
 
     protected Map<String, Shape> shapes;
     protected Map<String, Force> forces;
     protected ArrayList<Property> properties;
     protected Mesh mesh;
 	
 	double[] globalForces;
 	double[] globalPositions;
 	double[] globalVelocities;
 	double[] globalMasses;
 	
 
     public Scene() {
 	mesh = new Mesh();
 
 	shapes = new HashMap<String, Shape>();
 	forces = new HashMap<String, Force>();
 	properties = new ArrayList<Property>();
 
 	setProperties();
 	createShapes();
 	createForces();
 		
 	globalForces = new double[3*mesh.size()];
 	globalPositions = new double[3*mesh.size()];
 	globalVelocities = new double[3*mesh.size()];
 	globalMasses = new double[3*mesh.size()];
 		
     }
 
     protected static void print(String str) {
 	System.out.println(str);
     }
 
     public void addShape(Shape s) {
 		print("Adding a " + s.toString());
 		shapes.put(s.getName(), s);
 		mesh.append(s.getLocalMesh());
 	}
 
     public Shape getShape(String name) {
 	return shapes.get(name);
     }
 
     public void addForce(Force f) {
 	print("Adding a " + f.toString());
 		forces.put(f.toString(), f);
     }
 	
 	public void addProperty(Property p) {
 		print("Adding a " + p.toString());
 		properties.add(p);
 	}
 
     /**
      * This method steps through the list of local force 
      * potentials, evaluates each force potential, and then 
      * update a global force magnitude list 
      */	
     public double[] globalForceMagnitude() {
 		
 		// build global force, position, velocity, and mass vectors
 		// this will be convient when we get to implicit time stepping
 		for(int i = 0; i < mesh.size(); i++) {
 			// clear global fores
 			globalForces[3*i] = 0;
 			globalForces[3*i+1] = 0;
 			globalForces[3*i+2] = 0;
 			// get global positions
 			globalPositions[3*i] = mesh.getParticles().get(i).getPos().x();
 			globalPositions[3*i+1] = mesh.getParticles().get(i).getPos().y();
 			globalPositions[3*i+2] = mesh.getParticles().get(i).getPos().z();
 			// get global velocitiyes
 			globalVelocities[3*i] = mesh.getParticles().get(i).getVel().x();
 			globalVelocities[3*i+1] = mesh.getParticles().get(i).getVel().y();
 			globalVelocities[3*i+2] = mesh.getParticles().get(i).getVel().z();
 			// get global masses
 			globalMasses[3*i] = mesh.getParticles().get(i).getMass();
 			globalMasses[3*i+1] = mesh.getParticles().get(i).getMass();
 			globalMasses[3*i+2] = mesh.getParticles().get(i).getMass();
 		}
 		
 		//for each force potential
 		for(Force force : forces.values()){
 			//get the local force vector
 			double [] localForce = force.getLocalForce(globalPositions,
 								     globalVelocities, globalMasses);
 			//add to the global force vector at cooresponding particle
 			//indicies
 			if(force.isGlobal()) {
 				for(int i = 0; i < localForce.length; i++){
					globalForces[3*i] += localForce[3*i];
					globalForces[3*i+1] += localForce[3*i+1];
					globalForces[3*i+2] += localForce[3*i+2];
 				}
 			} else {
				for(int i = 0; i < localForce.length; i++){
 					globalForces[3*force.getStencilIdx(i)] += localForce[3*i];
 					globalForces[3*force.getStencilIdx(i)+1] += localForce[3*i+1];
 					globalForces[3*force.getStencilIdx(i)+2] += localForce[3*i+2];
 				}
 			}
 		}
 		
 		return globalForces;
     }
 	
     public Mesh getMesh() {
 	return mesh;
     }
 
     public void update() {
 		for (Property property : properties) 
 			property.update();
 		onFrame();
     }
 
     public abstract void setProperties();
     public abstract void createShapes();
     public abstract void createForces();
     public abstract void onFrame();
 }
