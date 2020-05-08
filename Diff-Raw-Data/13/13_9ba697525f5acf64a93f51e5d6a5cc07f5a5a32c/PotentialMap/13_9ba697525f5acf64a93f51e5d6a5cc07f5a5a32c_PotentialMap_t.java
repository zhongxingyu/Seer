 /**
  * Name of programmer: ApolloH
  * Description: Potential Board module
  * Variable: potential - 2D array holding the potential values for the map
  */
 
 public class PotentialMap {
 	private int [][] potential = new int[MicromouseRun.BOARD_MAX][MicromouseRun.BOARD_MAX];
 
     /**
      * Default constructor
      */
 	public PotentialMap(){
 		initPotential();
 		potential[MicromouseRun.GOAL][MicromouseRun.GOAL] = 0;
 	}
 
     /**
      * Get the potential value for a specific cell
      * @param locx Specified x-coordinate
      * @param locy Specified y-coordinate
      * @return Value of a specified cell in the potential map
      */
 	public int getPotential(int locx, int locy){
 		return potential[locy][locx];
 	}
 
     /**
      * Updates the potential map based on the traversal map
      */ // TODO Fix potential map algorithm
 	public int updatePotential(int locx, int locy, TraversalMap traversal){
         int temp;
         int upBound = MicromouseRun.BOARD_MAX*MicromouseRun.BOARD_MAX+1;
        boolean foundFlag = false;
 
         initPotential();
 
         // Set goal cell
         potential[MicromouseRun.GOAL][MicromouseRun.GOAL] = 0;
 
         // Make the potential map
         for(int i=0; i<upBound-1; i++){
            foundFlag = false;
             // look for where the potential is i
             for(int j=0; j<MicromouseRun.BOARD_MAX; j++){ // y-coordinates
                 for(int k=0; k<MicromouseRun.BOARD_MAX; k++){ // x-coordinates
                     if( potential[j][k] == i ){
                        foundFlag = true;
                         temp = traversal.getTraversal(k, j);
                         System.out.format("(%d, %d): Potential traversal: %x\n", k, j, temp);
                         // look at north
                         // cond. true if there is no wall or we haven't travelled there yet
                         if( j > 0 ){
                             if(((temp & 0x01) != 0x01) && (potential[j-1][k] == upBound)){
                                 // set potential of cell in front to potential of current cell + 1                                      ;
                                 potential[j-1][k] = potential[j][k] + 1;
                             }
                         }
                         // look at east
                         if( k < MicromouseRun.BOARD_MAX-1 ){
                             if(((temp & 0x02) != 0x02) && (potential[j][k+1] == upBound)){
                                 potential[j][k+1] = potential[j][k] + 1;
                             }
                         }
                         // look at south
                         if( j < MicromouseRun.BOARD_MAX-1 ){
                             if(((temp & 0x04) != 0x04) && (potential[j+1][k] == upBound)){
                                 potential[j+1][k] = potential[j][k] + 1;
                             }
                         }
                         // look at west
                         if( k > 0 ){
                             if(((temp & 0x08) != 0x08) &&  (potential[j][k-1] == upBound)){
                                     potential[j][k-1] = potential[j][k] + 1;
                             }
                         }
                     }
                 }
             }
            if(!foundFlag){ break; }
         }
         return 0;
 	}
 
     /**
      * Resets the potential map to it's initial state
      */
 	public void resetPotential(){
 		initPotential();
 		potential[MicromouseRun.GOAL][MicromouseRun.GOAL] = 0;
 	}
 
     /**
      * Initializes the potential map
      */
     private void initPotential(){
         for(int i=0; i<MicromouseRun.BOARD_MAX; i++){
             for(int j=0; j<MicromouseRun.BOARD_MAX; j++){
                 potential[i][j] = MicromouseRun.BOARD_MAX * MicromouseRun.BOARD_MAX + 1;
             }
         }
     }
 }
