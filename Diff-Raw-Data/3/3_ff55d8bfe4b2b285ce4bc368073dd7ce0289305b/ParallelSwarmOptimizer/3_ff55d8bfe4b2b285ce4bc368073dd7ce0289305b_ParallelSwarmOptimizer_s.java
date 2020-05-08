 /**
  * Copied from SwarmOptimizer.java on 2012-10-22
  * 
  * @author behrens
  *
  */
 package venn.optim;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ThreadPoolExecutor;
 
 import junit.framework.Assert;
 import venn.parallel.ExecutorServiceFactory;
 import venn.utility.ArrayUtility;
 import venn.utility.MathUtility;
 
 public class ParallelSwarmOptimizer extends AbstractOptimizer {
 	private final Random random;
 	private ExecutorService threadPool;
 	private Parameters params;
 	private IFunction func;
 	private Particle globalBest;
 	private Particle[] particles;
 	private int numIterations, numConstIterations;
 	private boolean valid;
 
 	ParallelSwarmOptimizer(Random random) {
 		threadPool=ExecutorServiceFactory.getExecutorService();
 		this.random = random;
 		params = new Parameters();
 		valid = false;
 	}
 
 	public ParallelSwarmOptimizer(Random random, IFunction errf,
 			Parameters params) {
 		threadPool=ExecutorServiceFactory.getExecutorService();
 		this.random = random;
 		this.params = params;
 		valid = false;
 		setFunction(errf);
 	}
 
 	public void setParameters(Parameters params) {
 		this.params = params;
 		invalidate();
 	}
 
 	public Parameters getParameters() {
 		return params;
 	}
 
 	public Particle getGlobalBest() {
 		validate();
 		return globalBest;
 	}
 
 	public void setFunction(IFunction function) {
 		func = function;
 		invalidate();
 	}
 
 	public IFunction getFunction() {
 		return func;
 		
 	}
 
 	public void validate() {
 		if (!valid) {
 			if (func == null)
 				throw new IllegalStateException(
 						"function must be set before calling validate()");
 
 			particles = new Particle[params.numParticles];
 			int iBest = 0;
 			for (int i = 0; i < particles.length; ++i) {
				particles[i] = new Particle(random, this);
 				if (particles[i].getFitness() > particles[iBest].getFitness())
 					iBest = i;
 			}
 			globalBest = new Particle(particles[iBest]);
 			valid = true;
 
 			reset();
 		}
 	}
 
 	public void invalidate() {
 		valid = false;
 	}
 
 	protected synchronized void performOptimization() {
 		validate();
 
 		Assert.assertFalse(endCondition());
 
 		++numIterations;
 
 		boolean improved = false;
 
 		// move the whole swarm
 
 		ArrayList<ParticleThread> runnables = new ArrayList<ParticleThread>();
 		ArrayList<Future> futures= new ArrayList<Future>();
 		for (Particle particle : particles) {
 
 			ParticleThread pThread = new ParticleThread(particle);
 			runnables.add(pThread);
 			futures.add(threadPool.submit(pThread));
 
 		}
 		
 		// wait for all threads to finish.
 		for (Future thread : futures) {
 				try {
 					thread.get();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ExecutionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		}
 
 		// assert results and get global best particle
 		for (Particle particle : particles) {
 			if (particle.getFitness() > globalBest.getFitness()) {
 				globalBest = new Particle(particle);
 				improved = true;
 			}
 		}
 
 		if (improved)
 			numConstIterations = 0;
 		else
 			++numConstIterations;
 	}
 
 	public int getMaxProgress() {
 		return params.maxIterations;
 	}
 
 	public int getProgress() {
 		return numIterations;
 	}
 
 	public boolean endCondition() {
 		return (numIterations >= params.maxIterations)
 				|| (numConstIterations >= params.maxConstIterations);
 	}
 
 	public double[] getOptimum() {
 		Particle p = getGlobalBest();
 		if (p == null)
 			return null;
 		return p.getValue();
 	}
 
 	public double getValue() {
 		Particle p = getGlobalBest();
 		if (p == null)
 			return 0.0;
 		return p.getFitness();
 	}
 
 	public void reset() {
 		numIterations = 0;
 		numConstIterations = 0;
 	}
 
 	public void writeState(Writer writer) throws IOException {
 		for (int i = 0; i < particles.length; ++i) {
 			writer.write(i + "\t" + (-particles[i].getFitness()) + "\t");
 			ArrayUtility.doubleVectorToStream(writer, particles[i].getValue(),
 					"\t");
 			// writer.write("\t");
 			// ArrayUtility.doubleVectorToStream(writer,particles[i].getVelocity(),"\t");
 			writer.write("\n");
 		}
 	}
 
 	/**
 	 * A single particle representing a solution in the N-dimensional space.
 	 * 
 	 * @author muellera
 	 * 
 	 */
 	public static class Particle {
 		/*
 		 * Sebastian Behrens 2012-10-24
 		 * Added an IFunction instance for each particle,
 		 * needed for parallelisation.
 		 */
 		private IFunction localFunc;
 		
 		private final Random random;
 
 		private ParallelSwarmOptimizer swarm;
 		private double[] value, // current position in parameter space
 				velocity;
 		private boolean valid; // error value valid
 		private Particle localBest;
 
 		private transient double cacheOutput;
 		private transient boolean outOfBox;
 
 		Particle(Random random, ParallelSwarmOptimizer swarm) {
 //			this.localFunc = new VennErrorFunction()
 			this.random = random;
 			this.swarm = swarm;
 			localFunc = swarm.func.copy();
 			reset();
 		}
 
 		Particle(Particle other) {
 			random = other.random;
 			swarm = other.swarm;
 			localFunc = other.localFunc.copy();
 			value = (double[]) (other.value.clone());
 			velocity = (double[]) (other.velocity.clone());
 			cacheOutput = other.cacheOutput;
 			valid = other.valid;
 			localBest = null;
 			outOfBox = false;
 		}
 
 		public void reset() {
 			int N = localFunc.getNumInput();
 			double[] L = localFunc.getLowerBounds(), U = localFunc
 					.getUpperBounds();
 
 			value = new double[N];
 			velocity = new double[N];
 
 			// choose random start value
 			for (int i = 0; i < N; ++i) {
 				value[i] = L[i] + random.nextDouble() * (U[i] - L[i]);
 			}
 
 			// choose velocities
 			for (int i = 0; i < N; ++i) {
 				velocity[i] = swarm.params.maxV
 						* (1.0 - 2.0 * random.nextDouble());
 			}
 
 			invalidate();
 
 			localBest = new Particle(this);
 			outOfBox = false;
 		}
 
 		public void invalidate() {
 			cacheOutput = 0.0;
 			valid = false;
 		}
 
 		public double[] getValue() {
 			return value;
 		}
 
 		public double[] getVelocity() {
 			return velocity;
 		}
 
 		/**
 		 * 
 		 * @return the current fitness of this particle
 		 */
 		public double getFitness() {
 			Assert.assertFalse(outOfBox);
 			if (!valid) {
 				localFunc.setInput(value);
 				cacheOutput = localFunc.getOutput();
 				valid = true;
 			}
 			return cacheOutput;
 		}
 
 		/**
 		 * 
 		 * @return the best instance of this particle
 		 */
 		public Particle getLocalBest() {
 			return localBest;
 		}
 
 		private void setBest(Particle particle) {
 			localBest = new Particle(particle);
 		}
 
 		/**
 		 * Moves this particle and updates the cost value.
 		 * 
 		 */
 		public void move() {
 			double[] L = localFunc.getLowerBounds(), U = localFunc
 					.getUpperBounds();
 
 			for (int i = 0; i < velocity.length; ++i) {
 				// update velocity
 				double d = U[i] - L[i];
 				if (d <= 0.0) {
 					d = 1.0;
 					velocity[i] = 0.0;
 				} else {
 					velocity[i] += (swarm.params.cGlobal * random.nextDouble()
 							* (swarm.getGlobalBest().value[i] - value[i]) + swarm.params.cLocal
 							* random.nextDouble()
 							* (getLocalBest().value[i] - value[i]))
 							/ d;
 					// restrict velocities ?
 					velocity[i] = MathUtility.restrict(velocity[i],
 							-swarm.params.maxV, swarm.params.maxV);
 				}
 
 				// move particle
 				value[i] += velocity[i] * (U[i] - L[i]);
 				outOfBox = false;
 
 				if (value[i] < L[i]) {
 					if (swarm.params.reflect) {
 						value[i] = L[i];
 						velocity[i] = Math.abs(velocity[i]);
 					} else {
 						outOfBox = true;
 					}
 				} else {
 					if (value[i] > U[i]) {
 						if (swarm.params.reflect) {
 							value[i] = U[i];
 							velocity[i] = -Math.abs(velocity[i]);
 						} else {
 							outOfBox = true;
 						}
 					}
 				}
 			}
 
 			invalidate();
 			// update the local best
 			if (!outOfBox) {
 				if (getFitness() > getLocalBest().getFitness()) {
 					setBest(this);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Parameter structure for this optimization algorithm.
 	 * 
 	 */
 	public static class Parameters implements Serializable {
 		private static final long serialVersionUID = 1L;
 
 		public static final int ID = 3;
 
 		public int numParticles; // size of the swarm
 		public double cGlobal, // global acceleration constants
 				cLocal, // local acceleration constants
 				maxV; // maximum velocity
 		public int maxIterations, // maximum number of iterations
 				maxConstIterations; // maximum number of iterations where the
 									// optimum does not improve
 		public boolean reflect; // reflect at the bounding box
 
 		public Parameters() {
 			numParticles = 30;
 			cGlobal = 1.0;
 			cLocal = 0.5;
 			maxV = 0.05;
 			maxIterations = 200;
 			maxConstIterations = 25;
 			reflect = true;
 		}
 
 		public boolean check() {
 			int oldint;
 			double olddouble;
 			boolean changed = false;
 
 			oldint = numParticles;
 			if (oldint != (numParticles = MathUtility.restrict(numParticles, 1,
 					1000)))
 				changed = true;
 
 			olddouble = cGlobal;
 			if (olddouble != (cGlobal = MathUtility.restrict(cGlobal, 0.0, 2.0)))
 				changed = true;
 
 			olddouble = cLocal;
 			if (olddouble != (cLocal = MathUtility.restrict(cLocal, 0.0, 2.0)))
 				changed = true;
 
 			olddouble = maxV;
 			if (olddouble != (maxV = MathUtility.restrict(maxV, 1E-6, 1.0)))
 				changed = true;
 
 			oldint = maxIterations;
 			if (oldint != (maxIterations = MathUtility.restrict(maxIterations,
 					1, 10000)))
 				changed = true;
 
 			oldint = maxConstIterations;
 			if (oldint != (maxConstIterations = MathUtility.restrict(
 					maxConstIterations, 2, maxIterations)))
 				changed = true;
 
 			return !changed;
 		}
 	}
 
 	private class ParticleThread extends Thread {
 
 		private Particle particle;
 
 		public ParticleThread(Particle particle) {
 			this.particle = particle;
 		}
 
 		@Override
 		public void run() {
 			particle.move();
 		}
 
 	}
 }
