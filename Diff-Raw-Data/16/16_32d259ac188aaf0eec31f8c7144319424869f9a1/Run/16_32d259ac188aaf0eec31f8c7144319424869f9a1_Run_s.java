 package model_example;
 
 // For simulation, it's important to use a high-quality PRNG,
 // with long cycles and good distribution.
 import org.uncommons.maths.random.MersenneTwisterRNG;
 import java.util.*;
 import edu.berkeley.path.model_mock.MockLogSink;
 
 public class Run extends edu.berkeley.path.model_base.Run {
   public long seed0;
   public long seed1;
   public MersenneTwisterRNG prng;
 
   /**
    * Construct a run with some example sources and sinks. Normally, the 
    * sources and sinks would be constructed externally to the Run and
    * assigned.
    **/
  public Run(Integer id, Context context, long seed0, long seed1) {
     super(id, context);
 
     this.seed0 = seed0;
     this.seed1 = seed1;
     this.prng = new MersenneTwisterRNG(longsToByteArray(seed0, seed1));
 
     sources().put("network", new NetworkSource());
     sources().put("density", new DensitySource());
     sinks().put("density", new DensitySink());
     sinks().put("log", new MockLogSink());
   }
   
   public State makeState() {
     return new State(this);
   }
 
   public static final byte[] longsToByteArray(long v0, long v1) {
     return new byte[] {
       (byte)(v0 >>> 56), (byte)(v0 >>> 48),
       (byte)(v0 >>> 40), (byte)(v0 >>> 32),
       (byte)(v0 >>> 24), (byte)(v0 >>> 16),
       (byte)(v0 >>> 8),  (byte)v0,
       (byte)(v1 >>> 56), (byte)(v1 >>> 48),
       (byte)(v1 >>> 40), (byte)(v1 >>> 32),
       (byte)(v1 >>> 24), (byte)(v1 >>> 16),
       (byte)(v1 >>> 8),  (byte)v1
     };
   }
 }
     
