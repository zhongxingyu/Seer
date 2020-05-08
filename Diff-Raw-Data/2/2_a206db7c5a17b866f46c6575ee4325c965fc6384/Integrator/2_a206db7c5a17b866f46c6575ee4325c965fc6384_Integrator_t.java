 package tu.modgeh.intfiresim;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 public class Integrator {
 
 	private List<SpikeListener> spikeListeners = new ArrayList<SpikeListener>();
 	private List<UpdateListener> updateListeners = new ArrayList<UpdateListener>();
 
 	/** r_m [Ohm] */
 	private double membraneResistance = 20E6;
 
 	/** c_m [F] */
 	private double membraneCapacitance = 0.5E-9;
 
 	/** V_reset [V] */
 	private double resetPotential = -63E-3;
 
 	/** E_r [V] */
 	private double restingPotential = -70E-3;
 
 	/** V_th [V] */
 	private double threasholdPotential = -54E-3;
 
 	/** sigma  [s] */
 	private double randStandardDeviation = 0.5E-3;
 
 	/** i [A] */
 	private double current = 0.5E-9;
 
 	/** delta-t [s] */
 	private double deltaT = 0.2E-3;
 
 	/** V [V] */
 	private double potential = 0.0;
 
 	/** t [s] */
 	private double time = 0.0;
 
 	/** V [V] */
 	private Random rand = new Random();
 
 
 	public Integrator() {
 	}
 
 	/** @param tillTime in seconds */
 	public void runSimulation(double tillTime) {
 
 		time = 0.0;
 		while (time < tillTime) {
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
 
		return - (deltaT / (membraneResistance * membraneCapacitance))
 					* (potential - restingPotential);
 	}
 
 	private double calcStored() {
 		return deltaT * (current / membraneCapacitance);
 	}
 
 	private double calcNoise() {
 		return Math.sqrt(deltaT) * (randStandardDeviation * rand.nextGaussian());
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
 		return membraneResistance;
 	}
 
 	public void setMembraneResistance(double membraneResistance) {
 		this.membraneResistance = membraneResistance;
 	}
 
 	public double getMembraneCapacitance() {
 		return membraneCapacitance;
 	}
 
 	public void setMembraneCapacitance(double membraneCapacitance) {
 		this.membraneCapacitance = membraneCapacitance;
 	}
 
 	public double getResetPotential() {
 		return resetPotential;
 	}
 
 	public void setResetPotential(double resetPotential) {
 		this.resetPotential = resetPotential;
 	}
 
 	public double getRestingPotential() {
 		return restingPotential;
 	}
 
 	public void setRestingPotential(double restingPotential) {
 		this.restingPotential = restingPotential;
 	}
 
 	public double getThreasholdPotential() {
 		return threasholdPotential;
 	}
 
 	public void setThreasholdPotential(double threasholdPotential) {
 		this.threasholdPotential = threasholdPotential;
 	}
 
 	public double getRandStandardDeviation() {
 		return randStandardDeviation;
 	}
 
 	public void setRandStandardDeviation(double randStandardDeviation) {
 		this.randStandardDeviation = randStandardDeviation;
 	}
 
 	public double getCurrent() {
 		return current;
 	}
 
 	public void setCurrent(double current) {
 		this.current = current;
 	}
 
 	public double getDeltaT() {
 		return deltaT;
 	}
 
 	public void setDeltaT(double deltaT) {
 		this.deltaT = deltaT;
 	}
 
 	public double getPotential() {
 		return potential;
 	}
 
 	public void setPotential(double potential) {
 		this.potential = potential;
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
 		str.append("sigma: ").append(getRandStandardDeviation()).append(" ");
 		str.append("i: ").append(getCurrent()).append("A ");
 		str.append("deltaT: ").append(getDeltaT()).append("s ");
 //		str.append("V: ").append(getPotential()).append("V ");
 //		str.append("t: ").append(getTime()).append("s ");
 		str.append("}");
 
 		return str.toString();
 	}
 }
