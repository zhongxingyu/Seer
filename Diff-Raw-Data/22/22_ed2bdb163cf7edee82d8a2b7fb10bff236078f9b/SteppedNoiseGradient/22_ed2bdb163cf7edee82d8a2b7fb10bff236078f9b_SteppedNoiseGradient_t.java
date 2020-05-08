 package logic.generator;
 
import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.BoundType;

 import teampg.datatypes.valuerange.ValueRangeMapper;
 import teampg.grid2d.point.Pos2D;
 import libnoiseforjava.module.ModuleBase;
 
 public class SteppedNoiseGradient<T> {
 	private static final double NOISE_FACTOR = 0.001D;
 
 	private final ModuleBase input;
 	private final ValueRangeMapper<Double, T> stepper;
 
 	public SteppedNoiseGradient(ModuleBase input, ValueRangeMapper<Double, T> stepper) {
		checkArgument(stepper.getBounds().hasUpperBound());
		checkArgument(stepper.getBounds().upperBoundType() == BoundType.CLOSED);
		checkArgument(stepper.getBounds().hasLowerBound());
		checkArgument(stepper.getBounds().lowerBoundType() == BoundType.CLOSED);

 		this.input = input;
 		this.stepper = stepper;
 	}
 
 	public T getValue(Pos2D forPos) {
		double noiseAtPos = input.getValue(forPos.x * NOISE_FACTOR, forPos.y * NOISE_FACTOR, 0);

		// random noise modules not guaranteed to always fit exactly into range, so coerce them
		if (noiseAtPos > stepper.getBounds().upperEndpoint()) {
			noiseAtPos = stepper.getBounds().upperEndpoint();
		} else if (noiseAtPos < stepper.getBounds().lowerEndpoint()) {
			noiseAtPos = stepper.getBounds().lowerEndpoint();
		}

		return stepper.getValue(noiseAtPos);
 	}
 }
