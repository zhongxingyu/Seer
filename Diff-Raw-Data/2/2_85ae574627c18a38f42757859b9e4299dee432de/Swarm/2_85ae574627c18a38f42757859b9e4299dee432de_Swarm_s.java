 import java.util.*;
 import java.lang.*;
 
 /****************
  Swarm is a java class for handling the behavior of a Particle Swarm.
  It uses methods supplied by Particle.java to update and manipulate individual
  particles in a swarm.
  ****************/
 public class Swarm
 {
 	public static final double MAX_VELOCITY = 2; // for any direction/dimension 
 	public static final double MIN_VELOCITY = -2;
 	
 	public static final double MAX_INIT_POSITION_SPHERE = 100.0; // for any direction/dimension
 	public static final double MIN_INIT_POSITION_SPHERE = 50.0;
 	public static final double SPHERE_RANGE = MAX_INIT_POSITION_SPHERE - MIN_INIT_POSITION_SPHERE;
 	public static final double MAX_INIT_POSITION_ROSENBROCK = 30.0;
 	public static final double MIN_INIT_POSITION_ROSENBROCK = 15.0;
 	public static final double ROSENBROCK_RANGE = MAX_INIT_POSITION_ROSENBROCK - MIN_INIT_POSITION_ROSENBROCK;
 	public static final double MAX_INIT_POSITION_GRIEWANK = 600.0;
 	public static final double MIN_INIT_POSITION_GRIEWANK = 300.0;
 	public static final double GRIEWANK_RANGE = MAX_INIT_POSITION_GRIEWANK - MIN_INIT_POSITION_GRIEWANK;
 	public static final double MAX_INIT_POSITION_ACKLEY = 32.0;
 	public static final double MIN_INIT_POSITION_ACKLEY = 16.0;
 	public static final double ACKLEY_RANGE = MAX_INIT_POSITION_ACKLEY - MIN_INIT_POSITION_ACKLEY;
 	public static final double MAX_INIT_POSITION_RASTRIGIN = 5.12;
 	public static final double MIN_INIT_POSITION_RASTRIGIN = 2.56;
 	public static final double RASTRIGIN_RANGE = MAX_INIT_POSITION_RASTRIGIN - MIN_INIT_POSITION_RASTRIGIN;
 	
 	public static final double DEFAULTS = 0; // inertia, phi, other constants ???#
 	public static final double PHI = 2.05;
 	public static final double RANDOMTOPOLOGY_PROBABILITY = 0.5;
 	
 	
 	// number of dimensions has no limit (other than hard drive space/allocation restrivtions for arrays)
 	private int dimensions;
 	private String topology;
 	private Boolean includeSelf;
 	private String influenceStructure;
 	private int size; // number of particles in swarm
 	private String function;
 	private double globalBest[];
 	private int bestParticle; // index of best particle
     private int iterations; // number of iterations, used to create the global best array (this.globalBest)
 	
 	
 	//holds all of the particles in our swarm
 	//Particle
 	private ArrayList<Particle> particles;
 	
 	private double phi1 = PHI, phi2 = PHI;
 	
 	//just for ease of use in calculating the constriction factor
   	private double phisum = phi1 + phi2;
   	
   	public  double constrictionFactor = 2.0 / (phisum - 2.0 + Math.sqrt(phisum*phisum - 4.0*phisum));
 	
 	private Random rand = new Random();
 	
 	// this is the constructor of a swarm, which is created by PSO.java
 	public Swarm(String topology, String includeSelf, String influenceStructure,
                  int swarmSize, String function, int dimensions, int iterations) {
         
 		
 		//initialize swarm variables
 		this.dimensions = dimensions;
 		this.topology = topology;
 		if (includeSelf.equalsIgnoreCase("yes")) this.includeSelf = true;
 		else this.includeSelf = false;
 		this.influenceStructure = influenceStructure;
 		this.size = swarmSize;
 		this.function = function;
         this.iterations = iterations;
 		
 		initSwarm();
 	}
 	
 	//initialize particles with velocity positions neighborhoods.
 	public void initSwarm(){
 		
 		// initialize the particle array
 		ArrayList<Particle> newParticles = new ArrayList<Particle>();
         
         // initialize the global best array
         this.globalBest = new double[this.iterations];
 		
 		for(int i = 0; i < this.size; i++){
 		    // particle constructor takes these arrays as parameters
 			int index = i;
 			List velocity = new ArrayList<Double>();
 			List position = new ArrayList<Double>();
 			
 			// set position and veocity for each dimension with random values within specified range
 			for (int j = 0; j < dimensions; j++){
 				if(this.function.equalsIgnoreCase("sphere")){
 					position.add(j, rand.nextDouble() * SPHERE_RANGE + MIN_INIT_POSITION_SPHERE);
 				}else if(this.function.equalsIgnoreCase("rosenbrock")){
 					position.add(j, rand.nextDouble() * ROSENBROCK_RANGE + MIN_INIT_POSITION_ROSENBROCK);
 				}else if(this.function.equalsIgnoreCase("griewank")){
 					position.add(j, rand.nextDouble() * GRIEWANK_RANGE + MIN_INIT_POSITION_GRIEWANK);
 				}else if(this.function.equalsIgnoreCase("ackley")){
 					position.add(j, rand.nextDouble() * ACKLEY_RANGE + MIN_INIT_POSITION_ACKLEY);
 				}else if(this.function.equalsIgnoreCase("rastrigin")){
 					position.add(j, rand.nextDouble() * RASTRIGIN_RANGE + MIN_INIT_POSITION_RASTRIGIN);
 				}else {
 					System.out.println("invalid function.");
 				}
 				velocity.add(j, rand.nextDouble() * MAX_VELOCITY);
 				velocity.set(j, (MIN_VELOCITY + (MAX_VELOCITY - MIN_VELOCITY) * rand.nextDouble()));
 			}
 			
 			Particle p = new Particle(index, velocity, position);
 
 			newParticles.add(i,p);
 		}
 		
 		// sets this swarm objects particle array to the new one we just created
 		this.particles = newParticles;
 		
 		//figure out who's in the neighborhood
 		for(Particle p: this.particles){
 			updateNeighborhood(p);
 		}
 		// initialize local bests
 		updateLocalBests();
 	}
 	
 	// this is where the "meat" of the PSO algorithm lies (we move each particle to a new location)
 	//called repeatedly by PSO.java
 	public void update(){
 		
 		// iterate through particles and update velocity and position based on personal and neighborhood bests
 		for(int i=0; i<this.particles.size(); i++){
 		    //grab the current velocities and save them as old
 		    Particle p = (Particle)this.particles.get(i);
             
 		    ArrayList<Double> oldVel = new ArrayList(p.getVel());
 			ArrayList<Double> oldPos = new ArrayList(p.getPos());
             
 //            System.out.println("position of particle " + i + " is: " + oldPos.get(0));
             
 			//new velocities will be formed using the acceleration, old velocity and position
 			List newVel = new ArrayList<Double>();
 			List newPos = new ArrayList<Double>();
 			ArrayList<Double> pBestPos = new ArrayList(p.getPBestPos());
 			
 			//implement fully informed particle swarm, accelerate toward all personal bests with same weight.
 			if (this.influenceStructure.equalsIgnoreCase("FIPS")){
 				double accTowardEachPBest[][] = new double[particles.size()][this.dimensions];
 				double accelerationVector[] = new double[this.dimensions]; // used to aggregate the accelerations toward all the bests
 				
 				//aggregate acceleration toward each neighbor for a given dimension.
                 // for(int nbr=0; nbr<particles.size(); nbr++){
 				for(int nbr = 0; nbr < p.getNeighborhood().size(); nbr++){
 					Particle neigh = p.getNeighborhood().get(nbr);
 					for (int dim = 0 ; dim < this.dimensions; dim++){
 						//determine the amount of accel towards both each neighbor's personal best
 						double randPhi2 = rand.nextDouble()*phi2;
                         
 						//we want vectors that go from the current position to the next position
 						//subtract current position vector from pbestposition vector (fixed)
 						ArrayList<Double> neighborhoodBestList = new ArrayList(neigh.getPBestPos());
 						double neighborhoodBest = neighborhoodBestList.get(dim);
 						accTowardEachPBest[nbr][dim] = randPhi2 * (neighborhoodBest - oldPos.get(dim));
                         
 						accelerationVector[dim] += accTowardEachPBest[nbr][dim];
 					}
 				}
 
 				for(int dim = 0; dim < this.dimensions; dim++){
 					newVel.add(dim, (((Double)oldVel.get(dim) + accelerationVector[dim]) * constrictionFactor));
 					newPos.add(dim, ((Double)oldPos.get(dim) + (Double)newVel.get(dim)));
 				}
                 
 			}
 			//normal swarm - influenced by personal and neighborhood best
 			else{
 				ArrayList<Double> nBestPos = new ArrayList(p.getNBestPos());
 				double accTowardPBest[] = new double[this.dimensions];
 				double accTowardNBest[] = new double[this.dimensions];
                 
 				// loop through dimensions, calculate and update velocity and position
 				for (int j = 0 ; j < this.dimensions; j++){
 					//determine the amount of accel towards both the personal and the global best
 					double randPhi1 = rand.nextDouble()*phi1;
 					double randPhi2 = rand.nextDouble()*phi2;
                     
 					//we want vectors that go from the current position to the next position
 					//subtract current position vector from pbestposition vector (fixed)
 					accTowardPBest[j] = randPhi1 * ((double)pBestPos.get(j) - (double)oldPos.get(j));
 					accTowardNBest[j] = randPhi2 * ((double)nBestPos.get(j) - (double)oldPos.get(j));
 					// this is the right math, but isn't working because list.get(j) is returning java.lang.object?
 					
 					
 					//actually calculate the new velocity and position from it
 					newVel.add(j, (((Double)oldVel.get(j) + accTowardPBest[j] + accTowardNBest[j]) * constrictionFactor));
 					newPos.add(j, ((Double)oldPos.get(j) + (Double)newVel.get(j)));
 				}
 			}
 			//send the vel/pos back to the Particle class
 			p.setVel(newVel);
 			p.setPos(newPos);
 		}
 		
 		//evaluate new particle fitness and figure out personal best
 		for(Particle p : this.particles){
 			List pos = p.getPos();
 //            for (int i)
 			double fitness = evaluate(pos);
             p.setVal(fitness);
 			if (fitness < p.getPBest()){
 				p.setPBest(fitness);
 				p.setPBestPos(pos);
 			}
 			
 			//if random topology, update neighborhoods here, good a place as any
 			if (this.topology.equalsIgnoreCase("random")){ //like this right?
 				updateNeighborhood(p);
 			}
 		}
 		
 		updateLocalBests();
 	}
 	
 	// go through particles, find and update neighborhood bests
 	public void updateLocalBests(){
 		for(Particle p : this.particles){
 			for(Particle neighbor : p.getNeighborhood()){
 				ArrayList<Particle> neigh = new ArrayList(p.getNeighborhood());
 				// search through neighbors for the best fitness
 				for(int j=0; j<neigh.size(); j++){
 				    Particle neighborParticle = neigh.get(j);
 					double neighBest = neighborParticle.getPBest();
 					if (neighBest < p.getNBest()){
 						p.setNBest(neighBest);
 						p.setNBestPos(neigh.get(j).getPBestPos());
 					}
 				}
 			}
 		}
 	}
 	
 	public double evaluate(List posList){
 	    ArrayList<Double> pos = new ArrayList(posList);
 		double fitness = 0;
 		
 		if(this.function.equalsIgnoreCase("sphere")){
 			for(int i=0 ; i<pos.size() ; i++){
 				fitness += pos.get(i) * pos.get(i);
 			}
 		}else if(this.function.equalsIgnoreCase("rosenbrock")){ //comment notation from bratton paper, x-sub-i is x[i]
 			for (int i=0 ; i <pos.size()-1 ; i++) {
 				double x = pos.get(i) * pos.get(i);	//x[i] ^ 2
 				double y = (pos.get(i) - 1); 		//x[i] - 1
 				y *= y; 							//(x[i] - 1)^2
 				double z = pos.get(i+1) - x; 		//x[i+1] - x[i]^2
 				z *= 100*z; 						//100(x[i+1] - x[i]^2)^2
 				fitness += z + y; // 				//100 ( x_{i+1} - x_i^2 )^2 + ( 1 - x_i )^2
 			}
 		}else if(this.function.equalsIgnoreCase("griewank")){
 		    double sum_a = 0, sum_b = 0;
 		    for (int i=0 ; i <pos.size() ; i++) {
 				sum_a+=Math.pow(pos.get(i),2);
 			}
 			for (int i=0 ; i <pos.size() ; i++) {
 				sum_b*=Math.cos(pos.get(i)/Math.sqrt(i));
 			}
 			fitness = sum_a/4000-sum_b+1;	
 		}else if(this.function.equalsIgnoreCase("ackley")){
			double sum1, sum2 = 0;
 			for (int i=0 ; i <pos.size() ; i++) {
 				sum1+= pos.get(i)*pos.get(i);
 			}
 			for (int i=0 ; i <pos.size() ; i++) {
 				sum2*=Math.cos(2.0 * Math.PI * pos.get(i));
 			}
 			fitness = -20.0 * Math.exp(-0.2 * Math.sqrt(sum1/2.0)) - Math.exp(sum2/2.0) + 20.0 + Math.E;
 			
 		}else if(this.function.equalsIgnoreCase("rastrigin")){
 			for (int i=0 ; i <pos.size() ; i++) {
 				double x_i = pos.get(i);
 				fitness += Math.pow(x_i, 2) - 10*Math.cos(2*Math.PI*x_i) + 10;
 			}
 		}else{
 			System.out.println("invalid function.");
 		}
 		return fitness;
 	}
 	
 	public void updateNeighborhood(Particle p){
 	    
 		if (this.topology.equalsIgnoreCase("gbest")){
             // neighborhood is entire swarm
             for (Particle q : this.particles)
             {
                 if(p != q)
                     p.addNeighbor(q);
             }
         }
 		else if (this.topology.equalsIgnoreCase("ring")){
 			// particles are imagined to be in a ring and the neighbors of each particle are the particles
 			// to the left and right of it
             
 			// if (object exists to the left of p in this.particles) -> add it
 			// else (object is first particle, so add the last particle in this.particles)
 			// if (object exists to the right of p) -> add it
 			// else (object is last particle, so add the first particle in this.particles)
 			
 			for (int i=0; i<this.particles.size(); i++){
 				int lastIndex = this.particles.size()-1;
 				if(i==0) p.addNeighbor(this.particles.get(lastIndex));
 				else p.addNeighbor(this.particles.get(i-1));
 				if(i==lastIndex) p.addNeighbor(this.particles.get(0));
 				else p.addNeighbor(this.particles.get(i+1));
 			}
 		}
 		else if (this.topology.equalsIgnoreCase("von_neumann")){
             // particles are imagined to be in a grid (that wraps around in both directions)
             // the neighbors of each particle are the particles above, below, and to the left and right of it
             
             int rowLength = (int)Math.sqrt(this.particles.size());
             int particles2d[][] = new int[rowLength][rowLength];
             int pRow = 0,pCol = 0; // p's row and col
             
             // fill 2d array with index of particle.
             for (int i=0; i<this.particles.size(); i++){
                 
                 int r,c; // row and column indeces of 2d array
                 if((i%rowLength) > 0){
                     r = (int) i/rowLength;
                     c = i%rowLength;
                 }else{
                     r = (int)i/rowLength;
                     c = 0;
                 }
                 particles2d[r][c] = i;
                 
                 if(i == p.getIndex()){
                     pRow = r;
                     pCol = c;
                 }
             }
             
             // go through the 2d array and check if row xor col matches p's, if so, add as neighbor
             try { //for debugging*
                 for(int r=0; r<rowLength; r++){
                     for(int c=0; c<rowLength; c++){
                         if(r == pRow ^ c == pCol){ // add neighbors in the same row or column, but not both
                             int index = particles2d[r][c];
                             p.addNeighbor(this.particles.get(index));
                         }
                     }
                 }
                 
             } catch (Exception e) {
                 //catch an error resulting from passing less velocity/position components than expected
                 System.out.println("pRow not set for some odd reason");
                 System.exit(0);
             }
 	    }
         
 		else if (this.topology.equalsIgnoreCase("random")){
     		// clear neighborhood and add iterate through particles, adding each with chance
 			// of RANDOMTOPOLOGY_PROBABILITY (default .5, but modifiable for testing purposes)
 			
 			p.clearNeighbors();
             
 			for (Particle q : this.particles)
 			{
 				if(rand.nextDouble() > RANDOMTOPOLOGY_PROBABILITY && p != q)
 			    	p.addNeighbor(q);
 			}
 		}
 		if(includeSelf)
 			p.addNeighbor(p);
 	}
 	
 	public double getBestVal(int iteration)
 	{
 	    this.globalBest[iteration] = 10000000;
         int i = 0;
 	    for (Particle p : this.particles)
 	    {
 	        double particleBest = p.getPBest();
             System.out.println("pbest of " + i + " = " + particleBest + " currVal of " + i + " = " + p.getVal());
 	        if (particleBest < this.globalBest[iteration])
 	            this.globalBest[iteration] = particleBest;
                                i++;
 	    }
 	    return this.globalBest[iteration];
 	}
 	
 }
