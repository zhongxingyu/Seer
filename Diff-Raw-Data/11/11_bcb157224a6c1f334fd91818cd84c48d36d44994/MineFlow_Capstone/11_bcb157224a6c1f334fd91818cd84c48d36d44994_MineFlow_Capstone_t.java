 import static java.lang.System.out;
 import java.util.concurrent.atomic.AtomicInteger;
 import processing.core.*;
 
 @SuppressWarnings("serial")
 public class MineFlow_Capstone extends PApplet{
 	final int THREADS = 3;
 	
 	final int MULTIPLIER = 24;
 	final int WID = 30 * MULTIPLIER;
 	final int HEI = 16 * MULTIPLIER;
 	final int MINES = (int) (99 * MULTIPLIER * MULTIPLIER);
 	
 	final double[] MINE_RATIOS = new double[] { 1 , .9 , 1.1 , 1 , 1 , 1.1 }; // the number of mines/thread is multiplied by one of these, in order, so each thread solves at a different rate
 	
 	final int DIM_AMOUNT = 0x3; // each pixel is dimmed by this amount each frame
 	
 	MinesweeperThread[] threads;
 	
 	AtomicInteger[] cell_color_array;
	@SuppressWarnings("deprecation")
 	final int S_WID = screen.width;
	@SuppressWarnings("deprecation")
 	final int S_HEI = screen.height;
 	float pixel_cell_ratio_width;
 	float pixel_cell_ratio_height;
 	
 	public static void main(String args[]) {
 	    PApplet.main(new String[] { "--present", "--bgcolor=#000000", "--hide-stop", "MineFlow_Capstone"});
 	    
 	    /*
 	     * uncomment the following line if you want it to run on two displays
 	     * make sure you go into MineFlow_Capstone_display2hack and switch the height/width
 	     * to exactly that of your second monitor
 	     * 
 	     * I use an application called ShiftWindow to shunt the new screen over to the other monitor, because processing's
 	     * --display=2 command was doing nothing
 	     *
 	     * ---------*
 	    	PApplet.main(new String[] { "--present", "--bgcolor=#000000", "--hide-stop", "MineFlow_Capstone_display2hack"});
 	     /* ---------
 	     */
 	}
 	
 	public void setup() {
 		size(S_WID,S_HEI,P2D);
 		
 		frameRate(24);
 		noCursor();
 		
 		/*
 		 * one AtomicInteger for every cell that minesweeper is played against
 		 * Each cell associates with a color, which is set by the minesweeper threads, and dimmed by the main thread
 		 * the main thread dims the cell, then copies it to the pixels[] array before updating the frame
 		 */
 		cell_color_array = new AtomicInteger[WID*HEI];
 		for(int i = 0; i<cell_color_array.length; i++){
 			cell_color_array[i] = new AtomicInteger(0x0);
 		}
 		
 		/*
 		 * create and start the minesweeper solving threads
 		 * each thread has the same width/height/mine count for the minesweeper board
 		 * and is given a reference to the cell_color_array
 		 * 
 		 * TODO: make each thread have a different number of mines, for cool behaviors
 		 */
 		
 		threads = new MinesweeperThread[THREADS];
 		for(int i = 0; i<threads.length; i++){
 			/*
 			 * each thread requires to be provided:
 			 * 	-	width/height of minesweeper board
 			 * 	-	width/height of the output screen
 			 * 	-	number of mines, which I multiply by a modifier so some threads go faster/slower than others
 			 * 	-	a reference to the cell_color_array, which each thread atomically writes to. Each cell of this array corresponds to a cell on the minesweeper board
 			 */
 			threads[i] = new MinesweeperThread(WID,HEI,S_WID,S_HEI,(int) (MINES * MINE_RATIOS[i % MINE_RATIOS.length]),cell_color_array);
 		}
 		
 		for(int i = 0; i<threads.length; i++){
 			threads[i].start();
 		}
 		
 		/*
 		 * computes the size the cell will be when drawn to the screen
 		 */
 		this.pixel_cell_ratio_width = ((float)S_WID)/WID; 
 		this.pixel_cell_ratio_height = ((float)S_HEI)/HEI;
 	}
 	
 	
 	public void draw(){
 		int current_pixel = 0;
 		// seperated_colors stores the red/blue/green components of each cell
 		int seperated_colors[] = new int[3];
 		if(frameCount == 1) loadPixels();
 		
 		/*
 		 * for each cell color,
 		 * it saves the current color (for compare and swap)
 		 * then separates the red, green, and blue components
 		 * it dims each component, splices them back together
 		 * then, if no minesweeper solver has touched the current cell it writes it's changes
 		 * if the CAS fails, it rewinds and tries again 
 		 */
 		for(int i = 0; i<cell_color_array.length; i++){
 			int expected_CAS;
 			do{
 				expected_CAS = cell_color_array[i].get();
 				current_pixel = expected_CAS;
 			
 				seperated_colors[0] = (current_pixel & 0x00ff0000) >> 16;  // red
 				seperated_colors[1] = (current_pixel & 0x0000ff00) >> 8;   // green
 				seperated_colors[2] = (current_pixel & 0x000000ff) >> 0;   // blue
 				
 				for(int color = 0; color < 3; color++){					   // this hopefully gets unrolled...
 			        if(seperated_colors[color] - DIM_AMOUNT > 0){
 			        	seperated_colors[color] -= DIM_AMOUNT;
 			        } else {
 			        	seperated_colors[color] = 0;
 			        }
 				}
 				current_pixel = (0xFF << 24) | (seperated_colors[0] << 16) | (seperated_colors[1] << 8) | seperated_colors[2];		//splice the colors back together
 			} while(!(cell_color_array[i].compareAndSet(expected_CAS, current_pixel)));
 			
 			/* 
 			 * computes the start and end screen-coordinates of the rectangle for the cell it's drawing
 			 * then writes it to the pixels[] array
 			 * because the cell_color_array is atomic, we are assured the drawing phase wont overwrite any changes made by the minesweeper solving threads
 			 * but the threads can conflict no problem, but that data loss isn't really a problem
 			 */
 			final int px = i % WID;
 			final int py = i / WID;
 			final int start_x = (int) (px * pixel_cell_ratio_width);
 			final int start_y = (int) (py * pixel_cell_ratio_height);
 			final int end_x =   (int) ((px+1) * pixel_cell_ratio_width);
 			final int end_y =   (int) ((py+1) * pixel_cell_ratio_height);
 			
 			for(int iy = start_y; iy <end_y; iy++){
 				for(int ix = start_x; ix <end_x; ix++){
 					pixels[iy * S_WID + ix] = current_pixel;
 				}
 			}
 		}
 		updatePixels();
 		
 		if(frameCount % 24 == 0) out.println(frameRate); // frame rate is printed at max every onc every second
 	}
 	
 	public void keyPressed(){
 		//if(frameCount>1) exit();
 	}
 	
 	public void mouseMoved(){
 		//if(frameCount>1) exit();
 	}
 	
 	public void mousePressed(){
 		//if(frameCount>1) exit();
 	}
 }
