 public class Main {
     public static void main(String[] argv) {
        // Have to start the bundle somehow and reproduce the
        // classloader-error here:
        new Activator();
     }
 }
