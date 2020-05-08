 package view.menu.subMenuPanels;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import view.menu.MenuButton;
 
 /**
  * Panel to save and load the game.
  * @author Vidar Eriksson
  *
  */
 @SuppressWarnings("serial")
 public class SaveLoadGame extends SubMenuPanel{
 	
 	/**
 	 * 
 	 * @param button the buttons to be added
 	 * @param bol <code>true<code> if the user is allowed to save.
 	 */
 	public SaveLoadGame(String timeSaved, MenuButton[] button, boolean bol) {
 		super(getText(bol), getPanel(timeSaved), button);
		button[2].setEnabled(bol);
 	}
 	private static String getText(boolean bol) {
 		if (bol){
 			return "Load / Save Game";
 		} else {
 			return "Load Game";
 		}
 	}
 	private static JPanel getPanel(String timeSaved) {
 		JPanel panel = new JPanel();
 		panel.add(new JLabel("Load/SavePanel  A list of saved games should be visible"));
 		return panel;
 	}
 
 }
