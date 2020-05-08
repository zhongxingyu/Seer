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
 
 	public Level3(Model m, GameContainer gc){
 		this.model = m;
 
 		commandbox_1 = new CommandBox(40, 200, "get bread");
 		commandbox_2 = new CommandBox(40, 260, "get peanut butter");
 		commandbox_3 = new CommandBox(180, 200, "get jelly");
 		commandbox_4 = new CommandBox(180, 260, "get knife");
 		commandbox_5 = new CommandBox(40, 320, "get large plate");
 		commandbox_6 = new CommandBox(40, 380, "place two slices of bread on counter");
 		commandbox_7 = new CommandBox(40, 440, "spread peanut butter on bread");
 		commandbox_8 = new CommandBox(180, 320, "spread jelly on bread");
 		commandbox_9 = new CommandBox(180, 380, "put bread slices together ");
 		commandbox_10 = new CommandBox(180, 440, "put sandwich on large plate");
 		commandbox_11 = new CommandBox(40, 500, "repeat 20 times");
 		commandbox_12 = new CommandBox(180, 500, "end repeat");
 
 
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
 
 	}
 
 	public void run(){
 
 		for (int i = 0 ; i< stack.num_boxes; i++) {
 			CommandBox temp = stack.box_stack[i];
 			if(temp == null){
 				//
 			}else if(!temp.str.equals(commandbox_11.str))
 				checkConditions(temp);
 			else{
 				int index = i+1;
 				int end = 0;
 				for(int j=0; j<20; j++){
 					do{
 						temp = stack.box_stack[index];
 						checkConditions(temp);
 						index++;
					} while(!temp.str.equals(commandbox_12.str));
 					if(temp.str.equals(commandbox_12.str)){
 						end = index;
 						index = i+1; //reseting index to beginning of repeat
 					}
 				}
 				i=end;;
 			}
 		}
 		if (sandwich_count == 20) {
 			model.cur_error = "Done!";
 			model.cur_prog = Model.Progress.SUCCESS;
 		}else{
 			model.cur_error = model.level3_error;
 			model.cur_prog = Model.Progress.ERROR;
 		}
 	}
 
 	public void checkConditions(CommandBox temp){
 		if (temp == null ){
 			//do nothing
 		}
 		else if (temp.str.equals(commandbox_1.str)){
 			has_bread = true;
 		}
 		else if (temp.str.equals(commandbox_2.str)) {
 			has_pb = true;
 		}
 		else if (temp.str.equals(commandbox_3.str)) {
 			has_jelly = true;
 		}
 		else if (temp.str.equals(commandbox_4.str)){
 			has_knife = true;
 		} else if (temp.str.equals(commandbox_5.str)){
 			has_plate = true;
 		} else if (temp.str.equals(commandbox_6.str) && has_bread){
 			bread_prepped = true;
 		} else if (temp.str.equals(commandbox_7.str) && bread_prepped && has_knife && has_pb){
 			pb = true; 
 		} else if (temp.str.equals(commandbox_8.str) && bread_prepped && has_knife && has_jelly){
 			jelly = true; 
 		} else if (temp.str.equals(commandbox_9.str) && pb && jelly){
 			sandwich = true;
 			bread_prepped = false;
 		} else if(temp.str.equals(commandbox_10.str) && sandwich){
 			sandwich_count++;
 			sandwich = false;
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
