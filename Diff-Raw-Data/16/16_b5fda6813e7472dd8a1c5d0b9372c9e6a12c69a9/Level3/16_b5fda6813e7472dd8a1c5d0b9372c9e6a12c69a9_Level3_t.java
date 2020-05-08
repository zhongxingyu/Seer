 import java.util.ArrayList;
 import org.newdawn.slick.gui.TextField;
 import org.newdawn.slick.*;
 
 import java.awt.Font;
 import org.newdawn.slick.font.*;
 
 public class Level3 extends Level{
 	//CommandBoxes
 	CommandBox commandbox_1;
 	CommandBox commandbox_2;
 	CommandBox commandbox_3;
 	CommandBox commandbox_4;
 	CommandBox commandbox_5;
 	CommandBox commandbox_6;
 	CommandBox commandbox_7;
 	CommandBox commandbox_8;
 	CommandBox commandbox_9;
 	CommandBox commandbox_10;
 	CommandBox commandbox_11;
 	CommandBox commandbox_12;
 
 	String description;
 
 	Model model;
 
 	TextField tf1;
 	TextField tf2;
 	TextField tf3;
 	TextField tf4;
 	TextField tf5;
 	TextField tf6;
 	TextField tf7;
 	ArrayList<TextField> tf_list;
 
 	Font font1;
 	TrueTypeFont font2;
 	ArrayList<CommandBox> boxes;
 	Level prev_level;
 	Level next_level;
 
 
 	Stack stack = new Stack(600, 40, 20);
 	
 	
 
 
 	//conditions
 	boolean has_knife = false;
 	boolean has_bread = false;
 	boolean has_pb = false;
 	boolean has_jelly = false;
 	boolean has_plate = false;
 	boolean bread_prepped = false;
 	boolean pb = false;
 	boolean jelly = false;
 	boolean sandwich = false;
 	int sandwich_count = 0;
 	
 	boolean materials_error = false;;
 	//String error1;
 	//String error2;
 	//String error3;
 	boolean bread_error = false;
 	boolean incomplete_sandwich_error = false;
 	boolean efficiency_error = false;
 	boolean no_end_error = false;
 
 	Image confused_robot;
 	Image no_sandwich;
 	Image sandwich_on_plate;
 	Image pb_loves_j;
 	Image bread_needed;
 	Image pbj_tools;
 	Image not_enough;
 	Image efficiency;
 	Image star;
 
 
 	public Level3(Model m, GameContainer gc) throws SlickException{
 		this.model = m;
 
 		commandbox_1 = new CommandBox(40, 200, "get bread");
 		commandbox_2 = new CommandBox(40, 260, "get peanut butter");
 		commandbox_3 = new CommandBox(210, 200, "get jelly");
 		commandbox_4 = new CommandBox(210, 260, "get knife");
 		commandbox_5 = new CommandBox(40, 320, "get large plate");
		commandbox_6 = new CommandBox(40, 380, "put bread slices on counter");
		commandbox_7 = new CommandBox(40, 440, "put peanut butter on bread");
		commandbox_8 = new CommandBox(210, 320, "put jelly on bread");
 		commandbox_9 = new CommandBox(210, 380, "put bread slices together ");
 		commandbox_10 = new CommandBox(210, 440, "put sandwich on large plate");
		commandbox_11 = new CommandBox(40, 500, "repeat 20 times:");
 		commandbox_12 = new CommandBox(210, 500, "end repeat");
 
 
 		boxes = new ArrayList<CommandBox>();	
 		boxes.add( commandbox_1);
 		boxes.add( commandbox_2);
 		boxes.add( commandbox_3);
 		boxes.add(commandbox_4);
 		boxes.add( commandbox_5);
 		boxes.add(commandbox_6);
 		boxes.add( commandbox_7);
 		boxes.add( commandbox_8);
 		boxes.add(commandbox_9);
 		boxes.add(commandbox_10);
 		boxes.add( commandbox_11);
 		boxes.add( commandbox_12);
 		String eol = System.getProperty("line.separator");
 		//		description = "Congratulations! You've made it to Level 3!\n"+ eol+" You are tasked with giving instructions to a robot to" +eol+
 		//				" make peanut butter sandwiches for your family.  But alas, your children have left the " +
 		//				"kitchen a complete mess!  Clean it up!";
 
 		font1 = new Font("Times New Roman", Font.PLAIN, 15);
 		font2 = new TrueTypeFont(font1, false);
 
 		tf1 = new TextField(gc,font2, 100, 40, 400, 20);
 		tf2 = new TextField(gc,font2, 100, 60, 400, 20);
 		tf3 = new TextField(gc,font2, 100, 80, 400, 20);
 		tf4 = new TextField(gc,font2, 100, 100, 400, 20);
 		tf5 = new TextField(gc, font2, 100, 120, 400, 20);
 		tf6 = new TextField(gc, font2, 100, 140, 400, 20);
 		tf7 = new TextField(gc, font2, 100, 160, 400, 20);
 		tf1.setText("Congratulations! You've made it to level 3!");
 		tf2.setText("You are having a family reunion this weekend and");
 		tf3.setText("you've decided to make everyone's favorite lunch:");
 		tf4.setText("PB&J sandwiches! You need to instruct your trusty robot");
 		tf5.setText("to make 20 sandwiches. You're robot cannot handle too");
 		tf6.setText("many instructions, so make sure you don't");
 		tf7.setText(" run out of space on the stack!");
 		tf_list = new ArrayList<TextField>();
 		tf_list.add(tf1);
 		tf_list.add(tf2);
 		tf_list.add(tf3);
 		tf_list.add(tf4);
 		tf_list.add(tf5);
 		tf_list.add(tf6);
 		tf_list.add(tf7);
 		
 
 		confused_robot = new BigImage("images/confused-robot.jpg",Image.FILTER_NEAREST);
 		confused_robot = confused_robot.getSubImage(0,0,300,450);
 		
 		no_sandwich = new BigImage("images/no-sandwich.jpg",Image.FILTER_NEAREST);
 		no_sandwich = no_sandwich.getSubImage(0,0,400,450);
 		
 		sandwich_on_plate = new BigImage("images/sandwich-on-plate.jpg", Image.FILTER_NEAREST);
 		sandwich_on_plate = sandwich_on_plate.getSubImage(0,0,510,400);
 		
 		pb_loves_j = new BigImage("images/pb-loves-j.png",Image.FILTER_NEAREST);
 		pb_loves_j = pb_loves_j.getSubImage(0, 0, 600, 400);
 		
 		bread_needed = new BigImage("images/bread-needed.jpg",Image.FILTER_NEAREST);
 		bread_needed = bread_needed.getSubImage(0, 0, 600, 600).getScaledCopy(500, 500);
 		
 		pbj_tools = new BigImage("images/pbj-tools.jpg",Image.FILTER_NEAREST);
 		pbj_tools = pbj_tools.getSubImage(0, 0, 650, 500).getScaledCopy(600, 450);
 		
 		not_enough = new BigImage("images/not-enough.jpg",Image.FILTER_NEAREST);
 		not_enough = not_enough.getSubImage(0,0,500,400);
 		
 		efficiency = new BigImage("images/efficiency.jpg",Image.FILTER_NEAREST);
 		efficiency = efficiency.getSubImage(0,0,650,400);
 
 		star = new BigImage("images/star.jpg", Image.FILTER_NEAREST, 1024);
 		star = star.getSubImage(0, 0, 499, 508);
 
 
 	}
 
 	public void run(){
 		sandwich_count = 0;
 		materials_error = false;
 		bread_error = false;
 		incomplete_sandwich_error = false;
 		efficiency_error = false;
 		no_end_error = false;
 		has_knife = false;
 		has_bread = false;
 		has_pb = false;
 		has_jelly = false;
 		has_plate = false;
 		bread_prepped = false;
 		pb = false;
 		jelly = false;
 		sandwich = false;
 		for (int i = 0 ; i< stack.num_boxes; i++) {
 			if(materials_error || bread_error || incomplete_sandwich_error || no_end_error || efficiency_error)
 				break;
 			CommandBox temp = stack.box_stack[i];
 			if(temp == null){
 				continue;
 			}else if(!temp.str.equals(commandbox_11.str))
 				checkConditions(temp);
 			else{ //temp = repeat20 block
 				int index = i; //index of repeat20 block
 				int end = 0;
 				//boolean test = true;
 				for(int j=0; j<20; j++){ //repeat code in repeat20 block
 					if(materials_error || bread_error || incomplete_sandwich_error || no_end_error || efficiency_error)
 						break;
 					do{//code in repeat20 block
 						if(materials_error || bread_error || incomplete_sandwich_error || no_end_error || efficiency_error)
 							break;
 						index++;
 						if(index >= stack.num_boxes){
 							no_end_error = true;
 							break;
 						}
 						temp = stack.box_stack[index];
 						if (temp == null) {
 							continue;
 						}
 						checkConditions(temp);
 					} while((temp == null || !temp.str.equals(commandbox_12.str)));
 					if(index >= stack.box_stack.length){
 						no_end_error = true;
 						break;
 					}else if(temp != null && temp.str.equals(commandbox_12.str)){
 						end = index;
 						index = i; //reseting index to beginning of repeat
 					}
 				}
 				i=end;;
 			}
 		}
 		if(!(has_bread && has_knife && has_plate && has_pb && has_jelly))
 			materials_error = true;
 		if(materials_error){
 			model.cur_error = "Looks like you don't have everything you need.";
 			model.show_image = true;
 			model.cur_image = pbj_tools;
 			model.cur_prog = Model.Progress.ERROR;
 		}else if(efficiency_error){
 			model.cur_error = "You don't need to get the same materials again!";
 			model.show_image = true;
 			model.cur_image = efficiency;
 			model.cur_prog = Model.Progress.ERROR;
 		}else if(bread_error){
 			model.cur_error = "Need bread out to spread the pb&j.";
 			model.show_image = true;
 			model.cur_image = bread_needed;
 			model.cur_prog = Model.Progress.ERROR;
 		}else if(incomplete_sandwich_error){
 			model.cur_error = "Your sandwiches are incomplete.";
 			model.show_image = true;
 			model.cur_image = pb_loves_j;
 			model.cur_prog = Model.Progress.ERROR;
 		}else if(no_end_error){
 			model.cur_error = "Your robot doesn't know how to stop making sandwiches!";
 			model.show_image = true;
 			model.cur_image = confused_robot;
 			model.cur_prog = Model.Progress.ERROR;
 		}else if(sandwich){
 			model.cur_error = "Your sandwiches should be on a plate.";
 			model.show_image = true;
 			model.cur_image = sandwich_on_plate;
 			model.cur_prog = Model.Progress.ERROR;
 		}else if(sandwich_count == 0){
 			model.cur_error = "You haven't made any sandwiches.";
 			model.show_image = true;
 			model.cur_image = no_sandwich;
 			model.cur_prog = Model.Progress.ERROR;
 		}else if (sandwich_count == 20) {
 			model.cur_error = "Done!";
 			model.cur_prog = Model.Progress.SUCCESS;
 			model.cur_image = star;
 			model.show_image = true;
 		}else if(sandwich_count > 0){
 			model.cur_error = "You don't have the right number of sandwiches on the plate.";
 			model.show_image = true;
 			model.cur_image = not_enough;
 			model.cur_prog = Model.Progress.ERROR;
 		}else{
 			model.cur_error = "Looks like something went wrong.";
 			model.cur_prog = Model.Progress.ERROR;
 			
 		}
 	}
 
 	public void checkConditions(CommandBox temp){
 		if (temp == null ){
 			//do nothing
 		}
 		else if (temp.str.equals(commandbox_1.str)){
 			if(has_bread)
 				efficiency_error = true;
 			else
 				has_bread = true;
 		}
 		else if (temp.str.equals(commandbox_2.str)) {
 			if(has_pb)
 				efficiency_error = true;
 			else
 				has_pb = true;
 		}
 		else if (temp.str.equals(commandbox_3.str)) {
 			if(has_jelly)
 				efficiency_error = true;
 			else
 				has_jelly = true;
 		}
 		else if (temp.str.equals(commandbox_4.str)){
 			if(has_knife)
 				efficiency_error = true;
 			else
 				has_knife = true;
 		} else if (temp.str.equals(commandbox_5.str)){
 			if(has_plate)
 				efficiency_error = true;
 			else
 				has_plate = true;
 		} else if (temp.str.equals(commandbox_6.str)){
 			if (has_bread)
 				bread_prepped = true;
 			else
 				materials_error = true;
 		} else if (temp.str.equals(commandbox_7.str)){
 			if(!(has_knife && has_jelly))
 				materials_error = true;
 			else{
 				if(!bread_prepped)
 					bread_error = true;
 				else
 					pb = true;
 			}	 
 		} else if (temp.str.equals(commandbox_8.str)){
 			if(!(has_knife && has_jelly))
 				materials_error = true;
 			else{
 				if(!bread_prepped)
 					bread_error = true;
 				else
 					jelly = true;
 			}
 		} else if (temp.str.equals(commandbox_9.str)){
 			if(pb && jelly){
 				sandwich = true;
 				bread_prepped = false;
 			}else
 				incomplete_sandwich_error = true;
 		} else if(temp.str.equals(commandbox_10.str)){
 			if (sandwich){
 				sandwich_count++;
 				sandwich = false;
 			}else{
 				incomplete_sandwich_error = true;
 			}
 		}
 	}
 
 	@Override
 	public void render() {
 		// TODO Auto-generated method stub
 
 
 
 	}
 
 	@Override
 	ArrayList<CommandBox> getBoxes() {
 		// TODO Auto-generated method stub
 		return boxes;
 	}
 
 	@Override
 	Stack getStack() {
 		// TODO Auto-generated method stub
 		return stack;
 	}
 
 	@Override
 	ArrayList<TextField> getTF() {
 		// TODO Auto-generated method stub
 		return tf_list;
 	}
 
 	@Override
 	void setPrevLevel(Level level) {
 		// TODO Auto-generated method stub
 		prev_level = level;
 
 	}
 
 	@Override
 	Level getPrevLevel() {
 		// TODO Auto-generated method stub
 		return prev_level;
 	}
 
 	@Override
 	void setNextLevel(Level level) {
 		// TODO Auto-generated method stub
 		next_level = level;
 
 	}
 
 	@Override
 	Level getNextLevel() {
 		// TODO Auto-generated method stub
 		return next_level;
 	}
 
 }
