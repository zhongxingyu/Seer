 package tu.modgeh.intfiresim;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 public class Integrator {
 
 	private List<SpikeListener> spikeListeners = new ArrayList<SpikeListener>();
 	private List<UpdateListener> updateListeners = new ArrayList<UpdateListener>();
 
 	/** r_m [MOhm] */
 	private double membraneResistance = 20.0;
 
 	/** c_m [nF] */
 	private double membraneCapacitance = 0.5;
 
 	/** V_reset [mV] */
	private double resetPotential = -62.0;
 
 	/** E_r [mV] */
 	private double restingPotential = -70.0;
 
 	/** V_th [mV] */
 	private double threasholdPotential = -54.0;
 
 	/** sigma */
 	private double noiseStandardDeviation = 0.5;
 
 	/** i [nA] */
 	private double current = 0.5;
 
 	/** delta-t [s] */
 	private double deltaT = 0.2E-3;
 
 	/** V [mV] */
 	private double potential = 0.0;
 
 	/** t [s] */
 	private double time = 0.0;
 
 	/** random generator for the noise */
 	private Random rand = new Random();
 
 
 	public Integrator() {
 	}
 
 	/** @param simulation duration in seconds */
 	public void runSimulation(double duration) {
 
 		potential = restingPotential;
 		time = 0.0;
 
 		while (time < duration) {
 			update();
 			time += deltaT;
 		}
 	}
 
 	public void addSpikeListener(SpikeListener spikeListener) {
 		spikeListeners.add(spikeListener);
 	}
 	public void removeSpikeListener(SpikeListener spikeListener) {
 		spikeListeners.remove(spikeListener);
 	}
 	protected void fireSpike() {
 
 		SpikeEvent spikeEvent = new SpikeEvent();
 		for (SpikeListener spikeListener : spikeListeners) {
 			spikeListener.spikeGenerated(spikeEvent);
 		}
 	}
 
 	public void addUpdateListener(UpdateListener updateListener) {
 		updateListeners.add(updateListener);
 	}
 	public void removeUpdateListener(UpdateListener updateListener) {
 		updateListeners.remove(updateListener);
 	}
 	protected void fireUpdateFinished() {
 
 		UpdateEvent updateEvent = new UpdateEvent();
 		for (UpdateListener updateListener : updateListeners) {
 			updateListener.stepFinnished(updateEvent);
 		}
 	}
 
 	private double calcLeak() {
 
 		return - (deltaT / (membraneResistance * membraneCapacitance / 1E3))
 					* (potential - restingPotential);
 	}
 
 	private double calcStored() {
 		return deltaT * (current / membraneCapacitance) * 1E3;
 	}
 
 	private double calcNoise() {
 		return Math.sqrt(deltaT) * (noiseStandardDeviation * rand.nextGaussian()) * 1E3;
 	}
 
 	private double calcMembranePotentialChange() {
 		return calcLeak() + calcStored() + calcNoise();
 	}
 
 	private void update() {
 
 		potential += calcMembranePotentialChange();
 
 		if (potential > threasholdPotential) {
 			fireSpike();
 			potential = resetPotential;
 		}
 
 		fireUpdateFinished();
 	}
 
 	public double getMembraneResistance() {
 		return membraneResistance * 1E6;
 	}
 
 	public void setMembraneResistance(double membraneResistance) {
 		this.membraneResistance = membraneResistance / 1E6;
 	}
 
 	public double getMembraneCapacitance() {
 		return membraneCapacitance / 1E9;
 	}
 
 	public void setMembraneCapacitance(double membraneCapacitance) {
 		this.membraneCapacitance = membraneCapacitance * 1E9;
 	}
 
 	public double getResetPotential() {
 		return resetPotential / 1E3;
 	}
 
 	public void setResetPotential(double resetPotential) {
 		this.resetPotential = resetPotential * 1E3;
 	}
 
 	public double getRestingPotential() {
 		return restingPotential / 1E3;
 	}
 
 	public void setRestingPotential(double restingPotential) {
 		this.restingPotential = restingPotential * 1E3;
 	}
 
 	public double getThreasholdPotential() {
 		return threasholdPotential / 1E3;
 	}
 
 	public void setThreasholdPotential(double threasholdPotential) {
 		this.threasholdPotential = threasholdPotential * 1E3;
 	}
 
 	public double getNoiseStandardDeviation() {
 		return noiseStandardDeviation;
 	}
 
 	public void setNoiseStandardDeviation(double randStandardDeviation) {
 		this.noiseStandardDeviation = randStandardDeviation;
 	}
 
 	public double getCurrent() {
 		return current / 1E9;
 	}
 
 	public void setCurrent(double current) {
 		this.current = current * 1E9;
 	}
 
 	public double getDeltaT() {
 		return deltaT;
 	}
 
 	public void setDeltaT(double deltaT) {
 		this.deltaT = deltaT;
 	}
 
 	public double getPotential() {
 		return potential / 1E3;
 	}
 
 	public void setPotential(double potential) {
 		this.potential = potential * 1E3;
 	}
 
 	public double getTime() {
 		return time;
 	}
 
 	public Random getRand() {
 		return rand;
 	}
 
 	public void setRand(Random rand) {
 		this.rand = rand;
 	}
 
 	@Override
 	public String toString() {
 
 		StringBuilder str = new StringBuilder();
 
 		str.append("Integrate-and-fire {");
 		str.append("r_m: ").append(getMembraneResistance()).append("Ohm ");
 		str.append("c_m: ").append(getMembraneCapacitance()).append("F ");
 		str.append("V_reset: ").append(getResetPotential()).append("V ");
 		str.append("E_r: ").append(getRestingPotential()).append("V ");
 		str.append("V_th: ").append(getThreasholdPotential()).append("V ");
 		str.append("sigma: ").append(getNoiseStandardDeviation()).append(" ");
 		str.append("i: ").append(getCurrent()).append("A ");
 		str.append("deltaT: ").append(getDeltaT()).append("s ");
 //		str.append("V: ").append(getPotential()).append("V ");
 //		str.append("t: ").append(getTime()).append("s ");
 		str.append("}");
 
 		return str.toString();
 	}
 }
