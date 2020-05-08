 import java.util.ArrayList;
 import org.newdawn.slick.gui.TextField;
 import org.newdawn.slick.*;
 
 import java.awt.Font;
 import org.newdawn.slick.font.*;
 
 
 public class SandboxLevel extends Level {
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
     CommandBox commandbox_13;
     CommandBox commandbox_14;
     CommandBox commandbox_15;
     CommandBox commandbox_16;
 		
 		
 		Model model;
 		
 		TextField tf1;
 		TextField tf2;
 		TextField tf3;
 		TextField tf4;
 		TextField tf5;
 		ArrayList<TextField> tf_list;
 		
 		Font font1;
 		TrueTypeFont font2;
     ArrayList<CommandBox> boxes;
 	
 
 
     Stack stack = new Stack(600, 40, 18);
     
     Image botfire;
     Image man_down;
 
     
 		public SandboxLevel(Model m, GameContainer gc) throws SlickException{
 			this.model = m;
 			
 			commandbox_1 = new CommandBox(40, 200, "if fridge full jump to top");
 			commandbox_2 = new CommandBox(40, 260, "take item in fridge");
 			commandbox_3 = new CommandBox(210, 200, "dump item on floor");
 			commandbox_4 = new CommandBox(210, 260, "loop while standing on floor");
 			commandbox_5 = new CommandBox(40, 320, "end repeated code");
 			commandbox_6 = new CommandBox(40, 380, "take off shoes");
 			commandbox_7 = new CommandBox(40, 440, "jump up and down ");
 			commandbox_8 = new CommandBox(210, 320, "if fell down, roll around");
 			commandbox_9 = new CommandBox(210, 380, "if fell down, get up");
 			commandbox_10 = new CommandBox(210, 440, "dump soap on floor");
 			commandbox_11 = new CommandBox(40, 500, "get soap");
       commandbox_12 = new CommandBox(40,140, "get pitcher of water");
       commandbox_13 = new CommandBox(210, 140, "walk to microwave");
       commandbox_14 = new CommandBox(40,80, "upend item");
       commandbox_15 = new CommandBox(210, 80, "walk to living room");
       commandbox_15 = new CommandBox(210, 500, "invite friends over");
 			
       boxes = new ArrayList<CommandBox>();	
       System.out.println("added box");
 			boxes.add( commandbox_1);
 			boxes.add( commandbox_2);
 			boxes.add( commandbox_3);
 			boxes.add(commandbox_4);
 			boxes.add( commandbox_5);
 			boxes.add(commandbox_6);
 			boxes.add( commandbox_7);
 			boxes.add( commandbox_8);
 			boxes.add( commandbox_9);
 			boxes.add( commandbox_10);
 			boxes.add( commandbox_11);
 			boxes.add( commandbox_11);
       boxes.add(commandbox_12);
       boxes.add(commandbox_13);
       boxes.add(commandbox_14);
       boxes.add(commandbox_15);
 
 			String eol = System.getProperty("line.separator");
 			
 			font1 = new Font("Times New Roman", Font.PLAIN, 15);
 			font2 = new TrueTypeFont(font1, false);
 			tf_list = new ArrayList<TextField>();
 			
 			botfire = new BigImage("images/botfire.jpg", Image.FILTER_NEAREST, 512);
 			botfire = botfire.getSubImage(0,0,600,300);
 			
			man_down = new BigImage("images/man_down.png", Image.FILTER_NEAREST, 512);
 			man_down = man_down.getSubImage(0,0,279,298);
 			
 	
 			
 		}
 		
 		public void run(){
 			/*
 			commandbox_1 = new CommandBox(40, 200, "if fridge full jump to top");
 			commandbox_2 = new CommandBox(40, 260, "take item in fridge");
 			commandbox_3 = new CommandBox(210, 200, "dump item on floor");
 			commandbox_4 = new CommandBox(210, 260, "loop while standing on floor");
 			commandbox_5 = new CommandBox(40, 320, "end repeated code");
 			commandbox_6 = new CommandBox(40, 380, "take off shoes");
 			commandbox_7 = new CommandBox(40, 440, "jump up and down ");
 			commandbox_8 = new CommandBox(210, 320, "if fell down, roll around");
 			commandbox_9 = new CommandBox(210, 380, "if fell down, get up");
 			commandbox_10 = new CommandBox(210, 440, "dump soap on floor");
 			commandbox_11 = new CommandBox(40, 500, "get soap");
       commandbox_12 = new CommandBox(40,140, "get pitcher of water");
       commandbox_13 = new CommandBox(210, 140, "walk to microwave");
       commandbox_14 = new CommandBox(40,80, "upend item(s)");
       commandbox_15 = new CommandBox(210, 80, "walk to living room");
       commandbox_15 = new CommandBox(210, 500, "invite friends over");
       */
 			
 			boolean has_soap = false;
 			boolean has_water = false;
 			boolean has_fridge_item = false;
 			boolean looping = false;
 			//boolean no_fridge_item = true;
 			boolean check_for_upend =false;
 			boolean fell= false;
 			
 			model.cur_prog = Model.Progress.WORK;
 			
 			for (int i = 0 ; i< stack.num_boxes; i++) {
 				CommandBox temp = stack.box_stack[i];
 				if (temp == null) {
 					continue;
 				}
 				if (temp.str.equals(commandbox_4.str)){
 					looping = true;
 				}
 				if (temp.str.equals(commandbox_11.str) && !looping){
 					has_soap = true;
 				}else if(temp.str.equals(commandbox_11.str) && looping){
 					model.cur_error = "Can't get soap more than once!";
 					model.cur_prog = Model.Progress.ERROR;
 					model.cur_image = botfire;
 					model.show_image = true;
 					
 					return;
 				}
 				if (temp.str.equals(commandbox_12.str)&& !looping){
 					has_water = true;
 				}else if (temp.str.equals(commandbox_12.str) && looping){
 					model.cur_error = "You're flooding the kitchen! NOOOO!!!";
 					model.cur_prog = Model.Progress.ERROR;
 					return;
 				}
 				if (temp.str.equals(commandbox_2.str)) {
 					has_fridge_item = true;
 				}else if (temp.str.equals(commandbox_2.str) && looping && has_fridge_item) {
 					check_for_upend = true;
 				}
 				if (temp.str.equals(commandbox_14.str)|| temp.str.equals(commandbox_3.str) && has_fridge_item && looping ){
 					check_for_upend = false;
 				}
 				
 				if (temp.str.equals(commandbox_5.str) && check_for_upend) {
 					model.cur_error = "You need to remove your item before getting more";
 					model.cur_prog=Model.Progress.ERROR;
 					model.cur_image = botfire;
 					model.show_image = true;
 					return;
 				}else if (temp.str.equals(commandbox_5.str)){
 					looping = false;
 				}
 				if (temp.str.equals(commandbox_10.str) && has_soap){
 					fell = true;
 					model.cur_error = "You got the floor all soapy, so you slipped a fell!  Now you can't get up! NOOOOOO";
 					model.cur_prog= Model.Progress.ERROR;
 					model.cur_image = man_down;
 					model.show_image = true;
 					
 				}else if (temp.str.equals(commandbox_10.str ) && !has_soap){
 					model.cur_error = "You can't dump soap without the soap! You've caused the robot to start on fire!";
 					model.cur_prog= Model.Progress.ERROR;
 					model.cur_image = botfire;
 					model.show_image = true;
 					return;
 				}
 				if (temp.str.equals(commandbox_8.str) && fell){
 					model.cur_error = "You fell down and are rolling around in the muck! That's disgusting.";
 					model.cur_prog = Model.Progress.ERROR;
 					model.cur_image = man_down;
 					model.show_image = true;
 					return;
 				}
 				if (temp.str.equals(commandbox_9.str) && fell) {
 					model.cur_error = "You fell down and tried to get up again, but you fell down again because you're clumsy..";
 					model.cur_prog=Model.Progress.ERROR;
 					model.cur_image = man_down;
 					model.show_image = true;
 					return;
 				}
 				if (temp.str.equals(commandbox_7.str) ){
 					fell = true;
 					model.cur_error = "you have fallen down!";
 					model.cur_prog = Model.Progress.ERROR;
 					model.cur_image = man_down;
 					model.show_image = true;
 				}
 				if (temp.str.equals(commandbox_15.str)) {
 					model.cur_error = "You invited your friends over for kitchen disaster!  They all hate you now.";
 					model.cur_prog = Model.Progress.ERROR;
 					model.cur_image = botfire;
 					model.show_image = true;
 					return;
 				}
 				
 			}
			if (fell) {
				model.cur_error = "you fell down!";
			}else {
				model.cur_error = "Do something crazy!";
			}
 			model.cur_prog = Model.Progress.ERROR;
 
 			
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
