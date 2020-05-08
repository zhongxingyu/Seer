 package graphics;
 
 import graphics.WavefrontObjLoader;
 
 import org.junit.Test;
 
 public class WavrfrontObjLoaderTest {
     public static final String MODEL_DIR = "assets/models/";
    public static final String[] FILES = {"cube_cube.obj", "cessna.obj", "shuttle.obj","bullet.obj","missile.obj","ship_test.obj"};
 
     @Test
     /* No output should be printed about unhandled tokens */
     public void testLoadString() {
         for(String file : FILES){
             graphics.Model model = WavefrontObjLoader.load(MODEL_DIR + file);
             System.out.println(model);
         }
     }
 }
