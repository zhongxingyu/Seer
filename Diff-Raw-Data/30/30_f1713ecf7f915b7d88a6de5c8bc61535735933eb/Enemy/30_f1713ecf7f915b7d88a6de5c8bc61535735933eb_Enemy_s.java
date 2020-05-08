 package creature;
 
 import java.util.LinkedList;
 
 import core.DefenseCore;
 
 import world.Field;
 
 public abstract class Enemy extends Thread{
 	protected int scaleField = 20;
 	int x;											//x position of the creature (NOT MATRIX POSITION)
 	int y;											//y position of the creature (NOT MATRIX POSITION)
 	private Field next;								//position 
 	private LinkedList<Field> path;					//path of the creature
 	private LinkedList<LinkedList<Field>> paths;	//List of next paths
 	private int speed;								//Speed of the object
 	private int spawnAt;							//Time to spawn, usefull for waves of mobs
 	private int moveCounter;						//Counter from start
 	private DefenseCore core;						//Need Defense core for cd with towers and projectiles
 	
 	public Enemy(int x, int y, LinkedList<Field> path, DefenseCore core) {
 		this.x = this.calcFromMatrix(x);
 		this.y = this.calcFromMatrix(y);
 		
 		this.path = path;
 		this.paths = new LinkedList<>();
 		
 		this.speed = 1;				//Normal speed
 		this.spawnAt = 1;			//Spawn at counter x
 		this.moveCounter = 0;
 		
 		this.core = core;
 	}
 	
 	public Enemy(int x, int y, LinkedList<Field> path, int spawnTimer, DefenseCore core) {
 		this.x = this.calcFromMatrix(x);
 		this.y = this.calcFromMatrix(y);
 		
 		this.path = path;
 		this.paths = new LinkedList<>();
 		
 		this.speed = 1;						//Normal speed
 		this.spawnAt = spawnTimer;			//Spawn at counter x
 		
 		this.core = core;
 	}
 	
 	public void run() {
 		while(!this.isInterrupted()) {
 			if(this.isInterrupted()) return;
 			
 			this.move();
 			
			if(this.reachedTarget()) core.removeEnemy(this.getAbsX(), this.getAbsY());
 			
 			try {
 				Thread.sleep(10);
 			}catch(Exception e) {}
 		}
 	}
 	
 	public int getMoveCounter() {
 		return this.moveCounter;
 	}
 	
 	public void addPath(LinkedList<Field> addPath) {
 		this.paths.addLast(addPath);
 	}
 	
 	public int getSpawnAt() {
 		return this.spawnAt;
 	}
 	
 	protected int calcFromMatrix(int value) {
 		return (value*this.scaleField)+(this.scaleField/2);
 	}
 	
 	protected void setSpeed(int speed) {
 		this.speed = speed;
 	}
 	
 	public int getX(){
 		return this.x/this.scaleField;
 	}
 
 	public int getAbsX() {
 		return this.x;
 	}
 	
 	public int getY(){
 		return this.y/this.scaleField;
 	}
 	
 	public int getAbsY() {
 		return this.y;
 	}
 	
 	public boolean equals(Enemy other) {
 		if(other == this) return true;
 		if(this.x == other.getAbsX() && this.y == other.getAbsY() && this.spawnAt == other.getSpawnAt()) return true;
 		return false;
 	}
 	
 	public boolean reachedTarget() {
 		int nextX = this.x;
 		int nextY = this.y;
 		if(this.next != null) {
 			nextX = calcFromMatrix(this.next.getX());
 			nextY = calcFromMatrix(this.next.getY());
 		}
 		
 		return (this.x == nextX && this.y == nextY) && this.paths.isEmpty() && this.path.isEmpty();
 	}
 	
 	public void move() {
 //		if(this.next != null) System.out.println("~~~");
 //		if(this.next != null) System.out.println("X: "+this.x+" == "+calcFromMatrix(this.next.getX()));
 //		if(this.next != null) System.out.println("Y: "+this.y+" == "+calcFromMatrix(this.next.getY()));		
 		int move = this.speed;
 		int nextX = this.x;
 		int nextY = this.y;
 		if(this.next != null) {
 			nextX = calcFromMatrix(this.next.getX());
 			nextY = calcFromMatrix(this.next.getY());
 		}
 		
 		if(this.next == null && this.path.isEmpty()) {
 			return;
 		}else if(this.next == null || (this.x == nextX && this.y == nextY)) {
 			if(this.path.isEmpty() && this.paths.isEmpty()){ 
 				return;
 			}else if(this.path.isEmpty() && !this.paths.isEmpty()){
 				this.path = this.paths.getFirst();
 				this.paths.removeFirst();
 			}
 			
 			this.next = this.path.getLast();
 			this.path.removeLast();
 //			System.out.println("NEXT");
 		}else if(this.x < nextX) {
 			this.x = (this.x - this.speed > nextX ? nextX : this.x + this.speed);
 			move = (this.x - this.speed > nextX ? nextX : this.speed);
 		}else if(this.x > nextX) {
 			this.x = (this.x - this.speed < nextX ? nextX : this.x - this.speed);
 			move = (this.x - this.speed < nextX ? nextX : this.speed);
 		}else if(this.y < nextY) {
 			this.y = (this.y - this.speed > nextY ? nextY : this.y + this.speed);
 			move = (this.y - this.speed > nextY ? nextY : this.speed);
 		}else if(this.y > nextY) {
 			this.y = (this.y - this.speed < nextY ? nextY : this.y - this.speed);
 			move = (this.y - this.speed < nextY ? nextY : this.speed);
 		}
 		
 		moveCounter += move;
 	}
 }
