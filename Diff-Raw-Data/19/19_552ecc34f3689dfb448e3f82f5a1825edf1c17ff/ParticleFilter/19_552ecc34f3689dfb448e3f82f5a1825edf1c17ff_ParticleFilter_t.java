 package raisa.domain;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import raisa.config.VisualizerConfig;
 import raisa.config.VisualizerConfigItemEnum;
 import raisa.config.VisualizerConfigListener;
 import raisa.util.CollectionUtil;
 import raisa.util.Vector2D;
 
 public class ParticleFilter implements SampleListener, VisualizerConfigListener {
 
 	private static final Logger log = LoggerFactory.getLogger(ParticleFilter.class);
 			
 	private WorldModel world;
 	private List<Particle> particles;
 	private List<Sample> samples = new ArrayList<Sample>();
 	private List<ParticleFilterListener> particleFilterListeners = new ArrayList<ParticleFilterListener>();
 	private boolean active = false;
 
 	public ParticleFilter(WorldModel world, int nparticles) {
 		this.world = world;
 		randomizeParticles(nparticles);
 		VisualizerConfig.getInstance().addVisualizerConfigListener(this);
 	}
 	
 	public synchronized void reset() {
 		randomizeParticles(particles.size());
 		samples = new ArrayList<Sample>();
 	}
 
 	public void randomizeParticles(int nparticles) {
 		particles = new ArrayList<Particle>();
 		for (int i = 0; i < nparticles; ++i) {
 			Particle particle = makeRandomParticle();
 			particles.add(particle);
 		}
 	}
 
 	public Particle makeRandomParticle() {
 		float width = world.getWidth();
 		float height = world.getHeight();
 		boolean ok = false;
 		Particle particle = new Particle();
 		while (!ok) {
 			float x = (float) Math.random() * width - 0.5f * width;
 			float y = (float) Math.random() * height - 0.5f * height;
 			Vector2D position = new Vector2D(x, y);
 			if (world.isClear(position)) {
 				float heading = (float) Math.random() * (float) Math.PI * 2.0f;
 				Robot robot = new Robot(position, heading);
 				particle.addState(robot);
 				ok = true;
 			}
 		}
 		return particle;
 	}
 
 	private synchronized void updateParticles(List<Sample> samples) {
		if (samples.isEmpty()) {
 			return;
		}
 
 		// estimate movement
 		RobotMovementEstimator estimator = new SimpleRobotMovementEstimator(true);
 		Sample lastSample = samples.get(samples.size() - 1);
 		for (Particle particle : particles) {
 			Robot newState = estimator.moveRobot(particle.getLastState(), lastSample);
 			particle.addState(newState);
 		}
 
 		// calculate weights
 		float totalWeights = 0.0f;
 		Map<Particle, Float> weights = new LinkedHashMap<Particle, Float>();
 		for (Particle particle : particles) {
 			float weight = particle.calculateWeight(world, samples);
 			weights.put(particle, weight);
 			totalWeights += weight;
 		}
 		if (totalWeights > 0.0f) {
 			// normalize weights
 			float maxWeight = 0.0f;
 			for (Particle particle : particles) {
 				float normalizedWeight = weights.get(particle) / totalWeights;
 				weights.put(particle, normalizedWeight);
 				if (maxWeight < normalizedWeight) {
 					maxWeight = normalizedWeight;
 				}
 			}
 			// sample new particles with replacement
 			List<Particle> newParticles = new ArrayList<Particle>();
 			
 			//executeMakeResampling(weights, newParticles);
 			executeThrunResampling(weights, maxWeight, newParticles);		
 
 			// add a few random particles to avoid local maxima
 			for (int i = 0; i < particles.size() / 10; ++i) {
 				newParticles.set(i, makeRandomParticle());
 			}
 
 			this.particles = newParticles;
 			notifyParticleFilterListeners();
 		}
 	}
 
 	private void executeMakeResampling(Map<Particle, Float> weights,
 			List<Particle> newParticles) {
 		for (int i = 0; i < particles.size(); ++i) {
 			float r = (float) Math.random();
 			float s = 0.0f;
 			Particle selectedParticle = null;
 			for (Particle particle : weights.keySet()) {
 				float weight = weights.get(particle);
 				s += weight;
 				if (r <= s) {
 					selectedParticle = particle;
 					break;
 				}
 			}
 			if (selectedParticle != null) {
 				newParticles.add(selectedParticle.copy());
 			}
 		}
 	}
 
 	private void executeThrunResampling(Map<Particle, Float> weights,
 			float maxWeight, List<Particle> newParticles) {
		if(particles.isEmpty()) {
			// this may happen when particle filter has just started
			return;
		}
 		int index = (int)Math.random() * particles.size();
 		float beta = 0.0f;
 		for (int i = 0; i < particles.size(); i++) {
 			beta += Math.random() * 2.0 * maxWeight;
 			float weight = weights.get(particles.get(index));
 			while (beta > weight) {
 				beta -= weight;
 				index = (index + 1) % particles.size();
 				weight = weights.get(particles.get(index));
 			}
 			newParticles.add(particles.get(index).copy());
 		}
 	}
 
 	private void notifyParticleFilterListeners() {
 		for (ParticleFilterListener listener : particleFilterListeners) {
 			listener.particlesChanged(this);
 		}
 	}
 
 	public List<Particle> getParticles() {
 		return particles;
 	}
 
 	@Override
 	public void sampleAdded(Sample sample) {
 		samples.add(sample);
 		samples = CollectionUtil.takeLast(samples, 50);
 		updateParticles(samples);
 	}
 
 	public void addParticleFilterListener(ParticleFilterListener listener) {
 		particleFilterListeners.add(listener);
 	}
 
 	@Override
 	public void visualizerConfigChanged(VisualizerConfig config) {
 		if (config.isChanged(VisualizerConfigItemEnum.LOCALIZATION_MODE)) {
 			switch (config.getLocalizationMode()) {
 			case PARTICLE_FILTER:
 				if (!active) {
 					log.info("Activating particle filter");
 					world.addSampleListener(this);
 					active = true;
 				}
 				break;
 			default:
 				if (active) {
 					log.info("Deactivating particle filter");				
 					world.removeSampleListener(this);
 					active = false;
 				}
 				break;
 			}
 		}
 	}
 }
