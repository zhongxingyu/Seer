 package editor;
 
 import org.lwjgl.opengl.GL11;
 
 public class Model<E extends Number> {
 	private CubicGrid<Block<E>> cubic_grid;
 	private int size;
 	private int scale; //3^scale == size of expanded pixel
 	
 	private static final double drawn_size = 0.1;
 	
 	public Model(int size){
 	  this.size = size;
 	  this.scale = 1;
 	}
 	
 	public void draw(){
 	  GL11.glPushMatrix();
   	  GL11.glBegin(GL11.GL_LINES);
   	    //Outer border
   	    GL11.glVertex2d(-drawn_size * size, -drawn_size * size);
   	    GL11.glVertex2d(-drawn_size * size, drawn_size * size);
   	    GL11.glVertex2d(-drawn_size * size, drawn_size * size);
   	    GL11.glVertex2d(drawn_size * size, drawn_size * size);
   	    GL11.glVertex2d(drawn_size * size, drawn_size * size);
   	    GL11.glVertex2d(drawn_size * size, -drawn_size * size);
   	    GL11.glVertex2d(drawn_size * size, -drawn_size * size);
   	    GL11.glVertex2d(-drawn_size * size, -drawn_size * size);
   	    
   	    
   	    double x_start = -drawn_size * size;
   	    double increment = (drawn_size*size*2);
   	    for(int i = (int) drawn_size*2; i < drawn_size*size*2; i+= drawn_size*2){
   	      //Vertical Divisions
   	      GL11.glVertex2d(x_start + drawn_size * i, -drawn_size * size);
   	      GL11.glVertex2d(x_start + drawn_size * i, drawn_size * size);
   	      
   	      //Horizontal Divisions
   	      GL11.glVertex2d(-drawn_size * size, x_start + drawn_size * i);
           GL11.glVertex2d( drawn_size * size, x_start + drawn_size * i);
   	    }
   	  GL11.glEnd();
 	  GL11.glPopMatrix();
 	  
 	  /*
 	  GL11.glBegin(GL11.GL_LINES);
 	  GL11.glVertex2i(-5, 0);
     GL11.glVertex2i(5, 0);
     GL11.glVertex2i(0, 5);
     GL11.glVertex2i(0, -5);
 	  GL11.glEnd();
 	  */
 	}
 	
 	/* Mutators */
 	public void setSize(int s){
 		CubicGrid<Block<E>> new_grid = new CubicGrid<Block<E>>(s);
 		for(int z=0;z<size;z++) {
 			for(int y=0;y<size;y++) {
 				for(int x=0;x<size;x++){
 					new_grid.set(x,y,z,cubic_grid.get(x,y,z));
 				}
 			}
 		}
 		cubic_grid = new_grid;
 	}
 	
 	public void increaseScale(int steps){
 		if( steps > 0){
 			CubicGrid<Block<E>> new_grid = new CubicGrid<Block<E>>((int)(size+(size*Math.pow(2,steps))));
 			int x_corner;
 			int y_corner;
 			int z_corner;
 			for(int z=0;z<size;z++) {
 				for(int y=0;y<size;y++) {
 					for(int x=0;x<size;x++){
 						x_corner = x*size;
 						y_corner = y*size;
 						z_corner = z*size;
 						//Set center
 						new_grid.set(x_corner,y_corner,z_corner,cubic_grid.get(x,y,z));
 						
 						
 						for(int i = 1; i < Math.pow(3, steps); i++){
 							for(int j = 1; j < Math.pow(3, steps); j++){
 								for(int k = 1; k < Math.pow(3, steps); k++){
 									new_grid.set(x_corner,y_corner,z_corner,cubic_grid.get(x,y,z));
 								}
 							}
 						}
 					}
 				}
 			}
 			cubic_grid = new_grid;
 			scale+=steps;
 		}
 	}
 	
 	public void decreaseScale(int steps){
 		if(steps > 0 && scale-steps > 0){
 			int new_size = (int)(size-(size*Math.pow(2,steps)));
 			CubicGrid<Block<E>> new_grid = new CubicGrid<Block<E>>(new_size);
 			int x_corner;
 			int y_corner;
 			int z_corner;
 			for(int z=0;z<new_size;z++) {
 				for(int y=0;y<new_size;y++) {
 					for(int x=0;x<new_size;x++){
 						x_corner = (x*new_size);
 						y_corner = (y*new_size);
 						z_corner = (z*new_size);
 						
 						//Set center
 						new_grid.set(x,y,z,averageCube(cubic_grid,x_corner,y_corner,z_corner,steps));		
 					}
 				}
 			}
 			cubic_grid = new_grid;
 			scale-=steps;
 		}
 	}
 	
 	public void setBlock(Coordinate<E> coor, Block<E> block){/*TODO*/}
 	
 	
 	/* Accessors */
 	public Block<E> getBlock(Coordinate<E> coor){
 		return null;
 	}
 	public int getScale(){
 		return scale;
 	}
 	public int getSize(){
 		return size;
 	}
 	
 	private Block<E> averageCube(CubicGrid<Block<E>> cubic_grid,int x_center,int y_center,int z_center,int steps){
 		x_center += (int)((Math.pow(3, steps)-1)/2);
 		y_center += (int)((Math.pow(3, steps)-1)/2);
 		z_center += (int)((Math.pow(3, steps)-1)/2);
 
 		return cubic_grid.get(x_center, y_center, z_center);
 	}
 }
