 package movement;
 
 import java.util.Random;
 import core.Coord;
 import core.Settings;
 
 public class GaussMarkovModel extends MovementModel {
 	private static final int PATH_LENGTH = 1;
 	private static final int EDGE_DISTANCE = 50;
 	
 	public static final String ALPHA = "alpha";
 	public static final String MEANSPEED = "meanSpeed";
 	public static final String SPEEDVARIANCE = "speedVariance";
 	public static final String PHASEVARIANCE = "phaseVariance";
 	public static final String SPEED_GAUSS_SEED = "speedGaussMarkovRNGSeed";
 	public static final String PHASE_GAUSS_SEED = "phaseGaussMarkovRNGSeed";
 	public static final String TIMEINTERVAL = "timeInterval";
 	
 	private static final String GAUSS_MARKOV_NS = "GaussMarkovModel";
 	
 	private Coord lastWaypoint;
 	
 	private double sN;
 	private double dN;
 	
 	private Random speedGaussianRNG;
 	private Random directionGaussianRNG;
 	private static double alpha = 0.5;
 	private static double meanSpeed = 2.0;
 	private static double speedVariance = 0.5;
 	private static int timeInterval = 10;
 	private static double phaseVariance = 1.0;
 	private double meanDirection = rng.nextDouble()*2*Math.PI;
 	
 	public GaussMarkovModel(Settings s) {
 		super(s);
 		Settings settings = new Settings(GAUSS_MARKOV_NS);
 		if(settings.contains(ALPHA))
 			GaussMarkovModel.alpha = settings.getDouble(ALPHA);
 		if(settings.contains(MEANSPEED))
 			GaussMarkovModel.meanSpeed = settings.getDouble(MEANSPEED);
 		if(settings.contains(SPEEDVARIANCE))
 			GaussMarkovModel.speedVariance = settings.getDouble(SPEEDVARIANCE);
 		if(settings.contains(PHASEVARIANCE))
 			GaussMarkovModel.phaseVariance = settings.getDouble(PHASEVARIANCE);
 		if(settings.contains(TIMEINTERVAL))
 			GaussMarkovModel.timeInterval = settings.getInt(TIMEINTERVAL);
 		
 		sN = Double.NaN;
 		dN = Double.NaN;
 		
 		if(settings.contains(SPEED_GAUSS_SEED))
 			this.speedGaussianRNG = new Random(settings.getInt(SPEED_GAUSS_SEED));
 		else
 			this.speedGaussianRNG = new Random(0);
 		
 		if(settings.contains(PHASE_GAUSS_SEED))
 			this.directionGaussianRNG = new Random(settings.getInt(PHASE_GAUSS_SEED));
 		else
 			this.directionGaussianRNG = new Random(0);
 	}
 	
 	private GaussMarkovModel(GaussMarkovModel gmm) {
 		super(gmm);
 		this.speedGaussianRNG = gmm.speedGaussianRNG;
 		this.directionGaussianRNG = gmm.directionGaussianRNG;
 		this.meanDirection = gmm.meanDirection;
 		this.sN = Double.NaN;
 		this.dN = Double.NaN;
 	}
 	
 	@Override
 	public Path getPath() {
 
 		/* 1. If speed not initialised set it to the mean value
 		   2. If direction not initialised, pick random direction (uniformly distributed between 0 and 2pi)
 		   3. in the loop (its length is based on PATH_LENGTH value) do: 
 		      - calculate s_n and d_n 
 			  - calculate coordinate of the new node's position by moving it from current location for fixed time interval
 				with calculated speed s_n in direction d_n
 			  - add a new waypoint to the path with (x_n, y_n) and speed s_n 
 		   4. store final value of s_n and d_n 
 		   5. return full path */
 
 
 		double currSpeed, currDirection;
 		double newX = 0, newY = 0, currX, currY;
 		int flag = 0;
 
 		Path p = new Path();
 		if(Double.isNaN(sN)) {
 			sN = meanSpeed;
 		} 
 		if(Double.isNaN(dN)) {
 			dN = rng.nextDouble()*2*Math.PI;
 		}		
 		currSpeed = sN;
 		currDirection = dN;
 		currX = lastWaypoint.getX();
 		currY = lastWaypoint.getY();

 
 		if(currX > 50 && currX < 1950 && currY < 50){
 			// Bottom Center - 180 degrees average shift
 			meanDirection = rng.nextDouble() * Math.PI;
 			flag = 1;
 		} else if(currX < 50 && currY < 50){
 			// bottom left - 45 degrees average shift
 			meanDirection = rng.nextDouble() * (Math.PI/2d); 
 			flag = 1;
 		} else if(currX < 50 && currY > 50 && currY < 1950){
 			// center left - 0 degree average shift
 			meanDirection = (rng.nextDouble() * Math.PI) + (3 * Math.PI / 2d);
 			flag = 1;
 		} else if(currX < 50 && currY > 1950){
 			// Top left - 315 degree average shift
 			meanDirection = (rng.nextDouble() * (Math.PI/2d)) + (3 * Math.PI / 2d);
 			flag = 1;
 		} else if(currX > 50 && currX < 1950 && currY > 1950){
			// top center - 370 degree average shift
 			meanDirection = (rng.nextDouble() * Math.PI) + Math.PI;
 			flag = 1;
 		} else if(currX > 1950 && currY > 1950){
 			// top right - 225 degree average shift
			meanDirection = (rng.nextDouble() * (Math.PI/2d)) + (Math.PI / 2d);
 			flag = 1;
 		} else if(currX > 1950 && currY > 50 && currY < 1950){
 			// center right - 180 degree average shift
 			meanDirection = (rng.nextDouble() * Math.PI) + (Math.PI / 2d);
 			flag = 1;
 		} else if(currX > 1950 && currY < 50){
 			// bottom right - 135 degree average shift
 			meanDirection = (rng.nextDouble() * (Math.PI/2d)) + (Math.PI / 2d);
 			flag = 1;
 		}


 		sN = generateSpeed(currSpeed);
 		if (flag == 1)
 			dN = meanDirection;
 		else
 			dN = generateDirection(currDirection);
		
 		newX = currX + sN*timeInterval*Math.cos(dN);
 		newY = currY + sN*timeInterval*Math.sin(dN);
 
 		Coord newCord = new Coord(newX,newY);
		p.addWaypoint(new Coord(currX, currY));
 		p.addWaypoint(newCord, sN);
 		this.lastWaypoint = newCord;

		
 		return p;
 	}
 
 	@Override
 	public Coord getInitialLocation() {
 		/* pick random location for the initial positioning (look and RWP code if you need help) */
 		assert rng != null : "MovementModel not initialized!";
 		Coord c = randomCoord();
 
 		this.lastWaypoint = c;
 		return c;
 	}
 	
 	protected double generateWaitTime() {
 		/* there are no pauses */
 		return 0;
 	}
 
 	@Override
 	public MovementModel replicate() {
 		return new GaussMarkovModel(this);
 	}
 	
 	protected double generateSpeed(double previousSpeed) {
 		/* draw sample from Gaussian distribution and calculate speed accoring to Gauss-Markov equation */
 		double oneMinusAlpha = (1d-alpha);
 		double sqrtOneMinusAlphaSquare = Math.sqrt((1d - alpha*alpha));
 		double generatedSpeed = (alpha*previousSpeed) + (oneMinusAlpha*meanSpeed) + Math.sqrt(speedVariance)*sqrtOneMinusAlphaSquare * getGaussianSample(speedGaussianRNG, speedVariance);
 		if(generatedSpeed < 0d){
 			return generatedSpeed * -1d;
 		} else {
 			return generatedSpeed;
 		}	
 	}
 	
 	protected double generateDirection(double previousDirection) {
 		/* draw sample from Gaussian distribution and calculate direction accoring to Gauss-Markov equation */
 		double oneMinusAlpha = 1d-alpha;
 		double sqrtOneMinusAlphaSquare = Math.sqrt(1d - alpha*alpha);
 		return((alpha*previousDirection) + (oneMinusAlpha*meanDirection) + Math.sqrt(phaseVariance)*sqrtOneMinusAlphaSquare * getGaussianSample(directionGaussianRNG, phaseVariance));
 		
 	}
 	
 	protected double getGaussianSample(Random rng, double variance) {
 		return rng.nextGaussian() * variance;
 	}
 	
 	protected Coord randomCoord() {
 		return new Coord(rng.nextDouble() * getMaxX(),
 				rng.nextDouble() * getMaxY());
 	}
 	
 }
