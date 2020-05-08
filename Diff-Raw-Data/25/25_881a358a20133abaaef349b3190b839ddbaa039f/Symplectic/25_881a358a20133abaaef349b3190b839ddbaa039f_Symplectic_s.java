 package uk.me.doitto.orbits;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author ian
  *
  * Top level class for symplectic integrator simulations
  */
 public class Symplectic {
 	
 	public final double iterations, g, timeStep, errorLimit;
 	
 	public double cogX, cogY, cogZ;
 	
 	public final int outputInterval, np;
 	
 	public final List<Particle> particles;
 	
 	private final Integrator integrator;
 	
 	public Symplectic (Scenario ic, Integrator integrator) {
 		this.particles = ic.bodies;
 		this.np = ic.bodies.size();
 		this.g = ic.g;
 		this.timeStep = ic.ts;
 		this.errorLimit = ic.errorLimit;
 		this.outputInterval = ic.outputInterval;
 		this.iterations = ic.simulationTime / ic.ts;
 		this.integrator = integrator;
 	}
 	
 	public Symplectic (double g, double simulationTime, double timeStep, double errorLimit, int outputInterval, List<Particle> bodies, String integrator) {
 		this.particles = new ArrayList<Particle>(bodies);
 		this.np = bodies.size();
 		this.g = g;
 		this.timeStep = timeStep;
 		this.errorLimit = errorLimit;
 		this.outputInterval = outputInterval;
 		this.iterations = simulationTime / timeStep;
 		this.integrator = Integrator.valueOf(integrator);
 	}
 
 	double distance (double xA, double yA, double zA, double xB, double yB, double zB) {
 		return Math.sqrt(Math.pow(xB - xA, 2) + Math.pow(yB - yA, 2) + Math.pow(zB - zA, 2));
 	}
 	
 	public double hamiltonian () {
 		double energy = 0.0;
 		for (int i = 0; i < np; i++) {
 			Particle a = particles.get(i);
 			energy += 0.5 * (a.pX * a.pX + a.pY * a.pY + a.pZ * a.pZ) / a.mass;
 			for (int j = 0; j < np; j++) {
 				if (i > j) {
 					Particle b = particles.get(j);
 					energy -= g * a.mass * b.mass / distance(a.qX, a.qY, a.qZ, b.qX, b.qY, b.qZ);
 				}
 			}
 		}
 		return energy;
 	}
 	
 	void cog () {
 		double X = 0.0;
 		double Y = 0.0;
 		double Z = 0.0;
 		double mT = 0.0;
 		for (int i = 0; i < np; i++) {
 			Particle a = particles.get(i);
 			X += a.qX * a.mass;
 			Y += a.qY * a.mass;
 			Z += a.qZ * a.mass;
 			mT += a.mass;
 		}
 		cogX = X / mT;
 		cogY = Y / mT;
 		cogZ = Z / mT;
 	}
 	
 	public void solveQP () {
 		this.integrator.solve(this, PhaseSpace.Q, PhaseSpace.P);
 	}
 	
 	public void solvePQ () {
 		this.integrator.solve(this, PhaseSpace.P, PhaseSpace.Q);
 	}
 	
 	public String particlesJson () {
 		StringBuilder json = new StringBuilder("[");
		for (Particle p : particles) {
			json.append(p.toString());
 		}
 		return (json + "]");
 	}
 }
