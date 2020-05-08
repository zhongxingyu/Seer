 package s4app;
 
 import org.apache.s4.core.App;
import org.ejml.data.D1Matrix64F;
 import org.ejml.data.DenseMatrix64F;
 
 public class HelloApp extends App {
 
     @Override
     protected void start() {
        D1Matrix64F tmpArray = new DenseMatrix64F(10, 1);
         System.out.println("Hello App! I'm starting...");
     }
 
     @Override
     protected void init() {
         System.out.println("Hello App! I'm initing...");
 
     }
 
     @Override
     protected void close() {
         System.out.println("Hello App! I'm closing...");
 
     }
 }
