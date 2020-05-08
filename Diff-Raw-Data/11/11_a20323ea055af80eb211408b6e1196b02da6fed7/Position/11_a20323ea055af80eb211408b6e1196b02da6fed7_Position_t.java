 import java.util.ArrayList;
 public class Position{
 
 	private int i;
 	private int j;
 	private static int gridWidth = 79;
 	private static int gridHeight = 24;
 
 	/* Pretty simple and self-explaining class.
 	*	Offers the tools for Position-management
 	*	
 	* TODO: 
 	*	ValidPosition needs to check based on the general constant
 	*
 	*/
 
 	public Position(int x, int y){
 		this.i = x;
 		this.j = y;
 	}
 
 	@Override
 	public boolean equals(Object other){
 	    if (other == null) return false;
     	if (other == this) return true;
 	    if (!(other instanceof Position))return false;
 	    Position another = (Position)other;
 	    if ((another.getX() == this.getX()) && (another.getY() == this.getY())) 
         return true;
 	    else return false;
 	}
 
 	// Only checks map limits (in general)
 	public boolean isValidPosition(){
 		if( 0 <= this.getX() && this.getX() <= gridWidth &&
 			0 <= this.getY() && this.getY() <= gridHeight ){
 			return true;
 		}
 		return false;
 	}
 
 	// Only checks for map limits(adapted for doors)
 	// We can't pick a border and the adjacent file/column because the doors
 	// 	will need corridors.
 	public boolean isValidPositionForDoor(){
 		if( 2 <= this.getX() && this.getX() <= gridWidth-2 &&
 			2 <= this.getY() && this.getY() <= gridHeight-2 ){
 			return true;
 		}
 		return false;
 	}
 
 	// Only checks for map limits(adapted for rooms)
 	// We can't pick a border, because the walls will be out of the grid.
 	public boolean isValidPositionForRoom(){
 		if( 1 <= this.getX() && this.getX() <= gridWidth-1  &&
 			1 <= this.getY() && this.getY() <= gridHeight-1
 			){
 			return true;
 		}
 		return false;
 	}
 
 	public ArrayList<Position> getFourNeighbours(){
 		ArrayList<Position> toReturn = new ArrayList<Position>();
 		toReturn.add(this.getPositionN());
 		toReturn.add(this.getPositionS());
 		toReturn.add(this.getPositionE());
 		toReturn.add(this.getPositionW());
 		return toReturn;
 	}
 
 	public ArrayList<Position> getEightNeighbours(){
 		ArrayList<Position> toReturn = new ArrayList<Position>();
 		toReturn.add(this.getPositionN());
 		toReturn.add(this.getPositionS());
 		toReturn.add(this.getPositionE());
 		toReturn.add(this.getPositionW());
 		toReturn.add(this.getPositionNE());
 		toReturn.add(this.getPositionNW());
 		toReturn.add(this.getPositionSE());
 		toReturn.add(this.getPositionSW());
 		return toReturn;
 	}
 
	public ArrayList<Position> getSquare(Position bR){
 		int i;
 		int ini;
 		int fin;
 		int j;
 		int numWalls=1;
		Position uL = this;
 		Position uR = new Position(bR.getX(), uL.getY());
 		Position bL = new Position(uL.getX(), bR.getY());
 		ArrayList<Position> toReturn = new ArrayList<Position>();
 		ini = uL.getPositionNW().getX();
 		fin = uR.getPositionNE().getX();
 		j = uL.getPositionNW().getY();
 		for(i = ini; i <= fin ; i++){
 			//System.out.println("Adding square "+numWalls+": "+i+", "+j);
 			numWalls++;
 			toReturn.add(new Position(i,j));
 		}
 		ini = uR.getPositionE().getY();
 		fin = bR.getPositionSE().getY();
 		j = uR.getPositionE().getX();
 		for(i = ini; i <= fin; i++){
 			//System.out.println("Adding square "+numWalls+": "+j+", "+i);
 			numWalls++;
 			toReturn.add(new Position(j,i));
 		}
 		ini = bR.getPositionS().getX();
 		fin = bL.getPositionSW().getX();
 		j = bR.getPositionS().getY();
 		for(i = ini; i >= fin; i--){
 			//System.out.println("Adding square "+numWalls+": "+i+", "+j);
 			numWalls++;
 			toReturn.add(new Position(i,j));
 		}
 		ini = bL.getPositionW().getY();
 		fin = uL.getPositionW().getY();
 		j = bL.getPositionW().getX();
 		for(i = ini; i >= fin; i--){
 			//System.out.println("Adding square "+numWalls+": "+j+", "+i);
 			numWalls++;
 			toReturn.add(new Position(j,i));
 		}
 		return toReturn;
 	}
 
 	public Position getPositionNW(){
 		Position p = new Position(this.getX()-1, this.getY()-1);
 		return p;
 	}
 
 	public Position getPositionNE(){
 		Position p = new Position(this.getX()+1, this.getY()-1);
 		return p;
 	}
 
 	public Position getPositionSW(){
 		Position p = new Position(this.getX()-1, this.getY()+1);
 		return p;
 	}
 
 	public Position getPositionSE(){
 		Position p = new Position(this.getX()+1, this.getY()+1);
 		return p;
 	}
 
 	public Position getPositionE(){
 		Position p = new Position(this.getX()+1, this.getY());
 		return p;
 	}	
 
 	public Position getPositionW(){
 		Position p = new Position(this.getX()-1, this.getY());
 		return p;
 	}
 	
 	public Position getPositionN(){
 		Position p = new Position(this.getX(), this.getY()-1);
 		return p;
 	}
 	
 	public Position getPositionS(){
 		Position p = new Position(this.getX(), this.getY()+1);
 		return p;
 	}
 
 	public int getX(){
 		return this.i;
 	}
 
 	public int getY(){
 		return this.j;
 	}
 
 	public void setX(int pos){
 		this.i=pos;
 	}
 
 	public void setY(int pos){
 		this.j=pos;
 	}
 
 }
