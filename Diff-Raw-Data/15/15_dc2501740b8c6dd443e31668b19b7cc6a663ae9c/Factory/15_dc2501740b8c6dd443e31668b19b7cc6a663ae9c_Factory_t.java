 package simulation;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import simulation.globalforces.CenterOfMassForce;
 import simulation.globalforces.Gravity;
 import simulation.globalforces.ViscosityForce;
 import simulation.globalforces.wallrepulsionforces.BottomWallRepulsionForce;
 import simulation.globalforces.wallrepulsionforces.LeftWallRepulsionForce;
 import simulation.globalforces.wallrepulsionforces.RightWallRepulsionForce;
 import simulation.globalforces.wallrepulsionforces.TopWallRepulsionForce;
 import simulation.globalforces.wallrepulsionforces.WallRepulsionForce;
 import simulation.masses.FixedMass;
 import simulation.masses.Mass;
 import simulation.springs.Muscle;
 import simulation.springs.Spring;
 
 
 /**
  * The Factory class is responsible for interpreting the input files and constructing the
  * appropriate objects.
  * 
  * @author Robert C. Duvall
  * @author David Winegar
  * @author David Le
  */
 public class Factory {
     // data file keywords
     private static final String MASS_KEYWORD = "mass";
     private static final String SPRING_KEYWORD = "spring";
     private static final String MUSCLE_KEYWORD = "muscle";
     private static final String GRAVITY_KEYWORD = "gravity";
     private static final String VISCOSITY_KEYWORD = "viscosity";
     private static final String CENTERMASS_KEYWORD = "centermass";
     private static final String WALL_KEYWORD = "wall";
 
     // mass IDs
     private Map<Integer, Mass> myMasses = new HashMap<Integer, Mass>();
 
     /**
     * Creates an assembly based on file input and passes it to the model.
      */
     public void loadModel (Model model, File modelFile) {
        Assembly currentAssembly = new Assembly();
         try {
             Scanner input = new Scanner(modelFile);
             while (input.hasNext()) {
                 Scanner line = new Scanner(input.nextLine());
                 if (line.hasNext()) {
                     String type = line.next();
                     if (MASS_KEYWORD.equals(type)) {
                        currentAssembly.add(massCommand(line));
                     }
                     else if (SPRING_KEYWORD.equals(type)) {
                        currentAssembly.add(springCommand(line));
                     }
                     else if (MUSCLE_KEYWORD.equals(type)) {
                        currentAssembly.add(muscleCommand(line));
                     }
                 }
             }
             input.close();
         }
         catch (FileNotFoundException e) {
             // should not happen because File came from user selection
             e.printStackTrace();
         }
        model.add(currentAssembly);
     }
 
     /**
      * Loads environment based on the file input (environment.xsp).
      * 
      * @param model
      * @param modelFile
      */
     public void loadEnvironment (Model model, File modelFile) {
         try {
             Scanner input = new Scanner(modelFile);
             while (input.hasNext()) {
                 Scanner line = new Scanner(input.nextLine());
                 if (line.hasNext()) {
                     String type = line.next();
                     if (GRAVITY_KEYWORD.equals(type)) {
                         model.add(gravityCommand(line));
                     }
                     else if (VISCOSITY_KEYWORD.equals(type)) {
                         model.add(viscosityCommand(line));
                     }
                     else if (CENTERMASS_KEYWORD.equals(type)) {
                         model.add(centerMassCommand(line));
                     }
                     else if (WALL_KEYWORD.equals(type)) {
                         model.add(wallCommand(line));
                     }
                 }
             }
             input.close();
         }
         catch (FileNotFoundException e) {
             // should not happen because File came from user selection
             e.printStackTrace();
         }
     }
 
     // create mass from formatted data
     private Mass massCommand (Scanner line) {
         int id = line.nextInt();
         double x = line.nextDouble();
         double y = line.nextDouble();
         double mass = line.nextDouble();
         Mass result;
         if (mass > 0) {
             result = new Mass(x, y, mass);
         }
         else {
             result = new FixedMass(x, y, mass);
         }
         myMasses.put(id, result);
         return result;
     }
 
     // create spring from formatted data
     private Spring springCommand (Scanner line) {
         Mass m1 = myMasses.get(line.nextInt());
         Mass m2 = myMasses.get(line.nextInt());
         double restLength = line.nextDouble();
         double ks = line.nextDouble();
         return new Spring(m1, m2, restLength, ks);
     }
 
     // create muscle from formatted data
     private Muscle muscleCommand (Scanner line) {
         Mass m1 = myMasses.get(line.nextInt());
         Mass m2 = myMasses.get(line.nextInt());
         double restLength = line.nextDouble();
         double ks = line.nextDouble();
         double amplitude = line.nextDouble();
         return new Muscle(m1, m2, restLength, ks, amplitude);
     }
 
     // create gravity from formatted data
     private Gravity gravityCommand (Scanner line) {
         double angle = line.nextDouble();
         double magnitude = line.nextDouble();
         return new Gravity(angle, magnitude);
     }
 
     // create gravity from formatted data
     private ViscosityForce viscosityCommand (Scanner line) {
         double scale = line.nextDouble();
         return new ViscosityForce(scale);
     }
 
     // create gravity from formatted data
     private CenterOfMassForce centerMassCommand (Scanner line) {
         double magnitude = line.nextDouble();
         double exponent = line.nextDouble();
         return new CenterOfMassForce(magnitude, exponent);
     }
 
     // create gravity from formatted data
     private WallRepulsionForce wallCommand (Scanner line) {
         int id = line.nextInt();
         double magnitude = line.nextDouble();
         double exponent = line.nextDouble();
         WallRepulsionForce[] forces = { new TopWallRepulsionForce(id, magnitude, exponent),
                                        new RightWallRepulsionForce(id, magnitude, exponent),
                                        new BottomWallRepulsionForce(id, magnitude, exponent),
                                        new LeftWallRepulsionForce(id, magnitude, exponent)
         };
         return forces[id - 1];
     }
 }
