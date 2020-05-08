 package s4app;
 
import java.util.Random;

 import org.apache.s4.core.App;
 import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;
 
 public class HelloApp extends App {
 
     @Override
     protected void start() {
         System.out.println("Hello App! I'm starting...");
     }
 
     @Override
     protected void init() {
         System.out.println("Hello App! I'm initing...");
 
        Random rand = new Random();
        DenseMatrix64F A = RandomMatrices.createSymmetric(20, -2, 3, rand);
        String result = Double.toString(CommonOps.det(A));
        System.out.println(">>> The result is: " + result);
     }
 
     @Override
     protected void close() {
         System.out.println("Hello App! I'm closing...");
 
     }
 }
