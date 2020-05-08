 // Optimizations suggested by Foobar, see http://blog.cdleary.com/2012/06/simple-selfish-and-unscientific-shootout/#comment-547177202
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.util.Random;
 import java.io.IOException;
 import java.util.concurrent.ThreadLocalRandom;
 
 class VecGenOpt
 {
     static final int INT_MAX = 2147483647;
 
     public static void main(String args[]) {
         if (args.length != 1) {
             System.err.println("Usage: VecGen <elem_count>");
             System.exit(-1);
         }
 
         int count = Integer.parseInt(args[0]);
 
         try {
             FileWriter fw = new FileWriter("vec_gen.out");
             BufferedWriter bw = new BufferedWriter(fw);
 
             Random rng = ThreadLocalRandom.current();
 
             for (int i = 0; i < count; ++i) {
                 int r = rng.nextInt(INT_MAX);
                bw.write(Integer.toString(r));
                 bw.write("\n");
             }
 
             bw.close();
         } catch (IOException e) {
             System.err.println("Received I/O exception: " + e);
             System.exit(-2);
         }
     }
 };
