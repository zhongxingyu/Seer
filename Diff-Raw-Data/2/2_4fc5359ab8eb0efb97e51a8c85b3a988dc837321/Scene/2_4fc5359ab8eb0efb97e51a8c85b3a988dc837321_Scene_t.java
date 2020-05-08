 package feynstein;
 
 
 import feynstein.forces.*;
 import feynstein.geometry.*;
 import feynstein.properties.*;
 import feynstein.properties.collision.*;
 import feynstein.properties.integrators.*;
 import feynstein.shapes.*;
 import feynstein.utilities.*;
 
 import java.awt.Frame;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.media.opengl.awt.*;
 import com.jogamp.opengl.util.*;
 
 public abstract class Scene {
     public static GLCanvas canvas = new GLCanvas();
     public static Frame frame = new Frame("Feynstein");
     public static Animator animator = new Animator(canvas);
 
     protected Map<String, Shape> shapes;
     protected List<Force> forces;
     protected Map<Class, Property> propertyMap;
     protected ArrayList<NarrowPhaseDetector> detectorList;
     protected List<Property> properties; //without collision responders, integrators
     protected List<Property> responders;
     protected Mesh mesh;
     protected Integrator integrator;
     private boolean hasInteg; //whether user's defined default already
     private boolean steppedInResponse;
 	
     double[] globalForces;
     double[] globalPositions;
     double[] globalVelocities;
     double[] globalMasses;
 
     public Scene() {
 	mesh = new Mesh();
 
 	shapes = new HashMap<String, Shape>();
 	forces = new ArrayList<Force>();
 	properties = new ArrayList<Property>();
 	responders = new ArrayList<Property>();
 	propertyMap = new HashMap<Class, Property>();
 	detectorList = new ArrayList<NarrowPhaseDetector>();
 
 	steppedInResponse = false;
 
 	createShapes();
 	setProperties();
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
 	print("Adding " + s.toString());
 	shapes.put(s.getName(), s);
 	mesh.append(s.getLocalMesh());
     }
 
     public Shape getShape(String name) {
 	return shapes.get(name);
     }
 
     public void addForce(Force f) {
 	print("Adding " + f.toString());
 	forces.add(f);
     }
 	
     public void addProperty(Property p) {
 	print("Adding " + p.toString());
 
 	if (p instanceof CollisionResponder) {
 	    responders.add(p);
 	} else if (p instanceof Integrator) {
 	    // if (hasInteg)
 	    integrator = (Integrator) p;
 	} else {
 	    properties.add(p);
 	}
 
 	propertyMap.put(p.getClass(), p);
 
 	if (p instanceof NarrowPhaseDetector)
 	    detectorList.add((NarrowPhaseDetector) p);
     }
 
     @SuppressWarnings("unchecked")
     public <E extends Property> E getProperty(Class c) {
 	return (E) propertyMap.get(c);
     }
 
     public NarrowPhaseDetector getDetectorByIndex(int index) {
 	return detectorList.get(index);
     }
 
     public Integrator getIntegrator() {
 	return integrator;
     }
 	
     public Mesh getMesh() {
 	return mesh;
     }
 
 	/**
      * This method steps through the list of local force 
      * potentials, evaluates each force potential, and then 
      * update a global force magnitude list 
      */	
 	public void updateGlobalForce() {
 		for(int i = 0; i < mesh.size(); i++) {
 			// get global positions
 			globalPositions[3*i] = mesh.getParticles().get(i).getPos().x();
 			globalPositions[3*i+1] = mesh.getParticles().get(i).getPos().y();
 			globalPositions[3*i+2] = mesh.getParticles().get(i).getPos().z();
 			// get global velocitiyes
 			globalVelocities[3*i] = mesh.getParticles().get(i).getVel().x();
 			globalVelocities[3*i+1] = mesh.getParticles().get(i).getVel().y();
 			globalVelocities[3*i+2] = mesh.getParticles().get(i).getVel().z();
 			// get global masses
 			if(!mesh.getParticles().get(i).isFixed()) {
 				globalMasses[3*i] = mesh.getParticles().get(i).getMass();
 				globalMasses[3*i+1] = mesh.getParticles().get(i).getMass();
 				globalMasses[3*i+2] = mesh.getParticles().get(i).getMass();
 			} else {
 				globalMasses[3*i] = 1/0.0;
 				globalMasses[3*i+1] = 1/0.0;
 				globalMasses[3*i+2] = 1/0.0;
 			}
 		}		
 		globalForces = getForcePotential(globalPositions, globalVelocities, globalMasses);
 	}
 	
 	/*
 	 * Gets the force potential given a new set of positions
 	 */
 	public double[] getForcePotential(double [] positions, double [] velocities, double [] masses) {
 		double [] forcePotential = new double[positions.length];
 		
 		//for each force potential
 		for(Force force : forces){
 			//get the local force vector
 			double [] localForce = force.getLocalForce(positions, velocities, masses);
 			//add to the global force vector at cooresponding particle
 			//indicies
 			if(force.isGlobal()) {
 				for(int i = 0; i < localForce.length; i++){
 					forcePotential[i] += localForce[i];
 				}
 			} else {
 				for(int i = 0; i < localForce.length/3; i++){
 					forcePotential[3*force.getStencilIdx(i)] += localForce[3*i];
 					forcePotential[3*force.getStencilIdx(i)+1] += localForce[3*i+1];
 					forcePotential[3*force.getStencilIdx(i)+2] += localForce[3*i+2];
 				}
 			}
 		}
 		return forcePotential;
 	}
 	
     public double[] globalForceMagnitude() {
 	return globalForces;
     }
     
     public void setGlobalForces(double[] newGlobalForces) {
 	globalForces = newGlobalForces;
     }
 
     public double[] getGlobalPositions() {
 	return globalPositions;
     }
 
     public double[] getGlobalVelocities() {
 	return globalVelocities;
     }
 
     public void setGlobalVelocities(double[] newVels) {
 	globalVelocities = newVels;
     }
 	
 	public double[] getGlobalMasses() {
 		return globalMasses;
     }
 
     public void update() {
 	updateGlobalForce();
 
 	for (Property property : properties) 
 	    property.update();
 	for (Property property : responders) {
 	    property.update();
 	}
	if (!steppedInResponse && integrator != null)
 	    integrator.update();
 	onFrame();
 	steppedInResponse = false;
     }
 
     public void hasStepped(boolean b) {
 	steppedInResponse = b;
     }
 
     public abstract void setProperties();
     public abstract void createShapes();
     public abstract void createForces();
     public abstract void onFrame();
 }
