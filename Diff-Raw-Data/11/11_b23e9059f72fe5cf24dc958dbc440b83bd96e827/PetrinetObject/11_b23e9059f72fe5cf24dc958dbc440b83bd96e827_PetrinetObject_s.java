 package petrinet.logic;
 
 import com.mxgraph.model.mxCell;
 
 public class PetrinetObject {
 	
 	final int PLACE_WIDTH = 80;
 	final int PLACE_HEIGHT = 30;
 	final int TRANSITION_WIDTH = 80;
 	final int TRANSITION_HEIGHT = 30;
 	final String PLACE_STYLE = "strokeColor=black;fillColor=gray";
 	final String TRANSITION_STYLE = "ROUNDED;strokeColor=green;fillColor=orange";
 	final String CAN_FIRE_STYLE = "ROUNDED;strokeColor=green;fillColor=green";
 
 	protected mxCell mxcell; 
 	
     private String name;
     
     public PetrinetObject(String name) {
         super();      	
         this.name = name;
         mxcell = new mxCell();
     }
     
     public mxCell getMxCell() {
     	return mxcell;
     }
     
     public void setMxCell(mxCell newCell) {
     	this.mxcell = newCell;
     }
     
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public String toString() {
         return /*getClass().getSimpleName() + " " + */ name;
     }
 }
