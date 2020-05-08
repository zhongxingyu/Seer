 package district;
 import graph.Tuple;
 
 import java.util.Random;
 
 import algorithms.SimulatedAnnealing;
 
 
 import datastructures.Charges;
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
		int houses=20;
 
 		Groundplan plan = planWijk(houses);
 		printSolution(plan);
 		
 	}
 
 	/**
 	 * Startpunt voor het oplossen van de opdracht
 	 */
 	public Groundplan planWijk(int houses) {
 		int infeasiblesolutions=0;
 		Groundplan optimalSolution=null;
 		double bestsolution=0;
 		Charges charges = new Charges(1,1,1,0,0);
 		Groundplan currentSolution=null;
		double size = Groundplan.WIDTH * Groundplan.HEIGHT * Groundplan.MINIMUM_WATER_PERCENTAGE;
 		double width =Math.sqrt(size/4);
 		double height=size/width;
 		
		algorithm = new SimulatedAnnealing(randomPlan(houses,width,height));
 		optimalSolution=algorithm.getGroundplan();
 		while(height>31)
 		{
 			//Calc initial solution:
 			currentSolution=algorithm.getOptimalSolution(1000,charges);
			charges = new Charges(1,1,1,0,0);
 			printSolution(currentSolution);		
 			
 			currentSolution = runSimulatedAnnealingChangingCharge(houses,
 					infeasiblesolutions, charges, currentSolution, width,
 					height);
 			width++;
 			height=size/width;
 			if(currentSolution.isValid() && currentSolution.getPlanValue()>optimalSolution.getPlanValue())
 			{
 				optimalSolution=currentSolution;
 				bestsolution=optimalSolution.getPlanValue();
 				System.out.println("Best value: "+bestsolution);
 			}
 		}
 		return currentSolution;
 	}
 
 	private Groundplan runSimulatedAnnealingChangingCharge(int houses,
 			int infeasiblesolutions, Charges charges,
 			Groundplan currentSolution, double width, double height) {
 		
 		Groundplan solution;
 		while(infeasiblesolutions<=2)
 		{
 			//run x iterations of simulated annealing algorithm
			algorithm = new SimulatedAnnealing(randomPlan(houses,width,height));
 			printSolution(currentSolution);
 			
 			solution= algorithm.getOptimalSolution(1000,charges);
 			
 			printSolution(solution);
 			if(!solution.isValid())
 				infeasiblesolutions++;
 			else infeasiblesolutions=0;
 			if(solution.getPlanValue()>currentSolution.getPlanValue() && solution.isValid())
 			{
 				System.out.println("Value: "+solution.getPlanValue()+" Feasible: "+solution.isValid());
 				currentSolution=solution;
 			}
 			else infeasiblesolutions++;
 			increaseCharges(charges);
 		}
 		return currentSolution;
 	}
 
 	private void printSolution(Groundplan solution) {
 		frame.setPlan(solution);
 		//System.out.println("Value: "+solution.getPlanValue()+" Feasible:"+solution.isValid());
 	}
 
 	private void increaseCharges(Charges charges) {
 		double randomnumber = random.nextDouble();
 		if(randomnumber<=0.33)
 			charges.cottagecharge+=0.5;
 		else if(randomnumber<=0.66)
 			charges.bungalowcharge+=0.5;
 		else
 			charges.mansioncharge+=0.5;		
 	}
 
	private Groundplan randomPlan(int houses,double waterwidth,double waterheight) {
 		Groundplan plan = new Groundplan(houses);
 		
 		for (int i = 0; i < Groundplan.MINIMUM_COTTAGE_PERCENTAGE * houses; i++) {
 			plan.addResidence(new Cottage(random.nextDouble()
 					* Groundplan.WIDTH, random.nextDouble() * Groundplan.HEIGHT));
 		}
 
 		for (int i = 0; i < Groundplan.MINIMUM_BUNGALOW_PERCENTAGE *houses ; i++) {
 			plan.addResidence(new Bungalow(random.nextDouble()
 					* Groundplan.WIDTH, random.nextDouble() * Groundplan.HEIGHT));
 		}
 
 		for (int i = 0; i < Groundplan.MINIMUM_MANSION_PERCENTAGE * houses; i++) {
 			plan.addResidence(new Mansion(random.nextDouble()
 					* Groundplan.WIDTH, random.nextDouble() * Groundplan.HEIGHT));
 		}

		plan.addWaterBody(new WaterBody(random.nextDouble() * (Groundplan.WIDTH - (waterwidth/4)),
 				random.nextDouble() * (Groundplan.HEIGHT - (waterheight/4)), waterwidth, waterheight));
 		
 		//System.out.println("Value of the plan: "+plan.getPlanValue());
 		return plan;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new DistrictPlanner();
 	}
 }
