 import java.util.ArrayList;
 import java.util.Arrays;
 
 
 /**
  ** Localizer: perform localization calculations
  **
  **
  **
  **
  */
 public class Localizer extends Thread {
 	public static final int NUM_PARTICLES = 10000;
 	public static final double PARTICLE_TOLERANCE = .95;
 
 
 	private boolean localized;
 	private double dx,dy,dYaw, dist;
 	// Doubles for means of all particles
 	private double meanX,meanY,meanYaw;
 	private int[][] map;
 	private GridMap gmap;
 	private double[] ranges;
 	private ArrayList<Particle> particleList;
 	private Particle expectedLocation;
 
 	/**
 	 * Create a new Localizer object to be used in a map with the given
 	 * dimensions. Fills the list of particles with NUM_PARTICLES particles or
 	 * mapw*maph particles, whichever is smaller. All particles are initalized
 	 * with equal weight, 1 divided by the number of particles.
 	 *
 	 * @param   map the map!
 	 */
 	public Localizer(int[][] map, GridMap gmap) {
 		localized = false;
 		this.map = map;
 		this.gmap = gmap;
 		int mapw = map.length;
 		int maph = map[0].length;
 		int numParticles = (NUM_PARTICLES > mapw*maph) ? 
 				mapw*maph : NUM_PARTICLES;
 		int ppp = Math.round(mapw*maph/numParticles);   // pixels per particle
 		particleList = new ArrayList<Particle>(numParticles);
 		int x = 0, y = 0;
 		double yaw = 0.0;
 		double weight = 1.0/numParticles;
 
 		for (int i = 0; i < numParticles; i++) {
 		    yaw = Math.random()*2*Math.PI - Math.PI;
 			if (map[x][y] != 0) {   // Can't be in an obstacle, silly
 				particleList.add(i,new Particle(x, y, yaw, weight));
 			} else {
 				for (int j = 1; j < numParticles; j++) {
 					if (map[(x+j)%mapw][((x+i)/mapw + y)%maph] == 255) {
 						particleList.add(i, 
 								new Particle((x+j)%mapw,((x+i)/mapw + y)%maph,yaw, weight));
 						//System.out.println("W: " + weight);
 						break;
 					}
 				}
 			}
 			x = (x+ppp) % mapw;
 			if (x < ppp) y ++;
 		}
 		expectedLocation = null;
 		
 		meanX = getMean(0);
 		meanY = getMean(1);
 		meanYaw = getMean(2);
 
 		drawMap();
 
 		System.out.printf("NUM_PARTICLES = %d\nParticles in ArrayList = %d\n",
 				NUM_PARTICLES,particleList.size());
 
 	}
 	private void predict() {
         System.out.println("Predictin: dx = " + dx + " | dy = " + dy + " | dyaw = " + dYaw); 
 		// Do we have enough particles?
         // Changing this for the moment to not be ESS, but list size
         double xDrift, yDrift, dir;
 		if (particleList.size() < PARTICLE_TOLERANCE * NUM_PARTICLES) {
 			int[] indexCopyList = resample();
 			for (int i = 0; i < indexCopyList.length; i++) {
 				Particle temp = (Particle) particleList.get(i).clone();
 				dir = Math.random() - .5;
 				xDrift = dir >= 0 ? Math.random()*5.0 : Math.random()*-5.0;
 				dir = Math.random() - .5;
 				yDrift = dir >= 0 ? Math.random()*5.0 : Math.random()*-5.0;
                 
                 temp.setX(temp.getX() + xDrift);
                 temp.setY(temp.getY() + yDrift);
                 
                 if (temp.getX() < 0 || temp.getX() >= map.length ||
                     temp.getY() < 0 || temp.getY() >= map[0].length) {
                     
                     i--;
                     continue;
                 }
 				double newYaw = Math.random()*2*Math.PI - Math.PI;
 				temp.setPose(newYaw);
 				temp.setWeight(1.0/NUM_PARTICLES);
 				particleList.add(temp);
 			}
 		}
 		//   for (j = 1 to M) do {Prediction after action A}
 		int m = particleList.size();
 		double dist = this.dist / Localization.MAP_METERS_PER_PIXEL;
 		for (int j = 0; j < m; j++) {
 			// Drifts, not sure exactly how we should do this, paper suggests using numbers selected randomly
 			//  from a gaussian.
 
 			Particle temp = particleList.get(j);
 			double pose = temp.getPose();
 
 			gmap.clearParticle(temp.getX(),temp.getY());
 
 			double tx = temp.getX() + dx/Localization.MAP_METERS_PER_PIXEL;//dist*Math.cos(pose);
 			double ty = temp.getY() - dy/Localization.MAP_METERS_PER_PIXEL;//dist*Math.sin(pose);
 			double tp = pose + dYaw;
 			// If we are greater than pi, wrap around to negatives.
 			if (tp > Math.PI)
 				tp = tp - 2*Math.PI;
 			if (tp < -1*Math.PI)
 			    tp = tp + 2*Math.PI;
 			temp.move(tx, ty, tp);
 			
 			if (tx < 0 || tx >= map.length || ty < 0 || ty >= map[0].length) {
 			    temp.setWeight(0.0);
 			} else if (map[(int)tx][(int)ty] == 0) {
 			    temp.setWeight(0.0);
 			}
 
 			//     X^k+1_j = F(X^k_j,A)
 			// particleList.set(j, temp);
 			//   end for
 		}
 	}
 	
 	/*
 	 * This will update and normalize the weights
 	 */
 	public void update() {
 	    System.out.println("Updating particle weights...");
 		//   for(j = 1 to M) do {Update the weights}
 		//      W^k+1_j  = W^K_j * W(s,X^k+1_j)
 		//   end for 
 		//   for(j=1 to M) do {Normalize the weights}
 		//     W^k+1_j = (W^k+1_j)/(Sumi=1 to M (W^k+1_i))
 		//   end for
 		double thesum = 0;
 		for (Particle p : particleList) {
 			double weight = p.getWeight() * prob(p,ranges);
 			//System.out.println("New weight = " + weight);
 			// accrue sum for normalizing
 			thesum += weight;
 			p.setWeight(weight);
 		}
 		// Normalize the weights
 		for (Particle p : particleList) {
 			double weight = p.getWeight() / thesum;
 		//	System.out.println("Normalized weight = " + weight);
 			p.setWeight(weight);
 		}
 	}
 
 	/**
 	 * Returns the effective sample size used for weeding out bad particles.
 	 */
 	public double effectiveSampleSize() {
 		// Effective Sample Size = (M)/(1 + c*(v_t)^2)
 		double ess = particleList.size() / (1 + coeffVariance());
 		//System.out.println("CV = " + coeffVariance() + " | ESS = " + ess);
 		return ess;
 	}
 
 	/**
 	 * This calculates the coefficient of Variance (cv^2_t) 
 	 *  as according to equation 4 in particle tutorial
 	 */
 	public double coeffVariance() {
 		double c = 0;
 		for (Particle p : particleList) {
 			//System.out.print("weight = " + p.getWeight() + " | ");
 			double x = particleList.size() * p.getWeight();
 			x = x - 1;
 			x = x * x;
 			c += x; 
 		}
 		c = c * (1.0/particleList.size());
 		return c;
 	}
 
 	/**
 	 * Called when the number of particles, according to the effective sample
 	 * size, drops below the threshold PARTICLE_TOLERANCE percentage.
 	 *
 	 * @return  integer array containing @TODO what does this return?
 	 */
 	public int[] resample() {
 	    System.out.println("Resampling...");
 	    int size = NUM_PARTICLES - particleList.size();
 	    size = size * 2;
 		int[] index = new int[size];
 		// require sumofi=1 to N (Wi) = 1
 		/*	AK - Removing this for now, I'm not positive why we have it in the first place
 		 *    (other than the algorithm said so.) And it is consistently botching stuff up.
 		 * double weightSum = sumOf(particleList);
 		 *if (1-weightSum < (1/NUM_PARTICLES)) {
 		 *	System.out.println("!! sum = " + weightSum);
 		 *	return null;
 		 *	}
 		 */
 
 		// Q = sumsum(W); 
 // 		double[] q = cumsum(particleList);
 // 		// t = rand(N+1);
 // 		double[] t = randArray(particleList.size()+1); // t is an array of N+1 random numbers
 // 		// T = sort(t);
 // 		Arrays.sort(t);
 		//T(N+1) = 1; i = 1; j = 1
 		int i = 0;
 		// while( i <= N) do
 // 		while( i < particleList.size() && j < particleList.size()) {
 // 			//  if T[i] < Q[j] then
 // 			if (t[i] < q[j]) { 
 // 				// Index[i] = j;
 // 				index[i] = j;
 // 				// i++
 // 				i++;
 // //				j = 0;
 // 			} else {
 // 				//  else
 // 				// j++;
 // 				j++;
 // 			}
 // 			//  end if
 // 			// end while
 // 		}
 
         // JD - Instead of doing what the book says, let's just take the
         // particles with the best weights and clone them.
         // This doesn't fix our problem. WTF
         Particle[] sorted = new Particle[particleList.size()];
         Arrays.sort(particleList.toArray(sorted));
         int j = Math.min(index.length,sorted.length);
         j -= 1;
         while (j  >= 0) {
             index[j] = particleList.indexOf(sorted[j]);
             j--;
         }
 		
 		// Return(Index)
 		return index;
 	}
 
 	/**
 	 * Calculates the probability of a particular particle represents the 
 	 * actual position and pose of the robot based on sensor dataz
 	 *
 	 * @param   p       Particle in question
 	 * @param   ranges  sensor dataz
 	 * @return  probability (weight) of the given particle
 	 */
 	private double prob(Particle p, double[] ranges) {
 		// dataz intergrayshunz
 		double prob = 1.1;
 	    for (int i = 0; i < ranges.length; i += ranges.length/10) {
 	        double angle = Math.PI/512 * i - (2*Math.PI/3);
 	        angle += p.getPose();
 	        
 	        // Trace line from roboparticle lasers: detect wall. If line we trace
 	        // is more than 4.5m long, what should we do dawg?
 	       boolean foundWall = false;
 	       double distance = 0;
 	       double j= 0;
 	       while (distance < 4.5) {
 	    	    j++;
                 double pointX = (int) Math.floor(j * Math.cos(angle) + p.getX());
                 double pointY = (int) Math.floor(-1*j * Math.sin(angle) + p.getY());
                 // get length of this laser
                 distance = Math.sqrt((j*Math.cos(angle)*j*Math.cos(angle)) + (j*Math.sin(angle)*j*Math.sin(angle))); 
                 distance = distance * Localization.MAP_METERS_PER_PIXEL;
                 if ((pointX < 0 || pointX >= map.length) || (pointY < 0 || pointY >= map[0].length)){
                 	distance = 5.0;
                 	continue;
                 }
                 if (map[(int) pointX][(int) pointY] == 0) {
                 	// We found obstacle!
                 	//Nao, compare to real readings
                 	// Find relative error to real reading (We can then use this to update probability)
                 	double error = .01*Math.abs(ranges[i] - distance)/ranges[i];
                 	if (ranges[i] >= 4.5)
                 	    error = .1;
                 	prob = prob * (1-error);
                 	foundWall = true;
                 }
                 
 	        }
 	       if (!foundWall) {
 	    	   // If the real laser didn't find a wall either, we safe.
 	    	   if (ranges[i] < 4.5) {
 		       double error = ranges[i] / 5;
 	    		  prob = prob * (error);
 	    	   }
 	       }
 	    }
 	    
 		return prob;
 	}
 
 	/**
 	 * Returns an array of random numbers 0.0 <= x < 1.0 of given size
 	 */
 	public double[] randArray(int size) {
 		double[] array = new double[size];
 		for (int i = 0; i < size;i ++) {
 			array[i] = Math.random();
 		}
 		return array;
 	}
 
 	/*
 	 * Calculate running totals :
 	 * array[j] = sumof[l=0,j](list[l])
 	 */
 	public double[] cumsum(ArrayList<Particle> list) {
 		double sum = 0;
 		double[] array = new double[list.size()];
 		for (int i = 0; i < list.size(); i++) {
 			sum += list.get(i).getWeight();
 			array[i] = sum;
 		}
 		return array;
 	}
 
 	/**
 	 * Sums up the weights of the all the existing particles and returns it
 	 * as a double
 	 */
 	public double sumOf(ArrayList<Particle> list) {
 		double sum = 0.0;
 		for (Particle p : list) {
 			sum += p.getWeight();
 		}
 		return sum;
 	}
 
 	/**
 	 * Check if the robot has localized via the localized flag
 	 */
 	public synchronized boolean isLocalized() {
 		return localized;
 	}
 
 	/**
 	 * Get an array containing all the weights of the existing particles
 	 */
 	public double[] getWeights() {
 		double[] weights = new double[particleList.size()];
 		for(int i = 0; i < weights.length; i++) {
 			weights[i] = particleList.get(i).getWeight();
 		}
 		return weights;
 	}
 	// we might not need this, not sure yet BROH.
 	private double getStandardDev(int v) {
		return Math.sqrt(getVariance(v)/particleList.size());
 	}
 
 	/* 
 	 * Calculate variance for x/y/yaw
 	 * @param v - 0 for x, 1 for y, 2 for yaw
 	 */
 	private double getVariance(int v) {
 		double variance = 0;
 		for (Particle p : particleList) {
 			if (v==0) 
 				variance += (meanX - p.getX())*(meanX - p.getX());
 			if (v==1)
 				variance += (meanY - p.getY())*(meanY - p.getY());
 			if (v==2) 
 				variance += (meanYaw - p.getPose())*(meanYaw - p.getPose());
 		}
 		return variance;
 	}
 
 	/*
 	 * Calculate mean values for x/y/yaw
 	 * @param int v - 0 for x, 1 for y, 2 for yaw 
 	 */
 	private double getMean(int v) {
 		double mean = 0.0;
 		for (Particle p : particleList) {
 			if (v == 0)
 				mean += p.getX();
 			if (v == 1) 
 				mean += p.getY();
 			if (v == 2)
 				mean += p.getPose();
 		}
 		mean = mean / particleList.size();
 		return mean;
 	}
 
 	/**
 	 * Called by the wanderer to update the localizer with the robot's change
 	 * in position and pose, as well as the newest sensor dataz
 	 */
 	public void receiveUpdate(double dx, double dy, double dYaw,
 			double[] ranges) {
 		//System.out.println("Update Received");
 
 			this.dx = dx;
 			this.dy = dy;
 			this.dist = Math.sqrt(dx*dx + dy*dy);
 			this.dYaw = dYaw;
 			this.ranges = ranges;
 	}
 	/**
 	 * Draws the map all pretty-like for us to look at
 	 */
 	public void drawMap() {
 	    System.out.println("Redrawing map...");
 		for (Particle p : particleList) {
 		    if (map[(int)p.getX()][(int)p.getY()] == 0)
 		        System.out.printf("%80s", "!!! Drawing in an obstacle !!!\n");
 			gmap.setParticle(p.getX(), p.getY());
 		}
 		gmap.repaint();
 	}
 	/**
 	 * Kill off bad points
 	 * 
 	 * 
 	 */
 	private void killBaddies() {
 		int j = 0;
 		boolean oob = false;
 		boolean obs = false;
 		while( j < particleList.size()) {
 			Particle p = particleList.get(j);
 			if (p.getWeight() < .01/(NUM_PARTICLES * NUM_PARTICLES) || p.getWeight() == 0) {
 			    oob = (p.getX() >= map.length || p.getY() >= map[0].length);
 			    oob = oob || p.getX() < 0 || p.getY() < 0;
 			    obs = oob ? true : map[(int)p.getX()][(int)p.getY()] == 0;
 				if (!obs)
 				    gmap.clearParticle(p.getX(),p.getY());
 				
 				particleList.remove(j);
 				// HOPE WE DON'T GO INFINITE :3
 				j--;
 			}
 			j++;
 		}
 		particleList.trimToSize();
 	}
 	/**
 	 * Checks all the particles to see if any of them are inside an obstacle.
 	 * This is impossible, so any that it finds get a weight of zero. This
 	 * ensures that they get wiped out and replaced on the next update.
 	 */
 	private void collisionCheck() {
 		for (Particle p : particleList) {
 			int x = (int) p.getX();
 			int y = (int) p.getY();
 			// AK BOUNDARY CHECKING BROSKI
 			if ((x < 0 || x >= map.length) || (y < 0 || y >= map[x].length)) {
 				p.setWeight(0.0);
 			} else {
 				if (map[x][y] == 0)
 					p.setWeight(0.0);
 			}
 		}
 	}
 
 
 	/**
 	 * What this thread does when it runs, yo
 	 */
 	@Override
 	public void run() {
 		//System.out.println("We've been started!");
 		int loops = 0;
 		while (!localized) {
 //			System.out.println("We're running in the loop!");
 			// Yo dawg, shouldn't we like, be waiting on updates?
 			if (!Wanderer.updateReady()) {
 			    loops++;
 			    continue;
 			}
 			
 			//System.out.printf("Uselessly looped %d times\n", loops);
 			loops = 0;
 
             Wanderer.sendUpdate(this);
             System.out.println("\nUpdate gotten, PROCESSING");
             predict();
             update();
 //            collisionCheck();
             killBaddies();
 //		    clearUpdates();
             drawMap();
             meanX = getMean(0);
             meanY = getMean(1);
             meanYaw = getMean(2);
             System.out.println("Particles: " + particleList.size());
             System.out.println("X variance = " + getStandardDev(0));
             System.out.println("Y variance = " + getStandardDev(1));
             System.out.println("Yaw var    = " + getStandardDev(2));
             if ((getVariance(0) / NUM_PARTICLES) < 200  && (getVariance(1) / NUM_PARTICLES) < 200) {
             	//localized = true;
             	expectedLocation = new Particle(meanX,meanY,meanYaw,1);
             }
 		}
 	}
 
 }
