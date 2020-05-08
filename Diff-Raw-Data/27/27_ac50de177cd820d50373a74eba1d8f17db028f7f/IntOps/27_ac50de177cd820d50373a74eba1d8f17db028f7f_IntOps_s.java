 // Oliver Kullmann, 22.9.2009 (Swansea)
 
 public class IntOps {
   public static void main(String[] args) {
    if (args.length <= 1)
      System.out.println("ERROR[IntOps]: Two arguments are expected (namely two numbers).");
    else {
      try {
        final int a = Integer.parseInt(args[0]);
        final int b = Integer.parseInt(args[1]);
        final int p = a*b; // ATTENTION: overflow possible! (e.g., a=888888888, b=9)
        final int q = a/b;
        final int r = a%b;
        System.out.println(a + " * " + b + " = " + p);
        System.out.println(a + " / " + b + " = " + q);
        System.out.println(a + " % " + b + " = " + r);
        System.out.println(a + " = " + q + " * " + b + " + " + r);
      }
      catch (Exception e) {
        System.out.println("ERROR[IntOps]: The two parameters must be integers!");
      }
    }
   }
 }
