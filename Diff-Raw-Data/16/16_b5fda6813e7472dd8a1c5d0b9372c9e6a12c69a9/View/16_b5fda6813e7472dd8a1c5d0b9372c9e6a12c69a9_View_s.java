 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.ShapeRenderer;
 import org.newdawn.slick.gui.TextField;
 
 
 import java.awt.Font;
 import java.util.ArrayList;
 
 
 public class View {
 
 	Model model;
 	Font times;
 	TrueTypeFont times2;
 	TrueTypeFont times_small;
 	Font times_small_1;
 
 	public View(Model model) {
 
 		this.model = model;
 		times = new Font("Times New Roman",Font.BOLD, 15);
 		times_small_1 =  new Font("Times New Roman", Font.PLAIN, 14);
 		times2 = new TrueTypeFont(times, false);
 		times_small = new TrueTypeFont(times_small_1, false);
 
 	}
 	// albhalbab
 
 	// get values from model and draw
 	public void render(GameContainer gc, Graphics g) {
 		g.setFont( times2);
 
 		//start screen
 		if (model.cur_level == model.start_screen){
 			model.start_screen.render(g);
 		}
 		//draw command boxes
 
 		else {
 			g.setBackground(Color.black);
 			// draw boxes
 			ArrayList<CommandBox> boxes = model.cur_level.getBoxes();
 			for (int x = 0; x < boxes.size(); x++) {
 
 				CommandBox temp_box = boxes.get(x);
 				g.setColor(temp_box.rect_color);
 				temp_box.render();
 				g.setColor(temp_box.text_color);
 				g.setFont(times_small);
 				g.drawString(temp_box.str, temp_box.cur_x, temp_box.cur_y+3);
 			}
 			// draw run button
 			g.setColor(model.run_button_color);
 			ShapeRenderer.fill(model.run);
 			g.setColor(Color.black);
 			g.drawString("RUN", model.run.getCenterX()-15, model.run.getCenterY() - 5);
 			// draw next
 			if (model.cur_prog == Model.Progress.SUCCESS && model.cur_level != model.sandbox_level) {
 				g.setColor(model.next_button_color);
 				ShapeRenderer.fill(model.next);
 				g.setColor(Color.black);
 				g.drawString("Next", model.next.getCenterX() - 15, model.next.getCenterY() - 5);
 
 			}
 			//draw back
 			g.setColor(model.back_button_color);
 			ShapeRenderer.fill(model.back);
 			g.setColor(Color.black);
 			g.drawString("Back", model.back.getCenterX() - 15, model.back.getCenterY() - 5);
 			// draw stack 
 			g.setColor(Color.white);
 			model.cur_level.getStack().render(g);
 			// draw error message	
 			if (model.cur_prog == Model.Progress.ERROR){
 				g.setColor(Color.red);
 				g.drawString(model.cur_error, 140, model.cur_level.getTF().size()*20+40);
 				if (model.show_image){
 					model.cur_image.draw(300,300);
 				}
 			} else if (model.cur_prog == Model.Progress.SUCCESS) {
 				g.setColor(Color.green);
				g.drawString(model.cur_error, 100, 140);
 				if (model.show_image){
 					model.cur_image.draw(300,300);
 				}
 			} 
 
 			// draw paragraph
 			ArrayList<TextField> tf_list = model.cur_level.getTF();
 			for (int x = 0; x<tf_list.size(); x++){
 				TextField tf = tf_list.get(x);
 				tf.setBorderColor(Color.black);
 				tf.setTextColor(Color.white);
 				g.setColor(Color.white);
 				tf.render(gc, g);
 			}
 
 
 		}
 
 
 	}
 
 }
