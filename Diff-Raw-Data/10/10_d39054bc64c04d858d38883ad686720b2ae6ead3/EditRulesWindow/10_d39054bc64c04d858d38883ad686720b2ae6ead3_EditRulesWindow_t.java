 import java.awt.BorderLayout;
 import java.awt.Container;
 
 import javax.swing.*;
 
 /**
  * New window which allows for addition and deletion of rules
  */
 
public class EditRulesWindow extends JFrame {
     
     Container panel;
 
     Player player;
 
     EditRulesPanel editRulesPanel;
 
    JButton exitB;

     public EditRulesWindow(Player player) {
 	super();
 	this.player = player;
 	
 	SwingUtilities.invokeLater(new Runnable() {
 		public void run() {
 		    createFrame();
 		}
 	    });
     }
 
     /**
      * Set up the window
      */
 
     private void createFrame() {
 	setSize(Config.EDIT_RULES_WINDOW_SIZE);
 	setTitle("Edit Rules for " + player.getID() + " Player");
 	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
 	panel = getContentPane();
 
 	editRulesPanel = new EditRulesPanel(player);
 
 	panel.add(Box.createVerticalStrut(10), BorderLayout.PAGE_START);
 	panel.add(Box.createHorizontalStrut(10), BorderLayout.LINE_START);
 
 	panel.add(editRulesPanel, BorderLayout.CENTER);
 
 	panel.add(Box.createHorizontalStrut(10), BorderLayout.LINE_END);

 	panel.add(Box.createVerticalStrut(10), BorderLayout.PAGE_END);
 
 	pack();
     }
 }
