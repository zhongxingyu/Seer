 package gui;
 
 import javax.swing.JFrame;
 
 public class MainFrame extends JFrame {
 
 	//Fields
 		private MainPanel mainPanel;
 		
 	//Constructors
 		public MainFrame(){
 			this.mainPanel = new MainPanel();
			
 			this.setContentPane(this.mainPanel);
 			
 			this.pack();
 			this.setLocationRelativeTo(null);
 			this.setVisible(true);
 		}
 	//Methods
 }
