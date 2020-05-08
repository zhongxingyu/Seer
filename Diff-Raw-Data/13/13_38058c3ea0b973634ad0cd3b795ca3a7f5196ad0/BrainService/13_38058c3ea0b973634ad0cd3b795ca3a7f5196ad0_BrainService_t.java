 package ch.lepeit.brain2;
 
 import java.util.Arrays;
 import java.util.List;
 
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import ch.lepeit.brain2.model.DeployArtefakt;
 import ch.lepeit.brain2.model.Stage;
 
 public class BrainService {
 
    @PersistenceContext
    private EntityManager em;
    
     public List<Stage> getAllStages() {
         return Arrays.asList(Stage.values()); 
     }
    
    public List<DeployArtefakt> getAllDepArts(){
        return null;
    }
 
 }
