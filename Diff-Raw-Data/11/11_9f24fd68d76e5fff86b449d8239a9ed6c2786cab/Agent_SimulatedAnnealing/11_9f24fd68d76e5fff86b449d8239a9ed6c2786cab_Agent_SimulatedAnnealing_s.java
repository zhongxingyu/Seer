 package pikater;
 
 import java.util.Random;
 
 import pikater.ontology.messages.Evaluation;
 import pikater.ontology.messages.Option;
 import pikater.ontology.messages.SearchItem;
 import pikater.ontology.messages.SearchSolution;
 import jade.util.leap.ArrayList;
 import jade.util.leap.Iterator;
 import jade.util.leap.List;
 
 public class Agent_SimulatedAnnealing extends Agent_Search {
 	/*
 	 * Implementation of Simulated Annealing search
 	 * Options:
 	 * -E float
 	 * minimum error rate (default 0.1)
 	 * 
 	 * -M int 
 	 * maximal number of generations (default 10)
 	 * 
 	 * -T float
 	 * Initial temperature (default 1.0)
 	 * 
 	 * -S float
 	 * Stability of generation of new option - probability of keeping of option (default 0.5)
 	 */
 	private static final long serialVersionUID = -5087231723984887596L;
 	private SearchSolution solution = null;
 	private SearchSolution new_solution = null;
 	private float evaluation = Float.MAX_VALUE;
 	private double temperature = 0.0;
 	private double stability = 0.5;
 	private int number_of_tries = 0;
 	private int maximum_tries = 50;
 	private double best_error_rate = 1;
 	private double final_error_rate = 0.1;
 	private boolean minimization = true;
 	protected Random rnd_gen = new Random(1);
 	@Override
 	protected String getAgentType() {
 		return "SimulatedAnnealing";
 	}
 
 	@Override
 	protected void loadSearchOptions() {
 		temperature = 1.0;//?
 		maximum_tries = 50;
 		stability = 0.5;
 		final_error_rate = 0.01;
 		List search_options = getSearch_options();
 		Iterator itr = search_options.iterator();
 		while (itr.hasNext()) {
 			Option next = (Option) itr.next();
 			if (next.getName().equals("E")){
 				final_error_rate = Float.parseFloat(next.getValue()); 
 			}
 			if (next.getName().equals("M")){
 				maximum_tries = Integer.parseInt(next.getValue()); 
 			}
 			if (next.getName().equals("S")){
 				stability = Float.parseFloat(next.getValue()); 
 			}
 			if (next.getName().equals("T")){
 				temperature = Float.parseFloat(next.getValue()); 
 			}
 		}
 	}
 	
 	@Override
 	protected boolean finished() {
 		//n>=nmax
 		if (number_of_tries >= maximum_tries) {
 			return true;
 		}
 		if (best_error_rate <= final_error_rate){
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	protected List generateNewSolutions(List solutions, List evaluations) {
 		
 		if(evaluations == null){
 			//inicializace
 			solution = null;
 			new_solution = null;
 			evaluation = Float.MAX_VALUE;
 			number_of_tries = 0;
 			best_error_rate = Double.MAX_VALUE;
 		}
 		
 		//create a new solution for evaluation
 		new_solution = Neighbor(solution);
 		
 		number_of_tries++;
 		
 		//List of solutions to send
 		List solutions_list = new ArrayList();
 		solutions_list.add(new_solution);
 		return solutions_list;
 	}
 	
 	@Override
 	protected void updateFinished(List evaluations) {
 		float new_evaluation;
 		
 		if (evaluations == null){
 			new_evaluation = Float.MAX_VALUE;
 		}
 		else{
 			new_evaluation = ((Evaluation)(evaluations.get(0))).getError_rate();
 		}
 		
 		//Actualize best evaluation
 		if(new_evaluation < best_error_rate){
 			best_error_rate = new_evaluation;
 		}
 		//Acceptance of new solutions
 		//System.out.print("<OK:> Temp:"+temperature+", e0: "+evaluation);
 		if (rnd_gen.nextDouble()<(acceptanceProb(new_evaluation-evaluation,temperature))){
 			solution = new_solution;
 			evaluation = new_evaluation;
 		}
 		//System.out.println(", e1:"+ new_evaluation+", acceptance: "+ acc+" ,.5->1:"+ acceptanceProb(1-0.5,temperature)+" ,1->.5:"+ acceptanceProb(0.5-1,temperature));
 		//Decrease temperature
 		Cooling();
 	}
 	
 	//Neighbor function: Random solutions in case of beginning, or mutation of existing
 	private SearchSolution Neighbor(SearchSolution sol){
 		List new_solution = new ArrayList();
 		if(sol == null){
 			//Completely new solution
 			Iterator itr = getSchema().iterator();
 			while (itr.hasNext()) {
 				//dont want to change old solutions
 				SearchItem si  = (SearchItem) itr.next();
 				new_solution.add(si.randomValue(rnd_gen));
 			}
 		}else{
 			//Neighbor function
 			Iterator sol_itr = sol.getValues().iterator();
 			Iterator schema_itr = getSchema().iterator();
 			while (sol_itr.hasNext()) {
 				String val = ((String) sol_itr.next());
 				if(rnd_gen.nextDouble() > stability)
					val = ((SearchItem)schema_itr.next()).randomValue(rnd_gen);
 				new_solution.add(val);
 			}
 		}
 		SearchSolution res_sol = new SearchSolution();
 		res_sol.setValues(new_solution);
 		return res_sol;
 	}
 	
 	/*Acceptance probability of annealed solutions: 
 	  -the better values are accepted
 	  -the worse with probability exp((e-e_new)/temperature)
 	*/
 	private double acceptanceProb(double delta, double temperature){
 		if(!minimization){/*for max problems*/
 			delta = -delta;
 		}
 		if(delta<0){//it is better
 			return 1.0;
 		}else{
 			return Math.exp(-delta/temperature);
 		}
 	}
 	
 	//Cooling scheme: 20% decrease in each step	
 	private void Cooling(){
 		temperature = 0.8*temperature;
 	}
 
 
 }
