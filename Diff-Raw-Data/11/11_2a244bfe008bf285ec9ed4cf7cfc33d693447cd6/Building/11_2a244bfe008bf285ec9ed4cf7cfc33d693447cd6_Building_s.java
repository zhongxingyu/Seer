 import java.util.ArrayList;
 import java.awt.Graphics;
 
 public class Building extends Item {
 
     private ArrayList obstacles;
 	private ArrayList birds;
 
 	public Building(int width, int height, int x, int y) {
 		setWidth(width);
 		setHeight(height);
 		setX(x);
 		setY(y);
 		this.obstacles = new ArrayList();
 		birds = new ArrayList();
 	}
 
 	public void move() {
 		setX(getX() - Board.getInstance().getSpeed());
 		moveObstacles();
 	}
 
 	public void paintComplete(Graphics g) {
 		this.paint(g);
 		this.paintObstacles(g);
 		this.paintBirds(g);
 	}
 
 	public ArrayList getObstacles() {
 		return obstacles;
 	}
 
 	public void spawnObstacles() {
 		int lastX = 0;
 		for(int i = 0; i <= Board.getRandom(0,2); ++i)
 		{
 			lastX += Board.getRandom(40,200);
 			if(lastX + 20 > getWidth()) break;
 			int currentX = lastX + (int) getX();
 			System.out.println(currentX);
 			Obstacle o = new Obstacle(20,20,currentX,(int) getY()-20);
 			this.obstacles.add(o);
 		}
 	}
 
 	public void moveObstacles() {
 		if(this.obstacles.size() == 0) return;
 		for(int i = 0; i < this.obstacles.size(); ++i) {
 			Obstacle o = (Obstacle) this.obstacles.get(i);
 			o.move();
 		}
 	}
 
 	public void paintObstacles(Graphics g) {
 		if(this.obstacles.size() == 0) return;
 		for(int i = 0; i < this.obstacles.size(); ++i) {
 			Obstacle o = (Obstacle) this.obstacles.get(i);
 			o.paint(g);
 		}
 	}
 	
 	public void spawnBirds() {
 		int quantity = Board.getRandom(10,50);
 		int lastX	 = 0;
 		
 		for(int i = 0; i <= 5; ++i) {
 				
			lastX += 2;
 			
 			if(lastX + 5 > getWidth()) break;
 			
			Bird b = new Bird(lastX, (int)getY()-5);
 			
 			birds.add(b);	
 		
 		
 		}
 	}
 	
 	
 	
 	
 	public void paintBirds(Graphics g) {
 		
		for(int i=0; i<=birds.size();++i) {
 			Bird b = (Bird) birds.get(i);
			g.drawImage(b.getImage(), b.getX(), b.getY(), Board.getInstance());
 		}	
 	
 	
 	}
 }
