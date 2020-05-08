 package edu.wheaton.simulator.gui;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JTextField;
 
 import edu.wheaton.simulator.gui.screen.Screen;
 	
 
 /**
  * There is a static method in the facade that will create a Rock Paper Scissors simulation.
  * 
  * @author David Emmanuel Pederson
  *
  */
 public class RockPaperScissorsFinishListener implements ActionListener {
 		
 		private JTextField name;
 		private JTextField width;
 		private JTextField height;
 		private SimulatorFacade gm;
 		
 		public RockPaperScissorsFinishListener(JTextField name, JTextField width, JTextField height, 
 										  SimulatorFacade gm){
 			this.name = name;
 			this.width = width;
 			this.height = height;
 			this.gm = gm;
 		}
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			try {
 				int heightInt = getHeight();
 				int widthInt = getWidth();
 				gm.load(name.getText(),widthInt, heightInt);
 				gm.updateGuiManager(getName(), widthInt, heightInt);
 			} catch(java.lang.NumberFormatException nfe) { 
 				System.err.println("Invalid input passed to RockPaperScissorsListener");
 			}
 			Screen upload = Gui.getScreenManager().getScreen("View Simulation");
 			gm.initRockPaperScissors();
 			gm.setStepLimit(1000);
 			gm.setStarted(false);
 			Gui.getScreenManager().load(upload);
 		}
 		public String getName(){
 			return name.getText();
 		}
 		public int getHeight(){
 			return Integer.parseInt(height.getText());
 		}
 		public int getWidth(){
 			return Integer.parseInt(width.getText());
 		}
 }
