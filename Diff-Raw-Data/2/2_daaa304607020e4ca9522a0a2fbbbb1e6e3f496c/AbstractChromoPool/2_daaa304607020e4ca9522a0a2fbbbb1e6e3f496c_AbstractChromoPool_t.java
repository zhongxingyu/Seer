 package neuralNets.lib.learning.GA;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import neuralNets.lib.learning.GA.chromosomes.Chromosome;
 import neuralNets.matlab.Settings;
 
 /**
  * Abstract chromopool implementation.
  * Contains everything but the update method and an initializer.
  * Chromopool can't have a constructor with arguments, as they have to be dynamically instantiated.
  * @author Maarten Slenter
  */
 public abstract class AbstractChromoPool<T extends Chromosome>
 {
     /**
      * The mutation rate of this chromosome pool
      */
     protected double mutationRate;
     
     /**
      * The cross over rate of this chromosome pool
      */
     protected double crossOverRate;
     
     /**
      * The settings
      */
    protected Settings settings;
     
     /**
      * The average fitness of this pool, set at each update
      */
     protected double avgFitness = 0;
     
     /**
      * The pool of chromosomes
      */
     protected ArrayList<Chromosome<T>> chromoPool = new ArrayList<Chromosome<T>>();
     
     /**
      * Initializes the chromo pool
      * @param crossOverRate The cross over rate of this chromo pool
      * @param mutationRate The mutation rate of this chromo pool
      * @param settings The settings
      */
     public void initialize(double crossOverRate, double mutationRate, Settings settings)
     {
         this.crossOverRate = crossOverRate;
         this.mutationRate = mutationRate;
         this.settings = settings;
     }
     
     /**
      * Adds all chromosomes from the supplied list to this pool
      * @param chromosomes The list of chromosomes to add
      */
     public void addChromosomes(Collection<Chromosome<T>> chromosomes)
     {
         chromoPool.addAll(chromosomes);
     }
     
     /**
      * Adds the supplied chromosome to this pool
      * @param chromosome The chromosome to add
      */
     public void addChromosome(Chromosome<T> chromosome)
     {
         chromoPool.add(chromosome);
     }
     
     /**
      * Updates the chromosome pool
      * @return A list with the new chromosomes
      */
     public abstract ArrayList<Chromosome<T>> update();
     
     /**
      * 
      * @return The average fitness of this chromosome pool
      */
     public double getAvgFitness()
     {
         return avgFitness;
     }
     
     /**
      * Resets the chromosome pool
      */
     public void reset()
     {
         chromoPool.clear();
         avgFitness = 0;
     }
     
     /**
      * Picks a chromosome from the supplied pool.
      * Chromosomes with a relatively higher fitness have more chance to be picked than those with a relatively lower fitness.
      * The picked chromosome will also directly be removed from the pool, to ensure the same chromosome can't be picked twice.
      * @param chromoPool The pool to pick the chromosome from
      * @return The picked chromosome
      */
     protected abstract Chromosome<T> pickChromosome(ArrayList<Chromosome<T>> chromoPool);
     
     /**
 	 * Dynamically instantiates a chromo pool
 	 * @param name The designation of the chromo pool
 	 * @return An instance of the requested chromo pool
 	 */
 	public static AbstractChromoPool createChromoPool(String name)
     {
         AbstractChromoPool chromoPool = null;
 
         try
         {
             chromoPool = (AbstractChromoPool) Class.forName("neuralNets.lib.learning.GA." + name + ".ChromoPool").newInstance();
         }
         catch (InstantiationException ex)
         {
             System.out.println(ex);
         }
         catch (IllegalAccessException ex)
         {
             System.out.println(ex);
         }
         catch (ClassNotFoundException ex)
         {
             System.out.println(ex);
         }
 
         return chromoPool;
     }
 }
