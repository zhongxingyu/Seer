 package raisa.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import raisa.util.CollectionUtil;
 
 public class AveragingSampleFixer implements SampleFixer {
 	private float lastKnownDirection;
 	private List<Sample> lastSamples = new ArrayList<Sample>();
 	private int windowLength;
 	private float maxDeviation;
 	
 	public AveragingSampleFixer(int windowLength, float maxDeviationInDegrees) {
 		this.windowLength = windowLength;
 		this.maxDeviation = (float)Math.PI * maxDeviationInDegrees / 180.0f;
 	}
 	
 	@Override
 	public Sample fix(Sample sample) {
 		Sample fixedSample = new Sample(sample);
 
 		if (lastSamples.isEmpty()) {
 			lastKnownDirection = sample.getCompassDirection();
 		}
 		lastSamples.add(sample);
 		lastSamples = CollectionUtil.takeLast(lastSamples, windowLength);
 
 		// calculate average direction of last samples
 		float ahx = 0.0f;
 		float ahy = 0.0f;
 		for (Sample lastSample : lastSamples) {
 			float dx = (float)Math.cos(lastSample.getCompassDirection());
 			float dy = (float)Math.sin(lastSample.getCompassDirection());
 			ahx += dx;
 			ahy += dy;
 		}		
 		float averageDirection = (float)Math.atan2(ahy, ahx);
 		
 		// calculate sum of squares of differences
 		float sumOfSquares = 0.0f;
 		ahx = (float)Math.cos(averageDirection);
 		ahy = (float)Math.sin(averageDirection);
 		for (Sample lastSample : lastSamples) {
 			float dx = (float)Math.cos(lastSample.getCompassDirection());
 			float dy = (float)Math.sin(lastSample.getCompassDirection());
 			float difference = (float)Math.acos(dx * ahx + dy * ahy);
 			sumOfSquares += difference * difference;
 		}
 		
 		// calculate square of sum of differences
 		float squareOfSums = 0.0f;
 		for (Sample lastSample : lastSamples) {
 			float direction = lastSample.getCompassDirection();
 			float difference = differenceInAngles(ahx, ahy, averageDirection, direction);
 			squareOfSums += difference;
 		}
 		squareOfSums = squareOfSums * squareOfSums;
 		
 		// calculate standard deviation
 		float standardDeviation = (float)Math.sqrt(sumOfSquares - squareOfSums);
 		
 		// if sample deviates too much, use last known good direction
 		if (standardDeviation > maxDeviation) {
 			fixedSample.setCompassDirection(lastKnownDirection);
 		}
 		return fixedSample;
 	}
 
 	private float differenceInAngles(float ahx, float ahy, float averageDirection, float direction) {
 		float dx = (float)Math.cos(direction);
 		float dy = (float)Math.sin(direction);
 		float difference = (float)Math.acos(dx * ahx + dy * ahy);
 		if (direction < averageDirection || (direction - averageDirection) > Math.PI) {
 			difference = -difference;
 		}
 		return difference;
 	}
 }
