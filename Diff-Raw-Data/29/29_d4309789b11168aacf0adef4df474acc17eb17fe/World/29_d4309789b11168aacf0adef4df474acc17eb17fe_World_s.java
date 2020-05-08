 package project1;
 
 import project1.Rabbit;
 import uchicago.src.sim.space.Object2DGrid;
 import java.util.Vector;
 import java.awt.Point;
 
 public class World {
 	//to fix. Figure out if the grass energy is a parameter or a fix value
 	private static final int MaxGrassEnergy = 5;
 
 	private Object2DGrid grassSpace;
 	private Object2DGrid rabbitSpace;
 
 	public World(int xSize, int ySize){
 		//build a grid of xSize x ySize with objects Integer(0)
 		grassSpace = new Object2DGrid(xSize, ySize);
 		for(int i = 0; i < xSize; i++){
 			for(int j = 0; j < ySize; j++){
 				grassSpace.putObjectAt(i,j,new Integer(0));
 			}
 		}
 		rabbitSpace = new Object2DGrid(xSize, ySize);
 	}
 	
 	public void growGrass(int grassAmount){
 		//randomly grows grass in the grid (loops grassGrowthRate times)
 		for(int i = 0; i < grassAmount; i++){
 			int x = (int)(Math.random()*(grassSpace.getSizeX()));
 			int y = (int)(Math.random()*(grassSpace.getSizeY()));
 			int grassEnergy = (int)(Math.random()* MaxGrassEnergy);
 
 			grassSpace.putObjectAt(x,y,new Integer(grassEnergy));
 		}
 	}
 	
 	public int eatGrassAt(int x, int y){
 		//check if at x,y there is grass. returns the energy of grass and delete the grass (eats the grass),
 		//returns 0 otherwise
 		int i;
 		if(grassSpace.getObjectAt(x,y)!= null){
 			i = ((Integer)grassSpace.getObjectAt(x,y)).intValue();
 			grassSpace.putObjectAt(x, y, new Integer(0));
 		}
 		else{
 			i = 0;
 		}
 		return i;
 	}
 
 	public boolean checkIfRabbitOn(int x, int y) {
 		//check if there is a rabbit at cell x ,y. return True if occupied, false otherwise
 		boolean retVal = false;
 		if(rabbitSpace.getObjectAt(x, y) != null) retVal = true;
 		return retVal;
 	}
 	public void removeRabbitAt(int x, int y) {
 		//remove rabbit from cell (to be used when a rabbits move or if it dies)
 		rabbitSpace.putObjectAt(x, y, null);
 	}
 	
 	public boolean placeRabbit(Rabbit rabbit) {
 		boolean addedRabbit = false;
 		int nbrFields = rabbitSpace.getSizeX() * rabbitSpace.getSizeY();
 		int i = 0;
 		System.out.println("Number of fields: "+nbrFields);
 
 		Vector<Point> pointVector = new Vector<Point>();
 		
 		while(i < nbrFields && addedRabbit == false) {
 			int x = (int) (Math.random()*(rabbitSpace.getSizeX()));
 			int y = (int) (Math.random()*(rabbitSpace.getSizeY()));
 			Point point = new Point(x, y);
 			System.out.println("Point (x) "+point.x+" and (y) "+point.y);
 			
 			if(false == pointVector.contains(point)) {
 				System.out.println("Point is not in list");
 				if(false == checkIfRabbitOn(x, y)) {
 					System.out.println("Field is free");
 					rabbit.setPosXY(x, y);
 					checkRabbitIn(rabbit);
 					addedRabbit = true;
 				} else {
 					pointVector.add(point);
 				}
 			}
 		}
 		
 		return addedRabbit;
 	}
 	
 	
 	public void checkRabbitIn(Rabbit rabbit) {
 		int x = rabbit.getPositionX();
 		int y = rabbit.getPositionY();
 		
 		rabbitSpace.putObjectAt(x, y, rabbit);
 	}
 	
 	public int checkBoundryX(int x) {
		int max = rabbitSpace.getSizeX();
 		
 		if(x > max) {
 			return x - max;
 		} else if (x < 0) {
 			return x + max;
 		} else {
 			return x;
 		}
 	}
 	
 	public int checkBoundryY(int y) {
 		int max = rabbitSpace.getSizeY();
 		
 		if(y > max) {
 			return y - max;
 		} else if (y < 0) {
 			return y + max;
 		} else {
 			return y;
 		}
 	}
 	
 
 	public Object2DGrid getCurrentGrassSpace() {
 	    return grassSpace;
 	}
 }
