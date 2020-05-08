 package district;
 import java.util.Random;
 
 import algorithms.SimulatedAnnealing;
 
 
 import districtobjects.Bungalow;
 import districtobjects.Cottage;
 import districtobjects.Mansion;
 import districtobjects.Residence;
 import districtobjects.WaterBody;
 
 /**
  * De wijkplanner is de basis file voor de Heuristieken opdracht Amstelhaege.
  * Het algoritme voor het oplossen van deze opdracht kan/hoort in deze class
  * geschreven te worden te beginnen in de functie planWijk.
  * 
  * @author bweel
  * 
  */
 public class DistrictPlanner {
 	Random random;
 	GroundplanFrame frame;
 	SimulatedAnnealing algorithm;
 	private static final int SCALE = 1;
 
 	public DistrictPlanner() {
 		random = new Random(1);
 		frame = new GroundplanFrame();
 		algorithm = new SimulatedAnnealing(randomPlan());
 		while(true)
 		{
 			Groundplan plan = planWijk();
 			frame.setPlan(plan);
 			System.out.println("Value: "+plan.getPlanValue()+" Feasible:"+plan.isValid());
 		}
 	}
 
 	/**
 	 * Startpunt voor het oplossen van de opdracht
 	 */
 	public Groundplan planWijk() {
 		// TODO: Implementeer je algoritme hier.
 		return algorithm.getOptimalSolution(999);
 	}
 
 	private Groundplan randomPlan() {
 		Groundplan plan = new Groundplan(20);
 		
		for (int i = 0; i < Groundplan.MINIMUM_COTTAGE_PERCENTAGE * 20; i++) {
 			plan.addResidence(new Cottage(random.nextDouble()
 					* Groundplan.WIDTH, random.nextDouble() * Groundplan.HEIGHT));
 		}
 
 		for (int i = 0; i < Groundplan.MINIMUM_BUNGALOW_PERCENTAGE * 20; i++) {
 			plan.addResidence(new Bungalow(random.nextDouble()
 					* Groundplan.WIDTH, random.nextDouble() * Groundplan.HEIGHT));
 		}
 
 		for (int i = 0; i < Groundplan.MINIMUM_MANSION_PERCENTAGE * 20; i++) {
 			plan.addResidence(new Mansion(random.nextDouble()
 					* Groundplan.WIDTH, random.nextDouble() * Groundplan.HEIGHT));
 		}
 
 		double size = Groundplan.WIDTH * Groundplan.HEIGHT * Groundplan.MINIMUM_WATER_PERCENTAGE;
 		double length = Math.sqrt(size);
 		plan.addWaterBody(new WaterBody(random.nextDouble() * (Groundplan.WIDTH - length),
 				random.nextDouble() * (Groundplan.HEIGHT - length), length, length));
 		
 		System.out.println("Value of the plan: "+plan.getPlanValue());
 		return plan;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new DistrictPlanner();
 	}
 }
