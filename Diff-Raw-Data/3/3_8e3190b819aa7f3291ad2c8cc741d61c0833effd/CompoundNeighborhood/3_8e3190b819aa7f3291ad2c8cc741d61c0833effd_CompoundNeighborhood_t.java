 package cs437.som.neighborhood;
 
 import cs437.som.NeighborhoodWidthFunction;
 import cs437.som.SOMError;
 import cs437.som.util.Reflector;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Collects a series of neighborhood width functions to be used sequentially in
  * training a SOM.  This allows a SOM to be trained with a large, linearly
  * decreasing neighborhood width initially and, afterwards, the width function
  * can change to a exponentially decreasing function.  Any number of
  * combinations should be possible.
  *
  * Using neighborhood width functions in a CompoundNeighborhood is an exception
  * to the instructions given in the NeighborhoodWidthFunction interface
  * documentation:
  *
  * The NeighborhoodWidthFunction object should have its expected iterations
  * property set before it is added to a CompoundNeighborhood object.
  *
  * Once a NeighborhoodWidthFunction has been added to a CompoundNeighborhood
  * object, its ownership is passed to the CompoundNeighborhood object ad should
  * not be modified by the user afterward.
  */
 public class CompoundNeighborhood implements NeighborhoodWidthFunction {
     private int nextTransition = -1;
     private int expectedIterations = 0;
 
     private NeighborhoodWidthFunction currentFunction = null;
     private final Map<Integer, NeighborhoodWidthFunction> widthFunctions
             = new TreeMap<Integer, NeighborhoodWidthFunction>();
 
    public CompoundNeighborhood() {
    }

     public CompoundNeighborhood(NeighborhoodWidthFunction initialWidthFuncton) {
         widthFunctions.put(0, initialWidthFuncton);
         currentFunction = initialWidthFuncton;
     }
 
     public void setExpectedIterations(int expectedIterations) {
         if (nextTransition == -1) {
             nextTransition = expectedIterations;
             this.expectedIterations = expectedIterations;
         }
     }
 
     public double neighborhoodWidth(int iteration) {
         if (iteration == nextTransition) {
             shiftFunctions();
         }
         return currentFunction.neighborhoodWidth(iteration);
     }
 
     /**
      * Add a neighborhood function to be used after a specific number of
      * iterations.
      *
      * @param neighborhood The neighborhood function object to add.
      * @param startAt The iteration at which to use {@code neighborhood}.
      */
     public void addNeighborhood(NeighborhoodWidthFunction neighborhood, int startAt) {
         widthFunctions.put(startAt, neighborhood);
         nextTransition = findLowest(0);
     }
 
     /**
      * Find the next neighborhood width function shift it into the current
      * slot.
      */
     private void shiftFunctions() {
         // Don't try to shift if there's nothing left to use.
         if (widthFunctions.isEmpty()) {
             return;
         }
 
         // Find the next lowest transition point
         int low = findLowest(nextTransition);
 
         // Move the next function into place.
         currentFunction = widthFunctions.get(low);
         nextTransition = low;
     }
 
     /**
      * Find the child neighborhood function with the next lowest starting
      * point.
      *
      * @param afterWhere The point after which to accept starting points.
      * @return The index into {@code widthFunctions} of the matching
      * neighborhood function.
      */
     private int findLowest(int afterWhere) {
         int match = expectedIterations;
         for (Integer i : widthFunctions.keySet()) {
             if (i < match && i > afterWhere) {
                 match = i;
             }
         }
         return match;
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder("CompoundNeighborhood begin");
         for (Map.Entry<Integer, NeighborhoodWidthFunction> next
                 : widthFunctions.entrySet()) {
             sb.append(String.format("%n    %d %s", next.getKey(), next.getValue()));
         }
         sb.append(String.format("%nend"));
 
         return sb.toString();
     }
 
     /**
      * Load a CompoundNeighborhood from a stream reader.
      *
      * @param reader The stream to read from.
      * @return A CompoundNeighborhood read from {@code reader}.
      * @throws IOException if an I/O error occurs.
      */
     public static NeighborhoodWidthFunction parse(BufferedReader reader)
             throws IOException {
         Pattern neighborhhodRegEx = Pattern.compile("\\s*(\\d*)\\s*(\\w*)\\s*(.*)");
         Pattern endLineRegEx = Pattern.compile("\\s*end.*", Pattern.CASE_INSENSITIVE);
 
         CompoundNeighborhood cnw = new CompoundNeighborhood();
         String line = reader.readLine();
         while (! endLineRegEx.matcher(line).matches()) {
             Matcher nwMatch = neighborhhodRegEx.matcher(line);
 
             if (!nwMatch.matches()) {
                 throw new SOMError("Bad input while parsing neighborhood "
                         + "functions: " + line);
             }
 
             NeighborhoodWidthFunction nw = (NeighborhoodWidthFunction)
                     Reflector.instantiateFromString("cs437.som.neighborhood",
                             nwMatch.group(2), nwMatch.group(3));
 
             int startsAt = Integer.parseInt(nwMatch.group(1));
             cnw.addNeighborhood(nw, startsAt);
             line = reader.readLine();
         }
 
         return cnw;
     }
 
     public CompoundNeighborhood(String parameters) {
         // todo IMPLEMENT!!!
         throw new UnsupportedOperationException(
                 "CompoundNeighborhood cannot be loaded from a file yet.");
     }
 }
