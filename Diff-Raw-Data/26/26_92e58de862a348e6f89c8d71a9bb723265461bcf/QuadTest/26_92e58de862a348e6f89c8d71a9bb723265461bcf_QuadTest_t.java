 /**
  * @author Joe Cumbo & Pratik Sampat
  * @since 10/2/2013
  * 
  *        This program will read the input.txt file and run calculations on the
  *        data.
  */
 
 public class QuadTest {
     private Quadratic quad1;
     private Quadratic quad2;
     private Quadratic quad3;
 
     public QuadTest() {
     }
 
     /**
      * Performs calculations on the stored quadratics.
      * 
      * @param scale
      *            The value to scale the quadratic by.
      * @param x
      *            The x value to use when evaluating the quadratic.
      */
     public void calculations(double scale, double x) {
         /*
          * Sidenote ~ We changed the default System.out OutputStream to a
          * PrintStream directed towards the output.txt file. This allows for a
          * more user friendly interface when working with this program. It also
          * results in less code that needs to be written. There is no output()
          * method because that is automatically handled by the PrintStream.
          */
         quad3 = quad1.clone();
         System.out.println("The first quadratic is:");
         System.out.println("\t" + quad1.toString());
         System.out.println("The values of the first quadratic expression with x = " + x + " is: " + quad1.evalExpression(x));
         System.out.println();
        System.out.println("The first quadratic with scaling R = " + scale + " is:");
        System.out.println("\t" + Quadratic.scale(quad1, scale).toString());
         System.out.println();
        switch(quad1.getRootNum()) {
        case 0:
            System.out.println("Number of roots: " + "There are no real roots.");
            break;
        case 3:
            System.out.println("Number of roots: " + "Infinity many roots.");
            break;
        default:
            System.out.println("Number of roots: " + quad1.getRootNum());
            break;
        }
         switch(quad1.getRootNum()) {
         case 2:
             System.out.println("\tValue of root 2: " + quad1.getRootTwo());
         case 1:
             System.out.println("\tValue of root 1: " + quad1.getRootOne());
         default:
             break;
         }
         System.out.println();
         System.out.println("The second quadratic is:");
         System.out.println("\t" + quad2.toString());
         System.out.println();
         System.out.println("The quadratic which is the sum of first and second quadratics is:");
         System.out.println("\t" + Quadratic.sum(quad1, quad2).toString());
         System.out.println();
         System.out.println("A clone of the first quadratic is:");
         System.out.println("\t" + quad3);
         System.out.println("The first quadratic and the clone " + (quad1.isAlias(quad3) ? "are" : "are not") + " aliases, but " + (quad1.equals(quad3) ? "are" : "are not") + " equal to each other.");
     }
 
     /**
      * This prints out a description of what the program will be doing.
      */
     public void intro() {
         System.out.println("This program will read each line of the input.txt file sequentially.");
         System.out.println("For each line it reads, it will parse the data then write its output to the output.txt file.");
         System.out.println("If any data is malformatted, it will warn the user and skip the line.");
     }
 
     /**
      * This method will parse the given line and then run calculations on the
      * data. If there is an error while reading the data, it will alert the user
      * and return without doing calculations.
      * 
      * @param line
      *            The line to extract the data from.
      */
     public void parse(String line) {
         String[] arguments = line.split(" ");
         double x = 0;
         double scale = 0;
 
         /*
          * Assert that there are a sufficient amount of arguments to parse.
          */
         if(arguments.length != 8) {
             System.out.println("Not enough data.");
             return;
         }
 
         /*
          * Parse the arguments. If an exception is thrown while parsing the
          * data, stop parsing, alert the user, and return.
          */
         try {
             quad1 = new Quadratic(Double.parseDouble(arguments[0]), Double.parseDouble(arguments[1]), Double.parseDouble(arguments[2]));
             x = Double.parseDouble(arguments[3]);
             scale = Double.parseDouble(arguments[4]);
             quad2 = new Quadratic(Double.parseDouble(arguments[5]), Double.parseDouble(arguments[6]), Double.parseDouble(arguments[7]));
         } catch(NullPointerException | NumberFormatException e) {
             System.out.println("Invalid data.");
             e.printStackTrace();
             return;
         }
 
         calculations(scale, x);
     }
 }
