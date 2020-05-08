 package se.chalmers.TowerDefence;
 
 import java.util.Observable;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 	public abstract class AbstractMonster extends Observable {
 		private Image image;
 		private RoadSquare currentSquare;
 		private RoadSquare nextSquare;
 		private int currentSquareX;
 		private int currentSquareY;
 		private int nextSquareY;
 		private int nextSquareX;
 		private Road road;
 		private boolean alive = true;
 		private float x;
 		private float y;
 		private final float speed;
 		public AbstractMonster(Road road) throws SlickException {
 			this.road = road;
			speed = 0.005f;
 			currentSquare = road.getFirst();
 			currentSquareX = currentSquare.getX();
 			currentSquareY = currentSquare.getY();
 			x = currentSquareX;
 			y = currentSquareY;
 			nextSquare = road.getNext(currentSquare);
 			nextSquareX = nextSquare.getX();
 			nextSquareY = nextSquare.getY();
 //			System.out.println("AbstractMonster: nextSquare:  " + currentSquareX + "  " + nextSquareX);
 			image = new Image("res/monster.gif");
 		}
 		
 		public void move() {
 			//*
 			if(Math.abs((int)(x-nextSquareX)) == 0 && Math.abs((int)(y-nextSquareY)) == 0) {	
 				currentSquare = nextSquare;
 				nextSquare = road.getNext(currentSquare);
 				currentSquareX = currentSquare.getX();
 				currentSquareY = currentSquare.getY();
 				nextSquareX = nextSquare.getX();
 				nextSquareY = nextSquare.getY();
 			}else{
 				x = x + ((float)(nextSquareX - currentSquareX)*speed);
 				y = y + ((float)(nextSquareY - currentSquareY)*speed);
 			} 
 			//*/
 		}
 		
 		public void draw(){
 			image.draw(x, y);		
 		}
 		
 //		public void die() {
 //			alive = false;
 //			notifyObservers(alive);
 //		}
 		
 //		public boolean equals(Object o) {
 //			if(o == null || !o.getClass().equals(this.getClass())) {
 //				return false;
 //			}
 //		}
 		
 	}
 
