 package jp.gr.java_conf.konkonlab.game_of_life.models;
 
 public class Cell {
 	public static final String AliveCell = "ALIVE";
 	public static final String DeadCell = "DEAD";
 
 	String status = DeadCell;
 	int group = 0;
 	
 	public Cell(String status) {
 		this.status = status;
 	}
 	
 	@Override
 	public String toString() {
 		return status;
 	}
 	
 	@Override
 	public boolean equals(Object object) {
 		Cell another = (Cell) object;
 		return status.equals(another.status);
 	}
 	
	public boolean isAlive()
	{
 		return status.equals(AliveCell);
 	}
 	
 	public void setGroup(int group) {
 		if( this.isAlive() ) {
 			this.group = group;
 		}
 	}
 	
 	public int getGroup() {
 		return group;
 	}
 
 	private Cell createAliveCell() {
 		return new Cell(AliveCell);
 	}
 
 	private Cell createDeadCell() {
 		return new Cell(DeadCell);
 	}
 	
 	public Cell createNextGeneration(int numOfLivingNeighbors) {
 		if( this.isAlive() ) {
 			if ( numOfLivingNeighbors <= 1 || numOfLivingNeighbors >= 4 ) {
 				return createDeadCell();
 			}
 			else {
 				return createAliveCell();
 			}
 		}
 		else {
 			if( numOfLivingNeighbors == 3 ) {
 				return createAliveCell();
 			}
 			else {
 				return createDeadCell();
 			}
 		}
 	}
 
 }
