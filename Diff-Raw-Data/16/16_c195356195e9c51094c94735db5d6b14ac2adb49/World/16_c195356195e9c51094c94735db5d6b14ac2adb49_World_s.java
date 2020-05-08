 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package evopaint;
 
 import evopaint.pixel.Pixel;
 import evopaint.util.RandomNumberGeneratorWrapper;
 import evopaint.interfaces.IRandomNumberGenerator;
 import evopaint.pixel.Rule;
 import evopaint.pixel.actions.AssimilationAction;
 import evopaint.pixel.actions.RewardAction;
 import evopaint.pixel.conditions.EnergyCondition;
 import evopaint.pixel.conditions.LikeColorCondition;
 import evopaint.util.Color;
 import evopaint.util.mapping.AbsoluteCoordinate;
 
 import evopaint.util.logging.Logger;
 import evopaint.util.mapping.ParallaxMap;
 import evopaint.util.mapping.RelativeCoordinate;
 import org.uncommons.maths.random.CellularAutomatonRNG;
 import org.uncommons.maths.random.DefaultSeedGenerator;
 import org.uncommons.maths.random.SeedException;
 import org.uncommons.maths.random.SeedGenerator;
 
 /**
  *
  * @author tam
  */
 public class World extends ParallaxMap<Pixel> {
 
     private Config configuration;
     private long time;
     private IRandomNumberGenerator rng;
 
     public void init() {
         this.initRNG();
         this.createPixels();
     }
 
     public void step() {
 
         if (time == Long.MAX_VALUE) {
             java.lang.System.out.println("time overflowed");
         }
  
         Logger.log.information("Time: %s", new Object[]{time});
        
         //Collections.shuffle(this.relations, this.rng.getRandom());
 
         //if (this.configuration.numThreads > 1) {
             //this.parallel();
         //} else {
             this.serial();
         //}
 
         this.time++;
 
         //if (time == 1000)
           //  java.lang.System.exit(0);
 
     }
 
     private void createPixels() {
        Rule number1 = new Rule(
                new EnergyCondition(RelativeCoordinate.HERE, 20, EnergyCondition.GREATER_OR_EQUAL),
                 new AssimilationAction(20, RelativeCoordinate.WEST, Color.MIX_HSB));
         Rule number2 = new Rule(
                new LikeColorCondition("is a little blue", RelativeCoordinate.NORTH, new Color(0xFF), 0.1, Color.COMPARE_BY_BLUE),
                 new RewardAction(10));
 
         System.out.println("Test with 2 hardcoded rules for every pixel:");
         System.out.println("Rule number1: '" + number1 + "'");
         System.out.println("Rule number2: '" + number2 + "'");
 
         for (int y = 0; y < configuration.initialPopulation.height; y++) {
             for (int x = 0; x < configuration.initialPopulation.width; x++) {
 
                 int energy = configuration.startingEnergy;
                 Color color = new Color(rng.nextPositiveInt());
                 AbsoluteCoordinate location = new AbsoluteCoordinate(configuration.dimension.width / 2 - configuration.initialPopulation.width / 2 + x,
                         configuration.dimension.height / 2 - configuration.initialPopulation.height / 2 + y, this);
                 Pixel pixie = new Pixel(energy, color, location);
                 pixie.learn(number1);
                 pixie.learn(number2);
                 set(pixie);
             }
         }
     }
 
     public void set(Pixel pixel) {
         super.set(pixel.getLocation().x, pixel.getLocation().y, pixel);
     }
 
     private void serial() {
         int [] indices = getShuffledIndices(rng);
         
         for (int i = 0; i < indices.length; i++) {
             Pixel pixie = get(indices[i]);
             if (pixie.isAlive()) {
                 pixie.act(this);
             } else {
                 set(indices[i], null);
             }
         }
     }
 /*
     private void parallel() {
         List<List<PixelRelation>> partitionedRelations = this.partition(this.relations, this.configuration.numRelationThreads);
 
         // make threads
         List<Thread> relatorThreads = new ArrayList<Thread>();
         for (List<PixelRelation> part : partitionedRelations) {
 
             // get random seed out of our rng
             byte [] seed = new byte[4];
             this.rng.getRandom().nextBytes(seed);
 
             // and seed a new rng for each thread, so they can do random shit in
             // a well defined order (without race conditions on the main rng)
             Thread relator = new Relator(this, part, new RandomNumberGeneratorWrapper(new CellularAutomatonRNG(seed)), configuration);
             relatorThreads.add(relator);
             relator.start();
         }
 
         // join threads
         for (Thread relatorThread : relatorThreads) {
             try {
                 relatorThread.join();
             }
             catch (InterruptedException ex) {
                 Logger.log.warning("Exception during Relating %s", ex.getMessage());
             }
         }
     }
 
     private List<List<PixelRelation>> partition(List<PixelRelation> relations, int numChunks) {
 
         List<List<PixelRelation>> ret = new ArrayList<List<PixelRelation>>();
 
         int chunkSize = relations.size() / numChunks;
         int rest = relations.size() % numChunks;
         for (int i = 0; i < numChunks; i++) {
             List subList = relations.subList(i * chunkSize, (i + 1) * chunkSize + (i < rest ? 1 : 0));
             ret.add(subList);
         }
 
         // do not ever try to do it this way. the ternary is just fine...
         // adding the rest like this will cause concurrent modification exceptions
         // in parallel operation mode
         // 
         // for (int i = 0; i < rest; i++) {
         //    ret.get(i).add(relations.get(relations.size() - i - 1));
         // }
 
         return ret;
     }
 */
     public Config getConfiguration() {
         return configuration;
     }
 
     private void initRNG() {
         // Random, SecureRandom, AESCounterRNG, CellularAutomatonRNG,
         // CMWC4096RNG, JavaRNG, MersenneTwisterRNG, XORShiftRNG
 
         // default seed size for cellularAutomatonRNG is 4 bytes;
         int seed_size_bytes = 4;
 
         // set fixed seed or null for generation
         byte [] seed = null;
        // byte [] seed = new byte [] { 1, 2, 3, 4 };
 
         // default seed generator checks some different approaches and will
         // always succeed
         if (seed == null) {
             SeedGenerator sg = DefaultSeedGenerator.getInstance();
             try {
                 seed = sg.generateSeed(4);
             } catch (SeedException e) {
                 Logger.log.error("got seed exception from default seed generator. this should not have happened.");
                 java.lang.System.exit(1);
             }
         }
         rng = new RandomNumberGeneratorWrapper(new CellularAutomatonRNG(seed));
     }
 
     public IRandomNumberGenerator getRandomNumberGenerator() {
         return rng;
     }
 
     public World(Pixel [] pixels, long time, Config configuration) {
         super(pixels, configuration.dimension.width, configuration.dimension.height);
         this.time = time;
         this.configuration = configuration;
         this.init();
     }
 }
