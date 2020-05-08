 package ussr.builder.constructionTools;
 
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 
 import ussr.builder.enumerations.MTRANStandardRotations;
 import ussr.builder.enumerations.SupportedModularRobots;
 import ussr.builder.helpers.BuilderHelper;
 import ussr.builder.helpers.ModuleMapEntryHelper;
 import ussr.builder.helpers.ModuleRotationMapEntryHelper;
 import ussr.description.geometry.VectorDescription;
 import ussr.model.Module;
 import ussr.physics.jme.JMEModuleComponent;
 import ussr.physics.jme.JMESimulation;
 import ussr.samples.mtran.MTRANSimulation;
 
 /**
  * The main responsibility of this class is to take care of construction functions for
  * construction of MTRAN modular robot's morphology. Here module consists of components. To be more precise
  * MTRAN module consists of three components: two half boxes-cylinders(blue and red)and one link(black).
  * Connectors are considered to be as parts of components. MTRAN module has 6 connectors, numbered as: 
  * 0,1,2,3,4 and 5. All methods are specific to MTRAN module design.
  * @author Konstantinas
  */
 public class MTRANConstructionTemplate extends ModularRobotConstructionTemplate {
 
 	/**
 	 * The numbers of connectors on the MTRAN module
 	 * Connectors Nr0 and 3 are special(simpler)cases than others, because they always are aligned with one of the coordinate axes 
 	 */
 	private final static int CONNECTORnr0 = 0, CONNECTORnr1 = 1,  CONNECTORnr2 = 2, CONNECTORnr3  = 3, CONNECTORnr4  = 4, CONNECTORnr5  = 5;
 
 	/**
 	 * The indexes of MTRAN module components 
 	 */
 	private final static int RED_BOX_INDEX =0, LINK_INDEX = 1, BLUE_BOX_INDEX =2;
 
 	/**
 	 * The length of the module. Used to move each component of new movable module along the selected module.
 	 * Particularly useful when moving the new movable module components along the axis which is alligned along the selected module.
 	 */
 	private final static  float MODULE_LENGHT_OFFSET =0.14f;
 
 	/**
 	 * The offset for moving link in between(middle) two half boxes-cylinders along the new movable module respectively to selected module.
 	 */
 	private final static float LINK_OFFSET_NR1 = 0.1028f;
 
 	/**
 	 * The offset for moving link in between(middle) two half boxes-cylinders across the new movable module respectively to selected module.
 	 */
 	private final static float LINK_OFFSET_NR2 = 0.0375f;
 
 	/**
 	 * The offset for aligning two half boxes-cylinders together (connected)
 	 */
	private final static float BOX_OFFSET_NR1 = 0.075f;
 
 	/**
 	 * The offset for aligning two half boxes-cylinders together (connected)
 	 */
	private final static float BOX_OFFSET_NR2 = 0.065f;	
 
 	/**
 	 * The array of objects containing information about MTRAN specific rotations.
 	 * The logic is: if rotation is "0RI1", then the rotation value is MTRANSimulation.ORI1 and opposite
 	 * to this rotation is MTRANSimulation.ORI1Y and MTRANSimulation.ORI1X is specific to opposite.
 	 * (Look the first entry in array beneath).
 	 */
 	private final static ModuleRotationMapEntryHelper[] MODULE_ROTATION_MAP =  {
 		new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI1.toString(),MTRANSimulation.ORI1,MTRANSimulation.ORI1Y,MTRANSimulation.ORI1X),
 		new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI1Y.toString(),MTRANSimulation.ORI1Y,MTRANSimulation.ORI1,MTRANSimulation.ORI1XY),
 		new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI1X.toString(),MTRANSimulation.ORI1X,MTRANSimulation.ORI1XY,MTRANSimulation.ORI1),
 		new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI1XY.toString(),MTRANSimulation.ORI1XY,MTRANSimulation.ORI1X,MTRANSimulation.ORI1Y),
 		new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI2.toString(),MTRANSimulation.ORI2,MTRANSimulation.ORI2Y,MTRANSimulation.ORI2X),
 		new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI2Y.toString(),MTRANSimulation.ORI2Y,MTRANSimulation.ORI2,MTRANSimulation.ORI2XY),
 		new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI2X.toString(),MTRANSimulation.ORI2X,MTRANSimulation.ORI2XY,MTRANSimulation.ORI2),
 		new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI2XY.toString(),MTRANSimulation.ORI2XY,MTRANSimulation.ORI2X,MTRANSimulation.ORI2Y),
 		new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI3.toString(),MTRANSimulation.ORI3,MTRANSimulation.ORI3Y,MTRANSimulation.ORI3X),
 		new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI3Y.toString(),MTRANSimulation.ORI3Y,MTRANSimulation.ORI3,MTRANSimulation.ORI3XY),
 		new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI3X.toString(),MTRANSimulation.ORI3X,MTRANSimulation.ORI3XY,MTRANSimulation.ORI3),
 		new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI3XY.toString(),MTRANSimulation.ORI3XY,MTRANSimulation.ORI3X,MTRANSimulation.ORI3Y)
 	};
 
 	/**
 	 * Internal component counter used to keep track which component of the module is considered.
 	 */
 	int componentCounter =-1;
 	
 	/**
 	 * Tolerance used to identify if component (module) already exists in interval of space.
 	 */
 	private final static float SEARCH_TOLERANCE = 0.06498f;
 	
 	
 	/**
 	 * Supports construction of MTRAN modular robot's morphology on the level of components.
 	 * @param simulation, the physical simulation.
 	 */
 	public MTRANConstructionTemplate(JMESimulation simulation) {
 		super(simulation);		
 	}
 
 	/**
 	 * Updates the array of objects containing the information about all available initial rotations 
 	 * of MTRAN modular robot module and rotations together with positions of newly added MTRAN module with respect to specific 
 	 * connector number on selected module and selected module itself.
 	 * This method is so-called "Primitive operation" for TEMPLATE method, called "moveModuleAccording(int connectorNr, Module selectedModule, Module newMovableModule)".	 
 	 * @param x, the amount of x coordinate of current position of MTRAN component.
 	 * @param y, the amount of y coordinate of current position of MTRAN component.
 	 * @param z, the amount of z coordinate of current position of MTRAN component. 
 	 */
 	public void updateModuleMap(float x, float y, float z){		
 
 		/*Different offsets along each of coordinate axes. This is done to get the position 
 		 * of newly added component of the module (movable module) with respect to selected one*/
 		float xMinusOffset = x - MODULE_LENGHT_OFFSET;
 		float xPlusOffset = x + MODULE_LENGHT_OFFSET;
 		float yMinusOffset = y - MODULE_LENGHT_OFFSET;
 		float yPlusOffset = y + MODULE_LENGHT_OFFSET;
 		float zPlusOffset = z + MODULE_LENGHT_OFFSET;
 		float zMinusOffset = z - MODULE_LENGHT_OFFSET;		
 
 		float xMinusLinkOffsetNr1 = x - LINK_OFFSET_NR1;
 		float xPlusLinkOffsetNr1 = x + LINK_OFFSET_NR1;
 		float yMinusLinkOffsetNr1 = y - LINK_OFFSET_NR1;
 		float yPlusLinkOffsetNr1 = y + LINK_OFFSET_NR1;
 		float zPlusLinkOffsetNr1 = z + LINK_OFFSET_NR1;
 		float zMinusLinkOffsetNr1  = z - LINK_OFFSET_NR1;		
 
 		float xMinusSecondLinkOffset = x - LINK_OFFSET_NR2;
 		float xPlusSecondLinkOffset = x + LINK_OFFSET_NR2;
 		float yMinusSecondLinkOffset = y - LINK_OFFSET_NR2;
 		float yPlusSecondLinkOffset = y + LINK_OFFSET_NR2;
 		float zPlusSecondLinkOffset = z + LINK_OFFSET_NR2;
 		float zMinusSecondLinkOffset  = z - LINK_OFFSET_NR2;		
 
 		float xMinusBoxOffsetNr1 = x - BOX_OFFSET_NR1;
 		float xPlusBoxOffsetNr1 = x + BOX_OFFSET_NR1;
 		float yMinusBoxOffsetNr1 = y - BOX_OFFSET_NR1;
 		float yPlusBoxOffsetNr1 = y + BOX_OFFSET_NR1;
 		float zPlusBoxOffsetNr1 = z + BOX_OFFSET_NR1;
 		float zMinusBoxOffsetNr1 = z - BOX_OFFSET_NR1;		
 
 		float xMinusBoxOffsetNr2 = x - BOX_OFFSET_NR2;
 		float xPlusBoxOffsetNr2 = x + BOX_OFFSET_NR2;
 		float yMinusBoxOffsetNr2 = y - BOX_OFFSET_NR2;
 		float yPlusBoxOffsetNr2 = y + BOX_OFFSET_NR2;
 		float zPlusBoxOffsetNr2 = z + BOX_OFFSET_NR2;
 		float zMinusBoxOffsetNr2 = z - BOX_OFFSET_NR2;
 
 		/*Array containing the data for adding the new movable component(module) with respect to selected module.
 		 * The format of the object is: (connector number on selected module, the rotation of selected module, 
 		 * the rotation of new movable module, the position of new movable module.  So the logic is, if connector
 		 * number is for example 0 and selected module rotation is MTRANSimulation.ORI2, then new module should
 		 * have rotation MTRANSimulation.ORI2 and the position of it will be new new Vector3f(xMinusOffset,y,z).
 		 * (Look the first entry in array)*/ 
 		ModuleMapEntryHelper[] moduleMap = {
 				/*ConnectorNr0*/
 				new ModuleMapEntryHelper(CONNECTORnr0,MTRANSimulation.ORI2,MTRANSimulation.ORI2,new Vector3f(xMinusOffset,y,z)),
 				new ModuleMapEntryHelper(CONNECTORnr0,MTRANSimulation.ORI2X,MTRANSimulation.ORI2X,new Vector3f(xMinusOffset,y,z)),
 				new ModuleMapEntryHelper(CONNECTORnr0,MTRANSimulation.ORI2Y,MTRANSimulation.ORI2Y,new Vector3f(xPlusOffset,y,z)),
 				new ModuleMapEntryHelper(CONNECTORnr0,MTRANSimulation.ORI2XY,MTRANSimulation.ORI2XY,new Vector3f(xPlusOffset,y,z)),
 				new ModuleMapEntryHelper(CONNECTORnr0,MTRANSimulation.ORI1,MTRANSimulation.ORI1,new Vector3f(x,yMinusOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr0,MTRANSimulation.ORI1X,MTRANSimulation.ORI1X,new Vector3f(x,yMinusOffset,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr0,MTRANSimulation.ORI1Y,MTRANSimulation.ORI1Y,new Vector3f(x,yPlusOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr0,MTRANSimulation.ORI1XY,MTRANSimulation.ORI1XY,new Vector3f(x,yPlusOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr0,MTRANSimulation.ORI3,MTRANSimulation.ORI3,new Vector3f(x,y,zPlusOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr0,MTRANSimulation.ORI3X,MTRANSimulation.ORI3X,new Vector3f(x,y,zPlusOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr0,MTRANSimulation.ORI3Y,MTRANSimulation.ORI3Y,new Vector3f(x,y,zMinusOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr0,MTRANSimulation.ORI3XY,MTRANSimulation.ORI3XY,new Vector3f(x,y,zMinusOffset)),
 
 				/* Here the logic is changing and it is, if connector number is for example 1 and  selected module rotation is MTRANSimulation.ORI2, then the
 				 * blueBox component of MTRAN module should have rotation MTRANSimulation.ORI3 and the position of it will be new Vector3f(xMinusBoxOffsetNr1,y,zPlusBoxOffsetNr2).
 				 * (Look the first entry in array).This is quite specific to MTRAN*/
 				/* ConnectorNr1. Components: 2 - blueBoxIndex, 1-linkIndex, 0 - redBoxIndex.*/
 				new ModuleMapEntryHelper(CONNECTORnr1,BLUE_BOX_INDEX,MTRANSimulation.ORI2,MTRANSimulation.ORI3,new Vector3f(xMinusBoxOffsetNr1,y,zPlusBoxOffsetNr2)),
 				new ModuleMapEntryHelper(CONNECTORnr1,LINK_INDEX,MTRANSimulation.ORI2,MTRANSimulation.ORI3,new Vector3f(xMinusSecondLinkOffset,y,zPlusLinkOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr1,RED_BOX_INDEX,MTRANSimulation.ORI2,MTRANSimulation.ORI3,new Vector3f(x,y,zPlusOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr1,BLUE_BOX_INDEX,MTRANSimulation.ORI2X,MTRANSimulation.ORI1XY,new Vector3f(xMinusBoxOffsetNr1,yPlusBoxOffsetNr2,z)),
 				new ModuleMapEntryHelper(CONNECTORnr1,LINK_INDEX,MTRANSimulation.ORI2X,MTRANSimulation.ORI1XY,new Vector3f(xMinusSecondLinkOffset,yPlusLinkOffsetNr1,z)),
 				new ModuleMapEntryHelper(CONNECTORnr1,RED_BOX_INDEX,MTRANSimulation.ORI2X,MTRANSimulation.ORI1XY,new Vector3f(x,yPlusOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr1,BLUE_BOX_INDEX,MTRANSimulation.ORI2Y,MTRANSimulation.ORI3Y,new Vector3f(xPlusBoxOffsetNr1,y,zMinusBoxOffsetNr2)),
 				new ModuleMapEntryHelper(CONNECTORnr1,LINK_INDEX,MTRANSimulation.ORI2Y,MTRANSimulation.ORI3Y,new Vector3f(xPlusSecondLinkOffset,y,zMinusLinkOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr1,RED_BOX_INDEX,MTRANSimulation.ORI2Y,MTRANSimulation.ORI3Y,new Vector3f(x,y,zMinusOffset)),				
 				new ModuleMapEntryHelper(CONNECTORnr1,BLUE_BOX_INDEX,MTRANSimulation.ORI2XY,MTRANSimulation.ORI1XY,new Vector3f(xPlusBoxOffsetNr1,yPlusBoxOffsetNr2,z)),
 				new ModuleMapEntryHelper(CONNECTORnr1,LINK_INDEX,MTRANSimulation.ORI2XY,MTRANSimulation.ORI1XY,new Vector3f(xPlusSecondLinkOffset,yPlusLinkOffsetNr1,z)),
 				new ModuleMapEntryHelper(CONNECTORnr1,RED_BOX_INDEX,MTRANSimulation.ORI2XY,MTRANSimulation.ORI1XY,new Vector3f(x,yPlusOffset,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr1,BLUE_BOX_INDEX,MTRANSimulation.ORI1,MTRANSimulation.ORI3X,new Vector3f(x,yMinusBoxOffsetNr1,zPlusBoxOffsetNr2)),
 				new ModuleMapEntryHelper(CONNECTORnr1,LINK_INDEX,MTRANSimulation.ORI1,MTRANSimulation.ORI3X,new Vector3f(x,yMinusSecondLinkOffset,zPlusLinkOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr1,RED_BOX_INDEX,MTRANSimulation.ORI1,MTRANSimulation.ORI3X,new Vector3f(x,y,zPlusOffset)),				
 				new ModuleMapEntryHelper(CONNECTORnr1,BLUE_BOX_INDEX,MTRANSimulation.ORI1X,MTRANSimulation.ORI2X,new Vector3f(xMinusBoxOffsetNr2,yMinusBoxOffsetNr1,z)),
 				new ModuleMapEntryHelper(CONNECTORnr1,LINK_INDEX,MTRANSimulation.ORI1X,MTRANSimulation.ORI2X,new Vector3f(xMinusLinkOffsetNr1,yMinusSecondLinkOffset,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr1,RED_BOX_INDEX,MTRANSimulation.ORI1X,MTRANSimulation.ORI2X,new Vector3f(xMinusOffset,y,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr1,BLUE_BOX_INDEX,MTRANSimulation.ORI1Y,MTRANSimulation.ORI3XY,new Vector3f(x,yPlusBoxOffsetNr1,zMinusBoxOffsetNr2)),
 				new ModuleMapEntryHelper(CONNECTORnr1,LINK_INDEX,MTRANSimulation.ORI1Y,MTRANSimulation.ORI3XY,new Vector3f(x,yPlusSecondLinkOffset,zMinusLinkOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr1,RED_BOX_INDEX,MTRANSimulation.ORI1Y,MTRANSimulation.ORI3XY,new Vector3f(x,y,zMinusOffset)),				
 				new ModuleMapEntryHelper(CONNECTORnr1,BLUE_BOX_INDEX,MTRANSimulation.ORI1XY,MTRANSimulation.ORI2X,new Vector3f(xMinusBoxOffsetNr2,yPlusBoxOffsetNr1,z)),
 				new ModuleMapEntryHelper(CONNECTORnr1,LINK_INDEX,MTRANSimulation.ORI1XY,MTRANSimulation.ORI2X,new Vector3f(xMinusLinkOffsetNr1,yPlusSecondLinkOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr1,RED_BOX_INDEX,MTRANSimulation.ORI1XY,MTRANSimulation.ORI2X,new Vector3f(xMinusOffset,y,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr1,BLUE_BOX_INDEX,MTRANSimulation.ORI3,MTRANSimulation.ORI2Y,new Vector3f(xPlusBoxOffsetNr2,y,zPlusBoxOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr1,LINK_INDEX,MTRANSimulation.ORI3,MTRANSimulation.ORI2Y,new Vector3f(xPlusLinkOffsetNr1,y,zPlusSecondLinkOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr1,RED_BOX_INDEX,MTRANSimulation.ORI3,MTRANSimulation.ORI2Y,new Vector3f(xPlusOffset,y,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr1,BLUE_BOX_INDEX,MTRANSimulation.ORI3X,MTRANSimulation.ORI1Y,new Vector3f(x,yPlusBoxOffsetNr2,zPlusBoxOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr1,LINK_INDEX,MTRANSimulation.ORI3X,MTRANSimulation.ORI1Y,new Vector3f(x,yPlusLinkOffsetNr1,zPlusSecondLinkOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr1,RED_BOX_INDEX,MTRANSimulation.ORI3X,MTRANSimulation.ORI1Y,new Vector3f(x,yPlusOffset,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr1,BLUE_BOX_INDEX,MTRANSimulation.ORI3Y,MTRANSimulation.ORI2,new Vector3f(xMinusBoxOffsetNr2,y,zMinusBoxOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr1,LINK_INDEX,MTRANSimulation.ORI3Y,MTRANSimulation.ORI2,new Vector3f(xMinusLinkOffsetNr1,y,zMinusSecondLinkOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr1,RED_BOX_INDEX,MTRANSimulation.ORI3Y,MTRANSimulation.ORI2,new Vector3f(xMinusOffset,y,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr1,BLUE_BOX_INDEX,MTRANSimulation.ORI3XY,MTRANSimulation.ORI1Y,new Vector3f(x,yPlusBoxOffsetNr2,zMinusBoxOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr1,LINK_INDEX,MTRANSimulation.ORI3XY,MTRANSimulation.ORI1Y,new Vector3f(x,yPlusLinkOffsetNr1,zMinusSecondLinkOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr1,RED_BOX_INDEX,MTRANSimulation.ORI3XY,MTRANSimulation.ORI1Y,new Vector3f(x,yPlusOffset,z)),
 				/*ConnectorNr2*/				
 				new ModuleMapEntryHelper(CONNECTORnr2,BLUE_BOX_INDEX,MTRANSimulation.ORI2,MTRANSimulation.ORI3Y,new Vector3f(xMinusBoxOffsetNr1,y,zMinusBoxOffsetNr2)),
 				new ModuleMapEntryHelper(CONNECTORnr2,LINK_INDEX,MTRANSimulation.ORI2,MTRANSimulation.ORI3Y,new Vector3f(xMinusSecondLinkOffset,y,zMinusLinkOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr2,RED_BOX_INDEX,MTRANSimulation.ORI2,MTRANSimulation.ORI3Y,new Vector3f(x,y,zMinusOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr2,BLUE_BOX_INDEX,MTRANSimulation.ORI2X,MTRANSimulation.ORI1X,new Vector3f(xMinusBoxOffsetNr1,yMinusBoxOffsetNr2,z)),
 				new ModuleMapEntryHelper(CONNECTORnr2,LINK_INDEX,MTRANSimulation.ORI2X,MTRANSimulation.ORI1X,new Vector3f(xMinusSecondLinkOffset,yMinusLinkOffsetNr1,z)),
 				new ModuleMapEntryHelper(CONNECTORnr2,RED_BOX_INDEX,MTRANSimulation.ORI2X,MTRANSimulation.ORI1X,new Vector3f(x,yMinusOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr2,BLUE_BOX_INDEX,MTRANSimulation.ORI2Y,MTRANSimulation.ORI3,new Vector3f(xPlusBoxOffsetNr1,y,zPlusBoxOffsetNr2)),
 				new ModuleMapEntryHelper(CONNECTORnr2,LINK_INDEX,MTRANSimulation.ORI2Y,MTRANSimulation.ORI3,new Vector3f(xPlusSecondLinkOffset,y,zPlusLinkOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr2,RED_BOX_INDEX,MTRANSimulation.ORI2Y,MTRANSimulation.ORI3,new Vector3f(x,y,zPlusOffset)),				
 				new ModuleMapEntryHelper(CONNECTORnr2,BLUE_BOX_INDEX,MTRANSimulation.ORI2XY,MTRANSimulation.ORI1X,new Vector3f(xPlusBoxOffsetNr1,yMinusBoxOffsetNr2,z)),
 				new ModuleMapEntryHelper(CONNECTORnr2,LINK_INDEX,MTRANSimulation.ORI2XY,MTRANSimulation.ORI1X,new Vector3f(xPlusSecondLinkOffset,yMinusLinkOffsetNr1,z)),
 				new ModuleMapEntryHelper(CONNECTORnr2,RED_BOX_INDEX,MTRANSimulation.ORI2XY,MTRANSimulation.ORI1X,new Vector3f(x,yMinusOffset,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr2,BLUE_BOX_INDEX,MTRANSimulation.ORI1,MTRANSimulation.ORI3XY,new Vector3f(x,yMinusBoxOffsetNr1,zMinusBoxOffsetNr2)),
 				new ModuleMapEntryHelper(CONNECTORnr2,LINK_INDEX,MTRANSimulation.ORI1,MTRANSimulation.ORI3XY,new Vector3f(x,yMinusSecondLinkOffset,zMinusLinkOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr2,RED_BOX_INDEX,MTRANSimulation.ORI1,MTRANSimulation.ORI3XY,new Vector3f(x,y,zMinusOffset)),				
 				new ModuleMapEntryHelper(CONNECTORnr2,BLUE_BOX_INDEX,MTRANSimulation.ORI1X,MTRANSimulation.ORI2XY,new Vector3f(xPlusBoxOffsetNr2,yMinusBoxOffsetNr1,z)),
 				new ModuleMapEntryHelper(CONNECTORnr2,LINK_INDEX,MTRANSimulation.ORI1X,MTRANSimulation.ORI2XY,new Vector3f(xPlusLinkOffsetNr1,yMinusSecondLinkOffset,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr2,RED_BOX_INDEX,MTRANSimulation.ORI1X,MTRANSimulation.ORI2XY,new Vector3f(xPlusOffset,y,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr2,BLUE_BOX_INDEX,MTRANSimulation.ORI1Y,MTRANSimulation.ORI3X,new Vector3f(x,yPlusBoxOffsetNr1,zPlusBoxOffsetNr2)),
 				new ModuleMapEntryHelper(CONNECTORnr2,LINK_INDEX,MTRANSimulation.ORI1Y,MTRANSimulation.ORI3X,new Vector3f(x,yPlusSecondLinkOffset,zPlusLinkOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr2,RED_BOX_INDEX,MTRANSimulation.ORI1Y,MTRANSimulation.ORI3X,new Vector3f(x,y,zPlusOffset)),				
 				new ModuleMapEntryHelper(CONNECTORnr2,BLUE_BOX_INDEX,MTRANSimulation.ORI1XY,MTRANSimulation.ORI2XY,new Vector3f(xPlusBoxOffsetNr2,yPlusBoxOffsetNr1,z)),
 				new ModuleMapEntryHelper(CONNECTORnr2,LINK_INDEX,MTRANSimulation.ORI1XY,MTRANSimulation.ORI2XY,new Vector3f(xPlusLinkOffsetNr1,yPlusSecondLinkOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr2,RED_BOX_INDEX,MTRANSimulation.ORI1XY,MTRANSimulation.ORI2XY,new Vector3f(xPlusOffset,y,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr2,BLUE_BOX_INDEX,MTRANSimulation.ORI3,MTRANSimulation.ORI2,new Vector3f(xMinusBoxOffsetNr2,y,zPlusBoxOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr2,LINK_INDEX,MTRANSimulation.ORI3,MTRANSimulation.ORI2,new Vector3f(xMinusLinkOffsetNr1,y,zPlusSecondLinkOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr2,RED_BOX_INDEX,MTRANSimulation.ORI3,MTRANSimulation.ORI2,new Vector3f(xMinusOffset,y,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr2,BLUE_BOX_INDEX,MTRANSimulation.ORI3X,MTRANSimulation.ORI1,new Vector3f(x,yMinusBoxOffsetNr2,zPlusBoxOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr2,LINK_INDEX,MTRANSimulation.ORI3X,MTRANSimulation.ORI1,new Vector3f(x,yMinusLinkOffsetNr1,zPlusSecondLinkOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr2,RED_BOX_INDEX,MTRANSimulation.ORI3X,MTRANSimulation.ORI1,new Vector3f(x,yMinusOffset,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr2,BLUE_BOX_INDEX,MTRANSimulation.ORI3Y,MTRANSimulation.ORI2Y,new Vector3f(xPlusBoxOffsetNr2,y,zMinusBoxOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr2,LINK_INDEX,MTRANSimulation.ORI3Y,MTRANSimulation.ORI2Y,new Vector3f(xPlusLinkOffsetNr1,y,zMinusSecondLinkOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr2,RED_BOX_INDEX,MTRANSimulation.ORI3Y,MTRANSimulation.ORI2Y,new Vector3f(xPlusOffset,y,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr2,BLUE_BOX_INDEX,MTRANSimulation.ORI3XY,MTRANSimulation.ORI1,new Vector3f(x,yMinusBoxOffsetNr2,zMinusBoxOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr2,LINK_INDEX,MTRANSimulation.ORI3XY,MTRANSimulation.ORI1,new Vector3f(x,yMinusLinkOffsetNr1,zMinusSecondLinkOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr2,RED_BOX_INDEX,MTRANSimulation.ORI3XY,MTRANSimulation.ORI1,new Vector3f(x,yMinusOffset,z)),				
 				/*ConnectorNr3*/				
 				new ModuleMapEntryHelper(CONNECTORnr3,MTRANSimulation.ORI2,MTRANSimulation.ORI2,new Vector3f(xPlusOffset,y,z)),
 				new ModuleMapEntryHelper(CONNECTORnr3,MTRANSimulation.ORI2X,MTRANSimulation.ORI2X,new Vector3f(xPlusOffset,y,z)),
 				new ModuleMapEntryHelper(CONNECTORnr3,MTRANSimulation.ORI2Y,MTRANSimulation.ORI2Y,new Vector3f(xMinusOffset,y,z)),
 				new ModuleMapEntryHelper(CONNECTORnr3,MTRANSimulation.ORI2XY,MTRANSimulation.ORI2XY,new Vector3f(xMinusOffset,y,z)),
 				new ModuleMapEntryHelper(CONNECTORnr3,MTRANSimulation.ORI1,MTRANSimulation.ORI1,new Vector3f(x,yPlusOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr3,MTRANSimulation.ORI1X,MTRANSimulation.ORI1X,new Vector3f(x,yPlusOffset,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr3,MTRANSimulation.ORI1Y,MTRANSimulation.ORI1Y,new Vector3f(x,yMinusOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr3,MTRANSimulation.ORI1XY,MTRANSimulation.ORI1XY,new Vector3f(x,yMinusOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr3,MTRANSimulation.ORI3,MTRANSimulation.ORI3,new Vector3f(x,y,zMinusOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr3,MTRANSimulation.ORI3X,MTRANSimulation.ORI3X,new Vector3f(x,y,zMinusOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr3,MTRANSimulation.ORI3Y,MTRANSimulation.ORI3Y,new Vector3f(x,y,zPlusOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr3,MTRANSimulation.ORI3XY,MTRANSimulation.ORI3XY,new Vector3f(x,y,zPlusOffset)),
 				/*Connector Nr4*/
 				new ModuleMapEntryHelper(CONNECTORnr4,BLUE_BOX_INDEX,MTRANSimulation.ORI2,MTRANSimulation.ORI3Y,new Vector3f(x,y,zPlusOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr4,LINK_INDEX,MTRANSimulation.ORI2,MTRANSimulation.ORI3Y,new Vector3f(xPlusSecondLinkOffset,y,zPlusLinkOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr4,RED_BOX_INDEX,MTRANSimulation.ORI2,MTRANSimulation.ORI3Y,new Vector3f(xPlusBoxOffsetNr1,y,zPlusBoxOffsetNr2)),
 				new ModuleMapEntryHelper(CONNECTORnr4,BLUE_BOX_INDEX,MTRANSimulation.ORI2X,MTRANSimulation.ORI1X,new Vector3f(x,yPlusOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr4,LINK_INDEX,MTRANSimulation.ORI2X,MTRANSimulation.ORI1X,new Vector3f(xPlusSecondLinkOffset,yPlusLinkOffsetNr1,z)),
 				new ModuleMapEntryHelper(CONNECTORnr4,RED_BOX_INDEX,MTRANSimulation.ORI2X,MTRANSimulation.ORI1X,new Vector3f(xPlusBoxOffsetNr1,yPlusBoxOffsetNr2,z)),
 				new ModuleMapEntryHelper(CONNECTORnr4,BLUE_BOX_INDEX,MTRANSimulation.ORI2Y,MTRANSimulation.ORI3,new Vector3f(x,y,zMinusOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr4,LINK_INDEX,MTRANSimulation.ORI2Y,MTRANSimulation.ORI3,new Vector3f(xMinusSecondLinkOffset,y,zMinusLinkOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr4,RED_BOX_INDEX,MTRANSimulation.ORI2Y,MTRANSimulation.ORI3,new Vector3f(xMinusBoxOffsetNr1,y,zMinusBoxOffsetNr2)),				
 				new ModuleMapEntryHelper(CONNECTORnr4,BLUE_BOX_INDEX,MTRANSimulation.ORI2XY,MTRANSimulation.ORI1X,new Vector3f(x,yPlusOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr4,LINK_INDEX,MTRANSimulation.ORI2XY,MTRANSimulation.ORI1X,new Vector3f(xMinusSecondLinkOffset,yPlusLinkOffsetNr1,z)),
 				new ModuleMapEntryHelper(CONNECTORnr4,RED_BOX_INDEX,MTRANSimulation.ORI2XY,MTRANSimulation.ORI1X,new Vector3f(xMinusBoxOffsetNr1,yPlusBoxOffsetNr2,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr4,BLUE_BOX_INDEX,MTRANSimulation.ORI1,MTRANSimulation.ORI3XY,new Vector3f(x,y,zPlusOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr4,LINK_INDEX,MTRANSimulation.ORI1,MTRANSimulation.ORI3XY,new Vector3f(x,yPlusSecondLinkOffset,zPlusLinkOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr4,RED_BOX_INDEX,MTRANSimulation.ORI1,MTRANSimulation.ORI3XY,new Vector3f(x,yPlusBoxOffsetNr1,zPlusBoxOffsetNr2)),				
 				new ModuleMapEntryHelper(CONNECTORnr4,BLUE_BOX_INDEX,MTRANSimulation.ORI1X,MTRANSimulation.ORI2XY,new Vector3f(xMinusOffset,y,z)),
 				new ModuleMapEntryHelper(CONNECTORnr4,LINK_INDEX,MTRANSimulation.ORI1X,MTRANSimulation.ORI2XY,new Vector3f(xMinusLinkOffsetNr1,yPlusSecondLinkOffset,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr4,RED_BOX_INDEX,MTRANSimulation.ORI1X,MTRANSimulation.ORI2XY,new Vector3f(xMinusBoxOffsetNr2,yPlusBoxOffsetNr1,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr4,BLUE_BOX_INDEX,MTRANSimulation.ORI1Y,MTRANSimulation.ORI3X,new Vector3f(x,y,zMinusOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr4,LINK_INDEX,MTRANSimulation.ORI1Y,MTRANSimulation.ORI3X,new Vector3f(x,yMinusSecondLinkOffset,zMinusLinkOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr4,RED_BOX_INDEX,MTRANSimulation.ORI1Y,MTRANSimulation.ORI3X,new Vector3f(x,yMinusBoxOffsetNr1,zMinusBoxOffsetNr2)),				
 				new ModuleMapEntryHelper(CONNECTORnr4,BLUE_BOX_INDEX,MTRANSimulation.ORI1XY,MTRANSimulation.ORI2XY,new Vector3f(xMinusOffset,y,z)),
 				new ModuleMapEntryHelper(CONNECTORnr4,LINK_INDEX,MTRANSimulation.ORI1XY,MTRANSimulation.ORI2XY,new Vector3f(xMinusLinkOffsetNr1,yMinusSecondLinkOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr4,RED_BOX_INDEX,MTRANSimulation.ORI1XY,MTRANSimulation.ORI2XY,new Vector3f(xMinusBoxOffsetNr2,yMinusBoxOffsetNr1,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr4,BLUE_BOX_INDEX,MTRANSimulation.ORI3,MTRANSimulation.ORI2,new Vector3f(xPlusOffset,y,z)),
 				new ModuleMapEntryHelper(CONNECTORnr4,LINK_INDEX,MTRANSimulation.ORI3,MTRANSimulation.ORI2,new Vector3f(xPlusLinkOffsetNr1,y,zMinusSecondLinkOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr4,RED_BOX_INDEX,MTRANSimulation.ORI3,MTRANSimulation.ORI2,new Vector3f(xPlusBoxOffsetNr2,y,zMinusBoxOffsetNr1)),				
 				new ModuleMapEntryHelper(CONNECTORnr4,BLUE_BOX_INDEX,MTRANSimulation.ORI3X,MTRANSimulation.ORI1,new Vector3f(x,yPlusOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr4,LINK_INDEX,MTRANSimulation.ORI3X,MTRANSimulation.ORI1,new Vector3f(x,yPlusLinkOffsetNr1,zMinusSecondLinkOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr4,RED_BOX_INDEX,MTRANSimulation.ORI3X,MTRANSimulation.ORI1,new Vector3f(x,yPlusBoxOffsetNr2,zMinusBoxOffsetNr1)),				
 				new ModuleMapEntryHelper(CONNECTORnr4,BLUE_BOX_INDEX,MTRANSimulation.ORI3Y,MTRANSimulation.ORI2Y,new Vector3f(xMinusOffset,y,z)),
 				new ModuleMapEntryHelper(CONNECTORnr4,LINK_INDEX,MTRANSimulation.ORI3Y,MTRANSimulation.ORI2Y,new Vector3f(xMinusLinkOffsetNr1,y,zPlusSecondLinkOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr4,RED_BOX_INDEX,MTRANSimulation.ORI3Y,MTRANSimulation.ORI2Y,new Vector3f(xMinusBoxOffsetNr2,y,zPlusBoxOffsetNr1)),				
 				new ModuleMapEntryHelper(CONNECTORnr4,BLUE_BOX_INDEX,MTRANSimulation.ORI3XY,MTRANSimulation.ORI1,new Vector3f(x,yPlusOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr4,LINK_INDEX,MTRANSimulation.ORI3XY,MTRANSimulation.ORI1,new Vector3f(x,yPlusLinkOffsetNr1,zPlusSecondLinkOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr4,RED_BOX_INDEX,MTRANSimulation.ORI3XY,MTRANSimulation.ORI1,new Vector3f(x,yPlusBoxOffsetNr2,zPlusBoxOffsetNr1)),				
 				/*ConnectorNr5*/
 				new ModuleMapEntryHelper(CONNECTORnr5,BLUE_BOX_INDEX,MTRANSimulation.ORI2,MTRANSimulation.ORI3,new Vector3f(x,y,zMinusOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr5,LINK_INDEX,MTRANSimulation.ORI2,MTRANSimulation.ORI3,new Vector3f(xPlusSecondLinkOffset,y,zMinusLinkOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr5,RED_BOX_INDEX,MTRANSimulation.ORI2,MTRANSimulation.ORI3,new Vector3f(xPlusBoxOffsetNr1,y,zMinusBoxOffsetNr2)),
 				new ModuleMapEntryHelper(CONNECTORnr5,BLUE_BOX_INDEX,MTRANSimulation.ORI2X,MTRANSimulation.ORI1XY,new Vector3f(x,yMinusOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr5,LINK_INDEX,MTRANSimulation.ORI2X,MTRANSimulation.ORI1XY,new Vector3f(xPlusSecondLinkOffset,yMinusLinkOffsetNr1,z)),
 				new ModuleMapEntryHelper(CONNECTORnr5,RED_BOX_INDEX,MTRANSimulation.ORI2X,MTRANSimulation.ORI1XY,new Vector3f(xPlusBoxOffsetNr1,yMinusBoxOffsetNr2,z)),
 				new ModuleMapEntryHelper(CONNECTORnr5,BLUE_BOX_INDEX,MTRANSimulation.ORI2Y,MTRANSimulation.ORI3Y,new Vector3f(x,y,zPlusOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr5,LINK_INDEX,MTRANSimulation.ORI2Y,MTRANSimulation.ORI3Y,new Vector3f(xMinusSecondLinkOffset,y,zPlusLinkOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr5,RED_BOX_INDEX,MTRANSimulation.ORI2Y,MTRANSimulation.ORI3Y,new Vector3f(xMinusBoxOffsetNr1,y,zPlusBoxOffsetNr2)),				
 				new ModuleMapEntryHelper(CONNECTORnr5,BLUE_BOX_INDEX,MTRANSimulation.ORI2XY,MTRANSimulation.ORI1XY,new Vector3f(x,yMinusOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr5,LINK_INDEX,MTRANSimulation.ORI2XY,MTRANSimulation.ORI1XY,new Vector3f(xMinusSecondLinkOffset,yMinusLinkOffsetNr1,z)),
 				new ModuleMapEntryHelper(CONNECTORnr5,RED_BOX_INDEX,MTRANSimulation.ORI2XY,MTRANSimulation.ORI1XY,new Vector3f(xMinusBoxOffsetNr1,yMinusBoxOffsetNr2,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr5,BLUE_BOX_INDEX,MTRANSimulation.ORI1,MTRANSimulation.ORI3X,new Vector3f(x,y,zMinusOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr5,LINK_INDEX,MTRANSimulation.ORI1,MTRANSimulation.ORI3X,new Vector3f(x,yPlusSecondLinkOffset,zMinusLinkOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr5,RED_BOX_INDEX,MTRANSimulation.ORI1,MTRANSimulation.ORI3X,new Vector3f(x,yPlusBoxOffsetNr1,zMinusBoxOffsetNr2)),				
 				new ModuleMapEntryHelper(CONNECTORnr5,BLUE_BOX_INDEX,MTRANSimulation.ORI1X,MTRANSimulation.ORI2X,new Vector3f(xPlusOffset,y,z)),
 				new ModuleMapEntryHelper(CONNECTORnr5,LINK_INDEX,MTRANSimulation.ORI1X,MTRANSimulation.ORI2X,new Vector3f(xPlusLinkOffsetNr1,yPlusSecondLinkOffset,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr5,RED_BOX_INDEX,MTRANSimulation.ORI1X,MTRANSimulation.ORI2X,new Vector3f(xPlusBoxOffsetNr2,yPlusBoxOffsetNr1,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr5,BLUE_BOX_INDEX,MTRANSimulation.ORI1Y,MTRANSimulation.ORI3XY,new Vector3f(x,y,zPlusOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr5,LINK_INDEX,MTRANSimulation.ORI1Y,MTRANSimulation.ORI3XY,new Vector3f(x,yMinusSecondLinkOffset,zPlusLinkOffsetNr1)),
 				new ModuleMapEntryHelper(CONNECTORnr5,RED_BOX_INDEX,MTRANSimulation.ORI1Y,MTRANSimulation.ORI3XY,new Vector3f(x,yMinusBoxOffsetNr1,zPlusBoxOffsetNr2)),				
 				new ModuleMapEntryHelper(CONNECTORnr5,BLUE_BOX_INDEX,MTRANSimulation.ORI1XY,MTRANSimulation.ORI2X,new Vector3f(xPlusOffset,y,z)),
 				new ModuleMapEntryHelper(CONNECTORnr5,LINK_INDEX,MTRANSimulation.ORI1XY,MTRANSimulation.ORI2X,new Vector3f(xPlusLinkOffsetNr1,yMinusSecondLinkOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr5,RED_BOX_INDEX,MTRANSimulation.ORI1XY,MTRANSimulation.ORI2X,new Vector3f(xPlusBoxOffsetNr2,yMinusBoxOffsetNr1,z)),				
 				new ModuleMapEntryHelper(CONNECTORnr5,BLUE_BOX_INDEX,MTRANSimulation.ORI3,MTRANSimulation.ORI2Y,new Vector3f(xMinusOffset,y,z)),
 				new ModuleMapEntryHelper(CONNECTORnr5,LINK_INDEX,MTRANSimulation.ORI3,MTRANSimulation.ORI2Y,new Vector3f(xMinusLinkOffsetNr1,y,zMinusSecondLinkOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr5,RED_BOX_INDEX,MTRANSimulation.ORI3,MTRANSimulation.ORI2Y,new Vector3f(xMinusBoxOffsetNr2,y,zMinusBoxOffsetNr1)),				
 				new ModuleMapEntryHelper(CONNECTORnr5,BLUE_BOX_INDEX,MTRANSimulation.ORI3X,MTRANSimulation.ORI1Y,new Vector3f(x,yMinusOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr5,LINK_INDEX,MTRANSimulation.ORI3X,MTRANSimulation.ORI1Y,new Vector3f(x,yMinusLinkOffsetNr1,zMinusSecondLinkOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr5,RED_BOX_INDEX,MTRANSimulation.ORI3X,MTRANSimulation.ORI1Y,new Vector3f(x,yMinusBoxOffsetNr2,zMinusBoxOffsetNr1)),				
 				new ModuleMapEntryHelper(CONNECTORnr5,BLUE_BOX_INDEX,MTRANSimulation.ORI3Y,MTRANSimulation.ORI2,new Vector3f(xPlusOffset,y,z)),
 				new ModuleMapEntryHelper(CONNECTORnr5,LINK_INDEX,MTRANSimulation.ORI3Y,MTRANSimulation.ORI2,new Vector3f(xPlusLinkOffsetNr1,y,zPlusSecondLinkOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr5,RED_BOX_INDEX,MTRANSimulation.ORI3Y,MTRANSimulation.ORI2,new Vector3f(xPlusBoxOffsetNr2,y,zPlusBoxOffsetNr1)),				
 				new ModuleMapEntryHelper(CONNECTORnr5,BLUE_BOX_INDEX,MTRANSimulation.ORI3XY,MTRANSimulation.ORI1Y,new Vector3f(x,yMinusOffset,z)),
 				new ModuleMapEntryHelper(CONNECTORnr5,LINK_INDEX,MTRANSimulation.ORI3XY,MTRANSimulation.ORI1Y,new Vector3f(x,yMinusLinkOffsetNr1,zPlusSecondLinkOffset)),
 				new ModuleMapEntryHelper(CONNECTORnr5,RED_BOX_INDEX,MTRANSimulation.ORI3XY,MTRANSimulation.ORI1Y,new Vector3f(x,yMinusBoxOffsetNr2,zPlusBoxOffsetNr1))				
 		};		
 		this.moduleMap = moduleMap;		
 	}		
 
 	/**
 	 * Moves newMovableModuleComponent of MTRAN according(respectively) to selected module preconditions,
 	 * like connector number, initial rotation of selected module component, and so on.
 	 * This method is so-called "Primitive operation" for TEMPLATE method,called "moveModuleAccording(int connectorNr, Module selectedModule, Module newMovableModule)". 
 	 * @param connectorNr, the connector number on selected MTRAN module.
 	 * @param selectedModule,  the MTRAN module selected in simulation environment.
 	 * @param movableModuleComponent, the new MTRAN module component to move respectively to selected one.
 	 * @param rotationQuatComponent, the rotation of current MTRAN component of selected module.	 
 	 */		
 	public void moveComponentAccording(int connectorNr,Module selectedModule, JMEModuleComponent movableModuleComponent,Quaternion rotationQuatComponent,boolean loopFlag) {
 		componentCounter++;	
 		/*Loop through and locate the object matching the description of current component(also selected module).*/
 		for (int i=0; i<moduleMap.length;i++){			
 			if (connectorNr ==CONNECTORnr0||connectorNr ==CONNECTORnr3){// connectors Nr 0 and 3 are special(simpler)cases than others, because they always are aligned with one of the coordinate axes 
 				if (moduleMap[i].getConnectorNr()==connectorNr && moduleMap[i].getInitialRotation().getRotation().equals(rotationQuatComponent)){
 					/*If component(module) already exists at current position, delete movableModuleComponent and newly added module.*/
 					if (componentExists(moduleMap[i].getNewPosition(), SEARCH_TOLERANCE)&&loopFlag== false){
 						BuilderHelper.deleteModule(movableModuleComponent.getModel());						
 					}else{/*move the component to new position with new rotation*/
 						moveModuleComponent(movableModuleComponent,moduleMap[i].getNewRotation(),moduleMap[i].getNewPosition());
 					}
 				}
 			}else if (moduleMap[i].getConnectorNr()==connectorNr &&moduleMap[i].getComponentNr()==componentCounter && moduleMap[i].getInitialRotation().getRotation().equals(rotationQuatComponent)){
 				/*If component(module) already exists at current position, delete movableModuleComponent and newly added module.*/
 				if (componentExists(moduleMap[i].getNewPosition(), SEARCH_TOLERANCE)&&loopFlag== false){					
 					BuilderHelper.deleteModule(movableModuleComponent.getModel());											
 				}else{/*move the component to new position with new rotation*/
 				moveModuleComponent(movableModuleComponent,moduleMap[i].getNewRotation(),moduleMap[i].getNewPosition());
 				}
 			}
 		} 
 		// reset the counter to -1 so this method can be run in a loop.
 		resetComponentCounter(componentCounter);		
 	}	
 
 	/**
 	 * Resets the counter to -1, so methods using it can be run in a loop and handle three MTRAN components (from 0 to 2).
 	 * @param componentCounter, the counter counting the number of components from 0 to 2.
 	 */
 	private void resetComponentCounter(int componentCounter){
 		if (componentCounter ==BLUE_BOX_INDEX){
 			this.componentCounter =-1;
 		}
 	}
 
 	/**
 	 * Rotates selected MTRAN module component according to its initial rotation with opposite rotation.
 	 * This method is so-called "Primitive operation" for TEMPLATE method, called "rotateModuleOpposite(Module selectedModule)". 	
 	 * @param selectedModuleComponent,the module component selected in simulation environment.
 	 * @param rotationQComponent,the rotation of selected component.	 
 	 */
 //	TODO MAYBE IS NOT USEFULL IN MTRAN CASE, BECAUSE THERE IS OFTEN THE CASE THAT THE SAME ACTIVE OR PASSIVE
 //	BOXES CONNECT WITH THE SAME TYPE OF THE BOX (CONFLICT). TOO MUCH FLEXIBILITY FOR THE USER.	
 	public void rotateComponentOpposite(JMEModuleComponent selectedModuleComponent,Quaternion  rotationQComponent) {
 		/*Locate matching rotation Quaternion in moduleRotationMap and rotate with opposite rotation Quaternion
 		 * from the same entry in  moduleRotationMap*/
 		for (int entry=0;entry<MODULE_ROTATION_MAP.length;entry++){
 			if (rotationQComponent.equals(MODULE_ROTATION_MAP[entry].getRotation().getRotation())){
 				rotateModuleComponent(selectedModuleComponent,MODULE_ROTATION_MAP[entry].getRotationOppositeValue().getRotation());
 			}
 		}
 		/*Swap the positions of red  and blue boxes composing MTRAN module. This is because
 		 * it is not enough to just rotate them */
 		swapComponentsPositions(selectedModuleComponent.getModel(),RED_BOX_INDEX,  BLUE_BOX_INDEX);		
 	}	
 
 	/**
 	 * Rotates selected MTRAN module component with standard rotations, passed as a string.
 	 * This method is so-called "Primitive Operation" for TEMPLATE method, called "rotateModuleSpecifically(Module selectedModule,String rotationName)". 
 	 * @param selectedModuleComponent,the module component selected in simulation environment.	 
 	 * @param rotationName,the name of standard(specific) rotation of the module.	 
 	 */	
 	public void rotateComponentSpecifically(JMEModuleComponent selectedModuleComponent, String rotationName) {
 		componentCounter++;	
 		/*link is never moved because it is always in the center, so we need to move all components respectively to it */
 		VectorDescription positionLink = selectedModuleComponent.getModel().getComponent(LINK_INDEX).getPosition();//red half-cylinder box		
 
 		/*Get x,y,z of link position.*/
 		float xLink = positionLink.getX();
 		float yLink = positionLink.getY();
 		float zLink = positionLink.getZ();
 
 		/*Update the array of objects storing information about all available specific rotations of MTRAN modules
 		 * and  and position of its components relative to the position of the component called link. */
 		ModuleRotationMapEntryHelper[] currentRotationAdditionalMap = updateModuleRotationAdditionalMap(xLink, yLink, zLink);
 
 		/*Find match and in case of link rotate it and in case of red and blue boxes move respectively to link*/
 		for (int entry=0;entry<currentRotationAdditionalMap.length;entry++){
 			if (rotationName.equals(currentRotationAdditionalMap[entry].getRotationName())){
 				if (currentRotationAdditionalMap[entry].getComponentNr()==componentCounter){//Red and Blue box
 					moveModuleComponent(selectedModuleComponent,currentRotationAdditionalMap[entry].getRotation(),currentRotationAdditionalMap[entry].getComponentNewPosition());
 				}else {//Link
 					rotateModuleComponent(selectedModuleComponent,currentRotationAdditionalMap[entry].getRotation().getRotation());
 				}
 			}
 		}
 		// reset the counter to -1 so this method can be run in a loop.
 		resetComponentCounter(componentCounter);
 	}
 
 
 	/**
 	 * Updates and returns the array of objects containing the information about all available specific
 	 * rotations of MTRAN module and position of its components relative to the position of the component
 	 * called link. 
 	 * @param x, the amount of x coordinate of current position of the link
 	 * @param y, the amount of y coordinate of current position of the link
 	 * @param z, the amount of z coordinate of current position of the link
 	 * @return moduleRotationAdditonalMap, updated array of objects.
 	 */
 	private  ModuleRotationMapEntryHelper[] updateModuleRotationAdditionalMap(float x, float y, float z){
 
 		/*Different offsets along each of coordinate axes. This is done to get the position 
 		 * of component of the module  with respect to the position of the link*/
 		float xMinusSecondLinkOffset = x - LINK_OFFSET_NR2;
 		float xPlusSecondLinkOffset = x + LINK_OFFSET_NR2;
 		float yMinusSecondLinkOffset = y - LINK_OFFSET_NR2;
 		float yPlusSecondLinkOffset = y + LINK_OFFSET_NR2;
 		float zPlusSecondLinkOffset = z + LINK_OFFSET_NR2;
 		float zMinusSecondLinkOffset  = z - LINK_OFFSET_NR2;	
 
 		/*Array containing the data for rearranging the components of MTRAN module to specific rotation
 		 * with respect to the position of the link*/ 
 		ModuleRotationMapEntryHelper[] moduleRotationAdditonalMap = {
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI1.toString(),RED_BOX_INDEX,MTRANSimulation.ORI1,new Vector3f(x,yMinusSecondLinkOffset,z)),
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI1.toString(),BLUE_BOX_INDEX,MTRANSimulation.ORI1,new Vector3f(x,yPlusSecondLinkOffset,z)),				
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI1X.toString(),RED_BOX_INDEX,MTRANSimulation.ORI1X,new Vector3f(x,yMinusSecondLinkOffset,z)),
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI1X.toString(),BLUE_BOX_INDEX,MTRANSimulation.ORI1X,new Vector3f(x,yPlusSecondLinkOffset,z)),				
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI1Y.toString(),RED_BOX_INDEX,MTRANSimulation.ORI1Y,new Vector3f(x,yPlusSecondLinkOffset,z)),
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI1Y.toString(),BLUE_BOX_INDEX,MTRANSimulation.ORI1Y,new Vector3f(x,yMinusSecondLinkOffset,z)),				
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI1XY.toString(),RED_BOX_INDEX,MTRANSimulation.ORI1XY,new Vector3f(x,yPlusSecondLinkOffset,z)),
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI1XY.toString(),BLUE_BOX_INDEX,MTRANSimulation.ORI1XY,new Vector3f(x,yMinusSecondLinkOffset,z)),				
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI2.toString(),RED_BOX_INDEX,MTRANSimulation.ORI2,new Vector3f(xMinusSecondLinkOffset,y,z)),
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI2.toString(),BLUE_BOX_INDEX,MTRANSimulation.ORI2,new Vector3f(xPlusSecondLinkOffset,y,z)),				
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI2X.toString(),RED_BOX_INDEX,MTRANSimulation.ORI2X,new Vector3f(xMinusSecondLinkOffset,y,z)),
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI2X.toString(),BLUE_BOX_INDEX,MTRANSimulation.ORI2X,new Vector3f(xPlusSecondLinkOffset,y,z)),				
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI2Y.toString(),RED_BOX_INDEX,MTRANSimulation.ORI2Y,new Vector3f(xPlusSecondLinkOffset,y,z)),
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI2Y.toString(),BLUE_BOX_INDEX,MTRANSimulation.ORI2Y,new Vector3f(xMinusSecondLinkOffset,y,z)),				
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI2XY.toString(),RED_BOX_INDEX,MTRANSimulation.ORI2XY,new Vector3f(xPlusSecondLinkOffset,y,z)),
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI2XY.toString(),BLUE_BOX_INDEX,MTRANSimulation.ORI2XY,new Vector3f(xMinusSecondLinkOffset,y,z)),				
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI3.toString(),RED_BOX_INDEX,MTRANSimulation.ORI3,new Vector3f(x,y,zPlusSecondLinkOffset)),
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI3.toString(),BLUE_BOX_INDEX,MTRANSimulation.ORI3,new Vector3f(x,y,zMinusSecondLinkOffset)),				
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI3X.toString(),RED_BOX_INDEX,MTRANSimulation.ORI3X,new Vector3f(x,y,zPlusSecondLinkOffset)),
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI3X.toString(),BLUE_BOX_INDEX,MTRANSimulation.ORI3X,new Vector3f(x,y,zMinusSecondLinkOffset)),				
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI3Y.toString(),RED_BOX_INDEX,MTRANSimulation.ORI3Y,new Vector3f(x,y,zMinusSecondLinkOffset)),
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI3Y.toString(),BLUE_BOX_INDEX,MTRANSimulation.ORI3Y,new Vector3f(x,y,zPlusSecondLinkOffset)),				
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI3XY.toString(),RED_BOX_INDEX,MTRANSimulation.ORI3XY,new Vector3f(x,y,zMinusSecondLinkOffset)),
 				new ModuleRotationMapEntryHelper(MTRANStandardRotations.ORI3XY.toString(),BLUE_BOX_INDEX,MTRANSimulation.ORI3XY,new Vector3f(x,y,zPlusSecondLinkOffset))
 		};		
 		return moduleRotationAdditonalMap;
 	}
 
 	/**
 	 * Additional method for implementing unique properties of MTRAN modular robot components. Like for example 
 	 * MTRAN has several more specific rotations, which are implemented to make the construction
 	 * more flexible.
 	 * This method is so-called "Primitive operation" for TEMPLATE method, called "variateModuleProperties(Module selectedModule)". 	
 	 * @param selectedModuleComponent,the module component selected in simulation environment.
 	 * @param rotationQComponent,the rotation of selected component.	 
 	 */	
 	public void variateComponentProperties(JMEModuleComponent selectedModuleComponent,Quaternion  rotationQComponent){
 		for (int entry=0;entry<MODULE_ROTATION_MAP.length;entry++){
 			if (rotationQComponent.equals(MODULE_ROTATION_MAP[entry].getRotation().getRotation())){
 				rotateModuleComponent(selectedModuleComponent,MODULE_ROTATION_MAP[entry].getRotationAroundAxisValue().getRotation());
 			}
 		}		
 	}
 
 	
 	/**
 	 * Returns array of objects containing information about supported specific rotations of modular robot.
 	 */
 	public ModuleRotationMapEntryHelper[] getMODULE_ROTATION_MAP() {
 		return MODULE_ROTATION_MAP;
 	}
 
 
 	/**
 	 * Returns the array of connector numbers of MTRAN modular robot.
 	 * @return, the array of connector numbers of MTRAN modular robot.
 	 */
 	public String[] getConnectors() {
 	   return SupportedModularRobots.MTRAN_CONNECTORS;
 	}	
 }
