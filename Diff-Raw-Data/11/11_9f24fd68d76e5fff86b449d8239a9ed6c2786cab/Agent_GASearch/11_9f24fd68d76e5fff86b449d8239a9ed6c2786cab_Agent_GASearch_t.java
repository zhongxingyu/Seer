 package pikater;
 
 import java.util.Random;
 
 import pikater.ontology.messages.Evaluation;
 import pikater.ontology.messages.Option;
 import pikater.ontology.messages.SearchItem;
 import pikater.ontology.messages.SearchSolution;
 import jade.util.leap.ArrayList;
 import jade.util.leap.Iterator;
 import jade.util.leap.List;
 
 public class Agent_GASearch extends Agent_Search {
 	/*
 	 * Implementation of Genetic algorithm search
 	 * Half uniform crossover, tournament selection
 	 * Options:
 	 * -E float
 	 * minimum error rate (default 0.1)
 	 * 
 	 * -M int 
 	 * maximal number of generations (default 10)
 	 * 
 	 * -T float
 	 * Mutation rate (default 0.2)
 	 * 
 	 * -X float
 	 * Crossover probability (default 0.5)
 	 * 
 	 * -P int
 	 * population size (default 5)
 	 * 
 	 * -S int
 	 * Size of tournament in selection (default 2)
 	 */
 	private ArrayList population;
 	//fitness is the error rate - the lower, the better!
 	float fitnesses[];
 	int pop_size = 0;
 	double mut_prob = 0.0;
 	double xover_prob = 0.0;
 	private int number_of_generations = 0;
 	private double best_error_rate = Double.MAX_VALUE;
 	
 	private int maximum_generations;
 	private double final_error_rate;
 	int tournament_size = 2;
 	protected Random rnd_gen = new Random(1);
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -387458001824777077L;
 	
 	@Override
 	protected List generateNewSolutions(List solutions, List evaluations) {
 		ArrayList new_population = new ArrayList(pop_size);
 		if(evaluations==null){
 			//create new population
 			
 			number_of_generations = 0;
 			best_error_rate = Double.MAX_VALUE;
 			fitnesses = new float[pop_size];
 			for(int i = 0; i < pop_size; i++){
 				new_population.add(randomIndividual());
 			}
 		} else{
 			//population from the old one
 			for(int i = 0; i < (pop_size/2);i++){
 				//pairs
 				SearchSolution ind1 = cloneSol(selectIndividual());
 				SearchSolution ind2 = cloneSol(selectIndividual());
 				
 				if(rnd_gen.nextDouble()<xover_prob){
 					xoverIndividuals(ind1, ind2);
 				}
 				mutateIndividual(ind1);
 				mutateIndividual(ind2);
 				new_population.add(ind1);
 				new_population.add(ind2);
 			}
 			if((pop_size%2)==1){
 				//one more, not in pair, if the pop is odd
 				SearchSolution ind = cloneSol(selectIndividual());
 				mutateIndividual(ind);
 				new_population.add(ind);
 			}
 		}
 		number_of_generations++;
 		population= new_population;
 		return population;
 	}
 
 	@Override
 	protected void updateFinished(List evaluations) {
 		//assign evaluations to the population as fitnesses
 		if(evaluations == null){
 			for(int i = 0; i < pop_size; i++){
 				fitnesses[i]=1;
 			}
 		}else{
 			for(int i = 0; i < evaluations.size(); i++){
 				//fitness
 				fitnesses[i]=((Evaluation)(evaluations.get(i))).getError_rate();
 				//actualize best_error_rate
 				if(fitnesses[i]<best_error_rate){
 					best_error_rate = fitnesses[i];
 				}
 			}
 		}
 
 	}
 
 	@Override
 	protected boolean finished() {
 		//number of generation, best error rate
 		if (number_of_generations >= maximum_generations) {
 			return true;
 		}
 
		if (best_error_rate <= final_error_rate) {
 			return true;
 		}
 		return false;
 
 	}
 
 	
 	@Override
 	protected String getAgentType() {
 		return "GASearch";
 	}
 
 	@Override
 	protected void loadSearchOptions() {
 		pop_size = 5;
 		mut_prob = 0.2;
 		xover_prob = 0.5;
 		maximum_generations = 10;
 		final_error_rate = 0.1;
 		tournament_size = 2;
 		List search_options = getSearch_options();
 		// find maximum tries in Options
 		Iterator itr = search_options.iterator();
 		while (itr.hasNext()) {
 			Option next = (Option) itr.next();
 			if (next.getName().equals("E")){
 				final_error_rate = Float.parseFloat(next.getValue()); 
 			}
 			if (next.getName().equals("M")){
 				maximum_generations = Integer.parseInt(next.getValue()); 
 			}
 			if (next.getName().equals("T")){
 				mut_prob = Float.parseFloat(next.getValue()); 
 			}
 			if (next.getName().equals("X")){
 				xover_prob = Float.parseFloat(next.getValue()); 
 			}
 			if (next.getName().equals("P")){
 				pop_size = Integer.parseInt(next.getValue()); 
 			}
 			if (next.getName().equals("S")){
 				tournament_size = Integer.parseInt(next.getValue()); 
 			}
 		}
 
 	}
 
 	//new random options
 	private SearchSolution randomIndividual(){
 		List new_solution = new ArrayList();
 		Iterator itr = getSchema().iterator();
 		while (itr.hasNext()) {
 			SearchItem si = ((SearchItem) itr.next());
 			String val = si.randomValue(rnd_gen);
 			new_solution.add(val);
 		}		
 		SearchSolution res_sol = new SearchSolution();
 		res_sol.setValues(new_solution);
 		return res_sol;
 	}
 	
 	//tournament selection (minimization)
 	private SearchSolution selectIndividual(){
 		float best_fit = Float.MAX_VALUE;
 		int best_index = -1;
 		for(int i = 0; i < tournament_size; i++){
 			int ind= rnd_gen.nextInt(fitnesses.length);
 			//MINIMIZATION!!!
 			if(fitnesses[ind] < best_fit){
 				best_fit = fitnesses[ind];
 				best_index = ind;
 			}
 		}
 		return (SearchSolution) population.get(best_index);
 	}
 	
 	//Half uniform crossover
 	private void xoverIndividuals(SearchSolution sol1, SearchSolution sol2){
 		List new_solution1 = new ArrayList();
 		List new_solution2 = new ArrayList();
 		Iterator itr1 = sol1.getValues().iterator();
 		Iterator itr2 = sol2.getValues().iterator();
 		while (itr1.hasNext()) {
 			String val1 = (String) itr1.next();
 			String val2 = (String) itr2.next();
 			if(rnd_gen.nextBoolean()){
 				//The same...
 				new_solution1.add(val1);
 				new_solution2.add(val2);
 			}else{
 				//Gene exchange
 				new_solution1.add(val2);
 				new_solution2.add(val1);
 			}
 		}
 		sol1.setValues(new_solution1);
 		sol2.setValues(new_solution2);
 	}
 	
 	//mutation of the option
 	private void mutateIndividual(SearchSolution sol){
 		List new_sol = new ArrayList();
 		Iterator sol_itr = sol.getValues().iterator();
 		Iterator sch_itr = getSchema().iterator();
 		while (sol_itr.hasNext()) {
 			SearchItem si = (SearchItem) sch_itr.next();
 			String val = ((String) sol_itr.next());
 			if(rnd_gen.nextDouble()<mut_prob)
 				val= si.randomValue(rnd_gen);
 			new_sol.add(val);
 		}
 		sol.setValues(new_sol);
 	}
 	
 	
 	//Clone options
 	private SearchSolution cloneSol(SearchSolution sol){
 		List new_solution = sol.getValues();
 		SearchSolution res_sol = new SearchSolution();
 		res_sol.setValues(new_solution);
 		return res_sol;
 	}
 	
 
 	
 }
