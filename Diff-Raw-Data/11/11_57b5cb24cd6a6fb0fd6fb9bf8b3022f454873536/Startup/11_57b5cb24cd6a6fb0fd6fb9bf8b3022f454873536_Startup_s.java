 package example3;
 
 /**
  * This is another poor example of encapsulation, but from a different
  * perspective. Here we have a class "Startup" that collaborates with two
  * other classes -- "Engine" and "Car". Think for a minute. Do most people
  * interact with their engines? No! We interact with our car and our car
  * interacts with the engine. For most people the details of how an engine
  * work are not of interest and beyond the comfort level of the average
  * car owner. Requiring communication with both the car and the engine just
  * complicates things. And when your code is complex, many bad things tend
  * to happen, including poor performance, difficult maintenance, and
  * a greater chance of introducing bugs in the code, however unintentional
  * they might be.
  *
  * Furthermore, now the "Startup" class is dependent on two classes -- "Car"
  * and "Engine". Dependencies are bad because they limit code reuse. The
  * more dependencies you have the worse things get. We don't need the "Engine"
  * object at all here (see example4).
  *
  * @author jlombardo
  */
 public class Startup {
 
     public static void main(String[] args) {
         // Why do we need to talk to the Engine? Overly complex!
         Engine engine = new Engine();
         // is this really necessary?
         engine.setCylinderCount(6);
         Car car = new Car();
        //car.setEngine(engine);
         // Is this what you do? Do you tell the engine to start? No!
         // You tell the car to start by turning a key.
        engine = car.getEngine();
         engine.start();
 
         // Again ... we're talking to the wrong object... should be car!
        System.out.println("Car running status: " + engine.isRunning());
        System.out.println("Engine Type: " + engine.getCylinderCount());
     }
 }
