 import java.util.*;
 import org.newdawn.slick.*;
 
 public class Stack {
 	
 	int x;
 	int y;
 	int width;
 	int height;
 	
 	int num_boxes;
 	CommandBox box1;
 	int box1_x;
 	int box1_y;
 	CommandBox box2;
 	CommandBox box3;
 	CommandBox[] box_stack;
 	
 	public Stack(int x, int y, int num_boxes){
 		this.x = x;
 		this.y = y;
 		width = CommandBox.width;
 		height = 400;
 		this.num_boxes = num_boxes;
 		box_stack = new CommandBox[num_boxes];
 		
 		
 		
 	}
 
 	
 	public void render(Graphics g) {
 		g.setColor(Color.white);
 		for (int i = 0; i < num_boxes; i ++) {
 			g.drawRect(x, y+i*CommandBox.height, CommandBox.width, CommandBox.height);
 		}
 		
 	}
 	
 	public int getClosestSection(CommandBox cur_cb, int cb_x, int cb_y) {
 		double diff = Integer.MAX_VALUE;
 		double temp_diff;
 		int closest_section = 0;
 		for (int i = 0; i< num_boxes; i ++) {
 			temp_diff = Math.sqrt( Math.pow(cb_x - x, 2) + Math.pow(cb_y - (y + i*CommandBox.height),2) );
 			if (temp_diff < diff) {
 				closest_section = i;
 				diff = temp_diff;
 			}
 		}
 		
 		if (diff < CommandBox.width) {
 			CommandBox temp = box_stack[closest_section];
 			if (temp != null) {
 				temp.cur_x = temp.x;
 				temp.cur_y = temp.y;
 			}
 			box_stack[closest_section] =  cur_cb;
 			return closest_section;
 		} else {
 			return  -1;
 		}
 		
 		
 	}
 	                    
 }
