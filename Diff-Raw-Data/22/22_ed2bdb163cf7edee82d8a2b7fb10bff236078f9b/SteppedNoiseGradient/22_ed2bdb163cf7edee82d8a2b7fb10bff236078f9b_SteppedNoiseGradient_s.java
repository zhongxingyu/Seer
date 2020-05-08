 package logic.generator;
 
 import teampg.datatypes.valuerange.ValueRangeMapper;
 import teampg.grid2d.point.Pos2D;
 import libnoiseforjava.module.ModuleBase;
 
 public class SteppedNoiseGradient<T> {
 	private static final double NOISE_FACTOR = 0.001D;
 
 	private final ModuleBase input;
 	private final ValueRangeMapper<Double, T> stepper;
 
 	public SteppedNoiseGradient(ModuleBase input, ValueRangeMapper<Double, T> stepper) {
 		this.input = input;
 		this.stepper = stepper;
 	}
 
 	public T getValue(Pos2D forPos) {
		return stepper.getValue(input.getValue(forPos.x * NOISE_FACTOR, forPos.y * NOISE_FACTOR, 0));
 	}
 }
