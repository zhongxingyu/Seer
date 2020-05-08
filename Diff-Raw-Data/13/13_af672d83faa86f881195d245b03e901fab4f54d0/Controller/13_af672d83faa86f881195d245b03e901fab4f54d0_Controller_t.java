 import java.util.ArrayList;
 
 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.Rectangle;
 
 
 public class Controller{
 	Model model;
 	Input input;
 	CommandBox cur_command_box;
 	
 	public Controller(Model model) {
 		
 		this.model = model;
 		
 		
 	}
 	
 	// get input from user and update model
 	public void update(Input input) {
 		this.input = input;
 		
 		
 	}
 
 	public void mouseClicked(int arg0, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
     if (model.cur_level  == model.start_screen) {
 
   		model.cur_level = model.level1;
   		model.cur_error = "Done!";
   		model.cur_prog = Model.Progress.SUCCESS;
     }
 		
 		
 		// check for run
 		
 	}
 
 
 	public void mouseDragged(int arg0, int arg1, int new_x, int new_y) {
 		// TODO Auto-generated method stub
 		
 		if (cur_command_box != null){
 			model.modifyCBPos(cur_command_box, cur_command_box.cur_x + ( new_x - arg0),cur_command_box.cur_y + (new_y - arg1) );
 		}
 		
     System.out.println("mouse dragged");
     System.out.println("model.overrun"+model.over_run_1);
     System.out.println(!overButton(model.run,new_x,new_y));
 	  if ( ! overButton(model.run, new_x, new_y) && model.over_run_1 ){
 			model.run_button_color = model.run_color;
 			System.out.println("set back");
 			model.over_run_1 = false;
 		} else if (! overButton(model.next, new_x, new_y) && model.over_next_1) {
       model.next_button_color = model.run_color;
       model.over_next_1 = false;
     } else if (! overButton(model.back, new_x, new_y) && model.over_back_1) {
       model.back_button_color = model.run_color;
       model.over_back_1 = false;
     }
 			
 	}
 
 	public void mouseMoved(int arg0, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
 		
 		
 		
 	}
 
 
 	public void mousePressed(int arg0, int x, int y) {
 		// TODO Auto-generated method stub
 		
 		// get command box (put into current command box)
 
 		cur_command_box = getCurBox(x, y);
 		if (cur_command_box != null) {
 			model.changeCBColor(Color.green.darker(), cur_command_box);
 		} else {
 			if ( overButton(model.run, x, y) && model.over_run_1 == false )  {
 				model.over_run_1 = true;
 				model.run_button_color = model.run_button_color.darker();
 				System.out.println("set to red");
 			}else if (overButton(model.next, x, y) && model.over_next_1 == false) {
         model.over_next_1 = true;
         model.next_button_color = model.next_button_color.darker();
       } else if (overButton(model.back, x, y) && model.over_back_1 == false) {
         model.over_back_1 = true;
         model.back_button_color =  model.next_button_color.darker();
       }
   	}
 		
 
 		
 	}
 
 	
 	public void mouseReleased(int arg0, int x, int y) {
 		// TODO Auto-generated method stub
 		
 		// check to modify stack
 
 			if (cur_command_box != null) {
 				model.changeCBColor(Color.green, cur_command_box);
 				int section = model.cur_level.getStack().getClosestSection(cur_command_box, 
                                cur_command_box.cur_x, cur_command_box.cur_y);
 				if (section == -1) {
 					model.modifyCBPos(cur_command_box, cur_command_box.x, cur_command_box.y);
 				} else {
 					model.modifyCBPos(cur_command_box, 
              model.level1.getStack().x , model.level1.getStack().y + section*CommandBox.height);
 				}
 			} else {
 				if (overButton(model.run, x, y)) {
 					model.run();
 					model.run_button_color = model.run_color;
 					System.out.println("set back1");
           model.over_run_1 = false;
 				}else if (overButton(model.next,x,y) && model.cur_prog == Model.Progress.SUCCESS) {
 					model.over_next_1 = false;
 					model.cur_prog = Model.Progress.WORK;
					model.cur_prog = Model.Progress.SUCCESS;
           model.cur_level = model.cur_level.getNextLevel();
           model.next_button_color = model.next_color;
 
         }else if (overButton(model.back,x,y)){
         	model.over_back_1 = false;
         	model.cur_prog = Model.Progress.WORK;
         	model.cur_level = model.cur_level.getPrevLevel();
         	model.back_button_color = model.back_color;
 
         }
 		
 			}
 
 		
 	}
 
 	public CommandBox getCurBox(int x, int y){
 		
     ArrayList<CommandBox> boxes = model.cur_level.getBoxes();
     if (boxes == null) {
     	return null;
     }
 		for (int i = 0; i< boxes.size(); i++){
 			CommandBox temp = boxes.get(i);
 			if (x > temp.cur_x && x < temp.cur_x+CommandBox.width &&
 				y > temp.cur_y && y < temp.cur_y + CommandBox.height) {
 					
 				return temp;
 			}
 		}
 		
 		return null;
 	}
 	
 	public boolean overButton(Rectangle rect, int x, int y) {
 		if (x > rect.getX() && x < rect.getX() + rect.getWidth() &&
 			y > rect.getY() && y < rect.getY() + rect.getHeight()) {
 			
 			return true;
 		}
 		return false;
 	}
 
 
 
 	                 
 }
