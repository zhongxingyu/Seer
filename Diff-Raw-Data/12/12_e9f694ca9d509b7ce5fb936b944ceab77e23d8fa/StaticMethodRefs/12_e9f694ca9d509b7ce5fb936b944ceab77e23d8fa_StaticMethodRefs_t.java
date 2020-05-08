 import org.junit.Test;
 
 import static junit.framework.Assert.*;
 
 public class StaticMethodRefs {
 
     @Test
     public void canRefByMethod(){
 
         MyExecute.isOn = false;
 
         Runnable task = MyExecute::go;
 
         assertFalse(MyExecute.isOn);
 
         task.run();
 
         assertTrue(MyExecute.isOn);
     }
 
     @Test
     public void canRefInline(){
 
         MyExecute.isOn = false;
 
         Runnable task = ()-> MyExecute.go();
 
         assertFalse(MyExecute.isOn);
 
         task.run();
 
         assertTrue(MyExecute.isOn);
     }
 
     @Test
     public void canRefAsMultiLineBlock(){
 
         MyExecute.isOn = false;
 
         Runnable task = ()-> {
             MyExecute.go();
         };
 
         assertFalse(MyExecute.isOn);
 
         task.run();
 
         assertTrue(MyExecute.isOn);
     }
 
     @Test
     public void canContsructWithMethodReference(){
         Runnable e = MyExecute::new;
 
         assertNotNull(e);
     }
 
     private static class MyExecute implements Runnable {
         private static boolean isOn;
         @Override
         public void run() {

        }

        public static void go() {
             isOn = true;
         }
     }
 
 
 
 }
