 package org.logparser.stats;
 
 import net.jcip.annotations.Immutable;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 
 /**
  * {@link Predicate} implementation that allows consumers to selectively filter
  * out matching instances according to the given condition.
  * 
  * @author jorge.decastro
  * 
  */
 @Immutable
 public class StandardDeviationPredicate implements Predicate<PredicateArguments> {
	private final double noOfStandardDeviations;
 
 	public StandardDeviationPredicate() {
 		this(1);
 	}
 
	public StandardDeviationPredicate(final double noOfStandardDeviations) {
		Preconditions.checkArgument(noOfStandardDeviations > 0, "Number of standard deviations must be a positive number.");
 		this.noOfStandardDeviations = noOfStandardDeviations;
 	}
 
 	public boolean apply(PredicateArguments arguments) {
 		double delta = arguments.getPrevious().getMean() - arguments.getCurrent();
 		double sd = noOfStandardDeviations * arguments.getPrevious().getStandardDeviation();
 		return sd > 0 && Math.abs(delta) > sd;
 	}
 
	public double getNumberOfStandardDeviations() {
 		return noOfStandardDeviations;
 	}
 }
