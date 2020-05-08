 package fnug;
 
 import java.io.IOException;
 
import junit.framework.Assert;

 import org.junit.Test;
 
 import com.googlecode.jslint4java.JSLint;
 import com.googlecode.jslint4java.JSLintBuilder;
 
 public class JSLint4JavaThreadTest {
 
     private final static String JS = "var a = 'lint this'";
 
     @Test
     public void test() throws IOException, InterruptedException {
 
         final JSLint jsLint = (new JSLintBuilder()).fromDefault();
         final boolean[] failed = new boolean[1];
 
         jsLint.lint("foo1", JS);
 
         Thread t1 = new Thread() {
             @Override
             public void run() {
                 try {
                     jsLint.lint("foo2", JS);
                     failed[0] = false;
                 } catch (Exception e) {
                     e.printStackTrace();
                     failed[0] = true;
                 }
             }
         };
 
         t1.start();
         t1.join();
 
         // Assert.assertFalse("Threaded lint failed.", failed[0]);
 
     }
 
 }
