import java.util.*;

 enum PeggyMode
 {
   BouncingBalls,
   Fireworks,
   Cancer,
   Squares,
  //BugTwo,
   Primes,
  Horticulture,
  Spore;
  
  private static final List<PeggyMode> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
  private static final int SIZE = VALUES.size();
  private static final Random RANDOM = new Random();
  
  public static PeggyMode randomMode() 
  {
    return VALUES.get(RANDOM.nextInt(SIZE));
  }
 }
