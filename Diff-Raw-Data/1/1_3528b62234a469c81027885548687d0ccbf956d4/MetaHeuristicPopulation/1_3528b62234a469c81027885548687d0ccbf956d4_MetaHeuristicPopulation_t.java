 package uk.ac.ed.inf.pepa.eclipse.ui.wizards.capacityplanning;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 
 public class MetaHeuristicPopulation {
 	
 	private ArrayList<ModelObject> mPopulation;
 	private boolean isCanceled = false;
 	Random generator = new Random();
 
 	public MetaHeuristicPopulation() {
 		this.mPopulation = new ArrayList<ModelObject>();
 	}
 	
 	/**
 	 * initialise the population, and start the evolution
 	 * @param monitor
 	 * @return
 	 */
 	public IStatus initialise(final IProgressMonitor monitor){
 		
 		IStatus test = null;
 		monitor.beginTask(null, IProgressMonitor.UNKNOWN);
 		
 		//setup best
 		CPAParameters.best = new ModelObject(monitor);
 		//CPAParameters.source += CPAParameters.best.toString();
 		
 		//Make the candidate population even :)
 		//bit cheeky, but saves having to write more UI!
		CPAParameters.candidatePopulationSize = CPAParameters.metaheuristicParameters.get("Candidate Population Size:").intValue();
 		if(!((CPAParameters.candidatePopulationSize%2)==0)){
 			CPAParameters.candidatePopulationSize++;
 		}
 		
 		//setup working ModelObject(s)
 		for(int i = 0; i < CPAParameters.candidatePopulationSize; i++){
 			ModelObject temp = new ModelObject(monitor);
 			//randomize the population seed, but keep one of them as original
 			if(i < (CPAParameters.candidatePopulationSize -1)){
 				temp.mutateMe();
 			}
 			this.mPopulation.add(temp);
 		}
 		
 		
 		//Go into repeating Queue
 		test = this.generator(CPAParameters.metaheuristicParameters.get("Generations:").intValue(), monitor);
 		
 		monitor.done();
 		return test;
 		
 	}
 	
 	/**
 	 * Evolution generator
 	 * @param generations
 	 * @param monitor
 	 * @return
 	 */
 	public IStatus generator(int generations, IProgressMonitor monitor){
 		
 		while(generations > 0){
 			
 			//cancel options
 			if (monitor.isCanceled() == true) {
 				this.isCanceled = true;
 				break;
 			}
 			
 			//Do hill climbing
 			if(CPAParameters.metaHeuristicChoice == 0){
 				for(int i = 0; i < CPAParameters.candidatePopulationSize; i++){
 					ModelObject temp = this.mPopulation.get(i);
 					if(temp.getFitness() < CPAParameters.best.getFitness()){
 						CPAParameters.best.setModelObject(temp.getSystemEquation());
 						CPAParameters.source += (CPAParameters.metaheuristicParameters.get("Generations:").intValue() - generations) + "," + CPAParameters.best.toString();
 					}
 					temp.mutateMe();
 				}
 				
 			//Do Genetic Algorithm
 			} else {
 				for(int i = 0; i < CPAParameters.candidatePopulationSize; i++){
 					
 					ModelObject temp = this.mPopulation.get(i);
 					if(temp.getFitness() < CPAParameters.best.getFitness()){
 						CPAParameters.best.setModelObject(temp.getSystemEquation());
 						CPAParameters.source += (CPAParameters.metaheuristicParameters.get("Generations:").intValue() - generations) + "," + CPAParameters.best.toString();
 					}
 					temp.mutateMe();
 					
 				}
 				ArrayList<ModelObject> q = new ArrayList<ModelObject>();
 				for(int i = 0; i < CPAParameters.candidatePopulationSize; i++){
 					
 					//tournament selection of ModelObjects from current population
 					ModelObject parentA = tournamentSelection();
 					ModelObject parentB = tournamentSelection();
 					
 					//cross over parents to produce two children
 					onePointCrossOver(parentA, parentB);
 					
 					//mutate both children
 					parentA.mutateMe();
 					parentB.mutateMe();
 					
 					//put children into 'q'
 					q.add(parentA);
 					q.add(parentB);
 					
 				}
 				//replace population with 'q'
 				mPopulation = q;
 			}
 				
 				
 			generations--;
 		}
 		
 		if (!isCanceled) {
 			
 			return Status.OK_STATUS;
 
 		} else
 			return Status.CANCEL_STATUS;
 	}
 	
 	public void mutateAll(){
 		for(ModelObject m : this.mPopulation){
 			m.mutateMe();
 		}
 	}
 	
 
 	public String toString() {
 		String temp = "";
 		for(ModelObject m : this.mPopulation){
 			temp += m.toString();
 		}
 		return temp;
 	}
 	
 	public ModelObject tournamentSelection(){
 		
 		int index = generator.nextInt(mPopulation.size());
 		ModelObject first = mPopulation.get(index);
 		//this could become a user setting...
 		int tournamentSize = 2;
 		for(int i = 0; i < tournamentSize; i++){
 			index = generator.nextInt(mPopulation.size());
 			ModelObject second = mPopulation.get(index);
 			if(second.getFitness() > first.getFitness()){
 				first = second;
 			}
 		}
 		
 		return first;
 	}
 	
 	public void onePointCrossOver(ModelObject a, ModelObject b){
 		
 		Map<String, Double> aSystemEquation = a.getSystemEquation();
 		Map<String, Double> bSystemEquation = b.getSystemEquation();
 		
 		int index = generator.nextInt(aSystemEquation.size() + 1);
 		int i = 0;
 		
 		//wasted cycles
 		//improve this so that we only loop over the ones we need to change...
 		for(Entry<String, Double> d : aSystemEquation.entrySet()){
 			if(i > index){
 				Double aPopulation = d.getValue();
 				Double bPopulation = bSystemEquation.get(i);
 				String componentName = d.getKey();
 				
 				bSystemEquation.put(componentName, aPopulation);
 				aSystemEquation.put(componentName, bPopulation);
 			}
 		}
 		
 		a.setModelObject(aSystemEquation);
 		b.setModelObject(bSystemEquation);
 		
 	}
 	
 }
