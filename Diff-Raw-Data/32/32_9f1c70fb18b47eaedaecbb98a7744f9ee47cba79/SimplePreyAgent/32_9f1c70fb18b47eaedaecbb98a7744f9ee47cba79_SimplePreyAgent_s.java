 package chalmers.dax021308.ecosystem.model.agent;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.lowagie.text.pdf.ArabicLigaturizer;
 
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
 import chalmers.dax021308.ecosystem.model.util.Gender;
 import chalmers.dax021308.ecosystem.model.util.Position;
 import chalmers.dax021308.ecosystem.model.util.Vector;
 
 /**
  * A basic implementation of the IAgent interface.
  * 
  * @author Albin
  */
 public class SimplePreyAgent extends AbstractAgent {
 
 	private boolean hungry = true;
 
 	public SimplePreyAgent(String name, Position p, Color c, int width,
 			int height, Vector velocity, double maxSpeed,
 			double maxAcceleration, double visionRange) {
 		super(name, p, c, width, height, velocity, maxSpeed, visionRange,
 				maxAcceleration);
 	}
 
 	@Override
 	public List<IAgent> reproduce(IAgent agent) {
 		if (hungry)
 			return null;
 		else {
 			List<IAgent> spawn = new ArrayList<IAgent>();
			if (Math.random() < 0.1) {
 				hungry = true;
				Position pos = new Position(getPosition());
				IAgent child = new SimplePreyAgent(name, pos, color, width, height, velocity,
 						maxSpeed, maxAcceleration, visionRange);
 				spawn.add(child);
 			}
 			return spawn;
 		}
 	}
 
 	/**
 	 * @author Sebbe
 	 */
 	@Override
 	public void calculateNextPosition(List<IPopulation> predators,
 			List<IPopulation> preys, List<IPopulation> neutral,
 			Dimension gridDimension) {
 
 		Vector predatorForce = getPredatorForce(predators);
 		Vector separationForce = getSeparationForce(neutral);
 		// Vector separationForce = new Vector();
 		Vector environmentForce = getEnvironmentForce(gridDimension);
 		Vector preyForce = getPreyForce(preys);
 
 		/*
 		 * Sum the forces from walls, predators and neutral to form the
 		 * acceleration force. If the acceleration exceeds maximum acceleration
 		 * --> scale it to maxAcceleration, but keep the correct direction of
 		 * the acceleration.
 		 */
 		Vector acceleration = environmentForce.multiply(100)
 				.add(predatorForce.multiply(3))
 				.add(separationForce.multiply(10).add(preyForce));
 		double accelerationNorm = acceleration.getNorm();
 		if (accelerationNorm > maxAcceleration) {
 			acceleration.multiply(maxAcceleration / accelerationNorm);
 		}
 
 		/*
 		 * The new velocity is then just: v(t+dt) = (v(t)+a(t+1)*dt)*decay,
 		 * where dt = 1 in this case. There is a decay that says if they are not
 		 * affected by any force, they will eventually stop. If speed exceeds
 		 * maxSpeed --> scale it to maxSpeed, but keep the correct direction.
 		 */
 		Vector newVelocity = this.getVelocity().add(acceleration)
 				.multiply(VELOCITY_DECAY);
 		double speed = newVelocity.getNorm();
 		if (speed > maxSpeed) {
 			newVelocity.multiply(maxSpeed / speed);
 		}
 
 		this.setVelocity(newVelocity);
 		nextPosition = Position.positionPlusVector(position, velocity);
 	}
 
 	/**
 	 * @author Henrik
 	 * @param preys
 	 *            The list of preys to eat
 	 * @return returns The force the preys attracts the agent with
 	 */
 	private Vector getPreyForce(List<IPopulation> preys) {
 		Vector preyForce = new Vector(0, 0);
 		for (IPopulation pop : preys) {
 			List<IAgent> agents = pop.getAgents();
 			for (int i = 0; i < pop.getAgents().size(); i++) {
 				IAgent a = agents.get(i);
 				Position p = a.getPosition();
 				double distance = getPosition().getDistance(p);
 				if (distance <= visionRange) {
 					if (distance <= INTERACTION_RANGE) {
 						// Food found, let's eat it and make some reproducing
 						// possible
 						pop.getAgents().remove(i);
 						hungry = false;
 					} else {
 						Vector newForce = new Vector(p, getPosition());
 						double norm = newForce.getNorm();
 						preyForce.add(newForce.multiply(1 / (norm * distance)));
 					}
 				}
 			}
 		}
 		preyForce.getNorm();
 		return preyForce;
 	}
 
 	/**
 	 * @author Sebbe "Predator Force" is defined as the sum of the vectors
 	 *         pointing away from all the predators in vision, weighted by the
 	 *         inverse of the distance to the predators, then normalized to have
 	 *         unit norm. Can be interpreted as the average sum of forces that
 	 *         the agent feels, weighted by how close the source of the force
 	 *         is.
 	 */
 	private Vector getPredatorForce(List<IPopulation> predators) {
 		Vector predatorForce = new Vector(0, 0);
 		int nVisiblePredators = 0;
 		for (IPopulation pop : predators) {
 			for (IAgent predator : pop.getAgents()) {
 				Position p = predator.getPosition();
 				double distance = getPosition().getDistance(p);
 				if (distance <= visionRange) { // If predator is in vision range
 												// for prey
 					/*
 					 * Create a vector that points away from the predator.
 					 */
 					Vector newForce = new Vector(this.getPosition(), p);
 
 					/*
 					 * Add this vector to the predator force, with proportion to
 					 * how close the predator is. Closer predators will affect
 					 * the force more than those far away.
 					 */
 					double norm = newForce.getNorm();
 					predatorForce.add(newForce.multiply(1 / (norm * distance)));
 					nVisiblePredators++;
 				}
 			}
 		}
 
 		if (nVisiblePredators == 0) { // No predators near --> Be unaffected
 			predatorForce.setVector(0, 0);
 		} else { // Else set the force depending on visible predators and
 					// normalize it to maxAcceleration.
 			double norm = predatorForce.getNorm();
 			predatorForce.multiply(maxAcceleration / norm);
 		}
 
 		return predatorForce;
 	}
 
 }
