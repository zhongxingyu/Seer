 package org.andrill.conop.search.schedules;
 
 import java.util.Properties;
 
 import org.andrill.conop.search.AbstractConfigurable;
 import org.andrill.conop.search.Solution;
 
 /**
  * An exponential cooling schedule.
  * 
  * @author Josh Reed (jareed@andrill.org)
  */
 public class ExponentialSchedule extends AbstractConfigurable implements CoolingSchedule {
 	protected long count;
 	protected double current;
 	protected double factor;
 	protected double initial;
 	protected long minStepsPer;
 	protected long noProgress;
 	protected double score = -1;
 	protected long stop = 0;
 
 	/**
 	 * Create a new ExponentialSchedule.
 	 */
 	public ExponentialSchedule() {
 		initial = 1000;
 		factor = 0.01;
 		minStepsPer = 100;
 		noProgress = Long.MAX_VALUE;
 	}
 
 	@Override
 	public void configure(final Properties properties) {
 		this.initial = Double.parseDouble(properties.getProperty("schedule.initial", "1000"));
 		this.factor = Double.parseDouble(properties.getProperty("schedule.delta", "0.01"));
 		this.minStepsPer = Long.parseLong(properties.getProperty("schedule.stepsPer", "100"));
 		String value = properties.getProperty("schedule.noProgress");
 		if (value == null) {
 			this.noProgress = Long.MAX_VALUE;
 		} else {
 			this.noProgress = Long.parseLong(value);
 		}
 	}
 
 	@Override
 	public double getInitial() {
 		return initial;
 	}
 
 	@Override
 	public double next(final Solution solution) {
 		count++;
 		stop++;
 		if (current < 0.01) {
 			return 0;
 		} else if (stop > noProgress) {
 			throw new RuntimeException("Stopped due to no progress in " + noProgress + " iterations");
 		} else if (score == -1) {
 			score = solution.getScore();
 			return current;
 		} else if (solution.getScore() <= score) {
 			score = solution.getScore();
 			count = 0;
 			stop = 0;
 			return current;
 		} else if (count > minStepsPer) {
 			current = current / (1 + current * factor);
 			count = 0;
 			return current;
 		} else {
 			return current;
 		}
 	}
 }
