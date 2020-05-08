 
 /**
  *
  * @author Englisch (e1125164), Lenz (e1126963), Schuster (e1025700)
  * @since December 2012
  *
  */
 public abstract class Car extends Thread {
 	private Integer score;
 	private int speed; // sleep in ms
 	protected Map m;
 	protected Strategy strategy;
 	private boolean gameStopped;
 	private Orientation o;
 	private Direction d;
 	private String name;
 
 	protected int x = 0;
 	protected int y = 0;
 
 	public Car(Map m, String name, int speed, Strategy strategy) {
 		this.m = m;
 		this.strategy = strategy;
		this.speed = speed;
 		this.name = name;
 		gameStopped = false;
 		score = 0;
 	}
 	
 	public String getCarName() {
 		return this.name;
 	}
 
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	// actually due to the state that the car can only be in one field and this list is syncronized, score shouldnt be a problem
 	public void scoreUp() {
 		synchronized(score) {score++;}
 	}
 
 	public void scoreDown() {
 		synchronized(score) {score--;}
 	}
 
 	public void stopGame() {
 		gameStopped = true;
 	}
 
 	public Direction getDirection() {
 		return d;
 	}
 
 	public void setOrientation(Orientation o) {
 		this.o = o;
 	}
 
 	public Orientation getOrientation() {
 		synchronized(o) { return o; }
 	}
 
 	public void changeOrientation() { //TODO nur n�tig wenn randbehandlung != exception
 		synchronized(o) { 
 			if (this.o == Orientation.NORTH) {
 				this.o = Orientation.SOUTH;
 			} else if (this.o == Orientation.SOUTH) {
 				this.o = Orientation.NORTH;
 			} else if (this.o == Orientation.WEST) {
 				this.o = Orientation.EAST;
 			} else if (this.o == Orientation.EAST) {
 				this.o = Orientation.WEST;
 			}
 		}
 	}
 
 	public void drive() {
 		Field[][] field = m.getMap();
 		int tempX = this.x;
 		int tempY = this.y;
 
 		if (this.getOrientation() == Orientation.EAST) {
 			if (this.getDirection() == Direction.Forward) {
 				tempX += 1;
 			} else if (this.getDirection() == Direction.Left) {
 				tempY -= 1;
 			} else if (this.getDirection() == Direction.Right) {
 				tempY += 1;
 			} else if (this.getDirection() == Direction.LeftForward) {
 				tempY -= 1;
 				tempX += 1;
 			} else if (this.getDirection() == Direction.RightForward) {
 				tempY += 1;
 				tempX += 1;
 			}			
 		} else if (this.getOrientation() == Orientation.NORTH) {
 			if (this.getDirection() == Direction.Forward) {
 				tempY -= 1;
 			} else if (this.getDirection() == Direction.Left) {
 				tempX -= 1;
 			} else if (this.getDirection() == Direction.Right) {
 				tempX += 1;
 			} else if (this.getDirection() == Direction.LeftForward) {
 				tempX -= 1;
 				tempY += 1;
 			} else if (this.getDirection() == Direction.RightForward) {
 				tempX += 1;
 				tempY += 1;
 			}
 		} else if (this.getOrientation() == Orientation.WEST) {
 			if (this.getDirection() == Direction.Forward) {
 				tempX -= 1;
 			} else if (this.getDirection() == Direction.Left) {
 				tempY += 1;
 			} else if (this.getDirection() == Direction.Right) {
 				tempY -= 1;
 			} else if (this.getDirection() == Direction.LeftForward) {
 				tempX -= 1;
 				tempY += 1;
 			} else if (this.getDirection() == Direction.RightForward) {
 				tempX -= 1;
 				tempY -= 1;
 			}
 		} else if (this.getOrientation() == Orientation.SOUTH) {
 			if (this.getDirection() == Direction.Forward) {
 				tempY += 1;
 			} else if (this.getDirection() == Direction.Left) {
 				tempX += 1;
 			} else if (this.getDirection() == Direction.Right) {
 				tempX -= 1;
 			} else if (this.getDirection() == Direction.LeftForward) {
 				tempX += 1;
 				tempY += 1;
 			} else if (this.getDirection() == Direction.RightForward) {
 				tempX -= 1;
 				tempY += 1;
 			}
 		}
 		
 		// TODO REMOVE THIS HACK
 		tempX %= m.w;
 		tempY %= m.h;
 		if(tempX < 0)tempX = 0;
 		if(tempY < 0)tempY = 0;
 
 		if (tempX >= m.getW() || tempX <= 0 || tempY >= m.getH() || tempY <= 0) {
 			this.changeOrientation();
 		}
 		field[this.y][this.x].moveAway(this);
 		field[tempY][tempX].putCar(this);
 		Test.addToLog("Car " + this.getCarName() + " moved from x = " + this.x + ", y = " + this.y + " to x = " + tempX + ", y = " + tempY);		
 		this.setX(tempX);
 		this.setY(tempY);
 	}
 
 	public void update(int round) {
 		if (this instanceof FastCar) {
 			if (strategy.getDirectionFromStrategy(round) == Direction.Right) {
 				d = Direction.RightForward;
 			} else if (strategy.getDirectionFromStrategy(round) == Direction.Left) {
 				d = Direction.LeftForward;
 			}
 		} else if (this instanceof AgileCar) {
 			d = strategy.getDirectionFromStrategy(round);
 		}
 	}
 
 	@Override
 	public void run() {
 		int round = 0;
 		while(!gameStopped) {
 			Test.addToLog("Round " + round + ":");
 			update(round);
 			drive();
 
 			if(score >= 10 || round >= 150) {
 				m.stopGame();
 			}
 
 			try {
 				Thread.sleep(speed);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			round++;
 		}
 		System.out.println("Game has ended! SCORE: "+score +" ROUNDS: " + round);
 	}
 }
