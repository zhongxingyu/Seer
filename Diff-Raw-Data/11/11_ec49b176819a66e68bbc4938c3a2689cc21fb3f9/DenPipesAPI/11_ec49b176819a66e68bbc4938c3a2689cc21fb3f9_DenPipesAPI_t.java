 package denoflionsx.DenPipes.API;
 
 import denoflionsx.DenPipes.API.Interfaces.IDenPipeAddon;
 import denoflionsx.DenPipes.API.Managers.DenPipesManager;
 import java.util.ArrayList;
 
 public class DenPipesAPI {
 
    public static final float normalPipeSpeed = 0.01F;
    
     // Register addons here. Internal addons are registered automatically.
     public static final ArrayList<IDenPipeAddon> addons = new ArrayList();
     // Do not interact with anything inside the manager until after preinit.
     public static final DenPipesManager manager = new DenPipesManager();
 }
