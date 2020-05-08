 import java.awt.event.ActionListener;
 import java.awt.event.*;
 
 public class Controller implements ActionListener, KeyListener{
 //~ Controller.java:4: error: Controller is not abstract and does not override
  //~ abstract method actionPerformed(ActionEvent) in ActionListener
 	public Controller(Model m) {
 		model = m;
 	}
 	public void actionPerformed(ActionEvent e){
 		String action = e.getActionCommand();
 		switch(action){
 		case("quit"): System.exit(0);
 			break;
 		case("start"): // Start the game!
 			break;
 		}
 		// add other cases here as needed? We might need to "implement" some kind of 
 		// "time" listener too I would assume.
 	}
 	
 	public void keyPressed(KeyEvent e){
 		String keyPressed = e.getKeyText(e.getKeyCode());
 		System.out.println(keyPressed);
		if(keyPressed == "W" || keyPressed == "Up"){
 			// tell game to move up
		}else if(keyPressed == "S" || keyPressed == "Down"){
 			// tell game to move down
		}else if(keyPressed == "A" || keyPressed == "Left"){
 			// Tell the game to shake it to the beat
		}else if(keyPressed == "D" || keyPressed == "Right"){
 			// whatever
 		}
 	}
 	
 	public void keyReleased(KeyEvent e){
 		// Let's just do nothing atm!
 	}
 	
 	public void keyTyped(KeyEvent e){
 		// Whateverrrrr 
 	}
 	public Model getModel(){
 		return model;
 	}
 	//~ system.out.println("Im a big time code writer");
 	private Model model;
 }
