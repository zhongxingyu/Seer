 package jUnit;
 
 import static org.junit.Assert.*;
 import generalNetwork.state.Profile;
import generalNetwork.state.State;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 
 import org.junit.Test;
 
 import com.google.gson.Gson;
 
 import dta_solver.Simulator;
 
 public class TestSimulation {
 
   static String parallel_path_network = "JUnitTests/parallelPath.json";
   static String parallel_path_data = "JUnitTests/parallelPathData.json";
 
   /**
    * @brief Save the simulation for the parallelPath network into file
    */
   public static void registerParallelPath() {
     Simulator simu = new Simulator(parallel_path_network, parallel_path_data,
         false);
    State state = simu.run(false);
 
     Gson gson = new Gson();
 
     for (int k = 0; k < simu.time_discretization.getNb_steps(); k++) {
       // Open the file
       Writer writer = null;
       try {
         writer = new FileWriter("JUnitTests/solutions/parallelPath" + k);
       } catch (IOException e) {
         e.printStackTrace();
       }
 
      gson.toJson(state.profiles[k], Profile.class, writer);
 
       // Close the file
       try {
         if (writer != null)
           writer.close();
       } catch (IOException e) {
         e.printStackTrace();
       }
     }
   }
 
   @Test
   public void testParallelPath() {
     Simulator simu = new Simulator(parallel_path_network, parallel_path_data,
         false);
    State state = simu.run(false);
 
     Gson gson = new Gson();
     Profile p;
     for (int k = 0; k < simu.time_discretization.getNb_steps(); k++) {
       Reader reader = null;
       try {
         reader = new FileReader("JUnitTests/solutions/parallelPath" + k);
       } catch (FileNotFoundException e) {
         e.printStackTrace();
       }
 
       p = gson.fromJson(reader, Profile.class);
 
      state.profiles[k].junction_info = null;
 
      System.out.println(gson.toJson(state.profiles[k], Profile.class));
       System.out.println(gson.toJson(p, Profile.class));
 
       assertTrue("The result for time step " + k
           + " is not the same as the registered solution",
          p.equals(state.profiles[k], 1e-6));
 
       try {
         if (reader != null)
           reader.close();
       } catch (IOException e) {
         e.printStackTrace();
       }
     }
   }
 
 }
