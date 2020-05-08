 package to.richard.tsp;
 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * Author: Richard To
  * Date: 2/6/13
  */
 
 /**
  * Roulette Wheel algorithm implementation.
  *
  * Roulette Wheel is Immutable.
  *
  * Windowing, sigma scaling, and ranking can be
  * applied before adding points.
  *
  * @param <E>
  */
 public class RouletteWheel<E> {

    /**
     * TreeMap used to keep order of probability segments. So .3, .5 .8 1.0.
     */
     private TreeMap<Double, E> _distributionMap;
     private IRandom _random;
 
     /**
      * Constructs a roulette wheel to randomly select objects, based on their weight.
      *
      * The valueMap is a TreeMap. The key is an integer representing the weight of the
      * object. The higher the weight in proportion to the objects, the bigger the
      * roulette wheel slice. The value of the TreeMap is any object.
      *
     * Pass in a LinkedHashMap to valueMap if insertion order is important. Seems only necessary
     * for testing.
     *
      * @param valueMap
      * @param random
      */
    public RouletteWheel(Map<Integer, E> valueMap, IRandom random) {
         _distributionMap = new TreeMap<Double, E>();
         _random = random;
         double totalProbability = 0.0;
         double probability = 0.0;
        double totalWeight = 0;
 
         for (Integer weight : valueMap.keySet()) {
             totalWeight += weight;
         }
         for (Map.Entry<Integer, E> entry : valueMap.entrySet()) {
             probability = entry.getKey() / totalWeight;
             totalProbability += probability;
             _distributionMap.put(totalProbability, entry.getValue());
         }
     }
 
     /**
     * Picks a value within a distribution point
      *
      * Object is returned by reference.
      *
      * @param point
      * @return E
      */
    public E pick(double point){
         Map.Entry<Double, E> entry = _distributionMap.higherEntry(point);
         if(entry != null){
             return entry.getValue();
         }
         return null;
     }
 
     /**
      * Picks one random element from distribution.
      *
      * Object is returned by reference.
      *
      * @return E
      */
    public E sampleOne() {
         double probability = _random.nextDouble();
        return pick(probability);
     }
 
     /**
      * Samples n elements from roulette wheel.
      *
      * Object is returned by reference.
      *
      * @param n
      * @return E
      */
     public ArrayList<E> sample(int n) {
         ArrayList<E> sampledObjects = new ArrayList<E>();
         double probability;
         for (int i = 0; i < n; i++) {
             probability = _random.nextDouble();
            sampledObjects.add(pick(probability));
         }
         return sampledObjects;
     }
 }
