 import java.util.ArrayList;
 import java.util.List;
 
 
 public class Ant {
 
 	private GroundCell gc;
 	private Colony col;
 	private Direction dir;
 	private  boolean food;
 	private List<Position> visited;
 	private boolean moved;
 
 	public Ant(Colony col, GroundCell gc){
 		this.gc = gc;
 		this.col = col;
 		visited=new ArrayList<Position>();
 		gc.addAnt(this);
 		moved=false;
 	}
 	
 	public List<Position> getVisited()
 	{
 		return visited;
 	}
 	
 	public GroundCell getLocation() {
 		return gc;
 	}
 
 	public void setLocation(GroundCell gc) {
 		this.gc = gc;
 	}
 
 	public Colony getCol() {
 		return col;
 	}
 	public void setCol(Colony col) {
 		this.col = col;
 	}
 	public Direction getDir() {
 		return dir;
 	}
 	public void setDir(Direction dir) {
 		this.dir = dir;
 	}
 
 	public void setFood(boolean food) {
 		this.food = food;
 	}
 
 	public boolean isCarryingFood(){
 		if (this.food == true){
 			return true;
 		}
 		return false;
 	}
 
 	public void moveDirection(){
 		Position p = gc.getPosition();
 		Ground ground = Ground.getInstance();
 		Position nextPosition = ground.findStrongestPheromone(this);
 		
 		int antX = p.getX();
 		int antY = p.getY();
 		
 		//Ant has food, is dropping it off
 		if (food && antX == col.getPosition().getX() && antY == col.getPosition().getY())
 		{
 			food = false;
 			visited.clear();
 		}
 		visited.add(gc.getPosition());
 		if(food)
 		{
 			
 			gc.addPheromone(new Pheromone(col, Ground.getInstance().getConfig().getPheromoneStrength(), gc));
 		}
 		
 		if(gc.getFoodPile()!=null&&(gc.getFoodPile().getFoodAmount()>0&&!food))
 		{
 			food=true;
 			gc.getFoodPile().decrementFood();
			gc.addPheromone(new Pheromone(col, Ground.getInstance().getConfig().getPheromoneStrength(), gc));
 		}
 		
 		gc.loseAnt(this);
 		
 		gc = ground.cellArray[nextPosition.getY()][nextPosition.getX()];
 		gc.addAnt(this);
 		
 		moved=true;
 		
 		System.out.println("Ant moved - new position = " + nextPosition.getY() + ", " + nextPosition.getX());
 	}
 	
 	boolean getMoved()
 	{
 		return moved;
 	}
 
 	void setMoved(boolean moved)
 	{
 		this.moved=moved;
 	}
 }
