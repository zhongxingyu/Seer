 package jp.gr.java_conf.konkonlab.game_of_life.models;
 
 public class Cell {
 	public static final String AliveCell = "ALIVE";
 	public static final String DeadCell = "DEAD";
 
 	private static final int BIRTH_AGE = 0;
 	private static final int ADALT_AGE = 5;
 
 	String status = DeadCell;
 	int group = -1;
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
 		if (this.isAlive()) {
 			this.group = group;
 		}
 	}
 
 	public void decideGroup(int numOfLivingNeighbors) {
 		if (this.isAlive()) {
 			this.group = calcGroup(numOfLivingNeighbors);
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
 		if (this.isAlive()) {
 			if (willBeDeath(numOfLivingNeighbors)) {
 				return createDeadCell();
 			}
 			else {
 				return createAliveCell(age + 1);
 			}
 		}
 		else {
 			if (willBeBorn(numOfLivingNeighbors)) {
 				return createAliveCell();
 			}
 			else {
 				return createDeadCell();
 			}
 		}
 	}
 
 	private boolean willBeBorn(int numOfLivingNeighbors) {
 		return numOfLivingNeighbors == 3;
 	}
 
 	private boolean willBeDeath(int numOfLivingNeighbors) {
 		return numOfLivingNeighbors <= 1 || numOfLivingNeighbors >= 4;
 	}
 
 	private int calcGroup(int numOfLivingNeighbors) {
 		if (age == BIRTH_AGE) {
 			return 0;
 		}
 		else if (willBeDeath(numOfLivingNeighbors)) {
 			return 3;
 		}
 		else if (age >= ADALT_AGE) {
 			return 2;
 		}
 		else {
 			return 1;
 		}
 	}
 }
