 package org.andrill.conop.search.schedules;
 
 import java.util.Properties;
 
 import org.andrill.conop.search.AbstractConfigurable;
 import org.andrill.conop.search.Solution;
 
 /**
  * A linear cooling schedule.
  * 
  * @author Josh Reed (jareed@andrill.org)
  */
 public class LinearSchedule extends AbstractConfigurable implements CoolingSchedule {
 	protected double current = 0.0;
 	protected double delta = 1;
 	protected double initial = 0;
 	protected long stepsLeft = 1;
 	protected long stepsPer = 1;
 
 	/**
 	 * Create a new LinearSchedule.
 	 */
 	public LinearSchedule() {
 		this.initial = 1000;
 		this.delta = 0.01;
 		this.stepsPer = 100;
		this.current = initial;
 	}
 
 	@Override
 	public void configure(final Properties properties) {
 		this.initial = Double.parseDouble(properties.getProperty("schedule.initial", "1000"));
 		this.delta = Double.parseDouble(properties.getProperty("schedule.delta", "0.01"));
 		this.stepsPer = Long.parseLong(properties.getProperty("schedule.stepsPer", "100"));
		this.current = initial;
 	}
 
 	@Override
 	public double getInitial() {
 		return initial;
 	}
 
 	@Override
 	public double next(final Solution solution) {
 		stepsLeft--;
 		if (stepsLeft == 0) {
 			stepsLeft = stepsPer;
 			current = Math.max(current - delta, 0);
 		}
 		return current;
 	}
 }
