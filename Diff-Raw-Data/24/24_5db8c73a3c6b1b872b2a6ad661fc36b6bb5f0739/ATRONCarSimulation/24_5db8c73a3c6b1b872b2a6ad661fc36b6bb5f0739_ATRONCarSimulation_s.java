 package ussr.samples.atron;
 
 import java.util.ArrayList;
 
 import ussr.model.Controller;
 import ussr.robotbuildingblocks.Robot;
 import ussr.robotbuildingblocks.VectorDescription;
 import ussr.robotbuildingblocks.WorldDescription;
 import ussr.robotbuildingblocks.WorldDescription.ModulePosition;
 
 public class ATRONCarSimulation extends GenericATRONSimulation {
 	
 	
 	public static void main( String[] args ) {
         new ATRONCarSimulation().main();
     }
 	
 	protected Robot getRobot() {
         return new ATRON() {
             public Controller createController() {
                 return new ATRONCarController1();
             }
         };
     }
 	private static ArrayList<ModulePosition> buildCar() {
     	float Yoffset = 0.25f;
     	ArrayList<ModulePosition> mPos = new ArrayList<ModulePosition>();
     	mPos.add(new WorldDescription.ModulePosition("driver", new VectorDescription(2*0*unit,0*unit-Yoffset,0*unit), rotation_EW));
    	mPos.add(new WorldDescription.ModulePosition("axes1", new VectorDescription(1*unit,-1*unit-Yoffset,0*unit), rotation_UD));
    	mPos.add(new WorldDescription.ModulePosition("axes2", new VectorDescription(-1*unit,-1*unit-Yoffset,0*unit), rotation_UD));
     	mPos.add(new WorldDescription.ModulePosition("wheel1", new VectorDescription(-1*unit,-2*unit-Yoffset,1*unit), rotation_SN));
     	mPos.add(new WorldDescription.ModulePosition("wheel2", new VectorDescription(-1*unit,-2*unit-Yoffset,-1*unit), rotation_NS));
     	mPos.add(new WorldDescription.ModulePosition("wheel3", new VectorDescription(1*unit,-2*unit-Yoffset,1*unit), rotation_SN));
     	mPos.add(new WorldDescription.ModulePosition("wheel4", new VectorDescription(1*unit,-2*unit-Yoffset,-1*unit), rotation_NS));
         return mPos;
 	}
 
 	protected ArrayList<ModulePosition> buildRobot() {
 		return buildCar();
 	}
 }
