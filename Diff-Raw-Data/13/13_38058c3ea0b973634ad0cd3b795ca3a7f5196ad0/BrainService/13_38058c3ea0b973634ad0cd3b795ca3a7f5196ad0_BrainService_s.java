 package ch.lepeit.brain2;
 
 import java.util.Arrays;
 import java.util.List;
 
 import ch.lepeit.brain2.model.Stage;
 
 public class BrainService {
 
     public List<Stage> getAllStages() {
         return Arrays.asList(Stage.values()); 
     }
 
 }
