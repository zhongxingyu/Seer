 package cs437.som;
 
 /**
  * Neighborhood width strategy interface for self-organizing maps.
  *
  * Implementations of this interface are intended to define the width of a
  * neighborhood around a neuron for training nearby neurons.  Once an object
  * of an implementing class is given to an SOM, that SOM assumes control of
  * that object.  It should not be given to multiple SOMs or modified once it
  * has been handed to an SOM.
 * 
  */
 public interface NeighborhoodWidthFunction {
 
     /**
      * expectedIterations is a write-only property that allows the learning
      * rate object to scale its value based on the number of iterations the SOM
      * will be trained through.
      *
      * This property is written by the containing map when it is added, so it
      * does not need to be set by the user.  If it is set by the user, the map
      * will overwrite the value when it takes control of the object.
      *
      * @param expectedIterations The number of iterations the containing SOM
      * expects to run through.
      */
     void setExpectedIterations(int expectedIterations);
 
     /**
      * Return the neighborhood width for a given iteration.
      *
      * @param iteration The SOM's current iteration.
      * @return The neighborhood width the SOM should use for the current
      * iteration.
      */
     double neighborhoodWidth(int iteration);
 }
