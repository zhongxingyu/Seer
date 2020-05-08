 package org.illithid.cccp.world;
 
 import org.illithid.cccp.ActorList;
 import org.illithid.cccp.bestiary.Actor;
 
 public abstract class BaseLevel implements Level {
     public static ActorList               actors = new ActorList();
     
     final int X     = 80;
     final int Y     = 25;
 
     Cell[][]  cells = new Cell[X][Y];
 
     public BaseLevel() {
         for (int i = 0; i < cells.length; i++) {
             for (int j = 0; j < cells[i].length; j++) {
                 cells[i][j] = new Cell();
             }
             
         }
     }
 
     public Actor[] getActors(){
         return actors.getAll();
     }
     
     public void act(){
         for(Actor a : actors.getAll()){
             if(a!=null)
                 a.act();
         }
             
     }
     
     public int getXof(Actor a){
         for (int i = 0; i < cells.length; i++) {
             for (int j = 0; j < cells[i].length; j++) {
                 if(cells[i][j].isOccupiedBy(a))
                 	return i;
            }
            
         }
         return -1;
     }
 
     public void move(int distance, Direction d, Actor a){
     	int ax = getXof(a);
     	int ay = getYof(a);
     	
     	int tx = -1;
 		try {
 			tx = calcX(distance, d, ax);
 		} catch (UnsupportedDirectionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	int ty = -1;
 		try {
 			ty = calcY(distance, d, ay);
 		} catch (UnsupportedDirectionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	
     	if(cells[tx][ty].isWalkable()){
     		doMove(cells[ax][ay], cells[tx][ty]);
     	}
     }
     
     private void doMove(Cell from, Cell to) {
 		to.place(from.getOccupant());
 		from.removeOccupant();
 		
 	}
 
 	private int calcX(int dist, Direction d, int ax) throws UnsupportedDirectionException {
 		switch (d) {
 		case NORTH:
 		case SOUTH:
 			return ax;
 		case NORTHEAST:
 		case EAST:
 		case SOUTHEAST:
 			return ax+dist;
 		case NORTHWEST:
 		case WEST:
 		case SOUTHWEST:
 			return ax-dist;
 		default:
 			throw new UnsupportedDirectionException();
 		}
 		
 	}
 
     private int calcY(int dist, Direction d, int ay) throws UnsupportedDirectionException {
 		switch (d) {
 		case WEST:
 		case EAST:
 			return ay;
 		case NORTHEAST:
 		case NORTH:
 		case NORTHWEST:
 			return ay-dist;
 		case SOUTHEAST:
 		case SOUTH:
 		case SOUTHWEST:
 			return ay+dist;
 		default:
 			throw new UnsupportedDirectionException();
 		}
 		
 	}
     
 	public int getYof(Actor a){
         for (int i = 0; i < cells.length; i++) {
             for (int j = 0; j < cells[i].length; j++) {
                 if(cells[i][j].isOccupiedBy(a))
                 	return j;
             }       
         }
         return -1;
     }
     
     public void add(Actor a){
         actors.add(a);
         place(a);
     }
     
     public int getX(){
         return X;
     }
     
     public int getY(){
         return Y;
     }
     
     public void add(Actor[] all) {
         for (Actor a : all)
             add(a);
     }
 }
