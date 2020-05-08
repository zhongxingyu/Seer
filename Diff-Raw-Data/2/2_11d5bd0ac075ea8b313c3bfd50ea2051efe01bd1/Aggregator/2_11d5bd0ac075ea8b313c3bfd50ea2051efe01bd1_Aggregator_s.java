 package ddg.model;
 
 import static ddg.model.Machine.ACTIVE_POWER_IN_WATTS;
 import static ddg.model.Machine.IDLE_POWER_IN_WATTS;
 import static ddg.model.Machine.SHUTDOWN_POWER_IN_WATTS;
 import static ddg.model.Machine.SHUTDOWN_TRANSITION_DURATION;
 import static ddg.model.Machine.SLEEP_POWER_IN_WATTS;
 import static ddg.model.Machine.SLEEP_TRANSITION_DURATION;
 import static ddg.model.Machine.TRANSITION_POWER_IN_WATTS;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import ddg.kernel.Time;
 
 public class Aggregator {
 
 	private final Map<String, MachineAvailability> availabilityTotalsPerMachine = 
 		new HashMap<String, MachineAvailability>();
 
 	private static Aggregator instance = new Aggregator();
 
 	public static Aggregator getInstance() {
 		return instance;
 	}
 
 	private Aggregator() { /* empty */ }
 	
 	/**
 	 * Clean all aggregated result. This method exists to make testing easy.
 	 */
 	public void reset() {
 		availabilityTotalsPerMachine.clear();
 	}
 	
 	public void aggregateActiveDuration(String machine, Time activeDuration) {
 		getMachineAvailability(machine).addActiveDuration(activeDuration);
 	}
 	
 	public void aggregateIdleDuration(String machine, Time idleDuration) {
 		getMachineAvailability(machine).addIdleDuration(idleDuration);
 	}
 	
 	public void aggregateSleepingDuration(String machine, Time sleepingDuration) {
 		getMachineAvailability(machine).addSleepingDuration(sleepingDuration);
 	}
 	
 	public void aggregateShutdownDuration(String machine, Time shutdownDuration) {
 		getMachineAvailability(machine).addShutdownDuration(shutdownDuration);
 	}
 	
 	public MachineAvailability getMachineAvailability(String machine) {
 		MachineAvailability machineAvailability = availabilityTotalsPerMachine.get(machine);
 		if(machineAvailability == null) {
 			machineAvailability = new MachineAvailability();
 			availabilityTotalsPerMachine.put(machine, machineAvailability);
 		}
 		
 		return machineAvailability;
 	}
 	
 	public String summarize() {
 		StringBuilder summary = new StringBuilder();
 		
 		//summarize availability
 		summary.append("\n\n============================================ \nAvailability summary: \n");
 		
 		Time totalActiveDuration = Time.GENESIS;
 		Time totalIdleDuration = Time.GENESIS;
 		Time totalSleepingDuration = Time.GENESIS;
 		Time totalShutdownDuration = Time.GENESIS;
 		int shutdownCount = 0;
 		int sleepCount = 0;
 		
 		for(String machine : availabilityTotalsPerMachine.keySet()) {
 			MachineAvailability ma = availabilityTotalsPerMachine.get(machine);
 			
 			Time machineActiveDuration =  ma.getTotalActiveDuration();
 			Time machineIdleDuration = ma.getTotalIdleDuration();
 			Time machineSleepingDuration = ma.getTotalSleepingDuration();
 			Time machineShutdownDuration = ma.getTotalShutdownDuration();
 			int machineShutdownCount = ma.getShutdownCount();  
 			int machineSleepCount = ma.getSleepCount();
 			
			String format = "\nMachine=%s\tActive=%d us\tIdle=%s us\tSleeping=%s us\tTurned off=%s us\t" +
 					"Shutdowns=%s\tSleepings=%s";
 			
 			summary.append(String.format(format, machine, machineActiveDuration, machineIdleDuration, 
 					machineSleepingDuration, machineShutdownDuration, machineShutdownCount, machineSleepCount));
 			
 			totalActiveDuration =  totalActiveDuration.plus(machineActiveDuration);
 			totalIdleDuration = totalIdleDuration.plus(machineIdleDuration);
 			totalSleepingDuration = totalSleepingDuration.plus(machineSleepingDuration);
 			totalShutdownDuration = totalShutdownDuration.plus(machineShutdownDuration);
 			shutdownCount += machineShutdownCount;
 			sleepCount += machineSleepCount;
 		}
 		
 		summary.append(String.format("\n\nTotal active duration:\t%d us",totalActiveDuration));
 		summary.append(String.format("\nTotal idle duration:\t%d us", totalIdleDuration));
 		summary.append(String.format("\nTotal sleeping duration:\t%d us", totalSleepingDuration));
 		summary.append(String.format("\nTotal turned off duration:\t%d us", totalShutdownDuration));
 		summary.append(String.format("\nTotal shutdowns:\t%d", shutdownCount));
 		summary.append(String.format("\nTotal sleeps:\t%d", sleepCount));
 		
 		//summarize energy consumption
 		summary.append("\n\n============================================ \nEnergy consumption summary: \n");
 		double activeConsumptionWh = totalActiveDuration.asHours() * ACTIVE_POWER_IN_WATTS;
 		
 		double idleConsumptionWh = totalIdleDuration.asHours() * IDLE_POWER_IN_WATTS;
 		
 		Time totalSleepTransition = SLEEP_TRANSITION_DURATION.times(sleepCount * 2); //each sleep corresponds to two transitions
 		double sleepTransitionConsumptionWh = totalSleepTransition.asHours() * TRANSITION_POWER_IN_WATTS;
 		double sleepConsumptionWh = totalSleepingDuration.minus(totalSleepTransition).asHours() * SLEEP_POWER_IN_WATTS;
 		
 		Time totalShutdownTransition = SHUTDOWN_TRANSITION_DURATION.times(shutdownCount * 2); //each shutdown corresponds to two transitions
 		double shutdownTransitionConsumptionWh = totalShutdownTransition.asHours() * TRANSITION_POWER_IN_WATTS;
 		double shutdownConsumptionWh = 
 				totalShutdownDuration.minus(totalShutdownTransition).asHours() * SHUTDOWN_POWER_IN_WATTS;
 		
 		
 		double energyConsumptionkWh = 
 				( 		activeConsumptionWh + idleConsumptionWh + 
 						sleepConsumptionWh + shutdownConsumptionWh + 
 						sleepTransitionConsumptionWh + shutdownTransitionConsumptionWh) / 1000; 
 		
 		summary.append(String.format("\nEnergy consumption:\t%f kWh", energyConsumptionkWh));
 		
 		return summary.toString();
 	}
 	
 }
