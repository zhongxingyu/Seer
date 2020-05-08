 package ui.panel;
 
 import game.Difficulty;
 import game.state.GameSetupState;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import ui.Window;
 import core.StateSelector;
 
 /**
  * 
  * @author grant
  * @author trevor
  */
 public class DifficultyPanel extends JPanel
 {
 	private JLabel title;
 	private JButton beginner;
 	private JButton standard;
 	private JButton tournament;
 	
 	public DifficultyPanel()
 	{
 		ButtonListener buttonListener = new ButtonListener();
 		
 		title = new JLabel("Difficulty");
 		add(title);
 		
 		beginner = new JButton("Beginner");
 		beginner.addActionListener(buttonListener);
 		add(beginner);
 		
 		standard = new JButton("Standard");
 		standard.addActionListener(buttonListener);
 		add(standard);
 		
 		tournament = new JButton("Tournamanet");
 		tournament.addActionListener(buttonListener);
 		add(tournament);
 		
 	}
 	
 	private class ButtonListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			JButton button = (JButton)e.getSource();	
 			String name = button.getName();
 	
 			
 			Difficulty difficulty = Difficulty.BEGINNER;
 			
 			switch (name)
 			{
 				case "Beginner": 
 					difficulty = Difficulty.BEGINNER;
 					break;
 				case "Standard":
 					difficulty = Difficulty.STANDARD;
 					break;
 				case "Tournament":
 					difficulty = Difficulty.TOURNAMENT;
 					break;
 			}
 
 			StateSelector stateSelector = StateSelector.getInstance();
 			GameSetupState state = (GameSetupState)stateSelector.getState();
 			state.setDifficulty(difficulty);
 			
 			Window window = Window.getInstance();
			window.setPanel(new SetMapTypePanel());
 		}
 	}
 }
