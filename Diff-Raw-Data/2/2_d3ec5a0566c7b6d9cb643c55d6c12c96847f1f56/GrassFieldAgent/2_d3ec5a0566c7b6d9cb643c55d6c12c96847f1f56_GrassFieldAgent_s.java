 package chalmers.dax021308.ecosystem.model.agent;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 
 import chalmers.dax021308.ecosystem.model.environment.SurroundingsSettings;
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
 import chalmers.dax021308.ecosystem.model.util.Position;
 import chalmers.dax021308.ecosystem.model.util.Vector;
 
 /**
  * Grass field, lowest part of the food chain. Instead of dying and getting
  * removed like the normal grassAgent, it stores a limited amount of energy
  * which decreases when another agent eats it.
  * 
  * @author Henrik
  */
 public class GrassFieldAgent extends AbstractAgent {
 
 	private static final double REPRODUCTION_RATE = 0.1;
 	private static final double MAX_ENERGY = 200;
 	private List<GrassSeed> seeds;
 
 	public GrassFieldAgent(String name, Position pos, Color color, int width,
 			int height, Vector velocity, double maxSpeed, int capacity) {
 		super(name, pos, color, width, height, velocity, maxSpeed, 0, 0);
 		this.capacity = capacity;
 		energy = 50;
		seeds = new ArrayList<>();
 	}
 
 	@Override
 	public void calculateNextPosition(List<IPopulation> predators,
 			List<IPopulation> preys, List<IPopulation> neutral,
 			SurroundingsSettings surroundings) {
 		// Do nothing, grass shouldn't move!
 	}
 
 	@Override
 	public void updatePosition() {
 		// Do nothing? Shouldn't get old or anything
 	}
 
 	@Override
 	public List<IAgent> reproduce(IAgent agent, int populationSize,
 			SurroundingsSettings surroundings) {
 		for (int i = 0; i < seeds.size(); i++) {
 			if (seeds.get(i).isBlooming()) {
 				energy += seeds.get(i).getEnergy();
 				if (energy > MAX_ENERGY)
 					energy = MAX_ENERGY;
 				seeds.remove(i--);
 			}
 		}
 		if (Math.random() < REPRODUCTION_RATE) {
 			double energyProportion = (double) energy / (double) MAX_ENERGY;
 			double newEnergy = energy * energyProportion
 					* (1 - energyProportion) * 0.1;
 			// System.out.println(newEnergy);
 			energy += newEnergy;
 			// seeds.add(new GrassSeed(newEnergy));
 		}
 		int red = (int) (150.0 - 150.0 * (((double) energy) / MAX_ENERGY));
 		int green = (int) (55.0 + 200.0 * (((double) energy) / MAX_ENERGY));
 		this.color = new Color(red, green, 0);
 		return null;
 
 	}
 
 	@Override
 	public synchronized boolean tryConsumeAgent() {
 		// Let the current agent be eaten if it has enough energy
 		if (energy >= 8) {
 			energy -= 8;
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean isLookingTasty(IAgent agent, double visionRange) {
 		double distance = agent.getPosition().getDistance(position)
 				- (width + height) / 2;
 		// If the agent has enough food for three agents, then it looks tasty!
 		if (energy >= 24)
 			return distance <= visionRange;
 		return false;
 	}
 
 	@Override
 	public double impactForcesBy() {
 		// if it has a low amount of food it should negatively impact the agents
 		// who wants to eat it
 		 return 1 - energy / 100;
 		// but for now it just means less food = less impact
 		//return energy / 200;
 	}
 
 	private class GrassSeed {
 		private int lifeLength = 0;
 		private double energy;
 		private static final int TIME_TO_BLOOM = 100;
 
 		private GrassSeed(double energy) {
 			this.energy = energy;
 		}
 
 		private double getEnergy() {
 			return energy;
 		}
 
 		private boolean isBlooming() {
 			return lifeLength++ > TIME_TO_BLOOM;
 		}
 	}
 }
