 package nl.naxanria.selgen;
 
 import no.runsafe.framework.minecraft.RunsafeWorld;
 
 public class Part
 {
 
 	public Part(GenerateHills generateHills, int startX, int endX, int startZ, int endZ, long seed,
 							double amplitude, double frequency, double scale, double seaLevel, double multitude, boolean hollow, RunsafeWorld world)
 	{
 		this.startX = startX;
 		this.endX = endX;
 		this.startZ = startZ;
 		this.endZ = endZ;
 		this.amplitude = amplitude;
 		this.frequency = frequency;
 		this.scale = scale;
 		this.seaLevel = seaLevel;
 		this.multitude = multitude;
 		this.hollow = hollow;
 		this.world = world;
 		this.generateHills = generateHills;
 		this.seed = seed;
 	}
 
 	public void generate()
 	{
		generateHills.generate(startX, startZ, endX, endZ, multitude, seaLevel, seed, scale, frequency, amplitude, hollow, world );
 	}
 
 	private GenerateHills generateHills;
 
 	private int startX;
 	private int endX;
 	private int startZ;
 	private int endZ;
 
 	private double amplitude;
 	private double frequency;
 	private double scale;
 
 	private double seaLevel;
 	private double multitude;
 
 	private boolean hollow;
 
 	private RunsafeWorld world;
 
 	private long seed;
 }
