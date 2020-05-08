 package knapsack;
 
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.Collections;
 import java.util.Random;
 import java.util.Iterator;
 
 import model.Genome;
 import model.PopMember;
 import model.Population;
 
 
 /*
  * SortedPopulation.java
  *
  * Implementation of the Popluation class.
  *
  */
 
 public class SortedPopulation implements Population 
 {
 	private SackItem[] items;
 	private double capacity;
     private PopMember[] population;
     private ArrayList<Integer> seqInts;
     
    /*
      * Name: SortedPopulation
      * Description: Basic constructor
      *
      */
     public SortedPopulation()
     {
         // creates population of size 500, with 1 placeholder.
         population = new PopMember[501];
     }
 
     /*
      * Name: insertionSort
      * Description: Performs an insertion sort on the population.
      */
     public void sort()
     {
         // This may be wrong, kinda rushed
         for(int i = 0; i < population.length; i++)
         {
             int j = i;
             PopMember curr = population[i];
 
             while((j > 0 && (population[j-1].getFitness() < curr.getFitness())))
             {
                 population[j] = population[j-1];
                 j--;
             }
 
             population[j] = curr;
         }
     }
     
     /*
      * Name: Insert
      * Description: Inserts a population member into the popluation.
      *
      * Parameters:
      *      member - member to be inserted into the population.
      */
     public void insert(PopMember member)
     {
         // inserts member as last of population
         population[population.length-1] = member;
         
         // sorts the population
         sort();
     }
 
     /*
      * Name: Populate
      * Description: Creates an initial population.
      */
     public void populate() {
    	Random rand = new Random();
     	for (int i=0; i < population.length; i++) {
 			Collections.shuffle(seqInts);
             
             BitSet bits = new BitSet(items.length);
 			bits.clear();
 			
 			int bit;
 			double weight = 0;
 			for (int j=0; j < seqInts.size(); j++) {
                 bit = (seqInts.get(j));
                 weight += items[bit].getVolume();
                 if (weight > capacity) {
                     weight -= items[bit].getVolume();
                     break;
                 }
                 bits.set(bit);
             }
             /*do {
                 System.out.println("pong");
 				bit = rand.nextInt(items.length);
 				if (!bits.get(bit)) {
 					bits.set(bit);
 					weight += items[bit].getVolume();
 				}
     		} while (weight <= capacity);
 			bits.flip(bit);
 			
             weight -= items[bit].getVolume();
 			*/
 			Genome genome = new Genome(bits);
 			double score = evaluateFitness(genome);
 			if (i < population.length)
                 population[i] = new PopMember(genome, score);
     	    else {
                 sort();
                 population[population.length] = new PopMember(genome, score);
             }
         }
     	sort();
     }
     
     /*
      * Name: getPopulation
      * Description: Returns the population.
      *
      * Returns:
      *      popMember array representing the population.
      */
     public PopMember[] getPopulation()
     {
         return population;
     }
     
     /*
      * Name: Remove
      * Description: Removes a member from the population.
      *
      * Parameters:
      *      member to be removed.
      */
     public void remove(PopMember member)
     {
         int found = -1;
 
         // finds the member in the population
         for(int i = 0; i < population.length; i++)
         {
             if(population[i] == member)
             {
                 found = i;
                 break;
             }
         }
 
         if (found != -1) {
         	PopMember remove = population[found];
         
 	        // Moves the rest of the population down
 	        for(int j = found; j < population.length; j++)
 	        {
 	            population[j] = population[j+1];
 	        }
 	
 	        // sets element as last of array to be overwritten.
 	        population[population.length-1] = remove;
         }
     }
 
     /*
      * Name: getBest
      * Description: Returns the best member of the popluation.
      *
      * Returns: 
      *      Best PopMember in the population.
      */
     public PopMember getBest()
     {
         return population[0];
     }
     
     public PopMember getWorst()
     {
     	return population[population.length-2];
     }
 
     /*
      * Name: returnParents
      * Description: Returns specified parents using the group selection method
      * 				First  1/4 - 50%
      * 				Second 1/4 - 30%
      * 				Third  1/4 - 15%
      * 				Last   1/4 - 5%
      *
      * Parameters:
      * 		numParents - number of parents to select
      * 
      * Returns:
      *      PopMember array of parents.
      */
     public PopMember[] returnParents(int numParents)
     {
         PopMember[] parents = new PopMember[numParents];
      
         Random rand = new Random();
         
         for (int i=0; i < numParents; i++) {
 	        double select = rand.nextDouble();
 	        
 	        int parentSelection;
 	        if (select < 0.50) {
 	        	parentSelection = 0;
 	        } else if (select < 0.80) {
 	        	parentSelection = (int) (0.25 * population.length);
 	        } else if (select < 0.95) {
 	        	parentSelection = (int) 0.50 * population.length;
 	        } else {
 	        	parentSelection = (int) 0.75 * population.length;
 	        }
 	        
 	        parents[i] = population[rand.nextInt(population.length/4) + parentSelection];
     	}
         
         return parents;
     }
 
 	/**
 	 * Generates a double value for how desirable the solution contained within a given Genotype
 	 * is. This value is for comparison purposes with other Genotype objects within the same population
 	 * @param genotype
 	 * @return a fitness value
 	 */
 	public double evaluateFitness(Genome genotype) {		
 		BitSet bits = genotype.getBits();
 		
 		double fitness = 0;
 		double weight = 0;
 		for (int i=0; i < items.length; i++) {
 			if (bits.get(i)) {
 				fitness += items[i].getBenefit();
 				weight  += items[i].getVolume();
 			}
 		}
 		
 		if (weight > capacity)
 			return 0;
 		return fitness;
 	}
 	
 	public SackItem[] getItems() {
 		return items;
 	}
 	
 	public void setItems(SackItem[] items) {
 		this.items = items;
         seqInts = new ArrayList<Integer>(items.length);
         for (int i=0; i < items.length; i++) {
             seqInts.add(i);
         }
 	}
 	
 	public double getCapacity() {
 		return capacity;
 	}
 	
 	public void setCapacity(double capacity) {
 		this.capacity = capacity;
 	}
 	
     public Iterator<PopMember> iterator()
     {
        return new PopMemberIterator();
     }
     
     //Iterator for PopMembers.
     private class PopMemberIterator implements Iterator<PopMember>
     {
         private int location = 0;
 
         /*
          * Name: hasNext
          * Description: Checks to see if there are any more
          * members of the population.
          *
          * Return:
          *      true if there are more popMembers in population.
          */
         public boolean hasNext()
         {
             if(location >= population.length-1) return false;
             return true;
         }
 
         /*
          * Name: next
          * Description: returns the next member of the population.
          *
          * Return:
          *      the next PopMember in the population.
          */
         public PopMember next()
         {
             location++;
             return population[location];
         }
 
         /*
          * Name: remove
          * Description: Removes the member at the current location.
          */
         public void remove()
         {
              SortedPopulation.this.remove(population[location]);
         }        
    }
 }
