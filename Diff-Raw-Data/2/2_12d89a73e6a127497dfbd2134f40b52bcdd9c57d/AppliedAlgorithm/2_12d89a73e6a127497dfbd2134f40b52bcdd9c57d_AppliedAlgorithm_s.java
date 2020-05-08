 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.Scanner;
 
 public abstract class AppliedAlgorithm {
 
    public static final int TIME_LIMIT = Integer.MAX_VALUE;//60 * 1000;
     static Class[] ALGORITHMS = new Class[]{
             Surjections.class,
             Cribbage.class,
             Mayan.class,
             Schedule.class,
             Anagrams.class,
             Subvector.class,
             Cyclic.class,
             Semigroup.class,
             Pills.class,
             Image.class,
             Espanol.class,
     };
 
     public static void main(String[] args) {
         for (Class clazz : ALGORITHMS) {
             final String name = clazz.getName();
 
             final AppliedAlgorithm algorithm;
             try {
                 algorithm = (AppliedAlgorithm) clazz.newInstance();
             } catch (InstantiationException | IllegalAccessException e) {
                 System.out.println("Could not instantiate " + name + " for testing");
                 continue;
             }
 
             try (Scanner in = new Scanner(new FileInputStream(algorithm.getInFile()));
                  PrintStream out = new PrintStream(new FileOutputStream(algorithm.getOutFile()))) {
 
                 Thread t = new Thread() {
                     @Override
                     public void run() {
                         algorithm.execute(in, out);
                     }
                 };
                 t.start();
                 t.join(TIME_LIMIT);
                 if (t.isAlive()) {
                     t.interrupt();
                     throw new InterruptedException("time limit exceeded");
                 }
 
                 out.flush();
                 out.close();
 
                 System.out.println(name + ": " + (testOutput(algorithm) ? "success" : "failure"));
             } catch (FileNotFoundException | InterruptedException e) {
                 System.out.println(name + ": error: " + e.getMessage());
             }
         }
     }
 
     private static boolean testOutput(AppliedAlgorithm algorithm) {
 
         boolean success = true;
 
         try (Scanner out = new Scanner(new FileInputStream(algorithm.getOutFile()));
              Scanner expect = new Scanner(new FileInputStream(algorithm.getExpectedFile()))) {
             while (success && (out.hasNextLine() || expect.hasNextLine())) {
                 success = out.hasNextLine() && expect.hasNextLine() &&
                         out.nextLine().equals(expect.nextLine());
             }
         } catch (FileNotFoundException e) {
             success = false;
         }
         return success;
     }
 
     protected String getFileNameBase() {
         return getClass().getName().toLowerCase();
     }
 
     private String getInFile() {
         return getFileNameBase() + ".in";
     }
 
     private String getOutFile() {
         return getFileNameBase() + ".out";
     }
 
     private String getExpectedFile() {
         return getFileNameBase() + ".expected";
     }
 
     abstract void execute(Scanner in, PrintStream out);
 }
