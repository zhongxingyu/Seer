 package jp.gr.java_conf.konkonlab.game_of_life.models;
 
 public class Cell {
 	public static final String AliveCell = "ALIVE";
 	public static final String DeadCell = "DEAD";
 
 	String status = DeadCell;
 	int group = 0;
	int age = 0;
 	
	private Cell(String status) {
 		this.status = status;
 	}
 	
	private Cell(String status, int age) {
		this.status = status;
		this.age = age;
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
 	
 	public boolean isAlive() {
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
 
 	public int getAge() {
		return age;
 	}
 	
 	public static Cell createAliveCell() {
 		return new Cell(AliveCell);
 	}
 
 	public static Cell createDeadCell() {
 		return new Cell(DeadCell);
 	}

	private static Cell createAliveCell(int age) {
		return new Cell(AliveCell, age);
	}
 	
 	public Cell createNextGeneration(int numOfLivingNeighbors) {
 		if( this.isAlive() ) {
 			if ( numOfLivingNeighbors <= 1 || numOfLivingNeighbors >= 4 ) {
 				return createDeadCell();
 			}
 			else {
				return createAliveCell(age + 1);
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
